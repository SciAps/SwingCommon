package com.sciaps.common.swing.events;


import com.sciaps.common.data.Instrument;

public class ConnectToInstrumentEvent {


    public final Instrument instrument;

    public ConnectToInstrumentEvent(Instrument instrument) {
        this.instrument = instrument;
    }
}
