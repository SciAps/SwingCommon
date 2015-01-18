package com.sciaps.common.swing.view;

import com.sciaps.common.data.Model;

import javax.swing.*;
import java.awt.*;


public class ModelCellRenderer extends JLabel implements ListCellRenderer<Model> {

    @Override
    public Component getListCellRendererComponent(JList<? extends Model> jList, Model model, int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        setText(model.name);

        return this;
    }
}
