package edu.jhuapl.saavtk.gui;

import java.awt.CardLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.util.Configuration;

public abstract class ViewManager extends JPanel
{
    private List<View> builtInViews = new ArrayList<View>();
    private List<View> customViews = new ArrayList<View>();
    private View currentView;
    private final StatusBar statusBar;
    private final Frame frame;
    private String tempCustomShapeModelPath;

    private static String defaultModelName=null;
    private final static Path defaultModelFile=Paths.get(Configuration.getApplicationDataDir()+File.separator+"defaultModelToLoad");



    /**
     * The top level frame is required so that the title can be updated
     * when the view changes.
     *
     * @param statusBar
     * @param frame
     * @param tempCustomShapeModelPath path to shape model. May be null.
     * If non-null, the main window will create a temporary custom view of the shape model
     * which will be shown first. This temporary view is not saved into the custom application
     * folder and will not be available unless explicitely imported.
     */
    public ViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(new CardLayout());
        setBorder(BorderFactory.createEmptyBorder());
        this.statusBar = statusBar;
        this.frame = frame;
        this.tempCustomShapeModelPath = tempCustomShapeModelPath;

        setupViews();
    }

    protected void addBuiltInView(View view)
    {
        builtInViews.add(view);
    }

    protected void addBuiltInViews(StatusBar statusBar)
    {
    }

    protected void setupViews()
    {
        // add in any built-in views
        addBuiltInViews(statusBar);

        // add built-in views to the top-level JPanel
        for (View view : getBuiltInViews())
            add(view, view.getUniqueName());

        // load in any custom views specified on the command line or in the configuration directory
        loadCustomViews(getTempCustomShapeModelPath(), statusBar);

        // add custom views to the top-level JPanel
        for (View view : getCustomViews())
            add(view, view.getUniqueName());

        // if no view was specified on the command line, search the built-in views for the default body to load,
        // or the first built-in view if no default body has been specified
        if (getTempCustomShapeModelPath() == null)
        {
            int idxToShow=0;
            for (int i=0; i<getBuiltInViews().size(); i++)
                if (getBuiltInViews().get(i).getConfig().getUniqueName().equals(getDefaultBodyToLoad()))
                    idxToShow=i;
            setCurrentView(getBuiltInViews().get(idxToShow));
        }
        // if a custom view was specified on the command line, set that as the current view
        else
        {
            int idxToShow=0;
            for (int i=0; i<getCustomViews().size(); i++)
                if (getCustomViews().get(i).getConfig().getUniqueName().equals(getDefaultBodyToLoad()))
                    idxToShow=i;
            setCurrentView(getCustomViews().get(idxToShow));
        }
    }

    public void setDefaultBodyToLoad(String uniqueName)
    {
        try
        {
            defaultModelName=uniqueName;
            if (defaultModelFile.toFile().exists())
                defaultModelFile.toFile().delete();
            defaultModelFile.toFile().createNewFile();
            FileWriter writer=new FileWriter(defaultModelFile.toFile());
            writer.write(defaultModelName);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getDefaultBodyToLoad()
    {
        try
        {
            if (!defaultModelFile.toFile().exists())
                return ViewConfig.getBuiltInConfigs().get(0).getUniqueName();
            //
            Scanner scanner=new Scanner(ViewManager.defaultModelFile.toFile());
            if (scanner.hasNextLine())
                defaultModelName=scanner.nextLine();
            else
                defaultModelName=null;
            scanner.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return defaultModelName;
    }

    public void resetDefaultBodyToLoad()
    {
        if (defaultModelFile.toFile().exists())
            defaultModelFile.toFile().delete();
    }

    protected void loadCustomViews(String newCustomShapeModelPath, StatusBar statusBar)
    {
        if (newCustomShapeModelPath != null)
        {
            addCustomView(statusBar, newCustomShapeModelPath);
        }

        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        if (dirs != null && dirs.length > 0)
        {
            Arrays.sort(dirs);
            for (File dir : dirs)
            {
                if (new File(dir, "model.vtk").isFile())
                {
                    addCustomView(createCustomView(statusBar, dir.getName(), false));
                }
            }
        }
    }

    protected void addCustomView(StatusBar statusBar, String shapeModelPath)
    {
        addCustomView(createCustomView(statusBar, shapeModelPath, true));
    }

    public List<View> getBuiltInViews()
    {
        return builtInViews;
    }

    public void setBuiltInViews(List<View> builtInViews)
    {
        this.builtInViews = builtInViews;
    }

    public List<View> getCustomViews()
    {
        return customViews;
    }

    public void setCustomViews(List<View> customViews)
    {
        this.customViews = customViews;
    }

    public String getTempCustomShapeModelPath()
    {
        return tempCustomShapeModelPath;
    }

    public View getCurrentView()
    {
        return currentView;
    }

    public void setCurrentView(View view)
    {
        CardLayout cardLayout = (CardLayout)(getLayout());
        cardLayout.show(this, view.getUniqueName());

        // defer initialization of View until we show it.
        repaint();
        validate();
        view.initialize();

        currentView = view;

        frame.setTitle(currentView.getConfig().getPathRepresentation());
    }

    public View getBuiltInView(int i)
    {
        return builtInViews.get(i);
    }

    public int getNumberOfBuiltInViews()
    {
        return builtInViews.size();
    }

    public View getCustomView(int i)
    {
        return customViews.get(i);
    }

    public int getNumberOfCustomViews()
    {
        return customViews.size();
    }

    protected void addCustomView(View view)
    {
        customViews.add(view);
    }

    public View addCustomView(String name)
    {
        View view = createCustomView(statusBar, name, false);
        addCustomView(view);
        add(view, view.getUniqueName());
        return view;
    }

    public View removeCustomView(String name)
    {
        for (View view : customViews)
        {
            if (view.getConfig().getShapeModelName().equals(name))
            {
                customViews.remove(view);
                remove(view);
                return view;
            }
        }

        return null;
    }

    public abstract View createCustomView(StatusBar statusBar, String name, boolean temporary);

    public View getCustomView(String name)
    {
        for (View view : customViews)
        {
            if (view.getUniqueName().equals(name))
            {
                return view;
            }
        }

        return null;
    }

    public List<View> getAllViews()
    {
        List<View> allViews = new ArrayList<View>();
        allViews.addAll(builtInViews);
        allViews.addAll(customViews);
        return allViews;
    }
}
