package com.sciaps.common.swing.libzunitapi;

import com.sciaps.common.data.Instrument;
import com.sciaps.common.data.LIBZTest;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;

import java.io.IOException;
import java.util.Collection;
import java.util.List;


public interface LibzUnitApiHandler {

    Instrument connectToLibzUnit() throws IOException;
    void pushToLibzUnit() throws IOException;

    Collection<String> getAllIds(Class<? extends DBObj> classType) throws IOException;

    <T extends DBObj> T loadObject(Class<T> classType, String id) throws IOException;

    LIBZPixelSpectrum downloadShot(String shotId) throws IOException;

    Collection<LIBZTest> getTestsForStandard(String standardid) throws IOException;
    List<LIBZTest> getTestsSince(long unixTimestamp) throws IOException;



}