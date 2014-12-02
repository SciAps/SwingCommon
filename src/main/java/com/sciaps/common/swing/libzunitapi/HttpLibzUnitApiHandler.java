package com.sciaps.common.swing.libzunitapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.EmissionLine;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.model.IsAlive;
import com.sciaps.model.SpectraFile;
import com.sciaps.utils.IOUtils;
import com.sciaps.utils.JsonUtils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.lang.math.DoubleRange;

/**
 *
 * @author sgowen
 */
public final class HttpLibzUnitApiHandler implements LibzUnitApiHandler
{
    @Override
    public boolean connectToLibzUnit(LibzUnitManager libzUnitManager)
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(libzUnitManager.getIpAddress());
        final String urlString = urlBaseString + "isAlive";

        BufferedReader bufferedReader = null;

        try
        {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(10000);

            bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder sb = new StringBuilder();

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                sb.append(inputLine);
            }

            String jsonResponse = sb.toString();
            if (jsonResponse != null)
            {
                Gson gson = new GsonBuilder().create();

                IsAlive isAlive = gson.fromJson(sb.toString(), IsAlive.class);
                libzUnitManager.setLibzUnitUniqueIdentifier(isAlive.libzUnitUniqueIdentifier);

                return true;
            }
        }
        catch (IOException e)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        finally
        {
            IOUtils.safeClose(bufferedReader);
        }

        return false;
    }

    @Override
    public boolean pullFromLibzUnit(LibzUnitManager libzUnitManager)
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(libzUnitManager.getIpAddress());

        List<Standard> standards = getStandards(urlBaseString + "standards");
        libzUnitManager.setStandards(standards);

        List<SpectraFile> spectraFiles = getSpectraFiles(urlBaseString + "spectra");
        libzUnitManager.setSpectraFiles(spectraFiles);

        if (spectraFiles != null)
        {
            List<LIBZPixelSpectrum> libzPixelSpectra = new ArrayList<LIBZPixelSpectrum>();

            for (SpectraFile sf : spectraFiles)
            {
                LIBZPixelSpectrum libzPixelSpectum = getLIBZPixelSpectrum(urlBaseString + "spectra", sf.id);
                if (libzPixelSpectum == null)
                {
                    Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.WARNING, "LIBZPixelSpectrum retrieved via id: {0} was NULL! Continuing to download the other LIBZPixelSpectrum objects...", sf.id);
                }

                libzPixelSpectra.add(libzPixelSpectum);
            }

            libzUnitManager.setLIBZPixelSpectra(libzPixelSpectra);
        }

        List<Region> regions = getRegions(urlBaseString + "regions");
        libzUnitManager.setRegions(regions);

        List<IRRatio> intensityRatios = getIntensityRatios(urlBaseString + "intensityratios");
        libzUnitManager.setIntensityRatios(intensityRatios);

        return libzUnitManager.isValidAfterPull();
    }

    @Override
    public boolean pushToLibzUnit(LibzUnitManager libzUnitManager)
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(libzUnitManager.getIpAddress());
        if (putStandards(urlBaseString + "standards", libzUnitManager.getStandards()))
        {
            if (putRegions(urlBaseString + "regions", libzUnitManager.getRegions()))
            {
                if (putIntensityRatios(urlBaseString + "intensityratios", libzUnitManager.getIntensityRatios()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static List<Standard> getStandards(final String getStandardsUrlString)
    {
        String jsonResponse = downloadJson(getStandardsUrlString);
        List<Standard> standards = JsonUtils.deserializeJsonIntoListOfType(jsonResponse, Standard[].class);

        System.out.println("# of Standards pulled from LIBZ Unit: " + standards.size());

        return standards;
    }

    private static List<SpectraFile> getSpectraFiles(final String getSpectraFilesUrlString)
    {
        String jsonResponse = downloadJson(getSpectraFilesUrlString);
        List<SpectraFile> spectraFiles = JsonUtils.deserializeJsonIntoListOfType(jsonResponse, SpectraFile[].class);

        System.out.println("# of Spectra Files pulled from LIBZ Unit: " + spectraFiles.size());

        return spectraFiles;
    }

    private static LIBZPixelSpectrum getLIBZPixelSpectrum(final String getLIBZPixelSpectrumUrlString, final String spectraId)
    {
        JsonReader jsonReader = null;

        try
        {
            URL url = new URL(getLIBZPixelSpectrumUrlString + "/" + spectraId);

            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            GZIPInputStream gzis = new GZIPInputStream(bis);
            jsonReader = new JsonReader(new InputStreamReader(gzis));

            Gson gson = new GsonBuilder().create();
            final LIBZPixelSpectrum.SerializationObj obj = gson.fromJson(jsonReader, LIBZPixelSpectrum.SerializationObj.class);
            if (obj == null)
            {
                return null;
            }

            LIBZPixelSpectrum libzPixelSpectrum = new LIBZPixelSpectrum(obj);

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

    private static List<Region> getRegions(final String getRegionsUrlString)
    {
        // *** BEGIN TEMPORARY UNTIL getRegions API call is implemented ***
        List<Region> regions = new ArrayList<Region>();
        for (Region r : sRegions)
        {
            try
            {
                Region region = new Region();
                region.wavelengthRange = new DoubleRange(r.wavelengthRange.getMinimumDouble(), r.wavelengthRange.getMaximumDouble());
                region.name = EmissionLine.parse(r.name.name);

                regions.add(region);
            }
            catch (Exception ex)
            {
                Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return regions;
        // ***  END  TEMPORARY UNTIL getRegions API call is implemented ***
    }

    private static List<IRRatio> getIntensityRatios(final String getIntensityRatiosUrlString)
    {
        // *** BEGIN TEMPORARY UNTIL getIntensityRatios API call is implemented ***
        List<IRRatio> intensityRatios = new ArrayList<IRRatio>();
        for (IRRatio ir : sIntensityRatios)
        {
            IRRatio intensityRatio = new IRRatio();
            intensityRatio.name = ir.name;
            intensityRatio.element = ir.element;
            intensityRatio.numerator = ir.numerator;
            intensityRatio.denominator = ir.denominator;

            intensityRatios.add(intensityRatio);
        }

        return intensityRatios;
        // ***  END  TEMPORARY UNTIL getIntensityRatios API call is implemented ***
    }

    private static String downloadJson(final String getJsonUrlString)
    {
        BufferedReader bufferedReader = null;

        try
        {
            URL url = new URL(getJsonUrlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(20000);

            bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder sb = new StringBuilder();

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                sb.append(inputLine);
            }

            return sb.toString();
        }
        catch (IOException e)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, e);

            return null;
        }
        finally
        {
            IOUtils.safeClose(bufferedReader);
        }
    }

    private static boolean putStandards(final String putStandardsUrlString, List<Standard> standards)
    {
        try
        {
            URL url = new URL(putStandardsUrlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

            Standard[] standardsArray = standards.toArray(new Standard[standards.size()]);
            Gson gson = new GsonBuilder().create();
            gson.toJson(standardsArray, Standard[].class, out);

            IOUtils.safeClose(out);

            con.connect();

            return con.getResponseCode() == 200;
        }
        catch (IOException ex)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private static boolean putRegions(final String putRegionsUrlString, List<Region> regions)
    {
        // *** BEGIN TEMPORARY UNTIL getRegions API call is implemented ***
        sRegions.clear();
        sRegions.addAll(regions);

        return true;
        // ***  END  TEMPORARY UNTIL getRegions API call is implemented ***
    }

    private static boolean putIntensityRatios(final String putIntensityRatiosUrlString, List<IRRatio> intensityRatios)
    {
        // *** BEGIN TEMPORARY UNTIL getIntensityRatios API call is implemented ***
        sIntensityRatios.clear();
        sIntensityRatios.addAll(intensityRatios);

        return true;
        // ***  END  TEMPORARY UNTIL getIntensityRatios API call is implemented ***
    }

    private static String getLibzUnitApiBaseUrl(String ipAddress)
    {
        final String urlBaseString = "http://" + ipAddress + "/";

        return urlBaseString;
    }

    // *** BEGIN TEMPORARY UNTIL getRegions/getIntensityRatios API calls are implemented ***
    private static final List<Region> sRegions = new ArrayList<Region>();
    private static final List<IRRatio> sIntensityRatios = new ArrayList<IRRatio>();

    static
    {
        try
        {
            Region region = new Region();
            region.wavelengthRange = new DoubleRange(380, 400);
            region.name = EmissionLine.parse("Cu_380-400");

            sRegions.add(region);

            IRRatio intensityRatio = new IRRatio();
            intensityRatio.name = "Copper Finder 10/22/14";
            intensityRatio.element = AtomicElement.Copper;
            intensityRatio.numerator = new double[][]
            {
                {
                    29, 380, 400, 29, 470, 472
                },
                {
                }
            };
            intensityRatio.denominator = new double[][]
            {
                {
                    13, 340, 351
                },
                {
                }
            };

            sIntensityRatios.add(intensityRatio);
        }
        catch (Exception ex)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // ***  END  TEMPORARY UNTIL getRegions/getIntensityRatios API calls are implemented ***
}