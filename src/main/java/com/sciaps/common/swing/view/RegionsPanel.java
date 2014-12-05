package com.sciaps.common.swing.view;

import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.SwingUtils;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
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

        _regionsTable = new JTable()
        {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        _regionsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _regionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _regionsTable.setFillsViewportHeight(true);
        _regionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
        JLabel regionsFilterLabel = new JLabel("Filter:", SwingConstants.TRAILING);
        filterForm.add(regionsFilterLabel);

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

        regionsFilterLabel.setLabelFor(_filterTextField);
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
                    if (region.name.equals(regionName))
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

        SwingUtils.refreshTable(_regionsTable);
        SwingUtils.fitTableToColumns(_regionsTable);
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
                row.add(region.name);
                row.add(region.getElement().symbol);
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