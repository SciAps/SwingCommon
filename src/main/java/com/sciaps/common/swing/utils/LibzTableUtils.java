package com.sciaps.common.swing.utils;

import javax.swing.JTable;

/**
 *
 * @author sgowen
 */
public final class LibzTableUtils
{
    public static String getSelectedObjectId(JTable table)
    {
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String objectId = (String) table.getModel().getValueAt(row, 0);

        return objectId;
    }

    private LibzTableUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}