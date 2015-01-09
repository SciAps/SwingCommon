package com.sciaps.common.swing.view;

import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.LibzTableUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class ShotDataJXCollapsiblePane extends JXCollapsiblePane
{
    public interface ShotDataJXCollapsiblePaneCallback
    {
        void shotDataSelected(String calibrationShotId);
    }

    private final ShotDataJXCollapsiblePaneCallback _callback;
    private JTable _calibrationShotsTable;
    private Vector _columnNames;
    private Vector _data;
    private DefaultTableModel _tableModel;
    private JTextField _filterTextField;
    private TableRowSorter<DefaultTableModel> _sorter;

    public ShotDataJXCollapsiblePane(Direction direction, ShotDataJXCollapsiblePaneCallback callback)
    {
        super(direction);

        _callback = callback;

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        _calibrationShotsTable = new JTable();
        _calibrationShotsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _calibrationShotsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _calibrationShotsTable.setFillsViewportHeight(true);
        _calibrationShotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _calibrationShotsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && _calibrationShotsTable.getModel().getRowCount() > 0 && _calibrationShotsTable.getSelectedRow() != -1)
                {
                    String calibrationShotId = LibzTableUtils.getSelectedObjectId(_calibrationShotsTable);
                    _callback.shotDataSelected(calibrationShotId);
                }
            }
        });

        _columnNames = new Vector();
        _columnNames.add("ID");
        _columnNames.add("Name");
        _columnNames.add("Standard");
        _columnNames.add("Timestamp");
        _data = new Vector();
        _tableModel = new DefaultTableModel();

        _sorter = new TableRowSorter<DefaultTableModel>(_tableModel);
        _calibrationShotsTable.setRowSorter(_sorter);

        refresh();

        JLabel title = new JLabel("Shot Data");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));

        add(title);

        JPanel filterForm = new JPanel(new SpringLayout());
        JLabel shotDataFilterLabel = new JLabel("Filter:", SwingConstants.TRAILING);
        filterForm.add(shotDataFilterLabel);

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

        shotDataFilterLabel.setLabelFor(_filterTextField);
        _filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, _filterTextField.getPreferredSize().height));
        filterForm.add(_filterTextField);

        SwingUtils.makeCompactGrid(filterForm, 1, 2, 6, 6, 6, 6);
        add(filterForm);

        JScrollPane scrollPane = new JScrollPane(_calibrationShotsTable);

        add(scrollPane);
    }

    public void refresh()
    {
        fillCalibrationShotsData();

        _tableModel.setDataVector(_data, _columnNames);
        _calibrationShotsTable.setModel(_tableModel);

        SwingUtils.refreshTable(_calibrationShotsTable);
        SwingUtils.fitTableToColumns(_calibrationShotsTable);

        _calibrationShotsTable.removeColumn(_calibrationShotsTable.getColumnModel().getColumn(0));
    }

    private void fillCalibrationShotsData()
    {
        if (LibzUnitManager.getInstance().getRegionsManager().getObjects() != null)
        {
            _data.clear();

            for (Map.Entry<String, CalibrationShot> entry : LibzUnitManager.getInstance().getCalibrationShots().entrySet())
            {
                Vector row = new Vector();

                row.add(entry.getKey());

                CalibrationShot calibrationShot = entry.getValue();
                Standard standardRepresentedByShotData = calibrationShot.standard;

                row.add(calibrationShot.displayName);
                row.add(standardRepresentedByShotData.name);
                row.add(calibrationShot.timeStamp.toString());

                _data.add(row);
            }
        }
    }

    private void filterTable()
    {
        try
        {
            final String regex = "(?i)" + _filterTextField.getText();
            RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter(regex, 1, 2, 3);
            _sorter.setRowFilter(rowFilter);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // If none of the expressions parse, don't update.
            Logger.getLogger(ShotDataJXCollapsiblePane.class.getName()).log(Level.INFO, null, e);
        }
    }
}