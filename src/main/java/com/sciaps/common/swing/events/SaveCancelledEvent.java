package com.sciaps.common.swing.events;

/**
 * Created by jchen on 4/8/15.
 */
public class SaveCancelledEvent {
    public String mCancelledReason = "";

    public SaveCancelledEvent(String reason) {
        mCancelledReason = reason;
    }
}
