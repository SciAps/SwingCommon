package com.sciaps.common.swing.async;

import com.sciaps.common.swing.listener.DownloadListener;
import com.sciaps.common.swing.utils.HttpUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 *
 * @author sgowen
 */
public final class DownloadFileSwingWorker extends SwingWorker<File, Void>
{
    public interface DownloadFileSwingWorkerCallback
    {
        void onComplete(File downloadedFile);

        void onFail();
    }

    private final String _urlString;
    private final DownloadFileSwingWorkerCallback _callback;
    private int _downloadSize;
    private ProgressMonitor _progressMonitor;

    public DownloadFileSwingWorker(String urlString, DownloadFileSwingWorkerCallback callback)
    {
        _urlString = urlString;
        _callback = callback;
    }

    public void start()
    {
        JPanel panel = new JPanel();
        panel.setOpaque(true);

        _progressMonitor = new ProgressMonitor(panel, "Downloading File...", "", 0, 100);
        _progressMonitor.setMillisToPopup(0);

        addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if ("progress".equals(evt.getPropertyName()))
                {
                    int progress = (Integer) evt.getNewValue();
                    _progressMonitor.setProgress(progress);
                    String message = String.format("Downloaded %d%%.\n", progress);
                    _progressMonitor.setNote(message);
                    if (_progressMonitor.isCanceled() || isDone())
                    {
                        if (_progressMonitor.isCanceled())
                        {
                            cancel(true);
                        }
                    }
                }
            }
        });

        execute();
    }

    @Override
    public File doInBackground()
    {
        _downloadSize = HttpUtils.getFileSize(_urlString);
        if (_downloadSize == -1)
        {
            return null;
        }

        File downloadedFile = HttpUtils.downloadFileFromUrl(_urlString, new DownloadListenerImpl());

        return downloadedFile;
    }

    @Override
    public void done()
    {
        setProgress(100);

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

    private final class DownloadListenerImpl implements DownloadListener
    {
        private long _totalBytesDownloaded;

        public DownloadListenerImpl()
        {
            _totalBytesDownloaded = 0;
        }

        @Override
        public void onBytesDownloaded(long bytesDownloaded)
        {
            _totalBytesDownloaded += bytesDownloaded;
            double progress = ((double) _totalBytesDownloaded / (double) _downloadSize);
            progress *= 100;

            setProgress((int) progress);
        }
    }
}