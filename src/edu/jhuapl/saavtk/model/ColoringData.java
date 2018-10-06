package edu.jhuapl.saavtk.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.file.CsvFileReader;
import edu.jhuapl.saavtk.util.file.DataFileReader.IncorrectFileFormatException;
import edu.jhuapl.saavtk.util.file.FieldNotFoundException;
import edu.jhuapl.saavtk.util.file.FitsFileReader;
import edu.jhuapl.saavtk.util.file.IndexableTuple;
import edu.jhuapl.saavtk.util.file.Tuple;
import vtk.vtkFloatArray;

public class ColoringData
{
	private static final Version COLORING_DATA_VERSION = Version.of(1, 0);

	// Metadata keys.
	static final Key<String> NAME = Key.of("Coloring name"); // Slope or Gravitational Vector

	static final Key<String> FILE_NAME = Key.of("File name");

	// Note: the metadata associated with this key is not yet being used. The ELEMENT_NAMES are supposed to tell
	// the load methods which columns to read from a CSV or FITS file, but there is not currently any way
	// for calling code to know which columns are correct, since the coloring metadata is set up before the files
	// are downloaded. If/when metadata is downloaded from the server, this key may be used.
	static final Key<List<String>> ELEMENT_NAMES = Key.of("Element names"); // [ "Slope" ] or [ "G_x", "G_y", "G_z" ]

	static final Key<String> UNITS = Key.of("Coloring units"); // deg or m/s^2
	static final Key<Integer> NUMBER_ELEMENTS = Key.of("Number of elements"); // 49xxx
	static final Key<Boolean> HAS_NULLS = Key.of("Coloring has nulls");

	public static ColoringData of(String name, String fileName, Iterable<String> elementNames, String units, int numberElements, boolean hasNulls)
	{
		return of(name, fileName, elementNames, units, numberElements, hasNulls, null);
	}

	public static ColoringData of(String name, Iterable<String> elementNames, String units, int numberElements, boolean hasNulls, vtkFloatArray data)
	{
		return of(name, null, elementNames, units, numberElements, hasNulls, data);
	}

	static ColoringData of(String name, File file)
	{
		FixedMetadata metadata = loadMetadata(name, file);
		return new ColoringData(metadata, null);
	}

	static ColoringData of(Metadata metadata)
	{
		Preconditions.checkNotNull(metadata);
		String name = metadata.get(NAME);
		String fileName = metadata.get(FILE_NAME);
		List<String> elementNames = metadata.get(ELEMENT_NAMES);
		String units = metadata.get(UNITS);
		int numberElements = metadata.get(NUMBER_ELEMENTS);
		boolean hasNulls = metadata.get(HAS_NULLS);
		return of(name, fileName, elementNames, units, numberElements, hasNulls);
	}

	private static ColoringData of(String name, String fileName, Iterable<String> elementNames, String units, int numberElements, boolean hasNulls, vtkFloatArray data)
	{
		FixedMetadata metadata = createMetadata(name, fileName, elementNames, units, numberElements, hasNulls);
		return new ColoringData(metadata, data);
	}

	private static FixedMetadata createMetadata(String name, String fileName, Iterable<String> elementNames, String units, int numberElements, boolean hasNulls)
	{
		Preconditions.checkNotNull(name);
		//		Preconditions.checkNotNull(fileName); // This one may be null.
		Preconditions.checkNotNull(elementNames);
		Preconditions.checkNotNull(units);

		SettableMetadata metadata = SettableMetadata.of(COLORING_DATA_VERSION);
		metadata.put(ColoringData.NAME, name);

		metadata.put(ColoringData.FILE_NAME, fileName);
		metadata.put(ColoringData.ELEMENT_NAMES, ImmutableList.copyOf(elementNames));

		metadata.put(ColoringData.UNITS, units);
		metadata.put(ColoringData.NUMBER_ELEMENTS, numberElements);
		metadata.put(ColoringData.HAS_NULLS, hasNulls);

		return FixedMetadata.of(metadata);
	}

	private static FixedMetadata loadMetadata(String name, File coloringFile)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(coloringFile);
		Preconditions.checkArgument(coloringFile.exists());

		SettableMetadata metadata = SettableMetadata.of(COLORING_DATA_VERSION);

		return FixedMetadata.of(metadata);
	}

	private final FixedMetadata metadata;
	private vtkFloatArray data;
	private double[] defaultRange;
	private boolean loadFailed;

	protected ColoringData(Metadata metadata, vtkFloatArray data)
	{
		this.metadata = FixedMetadata.of(metadata);
		this.data = data;
		this.defaultRange = this.data != null ? defaultRange = this.data.GetRange() : null;
		this.loadFailed = false;
	}

	public String getName()
	{
		return getMetadata().get(NAME);
	}

	public String getUnits()
	{
		return getMetadata().get(UNITS);
	}

	public Integer getNumberElements()
	{
		return getMetadata().get(NUMBER_ELEMENTS);
	}

	public String getFileName()
	{
		return getMetadata().get(FILE_NAME);
	}

	public List<String> getElementNames()
	{
		return ImmutableList.copyOf(getMetadata().get(ELEMENT_NAMES));
	}

	public Boolean hasNulls()
	{
		return getMetadata().get(HAS_NULLS);
	}

	public void load() throws IOException
	{
		if (loadFailed)
		{
			throw new IOException("Data failed to load previously");
		}
		if (!isLoaded())
		{
			loadFailed = true;

			String fileName = getFileName();
			if (fileName == null)
			{
				throw new IllegalStateException();
			}

			File file = FileCache.getFileFromServer(fileName);
			if (!file.exists())
			{
				String message = "Unable to download file " + fileName;
				JOptionPane.showMessageDialog(null, message, "error", JOptionPane.ERROR_MESSAGE);
				throw new IOException(message);
			}

			// If we get this far, the file was successfully downloaded.
			IndexableTuple indexable = null;

			String coloringName = getName();
			if (coloringName.toLowerCase().contains("error"))
			{
				// Try first for a vector.
				indexable = tryLoadFitsTuplesOnly(file, FitsColumnId.VECTOR_ERROR.getColumnNumbers());
				if (indexable == null)
				{
					indexable = tryLoadFitsTuplesOnly(file, FitsColumnId.SCALAR_ERROR.getColumnNumbers());
				}
			}
			else
			{
				// Try first for a vector.
				indexable = tryLoadTuples(file, FitsColumnId.VECTOR.getColumnNumbers(), CsvColumnId.VECTOR.getColumnNumbers());
				if (indexable == null)
				{
					indexable = tryLoadTuples(file, FitsColumnId.SCALAR.getColumnNumbers(), CsvColumnId.SCALAR.getColumnNumbers());
				}
			}
			if (indexable == null)
			{
				throw new IOException("Could not find coloring " + coloringName + " as vector or scalar data in file " + file);
			}

			vtkFloatArray data = new vtkFloatArray();
			final int numberCells = indexable.getNumberCells();
			final int numberRecords = indexable.size();

			if (numberRecords != getNumberElements())
			{
				throw new IOException("Plate coloring has " + numberRecords + " values, not " + getNumberElements() + " as expected in file " + file);
			}
			data.SetNumberOfComponents(numberCells);
			data.SetNumberOfTuples(numberRecords);

			for (int index = 0; index < numberRecords; ++index)
			{
				Tuple tuple = indexable.get(index);
				if (numberCells == 1)
				{
					data.SetTuple1(index, tuple.get(0));
				}
				else if (numberCells == 2)
				{
					data.SetTuple2(index, tuple.get(0), tuple.get(1));
				}
				else if (numberCells == 3)
				{
					data.SetTuple3(index, tuple.get(0), tuple.get(1), tuple.get(2));
				}
				else if (numberCells == 4)
				{
					data.SetTuple4(index, tuple.get(0), tuple.get(1), tuple.get(2), tuple.get(3));
				}
				else if (numberCells == 6)
				{
					data.SetTuple6(index, tuple.get(0), tuple.get(1), tuple.get(2), tuple.get(3), tuple.get(4), tuple.get(5));
				}
				else if (numberCells == 9)
				{
					data.SetTuple9(index, tuple.get(0), tuple.get(1), tuple.get(2), tuple.get(3), tuple.get(4), tuple.get(5), tuple.get(6), tuple.get(7), tuple.get(8));
				}
				else
				{
					throw new AssertionError();
				}
			}
			double[] defaultRange = computeDefaultColoringRange(data);
			this.data = data;
			this.defaultRange = defaultRange;
			loadFailed = false;
		}
	}

	public void clear()
	{
		if (getFileName() != null)
		{
			data = null;
			defaultRange = null;
			loadFailed = false;
		}
	}

	public void reload() throws IOException
	{
		clear();
		load();
	}

	public vtkFloatArray getData()
	{
		Preconditions.checkState(data != null);
		return data;
	}

	public double[] getDefaultRange()
	{
		Preconditions.checkState(defaultRange != null);
		return defaultRange;
	}

	public ColoringData copy()
	{
		return new ColoringData(getMetadata().copy(), data);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getMetadata().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof ColoringData))
		{
			return false;
		}
		ColoringData that = (ColoringData) other;
		if (!this.getMetadata().equals(that.getMetadata()))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(getName());
		append(builder, getUnits());
		String fileFormat = getFileName().replaceFirst(".*[/\\\\]", "").replaceFirst("[^\\.]*\\.", "");
		fileFormat = fileFormat.replaceFirst("\\.gz$", "").toUpperCase();
		append(builder, fileFormat);
		return builder.toString();
	}

	FixedMetadata getMetadata()
	{
		return metadata;
	}

	private final void append(StringBuilder builder, String toAppend)
	{
		if (toAppend != null && toAppend.matches(".*\\S.*"))
		{
			builder.append(", ");
			builder.append(toAppend);
		}
	}

	private final double[] computeDefaultColoringRange(vtkFloatArray data)
	{
		double[] result = data.GetRange();
		result = new double[] { result[0], result[1] };
		if (getMetadata().get(HAS_NULLS))
		{
			int numberValues = data.GetNumberOfTuples();
			double maximum = result[1];
			double minimum = maximum;
			for (int index = 0; index < numberValues; ++index)
			{
				double value = data.GetValue(index);
				if (value < minimum && value > result[0])
					minimum = value;
			}

			result[0] = minimum;
		}
		return result;
	}

	private boolean isLoaded()
	{
		return (data != null && defaultRange != null);
	}

	private IndexableTuple tryLoadFitsTuplesOnly(File file, Iterable<Integer> columnNumbers) throws IOException
	{
		IndexableTuple result = null;
		try
		{
			result = FitsFileReader.of().readTuples(file, 1, columnNumbers);
		}
		catch (IncorrectFileFormatException e)
		{
			throw new IOException(e);
		}
		catch (@SuppressWarnings("unused") FieldNotFoundException e)
		{
			// Fall through so caller can try something else.
		}
		return result;
	}

	private IndexableTuple tryLoadTuples(File file, Iterable<Integer> fitsColumnNumbers, Iterable<Integer> csvColumnNumbers) throws IOException
	{
		IndexableTuple result = null;
		try
		{
			try
			{
				result = FitsFileReader.of().readTuples(file, 1, fitsColumnNumbers);
			}
			catch (@SuppressWarnings("unused") IncorrectFileFormatException e)
			{
				// Try as a CSV file now.
				result = CsvFileReader.of().readTuples(file, csvColumnNumbers);
			}
		}
		catch (@SuppressWarnings("unused") FieldNotFoundException e)
		{
			// Fall through so caller can try something else.
		}
		return result;
	}

	private enum FitsColumnId
	{
		SCALAR(4),
		SCALAR_ERROR(5),
		VECTOR(4, 6, 8),
		VECTOR_ERROR(5, 7, 9);
		private final ImmutableList<Integer> columnNumbers;

		private FitsColumnId(int columnNumber)
		{
			this.columnNumbers = ImmutableList.of(columnNumber);
		}

		private FitsColumnId(int xColumnNumber, int yColumnNumber, int zColumnNumber)
		{
			this.columnNumbers = ImmutableList.of(xColumnNumber, yColumnNumber, zColumnNumber);
		}

		public ImmutableList<Integer> getColumnNumbers()
		{
			return columnNumbers;
		}

	}

	private enum CsvColumnId
	{
		SCALAR(0),
		VECTOR(0, 1, 2),;
		private final ImmutableList<Integer> columnNumbers;

		private CsvColumnId(int columnNumber)
		{
			this.columnNumbers = ImmutableList.of(columnNumber);
		}

		private CsvColumnId(int xColumnNumber, int yColumnNumber, int zColumnNumber)
		{
			this.columnNumbers = ImmutableList.of(xColumnNumber, yColumnNumber, zColumnNumber);
		}

		public ImmutableList<Integer> getColumnNumbers()
		{
			return columnNumbers;
		}

	}

}
