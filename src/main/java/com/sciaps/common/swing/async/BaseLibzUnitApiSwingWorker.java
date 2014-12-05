package com.sciaps.common.swing.async;

import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.global.InstanceManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author sgowen
 */
public abstract class BaseLibzUnitApiSwingWorker extends SwingWorker<Boolean, Void>
{
    public interface BaseLibzUnitApiSwingWorkerCallback
    {
        void onComplete(boolean isSuccessful);

        void onFail();
    }

    private final BaseLibzUnitApiSwingWorkerCallback _callback;
    protected final LibzUnitApiHandler _libzUnitApiHandler;

    public BaseLibzUnitApiSwingWorker(BaseLibzUnitApiSwingWorkerCallback callback, Class<? extends LibzUnitApiHandler> clazz)
    {
        _callback = callback;
        _libzUnitApiHandler = InstanceManager.getInstance().retrieveInstance(clazz);
    }

    public final void start()
    {
        execute();
    }

    @Override
    public void done()
    {
        try
        {
            _callback.onComplete(get());
        }
        catch (Exception e)
        {
            Logger.getLogger(DownloadFileSwingWorker.class.getName()).log(Level.SEVERE, null, e);

            _callback.onFail();
        }
    }
}