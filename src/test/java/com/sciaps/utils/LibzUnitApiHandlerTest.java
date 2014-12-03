package com.sciaps.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sciaps.common.data.EmissionLine;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.MockWebserver;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.global.InstanceManager;
import org.apache.commons.lang.math.DoubleRange;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.devsmart.miniweb.Server;
import org.junit.AfterClass;
import org.junit.Before;
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
    private LibzUnitApiHandler _libzUnitApiHandler;

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

    @Before
    public void setup()
    {
        _libzUnitApiHandler = InstanceManager.getInstance().retrieveInstance(LibzUnitApiHandler.class);
    }

    @Test
    public void testLibzUnitConnectPullAndPush()
    {
        assert (_libzUnitApiHandler.connectToLibzUnit());
        assert (_libzUnitApiHandler.pullFromLibzUnit());
        assert (_libzUnitApiHandler.pushToLibzUnit());
    }

    @Test
    public void testCreateRegion() throws Exception
    {
        Gson gson = new GsonBuilder().create();

        Region region = new Region();
        region.name = EmissionLine.parse("C_193");
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