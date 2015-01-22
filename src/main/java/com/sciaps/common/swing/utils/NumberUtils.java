package com.sciaps.common.swing.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sgowen
 */
public final class NumberUtils
{
    public static boolean isNumber(Object o)
    {
        try
        {
            String value = (String) o;
            Double.parseDouble(value);

            return true;
        }
        catch (Exception e)
        {
            Logger.getLogger(NumberUtils.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }

    public static double toDouble(Object o)
    {
        try
        {
            String value = (String) o;
            double number = Double.parseDouble(value);

            return number;
        }
        catch (Exception e)
        {
            Logger.getLogger(NumberUtils.class.getName()).log(Level.SEVERE, null, e);
            return 0;
        }
    }
    
    private NumberUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}