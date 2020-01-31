package edu.jhuapl.saavtk.util;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileInfo.FileStatus;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

public final class FileCache
{
    private static volatile boolean silenceInfoMessages = false;

    // TODO this should extend Exception and thus be checked.
    public static class NoInternetAccessException extends RuntimeException
    {
        private static final long serialVersionUID = -1250977612922624246L;
        private final URL url;

        private NoInternetAccessException(String cause, URL url)
        {
            super(cause);
            this.url = url;
        }

        private NoInternetAccessException(Exception cause, URL url)
        {
            super(cause);
            this.url = url;
        }

        public URL getURL()
        {
            return url;
        }
    }

    // TODO this should extend Exception and thus be checked.
    public static class UnauthorizedAccessException extends RuntimeException
    {
        private static final long serialVersionUID = 7671006960310656926L;
        private final URL url;

        private UnauthorizedAccessException(String cause, URL url)
        {
            super(cause);
            this.url = url;
        }

        private UnauthorizedAccessException(Exception cause, URL url)
        {
            super(cause);
            this.url = url;
        }

        public URL getURL()
        {
            return url;
        }
    }

    // TODO this should extend Exception and thus be checked.
    public static class NonexistentRemoteFile extends RuntimeException
    {
        private static final long serialVersionUID = 7671006960310656926L;
        private final URL url;

        private NonexistentRemoteFile(String cause, URL url)
        {
            super(cause);
            this.url = url;
        }

        private NonexistentRemoteFile(Exception cause, URL url)
        {
            super(cause);
            this.url = url;
        }

        public URL getURL()
        {
            return url;
        }
    }

    /**
     * Deprecated in favor of utilities in SafeURLPaths such as getString(...) and
     * getUrl(...), which encapsulate forming strings that contain valid file paths
     * and URLs.
     */
    @Deprecated
    public static final String FILE_PREFIX = "file://";

    private static DownloadableFileManager downloadableManager = null;

    public static void setSilenceInfoMessages(boolean enable)
    {
        silenceInfoMessages = enable;
        DownloadableFileManager.setSilenceInfoMessages(enable);
        FileDownloader.setSilenceInfoMessages(enable);
        UrlAccessManager.setSilenceInfoMessages(enable);
    }

    public static DownloadableFileManager instance()
    {
        if (downloadableManager == null)
        {
            downloadableManager = createDownloadManager();
        }
        return downloadableManager;
    }

    public static DownloadableFileState getState(String urlString)
    {
        return instance().query(urlString, false);
    }

    public static DownloadableFileState refreshStateInfo(String urlString)
    {
        return instance().query(urlString, true);
    }

    /**
     * 
     * @param urlString
     * @return
     */
    public static File getFileFromServer(String urlString)
    {
        Preconditions.checkNotNull(urlString);

        DownloadableFileState fileState = instance().getState(urlString);
        URL url = fileState.getUrlState().getUrl();
        File file = fileState.getFileState().getFile();

        RuntimeException exception = null;
        try
        {
            fileState = instance().getDownloadedFile(urlString, false);
        }
        catch (FileNotFoundException e)
        {
            exception = new NonexistentRemoteFile(e, url);
        }
        catch (ConnectException | UnknownHostException e)
        {
            exception = new NoInternetAccessException(e, url);
        }
        catch (Exception e)
        {
            exception = new RuntimeException("Getting file " + url, e);
        }

        if (file.exists())
        {
            if (exception != null)
            {
                if (!silenceInfoMessages)
                {
                    System.err.println("Warning: cached file exists, but unable to update cache from URL: " + url);
                    System.err.println("Ignored the following exception:");
                    exception.printStackTrace();
                }
            }
        }
        else
        {
            if (exception == null)
            {
                if (fileState.getUrlState().getStatus() == UrlStatus.NOT_AUTHORIZED)
                {
                    exception = new UnauthorizedAccessException("Cannot get file: access is restricted to URL: " + url, url);
                }
                else
                {
                    exception = new RuntimeException("Unknown problem trying to update file from URL: " + url);
                }
            }

            throw exception;
        }

        return file;
    }

    private static DownloadableFileManager createDownloadManager()
    {
        DownloadableFileManager result = DownloadableFileManager.of(Configuration.getDataRootURL(), new File(Configuration.getCacheDir()));

        return result;
    }

    /**
     * Determine if it appears that the provided data object identifier could be
     * downloaded and/or accessed. If the server is in use this will check whether
     * the file exists on the server. Otherwise it checks whether the file already
     * exists on disk. This will not actually open or download any files.
     * 
     * @param urlOrPathSegment the input URL string or path segment
     * @return true if it appears the file could be successfully downloaded/used
     * @throws UnauthorizedAccessException if a 401/403 (Unauthorized/Forbidden)
     *             error is encountered when attempting to access the server for the
     *             remote file
     */
    public static boolean isFileGettable(String urlOrPathSegment)
    {
        Preconditions.checkNotNull(urlOrPathSegment);

        boolean result = false;

        DownloadableFileState state = instance().query(urlOrPathSegment, false);

        if (state.getFileState().getStatus() == FileStatus.ACCESSIBLE)
        {
            result = true;
        }
        else
        {
            switch (state.getUrlState().getStatus())
            {
            case ACCESSIBLE:
                result = true;
                break;
            case NOT_AUTHORIZED:
                throw new UnauthorizedAccessException("Cannot access information about restricted URL: ", state.getUrlState().getUrl());
            default:
                break;
            }
        }

        return result;
    }

    /**
     * The function loads a file from the server and returns its contents as a list
     * of strings, one line per string.
     * 
     * @param filename file to read
     * @return contents of file as list of strings
     * @throws IOException
     */
    public static List<String> getFileLinesFromServerAsStringList(String filename) throws IOException
    {
        File file = getFileFromServer(filename);
        InputStream fs = new FileInputStream(file);
        if (filename.toLowerCase().endsWith(".gz"))
            fs = new GZIPInputStream(fs);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        List<String> lines = new ArrayList<>();
        String line;

        while ((line = in.readLine()) != null)
        {
            lines.add(line);
        }

        in.close();

        return lines;
    }

    /**
     * In "offline" mode, instead of querying a server, the image of the server is
     * expected to reside in a directory in the local file system. *
     * 
     * @param offlineMode if true, use the local directory as the "top" of the
     *            server.
     * @param offlineModeRootFolder if offlineMode is true, this is the location of
     *            the top of the local server hierarchy
     */
    public static void setOfflineMode(boolean offlineMode, String offlineModeRootFolder)
    {
        if (offlineMode)
        {
            Preconditions.checkNotNull(offlineModeRootFolder);
        }

        instance().setEnableServerAccess(!offlineMode);
    }

    public static void addServerUrlPropertyChangeListener(PropertyChangeListener listener)
    {
        DownloadableFileInfo rootInfo = instance().getRootInfo();
        rootInfo.addPropertyChangeListener(listener);
    }

    private FileCache()
    {
        throw new AssertionError();
    }

}
