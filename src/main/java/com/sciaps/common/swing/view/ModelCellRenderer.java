package com.sciaps.common.swing.view;

import com.sciaps.data.EmpiricalModel;

import javax.swing.*;
import java.awt.*;


public class ModelCellRenderer extends JLabel implements ListCellRenderer<EmpiricalModel> {

    @Override
    public Component getListCellRendererComponent(JList<? extends EmpiricalModel> jList, EmpiricalModel model, int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        if(model != null) {
            setText(model.getName());
        } else {
            setText("-- Select A Calibration --");
        }

        return this;
    }
}
