package com.sciaps.common.swing.libzunitapi;

import com.devsmart.ThreadUtils;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    public Instrument connectToLibzUnit() throws IOException {
        String baseUrl = getLibzUnitApiBaseUrl(mIPAddress);
        LIBZHttpClient httpClient = new LIBZHttpClient(baseUrl);

        Instrument instrument = httpClient.getInstrument();
        return instrument;
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
        if (calibrationShots != null) {
            for (Map.Entry<String, CalibrationShot> entry : calibrationShots.entrySet()) {
                CalibrationShot shot = entry.getValue();
                shot.mId = entry.getKey();
                shot.loadFields(objLoader);
                mObjTracker.trackObject(shot);
            }
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
        ArrayList<T> list = new ArrayList<T>();
        
        Iterator<T> it = tracker.getNewObjectsOfType(type);        
        while(it.hasNext()) {
            list.add(it.next());
        }
        
        for(T obj : list){
            saveIds(obj);
            obj.mId = client.createObject(obj);
            tracker.removeCreated(obj);
        }
        
    }

    private static <T extends DBObj> void updateAll(Class<T> type,
                                                    LIBZHttpClient.BasicObjectClient<T> client,
                                                    DBObjTracker tracker) throws IOException {
        ArrayList<T> list = new ArrayList<T>();
        
        Iterator<T> it = tracker.getModifiedObjectsOfType(type);
        while(it.hasNext()) {
            list.add(it.next());
        }
        
        for(T obj : list){
            saveIds(obj);
            client.updateObject(obj.mId, obj);           
        }       
    }

    private static <T extends DBObj> void deleteAll(Class<T> type,
                                                    LIBZHttpClient.BasicObjectClient<T> client,
                                                    DBObjTracker tracker) throws IOException {
        ArrayList<T> list = new ArrayList<T>();
        
        Iterator<T> it = tracker.getDeletedObjectsOfType(type);
        while(it.hasNext()) {
            list.add(it.next());
        }
        
        for(T obj : list){
            client.deleteObject(obj.mId);
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

        LIBZHttpClient httpClient = getClient();

        try {
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
                        
        } finally {
            returnClient();
        }
    }
    
    private LIBZHttpClient getClient() {
        if(mHttpClient == null) {
            String baseUrl = getLibzUnitApiBaseUrl(mIPAddress);
            mHttpClient = new LIBZHttpClient(baseUrl);
        }
        return mHttpClient;
    }
    
    private void returnClient() {
        mDeleteHttpClientTask = mFutureRunner.schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (HttpLibzUnitApiHandler.this) {
                    mHttpClient = null;
                    mDeleteHttpClientTask = null;
                }
            }
        }, 500, TimeUnit.MILLISECONDS);
    }


    private LIBZHttpClient mHttpClient;
    private Future<?> mDeleteHttpClientTask;
    private ScheduledExecutorService mFutureRunner = Executors.newSingleThreadScheduledExecutor();

    @Override
    public synchronized LIBZPixelSpectrum downloadShot(String shotId) throws IOException {
        LIBZHttpClient client = getClient();
        try {
            LIBZPixelSpectrum data = client.getCalibrationShot(shotId);

            if(mDeleteHttpClientTask != null) {
                mDeleteHttpClientTask.cancel(false);
            }
            return data;
        }finally {
            returnClient();   
        }        
    }

}