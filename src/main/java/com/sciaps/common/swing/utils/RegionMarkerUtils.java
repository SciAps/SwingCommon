package com.sciaps.common.swing.utils;

import com.sciaps.common.data.Region;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author sgowen
 */
public final class RegionMarkerUtils
{
    public static ValueMarker createMinValueMarkerForRegion(Region region)
    {
        return createValueMarkerForValue(region.wavelengthRange.getMinimumDouble());
    }

    public static ValueMarker createMaxValueMarkerForRegion(Region region)
    {
        return createValueMarkerForValue(region.wavelengthRange.getMaximumDouble());
    }

    public static ValueMarker createValueMarkerForValue(double x)
    {
        ValueMarker valueMarker = new ValueMarker(x);
        valueMarker.setPaint(Color.RED);

        return valueMarker;
    }

    public static IntervalMarker createIntervalMarkerForRegion(Region region)
    {
        return createIntervalMarkerWithLabelAndValues(region.name, region.wavelengthRange.getMinimumDouble(), region.wavelengthRange.getMaximumDouble());
    }

    public static IntervalMarker createIntervalMarkerForElementValues(String element, double firstValue, double secondValue)
    {
        double minValue = Math.min(firstValue, secondValue);
        double maxValue = Math.max(firstValue, secondValue);

        String regionName = element + "_" + (int) minValue + "-" + (int) maxValue;

        return createIntervalMarkerWithLabelAndValues(regionName, minValue, maxValue);
    }

    private static IntervalMarker createIntervalMarkerWithLabelAndValues(String label, double minValue, double maxValue)
    {
        final Color c = new Color(255, 60, 24, 63);
        final IntervalMarker bst = new IntervalMarker(minValue, maxValue, c, new BasicStroke(2.0f), null, null, 1.0f);

        bst.setLabel(label);
        bst.setLabelAnchor(RectangleAnchor.CENTER);
        bst.setLabelFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 10));
        bst.setLabelTextAnchor(TextAnchor.BASELINE_CENTER);
        bst.setLabelPaint(new Color(255, 255, 255, 100));

        return bst;
    }

    private RegionMarkerUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}