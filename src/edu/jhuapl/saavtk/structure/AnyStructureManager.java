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
import edu.jhuapl.saavtk.pick.HookUtil;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.structure.util.ControlPointUtil;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.structure.vtk.VtkLabelPainter;
import edu.jhuapl.saavtk.structure.vtk.VtkPolyLinePainter2;
import edu.jhuapl.saavtk.structure.vtk.VtkPolygonPainter;
import edu.jhuapl.saavtk.structure.vtk.VtkStructureMultiPainter;
import edu.jhuapl.saavtk.view.AssocActor;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.item.BaseItemManager;
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
public class AnyStructureManager extends BaseItemManager<Structure>
		implements StructureManager<Structure>, PickListener, VtkPropProvider
{
	// Reference vars
	private final StatusNotifier refStatusNotifier;
	private final PolyhedralModel refSmallBody;

	// State vars
	private RenderAttr renderAttr;
	private boolean allowPickAction;
	private final DecimalFormat decimalFormat;

	// VTK vars
	private final VtkStructureMultiPainter vPointVSMP;
	private final VtkStructureMultiPainter vRegularVSMP;

	/** Standard Constructor */
	public AnyStructureManager(SceneChangeNotifier aSceneChangeNotifier, StatusNotifier aStatusNotifier,
			PolyhedralModel aSmallBody)
	{
		refStatusNotifier = aStatusNotifier;
		refSmallBody = aSmallBody;

		var lineWidth = 1.0;
		var radialOffset = EllipseUtil.getRadialOffsetDef(aSmallBody);
		var pointRadius = EllipseUtil.getPointSizeDef(aSmallBody);
		renderAttr = new RenderAttr(lineWidth, radialOffset, 20, 4, pointRadius);
		allowPickAction = true;
		decimalFormat = new DecimalFormat("#.#####");

		vPointVSMP = new VtkStructureMultiPainter(aSceneChangeNotifier, aSmallBody, renderAttr);
		vPointVSMP.setInteriorOpacity(1.00);

		vRegularVSMP = new VtkStructureMultiPainter(aSceneChangeNotifier, aSmallBody, renderAttr);
		vRegularVSMP.setInteriorOpacity(0.15);
	}

	/**
	 * Adds the specified item to this manager.
	 */
	public void addItem(Structure aItem)
	{
		var fullL = new ArrayList<>(getAllItems());
		fullL.add(aItem);

		var pickL = ImmutableList.of(aItem);

		setAllItems(fullL);
		setSelectedItems(pickL);

		updatePolyData();
	}

	/**
	 * Returns all of the internal {@link VtkStructureMultiPainter}s.
	 */
	public ImmutableList<VtkStructureMultiPainter> getAllPainters()
	{
		return ImmutableList.of(vPointVSMP, vRegularVSMP);
	}

	/**
	 * Returns the vtkPolyData associated with the exterior of the structure.
	 * <p>
	 * Do NOT mutate the returned VTK object - it is meant only for read access!
	 */
	public vtkPolyData getVtkExteriorPolyDataFor(Structure aItem)
	{
		var multiPainter = getMultiPainterFor(aItem);
		var mainPainter = multiPainter.getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();
		mainPainter.vtkUpdateState();

		// Delegate
		return mainPainter.vtkGetExteriorRegPD();
	}

	/**
	 * Returns the {@link vtkPolyData} associated with the interior of the structure.
	 * <p>
	 * Do NOT mutate the returned VTK object - it is meant only for read access!
	 */
	public vtkPolyData getVtkInteriorPolyDataFor(Structure aItem)
	{
		var multiPainter = getMultiPainterFor(aItem);
		var mainPainter = multiPainter.getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();
		mainPainter.vtkUpdateState();

		// Delegate
		return mainPainter.vtkGetInteriorRegPD();
	}

	/**
	 * Returns true if the specified {@link PickTarget} is associated with this {@link AnyStructureManager}..
	 */
	public boolean isAssociatedPickTarget(PickTarget aPickTarget)
	{
		if (getMultiPainterFor(aPickTarget) == null)
			return false;

		return true;
	}

	/**
	 * Method that returns true if the manager is allowed to handle pick actions.
	 */
	public boolean getAllowPickAction()
	{
		return allowPickAction;
	}

	/**
	 * Method that sets if the manger is allowed to handle pick actions.
	 */
	public void setAllowPickAction(boolean aAllowPickAction)
	{
		allowPickAction = aAllowPickAction;
	}

	/**
	 * Sets this manager's {@link RenderAttr}.
	 */
	public void setRenderAttr(RenderAttr aRenderAttr)
	{
		renderAttr = aRenderAttr;

		// Update the child painters
		vRegularVSMP.setRenderAttr(aRenderAttr);
		vPointVSMP.setRenderAttr(aRenderAttr);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	/**
	 * Method that sets whether the list of {@link Structure}s should show their interior.
	 */
	public void setShowInterior(Collection<Structure> aItemC, boolean aIsShown)
	{
		for (var aItem : aItemC)
			configureInterior(aItem, aIsShown);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public Vector3D getCentroid(Structure aItem)
	{
		var centerPt = getCenter(aItem);
		return refSmallBody.findClosestPoint(centerPt);
	}

	@Override
	public List<vtkProp> getProps()
	{
		var retPropL = new ArrayList<vtkProp>();
		retPropL.addAll(vRegularVSMP.getProps());
		retPropL.addAll(vPointVSMP.getProps());
		return retPropL;
	}

	@Override
	public RenderAttr getRenderAttr()
	{
		return renderAttr;
	}

	@Override
	public void installItems(Task aTask, List<Structure> aItemC)
	{
		// Determine the set of new items
		var currS = new HashSet<>(getAllItems());
		var fullS = new LinkedHashSet<>(aItemC);
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

			var multiPainter = getMultiPainterFor(aItem);
			var compPainter = multiPainter.getOrCreateVtkPainterFor(aItem, refSmallBody);
			compPainter.vtkUpdateState();

			aTask.setProgress(tmpCnt, newS.size());
			tmpCnt++;
		}

		for (var aPainterVSMP : getAllPainters())
		{
			var partItemL = getItemsForPainter(aItemC, aPainterVSMP);
			aPainterVSMP.setWorkItems(partItemL);
		}

		// Finish on the AWT
		ThreadUtil.invokeAndWaitOnAwt(() -> setAllItems(aItemC));
	}

	@Override
	public void notifyItemsMutated(Collection<Structure> aItemC)
	{
		if (aItemC.isEmpty() == true)
			return;

		for (var aItem : aItemC)
			markPainterStale(aItem);

		updatePolyData();
		updateStatus(refStatusNotifier, aItemC, true);
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public Vector3D getCenter(Structure aItem)
	{
		if (aItem instanceof Ellipse aEllipse)
			return aEllipse.getCenter();
		else if (aItem instanceof Point aPoint)
			return aPoint.getCenter();

		return aItem.getRenderState().centerPt();
	}

	@Override
	public double getDiameter(Structure aItem)
	{
		if (aItem instanceof Ellipse aEllipse)
			return 2.0 * aEllipse.getRadius();
		else if (aItem instanceof PolyLine aPolyLine)
			return ControlPointUtil.calcSizeOnBody(refSmallBody, aPolyLine.getControlPoints());
		else if (aItem instanceof Point)
			return 0.0;

		throw new Error("Unsupported structure: " + aItem.getClass());
	}

	@Override
	public Structure getItem(int aIdx)
	{
		var tmpL = getAllItems();
		if (aIdx >= tmpL.size())
			return null;

		return tmpL.get(aIdx);
	}

	@Override
	public Vector3D getNormal(Structure aItem)
	{
		var center = getCenter(aItem);
		var ptArr = refSmallBody.getNormalAtPoint(center.toArray());
		return new Vector3D(ptArr);
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if we are not allowed to handle pick actions
		if (allowPickAction == false)
			return;

		// Bail if this is not a primary action
		if (aMode != PickMode.ActivePri)
			return;

		// Bail if popup trigger
		if (PickUtil.isPopupTrigger(aEvent) == true)
			return;

		// Bail if no associated painter
		var vMultiPainter = getMultiPainterFor(aPrimaryTarg);
		if (vMultiPainter == null)
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
	public void removeItems(Collection<Structure> aItemC)
	{
		if (aItemC.isEmpty() == true)
			return;

		var fullL = new ArrayList<>(getAllItems());
		fullL.removeAll(aItemC);
		setAllItems(fullL);
	}

	@Override
	public void setAllItems(Collection<Structure> aItemC)
	{
		super.setAllItems(aItemC);

		// Update our painters
		for (var aPainterVSMP : getAllPainters())
		{
			var partItemL = getItemsForPainter(aItemC, aPainterVSMP);
			aPainterVSMP.setWorkItems(partItemL);
		}
	}

	@Override
	public void setSelectedItems(Collection<Structure> aItemC)
	{
		// Keep track of the actual *changed* selection
		var origS = getSelectedItems();
		var targS = new HashSet<>(aItemC);
		var diffS = Sets.symmetricDifference(origS, targS);

		// Update our internal state
		super.setSelectedItems(aItemC);

		updateStatus(refStatusNotifier, aItemC, false);

		for (var aPainterVSMP : getAllPainters())
		{
			var partItemL = getItemsForPainter(aItemC, aPainterVSMP);
			var partDiffL = getItemsForPainter(diffS, aPainterVSMP);
			aPainterVSMP.setPickedItems(partItemL);
			aPainterVSMP.updateVtkColorsFor(partDiffL, true);
		}

		notifyListeners(this, ItemEventType.ItemsSelected);
	}

	@Override
	public void setColor(Collection<Structure> aItemC, Color aColor)
	{
		for (var aItem : aItemC)
			aItem.setColor(aColor);

		for (var aPainterVSMP : getAllPainters())
		{
			var partItemL = getItemsForPainter(aItemC, aPainterVSMP);
			aPainterVSMP.updateVtkColorsFor(partItemL, true);
		}

		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setColor(Structure aItem, Color aColor)
	{
		// Delegate
		setColor(ImmutableList.of(aItem), aColor);
	}

	@Override
	public void setIsVisible(Collection<Structure> aItemC, boolean aBool)
	{
		for (var aItem : aItemC)
			aItem.setVisible(aBool);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabel(Structure aItem, String aLabel)
	{
		aItem.setLabel(aLabel);

		// Update the appropriate painter
		var tmpPainter = getTextPainter(aItem);
		if (tmpPainter != null)
			tmpPainter.vtkMarkStale();

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelColor(Collection<Structure> aItemC, Color aColor)
	{
		for (var aItem : aItemC)
		{
			var tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), aColor, tmpFA.getSize(), tmpFA.getIsVisible());
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			var tmpPainter = getTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.vtkMarkStale();
		}

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelFontSize(Collection<Structure> aItemC, int aFontSize)
	{
		// Update the font size
		for (var aItem : aItemC)
		{
			var tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), aFontSize, tmpFA.getIsVisible());
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			var tmpPainter = getTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.vtkMarkStale();
		}

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelFontFamily(Collection<Structure> aItemC, String aFontFamily)
	{
		// Update the font size
		for (var aItem : aItemC)
		{
			var tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(aFontFamily, tmpFA.getColor(), tmpFA.getSize(), tmpFA.getIsVisible());
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			var tmpPainter = getTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.vtkMarkStale();
		}

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void setLabelVisible(Collection<Structure> aItemC, boolean aBool)
	{
		for (var aItem : aItemC)
		{
			var tmpFA = aItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), tmpFA.getSize(), aBool);
			aItem.setLabelFontAttr(tmpFA);

			// Update the appropriate painter
			var tmpPainter = getTextPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.vtkMarkStale();
		}

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	/**
	 * Helper method to set whether the specified item's interior is shown.
	 * <p>
	 * The associated VTK state will be updated.
	 */
	protected void configureInterior(Structure aItem, boolean aIsShown)
	{
		// Bail if this is not a ClosedShape
		if (aItem instanceof ClosedShape == false)
			return;
		var tmpItem = (ClosedShape) aItem;

		// Bail if nothing changes
		if (tmpItem.getShowInterior() == aIsShown)
			return;

		// Retrieve the relevant mainPainter
		var multiPainter = getMultiPainterFor(aItem);
		var mainPainter = multiPainter.getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();

		// Update the item and the corresponding mainPainter
		tmpItem.setShowInterior(aIsShown);
		// TODO: Optimization should be possible if the aIsShown is being toggled back on after just being toggled
		// off and nothing else has changed.
		mainPainter.vtkMarkStale();
		mainPainter.vtkUpdateState();

		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	/**
	 * Helper method to mark the painter(s) associated with the specified item as stale.
	 */
	protected void markPainterStale(Structure aItem)
	{
		// Locate the associated pointer
		var multiPainter = getMultiPainterFor(aItem);
		var compPainter = multiPainter.getVtkCompPainter(aItem);

		// Bail if no associated painter
		if (compPainter == null)
			return;

		compPainter.vtkMarkStale();
	}

	/**
	 * Helper method that delegates updating the VTK state of the sub painters.
	 */
	protected void updatePolyData()
	{
		vRegularVSMP.updatePolyData();
		vPointVSMP.updatePolyData();
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
	protected void updateStatus(StatusNotifier aStatusNotifier, Collection<Structure> aItemC, boolean aIsUpdate)
	{
		var tmpMsg = "";
		if (aItemC.size() == 1)
			tmpMsg = StructureMiscUtil.getStatusText(aItemC.iterator().next(), decimalFormat);
		else if (aItemC.size() > 1)
		{
			if (aIsUpdate == true)
				tmpMsg = "Multiple structures mutated: " + aItemC.size();
			else
				tmpMsg = "Multiple structures selected: " + aItemC.size();

			var totalArea = 0.0;
			for (var aItem : aItemC)
			{
				var tmpArea = aItem.getRenderState().surfaceArea();
				if (tmpArea > 0)
					totalArea += tmpArea;
			}
			if (totalArea > 0)
				tmpMsg += ", Area: " + decimalFormat.format(totalArea) + " km" + (char) 0x00B2;
		}

		refStatusNotifier.setPriStatus(tmpMsg, null);
	}

	/**
	 * Helper method that returns the relevant {@link Structure}s from the specified collection.
	 */
	private List<Structure> getItemsForPainter(Collection<Structure> aItemC, VtkStructureMultiPainter aPainter)
	{
		var retItemL = new ArrayList<Structure>();
		for (var aItem : aItemC)
		{
			if (getMultiPainterFor(aItem) == aPainter)
				retItemL.add(aItem);
		}

		return retItemL;
	}

	/**
	 * Helper method that returns the {@link VtkStructureMultiPainter} associated with the specified {@link PickTarget}.
	 */
	private VtkStructureMultiPainter getMultiPainterFor(PickTarget aPickTarget)
	{
		// Bail if the actor is not the right type
		var tmpProp = aPickTarget.getActor();
		if (tmpProp instanceof AssocActor == false)
			return null;

		var tmpObj = ((AssocActor) tmpProp).getAssocModel(Object.class);
		for (var aMultiPainter : getAllPainters())
			if (tmpObj == aMultiPainter)
				return aMultiPainter;

		return null;
	}

	/**
	 * Helper method that returns the {@link VtkStructureMultiPainter} that should be utilized for the specified
	 * {@link Structure}.
	 * <p>
	 * Returns null if a painter has not been created for the specified item.
	 */
	private VtkStructureMultiPainter getMultiPainterFor(Structure aItem)
	{
		if (aItem.getType() == StructureType.Point)
			return vPointVSMP;

		return vRegularVSMP;
	}

	/**
	 * Helper method that returns the {@link VtkLabelPainter} associated with the specified item.
	 * <p>
	 * Returns null if a painter has not been created for the specified item.
	 */
	private VtkLabelPainter<?> getTextPainter(Structure aItem)
	{
		return getMultiPainterFor(aItem).getVtkTextPainter(aItem);
	}

	/**
	 * Return all of the XYZ points for the specified item.
	 * <P>
	 * The returned XYZ points do not necessarily correspond to the control points. The number of points returned should
	 * be at a minimum equal to the number of control points.
	 */
	public ImmutableList<Vector3D> getXyzPointsFor(Structure aItem)
	{
		if (aItem.getType() == StructureType.Polygon && aItem instanceof Polygon aPolygon)
		{
			var tmpPainter = (VtkPolygonPainter) vRegularVSMP.getOrCreateVtkPainterFor(aPolygon, refSmallBody)
					.getMainPainter();

			tmpPainter.vtkUpdateState();
			return tmpPainter.getXyzPointList();
		}
		if (aItem.getType() == StructureType.Path && aItem instanceof PolyLine aPolyLine)
		{
			var tmpPainter = (VtkPolyLinePainter2) vRegularVSMP.getOrCreateVtkPainterFor(aPolyLine, refSmallBody)
					.getMainPainter();

			tmpPainter.vtkUpdateState();
			return tmpPainter.getXyzPointList();
		}

		throw new Error("Unsupported Structure: " + aItem.getClass() + " -> type: " + aItem.getType());
	}

}
