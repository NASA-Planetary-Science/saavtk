package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.input.CountingInputStream;

public class FileCache
{
    public static final String FILE_PREFIX = "file://";

    // Stores files already downloaded in this process
    private static ConcurrentHashMap<String, Object> downloadedFiles =
        new ConcurrentHashMap<String, Object>();

    private static volatile boolean abortDownload = false;

    // Download progress. Equal to number of bytes downloaded so far.
    private static volatile long downloadProgress = 0;

    // If true do not make a network connection to get the file but only retrieve
    // it from the cache if it exists. Usually set to false, but some batch scripts
    // may set it to true.
    private static boolean offlineMode = false;

    // When in offline mode, files are retrieved relative to this folder
    private static String offlineModeRootFolder = null;

    /**
     * Information returned about a remote file on the server
     */
    public static class FileInfo
    {
        // The location on disk of the file if actually downloaded or the location the file
        // would have if downloaded.
        public File file = null;

        // If the the file was not actually downloaded, this variable stores whether it
        // needs to be downloaded (i.e. if it is out of date)
        public boolean needToDownload = false;

        // The number of bytes in the file (regardless if actually downloaded)
        public long length = -1;

        public boolean existsOnServer;
    }

    /**
     * This function is used to both download a file from a server as well as to
     * check if the file is out of data and needs to be downloaded. This depends
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
    static private FileInfo getFileInfoFromServer(String path, boolean doDownloadIfNeeded)
    {
        path = replaceBackslashesWithForwardSlashes(path);

        FileInfo fi = new FileInfo();

        // If root URL starts with "file://", return file directly without caching it
        if (!Configuration.useFileCache() || Configuration.getDataRootURL().startsWith(FILE_PREFIX))
        {
            // If the file is gzipped, you will need to manually gunzip (in the same folder)
            // it in order for the following to work. Remember to leave the gzipped version
            // in place since otherwise you will break the web server!
            if (path.toLowerCase().endsWith(".gz"))
                path = path.substring(0, path.length()-3);
            File file = new File(Configuration.getDataRootURL().substring(FILE_PREFIX.length()) + path);
            fi.file = file;
            if (file.exists())
                fi.length = file.length();
            return fi;
        }

        if (offlineMode)
        {
            if (path.toLowerCase().endsWith(".gz"))
                path = path.substring(0, path.length()-3);
            File file = new File(offlineModeRootFolder + File.separator + path);
            fi.file = file;
            if (file.exists())
                fi.length = file.length();
            return fi;
        }

        String unzippedPath = path;
        if (unzippedPath.toLowerCase().endsWith(".gz"))
            unzippedPath = unzippedPath.substring(0, unzippedPath.length() - 3);

        File file = new File(Configuration.getCacheDir() + File.separator
                + unzippedPath);

        fi.file = file;

        // If we've already downloaded the file previously in this process,
        // simply return without making any network connections.
        boolean exists = file.exists();
        if (exists && downloadedFiles.containsKey(path))
        {
            fi.length = file.length();
            return fi;
        }

        // Open a connection to the server
        try
        {
            URL u = new URL(Configuration.getDataRootURL() + path);

            URLConnection conn = u.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/4.0");
            conn.setRequestProperty("Accept","*/*");

            try
            {
            //    u.openStream();   // sometimes this throws a FileNotFoundException because the server returns HTTP 404 even though the file exists; the declaration of conn just above, with the subsequent properties, seems to give the correct result (e.g. not a 404 for files that do exist)
                conn.getInputStream();
            }
            catch (IOException e)
            {
                //e.printStackTrace();
                fi.existsOnServer=false;
                return fi;
            }
            fi.existsOnServer=true;


            long urlLastModified = conn.getLastModified();

            if (exists && file.lastModified() >= urlLastModified)
            {
                fi.length = file.length();
                return fi;
            }
            else
            {
                if (doDownloadIfNeeded)
                {
                    long contentLength = 0;
                    String contentLengthStr = conn.getHeaderField("content-length");
                    if (contentLengthStr != null)
                        contentLength = Long.parseLong(contentLengthStr);
                    file = addToCache(path, conn.getInputStream(), urlLastModified, contentLength);
                    if (file != null) // file can be null if the user aborted the download
                    {
                        downloadedFiles.put(path, "");
                        fi.length = file.length();
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    fi.needToDownload = true;
                    String contentLengthStr = conn.getHeaderField("content-length");
                    if (contentLengthStr != null)
                        fi.length = Long.parseLong(contentLengthStr);
                }

                return fi;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // If we reach here, simply return the file if it exists.
        if (exists)
        {
            fi.length = file.length();
            return fi;
        }
        else
        {
            return null;
        }
    }

    /**
     * Get information about the file on the server without actually downloading.
     *
     * @param path
     * @return
     */
    static public FileInfo getFileInfoFromServer(String path)
    {
        return getFileInfoFromServer(path, false);
    }

    /**
     * Get (download) the file from the server. Place it in the cache for
     * future access.
     * If the path begins with "file://", then the file is assumed to be local
     * on disk and no server is contacted.
     *
     * @param path
     * @return
     */
    static public File getFileFromServer(String path)
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
                FileInfo fi = getFileInfoFromServer(path, true);
                if (fi != null)
                    return fi.file;
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
