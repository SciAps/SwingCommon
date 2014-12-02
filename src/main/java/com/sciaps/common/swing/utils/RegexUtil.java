package com.sciaps.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sgowen on 4/4/14.
 */
public final class RegexUtil
{
    public static String findValue(String text, Pattern pattern, int groupIndex)
    {
        if (text == null)
        {
            return null;
        }

        Matcher matcher = pattern.matcher(text);

        if (matcher.find())
        {
            if (matcher.groupCount() > 0)
            {
                final String value = matcher.group(groupIndex);

                return value.length() > 0 ? value : null;
            }
        }

        return null;
    }

    public static String findValue(String text, String regexPattern, int groupIndex)
    {
        return findValue(text, Pattern.compile(regexPattern), groupIndex);
    }
}