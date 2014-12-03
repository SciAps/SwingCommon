package com.sciaps.common.swing.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Line2D;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author sgowen
 */
public final class JFreeChartWrapperPanel extends JPanel
{
    private ChartPanel _chartPanel;
    private boolean _isChartLoaded;

    public JFreeChartWrapperPanel()
    {
        _chartPanel = null;
        _isChartLoaded = false;
    }

    public void populateSpectrumChartWithAbstractXYDataset(XYSeriesCollection dataset, String chartName, String xAxisName, String yAxisName)
    {
        JFreeChart jFreeChart = ChartFactory.createXYLineChart(chartName, xAxisName, yAxisName, dataset);

        XYPlot plot = jFreeChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // sets paint color for each series
        renderer.setSeriesPaint(0, Color.GREEN);
        renderer.setSeriesShape(0, new Line2D.Double());

        // sets thickness for series (using strokes)
        renderer.setSeriesStroke(0, new BasicStroke(1.0f));

        // sets paint color for plot outlines
        plot.setOutlinePaint(Color.LIGHT_GRAY);
        plot.setOutlineStroke(new BasicStroke(2.0f));

        // sets renderer for lines
        plot.setRenderer(renderer);

        // sets plot background
        plot.setBackgroundPaint(Color.DARK_GRAY);

        // sets paint color for the grid lines
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        if (_chartPanel != null)
        {
            remove(_chartPanel);
        }

        _chartPanel = new ChartPanel(jFreeChart);
        _chartPanel.setMouseWheelEnabled(true);
        _chartPanel.setSize(getWidth(), getHeight());

        setLayout(new BorderLayout());
        add(_chartPanel, BorderLayout.CENTER);

        _isChartLoaded = true;
    }

    public ChartPanel getChartPanel()
    {
        return _chartPanel;
    }

    public JFreeChart getJFreeChart()
    {
        return _chartPanel == null ? null : _chartPanel.getChart();
    }

    public boolean isChartLoaded()
    {
        return _isChartLoaded;
    }
}