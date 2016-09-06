package edu.jhuapl.saavtk.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import vtk.vtkProp;


/**
 * Model of structures drawn on a body such as lines and circles.
 */
public abstract class StructureModel extends AbstractModel
{
    public static abstract class Structure
    {
        public abstract String getClickStatusBarText();
        public abstract int getId();
        public abstract String getName();
        public abstract void setName(String name);
        public abstract String getType();
        public abstract String getInfo();
        public abstract int[] getColor();
        public abstract void setColor(int[] color);
        public abstract void setLabel(String label);
        public abstract String getLabel();
        public abstract boolean getHidden();
        public abstract boolean getLabelHidden();
        public abstract void setHidden(boolean b);
        public abstract void setLabelHidden(boolean b);
    }

    public abstract List<vtkProp> getProps();

    public abstract void addNewStructure();

    public abstract boolean supportsActivation();

    public abstract void activateStructure(int idx);

    public abstract int getActivatedStructureIndex();

    public abstract void selectStructures(int[] indices);

    public abstract int[] getSelectedStructures();

    public abstract int getNumberOfStructures();

    public abstract void removeStructure(int idx);

    public abstract void removeStructures(int[] indices);

    public abstract void removeAllStructures();

    public abstract Structure getStructure(int idx);

    public abstract int getStructureIndexFromCellId(int cellId, vtkProp prop);

    public abstract void loadModel(File file, boolean append) throws Exception;

    public abstract void saveModel(File file) throws Exception;

    public abstract void setStructureColor(int idx, int[] color);

    public abstract boolean setStructureLabel(int idx, String label);

    public abstract double getLineWidth();

    public abstract void setLineWidth(double width);

    public abstract void setStructuresHidden(int[] indices, boolean hidden, boolean labelHidden);

    public abstract boolean isStructureHidden(int id);

    public abstract void colorLabel(int[] colors);

    public abstract void setLabelsVisible(boolean b);

    public abstract boolean isLabelHidden(int id);

    public abstract void showBorders();

    public abstract void changeFont(int font_size, int structure);

    public abstract void changeFontType(int structure);

    public void savePlateDataInsideStructure(int idx, File file) throws IOException
    {
        // do nothing by default. Only structures that have an inside need to implement this.
    }

    // For polygons which take a long time to draw, implement this function
    // to only show interior when explicitly told. If not reimplemented, then interiod
    // is always shown.
    public void setShowStructuresInterior(int[] indices, boolean show)
    {
        // by default do nothing
    }

    public boolean isShowStructureInterior(int id)
    {
        return false;
    }

    // Get the center of the structure. For ellipses and points, this is obvious.
    // For paths and polygons, this is the mean of the control points.
    public abstract double[] getStructureCenter(int id);

    // Get a measure of the size of the structure. For ellipses and points, this is the diameter.
    // For paths and polygons, this is twice the distance from the centroid to the farthers point
    // from the centroid.
    public abstract double getStructureSize(int id);

    public abstract double[] getStructureNormal(int id);

    public abstract void showLabel(int idx);
}
