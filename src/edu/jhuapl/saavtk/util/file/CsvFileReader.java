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
 * Reader for CSV files. Required format for accepted files is:
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
 * CURRENTLY THIS READER DOES NOT SUPPORT THIS FORMAT! Instead it parses the first line of
 * the file as strings to form the "column names". If there is no top line defining column names,
 * the names will be the values of the first line of data. If the first line consists only
 * of numbers, the first line is also included in the data. Thus this works for legacy coloring
 * files, and would work for plain 3-column CSV vector coloring data.
 */
public class CsvFileReader extends DataFileReader
{
	private static final CsvFileReader INSTANCE = new CsvFileReader();

	/*
	 * This Pattern will match on either quoted text or text between commas,
	 * including whitespace, and accounting for beginning and end of line. Cribbed
	 * from a Stacktrace post.
	 */
	private static final Pattern CSV_PATTERN = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?:,|$)");

	public static CsvFileReader of()
	{
		return INSTANCE;
	}

	@Override
	public DataFileInfo readFileInfo(File file) throws IncorrectFileFormatException, IOException
	{
		if (file.toString().toLowerCase().endsWith(".gz"))
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

		if (file.toString().toLowerCase().endsWith(".gz"))
		{
			return readTuplesGzipped(file, numberColumns, columnNumbers);
		}
		else
		{
			return readTuplesUncompressed(file, numberColumns, columnNumbers);
		}
	}

	protected DataFileInfo readFileInfoGzipped(File file) throws IOException
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))))
		{
			return readFileInfo(file, in);
		}
	}

	protected DataFileInfo readFileInfoUncompressed(File file) throws IOException
	{
		try (BufferedReader in = new BufferedReader(new FileReader(file)))
		{
			return readFileInfo(file, in);
		}
	}

	protected DataFileInfo readFileInfo(File file, BufferedReader in) throws IOException
	{
		// Parse the first line, which is interpreted as the column titles.
		String line = in.readLine();
		if (line == null)
		{
			throw new IOException("File is empty");
		}

		ImmutableList.Builder<ColumnInfo> builder = ImmutableList.builder();
		boolean firstLineAllNumbers = true;
		for (String columnName : parseCSV(line))
		{
			builder.add(ColumnInfo.of(columnName, ""));
			try
			{
				Double.parseDouble(columnName);
			}
			catch (NumberFormatException e)
			{
				firstLineAllNumbers = false;
			}
		}
		ImmutableList<ColumnInfo> columnInfo = builder.build();
		final int numberColumns = columnInfo.size();

		int numberRows = firstLineAllNumbers ? 1 : 0;
		while ((line = in.readLine()) != null)
		{
			int numberColumnsInLine = parseCSV(line).size();
			if (numberColumnsInLine != numberColumns)
			{
				throw new IOException("Number of columns is " + numberColumnsInLine + ", not " + numberColumns + " as expected in line " + line);
			}
			++numberRows;
		}

		return DataFileInfo.of(file, FileFormat.CSV, ImmutableList.of(TableInfo.of(file.getName(), Description.of(ImmutableList.of(), ImmutableList.of()), numberRows, columnInfo)));
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

		final ImmutableList<String> names = getColumnValues(values, columnNumbers);

		try
		{
			// Also try to interpret the first row as numbers, since files
			// may actually not have titles.
			builder.add(stringsToDoubles(values, columnNumbers));
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
				builder.add(stringsToDoubles(values, columnNumbers));
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
					public double get(int cellIndex)
					{
						return valuesList.get(index).get(cellIndex);
					}
				};
			}

		};

	}

	private ImmutableList<String> getColumnValues(ImmutableList<String> line, Iterable<Integer> columnNumbers)
	{
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (Integer column : columnNumbers)
		{
			builder.add(line.get(column));
		}
		return builder.build();
	}

	private static ImmutableList<Double> stringsToDoubles(ImmutableList<String> line, Iterable<Integer> columnNumbers) throws NumberFormatException
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
