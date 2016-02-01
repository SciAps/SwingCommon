package com.sciaps.common.swing.events;

/**
 * Created by jchen on 4/8/15.
 */
public class GradeLibraryPanelEvent {

    public static final int RESET = 0;
    public static final int CLONE = 1;
    public static final int ADD = 2;
    public static final int DELETE = 3;
    public static final int IMPORT = 4;
    public static final int EXPORT = 5;
    public static final int ADD_GRADE = 6;
    public static final int DELETE_GRADE = 7;
    public static final int SHOW_GRADES = 8;
    public static final int SHOW_TABLE_COLUMNS = 9;

    public Object mOriginator;
    public int mAction;

    public GradeLibraryPanelEvent(int action, Object originator) {
        mAction = action;
        mOriginator = originator;
    }
}
