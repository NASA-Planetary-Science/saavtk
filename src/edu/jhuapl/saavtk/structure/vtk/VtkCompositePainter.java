package edu.jhuapl.saavtk.structure.vtk;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.vtk.VtkResource;

/**
 * Class which provides an abstraction of logic used to render a single
 * structure using the VTK framework.
 * <P>
 * This class provides a composite of two underlying Painters:
 * <UL>
 * <LI>The custom structure painter (relevant to the underlying structure)
 * <LI>A label painter used to render the associated painter.
 * </UL>
 *
 * @author lopeznr1
 */
public class VtkCompositePainter<G2 extends Structure, G1 extends VtkResource> implements VtkResource
{
	// State vars
	private final G1 mainPainter;
	private final VtkLabelPainter<G2> textPainter;

	/**
	 * Standard Constructor
	 * <P>
	 * Note that after providing the provided painters, the caller should no longer
	 * manage said painters but rather interact via this composite painter.
	 */
	public VtkCompositePainter(PolyhedralModel aSmallBody, StructureManager<G2> aManager, G2 aItem, G1 aMainPainter)
	{
		mainPainter = aMainPainter;
		textPainter = new VtkLabelPainter<>(aSmallBody, aManager, aItem);
	}

	/**
	 * Returns the main painter used to render the structure's body.
	 */
	public G1 getMainPainter()
	{
		return mainPainter;
	}

	/**
	 * Returns the text painter used to render the structure's label.
	 */
	public VtkLabelPainter<G2> getTextPainter()
	{
		return textPainter;
	}

	@Override
	public void vtkDispose()
	{
		// Delegate
		mainPainter.vtkDispose();
		textPainter.vtkDispose();
	}

	@Override
	public void vtkUpdateState()
	{
		// Delegate
		mainPainter.vtkUpdateState();
		textPainter.vtkUpdateState();
	}

}
