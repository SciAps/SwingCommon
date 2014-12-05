package com.sciaps.common.swing.view;

import com.sciaps.common.data.EmissionLine;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.view.RegionsPanel.RegionsPanelCallback;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import org.apache.commons.lang.math.DoubleRange;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jfree.chart.plot.Marker;

/**
 *
 * @author sgowen
 */
public final class RegionsJXCollapsiblePane extends JXCollapsiblePane
{
    public interface RegionsJXCollapsiblePaneCallback
    {
        void removeChartMarkers(Marker[] regionMarkers);
    }

    private final Map<String, Marker[]> regionAndAssociatedMarkersMap = new HashMap<String, Marker[]>();
    private final RegionsJXCollapsiblePaneCallback _callback;
    private final RegionsPanel _regionsPanel;

    public RegionsJXCollapsiblePane(Direction direction, RegionsJXCollapsiblePaneCallback callback)
    {
        super(direction);

        _callback = callback;

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        _regionsPanel = new RegionsPanel(new RegionsPanelCallback()
        {
            @Override
            public void onRegionDeleted(String regionName)
            {
                Marker[] markersAssociatedWithRegion = regionAndAssociatedMarkersMap.get(regionName);

                if (markersAssociatedWithRegion != null)
                {
                    _callback.removeChartMarkers(markersAssociatedWithRegion);
                }
            }
        });
        _regionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(_regionsPanel);
    }

    public void refresh()
    {
        _regionsPanel.refresh();
    }

    public void addRegion(String regionName, double wavelengthMin, double wavelengthMax, Marker... associatedMarkers)
    {
        try
        {
            Region region = new Region();
            region.wavelengthRange = new DoubleRange(wavelengthMin, wavelengthMax);
            region.name = regionName;

            LibzUnitManager.getInstance().getRegions().put(java.util.UUID.randomUUID().toString(), region);

            Marker[] markers = new Marker[associatedMarkers.length];
            System.arraycopy(associatedMarkers, 0, markers, 0, associatedMarkers.length);

            regionAndAssociatedMarkersMap.put(region.name, markers);

            refresh();
        }
        catch (Exception ex)
        {
            Logger.getLogger(RegionsJXCollapsiblePane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}