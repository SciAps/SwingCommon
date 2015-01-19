package com.sciaps.common.swing.view;

import java.util.ArrayList;
import java.util.List;
import org.jfree.data.xy.AbstractXYDataset;

/**
 *
 * @author sgowen
 */
public final class LabeledXYDataset extends AbstractXYDataset {

    public static class LabeledXYSeries {
        private Comparable _key;

        private List<Double> _x = new ArrayList();
        private List<Double> _y = new ArrayList();
        private List<String> _label = new ArrayList();

        public LabeledXYSeries(Comparable key) {
            _key = key;
        }

        public void add(double x, double y, String label) {
            _x.add(x);
            _y.add(y);
            _label.add(label);
        }

    }

    private ArrayList<LabeledXYSeries> _series = new ArrayList<LabeledXYSeries>();

    public void addSeries(LabeledXYSeries series) {
        _series.add(series);
        fireDatasetChanged();
    }

    public void removeSeries(LabeledXYSeries series) {
        _series.remove(series);
        fireDatasetChanged();
    }

    @Override
    public int getSeriesCount() {
        return _series.size();
    }

    @Override
    public Comparable getSeriesKey(int series) {
        return _series.get(series)._key;
    }

    @Override
    public int getItemCount(int series) {
        return _series.get(series)._x.size();
    }

    @Override
    public Number getX(int series, int item) {
        return _series.get(series)._x.get(item);
    }

    @Override
    public Number getY(int series, int item) {
        return _series.get(series)._y.get(item);
    }

    public String getLabel(int series, int item) {
        return _series.get(series)._label.get(item);
    }
}