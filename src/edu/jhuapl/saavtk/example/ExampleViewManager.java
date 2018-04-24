package edu.jhuapl.saavtk.example;

import java.awt.Frame;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.model.ShapeModelType;

public final class ExampleViewManager extends ViewManager
{
	public ExampleViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
	{
		super(statusBar, frame, tempCustomShapeModelPath);

		frame.setTitle("Example SAAVTK Tool");
		setupViews(); // Must be called before this view manager is used.
	}

	@Override
	protected void addBuiltInViews(StatusBar statusBar)
	{
		for (ViewConfig config : ExampleViewConfig.getBuiltInConfigs())
		{
			addBuiltInView(new ExampleView(statusBar, config));
		}
	}

	@Override
	public View createCustomView(StatusBar statusBar, String name, boolean temporary)
	{
		ViewConfig config = new ExampleViewConfig();
		config.modelLabel = name;
		config.customTemporary = temporary;
		config.author = ShapeModelType.CUSTOM;
		return new ExampleView(statusBar, config);
	}

	@Override
	public void setUpStateManagerOnce()
	{
		// TODO Auto-generated method stub
	}
}
