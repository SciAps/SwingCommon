package com.sciaps.common.swing.libzunitapi;

import com.google.inject.Inject;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.objtracker.DBObj.ObjLoader;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.global.MutableObjectsManager;
import com.sciaps.common.swing.utils.StandardFinderUtils;
import com.sciaps.common.webserver.LIBZHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author sgowen
 */
public final class HttpLibzUnitApiHandler implements LibzUnitApiHandler
{

    static Logger logger = LoggerFactory.getLogger(HttpLibzUnitApiHandler.class);


    private static String getLibzUnitApiBaseUrl(String ipAddress)
    {
        final String urlBaseString = "http://" + ipAddress;

        return urlBaseString;
    }


    @Inject
    LibzUnitManager mUnitManager;

    @Inject
    DBObjTracker mObjTracker;

    private String mIPAddress;

    public void setIpAddress(String ipAddress) {
        mIPAddress = ipAddress;
    }

    @Override
    public boolean connectToLibzUnit() {
        String baseUrl = getLibzUnitApiBaseUrl(mIPAddress);
        //LIBZHttpClient = new LIBZHttpClient(baseUrl);

        return true;
    }

    @Override
    public boolean pullFromLibzUnit()
    {
        try {
            String baseUrl = getLibzUnitApiBaseUrl(mIPAddress);

            LIBZHttpClient httpClient = new LIBZHttpClient(baseUrl);

            final Map<String, Standard> standards = getStandards();
            for (Map.Entry<String, Standard> entry : standards.entrySet()) {
                entry.getValue().mId = entry.getKey();
                mObjTracker.trackObject(entry.getValue());
            }


            Map<String, CalibrationShot> calibrationShots = getCalibrationShots();
            for (Map.Entry<String, CalibrationShot> entry : calibrationShots.entrySet()) {
                entry.getValue().mId = entry.getKey();
                mObjTracker.trackObject(entry.getValue());

                entry.getValue().loadFields(new ObjLoader() {
                    @Override
                    public Object load(String id, Class<?> type) {
                        Object retval = standards.get(id);
                        if (retval == null) {
                            logger.warn("unable to load object type: {} with id: {}", type, id);
                        }
                        return retval;
                    }
                });
            }


            final Map<String, Region> regions = getRegions();
            for (Map.Entry<String, Region> entry : regions.entrySet()) {
                entry.getValue().mId = entry.getKey();
                mObjTracker.trackObject(entry.getValue());
            }

            Map<String, IRRatio> intensityRatios = getIntensityRatios();
            for (Map.Entry<String, IRRatio> entry : intensityRatios.entrySet()) {
                entry.getValue().mId = entry.getKey();
                mObjTracker.trackObject(entry.getValue());
                entry.getValue().loadFields(new ObjLoader() {
                    @Override
                    public Object load(String id, Class<?> type) {
                        Object retval = regions.get(id);
                        if (retval == null) {
                            logger.warn("unable to load object type: {} with id: {}", type, id);
                        }
                        return retval;
                    }
                });
            }

            Map<String, Model> calModels = getCalibrationModels();
            for (Map.Entry<String, Model> entry : calModels.entrySet()) {
                entry.getValue().mId = entry.getKey();
                mObjTracker.trackObject(entry.getValue());
                entry.getValue().loadFields(new ObjLoader() {
                    @Override
                    public Object load(String id, Class<?> type) {
                        Object retval = standards.get(id);
                        if (retval == null) {
                            logger.warn("unable to load object type: {} with id: {}", type, id);
                        }
                        return retval;
                    }
                });

                for (Map.Entry<AtomicElement, IRCurve> irCurveEntry : entry.getValue().irs.entrySet()) {
                    irCurveEntry.getValue().loadFields(new ObjLoader() {
                        @Override
                        public Object load(String id, Class<?> type) {
                            Object retval = null;
                            if (Region.class == type) {
                                retval = regions.get(id);
                            } else if (Standard.class == type) {
                                retval = standards.get(id);
                            }

                            if (retval == null) {
                                logger.warn("unable to load object type: {} with id: {}", type, id);
                            }
                            return retval;
                        }
                    });
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }


    private static <T extends DBObj> void createAll(Class<T> type,
                                                    LIBZHttpClient.BasicObjectClient<T> client,
                                                    DBObjTracker tracker) throws IOException {
        Iterator<T> it = tracker.getNewObjectsOfType(type);
        while(it.hasNext()) {
            T obj = it.next();
            obj.mId = client.createObject(obj);
            tracker.removeCreated(obj);
        }

    }

    private static <T extends DBObj> void updateAll(Class<T> type,
                                                    LIBZHttpClient.BasicObjectClient<T> client,
                                                    DBObjTracker tracker) throws IOException {
        Iterator<T> it = tracker.getModifiedObjectsOfType(type);
        while(it.hasNext()) {
            T obj = it.next();
            client.updateObject(obj.mId, obj);
            tracker.removeModified(obj);
        }
    }

    private static <T extends DBObj> void deleteAll(Class<T> type,
                                                    LIBZHttpClient.BasicObjectClient<T> client,
                                                    DBObjTracker tracker) throws IOException {
        Iterator<T> it = tracker.getDeletedObjectsOfType(type);
        while(it.hasNext()) {
            T obj = it.next();
            client.deleteObject(obj.mId);
            tracker.removeDelete(obj);
        }
    }

    @Override
    public boolean pushToLibzUnit()
    {
        String baseUrl = getLibzUnitApiBaseUrl(mIPAddress);

        LIBZHttpClient httpClient = new LIBZHttpClient(baseUrl);

        try {

            createAll(Standard.class, httpClient.mStandardsObjClient, mObjTracker);
            createAll(Region.class, httpClient.mRegionObjClient, mObjTracker);
            createAll(IRRatio.class, httpClient.mIRObjClient, mObjTracker);
            createAll(Model.class, httpClient.mModelObjClient, mObjTracker);

            updateAll(Standard.class, httpClient.mStandardsObjClient, mObjTracker);
            updateAll(Region.class, httpClient.mRegionObjClient, mObjTracker);
            updateAll(IRRatio.class, httpClient.mIRObjClient, mObjTracker);
            updateAll(Model.class, httpClient.mModelObjClient, mObjTracker);

            deleteAll(Standard.class, httpClient.mStandardsObjClient, mObjTracker);
            deleteAll(Region.class, httpClient.mRegionObjClient, mObjTracker);
            deleteAll(IRRatio.class, httpClient.mIRObjClient, mObjTracker);
            deleteAll(Model.class, httpClient.mModelObjClient, mObjTracker);

            return true;

        }catch (IOException e) {
            logger.error("", e);
            return false;
        }


    }

    @Override
    public LIBZPixelSpectrum getLIBZPixelSpectrum(final String shotId)
    {
        if (mUnitManager.getLIBZPixelSpectra().containsKey(shotId))
        {
            return mUnitManager.getLIBZPixelSpectra().get(shotId);
        }
        else
        {
            try
            {
                LIBZPixelSpectrum libzPixelSpectrum = _libzHttpClient.getCalibrationShot(shotId);
                mUnitManager.getLIBZPixelSpectra().put(shotId, libzPixelSpectrum);

                return libzPixelSpectrum;
            }
            catch (IOException ex)
            {
                logger.error("", ex);
            }

            return null;
        }
    }

    private Map<String, Standard> getStandards()
    {
        return getObjects(_libzHttpClient.mStandardsObjClient);
    }

    private Map<String, CalibrationShot> getCalibrationShots()
    {
        try
        {
            Map<String, CalibrationShot> calibrationShots = _libzHttpClient.getCalibrationShots();

            return calibrationShots;
        }
        catch (IOException ex)
        {
            logger.error("", ex);
        }

        return null;
    }

    private Map<String, Region> getRegions()
    {
        return getObjects(_libzHttpClient.mRegionObjClient);
    }

    private Map<String, IRRatio> getIntensityRatios()
    {
        return getObjects(_libzHttpClient.mIRObjClient);
    }

    private Map<String, Model> getCalibrationModels()
    {
        return getObjects(_libzHttpClient.mModelObjClient);
    }

    private boolean pushStandards()
    {
        return push(_libzHttpClient.mStandardsObjClient, mUnitManager.getStandardsManager());
    }

    private boolean pushRegions()
    {
        return push(_libzHttpClient.mRegionObjClient, mUnitManager.getRegionsManager());
    }

    private boolean pushIntensityRatios()
    {
        return push(_libzHttpClient.mIRObjClient, mUnitManager.getIRRatiosManager());
    }

    private boolean pushCalibrationModels()
    {
        return push(_libzHttpClient.mModelObjClient, mUnitManager.getModelsManager());
    }

    private <T extends DBObj> Map<String, T> getObjects(LIBZHttpClient.BasicObjectClient<T> basicObjectClient)
    {
        Map<String, T> objects = new HashMap();

        try
        {
            List<String> objectIds = basicObjectClient.getIdList();
            if (objectIds != null && objectIds.size() > 0)
            {
                for (String objectId : objectIds)
                {
                    T object = basicObjectClient.getSingleObject(objectId);
                    object.mId = objectId;
                    objects.put(objectId, object);
                }
            }
        }
        catch (IOException ex)
        {
            logger.error("", ex);
        }

        return objects;
    }

    private <T extends DBObj> boolean push(LIBZHttpClient.BasicObjectClient<T> basicObjectClient, MutableObjectsManager<T> mutableObjectsManager)
    {
        try
        {
            createObjects(basicObjectClient, mutableObjectsManager.getObjectsToCreate(), mutableObjectsManager.getObjects());
            updateObjects(basicObjectClient, mutableObjectsManager.getObjectsToUpdate(), mutableObjectsManager.getObjects());
            deleteObjects(basicObjectClient, mutableObjectsManager.getObjectsToDelete(), mutableObjectsManager.getObjects());

            return true;
        }
        catch (IOException ex)
        {
            logger.error("", ex);
            return false;
        }
    }

    private <T extends DBObj> void createObjects(LIBZHttpClient.BasicObjectClient<T> basicObjectClient, Set<String> objectsToCreate, Map<String, T> workingLocalObjects) throws IOException
    {
        if (objectsToCreate.size() > 0)
        {
            Set<String> objectsCreated = new HashSet();

            try
            {
                for (String objectId : objectsToCreate)
                {
                    T object = workingLocalObjects.get(objectId);
                    String databaseObjectId = basicObjectClient.createObject(object);
                    object.mId = databaseObjectId;

                    objectsCreated.add(objectId);

                    workingLocalObjects.remove(objectId);
                    workingLocalObjects.put(databaseObjectId, object);
                }
            }
            finally
            {
                for (String objectId : objectsCreated)
                {
                    objectsToCreate.remove(objectId);
                }
            }
        }
    }

    private <T> void updateObjects(LIBZHttpClient.BasicObjectClient<T> basicObjectClient, Set<String> objectsToUpdate, Map<String, T> workingLocalObjects) throws IOException
    {
        if (objectsToUpdate.size() > 0)
        {
            Set<String> objectsUpdated = new HashSet();

            try
            {
                for (String objectId : objectsToUpdate)
                {
                    T object = workingLocalObjects.get(objectId);
                    basicObjectClient.updateObject(objectId, object);

                    objectsUpdated.add(objectId);
                }
            }
            finally
            {
                for (String objectId : objectsUpdated)
                {
                    objectsToUpdate.remove(objectId);
                }
            }
        }
    }

    private <T> void deleteObjects(LIBZHttpClient.BasicObjectClient<T> basicObjectClient, Set<String> objectsToDelete, Map<String, T> workingLocalObjects) throws IOException
    {
        if (objectsToDelete.size() > 0)
        {
            Set<String> objectsDeleted = new HashSet();

            try
            {
                for (String objectId : objectsToDelete)
                {
                    basicObjectClient.deleteObject(objectId);

                    objectsDeleted.add(objectId);
                }
            }
            finally
            {
                for (String objectId : objectsDeleted)
                {
                    objectsToDelete.remove(objectId);
                }
            }
        }
    }
}