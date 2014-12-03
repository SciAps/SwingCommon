package com.sciaps.common.swing.libzunitapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.EmissionLine;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.model.IsAlive;
import com.sciaps.common.swing.model.SpectraFile;
import com.sciaps.common.swing.utils.DownloadUtils;
import com.sciaps.common.swing.utils.IOUtils;
import com.sciaps.common.swing.utils.JsonUtils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public boolean connectToLibzUnit()
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(LibzUnitManager.getInstance().getIpAddress());
        final String urlString = urlBaseString + "api/isAlive";

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
                if (isAlive != null)
                {
                    LibzUnitManager.getInstance().setLibzUnitUniqueIdentifier(isAlive.libzUnitUniqueIdentifier);

                    return true;
                }
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
    public boolean pullFromLibzUnit()
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(LibzUnitManager.getInstance().getIpAddress());

        Map<String, Standard> standards = getStandards(urlBaseString + "data/standards/all");
        LibzUnitManager.getInstance().setStandards(standards);

        List<SpectraFile> spectraFiles = getSpectraFiles(urlBaseString + "api/spectra");
        LibzUnitManager.getInstance().setSpectraFiles(spectraFiles);

        if (spectraFiles != null)
        {
            List<LIBZPixelSpectrum> libzPixelSpectra = new ArrayList<LIBZPixelSpectrum>();
            for (SpectraFile sf : spectraFiles)
            {
                LIBZPixelSpectrum libzPixelSpectum = getLIBZPixelSpectrum(urlBaseString + "api/spectra", sf.id);
                if (libzPixelSpectum == null)
                {
                    Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.WARNING, "LIBZPixelSpectrum retrieved via id: {0} was NULL! Continuing to download the other LIBZPixelSpectrum objects...", sf.id);
                }

                libzPixelSpectra.add(libzPixelSpectum);
            }

            LibzUnitManager.getInstance().setLIBZPixelSpectra(libzPixelSpectra);
        }

        Map<String, Region> regions = getRegions(urlBaseString + "data/regions");
        LibzUnitManager.getInstance().setRegions(regions);

        Map<String, IRRatio> intensityRatios = getIntensityRatios(urlBaseString + "data/intensityratios");
        LibzUnitManager.getInstance().setIntensityRatios(intensityRatios);

        return LibzUnitManager.getInstance().isValidAfterPull();
    }

    @Override
    public boolean pushToLibzUnit()
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(LibzUnitManager.getInstance().getIpAddress());
        if (postStandards(urlBaseString + "data/standards/all", LibzUnitManager.getInstance().getStandards()))
        {
            if (postRegions(urlBaseString + "data/regions", LibzUnitManager.getInstance().getRegions()))
            {
                if (postIntensityRatios(urlBaseString + "data/intensityratios", LibzUnitManager.getInstance().getIntensityRatios()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Map<String, Standard> getStandards(final String getStandardsUrlString)
    {
        String jsonResponse = DownloadUtils.downloadJson(getStandardsUrlString);
        Type type = new TypeToken<Map<String, Standard>>()
        {
        }.getType();
        Map<String, Standard> standards = JsonUtils.deserializeJsonIntoType(jsonResponse, type);

        System.out.println("# of Standards pulled from LIBZ Unit: " + standards.size());

        return standards;
    }

    @Override
    public List<SpectraFile> getSpectraFiles(final String getSpectraFilesUrlString)
    {
        String jsonResponse = DownloadUtils.downloadJson(getSpectraFilesUrlString);
        Type type = new TypeToken<List<SpectraFile>>()
        {
        }.getType();
        List<SpectraFile> spectraFiles = JsonUtils.deserializeJsonIntoType(jsonResponse, type);

        System.out.println("# of Spectra Files pulled from LIBZ Unit: " + spectraFiles.size());

        return spectraFiles;
    }

    @Override
    public LIBZPixelSpectrum getLIBZPixelSpectrum(final String getLIBZPixelSpectrumUrlString, final String spectraId)
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

    @Override
    public Map<String, Region> getRegions(final String getRegionsUrlString)
    {
        // *** BEGIN TEMPORARY UNTIL getRegions API call is implemented ***
        Map<String, Region> regions = new HashMap<String, Region>();

        int id = 0;
        for (Region r : sRegions)
        {
            try
            {
                Region region = new Region();
                region.wavelengthRange = new DoubleRange(r.wavelengthRange.getMinimumDouble(), r.wavelengthRange.getMaximumDouble());
                region.name = EmissionLine.parse(r.name.name);

                regions.put("" + id, region);

                id++;
            }
            catch (Exception ex)
            {
                Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return regions;
        // ***  END  TEMPORARY UNTIL getRegions API call is implemented ***
    }

    @Override
    public Map<String, IRRatio> getIntensityRatios(final String getIntensityRatiosUrlString)
    {
        // *** BEGIN TEMPORARY UNTIL getIntensityRatios API call is implemented ***
        Map<String, IRRatio> intensityRatios = new HashMap<String, IRRatio>();

        int id = 0;
        for (IRRatio ir : sIntensityRatios)
        {
            IRRatio intensityRatio = new IRRatio();
            intensityRatio.name = ir.name;
            intensityRatio.element = ir.element;
            intensityRatio.numerator = ir.numerator;
            intensityRatio.denominator = ir.denominator;

            intensityRatios.put("" + id, intensityRatio);

            id++;
        }

        return intensityRatios;
        // ***  END  TEMPORARY UNTIL getIntensityRatios API call is implemented ***
    }

    @Override
    public boolean postStandards(final String postStandardsUrlString, Map<String, Standard> standards)
    {
        try
        {
            URL url = new URL(postStandardsUrlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

            Type type = new TypeToken<Map<String, Standard>>()
            {
            }.getType();
            Gson gson = new GsonBuilder().create();
            gson.toJson(standards, type, out);

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

    @Override
    public boolean postRegions(final String postRegionsUrlString, Map<String, Region> regions)
    {
        // *** BEGIN TEMPORARY UNTIL getRegions API call is implemented ***
        sRegions.clear();

        for (Map.Entry entry : regions.entrySet())
        {
            Region r = (Region) entry.getValue();
            sRegions.add(r);
        }

        return true;
        // ***  END  TEMPORARY UNTIL getRegions API call is implemented ***
    }

    @Override
    public boolean postIntensityRatios(final String postIntensityRatiosUrlString, Map<String, IRRatio> intensityRatios)
    {
        // *** BEGIN TEMPORARY UNTIL getIntensityRatios API call is implemented ***
        sIntensityRatios.clear();

        for (Map.Entry entry : intensityRatios.entrySet())
        {
            IRRatio ir = (IRRatio) entry.getValue();
            sIntensityRatios.add(ir);
        }

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