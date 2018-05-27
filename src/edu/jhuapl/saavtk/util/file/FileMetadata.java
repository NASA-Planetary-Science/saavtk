package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.MetadataDisplayPanel;
import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Serializers;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.util.Cursor;

public class FileMetadata
{
	public static final Version VERSION = Version.of(0, 1);

	// Data object (FITS: list of HDUs in the file).
	public static final Key<List<FixedMetadata>> DATA_OBJECTS = Key.of("Data objects"); // Each has TITLE, DESCRIPTION_FIELDS, DESCRIPTION

	public static final Key<String> TITLE = Key.of("Title");

	// Key-value pairs in this metadata must be displayable, i.e., its keys are Key<List<?>>.
	public static final Key<FixedMetadata> DESCRIPTION = Key.of("Data object description");

	// Fields used to describe the data object.
	public static final Key<List<String>> DESCRIPTION_FIELDS = Key.of("Fields"); // Meta-metadata that tells us what is in the description.

	// These are the fields from a FITS keyword other than the name of the keyword.
	public static final List<String> FITS_KEYWORD_FIELDS = ImmutableList.of("Value", "Comment");

	public static final Key<List<FixedMetadata>> COLUMNS = Key.of("Columns");
	public static final Key<Integer> NUMBER_RECORDS = Key.of("Number of records");
	public static final Key<String> COLUMN_NAME = Key.of("Column name");
	public static final Key<String> UNITS = Key.of("Units");

	private static final FileMetadata INSTANCE = new FileMetadata(Version.of(0, 1));

	private final Version version;

	public static FileMetadata of()
	{
		return INSTANCE;
	}

	private FileMetadata(Version version)
	{
		this.version = version;
	}

	public FixedMetadata summary(File file) throws IOException
	{
		Preconditions.checkNotNull(file);
		if (!file.exists())
		{
			throw new IOException("File does not exist: " + file);
		}

		FixedMetadata result = fitsSummary(file);

		if (result == null)
		{
			result = textSummary(file);
		}

		if (result == null)
		{
			throw new IOException("File has unknown format: " + file.toString());
		}

		return result;
	}

	private FixedMetadata fitsSummary(File file) throws IOException
	{
		FixedMetadata result = null;
		try (Fits fits = new Fits(file))
		{
			try
			{
				SettableMetadata metadata = SettableMetadata.of(version);
				ImmutableList.Builder<FixedMetadata> builder = ImmutableList.builder();

				BasicHDU<?>[] hdus = fits.read();
				for (int hduNum = 0; hduNum < hdus.length; ++hduNum)
				{
					SettableMetadata hduMetadata = SettableMetadata.of(version);
					addFitsKeywords(hduNum, hdus[hduNum], hduMetadata);
					//					if (hdu instanceof TableHDU)
					//					{
					//						TableHDU<?> tableHdu = (TableHDU<?>) hdu;
					//						builder.add(tableSummary(tableHdu));
					//					}
					//					else
					//					{
					//						// TODO handle image metadata too.
					//					}
					builder.add(FixedMetadata.of(hduMetadata));
				}
				metadata.put(DATA_OBJECTS, builder.build());
				result = FixedMetadata.of(metadata);
			}
			catch (FitsException e)
			{
				// This means there is a problem with this FITS file.
				throw new IOException(e);
			}
		}
		catch (@SuppressWarnings("unused") FitsException e)
		{
			// Ignore an exception thrown when opening the file; it probably
			// just means the file is not a FITS file.
		}
		return result;
	}

	private void addFitsKeywords(int hduNum, BasicHDU<?> hdu, SettableMetadata hduMetadata)
	{

		SettableMetadata keywordMetadata = SettableMetadata.of(VERSION);
		Header header = hdu.getHeader();

		// Derive the title.
		String extName = header.getStringValue("EXTNAME");
		boolean isPrimary = extName == null && header.getBooleanValue("SIMPLE");
		final String title = extName != null ? extName : isPrimary ? "Primary" : "HDU " + hduNum;
		hduMetadata.put(TITLE, title);

		// Add boilerplate FITS meta-metadata.
		hduMetadata.put(DESCRIPTION_FIELDS, FITS_KEYWORD_FIELDS);

		// Get all the keywords.
		Cursor<String, HeaderCard> iterator = header.iterator();
		while (iterator.hasNext())
		{
			HeaderCard card = iterator.next();
			ArrayList<String> valueAndComment = new ArrayList<>();
			valueAndComment.add(card.getValue());
			valueAndComment.add(card.getComment());
			keywordMetadata.put(Key.of(card.getKey()), valueAndComment);
		}

		hduMetadata.put(DESCRIPTION, FixedMetadata.of(keywordMetadata));
	}

	private FixedMetadata textSummary(File file) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	//	private FixedMetadata tableSummary(TableHDU<?> tableHdu) throws IOException
	//	{
	//		SettableMetadata metadata = SettableMetadata.of(version);
	//		ImmutableList.Builder<FixedMetadata> builder = ImmutableList.builder();
	//
	//		for (int index = 0; index < tableHdu.getNCols(); ++index)
	//		{
	//			builder.add(columnSummary(tableHdu, index));
	//		}
	//		metadata.put(NUMBER_RECORDS, tableHdu.getNRows());
	//		metadata.put(COLUMNS, builder.build());
	//		return FixedMetadata.of(metadata);
	//	}
	//
	//	private FixedMetadata columnSummary(TableHDU<?> tableHdu, int index)
	//	{
	//		SettableMetadata metadata = SettableMetadata.of(version);
	//		metadata.put(COLUMN_NAME, tableHdu.getColumnMeta(index, "TTYPE"));
	//		metadata.put(UNITS, tableHdu.getColumnMeta(index, "TUNIT"));
	//		return FixedMetadata.of(metadata);
	//	}

	public static void main(String[] args)
	{
		FileMetadata fileMetadata = FileMetadata.of();
		File fitsFile = new File("/Users/peachjm1/Downloads/Slope_res0.fits");
		File metadataFile = new File("/Users/peachjm1/Downloads/pc-import.sbmt");
		try
		{
			FixedMetadata metadata = fileMetadata.summary(fitsFile);
			Serializers.serialize("Slope_res0.fits", metadata, metadataFile);
			display(metadata);
			metadata = Serializers.deserialize(metadataFile, "Slope_res0.fits");
			System.out.println(metadata);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void display(FixedMetadata fileMetadata)
	{
		JTabbedPane jTabbedPane = new JTabbedPane();
		List<FixedMetadata> dataObjects = fileMetadata.get(DATA_OBJECTS);
		for (FixedMetadata metadata : dataObjects)
		{
			List<String> fields = metadata.get(DESCRIPTION_FIELDS);
			FixedMetadata description = metadata.get(DESCRIPTION);
			MetadataDisplayPanel panel = MetadataDisplayPanel.of(description, "Keyword", fields);
			jTabbedPane.add(metadata.get(TITLE), panel.getPanel());
		}

		JFrame jFrame = new JFrame("Test tabbed metadata display");
		jFrame.add(jTabbedPane);
		jFrame.pack();
		jFrame.validate();
		jFrame.setVisible(true);
	}
}
