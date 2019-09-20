package edu.jhuapl.saavtk.model.structure;

import java.io.File;

import edu.jhuapl.saavtk.structure.StructureManager;
import vtk.vtkActor;

/**
 * A type of structure which uses a set of control points to describe it. This
 * currently includes only paths (LineModel) and polygons (PolygonModel).
 */
public abstract class ControlPointsStructureModel<G1 extends Line> extends StructureManager<G1>
{
	abstract public vtkActor getActivationActor();

	abstract public void selectCurrentStructureVertex(int idx);

	abstract public void insertVertexIntoActivatedStructure(double[] newPoint);

	abstract public void updateActivatedStructureVertex(int vertexId, double[] newPoint);

	abstract public void moveActivationVertex(int vertexId, double[] newPoint);

	abstract public void removeCurrentStructureVertex();

	// optional for subclasses
	public G1 getStructureFromActivationCellId(int idx)
	{
		return null;
	}

	// optional for subclasses
	public int getVertexIdFromActivationCellId(int idx)
	{
		return -1;
	}

	public boolean hasProfileMode()
	{
		return false;
	}

	/**
	 * * Save out a file which contains the value of the various coloring data as a
	 * function of distance along the profile. A profile is path with only 2 control
	 * points.
	 * <P>
	 * Subclasses should only redefine this if they support profiles (i.e. they
	 * redefine hasProfileMode to return true).
	 *
	 * @param aItem
	 * @param aFile
	 * @throws Exception
	 */
	public void saveProfile(G1 aItem, File aFile) throws Exception
	{
		// do nothing
	}
}
