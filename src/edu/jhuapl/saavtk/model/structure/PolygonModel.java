package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.PolyLineMode;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.util.ControlPointUtil;
import edu.jhuapl.saavtk.structure.vtk.VtkPolyLinePainter;
import edu.jhuapl.saavtk.structure.vtk.VtkPolygonPainter;
import edu.jhuapl.saavtk.structure.vtk.VtkUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkCellData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkUnsignedCharArray;

/**
 * Model of polygon structures drawn on a body.
 */
public class PolygonModel extends LineModel<Polygon>
{
	// Ref vars
	private PolyhedralModel refSmallBody;

	// State vars
	private double interiorOpacity = 0.3;

	// VTK vars
	private final vtkPolyData vInteriorRegPD;
	private final vtkPolyData vInteriorDecPD;
	private final vtkAppendPolyData vInteriorFilterRegAPD;
	private final vtkAppendPolyData vInteriorFilterDecAPD;
	private final vtkPolyDataMapper vInteriorMapperRegPDM;
	private final vtkPolyDataMapper vInteriorMapperDecPDM;
	private final vtkActor vInteriorActor;

	private final vtkUnsignedCharArray vInteriorColorsRegUCA;
	private final vtkUnsignedCharArray vInteriorColorsDecUCA;

	private final vtkPolyData vEmptyPD;

	private static final String POLYGONS = "polygons";

	/**
	 * Standard Constructor
	 */
	public PolygonModel(PolyhedralModel aSmallBodyModel)
	{
		super(aSmallBodyModel, PolyLineMode.CLOSED);

		refSmallBody = aSmallBodyModel;

		vInteriorColorsRegUCA = new vtkUnsignedCharArray();
		vInteriorColorsDecUCA = new vtkUnsignedCharArray();
		vInteriorColorsRegUCA.SetNumberOfComponents(3);
		vInteriorColorsDecUCA.SetNumberOfComponents(3);

		vInteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
		vInteriorFilterRegAPD = new vtkAppendPolyData();
		vInteriorFilterDecAPD = new vtkAppendPolyData();
		vInteriorFilterRegAPD.UserManagedInputsOn();
		vInteriorFilterDecAPD.UserManagedInputsOn();
		vInteriorMapperRegPDM = new vtkPolyDataMapper();
		vInteriorMapperDecPDM = new vtkPolyDataMapper();
		vInteriorActor = new SaavtkLODActor();
		vtkProperty interiorProperty = vInteriorActor.GetProperty();
		interiorProperty.LightingOff();
		interiorProperty.SetOpacity(interiorOpacity);

		// Initialize an empty polydata for resetting
		vEmptyPD = VtkUtil.formEmptyPolyData();
	}

	/**
	 * Returns if the specified polygons interior is being shown.
	 */
	public boolean getShowInterior(Polygon aItem)
	{
		return aItem.getShowInterior();
	}

	/**
	 * Method that sets whether the list of polygons should show their interior.
	 */
	public void setShowInterior(Collection<Polygon> aItemC, boolean aIsShown)
	{
		for (Polygon aItem : aItemC)
			configurePolygonInterior(aItem, aIsShown);

		updatePolyData();
	}

	@Override
	public Polygon addItemWithControlPoints(int aId, List<Vector3D> aControlPointL)
	{
		// Create the item
		List<LatLon> tmpLatLonL = ControlPointUtil.convertToLatLonList(aControlPointL);
		Polygon retItem = new Polygon(aId, null, tmpLatLonL);

		// Install the item
		List<Polygon> fullL = new ArrayList<>(getAllItems());
		fullL.add(retItem);
		setAllItems(fullL);

		return retItem;
	}

	@Override
	public String getType()
	{
		return POLYGONS;
	}

	@Override
	protected void updatePolyData()
	{
		super.updatePolyData();

		int numberOfStructures = getNumItems();
		if (numberOfStructures > 0)
		{
			vInteriorFilterRegAPD.SetNumberOfInputs(numberOfStructures);
			vInteriorFilterDecAPD.SetNumberOfInputs(numberOfStructures);

			for (int i = 0; i < numberOfStructures; ++i)
			{
				Polygon polygon = getItem(i);

				VtkPolygonPainter tmpPainter = getOrCreateMainPainter(polygon);

				tmpPainter.updateInteriorPolydata();

				vtkPolyData tmpInteriorRegPD = tmpPainter.getVtkInteriorRegPD();
				vtkPolyData tmpInteriorDecPD = tmpPainter.getVtkInteriorDecPD();

				if (polygon.getVisible() == false)
				{
					tmpInteriorRegPD = vEmptyPD;
					tmpInteriorDecPD = vEmptyPD;
				}

				if (tmpInteriorRegPD != null)
					vInteriorFilterRegAPD.SetInputDataByNumber(i, tmpInteriorRegPD);

				if (tmpInteriorDecPD != null)
					vInteriorFilterDecAPD.SetInputDataByNumber(i, tmpInteriorDecPD);
			}

			vInteriorFilterRegAPD.Update();
			vInteriorFilterDecAPD.Update();

			vtkPolyData interiorAppendFilterOutput = vInteriorFilterRegAPD.GetOutput();
			vtkPolyData decimatedInteriorAppendFilterOutput = vInteriorFilterDecAPD.GetOutput();
			vInteriorRegPD.DeepCopy(interiorAppendFilterOutput);
			vInteriorDecPD.DeepCopy(decimatedInteriorAppendFilterOutput);

			PolyDataUtil.shiftPolyDataInNormalDirection(vInteriorRegPD, getOffset());
			PolyDataUtil.shiftPolyDataInNormalDirection(vInteriorDecPD, getOffset());

			vInteriorColorsRegUCA.SetNumberOfTuples(vInteriorRegPD.GetNumberOfCells());
			vInteriorColorsDecUCA.SetNumberOfTuples(vInteriorDecPD.GetNumberOfCells());
			for (int i = 0; i < numberOfStructures; ++i)
			{
				Polygon tmpItem = getItem(i);
				Color tmpColor = tmpItem.getColor();
				if (getSelectedItems().contains(tmpItem) == true)
					tmpColor = getCommonData().getSelectionColor();

				IdPair range = getCellIdRangeOfPolygon(i);
				for (int j = range.id1; j < range.id2; ++j)
					VtkUtil.setColorOnUCA3(vInteriorColorsRegUCA, j, tmpColor);

				range = getCellIdRangeOfDecimatedPolygon(i);
				for (int j = range.id1; j < range.id2; ++j)
					VtkUtil.setColorOnUCA3(vInteriorColorsDecUCA, j, tmpColor);
			}
			vtkCellData interiorCellData = vInteriorRegPD.GetCellData();
			vtkCellData decimatedInteriorCellData = vInteriorDecPD.GetCellData();

			interiorCellData.SetScalars(vInteriorColorsRegUCA);
			decimatedInteriorCellData.SetScalars(vInteriorColorsDecUCA);

			interiorAppendFilterOutput.Delete();
			decimatedInteriorAppendFilterOutput.Delete();
			interiorCellData.Delete();
			decimatedInteriorCellData.Delete();
		}
		else
		{
			vInteriorRegPD.DeepCopy(vEmptyPD);
			vInteriorDecPD.DeepCopy(vEmptyPD);
		}

		vInteriorMapperRegPDM.SetInputData(vInteriorRegPD);
		vInteriorMapperRegPDM.Update();

		vInteriorMapperDecPDM.SetInputData(vInteriorDecPD);
		vInteriorMapperDecPDM.Update();

		vInteriorActor.SetMapper(vInteriorMapperRegPDM);
		((SaavtkLODActor) vInteriorActor).setLODMapper(vInteriorMapperDecPDM);
		vInteriorActor.Modified();

		// Notify model change listeners
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		// Bail if no item clicked on
		Polygon tmpItem = getItemFromCellId(cellId, prop);
		if (tmpItem == null)
			return "";

		// Bail if no associated painter
		VtkPolyLinePainter<?> tmpPainter = getVtkMainPainter(tmpItem);
		if (tmpPainter == null)
			return "";

		String retStr = "Polygon, Id = " + tmpItem.getId();
		retStr += ", Length = " + decimalFormatter.format(tmpItem.getPathLength()) + " km";
		retStr += ", Surface Area = " + decimalFormatter.format(tmpItem.getSurfaceArea()) + " km" + (char) 0x00B2;
		retStr += ", Number of Vertices = " + tmpItem.getControlPoints().size();
		return retStr;
	}

	@Override
	public List<vtkProp> getProps()
	{
		List<vtkProp> retL = new ArrayList<>();
		retL.add(vInteriorActor);

		retL.addAll(super.getProps());
		return retL;
	}

	@Override
	public void addControlPoint(int aIdx, Vector3D aPoint)
	{
		// Disable the polygon interior for performance reasons
		configurePolygonInterior(getActivatedItem(), false);

		super.addControlPoint(aIdx, aPoint);
	}

	@Override
	public void delControlPoint(int aIdx)
	{
		// Disable the polygon interior for performance reasons
		configurePolygonInterior(getActivatedItem(), false);

		super.delControlPoint(aIdx);
	}

	@Override
	public void moveControlPoint(int aIdx, Vector3D aPoint, boolean aIsFinal)
	{
		// Disable the polygon interior for performance reasons
		if (aIsFinal == true)
			configurePolygonInterior(getActivatedItem(), false);

		super.moveControlPoint(aIdx, aPoint, aIsFinal);
	}

	@Override
	public Polygon getItemFromCellId(int aCellId, vtkProp aProp)
	{
		// A picker picking the actor of this model will return a cellId. But since
		// there are many polygons, we need to be able to figure out which polygon was
		// picked.

		if (aProp == vInteriorActor)
		{
			int cellCnt = 0;
			for (Polygon aItem : getAllItems())
			{
				VtkPolygonPainter tmpPainter = getOrCreateMainPainter(aItem);
				vtkPolyData tmpInteriorRegPD = tmpPainter.getVtkInteriorRegPD();

				cellCnt += tmpInteriorRegPD.GetNumberOfCells();
				if (aCellId < cellCnt)
					return aItem;
			}
		}

		return super.getItemFromCellId(aCellId, aProp);
	}

	@Override
	public void setVisible(boolean b)
	{
		vInteriorActor.SetVisibility(b ? 1 : 0);
		super.setVisible(b);
	}

	/**
	 * Helper method to set whether the specified polygon interior is shown.
	 * <P>
	 * The associated VTK state will be updated.
	 */
	protected void configurePolygonInterior(Polygon aItem, boolean aIsShown)
	{
		// Bail if nothing changes
		if (aItem.getShowInterior() == aIsShown)
			return;

		// Retrieve the VTK painter
		VtkPolygonPainter tmpPainter = getOrCreateMainPainter(aItem);

		// Update the item and the associated painter
		aItem.setShowInterior(aIsShown);
		tmpPainter.setShowInterior(aIsShown);
	}

	/**
	 * Helper method that will return the proper VTK painter for the specified item.
	 * <P>
	 * If the painter does not exist then it will be instantiated.
	 */
	protected VtkPolygonPainter getOrCreateMainPainter(Polygon aItem)
	{
		return (VtkPolygonPainter) getOrCreateVtkPainterFor(aItem, refSmallBody).getMainPainter();
	}

	@Override
	protected VtkPolygonPainter createPainter(Polygon aItem)
	{
		return new VtkPolygonPainter(aItem, refSmallBody);
	}

	private IdPair getCellIdRangeOfPolygon(int polygonId)
	{
		int startCell = 0;
		for (int i = 0; i < polygonId; ++i)
		{
			VtkPolygonPainter tmpPainter = getOrCreateMainPainter(getItem(i));
			startCell += tmpPainter.getVtkInteriorRegPD().GetNumberOfCells();
		}

		int endCell = startCell;
		VtkPolygonPainter tmpPainter = getOrCreateMainPainter(getItem(polygonId));
		endCell += tmpPainter.getVtkInteriorRegPD().GetNumberOfCells();

		return new IdPair(startCell, endCell);
	}

	private IdPair getCellIdRangeOfDecimatedPolygon(int polygonId)
	{
		int startCell = 0;
		for (int i = 0; i < polygonId; ++i)
		{
			VtkPolygonPainter tmpPainter = getOrCreateMainPainter(getItem(i));
			startCell += tmpPainter.getVtkInteriorDecPD().GetNumberOfCells();
		}

		int endCell = startCell;
		VtkPolygonPainter tmpPainter = getOrCreateMainPainter(getItem(polygonId));
		endCell += tmpPainter.getVtkInteriorDecPD().GetNumberOfCells();

		return new IdPair(startCell, endCell);
	}

}
