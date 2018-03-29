package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.input.CountingInputStream;

import edu.jhuapl.saavtk.util.FileCache.FileInfo.YesOrNo;

public class FileCache
{
	// TODO this should extend Exception and thus be checked, but waiting to
	// see whether this works out before going that route, since it would
	// require many files to change.
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

	public static final String FILE_PREFIX = "file://";

    // Stores files already downloaded in this process
    private static ConcurrentHashMap<String, Object> downloadedFiles =
        new ConcurrentHashMap<>();

    private static volatile boolean abortDownload = false;

    // Download progress. Equal to number of bytes downloaded so far.
    private static volatile long downloadProgress = 0;

    // If true do not make a network connection to get the file but only retrieve
    // it from the cache if it exists. Usually set to false, but some batch scripts
    // may set it to true.
    private static boolean offlineMode = false;

    // When in offline mode, files are retrieved relative to this folder
    private static String offlineModeRootFolder = null;

    private static boolean showDotsForFiles = false;

    public static void showDotsForFiles(boolean enable)
    {
    	showDotsForFiles = enable;
    }

    /**
     * Information returned about a remote file on the server
     */
    public static class FileInfo
    {
    	public enum YesOrNo {
    		YES,
    		NO,
    		UNKNOWN
    	}

    	// Remote location of resource.
    	private final URL url;
    	
        // The location on disk of the file if actually downloaded or the location the file
        // would have if downloaded.
        private final File file;

        // The number of bytes in the file; may be the number in local file OR on the server.
        private final long length;
        
        // If the the file was not actually downloaded, this variable stores whether it
        // needs to be downloaded (i.e. if it is out of date)
        private final boolean needToDownload;

        private final YesOrNo authorized;
		private final YesOrNo existsOnServer;
		private final boolean existsLocally;
		private final boolean failedToDownload;

        private FileInfo(String dataRoot, String path, boolean doDownloadIfNeeded)
        {
        	// Ensure path has clean form and strip off compression suffix.
        	path = cleanPath(path);
        	final String unzippedPath = path.toLowerCase().endsWith(".gz") ? path.substring(0, path.length() - 3) : path;

        	// Construct URL that may be used to access the remote file and the local File object.
        	URL url = null;
            String fileName = null;

            if (dataRoot.startsWith(FILE_PREFIX) || !Configuration.useFileCache())
            {
            	url = getURL(dataRoot + unzippedPath);
            	fileName = url.getPath();
            }
            else if (offlineMode)
            {
            	url = getURL(dataRoot + path);
            	fileName = SafePaths.getString(offlineModeRootFolder, unzippedPath);
            }
            else
            {
            	url = getURL(dataRoot + path);
            	fileName = SafePaths.getString(Configuration.getCacheDir(), unzippedPath);
            }

            File file = new File(fileName);
            long length = file.exists() ? file.length() : -1;

            // Confirm accessibility of URL and its existence.
            YesOrNo authorized = YesOrNo.UNKNOWN;
            YesOrNo urlExists = YesOrNo.UNKNOWN;
            boolean needToDownload = false;
            boolean failedToDownload = false;
            if (file.exists() && (downloadedFiles.containsKey(path) || file.isDirectory()))
            {
            	// The file was present and it has been checked already while this
            	// instance is running. Assume URL is authorized. No need to open
            	// a connection and/or download the file.
            	authorized = YesOrNo.YES;
            }
            else if (!offlineMode)
            {
            	try
            	{
            		// This test is based on code from stacktrace. The stacktrace code
            		// specifically included disabling redirects, but that leads to
            		// spurious 301 errors, so leaving these in here commented out.
//            		HttpURLConnection.setFollowRedirects(false);
            		URLConnection connection = url.openConnection();
            		Debug.out().println("FileInfo(): opened connection to " + url);
            		if (!Debug.isEnabled() && showDotsForFiles)
            		{
            			System.out.print('.');
            		}
            		// These two properties seem to be still necessary as of 2017-12-19.
        			connection.setRequestProperty("User-Agent", "Mozilla/4.0");
        			connection.setRequestProperty("Accept","*/*");
        			if (!doDownloadIfNeeded && connection instanceof HttpURLConnection)
        			{
        				// This access test seems to run quicker, doesn't throw as
        				// many exceptions, and gives more detailed information, so
        				// use it when just getting file information. However, because it
        				// only reads the header, this version is not suitable for when
        				// doDownloadIfNeeded is true.
        				HttpURLConnection httpConnection = (HttpURLConnection) connection;
        				// See note above.
//        				httpConnection.setInstanceFollowRedirects(false);

        				httpConnection.setRequestMethod("HEAD");
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
        			else
        			{
        				// This access test is much simpler and perhaps more robust, but gives less info
        				// and is slower because it gets an input stream to the whole file.
        				// Count on this throwing an exception if we don't have access or the file doesn't exist.
            			connection.getInputStream();
            			// It didn't throw, so assume all is well.
            			authorized = YesOrNo.YES;
            			urlExists = YesOrNo.YES;
            		}

            		// Check file existence and modification time to decide if we need to download.
        			// TODO: this seems always to return 0: does this in fact work?
            		long urlLastModified = connection.getLastModified();
        			needToDownload = (!file.exists() || file.lastModified() < urlLastModified) && !file.isDirectory();
            		if (needToDownload)
            		{
            			// TODO: the length is probably not correct if just the header was read above.
            			// Check into this and correct the behavior. Ideally length should not be an
            			// element of this class. The length is used only by progress monitors.
                        String contentLengthStr = connection.getHeaderField("content-length");
                        if (contentLengthStr != null)
                        {                        	
                        	length = Long.parseLong(contentLengthStr);
                        }
                        if (doDownloadIfNeeded)
                        {
                            File cachedFile = addToCache(path, connection.getInputStream(), urlLastModified, length);
                            if (cachedFile != null) // file can be null if the user aborted the download
                            {
                                downloadedFiles.put(path, "");
                                file = cachedFile;
                                length = file.length();
                                needToDownload = false;
                            }
                            else
                            {
                            	failedToDownload = true;
                            }
                        }
            		}
            	}
            	catch (ProtocolException e)
            	{
            		e.printStackTrace();
            		authorized = YesOrNo.NO;
            	}
            	catch (IOException e)
            	{
            		String message = e.getMessage();
            		if (message != null && (message.contains("401") || message.contains("403")))
            		{
            			e.printStackTrace();
            			authorized = YesOrNo.NO;
            		}
            	}
            }

            this.url = url;
            this.file = file;
            this.length = length;
            this.needToDownload = needToDownload;
            this.authorized = authorized;
            this.existsOnServer = urlExists;
            this.existsLocally = file.exists();
            this.failedToDownload = failedToDownload;
        }

        public URL getURL()
        {
        	return url;
        }

        public File getFile()
        {
        	return file;
        }

        public long getLength() {
        	return length;
        }
        
        public boolean isNeedToDownload() {
        	return needToDownload;
        }
        
        public YesOrNo isURLAccessAuthorized()
        {
        	return authorized;
        }

        public YesOrNo isExistsOnServer()
        {
        	return existsOnServer;
        }

		public boolean isExistsLocally() {
			return existsLocally;
		}

		public boolean isFailedToDownload() {
			return failedToDownload;
		}

		private static URL getURL(String url)
        {
            try
            {
            	return new URL(url);
            }
        	catch (MalformedURLException e)
        	{
        		throw new AssertionError(e);
        	}

        }
    }

    /**
     * This function is used to both download a file from a server as well as to
     * check if the file is out of date and needs to be downloaded. This depends
     * on the doDownloadIfNeeded parameter. If set to true it will download
     * the file if needed using the rules described below. If false, nothing
     * will be downloaded, but the server will be queried to see if a newer
     * version exists.
     *
     * If the file is requested to be actually downloaded from server it is placed
     * in the cache when downloaded so it does not need to be downloaded a second time.
     * The precise rules for determining whether or not we download the file from the
     * server or use the file already in the cache are as follows:
     *
     * - If the file does not exist in the cache, download it. - If the file
     * does exist, and was already downloaded by this very process (files
     * already downloaded are stored in the downloadedFiles hash set), then
     * return the cached file without comparing last modified times. - If the
     * file does exist, and has not been previously downloaded by this process,
     * compare the last modified time of the cached file to the remote file on
     * server. If the remote file is newer, download it, otherwise return the
     * cached file. - If there was a failure connecting to the server simply
     * return the file if it exists in the cache. - If the file could not be
     * retrieved for any reason, null is returned.
     *
     * Note the cache mirrors the file hierarchy on the server.
     *
     * Note also that if the Root URL (as returned by Configuration.getDataRootURL())
     * begins with "file://", then that means the "server" is not really an http server but
     * is really the local disk. In such a situation the cache is not used and the file
     * is returned directly. This is useful for running batch scripts so no http connections
     * are made. If the file is gzipped, you will need to manually gunzip (in the same folder)
     * it in order for the following to work. Remember to leave the gzipped version
     * in place since otherwise you will break the web server!
     *
     * @param path
     * @return
     */
    static private FileInfo getFileInfoFromServer(String dataRoot, String path, boolean doDownloadIfNeeded)
    {
        FileInfo fi = new FileInfo(dataRoot, path, doDownloadIfNeeded);

        return fi.isFailedToDownload() ? null : fi;
    }

    /**
     * Get information about the file on the server without actually downloading.
     *
     * @param path the path relative to the configuration's data root directory.
     * @return
     */
    static public FileInfo getFileInfoFromServer(String path)
    {
        return getFileInfoFromServer(Configuration.getDataRootURL(), path);
    }

    /**
     * Get information about the file on the server without actually downloading.
     *
     * @param dataRoot the root path prefix.
     * @param path the path relative to the provided data root directory.
     * @return
     */
    static public FileInfo getFileInfoFromServer(String dataRoot, String path)
    {
        return getFileInfoFromServer(dataRoot, path, false);
    }

    /**
     * Determine if it appears that the provided file name could be opened.
     * If the server is in use this will check whether the file exists
     * on the server. Otherwise it checks whether the file already exists
     * on disk. This will not actually open or download any files.
     * 
     * This method must be kept consistent with the getFileFromServer
     * method below.
     * @param fileName the file to check
     * @return
     * @throws UnauthorizedAccessException if a 401/403 (Unauthorized/Forbidden) error is encountered when attempting
     * to access the server for the remote file.
     */
    static public boolean isFileGettable(String fileName) throws UnauthorizedAccessException
    {
    	File file = null;
        if (!Configuration.useFileCache())
        {
            file = new File(fileName);
        }
        else
        {
            if (fileName.startsWith(FILE_PREFIX))
            {
                file = new File(fileName.substring(FILE_PREFIX.length()));
            }
            else
            {
                FileInfo fi = getFileInfoFromServer(fileName);
                if (fi.isExistsLocally() || fi.isExistsOnServer() == YesOrNo.YES)
                {
                	return true;
                }
                else if (fi.isURLAccessAuthorized() == YesOrNo.NO)
                {
                	URL url = fi.getURL();
					throw new UnauthorizedAccessException("Cannot access information about restricted URL: " + url, url);
                }
                file = fi.getFile();
            }
        }
        return file != null && file.exists();
    }
 
    /**
     * Get (download) the file from the server. Place it in the cache for
     * future access.
     * If the path begins with "file://", then the file is assumed to be local
     * on disk and no server is contacted.
     *
     * This method must be kept consistent with the isFileGettable method above.
     * @param path
     * @return
     * @throws UnauthorizedAccessException if a 401 (Unauthorized) error is encountered when attempting
     * to access the server for the remote file.
     */
    static public File getFileFromServer(String path) throws UnauthorizedAccessException
    {
        if (!Configuration.useFileCache())
        {
            return new File(path);
        }
        else
        {
            if (path.startsWith(FILE_PREFIX))
            {
                return new File(path.substring(FILE_PREFIX.length()));
            }
            else
            {
                FileInfo fi = getFileInfoFromServer(Configuration.getDataRootURL(), path, true);
                
                if (fi != null)
                {
                	if (fi.isURLAccessAuthorized() == YesOrNo.NO)
                	{
                		URL url = fi.getURL();
                		throw new UnauthorizedAccessException("Cannot get file: access is restricted to URL: " + url, url);
                	}
                	return fi.getFile();
                }
                else
                    return null;
            }
        }
    }

    /**
     * When adding to the cache, gzipped files are always uncompressed and saved
     * without the ".gz" extension.
     *
     * @throws IOException
     */
    static private File addToCache(String path, InputStream is, long urlLastModified, long contentLength) throws IOException
    {
        // Put in a counting stream so we can count the number of bytes
        // read. This is necessary because the number of bytes read
        // might be different than the number of bytes written, for example
        // when the file is gzipped. As a result, looking at how
        // many bytes were written to disk so far won't
        // tell us how much remains to be downloaded. Since this counting
        // stream is inserted beneath the GZIP stream, we can divide the number
        // of bytes reads by the content length to get the percentage of the
        // file downloaded.
        CountingInputStream cis = new CountingInputStream(is);
        is = cis;
        if (path.toLowerCase().endsWith(".gz"))
            is = new GZIPInputStream(is);

        if (path.toLowerCase().endsWith(".gz"))
            path = path.substring(0, path.length()-3);

        // While we are downloading the file, the file should be named on disk
        // with a ".saavtk" suffix so that if the user forcibly kills the program
        // during a download, the file will not be used when the program is restarted.
        // After the download is successful, rename the file to the correct name.
        String realFilename = Configuration.getCacheDir() + File.separator + path;
        File file = new File(realFilename + getTemporarySuffix());

        file.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(file);

        abortDownload = false;
        boolean downloadAborted = false;
        downloadProgress = 0;

        final int bufferSize = 2048;
        byte[] buff = new byte[bufferSize];
        int len;
        while((len = is.read(buff)) > 0)
        {
            downloadProgress = cis.getByteCount();

            if (abortDownload)
            {
                downloadAborted = true;
                break;
            }

            os.write(buff, 0, len);
        }

        os.close();
        is.close();

        downloadProgress = contentLength;

        if (downloadAborted)
        {
            file.delete();
            return null;
        }

        // Change the modified time of the file to that of the server.
        if (urlLastModified > 0)
            file.setLastModified(urlLastModified);

        // Okay, now rename the file to the real name.
        File realFile = new File(realFilename);
        realFile.delete();
        file.renameTo(realFile);

        // Change the modified time again just in case the process of
        // renaming the file caused the modified time to change.
        // (On Linux, changing the filename, does not change the modified
        // time so this is not necessary, but I'm not sure about other platforms)
        if (urlLastModified > 0)
            realFile.setLastModified(urlLastModified);

        return realFile;
    }

    static public String getTemporarySuffix()
    {
        return FileUtil.getTemporarySuffix();
    }

    static public void abortDownload()
    {
        abortDownload = true;
    }

    /**
     * Get download progress as number of bytes downloaded so far.
     * @return
     */
    static public long getDownloadProgess()
    {
        return downloadProgress;
    }

    static public void resetDownloadProgess()
    {
        downloadProgress = 0;
    }

    /**
     * This is needed on windows.
     * @param path
     * @return
     */
    static private String replaceBackslashesWithForwardSlashes(String path)
    {
        return path.replace('\\', '/');
    }

    /**
     * Make sure path is clean and starts with a slash.
     * @param path input path
     * @return the cleaned path
     */
    static private String cleanPath(String path)
    {
    	path = replaceBackslashesWithForwardSlashes(SafePaths.getString(path));
    	return path.replaceFirst("^/*", "/");
    }

    static public void setOfflineMode(boolean offline, String rootFolder)
    {
        offlineMode = offline;
        offlineModeRootFolder = rootFolder;
    }

    static public boolean getOfflineMode()
    {
        return offlineMode;
    }
}
