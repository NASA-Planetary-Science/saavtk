package edu.jhuapl.saavtk.util.file;

public interface IndexableTuple extends DataObject
{
	int getNumberCells();

	String getName(int cellIndex);

	String getUnits(int cellIndex);

	int size();

	Tuple get(int index);
}
