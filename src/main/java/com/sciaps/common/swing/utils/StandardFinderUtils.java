package com.sciaps.common.swing.utils;

import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.global.LibzUnitManager;
import java.util.Map;

/**
 *
 * @author sgowen
 */
public final class StandardFinderUtils
{
    public static String retreiveIdForStandard(Object standard)
    {
        for (Map.Entry<String, Standard> entry : LibzUnitManager.getInstance().getStandardsManager().getObjects().entrySet())
        {
            if (entry.getValue() == standard)
            {
                return entry.getKey();
            }
        }

        return null;
    }

    private StandardFinderUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}