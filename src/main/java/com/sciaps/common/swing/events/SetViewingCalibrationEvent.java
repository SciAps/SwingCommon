package com.sciaps.common.swing.events;


import com.sciaps.common.data.Model;

public class SetViewingCalibrationEvent {

    public final Model mModel;
    public final Object mOriginator;

    public SetViewingCalibrationEvent(Model model, Object originator) {
        mModel = model;
        mOriginator = originator;
    }
}
