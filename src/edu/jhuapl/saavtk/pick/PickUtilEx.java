package edu.jhuapl.saavtk.pick;

import java.util.List;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;

/**
 * Collection of (extra) utility methods associated with Pickers.
 * <p>
 * The following functionality is provided:
 * <ul>
 * <li>Creation of {@link vtkCellPicker}s (empty, single, or vtkProps located on
 * a {@link PolyhedralModel})
 * <li>Updating of target {@link vtkProp}s for picking
 * </ul>
 *
 * @author lopeznr1
 */
public class PickUtilEx
{
	/**
	 * Utility method to form a {@link vtkCellPicker} suitable for picking targets.
	 * <p>
	 * The returned picker will not have any registered {@link vtkProp}s and thus
	 * will (initially) not be able to pick anything.
	 */
	public static vtkCellPicker formEmptyPicker()
	{
		vtkCellPicker retCellPicker = new vtkCellPicker();
		retCellPicker.PickFromListOn();
		retCellPicker.InitializePickList();

		return retCellPicker;
	}

	/**
	 * Utility method to form a {@link vtkCellPicker} suitable for picking targets
	 * corresponding to the specified (single) {@link vtkProp}.
	 */
	public static vtkCellPicker formPickerFor(vtkProp aProp)
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
	 * Utility method to form a {@link vtkCellPicker} suitable for picking targets
	 * on the "small body".
	 */
	public static vtkCellPicker formSmallBodyPicker(PolyhedralModel aSmallBody)
	{
		// Delegate
		vtkCellPicker retCellPicker = formEmptyPicker();

		vtkPropCollection vPropCollection = retCellPicker.GetPickList();
		vPropCollection.RemoveAllItems();

		// Register the list of vtkProps
		List<vtkProp> tmpPropL = aSmallBody.getProps();
		for (vtkProp aProp : tmpPropL)
			retCellPicker.AddPickList(aProp);

		retCellPicker.AddLocator(aSmallBody.getCellLocator());

		return retCellPicker;
	}

	/**
	 * Utility method to update a {@link vtkCellPicker} with an updated list of
	 * {@link vtkProp}s corresponding to potential targets of interest.
	 */
	public static void updatePickerProps(vtkCellPicker aCellPicker, List<? extends vtkProp> aPropL)
	{
		// Utilize the reverse ordering so that items drawn on top will
		// be picked before items on bottom
		aPropL = Lists.reverse(aPropL);

		// Update the list of vtkProps that the picker will respond to
		vtkPropCollection vPropCollection = aCellPicker.GetPickList();
		vPropCollection.RemoveAllItems();
		for (vtkProp aProp : aPropL)
			aCellPicker.AddPickList(aProp);
	}

}
