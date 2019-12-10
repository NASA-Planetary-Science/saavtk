package edu.jhuapl.saavtk.structure;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.model.FacetColoringData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.model.structure.OccludingCaptionActor;
import edu.jhuapl.saavtk.pick.DefaultPicker;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.util.ProgressListener;
import edu.jhuapl.saavtk.util.Properties;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import vtk.vtkCaptionActor2D;
import vtk.vtkProp;

/**
 * Class that provides management logic for a collection of {@link Structure}s.
 * <P>
 * The following features are supported:
 * <UL>
 * <LI>Event handling via {@link ItemEventListener} mechanism
 * <LI>Management of items
 * <LI>Support for item selection
 * <LI>Configuration of various rendering properties
 * <LI>Support for editing structures via an activation mechanism.
 * </UL>
 * <P>
 * TODO: Consider decoupling the following functionality from this class:
 * <UL>
 * <LI>All I/O logic
 * <LI>VTK specific logic
 * </UL>
 * Currently (VTK) rendering of {@link Structure}s is supported, however that
 * capability may eventually be removed and placed in a separate class/module.
 */
public abstract class StructureManager<G1 extends Structure> extends SaavtkItemManager<G1> implements PickListener
{
	// State vars
	private int defFontSize = 16;

	@Override
	public abstract List<vtkProp> getProps();

	public abstract G1 addNewStructure();

	public abstract boolean supportsActivation();

	public abstract void activateStructure(G1 aItem);

	public abstract G1 getActivatedStructure();

	public abstract void removeStructures(Collection<G1> aItemC);

	public abstract void removeAllStructures();

	/**
	 * Returns the structure at the specified index.
	 * <P>
	 * Returns null if there are no items in the manager.
	 * <P>
	 */
	public G1 getStructure(int aIdx)
	{
		List<G1> tmpL = getAllItems();
		if (aIdx >= tmpL.size())
			return null;

		return tmpL.get(aIdx);
	}

	public abstract G1 getStructureFromCellId(int aCellId, vtkProp aProp);

	public abstract void loadModel(File file, boolean append, ProgressListener listener) throws Exception;

	public abstract void saveModel(File file) throws Exception;

	/**
	 * TODO: Add better comments.
	 * <P>
	 * Sets the label associated with the specified structure.
	 */
	public void setStructureLabel(G1 aItem, String aLabel)
	{
		aItem.setLabel(aLabel);

		// Clear the caption if the string is empty or null
		boolean tmpBool = aLabel != null && aLabel.equals("") == false;
		aItem.setLabelVisible(tmpBool);
		updateStructure(aItem);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	public abstract double getLineWidth();

	public abstract void setLineWidth(double width);

	/**
	 * TODO: Add comments. Not sure of purpose
	 */
	public void showBorders()
	{
		for (G1 aItem : getSelectedItems())
		{
			vtkCaptionActor2D v = updateStructure(aItem);
			if (v != null)
				v.SetBorder(1 - v.GetBorder());
		}

		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Returns the color of the label associated with the specified structure.
	 *
	 * @param aItem The structure of interest.
	 */
	public Color getLabelColor(G1 aItem)
	{
		return aItem.getLabelColor();
	}

	/**
	 * Sets the color of the labels associated with the specified structures.
	 *
	 * @param aItemC The collection of structures to change.
	 * @param aColor The color that the labels will be changed to.
	 */
	public void setLabelColor(Collection<G1> aItemC, Color aColor)
	{
		for (G1 aItem : aItemC)
		{
			aItem.setLabelColor(aColor);
			updateStructure(aItem);

		}

		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Returns the font size of the label associated with the specified structures.
	 */
	public int getLabelFontSize(G1 aItem)
	{
		return aItem.getLabelFontSize();
	}

	/**
	 * Sets the font size of the labels associated with the specified structures.
	 *
	 * @param aItemC    The collection of structures to change.
	 * @param aFontSize
	 */
	public void setLabelFontSize(Collection<G1> aItemC, int aFontSize)
	{
		// Change the default to reflect the latest setting
		defFontSize = aFontSize;

		// Update the font size
		for (G1 aItem : aItemC)
		{
			aItem.setLabelFontSize(aFontSize);
			updateStructure(aItem);
		}

		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Sets the font family of the label associated with the specified structures.
	 *
	 * @param aItemC      The collection of structures to change.
	 * @param aFontFamily The font family to switch to. Currently the only supported
	 *                    families are: [Times, Arial, Courier]. A
	 *                    {@link RuntimeException} will be thrown if not supported.
	 */
	public void setLabelFontType(Collection<G1> aItemC, String aFontFamily)
	{
		// Update the font size
		for (G1 aItem : aItemC)
		{
			vtkCaptionActor2D tmpCaption = getCaption(aItem);
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

		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Returns true if the label associated with the specified structure is visible.
	 */
	public boolean isLabelVisible(G1 aItem)
	{
		return aItem.getLabelVisible();
	}

	/**
	 * Sets the visibility of the labels associated with the specified structures.
	 *
	 * @param aItemC The collection of structures to change.
	 * @param aBool  Flag which defines whether to show the labels or not.
	 */
	public void setLabelVisible(Collection<G1> aItemC, boolean aBool)
	{
		for (G1 aItem : aItemC)
		{
			aItem.setLabelVisible(aBool);
			updateStructure(aItem);
		}

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	/**
	 * Returns the color of the specified structure.
	 */
	public Color getStructureColor(G1 aItem)
	{
		return aItem.getColor();
	}

	/**
	 * Sets the color of the specified structures.
	 *
	 * @param aItemC The collection of structures to change.
	 * @param aColor The color that the structures will be changed to.
	 */
	public void setStructureColor(Collection<G1> aItemC, Color aColor)
	{
		for (G1 aItem : aItemC)
		{
			aItem.setColor(aColor);
			pcs.firePropertyChange(Properties.COLOR_CHANGED, null, aItem);
		}

		// Update VTK state associated with colors
		updateVtkColorsFor(aItemC, true);

		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	/**
	 * Sets the color of the specified structure.
	 */
	public void setStructureColor(G1 aItem, Color aColor)
	{
		// Delegate
		setStructureColor(ImmutableList.of(aItem), aColor);
	}

	/**
	 * Returns true if the the specified structure is visible.
	 */
	public boolean isStructureVisible(G1 aItem)
	{
		return aItem.getVisible();
	}

	/**
	 * Sets the visibility of the specified structures.
	 *
	 * @param aItemC The collection of structures to change.
	 * @param aBool  Flag which defines whether to show the labels or not.
	 */
	public void setStructureVisible(Collection<G1> aItemC, boolean aBool)
	{
		for (G1 aItem : aItemC)
			aItem.setVisible(aBool);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if we are not the target model
		if (aPrimaryTarg.getModel() != this)
			return;

		// Respond only to active events
		if (aMode != PickMode.Active)
			return;

		// Determine if this is a modified action
		boolean isModifyKey = PickUtil.isModifyKey(aEvent);

		// Retrieve the clicked item
		int cellId = aPrimaryTarg.getCellId();
		vtkProp prop = aPrimaryTarg.getActor();
		G1 tmpItem = getStructureFromCellId(cellId, prop);

		// Determine the items that will be marked as selected
		List<G1> tmpL = new ArrayList<>(getSelectedItems());
		if (isModifyKey == false)
			tmpL = ImmutableList.of(tmpItem);
		else if (getSelectedItems().contains(tmpItem) == false)
			tmpL.add(tmpItem);
		else
			tmpL.remove(tmpItem);

		// Update the selected items
		setSelectedItems(tmpL);

		Object source = aEvent.getSource();
		notifyListeners(source, ItemEventType.ItemsSelected);
	}

	public void savePlateDataInsideStructure(G1 aItem, File aFile) throws IOException
	{
		// do nothing by default. Structures that have an inside need to implement this.
	}

	public void savePlateDataInsideStructure(Collection<G1> aItemC, File aFile) throws IOException
	{
		// do nothing by default. Structures that have an inside need to implement this.
	}

	public FacetColoringData[] getPlateDataInsideStructure(G1 aItem)
	{
		// do nothing by default. Structures that have an inside need to implement this.
		return null;
	}

	public FacetColoringData[] getPlateDataInsideStructure(Collection<G1> aItemC)
	{
		// do nothing by default. Structures that have an inside need to implement this.
		return null;
	}

	// For polygons which take a long time to draw, implement this function
	// to only show interior when explicitly told. If not reimplemented, then
	// interiod is always shown.
	public void setShowStructuresInterior(Collection<G1> aItemC, boolean aShow)
	{
		// by default do nothing
	}

	public boolean isShowStructureInterior(G1 aItem)
	{
		return false;
	}

	/**
	 * Get the center of the structure. For ellipses and points, this is obvious.
	 * For paths and polygons, this is the mean of the control points.
	 */
	public abstract double[] getStructureCenter(G1 aItem);

	/**
	 * Get a measure of the size of the structure. For ellipses and points, this is
	 * the diameter. For paths and polygons, this is twice the distance from the
	 * centroid to the farthers point from the centroid.
	 */
	public abstract double getStructureSize(G1 aItem);

	public abstract double[] getStructureNormal(G1 aItem);

	public abstract PolyhedralModel getPolyhedralModel();

	@Override
	public void setSelectedItems(List<G1> aItemL)
	{
		// Keep track of the actual *changed* selection
		Set<G1> origS = getSelectedItems();
		Set<G1> targS = new HashSet<>(aItemL);
		Set<G1> diffS = Sets.symmetricDifference(origS, targS);

		// Update our internal state
		super.setSelectedItems(aItemL);

		updateVtkColorsFor(diffS, true);

		notifyVtkStateChange();
	}

	@Override
	public void setVisible(boolean aBool)
	{
		boolean needToUpdate = false;
		for (G1 aItem : getAllItems())
		{
			// Skip to next if nothing has changed
			if (aItem.getVisible() == aBool)
				continue;

			aItem.setVisible(aBool);
			updateStructure(aItem);
			needToUpdate = true;
		}

		if (needToUpdate)
		{
			updatePolyData();
			notifyListeners(this, ItemEventType.ItemsMutated);
		}
	}

	/**
	 * TODO: We should be registered with the "DefaultPicker" through different
	 * means and not rely on unrelated third party registration...
	 */
	public void registerDefaultPickerHandler(DefaultPicker aDefaultPicker)
	{
		aDefaultPicker.addListener(this);
	}

	/**
	 * Helper method that will cause the relevant VTK state to be rebuilt.
	 */
	protected abstract void updatePolyData();

	/**
	 * Helper method that will update the VTK coloring state for the specified
	 * structures.
	 *
	 * @param aItemC The structures of interest.
	 */
	protected abstract void updateVtkColorsFor(Collection<G1> aItemC, boolean aSendNotification);

	protected vtkCaptionActor2D updateStructure(G1 aItem)
	{
		if (aItem.getVisible() == false || aItem.getLabelVisible() == false)
		{
			if (aItem.getCaption() != null)
			{
				aItem.getCaption().VisibilityOff();
				aItem.setCaption(null);
			}
		}
		else
		{
			double[] center = aItem.getCentroid(getPolyhedralModel());
			if (center != null)
			{
				vtkCaptionActor2D caption = aItem.getCaption();

				if (caption == null)
				{
					caption = formCaption(getPolyhedralModel(), center, aItem.getName(), aItem.getLabel());
					caption.GetCaptionTextProperty().SetJustificationToLeft();
					aItem.setCaption(caption);
				}
				else
				{
					caption.SetCaption(aItem.getLabel());
					caption.SetAttachmentPoint(center);
				}

				Color labelColor = aItem.getLabelColor();
				caption.GetCaptionTextProperty().SetColor(labelColor.getRed() / 255., labelColor.getGreen() / 255.,
						labelColor.getBlue() / 255.);
				caption.GetCaptionTextProperty().SetFontSize(aItem.getLabelFontSize());
				caption.VisibilityOn();
			}
		}

		return aItem.getCaption();
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
	protected vtkCaptionActor2D getCaption(G1 aItem)
	{
		return updateStructure(aItem);
	}

	/**
	 * Helper method to create a VTK caption.
	 *
	 * @param aSmallBodyModel
	 * @param aCenterPoint    The point where the caption will be placed.
	 * @param aName           A string value used to reference this caption.
	 * @param aLabel          The text that will be shown in the caption.
	 */
	private vtkCaptionActor2D formCaption(PolyhedralModel aSmallBodyModel, double[] aCenterPoint, String aName,
			String aLabel)
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

	/**
	 * Helper method that notifies the relevant system that our internal VTK state
	 * has been changed.
	 * <P>
	 * This is currently accomplished via firing off a
	 * {@link Properties#MODEL_CHANGED} event.
	 */
	private void notifyVtkStateChange()
	{
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

}
