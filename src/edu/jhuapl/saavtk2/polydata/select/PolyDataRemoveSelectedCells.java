package edu.jhuapl.saavtk2.polydata.select;

import java.util.List;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk2.polydata.PolyDataModifier;
import vtk.vtkExtractSelection;
import vtk.vtkGeometryFilter;
import vtk.vtkIdTypeArray;
import vtk.vtkPolyData;
import vtk.vtkSelection;
import vtk.vtkSelectionNode;
import vtk.vtkUnstructuredGrid;

public class PolyDataRemoveSelectedCells implements PolyDataModifier
{
	vtkIdTypeArray ids = new vtkIdTypeArray();

	public void setIndicesToRemove(List<Integer> selectedIndices)
	{
		ids.Delete();
		ids = new vtkIdTypeArray();
		for (int i = 0; i < selectedIndices.size(); i++)
			ids.InsertNextValue(selectedIndices.get(i));
	}

	@Override
	public vtkPolyData apply(vtkPolyData polyData)
	{
		vtkSelectionNode node = new vtkSelectionNode();
		node.SetFieldType(VtkSelectionField.CELL.ordinal());
		node.SetContentType(VtkSelectionContent.INDICES.ordinal());
		node.SetSelectionList(ids);
		vtkSelection selection = new vtkSelection();
		selection.AddNode(node);

		vtkExtractSelection extractSelection = new vtkExtractSelection();
		extractSelection.SetInputData(0, polyData);
		extractSelection.SetInputData(1, selection);
		extractSelection.Update();

		vtkUnstructuredGrid selectedGeometry = (vtkUnstructuredGrid) extractSelection.GetOutput();
		vtkGeometryFilter geometryFilter = new vtkGeometryFilter();
		geometryFilter.SetInputData(selectedGeometry);
		geometryFilter.Update();

		vtkPolyData result = new vtkPolyData();
		result.DeepCopy(geometryFilter.GetOutput());
		return result;
	}
}
