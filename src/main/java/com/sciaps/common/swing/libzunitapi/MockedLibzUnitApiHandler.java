package com.sciaps.common.swing.libzunitapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.*;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.IOUtils;
import com.sciaps.common.swing.utils.JsonUtils;
import org.apache.commons.lang.math.DoubleRange;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.zip.GZIPInputStream;


public final class MockedLibzUnitApiHandler implements LibzUnitApiHandler {

    @Inject
    LibzUnitManager mUnitManager;

    @Inject
    DBObjTracker mObjTracker;

    @Override
    public Instrument connectToLibzUnit() {
        Instrument retval = new Instrument();
        retval.id = "mockinstrument";
        retval.version = "mock 2.1";
        retval.versionCode = 500;
        return retval;
    }


    @Override
    public void pushToLibzUnit() throws IOException {

    }

    @Override
    public Collection<String> getAllIds(Class<? extends DBObj> classType) throws IOException {
        return null;
    }

    @Override
    public <T extends DBObj> T loadObject(Class<T> classType, String id) throws IOException {
        return null;
    }

    @Override
    public LIBZPixelSpectrum downloadShot(String shotId) throws IOException {
        final File testDataDir = new File("testdata");
        LIBZPixelSpectrum data = null;
        if (data == null) {
            File file = new File(testDataDir, shotId + ".json.gz");
            InputStream in = new FileInputStream(file);
            in = new GZIPInputStream(in);
            JsonReader jsonReader = new JsonReader(new InputStreamReader(in));
            try {
                Gson gson = new GsonBuilder().create();
                data = gson.fromJson(jsonReader, LIBZPixelSpectrum.class);
            } finally {
                IOUtils.safeClose(jsonReader);
            }

            try {
                //simulate download time
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    private <T extends DBObj> void updateIds(Map<String, T> idMap) {
        for(Map.Entry<String, T> e : idMap.entrySet()) {
            e.getValue().mId = e.getKey();
        }
    }

    private Map<String, Standard> getStandards()
    {
        try
        {
            final File testDataDir = new File("testdata");
            FileInputStream in = new FileInputStream(new File(testDataDir, "standards.json"));
            String json = IOUtils.extractStringFromInputStream(in);
            Type type = new TypeToken<Map<String, Standard>>()
            {
            }.getType();
            Map<String, Standard> standards = JsonUtils.deserializeJsonIntoType(json, type);
            updateIds(standards);

            System.out.println("# of Standards pulled from LIBZ Unit: " + standards.size());

            return standards;
        }
        catch (IOException e)
        {
            throw new RuntimeException("ERROR");
        }
    }

    @Override
    public Collection<LIBZTest> getTestsForStandard(String standardId) throws IOException {
        return null;
    }

    @Override
    public List<LIBZTest> getTestsSince(long unixTimestamp) throws IOException {
        return null;
    }


}