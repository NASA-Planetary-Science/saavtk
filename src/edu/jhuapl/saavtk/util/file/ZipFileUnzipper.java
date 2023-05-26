package edu.jhuapl.saavtk.util.file;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileCacheMessageUtil;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.file.StreamUnpacker.UnpackingStatus;

public class ZipFileUnzipper
{
    public static final String UNZIPPING_PROGRESS = "unzippingProgress";

    public static ZipFileUnzipper of(File file) throws IOException
    {
        return new ZipFileUnzipper(file, 8192);
    }

    public static ZipFileUnzipper of(File file, int bufferSize) throws IOException
    {
        return new ZipFileUnzipper(file, bufferSize);
    }

    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    private final PropertyChangeSupport pcs;
    private final ZipFile zipFile;
    private final long totalCompressedSize;
    private final String unzippedFolderName;
    private final File unzippedFolder;
    private final File tempExtractToFolder;
    private final int bufferSize;
    private volatile boolean canceled;

    protected ZipFileUnzipper(File file, int bufferSize) throws IOException
    {
        String zipFileFolder = file.getParent();
        String zipFileName = file.getName();
        String unzippedFolderName = zipFileName.replaceFirst("\\.[^\\.]*$", "");
        Preconditions.checkArgument(!zipFileName.equals(unzippedFolderName), "Cannot unzip a file on top of itself");

        ZipFile zipFile = new ZipFile(file);

        // Compute total size of all unzipped files. This is used to report progress.
        long totalCompressedSize = 0;
        {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                totalCompressedSize += entries.nextElement().getSize();
            }
        }

        this.pcs = new PropertyChangeSupport(this);
        this.zipFile = zipFile;
        this.totalCompressedSize = totalCompressedSize;
        this.unzippedFolderName = unzippedFolderName;
        this.unzippedFolder = SAFE_URL_PATHS.get(zipFileFolder, unzippedFolderName).toFile();
        this.tempExtractToFolder = new File(unzippedFolder.getPath() + "-" + UUID.randomUUID().toString());
        this.bufferSize = bufferSize;
        this.canceled = false;
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public void cancel()
    {
        this.canceled = true;
    }

    public long getTotalCompressedSize()
    {
        return totalCompressedSize;
    }

    public void unzip() throws IOException, InterruptedException
    {
        checkNotCanceled("Unzip aborted");

        System.out.println("Unzipping " + zipFile.getName() + " " + new Date());

        delete(tempExtractToFolder);

        try
        {
            AtomicReference<Long> unpackedByteCount = new AtomicReference<>(0l);
            long startTime = System.currentTimeMillis();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();

                checkNotCanceled("Unzip canceled");

                if (entry.isDirectory())
                {
                    SAFE_URL_PATHS.get(tempExtractToFolder.getPath(), entry.getName()).toFile().mkdirs();
                }
                else
                {
                    try (InputStream inputStream = zipFile.getInputStream(entry))
                    {
                        String outputFileName = SAFE_URL_PATHS.getString(tempExtractToFolder.getPath(), entry.getName());

                        // Ensure parent directory of output file exists.
                        File parent = SAFE_URL_PATHS.get(outputFileName).toFile().getParentFile();
                        if (!parent.isDirectory())
                        {
                            parent.mkdirs();
                        }
                        
                        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFileName)))
                        {
                            StreamUnpacker unpacker = StreamUnpacker.of(inputStream, bufferSize);

                            unpacker.addPropertyChangeListener(e -> {
                                if (StreamUnpacker.UNPACKING_STATUS.equals(e.getPropertyName()))
                                {
                                    UnpackingStatus unpackingStatus = (UnpackingStatus) e.getNewValue();

                                    long cumulativeByteCount = unpackedByteCount.get() + unpackingStatus.getUnpackedByteCount();

                                    unpackingStatus = UnpackingStatus.of("Unzipping", cumulativeByteCount, System.currentTimeMillis() - startTime);

                                    pcs.firePropertyChange(UNZIPPING_PROGRESS, null, unpackingStatus);
                                }

                            });

                            unpacker.unpack(outputStream);

                            unpackedByteCount.accumulateAndGet(entry.getSize(), add());

                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (!FileCacheMessageUtil.isDebugCache())
            {
                deleteQuietly(tempExtractToFolder);
            }

            throw e;
        }

        if (tempExtractToFolder.isDirectory())
        {
            delete(unzippedFolder);

            // Unzipping results in an extra layer, e.g.
            // /path/to/mapmaker-tmp-name/mapmaker. Therefore need to tack on the extra
            // folder name at the end.
            File unzippedFolder = SAFE_URL_PATHS.get(tempExtractToFolder.getPath(), unzippedFolderName).toFile();
            FileUtils.moveDirectory(unzippedFolder, this.unzippedFolder);

            // Need to delete the parent (very top level) zip folder.
            deleteQuietly(tempExtractToFolder);
        }
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

    protected void checkNotCanceled(String hint) throws InterruptedException
    {
        if (isCanceled())
        {
            throw new InterruptedException(hint);
        }
    }

    private void delete(File file) throws IOException
    {
        if (file.isDirectory())
        {
            for (File entry : file.listFiles())
                delete(entry);
        }

        if (file.exists() && !file.delete())
        {
            throw new FileNotFoundException("Failed to delete file: " + file);
        }
    }

    private void deleteQuietly(File file)
    {
        try
        {
            delete(file);
        }
        catch (@SuppressWarnings("unused") IOException ignored)
        {

        }
    }

    private BinaryOperator<Long> add()
    {
        return (l1, l2) -> {
            return l1 + l2;
        };
    }

}
