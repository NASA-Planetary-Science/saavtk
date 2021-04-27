package edu.jhuapl.saavtk.vtk;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.OccludingCaptionActor;
import edu.jhuapl.saavtk.view.AssocActor;
import vtk.vtkActor;
import vtk.vtkCaptionActor2D;
import vtk.vtkCellArray;
import vtk.vtkObject;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;

/**
 * Collection of VTK based utility methods.
 *
 * @author lopeznr1
 */
public class VtkUtil
{
	/**
	 * Utility method that will release the VTK memory associated with all of the
	 * items in aItemC
	 */
	public static void deleteAll(Collection<? extends vtkObject> aItemC)
	{
		for (vtkObject aItem : aItemC)
			aItem.Delete();
	}

	/**
	 * Utility method that removes any VtkResource that does not correspond to an
	 * item in the provided list, aItemL. The {@link VtkResource} will be released
	 * via {@link VtkResource#vtkDispose()} before it is removed from the map,
	 * aResourceM.
	 * <P>
	 * Note the updated map will not have any new entries - rather stale entries
	 * will just be removed.
	 *
	 * @param aValidC    The list of valid items. The {@link VtkResource}s
	 *                   corresponding to the items in this list will not be
	 *                   disposed.
	 * @param aResourceM A mapping of items to corresponding {@link VtkResource}.
	 */
	public static <G1, G2 extends VtkResource> void flushResourceMap(Map<G1, G2> aResourceM, Collection<G1> aValidC)
	{
		// Copy the map to allow us to use it as a working map
		Map<G1, G2> oldM = new HashMap<>(aResourceM);

		// Reconstruct the provided map, resourceM
		aResourceM.clear();
		for (G1 aItem : aValidC)
		{
			// Skip to next if no item was previously installed
			G2 tmpResource = oldM.remove(aItem);
			if (tmpResource == null)
				continue;

			aResourceM.put(aItem, tmpResource);
		}

		// Manually dispose of the (remaining) old VtkResources
		for (G2 aResource : oldM.values())
			aResource.vtkDispose();
	}

	/**
	 * Utility method to create a VTK caption.
	 *
	 * @param aSmallBody
	 * @param aCenterPt  The point where the caption will be placed.
	 * @param aName      A string value used to reference this caption.
	 * @param aLabel     The text that will be shown in the caption.
	 */
	public static vtkCaptionActor2D formCaption(PolyhedralModel aSmallBody, Vector3D aCenterPt, String aName,
			String aLabel, int aFontSize)
	{
		double[] ptArr = aCenterPt.toArray();

		vtkCaptionActor2D retCaption = new OccludingCaptionActor(ptArr, aName, aSmallBody);
		retCaption.GetCaptionTextProperty().SetColor(1.0, 1.0, 1.0);
		retCaption.GetCaptionTextProperty().SetJustificationToCentered();
		retCaption.GetCaptionTextProperty().BoldOn();

		retCaption.GetCaptionTextProperty().SetFontSize(aFontSize);
		retCaption.GetTextActor().SetTextScaleModeToNone();

		retCaption.VisibilityOn();
		retCaption.BorderOff();
		retCaption.LeaderOff();
		retCaption.SetAttachmentPoint(ptArr);
		retCaption.SetCaption(aLabel);

		return retCaption;
	}

	/**
	 * Utility helper method that forms a vtkPolyData that is useful for resetting
	 * other vtkPolyData.
	 */
	public static vtkPolyData formEmptyPolyData()
	{
		vtkPolyData retPD = new vtkPolyData();
		vtkPoints vTmpP = new vtkPoints();
		vtkCellArray vTmpCA = new vtkCellArray();

		// Initialize an empty vtkPolyData for resetting
		retPD.SetPoints(vTmpP);
		retPD.SetLines(vTmpCA);
		retPD.SetVerts(vTmpCA);

		vtkUnsignedCharArray colorUCA = new vtkUnsignedCharArray();
		colorUCA.SetNumberOfComponents(4);
		retPD.GetCellData().SetScalars(colorUCA);

		return retPD;
	}

	/**
	 * Utility method that returns the model associated with the specified
	 * {@link vtkActor}.
	 * <P>
	 * This method will return the associated model if the provided actor implements
	 * the {@link AssocActor} interface.
	 */
	public static Object getAssocModel(Object aActor)
	{
		if (aActor instanceof AssocActor == false)
			return null;

		return ((AssocActor) aActor).getAssocModel(Object.class);
	}

	/**
	 * Utility method that takes the color and sets them into the specified
	 * {@link vtkUnsignedCharArray} at the specified index, aIdx.
	 * <P>
	 * Note only 3 values (r,g,b) are set at the specified index.
	 *
	 * @param aUCA
	 * @param aIdx   The index of interest.
	 * @param aColor
	 */
	public static void setColorOnUCA3(vtkUnsignedCharArray aUCA, int aIdx, Color aColor)
	{
		aUCA.SetTuple3(aIdx, aColor.getRed(), aColor.getGreen(), aColor.getBlue());
	}

	/**
	 * Utility method that takes the color and sets them into the specified
	 * {@link vtkUnsignedCharArray} at the specified index, aIdx.
	 * <P>
	 * Note 4 values (r,g,b,a) are set at the specified index.
	 *
	 * @param aUCA
	 * @param aIdx   The index of interest.
	 * @param aColor
	 */
	public static void setColorOnUCA4(vtkUnsignedCharArray aUCA, int aIdx, Color aColor)
	{
		aUCA.SetTuple4(aIdx, aColor.getRed(), aColor.getGreen(), aColor.getBlue(), aColor.getAlpha());
	}

}
