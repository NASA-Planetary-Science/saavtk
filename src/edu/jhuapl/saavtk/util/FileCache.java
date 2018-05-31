package edu.jhuapl.saavtk.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.input.CountingInputStream;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileCache.FileInfo.YesOrNo;

public final class FileCache
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

	public static abstract class FileInfo
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
		private YesOrNo urlAccessAuthorized;
		private YesOrNo existsOnServer;
		private volatile long totalByteCount;
		private volatile long byteCount;

		protected FileInfo(URL url, File file, YesOrNo urlAccessAuthorized, YesOrNo existsOnServer)
		{
			Preconditions.checkNotNull(url);
			Preconditions.checkNotNull(file);
			Preconditions.checkNotNull(urlAccessAuthorized);
			Preconditions.checkNotNull(existsOnServer);
			this.url = url;
			this.file = file;
			this.urlAccessAuthorized = urlAccessAuthorized;
			this.existsOnServer = existsOnServer;
			this.totalByteCount = 0;
			this.byteCount = 0;
		}

		public abstract URLConnection getConnection() throws IOException;

		public abstract long getLastModifiedTime();

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

		public boolean isNeedToDownload()
		{
			File file = getFile();
			if (!file.exists())
			{
				return true;
			}

			long urlLastModified = getLastModifiedTime();
			return file.lastModified() < urlLastModified;
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
			return totalByteCount > 0 ? (double) byteCount / totalByteCount : 0.37;
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
			try
			{
				getConnection();
				builder.append(", connected");
			}
			catch (@SuppressWarnings("unused") Exception e)
			{
				builder.append(", not connected");
			}
			builder.append(") ->");
			builder.append(getFile());
			builder.append(" (exists = ");
			builder.append(isExistsLocally());
			builder.append(", need-dl = ");
			builder.append(isNeedToDownload());
			builder.append(")");
			return builder.toString();
		}
	}

	public static final String FILE_PREFIX = "file://";
	private static final ConcurrentHashMap<File, FileInfo> INFO_MAP = new ConcurrentHashMap<>();
	private static boolean showDotsForFiles = false;
	private static boolean offlineMode;
	private static String offlineModeRootFolder;

	public static void showDotsForFiles(boolean enable)
	{
		showDotsForFiles = enable;
	}

	public static FileInfo getFileInfoFromServer(String path)
	{
		return getFileInfoFromServer(Configuration.getDataRootURL(), path);
	}

	/**
	 * Get information about the file on the server without actually downloading.
	 *
	 * @param dataRoot the root path prefix
	 * @param path the path relative to the provided data root directory.
	 * @return
	 */
	public static FileInfo getFileInfoFromServer(String dataRoot, String path)
	{
		Preconditions.checkNotNull(dataRoot);
		Preconditions.checkNotNull(path);

		if (!Configuration.useFileCache())
		{
			throw new UnsupportedOperationException("Running without a cache not supported at present");
		}

		path = cleanPath(path);
		final String ungzippedPath = path.toLowerCase().endsWith(".gz") ? path.substring(0, path.length() - 3) : path;

		URL url = getURL(dataRoot + path);

		if (offlineMode)
		{
			return new LocalFileInfo(url, new File(SafePaths.getString(offlineModeRootFolder, ungzippedPath)), YesOrNo.UNKNOWN, YesOrNo.UNKNOWN);
		}

		if (ungzippedPath.equals(path) && dataRoot.toLowerCase().startsWith(FILE_PREFIX))
		{
			// File "on the server" is not gzipped, and is allegedly on local file system,
			// so just try to use it directly.
			File file = new File(url.getPath());

			FileInfo info = INFO_MAP.get(file);
			if (info == null)
			{
				info = new LocalFileInfo(url, file, YesOrNo.YES, file.exists() ? YesOrNo.YES : YesOrNo.NO);
				INFO_MAP.put(file, info);
			}
			return info;
		}

		// File must be gunzipped, so need the full FileInfo no matter where the URL points.
		File file = SafePaths.get(Configuration.getCacheDir(), ungzippedPath).toFile();

		FileInfo info = INFO_MAP.get(file);
		if (info == null)
		{
			// This code is based on code from stacktrace. The stacktrace code
			// specifically included disabling redirects, but that leads to
			// spurious 301 errors, so leaving these in here commented out.
			//                  HttpURLConnection.setFollowRedirects(false);
			YesOrNo authorized = YesOrNo.UNKNOWN;
			YesOrNo urlExists = YesOrNo.UNKNOWN;
			try
			{
				final URLConnection connection = url.openConnection();
				Debug.out().println("FileInfo(): opened connection to " + url);
				if (!Debug.isEnabled() && showDotsForFiles)
				{
					System.out.print('.');
				}

				// These two properties seem to be still necessary as of 2017-12-19.
				connection.setRequestProperty("User-Agent", "Mozilla/4.0");
				connection.setRequestProperty("Accept", "*/*");
				if (connection instanceof HttpURLConnection)
				{
					HttpURLConnection httpConnection = (HttpURLConnection) connection;

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
					final long lastModifiedTime = connection.getLastModified();
					info = new FileInfo(url, file, authorized, urlExists) {

						@Override
						public URLConnection getConnection() throws IOException
						{
							URLConnection connection = url.openConnection();

							// These two properties seem to be still necessary as of 2017-12-19.
							connection.setRequestProperty("User-Agent", "Mozilla/4.0");
							connection.setRequestProperty("Accept", "*/*");

							return connection;
						}

						@Override
						public long getLastModifiedTime()
						{
							return lastModifiedTime;
						}

					};
				}

			}
			catch (Exception e)
			{
				e.printStackTrace();
				String message = e.getMessage();
				if (message != null && (message.contains("401") || message.contains("403")))
				{
					authorized = YesOrNo.NO;
				}
				info = new LocalFileInfo(url, file, authorized, urlExists);
			}
			//			System.err.println("info is " + info);
			INFO_MAP.put(file, info);
		}

		return info;
	}

	/**
	 * Determine if it appears that the provided file name could be opened. If the
	 * server is in use this will check whether the file exists on the server.
	 * Otherwise it checks whether the file already exists on disk. This will not
	 * actually open or download any files.
	 * 
	 * This method must be kept consistent with the getFileFromServer method below.
	 * 
	 * @param fileName the file to check
	 * @return
	 * @throws UnauthorizedAccessException if a 401/403 (Unauthorized/Forbidden)
	 *             error is encountered when attempting to access the server for the
	 *             remote file.
	 */
	public static boolean isFileGettable(String fileName) throws UnauthorizedAccessException
	{
		FileInfo fileInfo = getFileInfoFromServer(fileName);
		if (fileInfo.isExistsLocally() || fileInfo.isExistsOnServer() == YesOrNo.YES)
		{
			return true;
		}
		else if (fileInfo.isURLAccessAuthorized() == YesOrNo.NO)
		{
			URL url = fileInfo.getURL();
			throw new UnauthorizedAccessException("Cannot access information about restricted URL: " + url, url);
		}
		return false;
	}

	/**
	 * Get (download) the file from the server. Place it in the cache for future
	 * access. If the path begins with "file://", then the file is assumed to be
	 * local on disk and no server is contacted.
	 *
	 * This method must be kept consistent with the isFileGettable method above.
	 * 
	 * @param path
	 * @return
	 * @throws UnauthorizedAccessException if a 401 (Unauthorized) error is
	 *             encountered when attempting to access the server for the remote
	 *             file.
	 */
	public static File getFileFromServer(String path) throws UnauthorizedAccessException
	{
		FileInfo fileInfo = getFileInfoFromServer(Configuration.getDataRootURL(), path);

		if (fileInfo.isURLAccessAuthorized() == YesOrNo.NO)
		{
			URL url = fileInfo.getURL();
			throw new UnauthorizedAccessException("Cannot get file: access is restricted to URL: " + url, url);
		}

		final File file = fileInfo.getFile();
		if (fileInfo.isNeedToDownload())
		{
			try (WrappedInputStream wrappedStream = new WrappedInputStream(fileInfo))
			{
				final long totalByteCount = wrappedStream.getTotalByteCount();
				fileInfo.setTotalByteCount(totalByteCount);
				final File tmpFile = new File(fileInfo.getFile() + FileUtil.getTemporarySuffix());

				file.getParentFile().mkdirs();
				try (FileOutputStream os = new FileOutputStream(tmpFile))
				{
					InputStream is = wrappedStream.getStream();
					//					abortDownload = false;
					//					boolean downloadAborted = false;
					//
					final int bufferSize = 4096;
					byte[] buff = new byte[bufferSize];
					int len;
					while ((len = is.read(buff)) > 0)
					{
						fileInfo.setByteCount(wrappedStream.getByteCount());

						//						if (abortDownload)
						//						{
						//							downloadAborted = true;
						//							break;
						//						}
						//
						os.write(buff, 0, len);
					}
					fileInfo.setByteCount(totalByteCount);
					//
					//					if (downloadAborted)
					//					{
					//						file.delete();
					//						return null;
					//					}
					//
					// Change the modified time of the file to that of the server.
					final long lastModified = wrappedStream.getLastModifiedTime();
					if (lastModified > 0)
						tmpFile.setLastModified(lastModified);

					// Okay, now rename the file to the real name.
					file.delete();
					tmpFile.renameTo(file);

					// Change the modified time again just in case the process of
					// renaming the file caused the modified time to change.
					// (On Linux, changing the filename, does not change the modified
					// time so this is not necessary, but I'm not sure about other platforms)
					if (lastModified > 0)
						file.setLastModified(lastModified);
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fileInfo.getFile();
	}

	public static void abortDownload()
	{

	}

	/**
	 * Get download progress as number of bytes downloaded so far.
	 * 
	 * @return
	 */
	public static long getDownloadProgess()
	{
		return -1;
	}

	public static void resetDownloadProgess()
	{

	}

	public static void setOfflineMode(boolean offlineMode, String offlineModeRootFolder)
	{
		if (offlineMode)
		{
			Preconditions.checkNotNull(offlineModeRootFolder);
		}
		FileCache.offlineMode = offlineMode;
		FileCache.offlineModeRootFolder = offlineModeRootFolder;
	}

	private static String cleanPath(String path)
	{
		path = SafePaths.getString(path).replace('\\', '/');
		return path.replaceFirst("^/*", "/");
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

	private FileCache()
	{
		throw new AssertionError();
	}

	private static class LocalFileInfo extends FileInfo
	{
		protected LocalFileInfo(URL url, File file, YesOrNo urlAccessAuthorized, YesOrNo existsOnServer)
		{
			super(url, file, urlAccessAuthorized, existsOnServer);
		}

		@Override
		public URLConnection getConnection()
		{
			throw new IllegalStateException();
		}

		@Override
		public boolean isNeedToDownload()
		{
			return !getFile().exists();
		}

		@Override
		public long getLastModifiedTime()
		{
			return getFile().lastModified();
		}
	}

	private static final class WrappedInputStream implements Closeable
	{
		private final URLConnection connection;
		private final long totalByteCount;
		private final long lastModifiedTime;
		private InputStream inputStream;
		private final CountingInputStream countingInputStream;

		private WrappedInputStream(FileInfo fileInfo) throws IOException
		{
			final boolean gunzip = fileInfo.getURL().getPath().toLowerCase().endsWith(".gz");

			this.connection = fileInfo.getConnection();
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
