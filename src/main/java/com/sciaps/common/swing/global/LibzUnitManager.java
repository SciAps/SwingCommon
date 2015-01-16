package com.sciaps.common.swing.global;

import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import java.util.HashMap;
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

    private final MutableObjectsManager<Standard> _standardsManager;
    private final MutableObjectsManager<Region> _regionsManager;
    private final MutableObjectsManager<IRRatio> _irRatiosManager;
    private final MutableObjectsManager<Model> _modelsManager;

    private String _ipAddress;
    private String _libzUnitUniqueIdentifier;
    private Map<String, CalibrationShot> _calibrationShots;
    private Map<String, LIBZPixelSpectrum> _libzPixelSpectra;

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
        return _standardsManager.isValid()
                && _regionsManager.isValid()
                && _irRatiosManager.isValid()
                && _modelsManager.isValid()
                && _calibrationShots != null
                && _libzPixelSpectra != null;
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

    public MutableObjectsManager<Standard> getStandardsManager()
    {
        return _standardsManager;
    }

    public Map<String, CalibrationShot> getCalibrationShots()
    {
        return _calibrationShots;
    }

    public void setCalibrationShots(Map<String, CalibrationShot> spectraFiles)
    {
        _calibrationShots = spectraFiles;
    }

    public Map<String, LIBZPixelSpectrum> getLIBZPixelSpectra()
    {
        return _libzPixelSpectra;
    }

    public void setLIBZPixelSpectra(Map<String, LIBZPixelSpectrum> libzPixelSpectra)
    {
        _libzPixelSpectra = libzPixelSpectra;
    }

    public MutableObjectsManager<Region> getRegionsManager()
    {
        return _regionsManager;
    }

    public MutableObjectsManager<IRRatio> getIRRatiosManager()
    {
        return _irRatiosManager;
    }

    public MutableObjectsManager<Model> getModelsManager()
    {
        return _modelsManager;
    }

    private LibzUnitManager()
    {
        // Hide Constructor for Singleton
        _standardsManager = new MutableObjectsManager();
        _regionsManager = new MutableObjectsManager();
        _irRatiosManager = new MutableObjectsManager();
        _modelsManager = new MutableObjectsManager();
        _calibrationShots = new HashMap();
        _libzPixelSpectra = new HashMap();
    }
}