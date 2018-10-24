package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;

public abstract class DataFileReader
{
	public static abstract class FileFormatException extends Exception
	{
		private static final long serialVersionUID = -5934810207683523722L;

		FileFormatException(String msg)
		{
			super(msg);
		}

		FileFormatException(Exception e)
		{
			super(e);
		}
	}

	/**
	 * This exception indicates an attempt to by a subclass to read a file of the
	 * wrong format, e.g., if a FITS reader attempts to read a CSV file.
	 */
	public static final class IncorrectFileFormatException extends FileFormatException
	{
		private static final long serialVersionUID = -3268081959880597315L;

		IncorrectFileFormatException(String msg)
		{
			super(msg);
		}

		IncorrectFileFormatException(Exception e)
		{
			super(e);
		}
	}

	/**
	 * This exception indicates a problem with the format of the file being read,
	 * e.g., inconsistent numbers of columns in a table.
	 */
	public static final class InvalidFileFormatException extends FileFormatException
	{
		private static final long serialVersionUID = -8918875089497257976L;

		InvalidFileFormatException(String msg)
		{
			super(msg);
		}

		InvalidFileFormatException(Exception e)
		{
			super(e);
		}
	}

	protected static final IndexableTuple EMPTY_INDEXABLE = createEmptyIndexable();

	private static final DataFileReader INSTANCE = createInstance();

	public static DataFileReader of()
	{
		return INSTANCE;
	}

	public abstract DataFileInfo readFileInfo(File file) throws IOException, IncorrectFileFormatException, InvalidFileFormatException;

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
			public DataFileInfo readFileInfo(File file) throws IOException, IncorrectFileFormatException, InvalidFileFormatException
			{
				Preconditions.checkNotNull(file);
				Preconditions.checkArgument(file.exists(), "File not found: " + file.getPath());

				try
				{
					return FitsFileReader.of().readFileInfo(file);
				}
				catch (@SuppressWarnings("unused") IncorrectFileFormatException e)
				{
					// Fall through to avoid nesting try-catch.
				}

				String lowerCaseUnzippedName = file.toString().toLowerCase().replaceFirst("\\.gz$", "");
				if (lowerCaseUnzippedName.endsWith(".csv") || lowerCaseUnzippedName.endsWith(".txt"))
				{
					return CsvFileReader.of().readFileInfo(file);
				}

				String fileType = file.toString().replaceFirst("\\.gz$", "");
				fileType = fileType.replaceFirst(".*\\.", "");
				throw new IncorrectFileFormatException("File has unsupported type: " + fileType);
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
					public String getAsString(@SuppressWarnings("unused") int cellIndex)
					{
						throw new IndexOutOfBoundsException();
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
