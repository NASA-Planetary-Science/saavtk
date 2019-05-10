package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.google.common.base.Preconditions;

public class FileCacheManager
{
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    public static FileCacheManager of(File cacheRoot) throws IOException
    {
        Preconditions.checkNotNull(cacheRoot);
        Preconditions.checkArgument(cacheRoot.isDirectory() || !cacheRoot.exists());

        if (!cacheRoot.exists())
        {
            if (!cacheRoot.mkdirs())
            {
                throw new IOException("Unable to create local file cache directory " + cacheRoot);
            }
        }

        return new FileCacheManager(cacheRoot.toPath());
    }

    private final Path cacheRoot;

    protected FileCacheManager(Path cacheRoot)
    {
        this.cacheRoot = cacheRoot;
    }

    public boolean isAccessible(String fileIdentifier)
    {
        Preconditions.checkNotNull(fileIdentifier);

        return getFile(fileIdentifier).exists();
    }

    public File getFile(String fileIdentifier)
    {
        Path filePath = SAFE_URL_PATHS.get(cacheRoot.toString(), fileIdentifier);

        return filePath.toFile();
    }

}
