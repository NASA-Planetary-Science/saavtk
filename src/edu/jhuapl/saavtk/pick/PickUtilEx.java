package edu.jhuapl.saavtk.pick;

import java.util.List;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;

/**
 * Collection of package private utility methods associated with Pickers.
 */
class PickUtilEx
{
	/**
	 * Utility method to form a vtkCellPicker suitable for picking targets on the
	 * "small body".
	 */
	public static vtkCellPicker formSmallBodyPicker(PolyhedralModel aSmallBodyModel)
	{
		vtkCellPicker retCellPicker = new vtkCellPicker();
		retCellPicker.PickFromListOn();
		retCellPicker.InitializePickList();

		vtkPropCollection vPropCollection = retCellPicker.GetPickList();
		vPropCollection.RemoveAllItems();

		List<vtkProp> actors = aSmallBodyModel.getProps();
		for (vtkProp act : actors)
			retCellPicker.AddPickList(act);

		retCellPicker.AddLocator(aSmallBodyModel.getCellLocator());

		return retCellPicker;
	}

	/**
	 * Utility method to form a vtkCellPicker suitable for picking targets
	 * corresponding to the actual structural models.
	 */
	public static vtkCellPicker formStructurePicker(vtkActor aActor)
	{
		vtkCellPicker retCellPicker = new vtkCellPicker();
		retCellPicker.PickFromListOn();
		retCellPicker.InitializePickList();

		vtkPropCollection vPropCollection = retCellPicker.GetPickList();
		vPropCollection.RemoveAllItems();
		retCellPicker.AddPickList(aActor);

		return retCellPicker;
	}

}
