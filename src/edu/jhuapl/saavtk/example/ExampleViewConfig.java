package edu.jhuapl.saavtk.example;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;

public class ExampleViewConfig extends ViewConfig
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
		List<ViewConfig> configArray = getBuiltInConfigs();

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

}
