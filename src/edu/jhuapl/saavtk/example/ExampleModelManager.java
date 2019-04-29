package edu.jhuapl.saavtk.example;

import java.beans.PropertyChangeListener;
import java.util.Map;

import edu.jhuapl.saavtk.model.AbstractModelManager;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;

public class ExampleModelManager extends AbstractModelManager implements PropertyChangeListener
{
	public ExampleModelManager(PolyhedralModel mainModel, Map<ModelNames, Model> allModels)
	{
		super(mainModel, allModels);
	}
}
