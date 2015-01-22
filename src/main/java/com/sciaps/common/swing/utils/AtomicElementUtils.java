package com.sciaps.common.swing.utils;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.swing.global.LibzUnitManager;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sgowen
 */
public final class AtomicElementUtils
{
    public static String[] getArrayOfAtomicElementSymbols()
    {
        List<String> elements = new ArrayList<String>();
        for (int i = 1; i <= LibzUnitManager.NUM_ATOMIC_ELEMENTS; i++)
        {
            AtomicElement ae = AtomicElement.getElementByAtomicNum(i);
            elements.add(ae.symbol);
        }

        String[] elementsArray = new String[elements.size()];
        elementsArray = elements.toArray(elementsArray);

        return elementsArray;
    }

    private AtomicElementUtils()
    {
        // Hide Constructor For Static Utility Class
    }
}