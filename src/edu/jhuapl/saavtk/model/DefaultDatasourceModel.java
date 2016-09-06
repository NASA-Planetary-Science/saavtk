package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import vtk.vtkAbstractTransform;
import vtk.vtkAlgorithmOutput;
import vtk.vtkAppendPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkPlane;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkProp;
import vtk.vtkSphericalTransform;
import vtk.vtkTransformPolyDataFilter;
import vtk.vtksbCellLocator;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Preferences;
import edu.jhuapl.saavtk.util.Properties;

public class DefaultDatasourceModel extends DatasourceModel
{
    public boolean isBuiltIn() { return false; }

    public void updateScaleBarValue(double pixelSizeInKm) {}

    public void updateScaleBarPosition(int windowWidth, int windowHeight) {}

    public List<vtkProp> getProps() { return null; }

    public vtksbCellLocator getCellLocator() { return null; }

    public BoundingBox getBoundingBox() { return null; }

    public void setShowScaleBar(boolean enabled) { }

    public boolean getShowScaleBar() { return false; }

    public boolean isEllipsoid() { return false; }

    public vtkPolyData getSmallBodyPolyData() { return null; }

    public List<vtkPolyData> getSmallBodyPolyDatas() { return null; }

    public String getCustomDataFolder() { return null; }

    public String getConfigFilename() { return null; }
}
