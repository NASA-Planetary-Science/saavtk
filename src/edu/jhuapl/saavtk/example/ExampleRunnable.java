package edu.jhuapl.saavtk.example;

import java.util.concurrent.TimeUnit;

import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import vtk.vtkJavaGarbageCollector;

public class ExampleRunnable implements Runnable
{
    private String[] args;

    public ExampleRunnable(String[] args)
    {
        this.args = args;
    }

    @Override
    public void run()
    {
        NativeLibraryLoader.loadVtkLibraries();

        ExampleViewConfig.initialize();

        String tempShapeModelPath = null;
        // Parse options that come first
        int nargs = args.length;
        int i = 0;
        for (; i < nargs; ++i)
        {
            if (args[i].equals("--model") && i < nargs + 1)
            {
                tempShapeModelPath = args[i + 1];
            }
            else
            {
                // We've encountered something that is not an option, must be at the args
                break;
            }
        }

//        if (tempShapeModelPath == null)
//            tempShapeModelPath = "data/brain.obj";

        vtkJavaGarbageCollector garbageCollector = new vtkJavaGarbageCollector();
        // garbageCollector.SetDebug(true);
        garbageCollector.SetScheduleTime(5, TimeUnit.SECONDS);
        garbageCollector.SetAutoGarbageCollection(true);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setDismissDelay(600000); // 10 minutes

        MainWindow frame = new ExampleMainWindow(tempShapeModelPath);
        // MainWindow.setMainWindow(frame);
        frame.setVisible(true);
    }

}
