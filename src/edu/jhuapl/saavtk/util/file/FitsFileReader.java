package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.TableHDU;
import nom.tam.util.Cursor;

public final class FitsFileReader extends FileReader
{
	public static final Version VERSION = Version.of(0, 1);

	// These are the fields from a FITS keyword other than the name of the keyword.
	private static final List<String> FITS_KEYWORD_FIELDS = ImmutableList.of("Value", "Comment");

	private static final FitsFileReader INSTANCE = new FitsFileReader();

	public static FitsFileReader of()
	{
		return INSTANCE;
	}

	@Override
	public FixedMetadata readMetadata(File file) throws IOException, FileReader.IncorrectFileFormatException
	{
		FixedMetadata result = null;
		try (Fits fits = new Fits(file))
		{
			BasicHDU<?>[] hdus = fits.read();
			SettableMetadata metadata = SettableMetadata.of(VERSION);
			ImmutableList.Builder<FixedMetadata> builder = ImmutableList.builder();

			for (int hduNum = 0; hduNum < hdus.length; ++hduNum)
			{
				Metadata hduMetadata = readMetadata(hdus[hduNum], hduNum);
				builder.add(FixedMetadata.of(hduMetadata));
			}
			metadata.put(FileMetadata.DATA_OBJECTS, builder.build());
			result = FixedMetadata.of(metadata);
		}
		catch (FitsException e)
		{
			throw new FileReader.IncorrectFileFormatException(e);
		}
		return result;
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
	 * @throws FileReader.IncorrectFileFormatException if the file appears not to be
	 *             a FITS file
	 * @throws IOException if any other error occurs in reading the slice of the
	 *             table identified by the arguments
	 */
	public IndexableTuple readTuples(File file, int tableHduNumber, Iterable<Integer> columnNumbers) throws FileReader.IncorrectFileFormatException, FieldNotFoundException, IOException
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
			throw new FileReader.IncorrectFileFormatException(e);
		}
	}

	private SettableMetadata readMetadata(BasicHDU<?> hdu, int hduNumber)
	{
		SettableMetadata hduMetadata = SettableMetadata.of(VERSION);
		Header header = hdu.getHeader();

		// Derive the title.
		String extName = header.getStringValue("EXTNAME");
		boolean isPrimary = extName == null && header.getBooleanValue("SIMPLE");
		final String title = extName != null ? extName : isPrimary ? "Primary Image" : "HDU " + hduNumber;
		hduMetadata.put(FileMetadata.TITLE, title);

		// Add boilerplate FITS meta-metadata.
		hduMetadata.put(FileMetadata.DESCRIPTION_FIELDS, FITS_KEYWORD_FIELDS);

		// Get all the keywords and put them in a self-contained "sub-metadata" object.
		SettableMetadata keywordMetadata = SettableMetadata.of(VERSION);
		Cursor<String, HeaderCard> iterator = header.iterator();
		while (iterator.hasNext())
		{
			HeaderCard card = iterator.next();
			ArrayList<String> valueAndComment = new ArrayList<>();
			valueAndComment.add(card.getValue());
			valueAndComment.add(card.getComment());
			keywordMetadata.put(Key.of(card.getKey()), valueAndComment);
		}

		hduMetadata.put(FileMetadata.DESCRIPTION, FixedMetadata.of(keywordMetadata));
		return hduMetadata;
	}

	private interface GettableAsDouble
	{
		double get(int cellIndex);
	}

	private IndexableTuple readTuples(TableHDU<?> table, int tableHduNumber, Iterable<Integer> columnNumbers) throws FitsException, FieldNotFoundException, IOException
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

		final Metadata metadata = readMetadata(table, tableHduNumber);

		return new IndexableTuple() {

			@Override
			public Metadata getMetadata()
			{
				return metadata;
			}

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
}
