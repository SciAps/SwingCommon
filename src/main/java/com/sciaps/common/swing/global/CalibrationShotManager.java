package com.sciaps.common.swing.global;


import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.common.utils.ShotDataHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CalibrationShotManager {

    static Logger logger = LoggerFactory.getLogger(CalibrationShotManager.class);

    private String encodeKey(String testId, int shotNum) {
        return String.format("%s_%d", testId, shotNum);
    }


    private static class Key {
        String testId;
        int shotNum;

        public Key(String testId, int shotNum) {
            this.testId = testId;
            this.shotNum = shotNum;
        }

        @Override
        public boolean equals(Object obj) {
            boolean retval = false;
            if (obj instanceof Key) {
                Key other = (Key) obj;
                retval = Objects.equal(testId, other.testId)
                        && shotNum == other.shotNum;
            }
            return retval;
        }

        @Override
        public int hashCode() {
            int retval = Objects.hashCode(testId, shotNum);
            return retval;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("test", testId)
                    .add("shot", shotNum)
                    .toString();
        }
    }


    private final File mCacheDir;
    private LoadingCache<Key, LIBZPixelSpectrum> mCache;

    // This list will get reset by the StatusBar when a connection is re-establish
    public static HashMap<String, ArrayList<Integer>> mDroppedShots = new HashMap<String, ArrayList<Integer>>();

    @Inject
    LibzUnitApiHandler mApiHandler;

    public CalibrationShotManager(File cacheDir) {
        mCacheDir = cacheDir;
        mCache = CacheBuilder
                .newBuilder()
                        //.maximumSize(60 * 3 * 13)
                .maximumSize(2400)
                .build(mLoader);
    }


    public LIBZPixelSpectrum getShot(String testId, int shotNum) throws Exception {
        final Key key = new Key(testId, shotNum);
        LIBZPixelSpectrum retval = null;
        try {
            retval = mCache.get(key);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            // This exception is not a connection exception, just that nothing in the cache due to dropped shot issue.
            logger.error("", e);
        }
        return retval;
    }

    /*
    public void storeShot(String testId, int shotNum, LIBZPixelSpectrum data) {
        Key key = new Key(testId, shotNum);
        mMemoryStore.put(key, data);
    }
    */


    private CacheLoader<? super Key, LIBZPixelSpectrum> mLoader = new CacheLoader<Key, LIBZPixelSpectrum>() {
        @Override
        public LIBZPixelSpectrum load(Key key) throws Exception {

            LIBZPixelSpectrum retval = null;

            final File testDir = new File(mCacheDir, key.testId);
            File file = new File(testDir, String.format("shot_%d.dat", key.shotNum));
            if (file.exists()) {
                try {
                    retval = ShotDataHelper.loadCompressedFile(file);
                } catch (IOException e) {
                    logger.error("could not load cache file: {}", file.getAbsolutePath());
                }
            }

            if (retval == null) {

                ArrayList<Integer> tmpDroppedShotNumForTest = mDroppedShots.get(key.testId);
                if (tmpDroppedShotNumForTest == null || tmpDroppedShotNumForTest.contains(key.shotNum) == false) {

                    logger.info("downloading shot: {}", key);
                    retval = mApiHandler.downloadShot(key.testId, key.shotNum);
                    if (retval != null) {
                        testDir.mkdirs();
                        ShotDataHelper.saveCompressedFile(retval, file);
                    } else {

                        if (mDroppedShots.get(key.testId) == null) {
                            ArrayList<Integer> list = new ArrayList<Integer>();
                            list.add(key.shotNum);
                            mDroppedShots.put(key.testId, list);
                        } else {
                            mDroppedShots.get(key.testId).add(key.shotNum);
                        }
                    }
                }
            }

            return retval;
        }
    };
}
