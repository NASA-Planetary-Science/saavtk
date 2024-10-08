package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.BaseStructureManager;
import edu.jhuapl.saavtk.structure.ControlPointsHandler;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.PolyLineMode;
import edu.jhuapl.saavtk.structure.util.ControlPointUtil;
import edu.jhuapl.saavtk.structure.vtk.VtkCompositePainter;
import edu.jhuapl.saavtk.structure.vtk.VtkControlPointPainter;
import edu.jhuapl.saavtk.structure.vtk.VtkLabelPainter;
import edu.jhuapl.saavtk.structure.vtk.VtkPolyLinePainter;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.VtkLodActor;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import glum.item.ItemEventType;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkUnsignedCharArray;

/**
 * Model of line structures drawn on a body.
 */
public class LineModel<G1 extends PolyLine> extends BaseStructureManager<G1, VtkPolyLinePainter<G1>>
		implements ControlPointsHandler<G1>, PropertyChangeListener
{
	// Constants
	private static final String LINES = "lines";

	private static final Color redColor = Color.RED;
	private static final Color greenColor = Color.GREEN;
	private static final Color blueColor = Color.BLUE;

	// Ref vars
	private final PolyhedralModel refSmallBody;

	// State vars
	private Map<PolyLine, Vector3D> propM;

	private final PolyLineMode mode;
	private G1 activatedLine;
	private int activatedControlPointIdx;

	private Color spawnColor;
	private int maximumVerticesPerLine;

	// VTK vars
	private final VtkControlPointPainter activationPainter;

	private final vtkPolyData vLinesRegPD;
	private final vtkPolyData vLinesDecPD;
	private final vtkPolyData vActivationPD;

	private final vtkPolyDataMapper vLineMapperRegPDM;
	private final vtkPolyDataMapper vLineMapperDecPDM;
	private final vtkPolyDataMapper vLineActivationMapperPDM;
	private final VtkLodActor vLineActor;
	private final vtkActor vLineActivationActor;
	private final vtkIdList vIdRegIL;
	private final vtkIdList vIdDecIL;

	private final vtkPolyData vEmptyPD;

	/** Standard Constructor */
	public LineModel(SceneChangeNotifier aSceneChangeNotifier, StatusNotifier aStatusNotifier,
			PolyhedralModel aSmallBody, PolyLineMode aMode)
	{
		super(aSceneChangeNotifier, aStatusNotifier, aSmallBody);

		refSmallBody = aSmallBody;

		propM = new HashMap<>();

		mode = aMode;
		activatedLine = null;
		activatedControlPointIdx = -1;

		spawnColor = Color.MAGENTA;
		maximumVerticesPerLine = Integer.MAX_VALUE;
		if (hasProfileMode() == true)
			setMaximumVerticesPerLine(2);

		refSmallBody.addPropertyChangeListener(this);

		vIdRegIL = new vtkIdList();
		vIdDecIL = new vtkIdList();

		vLineActor = new VtkLodActor(this);
		vtkProperty lineProperty = vLineActor.GetProperty();

		lineProperty.SetLineWidth((float)getRenderAttr().lineWidth());
		if (hasProfileMode() == true)
			lineProperty.SetLineWidth(3.0f);

		activationPainter = new VtkControlPointPainter();
		vLineActivationActor = new vtkActor();
		vtkProperty lineActivationProperty = vLineActivationActor.GetProperty();
		lineActivationProperty.SetColor(1.0, 0.0, 0.0);
		lineActivationProperty.SetPointSize(7.0f);

		// Initialize an empty polydata for resetting
		vEmptyPD = VtkUtil.formEmptyPolyData();

		vLinesRegPD = new vtkPolyData();
		vLinesDecPD = new vtkPolyData();
		vLinesRegPD.DeepCopy(vEmptyPD);
		vLinesDecPD.DeepCopy(vEmptyPD);

		vActivationPD = new vtkPolyData();
		vActivationPD.DeepCopy(vEmptyPD);

		vLineMapperRegPDM = new vtkPolyDataMapper();
		vLineMapperDecPDM = new vtkPolyDataMapper();

		vLineActivationMapperPDM = new vtkPolyDataMapper();
		vLineActivationMapperPDM.SetInputData(vActivationPD);
		vLineActivationMapperPDM.Update();

		vLineActivationActor.SetMapper(vLineActivationMapperPDM);
		vLineActivationActor.Modified();
	}

	/** Simplified Constructor */
	public LineModel(SceneChangeNotifier aSceneChangeNotifier, StatusNotifier aStatusNotifier,
			PolyhedralModel aSmallBody)
	{
		this(aSceneChangeNotifier, aStatusNotifier, aSmallBody, PolyLineMode.DEFAULT);
	}

	@Override
	public G1 addItemWithControlPoints(int aId, List<Vector3D> aControlPointL)
	{
		// Create the item
		List<LatLon> tmpLatLonL = ControlPointUtil.convertToLatLonList(aControlPointL);
		G1 retItem = (G1) new PolyLine(aId, null, tmpLatLonL);
		retItem.setColor(spawnColor);

		// Install the item
		List<G1> tmpL = new ArrayList<>(getAllItems());
		tmpL.add(retItem);
		setAllItems(tmpL);

		return retItem;
	}

	public String getType()
	{
		return LINES;
	}

	/**
	 * Returns the ControlPoint painter
	 */
	public VtkControlPointPainter getControlPointPainter()
	{
		return activationPainter;
	}

	/**
	 * Return all of the XYZ points for the specified item.
	 * <P>
	 * The returned XYZ points do not necessarily correspond to the control points.
	 * The number of points returned should be at a minimum equal to the number of
	 * control points.
	 */
	public ImmutableList<Vector3D> getXyzPointsFor(G1 aItem)
	{
		VtkPolyLinePainter<G1> tmpPainter = getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();
		return tmpPainter.getXyzPointList();
	}

	/**
	 * Method to send out notification of the a change in the state of the
	 * activation painter.
	 * <P>
	 * TODO: This method exists due to the poorly coupled design between LineModel
	 * and it's "ControlPointPicker".
	 */
	public void notifyModelChanged()
	{
		notifyVtkStateChange();

		if (activatedLine != null)
			notifyItemsMutated(ImmutableList.of(activatedLine));
	}

	/**
	 * Sets the color for which newly created items will be set to.
	 */
	public void setSpawnColor(Color aColor)
	{
		spawnColor = aColor;
	}

	@Override
	public void setAllItems(Collection<G1> aItemC)
	{
		// Clear relevant state vars
		propM = new HashMap<>();

		super.setAllItems(aItemC);

		updateLineActivation();
	}

	@Override
	protected void updatePolyData()
	{
		vLinesRegPD.DeepCopy(vEmptyPD);
		vtkPoints points = vLinesRegPD.GetPoints();
		vtkCellArray lineCells = vLinesRegPD.GetLines();
		vtkCellData cellData = vLinesRegPD.GetCellData();
		vtkUnsignedCharArray colors = (vtkUnsignedCharArray) cellData.GetScalars();

		List<G1> tmpL = getAllItems();

		int c = 0;
		for (G1 aItem : tmpL)
		{
			// Ensure the painters are synchronized
			VtkCompositePainter<?, VtkPolyLinePainter<G1>> tmpPainter = getOrCreateVtkPainterFor(aItem, refSmallBody);
			tmpPainter.vtkUpdateState();

			List<Vector3D> xyzPointL = tmpPainter.getMainPainter().getXyzPointList();
			int size = xyzPointL.size();
			if (mode == PolyLineMode.CLOSED && size > 2)
				vIdRegIL.SetNumberOfIds(size + 1);
			else
				vIdRegIL.SetNumberOfIds(size);

			int startId = 0;
			for (int i = 0; i < size; ++i)
			{
				if (i == 0)
					startId = c;

				points.InsertNextPoint(xyzPointL.get(i).toArray());
				if (aItem.getVisible() == false)
					vIdRegIL.SetId(i, 0); // set to degenerate line if hidden
				else
					vIdRegIL.SetId(i, c);
				++c;
			}

			if (mode == PolyLineMode.CLOSED && size > 2)
			{
				if (aItem.getVisible() == false)
					vIdRegIL.SetId(size, 0);
				else
					vIdRegIL.SetId(size, startId);
			}

			Color tmpColor = getDrawColor(aItem);
			lineCells.InsertNextCell(vIdRegIL);
			colors.InsertNextTuple4(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), 255);
		}

		// Repeat for decimated data
		vLinesDecPD.DeepCopy(vEmptyPD);
		vtkPoints decimatedPoints = vLinesDecPD.GetPoints();
		vtkCellArray decimatedLineCells = vLinesDecPD.GetLines();
		vtkCellData decimatedCellData = vLinesDecPD.GetCellData();
		vtkUnsignedCharArray decimatedColors = (vtkUnsignedCharArray) decimatedCellData.GetScalars();

		c = 0;
		int drawId = 0;
		for (G1 aItem : tmpL)
		{
			VtkPolyLinePainter<?> tmpPainter = getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();
			tmpPainter.setVtkDrawId(drawId);
			drawId++;

			List<Integer> controlPointIdL = tmpPainter.getControlPointIdList();
			List<Vector3D> xyzPointL = tmpPainter.getXyzPointList();

			int size = controlPointIdL.size();
			// int size = lin.xyzPointList.size();
			if (mode == PolyLineMode.CLOSED && size > 2)
				vIdDecIL.SetNumberOfIds(size + 1);
			else
				vIdDecIL.SetNumberOfIds(size);

			int startId = 0;
			for (int i = 0; i < size; ++i)
			{
				if (i == 0)
					startId = c;

				decimatedPoints.InsertNextPoint(xyzPointL.get(controlPointIdL.get(i)).toArray());
				if (aItem.getVisible() == false)
					vIdDecIL.SetId(i, 0); // set to degenerate line if hidden
				else
					vIdDecIL.SetId(i, c);
				++c;

			}

			if (mode == PolyLineMode.CLOSED && size > 2)
			{
				if (aItem.getVisible() == false)
					vIdDecIL.SetId(size, 0);
				else
					vIdDecIL.SetId(size, startId);
			}

			Color tmpColor = getDrawColor(aItem);
			decimatedLineCells.InsertNextCell(vIdDecIL);
			decimatedColors.InsertNextTuple4(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), 255);
		}

		// Setup mapper, actor, etc.
		double offset = getOffset();
		refSmallBody.shiftPolyLineInNormalDirection(vLinesRegPD, offset);
		refSmallBody.shiftPolyLineInNormalDirection(vLinesDecPD, offset);

		vLineMapperRegPDM.SetInputData(vLinesRegPD);
		vLineMapperDecPDM.SetInputData(vLinesDecPD);
		vLineMapperRegPDM.Update();
		vLineMapperDecPDM.Update();

		vLineActor.setDefaultMapper(vLineMapperRegPDM);
		vLineActor.setLodMapper(LodMode.MaxQuality, vLineMapperRegPDM);
		vLineActor.setLodMapper(LodMode.MaxSpeed, vLineMapperDecPDM);
		vLineActor.Modified();

		notifyVtkStateChange();
	}

	@Override
	public List<vtkProp> getProps()
	{
		List<vtkProp> retL = new ArrayList<>();

		retL = new ArrayList<>();

		retL.add(vLineActor);

		for (G1 aItem : getAllItems())
		{
			VtkLabelPainter<?> tmpPainter = getVtkTextPainter(aItem);
			if (tmpPainter == null)
				continue;

			vtkProp tmpProp = tmpPainter.getActor();
			tmpPainter.vtkUpdateState();
			if (tmpProp != null)
				retL.add(tmpProp);
		}

		if (activatedLine != null)
			retL.add(vLineActivationActor);

		if (activationPainter.getPoints().size() > 0)
			retL.add(activationPainter.getActor());

		return retL;
	}

	@Override
	public void addControlPoint(int aIdx, Vector3D aPoint)
	{
		// Bail if no activated line
		if (activatedLine == null)
			return;

		// Bail if we have reached the maximum number of control points
		G1 tmpLine = activatedLine;
		if (tmpLine.getControlPoints().size() == maximumVerticesPerLine)
			return;

		// Clear out cache vars
		propM.remove(tmpLine);

		// Install the control point
		LatLon tmpLL = MathUtil.reclat(aPoint.toArray());
		tmpLine.addControlPoint(aIdx, tmpLL);

		// Delegate VTK specific update
		VtkPolyLinePainter<?> tmpPainter = getVtkMainPainter(tmpLine);
		if (tmpPainter != null)
			tmpPainter.addControlPoint(aIdx, aPoint);

		updatePolyData();

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
		notifyItemsMutated(ImmutableList.of(tmpLine));
	}

	@Override
	public void delControlPoint(int aIdx)
	{
		// Bail if we are not provided a valid index
		G1 tmpLine = activatedLine;
		if (aIdx < 0 || aIdx >= tmpLine.getControlPoints().size())
			return;

		// Remove the item if there will no longer be enough points to
		// provide a valid definition of the structure
		int numPts = tmpLine.getControlPoints().size();
		if (getNumPointsNeededForNewItem() == numPts)
		{
			List<G1> tmpL = ImmutableList.of(tmpLine);
			removeItems(tmpL);

			setActivatedItem(null);

			return;
		}

		// Clear out cache vars
		propM.remove(tmpLine);

		// Remove the control point
		tmpLine.delControlPoint(aIdx);

		// Delegate VTK specific update
		VtkPolyLinePainter<?> tmpPainter = getVtkMainPainter(tmpLine);
		if (tmpPainter != null)
			tmpPainter.delControlPoint(aIdx);

		// Update the activated control point index (if necessary)
		if (aIdx <= activatedControlPointIdx)
		{
			activatedControlPointIdx--;
			if (activatedControlPointIdx < 0 && tmpLine.getControlPoints().size() > 0)
				activatedControlPointIdx = 0;
		}

		updatePolyData();

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
		notifyItemsMutated(ImmutableList.of(tmpLine));
	}

	@Override
	public void moveControlPoint(int aIdx, Vector3D aPoint, boolean aIsFinal)
	{
		// Just perform a (quick) update of the the control point
		// The structure will not be updated for performance reasons.
		if (aIsFinal == false)
		{
			// Retrieve the cellId corresponding to the specified control point index
			int tmpCellId = getCellIdForControlPoint(aIdx);

			vtkPoints points = vActivationPD.GetPoints();
			points.SetPoint(tmpCellId, aPoint.toArray());
			vActivationPD.Modified();

			notifyListeners(this, ItemEventType.ItemsMutated);
			notifyVtkStateChange();
			return;
		}

		// Delegate
		updateControlPoint(aIdx, aPoint);
	}

	@Override
	public G1 getActivatedItem()
	{
		return activatedLine;
	}

	@Override
	public int getNumPointsNeededForNewItem()
	{
		if (mode == PolyLineMode.CLOSED)
			return 3;

		return 2;
	}

	@Override
	public vtkActor getVtkControlPointActor()
	{
		return vLineActivationActor;
	}

	@Override
	public vtkActor getVtkItemActor()
	{
		return vLineActor;
	}

	@Override
	public void removeItems(Collection<G1> aItemC)
	{
		super.removeItems(aItemC);

		if (aItemC.contains(activatedLine) == true)
			setActivatedItem(null);
	}

	protected void updateLineActivation()
	{
		if (hasProfileMode())
		{
			vActivationPD.DeepCopy(vEmptyPD);
			vtkPoints points = vActivationPD.GetPoints();
			vtkCellArray vert = vActivationPD.GetVerts();
			vtkCellData cellData = vActivationPD.GetCellData();
			vtkUnsignedCharArray colors = (vtkUnsignedCharArray) cellData.GetScalars();

			vIdRegIL.SetNumberOfIds(1);

			int count = 0;
			for (G1 aItem : getAllItems())
			{
				VtkPolyLinePainter<?> tmpPainter = getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();
				List<Integer> controlPointIdL = tmpPainter.getControlPointIdList();
				List<Vector3D> xyzPointL = tmpPainter.getXyzPointList();

				for (int i = 0; i < controlPointIdL.size(); ++i)
				{
					int idx = controlPointIdL.get(i);

					points.InsertNextPoint(xyzPointL.get(idx).toArray());
					vIdRegIL.SetId(0, count++);
					vert.InsertNextCell(vIdRegIL);

					Color tmpColor = redColor;
					if (i == 0)
						tmpColor = greenColor;
					colors.InsertNextTuple4(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), tmpColor.getAlpha());
				}
			}

			refSmallBody.shiftPolyLineInNormalDirection(vActivationPD, refSmallBody.getMinShiftAmount());
		}
		else
		{
			if (activatedLine == null)
			{
				notifyVtkStateChange();
				return;
			}

			VtkPolyLinePainter<?> tmpPainter = getOrCreateVtkPainterFor(activatedLine, refSmallBody).getMainPainter();
			List<Integer> controlPointIdL = tmpPainter.getControlPointIdList();
			List<Vector3D> xyzPointL = tmpPainter.getXyzPointList();

			vActivationPD.DeepCopy(vEmptyPD);
			vtkPoints points = vActivationPD.GetPoints();
			vtkCellArray vert = vActivationPD.GetVerts();
			vtkCellData cellData = vActivationPD.GetCellData();
			vtkUnsignedCharArray colors = (vtkUnsignedCharArray) cellData.GetScalars();

			int numPoints = controlPointIdL.size();

			points.SetNumberOfPoints(numPoints);

			vIdRegIL.SetNumberOfIds(1);

			for (int i = 0; i < numPoints; ++i)
			{
				int idx = controlPointIdL.get(i);
				points.SetPoint(i, xyzPointL.get(idx).toArray());
				vIdRegIL.SetId(0, i);
				vert.InsertNextCell(vIdRegIL);

				Color tmpColor = redColor;
				if (i == activatedControlPointIdx)
					tmpColor = blueColor;
				colors.InsertNextTuple4(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), tmpColor.getAlpha());
			}

			refSmallBody.shiftPolyLineInNormalDirection(vActivationPD, getOffset());
		}

		notifyVtkStateChange();
	}

	@Override
	public void setActivatedItem(G1 aItem)
	{
		if (aItem == activatedLine)
			return;

		activatedLine = aItem;

		activatedControlPointIdx = -1;
		if (aItem != null)
			activatedControlPointIdx = aItem.getControlPoints().size() - 1;

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public int getActivatedControlPoint()
	{
		return activatedControlPointIdx;
	}

	@Override
	public void setActivatedControlPoint(int aIdx)
	{
		activatedControlPointIdx = aIdx;

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public G1 getItemFromCellId(int aCellId, vtkProp aProp)
	{
		if (aProp == vLineActor)
			return getItem(aCellId);
		else if (aProp == vLineActivationActor)
			return activatedLine;
		else
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

	public void setMaximumVerticesPerLine(int max)
	{
		maximumVerticesPerLine = max;
	}

	@Override
	public boolean hasProfileMode()
	{
		return mode == PolyLineMode.PROFILE;
	}

	@Override
	public int getControlPointIndexFromActivationCellId(int aCellId)
	{
		// NON-Profile Mode: Only one polygon/line can be selected and thus
		// aCellId corresponds to the actual vertex index (of the activated line)
		if (hasProfileMode() == false)
			return aCellId;

		// Iterate through all the lines (which are all activated) and determine
		// which vertex (of the corresponding line) aCellId corresponds to
		for (G1 aItem : getAllItems())
		{
			// Skip to next if item has not been rendered
			VtkPolyLinePainter<G1> tmpPainter = getVtkMainPainter(aItem);
			if (tmpPainter == null)
				continue;

			int size = tmpPainter.getNumControlPointIds();

			if (aCellId == 0)
				return 0;
			else if (aCellId == 1 && size == 2)
				return 1;
			else
				aCellId -= size;
		}

		return -1;
	}

	@Override
	public G1 getItemFromActivationCellId(int aCellId)
	{
		// NON-Profile Mode: Only one polygon/line can be selected and thus
		// aCellId corresponds to the current activated structure
		if (hasProfileMode() == false)
			return activatedLine;

		int count = 0;
		for (G1 aItem : getAllItems())
		{
			// Skip to next if item has not been rendered
			VtkPolyLinePainter<G1> tmpPainter = getVtkMainPainter(aItem);
			if (tmpPainter == null)
				continue;

			int size = tmpPainter.getNumControlPointIds();
			count += size;
			if (aCellId < count)
				return aItem;
		}

		return null;
	}

	@Override
	public void setOffset(double aOffset)
	{
		var tmpDefaultAttr = getRenderAttr().withRadialOffset(aOffset);
		setRenderAttr(tmpDefaultAttr);

		// Clear out cache vars
		propM.clear();

		updatePolyData();
		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public double getOffset()
	{
		return getRenderAttr().radialOffset();
	}

	@Override
	public Vector3D getCentroid(G1 aItem)
	{
		Vector3D tmpCentroid = propM.get(aItem);
		if (tmpCentroid != null)
			return tmpCentroid;

		tmpCentroid = ControlPointUtil.calcCentroidOnBody(refSmallBody, aItem.getControlPoints());
		propM.put(aItem, tmpCentroid);

		return tmpCentroid;
	}

	/**
	 * {@inheritDoc}
	 * <P>
	 * The center is defined as the mean of the control points.
	 */
	@Override
	public Vector3D getCenter(G1 aItem)
	{
		return getCentroid(aItem);
	}

	/**
	 * {@inheritDoc}
	 * <P>
	 * The diameter is defined as twice the distance from the centroid to the
	 * farthest control point from the centroid.
	 */
	@Override
	public double getDiameter(G1 aItem)
	{
		// Delegate
		return ControlPointUtil.calcSizeOnBody(refSmallBody, aItem.getControlPoints());
	}

	@Override
	protected VtkPolyLinePainter<G1> createPainter(G1 aItem)
	{
		return new VtkPolyLinePainter<>(refSmallBody, aItem);
	}

	/**
	 * Helper method that returns the color for which the item should be drawn.
	 */
	@Override
	protected Color getDrawColor(G1 aItem)
	{
		// Profile mode does not render selected lines in a different color
		if (mode == PolyLineMode.PROFILE)
			return aItem.getColor();

		return super.getDrawColor(aItem);
	}

	@Override
	protected void updateVtkColorsFor(Collection<G1> aItemC, boolean aSendNotification)
	{
		// Gather VTK vars of interest
		vtkCellData regCD = vLinesRegPD.GetCellData();
		vtkUnsignedCharArray regColorUCA = (vtkUnsignedCharArray) regCD.GetScalars();

		vtkCellData decCD = vLinesDecPD.GetCellData();
		vtkUnsignedCharArray decColorUCA = (vtkUnsignedCharArray) decCD.GetScalars();

		// Update internal VTK state
		for (var aItem : aItemC)
		{
			// Skip to next if not visible
			if (aItem.getVisible() == false)
				continue;

			// Skip to next if not rendered
			VtkPolyLinePainter<?> tmpPainter = getVtkMainPainter(aItem);
			if (tmpPainter == null)
				continue;

			// Skip to next as VTK draw state has not been initialized
			if (tmpPainter.getVtkDrawId() == -1)
				continue;

			// Update the color related state
			Color tmpColor = getDrawColor(aItem);
			int tmpId = tmpPainter.getVtkDrawId();
			VtkUtil.setColorOnUCA4(regColorUCA, tmpId, tmpColor);
			VtkUtil.setColorOnUCA4(decColorUCA, tmpId, tmpColor);
		}

		regColorUCA.Modified();
		decColorUCA.Modified();

		notifyVtkStateChange();
	}

	/**
	 * Returns the cellId corresponding to the specified control point index.
	 * <P>
	 * The returned cellId is associated with the activationActor.
	 */
	private int getCellIdForControlPoint(int aIdx)
	{
		int retCellId = aIdx;
		if (hasProfileMode() == false)
			return retCellId;

		// Need to iterate through all items to locate the relevant cellId
		retCellId = 0;
		for (PolyLine aItem : getAllItems())
		{
			int size = aItem.getControlPoints().size();
			if (aItem == activatedLine)
			{
				retCellId += aIdx;
				break;
			}

			retCellId += size;
		}

		return retCellId;
	}

	// TODO: Add comments
	private void updateControlPoint(int aIdx, Vector3D aPoint)
	{
		LatLon ll = MathUtil.reclat(aPoint.toArray());
		activatedLine.setControlPoint(aIdx, ll);

		// Delegate VTK specific update
		VtkPolyLinePainter<?> tmpPainter = getVtkMainPainter(activatedLine);
		if (tmpPainter != null)
			tmpPainter.updateControlPoint(aIdx, aPoint);

		// Clear out cache vars
		propM.remove(activatedLine);

		updatePolyData();

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
		if (activatedLine != null)
			notifyItemsMutated(ImmutableList.of(activatedLine));
	}

	// TODO: Add comments
	private void redrawAllStructures()
	{
		for (G1 aItem : getAllItems())
		{
			// Update the control points
			List<LatLon> tmpControlPointL = aItem.getControlPoints();
			tmpControlPointL = ControlPointUtil.shiftControlPointsToNearestPointOnBody(refSmallBody, tmpControlPointL);
			aItem.setControlPoints(tmpControlPointL);

			VtkPolyLinePainter<?> tmpPainter = getVtkMainPainter(aItem);
			if (tmpPainter != null)
				tmpPainter.vtkMarkStale();
		}
		notifyListeners(this, ItemEventType.ItemsMutated);

		updatePolyData();

		updateLineActivation();
	}

}
