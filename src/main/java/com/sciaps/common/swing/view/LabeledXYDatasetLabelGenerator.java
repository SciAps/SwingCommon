package com.sciaps.common.swing.view;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author sgowen
 */
public final class LabeledXYDatasetLabelGenerator implements XYItemLabelGenerator {
    @Override
    public String generateLabel(XYDataset dataset, int series, int item) {
        LabeledXYDataset labelSource = (LabeledXYDataset) dataset;
        return labelSource.getLabel(series, item);
    }
}