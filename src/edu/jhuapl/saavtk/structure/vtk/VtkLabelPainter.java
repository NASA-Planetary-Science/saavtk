package edu.jhuapl.saavtk.structure.vtk;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.FontAttr;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.vtk.VtkResource;
import vtk.vtkCaptionActor2D;
import vtk.vtkProp;

/**
 * Class which contains the logic to render a single caption (structure's label)
 * using the VTK framework.
 * <P>
 * This class supports the following:
 * <UL>
 * <LI>Update / Refresh mechanism
 * <LI>VTK state management
 * <LI>Retrieval of VTK caption
 * <LI>Font color
 * <LI>TODO: Font face
 * <LI>TODO: Font style
 * <LI>Font size
 * </UL>
 *
 * @author lopeznr1
 */
public class VtkLabelPainter<G1 extends Structure> implements VtkResource
{
	// Reference vars
	private final PolyhedralModel refSmallBody;
	private final StructureManager<G1> refManager;
	private final G1 refItem;

	// VTK vars
	private vtkCaptionActor2D vLabelCA;
	private boolean vIsStale;

	// Cache vars
	private Vector3D cCentroid;

	/**
	 * Standard Constructor
	 */
	public VtkLabelPainter(PolyhedralModel aSmallBody, StructureManager<G1> aManager, G1 aItem)
	{
		refSmallBody = aSmallBody;
		refManager = aManager;
		refItem = aItem;

		vLabelCA = null;
		vIsStale = true;

		cCentroid = Vector3D.ZERO;
	}

	/**
	 * Returns the associated VTK actor.
	 */
	public vtkProp getActor()
	{
		// No caption if both are not visible
		if (refItem.getVisible() == false || refItem.getLabelFontAttr().getIsVisible() == false)
			return null;

		return vLabelCA;
	}

	/**
	 * Notification that the VTK state is stale.
	 */
	public void markStale()
	{
		vIsStale = true;
	}

	@Override
	public void vtkDispose()
	{
		if (vLabelCA == null)
			return;

		vLabelCA.VisibilityOff();
		vLabelCA.Delete();
	}

	@Override
	public void vtkUpdateState()
	{
		// Bail if we are not visible
		FontAttr labelFA = refItem.getLabelFontAttr();
		if (refItem.getVisible() == false || labelFA.getIsVisible() == false)
			return;

		// Update the stale flag to account for changes in the item's centroid
		Vector3D tmpCentroid = refManager.getCentroid(refItem);
		vIsStale |= tmpCentroid.equals(cCentroid) == false;

		// Bail if the not stale
		if (vIsStale == false)
			return;
		vIsStale = false;

		// Update our cache vars
		cCentroid = tmpCentroid;

		// Lazy init VTK actor
		if (vLabelCA == null)
		{
			vLabelCA = VtkUtil.formCaption(refSmallBody, cCentroid, refItem.getName(), refItem.getLabel(),
					labelFA.getSize());
			vLabelCA.GetCaptionTextProperty().SetJustificationToLeft();
		}
		else
		{
			vLabelCA.SetCaption(refItem.getLabel());
			vLabelCA.SetAttachmentPoint(cCentroid.toArray());
		}

//		// Label: border
//		int showBorder = 0;
//		if (labelFA.getShowBorder() == true)
//			showBorder = 1;
//		vLabelCA.SetBorder(showBorder);

		// Label: color, size, font
		Color tmpColor = labelFA.getColor();
		vLabelCA.GetCaptionTextProperty().SetColor(tmpColor.getRed() / 255.0, tmpColor.getGreen() / 255.0,
				tmpColor.getBlue() / 255.0);
		vLabelCA.GetCaptionTextProperty().SetFontSize(labelFA.getSize());
		updateFontFamily(vLabelCA, labelFA.getFace());
		vLabelCA.VisibilityOn();
	}

	/**
	 * Utility helper method to set the font family on the specified
	 * {@link vtkCaptionActor2D}.
	 */
	private static void updateFontFamily(vtkCaptionActor2D aLabelCA, String aFontFamily)
	{
		// TODO: Eventually enable this functionality

//		aFontFamily = aFontFamily.toUpperCase();
//		if (aFontFamily.equals("TIMES") == true)
//			aLabelCA.GetCaptionTextProperty().SetFontFamilyToTimes();
//		else if (aFontFamily.equals("ARIAL") == true)
//			aLabelCA.GetCaptionTextProperty().SetFontFamilyToArial();
//		else if (aFontFamily.equals("COURIER") == true)
//			aLabelCA.GetCaptionTextProperty().SetFontFamilyToCourier();
//		else
//			throw new RuntimeException("FontFamily is not supported. Input: " + aFontFamily);
	}

}
