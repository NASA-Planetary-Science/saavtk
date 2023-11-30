package edu.jhuapl.saavtk.structure.vtk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Point;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.RenderAttr;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.VtkLodActor;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkUnsignedCharArray;

/**
 * Class used to render multiple {@link Structure} objects via the VTK framework.
 * <p>
 * This class supports the following configurable state:
 * <ul>
 * <li>interior opacity
 * <li>line width
 * <li>offset (from surface)
 * </ul>
 *
 * @author lopeznr1
 */
public class VtkStructureMultiPainter implements VtkPropProvider
{
	// Reference vars
	private final SceneChangeNotifier refSceneChangeNotifier;
	private final PolyhedralModel refSmallBody;

	// State vars
	private ImmutableSet<Structure> workItemS;
	private ImmutableSet<Structure> pickItemS;
	private ImmutableSet<Structure> dimmedItemS;

	private RenderAttr renderAttr;
	private Color drawColor;
	private Color pickColor;
	private double interiorOpacity;

	private final Map<Structure, VtkCompositePainter<Structure, VtkStructurePainter>> vPainterM;

	// Attributes
	private Map<Structure, VtkDrawState> drawM;

	// VTK vars
	private final List<vtkProp> actorL;
	private vtkPolyData vExteriorRegPD;
	private vtkPolyData vExteriorDecPD;
	private vtkAppendPolyData vExteriorFilterRegAPD;
	private vtkAppendPolyData vExteriorFilterDecAPD;
	private vtkPolyDataMapper vExteriorRegPDM;
	private vtkPolyDataMapper vExteriorDecPDM;
	private VtkLodActor vExteriorActor;

	private vtkPolyData vInteriorRegPD;
	private vtkPolyData vInteriorDecPD;
	private vtkAppendPolyData vInteriorFilterRegAPD;
	private vtkAppendPolyData vInteriorFilterDecAPD;
	private vtkPolyDataMapper vInteriorRegPDM;
	private vtkPolyDataMapper vInteriorDecPDM;
	private VtkLodActor vInteriorActor;

	private vtkUnsignedCharArray vExteriorColorsRegUCA;
	private vtkUnsignedCharArray vExteriorColorsDecUCA;
	private vtkUnsignedCharArray vInteriorColorsRegUCA;
	private vtkUnsignedCharArray vInteriorColorsDecUCA;

	private vtkPolyData vEmptyPD;

	/** Standard Constructor */
	public VtkStructureMultiPainter(SceneChangeNotifier aSceneChangeNotifier, PolyhedralModel aSmallBody,
			RenderAttr aRenderAttr)
	{
		refSceneChangeNotifier = aSceneChangeNotifier;
		refSmallBody = aSmallBody;

		workItemS = ImmutableSet.of();
		pickItemS = ImmutableSet.of();
		dimmedItemS = ImmutableSet.of();

		renderAttr = aRenderAttr;
		drawColor = null;
		pickColor = new Color(0, 0, 255);
		interiorOpacity = 0.3;

		vPainterM = new HashMap<>();

		drawM = new HashMap<>();

		vEmptyPD = new vtkPolyData();

		vExteriorColorsRegUCA = new vtkUnsignedCharArray();
		vExteriorColorsDecUCA = new vtkUnsignedCharArray();
		vExteriorColorsRegUCA.SetNumberOfComponents(4);
		vExteriorColorsDecUCA.SetNumberOfComponents(4);

		vInteriorColorsRegUCA = new vtkUnsignedCharArray();
		vInteriorColorsDecUCA = new vtkUnsignedCharArray();
		vInteriorColorsRegUCA.SetNumberOfComponents(4);
		vInteriorColorsDecUCA.SetNumberOfComponents(4);

		vExteriorRegPD = new vtkPolyData();
		vExteriorDecPD = new vtkPolyData();
		vExteriorFilterRegAPD = new vtkAppendPolyData();
		vExteriorFilterRegAPD.UserManagedInputsOn();
		vExteriorFilterDecAPD = new vtkAppendPolyData();
		vExteriorFilterDecAPD.UserManagedInputsOn();
		vExteriorRegPDM = new vtkPolyDataMapper();
		vExteriorDecPDM = new vtkPolyDataMapper();
		vExteriorActor = new VtkLodActor(this);
		var exteriorProperty = vExteriorActor.GetProperty();
		exteriorProperty.LightingOff();
		exteriorProperty.SetLineWidth((float) renderAttr.lineWidth());

		vInteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
		vInteriorFilterRegAPD = new vtkAppendPolyData();
		vInteriorFilterRegAPD.UserManagedInputsOn();
		vInteriorFilterDecAPD = new vtkAppendPolyData();
		vInteriorFilterDecAPD.UserManagedInputsOn();
		vInteriorRegPDM = new vtkPolyDataMapper();
		vInteriorDecPDM = new vtkPolyDataMapper();
		vInteriorActor = new VtkLodActor(this);
		var interiorProperty = vInteriorActor.GetProperty();
		interiorProperty.LightingOff();
		interiorProperty.SetOpacity(interiorOpacity);

		actorL = new ArrayList<>();
	}

	/**
	 * Returns all of the {@link vtkActor}s that can be hooked.
	 */
	public ImmutableList<vtkActor> getHookActors()
	{
		return ImmutableList.of(vExteriorActor, vInteriorActor);
	}

	public double getInteriorOpacity()
	{
		return interiorOpacity;
	}

	/**
	 * Sets the items that should be rendered in a deemphasized state.
	 */
	public void setDimmedItems(Collection<? extends Structure> aItemC)
	{
		// Keep track of all items that will need to have updated coloring
		var updateItemL = new ArrayList<Structure>();
		updateItemL.addAll(dimmedItemS);
		updateItemL.addAll(aItemC);

		// Update internal state
		dimmedItemS = ImmutableSet.copyOf(aItemC);

		// Update coloring of the relevant items
		updateVtkColorsFor(updateItemL, true);
	}

	public void setDrawColor(Color aColor)
	{
		drawColor = aColor;
	}

	public void setInteriorOpacity(double opacity)
	{
		interiorOpacity = opacity;
		vInteriorActor.GetProperty().SetOpacity(opacity);
		notifyVtkStateChange();
	}

	/**
	 * Sets the {@link RenderAttr} used by this painter.
	 */
	public void setRenderAttr(RenderAttr aRenderAttr)
	{
		// Update the lineWidth only if it is valid and has changed
		var lineWidth = aRenderAttr.lineWidth();
		if (lineWidth >= 1.0 && lineWidth != renderAttr.lineWidth())
		{
			var exteriorProperty = vExteriorActor.GetProperty();
			exteriorProperty.SetLineWidth((float) lineWidth);
			notifyVtkStateChange();
		}

		// Update the VtkPointPainters
		var isVtkStateStale = false;
		for (var aCompPainter : vPainterM.values())
		{
			if (aCompPainter.getMainPainter() instanceof VtkPointPainter aMainPainter)
			{
				aMainPainter.setRenderAttr(aRenderAttr);
				isVtkStateStale = true;
			}
		}

		if (isVtkStateStale == true)
			updatePolyData();

		renderAttr = aRenderAttr;
	}

	/**
	 * Returns the structure corresponding to the specified cell id.
	 * <p>
	 * Many cells are associated with each item, thus we need to determine which item the selected cell corresponds to.
	 */
	public Structure getItemFromCellId(vtkProp aProp, int aCellId)
	{
		// Bail if we are not associated with the vtkProp
		if (aProp != vInteriorActor && aProp != vExteriorActor)
			return null;

		// Locate the item corresponding to aCellId
		int fullCellCnt = 0;
		for (var aItem : workItemS)
		{
			// Skip over invisible items
			if (aItem.getVisible() == false)
				continue;

			// Skip over non rendered items
			var tmpPainter = getVtkMainPainter(aItem);
			if (tmpPainter == null)
				continue;

			if (aProp == vInteriorActor)
				fullCellCnt += tmpPainter.vtkGetInteriorRegPD().GetNumberOfCells();
			else
				fullCellCnt += tmpPainter.vtkGetExteriorRegPD().GetNumberOfCells();

			if (fullCellCnt > aCellId)
				return aItem;
		}

		return null;
	}

	/**
	 * Returns the (composite) painter for the specified item.
	 * <p>
	 * A painter will be instantiated if necessary.
	 */
	public VtkCompositePainter<Structure, VtkStructurePainter> getOrCreateVtkPainterFor(Structure aItem,
			PolyhedralModel aSmallBody)
	{
		var retPainter = vPainterM.get(aItem);
		if (retPainter == null)
		{
			retPainter = new VtkCompositePainter<>(aSmallBody, aItem, createPainter(aItem));
			vPainterM.put(aItem, retPainter);
		}

		return retPainter;
	}

	/**
	 * Returns the (composite) painter for the specified item.
	 * <p>
	 * A painter will not be instantiated, but rather null will be returned.
	 */
	public VtkCompositePainter<Structure, VtkStructurePainter> getVtkCompPainter(Structure aItem)
	{
		return vPainterM.get(aItem);
	}

	/**
	 * Returns the {@link VtkLabelPainter} for the specified item. This is the painter responsible for the rendering of
	 * label.
	 */
	public VtkLabelPainter<?> getVtkTextPainter(Structure aItem)
	{
		var tmpPainter = vPainterM.get(aItem);
		if (tmpPainter == null)
			return null;

		return tmpPainter.getTextPainter();
	}

	/**
	 * Returns all of the items this painter is responsible for.
	 */
	public ImmutableSet<Structure> getWorkItems()
	{
		return workItemS;
	}

	/**
	 * Notifies this painter of items that are in a "picked" state.
	 */
	public void setPickedItems(Collection<? extends Structure> aItemC)
	{
		pickItemS = ImmutableSet.copyOf(aItemC);
	}

	/**
	 * Sets in the items this painter is responsible for.
	 */
	public void setWorkItems(Collection<? extends Structure> aItemC)
	{
		workItemS = ImmutableSet.copyOf(aItemC);

		updatePolyData();

		// Clear out unused painters in vPainterM
		VtkUtil.flushResourceMap(vPainterM, aItemC);
	}

	/**
	 * Method to cause the relevant VTK state to be updated.
	 */
	public void updatePolyData()
	{
		var radialOffset = renderAttr.radialOffset();

		actorL.clear();

		if (workItemS.size() > 0)
		{
			vExteriorFilterRegAPD.SetNumberOfInputs(workItemS.size());
			vExteriorFilterDecAPD.SetNumberOfInputs(workItemS.size());
			vInteriorFilterRegAPD.SetNumberOfInputs(workItemS.size());
			vInteriorFilterDecAPD.SetNumberOfInputs(workItemS.size());

			// Keep track of the begin idx for each item (and corresponding PolyData)
			drawM = new HashMap<>();
			int extCurrCntDec = 0;
			int extCurrCntReg = 0;
			int intCurrCntDec = 0;
			int intCurrCntReg = 0;
			int idx = 0;
			for (var aItem : workItemS)
			{
				var tmpPainter = getOrCreateVtkPainterFor(aItem, refSmallBody);
				tmpPainter.vtkUpdateState();

				var mainPainter = tmpPainter.getMainPainter();
				var extRegPD = mainPainter.vtkGetExteriorRegPD();
				var extDecPD = mainPainter.vtkGetExteriorDecPD();
				var intRegPD = mainPainter.vtkGetInteriorRegPD();
				var intDecPD = mainPainter.vtkGetInteriorDecPD();
				if (aItem.getVisible() == false)
				{
					extRegPD = vEmptyPD;
					extDecPD = vEmptyPD;
					intRegPD = vEmptyPD;
					intDecPD = vEmptyPD;
				}

				drawM.put(aItem, new VtkDrawState(extCurrCntDec, extCurrCntReg, intCurrCntDec, intCurrCntReg));
				extCurrCntDec += extDecPD.GetNumberOfCells();
				extCurrCntReg += extRegPD.GetNumberOfCells();
				intCurrCntDec += intDecPD.GetNumberOfCells();
				intCurrCntReg += intRegPD.GetNumberOfCells();

				vExteriorFilterRegAPD.SetInputDataByNumber(idx, extRegPD);
				vExteriorFilterDecAPD.SetInputDataByNumber(idx, extDecPD);
				vInteriorFilterRegAPD.SetInputDataByNumber(idx, intRegPD);
				vInteriorFilterDecAPD.SetInputDataByNumber(idx, intDecPD);

				idx++;

				// Keep track of captions that are displayed
				var textPainter = tmpPainter.getTextPainter();
				var tmpActor = textPainter.getActor();
				if (tmpActor != null)
					actorL.add(tmpActor);
			}

			vExteriorFilterRegAPD.Update();
			vExteriorFilterDecAPD.Update();
			vInteriorFilterRegAPD.Update();
			vInteriorFilterDecAPD.Update();

			var exteriorFilterAppendRegOutput = vExteriorFilterRegAPD.GetOutput();
			var exteriorFilterAppendDecOutput = vExteriorFilterDecAPD.GetOutput();
			var interiorFilterAppendRegOutput = vInteriorFilterRegAPD.GetOutput();
			var interiorFilterAppendDecOutput = vInteriorFilterDecAPD.GetOutput();
			vExteriorRegPD.DeepCopy(exteriorFilterAppendRegOutput);
			vExteriorDecPD.DeepCopy(exteriorFilterAppendDecOutput);
			vInteriorRegPD.DeepCopy(interiorFilterAppendRegOutput);
			vInteriorDecPD.DeepCopy(interiorFilterAppendDecOutput);

			refSmallBody.shiftPolyLineInNormalDirection(vExteriorRegPD, radialOffset);
			refSmallBody.shiftPolyLineInNormalDirection(vExteriorDecPD, radialOffset);
			PolyDataUtil.shiftPolyDataInNormalDirection(vInteriorRegPD, radialOffset);
			PolyDataUtil.shiftPolyDataInNormalDirection(vInteriorDecPD, radialOffset);

			vExteriorColorsRegUCA.SetNumberOfTuples(vExteriorRegPD.GetNumberOfCells());
			vExteriorColorsDecUCA.SetNumberOfTuples(vExteriorDecPD.GetNumberOfCells());
			vInteriorColorsRegUCA.SetNumberOfTuples(vInteriorRegPD.GetNumberOfCells());
			vInteriorColorsDecUCA.SetNumberOfTuples(vInteriorDecPD.GetNumberOfCells());

			updateVtkColorsFor(workItemS, false);

			var exteriorRegCD = vExteriorRegPD.GetCellData();
			var exteriorDecCD = vExteriorDecPD.GetCellData();
			var interiorRegCD = vInteriorRegPD.GetCellData();
			var interiorDecCD = vInteriorDecPD.GetCellData();

			actorL.add(vInteriorActor);
			actorL.add(vExteriorActor);

			exteriorRegCD.SetScalars(vExteriorColorsRegUCA);
			exteriorDecCD.SetScalars(vExteriorColorsDecUCA);
			interiorRegCD.SetScalars(vInteriorColorsRegUCA);
			interiorDecCD.SetScalars(vInteriorColorsDecUCA);

			exteriorFilterAppendRegOutput.Delete();
			exteriorFilterAppendDecOutput.Delete();
			interiorFilterAppendRegOutput.Delete();
			interiorFilterAppendDecOutput.Delete();
			exteriorRegCD.Delete();
			exteriorDecCD.Delete();
			interiorRegCD.Delete();
			interiorDecCD.Delete();
		}
		else
		{
			vExteriorRegPD.DeepCopy(vEmptyPD);
			vExteriorDecPD.DeepCopy(vEmptyPD);
			vInteriorRegPD.DeepCopy(vEmptyPD);
			vInteriorDecPD.DeepCopy(vEmptyPD);
		}

		vExteriorRegPDM.SetInputData(vExteriorRegPD);
		vExteriorDecPDM.SetInputData(vExteriorDecPD);
		vInteriorRegPDM.SetInputData(vInteriorRegPD);
		vInteriorDecPDM.SetInputData(vInteriorDecPD);

		vExteriorActor.setDefaultMapper(vExteriorRegPDM);
		vExteriorActor.setLodMapper(LodMode.MaxQuality, vExteriorRegPDM);
		vExteriorActor.setLodMapper(LodMode.MaxSpeed, vExteriorDecPDM);
		vInteriorActor.setDefaultMapper(vInteriorRegPDM);
		vInteriorActor.setLodMapper(LodMode.MaxQuality, vInteriorRegPDM);
		vInteriorActor.setLodMapper(LodMode.MaxSpeed, vInteriorDecPDM);

		vExteriorActor.Modified();
		vInteriorActor.Modified();

		notifyVtkStateChange();
	}

	/**
	 * Method to update the VTK coloring state for the specified items.
	 *
	 * @param aItemC
	 *    The items of interest.
	 */
	public void updateVtkColorsFor(Collection<? extends Structure> aItemC, boolean aSendNotification)
	{
		// Update internal VTK state
		for (var aItem : aItemC)
		{
			// Skip to next if not visible
			if (aItem.getVisible() == false)
				continue;

			// Skip to next if not rendered
			var tmpPainter = getVtkMainPainter(aItem);
			if (tmpPainter == null)
				continue;

			// Skip to next if no associated draw state
			var vDrawState = drawM.get(aItem);
			if (vDrawState == null)
				continue;

			// Update label color
			var tmpOpacity = 1.0;
			var isDimmed = dimmedItemS.contains(aItem) == true;
			if (isDimmed == true)
				tmpOpacity = 0.35;

			var textPainter = getVtkTextPainter(aItem);
			textPainter.setOpacity(tmpOpacity);

			// Update the color related state
			var tmpColor = drawColor;
			if (tmpColor == null)
			{
				tmpColor = aItem.getColor();
				if (isDimmed == true)
					tmpColor = new Color(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(),
							tmpColor.getAlpha() / 4);
				else if (pickItemS.contains(aItem) == true)
					tmpColor = pickColor;
			}
			int begIdx, endIdx;

			begIdx = vDrawState.extBegIdxReg;
			endIdx = begIdx + (int) tmpPainter.vtkGetExteriorRegPD().GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA4(vExteriorColorsRegUCA, aIdx, tmpColor);

			begIdx = vDrawState.extBegIdxDec;
			endIdx = begIdx + (int) tmpPainter.vtkGetExteriorDecPD().GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA4(vExteriorColorsDecUCA, aIdx, tmpColor);

			begIdx = vDrawState.intBegIdxReg;
			endIdx = begIdx + (int) tmpPainter.vtkGetInteriorRegPD().GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA4(vInteriorColorsRegUCA, aIdx, tmpColor);

			begIdx = vDrawState.intBegIdxDec;
			endIdx = begIdx + (int) tmpPainter.vtkGetInteriorDecPD().GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA4(vInteriorColorsDecUCA, aIdx, tmpColor);
		}

		// Bail if notification is not needed
		if (aSendNotification == false)
			return;

		vExteriorColorsRegUCA.Modified();
		vExteriorColorsDecUCA.Modified();
		vInteriorColorsRegUCA.Modified();
		vInteriorColorsDecUCA.Modified();

		notifyVtkStateChange();
	}

	@Override
	public List<vtkProp> getProps()
	{
		return actorL;
	}

	/**
	 * Helper method to create a painter for the specified item.
	 */
	private VtkStructurePainter createPainter(Structure aItem)
	{
		if (aItem instanceof Point aPoint)
			return new VtkPointPainter(refSmallBody, aPoint, renderAttr);
		else if (aItem instanceof Ellipse aEllipse)
			return new VtkEllipsePainter(refSmallBody, aEllipse, renderAttr);
		else if (aItem instanceof Polygon aPolygon)
			return new VtkPolygonPainter(refSmallBody, aPolygon);
		else if (aItem instanceof PolyLine aPolyLine)
			return new VtkPolyLinePainter2(refSmallBody, aPolyLine);

		throw new Error("Unsupported Structure: " + aItem.getClass() + " -> type: " + aItem.getType());
	}

	/**
	 * Helper method that returns the primary painter for the specified item. This is the painter responsible for
	 * rendering a structure's shape.
	 */
	private VtkStructurePainter getVtkMainPainter(Structure aItem)
	{
		var tmpPainter = vPainterM.get(aItem);
		if (tmpPainter == null)
			return null;

		return tmpPainter.getMainPainter();
	}

	/**
	 * Helper method that notifies the system that our internal VTK state has been changed.
	 */
	private void notifyVtkStateChange()
	{
		refSceneChangeNotifier.notifySceneChange();
	}

	/**
	 * Record used to track the VTK (index) state associated with a structure.
	 *
	 * @author lopeznr1
	 */
	record VtkDrawState(int extBegIdxDec, int extBegIdxReg, int intBegIdxDec, int intBegIdxReg)
	{
	}

}
