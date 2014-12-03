package com.sciaps.common.swing.async;

import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;

/**
 *
 * @author sgowen
 */
public final class LibzUnitPushSwingWorker extends BaseLibzUnitApiSwingWorker
{
    public LibzUnitPushSwingWorker(BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback, Class<? extends LibzUnitApiHandler> clazz)
    {
        super(callback, clazz);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return Boolean.valueOf(_libzUnitApiHandler.pushToLibzUnit());
    }
}