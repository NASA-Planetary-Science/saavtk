package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.FixedMetadata;

public abstract class DataFileReader
{
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

	protected static final IndexableTuple EMPTY_INDEXABLE = createEmptyIndexable();
	protected static final FixedMetadata EMPTY_TABLE_METADATA = FixedMetadata.of(FileMetadata.createColumnsMetadata(ImmutableList.of()));
	protected static final FileMetadata EMPTY_FILE_METADATA = FileMetadata.of(ImmutableList.of());

	private static final DataFileReader INSTANCE = createInstance();

	public static DataFileReader of()
	{
		return INSTANCE;
	}

	public abstract DataFileInfo readFileInfo(File file) throws IncorrectFileFormatException, IOException;

	public abstract FileMetadata readMetadata(File file) throws IncorrectFileFormatException, IOException;

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

	private static DataFileReader createInstance()
	{
		return new DataFileReader() {

			@Override
			public DataFileInfo readFileInfo(File file) throws IncorrectFileFormatException, IOException
			{
				Preconditions.checkNotNull(file);
				Preconditions.checkArgument(file.exists());

				try
				{
					return FitsFileReader.of().readFileInfo(file);
				}
				catch (@SuppressWarnings("unused") IncorrectFileFormatException e)
				{
					return CsvFileReader.of().readFileInfo(file);
				}
			}

			@Override
			public FileMetadata readMetadata(File file) throws IncorrectFileFormatException, IOException
			{
				Preconditions.checkNotNull(file);
				Preconditions.checkArgument(file.exists());

				try
				{
					return FitsFileReader.of().readMetadata(file);
				}
				catch (@SuppressWarnings("unused") IncorrectFileFormatException e)
				{
					return CsvFileReader.of().readMetadata(file);
				}
			}
		};
	}

	private static IndexableTuple createEmptyIndexable()
	{
		return new IndexableTuple() {
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
	}
}
