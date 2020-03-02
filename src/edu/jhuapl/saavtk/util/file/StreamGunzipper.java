package edu.jhuapl.saavtk.util.file;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.google.common.base.Preconditions;
import com.google.common.io.CountingInputStream;

public class StreamGunzipper extends StreamUnpacker implements Closeable
{
    public static StreamGunzipper of(InputStream inputStream) throws IOException
    {
        Preconditions.checkNotNull(inputStream);

        return new StreamGunzipper(inputStream, 8192);
    }

    public static StreamGunzipper of(InputStream inputStream, int bufferSize) throws IOException
    {
        Preconditions.checkNotNull(inputStream);

        return new StreamGunzipper(inputStream, bufferSize);
    }

    private final List<Closeable> openedStreams;
    private final CountingInputStream countingInputStream;
    private final InputStream inputStream;

    protected StreamGunzipper(InputStream inputStream, int bufferSize) throws IOException
    {
        super(bufferSize);

        this.openedStreams = new ArrayList<>();

        if (!inputStream.markSupported())
        {
            inputStream = new BufferedInputStream(inputStream);
            openedStreams.add(inputStream);
        }

        this.countingInputStream = new CountingInputStream(inputStream);
        openedStreams.add(countingInputStream);

        if (isGzipped(countingInputStream))
        {
            inputStream = new GZIPInputStream(countingInputStream);
            openedStreams.add(inputStream);
        }
        else
        {
            inputStream = countingInputStream;
        }

        this.inputStream = inputStream;
    }

    @Override
    public void close() throws IOException
    {
        for (int index = openedStreams.size() - 1; index >= 0; --index)
        {
            openedStreams.get(index).close();
        }
    }

    protected boolean isGzipped(InputStream in) throws IOException
    {
        in.mark(2);
        int magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
        in.reset();

        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    @Override
    protected InputStream getInputStream()
    {
        return inputStream;
    }

    @Override
    protected long updateTotalUnpackedByteCount(@SuppressWarnings("unused") long ignoredTotalUnpackedByteCount, @SuppressWarnings("unused") long ignoredUnpackedByteCount)
    {
        return countingInputStream.getCount();
    }
}
