package com.sciaps.common.swing.global;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.sciaps.common.data.Instrument;
import com.sciaps.common.data.LIBZTest;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObjLoader;
import com.sciaps.common.swing.events.PullEvent;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;

import java.util.ArrayList;
import java.util.Collection;


public class LibzUnitManager {

    public Instrument instrument;
    public CalibrationShotManager shotManager;
    private LoadingCache<String, Collection<LIBZTest>> mStandardTests;

    public LibzUnitManager() {
        recreateCache();
    }

    @Inject
    private LibzUnitApiHandler mApiHandler;

    @Inject
    private DBObjLoader mObjLoader;

    @Inject
    public void setEventBus(EventBus eventBus) {
        eventBus.register(this);
    }

    @Subscribe
    public void onPullEvent(PullEvent pullEvent) {
        recreateCache();
    }

    private void recreateCache() {
        mStandardTests = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<String, Collection<LIBZTest>>() {
                    @Override
                    public Collection<LIBZTest> load(String key) throws Exception {
                        Collection<String> retval = mApiHandler.getTestsForStandard(key);
                        ArrayList<LIBZTest> tests = new ArrayList<LIBZTest>(retval.size());
                        for(String testId : retval) {
                            tests.add(mObjLoader.deepLoad(LIBZTest.class, testId));
                        }
                        return tests;
                    }
                });
    }


    public Collection<LIBZTest> getTestsForStandard(Standard standard) throws Exception {
        return mStandardTests.get(standard.mId);
    }

    public int getNumberShotsForStandard(Standard standard) throws Exception {
        int retval = 0;
        for(LIBZTest test : getTestsForStandard(standard)) {
            retval += test.getNumShots();
        }
        return retval;
    }
}