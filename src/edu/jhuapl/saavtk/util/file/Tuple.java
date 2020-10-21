package edu.jhuapl.saavtk.util.file;

public interface Tuple
{
	int size();
	
	double[] get();

	String getAsString(int cellIndex);

	double get(int cellIndex);
}
