package edu.jhuapl.saavtk.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
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

	// Metadata keys.
	static final Key<String> NAME = Key.of("Coloring name"); // Slope or Gravitational Vector

	static final Key<String> FILE_NAME = Key.of("File name");
	static final Key<String> FILE_FORMAT = Key.of("File format"); // Stand-in for Format enumeration.
	static final Key<List<String>> ELEMENT_NAMES = Key.of("Element names"); // [ "Slope" ] or [ "G_x", "G_y", "G_z" ]

	static final Key<String> UNITS = Key.of("Coloring units"); // deg or m/s^2
	static final Key<Integer> NUMBER_ELEMENTS = Key.of("Number of elements"); // 49xxx
	static final Key<Boolean> HAS_NULLS = Key.of("Coloring has nulls");

	public static ColoringData of(String name, String fileName, Format fileFormat, Iterable<String> elementNames, String units, int numberElements, boolean hasNulls)
	{
		Metadata metadata = createMetadata(name, fileName, fileFormat, elementNames, units, numberElements, hasNulls);
		return new ColoringData(metadata, null);
	}

	public static ColoringData of(String name, String fileName, Format fileFormat, Iterable<String> elementNames, String units, int numberElements, boolean hasNulls, vtkFloatArray data)
	{
		Metadata metadata = createMetadata(name, fileName, fileFormat, elementNames, units, numberElements, hasNulls);
		return new ColoringData(metadata, data);
	}

	private static Metadata createMetadata(String name, String fileName, Format fileFormat, Iterable<String> elementNames, String units, int numberElements, boolean hasNulls)
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

		return metadata;
	}

	private final Metadata metadata;
	private vtkFloatArray data;

	protected ColoringData(Metadata metadata, vtkFloatArray data)
	{
		this.metadata = metadata;
		this.data = data;
	}

	public String getName()
	{
		return metadata.get(NAME);
	}

	public String getUnits()
	{
		return metadata.get(UNITS);
	}

	public vtkFloatArray getData() throws IOException
	{
		if (data == null)
		{
			load();
		}
		return data;
	}

	int getNumberElements()
	{
		return metadata.get(NUMBER_ELEMENTS);
	}

	Metadata getMetadata()
	{
		return metadata;
	}

	private String getFileName()
	{
		return metadata.get(FILE_NAME);
	}

	private void load() throws IOException
	{
		String fileName = getFileName();
		File file = FileCache.getFileFromServer(fileName);
		if (file == null)
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
				Float.parseFloat(line);
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
		try (Fits fits = new Fits(file))
		{
			fits.read();
			int numberElements = metadata.get(NUMBER_ELEMENTS);

			BasicHDU<?> hdu = fits.getHDU(1);
			if (hdu instanceof TableHDU)
			{
				TableHDU<?> table = (TableHDU<?>) hdu;
				int numberRows = table.getNRows();
				if (numberRows != numberElements)
				{
					String message = "Number of lines in FITS file " + file + " is " + numberRows + ", not " + numberElements + " as expected.";
					JOptionPane.showMessageDialog(null, message, "error", JOptionPane.ERROR_MESSAGE);
					throw new IOException(message);
				}

				ImmutableList<String> columnNames = findMatchingColumnNameCaseInsensitive(table, metadata.get(ELEMENT_NAMES));

				vtkFloatArray data = new vtkFloatArray();

				data.SetNumberOfComponents(1);
				data.SetNumberOfTuples(numberElements);

				float[] floatData = (float[]) table.getColumn(columnNames.get(0));
				for (int index = 0; index < numberElements; index++)
				{
					float value = floatData[index];
					data.SetTuple1(index, value);
				}

				this.data = data;
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
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
		{
			vtkFloatArray data = new vtkFloatArray();

			int numberElements = metadata.get(NUMBER_ELEMENTS);
			data.SetNumberOfComponents(1);
			data.SetNumberOfTuples(numberElements);

			String line;
			int index = 0;
			while ((line = in.readLine()) != null)
			{
				float value = Float.parseFloat(line);
				data.SetTuple1(index, value);
				++index;
			}

			if (index != numberElements)
			{
				String message = "Number of lines in text file " + file + " is " + index + ", not " + numberElements + " as expected.";
				JOptionPane.showMessageDialog(null, message, "error", JOptionPane.ERROR_MESSAGE);
				throw new IOException(message);
			}
			// Everything worked so assign to the data field.
			this.data = data;
		}

	}
}
