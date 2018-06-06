package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.TableHDU;
import vtk.vtkFloatArray;

abstract class FitsLoadableTable extends LoadableTable
{
	private final File file;
	private final int tableHduNumber;
	private final ImmutableList<Integer> columnNumbers;

	protected FitsLoadableTable(File file, int tableHduNumber, ImmutableList<Integer> columnNumbers)
	{
		Preconditions.checkNotNull(file);
		Preconditions.checkNotNull(columnNumbers);

		Preconditions.checkArgument(tableHduNumber >= 0);

		final int numberColumns = columnNumbers.size();
		Preconditions.checkArgument(numberColumns == 1 || numberColumns == 2 || numberColumns == 3 || numberColumns == 4 || numberColumns == 6 || numberColumns == 9);

		for (Integer columnNumber : columnNumbers)
		{
			Preconditions.checkArgument(columnNumber >= 0);
		}

		this.file = file;
		this.tableHduNumber = tableHduNumber;
		this.columnNumbers = columnNumbers;
	}

	protected abstract void setTuple(ImmutableList<float[]> columns, int index, vtkFloatArray data);

	@Override
	public void doLoad() throws IOException
	{
		try (Fits fits = new Fits(file))
		{
			fits.read();
			BasicHDU<?> hdu = fits.getHDU(tableHduNumber);
			if (hdu instanceof TableHDU)
			{
				TableHDU<?> table = (TableHDU<?>) hdu;
				final int numberRecords = table.getNRows();
				final int numberColumns = table.getNCols();
				ImmutableList.Builder<float[]> builder = ImmutableList.builder();
				for (Integer columnNumber : columnNumbers)
				{
					if (Integer.compare(numberColumns, columnNumber) < 0)
					{
						throw new IOException("Cannot get column #" + columnNumber + " from FITS table/HDU #" + tableHduNumber + ", which has only " + numberColumns + " columns");
					}

					Object column = table.getColumn(columnNumber);
					if (column instanceof float[])
					{
						builder.add((float[]) column);
					}
					else
					{
						throw new IOException("Column #" + columnNumber + " from FITS table/HDU #" + tableHduNumber + " is not an array of float");
					}
				}
				ImmutableList<float[]> columns = builder.build();

				vtkFloatArray data = new vtkFloatArray();
				data.SetNumberOfComponents(columnNumbers.size());
				data.SetNumberOfTuples(numberRecords);
				for (int index = 0; index < numberRecords; index++)
				{
					setTuple(columns, index, data);
				}

				set(data, data.GetRange());
			}
			else
			{
				throw new IOException("HDU #" + tableHduNumber + " is not a table in file " + file);
			}

		}
		catch (FitsException e)
		{
			throw new IOException(e);
		}
	}

}
