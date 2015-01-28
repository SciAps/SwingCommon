package com.sciaps.common.swing.libzunitapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.IOUtils;
import com.sciaps.common.swing.utils.JsonUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.lang.math.DoubleRange;

/**
 *
 * @author sgowen
 */
public final class MockedLibzUnitApiHandler implements LibzUnitApiHandler
{
    @Override
    public boolean connectToLibzUnit()
    {
        LibzUnitManager.getInstance().setLibzUnitUniqueIdentifier("UNIQUE_LIBZ_UNIT_ID_HERE");

        return true;
    }

    @Override
    public boolean pullFromLibzUnit()
    {
        final Map<String, Standard> standards = getStandards();
        LibzUnitManager.getInstance().getStandardsManager().reset();
        LibzUnitManager.getInstance().getStandardsManager().getObjects().putAll(standards);

        Map<String, CalibrationShot> calibrationShots = getCalibrationShots();
        LibzUnitManager.getInstance().getCalibrationShots().clear();
        LibzUnitManager.getInstance().getCalibrationShots().putAll(calibrationShots);
        
        final Map<String, Region> regions = getRegions();
        LibzUnitManager.getInstance().getRegionsManager().reset();
        LibzUnitManager.getInstance().getRegionsManager().getObjects().putAll(regions);

        Map<String, IRRatio> intensityRatios = getIntensityRatios();
        LibzUnitManager.getInstance().getIRRatiosManager().reset();
        LibzUnitManager.getInstance().getIRRatiosManager().getObjects().putAll(intensityRatios);

        Map<String, Model> calModels = getCalibrationModels();
        LibzUnitManager.getInstance().getModelsManager().reset();
        LibzUnitManager.getInstance().getModelsManager().getObjects().putAll(calModels);

        return LibzUnitManager.getInstance().isValidAfterPull();
    }

    @Override
    public boolean pushToLibzUnit()
    {
        return false;
    }

    @Override
    public LIBZPixelSpectrum getLIBZPixelSpectrum(final String shotId)
    {
        if (LibzUnitManager.getInstance().getLIBZPixelSpectra().containsKey(shotId))
        {
            return LibzUnitManager.getInstance().getLIBZPixelSpectra().get(shotId);
        }
        else
        {
            JsonReader jsonReader = null;

            try
            {
                URL url = ClassLoader.getSystemResource(shotId + ".json.gz");

                BufferedInputStream bis = new BufferedInputStream(url.openStream());
                GZIPInputStream gzis = new GZIPInputStream(bis);
                jsonReader = new JsonReader(new InputStreamReader(gzis));

                Gson gson = new GsonBuilder().create();
                final LIBZPixelSpectrum libzPixelSpectrum = gson.fromJson(jsonReader, LIBZPixelSpectrum.class);
                LibzUnitManager.getInstance().getLIBZPixelSpectra().put(shotId, libzPixelSpectrum);

                return libzPixelSpectrum;
            }
            catch (IOException e)
            {
                Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, e);

                return null;
            }
            finally
            {
                IOUtils.safeClose(jsonReader);
            }
        }
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
            URL url = ClassLoader.getSystemResource("standards.json");
            String json = IOUtils.extractStringFromInputStream(url.openStream());
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

    private Map<String, CalibrationShot> getCalibrationShots()
    {
        Map<String, CalibrationShot> calibrationShots = new HashMap<String, CalibrationShot>();
        CalibrationShot cal1 = new CalibrationShot();
        cal1.displayName = "Shot Data 1";
        cal1.timeStamp = new Date();
        cal1.standard = LibzUnitManager.getInstance().getStandardsManager().getObjects().get("123456789");
        CalibrationShot cal2 = new CalibrationShot();
        cal2.displayName = "Shot Data 2";
        cal2.timeStamp = new Date();
        cal2.standard = LibzUnitManager.getInstance().getStandardsManager().getObjects().get("1");
        CalibrationShot cal3 = new CalibrationShot();
        cal3.displayName = "Shot Data 3";
        cal3.timeStamp = new Date();
        cal3.standard = LibzUnitManager.getInstance().getStandardsManager().getObjects().get("12");
        CalibrationShot cal4 = new CalibrationShot();
        cal4.displayName = "Shot Data 4";
        cal4.timeStamp = new Date();
        cal4.standard = LibzUnitManager.getInstance().getStandardsManager().getObjects().get("123");

        calibrationShots.put("a4653d0b-4c1f-429b-9cb2-0403817f8e16", cal1);
        calibrationShots.put("a4653d0b-4c1f-429b-9cb2-0403817f8e17", cal2);
        calibrationShots.put("a4653d0b-4c1f-429b-9cb2-0403817f8e18", cal3);
        calibrationShots.put("a4653d0b-4c1f-429b-9cb2-0403817f8e19", cal4);

        return calibrationShots;
    }

    private Map<String, Region> getRegions()
    {
        Map<String, Region> regions = new HashMap();
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

        System.out.println("# of Regions pulled from LIBZ Unit: " + regions.size());

        return regions;
    }

    private Map<String, IRRatio> getIntensityRatios()
    {
        Map<String, IRRatio> intensityRatios = new HashMap<String, IRRatio>();
        IRRatio intensityRatio = new IRRatio();
        intensityRatio.name = "Copper Finder 12/10/14";
        intensityRatio.element = AtomicElement.Copper;
        intensityRatio.numerator = new ArrayList<Region>();
        intensityRatio.numerator.add(LibzUnitManager.getInstance().getRegionsManager().getObjects().get("7af7ec16-b1ea-46a4-bf6b-1dfab8318d33"));
        intensityRatio.numerator.add(LibzUnitManager.getInstance().getRegionsManager().getObjects().get("23j4209sdf8"));
        intensityRatio.denominator = new ArrayList<Region>();
        intensityRatio.denominator.add(LibzUnitManager.getInstance().getRegionsManager().getObjects().get("24d7c7d1-3abc-48b9-b07a-292ea44f0738"));

        IRRatio intensityRatio2 = new IRRatio();
        intensityRatio2.name = "Aluminum Finder 12/10/14";
        intensityRatio2.element = AtomicElement.Aluminum;
        intensityRatio2.numerator = new ArrayList<Region>();
        intensityRatio2.numerator.add(LibzUnitManager.getInstance().getRegionsManager().getObjects().get("24d7c7d1-3abc-48b9-b07a-292ea44f0738"));
        intensityRatio2.denominator = new ArrayList<Region>();
        intensityRatio2.denominator.add(LibzUnitManager.getInstance().getRegionsManager().getObjects().get("7af7ec16-b1ea-46a4-bf6b-1dfab8318d33"));

        intensityRatios.put("UNIQUE_ID_IR_1", intensityRatio);
        intensityRatios.put("UNIQUE_ID_IR_2", intensityRatio2);

        return intensityRatios;
    }

    private Map<String, Model> getCalibrationModels()
    {
        Map<String, Model> calModels = new HashMap();

        {
            Model calModel = new Model();
            calModel.name = "Copper Cal Model";
            calModel.standardList.add(LibzUnitManager.getInstance().getStandardsManager().getObjects().get("123456789"));
            calModel.standardList.add(LibzUnitManager.getInstance().getStandardsManager().getObjects().get("12"));
            calModel.standardList.add(LibzUnitManager.getInstance().getStandardsManager().getObjects().get("123"));

            {
                IRRatio irRatio = LibzUnitManager.getInstance().getIRRatiosManager().getObjects().get("UNIQUE_ID_IR_1");
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
            calModel.standardList.add(LibzUnitManager.getInstance().getStandardsManager().getObjects().get("123456789"));
            calModel.standardList.add(LibzUnitManager.getInstance().getStandardsManager().getObjects().get("12"));
            calModel.standardList.add(LibzUnitManager.getInstance().getStandardsManager().getObjects().get("123"));

            //unshot data
            calModel.standardList.add(LibzUnitManager.getInstance().getStandardsManager().getObjects().get("123456"));



            IRRatio irRatio = LibzUnitManager.getInstance().getIRRatiosManager().getObjects().get("UNIQUE_ID_IR_1");
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



        return calModels;
    }
}