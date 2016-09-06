package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import vtk.vtkProp;

public interface ModelManager extends Model, PropertyChangeListener
{
    public boolean isBuiltIn();

    public CommonData getCommonData();

    public void setModels(HashMap<ModelNames, Model> models);

    public ArrayList<vtkProp> getProps();

    public ArrayList<vtkProp> getPropsExceptSmallBody();

    public void propertyChange(PropertyChangeEvent evt);

    public Model getModel(vtkProp prop);

    public Model getModel(ModelNames modelName);

    public PolyhedralModel getPolyhedralModel();

    public void deleteAllModels();

    public void set2DMode(boolean enable);

    public boolean is2DMode();

    public void addPropertyChangeListener(PropertyChangeListener listener);

}
