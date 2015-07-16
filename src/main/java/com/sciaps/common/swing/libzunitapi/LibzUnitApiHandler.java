package com.sciaps.common.swing.libzunitapi;

import com.sciaps.common.Exceptions.LaserNotArmedException;
import com.sciaps.common.data.Instrument;
import com.sciaps.common.data.LIBZTest;
import com.sciaps.common.objtracker.DBObj;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.webserver.ILaserController;
import com.sciaps.common.webserver.ILaserController.RasterParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public interface LibzUnitApiHandler {

    public static ArrayList<String> mFactoryCalModelIDList = new ArrayList<String>();
    public static ArrayList<String> mFactoryStandardIDList = new ArrayList<String>();
    public static ArrayList<String> mFactoryTestIDList = new ArrayList<String>();
    public static ArrayList<String> mFactoryRegionIDList = new ArrayList<String>();

    void setFactoryLockDownMode(boolean val);

    Instrument connectToLibzUnit() throws IOException;
    ILaserController.RasterParams getDefaultParams() throws IOException;
    void pushToLibzUnit() throws IOException;

    Collection<String> getAllIds(Class<? extends DBObj> classType) throws IOException;

    <T extends DBObj> T loadObject(Class<T> classType, String id) throws IOException;
    <T extends DBObj> void createNewObject(T newObj) throws IOException;

    void uploadShot(String testId, int shotNum, LIBZPixelSpectrum data) throws IOException;

    LIBZPixelSpectrum downloadShot(String testId, int shotNum) throws IOException;

    Collection<String> getTestsForStandard(String standardid) throws IOException;
    List<String> getTestsSince(long unixTimestamp) throws IOException;
    List<LIBZPixelSpectrum> rasterTest(RasterParams params) throws IOException, LaserNotArmedException;
    String takeRasterTest(RasterParams params) throws IOException, LaserNotArmedException;
    float getArgonLevel() throws IOException;
}