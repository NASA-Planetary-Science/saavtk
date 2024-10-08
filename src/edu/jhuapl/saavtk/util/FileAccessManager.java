package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;

public class FileAccessManager
{
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    public static FileAccessManager of(File rootDirectory) throws IOException
    {
        Preconditions.checkNotNull(rootDirectory);
        Preconditions.checkArgument(rootDirectory.isDirectory() || !rootDirectory.exists());

        if (!rootDirectory.exists())
        {
            if (!rootDirectory.mkdirs())
            {
                throw new IOException("Unable to create local file cache directory " + rootDirectory);
            }
        }

        return new FileAccessManager(rootDirectory);
    }

    private final File rootDirectory;
    private final ConcurrentMap<File, FileInfo> fileInfoCache;

    protected FileAccessManager(File rootPath)
    {
        this.rootDirectory = rootPath;
        this.fileInfoCache = new ConcurrentHashMap<>();
    }

    public File getRootDirectory()
    {
        return rootDirectory;
    }

    public File getFile(Path path)
    {
        Preconditions.checkNotNull(path);

        return getFile(path.toString());
    }

    public File getFile(String pathString)
    {
        Preconditions.checkNotNull(pathString);

        Path path = SAFE_URL_PATHS.get(pathString);

        return path.isAbsolute() ? path.toFile() : SAFE_URL_PATHS.get(rootDirectory.getAbsolutePath(), pathString).toFile();
    }

    public FileInfo getInfo(Path path)
    {
        return getInfo(getFile(path));
    }

    public FileInfo getInfo(String pathString)
    {
        return getInfo(getFile(pathString));
    }

    public FileInfo getInfo(File file)
    {
        Preconditions.checkNotNull(file);

        FileInfo result;
        synchronized (this.fileInfoCache)
        {
            result = fileInfoCache.get(file);
            if (result == null)
            {
                result = FileInfo.of(file);
                fileInfoCache.put(file, result);
            }
        }

        return result;
    }

    public FileInfo queryFileSystem(String pathString, boolean forceUpdate)
    {
        return queryFileSystem(getFile(pathString), forceUpdate);
    }

    public FileInfo queryFileSystem(File file, boolean forceUpdate)
    {
        Preconditions.checkNotNull(file);

        FileInfo result = getInfo(file);
        FileState state = result.getState();
        if (state.getStatus() == FileStatus.UNKNOWN || forceUpdate)
        {
            FileCacheMessageUtil.debugCache().out().println("Querying FS about " + file);
            result.update();
        }

        return result;
    }

    @Override
    public String toString()
    {
        return "FileAccessManager(" + rootDirectory + ")";
    }

    public static void main(String[] args)
    {
        try
        {
            FileAccessManager manager = of(new File(System.getProperty("user.home")));

            System.err.println(manager.getFile("spud/junk.txt"));
            System.err.println(manager.getFile("/spud/junk.txt"));
            System.err.println(manager.getFile(manager.getRootDirectory().getPath()));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
