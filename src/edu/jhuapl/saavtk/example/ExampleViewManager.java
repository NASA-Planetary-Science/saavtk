package edu.jhuapl.saavtk.example;

import java.awt.Frame;
import java.io.File;

import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.status.StatusNotifier;

public final class ExampleViewManager extends ViewManager
{
	public ExampleViewManager(StatusNotifier aStatusNotifier, Frame frame, String tempCustomShapeModelPath)
	{
		super(aStatusNotifier, frame, tempCustomShapeModelPath);

		frame.setTitle("Example SAAVTK Tool");
		setupViews(); // Must be called before this view manager is used.
	}

	@Override
	protected void addBuiltInViews(StatusNotifier aStatusNotifier)
	{
		for (IBodyViewConfig config : ExampleViewConfig.getBuiltInConfigs())
		{
			addBuiltInView(new ExampleView(aStatusNotifier, (ViewConfig)config));
		}
	}

	@Override
	protected View createCustomView(StatusNotifier aStatusNotifier, String name, boolean temporary)
	{
		IBodyViewConfig config = new ExampleViewConfig();
		config.setModelLabel(name);
		config.setCustomTemporary(temporary);
		config.setAuthor(ShapeModelType.CUSTOM);
		return new ExampleView(aStatusNotifier, (ViewConfig)config);
	}

	@Override
	public View createCustomView(String name, boolean temporary, File metadata) {
		// TODO Auto-generated method stub
		return createCustomView(refStatusNotifier, name, temporary);
	}

	@Override
	public void initializeStateManager()
	{
		// TODO Auto-generated method stub
	}
}
