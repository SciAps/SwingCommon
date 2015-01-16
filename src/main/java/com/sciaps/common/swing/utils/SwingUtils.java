package com.sciaps.common.swing.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class SwingUtils
{
    /**
     * Aligns the first <code>rows</code> * <code>cols</code> components of
     * <code>parent</code> in a grid. Each component in a column is as wide as
     * the maximum preferred width of the components in that column; height is
     * similarly determined for each row. The parent is made just big enough to
     * fit them all.
     *
     * @param parent a container that uses SpringLayout
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad)
    {
        SpringLayout layout;
        try
        {
            layout = (SpringLayout) parent.getLayout();
        }
        catch (ClassCastException e)
        {
            Logger.getLogger(SwingUtils.class.getName()).log(Level.SEVERE, "The Container must use SpringLayout.", e);
            return;
        }

        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++)
        {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++)
            {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++)
            {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }

            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++)
        {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++)
            {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++)
            {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }

            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    public static void enforceMinWidthForColumns(JTable table, int minWidth)
    {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++)
        {
            int width = minWidth;
            for (int row = 0; row < table.getRowCount(); row++)
            {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width, width);
            }

            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    public static void refreshTable(final JTable table)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                for (int column = 0; column < table.getColumnCount(); column++)
                {
                    TableColumn tableColumn = table.getColumnModel().getColumn(column);
                    int preferredWidth = tableColumn.getMinWidth();
                    int maxWidth = tableColumn.getMaxWidth();

                    JTableHeader th = table.getTableHeader();
                    TableColumnModel tcm = th.getColumnModel();

                    for (int x = 1; x < tcm.getColumnCount(); x++)
                    {
                        TableColumn tc = tcm.getColumn(x);
                        preferredWidth = Math.max(preferredWidth, tc.getWidth());
                    }

                    for (int row = 0; row < table.getRowCount(); row++)
                    {
                        TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                        Component c = table.prepareRenderer(cellRenderer, row, column);
                        int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                        preferredWidth = Math.max(preferredWidth, width);

                        //  We've exceeded the maximum width, no need to check other rows
                        if (preferredWidth >= maxWidth)
                        {
                            preferredWidth = maxWidth;
                            break;
                        }
                    }

                    tableColumn.setPreferredWidth(preferredWidth);
                }

                table.invalidate();
            }
        });
    }

    public static void fitTableToColumns(final JTable table)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                int totalColumnWidth = 0;
                for (int column = 0; column < table.getColumnCount(); column++)
                {
                    TableColumn tableColumn = table.getColumnModel().getColumn(column);
                    totalColumnWidth += tableColumn.getWidth();
                }

                table.setPreferredScrollableViewportSize(new Dimension(totalColumnWidth, table.getPreferredSize().height));

                table.invalidate();
            }
        });
    }

    public static void scrollTable(final JTable table, final int row, final int column)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(250);
                }
                catch (InterruptedException e)
                {
                    Logger.getLogger(SwingUtils.class.getName()).log(Level.SEVERE, null, e);
                };

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Rectangle bottomRect = table.getCellRect(row, column, true);
                        table.scrollRectToVisible(bottomRect);
                    }
                });
            }
        }.start();
    }

    public static void hideDialog(final Dialog dialog)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
    }

    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols)
    {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);

        return layout.getConstraints(c);
    }
}