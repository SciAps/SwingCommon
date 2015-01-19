package com.sciaps.utils;

import com.devsmart.miniweb.Server;
import com.sciaps.common.swing.MockWebserver;
import com.sciaps.common.swing.global.InstanceManager;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
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
}