package edu.jhuapl.saavtk.gui.render;

public enum VtkInteractorStyleMotionFlags
{

	VTKIS_START(0),
	VTKIS_NONE(0),
	VTKIS_ROTATE(1),
	VTKIS_PAN(2),
	VTKIS_SPIN(3),
	VTKIS_DOLLY(4),
	VTKIS_ZOOM(5),
	VTKIS_USCALE(6),
	VTKIS_TIMER(7),
	VTKIS_FORWARDFLY(8),
	VTKIS_REVERSEFLY(9),
	VTKIS_TWO_POINTER(10),
	VTKIS_CLIP(11),
	VTKIS_PICK(12),						// perform a pick at the last location
	VTKIS_LOAD_CAMERA_POSE(13), 		// iterate through saved camera poses
	VTKIS_POSITION_PROP(14), 			// adjust the position, orientation of a prop
	VTKIS_EXIT(15), 					// call exit callback
	VTKIS_TOGGLE_DRAW_CONTROLS(16), 	// draw device controls helpers
	VTKIS_MENU(17), 					// invoke an application menu
	VTKIS_ANIM_OFF(0),
	VTKIS_ANIM_ON(1);

	int vtkCode;

	private VtkInteractorStyleMotionFlags(int vtkCode)
	{
		this.vtkCode = vtkCode;
	}

	public int getVtkCode()
	{
		return vtkCode;
	}
}
