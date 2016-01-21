package com.sciaps.common.swing.events;

import com.devsmart.microdb.DBObject;

public class DBObjEvent {

    public static final int CREATED = 0;
    public static final int MODIFIED = 1;
    public static final int DELETED = 2;


    public final int type;
    public final DBObject obj;
    public final Object originator;

    public DBObjEvent(DBObject obj, int type, Object originator) {
        this.obj = obj;
        this.type = type;
        this.originator = originator;
    }
}
