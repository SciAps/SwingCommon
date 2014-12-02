package com.sciaps.common.swing.global;

import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.model.SpectraFile;
import java.util.List;

/**
 *
 * @author sgowen
 */
public final class LibzUnitManager
{
    public static final int NUM_ATOMIC_ELEMENTS = 118;
    private static final Object LOCK = new Object();

    private static LibzUnitManager instance;

    private String _ipAddress;
    private String _libzUnitUniqueIdentifier;
    private List<Standard> _standards;
    private List<SpectraFile> _spectraFiles;
    private List<LIBZPixelSpectrum> _libzPixelSpectra;
    private List<Region> _regions;
    private List<IRRatio> _intensityRatios;

    public static LibzUnitManager getInstance()
    {
        synchronized (LOCK)
        {
            if (instance == null)
            {
                instance = new LibzUnitManager();
            }

            return instance;
        }
    }

    public boolean isValidAfterPull()
    {
        return _standards != null && _spectraFiles != null && _libzPixelSpectra != null && _regions != null && _intensityRatios != null;
    }

    public String getIpAddress()
    {
        return _ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        _ipAddress = ipAddress;
    }

    public String getLibzUnitUniqueIdentifier()
    {
        return _libzUnitUniqueIdentifier;
    }

    public void setLibzUnitUniqueIdentifier(String libzUnitUniqueIdentifier)
    {
        _libzUnitUniqueIdentifier = libzUnitUniqueIdentifier;
    }

    public List<Standard> getStandards()
    {
        return _standards;
    }

    public void setStandards(List<Standard> standards)
    {
        _standards = standards;
    }

    public List<SpectraFile> getSpectraFiles()
    {
        return _spectraFiles;
    }

    public void setSpectraFiles(List<SpectraFile> spectraFiles)
    {
        _spectraFiles = spectraFiles;
    }

    public List<LIBZPixelSpectrum> getLIBZPixelSpectra()
    {
        return _libzPixelSpectra;
    }

    public void setLIBZPixelSpectra(List<LIBZPixelSpectrum> libzPixelSpectra)
    {
        _libzPixelSpectra = libzPixelSpectra;
    }

    public List<Region> getRegions()
    {
        return _regions;
    }

    public void setRegions(List<Region> regions)
    {
        _regions = regions;
    }

    public List<IRRatio> getIntensityRatios()
    {
        return _intensityRatios;
    }

    public void setIntensityRatios(List<IRRatio> intensityRatios)
    {
        _intensityRatios = intensityRatios;
    }

    private LibzUnitManager()
    {
        // Hide Constructor
    }
}