package com.sciaps.common.swing.view;

import javax.swing.JTable;

/**
 *
 * @author sgowen
 */
public final class ImmutableTable extends JTable
{
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }
}