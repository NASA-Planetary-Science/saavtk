package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;

import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import vtk.vtkProp;

public interface ModelManager extends Model, PropertyChangeListener, SceneChangeNotifier
{
	@Override
	public boolean isBuiltIn();

	@Override
	public CommonData getCommonData();

	public void setModels(HashMap<ModelNames, Model> models);

	@Override
	public List<vtkProp> getProps();

	public List<vtkProp> getPropsExceptSmallBody();

	@Override
	public void propertyChange(PropertyChangeEvent evt);

	public Model getModel(vtkProp prop);

	public Model getModel(ModelNames modelName);

	public PolyhedralModel getPolyhedralModel();

	public void deleteAllModels();

	@Override
	public void set2DMode(boolean enable);

	public boolean is2DMode();

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener);

	@Override
	public void notifySceneChange();

}
