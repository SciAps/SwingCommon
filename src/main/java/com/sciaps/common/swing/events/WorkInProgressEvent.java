package com.sciaps.common.swing.events;/**
 * Created by jchen on 7/9/15.
 */
public class WorkInProgressEvent {
    public String mMessage;
    public boolean mCompleted = false;
    public boolean mAbortable = false;

    public WorkInProgressEvent(String msg, boolean inProgress) {
        mMessage = msg;
        mCompleted = inProgress;
    }

    public WorkInProgressEvent(String msg, boolean inProgress, boolean abortable) {
        mMessage = msg;
        mCompleted = inProgress;
        mAbortable = abortable;
    }
}
