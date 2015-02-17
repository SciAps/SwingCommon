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
import java.util.concurrent.ExecutionException;

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
            if(obj instanceof Key) {
                Key other = (Key)obj;
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

    @Inject
    LibzUnitApiHandler mApiHandler;

    public CalibrationShotManager(File cacheDir) {
        mCacheDir = cacheDir;
        mCache = CacheBuilder
                .newBuilder()
                .maximumSize(60 * 3 * 30)
                .build(mLoader);
    }


    public LIBZPixelSpectrum getShot(String testId, int shotNum) {
        LIBZPixelSpectrum retval = null;
        try {
            retval = mCache.get(new Key(testId, shotNum));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return retval;
    }


    private CacheLoader<? super Key, LIBZPixelSpectrum> mLoader = new CacheLoader<Key, LIBZPixelSpectrum>() {
        @Override
        public LIBZPixelSpectrum load(Key key) throws Exception {

            LIBZPixelSpectrum retval = null;

            final File testDir = new File(mCacheDir, key.testId);
            testDir.mkdirs();
            File file = new File(testDir, String.format("shot_%d.dat", key.shotNum));
            if(file.exists()){
                try {
                    retval = ShotDataHelper.loadCompressedFile(file);
                } catch (IOException e) {
                    logger.error("could not load cache file: {}", file.getAbsolutePath());
                }
            }

            if(retval == null) {
                logger.info("downloading shot: {}", key);
                retval = mApiHandler.downloadShot(key.testId, key.shotNum);
                ShotDataHelper.saveCompressedFile(retval, file);
            }

            return retval;
        }
    };
}
