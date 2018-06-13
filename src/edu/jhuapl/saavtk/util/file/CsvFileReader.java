package edu.jhuapl.saavtk.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.FixedMetadata;

public class CsvFileReader extends FileReader
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
	public FileMetadata readMetadata(@SuppressWarnings("unused") File file) throws IOException
	{
		return EMPTY_FILE_METADATA;
	}

	public IndexableTuple readTuples(File file, Iterable<Integer> columnNumbers) throws FieldNotFoundException, IOException
	{
		Preconditions.checkNotNull(file);
		Preconditions.checkArgument(file.exists());
		int numberColumns = checkColumnNumbers(columnNumbers);

		if (numberColumns == 0)
		{
			return EMPTY_INDEXABLE;
		}

		final int numberCells = numberColumns;

		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
		{
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
				public FixedMetadata getMetadata()
				{
					return EMPTY_TABLE_METADATA;
				}

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
