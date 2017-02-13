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
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;

public abstract class AbstractModel implements Model
{
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport( this );

    public void addPropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.addPropertyChangeListener( listener ); }

    public void removePropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.removePropertyChangeListener( listener ); }

    private boolean visible = true;

    private CommonData commonData;

    /**
     * Should be called be the model manager to set the common data.
     * @param commonData
     */
    public void setCommonData(CommonData commonData)
    {
        this.commonData = commonData;
    }

    public CommonData getCommonData()
    {
        return this.commonData;
    }

    public abstract List<vtkProp> getProps();

    public boolean isBuiltIn()
    {
        return true;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean b)
    {
        if (this.visible != b)
        {
            this.visible = b;
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    /**
     * Return what text should be displayed if the user clicks on one of the
     * props of this model and the specified cellId and point. By default an empty string
     * is returned. Subclasses may override this behavior.
     * @param prop
     * @param cellId
     * @param pickPosition
     * @return
     */
    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        return "";
    }

    public void setOpacity(double opacity)
    {
        // Do nothing. Subclasses should redefine this if they support opacity.
    }

    public double getOpacity()
    {
        // Subclasses should redefine this if they support opacity.
        return 1.0;
    }

    /**
     * Some models have vertex values that overlap the SmallBodyModel and are thus obscured.
     * In these cases it may be helpful to shift the vertices slightly away from the shape model
     * so they are not obscured. This function, which by default does nothing may be used
     * by subclasses for this purpose.
     *
     * @param offset
     */
    public void setOffset(double offset)
    {
        // Do nothing. Subclasses should redefine this if they support offset.
    }

    public double getOffset()
    {
        // Subclasses should redefine this if they support offset.
        return 0.0;
    }

    public double getDefaultOffset()
    {
        // Subclasses should redefine this if they support offset.
        return 0.0;
    }

    public void delete()
    {

    }

    public void set2DMode(boolean enable)
    {
        // do nothing by default. Subclass must override if it supports 2D mode
        // and create actors that are in 2D.
    }

    public boolean supports2DMode()
    {
        // By default, a model does not support 2D and this function returns false
        // unless model overrides this function to return true. Model must
        // also override the function set2DMode.
        return false;
    }

    private void fixCellsAlongSeam(vtkPolyData polydata, boolean lowerSide)
    {
        int numberPoints = polydata.GetNumberOfPoints();
        vtkPoints points = polydata.GetPoints();
        double[] point = new double[3];
        for (int i=0; i<numberPoints; ++i)
        {
            points.GetPoint(i, point);
            double longitude = point[2];
            if (lowerSide)
            {
                if (longitude > 1.5 * Math.PI)
                    longitude = 0.0;
            }
            else
            {
                if (longitude < 0.5 * Math.PI)
                    longitude = 2.0 * Math.PI;
            }

            point[2] = longitude;
            points.SetPoint(i, point);
        }
    }

    protected vtkAlgorithmOutput projectTo2D(vtkPolyData polydata)
    {
        final double[] origin = {0.0, 0.0, 0.0};
        final double[] zaxis = {0.0, 0.0, 1.0};
        vtkPlane planeZeroLon = new vtkPlane();
        double[] vec = MathUtil.latrec(new LatLon(0.0, 0.0, 1.0));
        double[] normal = new double[3];
        MathUtil.vcrss(vec, zaxis, normal);
        planeZeroLon.SetOrigin(origin);
        planeZeroLon.SetNormal(normal);

        vtkClipPolyData clipPolyData = new vtkClipPolyData();
        clipPolyData.SetClipFunction(planeZeroLon);
        clipPolyData.SetInputData(polydata);
        clipPolyData.GenerateClippedOutputOn();
        clipPolyData.Update();
        vtkAlgorithmOutput outputPort = clipPolyData.GetOutputPort();
        vtkAlgorithmOutput clippedOutputPort = clipPolyData.GetClippedOutputPort();

        vtkSphericalTransform transform = new vtkSphericalTransform();
        vtkAbstractTransform inverse = transform.GetInverse();

        vtkTransformPolyDataFilter transformFilter1 = new vtkTransformPolyDataFilter();
        transformFilter1.SetInputConnection(outputPort);
        transformFilter1.SetTransform(inverse);
        transformFilter1.Update();
        vtkPolyData transformFilter1Output = transformFilter1.GetOutput();

        vtkTransformPolyDataFilter transformFilter2 = new vtkTransformPolyDataFilter();
        transformFilter2.SetInputConnection(clippedOutputPort);
        transformFilter2.SetTransform(inverse);
        transformFilter2.Update();
        vtkPolyData transformFilter2Output = transformFilter2.GetOutput();

        vtkPolyData lowerHalf = new vtkPolyData();
        vtkPolyData upperHalf = new vtkPolyData();

        lowerHalf.ShallowCopy(transformFilter1Output);
        upperHalf.ShallowCopy(transformFilter2Output);

        fixCellsAlongSeam(lowerHalf, false);
        fixCellsAlongSeam(upperHalf, true);

        vtkAppendPolyData appendFilter = new vtkAppendPolyData();
        appendFilter.UserManagedInputsOff();
        appendFilter.AddInputData(lowerHalf);
        appendFilter.AddInputData(upperHalf);
        appendFilter.Update();

        return appendFilter.GetOutputPort();
    }
}
