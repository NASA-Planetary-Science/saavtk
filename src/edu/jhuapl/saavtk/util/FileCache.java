package edu.jhuapl.saavtk.util;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.input.CountingInputStream;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.DownloadableFileInfo.DownloadableFileState;
import edu.jhuapl.saavtk.util.FileCache.FileInfo.YesOrNo;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

public final class FileCache
{
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

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
     * Class encapsulating what is known about files in the FileCache and the URLs
     * whence they were downloaded. There is also dynamically updated information
     * about file downloads in progress.
     * 
     * Use {@link UrlInfo} instead.
     */
    @Deprecated
    public static final class FileInfo
    {
        // TODO move this somewhere?
        public enum YesOrNo
        {
            YES,
            NO,
            UNKNOWN
        }

        private final URL url;
        private final File file;
        private final YesOrNo urlAccessAuthorized;
        private final YesOrNo existsOnServer;
        private final long lastModified;
        private volatile long totalByteCount;
        private volatile long byteCount;
        private volatile boolean abortDownloadRequested;

        /**
         * Create a file information object from the provided arguments.
         * 
         * @param url the source URL for the data object
         * @param file the file in the local file system
         * @param urlAccessAuthorized flag indicating whether access to the URL was
         *            obtained, or if this is unknown
         * @param existsOnServer flag indicating whether the object referred to by the
         *            URL exists, or if this is unknown
         * @param lastModified the last modification time of the URL, or 0 if unkonwn
         */
        private FileInfo(URL url, File file, YesOrNo urlAccessAuthorized, YesOrNo existsOnServer, long lastModified)
        {
            Preconditions.checkNotNull(url);
            Preconditions.checkNotNull(file);
            Preconditions.checkNotNull(urlAccessAuthorized);
            Preconditions.checkNotNull(existsOnServer);
            Preconditions.checkNotNull(lastModified);
            this.url = url;
            this.file = file;
            this.urlAccessAuthorized = urlAccessAuthorized;
            this.existsOnServer = existsOnServer;
            this.lastModified = lastModified;
            this.totalByteCount = 0;
            this.byteCount = 0;
            this.abortDownloadRequested = false;
        }

        public URL getURL()
        {
            return url;
        }

        public File getFile()
        {
            return file;
        }

        public YesOrNo isURLAccessAuthorized()
        {
            return urlAccessAuthorized;
        }

        public YesOrNo isExistsOnServer()
        {
            return existsOnServer;
        }

        public long getLastModifiedTime()
        {
            return lastModified;
        }

        /**
         * Determine whether the remote file needs to be downloaded.
         * 
         * @return true if the local file does not exist or the remote file has been
         *         modified more recently than the local file, false otherwise
         */
        public boolean isNeedToDownload()
        {
            File file = getFile();
            if (!file.exists())
            {
                return true;
            }

            return file.lastModified() < getLastModifiedTime();
        }

        public boolean isExistsLocally()
        {
            return file.exists();
        }

        public long getTotalByteCount()
        {
            return totalByteCount > 0 ? totalByteCount : 0;
        }

        private void setTotalByteCount(long totalByteCount)
        {
            this.totalByteCount = totalByteCount;
        }

        public long getByteCount()
        {
            return byteCount > 0 ? byteCount : 0;
        }

        private void setByteCount(long byteCount)
        {
            this.byteCount = byteCount;
        }

        public double getPercentDownloaded()
        {
            final long byteCount = getByteCount();
            if (byteCount == 0)
            {
                return 0.;
            }
            final long totalByteCount = getTotalByteCount();
            return totalByteCount > 0 ? (double) byteCount / totalByteCount : 0.;
        }

        public void requestAbortDownload()
        {
            abortDownloadRequested = true;
        }

        private void startDownload()
        {
            abortDownloadRequested = false;
        }

        private void maybeAbort()
        {
            if (abortDownloadRequested)
            {
                throw new RuntimeException("Download aborted");
            }
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(getURL());
            builder.append(" (auth = ");
            builder.append(isURLAccessAuthorized());
            builder.append(", exists = ");
            builder.append(isExistsOnServer());
            builder.append(") -> ");
            builder.append(getFile());
            builder.append(" (exists = ");
            builder.append(isExistsLocally());
            builder.append(", need-dl = ");
            builder.append(isNeedToDownload());
            builder.append(")");
            return builder.toString();
        }
    }

    /**
     * Deprecated in favor of utilities in SafeURLPaths such as getString(...) and
     * getUrl(...), which encapsulate forming strings that contain valid file paths
     * and URLs.
     */
    @Deprecated
    public static final String FILE_PREFIX = "file://";

    private static final ConcurrentHashMap<File, FileInfo> INFO_MAP = new ConcurrentHashMap<>();
    private static boolean showDotsForFiles = false;
    private static String offlineModeRootFolder;

    private static final DownloadableFileManager downloadableManager = createDownloadManager();

    public static DownloadableFileManager instance()
    {
        return downloadableManager;
    }

    public static URL getURL(String urlString)
    {
        return downloadableManager.getState(urlString).getUrlState().getUrl();
    }

    public static DownloadableFileState getState(String urlString)
    {
        return downloadableManager.query(urlString, false);
    }

    public static DownloadableFileState refreshStateInfo(String urlString)
    {
        return downloadableManager.query(urlString, true);
    }

    public static boolean isDownloadNeeded(String urlString)
    {
        DownloadableFileState state = getState(urlString);

        File file = state.getFileState().getFile();

        return !file.exists() || state.getUrlState().getLastModified() > file.lastModified();
    }

    public static DownloadableFileState refreshDownload(String urlString) throws IOException, InterruptedException
    {
        return instance().getDownloadedFile(urlString, false);
    }

    public static FileDownloader getDownloader(String urlString)
    {
        return instance().getDownloader(urlString, false);
    }

    /**
     * Return a File object representing the fully qualified location in the cache
     * where the supplied URL was or would be downloaded. This method does not
     * download the file, nor query the server for any information about the URL.
     * Thus this method implies nothing about whether File.exists() will return true
     * or false.
     * 
     * @param urlString the URL
     * @return the File object
     */
    public static File getDownloadFile(String urlString)
    {
        return getState(urlString).getFileState().getFile();
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
            fileState = downloadableManager.getDownloadedFile(urlString, false);
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
                System.err.println("Cached file exists, but unable to update cache from URL: " + url);
                System.err.println("Ignored the following exception:");
                exception.printStackTrace();
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

//        result.startAccessMonitor();

        return result;
    }

    /**
     * Get information about the resource identified by the provided URL string or
     * path segment. If the argument specifies a lexically valid URL string, it is
     * used *without modification*. If the argument specifies a path segment such as
     * "local/directory/filename.ext", a URL is constructed by tacking the path
     * segment onto the data root URL provided by the {@link Configuration} class.
     * 
     * It is legal for path segments to have file-system-specific path delimiters.
     * These will automatically be converted into forward slashes / in the URL.
     * 
     * For all but local file ("file:/") type URLs, a connection is opened to obtain
     * information about the URL and its referent remote data object.
     * 
     * For all URLs, information is also obtained about the location of the
     * corresponding local file in the file system (and the file itself if it is
     * present).
     * 
     * The returned file information object is cached, so that subsequent calls to
     * this method will run more quickly because they will not open another
     * connection. This means that the returned modication time and other
     * information about the remote file will not in general be refreshed in
     * subsequent calls. An exception to this is that new connections will be made
     * and the information refreshed for any URL for which authorization initially
     * failed (401 or 403 error). This will happen each time this method is called
     * until authorization is granted (or the program exits).
     * 
     * @param urlOrPathSegment the input URL string or path segment
     * @return the file/URL information
     * 
     *         DEPRECATED: use getUrlInfo()/refreshUrlInfo() instead.
     */
    @Deprecated
    public static FileInfo getFileInfoFromServer(String urlOrPathSegment)
    {
        Preconditions.checkNotNull(urlOrPathSegment);

        // Clean up the path or URL.
        urlOrPathSegment = SAFE_URL_PATHS.getString(urlOrPathSegment);

        URL url = null;
        URL dataRootUrl = Configuration.getDataRootURL();
        try
        {
            // First parse the whole thing to see if it can be done without
            // throwing an exception.
            url = new URL(urlOrPathSegment);

            // Handle case where this URL starts at the top
            // of the server path.
            if (urlOrPathSegment.startsWith(dataRootUrl.toString() + "/"))
            {
                urlOrPathSegment = urlOrPathSegment.substring(dataRootUrl.toString().length() + 1);
            }
            else
            {
                // Extract the path portion of the URL.
                urlOrPathSegment = url.getFile();
            }
        }
        catch (@SuppressWarnings("unused") MalformedURLException e)
        {
            // Assume the argument is a path segment and use it to
            // construct the URL relative to the data root.
            try
            {
                url = new URL(toUrlSegment(dataRootUrl.toString(), urlOrPathSegment));
            }
            catch (MalformedURLException e1)
            {
                throw new IllegalArgumentException(e1);
            }
        }
        return getFileInfoFromServer(url, urlOrPathSegment);
    }

    /**
     * Get information about the cached file, which is identified by the provided
     * URL object, and located in the cache using the provided path segment.
     *
     * @param url the complete URL used without modification
     * @param pathSegment the path relative to the data cache top for the local
     *            object
     * @return the file information object
     * 
     *         DEPRECATED: this is a utility method superseded by
     *         UrlInfo/UrlAccessManager.
     */
    @Deprecated
    private static FileInfo getFileInfoFromServer(final URL url, String pathSegment)
    {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(pathSegment);

        if (!Configuration.useFileCache())
        {
            throw new UnsupportedOperationException("This method is not currently supported if the file cache is disabled.");
        }

        final String ungzippedPath = pathSegment.toLowerCase().endsWith(".gz") ? pathSegment.substring(0, pathSegment.length() - 3) : pathSegment;

        if (getOfflineMode())
        {
            // It's possible there is information about this file already from a previous
            // query
            // when offlineMode was disabled.
            File file = url.getProtocol().equalsIgnoreCase("file") ? SAFE_URL_PATHS.get(url.getFile()).toFile() : SAFE_URL_PATHS.get(offlineModeRootFolder, ungzippedPath).toFile();
            FileInfo info = INFO_MAP.get(file);
            if (info == null)
            {
                // Didn't already have info about this file, so return file info with
                // partial information.
                info = new FileInfo(url, file, YesOrNo.UNKNOWN, YesOrNo.UNKNOWN, 0);
            }
            return info;
        }

        if (ungzippedPath.equals(pathSegment) && url.getProtocol().equalsIgnoreCase("file"))
        {
            // File "on the server" is not gzipped, and is allegedly on local file system,
            // so just try to use it directly.
            File file = SAFE_URL_PATHS.get(url.getFile()).toFile();

            FileInfo info = INFO_MAP.get(file);
            if (info == null)
            {
                info = new FileInfo(url, file, YesOrNo.YES, file.exists() ? YesOrNo.YES : YesOrNo.NO, file.lastModified());
                INFO_MAP.put(file, info);
            }
            return info;
        }

        // Local file must be gunzipped, so need the full FileInfo no matter where the
        // URL points.
        File file = SAFE_URL_PATHS.get(Configuration.getCacheDir(), ungzippedPath).toFile();

        try
        {
            FileInfo info = INFO_MAP.get(file);
            if (info == null && file.isDirectory())
            {
                info = new FileInfo(url, file, YesOrNo.YES, YesOrNo.YES, 0);
            }

            if (info == null || info.isURLAccessAuthorized() != YesOrNo.YES)
            {
                info = getFileInfo(url, file);
            }

            INFO_MAP.put(file, info);
        }
        finally
        {
            INFO_MAP.putIfAbsent(file, new FileInfo(url, file, YesOrNo.UNKNOWN, YesOrNo.UNKNOWN, 0));
        }

        return INFO_MAP.get(file);
    }

    /**
     * DEPRECATED: this is a utility method superseded by UrlInfo/UrlAccessManager.
     */
    @Deprecated
    private static FileInfo getFileInfo(URL url, File file)
    {
        // This code is based on code from stacktrace. The stacktrace code
        // specifically included disabling redirects, but that leads to
        // spurious 301 errors, so leaving these in here commented out.
        // HttpURLConnection.setFollowRedirects(false);
        YesOrNo authorized = YesOrNo.UNKNOWN;
        YesOrNo urlExists = YesOrNo.UNKNOWN;
        long lastModified = 0;
        HttpURLConnection httpConnection = null;
        try
        {
            URL uncachedUrl = new URL(url.toString() + "?cacheFrom=" + System.currentTimeMillis());
            URLConnection connection = uncachedUrl.openConnection();

            Debug.out().println("Opened connection for info to " + url);
            if (!Debug.isEnabled() && showDotsForFiles)
            {
                System.out.print('.');
            }

            connection.setUseCaches(false);

            // These two properties seem to be still necessary as of 2017-12-19.
            connection.setRequestProperty("User-Agent", "Mozilla/4.0");
            connection.setRequestProperty("Accept", "*/*");
            if (connection instanceof HttpURLConnection)
            {
                httpConnection = (HttpURLConnection) connection;

                httpConnection.setRequestMethod("HEAD");

                httpConnection.setConnectTimeout(5000);
                httpConnection.setReadTimeout(5000);

                int code = httpConnection.getResponseCode();

                if (code == HttpURLConnection.HTTP_OK)
                {
                    authorized = YesOrNo.YES;
                    urlExists = YesOrNo.YES;
                }
                else if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN)
                {
                    authorized = YesOrNo.NO;
                }
                else if (code == HttpURLConnection.HTTP_NOT_FOUND)
                {
                    authorized = YesOrNo.YES;
                    urlExists = YesOrNo.NO;
                }
            }
            lastModified = connection.getLastModified();
        }
        catch (ConnectException | UnknownHostException e)
        {
            throw new NoInternetAccessException(e, url);
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (message != null && (message.contains("401") || message.contains("403")))
            {
                e.printStackTrace();
                authorized = YesOrNo.NO;
            }
            else if (e instanceof SocketTimeoutException)
            {
                authorized = YesOrNo.UNKNOWN;
            }
        }
        finally
        {
            if (httpConnection != null)
            {
                httpConnection.disconnect();
            }
        }

        return new FileInfo(url, file, authorized, urlExists, lastModified);
    }

    /**
     * Determine if it appears that the provided data object identifier could be
     * downloaded and/or accessed. (See getFileInfoFromServer method for more
     * details). If the server is in use this will check whether the file exists on
     * the server. Otherwise it checks whether the file already exists on disk. This
     * will not actually open or download any files.
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

        DownloadableFileState state = getState(urlOrPathSegment);

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
     * Get (download) the file from the server. Place it in the cache for future
     * access. If the path begins with "file:/", then the file is assumed to be
     * local on disk and no server is contacted. Files resident in the local file
     * system will be simply used directly unless they are gzipped, in which case
     * the local file is gunzipped into the local cache.
     *
     * Note that this method will try to get the file even for some cases in which
     * isFileGettable returns false.
     * 
     * @param urlOrPathSegment the URL
     * @return the local file object; however, the file on disk may not exist
     * @throws NoInternetAccessException if a 401 (Unauthorized) error is
     *             encountered when attempting to access the server for the remote
     *             file.
     * 
     *             DEPRECATED: this was the original workhorse method for getting
     *             files out of the cache. It's been superseded by the capabilities
     *             of UrlInfo, UrlAccessManager and FileCacheManager. Leaving it
     *             here just for reference as nothing calls it nor should call it.
     */
    @Deprecated
    public static File getFileFromServerOrig(String urlOrPathSegment) throws NoInternetAccessException
    {
        FileInfo fileInfo = getFileInfoFromServer(urlOrPathSegment);
        URL url = fileInfo.getURL();

        if (fileInfo.isURLAccessAuthorized() == YesOrNo.NO)
        {
            throw new UnauthorizedAccessException("Cannot get file: access is restricted to URL: " + url, url);
        }

        if (fileInfo.isNeedToDownload())
        {
            if (fileInfo.isExistsOnServer() == YesOrNo.NO)
            {
                throw new NonexistentRemoteFile("File pointed to does not exist: " + url, url);
            }

            if (getOfflineMode())
            {
                throw new NoInternetAccessException("Offline mode; unable to retrieve " + url, url);
            }

            try
            {
                final File file = fileInfo.getFile();
                final Path tmpFilePath = SAFE_URL_PATHS.get(file + FileUtil.getTemporarySuffix());
                long lastModified = 0;

                Files.deleteIfExists(tmpFilePath);

                fileInfo.startDownload();
                try (WrappedInputStream wrappedStream = new WrappedInputStream(fileInfo))
                {
                    final long totalByteCount = wrappedStream.getTotalByteCount();
                    fileInfo.setTotalByteCount(totalByteCount);
                    File tmpFile = tmpFilePath.toFile();

                    file.getParentFile().mkdirs();
                    fileInfo.maybeAbort();
                    try (FileOutputStream os = new FileOutputStream(tmpFile))
                    {
                        InputStream is = wrappedStream.getStream();
                        final int bufferSize = Math.max(is.available(), 8192);
                        byte[] buff = new byte[bufferSize];
                        int len;
                        while ((len = is.read(buff)) > 0)
                        {
                            fileInfo.setByteCount(wrappedStream.getByteCount());
                            fileInfo.maybeAbort();
                            os.write(buff, 0, len);
                        }
                        fileInfo.setByteCount(totalByteCount);
                        fileInfo.maybeAbort();

                        // Change the modified time of the file to that of the server.
                        lastModified = wrappedStream.getLastModifiedTime();
                        if (lastModified > 0)
                            tmpFile.setLastModified(lastModified);
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

                // Okay, now rename the file to the real name.
                File tmpFile = tmpFilePath.toFile();
                file.delete();
                if (!tmpFile.renameTo(file))
                {
                    throw new IOException("Failed to rename temporary file " + tmpFile);
                }

                // Change the modified time again just in case the process of
                // renaming the file caused the modified time to change.
                // (On Linux, changing the filename, does not change the modified
                // time so this is not necessary, but I'm not sure about other platforms)
                if (lastModified > 0)
                    file.setLastModified(lastModified);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        return fileInfo.getFile();
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

    public static boolean getOfflineMode()
    {
        return !downloadableManager.isServerAccessEnabled();
    }

    public static void setOfflineMode(boolean offlineMode)
    {
        setOfflineMode(offlineMode, offlineModeRootFolder != null ? offlineModeRootFolder : Configuration.getCacheDir());
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
        FileCache.offlineModeRootFolder = offlineModeRootFolder;

        downloadableManager.setEnableServerAccess(!offlineMode);
    }

    public static void addServerUrlPropertyChangeListener(PropertyChangeListener listener)
    {
        DownloadableFileInfo rootInfo = instance().getRootInfo();
        rootInfo.addPropertyChangeListener(listener);
    }

    /**
     * Toggle whether dots are shown for each file whose information is downloaded
     * from the server.
     * 
     * @param enable if true, dots will be shown, if false, no dots will be shown.
     */
    public static void showDotsForFiles(boolean enable)
    {
        showDotsForFiles = enable;
        FileDownloader.setShowDotsForFileChecks(enable);
    }

    private static String toUrlSegment(String firstSegment, String... pathSegments)
    {
        // Concatenate the paths safely with a single delimiter.
        return SAFE_URL_PATHS.getString(firstSegment, pathSegments);
    }

    private FileCache()
    {
        throw new AssertionError();
    }

    private static final class WrappedInputStream implements Closeable
    {
        private final long totalByteCount;
        private final long lastModifiedTime;
        private InputStream inputStream;
        private final CountingInputStream countingInputStream;

        private WrappedInputStream(FileInfo fileInfo) throws IOException
        {
            URL url = fileInfo.getURL();
            final boolean gunzip = url.getPath().toLowerCase().endsWith(".gz");

            URLConnection connection = url.openConnection();
            Debug.out().println("Opened connection for download to " + url);
            if (!Debug.isEnabled() && showDotsForFiles)
            {
                System.out.print('.');
            }

            // These two properties seem to be still necessary as of 2017-12-19.
            connection.setRequestProperty("User-Agent", "Mozilla/4.0");
            connection.setRequestProperty("Accept", "*/*");

            this.totalByteCount = connection.getContentLengthLong();
            this.lastModifiedTime = connection.getLastModified();
            this.inputStream = connection.getInputStream();
            this.countingInputStream = new CountingInputStream(this.inputStream);
            this.inputStream = this.countingInputStream;
            if (gunzip)
            {
                this.inputStream = new GZIPInputStream(this.inputStream);
            }
        }

        @Override
        public void close() throws IOException
        {
            if (inputStream != null)
            {
                inputStream.close();
                inputStream = null;
            }
        }

        public InputStream getStream()
        {
            return inputStream;
        }

        public long getByteCount()
        {
            return countingInputStream.getByteCount();
        }

        public long getTotalByteCount()
        {
            return totalByteCount;
        }

        public long getLastModifiedTime()
        {
            return lastModifiedTime;
        }
    }

}
