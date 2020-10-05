package edu.jhuapl.saavtk.structure;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.pick.HookUtil;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.structure.gui.StructureGuiUtil;
import edu.jhuapl.saavtk.structure.vtk.VtkCompositePainter;
import edu.jhuapl.saavtk.structure.vtk.VtkLabelPainter;
import edu.jhuapl.saavtk.vtk.VtkResource;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.item.ItemEventType;
import glum.task.Task;
import glum.util.ThreadUtil;
import vtk.vtkProp;

/**
 * Base implementation of the StructureManager interface.
 * <P>
 * This class provides the following functionality:
 * <UL>
 * <LI>Management of items
 * <LI>Logic to handle item selection
 * <LI>Configuration of various rendering properties
 * <LI>Base VTK render logic
 * </UL>
 *
 * @author lopeznr1
 */
public abstract class BaseStructureManager<G1 extends Structure, G2 extends VtkResource> extends SaavtkItemManager<G1>
		implements StructureManager<G1>, PickListener
{
	// Ref vars
	private PolyhedralModel refSmallBody;

	// VTK vars
	private final Map<G1, VtkCompositePainter<G1, G2>> vPainterM;

	/**
	 * Standard Constructor
	 */
	public BaseStructureManager(PolyhedralModel aSmallBody)
	{
		refSmallBody = aSmallBody;

		vPainterM = new HashMap<>();
	}

	/**
	 * Method that returns the structure that was picked given the specified cell id
	 * and vtkProp.
	 */
	public abstract G1 getItemFromCellId(int aCellId, vtkProp aProp);

	/**
	 * Helper method that instantiates a painter suitable for the specified item.
	 */
	protected abstract G2 createPainter(G1 aItem);

	/**
	 * Helper method that will cause the relevant VTK state to be updated.
	 */
	protected abstract void updatePolyData();

	/**
	 * Helper method that will update the VTK coloring state for the specified
	 * structures.
	 *
	 * @param aItemC The structures of interest.
	 */
	protected abstract void updateVtkColorsFor(Collection<G1> aItemC, boolean aSendNotification);

	/**
	 * Removes all of the items from this manager.
	 */
	public void removeAllStructures()
	{
		// Delegate
		removeItems(getAllItems());
	}

	@Override
	public G1 getItem(int aIdx)
	{
		List<G1> tmpL = getAllItems();
		if (aIdx >= tmpL.size())
			return null;

		return tmpL.get(aIdx);
	}

	@Override
	public Color getColor(G1 aItem)
	{
		return aItem.getColor();
	}

	@Override
	public boolean getIsVisible(G1 aItem)
	{
		return aItem.getVisible();
	}

	@Override
	public Vector3D getNormal(G1 aItem)
	{
		Vector3D center = getCenter(aItem);
		double[] ptArr = refSmallBody.getNormalAtPoint(center.toArray());
		return new Vector3D(ptArr);
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if this is not a primary action
		if (aMode != PickMode.ActivePri)
			return;

		// Bail if we are are not associated with the PickTarget
		if (StructureGuiUtil.isAssociatedPickTarget(aPrimaryTarg, this) == false)
			return;

		// Retrieve the clicked item
		int cellId = aPrimaryTarg.getCellId();
		vtkProp prop = aPrimaryTarg.getActor();
		G1 tmpItem = getItemFromCellId(cellId, prop);

		// Update the selection
		HookUtil.updateSelection(this, aEvent, tmpItem);

		Object source = aEvent.getSource();
		notifyListeners(source, ItemEventType.ItemsSelected);
	}

	@Override
	public void installItems(Task aTask, List<G1> aItemL)
	{
		// Determine the set of new items
		Set<G1> currS = new HashSet<>(getAllItems());
		Set<G1> fullS = new LinkedHashSet<>(aItemL);
		Set<G1> newS = Sets.difference(fullS, currS);

		// Init the VTK state of the new items
		int tmpCnt = 0;
		for (G1 aItem : newS)
		{
			// Bail if aTask is aborted
			if (aTask.isActive() == false)
			{
				ThreadUtil.invokeAndWaitOnAwt(() -> setAllItems(getAllItems()));
				return;
			}

			VtkCompositePainter<?, ?> tmpPainter = getOrCreateVtkPainterFor(aItem, refSmallBody);
			tmpPainter.vtkUpdateState();

			aTask.setProgress(tmpCnt, newS.size());
			tmpCnt++;
		}

		// Finish on the AWT
		ThreadUtil.invokeAndWaitOnAwt(() -> setAllItems(aItemL));
	}

	@Override
	public void removeItems(Collection<G1> aItemC)
	{
		if (aItemC.isEmpty() == true)
			return;

		List<G1> fullL = new ArrayList<>(getAllItems());
		fullL.removeAll(aItemC);
		setAllItems(fullL);
	}

	@Override
	public void setAllItems(Collection<G1> aItemC)
	{
		// Clear out unused painters in vPainterM
		VtkUtil.flushResourceMap(vPainterM, aItemC);

		super.setAllItems(aItemC);

		updatePolyData();
	}

	@Override
	public void setSelectedItems(Collection<G1> aItemC)
	{
		// Keep track of the actual *changed* selection
		Set<G1> origS = getSelectedItems();
		Set<G1> targS = new HashSet<>(aItemC);
		Set<G1> diffS = Sets.symmetricDifference(origS, targS);

		// Update our internal state
		super.setSelectedItems(aItemC);

		updateVtkColorsFor(diffS, true);
	}

	@Override
	public void setColor(Collection<G1> aItemC, Color aColor)
	{
		for (G1 aItem : aItemC)
			aItem.setColor(aColor);

		updateVtkColorsFor(aItemC, true);
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setColor(G1 aItem, Color aColor)
	{
		// Delegate
		setColor(ImmutableList.of(aItem), aColor);
	}

	@Override
	public void setIsVisible(Collection<G1> aItemC, boolean aBool)
	{
		for (G1 aItem : aItemC)
			aItem.setVisible(aBool);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabel(G1 aItem, String aLabel)
	{
		aItem.setLabel(aLabel);

		// Update the appropriate painter
		VtkLabelPainter<?> tmpPainter = getVtkTextPainter(aItem);
		if (tmpPainter != null)
			tmpPainter.markStale();

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelColor(Collection<G1> aItemC, Color aColor)
	{
		for (G1 aItem : aItemC)
		{
			FontAttr tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), aColor, tmpFA.getSize(), tmpFA.getIsVisible());
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			VtkLabelPainter<?> tmpPainter = getVtkTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.markStale();
		}

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelFontSize(Collection<G1> aItemC, int aFontSize)
	{
		// Update the font size
		for (G1 aItem : aItemC)
		{
			FontAttr tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), aFontSize, tmpFA.getIsVisible());
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			VtkLabelPainter<?> tmpPainter = getVtkTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.markStale();
		}

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelFontFamily(Collection<G1> aItemC, String aFontFamily)
	{
		// Update the font size
		for (G1 aItem : aItemC)
		{
			FontAttr tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(aFontFamily, tmpFA.getColor(), tmpFA.getSize(), tmpFA.getIsVisible());
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			VtkLabelPainter<?> tmpPainter = getVtkTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.markStale();
		}

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelVisible(Collection<G1> aItemC, boolean aBool)
	{
		for (G1 aItem : aItemC)
		{
			FontAttr tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), tmpFA.getSize(), aBool);
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			VtkLabelPainter<?> tmpPainter = getVtkTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.markStale();
		}

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
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
			needToUpdate = true;

			// Update the appropriate painter
			VtkLabelPainter<?> tmpPainter = getVtkTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.markStale();
		}

		if (needToUpdate)
		{
			updatePolyData();
			notifyListeners(this, ItemEventType.ItemsMutated);
		}
	}

	/**
	 * Helper method that returns the (composite) painter for the specified item.
	 * <P>
	 * A painter will be instantiated if necessary.
	 */
	protected VtkCompositePainter<G1, G2> getOrCreateVtkPainterFor(G1 aItem, PolyhedralModel aSmallBody)
	{
		VtkCompositePainter<G1, G2> retPainter = vPainterM.get(aItem);
		if (retPainter == null)
		{
			retPainter = new VtkCompositePainter<>(aSmallBody, this, aItem, createPainter(aItem));
			vPainterM.put(aItem, retPainter);
		}

		return retPainter;
	}

	/**
	 * Helper method that returns the (composite) painter for the specified item.
	 */
	protected VtkCompositePainter<G1, G2> getVtkCompPainter(G1 aItem)
	{
		return vPainterM.get(aItem);
	}

	/**
	 * Helper method that returns the primary painter for the specified item. This
	 * is the painter responsible for rendering a structure's shape.
	 */
	protected G2 getVtkMainPainter(G1 aItem)
	{
		VtkCompositePainter<?, G2> tmpPainter = vPainterM.get(aItem);
		if (tmpPainter == null)
			return null;

		return tmpPainter.getMainPainter();
	}

	/**
	 * Helper method that returns the {@link VtkLabelPainter} for the specified
	 * item. This is the painter responsible for the rendering of label.
	 */
	protected VtkLabelPainter<?> getVtkTextPainter(G1 aItem)
	{
		VtkCompositePainter<G1, G2> tmpPainter = vPainterM.get(aItem);
		if (tmpPainter == null)
			return null;

		return tmpPainter.getTextPainter();
	}

}
