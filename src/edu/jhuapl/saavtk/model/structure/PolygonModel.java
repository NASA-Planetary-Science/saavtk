package edu.jhuapl.saavtk.model.structure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.jhuapl.saavtk.model.FacetColoringData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
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
public class PolygonModel extends LineModel
{
	private vtkPolyData interiorPolyData;
	private vtkPolyData decimatedInteriorPolyData;
	private vtkAppendPolyData interiorAppendFilter;
	private vtkAppendPolyData decimatedInteriorAppendFilter;
	private vtkPolyDataMapper interiorMapper;
	private vtkPolyDataMapper decimatedInteriorMapper;
	private vtkActor interiorActor;

	private vtkUnsignedCharArray interiorColors;
	private vtkUnsignedCharArray decimatedInteriorColors;

	private List<vtkProp> actors = new ArrayList<>();

	private PolyhedralModel smallBodyModel;

	private double interiorOpacity = 0.3;
	private int maxPolygonId = 0;

	private vtkPolyData emptyPolyData;

	private static final String POLYGONS = "polygons";

	public PolygonModel(PolyhedralModel smallBodyModel)
	{
		super(smallBodyModel, Mode.CLOSED);

		this.smallBodyModel = smallBodyModel;

		interiorColors = new vtkUnsignedCharArray();
		decimatedInteriorColors = new vtkUnsignedCharArray();
		interiorColors.SetNumberOfComponents(3);
		decimatedInteriorColors.SetNumberOfComponents(3);

		interiorPolyData = new vtkPolyData();
		decimatedInteriorPolyData = new vtkPolyData();
		interiorAppendFilter = new vtkAppendPolyData();
		decimatedInteriorAppendFilter = new vtkAppendPolyData();
		interiorAppendFilter.UserManagedInputsOn();
		decimatedInteriorAppendFilter.UserManagedInputsOn();
		interiorMapper = new vtkPolyDataMapper();
		decimatedInteriorMapper = new vtkPolyDataMapper();
		interiorActor = new SaavtkLODActor();
		vtkProperty interiorProperty = interiorActor.GetProperty();
		interiorProperty.LightingOff();
		interiorProperty.SetOpacity(interiorOpacity);

		actors.add(interiorActor);

		// Initialize an empty polydata for resetting
		emptyPolyData = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
		colors.SetNumberOfComponents(4);
		emptyPolyData.SetPoints(points);
		emptyPolyData.SetLines(cells);
		emptyPolyData.SetVerts(cells);
		vtkCellData cellData = emptyPolyData.GetCellData();
		cellData.SetScalars(colors);
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

		int numberOfStructures = getNumberOfStructures();
		if (numberOfStructures > 0)
		{
			interiorAppendFilter.SetNumberOfInputs(numberOfStructures);
			decimatedInteriorAppendFilter.SetNumberOfInputs(numberOfStructures);

			for (int i = 0; i < numberOfStructures; ++i)
			{
				Polygon polygon = getPolygon(i);
				polygon.updateInteriorPolydata(smallBodyModel);
				vtkPolyData poly = polygon.interiorPolyData;
				vtkPolyData decimatedPoly = polygon.decimatedInteriorPolyData;

				if (polygon.getHidden())
				{
					poly = emptyPolyData;
					decimatedPoly = emptyPolyData;
				}

				if (poly != null)
					interiorAppendFilter.SetInputDataByNumber(i, poly);

				if (decimatedPoly != null)
					decimatedInteriorAppendFilter.SetInputDataByNumber(i, decimatedPoly);
			}

			interiorAppendFilter.Update();
			decimatedInteriorAppendFilter.Update();

			vtkPolyData interiorAppendFilterOutput = interiorAppendFilter.GetOutput();
			vtkPolyData decimatedInteriorAppendFilterOutput = decimatedInteriorAppendFilter.GetOutput();
			interiorPolyData.DeepCopy(interiorAppendFilterOutput);
			decimatedInteriorPolyData.DeepCopy(decimatedInteriorAppendFilterOutput);

			PolyDataUtil.shiftPolyDataInNormalDirection(interiorPolyData, getOffset());
			PolyDataUtil.shiftPolyDataInNormalDirection(decimatedInteriorPolyData, getOffset());

			interiorColors.SetNumberOfTuples(interiorPolyData.GetNumberOfCells());
			decimatedInteriorColors.SetNumberOfTuples(decimatedInteriorPolyData.GetNumberOfCells());
			for (int i = 0; i < numberOfStructures; ++i)
			{
				int[] color = getPolygon(i).getColor();

				if (Arrays.binarySearch(getSelectedStructures(), i) >= 0)
					color = getCommonData().getSelectionColor();

				IdPair range = this.getCellIdRangeOfPolygon(i);
				for (int j = range.id1; j < range.id2; ++j)
					interiorColors.SetTuple3(j, color[0], color[1], color[2]);

				range = this.getCellIdRangeOfDecimatedPolygon(i);
				for (int j = range.id1; j < range.id2; ++j)
					decimatedInteriorColors.SetTuple3(j, color[0], color[1], color[2]);
			}
			vtkCellData interiorCellData = interiorPolyData.GetCellData();
			vtkCellData decimatedInteriorCellData = decimatedInteriorPolyData.GetCellData();

			interiorCellData.SetScalars(interiorColors);
			decimatedInteriorCellData.SetScalars(decimatedInteriorColors);

			interiorAppendFilterOutput.Delete();
			decimatedInteriorAppendFilterOutput.Delete();
			interiorCellData.Delete();
			decimatedInteriorCellData.Delete();
		}
		else
		{
			interiorPolyData.DeepCopy(emptyPolyData);
			decimatedInteriorPolyData.DeepCopy(emptyPolyData);
		}

		interiorMapper.SetInputData(interiorPolyData);
		interiorMapper.Update();

		decimatedInteriorMapper.SetInputData(decimatedInteriorPolyData);
		decimatedInteriorMapper.Update();

		interiorActor.SetMapper(interiorMapper);
		((SaavtkLODActor) interiorActor).setLODMapper(decimatedInteriorMapper);
		interiorActor.Modified();
	}

	@Override
	public List<vtkProp> getProps()
	{
		List<vtkProp> allActors = new ArrayList<>(actors);
		allActors.addAll(super.getProps());
		return allActors;
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, @SuppressWarnings("unused") double[] pickPosition)
	{
		int polygonId = -1;
		if (prop == super.getStructureActor())
		{
			polygonId = super.getStructureIndexFromCellId(cellId, prop);
		}
		else if (prop == interiorActor)
		{
			polygonId = this.getPolygonIdFromCellId(cellId);
		}

		if (polygonId >= 0)
		{
			Polygon pol = getPolygon(polygonId);
			return pol.getClickStatusBarText();
		}
		else
		{
			return "";
		}
	}

	public vtkActor getInteriorActor()
	{
		return interiorActor;
	}

	@Override
	public void updateActivatedStructureVertex(int vertexId, double[] newPoint)
	{
		Polygon pol = getActivatedPolygon();
		pol.setShowInterior(smallBodyModel, false);

		super.updateActivatedStructureVertex(vertexId, newPoint);
	}

	@Override
	public void insertVertexIntoActivatedStructure(double[] newPoint)
	{
		Polygon pol = getActivatedPolygon();
		pol.setShowInterior(smallBodyModel, false);

		super.insertVertexIntoActivatedStructure(newPoint);
	}

	@Override
	public void removeCurrentStructureVertex()
	{
		Polygon pol = getActivatedPolygon();
		pol.setShowInterior(smallBodyModel, false);

		super.removeCurrentStructureVertex();
	}

	@Override
	public int getStructureIndexFromCellId(int cellId, vtkProp prop)
	{
		if (prop == interiorActor)
		{
			return getPolygonIdFromCellId(cellId);
		}

		return super.getStructureIndexFromCellId(cellId, prop);
	}

	/**
	 * A picker picking the actor of this model will return a cellId. But since
	 * there are many polygons, we need to be able to figure out which polygon was
	 * picked.
	 */
	private int getPolygonIdFromCellId(int cellId)
	{
		int numberCellsSoFar = 0;
		int size = getNumberOfStructures();
		for (int i = 0; i < size; ++i)
		{
			numberCellsSoFar += getPolygon(i).interiorPolyData.GetNumberOfCells();

			if (cellId < numberCellsSoFar)
				return i;
		}
		return -1;
	}

	private IdPair getCellIdRangeOfPolygon(int polygonId)
	{
		int startCell = 0;
		for (int i = 0; i < polygonId; ++i)
		{
			startCell += getPolygon(i).interiorPolyData.GetNumberOfCells();
		}

		int endCell = startCell;
		endCell += getPolygon(polygonId).interiorPolyData.GetNumberOfCells();

		return new IdPair(startCell, endCell);
	}

	private IdPair getCellIdRangeOfDecimatedPolygon(int polygonId)
	{
		int startCell = 0;
		for (int i = 0; i < polygonId; ++i)
		{
			startCell += getPolygon(i).decimatedInteriorPolyData.GetNumberOfCells();
		}

		int endCell = startCell;
		endCell += getPolygon(polygonId).decimatedInteriorPolyData.GetNumberOfCells();

		return new IdPair(startCell, endCell);
	}

	@Override
	public void setVisible(boolean b)
	{
		interiorActor.SetVisibility(b ? 1 : 0);
		super.setVisible(b);
	}

	@Override
	public void savePlateDataInsideStructure(int idx, File file) throws IOException
	{
		Polygon pol = getPolygon(idx);
		if (pol.isShowInterior())
		{
			vtkPolyData polydata = getPolygon(idx).interiorPolyData;
			smallBodyModel.savePlateDataInsidePolydata(polydata, file);
		}
		else
		{
			pol.setShowInterior(smallBodyModel, true);

			vtkPolyData polydata = getPolygon(idx).interiorPolyData;
			smallBodyModel.savePlateDataInsidePolydata(polydata, file);

			pol.setShowInterior(smallBodyModel, false);
		}
	}
	
	@Override
	public void savePlateDataInsideStructure(int[] idx, File file) throws IOException
	{
		
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (int i = 0; i < idx.length; i++)
		{
			Polygon pol = getPolygon(idx[i]);
			pol.setShowInterior(smallBodyModel, true);
			appendFilter.AddInputData(pol.interiorPolyData);
			appendFilter.Update();
			pol.setShowInterior(smallBodyModel, false);
		}
		smallBodyModel.savePlateDataInsidePolydata(appendFilter.GetOutput(), file);
	}

	@Override
	public FacetColoringData[] getPlateDataInsideStructure(int idx)
	{
		Polygon pol = getPolygon(idx);
		if (pol.isShowInterior())
		{
			vtkPolyData polydata = getPolygon(idx).interiorPolyData;
			return smallBodyModel.getPlateDataInsidePolydata(polydata);
		}
		else
		{
			pol.setShowInterior(smallBodyModel, true);

			vtkPolyData polydata = getPolygon(idx).interiorPolyData;
			FacetColoringData[] data = smallBodyModel.getPlateDataInsidePolydata(polydata);
			pol.setShowInterior(smallBodyModel, false);
			return data;
		}
	}
	
	@Override
	public FacetColoringData[] getPlateDataInsideStructure(int[] idx)
	{
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (int i = 0; i < idx.length; i++)
		{
			Polygon pol = getPolygon(idx[i]);
			pol.setShowInterior(smallBodyModel, true);
			appendFilter.AddInputData(pol.interiorPolyData);
			appendFilter.Update();
			pol.setShowInterior(smallBodyModel, false);
		}
		
		FacetColoringData[] data = smallBodyModel.getPlateDataInsidePolydata(appendFilter.GetOutput());
		return data;
	}

	@Override
	public void setShowStructuresInterior(int[] polygonIds, boolean show)
	{
		for (int i = 0; i < polygonIds.length; ++i)
		{
			Polygon pol = getPolygon(polygonIds[i]);
			if (pol.isShowInterior() != show)
			{
				pol.setShowInterior(smallBodyModel, show);
			}
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public boolean isShowStructureInterior(int id)
	{
		return getPolygon(id).isShowInterior();
	}

	@Override
	protected StructureModel.Structure createStructure()
	{
		return Polygon.of(++maxPolygonId);
	}

	private Polygon getPolygon(int i)
	{
		return (Polygon) getStructure(i);
	}

	private Polygon getActivatedPolygon()
	{
		return (Polygon) getActivatedLine();
	}

}
