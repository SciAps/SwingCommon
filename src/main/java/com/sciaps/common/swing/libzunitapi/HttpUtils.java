package com.sciaps.common.swing.libzunitapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sciaps.common.swing.listener.DownloadListener;
import com.sciaps.common.swing.utils.IOUtils;
import com.sciaps.common.swing.utils.RegexUtil;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sgowen
 */
public final class HttpUtils
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

    public static <T> boolean postJson(final String postJsonUrlString, Map<String, T> map)
    {
        try
        {
            URL url = new URL(postJsonUrlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

            Type type = new TypeToken<Map<String, T>>()
            {
            }.getType();
            Gson gson = new GsonBuilder().create();
            gson.toJson(map, type, out);

            IOUtils.safeClose(out);

            con.connect();

            return con.getResponseCode() == 200;
        }
        catch (IOException ex)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
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
            Logger.getLogger(HttpUtils.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(HttpUtils.class.getName()).log(Level.SEVERE, null, e);
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