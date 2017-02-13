package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.HashMap;

import vtk.vtkProp;

public interface ModelManager extends Renderable, PropertyChangeListener
{
    public boolean isBuiltIn();

    public CommonData getCommonData();

    public void setModels(HashMap<ModelNames, Renderable> models);

    public List<vtkProp> getProps();

    public List<vtkProp> getPropsExceptSmallBody();

    public void propertyChange(PropertyChangeEvent evt);

    public Renderable getModel(vtkProp prop);

    public Renderable getModel(ModelNames modelName);

    public PolyhedralModel getPolyhedralModel();

    public void deleteAllModels();

    public void set2DMode(boolean enable);

    public boolean is2DMode();

    public void addPropertyChangeListener(PropertyChangeListener listener);

}
