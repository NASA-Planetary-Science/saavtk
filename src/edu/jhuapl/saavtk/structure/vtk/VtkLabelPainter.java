package edu.jhuapl.saavtk.structure.vtk;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.util.ControlPointUtil;
import edu.jhuapl.saavtk.vtk.VtkResource;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import vtk.vtkCaptionActor2D;
import vtk.vtkProp;

/**
 * Object which defines the logic to render a single caption (structure's label) using the VTK framework.
 * <p>
 * This class supports the following:
 * <ul>
 * <li>Update / Refresh mechanism
 * <li>VTK state management
 * <li>Retrieval of VTK caption
 * <li>Font color
 * <li>TODO: Font face
 * <li>TODO: Font style
 * <li>Font size
 * </ul>
 *
 * @author lopeznr1
 */
public class VtkLabelPainter<G1 extends Structure> implements VtkResource
{
	// Reference vars
	private final PolyhedralModel refSmallBody;
	private final G1 refItem;

	// VTK vars
	private vtkCaptionActor2D vLabelCA;
	private boolean vIsStale;

	// Cache vars
	private Object cCentroidBasis;
	private Vector3D cCentroid;

	// State vars
	private double opacity;

	/**
	 * Standard Constructor
	 */
	public VtkLabelPainter(PolyhedralModel aSmallBody, G1 aItem)
	{
		refSmallBody = aSmallBody;
		refItem = aItem;

		vLabelCA = null;
		vIsStale = true;

		cCentroidBasis = null;
		cCentroid = Vector3D.ZERO;

		opacity = 1.0;
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

	/**
	 * Sets the opacity of this painter.
	 */
	public void setOpacity(double aOpacity)
	{
		opacity = aOpacity;

		if (vLabelCA != null)
			vLabelCA.GetCaptionTextProperty().SetOpacity(opacity);
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

		// Refresh the centroid (if necessary)
		refreshCetroid();

		// Bail if not stale
		if (vIsStale == false)
			return;
		vIsStale = false;

		// Lazy init VTK actor
		if (vLabelCA == null)
		{
			vLabelCA = VtkUtil.formCaption(refSmallBody, cCentroid, refItem.getName(), refItem.getLabel(),
					labelFA.getSize());
			vLabelCA.GetCaptionTextProperty().SetJustificationToLeft();
			vLabelCA.GetCaptionTextProperty().SetOpacity(opacity);
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
	 * Helper method that refreshes the refItem's centroid (if necessary).
	 * <p>
	 * If a refresh is necessary then, the state var vIsStale will be set to true.
	 */
	private void refreshCetroid()
	{
		if (refItem instanceof Ellipse aEllipse)
		{
			var tmpCenter = aEllipse.getCenter();
			if (tmpCenter.equals(cCentroidBasis) == false)
			{
				cCentroidBasis = tmpCenter;
				cCentroid = refSmallBody.findClosestPoint(tmpCenter);
				vIsStale = true;
			}
		}
		else if (refItem instanceof PolyLine aPolyLine)
		{
			var controlPointL = aPolyLine.getControlPoints();
			if (controlPointL.equals(cCentroidBasis) == false)
			{
				cCentroidBasis = controlPointL;
				cCentroid = ControlPointUtil.calcCentroidOnBody(refSmallBody, controlPointL);
				vIsStale = true;
			}
		}
		else
		{
			throw new RuntimeException("Unsupported structure: " + refItem.getClass());
		}
	}

	/**
	 * Utility helper method to set the font family on the specified {@link vtkCaptionActor2D}.
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
