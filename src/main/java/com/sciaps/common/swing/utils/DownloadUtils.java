package com.sciaps.utils;

import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.listener.DownloadListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sgowen
 */
public final class DownloadUtils
{
    private static final String EXTRACT_FILE_FROM_URL_REGEX = "^(https?|ftp|file)://[^/]+/(?:[^/]+/)*((?:[^/.]+\\.)+[^/.]+)$";

    public static String downloadJson(final String getJsonUrlString)
    {
        BufferedReader bufferedReader = null;

        try
        {
            URL url = new URL(getJsonUrlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(20000);

            bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder sb = new StringBuilder();

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                sb.append(inputLine);
            }

            return sb.toString();
        }
        catch (IOException e)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, e);

            return null;
        }
        finally
        {
            IOUtils.safeClose(bufferedReader);
        }
    }

    public static File downloadFileFromUrl(String urlString, DownloadListener downloadListener)
    {
        InputStream in = null;
        FileOutputStream fos = null;

        String fileName = RegexUtil.findValue(urlString, EXTRACT_FILE_FROM_URL_REGEX, 2);
        File downloadedFile = new File(fileName);

        boolean isDownloadSuccessful = false;

        try
        {
            URL url = new URL(urlString);

            in = new BufferedInputStream(url.openStream());
            fos = new FileOutputStream(fileName);

            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1)
            {
                fos.write(buf, 0, n);
                downloadListener.onBytesDownloaded(n);
            }

            IOUtils.safeClose(fos);

            isDownloadSuccessful = true;

            return downloadedFile;
        }
        catch (IOException e)
        {
            Logger.getLogger(DownloadUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        finally
        {
            IOUtils.safeClose(in);
            IOUtils.safeClose(fos);

            if (!isDownloadSuccessful)
            {
                downloadedFile.delete();
            }
        }

        return null;
    }

    public static int getFileSize(String urlString)
    {
        HttpURLConnection conn = null;
        try
        {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            return conn.getContentLength();
        }
        catch (IOException e)
        {
            Logger.getLogger(DownloadUtils.class.getName()).log(Level.SEVERE, null, e);
            return -1;
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }
}