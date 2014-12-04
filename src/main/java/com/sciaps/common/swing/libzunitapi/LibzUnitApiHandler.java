package com.sciaps.common.swing.libzunitapi;

import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.model.SpectraFile;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sgowen
 */
public interface LibzUnitApiHandler
{
    /**
     * This is ALWAYS the first method to call when beginning interactions with
     * the LIBZ Unit. If it is successful, an IsAlive object will be returned
     * that contains a unique ID for the LIBZ Unit. This unique ID will be used
     * in all additional API calls
     *
     * @return true if a valid IsAlive response was received
     */
    boolean connectToLibzUnit();

    /**
     * This method is used to perform a mass pull, which essentially means it is
     * going to call all of the GET methods on the unit
     *
     * @return true if all the GET calls executed successfully
     */
    boolean pullFromLibzUnit();

    /**
     * This method is used to perform a mass push, which essentially means it is
     * going to call all of the POST methods on the unit
     *
     * @return true if all the POST calls executed successfully
     */
    boolean pushToLibzUnit();

    Map<String, Standard> getStandards(final String getStandardsUrlString);

    Map<String, CalibrationShot> getCalibrationShots(final String getCalibrationShotsUrlString);

    LIBZPixelSpectrum getLIBZPixelSpectrum(final String getLIBZPixelSpectrumUrlString, final Object shotId);

    Map<String, Region> getRegions(final String getRegionsUrlString);

    Map<String, IRRatio> getIntensityRatios(final String getIntensityRatiosUrlString);

    boolean postStandards(final String putStandardsUrlString, Map<String, Standard> standards);

    boolean postRegions(final String putRegionsUrlString, Map<String, Region> regions);

    boolean postIntensityRatios(final String putIntensityRatiosUrlString, Map<String, IRRatio> intensityRatios);
}