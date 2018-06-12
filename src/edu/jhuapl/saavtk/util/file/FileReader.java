package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;

public abstract class FileReader
{
	protected static final FixedMetadata EMPTY_METADATA = FixedMetadata.of(SettableMetadata.of(Version.of(1, 0), ImmutableList.of(FileMetadata.DATA_OBJECTS), ImmutableMap.of(FileMetadata.DATA_OBJECTS, ImmutableList.of())));

	/**
	 * This exception indicates an attempt to access a non-FITS file using a FITS
	 * file reader.
	 */
	public static final class IncorrectFileFormatException extends Exception
	{
		private static final long serialVersionUID = -3268081959880597315L;

		IncorrectFileFormatException(Exception e)
		{
			super(e);
		}
	}

	private static final FileReader INSTANCE = createInstance();

	public static FileReader of()
	{
		return INSTANCE;
	}

	public abstract Metadata readMetadata(File file) throws IncorrectFileFormatException, IOException;

	protected static final IndexableTuple EMPTY_INDEXABLE = new IndexableTuple() {
		@Override
		public Metadata getMetadata()
		{
			return EMPTY_METADATA;
		}

		@Override
		public int getNumberCells()
		{
			return 0;
		}

		@Override
		public String getName(@SuppressWarnings("unused") int cellIndex)
		{
			throw new IndexOutOfBoundsException();
		}

		@Override
		public String getUnits(@SuppressWarnings("unused") int cellIndex)
		{
			throw new IndexOutOfBoundsException();
		}

		@Override
		public int size()
		{
			return 0;
		}

		@Override
		public Tuple get(@SuppressWarnings("unused") int index)
		{
			return new Tuple() {

				@Override
				public int size()
				{
					return 0;
				}

				@Override
				public double get(@SuppressWarnings("unused") int cellIndex)
				{
					throw new IndexOutOfBoundsException();
				}

			};
		}

	};

	protected static int checkColumnNumbers(Iterable<Integer> columnNumbers)
	{
		Preconditions.checkNotNull(columnNumbers);

		int numberColumns = 0;
		for (Integer column : columnNumbers)
		{
			Preconditions.checkArgument(column >= 0);
			++numberColumns;
		}
		return numberColumns;
	}

	private static FileReader createInstance()
	{
		return new FileReader() {

			@Override
			public Metadata readMetadata(File file) throws IOException
			{
				Preconditions.checkNotNull(file);
				Preconditions.checkArgument(file.exists());

				try
				{
					return FitsFileReader.of().readMetadata(file);
				}
				catch (@SuppressWarnings("unused") IncorrectFileFormatException e)
				{
					// Fall through to see if it's a CSV file.
				}
				return CsvFileReader.of().readMetadata(file);
			}

		};
	}

}
