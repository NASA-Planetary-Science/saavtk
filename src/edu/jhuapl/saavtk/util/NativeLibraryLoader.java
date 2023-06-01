package edu.jhuapl.saavtk.util;

import static ch.unibas.cs.gravis.vtkjavanativelibs.VtkNativeLibraries.MAJOR_VERSION;
import static ch.unibas.cs.gravis.vtkjavanativelibs.VtkNativeLibraries.MINOR_VERSION;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jogamp.common.jvm.JNILibLoaderBase;

import ch.unibas.cs.gravis.vtkjavanativelibs.Platform;
import ch.unibas.cs.gravis.vtkjavanativelibs.Util;
import ch.unibas.cs.gravis.vtkjavanativelibs.VtkJavaNativeLibraryException;
import ch.unibas.cs.gravis.vtkjavanativelibs.VtkNativeLibraries;
import ch.unibas.cs.gravis.vtkjavanativelibs.VtkNativeLibrariesImpl;
import vtk.vtkNativeLibrary;

/**
 * Utility class for initializing native libraries, either all the libraries or
 * just the ones that are usable in "headless" mode.
 */
public class NativeLibraryLoader
{
    private static final AtomicBoolean isVtkInitialized = new AtomicBoolean(false);
    public static boolean debug = false;
    private static File nativeVTKLibraryDir;
    
    private static void unpackNatives()
    {
    	if(debug)
    	{
    		System.out.println("vtk-native version: " + MAJOR_VERSION + "." + MINOR_VERSION);
    		System.out.println("Java version: " + System.getProperty("java.version"));
    		System.out.println("Current platform: " + Platform.getPlatform());
    	}
        if (Platform.isUnknown()) {
        	System.err.println("Cannot determine the platform you are running on.");
        	System.exit(1);
        }

//        File nativeDir = new File(System.getProperty("java.io.tmpdir"));
        File nativeDir = new File(System.getProperty("user.home") + File.separator +".nativelibs");

        if(debug)
        	System.out.println("Will unpack to : " + nativeDir);

        try {
            NativeLibraryLoader.initialize(nativeDir);
            System.out.println("VTK: Initialization done, ");
        } catch (Throwable t) {
            System.err.println("Initialization failed with " + t.getClass().getSimpleName() + ", stacktrace follows.");
            t.printStackTrace(System.err);
            System.err.println("stacktrace above.");
            System.exit(1);
        }

        try {
            System.out.println(new vtk.vtkVersion().GetVTKVersion());
//            new vtkJoglPanelComponent();
        } catch (Throwable t) {
            System.out.println("Could not invoke vtk Methode" +t.getMessage());
            t.printStackTrace();
        }
    }
    
    public static void initialize(File nativeLibraryBaseDirectory)
    	      throws VtkJavaNativeLibraryException {


	    VtkNativeLibrariesImpl impl = VtkNativeLibraries.detectPlatform();
	    
	    initialize(nativeLibraryBaseDirectory, impl);
	  }
    
    /**
     * Initialize a given native library bundle by extracting it in a given directory.
     *
     * @throws VtkJavaNativeLibraryException if anything goes wrong.
     */
    public static void initialize(File nativeLibraryBaseDirectory, VtkNativeLibrariesImpl impl)
        throws VtkJavaNativeLibraryException {
      
      if(debug)
        System.out.println("Using natives provided by " + impl.getClass().getSimpleName());
      
      // Create the target directory if it does not exist
      nativeVTKLibraryDir = Util.createNativeDirectory(nativeLibraryBaseDirectory);

      if(debug)
        System.out.println("Extract VTK to " + nativeVTKLibraryDir);
      
      // Loads mawt.so
      Toolkit.getDefaultToolkit();

      // Loads jawt.so - this seems to be required on some systems
      try {
        System.loadLibrary("jawt");
      } catch (UnsatisfiedLinkError ignored) {
      }

      // ---------------------------------
      // Copy and load JOGL libraries one by one

      for (URL libraryUrl : impl.getJoglLibraries()) {
        String nativeName = libraryUrl.getFile();
//        System.out.println("NativeLibraryLoader: initialize: loading JOGL " + nativeName);
        File file = new File(nativeVTKLibraryDir,
            nativeName.substring(nativeName.lastIndexOf('/') + 1, nativeName.length()));

        try {
          Util.copyUrlToFile(libraryUrl, file);
        } catch (IOException e) {
          throw new VtkJavaNativeLibraryException("Error while copying library " + nativeName, e);
        }

        Runtime.getRuntime().load(file.getAbsolutePath());

        // we register the library as loaded in jogl, as otherwise it will try to load it again.
        String joglName = getBasename(file);
        if (joglName.startsWith("lib")) {
          joglName = joglName.replace("lib", "");
        }
        JNILibLoaderBase.addLoaded(joglName);

        Runtime.getRuntime().load(file.getAbsolutePath());
      }

      // ---------------------------------
      // Copy and load VTK libraries one by one

      for (URL libraryUrl : impl.getVtkLibraries()) {
        String nativeName = libraryUrl.getFile();

        File file = new File(nativeVTKLibraryDir,
            nativeName.substring(nativeName.lastIndexOf('/') + 1, nativeName.length()));

        try {
          Util.copyUrlToFile(libraryUrl, file);
        } catch (IOException e) {
          throw new VtkJavaNativeLibraryException("Error while copying library " + nativeName, e);
        }
      }
      
      for (URL libraryUrl : impl.getVtkLibraries()) {
          String nativeName = libraryUrl.getFile();
          File file = new File(nativeVTKLibraryDir,
              nativeName.substring(nativeName.lastIndexOf('/') + 1, nativeName.length()));
//          System.out.println("file is " + file.getAbsolutePath());
          Runtime.getRuntime().load(file.getAbsolutePath());
      }
      


      // vtkNativeLibrary.DisableOutputWindow(null);
    }
    
    private static String getBasename(File file) {
        String filename = file.getName().toString();
        String[] tokens = filename.split("\\.(?=[^\\.]+$)");
        return tokens[0];
      }
    
    
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

    	unpackNatives();
        if (Configuration.isHeadless())
        {
            loadHeadlessVtkLibraries();
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
     * @throws HeadlessException if the runtime environment is headless.
     * @throws RuntimeException if the AWT cannot be initialized.
     */
    public static void loadAllVtkLibraries()
    {
//    	try {
//    		File nativeDir = new File(System.getProperty("user.home") + File.separator +".nativelibs");
//			VtkNativeLibraries.initialize(nativeDir);
//		} catch (VtkJavaNativeLibraryException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	System.out.println("NativeLibraryLoader: loadAllVtkLibraries: unpacking natives");
    	unpackNatives();
        if (isVtkInitialized.compareAndSet(false, true))
        {
            if (Configuration.isHeadless())
            {
                throw new HeadlessException("Cannot load all VTK libraries in a headless environment");
            }

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
                    if (!lib.IsLoaded())
                    {
//                    	System.out.println("NativeLibraryLoader: loadAllVtkLibraries: loading " + lib.GetLibraryName());
//                        lib.LoadLibrary();
                    	
                    	if (System.getProperty("os.name").contains("Mac"))
                    			System.load(new File(nativeVTKLibraryDir, "lib" + lib.GetLibraryName() + ".jnilib").getAbsolutePath());
                    	else if (System.getProperty("os.name").contains("Win"))
                			System.load(new File(nativeVTKLibraryDir, lib.GetLibraryName() + ".dll").getAbsolutePath());
                    	else
                			System.load(new File(nativeVTKLibraryDir, "lib" + lib.GetLibraryName() + ".so").getAbsolutePath());

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
    public static void loadHeadlessVtkLibraries()
    {
        if (isVtkInitialized.compareAndSet(false, true))
        {
            boolean caughtLinkError = false;
            for (vtkNativeLibrary lib : vtkNativeLibrary.values())
            {
                try
                {
                    if (!lib.IsLoaded() && !lib.GetLibraryName().startsWith("vtkRendering")
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
                    	System.load(new File(nativeVTKLibraryDir, "lib" + lib.GetLibraryName() + ".jnilib").getAbsolutePath());
//                        lib.LoadLibrary();
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
    	if (System.getProperty("os.name").contains("Mac"))
    		System.load(new File(nativeVTKLibraryDir, "libJNISpice.jnilib").getAbsolutePath());
    	else if (System.getProperty("os.name").contains("Win"))
    		System.load(new File(nativeVTKLibraryDir, "JNISpice.dll").getAbsolutePath());
    	else
    		System.load(new File(nativeVTKLibraryDir, "libJNISpice.so").getAbsolutePath());
//        System.loadLibrary("JNISpice");
    }

}
