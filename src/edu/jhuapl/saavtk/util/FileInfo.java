package edu.jhuapl.saavtk.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import com.google.common.base.Preconditions;

/**
 * Collect and store essential properties associated with a local file resource.
 * These properties are useful for determining whether it is necessary and/or
 * possible to download a remote file, or to determine the size of the file.
 */
public class FileInfo
{
    public static final String STATE_PROPERTY = "fileInfoState";

    public enum FileStatus
    {
        ACCESSIBLE,
        INACCESSIBLE,
        UNKNOWN,
        ;
    }

    public static class FileState
    {
        public static FileState of(File file)
        {
            return of(file, FileStatus.UNKNOWN, -1, 0);
        }

        public static FileState of(File file, FileStatus status, long length, long lastModified)
        {
            Preconditions.checkNotNull(file);
            Preconditions.checkNotNull(status);

            return new FileState(file, status, length, lastModified);
        }

        private final File file;
        private final FileStatus status;
        private final long length;
        private final long lastModified;

        protected FileState(File file, FileStatus status, long length, long lastModified)
        {
            this.file = file;
            this.status = status;
            this.length = length;
            this.lastModified = lastModified;
        }

        public File getFile()
        {
            return file;
        }

        public FileStatus getStatus()
        {
            return status;
        }

        public long getLength()
        {
            return length;
        }

        public long getLastModified()
        {
            return lastModified;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;

            result = prime * result + file.hashCode();
            result = prime * result + (int) (length ^ (length >>> 32));
            result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
            result = prime * result + status.hashCode();

            return result;
        }

        @Override
        public boolean equals(Object other)
        {
            if (this == other)
                return true;
            if (other instanceof FileState)
            {
                FileState that = (FileState) other;
                if (!this.file.equals(that.file))
                    return false;
                if (this.status != that.status)
                    return false;
                if (this.length != that.length)
                    return false;
                if (this.lastModified != that.lastModified)
                    return false;

                return true;
            }

            return false;
        }

        @Override
        public String toString()
        {
            return "FileState [" + (file != null ? "file=" + file + ", " : "") + //
                    "status=" + status + ", length=" + length + ", lastModified=" + lastModified + "]";
        }

    }

    public static FileInfo of(File file)
    {
        return new FileInfo(file);
    }

    private final PropertyChangeSupport pcs;
    private volatile FileState state;

    protected FileInfo(File file)
    {
        this.pcs = new PropertyChangeSupport(this);
        this.state = FileState.of(file);
    }

    public FileState getState()
    {
        return state;
    }

    public void update(FileStatus status, long length, long lastModified)
    {
        Preconditions.checkNotNull(status);

        state = FileState.of(state.getFile(), status, length, lastModified);

        pcs.firePropertyChange(STATE_PROPERTY, null, state);
    }

    public void update()
    {
        File file = state.getFile();

        FileStatus status;
        long length;
        long lastModified;

        synchronized (this.state)
        {
            status = this.state.getStatus();
            length = this.state.getLength();
            lastModified = this.state.getLastModified();
        }

        status = file.exists() ? FileStatus.ACCESSIBLE : FileStatus.INACCESSIBLE;
        length = file.length();
        lastModified = file.lastModified();

        update(status, length, lastModified);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public String toString()
    {
        return state.toString();
    }

}