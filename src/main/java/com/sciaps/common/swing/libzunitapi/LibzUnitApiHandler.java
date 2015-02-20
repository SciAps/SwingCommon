package com.sciaps.common.swing.libzunitapi;

import com.sciaps.common.data.Instrument;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.webserver.ILaserController.RasterParams;

import java.io.IOException;
import java.util.Collection;
import java.util.List;


public interface LibzUnitApiHandler {

    Instrument connectToLibzUnit() throws IOException;
    void pushToLibzUnit() throws IOException;

    Collection<String> getAllIds(Class<? extends DBObj> classType) throws IOException;

    <T extends DBObj> T loadObject(Class<T> classType, String id) throws IOException;

    LIBZPixelSpectrum downloadShot(String testId, int shotNum) throws IOException;

    Collection<String> getTestsForStandard(String standardid) throws IOException;
    List<String> getTestsSince(long unixTimestamp) throws IOException;
    List<LIBZPixelSpectrum> rasterTest(RasterParams params) throws IOException;
}