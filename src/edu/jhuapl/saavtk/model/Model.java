package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeListener;
import java.util.List;

import vtk.vtkProp;

public interface Model
{
    public void addPropertyChangeListener( PropertyChangeListener listener );
    public void removePropertyChangeListener( PropertyChangeListener listener );

    /** The purpose of this class is to store various parameters that are shared
        across all models, .e.g. selection color. The ModelManager should instantiate
        an instance of this class and pass on the instance to each model via the
        setCommonData function. This way each model has access to the data.
     */
    /**
     * Should be called be the model manager to set the common data.
     * @param commonData
     */
    public void setCommonData(CommonData commonData);

    public CommonData getCommonData();

    public List<vtkProp> getProps();

    public boolean isBuiltIn();

    public boolean isVisible();

    public void setVisible(boolean b);

    /**
     * Return what text should be displayed if the user clicks on one of the
     * props of this model and the specified cellId and point. By default an empty string
     * is returned. Subclasses may override this behavior.
     * @param prop
     * @param cellId
     * @param pickPosition
     * @return
     */
    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition);

    public void setOpacity(double opacity);

    public double getOpacity();

    /**
     * Some models have vertex values that overlap the SmallBodyModel and are thus obscured.
     * In these cases it may be helpful to shift the vertices slightly away from the shape model
     * so they are not obscured. This function, which by default does nothing may be used
     * by subclasses for this purpose.
     *
     * @param offset
     */
    public void setOffset(double offset);

    public double getOffset();

    public double getDefaultOffset();

    public void delete();

    public void set2DMode(boolean enable);

    public boolean supports2DMode();
}
