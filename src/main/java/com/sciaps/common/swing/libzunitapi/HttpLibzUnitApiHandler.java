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
import com.sciaps.common.webserver.ILIBZClientInterface;
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
                        return mHttpClient.getIdList(Standard.class);
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
                        return mHttpClient.getIdList(Region.class);
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
                        return mHttpClient.getIdList(IRRatio.class);
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
                        return mHttpClient.getIdList(Model.class);
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
                        return mHttpClient.getIdList(FingerprintLibraryTemplate.class);
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
                        return mHttpClient.getSingleObject(Standard.class, id);
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
                        return mHttpClient.getSingleObject(Region.class, id);
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
                        return mHttpClient.getSingleObject(IRRatio.class, id);
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
                        return mHttpClient.getSingleObject(Model.class, id);
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
                        return mHttpClient.getSingleObject(FingerprintLibraryTemplate.class, id);
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
                        return mHttpClient.getSingleObject(LIBZTest.class, id);
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
                        String id = mHttpClient.createObject(obj);
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
                                                    ILIBZClientInterface client,
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
                                                    ILIBZClientInterface client,
                                                    DBObjTracker tracker) throws IOException {
        ArrayList<T> list = new ArrayList<T>();

        Iterator<T> it = tracker.getModifiedObjectsOfType(type);
        while (it.hasNext()) {
            list.add(it.next());
        }

        for (T obj : list) {
            saveIds(obj);
            client.updateObject(obj);
            tracker.removeModified(obj);
        }
    }

    private static <T extends DBObj> void deleteAll(Class<T> type,
                                                    ILIBZClientInterface client,
                                                    DBObjTracker tracker) throws IOException {
        ArrayList<T> list = new ArrayList<T>();

        Iterator<T> it = tracker.getDeletedObjectsOfType(type);
        while (it.hasNext()) {
            list.add(it.next());
        }

        for (T obj : list) {
            try {
                client.deleteObject(obj);
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

    private void createAllGradeLibraries(ILIBZClientInterface client) throws IOException {
        ArrayList<GradeLibrary> list = new ArrayList<GradeLibrary>();

        Iterator<GradeLibrary> it = mObjTracker.getNewObjectsOfType(GradeLibrary.class);
        while (it.hasNext()) {
            list.add(it.next());
        }

        for (GradeLibrary gradeLibrary : list) {
            Grade[] grades = gradeLibrary.mGrades.toArray(new Grade[gradeLibrary.mGrades.size()]);
            client.createGradeLib(gradeLibrary.mName, grades);
            gradeLibrary.mId = "cannedID";
            mObjTracker.removeCreated(gradeLibrary);
        }
    }

    private void updateAllGradeLibraries(ILIBZClientInterface client) throws IOException {
        ArrayList<GradeLibrary> list = new ArrayList<GradeLibrary>();

        Iterator<GradeLibrary> it = mObjTracker.getModifiedObjectsOfType(GradeLibrary.class);
        while (it.hasNext()) {
            list.add(it.next());
        }

        for (GradeLibrary gradeLibrary : list) {
            Grade[] grades = gradeLibrary.mGrades.toArray(new Grade[gradeLibrary.mGrades.size()]);
            client.updateGradeLib(gradeLibrary.mName, grades);
            mObjTracker.removeModified(gradeLibrary);
        }
    }

    private void deleteAllGradeLibraries(ILIBZClientInterface client) throws IOException {
        ArrayList<GradeLibrary> list = new ArrayList<GradeLibrary>();

        Iterator<GradeLibrary> it = mObjTracker.getDeletedObjectsOfType(GradeLibrary.class);
        while (it.hasNext()) {
            list.add(it.next());
        }

        for (GradeLibrary gradeLibrary : list) {
            client.deleteGradeLib(gradeLibrary.mName);
            mObjTracker.removeDelete(gradeLibrary);
        }
    }

    @Override
    public synchronized void pushToLibzUnit() throws IOException {

        ILIBZClientInterface client = getClient();

        try {
            createAll(Standard.class, client, mObjTracker);
            createAll(Region.class, client, mObjTracker);
            createAll(IRRatio.class, client, mObjTracker);
            saveModelIds(mObjTracker.getNewObjectsOfType(Model.class));
            createAll(Model.class, client, mObjTracker);
            createAll(LIBZTest.class, client, mObjTracker);
            createAll(FingerprintLibraryTemplate.class, client, mObjTracker);

            updateAll(Standard.class, client, mObjTracker);
            updateAll(Region.class, client, mObjTracker);
            updateAll(IRRatio.class, client, mObjTracker);
            saveModelIds(mObjTracker.getModifiedObjectsOfType(Model.class));
            updateAll(Model.class, client, mObjTracker);
            updateAll(LIBZTest.class, client, mObjTracker);
            updateAll(FingerprintLibraryTemplate.class, client, mObjTracker);

            deleteAll(Standard.class, client, mObjTracker);
            deleteAll(Region.class, client, mObjTracker);
            deleteAll(IRRatio.class, client, mObjTracker);
            deleteAll(Model.class, client, mObjTracker);
            deleteAll(LIBZTest.class, client, mObjTracker);
            deleteAll(FingerprintLibraryTemplate.class, client, mObjTracker);

            createAllGradeLibraries(client);
            updateAllGradeLibraries(client);
            deleteAllGradeLibraries(client);

        } finally {
            returnClient();
        }
    }

    private synchronized ILIBZClientInterface getClient() {
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


    private ILIBZClientInterface mHttpClient;
    private Future<?> mDeleteHttpClientTask;
    private ScheduledExecutorService mFutureRunner = Executors.newSingleThreadScheduledExecutor();

    @Override
    public synchronized LIBZPixelSpectrum downloadShot(String testId, int shotNum) throws IOException {
        ILIBZClientInterface client = getClient();
        try {
            LIBZPixelSpectrum data = client.getShotSpectrum(testId, shotNum);
            return data;
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized Collection<String> getTestsForStandard(String standardId) throws IOException {
        ILIBZClientInterface client = getClient();
        try {
            Collection<String> ids = client.getTestsForStandard(standardId);
            return ids;
        } finally {
            returnClient();
        }

    }

    @Override
    public synchronized List<String> getTestsSince(long unixTimestamp) throws IOException {
        ILIBZClientInterface client = getClient();
        try {
            List<String> ids = client.getTestsSince(unixTimestamp);
            return ids;
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized List<LIBZPixelSpectrum> rasterTest(RasterParams params) throws IOException, LaserNotArmedException {
        ILIBZClientInterface client = getClient();
        try {
            return client.rasterTest(params);
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized String takeRasterTest(RasterParams params) throws IOException, LaserNotArmedException {
        ILIBZClientInterface client = getClient();
        try {
            return client.takeTest(params);
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized void postFPLibrary(FingerprintLibraryTemplate libTemplate, FingerprintLibrary fplib) throws IOException {
        ILIBZClientInterface client = getClient();
        try {
            client.postFingerprintLibrary(libTemplate, fplib);
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized Collection<String> getGradeLibraries() throws IOException {
        ILIBZClientInterface client = getClient();
        try {
            return client.getGradeLibraries();
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized Grade[] getGradeLib(String gradelibName) throws IOException {
        ILIBZClientInterface client = getClient();
        try {
            return client.getGradeLib(gradelibName);
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized void createGradeLib(String gradelibName, Grade[] grades) throws IOException {
        ILIBZClientInterface client = getClient();
        try {
            client.createGradeLib(gradelibName, grades);
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized void updateGradeLib(String gradelibName, Grade[] grades) throws IOException {
        ILIBZClientInterface client = getClient();
        try {
            client.updateGradeLib(gradelibName, grades);
        } finally {
            returnClient();
        }
    }

    @Override
    public synchronized void deleteGradeLib(String gradelibName) throws IOException {
        ILIBZClientInterface client = getClient();
        try {
            client.deleteGradeLib(gradelibName);
        } finally {
            returnClient();
        }
    }

}