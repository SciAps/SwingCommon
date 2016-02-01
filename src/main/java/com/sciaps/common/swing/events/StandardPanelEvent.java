package com.sciaps.common.swing.events;

/**
 * Created by jchen on 4/8/15.
 */
public class StandardPanelEvent {

    public static final int RESET = 0;
    public static final int ADD = 1;
    public static final int DUPLICATE = 2;
    public static final int DELETE = 3;
    public static final int IMPORT = 4;
    public static final int EXPORT = 5;
    public static final int RENAME = 6;
    public static final int SHOW_SPECTRUM = 7;
    public static final int SHOW_ASSAYS = 8;
    public static final int SHOW_TABLE_COLUMNS = 9;
    public static final int STANDARD_SELECTED = 10;


    public Object mOriginator;
    public int mAction;

    public StandardPanelEvent(int action, Object originator) {
        mAction = action;
        mOriginator = originator;
    }
}
