package edu.jhuapl.saavtk.util;

import vtk.vtkNativeLibrary;


public class NativeLibraryLoader
{
    /**
     * Loads in the vtk shared libraries. This needs to be called before any vtk classes
     * are used. This has been tested with vtk version 5.6.0 only.
     *
     */
    static public void loadVtkLibraries()
    {
        // Before loading the native vtk libraries, we want to make sure the
        // awt/swing subsystem is loaded and initialized since by doing this, we
        // ensure that other java-internal shared libraries which vtk depends on are
        // already loaded in. Failure to do so may result in linking errors when
        // loading in the vtk shared libraries (especially vtkRenderingJava).
        // The following dummy thread seems to do the trick.
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                // do nothing
            }
        });

        System.loadLibrary("jawt"); // For some reason this is not loaded automatically
        for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
            try {
                if (lib.IsBuilt()) {
                    lib.LoadLibrary();
                }
            }
            catch (UnsatisfiedLinkError e) {
                e.printStackTrace();
            }
        }
    }

    static public void loadVtkLibrariesHeadless()
    {
        for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
            try {
                if (lib.IsBuilt() && !lib.GetLibraryName().startsWith("vtkRendering")
                        && !lib.GetLibraryName().startsWith("vtkViews")
                        && !lib.GetLibraryName().startsWith("vtkInteraction")
                        && !lib.GetLibraryName().startsWith("vtkCharts")
                        && !lib.GetLibraryName().startsWith("vtkDomainsChemistry")
                        && !lib.GetLibraryName().startsWith("vtkIOParallel")
                        && !lib.GetLibraryName().startsWith("vtkIOExport")
                        && !lib.GetLibraryName().startsWith("vtkIOImport")
                        && !lib.GetLibraryName().startsWith("vtkIOMINC")
                        && !lib.GetLibraryName().startsWith("vtkFiltersHybrid")
                        && !lib.GetLibraryName().startsWith("vtkFiltersParallel")
                        && !lib.GetLibraryName().startsWith("vtkGeovis")) {
                    lib.LoadLibrary();
                }
            }
            catch (UnsatisfiedLinkError e) {
                e.printStackTrace();
            }
        }
    }

}
