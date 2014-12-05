package com.sciaps.common.swing.listener;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 *
 * @author sgowen
 *
 * This class listens for changes made to the data in the table via the
 * TableCellEditor. When editing is started, the value of the cell is saved When
 * editing is stopped the new value is saved. When the old and new values are
 * different, then the provided Action is invoked.
 */
public final class TableCellListener implements PropertyChangeListener, Runnable
{
    private final JTable _table;
    private final Action _action;

    private int _row;
    private int _column;
    private Object _oldValue;
    private Object _newValue;

    public TableCellListener(JTable table, Action action)
    {
        _table = table;
        _action = action;
        _table.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        if ("tableCellEditor".equals(e.getPropertyName()))
        {
            if (_table.isEditing())
            {
                SwingUtilities.invokeLater(this);
            }
            else
            {
                _newValue = _table.getModel().getValueAt(_row, _column);

                if (!_newValue.equals(_oldValue))
                {
                    TableCellListener tcl = new TableCellListener(getTable(), getRow(), getColumn(), getOldValue(), getNewValue());

                    ActionEvent event = new ActionEvent(tcl, ActionEvent.ACTION_PERFORMED, "");
                    _action.actionPerformed(event);
                }
            }
        }
    }

    @Override
    public void run()
    {
        _row = _table.convertRowIndexToModel(_table.getEditingRow());
        _column = _table.convertColumnIndexToModel(_table.getEditingColumn());
        _oldValue = _table.getModel().getValueAt(_row, _column);
        _newValue = null;
    }

    public JTable getTable()
    {
        return _table;
    }

    public int getRow()
    {
        return _row;
    }

    public int getColumn()
    {
        return _column;
    }

    public Object getNewValue()
    {
        return _newValue;
    }

    public Object getOldValue()
    {
        return _oldValue;
    }

    private TableCellListener(JTable table, int row, int column, Object oldValue, Object newValue)
    {
        _table = table;
        _action = null;
        _row = row;
        _column = column;
        _oldValue = oldValue;
        _newValue = newValue;
    }
}