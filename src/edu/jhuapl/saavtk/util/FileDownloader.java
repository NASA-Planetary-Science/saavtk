package edu.jhuapl.saavtk.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;
import edu.jhuapl.saavtk.util.DownloadableFileInfo.DownloadableFileState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;
import edu.jhuapl.saavtk.util.file.StreamGunzipper;
import edu.jhuapl.saavtk.util.file.StreamUnpacker;
import edu.jhuapl.saavtk.util.file.StreamUnpacker.UnpackingStatus;
import edu.jhuapl.saavtk.util.file.ZipFileUnzipper;

public abstract class FileDownloader implements Runnable
{
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    public static final String DOWNLOAD_PROGRESS = "downloadProgress";
    public static final String DOWNLOAD_DONE = "downloadDone";
    public static final String DOWNLOAD_CANCELED = "downloadCanceled";

    public static FileDownloader of(UrlInfo urlInfo, FileInfo fileInfo, boolean forceDownload)
    {
        Preconditions.checkNotNull(urlInfo);
        Preconditions.checkNotNull(fileInfo);

        return new FileDownloader(urlInfo, fileInfo, forceDownload) {

            @Override
            public void run()
            {
                try
                {
                    downloadAndUnzip();
                    pcs.firePropertyChange(DOWNLOAD_DONE, null, DownloadableFileState.of(urlInfo.getState(), fileInfo.getState()));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    pcs.firePropertyChange(DOWNLOAD_CANCELED, null, DownloadableFileState.of(urlInfo.getState(), fileInfo.getState()));
                }
            }

        };
    }

    protected final PropertyChangeSupport pcs;
    private final UrlInfo urlInfo;
    private final File file;
    private final FileInfo fileInfo;
    private final boolean forceDownload;
    private volatile String progressMessage;
    private volatile double progress;
    private volatile boolean canceled;

    protected FileDownloader(UrlInfo urlInfo, FileInfo fileInfo, boolean forceDownload)
    {
        this.pcs = new PropertyChangeSupport(this);
        this.urlInfo = urlInfo;
        this.file = fileInfo.getState().getFile();
        this.fileInfo = fileInfo;
        this.forceDownload = forceDownload;
        this.progress = 0.;
        this.canceled = false;
        this.progressMessage = "<html>Downloading file<br>0% completed</html>";
    }

    public UrlInfo getUrlInfo()
    {
        return urlInfo;
    }

    public FileInfo getFileInfo()
    {
        return fileInfo;
    }

    public DownloadableFileState getState()
    {
        return DownloadableFileState.of(urlInfo.getState(), fileInfo.getState());
    }

    public String getProgressMessage()
    {
        return progressMessage;
    }

    protected void setProgressMessage(String progressMessage)
    {
        this.progressMessage = progressMessage;
    }

    public double getProgress()
    {
        return progress;
    }

    public void downloadAndUnzip() throws IOException, InterruptedException
    {
        download();
        unzipIfNecessary();
    }

    public void download() throws IOException, InterruptedException
    {
        // Do nothing if the URL already points to the cached file.
        UrlState urlState = urlInfo.getState();
        URL url = urlState.getUrl();
        File file = fileInfo.getState().getFile();

        if (SAFE_URL_PATHS.hasFileProtocol(url.toString()) && url.getPath().equals(file.getAbsolutePath()))
        {
            return;
        }

//        Debug.out().println("Querying FS and server before maybe downloading " + url);

        fileInfo.update();

        try (CloseableUrlConnection closeableConnection = CloseableUrlConnection.of(url, HttpRequestMethod.GET))
        {
            urlInfo.update(closeableConnection.getConnection());

            if (forceDownload || isDownloadNeeded())
            {
                if (isDownloadable())
                {
                    download(closeableConnection);
                    System.out.println("Downloaded file from " + url);
                }
                else
                {
                    Debug.out().println("Debug: unable to download " + file);
                }
            }
            else
            {
                if (isDownloadable())
                {
                    System.out.println("File cache is up to date. Skipped download from " + url);
                }
            }
        }
        catch (Exception e)
        {
            Debug.out().println("Failed attempt to download file " + url);
            throw e;
        }
    }

    public void unzipIfNecessary() throws IOException, InterruptedException
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

    public void unzip() throws IOException, InterruptedException
    {
        ZipFileUnzipper unzipper = ZipFileUnzipper.of(file);

        long totalDecompressedSize = unzipper.getTotalCompressedSize();
        setProgress(0, totalDecompressedSize);

        unzipper.addPropertyChangeListener(e -> {
            if (ZipFileUnzipper.UNZIPPING_PROGRESS.equals(e.getPropertyName()))
            {
                UnpackingStatus unpackingStatus = (UnpackingStatus) e.getNewValue();
                long unpackedByteCount = unpackingStatus.getUnpackedByteCount();
                long elapsedTime = unpackingStatus.getElapsedTime();

                setProgress(unpackedByteCount, totalDecompressedSize);

                String progressMessage = unpackingStatus.createProgressMessage("Unzipping", totalDecompressedSize);

                setProgressMessage(progressMessage);
                unpackingStatus = UnpackingStatus.of(progressMessage, unpackedByteCount, elapsedTime);
                pcs.firePropertyChange(DOWNLOAD_PROGRESS, null, unpackingStatus);
            }

            if (isCanceled())
                unzipper.cancel();
        });

        unzipper.unzip();
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public void cancel()
    {
        canceled = true;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        Preconditions.checkNotNull(listener);

        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        Preconditions.checkNotNull(listener);

        pcs.removePropertyChangeListener(listener);
    }

    protected void download(CloseableUrlConnection closeableConnection) throws IOException, InterruptedException
    {
//        Debug.out().println("Downloading " + urlInfo.getState().getUrl());

        URLConnection connection = closeableConnection.getConnection();

        long contentLength = connection.getContentLengthLong();
        long lastModifiedTime = connection.getLastModified();

        setProgress(0, contentLength);

        Path tmpFilePath = SAFE_URL_PATHS.get(file.toPath() + "-" + UUID.randomUUID().toString());

        Files.deleteIfExists(tmpFilePath);

        File tmpFile = tmpFilePath.toFile();
        File parent = tmpFile.getParentFile();

        if (parent.exists() && !parent.isDirectory())
        {
            throw new IOException(parent + " exists but is not a directory.");
        }

        parent.mkdirs();

        try (StreamGunzipper gunzipper = StreamGunzipper.of(connection.getInputStream()))
        {
            try (FileOutputStream outputStream = new FileOutputStream(tmpFile))
            {
                gunzipper.addPropertyChangeListener(e -> {
                    if (StreamUnpacker.UNPACKING_STATUS.equals(e.getPropertyName()))
                    {
                        UnpackingStatus unpackingStatus = (UnpackingStatus) e.getNewValue();
                        long unpackedByteCount = unpackingStatus.getUnpackedByteCount();
                        long elapsedTime = unpackingStatus.getElapsedTime();

                        setProgress(unpackedByteCount, contentLength);

                        String progressMessage = unpackingStatus.createProgressMessage("Downloading", contentLength);

                        setProgressMessage(progressMessage);
                        unpackingStatus = UnpackingStatus.of(progressMessage, unpackedByteCount, elapsedTime);
                        pcs.firePropertyChange(DOWNLOAD_PROGRESS, null, unpackingStatus);
                    }

                    if (isCanceled())
                        gunzipper.cancel();
                });

                gunzipper.unpack(outputStream);
            }

            // Rename the temporary file to the real name.
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
        catch (Exception e)
        {
            if (!Debug.isEnabled())
            {
                Files.deleteIfExists(tmpFilePath);
            }
            throw e;
        }

        fileInfo.update();
    }

    protected boolean isDownloadNeeded()
    {
        return !file.exists() || urlInfo.getState().getLastModified() > fileInfo.getState().getLastModified();
    }

    protected boolean isDownloadable()
    {
        return urlInfo.getState().getStatus() == UrlStatus.ACCESSIBLE;
    }

    protected void setProgress(long unpackedByteCount, long totalUnpackedByteCount)
    {
        if (totalUnpackedByteCount <= 0)
        {
            progress = 0.;
        }
        else
        {
            progress = Math.min(Math.max(100. * unpackedByteCount / totalUnpackedByteCount, 0.), 100.);
        }
    }

//    public static void main(String[] args) throws MalformedURLException
//    {
//        File file = SafeURLPaths.instance().get(System.getProperty("user.home"), "Downloads", "spud").toFile();
//
//        FileDownloader downloader = FileDownloader.of(UrlInfo.of(new URL("http://sbmt.jhuapl.edu")), FileInfo.of(file), true);
//        try
//        {
//            downloader.download();
//            System.out.println("Done");
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
}