package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.TableHDU;

public final class FitsFileReader extends FileReader
{
	/**
	 * This exception indicates an attempt to access a non-FITS file using a FITS
	 * file reader.
	 */
	public static final class NotFitsFileException extends Exception
	{
		private static final long serialVersionUID = -3268081959880597315L;

		private NotFitsFileException(Exception e)
		{
			super(e);
		}
	}

	/**
	 * Read a set of columns from a table from a FITS file, returning an object that
	 * provides random access to the table elements. The columns are all loaded into
	 * memory.
	 * 
	 * @param file the file containing the table
	 * @param tableHduNumber the HDU number of the table extension
	 * @param columnNumbers the column numbers to read
	 * @return a row-oriented accessor to the slice of the table identified by the
	 *         arguments
	 * @throws NotFitsFileException if the file appears not to be a FITS file
	 * @throws IOException if any other error occurs in reading the slice of the
	 *             table identified by the arguments
	 */
	public static IndexableTuple readTuples(File file, int tableHduNumber, Iterable<Integer> columnNumbers) throws NotFitsFileException, FieldNotFoundException, IOException
	{
		Preconditions.checkNotNull(file);
		Preconditions.checkArgument(file.exists());
		Preconditions.checkArgument(tableHduNumber >= 0); // Catch the case of primary HDU below.
		int numberColumns = checkColumnNumbers(columnNumbers);

		if (numberColumns == 0)
		{
			return EMPTY_INDEXABLE;
		}

		try (Fits fits = new Fits(file))
		{
			BasicHDU<?>[] hdus = fits.read();

			if (hdus == null)
			{
				throw new IOException("No HDUs in FITS file " + file);
			}

			if (tableHduNumber >= hdus.length)
			{
				throw new IOException("Cannot get table #" + tableHduNumber + "; only " + hdus.length + " HDUs in file " + file);
			}

			try
			{
				BasicHDU<?> hdu = hdus[tableHduNumber];
				if (hdu instanceof TableHDU)
				{
					return readTuples((TableHDU<?>) hdu, tableHduNumber, columnNumbers);
				}
				else
				{
					throw new IOException("HDU #" + tableHduNumber + " is not a table in FITS file " + file);
				}
			}
			catch (FitsException e)
			{
				// This exception was thrown after the file was successfully opened and processed
				// up to some point, so convert it here to an IOException and re-throw it.
				throw new IOException("Exception loading coloring from file " + file, e);
			}
		}
		catch (FitsException e)
		{
			// This can only come from fits.read(), probably a corrupt FITS header
			// error. Since the file is known to exist, the most likely cause
			// is that it's not a FITS file.
			throw new NotFitsFileException(e);
		}
	}

	private interface GettableAsDouble
	{
		double get(int cellIndex);
	}

	private static IndexableTuple readTuples(TableHDU<?> table, int tableHduNumber, Iterable<Integer> columnNumbers) throws FitsException, FieldNotFoundException, IOException
	{
		final int numberRecords = table.getNRows();
		final int numberColumnsInTable = table.getNCols();

		ImmutableList.Builder<GettableAsDouble> columnBuilder = ImmutableList.builder();
		ImmutableList.Builder<String> nameBuilder = ImmutableList.builder();
		final List<String> unitsList = new ArrayList<>(); // Needs to accept null values.

		for (Integer columnNumber : columnNumbers)
		{
			if (Integer.compare(numberColumnsInTable, columnNumber) <= 0)
			{
				throw new FieldNotFoundException("Cannot get column #" + columnNumber + " from FITS table/HDU #" + tableHduNumber + ", which has only " + numberColumnsInTable + " columns");
			}

			Object column = table.getColumn(columnNumber);
			String name = table.getColumnName(columnNumber);
			String units = table.getColumnMeta(columnNumber, "TUNIT");
			if (column instanceof double[])
			{
				columnBuilder.add((cellIndex) -> {
					return ((double[]) column)[cellIndex];
				});
			}
			else if (column instanceof float[])
			{
				columnBuilder.add((cellIndex) -> {
					return ((float[]) column)[cellIndex];
				});
			}
			else if (column instanceof int[])
			{
				columnBuilder.add((cellIndex) -> {
					return ((int[]) column)[cellIndex];
				});
			}
			else if (column instanceof long[])
			{
				columnBuilder.add((cellIndex) -> {
					return ((long[]) column)[cellIndex];
				});
			}
			else
			{
				throw new IOException("Column #" + columnNumber + " from FITS table/HDU #" + tableHduNumber + " is not a supported numeric array type");
			}
			nameBuilder.add(name);
			unitsList.add(units);
		}

		final ImmutableList<GettableAsDouble> columns = columnBuilder.build();
		final ImmutableList<String> names = nameBuilder.build();

		// This is a bit of paranoid defensive programming. Should any changes
		// to the above code result in violating this invariant, detect it here.
		if (columns.size() != names.size() || columns.size() != unitsList.size())
		{
			throw new AssertionError();
		}

		final int numberCells = columns.size();

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
			public String getUnits(int cellIndex)
			{
				return unitsList.get(cellIndex);
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
						return columns.get(cellIndex).get(index);
					}
				};
			}

		};
	}

	private FitsFileReader()
	{
		throw new AssertionError();
	}
}
