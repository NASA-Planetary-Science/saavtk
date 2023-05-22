package edu.jhuapl.saavtk.structure;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.pick.HookUtil;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.gui.StructureGuiUtil;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.structure.vtk.VtkEllipseMultiPainter;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.item.ItemEventType;
import glum.task.Task;
import glum.util.ThreadUtil;
import vtk.vtkPolyData;
import vtk.vtkProp;

/**
 * Class that provides management logic for a collection of structures.
 * <p>
 * The following features are supported:
 * <ul>
 * <li>Event handling
 * <li>Management of collection of structures
 * <li>Support for structure selection
 * <li>Configuration of rendering properties
 * </ul>
 * Manager that defines a collection of methods to manage, handle notification, and customize display of a collection of
 * structures.
 *
 * @author lopeznr1
 */
public class EllipseManager extends SaavtkItemManager<Ellipse>
		implements StructureManager<Ellipse>, PickListener, VtkPropProvider
{
	// Reference vars
	private final StatusNotifier refStatusNotifier;
	private final PolyhedralModel refSmallBody;

	// State vars
	private final DecimalFormat decimalFormat;

	// VTK vars
	private final VtkEllipseMultiPainter vMultiPainter;

	/** Standard Constructor */
	public EllipseManager(SceneChangeNotifier aSceneChangeNotifier, StatusNotifier aStatusNotifier,
			PolyhedralModel aSmallBody, int aNumSides)
	{
		refStatusNotifier = aStatusNotifier;
		refSmallBody = aSmallBody;

		decimalFormat = new DecimalFormat("#.#####");

		vMultiPainter = new VtkEllipseMultiPainter(aSceneChangeNotifier, aSmallBody, aNumSides);
	}

	/**
	 * Adds the specified item to this manager.
	 */
	public void addItem(Ellipse aItem)
	{
		var fullL = new ArrayList<>(getAllItems());
		fullL.add(aItem);

		var pickL = ImmutableList.of(aItem);

		setAllItems(fullL);
		setSelectedItems(pickL);

		vMultiPainter.updatePolyData();
	}

	/**
	 * Returns the vtkPolyData associated with the exterior of the structure.
	 * <P>
	 * Do NOT mutate the returned VTK object - it is meant only for read access!
	 */
	public vtkPolyData getVtkExteriorPolyDataFor(Ellipse aItem)
	{
		var tmpPainter = vMultiPainter.getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();
		tmpPainter.vtkUpdateState();

		// Delegate
		return tmpPainter.getVtkExteriorPolyData();
	}

	/**
	 * Returns the vtkPolyData associated with the interior of the structure.
	 * <P>
	 * Do NOT mutate the returned VTK object - it is meant only for read access!
	 */
	public vtkPolyData getVtkInteriorPolyDataFor(Ellipse aItem)
	{
		var tmpPainter = vMultiPainter.getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();
		tmpPainter.vtkUpdateState();

		// Delegate
		return tmpPainter.getVtkInteriorPolyData();
	}

	/**
	 * Returns the {@link VtkEllipseMultiPainter} used to draw the items.
	 */
	public VtkEllipseMultiPainter getVtkMultiPainter()
	{
		return vMultiPainter;
	}

	@Override
	public Vector3D getCentroid(Ellipse aItem)
	{
		// Delegate
		return refSmallBody.findClosestPoint(aItem.getCenter());
	}

	@Override
	public List<vtkProp> getProps()
	{
		// Delegate
		return vMultiPainter.getProps();
	}

	@Override
	public void installItems(Task aTask, List<Ellipse> aItemL)
	{
		// Determine the set of new items
		var currS = new HashSet<>(getAllItems());
		var fullS = new LinkedHashSet<>(aItemL);
		var newS = Sets.difference(fullS, currS);

		// Init the VTK state of the new items
		int tmpCnt = 0;
		for (var aItem : newS)
		{
			// Bail if aTask is aborted
			if (aTask.isActive() == false)
			{
				ThreadUtil.invokeAndWaitOnAwt(() -> setAllItems(getAllItems()));
				return;
			}

			var tmpPainter = vMultiPainter.getOrCreateVtkPainterFor(aItem, refSmallBody);
			tmpPainter.vtkUpdateState();

			aTask.setProgress(tmpCnt, newS.size());
			tmpCnt++;
		}

		vMultiPainter.setWorkItems(aItemL);

		// Finish on the AWT
		ThreadUtil.invokeAndWaitOnAwt(() -> setAllItems(aItemL));
	}

	@Override
	public void notifyItemsMutated(Collection<Ellipse> aItemC)
	{
		if (aItemC.isEmpty() == true)
			return;

		for (var aItem : aItemC)
			markPainterStale(aItem);

		vMultiPainter.updatePolyData();
		updateStatus(refStatusNotifier, aItemC, true);
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public boolean supportsActivation()
	{
		return false;
	}

	@Override
	public void setOffset(double aOffset)
	{
		vMultiPainter.setOffset(aOffset);
		vMultiPainter.updatePolyData();

		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public double getOffset()
	{
		return vMultiPainter.getOffset();
	}

	@Override
	public double getLineWidth()
	{
		return vMultiPainter.getLineWidth();
	}

	@Override
	public void setLineWidth(double aWidth)
	{
		if (aWidth >= 1.0)
		{
			vMultiPainter.setLineWidth(aWidth);

			// Send out the appropriate notifications
			notifyListeners(this, ItemEventType.ItemsMutated);
		}
	}

	@Override
	public Vector3D getCenter(Ellipse aItem)
	{
		return aItem.getCenter();
	}

	@Override
	public double getDiameter(Ellipse aItem)
	{
		return 2.0 * aItem.getRadius();
	}

	@Override
	public Ellipse getItem(int aIdx)
	{
		var tmpL = getAllItems();
		if (aIdx >= tmpL.size())
			return null;

		return tmpL.get(aIdx);
	}

	@Override
	public Color getColor(Ellipse aItem)
	{
		return aItem.getColor();
	}

	@Override
	public boolean getIsVisible(Ellipse aItem)
	{
		return aItem.getVisible();
	}

	@Override
	public Vector3D getNormal(Ellipse aItem)
	{
		var center = getCenter(aItem);
		var ptArr = refSmallBody.getNormalAtPoint(center.toArray());
		return new Vector3D(ptArr);
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if this is not a primary action
		if (aMode != PickMode.ActivePri)
			return;

		// Bail if popup trigger
		if (PickUtil.isPopupTrigger(aEvent) == true)
			return;

		// Bail if no associated painter
		if (StructureGuiUtil.isAssociatedPickTarget(aPrimaryTarg, this) == false)
			return;

		// Retrieve the clicked item
		var cellId = aPrimaryTarg.getCellId();
		var prop = aPrimaryTarg.getActor();
		var tmpItem = vMultiPainter.getItemFromCellId(prop, cellId);

		// Update the selection
		HookUtil.updateSelection(this, aEvent, tmpItem);

		var source = aEvent.getSource();
		notifyListeners(source, ItemEventType.ItemsSelected);
	}

	@Override
	public void removeItems(Collection<Ellipse> aItemC)
	{
		if (aItemC.isEmpty() == true)
			return;

		var fullL = new ArrayList<>(getAllItems());
		fullL.removeAll(aItemC);
		setAllItems(fullL);
	}

	@Override
	public void setAllItems(Collection<Ellipse> aItemC)
	{
		super.setAllItems(aItemC);

		// Update our painters
		vMultiPainter.setWorkItems(aItemC);
	}

	@Override
	public void setSelectedItems(Collection<Ellipse> aItemC)
	{
		// Keep track of the actual *changed* selection
		var origS = getSelectedItems();
		var targS = new HashSet<>(aItemC);
		var diffS = Sets.symmetricDifference(origS, targS);

		// Update our internal state
		super.setSelectedItems(aItemC);

		updateStatus(refStatusNotifier, aItemC, false);

		vMultiPainter.setPickedItems(aItemC);
		vMultiPainter.updateVtkColorsFor(diffS, true);
	}

	@Override
	public void setCenter(Ellipse aItem, Vector3D aCenter)
	{
		aItem.setCenter(aCenter);
		markPainterStale(aItem);

		vMultiPainter.updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setColor(Collection<Ellipse> aItemC, Color aColor)
	{
		for (var aItem : aItemC)
			aItem.setColor(aColor);

		vMultiPainter.updateVtkColorsFor(aItemC, true);
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setColor(Ellipse aItem, Color aColor)
	{
		// Delegate
		setColor(ImmutableList.of(aItem), aColor);
	}

	@Override
	public void setIsVisible(Collection<Ellipse> aItemC, boolean aBool)
	{
		for (var aItem : aItemC)
			aItem.setVisible(aBool);

		vMultiPainter.updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabel(Ellipse aItem, String aLabel)
	{
		aItem.setLabel(aLabel);

		// Update the appropriate painter
		var tmpPainter = vMultiPainter.getVtkTextPainter(aItem);
		if (tmpPainter != null)
			tmpPainter.markStale();

		vMultiPainter.updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelColor(Collection<Ellipse> aItemC, Color aColor)
	{
		for (var aItem : aItemC)
		{
			var tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), aColor, tmpFA.getSize(), tmpFA.getIsVisible());
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			var tmpPainter = vMultiPainter.getVtkTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.markStale();
		}

		vMultiPainter.updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelFontSize(Collection<Ellipse> aItemC, int aFontSize)
	{
		// Update the font size
		for (var aItem : aItemC)
		{
			var tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), aFontSize, tmpFA.getIsVisible());
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			var tmpPainter = vMultiPainter.getVtkTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.markStale();
		}

		vMultiPainter.updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelFontFamily(Collection<Ellipse> aItemC, String aFontFamily)
	{
		// Update the font size
		for (var aItem : aItemC)
		{
			var tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(aFontFamily, tmpFA.getColor(), tmpFA.getSize(), tmpFA.getIsVisible());
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			var tmpPainter = vMultiPainter.getVtkTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.markStale();
		}

		vMultiPainter.updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelVisible(Collection<Ellipse> aItemC, boolean aBool)
	{
		for (var aItem : aItemC)
		{
			var tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), tmpFA.getSize(), aBool);
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			var tmpPainter = vMultiPainter.getVtkTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.markStale();
		}

		vMultiPainter.updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	/**
	 * Helper method to mark the painter(s) associated with the specified item as stale.
	 */
	protected void markPainterStale(Ellipse aItem)
	{
		var tmpPainter = vMultiPainter.getVtkCompPainter(aItem);
		if (tmpPainter == null)
			return;

		tmpPainter.getMainPainter().markStale();
		tmpPainter.getTextPainter().markStale();
	}

	/**
	 * Helper method that updates the {@link StatusNotifier} with the specified items.
	 *
	 * @param aStatusNotifier
	 *    The {@link StatusNotifier} where updates will be posted.
	 * @param aItemC
	 *    The collection of items of interest.
	 * @param aIsUpdate
	 *    If set to true then the update is due to items being mutated rather than selected.
	 */
	protected void updateStatus(StatusNotifier aStatusNotifier, Collection<Ellipse> aItemC, boolean aIsUpdate)
	{
		String tmpMsg = null;
		if (aItemC.size() == 1)
			tmpMsg = StructureMiscUtil.getStatusText(aItemC.iterator().next(), decimalFormat);
		else if (aItemC.size() > 1)
		{
			if (aIsUpdate == true)
				tmpMsg = "Multiple structures mutated: " + aItemC.size();
			else
				tmpMsg = "Multiple structures selected: " + aItemC.size();
		}

		refStatusNotifier.setPriStatus(tmpMsg, null);
	}

}
