package edu.jhuapl.saavtk.model;

import java.io.File;

import vtk.vtkActor;


/**
 * A type of structure which uses a set of control points to describe it. This
 * currently includes only paths (LineModel) and polygons (PolygonModel).
 */
public abstract class ControlPointsStructureModel extends StructureModel
{
    abstract public vtkActor getActivationActor();

    abstract public void selectCurrentStructureVertex(int idx);

    abstract public void insertVertexIntoActivatedStructure(double[] newPoint);

    abstract public void updateActivatedStructureVertex(int vertexId, double[] newPoint);

    abstract public void moveActivationVertex(int vertexId, double[] newPoint);

    abstract public void removeCurrentStructureVertex();

    //optional for subclasses
    public int getStructureIdFromActivationCellId(int idx)
    {
        return -1;
    }

    //optional for subclasses
    public int getVertexIdFromActivationCellId(int idx)
    {
        return -1;
    }

    public boolean hasProfileMode()
    {
        return false;
    }

    /**
     * Subclasses should only redefine this if they support profiles (i.e. they redefine
     * hasProfileMode to return true).
     *
     * @param cellId
     * @param file
     * @throws Exception
     */
    public void saveProfile(int cellId, File file) throws Exception
    {
        // do nothing
    }
}
