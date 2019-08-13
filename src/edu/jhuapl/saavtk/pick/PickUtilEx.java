package edu.jhuapl.saavtk.pick;

import java.util.List;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;

/**
 * Collection of (extra) utility methods associated with Pickers.
 */
public class PickUtilEx
{
	/**
	 * Utility method to form a vtkCellPicker suitable for picking targets.
	 * <P>
	 * The returned picker will not have any registered vtkPros and thus will
	 * (initially) not be able to pick anything.
	 */
	public static vtkCellPicker formEmptyPicker()
	{
		vtkCellPicker retCellPicker = new vtkCellPicker();
		retCellPicker.PickFromListOn();
		retCellPicker.InitializePickList();

		return retCellPicker;
	}

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
	 * corresponding to the specified vtkProp.
	 */
	public static vtkCellPicker formStructurePicker(vtkActor aProp)
	{
		// Delegate
		vtkCellPicker retCellPicker = formEmptyPicker();

		// Register the single vtkProp
		vtkPropCollection vPropCollection = retCellPicker.GetPickList();
		vPropCollection.RemoveAllItems();
		retCellPicker.AddPickList(aProp);

		return retCellPicker;
	}

	/**
	 * Utility method to update a vtkCellPicker with an updated list of vtkProps
	 * corresponding to potential targets of interest.
	 */
	public static void updatePickerProps(vtkCellPicker aCellPicker, List<vtkProp> aPropL)
	{
		// Utilize the reverse ordering so that items drawn on top will
		// be picked before items on bottom
		aPropL = Lists.reverse(aPropL);

		// Update the list of vtkProps that the picker will respond to
		vtkPropCollection vPropCollection = aCellPicker.GetPickList();
		vPropCollection.RemoveAllItems();
		for (vtkProp aActor : aPropL)
			aCellPicker.AddPickList(aActor);
	}

}
