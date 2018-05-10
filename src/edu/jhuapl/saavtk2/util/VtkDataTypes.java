package edu.jhuapl.saavtk2.util;

public enum VtkDataTypes
{
	// cf. http://vtk.1045678.n5.nabble.com/vtkScalarType-question-td5721173.html
	VTK_VOID(0), VTK_BIT(1), VTK_CHAR(2), VTK_UNSIGNED_CHAR(3), VTK_SHORT(4), VTK_UNSIGNED_SHORT(5), VTK_INT(6), VTK_UNSIGNED_INT(7), VTK_LONG(8), VTK_UNSIGNED_LONG(9), VTK_FLOAT(10), VTK_DOUBLE(11), VTK_ID_TYPE(12), VTK_SIGNED_CHAR(15);

	int vtkIntId;

	private VtkDataTypes(int vtkIntId)
	{
		this.vtkIntId = vtkIntId;
	}

	public int getVtkIntId()
	{
		return vtkIntId;
	}
}
