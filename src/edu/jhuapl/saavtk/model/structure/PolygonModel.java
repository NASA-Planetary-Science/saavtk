package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.jhuapl.saavtk.model.FacetColoringData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkPoints;
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
	private PolyhedralModel refSmallBodyModel;

	// State vars
	private double interiorOpacity = 0.3;
	private int maxPolygonId = 0;

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

	public PolygonModel(PolyhedralModel aSmallBodyModel)
	{
		super(aSmallBodyModel, Mode.CLOSED);

		refSmallBodyModel = aSmallBodyModel;

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
		vEmptyPD = formEmptyPolyData();
	}

	@Override
	protected String getType()
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
				Polygon polygon = getStructure(i);
				polygon.updateInteriorPolydata(refSmallBodyModel);
				vtkPolyData polyRegPD = polygon.vInteriorRegPD;
				vtkPolyData polyDecPD = polygon.vInteriorDecPD;

				if (polygon.getVisible() == false)
				{
					polyRegPD = vEmptyPD;
					polyDecPD = vEmptyPD;
				}

				if (polyRegPD != null)
					vInteriorFilterRegAPD.SetInputDataByNumber(i, polyRegPD);

				if (polyDecPD != null)
					vInteriorFilterDecAPD.SetInputDataByNumber(i, polyDecPD);
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
				Polygon tmpItem = getStructure(i);
				Color tmpColor = tmpItem.getColor();
				if (getSelectedItems().contains(tmpItem) == true)
					tmpColor = getCommonData().getSelectionColor();

				IdPair range = this.getCellIdRangeOfPolygon(i);
				for (int j = range.id1; j < range.id2; ++j)
					VtkUtil.setColorOnUCA3(vInteriorColorsRegUCA, j, tmpColor);

				range = this.getCellIdRangeOfDecimatedPolygon(i);
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
	public List<vtkProp> getProps()
	{
		List<vtkProp> retL = new ArrayList<>();
		retL.add(vInteriorActor);

		retL.addAll(super.getProps());
		return retL;
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		Polygon tmpItem = getStructureFromCellId(cellId, prop);
		if (tmpItem == null)
			return "";

		return tmpItem.getClickStatusBarText();
	}

	@Override
	public void updateActivatedStructureVertex(int vertexId, double[] newPoint)
	{
		Polygon pol = getActivatedStructure();
		pol.setShowInterior(refSmallBodyModel, false);

		super.updateActivatedStructureVertex(vertexId, newPoint);
	}

	@Override
	public void insertVertexIntoActivatedStructure(double[] newPoint)
	{
		Polygon pol = getActivatedStructure();
		pol.setShowInterior(refSmallBodyModel, false);

		super.insertVertexIntoActivatedStructure(newPoint);
	}

	@Override
	public void removeCurrentStructureVertex()
	{
		Polygon pol = getActivatedStructure();
		pol.setShowInterior(refSmallBodyModel, false);

		super.removeCurrentStructureVertex();
	}

	@Override
	public Polygon getStructureFromCellId(int aCellId, vtkProp aProp)
	{
		// A picker picking the actor of this model will return a cellId. But since
		// there are many polygons, we need to be able to figure out which polygon was
		// picked.

		if (aProp == vInteriorActor)
		{
			int cellCnt = 0;
			for (Polygon aItem : getAllItems())
			{
				cellCnt += aItem.vInteriorRegPD.GetNumberOfCells();
				if (aCellId < cellCnt)
					return aItem;
			}
		}

		return super.getStructureFromCellId(aCellId, aProp);
	}

	private IdPair getCellIdRangeOfPolygon(int polygonId)
	{
		int startCell = 0;
		for (int i = 0; i < polygonId; ++i)
		{
			startCell += getStructure(i).vInteriorRegPD.GetNumberOfCells();
		}

		int endCell = startCell;
		endCell += getStructure(polygonId).vInteriorRegPD.GetNumberOfCells();

		return new IdPair(startCell, endCell);
	}

	private IdPair getCellIdRangeOfDecimatedPolygon(int polygonId)
	{
		int startCell = 0;
		for (int i = 0; i < polygonId; ++i)
		{
			startCell += getStructure(i).vInteriorDecPD.GetNumberOfCells();
		}

		int endCell = startCell;
		endCell += getStructure(polygonId).vInteriorDecPD.GetNumberOfCells();

		return new IdPair(startCell, endCell);
	}

	@Override
	public void setVisible(boolean b)
	{
		vInteriorActor.SetVisibility(b ? 1 : 0);
		super.setVisible(b);
	}

	@Override
	public void savePlateDataInsideStructure(Polygon aItem, File aFile) throws IOException
	{
		if (aItem.isShowInterior())
		{
			vtkPolyData polydata = aItem.vInteriorRegPD;
			refSmallBodyModel.savePlateDataInsidePolydata(polydata, aFile);
		}
		else
		{
			aItem.setShowInterior(refSmallBodyModel, true);

			vtkPolyData polydata = aItem.vInteriorRegPD;
			refSmallBodyModel.savePlateDataInsidePolydata(polydata, aFile);

			aItem.setShowInterior(refSmallBodyModel, false);
		}
	}

	@Override
	public void savePlateDataInsideStructure(Collection<Polygon> aItemC, File aFile) throws IOException
	{

		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (Polygon aItem : aItemC)
		{
			aItem.setShowInterior(refSmallBodyModel, true);
			appendFilter.AddInputData(aItem.vInteriorRegPD);
			appendFilter.Update();
			aItem.setShowInterior(refSmallBodyModel, false);
		}
		refSmallBodyModel.savePlateDataInsidePolydata(appendFilter.GetOutput(), aFile);
	}

	@Override
	public FacetColoringData[] getPlateDataInsideStructure(Polygon aItem)
	{
		if (aItem.isShowInterior())
		{
			vtkPolyData polydata = aItem.vInteriorRegPD;
			return refSmallBodyModel.getPlateDataInsidePolydata(polydata);
		}
		else
		{
			aItem.setShowInterior(refSmallBodyModel, true);

			vtkPolyData polydata = aItem.vInteriorRegPD;
			FacetColoringData[] data = refSmallBodyModel.getPlateDataInsidePolydata(polydata);
			aItem.setShowInterior(refSmallBodyModel, false);
			return data;
		}
	}

	@Override
	public FacetColoringData[] getPlateDataInsideStructure(Collection<Polygon> aItemC)
	{
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (Polygon aItem : aItemC)
		{
			aItem.setShowInterior(refSmallBodyModel, true);
			appendFilter.AddInputData(aItem.vInteriorRegPD);
			appendFilter.Update();
			aItem.setShowInterior(refSmallBodyModel, false);
		}

		FacetColoringData[] data = refSmallBodyModel.getPlateDataInsidePolydata(appendFilter.GetOutput());
		return data;
	}

	@Override
	public void setShowStructuresInterior(Collection<Polygon> aItemC, boolean show)
	{
		for (Polygon aItem : aItemC)
		{
			if (aItem.isShowInterior() != show)
				aItem.setShowInterior(refSmallBodyModel, show);
		}

		updatePolyData();
	}

	@Override
	public boolean isShowStructureInterior(Polygon aItem)
	{
		return aItem.isShowInterior();
	}

	@Override
	protected Polygon createStructure()
	{
		return new Polygon(++maxPolygonId);
	}

	/**
	 * Utility helper method to form an empty vtkPolyData
	 */
	private static vtkPolyData formEmptyPolyData()
	{
		vtkPolyData vEmptyPD = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
		colors.SetNumberOfComponents(4);
		vEmptyPD.SetPoints(points);
		vEmptyPD.SetLines(cells);
		vEmptyPD.SetVerts(cells);
		vtkCellData cellData = vEmptyPD.GetCellData();
		cellData.SetScalars(colors);

		return vEmptyPD;
	}

}
