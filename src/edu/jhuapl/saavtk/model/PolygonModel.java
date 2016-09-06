package edu.jhuapl.saavtk.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;

/**
 * Model of polygon structures drawn on a body.
 */
public class PolygonModel extends LineModel
{
    private vtkPolyData interiorPolyData;
    private vtkAppendPolyData interiorAppendFilter;
    private vtkPolyDataMapper interiorMapper;
    private vtkActor interiorActor;

    private vtkUnsignedCharArray interiorColors;

    private List<vtkProp> actors = new ArrayList<vtkProp>();

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
        interiorColors.SetNumberOfComponents(3);

        interiorPolyData = new vtkPolyData();
        interiorAppendFilter = new vtkAppendPolyData();
        interiorAppendFilter.UserManagedInputsOn();
        interiorMapper = new vtkPolyDataMapper();
        interiorActor = new vtkActor();
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

    protected String getType()
    {
        return POLYGONS;
    }

    protected void updatePolyData()
    {
        super.updatePolyData();

        int numberOfStructures = getNumberOfStructures();
        if (numberOfStructures > 0)
        {
            interiorAppendFilter.SetNumberOfInputs(numberOfStructures);

            for (int i=0; i<numberOfStructures; ++i)
            {
                Polygon polygon = getPolygon(i);
                vtkPolyData poly = polygon.interiorPolyData;
                if (polygon.hidden)
                    poly = emptyPolyData;

                if (poly != null)
                    interiorAppendFilter.SetInputDataByNumber(i, poly);
            }

            interiorAppendFilter.Update();

            vtkPolyData interiorAppendFilterOutput = interiorAppendFilter.GetOutput();
            interiorPolyData.DeepCopy(interiorAppendFilterOutput);

            PolyDataUtil.shiftPolyDataInNormalDirection(interiorPolyData, getOffset());

            interiorColors.SetNumberOfTuples(interiorPolyData.GetNumberOfCells());
            for (int i=0; i<numberOfStructures; ++i)
            {
                int[] color = getPolygon(i).color;

                if (Arrays.binarySearch(getSelectedStructures(), i) >= 0)
                    color = getCommonData().getSelectionColor();

                IdPair range = this.getCellIdRangeOfPolygon(i);
                for (int j=range.id1; j<range.id2; ++j)
                    interiorColors.SetTuple3(j, color[0], color[1], color[2]);
            }
            vtkCellData interiorCellData = interiorPolyData.GetCellData();

            interiorCellData.SetScalars(interiorColors);

            interiorAppendFilterOutput.Delete();
            interiorCellData.Delete();
        }
        else
        {
            interiorPolyData.DeepCopy(emptyPolyData);
        }

        interiorMapper.SetInputData(interiorPolyData);
        interiorMapper.Update();

        interiorActor.SetMapper(interiorMapper);
        interiorActor.Modified();

    }

    public List<vtkProp> getProps()
    {
        ArrayList<vtkProp> allActors = new ArrayList<vtkProp>(actors);
        allActors.addAll(super.getProps());
        return allActors;
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        int polygonId = -1;
        if (prop == super.getStructureActor())
        {
            polygonId = super.getStructureIndexFromCellId(cellId, prop);
        }
        else if ( prop == interiorActor)
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

    public void updateActivatedStructureVertex(int vertexId, double[] newPoint)
    {
        Polygon pol = getActivatedPolygon();
        pol.setShowInterior(false);

        super.updateActivatedStructureVertex(vertexId, newPoint);
    }

    public void insertVertexIntoActivatedStructure(double[] newPoint)
    {
        Polygon pol = getActivatedPolygon();
        pol.setShowInterior(false);

        super.insertVertexIntoActivatedStructure(newPoint);
    }

    public void removeCurrentStructureVertex()
    {
        Polygon pol = getActivatedPolygon();
        pol.setShowInterior(false);

        super.removeCurrentStructureVertex();
    }

    public int getStructureIndexFromCellId(int cellId, vtkProp prop)
    {
        if (prop == interiorActor)
        {
            return getPolygonIdFromCellId(cellId);
        }

        return super.getStructureIndexFromCellId(cellId, prop);
    }

    /**
     * A picker picking the actor of this model will return a
     * cellId. But since there are many polygons, we need to be
     * able to figure out which polygon was picked.
     */
    private int getPolygonIdFromCellId(int cellId)
    {
        int numberCellsSoFar = 0;
        int size = getNumberOfStructures();
        for (int i=0; i<size; ++i)
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
        for (int i=0; i<polygonId; ++i)
        {
            startCell += getPolygon(i).interiorPolyData.GetNumberOfCells();
        }

        int endCell = startCell;
        endCell += getPolygon(polygonId).interiorPolyData.GetNumberOfCells();

        return new IdPair(startCell, endCell);
    }

    public void setVisible(boolean b)
    {
        interiorActor.SetVisibility(b ? 1 : 0);
        super.setVisible(b);
    }

    public void savePlateDataInsideStructure(int idx, File file) throws IOException
    {
        vtkPolyData polydata = getPolygon(idx).interiorPolyData;
        smallBodyModel.savePlateDataInsidePolydata(polydata, file);
    }

    public void setShowStructuresInterior(int[] polygonIds, boolean show)
    {
        for (int i=0; i<polygonIds.length; ++i)
        {
            Polygon pol = getPolygon(polygonIds[i]);
            if (pol.isShowInterior() != show)
            {
                pol.setShowInterior(show);
            }
        }

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public boolean isShowStructureInterior(int id)
    {
        return getPolygon(id).isShowInterior();
    }

    protected StructureModel.Structure createStructure(PolyhedralModel smallBodyModel)
    {
        return new Polygon(smallBodyModel, ++maxPolygonId);
    }

    private Polygon getPolygon(int i)
    {
        return (Polygon)getStructure(i);
    }

    private Polygon getActivatedPolygon()
    {
        return (Polygon)getActivatedLine();
    }

}
