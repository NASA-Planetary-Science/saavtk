package edu.jhuapl.saavtk.structure.gui.action;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.util.MathUtil;
import glum.gui.action.PopAction;
import vtk.vtkCamera;
import vtk.rendering.vtkAbstractComponent;

/**
 * {@link PopAction} that will center a single {@link Structure}.
 * 
 * @author lopeznr1
 */
public class CenterStructureAction<G1 extends Structure> extends PopAction<G1>
{
	// Ref vars
	private final StructureManager<G1> refManager;
	private final Renderer refRenderer;
	private final PolyhedralModel refSmallBody;

	// Attributes
	private final boolean preserveCurrentDistance;

	/**
	 * Standard Constructor
	 */
	public CenterStructureAction(StructureManager<G1> aManager, Renderer aRenderer, PolyhedralModel aSmallBody,
			boolean aPreserveCurrentDistance)
	{
		refManager = aManager;
		refRenderer = aRenderer;
		refSmallBody = aSmallBody;

		preserveCurrentDistance = aPreserveCurrentDistance;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if a single item is not selected
		if (aItemL.size() != 1)
			return;
		G1 tmpItem = aItemL.get(0);

		double viewAngle = refRenderer.getCameraViewAngle();
		double[] focalPoint = refManager.getCenter(tmpItem).toArray();
		double[] normal = refManager.getNormal(tmpItem).toArray();
		vtkAbstractComponent renWin = refRenderer.getRenderWindowPanel();

		double distanceToStructure = 0.0;
		if (preserveCurrentDistance)
		{
			vtkCamera activeCamera = renWin.getRenderer().GetActiveCamera();
			double[] pos = activeCamera.GetPosition();
			double[] closestPoint = refSmallBody.findClosestPoint(pos);
			distanceToStructure = MathUtil.distanceBetween(pos, closestPoint);
		}
		else
		{
			double size = refManager.getDiameter(tmpItem);
			distanceToStructure = size / Math.tan(Math.toRadians(viewAngle) / 2.0);
		}

		double[] newPos = { focalPoint[0] + distanceToStructure * normal[0],
				focalPoint[1] + distanceToStructure * normal[1], focalPoint[2] + distanceToStructure * normal[2] };

		// compute up vector
		double[] dir = { focalPoint[0] - newPos[0], focalPoint[1] - newPos[1], focalPoint[2] - newPos[2] };
		MathUtil.vhat(dir, dir);
		double[] zAxis = { 0.0, 0.0, 1.0 };
		double[] upVector = new double[3];
		MathUtil.vcrss(dir, zAxis, upVector);

		if (upVector[0] != 0.0 || upVector[1] != 0.0 || upVector[2] != 0.0)
			MathUtil.vcrss(upVector, dir, upVector);
		else
			upVector = new double[] { 1.0, 0.0, 0.0 };

		refRenderer.setCameraOrientation(newPos, focalPoint, upVector, viewAngle);
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enable the item if the number of selected items == 1
		boolean isEnabled = aItemC.size() == 1;
		aAssocMI.setEnabled(isEnabled);
	}

}
