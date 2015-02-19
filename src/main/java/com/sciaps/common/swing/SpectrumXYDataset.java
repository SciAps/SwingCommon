package com.sciaps.common.swing;

import com.sciaps.common.spectrum.Spectrum;
import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.jfree.data.xy.AbstractXYDataset;

import java.util.ArrayList;

public class SpectrumXYDataset extends AbstractXYDataset {

    private static final double SampleRate = 30;

    private ArrayList<Spectrum> mSpectrum = new ArrayList<Spectrum>();
    private ArrayList<String> mSpectrumKey = new ArrayList<String>();

    public void addSpectrum(Spectrum spectrum, String name) {
        if(spectrum == null || name == null) {
            return;
        }
        mSpectrum.add(spectrum);
        mSpectrumKey.add(name);
        fireDatasetChanged();
    }

    public void removeSpectrum(Spectrum spectrum) {
        int i = mSpectrum.indexOf(spectrum);
        if(i >= 0) {
            mSpectrum.remove(i);
            mSpectrumKey.remove(i);
            fireDatasetChanged();
        }
    }

    public void removeAll() {
        mSpectrum.clear();
        mSpectrumKey.clear();
        fireDatasetChanged();
    }

    @Override
    public int getSeriesCount() {
        return mSpectrum.size();
    }

    @Override
    public Comparable getSeriesKey(int series) {
        return mSpectrumKey.get(series);
    }

    @Override
    public int getItemCount(int series) {
        Spectrum spectrum = mSpectrum.get(series);
        DoubleRange range = spectrum.getValidRange();
        double width = (range.getMaximumDouble() - range.getMinimumDouble());
        int numSamples = (int) (width * SampleRate);
        return numSamples;
    }

    @Override
    public Number getX(int series, int item) {
        Spectrum spectrum = mSpectrum.get(series);
        DoubleRange range = spectrum.getValidRange();
        double retval = range.getMinimumDouble() + item / SampleRate;
        return retval;
    }

    @Override
    public Number getY(int series, int item) {
        Spectrum spectrum = mSpectrum.get(series);
        DoubleRange range = spectrum.getValidRange();
        double x = range.getMinimumDouble() + item / SampleRate;

        UnivariateFunction ivf = spectrum.getIntensityFunction();
        return ivf.value(x);
    }
}
