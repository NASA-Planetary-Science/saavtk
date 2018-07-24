package edu.jhuapl.saavtk.util.file;

public interface Tuple
{
	int size();

	String getAsString(int cellIndex);

	double get(int cellIndex) throws NumberFormatException;
}
