package edu.jhuapl.saavtk.util.file;

public interface IndexableTuple extends DataObject
{
	int getNumberFields();

	String getName(int fieldIndex);

	String getUnits(int fieldIndex);

	int size();

	Tuple get(int index);
}
