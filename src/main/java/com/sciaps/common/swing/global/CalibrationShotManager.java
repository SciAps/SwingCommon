package com.sciaps.common.swing.global;


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


    private final File mCacheDir;
    private LoadingCache<String, LIBZPixelSpectrum> mCache;

    @Inject
    LibzUnitApiHandler mApiHandler;

    public CalibrationShotManager(File cacheDir) {
        mCacheDir = cacheDir;
        mCache = CacheBuilder
                .newBuilder()
                .maximumSize(60 * 3 * 30)
                .build(mLoader);
    }


    public LIBZPixelSpectrum getShot(String id) {
        LIBZPixelSpectrum retval = null;
        try {
            retval = mCache.get(id);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return retval;
    }


    private CacheLoader<? super String, LIBZPixelSpectrum> mLoader = new CacheLoader<String, LIBZPixelSpectrum>() {
        @Override
        public LIBZPixelSpectrum load(String key) throws Exception {

            LIBZPixelSpectrum retval = null;
            key = key.replace(File.pathSeparatorChar, '_');
            String filename = String.format("%s.data", key);
            File file = new File(mCacheDir, filename);
            if(file.exists()){
                try {
                    retval = ShotDataHelper.loadCompressedFile(file);
                } catch (IOException e) {
                    logger.error("could not load cache file: {}", file.getAbsolutePath());
                }
            }

            if(retval == null) {
                logger.info("downloading shot: {}", key);
                retval = mApiHandler.downloadShot(key);
                ShotDataHelper.saveCompressedFile(retval, file);
            }

            return retval;
        }
    };
}
