package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;
import edu.jhuapl.saavtk.model.CommonData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.BaseStructureManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.io.StructureLegacyUtil;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.structure.vtk.VtkCompositePainter;
import edu.jhuapl.saavtk.structure.vtk.VtkEllipsePainter;
import edu.jhuapl.saavtk.structure.vtk.VtkLabelPainter;
import edu.jhuapl.saavtk.structure.vtk.VtkUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import glum.item.ItemEventType;
import glum.task.Task;
import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkCellData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkUnsignedCharArray;

/**
 * Model of regular polygon structures drawn on a body.
 */
abstract public class AbstractEllipsePolygonModel extends BaseStructureManager<Ellipse, VtkEllipsePainter>
		implements PropertyChangeListener, MetadataManager
{
	// Attributes
	private final PolyhedralModel refSmallBody;
	private final Mode mode;
	private final String type;

	// State vars
	private Map<Ellipse, VtkDrawState> drawM;
	private double defaultRadius;
	private final double maxRadius;
	private final int numberOfSides;
	private Color defaultColor = new Color(0, 191, 255);
	private double interiorOpacity = 0.3;
	private double offset;
	private double lineWidth;

	// VTK vars
	private final List<vtkProp> actorL;
	private vtkPolyData vExteriorRegPD;
	private vtkPolyData vExteriorDecPD;
	private vtkAppendPolyData vExteriorFilterRegAPD;
	private vtkAppendPolyData vExteriorFilterDecAPD;
	private vtkPolyDataMapper vExteriorRegPDM;
	private vtkPolyDataMapper vExteriorDecPDM;
	private SaavtkLODActor vExteriorActor;

	private vtkPolyData vInteriorRegPD;
	private vtkPolyData vInteriorDecPD;
	private vtkAppendPolyData vInteriorFilterRegAPD;
	private vtkAppendPolyData vInteriorFilterDecAPD;
	private vtkPolyDataMapper vInteriorRegPDM;
	private vtkPolyDataMapper vInteriorDecPDM;
	private SaavtkLODActor vInteriorActor;

	private vtkUnsignedCharArray vExteriorColorsRegUCA;
	private vtkUnsignedCharArray vExteriorColorsDecUCA;
	private vtkUnsignedCharArray vInteriorColorsRegUCA;
	private vtkUnsignedCharArray vInteriorColorsDecUCA;

	private vtkPolyData vEmptyPD;

	public enum Mode
	{
		POINT_MODE, CIRCLE_MODE, ELLIPSE_MODE
	}

	public AbstractEllipsePolygonModel(PolyhedralModel aSmallBodyModel, int aNumberOfSides, Mode aMode, String aType)
	{
		super(aSmallBodyModel);

		refSmallBody = aSmallBodyModel;
		mode = aMode;
		type = aType;

		drawM = new HashMap<>();

		offset = getDefaultOffset();

		defaultRadius = aSmallBodyModel.getBoundingBoxDiagonalLength() / 155.0;
		maxRadius = aSmallBodyModel.getBoundingBoxDiagonalLength() / 8.0;

		refSmallBody.addPropertyChangeListener(this);

		vEmptyPD = new vtkPolyData();

		numberOfSides = aNumberOfSides;

		vExteriorColorsRegUCA = new vtkUnsignedCharArray();
		vExteriorColorsDecUCA = new vtkUnsignedCharArray();
		vExteriorColorsRegUCA.SetNumberOfComponents(3);
		vExteriorColorsDecUCA.SetNumberOfComponents(3);

		vInteriorColorsRegUCA = new vtkUnsignedCharArray();
		vInteriorColorsDecUCA = new vtkUnsignedCharArray();
		vInteriorColorsRegUCA.SetNumberOfComponents(3);
		vInteriorColorsDecUCA.SetNumberOfComponents(3);

		vExteriorRegPD = new vtkPolyData();
		vExteriorDecPD = new vtkPolyData();
		vExteriorFilterRegAPD = new vtkAppendPolyData();
		vExteriorFilterRegAPD.UserManagedInputsOn();
		vExteriorFilterDecAPD = new vtkAppendPolyData();
		vExteriorFilterDecAPD.UserManagedInputsOn();
		vExteriorRegPDM = new vtkPolyDataMapper();
		vExteriorDecPDM = new vtkPolyDataMapper();
		vExteriorActor = new SaavtkLODActor();
		vtkProperty boundaryProperty = vExteriorActor.GetProperty();
		boundaryProperty.LightingOff();
		lineWidth = 2.;
		boundaryProperty.SetLineWidth(lineWidth);

		vInteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
		vInteriorFilterRegAPD = new vtkAppendPolyData();
		vInteriorFilterRegAPD.UserManagedInputsOn();
		vInteriorFilterDecAPD = new vtkAppendPolyData();
		vInteriorFilterDecAPD.UserManagedInputsOn();
		vInteriorRegPDM = new vtkPolyDataMapper();
		vInteriorDecPDM = new vtkPolyDataMapper();
		vInteriorActor = new SaavtkLODActor();
		vtkProperty interiorProperty = vInteriorActor.GetProperty();
		interiorProperty.LightingOff();
		interiorProperty.SetOpacity(interiorOpacity);
		// interiorProperty.SetLineWidth(2.0);

		actorL = new ArrayList<>();
	}

	public void setDefaultColor(Color aColor)
	{
		defaultColor = aColor;
	}

	public double getInteriorOpacity()
	{
		return interiorOpacity;
	}

	public void setInteriorOpacity(double opacity)
	{
		interiorOpacity = opacity;
		vInteriorActor.GetProperty().SetOpacity(opacity);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public Mode getMode()
	{
		return mode;
	}

	public int getNumberOfSides()
	{
		return numberOfSides;
	}

	/**
	 * Returns the vtkPolyData associated with the exterior of the structure.
	 * <P>
	 * Do NOT mutate the returned VTK object - it is meant only for read access!
	 */
	public vtkPolyData getVtkExteriorPolyDataFor(Ellipse aItem)
	{
		VtkEllipsePainter tmpPainter = getOrCreateVtkMainPainterFor(aItem);
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
		VtkEllipsePainter tmpPainter = getOrCreateVtkMainPainterFor(aItem);
		tmpPainter.vtkUpdateState();

		// Delegate
		return tmpPainter.getVtkInteriorPolyData();
	}

	@Override
	public Vector3D getCentroid(Ellipse aItem)
	{
		double[] tmpArr = refSmallBody.findClosestPoint(aItem.getCenter().toArray());
		return new Vector3D(tmpArr);
	}

	@Override
	public List<vtkProp> getProps()
	{
		return actorL;
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		if (prop != vExteriorActor && prop != vInteriorActor)
			return "";

		Ellipse tmpItem = getItemFromCellId(cellId, prop);
		if (tmpItem == null)
			return "";

		String retStr = type + ", Id = " + tmpItem.getId();
		retStr += ", Diameter = " + 2.0 * tmpItem.getRadius() + " km";
		return retStr;
	}

	@Override
	public void installItems(Task aTask, List<Ellipse> aItemL)
	{
		// Ensure all items are fully initialized
		for (Ellipse aItem : aItemL)
		{
			if (aItem.getColor() == null)
				aItem.setColor(defaultColor);
			if (Double.isNaN(aItem.getRadius()) == true)
				aItem.setRadius(defaultRadius);
		}

		super.installItems(aTask, aItemL);
	}

	public vtkActor getBoundaryActor()
	{
		return vExteriorActor;
	}

	public vtkActor getInteriorActor()
	{
		return vInteriorActor;
	}

	public void addNewStructure(Vector3D aCenter, double aRadius, double aFlattening, double aAngle)
	{
		int tmpId = StructureMiscUtil.calcNextId(this);
		Ellipse tmpItem = new Ellipse(tmpId, null, mode, aCenter, aRadius, aAngle, aFlattening, defaultColor, "");

		List<Ellipse> fullL = new ArrayList<>(getAllItems());
		fullL.add(tmpItem);

		List<Ellipse> pickL = ImmutableList.of(tmpItem);

		setAllItems(fullL);
		setSelectedItems(pickL);

		updatePolyData();
	}

	public void addNewStructure(Vector3D aCenter)
	{
		addNewStructure(aCenter, defaultRadius, 1.0, 0.);
	}

	public void movePolygon(Ellipse aItem, Vector3D aCenter)
	{
		aItem.setCenter(aCenter);
		markPainterStale(aItem);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	/**
	 * Move the polygon to the specified latitude and longitude.
	 *
	 * @param aItem
	 * @param latitude  - in radians
	 * @param longitude - in radians
	 */
	public void movePolygon(Ellipse aItem, double latitude, double longitude)
	{
		Vector3D oldCenter = getCenter(aItem);

		double[] newCenterArr = new double[3];
		refSmallBody.getPointAndCellIdFromLatLon(latitude, longitude, newCenterArr);
		Vector3D newCenter = new Vector3D(newCenterArr);

		// there is sometimes a radial offset (parallel to both center and newCenter)
		// that needs to be corrected
		newCenter = newCenter.scalarMultiply(oldCenter.getNorm() / newCenter.getNorm());

		// System.out.println(newCenterVec+" "+centerVec+"
		// "+newCenterVec.crossProduct(centerVec));
		// LatLon ll=MathUtil.reclat(centerVec.toArray());
		// LatLon ll2=MathUtil.reclat(newCenterVec.toArray());
		// System.out.println(Math.toDegrees(ll.lat)+" "+Math.toDegrees(ll.lon)+"
		// "+Math.toDegrees(ll2.lat)+" "+Math.toDegrees(ll2.lon));
		movePolygon(aItem, newCenter);
	}

	public void changeRadiusOfPolygon(Ellipse aItem, Vector3D aNewPointOnPerimeter)
	{
		double[] newPointOnPerimeterArr = aNewPointOnPerimeter.toArray();

		double[] center = aItem.getCenter().toArray();
		double newRadius = Math.sqrt((center[0] - newPointOnPerimeterArr[0]) * (center[0] - newPointOnPerimeterArr[0])
				+ (center[1] - newPointOnPerimeterArr[1]) * (center[1] - newPointOnPerimeterArr[1])
				+ (center[2] - newPointOnPerimeterArr[2]) * (center[2] - newPointOnPerimeterArr[2]));
		if (newRadius > maxRadius)
			newRadius = maxRadius;

		aItem.setRadius(newRadius);
		markPainterStale(aItem);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	public void changeFlatteningOfPolygon(Ellipse aItem, Vector3D aNewPointOnPerimeter)
	{
		double tmpFlattening = EllipseUtil.computeFlatteningOfPolygon(refSmallBody, aItem.getCenter(), aItem.getRadius(),
				aItem.getAngle(), aNewPointOnPerimeter);

		aItem.setFlattening(tmpFlattening);
		markPainterStale(aItem);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	// TODO: Better comments
	// TODO: Perhaps this should act on the Structure level
	public int getPolygonIdFromBoundaryCellId(int cellId)
	{
		return getPolygonIdFromCellId(cellId, false);
	}

	// TODO: Better comments
	// TODO: Perhaps this should act on the Structure level
	public int getPolygonIdFromInteriorCellId(int cellId)
	{
		return getPolygonIdFromCellId(cellId, true);
	}

	// TODO: Add comments
	public void changeAngleOfPolygon(Ellipse aItem, Vector3D aNewPointOnPerimeter)
	{
		double tmpAngle = EllipseUtil.computeAngleOfPolygon(refSmallBody, aItem.getCenter(), aNewPointOnPerimeter);

		aItem.setAngle(tmpAngle);
		markPainterStale(aItem);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	// TODO: Add comments
	public void changeRadiusOfAllPolygons(double aRadius)
	{
		for (Ellipse aItem : getAllItems())
		{
			aItem.setRadius(aRadius);
			markPainterStale(aItem);
		}

		updatePolyData();
	}

	/**
	 * A picker picking the actor of this model will return a cellId. But since
	 * there are many cells per RegularPolygon, we need to be able to figure out
	 * which RegularPolygon was picked
	 */
	private int getPolygonIdFromCellId(int cellId, boolean interior)
	{
		int numberCellsSoFar = 0;
		for (int i = 0; i < getAllItems().size(); ++i)
		{
			// Skip over invisible items
			Ellipse tmpItem = getAllItems().get(i);
			if (tmpItem.getVisible() == false)
				continue;

			// Skip over non rendered items
			VtkEllipsePainter tmpPainter = getVtkMainPainter(tmpItem);
			if (tmpPainter == null)
				continue;

			if (interior == true)
				numberCellsSoFar += tmpPainter.getVtkInteriorPolyData().GetNumberOfCells();
			else
				numberCellsSoFar += tmpPainter.getVtkExteriorPolyData().GetNumberOfCells();
			if (cellId < numberCellsSoFar)
				return i;
		}
		return -1;
	}

	public void redrawAllStructures()
	{
		for (Ellipse aItem : getAllItems())
			markPainterStale(aItem);

		updatePolyData();
	}

	@Override
	public boolean supportsActivation()
	{
		return false;
	}

	public double getDefaultRadius()
	{
		return defaultRadius;
	}

	public void setDefaultRadius(double radius)
	{
		defaultRadius = radius;
	}

	@Override
	public Ellipse getItemFromCellId(int aCellId, vtkProp aProp)
	{
		int tmpIdx = -1;
		if (aProp == vExteriorActor)
			tmpIdx = getPolygonIdFromCellId(aCellId, false);
		else if (aProp == vInteriorActor)
			tmpIdx = getPolygonIdFromCellId(aCellId, true);

		if (tmpIdx != -1)
			return getItem(tmpIdx);

		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
		{
			redrawAllStructures();
		}
	}

	@Override
	public double getDefaultOffset()
	{
		return 5.0 * refSmallBody.getMinShiftAmount();
	}

	@Override
	public void setOffset(double aOffset)
	{
		offset = aOffset;

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public double getOffset()
	{
		return offset;
	}

	@Override
	public double getLineWidth()
	{
		return lineWidth;
	}

	@Override
	public void setLineWidth(double aWidth)
	{
		if (aWidth >= 1.0)
		{
			lineWidth = aWidth;
			vtkProperty boundaryProperty = vExteriorActor.GetProperty();
			boundaryProperty.SetLineWidth(lineWidth);

			notifyListeners(this, ItemEventType.ItemsMutated);
			pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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
	protected VtkEllipsePainter createPainter(Ellipse aItem)
	{
		return new VtkEllipsePainter(refSmallBody, aItem, numberOfSides);
	}

	/**
	 * Helper method that will return the proper VTK painter for the specified item.
	 */
	protected VtkEllipsePainter getOrCreateVtkMainPainterFor(Ellipse aItem)
	{
		return getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();
	}

	@Override
	protected void updatePolyData()
	{
		actorL.clear();

		List<Ellipse> tmpL = getAllItems();
		if (tmpL.size() > 0)
		{
			vExteriorFilterRegAPD.SetNumberOfInputs(tmpL.size());
			vExteriorFilterDecAPD.SetNumberOfInputs(tmpL.size());
			vInteriorFilterRegAPD.SetNumberOfInputs(tmpL.size());
			vInteriorFilterDecAPD.SetNumberOfInputs(tmpL.size());

			// Keep track of the begin idx for each item (and corresponding PolyData)
			drawM = new HashMap<>();
			int extCurrCntDec = 0;
			int extCurrCntReg = 0;
			int intCurrCntDec = 0;
			int intCurrCntReg = 0;
			int idx = 0;
			for (Ellipse aItem : tmpL)
			{
				VtkCompositePainter<?, VtkEllipsePainter> tmpPainter = getOrCreateVtkPainterFor(aItem, refSmallBody);
				tmpPainter.vtkUpdateState();

				VtkEllipsePainter mainPainter = tmpPainter.getMainPainter();
				vtkPolyData extRegPD = mainPainter.getVtkExteriorPolyData();
				vtkPolyData extDecPD = mainPainter.getVtkExteriorDecPolyData();
				vtkPolyData intRegPD = mainPainter.getVtkInteriorPolyData();
				vtkPolyData intDecPD = mainPainter.getVtkInteriorDecPolyData();
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
				VtkLabelPainter<?> textPainter = tmpPainter.getTextPainter();
				vtkProp tmpActor = textPainter.getActor();
				if (tmpActor != null)
					actorL.add(tmpActor);
			}

			vExteriorFilterRegAPD.Update();
			vExteriorFilterDecAPD.Update();
			vInteriorFilterRegAPD.Update();
			vInteriorFilterDecAPD.Update();

			vtkPolyData boundaryAppendFilterOutput = vExteriorFilterRegAPD.GetOutput();
			vtkPolyData decimatedBoundaryAppendFilterOutput = vExteriorFilterDecAPD.GetOutput();
			vtkPolyData interiorAppendFilterOutput = vInteriorFilterRegAPD.GetOutput();
			vtkPolyData decimatedInteriorAppendFilterOutput = vInteriorFilterDecAPD.GetOutput();
			vExteriorRegPD.DeepCopy(boundaryAppendFilterOutput);
			vExteriorDecPD.DeepCopy(decimatedBoundaryAppendFilterOutput);
			vInteriorRegPD.DeepCopy(interiorAppendFilterOutput);
			vInteriorDecPD.DeepCopy(decimatedInteriorAppendFilterOutput);

			refSmallBody.shiftPolyLineInNormalDirection(vExteriorRegPD, offset);
			refSmallBody.shiftPolyLineInNormalDirection(vExteriorDecPD, offset);
			PolyDataUtil.shiftPolyDataInNormalDirection(vInteriorRegPD, offset);
			PolyDataUtil.shiftPolyDataInNormalDirection(vInteriorDecPD, offset);

			vExteriorColorsRegUCA.SetNumberOfTuples(vExteriorRegPD.GetNumberOfCells());
			vExteriorColorsDecUCA.SetNumberOfTuples(vExteriorDecPD.GetNumberOfCells());
			vInteriorColorsRegUCA.SetNumberOfTuples(vInteriorRegPD.GetNumberOfCells());
			vInteriorColorsDecUCA.SetNumberOfTuples(vInteriorDecPD.GetNumberOfCells());

			updateVtkColorsFor(tmpL, false);

			vtkCellData boundaryCellData = vExteriorRegPD.GetCellData();
			vtkCellData decimatedBoundaryCellData = vExteriorDecPD.GetCellData();
			vtkCellData interiorCellData = vInteriorRegPD.GetCellData();
			vtkCellData decimatedInteriorCellData = vInteriorDecPD.GetCellData();

			actorL.add(vInteriorActor);
			actorL.add(vExteriorActor);

			boundaryCellData.SetScalars(vExteriorColorsRegUCA);
			decimatedBoundaryCellData.SetScalars(vExteriorColorsDecUCA);
			interiorCellData.SetScalars(vInteriorColorsRegUCA);
			decimatedInteriorCellData.SetScalars(vInteriorColorsDecUCA);

			boundaryAppendFilterOutput.Delete();
			decimatedBoundaryAppendFilterOutput.Delete();
			interiorAppendFilterOutput.Delete();
			decimatedInteriorAppendFilterOutput.Delete();
			boundaryCellData.Delete();
			decimatedBoundaryCellData.Delete();
			interiorCellData.Delete();
			decimatedInteriorCellData.Delete();
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

		vExteriorActor.SetMapper(vExteriorRegPDM);
		vExteriorActor.setLODMapper(vExteriorDecPDM);
		vInteriorActor.SetMapper(vInteriorRegPDM);
		vInteriorActor.setLODMapper(vInteriorDecPDM);

		vExteriorActor.Modified();
		vInteriorActor.Modified();

		// Notify model change listeners
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	protected void updateVtkColorsFor(Collection<Ellipse> aItemC, boolean aSendNotification)
	{
		Color pickColor = null;
		CommonData commonData = getCommonData();
		if (commonData != null)
			pickColor = commonData.getSelectionColor();

		// Update internal VTK state
		for (Ellipse aItem : aItemC)
		{
			// Skip to next if not visible
			if (aItem.getVisible() == false)
				continue;

			// Skip to next if not rendered
			VtkEllipsePainter tmpPainter = getVtkMainPainter(aItem);
			if (tmpPainter == null)
				continue;

			// Skip to next if no associated draw state
			VtkDrawState vDrawState = drawM.get(aItem);
			if (vDrawState == null)
				continue;

			// Update the color related state
			Color tmpColor = aItem.getColor();
			if (pickColor != null && getSelectedItems().contains(aItem) == true)
				tmpColor = pickColor;

			int begIdx, endIdx;

			begIdx = vDrawState.extBegIdxReg;
			endIdx = begIdx + tmpPainter.getVtkExteriorPolyData().GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA3(vExteriorColorsRegUCA, aIdx, tmpColor);

			begIdx = vDrawState.extBegIdxDec;
			endIdx = begIdx + tmpPainter.getVtkExteriorDecPolyData().GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA3(vExteriorColorsDecUCA, aIdx, tmpColor);

			begIdx = vDrawState.intBegIdxReg;
			endIdx = begIdx + tmpPainter.getVtkInteriorPolyData().GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA3(vInteriorColorsRegUCA, aIdx, tmpColor);

			begIdx = vDrawState.intBegIdxDec;
			endIdx = begIdx + tmpPainter.getVtkInteriorDecPolyData().GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA3(vInteriorColorsDecUCA, aIdx, tmpColor);
		}

		// Bail if notification is not needed
		if (aSendNotification == false)
			return;

		vExteriorColorsRegUCA.Modified();
		vExteriorColorsDecUCA.Modified();
		vInteriorColorsRegUCA.Modified();
		vInteriorColorsDecUCA.Modified();

		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Helper method to mark the painter(s) associated with the specified item as
	 * stale.
	 */
	private void markPainterStale(Ellipse aItem)
	{
		VtkCompositePainter<?, VtkEllipsePainter> tmpPainter = getVtkCompPainter(aItem);
		if (tmpPainter == null)
			return;

		tmpPainter.getMainPainter().markStale();
		tmpPainter.getTextPainter().markStale();
	}

	/**
	 * Class used to track the VTK (index) state associated with a structure.
	 *
	 * @author lopeznr1
	 */
	class VtkDrawState
	{
		public VtkDrawState(int aExtBegIdxDec, int aExtBegIdxReg, int aIntBegIdxDec, int aIntBegIdxReg)
		{
			extBegIdxDec = aExtBegIdxDec;
			extBegIdxReg = aExtBegIdxReg;
			intBegIdxDec = aIntBegIdxDec;
			intBegIdxReg = aIntBegIdxReg;
		}

		// State vars
		final int extBegIdxDec;
		final int extBegIdxReg;
		final int intBegIdxDec;
		final int intBegIdxReg;
	}

	private static final Key<List<Ellipse>> ELLIPSE_POLYGON_KEY = Key.of("ellipses");
	private static final Key<Double> DEFAULT_RADIUS_KEY = Key.of("defaultRadius");
	private static final Key<int[]> DEFAULT_COLOR_KEY = Key.of("defaultColor");
	private static final Key<Double> INTERIOR_OPACITY_KEY = Key.of("interiorOpacity");
	private static final Key<int[]> SELECTED_STRUCTURES_KEY = Key.of("selectedStructures");
	private static final Key<Double> LINE_WIDTH_KEY = Key.of("lineWidth");
	private static final Key<Double> OFFSET_KEY = Key.of("offset");

	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));

		result.put(ELLIPSE_POLYGON_KEY, getAllItems());
		result.put(DEFAULT_RADIUS_KEY, defaultRadius);
		result.put(DEFAULT_COLOR_KEY, StructureLegacyUtil.convertColorToRgba(defaultColor));
		result.put(INTERIOR_OPACITY_KEY, interiorOpacity);

		List<Ellipse> fullL = getAllItems();
		Set<Ellipse> pickS = getSelectedItems();
		int[] idArr = new int[pickS.size()];// List<Integer> idL = new ArrayList<>();
		int cntA = 0;
		for (Ellipse aItem : pickS)
		{
			int tmpIdx = fullL.indexOf(aItem);
			idArr[cntA] = tmpIdx;
			cntA++;
		}
		result.put(SELECTED_STRUCTURES_KEY, idArr);

		result.put(LINE_WIDTH_KEY, lineWidth);
		result.put(OFFSET_KEY, offset);

		return result;
	}

	@Override
	public void retrieve(Metadata source)
	{
		// The order of these operations is significant to try to keep the object state
		// consistent. First get everything from the metadata into local variables.
		// Don't touch the model yet in case there's a problem.
		double defaultRadius = source.get(DEFAULT_RADIUS_KEY);
		int[] defaultColor = source.get(DEFAULT_COLOR_KEY);
		double interiorOpacity = source.get(INTERIOR_OPACITY_KEY);
		int[] selectedStructures = source.get(SELECTED_STRUCTURES_KEY);
		double lineWidth = source.get(LINE_WIDTH_KEY);
		double offset = source.get(OFFSET_KEY);
		List<Ellipse> restoredPolygons = source.get(ELLIPSE_POLYGON_KEY);

		// Now we're committed. Get rid of whatever's currently in this model and then
		// add the restored polygons.
		// Finally, change the rest of the fields.
		this.defaultRadius = defaultRadius;
		this.defaultColor = StructureLegacyUtil.convertRgbaToColor(defaultColor);
		this.interiorOpacity = interiorOpacity;
		this.offset = offset;

		List<Ellipse> pickL = new ArrayList<>();
		for (int aIdx : selectedStructures)
			pickL.add(restoredPolygons.get(aIdx));

		// Put the restored polygons in the list.
		setAllItems(restoredPolygons);
		setSelectedItems(pickL);

		setLineWidth(lineWidth);
	}

}
