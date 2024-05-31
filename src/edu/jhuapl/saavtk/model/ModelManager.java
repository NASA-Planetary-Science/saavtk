package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.ses.jsqrl.api.MetadataManager;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;
import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.util.Properties;
import vtk.vtkProp;

public class ModelManager extends AbstractModel
		implements PropertyChangeListener, MetadataManager, SceneChangeNotifier, VtkPropProvider
{
	private static final Version METADATA_VERSION = Version.of(1, 0);
	private final CommonData commonData;
	private final PolyhedralModel mainModel;
	private final ImmutableMap<ModelNames, List<Model>> allModels;

	private List<vtkProp> props = new ArrayList<>();
	private List<vtkProp> propsExceptSmallBody = new ArrayList<>();
	private HashMap<vtkProp, Model> propToModelMap = new HashMap<>();

	public ModelManager(PolyhedralModel mainModel, Map<ModelNames, List<Model>> allModels)
	{
		commonData = new CommonData();
		this.mainModel = mainModel;
		this.allModels = ImmutableMap.copyOf(allModels);

		for (ModelNames modelName : this.allModels.keySet())
		{
			List<Model> models = this.allModels.get(modelName);
			for (Model model : models)
			{
				model.setCommonData(commonData);
				model.addPropertyChangeListener(this);
			}
		}

		updateProps();
	}

	@Override
	public CommonData getCommonData()
	{
		return commonData;
	}

	@Override
	public void setCommonData(CommonData commonData)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isBuiltIn()
	{
		return getPolyhedralModel().isBuiltIn();
	}

	@Override
	public List<vtkProp> getProps()
	{
		return props;
	}

	// TODO: Add method comments
	public List<vtkProp> getPropsExceptSmallBody()
	{
		return propsExceptSmallBody;
	}

	@Override
	public void notifySceneChange()
	{
		updateProps();
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
			notifySceneChange();
	}

	private void clearProps()
	{
		props.clear();
		propsExceptSmallBody.clear();
		propToModelMap.clear();
	}

	private void updateProps()
	{
		clearProps();

		for (ModelNames modelName : allModels.keySet())
		{
			List<Model> models = allModels.get(modelName);
			for (Model model : models)
			{
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
	}

	// TODO: Add method comments
	public PolyhedralModel getPolyhedralModel()
	{
		return mainModel;
	}

	// TODO: Add method comments
	public Model getModel(vtkProp prop)
	{
		return propToModelMap.get(prop);
	}

	// TODO: Add method comments
	public Model getModel(ModelNames modelName)
	{
		return allModels.get(modelName).get(0);
	}
	
	// TODO: Add method comments
	public List<Model> getModels(ModelNames modelName)
	{
		return allModels.get(modelName);
	}

	// TODO: Add method comments
	public Map<ModelNames, List<Model>> getAllModels()
	{
		return allModels;
	}

	// TODO: Add method comments
	public void deleteAllModels()
	{
		for (ModelNames modelName : allModels.keySet())
			for (Model model : allModels.get(modelName))
				model.delete();	
	}

	@Override
	public void set2DMode(boolean enable)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Metadata store()
	{
		SettableMetadata metadata = SettableMetadata.of(METADATA_VERSION);
		for (Entry<ModelNames, List<Model>> entry : allModels.entrySet())
		{
			List<Model> models = entry.getValue();
			for (Model model : models)
			{
				if (model instanceof MetadataManager)
				{
					metadata.put(Key.of(entry.getKey().name()), ((MetadataManager) model).store());
				}
			}
		}

		return metadata;
	}

	@Override
	public void retrieve(Metadata source)
	{
		for (Entry<ModelNames, List<Model>> entry : allModels.entrySet())
		{
			List<Model> models = entry.getValue();
			for (Model model : models)
			{
				Key<Metadata> key = Key.of(entry.getKey().name());
				if (source.hasKey(key))
				{
//					Model model = entry.getValue();
					if (model instanceof MetadataManager)
					{
						((MetadataManager) model).retrieve(source.get(key));
					}
				}
			}
		}
	}

}
