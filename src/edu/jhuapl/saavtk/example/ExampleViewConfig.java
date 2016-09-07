package edu.jhuapl.saavtk.example;

import java.util.List;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;

public class ExampleViewConfig extends ViewConfig
{
    static public ExampleViewConfig getExampleConfig(ShapeModelBody name, ShapeModelAuthor author)
    {
        return (ExampleViewConfig)getConfig(name, author, null);
    }

    static public ExampleViewConfig getExampleConfig(ShapeModelBody name, ShapeModelAuthor author, String version)
    {
        return (ExampleViewConfig)getConfig(name, author, version);
    }

    public static void initialize()
    {
        List<ViewConfig> configArray = getBuiltInConfigs();

        ExampleViewConfig config = new ExampleViewConfig();
        config.customName = "data/brain.obj";
        config.customTemporary = true;
        config.author = ShapeModelAuthor.CUSTOM;
        configArray.add(config);

        config = new ExampleViewConfig();
        config.customName = "data/left-lung.obj";
        config.customTemporary = true;
        config.author = ShapeModelAuthor.CUSTOM;
        configArray.add(config);
    }

    public ExampleViewConfig clone() // throws CloneNotSupportedException
    {
        ExampleViewConfig c = (ExampleViewConfig)super.clone();

        return c;
    }

}
