package com.sciaps.common.swing.utils;

import com.sciaps.common.swing.model.CSV;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author sgowen
 */
public final class CSVReader
{
    /**
     * Read a CSV file from the provided InputStream. A BufferedReader is
     * created from the InputStream and is guaranteed to be closed Closing the
     * provided InputStream afterwards is entirely up to you.
     *
     * @param is
     * @return
     * @throws IOException, so that the caller can appropriately handle
     * (logging, escalation, etc.)
     */
    public static CSV readCSVFromInputStream(InputStream is) throws IOException
    {
        CSV csv = new CSV();
        csv.csvRows = new ArrayList<String[]>();

        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new InputStreamReader(is));

            String strLine;
            while ((strLine = br.readLine()) != null)
            {
                String[] columns = strLine.split(",");
                csv.csvRows.add(columns);
            }
        }
        finally
        {
            IOUtils.safeClose(br);
        }

        return csv;
    }

    private CSVReader()
    {
        // Hide Constructor For Static Utility Class
    }
}