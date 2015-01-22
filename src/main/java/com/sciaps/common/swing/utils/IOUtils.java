package com.sciaps.common.swing.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author sgowen
 */
public final class IOUtils
{
    public interface DataProgressCallback
    {
        void onDataProgress(int bytesWritten);
    }

    public static void safeFlush(Flushable flushable)
    {
        if (flushable != null)
        {
            try
            {
                flushable.flush();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void safeClose(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String extractStringFromInputStream(InputStream inputStream) throws IOException
    {
        byte[] buffer = new byte[1024];

        StringBuilder sb = new StringBuilder();

        int bufferLength;
        while ((bufferLength = inputStream.read(buffer)) != -1)
        {
            sb.append(new String(buffer, 0, bufferLength));
        }

        return sb.toString();
    }

    public static byte[] toByteArray(InputStream in) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pumpStream(in, out, null);
        return out.toByteArray();
    }

    public static void writeByteArrayToFile(File file, byte[] bytes) throws IOException
    {
        FileOutputStream fout = null;
        try
        {
            fout = new FileOutputStream(file);
            fout.write(bytes);
        }
        finally
        {
            IOUtils.safeClose(fout);
        }
    }

    public static void copyFile(File src, File dest) throws IOException
    {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }
        IOUtils.safeClose(in);
        IOUtils.safeClose(out);
    }

    public static void pumpStream(InputStream in, OutputStream out, DataProgressCallback callback) throws IOException
    {
        byte[] buff = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buff, 0, buff.length)) != -1)
        {
            out.write(buff, 0, bytesRead);
            if (callback != null)
            {
                callback.onDataProgress(bytesRead);
            }
        }
        IOUtils.safeClose(out);
        IOUtils.safeClose(in);
    }

    private IOUtils()
    {
        // Hide Constructor For Static Utility Class
    }
}