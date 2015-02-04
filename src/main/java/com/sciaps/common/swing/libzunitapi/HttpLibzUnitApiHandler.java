package com.sciaps.common.swing.libzunitapi;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sciaps.common.data.*;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.objtracker.DBObj.ObjLoader;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.objtracker.IdReference;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.events.SetIPAddressEvent;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.webserver.LIBZHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
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
        final String urlBaseString = "http://" + ipAddress + ":9000";

        return urlBaseString;
    }


    @Inject
    LibzUnitManager mUnitManager;

    @Inject
    DBObjTracker mObjTracker;

    private EventBus mEventBus;

    @Inject
    public void setEventBus(EventBus eventBus) {
        mEventBus = eventBus;
        mEventBus.register(this);
    }

    @Subscribe
    public void onSetIpEvent(SetIPAddressEvent event) {
        setIpAddress(event.ipAddress);
    }

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


    private static <T extends DBObj> void loadAllObjects(Class<T> type,
                                                         LIBZHttpClient.BasicObjectClient<T> client,
                                                         DBObjTracker tracker,
                                                         SimpleIdMapObjLoader objLoader) throws IOException {
        for(String id : client.getIdList()){
            T obj = client.getSingleObject(id);
            obj.mId = id;
            objLoader.idMap.put(id, obj);
            tracker.trackObject(obj);
            obj.loadFields(objLoader);
        }
    }

    private static class SimpleIdMapObjLoader implements ObjLoader {

        public final HashMap<String, DBObj> idMap = new HashMap<String, DBObj>();

        @Override
        public Object load(String id, Class<?> type) {
            Object retval = idMap.get(id);
            if(retval == null){
                logger.warn("could not find object for id: {}", id);
            }
            return retval;
        }
    }

    @Override
    public synchronized void pullFromLibzUnit() throws IOException {
        String baseUrl = getLibzUnitApiBaseUrl(mIPAddress);
        LIBZHttpClient httpClient = new LIBZHttpClient(baseUrl);


        SimpleIdMapObjLoader objLoader = new SimpleIdMapObjLoader();

        loadAllObjects(Standard.class, httpClient.mStandardsObjClient, mObjTracker, objLoader);
        loadAllObjects(Region.class, httpClient.mRegionObjClient, mObjTracker, objLoader);
        loadAllObjects(IRRatio.class, httpClient.mIRObjClient, mObjTracker, objLoader);
        loadAllObjects(Model.class, httpClient.mModelObjClient, mObjTracker, objLoader);

        Iterator<Model> it = mObjTracker.getAllObjectsOfType(Model.class);
        while(it.hasNext()) {
            Model model = it.next();
            for(IRCurve curve : model.irs.values()) {
                curve.loadFields(objLoader);
            }
        }


        Map<String, CalibrationShot> calibrationShots = httpClient.getCalibrationShots();
        for (Map.Entry<String, CalibrationShot> entry : calibrationShots.entrySet()) {
            CalibrationShot shot = entry.getValue();
            shot.mId = entry.getKey();
            shot.loadFields(objLoader);
            mObjTracker.trackObject(shot);
        }
    }

    private static void saveIds(DBObj obj) {
        for(Field field : obj.getClass().getFields()){
            final String fieldName = field.getName();
            if(field.getAnnotation(IdReference.class) != null){
                try {
                    Object fieldValue = field.get(obj);
                    String[] ids = null;
                    if (Iterable.class.isAssignableFrom(field.getType())) {
                        ArrayList<String> idlist = new ArrayList<String>();
                        for (Iterator it = ((Iterable) fieldValue).iterator(); it.hasNext(); ) {
                            DBObj listValue = (DBObj)it.next();
                            saveIds(listValue);
                            String id = listValue.mId;
                            idlist.add(id);
                        }
                        ids = idlist.toArray(new String[idlist.size()]);
                    } else {
                        ids = new String[]{((DBObj)fieldValue).mId};
                    }
                    obj.mFieldIds.put(fieldName, ids);
                } catch (IllegalAccessException e) {
                    logger.error("", e);
                }
            }
        }
    }


    private static <T extends DBObj> void createAll(Class<T> type,
                                                    LIBZHttpClient.BasicObjectClient<T> client,
                                                    DBObjTracker tracker) throws IOException {
        Iterator<T> it = tracker.getNewObjectsOfType(type);
        while(it.hasNext()) {
            T obj = it.next();
            saveIds(obj);
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
            saveIds(obj);
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

    private void saveModelIds(Iterator<Model> models) {
        while(models.hasNext()) {
            Model model = models.next();
            for(IRCurve curve : model.irs.values()){
                saveIds(curve);
            }
        }
    }

    @Override
    public synchronized void pushToLibzUnit() throws IOException {
        String baseUrl = getLibzUnitApiBaseUrl(mIPAddress);
        LIBZHttpClient httpClient = new LIBZHttpClient(baseUrl);

        createAll(Standard.class, httpClient.mStandardsObjClient, mObjTracker);
        createAll(Region.class, httpClient.mRegionObjClient, mObjTracker);
        createAll(IRRatio.class, httpClient.mIRObjClient, mObjTracker);
        saveModelIds(mObjTracker.getNewObjectsOfType(Model.class));
        createAll(Model.class, httpClient.mModelObjClient, mObjTracker);

        updateAll(Standard.class, httpClient.mStandardsObjClient, mObjTracker);
        updateAll(Region.class, httpClient.mRegionObjClient, mObjTracker);
        updateAll(IRRatio.class, httpClient.mIRObjClient, mObjTracker);
        saveModelIds(mObjTracker.getModifiedObjectsOfType(Model.class));
        updateAll(Model.class, httpClient.mModelObjClient, mObjTracker);

        deleteAll(Standard.class, httpClient.mStandardsObjClient, mObjTracker);
        deleteAll(Region.class, httpClient.mRegionObjClient, mObjTracker);
        deleteAll(IRRatio.class, httpClient.mIRObjClient, mObjTracker);
        deleteAll(Model.class, httpClient.mModelObjClient, mObjTracker);
    }

    @Override
    public synchronized void getLIBZPixelSpectrum(final List<String> shotIds, DownloadCallback callback) throws IOException {
        LIBZHttpClient httpClient = null;
        for(String shotId : shotIds) {

            LIBZPixelSpectrum data = mUnitManager.calShotIdCache.get(shotId);
            if(data == null) {
                if(httpClient == null) {
                    String baseUrl = getLibzUnitApiBaseUrl(mIPAddress);
                    httpClient = new LIBZHttpClient(baseUrl);
                }

                data = httpClient.getCalibrationShot(shotId);
                mUnitManager.calShotIdCache.put(shotId, data);
            }

            if(callback != null){
                callback.onData(shotId, data);
            }
        }
    }

}