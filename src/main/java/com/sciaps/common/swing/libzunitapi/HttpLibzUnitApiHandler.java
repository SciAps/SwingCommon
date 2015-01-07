package com.sciaps.common.swing.libzunitapi;

import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObj.ObjLoader;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.global.MutableObjectsManager;
import com.sciaps.common.webserver.LIBZHttpClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sgowen
 */
public final class HttpLibzUnitApiHandler implements LibzUnitApiHandler
{
    private static String getLibzUnitApiBaseUrl(String ipAddress)
    {
        final String urlBaseString = "http://" + ipAddress;

        return urlBaseString;
    }

    private LIBZHttpClient _libzHttpClient;

    @Override
    public boolean connectToLibzUnit()
    {
        String baseUrl = getLibzUnitApiBaseUrl(LibzUnitManager.getInstance().getIpAddress());

        _libzHttpClient = new LIBZHttpClient(baseUrl);

        LibzUnitManager.getInstance().setLibzUnitUniqueIdentifier("UNIQUE_LIBZ_UNIT_ID_HERE");

        return true;
    }

    @Override
    public boolean pullFromLibzUnit()
    {
        String baseUrl = getLibzUnitApiBaseUrl(LibzUnitManager.getInstance().getIpAddress());

        _libzHttpClient = new LIBZHttpClient(baseUrl);
        
        System.out.println("Getting standards");
        final Map<String, Standard> standards = getStandards();
        LibzUnitManager.getInstance().getStandardsManager().reset();
        LibzUnitManager.getInstance().getStandardsManager().getObjects().putAll(standards);

        System.out.println("Getting calibration shots");
        Map<String, CalibrationShot> calibrationShots = getCalibrationShots();
        for (Map.Entry<String, CalibrationShot> entry : calibrationShots.entrySet())
        {
            entry.getValue().loadFields(new ObjLoader()
            {
                @Override
                public Object load(String id, Class<?> type)
                {
                    return standards.get(id);
                }
            });
        }
        LibzUnitManager.getInstance().setCalibrationShots(calibrationShots);

        long startTime = System.currentTimeMillis();
        System.out.println("Getting LIBZPixelSpectrum data");
        Map<String, LIBZPixelSpectrum> libzPixelSpectra = new HashMap();
        for (Map.Entry<String, CalibrationShot> entry : calibrationShots.entrySet())
        {
            LIBZPixelSpectrum libzPixelSpectum = getLIBZPixelSpectrum(entry.getKey());
            if (libzPixelSpectum == null)
            {
                Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.WARNING, "LIBZPixelSpectrum retrieved via id: {0} was NULL! Continuing to download the other LIBZPixelSpectrum objects...", entry.getKey());
            }
            else
            {
                libzPixelSpectra.put(entry.getKey(), libzPixelSpectum);
            }
        }

        LibzUnitManager.getInstance().setLIBZPixelSpectra(libzPixelSpectra);
        long endTime = System.currentTimeMillis();
        long delta = endTime - startTime;
        long deltaSeconds = delta / 1000;

        System.out.println("It took us " + delta + " milliseconds (" + deltaSeconds + " seconds) to download all of the shot data!");

        System.out.println("Getting regions");
        final Map<String, Region> regions = getRegions();
        LibzUnitManager.getInstance().getRegionsManager().reset();
        LibzUnitManager.getInstance().getRegionsManager().getObjects().putAll(regions);

        System.out.println("Getting intensity ratios");
        Map<String, IRRatio> intensityRatios = getIntensityRatios();
        for (Map.Entry<String, IRRatio> entry : intensityRatios.entrySet())
        {
            entry.getValue().loadFields(new ObjLoader()
            {
                @Override
                public Object load(String id, Class<?> type)
                {
                    return regions.get(id);
                }
            });
        }
        LibzUnitManager.getInstance().getIRRatiosManager().reset();
        LibzUnitManager.getInstance().getIRRatiosManager().getObjects().putAll(intensityRatios);

        System.out.println("Getting calibration models");
        Map<String, Model> calModels = getCalibrationModels();
        for (Map.Entry<String, Model> entry : calModels.entrySet())
        {
            entry.getValue().loadFields(new ObjLoader()
            {
                @Override
                public Object load(String id, Class<?> type)
                {
                    return standards.get(id);
                }
            });
        }
        LibzUnitManager.getInstance().getModelsManager().reset();
        LibzUnitManager.getInstance().getModelsManager().getObjects().putAll(calModels);

        return LibzUnitManager.getInstance().isValidAfterPull();
    }

    @Override
    public boolean pushToLibzUnit()
    {
        String baseUrl = getLibzUnitApiBaseUrl(LibzUnitManager.getInstance().getIpAddress());

        _libzHttpClient = new LIBZHttpClient(baseUrl);
        
        if (pushStandards())
        {
            if (pushRegions())
            {
                if (pushIntensityRatios())
                {
                    if (pushCalibrationModels())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public Map<String, Standard> getStandards()
    {
        return getObjects(_libzHttpClient.mStandardsObjClient);
    }

    @Override
    public Map<String, CalibrationShot> getCalibrationShots()
    {
        try
        {
            Map<String, CalibrationShot> calibrationShots = _libzHttpClient.getCalibrationShots();

            System.out.println("calibrationShots.size == " + calibrationShots.size());

            return calibrationShots;
        }
        catch (IOException ex)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public LIBZPixelSpectrum getLIBZPixelSpectrum(final String shotId)
    {
        try
        {
            return _libzHttpClient.getCalibrationShot(shotId);
        }
        catch (IOException ex)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public Map<String, Region> getRegions()
    {
        return getObjects(_libzHttpClient.mRegionObjClient);
    }

    @Override
    public Map<String, IRRatio> getIntensityRatios()
    {
        return getObjects(_libzHttpClient.mIRObjClient);
    }

    @Override
    public Map<String, Model> getCalibrationModels()
    {
        return getObjects(_libzHttpClient.mModelObjClient);
    }

    @Override
    public boolean pushStandards()
    {
        return push(_libzHttpClient.mStandardsObjClient, LibzUnitManager.getInstance().getStandardsManager());
    }

    @Override
    public boolean pushRegions()
    {
        return push(_libzHttpClient.mRegionObjClient, LibzUnitManager.getInstance().getRegionsManager());
    }

    @Override
    public boolean pushIntensityRatios()
    {
        return push(_libzHttpClient.mIRObjClient, LibzUnitManager.getInstance().getIRRatiosManager());
    }

    @Override
    public boolean pushCalibrationModels()
    {
        return push(_libzHttpClient.mModelObjClient, LibzUnitManager.getInstance().getModelsManager());
    }

    private <T> Map<String, T> getObjects(LIBZHttpClient.BasicObjectClient<T> basicObjectClient)
    {
        Map<String, T> objects = new HashMap();

        try
        {
            List<String> objectIds = basicObjectClient.getIdList();
            if (objectIds != null && objectIds.size() > 0)
            {
                System.out.println("objectIds.size == " + objectIds.size());

                for (String objectId : objectIds)
                {
                    System.out.println("Fetching object");
                    T object = basicObjectClient.getSingleObject(objectId);
                    System.out.println("object with id: " + objectId + " received!");
                    objects.put(objectId, object);
                }
            }
            else
            {
                System.out.println("objectIds list is null");
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return objects;
    }

    private <T> boolean push(LIBZHttpClient.BasicObjectClient<T> basicObjectClient, MutableObjectsManager<T> mutableObjectsManager)
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
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);

            return false;
        }
    }

    private <T> void createObjects(LIBZHttpClient.BasicObjectClient<T> basicObjectClient, Set<String> objectsToCreate, Map<String, T> workingLocalObjects) throws IOException
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