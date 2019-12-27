package edu.jhuapl.saavtk.util;

import java.awt.EventQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import vtk.vtkNativeLibrary;

/**
 * Utility class for initializing native libraries, either all the libraries or
 * just the ones that are usable in "headless" mode.
 */
public class NativeLibraryLoader
{
    private static final AtomicBoolean isVtkInitialized = new AtomicBoolean(false);

    /**
     * Load all VTK libraries for the current execution mode. In "headless" mode
     * this will load only those VTK libraries that do not interact with keyboard,
     * mouse or display. Otherwise all VTK libraries are loaded.
     * <p>
     * This should be called before any attempt to use any VTK abstractions. This
     * method attempts to load each library using the
     * {@link #vtkNativeLibrary.LoadLibrary()} method, and reports all
     * {@link UnsatisfiedLinkError}s that are thrown by any of these invocations.
     * Then, if any such errors were thrown, this method will itself throw an
     * {@link UnsatisfiedLinkError}.
     * <p>
     * Note that this method does not otherwise handle any other {@link Throwable}
     * thrown by VTK.
     * <p>
     * All VTK loading methods only attempt to load the libraries the first time any
     * of them is called. Subsequent calls to any method have no effect.
     * <p>
     * Before attempting to initialize VTK, this method ensures that Java AWT event
     * dispatch thread is running. If an exception gets thrown in the process, this
     * method throws a {@link RuntimeException} before even trying to set up VTK.
     * 
     * @throws UnsatisfiedLinkError if any of the native libraries failed to load.
     * @throws RuntimeException if the AWT cannot be initialized.
     *
     */
    public static void loadVtkLibraries()
    {
        if (Configuration.isHeadless())
        {
            loadVtkLibrariesHeadless();
        }
        else
        {
            loadAllVtkLibraries();
        }
    }

    /**
     * Load all the VTK native shared libraries.
     * <p>
     * This should be called before any attempt to use any VTK abstractions. This
     * method attempts to load each library using the
     * {@link #vtkNativeLibrary.LoadLibrary()} method, and reports all
     * {@link UnsatisfiedLinkError}s that are thrown by any of these invocations.
     * Then, if *any* such errors were thrown, this method will itself throw an
     * {@link UnsatisfiedLinkError}.
     * <p>
     * Note that this method does not otherwise handle any other {@link Throwable}
     * thrown by VTK.
     * <p>
     * All VTK loading methods only attempt to load the libraries the first time any
     * of them is called. Subsequent calls to any method have no effect.
     * <p>
     * Before attempting to initialize VTK, this method ensures that Java AWT event
     * dispatch thread is running. If that throws an exception, this method throws a
     * {@link RuntimeException} before even trying to set up VTK.
     * 
     * @throws UnsatisfiedLinkError if any of the native libraries failed to load.
     * @throws RuntimeException if the AWT cannot be initialized.
     *
     */
    public static void loadAllVtkLibraries()
    {
        if (isVtkInitialized.compareAndSet(false, true))
        {
            // Note made during refactoring in late 2019 (Redmine issue #2045).
            // The code below for ensuring the AWT was initialized appears to be
            // unnecessary. It is possible an earlier version of VTK needed it. Leaving it
            // in for now out of an abundance of caution. However, the original code for
            // this was flawed in that it did not block until AWT was initialized. Corrected
            // it so that this code behaves as was evidently intended. Original comment and
            // commented-out code below:
            //
            // Before loading the native vtk libraries, we want to make sure the
            // awt/swing subsystem is loaded and initialized since by doing this, we
            // ensure that other java-internal shared libraries which vtk depends on are
            // already loaded in. Failure to do so may result in linking errors when
            // loading in the vtk shared libraries (especially vtkRenderingJava).
            // The following dummy thread seems to do the trick.
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run()
//                {
//                    // do nothing
//                }
//            });

            if (!EventQueue.isDispatchThread())
            {
                try
                {
                    EventQueue.invokeAndWait(() -> {});
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            // 2019 (Redmine issue #2045): This is probably also unnecessary:
            System.loadLibrary("jawt"); // For some reason this is not loaded automatically
            boolean caughtLinkError = false;
            for (vtkNativeLibrary lib : vtkNativeLibrary.values())
            {
                try
                {
                    if (lib.IsBuilt())
                    {
                        lib.LoadLibrary();
                    }
                }
                catch (UnsatisfiedLinkError e)
                {
                    caughtLinkError = true;
                    e.printStackTrace();
                }
            }

            if (caughtLinkError)
            {
                throw new UnsatisfiedLinkError("One or more VTK libraries failed to load");
            }
        }
    }

    /**
     * Load only the VTK native shared libraries that are functional in "headless"
     * mode.
     * <p>
     * This should be called before any attempt to use any VTK abstractions. This
     * method attempts to load each library using the
     * {@link #vtkNativeLibrary.LoadLibrary()} method, and reports all
     * {@link UnsatisfiedLinkError}s that are thrown by any of these invocations.
     * Then, if *any* such errors were thrown, this method will itself throw an
     * {@link UnsatisfiedLinkError}.
     * <p>
     * Note that this method does not otherwise handle any other {@link Throwable}
     * thrown by VTK.
     * <p>
     * All VTK loading methods only attempt to load the libraries the first time any
     * of them is called. Subsequent calls to any method have no effect.
     * 
     * @throws UnsatisfiedLinkError if any of the native libraries failed to load.
     *
     */
    public static void loadVtkLibrariesHeadless()
    {
        if (isVtkInitialized.compareAndSet(false, true))
        {
            boolean caughtLinkError = false;
            for (vtkNativeLibrary lib : vtkNativeLibrary.values())
            {
                try
                {
                    if (lib.IsBuilt() && !lib.GetLibraryName().startsWith("vtkRendering")
                            && !lib.GetLibraryName().startsWith("vtkViews")
                            && !lib.GetLibraryName().startsWith("vtkInteraction")
                            && !lib.GetLibraryName().startsWith("vtkCharts")
                            && !lib.GetLibraryName().startsWith("vtkDomainsChemistry")
                            && !lib.GetLibraryName().startsWith("vtkIOParallel")
                            && !lib.GetLibraryName().startsWith("vtkIOExport")
                            && !lib.GetLibraryName().startsWith("vtkIOImport")
                            && !lib.GetLibraryName().startsWith("vtkIOMINC")
                            // && !lib.GetLibraryName().startsWith("vtkFiltersHybrid")
                            && !lib.GetLibraryName().startsWith("vtkFiltersParallel")
                            && !lib.GetLibraryName().startsWith("vtkGeovis"))
                    {
                        lib.LoadLibrary();
                    }
                }
                catch (UnsatisfiedLinkError e)
                {
                    e.printStackTrace();
                }
            }

            if (caughtLinkError)
            {
                throw new UnsatisfiedLinkError("One or more (headless) VTK libraries failed to load");
            }
        }
    }

    public static void loadSpiceLibraries()
    {
        System.loadLibrary("JNISpice");
    }

}
