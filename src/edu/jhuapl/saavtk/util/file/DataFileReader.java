package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public abstract class DataFileReader
{
	public static abstract class FileFormatException extends Exception
	{
		private static final long serialVersionUID = -5934810207683523722L;

		public FileFormatException(String msg)
		{
			super(msg);
		}

		public FileFormatException(Exception e)
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

		public IncorrectFileFormatException(String msg)
		{
			super(msg);
		}

		public IncorrectFileFormatException(Exception e)
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

		public InvalidFileFormatException(String msg)
		{
			super(msg);
		}

		public InvalidFileFormatException(Exception e)
		{
			super(e);
		}
	}

	private static final IndexableTuple EMPTY_INDEXABLE = createEmptyIndexable();

	private static DataFileReader instance = null;

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

    public static DataFileReader multiFileFormatReader()
    {
        if (instance == null)
        {
            instance = createInstance();
        }
        return instance;
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
	    ImmutableList<DataFileReader> readers = ImmutableList.of(FitsFileReader.of(), CsvFileReader.of());

	    return new DataFileReader() {

            @Override
            public void checkFormat(File file) throws FileFormatException, IOException
            {
                Preconditions.checkNotNull(file);
                Preconditions.checkArgument(file.exists(), "File not found: " + file.getPath());

                for (DataFileReader reader : readers)
                {
                    try
                    {
                        reader.checkFormat(file);
                        return;
                    }
                    catch (IncorrectFileFormatException e)
                    {
                        // Ignore this exception and hope one of the other readers can handle this file.
                    }
                }
                // If execution reaches this point, all the handlers threw the "incorrect
                // format" exception.
                throw new IncorrectFileFormatException("Could not determine format from file " + file + "; tried FITS, VTK and CSV formats");
            }

            @Override
            public DataFileInfo readFileInfo(File file) throws IOException, FileFormatException
            {
                Preconditions.checkNotNull(file);
                Preconditions.checkArgument(file.exists(), "File not found: " + file.getPath());

                for (DataFileReader reader : readers)
                {
                    try
                    {
                        return reader.readFileInfo(file);
                    }
                    catch (IncorrectFileFormatException e)
                    {
                        // Ignore this exception and hope one of the other readers can handle this file.
                    }
                }
                // If execution reaches this point, all the handlers threw the "incorrect
                // format" exception.
                throw new IncorrectFileFormatException("Could not read file " + file + "; tried FITS, VTK and CSV formats");
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
			public Tuple get(int tupleIndex)
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
					public double get(int fieldIndex)
					{
						throw new IndexOutOfBoundsException();
					}

				};
			}

		};
	}
}
