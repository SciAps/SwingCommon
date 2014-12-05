package com.sciaps.common.swing.global;

import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import java.util.List;
import java.util.Map;

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
    private Map<String, Standard> _standards;
    private Map<String, CalibrationShot> _calibrationShots;
    private List<LIBZPixelSpectrum> _libzPixelSpectra;
    private Map<String, Region> _regions;
    private Map<String, IRRatio> _intensityRatios;

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
        return _standards != null && _calibrationShots != null && _libzPixelSpectra != null && _calibrationShots.size() == _libzPixelSpectra.size() && _regions != null && _intensityRatios != null;
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

    public Map<String, Standard> getStandards()
    {
        return _standards;
    }

    public void setStandards(Map<String, Standard> standards)
    {
        _standards = standards;
    }

    public Map<String, CalibrationShot> getCalibrationShots()
    {
        return _calibrationShots;
    }

    public void setCalibrationShots(Map<String, CalibrationShot> spectraFiles)
    {
        _calibrationShots = spectraFiles;
    }

    public List<LIBZPixelSpectrum> getLIBZPixelSpectra()
    {
        return _libzPixelSpectra;
    }

    public void setLIBZPixelSpectra(List<LIBZPixelSpectrum> libzPixelSpectra)
    {
        _libzPixelSpectra = libzPixelSpectra;
    }

    public Map<String, Region> getRegions()
    {
        return _regions;
    }

    public void setRegions(Map<String, Region> regions)
    {
        _regions = regions;
    }

    public Map<String, IRRatio> getIntensityRatios()
    {
        return _intensityRatios;
    }

    public void setIntensityRatios(Map<String, IRRatio> intensityRatios)
    {
        _intensityRatios = intensityRatios;
    }

    private LibzUnitManager()
    {
        // Hide Constructor for Singleton
    }
}