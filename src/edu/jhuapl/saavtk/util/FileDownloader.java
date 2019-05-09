package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import javax.swing.SwingWorker;

import com.google.common.base.Preconditions;
import com.google.common.io.CountingInputStream;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;

public class UrlDownloader extends SwingWorker<Void, Void>
{
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    public static UrlDownloader of(UrlInfo urlInfo, File file)
    {
        Preconditions.checkNotNull(urlInfo);
        Preconditions.checkNotNull(file);

        return new UrlDownloader(urlInfo, file);
    }

    private final UrlInfo urlInfo;
    private final File file;

    protected UrlDownloader(UrlInfo urlInfo, File file)
    {
        this.urlInfo = urlInfo;
        this.file = file;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        download();
        return null;
    }

    protected void download() throws IOException, InterruptedException
    {
        try (CloseableUrlConnection closeableConnection = CloseableUrlConnection.of(urlInfo, HttpRequestMethod.GET))
        {
            download(closeableConnection);
        }
    }

    protected void download(CloseableUrlConnection closeableConnection) throws IOException, InterruptedException
    {
        URLConnection connection = closeableConnection.getConnection();

        urlInfo.update(connection);

        URL url = connection.getURL();
        boolean gunzip = url.getPath().toLowerCase().endsWith(".gz");

        long totalByteCount = connection.getContentLengthLong();
        long lastModifiedTime = connection.getLastModified();

        File parentFile = file.getParentFile();
        Path tmpFilePath = SAFE_URL_PATHS.get(parentFile.getPath(), UUID.randomUUID().toString());

        // Don't use try with resources because there are two references to the same
        // underlying stream.
        CountingInputStream countingInputStream = null;
        InputStream inputStream = null;

        try
        {
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
                    while ((len = inputStream.read(buff)) > 0 && !isCancelled())
                    {
                        os.write(buff, 0, len);
                        setProgress((int) (100. * countingInputStream.getCount() / totalByteCount));
                    }

                    if (isCancelled())
                    {
                        throw new InterruptedException();
                    }

                    setProgress(100);
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

        // Okay, now rename the file to the real name.
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

}