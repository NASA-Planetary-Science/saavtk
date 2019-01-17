package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.util.Properties;
import vtk.vtkProp;
import vtk.vtksbCellLocator;

public class AbstractModelManager extends DefaultDatasourceModel implements ModelManager, PropertyChangeListener
{
	private final PolyhedralModel mainModel;
	private final ImmutableMap<ModelNames, Model> allModels;

	private List<vtkProp> props = new ArrayList<>();
	private List<vtkProp> propsExceptSmallBody = new ArrayList<>();
	private HashMap<vtkProp, Model> propToModelMap = new HashMap<>();
	private boolean mode2D = false;

	public AbstractModelManager(PolyhedralModel mainModel, Map<ModelNames, Model> allModels)
	{
		super(new CommonData());
		this.mainModel = mainModel;
		this.allModels = ImmutableMap.copyOf(allModels);

		final CommonData commonData = getCommonData();
		for (ModelNames modelName : this.allModels.keySet())
		{
			Model model = this.allModels.get(modelName);
			model.setCommonData(commonData);
			model.addPropertyChangeListener(this);
		}

		updateProps();
	}

	protected void addProp(vtkProp prop, Model model)
	{
		propToModelMap.put(prop, model);
	}

	@Override
	public void updateScaleBarValue(@SuppressWarnings("unused") double pixelSizeInKm)
	{}

	@Override
	public void updateScaleBarPosition(@SuppressWarnings("unused") int windowWidth, @SuppressWarnings("unused") int windowHeight)
	{}

	@Override
	public vtksbCellLocator getCellLocator()
	{
		return null;
	}

	@Override
	public boolean isBuiltIn()
	{
		return getPolyhedralModel().isBuiltIn();
	}

	@Override
	public void setModels(@SuppressWarnings("unused") HashMap<ModelNames, Model> models)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<vtkProp> getProps()
	{
		return props;
	}

	@Override
	public List<vtkProp> getPropsExceptSmallBody()
	{
		return propsExceptSmallBody;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			updateProps();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	protected void clearProps()
	{
		props.clear();
		propsExceptSmallBody.clear();
		propToModelMap.clear();
	}

	public void updateProps()
	{
		clearProps();

		for (ModelNames modelName : allModels.keySet())
		{
			Model model = allModels.get(modelName);
			if (model.isVisible())
			{
				props.addAll(model.getProps());

				for (vtkProp prop : model.getProps())
					propToModelMap.put(prop, model);

				if (!(model instanceof PolyhedralModel))
					propsExceptSmallBody.addAll(model.getProps());
			}
		}
	}

	@Override
	public PolyhedralModel getPolyhedralModel()
	{
		return mainModel;

		//        for (ModelNames modelName : allModels.keySet())
		//        {
		//            Model model = allModels.get(modelName);
		//            if (model instanceof PolyhedralModel)
		//                return (PolyhedralModel)model;
		//        }
		//
		//        return null;
	}

	@Override
	public Model getModel(vtkProp prop)
	{
		return propToModelMap.get(prop);
	}

	@Override
	public Model getModel(ModelNames modelName)
	{
		return allModels.get(modelName);
	}

	public Map<ModelNames, Model> getAllModels()
	{
		return allModels;
	}

	@Override
	public void deleteAllModels()
	{
		for (ModelNames modelName : allModels.keySet())
			allModels.get(modelName).delete();
	}

	@Override
	public void set2DMode(boolean enable)
	{
		mode2D = enable;

		for (ModelNames modelName : allModels.keySet())
			allModels.get(modelName).set2DMode(enable);
	}

	@Override
	public boolean is2DMode()
	{
		return mode2D;
	}
}
