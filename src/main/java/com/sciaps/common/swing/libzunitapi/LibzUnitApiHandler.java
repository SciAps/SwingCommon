package com.sciaps.common.swing.libzunitapi;

import com.sciaps.common.data.Instrument;
import com.sciaps.common.data.LIBZTest;
import com.sciaps.common.data.LaserShot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author sgowen
 */
public interface LibzUnitApiHandler
{

    Instrument connectToLibzUnit() throws IOException;

    void pullFromLibzUnit() throws IOException;

    void pushToLibzUnit() throws IOException;


    LIBZPixelSpectrum downloadShot(String shotId) throws IOException;

    Collection<LIBZTest> getTestsForStandard(String standardid) throws IOException;



}