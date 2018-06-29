package edu.jhuapl.saavtk.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

abstract class CSVLoadableTable extends LoadableTable
{
	/*
	 * This Pattern will match on either quoted text or text between commas,
	 * including whitespace, and accounting for beginning and end of line. Cribbed
	 * from a Stacktrace post.
	 */
	private static final Pattern CSV_PATTERN = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?:,|$)");

	private final File file;

	protected CSVLoadableTable(File file, ImmutableList<String> columnIdentifiers)
	{
		super(columnIdentifiers);
		Preconditions.checkNotNull(file);
		this.file = file;
	}

	@Override
	protected ImmutableList<ImmutableList<Double>> doLoad() throws IOException
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
		{
			ImmutableList.Builder<ImmutableList<Double>> builder = ImmutableList.builder();

			// Parse the first line
			String line = in.readLine();
			String[] values = parseCSV(line);
			final int numberComponents = values.length;
			try
			{
				builder.add(stringsToDoubles(values));
			}
			catch (@SuppressWarnings("unused") NumberFormatException e)
			{
				// Must be column titles -- just skip this one.
			}

			while ((line = in.readLine()) != null)
			{
				values = parseCSV(line);
				if (values.length != numberComponents)
				{
					throw new IOException("Line in CSV file: " + line + " does not have " + numberComponents + " values in file " + file);
				}
				try
				{
					builder.add(stringsToDoubles(values));
				}
				catch (NumberFormatException e)
				{
					throw new IOException(e);
				}
			}

			return builder.build();
		}
	}

	protected ImmutableList<Double> stringsToDoubles(String[] line) throws NumberFormatException
	{
		if (line == null)
		{
			return null;
		}
		ImmutableList.Builder<Double> builder = ImmutableList.builder();
		for (String cell : line)
		{
			builder.add(Double.parseDouble(cell));
		}
		return builder.build();
	}

	protected String[] parseCSV(String line)
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

}
