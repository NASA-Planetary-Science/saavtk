package edu.jhuapl.saavtk.model.plateColoring;

import com.google.common.collect.ImmutableList;

public interface ColoringDataManager
{
	String getId();

	ImmutableList<String> getNames();

	ImmutableList<Integer> getResolutions();

	boolean has(String name, int numberElements);

	ColoringData get(String name, int numberElements);

	ImmutableList<ColoringData> get(int numberElements);

	ColoringDataManager copy();

}
