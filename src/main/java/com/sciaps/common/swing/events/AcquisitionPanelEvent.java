package com.sciaps.common.swing.events;

/**
 * Created by jchen on 4/8/15.
 */
public class AcquisitionPanelEvent {
    public static final int RESET = 0;
    public static final int IMPORT = 1;
    public static final int EXPORT = 2;
    public static final int DELETE = 3;
    public static final int ACQUISITION_SETTING = 4;
    public static final int ANALYZE_SETTING = 5;
    public static final int ANALYZE_SPECTRUM = 6;
    public static final int SHOW_REJECTED_ELEMENTS = 7;
    public static final int SHOW_EMISSION_LINES = 8;
    public static final int HIDE_EMISSION_LINES = 9;
    public static final int TEST_SELECTED = 10;

    public Object mOriginator;
    public int mAction;

    public AcquisitionPanelEvent(int action, Object originator) {
        mAction = action;
        mOriginator = originator;
    }
}
