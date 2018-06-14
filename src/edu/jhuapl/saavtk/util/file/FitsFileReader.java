package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.util.file.DataObjectInfo.Description;
import edu.jhuapl.saavtk.util.file.DataObjectInfo.InfoElements;
import edu.jhuapl.saavtk.util.file.TableInfo.ColumnInfo;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.TableHDU;
import nom.tam.util.Cursor;

public final class FitsFileReader extends DataFileReader
{
	public static final Version VERSION = Version.of(0, 1);

	// These are the fields from a FITS keyword other than the name of the keyword.
	private static final ImmutableList<String> FITS_KEYWORD_FIELDS = ImmutableList.of("Keyword", "Value", "Comment");

	private static final FitsFileReader INSTANCE = new FitsFileReader();

	public static FitsFileReader of()
	{
		return INSTANCE;
	}

	@Override
	public DataFileInfo readFileInfo(File file) throws IncorrectFileFormatException, IOException
	{
		Preconditions.checkNotNull(file);

		try (Fits fits = new Fits(file))
		{
			BasicHDU<?>[] hdus = fits.read();
			ImmutableList.Builder<DataObjectInfo> builder = ImmutableList.builder();

			for (int hduNum = 0; hduNum < hdus.length; ++hduNum)
			{
				DataObjectInfo hduInfo = readInfo(hdus[hduNum], hduNum);
				builder.add(hduInfo);
			}
			return DataFileInfo.of(builder.build());
		}
		catch (FitsException e)
		{
			throw new IncorrectFileFormatException(e);
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
	 * @throws IncorrectFileFormatException if the file appears not to be a FITS
	 *             file
	 * @throws IOException if any other error occurs in reading the slice of the
	 *             table identified by the arguments
	 */
	public IndexableTuple readTuples(File file, int tableHduNumber, Iterable<Integer> columnNumbers) throws IncorrectFileFormatException, FieldNotFoundException, IOException
	{
		Preconditions.checkNotNull(file);
		Preconditions.checkArgument(file.exists());
		Preconditions.checkArgument(tableHduNumber >= 0); // Handle the case of primary HDU below.
		checkColumnNumbers(columnNumbers);

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
			throw new IncorrectFileFormatException(e);
		}
	}

	private interface GettableAsDouble
	{
		double get(int cellIndex);
	}

	private IndexableTuple readTuples(TableHDU<?> table, int tableHduNumber, Iterable<Integer> columnNumbers) throws FitsException, FieldNotFoundException, IOException
	{
		// Read file info first so we process the table in the natural FITS order: header then data.
		TableInfo dataObjectInfo = (TableInfo) readInfo(table, tableHduNumber);

		final int numberRecords = table.getNRows();
		final int numberColumnsInTable = table.getNCols();

		ImmutableList.Builder<GettableAsDouble> columnBuilder = ImmutableList.builder();
		for (Integer columnNumber : columnNumbers)
		{
			if (Integer.compare(numberColumnsInTable, columnNumber) <= 0)
			{
				throw new FieldNotFoundException("Cannot get column #" + columnNumber + " from FITS table/HDU #" + tableHduNumber + ", which has only " + numberColumnsInTable + " columns");
			}

			Object column = table.getColumn(columnNumber);
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
		}
		final ImmutableList<GettableAsDouble> columns = columnBuilder.build();

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
				return dataObjectInfo.getColumnInfo(cellIndex).getName();
			}

			@Override
			public String getUnits(int cellIndex)
			{
				return dataObjectInfo.getColumnInfo(cellIndex).getUnits();
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

	private DataObjectInfo readInfo(BasicHDU<?> hdu, int hduNumber) throws IOException
	{
		Header header = hdu.getHeader();

		// Derive the title.
		String extName = header.getStringValue("EXTNAME");
		boolean isPrimary = extName == null && header.getBooleanValue("SIMPLE");
		final String title = extName != null ? extName : isPrimary ? "Primary Image" : "HDU " + hduNumber;

		// Put all the keywords in the data object info.
		Cursor<String, HeaderCard> iterator = header.iterator();
		ImmutableList.Builder<InfoElements> infoElementsBuilder = ImmutableList.builder();
		while (iterator.hasNext())
		{
			HeaderCard card = iterator.next();
			ArrayList<String> keywordInfo = new ArrayList<>();
			keywordInfo.add(card.getKey());
			keywordInfo.add(card.getValue());
			keywordInfo.add(card.getComment());
			infoElementsBuilder.add(InfoElements.of(keywordInfo));
		}
		Description description = Description.of(FITS_KEYWORD_FIELDS, infoElementsBuilder.build());

		if (hdu instanceof TableHDU)
		{
			// Read structural metadata about table.
			TableHDU<?> table = (TableHDU<?>) hdu;
			ImmutableList.Builder<ColumnInfo> columnInfoBuilder = ImmutableList.builder();
			for (int columnIndex = 0; columnIndex < table.getNCols(); ++columnIndex)
			{
				String name = table.getColumnName(columnIndex);
				String units = table.getColumnMeta(columnIndex, "TUNIT");
				columnInfoBuilder.add(ColumnInfo.of(name, units != null ? units : ""));
			}

			// Return the data object metadata combined with the column metadata.
			return TableInfo.of(title, description, columnInfoBuilder.build());
		}

		// Not a table, so just return the data object metadata.
		return DataObjectInfo.of(title, description);
	}

}
