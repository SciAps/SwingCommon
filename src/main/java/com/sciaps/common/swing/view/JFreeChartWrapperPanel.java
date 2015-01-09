package com.sciaps.common.swing.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Line2D;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

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

    public void populateCurveChart(String chartName, String xAxisName, String yAxisName, AbstractXYDataset pointsDataset, AbstractXYDataset standardsDataset)
    {
        XYPlot plot = new XYPlot();

        XYDataset collection1 = pointsDataset;
        XYLineAndShapeRenderer renderer1 = createXYLineAndShapeRenderer();
        ValueAxis domain1 = new NumberAxis(xAxisName);
        ValueAxis range1 = new NumberAxis(yAxisName);

        plot.setDataset(0, collection1);
        plot.setDomainAxis(0, domain1);
        plot.setRangeAxis(0, range1);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);
        customizePlot(plot, renderer1);

        XYDataset collection2 = standardsDataset;
        XYItemRenderer renderer2 = new XYLineAndShapeRenderer(false, true);
        renderer2.setBaseItemLabelGenerator(new LabelGenerator());
        renderer2.setBaseItemLabelPaint(new Color(255, 60, 24, 255));
        renderer2.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        renderer2.setBaseItemLabelFont(renderer2.getBaseItemLabelFont().deriveFont(14f));
        renderer2.setBaseItemLabelsVisible(true);
        renderer2.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        plot.setDataset(1, collection2);
        plot.setRenderer(1, renderer2);
        plot.mapDatasetToDomainAxis(1, 0);
        plot.mapDatasetToRangeAxis(1, 0);

        JFreeChart jFreeChart = new JFreeChart(chartName, JFreeChart.DEFAULT_TITLE_FONT, plot, true);

        load(jFreeChart);
    }

    public void populateSpectrumChartWithAbstractXYDataset(XYSeriesCollection dataset, String chartName, String xAxisName, String yAxisName)
    {
        JFreeChart jFreeChart = ChartFactory.createXYLineChart(chartName, xAxisName, yAxisName, dataset);

        XYPlot plot = jFreeChart.getXYPlot();
        XYLineAndShapeRenderer renderer = createXYLineAndShapeRenderer();

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