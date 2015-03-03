package com.sciaps.common.swing;

import javax.swing.*;
import java.awt.*;


public class FramePanel extends JLayeredPane {

    @Override
    public void doLayout() {
        super.doLayout();
        final int width = getWidth();
        final int height = getHeight();
        for(Component c : getComponents()) {
            c.setBounds(0, 0, width, height);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dimension = new Dimension(0,0);
        for(Component c : getComponents()) {
            Dimension tmpDimension = c.getPreferredSize();
            dimension.height = Math.max(tmpDimension.height, dimension.height);
            dimension.width = Math.max(tmpDimension.width, dimension.width);
        }
       return dimension;
    }
}
