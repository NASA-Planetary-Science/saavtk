package edu.jhuapl.saavtk.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.file.DataFileInfo.FileFormat;
import edu.jhuapl.saavtk.util.file.DataObjectInfo.Description;
import edu.jhuapl.saavtk.util.file.TableInfo.ColumnInfo;

/*
 * Reader for CSV files. Current format for accepted files is:
 * 
 *          [ name-0 ], [ name-1 ], ...
 *          [ units-0 ], [ units-1 ], ...
 *          value-0, value-1, ...
 * 
 * where the "name" and "units" lines are optional.
 * 
 * Proposed more general format (not yet implemented) for accepted files is:
 * 
 * Header - (optional) metadata. If present, it may have any number of lines, 
 *          but it must have the form:
 * 
 *          <CSV>, first-line-content, second-line-content ...
 *          first-line-value-0, first-line-value-1, ...
 *          second-line-value-0, second-line-value-1, ...
 *          ...
 * 
 *          The header is comma-delimited. All the lines except the first must have the
 *          same number of fields as the Data block (see below). The first line (including
 *          the <CSV>) must have M+1 fields, where M is the number of lines of *metadata*.
 * 
 * Data -   1 or more rows of comma-separated values. Leading and trailing whitespace is ignored
 *          for each value. Double quotes are optional, but may be used to include commas or
 *          whitespace within a value. Each row in the data block must have the same
 *          number of fields (commas). Individual blank fields are acceptable and are
 *          interpreted as null strings. Note however, that null may not be an acceptable
 *          value for all contexts.
 * 
 *          Example showing header and data:
 *          
 *          # This example shows how to give the name and units of each column in the header.
 *          <CSV>, Name, Units
 *          File, X, Y, Z, P, Q
 *                   ,  deg,  deg,  deg,     ,   kg-m/s^2
 *          file1.txt,   3.,   4.,   5.,  17.,   42.
 *          file2.pdf,  -4.,  -2.,    7,  14.,   9.8e1
 *          
 *          # This example shows how one would use a CSV file for imagefile/sumfile correspondence.
 *          <CSV>, title
 *          Image file, sum file
 *          file1.fits, file1.sum
 *          ...
 * 
 * Any line anywhere that begins with whitespace followed by a # is a comment and is ignored.
 * Blank lines are also ignored.
 * 
 */
public class CsvFileReader extends DataFileReader
{
	public static final FileFormat CSV_FORMAT = new FileFormat() {
		@Override
		public String toString()
		{
			return "CSV";
		}
	};

	private static final CsvFileReader INSTANCE = new CsvFileReader();

	/*
	 * This Pattern will match on either quoted text or text between commas,
	 * including whitespace, and accounting for beginning and end of line. Cribbed
	 * from a Stacktrace post.
	 */
	private static final Pattern CSV_PATTERN = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?:,|$)");

	/*
	 * CSV files do not have metadata such as key-value pairs, so their data objects
	 * will return blank descriptions.
	 */
	private static final Description BLANK_DESCRIPTION = Description.of(ImmutableList.of(), ImmutableList.of());

	public static CsvFileReader of()
	{
		return INSTANCE;
	}

	@Override
	public void checkFormat(File file) throws IOException, FileFormatException
	{
		String lowerCaseUnzippedName = file.toString().toLowerCase().replaceFirst("\\.gz$", "");
		if (!(lowerCaseUnzippedName.endsWith(".csv") || lowerCaseUnzippedName.endsWith(".txt")))
		{
			String fileType = file.toString().toLowerCase().replaceFirst("\\.gz$", "");
			fileType = fileType.replaceFirst(".*\\.", "");
			throw new IncorrectFileFormatException("File has unknown/unsupported type: " + fileType);
		}
	}

	@Override
	public DataFileInfo readFileInfo(File file) throws IOException, FileFormatException
	{
		checkFormat(file);

		if (isFileGzipped(file))
		{
			return readFileInfoGzipped(file);
		}
		else
		{
			return readFileInfoUncompressed(file);
		}
	}

	public IndexableTuple readTuples(File file, Iterable<Integer> columnNumbers) throws FieldNotFoundException, IOException
	{
		int numberColumns = checkColumnNumbers(columnNumbers);

		if (numberColumns == 0)
		{
			return EMPTY_INDEXABLE;
		}
		Preconditions.checkNotNull(file);
		Preconditions.checkArgument(file.exists());

		if (isFileGzipped(file))
		{
			return readTuplesGzipped(file, numberColumns, columnNumbers);
		}
		else
		{
			return readTuplesUncompressed(file, numberColumns, columnNumbers);
		}
	}

	protected DataFileInfo readFileInfoGzipped(File file) throws IOException, InvalidFileFormatException
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))))
		{
			return readFileInfo(file, in);
		}
	}

	protected DataFileInfo readFileInfoUncompressed(File file) throws IOException, InvalidFileFormatException
	{
		try (BufferedReader in = new BufferedReader(new FileReader(file)))
		{
			return readFileInfo(file, in);
		}
	}

	protected DataFileInfo readFileInfo(File file, BufferedReader in) throws IOException, InvalidFileFormatException
	{
		// Iterate over the whole file to validate and extract available metadata.
		int numberColumns = 0;
		int numberRows = 0;
		String line;
		ImmutableList<String> columnNames = null;
		ImmutableList<String> columnUnits = null;
		while ((line = in.readLine()) != null)
		{
			ImmutableList<String> row = parseCSV(line);
			numberColumns = getRowSize(row, numberColumns);
			try
			{
				getRowAsDoubles(row);
				++numberRows;
			}
			catch (@SuppressWarnings("unused") NumberFormatException e)
			{
				if (columnNames == null)
				{
					columnNames = row;
				}
				else if (columnUnits == null)
				{
					columnUnits = row;
				}
				else
				{
					++numberRows;
				}
			}
		}

		// Create column information.
		ImmutableList.Builder<ColumnInfo> builder = ImmutableList.builder();

		for (int index = 0; index < numberColumns; ++index)
		{
			String name = columnNames != null ? columnNames.get(index) : "";
			String units = columnUnits != null ? columnUnits.get(index) : "";
			builder.add(ColumnInfo.of(name, units));
		}
		ImmutableList<ColumnInfo> columnInfo = builder.build();

		return DataFileInfo.of(file, CSV_FORMAT, ImmutableList.of(TableInfo.of(file.getName(), BLANK_DESCRIPTION, numberRows, columnInfo)));
	}

	protected IndexableTuple readTuplesGzipped(File file, int numberColumns, Iterable<Integer> columnNumbers) throws FieldNotFoundException, IOException
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))))
		{
			return readTuples(file, in, numberColumns, columnNumbers);
		}
	}

	protected IndexableTuple readTuplesUncompressed(File file, int numberColumns, Iterable<Integer> columnNumbers) throws FieldNotFoundException, IOException
	{
		try (BufferedReader in = new BufferedReader(new FileReader(file)))
		{
			return readTuples(file, in, numberColumns, columnNumbers);
		}
	}

	protected IndexableTuple readTuples(File file, BufferedReader in, int numberColumns, Iterable<Integer> columnNumbers) throws FieldNotFoundException, IOException
	{
		final int numberCells = numberColumns;
		ImmutableList.Builder<ImmutableList<Double>> builder = ImmutableList.builder();

		// Parse the first line, which is interpreted as the column titles.
		String line = in.readLine();
		if (line == null)
		{
			throw new FieldNotFoundException("CSV file has no content: " + file);
		}

		ImmutableList<String> values = parseCSV(line);
		final int numberColumnsInTable = values.size();
		for (Integer columnNumber : columnNumbers)
		{
			if (Integer.compare(numberColumnsInTable, columnNumber) <= 0)
			{
				throw new FieldNotFoundException("Cannot get column #" + columnNumber + "; CSV file has only " + numberColumnsInTable + " columns in file " + file);
			}
		}

		final ImmutableList<String> names = getRowSliceAsStrings(values, columnNumbers);

		try
		{
			// Also try to interpret the first row as numbers, since files
			// may actually not have titles.
			builder.add(getRowSliceAsDoubles(values, columnNumbers));
		}
		catch (@SuppressWarnings("unused") NumberFormatException e)
		{
			// Must be just column titles -- just skip this one.
		}

		while ((line = in.readLine()) != null)
		{
			values = parseCSV(line);
			if (values.size() != numberColumnsInTable)
			{
				throw new IOException("Line in CSV file: " + line + " does not have " + numberColumnsInTable + " values in file " + file);
			}
			try
			{
				builder.add(getRowSliceAsDoubles(values, columnNumbers));
			}
			catch (NumberFormatException e)
			{
				throw new IOException(e);
			}
		}

		final ImmutableList<ImmutableList<Double>> valuesList = builder.build();
		final int numberRecords = valuesList.size();

		return new IndexableTuple() {

			@Override
			public int getNumberCells()
			{
				return numberCells;
			}

			@Override
			public String getName(int cellIndex)
			{
				return names.get(cellIndex);
			}

			@Override
			public String getUnits(@SuppressWarnings("unused") int cellIndex)
			{
				return "";
			}

			@Override
			public int size()
			{
				return numberRecords;
			}

			@Override
			public Tuple get(int index)
			{
				return new Tuple() {

					@Override
					public int size()
					{
						return numberCells;
					}

					@Override
					public String getAsString(int cellIndex)
					{
						return Double.toString(get(cellIndex));
					}

					@Override
					public double get(int cellIndex)
					{
						return valuesList.get(index).get(cellIndex);
					}
				};
			}

		};

	}

	private int getRowSize(ImmutableList<?> row, int rowSize) throws InvalidFileFormatException
	{
		if (rowSize == 0)
		{
			rowSize = row.size();
		}
		else
		{
			if (row.size() != rowSize)
			{
				throw new InvalidFileFormatException("Inconsistent number of columns in row of CSV file");
			}
		}
		return rowSize;
	}

	private ImmutableList<Double> getRowAsDoubles(ImmutableList<String> line) throws NumberFormatException
	{
		ImmutableList.Builder<Double> builder = ImmutableList.builder();
		for (String cell : line)
		{
			builder.add(Double.parseDouble(cell));
		}
		return builder.build();
	}

	private ImmutableList<String> getRowSliceAsStrings(ImmutableList<String> line, Iterable<Integer> columnNumbers)
	{
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (Integer column : columnNumbers)
		{
			builder.add(line.get(column));
		}
		return builder.build();
	}

	private ImmutableList<Double> getRowSliceAsDoubles(ImmutableList<String> line, Iterable<Integer> columnNumbers) throws NumberFormatException
	{
		ImmutableList.Builder<Double> builder = ImmutableList.builder();
		for (Integer column : columnNumbers)
		{
			builder.add(Double.parseDouble(line.get(column)));
		}
		return builder.build();
	}

	private static ImmutableList<String> parseCSV(String line)
	{
		Matcher matcher = CSV_PATTERN.matcher(line);
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		while (matcher.find())
		{
			String match = matcher.group(1);
			if (match != null)
			{
				builder.add(match);
			}
			else
			{
				builder.add(matcher.group(2));
			}
		}

		return builder.build();
	}

}
