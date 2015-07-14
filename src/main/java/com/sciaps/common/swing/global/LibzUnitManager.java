package com.sciaps.common.swing.global;

import com.devsmart.ThreadUtils;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.sciaps.common.data.*;
import com.sciaps.common.objtracker.DBIndex;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.objtracker.DBObjLoader;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.swing.events.ConnectToInstrumentEvent;
import com.sciaps.common.swing.events.DBObjEvent;
import com.sciaps.common.swing.events.PullEvent;
import com.sciaps.common.swing.events.PushEvent;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.common.webserver.ILaserController.RasterParams;
import java.io.IOException;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LibzUnitManager {

    static Logger logger = LoggerFactory.getLogger(LibzUnitManager.class);
    
    public Instrument instrument;
    public CalibrationShotManager shotManager;
    public RasterParams mRasterParams = new RasterParams();
    private boolean mGotRasterParams = false;
    private DBIndex<Standard> mTestsOfStandard = new DBIndex<Standard>(new DBIndex.MapFunction(){

        @Override
        public void map(DBObj obj, DBIndex.Emitter emitter) {
            if(obj instanceof LIBZTest) {
                LIBZTest test = (LIBZTest)obj;
                emitter.emit(test.standard);
            }
        }
    });

    public DBIndex<Date> mTestsByTime = new DBIndex<Date>(new DBIndex.MapFunction() {
        @Override
        public void map(DBObj obj, DBIndex.Emitter emitter) {
            if(obj instanceof LIBZTest) {
                LIBZTest test = (LIBZTest)obj;
                emitter.emit(new Date(test.unixTime * 1000));
            }
        }
    });

    private ArrayList<Model> mCalibrationModelList = new ArrayList<Model>();


    public LibzUnitManager() {
        recreateCache();
    }

    @Inject
    private LibzUnitApiHandler mApiHandler;

    @Inject
    private DBObjLoader mObjLoader;

    @Inject
    private DBObjTracker mObjTracker;

    @Inject
    public void setEventBus(EventBus eventBus) {
        eventBus.register(this);
    }

    @Subscribe
    public void onPullEvent(PullEvent pullEvent) {
        recreateCache();
        mObjTracker.clear();

        loadCalibrationModels();
    }

    @Subscribe
    public void onPushEvent(PushEvent pushEvent) {

    }

    @Subscribe
    public void onDBObjEvent(DBObjEvent modifiedEvent) {

        switch (modifiedEvent.type) {
            case DBObjEvent.CREATED:
                mTestsOfStandard.insert(modifiedEvent.obj);
                mTestsByTime.insert(modifiedEvent.obj);

                if (modifiedEvent.obj instanceof Model) {
                    mCalibrationModelList.add((Model) modifiedEvent.obj);
                }
                break;

            case DBObjEvent.MODIFIED:
                mTestsOfStandard.update(modifiedEvent.obj);
                mTestsByTime.update(modifiedEvent.obj);
                break;

            case DBObjEvent.DELETED:
                mTestsOfStandard.delete(modifiedEvent.obj);
                mTestsByTime.delete(modifiedEvent.obj);

                if (modifiedEvent.obj instanceof Model) {
                    mCalibrationModelList.remove(modifiedEvent.obj);
                }
                break;
        }
    }



    private void recreateCache() {
        mLoadedStandards.clear();
    }

    private HashSet<String> mLoadedStandards = new HashSet<String>();

    private void load(Standard standard) throws Exception {
        if(standard.mId != null && !mLoadedStandards.contains(standard.mId)) {
            Collection<String> retval = mApiHandler.getTestsForStandard(standard.mId);
            for(String testId : retval) {
                LIBZTest test = mObjLoader.deepLoad(LIBZTest.class, testId);
                mTestsOfStandard.update(test);
            }
            mLoadedStandards.add(standard.mId);
        }
    }


    public synchronized Collection<LIBZTest> getTestsForStandard(Standard standard) throws Exception {

        load(standard);
        Iterator<DBIndex<Standard>.Row> it = mTestsOfStandard.query(standard);
        LinkedList<LIBZTest> retval = new LinkedList<LIBZTest>();

        while(it.hasNext()) {
            DBIndex<Standard>.Row row = it.next();
            if(row.value != standard) {
                break;
            }
            if(row.obj instanceof LIBZTest) {
                retval.add((LIBZTest) row.obj);
            }
        }

        return retval;
    }

    public int getNumberShotsForStandard(Standard standard) throws Exception {
        int retval = 0;
        for(LIBZTest test : getTestsForStandard(standard)) {
            retval += test.getNumShots();
        }
        return retval;
    }
    
    @Subscribe
    public void onConnectToInstrumentEvent(ConnectToInstrumentEvent event) {

        if (mGotRasterParams == false) {
            ThreadUtils.IOThreads.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        mRasterParams = mApiHandler.getDefaultParams();
                        mGotRasterParams = true;
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
            });
        }
    }

    public ArrayList<Model> getModelsForStandard(Standard standard) {

        ArrayList<Model> models = new ArrayList<Model>();

        // save a list of existing models
        if (mCalibrationModelList.isEmpty()) {
            loadCalibrationModels();
        }

        for(Model model : mCalibrationModelList) {
            if (model.standardList.contains(standard)) {
                models.add(model);
            }
        }

        return models;
    }

    public ArrayList<Model> getModelsForRegion(Region theRegion) throws Exception {

        ArrayList<Model> models = new ArrayList<Model>();

        // save a list of existing models
        if (mCalibrationModelList.isEmpty()) {
            loadCalibrationModels();
        }

        for (Model model : mCalibrationModelList) {

            for (IRCurve curve : model.irs.values()) {
                mObjLoader.deepLoad(curve);

                for (Region region : curve.numerator) {
                    if (region == theRegion) {

                        // add it if not already added
                        if (models.contains(model) == false) {
                            models.add(model);
                        }
                    }
                }

                for (Region region : curve.denominator) {
                    if (region == theRegion) {

                        // add it if not already added
                        if (models.contains(model) == false) {
                            models.add(model);
                        }
                    }
                }
            }
        }
        return models;
    }

    private void loadCalibrationModels() {

        try {
            Collection<String> modelIds = mApiHandler.getAllIds(Model.class);
            for (String modelId : modelIds) {
                Model model = mObjLoader.deepLoad(Model.class, modelId);
                if (model != null) {
                    mCalibrationModelList.add(model);

                    // deep load it now once, so it wont slow down the processing later on
                    for (IRCurve curve : model.irs.values()) {
                        mObjLoader.deepLoad(curve);
                    }
                }
            }
        } catch (Exception e) {
            // if anything happen, clear and try reload in later time
            mCalibrationModelList.clear();
        }
    }
}