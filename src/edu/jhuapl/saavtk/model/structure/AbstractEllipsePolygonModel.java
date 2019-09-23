package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.CommonData;
import edu.jhuapl.saavtk.model.FacetColoringData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.io.StructureLoadUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.ProgressListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import glum.item.ItemEventType;
import glum.util.ThreadUtil;
import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkCaptionActor2D;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkIdTypeArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTransform;
import vtk.vtkUnsignedCharArray;

/**
 * Model of regular polygon structures drawn on a body.
 */
abstract public class AbstractEllipsePolygonModel extends StructureManager<EllipsePolygon>
		implements PropertyChangeListener, MetadataManager
{
	// Attributes
	private final PolyhedralModel smallBodyModel;
	private final Mode mode;
	private final String type;

	// State vars
	private Map<EllipsePolygon, VtkDrawState> drawM;
	private double defaultRadius;
	private final double maxRadius;
	private final int numberOfSides;
	private Color defaultColor = new Color(0, 191, 255);
	private double interiorOpacity = 0.3;
	private int maxPolygonId = 0;
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
		smallBodyModel = aSmallBodyModel;
		mode = aMode;
		type = aType;

		drawM = new HashMap<>();

		offset = getDefaultOffset();

		defaultRadius = aSmallBodyModel.getBoundingBoxDiagonalLength() / 155.0;
		maxRadius = aSmallBodyModel.getBoundingBoxDiagonalLength() / 8.0;

		smallBodyModel.addPropertyChangeListener(this);

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

	public Color getDefaultColor()
	{
		return defaultColor;
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

	@Override
	protected void updatePolyData()
	{
		actorL.clear();

		List<EllipsePolygon> tmpL = getAllItems();
		for (EllipsePolygon aItem : tmpL)
			aItem.updateVtkState(smallBodyModel);

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
			for (EllipsePolygon aItem : tmpL)
			{
				vtkPolyData extRegPD = aItem.vExteriorRegPD;
				vtkPolyData extDecPD = aItem.vExteriorDecPD;
				vtkPolyData intRegPD = aItem.vInteriorRegPD;
				vtkPolyData intDecPD = aItem.vInteriorDecPD;
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
			}

			for (EllipsePolygon aItem : tmpL)
			{
				vtkCaptionActor2D caption = updateStructure(aItem);
				if (caption != null)
					actorL.add(caption);
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

			smallBodyModel.shiftPolyLineInNormalDirection(vExteriorRegPD, offset);
			smallBodyModel.shiftPolyLineInNormalDirection(vExteriorDecPD, offset);
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
	public List<vtkProp> getProps()
	{
		return actorL;
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		if (prop != vExteriorActor && prop != vInteriorActor)
			return "";

		EllipsePolygon tmpItem = getStructureFromCellId(cellId, prop);
		if (tmpItem == null)
			return "";

		return tmpItem.getClickStatusBarText();
	}

	public vtkActor getBoundaryActor()
	{
		return vExteriorActor;
	}

	public vtkActor getInteriorActor()
	{
		return vInteriorActor;
	}

	@Override
	public EllipsePolygon addNewStructure()
	{
		throw new UnsupportedOperationException();
	}

	public void addNewStructure(double[] aCenter, double aRadius, double aFlattening, double aAngle)
	{
		EllipsePolygon tmpItem = new EllipsePolygon(numberOfSides, type, defaultColor, mode, ++maxPolygonId, "");
		tmpItem.setAngle(aAngle);
		tmpItem.setCenter(aCenter);
		tmpItem.setFlattening(aFlattening);
		tmpItem.setRadius(aRadius);

		List<EllipsePolygon> fullL = new ArrayList<>(getAllItems());
		fullL.add(tmpItem);

		List<EllipsePolygon> pickL = ImmutableList.of(tmpItem);

		setAllItems(fullL);
		setSelectedItems(pickL);

		updatePolyData();
	}

	public void addNewStructure(double[] pos)
	{
		addNewStructure(pos, defaultRadius, 1.0, 0.);
	}

	@Override
	public void removeStructures(Collection<EllipsePolygon> aItemC)
	{
		if (aItemC.isEmpty() == true)
			return;

		// Update VTK state
		for (EllipsePolygon aItem : aItemC)
		{
			aItem.clearVtkState();
			pcs.firePropertyChange(Properties.STRUCTURE_REMOVED, null, aItem);
		}

		List<EllipsePolygon> fullL = new ArrayList<>(getAllItems());
		fullL.removeAll(aItemC);
		setAllItems(fullL);

		updatePolyData();
	}

	@Override
	public void removeAllStructures()
	{
		// Update VTK state
		for (EllipsePolygon aItem : getAllItems())
			aItem.clearVtkState();

		setAllItems(ImmutableList.of());

		updatePolyData();
		pcs.firePropertyChange(Properties.ALL_STRUCTURES_REMOVED, null, null);
	}

	@Override
	public PolyhedralModel getPolyhedralModel()
	{
		return smallBodyModel;
	}

	public void movePolygon(EllipsePolygon aItem, double[] aCenter)
	{
		aItem.setCenter(aCenter);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Move the polygon to the specified latitude and longitude.
	 *
	 * @param aItem
	 * @param latitude  - in radians
	 * @param longitude - in radians
	 */
	public void movePolygon(EllipsePolygon aItem, double latitude, double longitude)
	{
		double[] newCenter = new double[3];
		smallBodyModel.getPointAndCellIdFromLatLon(latitude, longitude, newCenter);
		double[] center = getStructureCenter(aItem);

		// there is sometimes a radial offset (parallel to both center and newCenter)
		// that needs to be corrected
		Vector3D centerVec = new Vector3D(center);
		Vector3D newCenterVec = new Vector3D(newCenter);
		newCenterVec = newCenterVec.scalarMultiply(centerVec.getNorm() / newCenterVec.getNorm());

		// System.out.println(newCenterVec+" "+centerVec+"
		// "+newCenterVec.crossProduct(centerVec));
		// LatLon ll=MathUtil.reclat(centerVec.toArray());
		// LatLon ll2=MathUtil.reclat(newCenterVec.toArray());
		// System.out.println(Math.toDegrees(ll.lat)+" "+Math.toDegrees(ll.lon)+"
		// "+Math.toDegrees(ll2.lat)+" "+Math.toDegrees(ll2.lon));
		movePolygon(aItem, newCenterVec.toArray());
	}

	public void changeRadiusOfPolygon(EllipsePolygon aItem, double[] aNewPointOnPerimeter)
	{
		double[] center = aItem.getCenter();
		double newRadius = Math.sqrt((center[0] - aNewPointOnPerimeter[0]) * (center[0] - aNewPointOnPerimeter[0])
				+ (center[1] - aNewPointOnPerimeter[1]) * (center[1] - aNewPointOnPerimeter[1])
				+ (center[2] - aNewPointOnPerimeter[2]) * (center[2] - aNewPointOnPerimeter[2]));
		if (newRadius > maxRadius)
			newRadius = maxRadius;

		aItem.setRadius(newRadius);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	protected double computeFlatteningOfPolygon(double[] center, double radius, double angle,
			double[] newPointOnPerimeter)
	{
		// The following math does this: we need to find the direction of
		// the semimajor axis of the ellipse. Then once we have that
		// we need to find the distance to that line from the point the mouse
		// is hovering, where that point is first projected onto the
		// tangent plane of the asteroid at the ellipse center.
		// This distance divided by the semimajor axis of the ellipse
		// is what we call the flattening.

		// First compute cross product of normal and z axis
		double[] normal = smallBodyModel.getNormalAtPoint(center);
		double[] zaxis = { 0.0, 0.0, 1.0 };
		double[] cross = new double[3];
		MathUtil.vcrss(zaxis, normal, cross);
		// Compute angle between normal and zaxis
		double sepAngle = MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

		vtkTransform transform = new vtkTransform();
		transform.Translate(center);
		transform.RotateWXYZ(sepAngle, cross);
		transform.RotateZ(angle);

		double[] xaxis = { 1.0, 0.0, 0.0 };
		xaxis = transform.TransformDoubleVector(xaxis);
		MathUtil.vhat(xaxis, xaxis);

		// Project newPoint onto the plane perpendicular to the
		// normal of the shape model.
		double[] projPoint = new double[3];
		MathUtil.vprjp(newPointOnPerimeter, normal, center, projPoint);
		double[] projDir = new double[3];
		MathUtil.vsub(projPoint, center, projDir);

		double[] proj = new double[3];
		MathUtil.vproj(projDir, xaxis, proj);
		double[] distVec = new double[3];
		MathUtil.vsub(projDir, proj, distVec);
		double newRadius = MathUtil.vnorm(distVec);

		double newFlattening = 1.0;
		if (radius > 0.0)
			newFlattening = newRadius / radius;

		if (newFlattening < 0.001)
			newFlattening = 0.001;
		else if (newFlattening > 1.0)
			newFlattening = 1.0;

		transform.Delete();

		return newFlattening;
	}

	public void changeFlatteningOfPolygon(EllipsePolygon aItem, double[] aNewPointOnPerimeter)
	{
		double tmpFlattening = computeFlatteningOfPolygon(aItem.getCenter(), aItem.getRadius(), aItem.getAngle(),
				aNewPointOnPerimeter);
		aItem.setFlattening(tmpFlattening);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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

	protected double computeAngleOfPolygon(double[] aCenter, double[] aNewPointOnPerimeter)
	{
		// The following math does this: we need to find the direction of
		// the semimajor axis of the ellipse. Then once we have that
		// we need to find the angular distance between the axis and the
		// vector from the ellipse center to the point the mouse
		// is hovering, where that vector is first projected onto the
		// tangent plane of the asteroid at the ellipse center.
		// This angular distance is what we rotate the ellipse by.

		// First compute cross product of normal and z axis
		double[] normal = smallBodyModel.getNormalAtPoint(aCenter);
		double[] zaxis = { 0.0, 0.0, 1.0 };
		double[] cross = new double[3];
		MathUtil.vcrss(zaxis, normal, cross);
		// Compute angle between normal and zaxis
		double sepAngle = MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

		vtkTransform transform = new vtkTransform();
		transform.Translate(aCenter);
		transform.RotateWXYZ(sepAngle, cross);

		double[] xaxis = { 1.0, 0.0, 0.0 };
		xaxis = transform.TransformDoubleVector(xaxis);
		MathUtil.vhat(xaxis, xaxis);

		// Project newPoint onto the plane perpendicular to the
		// normal of the shape model.
		double[] projPoint = new double[3];
		MathUtil.vprjp(aNewPointOnPerimeter, normal, aCenter, projPoint);
		double[] projDir = new double[3];
		MathUtil.vsub(projPoint, aCenter, projDir);
		MathUtil.vhat(projDir, projDir);

		// Compute angular distance between projected direction and transformed x-axis
		double newAngle = MathUtil.vsep(projDir, xaxis) * 180.0 / Math.PI;

		// We need to negate this angle under certain conditions.
		if (newAngle != 0.0)
		{
			MathUtil.vcrss(xaxis, projDir, cross);
			double a = MathUtil.vsep(cross, normal) * 180.0 / Math.PI;
			if (a > 90.0)
				newAngle = -newAngle;
		}

		transform.Delete();

		return newAngle;
	}

	public void changeAngleOfPolygon(EllipsePolygon aItem, double[] aNewPointOnPerimeter)
	{
		double tmpAngle = computeAngleOfPolygon(aItem.getCenter(), aNewPointOnPerimeter);
		aItem.setAngle(tmpAngle);

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void changeRadiusOfAllPolygons(double aRadius)
	{
		for (EllipsePolygon aItem : getAllItems())
			aItem.setRadius(aRadius);

		updatePolyData();
	}

	@Override
	public void activateStructure(EllipsePolygon aItem)
	{
		// Do nothing. RegularPolygonModel does not support activation.
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
			EllipsePolygon tmpItem = getAllItems().get(i);
			if (tmpItem.getVisible() == false)
				continue;

			if (interior)
				numberCellsSoFar += tmpItem.vInteriorRegPD.GetNumberOfCells();
			else
				numberCellsSoFar += tmpItem.vExteriorRegPD.GetNumberOfCells();
			if (cellId < numberCellsSoFar)
				return i;
		}
		return -1;
	}

	@Override
	public void loadModel(File aFile, boolean aAppend, ProgressListener aListener) throws IOException
	{
		// Load the EllipsePolygons file
		List<EllipsePolygon> tmpPolyL = StructureLoadUtil.loadEllipsePolygons(aFile, mode, defaultRadius, defaultColor,
				numberOfSides, type);

		// Init the VTK state
		int tmpCnt = 0;
		for (EllipsePolygon aPoly : tmpPolyL)
		{
			if (aPoly.getId() > maxPolygonId)
				maxPolygonId = aPoly.getId();

			aPoly.updateVtkState(smallBodyModel);
			updateStructure(aPoly);

			if (aListener != null)
				aListener.setProgress(tmpCnt * 98 / tmpPolyL.size());

			tmpCnt++;
		}

		// Finish on the AWT
		ThreadUtil.invokeAndWaitOnAwt(() -> {
			// Update our list of polygons
			List<EllipsePolygon> fullL = new ArrayList<>(getAllItems());
			if (aAppend == false)
				fullL.clear();
			fullL.addAll(tmpPolyL);

			setAllItems(fullL);
			updatePolyData();

			if (aListener != null)
				aListener.setProgress(100);
		});
	}

	@Override
	public void saveModel(File file) throws IOException
	{
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);

		for (EllipsePolygon pol : getAllItems())
		{
			String name = pol.getName();
			if (name.length() == 0)
				name = "default";

			// Since tab is used as the delimiter, replace any tabs in the name with spaces.
			name = name.replace('\t', ' ');

			LatLon llr = MathUtil.reclat(pol.getCenter());
			double lat = llr.lat * 180.0 / Math.PI;
			double lon = llr.lon * 180.0 / Math.PI;
			if (lon < 0.0)
				lon += 360.0;

			String str = "" + pol.getId() + "\t" + name + "\t" + pol.getCenter()[0] + "\t" + pol.getCenter()[1] + "\t"
					+ pol.getCenter()[2] + "\t" + lat + "\t" + lon + "\t" + llr.rad;

			str += "\t";

			double[] values = getStandardColoringValuesAtPolygon(pol);
			for (int i = 0; i < values.length; ++i)
			{
				str += Double.isNaN(values[i]) ? "NA" : values[i];
				if (i < values.length - 1)
					str += "\t";
			}

			str += "\t" + 2.0 * pol.getRadius(); // save out as diameter, not radius

			str += "\t" + pol.getFlattening() + "\t" + pol.getAngle();

			Color color = pol.getColor();
			str += "\t" + color.getRed() + "," + color.getGreen() + "," + color.getBlue();

			if (mode == Mode.ELLIPSE_MODE)
			{
				Double gravityAngle = getEllipseAngleRelativeToGravityVector(pol);
				if (gravityAngle != null)
					str += "\t" + gravityAngle;
				else
					str += "\t" + "NA";
			}

			str += "\t" + "\"" + pol.getLabel() + "\"";

			// String labelcolorStr="\tlc:"+pol.labelcolor[0] + "," + pol.labelcolor[1] +
			// "," + pol.labelcolor[2];
			// str+=labelcolorStr;

			str += "\n";

			out.write(str);
		}

		out.close();
	}

	@Override
	public EllipsePolygon getActivatedStructure()
	{
		return null;
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
	public EllipsePolygon getStructureFromCellId(int aCellId, vtkProp aProp)
	{
		int tmpIdx = -1;
		if (aProp == vExteriorActor)
			tmpIdx = getPolygonIdFromCellId(aCellId, false);
		else if (aProp == vInteriorActor)
			tmpIdx = getPolygonIdFromCellId(aCellId, true);

		if (tmpIdx != -1)
			return getStructure(tmpIdx);

		return null;
	}

	public void redrawAllStructures()
	{
		for (EllipsePolygon aItem : getAllItems())
			aItem.vIsStale = true;

		updatePolyData();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
		{
			redrawAllStructures();
		}
	}

	private double[] getStandardColoringValuesAtPolygon(EllipsePolygon pol) throws IOException
	{
		// Output array of 4 standard colorings (Slope, Elevation, GravAccel,
		// GravPotential).
		// Assume at the outset that none of the standard colorings are available.
		final double[] standardValues = new double[] { Double.NaN, Double.NaN, Double.NaN, Double.NaN };

		if (!smallBodyModel.isColoringDataAvailable())
			return standardValues;

		int slopeIndex = -1;
		int elevationIndex = -1;
		int accelerationIndex = -1;
		int potentialIndex = -1;

		// Locate any of the 4 standard plate colorings in the list of all colorings
		// available for this resolution.
		// Usually the standard colorings are first in the list, so the loop could
		// terminate after all
		// 4 are >= 0, but omitting this check for brevity and readability.
		List<ColoringData> coloringDataList = smallBodyModel.getAllColoringData();
		for (int index = 0; index < coloringDataList.size(); ++index)
		{
			String name = coloringDataList.get(index).getName();
			if (name.equalsIgnoreCase(PolyhedralModel.SlopeStr))
			{
				slopeIndex = index;
			}
			else if (name.equalsIgnoreCase(PolyhedralModel.ElevStr))
			{
				elevationIndex = index;
			}
			else if (name.equalsIgnoreCase(PolyhedralModel.GravAccStr))
			{
				accelerationIndex = index;
			}
			// This is a hack -- unfortunately, in at least OREx's case, this vector is
			// given a different name.
			else if (name.equalsIgnoreCase("Gravitational Magnitude"))
			{
				accelerationIndex = index;
			}
			else if (name.equalsIgnoreCase(PolyhedralModel.GravPotStr))
			{
				potentialIndex = index;
			}
		}

		// Get all the coloring values interpolated at the center of the polygon.
		double[] allValues;

		try
		{
			allValues = smallBodyModel.getAllColoringValues(pol.getCenter());
			if (mode != Mode.POINT_MODE)
			{
				// Replace slope and/or elevation central values with the average over the rim
				// of the circle.
				if (slopeIndex != -1 || elevationIndex != -1)
				{
					if (slopeIndex != -1)
						allValues[slopeIndex] = 0.; // Accumulate weighted sum in situ.
					if (elevationIndex != -1)
						allValues[elevationIndex] = 0.; // Accumulate weighted sum in situ.

					vtkCellArray lines = pol.vExteriorRegPD.GetLines();
					vtkPoints points = pol.vExteriorRegPD.GetPoints();

					vtkIdTypeArray idArray = lines.GetData();
					int size = idArray.GetNumberOfTuples();

					double totalLength = 0.0;
					double[] midpoint = new double[3];
					for (int i = 0; i < size; i += 3)
					{
						if (idArray.GetValue(i) != 2)
						{
							System.out.println("Big problem: polydata corrupted");
							return standardValues;
						}

						double[] pt1 = points.GetPoint(idArray.GetValue(i + 1));
						double[] pt2 = points.GetPoint(idArray.GetValue(i + 2));

						MathUtil.midpointBetween(pt1, pt2, midpoint);
						double dist = MathUtil.distanceBetween(pt1, pt2);
						totalLength += dist;

						double[] valuesAtMidpoint = smallBodyModel.getAllColoringValues(midpoint);

						// Accumulate sums weighted by the length of this polygon segment.
						if (slopeIndex != -1)
							allValues[slopeIndex] += valuesAtMidpoint[slopeIndex] * dist;
						if (elevationIndex != -1)
							allValues[elevationIndex] += valuesAtMidpoint[elevationIndex] * dist;
					}

					// Normalize by the total (perimeter).
					if (slopeIndex != -1)
						allValues[slopeIndex] /= totalLength;
					if (elevationIndex != -1)
						allValues[elevationIndex] /= totalLength;
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Warning: plate coloring values were not available; omitting them from structures file.");
			System.err.println("Exception thrown was " + e.getMessage());

			allValues = new double[coloringDataList.size()];
			for (int index = 0; index < allValues.length; ++index)
			{
				allValues[index] = Double.NaN;
			}
		}

		// Use whichever standard coloring values are present to populate the output
		// array.
		if (slopeIndex != -1)
			standardValues[0] = allValues[slopeIndex];
		if (elevationIndex != -1)
			standardValues[1] = allValues[elevationIndex];
		if (accelerationIndex != -1)
			standardValues[2] = allValues[accelerationIndex];
		if (potentialIndex != -1)
			standardValues[3] = allValues[potentialIndex];

		return standardValues;
	}

	private Double getEllipseAngleRelativeToGravityVector(EllipsePolygon pol)
	{
		double[] gravityVector = smallBodyModel.getGravityVector(pol.getCenter());
		if (gravityVector == null)
			return null;
		MathUtil.vhat(gravityVector, gravityVector);

		// First compute cross product of normal and z axis
		double[] normal = smallBodyModel.getNormalAtPoint(pol.getCenter());
		double[] zaxis = { 0.0, 0.0, 1.0 };
		double[] cross = new double[3];
		MathUtil.vcrss(zaxis, normal, cross);
		// Compute angle between normal and zaxis
		double sepAngle = -MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

		// Rotate gravity vector and center of ellipse by amount
		// such that normal of ellipse faces positive z-axis
		vtkTransform transform = new vtkTransform();
		transform.RotateWXYZ(sepAngle, cross);

		gravityVector = transform.TransformDoubleVector(gravityVector);
		double[] center = transform.TransformDoublePoint(pol.getCenter());

		// project gravity into xy plane
		double[] gravityPoint = { center[0] + gravityVector[0], center[1] + gravityVector[1],
				center[2] + gravityVector[2], };
		double[] projGravityPoint = new double[3];
		MathUtil.vprjp(gravityPoint, zaxis, center, projGravityPoint);
		double[] projGravityVector = new double[3];
		MathUtil.vsub(projGravityPoint, center, projGravityVector);
		MathUtil.vhat(projGravityVector, projGravityVector);

		// Compute direction of semimajor axis (both directions) in xy plane
		transform.Delete();
		transform = new vtkTransform();
		transform.RotateZ(pol.getAngle());

		// Positive x direction
		double[] xaxis = { 1.0, 0.0, 0.0 };
		double[] semimajoraxis1 = transform.TransformDoubleVector(xaxis);

		// Negative x direction
		double[] mxaxis = { -1.0, 0.0, 0.0 };
		double[] semimajoraxis2 = transform.TransformDoubleVector(mxaxis);

		// Compute angular separation of projected gravity vector
		// with respect to x-axis using atan2
		double gravAngle = Math.atan2(projGravityVector[1], projGravityVector[0]) * 180.0 / Math.PI;
		if (gravAngle < 0.0)
			gravAngle += 360.0;

		// Compute angular separations of semimajor axes vectors (both directions)
		// with respect to x-axis using atan2
		double smaxisangle1 = Math.atan2(semimajoraxis1[1], semimajoraxis1[0]) * 180.0 / Math.PI;
		if (smaxisangle1 < 0.0)
			smaxisangle1 += 360.0;

		double smaxisangle2 = Math.atan2(semimajoraxis2[1], semimajoraxis2[0]) * 180.0 / Math.PI;
		if (smaxisangle2 < 0.0)
			smaxisangle2 += 360.0;

		// Compute angular separations between semimajor axes and gravity vector.
		// The smaller one is the one we want, which should be between 0 and 180
		// degrees.
		double sepAngle1 = smaxisangle1 - gravAngle;
		if (sepAngle1 < 0.0)
			sepAngle1 += 360.0;

		double sepAngle2 = smaxisangle2 - gravAngle;
		if (sepAngle2 < 0.0)
			sepAngle2 += 360.0;

		transform.Delete();

		return Math.min(sepAngle1, sepAngle2);
	}

	@Override
	public double getDefaultOffset()
	{
		return 5.0 * smallBodyModel.getMinShiftAmount();
	}

	@Override
	public void setOffset(double offset)
	{
		this.offset = offset;

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
	public void savePlateDataInsideStructure(EllipsePolygon aItem, File file) throws IOException
	{
		vtkPolyData polydata = aItem.vInteriorRegPD;
		smallBodyModel.savePlateDataInsidePolydata(polydata, file);
	}

	@Override
	public void savePlateDataInsideStructure(Collection<EllipsePolygon> aItemC, File file) throws IOException
	{
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();

		for (EllipsePolygon aItem : aItemC)
			appendFilter.AddInputData(aItem.vInteriorRegPD);
		appendFilter.Update();
		// vtkPolyData polydata = aItem.interiorPolyData;
		smallBodyModel.savePlateDataInsidePolydata(appendFilter.GetOutput(), file);
	}

	@Override
	public FacetColoringData[] getPlateDataInsideStructure(EllipsePolygon aItem)
	{
		vtkPolyData polydata = aItem.vInteriorRegPD;
		return smallBodyModel.getPlateDataInsidePolydata(polydata);
	}

	@Override
	public FacetColoringData[] getPlateDataInsideStructure(Collection<EllipsePolygon> aItemC)
	{
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (EllipsePolygon aItem : aItemC)
			appendFilter.AddInputData(aItem.vInteriorRegPD);
		appendFilter.Update();
		return smallBodyModel.getPlateDataInsidePolydata(appendFilter.GetOutput());
	}

	@Override
	public double[] getStructureCenter(EllipsePolygon aItem)
	{
		return aItem.getCenter();
	}

	@Override
	public double[] getStructureNormal(EllipsePolygon aItem)
	{
		double[] center = getStructureCenter(aItem);
		return smallBodyModel.getNormalAtPoint(center);
	}

	@Override
	public double getStructureSize(EllipsePolygon aItem)
	{
		return 2.0 * aItem.getRadius();
	}

	private static final Key<List<EllipsePolygon>> ELLIPSE_POLYGON_KEY = Key.of("ellipses");
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
		result.put(DEFAULT_COLOR_KEY, StructureLoadUtil.convertColorToRgba(defaultColor));
		result.put(INTERIOR_OPACITY_KEY, interiorOpacity);

		List<EllipsePolygon> fullL = getAllItems();
		Set<EllipsePolygon> pickS = getSelectedItems();
		int[] idArr = new int[pickS.size()];// List<Integer> idL = new ArrayList<>();
		int cntA = 0;
		for (EllipsePolygon aItem : pickS)
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
		List<EllipsePolygon> restoredPolygons = source.get(ELLIPSE_POLYGON_KEY);

		// Now we're committed. Get rid of whatever's currently in this model and then
		// add the restored polygons.
		// Finally, change the rest of the fields.
		this.defaultRadius = defaultRadius;
		this.defaultColor = StructureLoadUtil.convertRgbaToColor(defaultColor);
		this.interiorOpacity = interiorOpacity;
		this.offset = offset;

		List<EllipsePolygon> pickL = new ArrayList<>();
		for (int aIdx : selectedStructures)
			pickL.add(restoredPolygons.get(aIdx));

		// Put the restored polygons in the list.
		setAllItems(restoredPolygons);
		setSelectedItems(pickL);

		// Sync everything up.
		updatePolyData();
		setLineWidth(lineWidth);
	}

	@Override
	protected void updateVtkColorsFor(Collection<EllipsePolygon> aItemC, boolean aSendNotification)
	{
		Color pickColor = null;
		CommonData commonData = getCommonData();
		if (commonData != null)
			pickColor = commonData.getSelectionColor();

		// Update internal VTK state
		for (EllipsePolygon aItem : aItemC)
		{
			// Skip to next if not visible
			if (aItem.getVisible() == false)
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
			endIdx = begIdx + aItem.vExteriorRegPD.GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA3(vExteriorColorsRegUCA, aIdx, tmpColor);

			begIdx = vDrawState.extBegIdxDec;
			endIdx = begIdx + aItem.vExteriorDecPD.GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA3(vExteriorColorsDecUCA, aIdx, tmpColor);

			begIdx = vDrawState.intBegIdxReg;
			endIdx = begIdx + aItem.vInteriorRegPD.GetNumberOfCells();
			for (int aIdx = begIdx; aIdx < endIdx; aIdx++)
				VtkUtil.setColorOnUCA3(vInteriorColorsRegUCA, aIdx, tmpColor);

			begIdx = vDrawState.intBegIdxDec;
			endIdx = begIdx + aItem.vInteriorDecPD.GetNumberOfCells();
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

}
