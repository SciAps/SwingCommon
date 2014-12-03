package com.sciaps.common.swing.libzunitapi;

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
     * going to call all of the Get methods on the unit
     *
     * @return true if all the Get calls executed successfully
     */
    boolean pullFromLibzUnit();

    /**
     * This method is used to perform a mass push, which essentially means it is
     * going to call all of the Put methods on the unit
     *
     * @return true if all the Put calls executed successfully
     */
    boolean pushToLibzUnit();

    Map<String, Standard> getStandards(final String getStandardsUrlString);

    Map<String, SpectraFile> getSpectraFiles(final String getSpectraFilesUrlString);

    LIBZPixelSpectrum getLIBZPixelSpectrum(final String getLIBZPixelSpectrumUrlString, final String spectraId);

    Map<String, Region> getRegions(final String getRegionsUrlString);

    Map<String, IRRatio> getIntensityRatios(final String getIntensityRatiosUrlString);

    boolean putStandards(final String putStandardsUrlString, Map<String, Standard> standards);

    boolean putRegions(final String putRegionsUrlString, Map<String, Region> regions);

    boolean putIntensityRatios(final String putIntensityRatiosUrlString, Map<String, IRRatio> intensityRatios);
}