package com.sciaps.common.swing.events;

/**
 * Created by jchen on 4/8/15.
 */
public class FingerprintPanelEvent {

    public static final int RESET = 0;
    public static final int SHOW_PC = 1;
    public static final int IMPORT = 2;
    public static final int EXPORT = 3;


    public Object mOriginator;
    public int mAction;

    public FingerprintPanelEvent(int action, Object originator) {
        mAction = action;
        mOriginator = originator;
    }
}
