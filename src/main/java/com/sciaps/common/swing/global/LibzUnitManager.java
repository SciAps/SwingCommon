package com.sciaps.common.swing.global;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.sciaps.common.data.Instrument;
import com.sciaps.common.data.LIBZTest;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBIndex;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.objtracker.DBObjLoader;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.swing.events.DBObjEvent;
import com.sciaps.common.swing.events.PullEvent;
import com.sciaps.common.swing.events.PushEvent;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;

import java.util.*;


public class LibzUnitManager {

    public Instrument instrument;
    public CalibrationShotManager shotManager;
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
                break;

            case DBObjEvent.MODIFIED:
                mTestsOfStandard.update(modifiedEvent.obj);
                mTestsByTime.update(modifiedEvent.obj);
                break;

            case DBObjEvent.DELETED:
                mTestsOfStandard.delete(modifiedEvent.obj);
                mTestsByTime.delete(modifiedEvent.obj);
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
                mTestsOfStandard.insert(test);
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
}