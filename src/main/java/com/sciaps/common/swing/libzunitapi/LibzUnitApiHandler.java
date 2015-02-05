package com.sciaps.common.swing.libzunitapi;

import com.sciaps.common.data.Instrument;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author sgowen
 */
public interface LibzUnitApiHandler
{

    Instrument connectToLibzUnit() throws IOException;

    void pullFromLibzUnit() throws IOException;

    void pushToLibzUnit() throws IOException;


    interface DownloadCallback {
        void onData(String shotId, LIBZPixelSpectrum pixelSpectrum);
    }

    void getLIBZPixelSpectrum(final List<String> shotIds, DownloadCallback cb) throws IOException;
}