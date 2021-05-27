package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.github.davidmoten.guavamini.Preconditions;
import com.google.common.collect.ImmutableList;

import crucible.core.math.Statistics;

/**
 * Simple class to time the execution of arbitrary sections of code, possibly
 * factored into any number of classes/methods. Can work for multiple threads
 * and/or multiple runs of an application. The class itself is safe to access
 * from multiple threads, but generally it would be most useful to have distinct
 * {@link Profiler} instances for each thread.
 * <p>
 * In addition to support for timing an individual run of profiling code, the
 * class features a means to summarize the results of an arbitrary number of
 * runs.
 * 
 * @author James Peachey
 *
 */
public class Profiler
{
    protected static final ExecutorService Executor = Executors.newSingleThreadExecutor();
    protected static final AtomicBoolean GlobalEnableProfiling = new AtomicBoolean(false);

    protected final AtomicReference<Long> startTime;
    protected final AtomicReference<Path> profilePath;
    protected final AtomicReference<String> timeStampFileName;
    protected final List<Long> times;

    private String profilePathPrefix;

    public static void globalEnableProfiling(boolean enable)
    {
        GlobalEnableProfiling.set(enable);
    }

    /**
     * Return a new profiler that stores its results under the path given by
     * {@link Configuration#getApplicationDataDir()} in a subdirectory named by the
     * specified path prefix.
     * 
     * @param profilePathPrefix the prefix for the path
     * @return the profiler.
     */
    public static Profiler of(String profilePathPrefix)
    {
        return new Profiler(profilePathPrefix);
    }

    protected Profiler(String profilePathPrefix)
    {
        super();

        this.startTime = new AtomicReference<>();
        this.profilePath = new AtomicReference<>();
        this.timeStampFileName = new AtomicReference<>(UUID.randomUUID().toString() + ".txt");
        this.times = new ArrayList<>();
        this.profilePathPrefix = profilePathPrefix;
    }

    /**
     * Start the profile timer if it has not already been started. Calling it
     * additional times has no effect.
     */
    public void start()
    {
        if (!GlobalEnableProfiling.get())
        {
            return;
        }
        startTime.compareAndSet(null, System.nanoTime());
    }

    /**
     * Compute the time elapsed since the last call to accumulate(), or since the
     * {@link #start()} method was first called. Store that elapsed time in the list
     * of elapsed times.
     */
    public void accumulate()
    {
        if (!GlobalEnableProfiling.get())
        {
            return;
        }

        long now = System.nanoTime();
        Long startTime = this.startTime.getAndSet(now);
        if (startTime != null)
        {
            synchronized (this.times)
            {
                times.add(now - startTime);
            }
        }
    }

    /**
     * Write the elapsed times that have been accumulated up to this point in
     * program execution to a randomly named file in the profiling directory. The
     * written times are times between subsequent calls to the {@link #accumulate()}
     * method.
     * <p>
     * For a given instance of {@link Profiler}, the same random file name is always
     * used by this method to report the time. This method appends to the output
     * file each time this method is called, and discards the times after they are
     * written.
     * <p>
     * The times written to the file will be floating point values giving the
     * elapsed times in milli-seconds.
     * <p>
     * The I/O is performed on a dedicated thread to avoid using CPU cycles on the
     * thread being profiled.
     */
    public void reportElapsedTimes()
    {
        if (!GlobalEnableProfiling.get())
        {
            return;
        }

        ImmutableList<Long> timesToReport;
        synchronized (this.times)
        {
            timesToReport = ImmutableList.copyOf(times);
            times.clear();
        }

        if (timesToReport.isEmpty())
        {
            return;
        }

        Executor.execute(() -> {
            Path profileDirPath = getProfilePath();
            profileDirPath.toFile().mkdirs();
            Path profileFilePath = profileDirPath.resolve(timeStampFileName.get());

            FileCacheMessageUtil.info().println("Writing performance data for " + profilePathPrefix + " to " + timeStampFileName.get());

            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(profileFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)))
            {
                for (Long time : timesToReport)
                {
                    // Write the elapsed time, converting to seconds.
                    writer.printf("%#.4f\n", 1.e-9 * time);
                }
            }
            catch (Exception e)
            {
//            e.printStackTrace();
            }
        });
    }

    /**
     * Summarize performance of all profiles that were written individually by any
     * profiler. The summary is written in plain text under the profiling directory
     * to a randomly named text file that begins with the same prefix as that
     * returned by the {@link #getProfilePathPrefix()} method.
     */
    public void summarizeAllPerformance(String header)
    {
        Preconditions.checkNotNull(header);

        if (!GlobalEnableProfiling.get())
        {
            return;
        }

        try
        {
            List<File> files = getProfileFiles();
            ImmutableList.Builder<Double> builder = ImmutableList.builder();
            for (File file : files)
            {
                try (Scanner s = new Scanner(file))
                {
                    while (s.hasNextDouble())
                    {
                        builder.add(s.nextDouble());
                    }
                }
                catch (Exception e)
                {
//                    e.printStackTrace();
                }
            }
            ImmutableList<Double> timeList = builder.build();

            Statistics stat = null;
            double median = 0.;
            Double[] times = null;

            if (!timeList.isEmpty())
            {
                stat = Statistics.builder().accumulate(timeList).build();

                times = new Double[timeList.size()];
                Arrays.sort(timeList.toArray(times));
                int length = times.length;

                median = times.length % 2 == 0 ? (times[length / 2] + times[length / 2 - 1]) / 2. : times[length / 2];
            }

            File summaryFile = SafeURLPaths.instance().get(getProfilePath().toString() + "-" + timeStampFileName).toFile();
            try (PrintStream s = new PrintStream(summaryFile))
            {
                s.println(header);
                if (stat != null)
                {
                    s.printf("Number of data = %d\n", stat.getSamples());
                    s.printf("Mean time = %#.2f\n", stat.getMean());
                    s.printf("Median time = %#.2f\n", median);
                    s.printf("Maximum time = %#.2f\n", stat.getMaximumValue());
                    s.printf("Minimum time = %#.2f\n", stat.getMinimumValue());

                    if (times != null)
                    {
                        s.println("Individual times");
                        for (Double time : times)
                        {
                            s.printf("%#.2f\n", time);
                        }
                    }
                }
                else
                {
                    s.println("No profiling data found.");
                }
            }
            catch (Exception e)
            {
//                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
//            e.printStackTrace();
        }
    }

    /**
     * Remove all the files in the profile area associated with ALL {@link Profiler}
     * instances, and the parent directory itself. This is useful for clearing out a
     * previous profiling result automatically.
     * <p>
     * This method clears out the profile area even if profiling is switched off (if
     * {@link #GlobalEnableProfiling} is false)
     */
    public void deleteProfileArea()
    {
        List<File> files = getProfileFiles();
        for (File file : files)
        {
            file.delete();
        }
        getProfilePath().toFile().delete();
    }

    /**
     * Return the prefix used to determine where the profile data are written.
     * Individual timing results are written in files under a directory with this
     * name in the profiling area. Summaries are written to a file based on this
     * name.
     * 
     * @return the path prefix
     */
    protected String getProfilePathPrefix()
    {
        return profilePathPrefix;
    };

    /**
     * Return a list of all files currently in the profile area.
     */
    protected List<File> getProfileFiles()
    {
        ImmutableList.Builder<File> builder = ImmutableList.builder();
        Path profilePath = getProfilePath();
        if (profilePath.toFile().isDirectory())
        {
            try (Stream<Path> paths = Files.walk(profilePath))
            {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    builder.add(path.toFile());
                });
            }
            catch (Exception e)
            {
//                e.printStackTrace();
            }
        }

        return builder.build();
    }

    /**
     * Return the path under which the results of the profiling are written.
     * 
     * @return the path
     */
    protected Path getProfilePath()
    {
        profilePath.compareAndSet(null, SafeURLPaths.instance().get(Configuration.getApplicationDataDir(), getProfilePathPrefix()));

        return profilePath.get();
    }

    /**
     * Test code.
     * 
     * @param args
     */
//    public static void main(String[] args)
//    {
//        int numberJobs = 32;
//        ExecutorService executor = Executors.newCachedThreadPool();
//
//        CountDownLatch latch = new CountDownLatch(numberJobs);
//
//        ImmutableList.Builder<Callable<Void>> builder = ImmutableList.builder();
//        for (int index = 0; index < numberJobs; ++index)
//        {
//            builder.add(() -> {
//                Profiler profiler = Profiler.of();
//
//                try
//                {
//                    profiler.start();
//                    Thread.sleep((long) (2000 * Math.random()));
//                }
//                finally
//                {
//                    profiler.accumulate();
//                    profiler.reportElapsedTimes();
//                    latch.countDown();
//                }
//
//                return null;
//            });
//        }
//
//        try
//        {
//            executor.invokeAll(builder.build());
//            latch.await(10, TimeUnit.SECONDS);
//            executor.shutdown();
//        }
//        catch (InterruptedException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        finally
//        {
//            Profiler profiler = Profiler.of();
//            profiler.summarizeAllPerformance();
//            profiler.deleteProfileArea();
//        }
//
//    }
}
