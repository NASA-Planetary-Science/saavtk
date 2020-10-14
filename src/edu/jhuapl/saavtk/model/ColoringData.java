package edu.jhuapl.saavtk.model;

import java.util.List;

import vtk.vtkFloatArray;

public interface ColoringData
{
    String getName();
    
    String getUnits();
    
    Integer getNumberElements();
    
    List<String> getElementNames();

    Boolean hasNulls();
    
    vtkFloatArray getData();
    
    double[] getCurrentRange();
    
    double[] getDefaultRange();

    void clear();
}
