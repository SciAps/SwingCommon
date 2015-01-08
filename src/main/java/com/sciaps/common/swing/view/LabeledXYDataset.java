package com.sciaps.common.swing.view;

import java.util.ArrayList;
import java.util.List;
import org.jfree.data.xy.AbstractXYDataset;

/**
 *
 * @author sgowen
 */
public final class LabeledXYDataset extends AbstractXYDataset
{
    private List<Double> _x = new ArrayList();
    private List<Double> _y = new ArrayList();
    private List<String> _label = new ArrayList();

    public void add(double x, double y, String label)
    {
        _x.add(x);
        _y.add(y);
        _label.add(label);
    }

    public String getLabel(int series, int item)
    {
        return _label.get(item);
    }

    @Override
    public int getSeriesCount()
    {
        return 1;
    }

    @Override
    public Comparable getSeriesKey(int series)
    {
        return "Standard";
    }

    @Override
    public int getItemCount(int series)
    {
        return _label.size();
    }

    @Override
    public Number getX(int series, int item)
    {
        return _x.get(item);
    }

    @Override
    public Number getY(int series, int item)
    {
        return _y.get(item);
    }
}