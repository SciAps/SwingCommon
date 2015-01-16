package com.sciaps.common.swing.libzunitapi;

import com.sciaps.common.spectrum.LIBZPixelSpectrum;

/**
 *
 * @author sgowen
 */
public interface LibzUnitApiHandler
{
    /**
     * This is ALWAYS the first method to call when beginning interactions with
     * the LIBZ Unit. If it is successful, an IsAlive object will be returned
     * that contains a unique ID for the LIBZ Unit. This unique ID will be used
     * in all additional API calls
     *
     * @return true if a valid IsAlive response was received
     */
    boolean connectToLibzUnit();

    /**
     * Perform a mass pull, which essentially means it is going to call all of
     * the GET methods on the unit
     *
     * @return true if all the GET calls executed successfully
     */
    boolean pullFromLibzUnit();

    /**
     * Perform a mass push, which essentially means it is going to call all of
     * the POST methods on the unit
     *
     * @return true if all the POST calls executed successfully
     */
    boolean pushToLibzUnit();

    /**
     * Retrieve a LIBZPixelSpectrum object deserialized from binary shot data
     * associated with the provided shotId. Any data returned from this method
     * should be cached so that additional invocations will return immediately
     * without fetching.
     *
     * @param shotId
     * @return
     */
    LIBZPixelSpectrum getLIBZPixelSpectrum(final String shotId);
}