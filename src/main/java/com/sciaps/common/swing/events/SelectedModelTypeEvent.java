package com.sciaps.common.swing.events;


public class SelectedModelTypeEvent {

    public final int mSelectedModelType;
    public final Object mOriginator;

    public SelectedModelTypeEvent(int modelType, Object originator) {
        mSelectedModelType = modelType;
        mOriginator = originator;
    }
}
