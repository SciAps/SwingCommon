package com.sciaps.utils;

import com.devsmart.miniweb.Server;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sciaps.common.swing.MockWebserver;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author sgowen
 */
public final class LibzUnitApiHandlerTest
{
    private static Server s_server;
    private static Injector mInjector;

    private static HttpLibzUnitApiHandler httpHandler;

    @BeforeClass
    public static void initMockWebserver() throws Exception
    {
        s_server = MockWebserver.init("mockdata", 9100);
        mInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(LibzUnitApiHandler.class).to(HttpLibzUnitApiHandler.class);
            }
        });

        httpHandler = mInjector.getInstance(HttpLibzUnitApiHandler.class);
        httpHandler.setIpAddress("localhost:9100");

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
        assertTrue(httpHandler.connectToLibzUnit());
    }

    @Test
    public void testLibzUnitPull() throws IOException {
        httpHandler.pullFromLibzUnit();
    }
}