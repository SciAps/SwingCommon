package com.sciaps.common.swing.events;


import com.sciaps.common.data.Model;

public class CalibrationSelectedEvent {

    public final Model mModel;

    public CalibrationSelectedEvent(Model model) {
        mModel = model;
    }
}
