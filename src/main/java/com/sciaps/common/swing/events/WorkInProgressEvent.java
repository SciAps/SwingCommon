package com.sciaps.common.swing.events;/**
 * Created by jchen on 7/9/15.
 */
public class WorkInProgressEvent {
    public String mMessage;
    public boolean mCompleted = false;

    public WorkInProgressEvent(String msg, boolean inProgress) {
        mMessage = msg;
        mCompleted = inProgress;
    }
}
