package com.sciaps.common.swing.view;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

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

    public void populateCurveChart(String chartName, String xAxisName, String yAxisName, AbstractXYDataset curveDataset, AbstractXYDataset pointsDataset)
    {
        JFreeChart chart = ChartFactory.createXYLineChart(chartName, xAxisName, yAxisName, curveDataset);
        XYPlot plot = chart.getXYPlot();

        {
            //curve line
            XYSplineRenderer renderer1 = new XYSplineRenderer();
            renderer1.setShapesVisible(false);
            customizePlot(plot, renderer1);
        }

        XYItemRenderer pointRenderer;
        {
            //enabled points
            pointRenderer = new XYLineAndShapeRenderer(false, true);
            pointRenderer.setBaseItemLabelGenerator(new LabelGenerator());
            pointRenderer.setBaseItemLabelPaint(Color.LIGHT_GRAY);
            pointRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BOTTOM_CENTER));
            pointRenderer.setBaseItemLabelsVisible(true);
            pointRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
            pointRenderer.setSeriesPaint(0, Color.GREEN);
            pointRenderer.setSeriesPaint(1, Color.RED);
        }

        plot.setDataset(1, pointsDataset);
        plot.setRenderer(1, pointRenderer);

        load(chart);
    }

    public void populateSpectrumChartWithAbstractXYDataset(XYSeriesCollection dataset, String chartName, String xAxisName, String yAxisName)
    {
        JFreeChart jFreeChart = ChartFactory.createXYLineChart(chartName, xAxisName, yAxisName, dataset);

        XYPlot plot = jFreeChart.getXYPlot();
        XYLineAndShapeRenderer renderer = createXYLineAndShapeRenderer();

        customizePlot(plot, renderer);

        load(jFreeChart);
    }

    public void createSpectrumChart(XYSeriesCollection dataset, String chartName, String xAxisName, String yAxisName)
    {
        JFreeChart jFreeChart = ChartFactory.createXYLineChart(chartName, xAxisName, yAxisName, dataset);

        XYPlot plot = jFreeChart.getXYPlot();
        XYLineAndShapeRenderer renderer = createXYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);

        customizePlot(plot, renderer);

        load(jFreeChart);
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

    private XYLineAndShapeRenderer createXYLineAndShapeRenderer()
    {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // sets paint color for each series
        renderer.setSeriesPaint(0, Color.GREEN);
        renderer.setSeriesShape(0, new Line2D.Double());

        // sets thickness for series (using strokes)
        renderer.setSeriesStroke(0, new BasicStroke(1.0f));

        return renderer;
    }

    private void customizePlot(XYPlot plot, XYLineAndShapeRenderer renderer)
    {
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
    }

    private void load(JFreeChart jFreeChart)
    {
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
}