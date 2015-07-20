package com.sciaps.common.swing.events;


public class InstrumentStatusEvent {

    public static final int UNSUPPORTED_FEATURE = -100;
    public static final int UNKNOWN = -1;
    public static final int TRUE_VALUE = 0;
    public static final int FALSE_VALUE = 1;

    public boolean mConnected = false;
    public int mInstrumentVersionCode = 0;
    public int mWLCalibrationCode = FALSE_VALUE;
    public float mArgonPSILevel = -1;

    public InstrumentStatusEvent(boolean isConnected, int instrumentVersion, int wlCalibrationCode, float argonPSILevel) {
        mConnected = isConnected;
        mInstrumentVersionCode = instrumentVersion;
        mWLCalibrationCode = wlCalibrationCode;
        mArgonPSILevel = argonPSILevel;
    }

}
