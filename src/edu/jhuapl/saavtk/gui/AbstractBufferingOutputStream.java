package edu.jhuapl.saavtk.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe abstract extension of {@link OutputStream} that buffers input fed
 * to it via any of the standard methods {@link #write(int)},
 * {@link #write(byte[])} or {@link #write(byte[], int, int)}. In the base
 * implementation, all these methods update the buffer, then call the method
 * {@link #flushIfReady()} to flush and reset the buffer when appropriate.
 * <p>
 * The base implementation performs a variation of auto-flush similar to but not
 * identical to {@link PrintStream}'s behavior when it is set up with autoflush
 * == true, namely, if the buffer gets full or if a byte matching newline is
 * encountered, {@link #isReadyToFlush()} will return true. {@link PrintStream}
 * flushes in these cases, BUT also flushes whenever ANY of its
 * <code>write(...)</code> overloades are called. Because this class flushes
 * somewhat less frequently, it does a slightly better job at keeping lines
 * together that come from the same thread.
 * <p>
 * If subclassing this class, ensure that all operations that read or affect
 * ANY/ALL of this class's fields are synchronized on the buffer reference
 * field.
 * <p>
 * A copy of this class is maintained in the saatvk repository for use by the
 * SBMT. If revising it, need to test it in both contexts. The "original" class
 * is part of the timeseries package used by MIDL.
 * 
 * @author James Peachey
 *
 */
public abstract class AbstractBufferingOutputStream extends OutputStream
{

    /**
     * Synchronize on bufRef whenever reading or updating ANY field.
     */
    protected final AtomicReference<byte[]> bufRef;
    protected volatile int begin;
    protected volatile int end;

    /**
     * Constructor accepting size of the buffer as an argument.
     * 
     * @param bufSize the buffer size
     */
    protected AbstractBufferingOutputStream(int bufSize)
    {
        super();

        this.bufRef = new AtomicReference<>(new byte[bufSize]);
        this.begin = 0;
        this.end = 0;
    }

    @Override
    public void write(int bInt) throws IOException
    {
        byte b = (byte) bInt;

        synchronized (bufRef)
        {
            if (end < 0)
            {
                throw new IOException("Stream was closed");
            }

            byte[] buf = bufRef.get();

            buf[end++] = b;

            flushIfReady();
        }
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException
    {
        // Argument checking block taken verbatim from base implementation.
        if (b == null)
        {
            throw new NullPointerException();
        }
        else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)
                || ((off + len) < 0))
        {
            throw new IndexOutOfBoundsException();
        }
        else if (len == 0)
        {
            return;
        }

        synchronized (bufRef)
        {
            if (end < 0)
            {
                throw new IOException("Stream was closed");
            }

            byte[] buf = bufRef.get();

            if (len > buf.length)
            {
                // The buffer b that was passed in won't fit in this buffer, so just flush this
                // buffer and
                // then bypass it, writing b directly.
                flush();
                doWrite(b, off, len);

            }
            else
            {

                // Don't increment bBegin here; it's propertly incremented multiple times in the
                // inner loop.
                for (int bBegin = 0; bBegin < len;)
                {
                    // Determine how many bytes we can safely write without going past end of input
                    // OR output.
                    int n = Math.min(buf.length - end, len - bBegin);

                    for (int i = 0; i < n; ++i)
                    {
                        // Copy the current byte, updating the counters: bBegin and end.
                        buf[end++] = b[off + bBegin++];

                        // Terminate the loop here if a flush is indicated.
                        if (isReadyToFlush())
                        {
                            break;
                        }
                    }

                    flushIfReady();
                }

            }
        }
    }

    /**
     * The implementation in {@link AbstractBufferingOutputStream} checks whether
     * the buffer has any unwritten content, and if it does, it calls
     * {@link #doWrite(byte[], int, int)}, passing arguments such that only the
     * unwritten content is written. Then it updates the stream state so that the
     * buffer is ready to receive further input.
     */
    @Override
    public void flush() throws IOException
    {
        synchronized (bufRef)
        {
            if (end < 0)
            {
                throw new IOException("Stream was closed");
            }

            byte[] buf = bufRef.get();

            if (end > begin)
            {
                doWrite(buf, begin, end - begin);

                if (buf.length == end)
                {
                    end = 0;
                }

                begin = end;
            }
        }

        super.flush();
    }

    /**
     * The implementation in {@link AbstractBufferingOutputStream} flushes the
     * buffer, then frees it and marks the stream closed so that any further
     * operations on the stream will throw {@link IOException}.
     */
    @Override
    public void close() throws IOException
    {
        synchronized (bufRef)
        {
            flush();

            bufRef.set(null);
            begin = -1;
            end = -1;
        }

        super.close();
    }

    /**
     * Subclasses implement this to specify how actually to write a byte array. Note
     * that the array is specified as an argument, i.e., this method may be called
     * to write any array, not just the buffer.
     * <p>
     * All methods in {@link AbstractBufferingOutputStream} that call this method
     * take care only to do so when properly synchronized, and passing only valid
     * arguments, so the implementer of this method normally need not worry about
     * thread safety or argument checking.
     * 
     * @param b buffer to write
     * @param off offset from beginning of the buffer
     * @param len number of bytes to write
     * @throws IOException
     */
    protected abstract void doWrite(byte b[], int off, int len) throws IOException;

    /**
     * In {@link AbstractBufferingOutputStream}, this method calls
     * {@link #isReadyToFlush()} and if it returns true, it calls {@link #flush()}.
     * 
     * @throws IOException
     */
    protected void flushIfReady() throws IOException
    {
        synchronized (bufRef)
        {

            if (isReadyToFlush())
            {
                flush();
            }

        }
    }

    /**
     * Return a flag indicating whether the buffer is ready to be flushed. In
     * {@link AbstractBufferingOutputStream} this returns true if either the buffer
     * is full, or if the byte most recently written to the buffer was a newline
     * character.
     * 
     * @return true if the buffer is ready to be flushed, false otherwise
     */
    protected boolean isReadyToFlush()
    {
        synchronized (bufRef)
        {
            byte[] buf = bufRef.get();
            return end == buf.length || (end > 0 && buf[end - 1] == '\n');
        }
    }

    public static void main(String[] args)
    {
        try (PrintStream ps = new PrintStream(new AbstractBufferingOutputStream(8192) {

            @Override
            protected void doWrite(byte[] b, int off, int len) throws IOException
            {
                System.out.write(b, off, len);
            }

        }))
        {
            ps.print("\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
