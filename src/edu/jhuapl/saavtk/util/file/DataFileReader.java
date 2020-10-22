package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

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

	private static final IndexableTuple EMPTY_INDEXABLE = createEmptyIndexable();

	private static final DataFileReader INSTANCE = createInstance();

	public static IndexableTuple emptyIndexable()
	{
	    return EMPTY_INDEXABLE;
	}
	
    public static File gunzip(File file) throws IOException
    {
        String gzippedFileName = file.toString();
        String unGzippedFileName = gzippedFileName.replaceFirst("\\.[gG][Z]$", "");
        Preconditions.checkArgument(!gzippedFileName.equals(unGzippedFileName), "File " + gzippedFileName + " does not appear to be Gzipped");

        File result = new File(unGzippedFileName);

        if (!result.isFile())
        {
            try (GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(file)))
            {
                try (FileWriter outputStream = new FileWriter(result))
                {
                    IOUtils.copy(inputStream, outputStream);
                }
            }
            catch (Exception e)
            {
                result.delete();
                throw e;
            }
        }

        return result;
    }

	public static DataFileReader of()
	{
		return INSTANCE;
	}

	public abstract void checkFormat(File file) throws IOException, FileFormatException;

	public abstract DataFileInfo readFileInfo(File file) throws IOException, FileFormatException;

	protected boolean isFileGzipped(File file)
	{
		return file.getName().toLowerCase().endsWith(".gz");
	}

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
			public void checkFormat(File file) throws FileFormatException, IOException
			{
				Preconditions.checkNotNull(file);
				Preconditions.checkArgument(file.exists(), "File not found: " + file.getPath());

				try
				{
					FitsFileReader.of().checkFormat(file);
				}
				catch (@SuppressWarnings("unused") IncorrectFileFormatException e)
				{
					CsvFileReader.of().checkFormat(file);
				}
			}

			@Override
			public DataFileInfo readFileInfo(File file) throws IOException, FileFormatException
			{
				Preconditions.checkNotNull(file);
				Preconditions.checkArgument(file.exists(), "File not found: " + file.getPath());

				try
				{
					return FitsFileReader.of().readFileInfo(file);
				}
				catch (@SuppressWarnings("unused") IncorrectFileFormatException e)
				{
					return CsvFileReader.of().readFileInfo(file);
				}
			}

		};
	}

	private static IndexableTuple createEmptyIndexable()
	{
	    double[] emptyArray = new double[0];

	    return new IndexableTuple() {
			@Override
			public int getNumberFields()
			{
				return 0;
			}

			@Override
			public String getName(int fieldIndex)
			{
				throw new IndexOutOfBoundsException();
			}

			@Override
			public String getUnits(int fieldIndex)
			{
				throw new IndexOutOfBoundsException();
			}

			@Override
			public int size()
			{
				return 0;
			}

			@Override
			public Tuple get(int index)
			{
				return new Tuple() {

					@Override
					public int size()
					{
						return 0;
					}

					@Override
                    public double[] get()
					{
					    return emptyArray;
					}

					@Override
					public String getAsString(int fieldIndex)
					{
						throw new IndexOutOfBoundsException();
					}

					@Override
					public double get(int fieldIndex)
					{
						throw new IndexOutOfBoundsException();
					}

				};
			}

		};
	}
}
