package edu.jhuapl.saavtk.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.model.PolyhedralModel.Format;
import edu.jhuapl.saavtk.util.FileCache;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.TableHDU;
import vtk.vtkFloatArray;

public class ColoringData
{
	private static final Version COLORING_DATA_VERSION = Version.of(1, 0);
	/*
	 * This Pattern will match on either quoted text or text between commas,
	 * including whitespace, and accounting for beginning and end of line. Cribbed
	 * from a Stacktrace post.
	 */
	private static final Pattern CSV_PATTERN = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?:,|$)");

	// Metadata keys.
	static final Key<String> NAME = Key.of("Coloring name"); // Slope or Gravitational Vector

	static final Key<String> FILE_NAME = Key.of("File name");
	static final Key<List<String>> FIELD_NAMES = Key.of("Field names"); // [ "Slope" ] or [ "G_x", "G_y", "G_z" ]

	static final Key<String> UNITS = Key.of("Coloring units"); // deg or m/s^2
	static final Key<Integer> NUMBER_ELEMENTS = Key.of("Number of elements"); // 49xxx
	static final Key<Boolean> HAS_NULLS = Key.of("Coloring has nulls");

	public static ColoringData of(String name, String fileName, Iterable<String> fieldNames, String units, int numberElements, boolean hasNulls)
	{
		return of(name, fileName, fieldNames, units, numberElements, hasNulls, null);
	}

	public static ColoringData of(String name, Iterable<String> fieldNames, String units, int numberElements, boolean hasNulls, vtkFloatArray data)
	{
		return of(name, null, fieldNames, units, numberElements, hasNulls, data);
	}

	public static ColoringData of(String name, String fileName, Iterable<String> fieldNames, String units, int numberElements, boolean hasNulls, vtkFloatArray data)
	{
		FixedMetadata metadata = createMetadata(name, fileName, fieldNames, units, numberElements, hasNulls);
		return new ColoringData(metadata, data);
	}

	private static FixedMetadata createMetadata(String name, String fileName, Iterable<String> fieldNames, String units, int numberElements, boolean hasNulls)
	{
		Preconditions.checkNotNull(name);
		//		Preconditions.checkNotNull(fileName); // This one may be null.
		Preconditions.checkNotNull(fieldNames);
		Preconditions.checkNotNull(units);

		SettableMetadata metadata = SettableMetadata.of(COLORING_DATA_VERSION);
		metadata.put(ColoringData.NAME, name);

		metadata.put(ColoringData.FILE_NAME, fileName);
		metadata.put(ColoringData.FIELD_NAMES, ImmutableList.copyOf(fieldNames));

		metadata.put(ColoringData.UNITS, units);
		metadata.put(ColoringData.NUMBER_ELEMENTS, numberElements);
		metadata.put(ColoringData.HAS_NULLS, hasNulls);

		return FixedMetadata.of(metadata);
	}

	private final FixedMetadata metadata;
	private vtkFloatArray data;
	private double[] defaultRange;
	private boolean loadFailed;

	protected ColoringData(FixedMetadata metadata, vtkFloatArray data)
	{
		this.metadata = metadata;
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

	public List<String> getFieldNames()
	{
		return ImmutableList.copyOf(getMetadata().get(FIELD_NAMES));
	}

	public Boolean hasNulls()
	{
		return getMetadata().get(HAS_NULLS);
	}

	public boolean loadFailed()
	{
		return loadFailed;
	}

	public void load() throws IOException
	{
		if (this.data == null)
		{
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
			Format format = getFileFormat(file);
			switch (format)
			{
			case FIT:
				loadColoringDataFits(file);
				break;
			case TXT:
				loadColoringDataTxt(file);
				break;
			case UNKNOWN:
				throw new IOException("Do not recognize the type of file " + fileName);
			default:
				throw new AssertionError("Unhandled file format type");
			}
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

	private final void append(StringBuilder builder, String toAppend)
	{
		if (toAppend != null && toAppend.matches(".*\\S.*"))
		{
			builder.append(", ");
			builder.append(toAppend);
		}
	}

	FixedMetadata getMetadata()
	{
		return metadata;
	}

	private Format getFileFormat(File file)
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
		{
			String line = in.readLine();
			if (line.matches("^[Ss][Ii][Mm][Pp][Ll][Ee]\\s*=\\s*[Tt][Rr]?[Uu]?[Ee]?\\b.*$"))
			{
				return Format.FIT;
			}
			else
			{
				//				Float.parseFloat(line);
				return Format.TXT;
			}
		}
		catch (@SuppressWarnings("unused") IOException | NumberFormatException e)
		{
			return Format.UNKNOWN;
		}
	}

	private void loadColoringDataFits(File file) throws IOException
	{
		if (loadFailed)
		{
			throw new IOException("Coloring data failed to load");
		}
		try (Fits fits = new Fits(file))
		{
			fits.read();
			int numberElements = getMetadata().get(NUMBER_ELEMENTS);

			BasicHDU<?> hdu = fits.getHDU(1);
			if (hdu instanceof TableHDU)
			{
				//				try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/Users/peachjm1/Downloads/custom-vector-pc.csv"))))
				//				{
				TableHDU<?> table = (TableHDU<?>) hdu;
				int numberRows = table.getNRows();
				if (numberRows != numberElements)
				{
					loadFailed = true;
					String message = "Number of rows in FITS file " + file + " is " + numberRows + ", not " + numberElements + " as expected.";
					throw new IOException(message);
				}

				//				ImmutableList<String> columnNames = findMatchingColumnNameCaseInsensitive(table, metadata.get(FIELD_NAMES));

				vtkFloatArray data = new vtkFloatArray();

				int numberColumns = table.getNCols();
				int numberComponents = numberColumns > 9 ? 3 : numberColumns > 7 ? 2 : 1;
				data.SetNumberOfComponents(numberComponents);
				data.SetNumberOfTuples(numberElements);

				//				float[] floatData = (float[]) table.getColumn(columnNames.get(0));
				float[] xColumn = null;
				float[] yColumn = null;
				float[] zColumn = null;
				xColumn = (float[]) table.getColumn(4);
				if (table.getNCols() > 7)
				{
					yColumn = (float[]) table.getColumn(6);
				}
				if (table.getNCols() > 9)
				{
					zColumn = (float[]) table.getColumn(8);
				}
				for (int index = 0; index < numberElements; index++)
				{
					if (yColumn != null && zColumn != null)
					{
						//							writer.write(xColumn[index] + ", " + yColumn[index] + ", " + zColumn[index] + "\n");
						data.SetTuple3(index, xColumn[index], yColumn[index], zColumn[index]);
					}
					else if (yColumn != null)
					{
						data.SetTuple2(index, xColumn[index], yColumn[index]);
					}
					else
					{
						data.SetTuple1(index, xColumn[index]);
					}
				}

				this.defaultRange = computeDefaultColoringRange(data);

				// Everything worked so assign to the data field.
				this.data = data;
				//				}
			}
			else
			{
				throw new IOException("First extension of file " + file + " is not a FITS table HDU");
			}
		}
		catch (FitsException e)
		{
			throw new IOException(e);
		}
	}

	private ImmutableList<String> findMatchingColumnNameCaseInsensitive(TableHDU<?> table, List<String> columnNames) throws IOException
	{
		// Make a map of the table's actual file names, keyed on the all uppercase version of each name.
		Map<String, String> tableColumnNames = new HashMap<>();
		for (int index = 0; index < table.getNCols(); ++index)
		{
			String columnName = table.getColumnName(index);
			tableColumnNames.put(columnName.toUpperCase(), columnName);
		}

		// Use the table map to look up the case-sensitive names that match the input column names.
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (String columnName : columnNames)
		{
			String tableColumnName = tableColumnNames.get(columnName.toUpperCase());
			if (tableColumnName != null)
			{
				builder.add(tableColumnName);
			}
			else
			{
				throw new IOException("Cannot find a column with name matching " + columnName.toUpperCase());
			}
		}
		return builder.build();
	}

	private void loadColoringDataTxt(File file) throws IOException
	{
		if (loadFailed)
		{
			throw new IOException("Coloring data failed to load");
		}
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
		{
			int numberElements = getMetadata().get(NUMBER_ELEMENTS);

			int index = 0;
			String[] line = parseCSV(in.readLine());

			if (line.length < 1 || line.length > 3)
			{
				throw new IOException("Text (CSV) coloring data must have between 1 and 3 elements per line");
			}
			vtkFloatArray data = new vtkFloatArray();

			data.SetNumberOfComponents(line.length);
			data.SetNumberOfTuples(numberElements);

			float[] values = new float[line.length];
			do
			{
				if (line.length < 1 || line.length > 3)
				{
					throw new IOException("Text (CSV) coloring data must have between 1 and 3 elements per line");
				}
				// This will pass the first time, but need to confirm this is true for every line in the loop.
				if (line.length != values.length)
				{
					throw new IOException("Text (CSV) coloring data must have the same number of elements on each line");
				}

				if (index < numberElements)
				{
					for (int fieldIndex = 0; fieldIndex < values.length; ++fieldIndex)
					{
						values[fieldIndex] = Float.parseFloat(line[fieldIndex]);
					}
					if (values.length == 1)
					{
						data.SetTuple1(index, values[0]);
					}
					else if (values.length == 2)
					{
						data.SetTuple2(index, values[0], values[1]);
					}
					else if (values.length == 3)
					{
						data.SetTuple3(index, values[0], values[1], values[2]);
					}
				}
				++index;
			}
			while ((line = parseCSV(in.readLine())) != null);

			if (index != numberElements)
			{
				loadFailed = true;
				String message = "Number of lines in text file " + file + " is " + index + ", not " + numberElements + " as expected.";
				throw new IOException(message);
			}

			this.defaultRange = computeDefaultColoringRange(data);

			// Everything worked so assign to the data field.
			this.data = data;
		}

	}

	private String[] parseCSV(String line)
	{
		if (line == null)
		{
			return null;
		}

		Matcher matcher = CSV_PATTERN.matcher(line);
		List<String> matches = new ArrayList<>();
		while (matcher.find())
		{
			String match = matcher.group(1);
			if (match != null)
			{
				matches.add(match);
			}
			else
			{
				matches.add(matcher.group(2));
			}
		}

		int size = matches.size();
		return matches.toArray(new String[size]);

	}

	private final double[] computeDefaultColoringRange(vtkFloatArray data)
	{
		double[] result = data.GetRange();
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

}
