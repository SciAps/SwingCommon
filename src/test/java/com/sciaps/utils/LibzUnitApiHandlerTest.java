package com.sciaps.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.MockWebserver;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.global.InstanceManager;
import java.util.HashMap;
import org.apache.commons.lang.math.DoubleRange;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.devsmart.miniweb.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
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
        region.wavelengthRange = new DoubleRange(380, 410);
        LibzUnitManager.getInstance().getRegions().put(java.util.UUID.randomUUID().toString(), region);

        IRRatio intensityRatio = new IRRatio();
        intensityRatio.name = "Copper Finder 10/22/14";
        intensityRatio.element = AtomicElement.Copper;
        intensityRatio.numerator = new double[][]
        {
            {
                29, 380, 400, 29, 470, 472
            },
            {
            }
        };
        intensityRatio.denominator = new double[][]
        {
            {
                13, 340, 351
            },
            {
            }
        };
        LibzUnitManager.getInstance().getIntensityRatios().put(java.util.UUID.randomUUID().toString(), intensityRatio);

        LibzUnitApiHandler libzUnitApiHandler = InstanceManager.getInstance().retrieveInstance(LibzUnitApiHandler.class);

        assertTrue(libzUnitApiHandler.pushToLibzUnit());
    }

    @Test
    public void testCreateRegion() throws Exception
    {
        Gson gson = new GsonBuilder().create();

        Region region = new Region();
        region.name = "C_193";
        region.wavelengthRange = new DoubleRange(193.2, 193.6);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpPost post = new HttpPost("http://localhost:9100/data/regions");

        String jsonstr = gson.toJson(region);
        StringEntity body = new StringEntity(jsonstr, "UTF8");
        body.setContentType("application/json");
        post.setEntity(body);

        CloseableHttpResponse response = httpclient.execute(post);
        assertNotNull(response);
        assertTrue(response.getStatusLine().getStatusCode() == 200);
    }
}