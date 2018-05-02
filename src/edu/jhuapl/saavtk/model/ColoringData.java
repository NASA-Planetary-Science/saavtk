package edu.jhuapl.saavtk.model;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.model.PolyhedralModel.Format;
import vtk.vtkFloatArray;

public class ColoringData
{
	private static final Version COLORING_DATA_VERSION = Version.of(1, 0);
	// Metadata keys.
	public static final Key<String> NAME = Key.of("Coloring name"); // Slope or Gravitational Vector

	public static final Key<String> FILE_NAME = Key.of("File name");
	public static final Key<String> FILE_FORMAT = Key.of("File format"); // Stand-in for Format enumeration.
	public static final Key<List<String>> ELEMENT_NAMES = Key.of("Element names"); // [ "Slope" ] or [ "G_x", "G_y", "G_z" ]

	public static final Key<String> UNITS = Key.of("Coloring units"); // deg or m/s^2
	public static final Key<Integer> NUMBER_ELEMENTS = Key.of("Number of elements"); // 49xxx
	public static final Key<Boolean> HAS_NULLS = Key.of("Coloring has nulls");

	public static ColoringData of(String name, String fileName, Format fileFormat, Iterable<String> elementNames, String units, int numberElements, boolean hasNulls)
	{
		Preconditions.checkNotNull(name);
		// TODO check others too.

		Metadata metadata = Metadata.of(COLORING_DATA_VERSION);
		metadata.put(ColoringData.NAME, name);

		metadata.put(ColoringData.FILE_NAME, fileName);
		metadata.put(ColoringData.FILE_FORMAT, fileFormat.toString());
		metadata.put(ColoringData.ELEMENT_NAMES, ImmutableList.copyOf(elementNames));

		metadata.put(ColoringData.UNITS, units);
		metadata.put(ColoringData.NUMBER_ELEMENTS, numberElements);
		metadata.put(ColoringData.HAS_NULLS, hasNulls);

		return new ColoringData(metadata);
	}

	public static ColoringData of(Metadata metadata)
	{
		// TODO validate metadata before using.
		return new ColoringData(metadata);
	}

	private final Metadata metadata;
	private vtkFloatArray data;

	protected ColoringData(Metadata metadata)
	{
		this.metadata = metadata;
		this.data = null;
	}

	public String getName()
	{
		return metadata.get(NAME);
	}

	public String getUnits()
	{
		return metadata.get(UNITS);
	}

	public vtkFloatArray getData()
	{
		if (data == null)
		{
			data = load();
		}
		return null;
	}

	//	String getFileName()
	//	{
	//		return metadata.get(FILE_NAME);
	//	}

	int getNumberElements()
	{
		return metadata.get(NUMBER_ELEMENTS);
	}

	Metadata getMetadata()
	{
		return metadata;
	}

	private vtkFloatArray load()
	{
		// TODO put load code here.
		return null;
	}

}
