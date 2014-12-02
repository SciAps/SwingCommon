package com.sciaps.common.swing.libzunitapi;

import com.sciaps.common.swing.global.LibzUnitManager;

/**
 *
 * @author sgowen
 */
public interface LibzUnitApiHandler
{
    boolean connectToLibzUnit(LibzUnitManager libzUnitManager);

    boolean pullFromLibzUnit(LibzUnitManager libzUnitManager);

    boolean pushToLibzUnit(LibzUnitManager libzUnitManager);
}