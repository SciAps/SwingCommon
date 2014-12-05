package com.sciaps.common.swing.utils;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author sgowen
 */
public final class CSVFileFilter extends FileFilter
{
    @Override
    public boolean accept(File f)
    {
        if (f.isDirectory())
        {
            return true;
        }

        String fileExtension = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1)
        {
            fileExtension = s.substring(i + 1).toLowerCase();
        }

        if (fileExtension != null)
        {
            return fileExtension.equalsIgnoreCase("csv");
        }

        return false;
    }

    @Override
    public String getDescription()
    {
        return "Spectrum as CSV";
    }
}