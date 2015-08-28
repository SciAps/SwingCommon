package com.sciaps.common.swing.view;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * Created by paul on 9/1/15.
 */
public class LabeledXYDatasetToolTipGenerator implements XYToolTipGenerator {
    @Override
    public String generateToolTip(XYDataset dataset, int series, int item) {
        LabeledXYDataset labelSource = (LabeledXYDataset) dataset;
        return labelSource.getLabel(series, item);
    }
}
