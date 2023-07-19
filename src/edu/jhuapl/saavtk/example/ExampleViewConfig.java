package edu.jhuapl.saavtk.example;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;

public class ExampleViewConfig extends ViewConfig implements IBodyViewConfig
{

    static public ExampleViewConfig getExampleConfig(ShapeModelBody name, ShapeModelType author)
    {
        return (ExampleViewConfig) getConfig(name, author, null);
    }

    static public ExampleViewConfig getExampleConfig(ShapeModelBody name, ShapeModelType author, String version)
    {
        return (ExampleViewConfig) getConfig(name, author, version);
    }

    public static void initialize()
    {
        List<IBodyViewConfig> configArray = getBuiltInConfigs();

        ExampleViewConfig config = new ExampleViewConfig();
        config.modelLabel = "data/brain.obj";
        config.customTemporary = true;
        config.author = ShapeModelType.CUSTOM;
        configArray.add(config);

        config = new ExampleViewConfig();
        config.modelLabel = "data/left-lung.obj";
        config.customTemporary = true;
        config.author = ShapeModelType.CUSTOM;
        configArray.add(config);
    }

    protected ExampleViewConfig()
    {
        super(ImmutableList.of(), ImmutableList.of());
    }

    @Override
    public ExampleViewConfig clone() // throws CloneNotSupportedException
    {
        ExampleViewConfig c = (ExampleViewConfig) super.clone();

        return c;
    }

    @Override
    public boolean isAccessible()
    {
        return true;
    }

    @Override
    public String[] getShapeModelFileNames()
    {
        return new String[] { modelLabel };
    }

	@Override
	public String getRootDirOnServer()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasColoringData()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getDensity()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRotationRate()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String serverPath(String fileName)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
