package com.sciaps.common.swing.global;

import com.google.inject.Inject;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sgowen
 */
public final class LibzUnitManager {


    private String _libzUnitUniqueIdentifier;
    private Map<String, CalibrationShot> _calibrationShots;
    private Map<String, LIBZPixelSpectrum> _libzPixelSpectra;


}