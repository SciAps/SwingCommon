package com.sciaps.common.swing.libzunitapi;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.sciaps.common.Exceptions.LaserNotArmedException;
import com.sciaps.common.data.*;
import com.sciaps.common.data.fingerprint.FingerprintLibrary;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.objtracker.IdReference;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.events.SetIPAddressEvent;
import com.sciaps.common.webserver.ILaserController.RasterParams;
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


public final class HttpLibzUnitApiHandler implements LibzUnitApiHandler {

    static Logger logger = LoggerFactory.getLogger(HttpLibzUnitApiHandler.class);

    private interface GetAllIds {
        Collection<String> get() throws IOException;
    }

    private final Map<Class, GetAllIds> mGetAllIdsFunctions = new HashMap<Class, GetAllIds>();

    private interface LoadObject<T extends DBObj> {
        T get(String id) throws IOException;
    }

    private final Map<Class, LoadObject> mGetObjLoadFunctions = new HashMap<Class, LoadObject>();

    private interface CreateObject<T extends DBObj> {
        void create(T obj) throws IOException;
    }

    private final Map<Class, CreateObject> mCreateObjFunctions = new HashMap<Class, CreateObject>();

    public HttpLibzUnitApiHandler() {

        mGetAllIdsFunctions.put(Standard.class, new GetAllIds() {
            @Override
            public Collection<String> get() throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mStandardsObjClient.getIdList();
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mGetAllIdsFunctions.put(Region.class, new GetAllIds() {
            @Override
            public Collection<String> get() throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mRegionObjClient.getIdList();
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mGetAllIdsFunctions.put(IRRatio.class, new GetAllIds() {
            @Override
            public Collection<String> get() throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mIRObjClient.getIdList();
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mGetAllIdsFunctions.put(Model.class, new GetAllIds() {
            @Override
            public Collection<String> get() throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mModelObjClient.getIdList();
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mGetAllIdsFunctions.put(FingerprintLibraryTemplate.class, new GetAllIds() {
            @Override
            public Collection<String> get() throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mFPLibTemplate.getIdList();
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        ///Loaders
        mGetObjLoadFunctions.put(Standard.class, new LoadObject<Standard>() {
            @Override
            public Standard get(String id) throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mStandardsObjClient.getSingleObject(id);
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mGetObjLoadFunctions.put(Region.class, new LoadObject<Region>() {
            @Override
            public Region get(String id) throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mRegionObjClient.getSingleObject(id);
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mGetObjLoadFunctions.put(IRRatio.class, new LoadObject<IRRatio>() {
            @Override
            public IRRatio get(String id) throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mIRObjClient.getSingleObject(id);
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mGetObjLoadFunctions.put(Model.class, new LoadObject() {
            @Override
            public DBObj get(String id) throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mModelObjClient.getSingleObject(id);
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mGetObjLoadFunctions.put(FingerprintLibraryTemplate.class, new LoadObject() {
            @Override
            public DBObj get(String id) throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mFPLibTemplate.getSingleObject(id);
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mGetObjLoadFunctions.put(LIBZTest.class, new LoadObject() {
            @Override
            public DBObj get(String id) throws IOException {
                synchronized (HttpLibzUnitApiHandler.this) {
                    getClient();
                    try {
                        return mHttpClient.mTestObjClient.getSingleObject(id);
                    } finally {
                        returnClient();
                    }
                }
            }
        });

        mCreateObjFunctions.put(LIBZTest.class, new CreateObject<LIBZTest>() {
            @Override
            public void create(LIBZTest obj) throws IOException {

                synchronized (HttpLibzUnitApiHandler.this) {
                    saveIds(obj);
                    getClient();
                    try {
                        String id = mHttpClient.mTestObjClient.createObject(obj);
                        mObjTracker.setId(obj, id);
                    } finally {
                        returnClient();
                    }
                }
            }
        });


    }

    private static String getLibzUnitApiBaseUrl(String ipAddress) {
        final String urlBaseString = "http://" + ipAddress + ":9000";

        return urlBaseString;
    }


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
    public synchronized Instrument connectToLibzUnit() throws IOException {
        getClient();
        try {
            return mHttpClient.getInstrument();
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized RasterParams getDefaultParams() throws IOException {
        getClient();
        try {
            return mHttpClient.getDefaultParams();
        } finally {
            returnClient();
        }
    }


    @Override
    public synchronized Collection<String> getAllIds(Class<? extends DBObj> classType) throws IOException {
        Collection<String> retval = mGetAllIdsFunctions.get(classType).get();

        return retval;
    }

    @Override
    public <T extends DBObj> T loadObject(Class<T> classType, String id) throws IOException {
        DBObj retval = mGetObjLoadFunctions.get(classType).get(id);
        return (T) retval;
    }

    @Override
    public <T extends DBObj> void createNewObject(T newObj) throws IOException {
        mCreateObjFunctions.get(newObj.getClass()).create(newObj);
    }

    @Override
    public void uploadShot(String testId, int shotNum, LIBZPixelSpectrum data) throws IOException {
        getClient();
        try {
            mHttpClient.postShotSpectrum(testId, shotNum, data);
        } finally {
            returnClient();
        }
    }

    private static void saveIds(DBObj obj) {
        for (Field field : obj.getClass().getFields()) {
            final String fieldName = field.getName();
            if (field.getAnnotation(IdReference.class) != null) {
                try {
                    Object fieldValue = field.get(obj);
                    String[] ids = null;
                    if (Iterable.class.isAssignableFrom(field.getType())) {
                        ArrayList<String> idlist = new ArrayList<String>();
                        for (Iterator it = ((Iterable) fieldValue).iterator(); it.hasNext(); ) {
                            DBObj listValue = (DBObj) it.next();
                            saveIds(listValue);
                            String id = listValue.mId;
                            idlist.add(id);
                        }
                        ids = idlist.toArray(new String[idlist.size()]);
                    } else {
                        if (fieldValue != null) {
                            ids = new String[]{((DBObj) fieldValue).mId};
                        } else {
                            ids = new String[]{null};
                        }
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
        while (it.hasNext()) {
            list.add(it.next());
        }

        for (T obj : list) {
            saveIds(obj);
            tracker.setId(obj, client.createObject(obj));
            tracker.removeCreated(obj);
        }

    }

    private static <T extends DBObj> void updateAll(Class<T> type,
                                                    LIBZHttpClient.BasicObjectClient<T> client,
                                                    DBObjTracker tracker) throws IOException {
        ArrayList<T> list = new ArrayList<T>();

        Iterator<T> it = tracker.getModifiedObjectsOfType(type);
        while (it.hasNext()) {
            list.add(it.next());
        }

        for (T obj : list) {
            saveIds(obj);
            client.updateObject(obj.mId, obj);
            tracker.removeModified(obj);
        }
    }

    private static <T extends DBObj> void deleteAll(Class<T> type,
                                                    LIBZHttpClient.BasicObjectClient<T> client,
                                                    DBObjTracker tracker) throws IOException {
        ArrayList<T> list = new ArrayList<T>();

        Iterator<T> it = tracker.getDeletedObjectsOfType(type);
        while (it.hasNext()) {
            list.add(it.next());
        }

        for (T obj : list) {
            try {
                client.deleteObject(obj.mId);
                tracker.removeDelete(obj);
            } catch (Exception e) {
                logger.error("delete {} id: {}", type.getSimpleName(), obj.mId, e);
            }
        }
    }

    private void saveModelIds(Iterator<Model> models) {
        while (models.hasNext()) {
            Model model = models.next();
            for (IRCurve curve : model.irs.values()) {
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
            createAll(LIBZTest.class, httpClient.mTestObjClient, mObjTracker);
            createAll(FingerprintLibraryTemplate.class, httpClient.mFPLibTemplate, mObjTracker);

            updateAll(Standard.class, httpClient.mStandardsObjClient, mObjTracker);
            updateAll(Region.class, httpClient.mRegionObjClient, mObjTracker);
            updateAll(IRRatio.class, httpClient.mIRObjClient, mObjTracker);
            saveModelIds(mObjTracker.getModifiedObjectsOfType(Model.class));
            updateAll(Model.class, httpClient.mModelObjClient, mObjTracker);
            updateAll(LIBZTest.class, httpClient.mTestObjClient, mObjTracker);
            updateAll(FingerprintLibraryTemplate.class, httpClient.mFPLibTemplate, mObjTracker);

            deleteAll(Standard.class, httpClient.mStandardsObjClient, mObjTracker);
            deleteAll(Region.class, httpClient.mRegionObjClient, mObjTracker);
            deleteAll(IRRatio.class, httpClient.mIRObjClient, mObjTracker);
            deleteAll(Model.class, httpClient.mModelObjClient, mObjTracker);
            deleteAll(LIBZTest.class, httpClient.mTestObjClient, mObjTracker);
            deleteAll(FingerprintLibraryTemplate.class, httpClient.mFPLibTemplate, mObjTracker);

        } finally {
            returnClient();
        }
    }

    private synchronized LIBZHttpClient getClient() {
        if (mHttpClient == null) {
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
    public synchronized LIBZPixelSpectrum downloadShot(String testId, int shotNum) throws IOException {
        LIBZHttpClient client = getClient();
        try {
            LIBZPixelSpectrum data = client.getShotSpectrum(testId, shotNum);
            return data;
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized Collection<String> getTestsForStandard(String standardId) throws IOException {
        LIBZHttpClient client = getClient();
        try {
            Collection<String> ids = client.getTestsForStandard(standardId);
            return ids;
        } finally {
            returnClient();
        }

    }

    @Override
    public synchronized List<String> getTestsSince(long unixTimestamp) throws IOException {
        LIBZHttpClient client = getClient();
        try {
            List<String> ids = client.getTestsSince(unixTimestamp);
            return ids;
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized List<LIBZPixelSpectrum> rasterTest(RasterParams params) throws IOException, LaserNotArmedException {
        LIBZHttpClient client = getClient();
        try {
            return client.rasterTest(params);
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized String takeRasterTest(RasterParams params) throws IOException, LaserNotArmedException {
        LIBZHttpClient client = getClient();
        try {
            return client.takeTest(params);
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized void postFPLibrary(FingerprintLibraryTemplate libTemplate, FingerprintLibrary fplib) throws IOException {
        LIBZHttpClient client = getClient();
        try {
            client.postFingerprintLibrary(libTemplate, fplib);
        } finally {
            returnClient();
        }
    }

    @Override
    public Collection<String> getGradeLibraries() throws IOException {
        LIBZHttpClient client = getClient();
        try {
            return client.getGradeLibraries();
        } finally {
            returnClient();
        }
    }

    @Override
    public Grade[] getGradeLib(String gradelibName) throws IOException {
        LIBZHttpClient client = getClient();
        try {
            return client.getGradeLib(gradelibName);
        } finally {
            returnClient();
        }
    }

    @Override
    public void createGradeLib(String gradelibName, Grade[] grades) throws IOException {
        LIBZHttpClient client = getClient();
        try {
            client.createGradeLib(gradelibName, grades);
        } finally {
            returnClient();
        }
    }

    @Override
    public void updateGradeLib(String gradelibName, Grade[] grades) throws IOException {
        LIBZHttpClient client = getClient();
        try {
            client.updateGradeLib(gradelibName, grades);
        } finally {
            returnClient();
        }
    }

    @Override
    public void deleteGradeLib(String gradelibName) throws IOException {
        LIBZHttpClient client = getClient();
        try {
            client.deleteGradeLib(gradelibName);
        } finally {
            returnClient();
        }
    }

}