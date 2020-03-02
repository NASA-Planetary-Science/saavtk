package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.io.IOException;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileDownloader;
import edu.jhuapl.saavtk.util.file.StreamUnpacker.UnpackingStatus;

public class FileDownloadSwingWorker extends ProgressBarSwingWorker
{
    private final FileDownloader downloader;

    public static FileDownloadSwingWorker of(String fileName, Component c, String title, boolean indeterminate)
    {
        Preconditions.checkNotNull(fileName);
        // Component may be null.
        Preconditions.checkNotNull(title);

        return new FileDownloadSwingWorker(fileName, c, title, null, indeterminate);
    }

    public static FileDownloadSwingWorker of(String fileName, Component c, String title, boolean indeterminate, Runnable completionBlock)
    {
        Preconditions.checkNotNull(fileName);
        // Component may be null.
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(completionBlock);

        return new FileDownloadSwingWorker(fileName, c, title, null, indeterminate) {
            @Override
            protected Void doInBackground() throws IOException, InterruptedException
            {
                super.doInBackground();
                completionBlock.run();

                return null;
            }
        };
    }

    protected FileDownloadSwingWorker(String fileName, Component c, String title, String initialLabelText, boolean indeterminate)
    {
        super(c, title, initialLabelText, indeterminate);

        this.downloader = FileCache.instance().getDownloader(fileName, false);
        downloader.addPropertyChangeListener(e -> {
            String propertyName = e.getPropertyName();
            if (FileDownloader.DOWNLOAD_PROGRESS.equals(propertyName))
            {
                long contentLength = downloader.getUrlInfo().getState().getContentLength();

                UnpackingStatus unpackingStatus = (UnpackingStatus) e.getNewValue();

                String labelText = unpackingStatus.getMessage();
                long unpackedByteCount = unpackingStatus.getUnpackedByteCount();
                int progress = computeProgress(unpackedByteCount, contentLength);

                updateCompletionTimeEstimate(unpackingStatus.getEstimatedTimeToComplete(contentLength));

                updateProgressDialog(labelText, progress);
            }

            if (isCancelled())
            {
                downloader.cancel();
            }
        });

    }

    public boolean getIfNeedToDownload()
    {
        return downloader.getState().isDownloadNecessary();
    }

    @Override
    protected Void doInBackground() throws IOException, InterruptedException
    {
        downloader.downloadAndUnzip();

        checkNotCanceled("Download canceled");

        return null;
    }

    protected String getFileDownloaded()
    {
        return downloader.getFileInfo().getState().getFile().toString();
    }

}
