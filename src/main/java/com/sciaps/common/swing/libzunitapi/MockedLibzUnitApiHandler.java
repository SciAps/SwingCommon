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

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.apache.commons.lang.math.DoubleRange;

/**
 *
 * @author sgowen
 */
public final class MockedLibzUnitApiHandler implements LibzUnitApiHandler
{

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
    public void pullFromLibzUnit() throws IOException {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Map<String, Standard> standards = getStandards();
        for(Map.Entry<String, Standard> entry : standards.entrySet()) {
            Standard standard = entry.getValue();
            standard.mId = entry.getKey();
            mObjTracker.trackObject(standard);
        }

        Map<String, Region> regions = new HashMap<String, Region>();
        {

            Region region1 = new Region();
            region1.wavelengthRange = new DoubleRange(640.0, 670.0);
            region1.name = "Cu_640-670";

            Region region2 = new Region();
            region2.wavelengthRange = new DoubleRange(380.0, 410.0);
            region2.name = "Al_380-410";

            regions.put("7af7ec16-b1ea-46a4-bf6b-1dfab8318d33", region1);
            regions.put("24d7c7d1-3abc-48b9-b07a-292ea44f0738", region2);

            {
                Region r = new Region();
                r.wavelengthRange = new DoubleRange(396.2, 396.5);
                r.name = "Al_396";
                regions.put("23j4209sdf8", r);
            }

            for(Map.Entry<String, Region> entry : regions.entrySet()) {
                Region standard = entry.getValue();
                standard.mId = entry.getKey();
                mObjTracker.trackObject(standard);
            }
        }

        Map<String, IRRatio> intensityRatios = new HashMap<String, IRRatio>();
        {
            IRRatio intensityRatio = new IRRatio();
            intensityRatio.name = "Copper Finder 12/10/14";
            intensityRatio.element = AtomicElement.Copper;
            intensityRatio.numerator = new ArrayList<Region>();
            intensityRatio.numerator.add(regions.get("7af7ec16-b1ea-46a4-bf6b-1dfab8318d33"));
            intensityRatio.numerator.add(regions.get("23j4209sdf8"));
            intensityRatio.denominator = new ArrayList<Region>();
            intensityRatio.denominator.add(regions.get("24d7c7d1-3abc-48b9-b07a-292ea44f0738"));

            IRRatio intensityRatio2 = new IRRatio();
            intensityRatio2.name = "Aluminum Finder 12/10/14";
            intensityRatio2.element = AtomicElement.Aluminum;
            intensityRatio2.numerator = new ArrayList<Region>();
            intensityRatio2.numerator.add(regions.get("24d7c7d1-3abc-48b9-b07a-292ea44f0738"));
            intensityRatio2.denominator = new ArrayList<Region>();
            intensityRatio2.denominator.add(regions.get("7af7ec16-b1ea-46a4-bf6b-1dfab8318d33"));

            intensityRatios.put("UNIQUE_ID_IR_1", intensityRatio);
            intensityRatios.put("UNIQUE_ID_IR_2", intensityRatio2);

            for(Map.Entry<String, IRRatio> entry : intensityRatios.entrySet()) {
                IRRatio standard = entry.getValue();
                standard.mId = entry.getKey();
                mObjTracker.trackObject(standard);
            }

        }

        {
            Map<String, Model> calModels = new HashMap();

            {
                Model calModel = new Model();
                calModel.name = "Copper Cal Model";
                calModel.standardList.add(standards.get("123456789"));
                calModel.standardList.add(standards.get("12"));
                calModel.standardList.add(standards.get("123"));

                {
                    IRRatio irRatio = intensityRatios.get("UNIQUE_ID_IR_1");
                    IRCurve irCurve = new IRCurve();
                    irCurve.name = irRatio.name;
                    irCurve.element = irRatio.element;
                    irCurve.numerator = irRatio.numerator;
                    irCurve.denominator = irRatio.denominator;
                    calModel.irs.put(AtomicElement.Copper, irCurve);
                }


                {
                    IRCurve irCurve = new IRCurve();
                    irCurve.name = "other curve";
                    irCurve.element = AtomicElement.Carbon;

                    Region n1 = Region.parse("Al 396-397");
                    irCurve.numerator.add(n1);

                    Region d1 = Region.parse("Fe 384-385");
                    irCurve.denominator.add(d1);
                    calModel.irs.put(AtomicElement.Aluminum, irCurve);
                }

                final String modelId = java.util.UUID.randomUUID().toString();
                calModel.mId = modelId;
                calModels.put(modelId, calModel);
            }

            {
                Model calModel = new Model();
                calModel.name = "Stainless Model";
                calModel.standardList.add(standards.get("123456789"));
                calModel.standardList.add(standards.get("12"));
                calModel.standardList.add(standards.get("123"));

                //unshot data
                calModel.standardList.add(standards.get("123456"));



                IRRatio irRatio = intensityRatios.get("UNIQUE_ID_IR_1");
                IRCurve irCurve = new IRCurve();
                irCurve.name = irRatio.name;
                irCurve.element = irRatio.element;
                irCurve.numerator = irRatio.numerator;
                irCurve.denominator = irRatio.denominator;
                calModel.irs.put(AtomicElement.Copper, irCurve);

                final String modelId = java.util.UUID.randomUUID().toString();
                calModel.mId = modelId;
                calModels.put(modelId, calModel);
            }

            for(Map.Entry<String, Model> entry : calModels.entrySet()) {
                Model standard = entry.getValue();
                standard.mId = entry.getKey();
                mObjTracker.trackObject(standard);
            }
        }


    }

    @Override
    public void pushToLibzUnit() throws IOException {

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



}