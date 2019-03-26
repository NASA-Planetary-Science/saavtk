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
	 * corresponding to the specified vtkActor.
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

	/**
	 * Utility method to form a vtkCellPicker suitable for picking targets
	 * corresponding to the specified list of vtkActors.
	 */
	public static vtkCellPicker formStructurePicker(List<vtkProp> aActorL)
	{
		vtkCellPicker retCellPicker = new vtkCellPicker();
		retCellPicker.PickFromListOn();
		retCellPicker.InitializePickList();

		// Utilize the reverse ordering so that items drawn on top will
		// be picked before items on bottom
		aActorL = Lists.reverse(aActorL);

		vtkPropCollection vPropCollection = retCellPicker.GetPickList();
		vPropCollection.RemoveAllItems();
		for (vtkProp aActor : aActorL)
			retCellPicker.AddPickList(aActor);

		return retCellPicker;
	}

}
