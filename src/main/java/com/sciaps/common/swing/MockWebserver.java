package com.sciaps.common.swing;

import com.sciaps.common.swing.model.IsAlive;
import com.sciaps.common.webserver.*;
import org.devsmart.miniweb.Server;
import org.devsmart.miniweb.ServerBuilder;
import org.devsmart.miniweb.handlers.controller.Body;
import org.devsmart.miniweb.handlers.controller.Controller;
import org.devsmart.miniweb.handlers.controller.RequestMapping;
import org.devsmart.miniweb.utils.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MockWebserver
{
    @Controller
    public static class LIBZMockController
    {
        @RequestMapping(value = "isAlive", method = RequestMethod.GET)
        @Body
        public IsAlive handleIsAlive()
        {
            IsAlive isAlive = new IsAlive();
            isAlive.libzUnitUniqueIdentifier = "LIBZ_UNIT_UNIQUE_ID";

            return isAlive;
        }
    }

    public static Server init(final String baseDirPath, int portNumber) throws IOException
    {
        File baseDir = new File(baseDirPath);

        FSStandardsController fsStandardsController = new FSStandardsController(new File(baseDir, "standards.json"));
        FSRegionController fsRegionController = new FSRegionController(new File(baseDir, "regions.json"));
        FSIRatioController fsiRatioController = new FSIRatioController(new File(baseDir, "iratios.json"));
        FSModelController fsModelController = new FSModelController(new File(baseDir, "models.json"));
        FSCalibrationController fsCalibrationController = new FSCalibrationController(new File(baseDir, "calibrationShot.json"), new File(baseDir, "calibrationShots"));

        Server server = new ServerBuilder()
                .port(portNumber)
                .mapController("/api", new LIBZMockController())
                .mapController("/data", fsStandardsController, fsRegionController, fsiRatioController, fsModelController, fsCalibrationController)
                .create();

        server.start();

        return server;
    }

    public static void main(String[] args)
    {
        try
        {
            Server mockWebServer = init(args[0], 9000);

            System.out.println("Press the enter key to shut down the server...");

            Scanner exitInput = new Scanner(System.in);
            exitInput.nextLine();

            mockWebServer.shutdown();
        }
        catch (IOException e)
        {
            Logger.getLogger(MockWebserver.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}