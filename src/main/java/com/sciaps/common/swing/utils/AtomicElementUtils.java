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

        AtomicElement[] allElements = AtomicElement.values();
        String[] elementsArray = new String[allElements.length];
        for(int i=0;i<allElements.length;i++) {
            elementsArray[i] = allElements[i].symbol;
        }

        return elementsArray;
    }

    private AtomicElementUtils()
    {
        // Hide Constructor For Static Utility Class
    }
}