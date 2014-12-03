package com.sciaps.common.swing.view;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.listener.TableCellListener;
import com.sciaps.common.swing.utils.NumberUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author sgowen
 */
public final class RegionsPanel extends JPanel
{
    public interface RegionsPanelCallback
    {
        void onRegionDeleted(String regionName);
    }

    private final RegionsPanelCallback _callback;
    private JTable _regionsTable;
    private Vector _columnNames;
    private Vector _data;
    private DefaultTableModel _tableModel;
    private JTextField _filterTextField;
    private TableRowSorter<DefaultTableModel> _sorter;

    public RegionsPanel(RegionsPanelCallback callback)
    {
        _callback = callback;

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        _regionsTable = new JTable();
        _regionsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _regionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _regionsTable.setFillsViewportHeight(true);
        _regionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableCellListener tcl = new TableCellListener(_regionsTable, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TableCellListener tcl = (TableCellListener) e.getSource();

                System.out.println("Row   : " + tcl.getRow());
                System.out.println("Column: " + tcl.getColumn());
                System.out.println("Old   : " + tcl.getOldValue());
                System.out.println("New   : " + tcl.getNewValue());

                int rowIndexChanged = tcl.getRow();
                int columnIndexChanged = tcl.getColumn();
                TableModel model = _regionsTable.getModel();

                Object newValue = tcl.getNewValue();

                String regionChanged = (String) model.getValueAt(rowIndexChanged, 0);

                JTableHeader th = _regionsTable.getTableHeader();
                TableColumnModel tcm = th.getColumnModel();
                TableColumn tc = tcm.getColumn(columnIndexChanged);

                final String columnChanged = (String) tc.getHeaderValue();
                boolean isNewValueInvalid = false;
                if (columnChanged.equals("Name"))
                {
                    // Can't change Name value, because logic relies on it
                    isNewValueInvalid = true;
                }
                else if (columnChanged.equals("Element"))
                {
                    // Can't change Element value either, because it is tied to the name
                    isNewValueInvalid = true;
                }
                else if (columnChanged.equals("Min"))
                {
                    if (NumberUtils.isNumber(newValue))
                    {
                        double newPercentageValue = NumberUtils.toDouble(newValue);

                        // We could let the user change the Min value,
                        // but then they would want to change the name, which we don't allow, so...
                        isNewValueInvalid = true;
                    }
                    else
                    {
                        isNewValueInvalid = true;
                    }
                }
                else if (columnChanged.equals("Max"))
                {
                    if (NumberUtils.isNumber(newValue))
                    {
                        double newPercentageValue = NumberUtils.toDouble(newValue);

                        // We could let the user change the Max value,
                        // but then they would want to change the name, which we don't allow, so...
                        isNewValueInvalid = true;
                    }
                    else
                    {
                        isNewValueInvalid = true;
                    }
                }

                if (isNewValueInvalid)
                {
                    model.setValueAt(tcl.getOldValue(), rowIndexChanged, columnIndexChanged);
                }
            }
        });

        _columnNames = new Vector();
        _columnNames.add("Name");
        _columnNames.add("Element");
        _columnNames.add("Min");
        _columnNames.add("Max");
        _data = new Vector();
        _tableModel = new DefaultTableModel();

        _sorter = new TableRowSorter<DefaultTableModel>(_tableModel);
        _regionsTable.setRowSorter(_sorter);

        refresh();

        JLabel title = new JLabel("Regions");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));

        add(title);

        JPanel filterForm = new JPanel(new SpringLayout());
        JLabel standardsFilterLabel = new JLabel("Filter:", SwingConstants.TRAILING);
        filterForm.add(standardsFilterLabel);

        _filterTextField = new JTextField();
        _filterTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                filterTable();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                filterTable();
            }
        });

        standardsFilterLabel.setLabelFor(_filterTextField);
        _filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, _filterTextField.getPreferredSize().height));
        filterForm.add(_filterTextField);

        JButton deleteRegionButton = new JButton("Delete");
        deleteRegionButton.setAlignmentX(JButton.RIGHT_ALIGNMENT);
        deleteRegionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedRowIndex = _regionsTable.getSelectedRow();
                if (selectedRowIndex == -1)
                {
                    return;
                }

                String regionName = (String) _regionsTable.getModel().getValueAt(selectedRowIndex, 0);

                Object regionToRemoveId = null;
                for (Map.Entry entry : LibzUnitManager.getInstance().getRegions().entrySet())
                {
                    Region region = (Region) entry.getValue();
                    if (region.name.name.equals(regionName))
                    {
                        regionToRemoveId = entry.getKey();
                    }
                }

                if (regionToRemoveId != null)
                {
                    LibzUnitManager.getInstance().getRegions().remove(regionToRemoveId);
                    refresh();
                }

                if (_callback != null)
                {
                    _callback.onRegionDeleted(regionName);
                }
            }
        });

        filterForm.add(deleteRegionButton);

        SwingUtils.makeCompactGrid(filterForm, 1, 3, 6, 6, 6, 6);
        add(filterForm);

        JScrollPane scrollPane = new JScrollPane(_regionsTable);

        add(scrollPane);
    }

    public void refresh()
    {
        fillRegionsData();

        _tableModel.setDataVector(_data, _columnNames);
        _regionsTable.setModel(_tableModel);

        initElementColumn(_regionsTable.getColumnModel().getColumn(1));

        SwingUtils.refreshTable(_regionsTable);
        SwingUtils.fitTableToColumns(_regionsTable);
    }

    private void initElementColumn(TableColumn elementColumn)
    {
        //Set up the editor for the element cells.
        JComboBox comboBox = new JComboBox();
        for (int i = 1; i <= LibzUnitManager.NUM_ATOMIC_ELEMENTS; i++)
        {
            AtomicElement ae = AtomicElement.getElementByAtomicNum(i);
            comboBox.addItem(ae.symbol);
        }

        elementColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }

    private void fillRegionsData()
    {
        if (LibzUnitManager.getInstance().getRegions() != null)
        {
            _data.clear();

            for (Map.Entry entry : LibzUnitManager.getInstance().getRegions().entrySet())
            {
                Region region = (Region) entry.getValue();
                Vector row = new Vector();
                row.add(region.name.name);
                row.add(region.name.element.symbol);
                row.add(region.wavelengthRange.getMinimumDouble());
                row.add(region.wavelengthRange.getMaximumDouble());

                _data.add(row);
            }
        }
    }

    private void filterTable()
    {
        try
        {
            RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + _filterTextField.getText(), 0);
            _sorter.setRowFilter(rowFilter);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // If current expression doesn't parse, don't update.
            Logger.getLogger(RegionsPanel.class.getName()).log(Level.INFO, null, e);
        }
    }
}