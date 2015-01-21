package com.sciaps.common.swing.listener;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.swing.global.LibzUnitManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
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
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

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

        ValueMarker marker = new ValueMarker(chartX);
        marker.setPaint(Color.RED);

        if (_valueMarkersAddedToChart.size() % 2 == 0)
        {
            _valueMarkersAddedToChart.add(marker);
            plot.addDomainMarker(marker);
        }
        else
        {
            String[] elements = getArrayOfElements();
            String element = (String) JOptionPane.showInputDialog(_parentComponent, "Please specify an element for this region:", "Elements", JOptionPane.INFORMATION_MESSAGE, null, elements, null);

            if (element != null)
            {
                double firstValue = Math.min(marker.getValue(), _valueMarkersAddedToChart.get(0).getValue());
                double secondValue = Math.max(marker.getValue(), _valueMarkersAddedToChart.get(0).getValue());

                String regionName = element + "_" + (int)firstValue + "-" + (int)secondValue;

                final Color c = new Color(255, 60, 24, 63);
                final IntervalMarker bst = new IntervalMarker(firstValue, secondValue, c, new BasicStroke(2.0f), null, null, 1.0f);

                bst.setLabel(regionName);
                bst.setLabelAnchor(RectangleAnchor.CENTER);
                bst.setLabelFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 10));
                bst.setLabelTextAnchor(TextAnchor.BASELINE_CENTER);
                bst.setLabelPaint(new Color(255, 255, 255, 100));

                plot.addDomainMarker(marker);
                plot.addDomainMarker(bst, Layer.BACKGROUND);

                if (_callback != null)
                {
                    _callback.addRegion(regionName, firstValue, secondValue, _valueMarkersAddedToChart.get(0), marker, bst);
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

    private static String[] getArrayOfElements()
    {
        List<String> elements = new ArrayList<String>();
        for (int i = 1; i <= LibzUnitManager.NUM_ATOMIC_ELEMENTS; i++)
        {
            AtomicElement ae = AtomicElement.getElementByAtomicNum(i);
            elements.add(ae.symbol);
        }

        String[] elementsArray = new String[elements.size()];
        elementsArray = elements.toArray(elementsArray);

        return elementsArray;
    }
}