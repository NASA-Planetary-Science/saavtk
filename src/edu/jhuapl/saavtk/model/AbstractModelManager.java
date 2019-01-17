package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.jhuapl.saavtk.util.Properties;
import vtk.vtkProp;
import vtk.vtksbCellLocator;

public class AbstractModelManager extends DefaultDatasourceModel implements ModelManager, PropertyChangeListener
{
	private PolyhedralModel mainModel;
	private List<vtkProp> props = new ArrayList<vtkProp>();
	private List<vtkProp> propsExceptSmallBody = new ArrayList<vtkProp>();
	private HashMap<vtkProp, Model> propToModelMap = new HashMap<vtkProp, Model>();
	private HashMap<ModelNames, Model> allModels = new HashMap<ModelNames, Model>();
	private boolean mode2D = false;

	public AbstractModelManager(PolyhedralModel mainModel)
	{
		super();
		this.mainModel = mainModel;
	}

	protected void addProp(vtkProp prop, Model model)
	{
		propToModelMap.put(prop, model);
	}

	@Override
	public void updateScaleBarValue(double pixelSizeInKm)
	{}

	@Override
	public void updateScaleBarPosition(int windowWidth, int windowHeight)
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
	public void setModels(HashMap<ModelNames, Model> models)
	{
		allModels.clear();

		CommonData commonData = new CommonData();
		setCommonData(commonData);

		for (ModelNames modelName : models.keySet())
		{
			Model model = models.get(modelName);
			model.addPropertyChangeListener(this);
			model.setCommonData(commonData);
			allModels.put(modelName, model);
		}

		updateProps();
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
