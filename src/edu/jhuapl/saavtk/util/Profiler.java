package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import crucible.core.math.Statistics;

/**
 * Simple class to time the execution of arbitrary sections of code, possibly
 * factored into any number of classes/methods. Can work for multiple threads
 * and/or multiple runs of an application. The class itself is safe to access
 * from multiple threads, but generally it would be most useful to have a
 * distinct {@link Profiler} instances for each thread.
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
    private final AtomicReference<Long> startTime;
    private final AtomicReference<Path> profilePath;
    private final String timeStampFileName;

    /**
     * Return a new profiler that stores its results under the path given by
     * {@link Configuration#getApplicationDataDir()} in a subdirectory named
     * "profile".
     * 
     * @return the profiler.
     */
    public static Profiler of()
    {
        return new Profiler();
    }

    protected Profiler()
    {
        super();

        this.startTime = new AtomicReference<>();
        this.profilePath = new AtomicReference<>();
        this.timeStampFileName = UUID.randomUUID().toString() + ".txt";
    }

    /**
     * Start the profile timer if it has not already been started.
     */
    public void start()
    {
        startTime.compareAndSet(null, System.nanoTime());
    }

    /**
     * Return the start time of the profiled operation in nanoseconds.
     *
     * @return the start time of the application in nano seconds
     * @throws NullPointerException if {@link #start()} was not called first.
     */
    public long getStartNanoTime()
    {
        return startTime.get();
    }

    /**
     * Return the path under which the results of the profiling are written.
     * 
     * @return the path
     */
    public Path getProfilePath()
    {
        profilePath.compareAndSet(null, SafeURLPaths.instance().get(Configuration.getApplicationDataDir(), getProfilePathPrefix()));

        return profilePath.get();
    }

    /**
     * Write the elapsed time at this point in program execution to a randomly named
     * file in the profiling directory. The written time is the time since
     * {@link #start()} was called.
     * <p>
     * For a given instance of {@link Profiler}, the same random file name is always
     * used by this method to report the time. This method overwrites the output
     * file each time this method is called. Thus the output file contains only the
     * result of the last time this method was called.
     * <p>
     * The time passed as a paramter to this method is assumed to be in
     * NANO-seconds, but the time written to the file will be in milli-seconds and
     * written in floating point.
     *
     * @parm time the time to report in nano-seconds.
     */
    public void reportElapsedTime(long time)
    {
        Path profilePath = getProfilePath();
        profilePath.toFile().mkdirs();
        File profileFile = profilePath.resolve(timeStampFileName).toFile();

        try (PrintStream s = new PrintStream(profileFile))
        {
            // Write the elapsed time, converting to ms.
            s.printf("%#.2f\n", 1.e-6 * (time - getStartNanoTime()));
        }
        catch (Exception e)
        {
            // TAKE THIS OUT BEFORE COMMITTING!!!
            e.printStackTrace();
        }
    }

    /**
     * Summarize performance of all profiles that were written individually by any
     * profiler. The summary is written in plain text under the profiling directory
     * to a randomly named text file that begins with the same prefix as that
     * returned by the {@link #getProfilePathPrefix()} method.
     */
    public void summarizeAllPerformance()
    {
        try
        {
            List<File> files = getProfileFiles();
            ImmutableList.Builder<Double> builder = ImmutableList.builder();
            for (File file : files)
            {
                try (Scanner s = new Scanner(file))
                {
                    if (s.hasNextDouble())
                    {
                        builder.add(s.nextDouble());
                    }
                }
                catch (Exception e)
                {
                    // TAKE THIS OUT BEFORE COMMITTING!!!
                    e.printStackTrace();
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Remove all the files in the profile area associated with ALL {@link Profiler}
     * instances, and the parent directory itself. This is useful for clearing out a
     * previous profiling result automatically.
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
        return "profile";
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return builder.build();
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
//                    profiler.reportElapsedTime(System.nanoTime());
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
