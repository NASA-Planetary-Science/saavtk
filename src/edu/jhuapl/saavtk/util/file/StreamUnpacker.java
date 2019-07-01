package edu.jhuapl.saavtk.util.file;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import com.google.common.base.Preconditions;

public abstract class StreamUnpacker
{
    public static final String UNPACKING_STATUS = "unpackingStatus";

    public static StreamUnpacker of(InputStream inputStream) throws IOException
    {
        return of(inputStream, 8192);
    }

    public static StreamUnpacker of(InputStream inputStream, int bufferSize) throws IOException
    {
        Preconditions.checkNotNull(inputStream);

        return new StreamUnpacker(bufferSize) {

            @Override
            protected InputStream getInputStream()
            {
                return inputStream;
            }

        };
    }

    private final PropertyChangeSupport pcs;
    private final byte[] buffer;
    private volatile boolean canceled;

    protected StreamUnpacker(int bufferSize) throws IOException
    {
        this.pcs = new PropertyChangeSupport(this);
        this.buffer = new byte[bufferSize];
        this.canceled = false;
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public void cancel()
    {
        canceled = true;
    }

    public void unpack(OutputStream outputStream) throws IOException, InterruptedException
    {
        long totalUnpackedByteCount = 0;
        long unpackedByteCount = 0;
        long startTime = System.currentTimeMillis();
        boolean firstBuffer = true;
        do
        {
            checkNotCanceled("Unpacking canceled");

            unpackedByteCount = unpackOneBuffer(outputStream, firstBuffer);
            firstBuffer = false;
            if (unpackedByteCount > 0)
            {
                totalUnpackedByteCount = updateTotalUnpackedByteCount(totalUnpackedByteCount, unpackedByteCount);
            }

            pcs.firePropertyChange(UNPACKING_STATUS, null, new UnpackingStatus("Unpacking", totalUnpackedByteCount, System.currentTimeMillis() - startTime));

        }
        while (unpackedByteCount > 0);
    }

    protected long unpackOneBuffer(OutputStream outputStream, boolean firstBuffer) throws IOException
    {
        synchronized (this.buffer)
        {
            int numberBytesRead = getInputStream().read(buffer);
            if (numberBytesRead > 0)
            {
                if (firstBuffer)
                {
                    checkForRejectedRequest(numberBytesRead);
                }
                outputStream.write(buffer, 0, numberBytesRead);
            }

            return numberBytesRead;
        }
    }

    protected void checkForRejectedRequest(int numberBytesRead) throws IOException
    {
        synchronized (this.buffer)
        {
            final int numberBytesToCheck = Math.min(numberBytesRead, 64);
            char[] firstCharacters = new char[numberBytesToCheck];
            for (int index = 0; index < numberBytesToCheck; ++index)
            {
                firstCharacters[index] = (char) buffer[index];
            }

            String firstCharacterString = new String(firstCharacters);
            if (firstCharacterString.matches(("^<html>.*Request Rejected.*")))
            {
                throw new IOException("Request for file was rejected by the server.");
            }
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

    public static final class UnpackingStatus
    {
        public static UnpackingStatus of(long unpackedByteCount, long elapsedTime)
        {
            return new UnpackingStatus("Unpacking", unpackedByteCount, elapsedTime);
        }

        public static UnpackingStatus of(String message, long unpackedByteCount, long elapsedTime)
        {
            return new UnpackingStatus(message, unpackedByteCount, elapsedTime);
        }

        private final String message;
        private final long unpackedByteCount;
        private final long elapsedTime;

        private UnpackingStatus(String message, long unpackedByteCount, long elapsedTime)
        {
            this.message = message;
            this.unpackedByteCount = unpackedByteCount;
            this.elapsedTime = elapsedTime;
        }

        public String getMessage()
        {
            return message;
        }

        public long getUnpackedByteCount()
        {
            return unpackedByteCount;
        }

        public long getElapsedTime()
        {
            return elapsedTime;
        }

        public long getEstimatedTimeToComplete(long totalByteCount)
        {
            Preconditions.checkArgument(totalByteCount > 0);

            long result = -1; // If nothing downloaded yet, return -1.

            if (unpackedByteCount > 0 && unpackedByteCount < totalByteCount)
            {
                result = (long) ((double) elapsedTime * (totalByteCount - unpackedByteCount) / unpackedByteCount);
            }

            return result;
        }

        public String createProgressMessage(String prefix, long totalByteCount)
        {
            double percentDownloaded = (double) unpackedByteCount / totalByteCount;

            String result = "<html>" + prefix + "<br>" + PF.format(percentDownloaded) + " Complete (" + //
                    ByteScale.describe(unpackedByteCount) + " of " + ByteScale.describe(totalByteCount) + ")";

            long estimatedTimeToComplete = getEstimatedTimeToComplete(totalByteCount);
            if (estimatedTimeToComplete > 0)
            {
                result += "<br>Estimated time remaining: " + TimeScale.describe(estimatedTimeToComplete);
            }
            else
            {
                result += "<br>Estimating time remaining...";
            }

            result += "</html>";

            return result;
        }

        protected static final DecimalFormat PF = new DecimalFormat("0%");

        private enum ByteScale
        {
            TBYTES("TB", 1.e12), //
            GBYTES("GB", 1.e9), //
            MBTYES("MB", 1.e6), //
            KBYTES("kB", 1.e3), //
            ;

            protected static final DecimalFormat DF = new DecimalFormat("0.0");

            public static String describe(double numberOfBytes)
            {
                for (ByteScale scale : values())
                {
                    if (numberOfBytes > scale.multiplier)
                    {
                        return DF.format(numberOfBytes / scale.multiplier) + " " + scale.description;
                    }
                }

                return numberOfBytes + " Bytes";
            }

            private final String description;
            private final double multiplier;

            private ByteScale(String name, double scale)
            {
                this.description = name;
                this.multiplier = scale;
            }

        }

        private enum TimeScale
        {
            DAYS("day", 86400.e3), //
            HOURS("hour", 3600.e3), //
            MINUTES("minute", 60.e3), //
            SECONDS("second", 1.e3), //
            ;

            public static String describe(double numberOfBytes)
            {
                for (TimeScale scale : values())
                {
                    if (numberOfBytes > scale.multiplier)
                    {
                        int integerScaledBytes = (int) (numberOfBytes / scale.multiplier);
                        return integerScaledBytes + " " + scale.description + (integerScaledBytes == 1 ? "" : "s");
                    }
                }

                return 1 + " " + SECONDS.description;
            }

            private final String description;
            private final double multiplier;

            private TimeScale(String name, double scale)
            {
                this.description = name;
                this.multiplier = scale;
            }

        }

    }

    protected abstract InputStream getInputStream();

    protected void checkNotCanceled(String hint) throws InterruptedException
    {
        if (isCanceled())
        {
            throw new InterruptedException(hint);
        }
    }

    protected long updateTotalUnpackedByteCount(long totalUnpackedByteCount, long unpackedByteCount)
    {
        return totalUnpackedByteCount + unpackedByteCount;
    }
}
