package com.sciaps.common.swing.events;

import com.sciaps.common.objtracker.DBObj;

public class DBObjEvent {

    public static final int CREATED = 0;
    public static final int MODIFIED = 1;
    public static final int DELETED = 2;


    public final int type;
    public final DBObj obj;

    public DBObjEvent(DBObj obj, int type) {
        this.obj = obj;
        this.type = type;
    }
}
