package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.TableHDU;

abstract class FitsLoadableTable extends LoadableTable
{
	private final File file;
	private final int tableHduNumber;

	protected FitsLoadableTable(File file, int tableHduNumber, ImmutableList<Integer> columnIdentifiers)
	{
		super(columnIdentifiers);
		Preconditions.checkNotNull(file);
		Preconditions.checkArgument(tableHduNumber > 0); // 0 is the primary.

		this.file = file;
		this.tableHduNumber = tableHduNumber;
	}

	@Override
	public ImmutableList<ImmutableList<Double>> doLoad() throws IOException
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
				ImmutableList.Builder<float[]> columnBuilder = ImmutableList.builder();

				@SuppressWarnings("unchecked")
				ImmutableList<Integer> columnNumbers = (ImmutableList<Integer>) getColumnIdentifiers();
				for (Integer columnNumber : columnNumbers)
				{
					if (Integer.compare(numberColumns, columnNumber) < 0)
					{
						throw new IOException("Cannot get column #" + columnNumber + " from FITS table/HDU #" + tableHduNumber + ", which has only " + numberColumns + " columns");
					}

					Object column = table.getColumn(columnNumber);
					if (column instanceof float[])
					{
						columnBuilder.add((float[]) column);
					}
					else
					{
						throw new IOException("Column #" + columnNumber + " from FITS table/HDU #" + tableHduNumber + " is not an array of float");
					}
				}
				ImmutableList<float[]> columns = columnBuilder.build();
				ImmutableList.Builder<ImmutableList<Double>> tupleBuilder = ImmutableList.builder();

				for (int rowIndex = 0; rowIndex < numberRecords; ++rowIndex)
				{
					ImmutableList.Builder<Double> rowBuilder = ImmutableList.builder();
					for (int columnIndex = 0; columnIndex < numberColumns; ++columnIndex)
					{
						rowBuilder.add((double) columns.get(columnIndex)[rowIndex]);
					}
					tupleBuilder.add(rowBuilder.build());
				}
				return tupleBuilder.build();
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

	private ImmutableList<String> findMatchingColumnNameCaseInsensitive(TableHDU<?> table, List<String> columnNames) throws IOException
	{
		// Make a map of the table's actual file names, keyed on the all uppercase version of each name.
		Map<String, String> tableColumnNames = new HashMap<>();
		for (int index = 0; index < table.getNCols(); ++index)
		{
			String columnName = table.getColumnName(index);
			tableColumnNames.put(columnName.toUpperCase(), columnName);
		}

		// Use the table map to look up the case-sensitive names that match the input column names.
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (String columnName : columnNames)
		{
			String tableColumnName = tableColumnNames.get(columnName.toUpperCase());
			if (tableColumnName != null)
			{
				builder.add(tableColumnName);
			}
			else
			{
				throw new IOException("Cannot find a column with name matching " + columnName.toUpperCase());
			}
		}
		return builder.build();
	}

}
