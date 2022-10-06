package edu.jhuapl.saavtk.heatmap;

import vtk.vtkActor;
import vtk.vtkCell;
import vtk.vtkColorTransferFunction;
import vtk.vtkFloatArray;
import vtk.vtkIdList;
import vtk.vtkLookupTable;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProperty;
import vtk.vtkUnsignedCharArray;

public class PointDataHeatMap implements HeatMap
{
    vtkPolyDataMapper heatmapMapper = new vtkPolyDataMapper();
    vtkActor heatmapActor = new vtkActor();
    private vtkPolyData polyData;
    private vtkLookupTable lut;

    public PointDataHeatMap(vtkPolyData polyData)
    {
//      this.polyData = new vtkPolyData();
//      this.polyData.DeepCopy(polyData);

        this.polyData = polyData;

        this.lut = new vtkLookupTable();
    }


    public PointDataHeatMap makeHSVRangeCLUT(double[] hue, double[] sat, double[] value)
    {
        this.lut.SetHueRange(hue[0], hue[1]);
        this.lut.SetSaturationRange(sat[0], sat[1]);
        this.lut.SetValueRange(value[0], value[1]);
        return this;
    }

    public PointDataHeatMap makeDefaultCLUT()
    {
//        System.out.println("PolyDataHeatMap: makeDefaultCLUT: making default clut");
        double[] range = {0.0, 1.0};
        this.lut.SetTableRange(range);
        lut.SetNumberOfColors(100);
//        double opacity = 1.0;
        this.lut.SetRampToLinear();
        this.lut.SetNanColor(1.0, 1.0, 1.0, 1.0);
//        lut.SetTableValue(0, 1.0, 1.0, 1.0, opacity);
//        lut.SetTableValue(1, 1.0, 0.75, 0.0, opacity);
//        lut.SetTableValue(2, 1.0, 0.5, 0.0, opacity);
//        lut.SetTableValue(3, 1.0, 0.0, 0.25, opacity);
//        lut.SetTableValue(4, 1.0, 0.0, 0.5, opacity);
//        lut.SetTableValue(5, 1.0, 0.0, 0.75, opacity);
//        lut.SetTableValue(6, 1.0, 0.0, 1.0, opacity);
//        lut.SetTableValue(7, 0.75, 0.0, 1.0, opacity);
        ((vtkLookupTable)heatmapMapper.GetLookupTable()).ForceBuild();

        return this;
    }

    public PointDataHeatMap makeCLUTFromCTF(vtkColorTransferFunction ctf)
    {
//       Nothing Here Yet
       return this;
    }


    public void setupMapperAndActor()
    {
        this.heatmapMapper.UseLookupTableScalarRangeOn();
        this.heatmapMapper.SetLookupTable(this.lut);

        this.heatmapMapper.SetInputData(this.polyData);

        this.heatmapActor.SetMapper(this.heatmapMapper);
        vtkProperty smallBodyProperty = this.heatmapActor.GetProperty();
        smallBodyProperty.SetInterpolationToGouraud();

//        smallBodyProperty.SetRepresentationToWireframe();

        smallBodyProperty.SetRepresentationToSurface();
        smallBodyProperty.SetBackfaceCulling(0);

//        smallBodyProperty.SetOpacity(0.9);

    }

    public void mapScalarsToVertices(double[] scalars)
    {
        this.heatmapMapper.SetScalarModeToUsePointData();
        this.heatmapMapper.Update();

        this.polyData.GetPointData().SetScalars(toFloatArray(scalars));
    }

    /**
     * Map the scalar values to the polygons. If averageBoundingVertices is true, interpret
     * the scalars as polygon vertex values, which are averaged to calculate the value of
     * the polygon.
     */
    public void mapScalarsToPolys(double[] scalars, boolean averageBoundingVertices)
    {
        this.heatmapMapper.SetScalarModeToUseCellData();
        this.heatmapMapper.Update();

        if (averageBoundingVertices)
        {
            int cellDataSize = (int)polyData.GetNumberOfCells();
            double[] cellScalars = new double[cellDataSize];
            for (int i=0; i<cellDataSize; i++)
            {
                vtkCell cell = polyData.GetCell(i);
                double average = 0.0;
                vtkIdList points = cell.GetPointIds();
                int nids = (int)points.GetNumberOfIds();
                for (int j=0; j<nids; j++)
                {
                    int index = (int)points.GetId(j);
                    double scalar = scalars[index];
                    average += scalar;
                }
                average = average / nids;
                cellScalars[i] = average;
            }
            this.polyData.GetCellData().SetScalars(toFloatArray(cellScalars));

        }
        else
        {
            this.polyData.GetCellData().SetScalars(toFloatArray(scalars));
        }
    }

    public void mapScalarsToPolysWithNullColor(double[] scalars)
    {
        this.heatmapMapper.SetScalarModeToUseCellData();
        this.heatmapMapper.Update();

        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
        double nullValue = 0.0;
        int numberValues = scalars.length;
        colors.SetNumberOfComponents(3);
        colors.SetNumberOfTuples(numberValues);
        double[] rgb = new double[3];
        for (int i=0; i<numberValues; ++i)
        {
          double v = scalars[i];
          if (v != nullValue)
          {
              lut.GetColor(v, rgb);
              colors.SetTuple3(i, 255.0*rgb[0], 255.0*rgb[1], 255.0*rgb[2]);
              System.out.print("Scalar Value: " + v);
              System.out.print("RGB Value: " + rgb[0] + " " + rgb[1] + " " + rgb[2] + "\r");
          }
          else
          {
              // Map null values to white
              colors.SetTuple3(i, 255.0, 255.0, 255.0);
          }
        }
        this.polyData.GetCellData().SetScalars(colors);
    }

//    private void mapScalarsThroughLookupTable(double[] scalars, vtkLookupTable lut, vtkUnsignedCharArray colors)
//    {
////        double nullValue = scalars.GetRange()[0];
//        double nullValue = 0.0;
//        int numberValues = scalars.length;
//        colors.SetNumberOfComponents(3);
//        colors.SetNumberOfTuples(numberValues);
//        double[] rgb = new double[3];
//        for (int i=0; i<numberValues; ++i)
//        {
////            double v = scalars.GetTuple1(i);
//            double v = scalars[i];
//            if (v != nullValue)
//            {
//                lut.GetColor(v, rgb);
//                colors.SetTuple3(i, 255.0*rgb[0], 255.0*rgb[1], 255.0*rgb[2]);
////                colors.SetTuple3(i, 255.0, 255.0, 255.0*0.0);
//                System.out.print("Scalar Value: " + v);
//                System.out.print("RGB Value: " + rgb[0] + " " + rgb[1] + " " + rgb[2] + "\r");
//            }
//            else
//            {
//                // Map null values to white
//                colors.SetTuple3(i, 255.0, 255.0, 255.0);
//            }
//        }
//    }




    private vtkFloatArray toFloatArray(double[] scalars)
    {
        vtkFloatArray floatArray = new vtkFloatArray();
        floatArray.SetNumberOfComponents(1);
        for (int i=0; i<scalars.length; i++)
        {
            if (scalars[i] == 0.0)
                floatArray.InsertNextTuple1(Double.NaN);
            else
                floatArray.InsertNextTuple1(scalars[i]);
        }
        return floatArray;
    }


    public vtkLookupTable getLUT()
    {
        return this.lut;
    }

    public vtkPolyDataMapper getHeatMapMapper()
    {
        return this.heatmapMapper;
    }

    public vtkActor getHeatMapActor()
    {
        return this.heatmapActor;
    }

    public void setOpacity(double opacity)
    {
        if (opacity <= 1.0 && opacity >= 0.0)
            this.heatmapActor.GetProperty().SetOpacity(opacity);
    }

    public void setInterpolationToGouraud()
    {
        this.heatmapActor.GetProperty().SetInterpolationToGouraud();
    }

}
