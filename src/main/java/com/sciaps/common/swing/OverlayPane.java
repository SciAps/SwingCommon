package com.sciaps.common.swing;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class OverlayPane extends JComponent implements MouseListener {

    public final JPanel mContentPanel;

    public OverlayPane() {
        mContentPanel = new JPanel();
        add(mContentPanel);
        addMouseListener(this);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension dimension = getParent().getSize();
        return new Dimension(dimension);
    }

    @Override
    public void doLayout() {
        int x = (getWidth() - mContentPanel.getPreferredSize().width) / 2 ;
        int y = (getHeight() - mContentPanel.getPreferredSize().height) / 2;
        mContentPanel.setLocation(x, y);
        mContentPanel.setSize(mContentPanel.getPreferredSize());
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 0.6f));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public void redispatchMouseEvent(MouseEvent e) {

        Component component = SwingUtilities.getDeepestComponentAt(
                        this,
                        e.getX(),
                        e.getY());

        //if this mouse event is over the mContentPanel, forward it
        //otherwise consume the event
        if(component == mContentPanel) {
            Point componentPoint = SwingUtilities.convertPoint(
                    this,
                    e.getPoint(),
                    component);

            component.dispatchEvent(new MouseEvent(component,
                    e.getID(),
                    e.getWhen(),
                    e.getModifiers(),
                    componentPoint.x,
                    componentPoint.y,
                    e.getClickCount(),
                    e.isPopupTrigger()));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        redispatchMouseEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        redispatchMouseEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        redispatchMouseEvent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        redispatchMouseEvent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        redispatchMouseEvent(e);
    }
}
