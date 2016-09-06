package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.io.File;
import java.text.DecimalFormat;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.FileCache.FileInfo;


public class FileDownloadSwingWorker extends ProgressBarSwingWorker
{
    private String filename;
    private DecimalFormat df = new DecimalFormat("0.00");

    public FileDownloadSwingWorker(Component c, String title, String filename)
    {
        super(c, title);
        this.filename = filename;
        setLabelText("<html>Downloading file<br>0% completed</html>");
    }

    public static boolean downloadFile(Component c, String title, String filename)
    {
        FileDownloadSwingWorker worker = new FileDownloadSwingWorker(c, title, filename);
        worker.executeDialog();
        return !worker.isCancelled();
    }

    public boolean getIfNeedToDownload()
    {
        return FileCache.getFileInfoFromServer(filename).needToDownload;
    }

    @Override
    protected Void doInBackground()
    {
        final FileInfo fileInfo = FileCache.getFileInfoFromServer(filename);
        final boolean needToDownload = fileInfo.needToDownload;

        String zipfile = fileInfo.file.getAbsolutePath();
        File zipRootFolder = new File(zipfile.substring(0, zipfile.length()-4));
        final boolean needToUnzip = filename.endsWith(".zip") &&
                                    (needToDownload || !zipRootFolder.exists());

        if (!needToDownload && !needToUnzip)
            return null;

        if (needToDownload)
            FileCache.resetDownloadProgess();
        if (needToUnzip)
        {
            FileUtil.resetUnzipProgress();
            // If we need to unzip always show progress dialog and don't bother
            // to calculate time to unzip
            // TODO calculate how long to unzip and only show progress dialog
            // if necessary
            setCompletionTimeEstimate(1000000.0); // a very high number to force progress bar to show
        }

        Runnable runner = new Runnable()
        {
            public void run()
            {
                File file = FileCache.getFileFromServer(filename);

                if (file != null && needToUnzip)
                    FileUtil.unzipFile(file);
            }
        };
        Thread downloadThread = new Thread(runner);
        downloadThread.start();

        long prevTime = System.currentTimeMillis();
        boolean computedTimeEstimate = false;

        try
        {
            String name = new File(filename).getName();
            while (downloadThread.isAlive() && !isCancelled())
            {
                long downloadProgress = FileCache.getDownloadProgess();
                double percentDownloaded = 100.0 * (double)downloadProgress / (double)fileInfo.length;
                double downloadedSoFarInMB = (double)downloadProgress / 1048576.0;
                double totalSizeInMB = (double)fileInfo.length / 1048576.0;

                double unzipProgress = FileUtil.getUnzipProgress();
                if (downloadProgress < fileInfo.length && needToDownload)
                {
                    setLabelText("<html>Downloading " + name + "<br>" +
                            df.format(percentDownloaded) + "% completed " +
                             "  (" + df.format(downloadedSoFarInMB) + " of " +
                             df.format(totalSizeInMB) + " MB)</html>");

                    // Call firePropertyChange rather than setProgess since the latter will
                    // only cause a property change if the percent downloaded (cast to an int)
                    // changes, whereas firePropertyChange always forces a property change.
                    firePropertyChange("progress", null, Math.min((int)percentDownloaded, 99));
                }
                else if (unzipProgress < 100.0 && needToUnzip)
                {
                    setLabelText("<html>Unzipping " + name + "<br>" +
                            df.format(unzipProgress) + "% completed</html>");

                    // See comment in previous if block
                    firePropertyChange("progress", null, Math.min((int)unzipProgress, 99));
                }

                Thread.sleep(333);

                if (needToDownload && !computedTimeEstimate)
                {
                    double timeElapsed = (double)(System.currentTimeMillis() - prevTime) / 1000.0;
                    double downloadRate = (double)FileCache.getDownloadProgess() / timeElapsed;
                    double totalTimeToDownload = (double)fileInfo.length / downloadRate;
                    setCompletionTimeEstimate(totalTimeToDownload);
                    computedTimeEstimate = true;
                }
            }
        }
        catch (InterruptedException ignore)
        {
            //ignore.printStackTrace();
        }

        if (isCancelled())
        {
            if (needToDownload)
                FileCache.abortDownload();
            if (needToUnzip)
                FileUtil.abortUnzip();
        }

        return null;
    }

    protected String getFileDownloaded()
    {
        return filename;
    }
}
