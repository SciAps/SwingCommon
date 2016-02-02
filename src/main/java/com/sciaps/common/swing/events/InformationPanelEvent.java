package com.sciaps.common.swing.events;

/**
 * Created by jchen on 4/8/15.
 */
public class InformationPanelEvent {
    public static final int RESET = 0;
    public static final int ADD = 1;
    public static final int DELETE = 2;
    public static final int SAVE = 3;

    public Object mOriginator;
    public int mAction;

    public InformationPanelEvent(int action, Object originator) {
        mAction = action;
        mOriginator = originator;
    }
}
