package com.sciaps.utils;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.MockWebserver;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.global.InstanceManager;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang.math.DoubleRange;
import org.devsmart.miniweb.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author sgowen
 */
public final class LibzUnitApiHandlerTest
{
    private static Server s_server;

    @BeforeClass
    public static void initMockWebserver() throws Exception
    {
        s_server = MockWebserver.init("mockdata", 9100);

        InstanceManager.getInstance().storeInstance(LibzUnitApiHandler.class, new HttpLibzUnitApiHandler());
        LibzUnitManager.getInstance().setIpAddress("localhost:9100");

        // Give the server time to fully initialize
        Thread.sleep(3000);
    }

    @AfterClass
    public static void stopMockWebserver()
    {
        s_server.shutdown();
    }

    @Test
    public void testLibzUnitConnect()
    {
        LibzUnitApiHandler libzUnitApiHandler = InstanceManager.getInstance().retrieveInstance(LibzUnitApiHandler.class);

        assertTrue(libzUnitApiHandler.connectToLibzUnit());
    }

    @Test
    public void testLibzUnitPull()
    {
        LibzUnitApiHandler libzUnitApiHandler = InstanceManager.getInstance().retrieveInstance(LibzUnitApiHandler.class);

        assertTrue(libzUnitApiHandler.pullFromLibzUnit());
    }

    @Test
    public void testLibzUnitPush() throws Exception
    {
        LibzUnitManager.getInstance().setStandards(new HashMap<String, Standard>());
        LibzUnitManager.getInstance().setRegions(new HashMap<String, Region>());
        LibzUnitManager.getInstance().setIntensityRatios(new HashMap<String, IRRatio>());

        Standard newStandard = new Standard();
        newStandard.name = "Al_2027";
        LibzUnitManager.getInstance().getStandards().put("123456789", newStandard);

        Region region = new Region();
        region.name = "Al_380-410";
        region.wavelengthRange = new DoubleRange(380, 410);
        LibzUnitManager.getInstance().getRegions().put(java.util.UUID.randomUUID().toString(), region);

        Region region2 = new Region();
        region2.name = "Cu_640-670";
        region2.wavelengthRange = new DoubleRange(640, 670);
        LibzUnitManager.getInstance().getRegions().put(java.util.UUID.randomUUID().toString(), region2);

        IRRatio intensityRatio = new IRRatio();
        intensityRatio.name = "Aluminum Finder 12/10/14";
        intensityRatio.element = AtomicElement.Copper;
        intensityRatio.numerator = new ArrayList<Region>();
        intensityRatio.numerator.add(region);
        intensityRatio.denominator = new ArrayList<Region>();
        intensityRatio.denominator.add(region2);

        LibzUnitManager.getInstance().getIntensityRatios().put(java.util.UUID.randomUUID().toString(), intensityRatio);

        LibzUnitApiHandler libzUnitApiHandler = InstanceManager.getInstance().retrieveInstance(LibzUnitApiHandler.class);

        assertTrue(libzUnitApiHandler.pushToLibzUnit());
    }
}