package edu.jhuapl.saavtk.model;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.saavtk.model.structure.OccludingCaptionActor;
import edu.jhuapl.saavtk.util.ProgressListener;
import edu.jhuapl.saavtk.util.Properties;
import vtk.vtkCaptionActor2D;
import vtk.vtkProp;

/**
 * Model of structures drawn on a body such as lines and circles.
 */
public abstract class StructureModel extends AbstractModel
{
	// State vars
	private int defFontSize = 16;

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

		public abstract double[] getCentroid(PolyhedralModel smallBodyModel);

		public abstract vtkCaptionActor2D getCaption();

		public abstract void setCaption(vtkCaptionActor2D caption);
	}

	@Override
	public abstract List<vtkProp> getProps();

	public abstract Structure addNewStructure();

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

	public abstract void loadModel(File file, boolean append, ProgressListener listener) throws Exception;

	public abstract void saveModel(File file) throws Exception;

	public abstract void setStructureColor(int idx, int[] color);

	public abstract void setStructureLabel(int idx, String label);

	public abstract double getLineWidth();

	public abstract void setLineWidth(double width);

	public abstract void showBorders();

	/**
	 * Returns the color of the label associated with the (sub)structure at the
	 * specified index.
	 * 
	 * @param aIdx The index associated with the (sub)structure of interest.
	 */
	public Color getLabelColor(int aIdx)
	{
		vtkCaptionActor2D tmpCaption = getCaption(aIdx);
		if (tmpCaption == null)
			return null;

		double[] rgbArr = tmpCaption.GetCaptionTextProperty().GetColor();
		return new Color((float) rgbArr[0], (float) rgbArr[1], (float) rgbArr[2]);
	}

	/**
	 * Sets the color of the labels associated with the (sub)structure at the
	 * specified indexes.
	 * 
	 * @param aIdxArr An array which holds the indexes corresponding to the
	 *            (sub)structures to change
	 * @param aColor The color that the labels will be changed to.
	 */
	public void setLabelColor(int[] aIdxArr, Color aColor)
	{
		double[] rgbArr = { aColor.getRed() / 255.0, aColor.getGreen() / 255.0, aColor.getBlue() / 255.0 };
		for (int aIdx : aIdxArr)
		{
			vtkCaptionActor2D tmpCaption = getCaption(aIdx);
			if (tmpCaption == null)
				continue;

			tmpCaption.GetCaptionTextProperty().SetColor(rgbArr);
		}

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Returns the font size of the label associated with the (sub)structures at the
	 * specified index.
	 */
	public int getLabelFontSize(int aIndex)
	{
		vtkCaptionActor2D tmpCaption = getCaption(aIndex);
		if (tmpCaption == null)
			return defFontSize;

		return tmpCaption.GetCaptionTextProperty().GetFontSize();
	}

	/**
	 * Sets the font size of the labels associated with the (sub)structure at the
	 * specified indexes.
	 * 
	 * @param aIdxArr An array which holds the indexes corresponding to the
	 *            (sub)structures to change
	 * @param aFontSize
	 */
	public void setLabelFontSize(int[] aIdxArr, int aFontSize)
	{
		// Change the default to reflect the latest setting
		defFontSize = aFontSize;

		// Update the font size
		for (int aIdx : aIdxArr)
		{
			vtkCaptionActor2D tmpCaption = getCaption(aIdx);
			if (tmpCaption == null)
				continue;
			tmpCaption.GetCaptionTextProperty().SetFontSize(aFontSize);
		}

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Sets the font family of the label associated with the (sub)structure at the
	 * specified index.
	 * 
	 * @param aIdxArr An array which holds the indexes corresponding to the
	 *            (sub)structures to change
	 * @param aFontFamily The font family to switch to. Currently the only supported
	 *            families are: [Times, Arial, Courier]. A {@link RuntimeException}
	 *            will be thrown if not supported.
	 */
	public void setLabelFontType(int[] aIdxArr, String aFontFamily)
	{
		// Update the font size
		for (int aIdx : aIdxArr)
		{
			vtkCaptionActor2D tmpCaption = getCaption(aIdx);
			if (tmpCaption == null)
				continue;

			aFontFamily = aFontFamily.toUpperCase();
			if (aFontFamily.equals("TIMES") == true)
				tmpCaption.GetCaptionTextProperty().SetFontFamilyToTimes();
			else if (aFontFamily.equals("ARIAL") == true)
				tmpCaption.GetCaptionTextProperty().SetFontFamilyToArial();
			else if (aFontFamily.equals("COURIER") == true)
				tmpCaption.GetCaptionTextProperty().SetFontFamilyToCourier();
			else
				throw new RuntimeException("FontFamily is not supported. Input: " + aFontFamily);
		}

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Returns true if the label associated with the (sub)structure at the specified
	 * index is visible.
	 * 
	 * @param aIdx The index associated with the (sub)structure of interest.
	 */
	public abstract boolean isLabelVisible(int aIdx);

	/**
	 * Sets the visibility of the labels associated with the (sub)structures at the
	 * specified indexes.
	 * 
	 * @param aIdxArr An array which holds the indexes corresponding to the
	 *            (sub)structures to change
	 * @param aIsVisible Flag which defines whether to show the labels or not.
	 */
	public abstract void setLabelVisible(int[] aIdxArr, boolean aIsVisible);

	/**
	 * Returns the color of the (sub)structure at the specified index.
	 */
	public abstract Color getStructureColor(int aIdx);

	/**
	 * Sets the color of the (sub)structures at the specified indexes.
	 * 
	 * @param aIdxArr An array which holds the indexes corresponding to the
	 *            (sub)structures to change
	 * @param aColor The color that the (sub)structures will be changed to.
	 */
	public abstract void setStructureColor(int[] aIdxArr, Color aColor);

	/**
	 * Returns true if the the (sub)structure at the specified index is visible.
	 * 
	 * @param aIdx The index associated with the (sub)structure of interest.
	 */
	public abstract boolean isStructureVisible(int aIdx);

	/**
	 * Sets the visibility of the (sub)structures associated with the specified
	 * indexes.
	 * 
	 * @param aIdxArr An array which holds the indexes corresponding to the
	 *            (sub)structures to change
	 * @param aIsVisible Flag which defines whether to show the labels or not.
	 */
	public abstract void setStructureVisible(int[] aIdxArr, boolean aIsVisible);

	public void savePlateDataInsideStructure(int idx, File file) throws IOException
	{
		// do nothing by default. Only structures that have an inside need to implement this.
	}

	public FacetColoringData[] getPlateDataInsideStructure(int idx)
	{
		// do nothing by default. Only structures that have an inside need to implement this.
		return null;
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

	public abstract PolyhedralModel getPolyhedralModel();

	protected vtkCaptionActor2D updateStructure(Structure structure)
	{
		if (structure.getHidden() || structure.getLabelHidden())
		{
			if (structure.getCaption() != null)
			{
				structure.getCaption().VisibilityOff();
				structure.setCaption(null);
			}
		}
		else
		{
			double[] center = structure.getCentroid(getPolyhedralModel());
			if (center != null)
			{
				vtkCaptionActor2D caption = structure.getCaption();

				if (caption == null)
				{
					caption = formCaption(getPolyhedralModel(), center, structure.getName(), structure.getLabel());
					caption.GetCaptionTextProperty().SetJustificationToLeft();
					structure.setCaption(caption);
				}
				else
				{
					caption.SetCaption(structure.getLabel());
					caption.SetAttachmentPoint(center);
				}

				caption.VisibilityOn();
			}
		}

		return structure.getCaption();
	}

	/**
	 * Returns the caption associated with the specified index.
	 * <P>
	 * This method is protected so that VTK specific classes will remain as an
	 * implementation detail.
	 * <P>
	 * May return null.
	 * <P>
	 * TODO: Should we allow nulls - or just have empty non-rendered captions?
	 */
	protected abstract vtkCaptionActor2D getCaption(int aIndex);

	/**
	 * Helper method to create a VTK caption.
	 * 
	 * @param aSmallBodyModel
	 * @param aCenterPoint The point where the caption will be placed.
	 * @param aName A string value used to reference this caption.
	 * @param aLabel The text that will be shown in the caption.
	 */
	protected vtkCaptionActor2D formCaption(PolyhedralModel aSmallBodyModel, double[] aCenterPoint, String aName, String aLabel)
	{
		vtkCaptionActor2D retCaption;

		retCaption = new OccludingCaptionActor(aCenterPoint, aName, aSmallBodyModel);
		retCaption.GetCaptionTextProperty().SetColor(1.0, 1.0, 1.0);
		retCaption.GetCaptionTextProperty().SetJustificationToCentered();
		retCaption.GetCaptionTextProperty().BoldOn();

		retCaption.GetCaptionTextProperty().SetFontSize(defFontSize);
		retCaption.GetTextActor().SetTextScaleModeToNone();

		retCaption.VisibilityOn();
		retCaption.BorderOff();
		retCaption.LeaderOff();
		retCaption.SetAttachmentPoint(aCenterPoint);
		retCaption.SetCaption(aLabel);

		return retCaption;
	}

}
