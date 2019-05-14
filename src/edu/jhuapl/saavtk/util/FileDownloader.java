package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

import javax.swing.SwingWorker;

import com.google.common.base.Preconditions;
import com.google.common.io.CountingInputStream;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

public class FileDownloader extends SwingWorker<Void, Void>
{
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();
    protected static final DecimalFormat PF = new DecimalFormat("0%");
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public static final String PROGRESS_PROPERTY = "downloadProgress";
    public static final String DONE_PROPERTY = "downloadDone";
    public static final String CANCELED_PROPERTY = "downloadCanceled";

    protected enum ByteScale
    {
        TBYTES("TB", 1.e12),
        GBYTES("GB", 1.e9),
        MBTYES("MB", 1.e6), //
        KBYTES("kB", 1.e3), //
        ;

        protected static final DecimalFormat DF = new DecimalFormat("0.0");

        public static String describe(double numberOfBytes)
        {
            for (ByteScale scale : values())
            {
                if (numberOfBytes > scale.multiplier)
                {
                    return DF.format(numberOfBytes / scale.multiplier) + " " + scale.description;
                }
            }

            return numberOfBytes + " Bytes";
        }

        private final String description;
        private final double multiplier;

        private ByteScale(String name, double scale)
        {
            this.description = name;
            this.multiplier = scale;
        }

    }

    public static FileDownloader of(UrlInfo urlInfo, FileInfo fileInfo, boolean forceDownload)
    {
        Preconditions.checkNotNull(urlInfo);
        Preconditions.checkNotNull(fileInfo);

        return new FileDownloader(urlInfo, fileInfo, forceDownload);
    }

    private final UrlInfo urlInfo;
    private final File file;
    private final FileInfo fileInfo;
    private final boolean forceDownload;
    private volatile Boolean unzipping;

    protected FileDownloader(UrlInfo urlInfo, FileInfo fileInfo, boolean forceDownload)
    {
        this.urlInfo = urlInfo;
        this.file = fileInfo.getState().getFile();
        this.fileInfo = fileInfo;
        this.forceDownload = forceDownload;
        this.unzipping = Boolean.FALSE;
    }

    public UrlInfo getUrlInfo()
    {
        return urlInfo;
    }

    public FileInfo getFileInfo()
    {
        return fileInfo;
    }

    public String createProgressMessage()
    {
        if (unzipping)
        {
            return createProgressMessage("Unzipping " + file.getName(), FileUtil.getDecompressedSize());
        }
        else
        {
            return createProgressMessage("Downloading " + file.getName(), urlInfo.getState().getContentLength());
        }
    }

    public void download() throws IOException, InterruptedException
    {
        // Do nothing if the URL really is just a pointer to a local file.
        String urlPath = urlInfo.getState().getUrl().getPath();
        if (SAFE_URL_PATHS.hasFileProtocol(urlPath) && urlPath.equals(fileInfo.getState().getFile().getAbsolutePath()))
        {
            return;
        }

        if (forceDownload || isFileOutOfDate() || urlInfo.getState().getStatus() == UrlStatus.UNKNOWN || fileInfo.getState().getStatus() == FileStatus.UNKNOWN)
        {
            fileInfo.update();

            try (CloseableUrlConnection closeableConnection = CloseableUrlConnection.of(urlInfo, HttpRequestMethod.GET))
            {
                urlInfo.update(closeableConnection.getConnection());

                if (forceDownload || isFileOutOfDate())
                {
                    download(closeableConnection);
                }
            }
        }

    }

    public void downloadInBackground()
    {
        THREAD_POOL.execute(this);
    }

    @Override
    public void done()
    {
        fileInfo.update();

        firePropertyChange(isCancelled() ? CANCELED_PROPERTY : DONE_PROPERTY, null, fileInfo);
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        download();
        unzipIfNecessary();

        return null;
    }

    protected void download(CloseableUrlConnection closeableConnection) throws IOException, InterruptedException
    {
        Debug.out().println("Opened connection to download " + urlInfo.getState().getUrl());

        URLConnection connection = closeableConnection.getConnection();

        URL url = connection.getURL();
        boolean gunzip = url.getPath().toLowerCase().endsWith(".gz");

        long totalByteCount = connection.getContentLengthLong();
        long lastModifiedTime = connection.getLastModified();

        File parentFile = file.getParentFile();
        Path tmpFilePath = SAFE_URL_PATHS.get(file.toPath() + "-" + UUID.randomUUID().toString());

        // Don't use try with resources because there are two references to the same
        // underlying stream.
        CountingInputStream countingInputStream = null;
        InputStream inputStream = null;

        try
        {
            unzipping = Boolean.FALSE;
            // Count characters that were read based on the connection's original stream,
            // i.e.,
            // before any unzipping is performed.
            countingInputStream = new CountingInputStream(connection.getInputStream());

            inputStream = gunzip ? new GZIPInputStream(countingInputStream) : countingInputStream;

            if (!isCancelled())
            {
                parentFile.mkdirs();

                File tmpFile = tmpFilePath.toFile();

                try (FileOutputStream os = new FileOutputStream(tmpFile))
                {
                    final int bufferSize = inputStream.available();
                    byte[] buff = new byte[bufferSize];
                    int len;
                    double progress = 0;
                    while ((len = inputStream.read(buff)) > 0 && !isCancelled())
                    {
                        os.write(buff, 0, len);
                        double oldProgress = progress;
                        progress = Math.min(100. * countingInputStream.getCount() / totalByteCount, 99.);
                        setProgress((int) progress);
                        firePropertyChange(PROGRESS_PROPERTY, (int) oldProgress, (int) progress);
                    }

                    if (isCancelled())
                    {
                        throw new InterruptedException();
                    }

                    setProgress(99);
                    firePropertyChange(PROGRESS_PROPERTY, progress, 99);
                }
            }
        }
        catch (Exception e)
        {
            if (!Debug.isEnabled())
            {
                Files.deleteIfExists(tmpFilePath);
            }
            throw e;
        }
        finally
        {
            if (inputStream != null)
            {
                inputStream.close();
            }
            else if (countingInputStream != null)
            {
                countingInputStream.close();
            }
        }

        // Rename the temporary file to the real name.
        File tmpFile = tmpFilePath.toFile();
        if (tmpFile.exists())
        {
            file.delete();

            if (!tmpFile.renameTo(file))
            {
                throw new IOException("Failed to rename temporary file " + tmpFile);
            }

            // Change the modified time of the final real file.
            if (lastModifiedTime > 0)
                file.setLastModified(lastModifiedTime);
        }
    }

    protected boolean isFileOutOfDate()
    {
        return !file.exists() || urlInfo.getState().getLastModified() > file.lastModified();
    }

    protected void unzipIfNecessary() throws IOException, InterruptedException
    {
        String filePath = file.getAbsolutePath();
        if (filePath.matches("^.*\\.[Zz][Ii][Pp]$"))
        {
            File zipRootFolder = new File(filePath.substring(0, filePath.length() - ".zip".length()));
            if (!zipRootFolder.isDirectory())
            {
                if (!file.exists())
                {
                    throw new FileNotFoundException("Cannot unzip file " + filePath);
                }
                unzip();
            }
        }
    }

    protected void unzip() throws InterruptedException
    {
        AtomicBoolean done = new AtomicBoolean(false);
        Runnable runnable = () -> {
            try
            {
                FileUtil.unzipFile(file);
            }
            finally
            {
                done.set(true);
            }
        };

        int oldProgress = getProgress();

        try
        {
            unzipping = Boolean.TRUE;

            THREAD_POOL.execute(runnable);

            while (!done.get())
            {
                int progress = Math.min((int) FileUtil.getUnzipProgress(), 99);
                setProgress(progress);
                firePropertyChange(PROGRESS_PROPERTY, oldProgress, progress);
                oldProgress = progress;

                if (isCancelled())
                {
                    throw new InterruptedException();
                }

                Thread.sleep(333);
            }
        }
        finally
        {
            setProgress(99);
            firePropertyChange(PROGRESS_PROPERTY, oldProgress, 99);
        }
    }

    protected String createProgressMessage(String prefix, long contentLength)
    {
        double percentDownloaded = getProgress() / 100.;
        long progressInBytes = (long) (percentDownloaded * contentLength);

        return "<html>" + prefix + "<br>" + PF.format(percentDownloaded) + " complete (" + //
                ByteScale.describe(progressInBytes) + " of " + ByteScale.describe(contentLength) + ")</html>";
    }

}