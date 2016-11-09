package edu.jhuapl.saavtk.heatmap;

import vtk.vtkActor;
import vtk.vtkColorTransferFunction;
import vtk.vtkLookupTable;
import vtk.vtkPolyDataMapper;

interface HeatMap
{
    public HeatMap makeDefaultCLUT();

    public HeatMap makeCLUTFromCTF(vtkColorTransferFunction ctf);

    public void setupMapperAndActor();

    public vtkLookupTable getLUT();

    public vtkPolyDataMapper getHeatMapMapper();

    public vtkActor getHeatMapActor();

    public void setOpacity(double opacity);
}
