package com.sciaps.common.swing.view;

import com.google.inject.Inject;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.objtracker.ObjTracker;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.LibzTableUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
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

    class CalibrationShotTableModel extends AbstractTableModel {

        private ArrayList<CalibrationShot> mShots = new ArrayList<CalibrationShot>();

        @Override
        public int getRowCount() {
            return mShots.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CalibrationShot shot = mShots.get(rowIndex);
            switch(columnIndex) {
                case 0:
                    return shot.displayName;
                case 1:
                    return shot.standard.name;
                case 2:
                    return shot.timeStamp;

                default:
                    return null;
            }
        }
    }

    private final ShotDataJXCollapsiblePaneCallback _callback;
    private JTable _calibrationShotsTable;

    private CalibrationShotTableModel _tableModel;
    private JTextField _filterTextField;
    private TableRowSorter<CalibrationShotTableModel> _sorter;

    @Inject
    LibzUnitManager mUnitManager;

    @Inject
    DBObjTracker mObjTracker;

    public ShotDataJXCollapsiblePane(Direction direction, ShotDataJXCollapsiblePaneCallback callback)
    {
        super(direction);

        _callback = callback;

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        _tableModel = new CalibrationShotTableModel();
        _calibrationShotsTable = new JTable(_tableModel);
        _calibrationShotsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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


        _sorter = new TableRowSorter<CalibrationShotTableModel>(_tableModel);
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
        if (!isCollapsed())
        {
            SwingUtils.fitTableToColumns(_calibrationShotsTable);
        }
    }

    private void fillCalibrationShotsData()
    {
        _tableModel.mShots.clear();
        Iterator<CalibrationShot> it = mObjTracker.getAllObjectsOfType(CalibrationShot.class);
        while(it.hasNext()) {
            _tableModel.mShots.add(it.next());
        }
        _tableModel.fireTableDataChanged();
    }

    private void filterTable()
    {
        try
        {
            final String regex = "(?i)" + _filterTextField.getText();
            RowFilter<CalibrationShotTableModel, Object> rowFilter = RowFilter.regexFilter(regex, 1, 2, 3);
            _sorter.setRowFilter(rowFilter);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // If none of the expressions parse, don't update.
            Logger.getLogger(ShotDataJXCollapsiblePane.class.getName()).log(Level.INFO, null, e);
        }
    }
}