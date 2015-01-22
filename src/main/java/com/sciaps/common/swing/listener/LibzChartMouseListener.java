package com.sciaps.common.swing.listener;

import com.sciaps.common.swing.utils.AtomicElementUtils;
import com.sciaps.common.swing.utils.RegionMarkerUtils;
import java.awt.Component;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Layer;

/**
 *
 * @author sgowen
 */
public final class LibzChartMouseListener implements ChartMouseListener
{
    public interface LibzChartMouseListenerCallback
    {
        void addRegion(String regionName, double wavelengthMin, double wavelengthMax, Marker... associatedMarkers);
    }

    private final List<ValueMarker> _valueMarkersAddedToChart;
    private final ChartPanel _chartPanel;
    private final JFreeChart _jFreeChart;
    private final Component _parentComponent;
    private final LibzChartMouseListenerCallback _callback;

    public LibzChartMouseListener(ChartPanel chartPanel, JFreeChart jFreeChart, Component parentComponent, LibzChartMouseListenerCallback callback)
    {
        _valueMarkersAddedToChart = new ArrayList<ValueMarker>();
        _chartPanel = chartPanel;
        _jFreeChart = jFreeChart;
        _parentComponent = parentComponent;
        _callback = callback;
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent event)
    {
        Point2D p = event.getTrigger().getPoint();
        Rectangle2D plotArea = _chartPanel.getScreenDataArea();
        XYPlot plot = (XYPlot) _jFreeChart.getPlot();
        double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());

        ValueMarker marker = RegionMarkerUtils.createValueMarkerForValue(chartX);

        if (_valueMarkersAddedToChart.size() % 2 == 0)
        {
            _valueMarkersAddedToChart.add(marker);
            plot.addDomainMarker(marker);
        }
        else
        {
            String[] elements = AtomicElementUtils.getArrayOfAtomicElementSymbols();
            String element = (String) JOptionPane.showInputDialog(_parentComponent, "Please specify an element for this region:", "Elements", JOptionPane.INFORMATION_MESSAGE, null, elements, null);

            if (element != null)
            {
                final IntervalMarker bst = RegionMarkerUtils.createIntervalMarkerForElementValues(element, marker.getValue(), _valueMarkersAddedToChart.get(0).getValue());

                plot.addDomainMarker(marker);
                plot.addDomainMarker(bst, Layer.BACKGROUND);

                if (_callback != null)
                {
                    _callback.addRegion(bst.getLabel(), bst.getStartValue(), bst.getEndValue(), _valueMarkersAddedToChart.get(0), marker, bst);
                }

                _valueMarkersAddedToChart.clear();
            }
        }
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent event)
    {
        // Empty
    }
}