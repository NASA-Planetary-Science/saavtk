package edu.jhuapl.saavtk.example;

import java.awt.Frame;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.model.Config;
import edu.jhuapl.saavtk.model.PolyhedralModelConfig;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;

public class ExampleViewManager extends ViewManager
{
    public ExampleViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);

        frame.setTitle("Example SAAVTK Tool");

    }

    protected void addBuiltInViews(StatusBar statusBar)
    {
        for (Config config: ExampleConfig.getBuiltInConfigs())
        {
            addBuiltInView(new ExampleView(statusBar, (ExampleConfig)config));
        }
    }

    public View createCustomView(StatusBar statusBar, String name, boolean temporary)
    {
        PolyhedralModelConfig config = new ExampleConfig();
        config.customName = name;
        config.customTemporary = temporary;
        config.author = ShapeModelAuthor.CUSTOM;
        return new ExampleView(statusBar, config);
    }
}
