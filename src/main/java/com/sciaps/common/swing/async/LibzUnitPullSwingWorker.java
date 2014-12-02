package com.sciaps.common.swing.async;

import com.sciaps.common.swing.global.LibzUnitManager;

/**
 *
 * @author sgowen
 */
public final class LibzUnitPullSwingWorker extends BaseLibzUnitApiSwingWorker
{
    public LibzUnitPullSwingWorker(BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback)
    {
        super(callback);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return Boolean.valueOf(_libzUnitApiHandler.pullFromLibzUnit(LibzUnitManager.getInstance()));
    }
}