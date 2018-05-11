package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Serializers;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.TableHDU;

public class FileMetadata
{
	public static final Key<List<FixedMetadata>> TABLES = Key.of("Tables");
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

	public enum Format
	{
		FITS,
		TEXT,;
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
				for (BasicHDU<?> hdu : fits.read())
				{
					if (hdu instanceof TableHDU)
					{
						TableHDU<?> tableHdu = (TableHDU<?>) hdu;
						builder.add(tableSummary(tableHdu));
					}
					else
					{
						// TODO handle image metadata too.
					}
				}
				metadata.put(TABLES, builder.build());
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

	private FixedMetadata textSummary(File file) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	private FixedMetadata tableSummary(TableHDU<?> tableHdu) throws IOException
	{
		SettableMetadata metadata = SettableMetadata.of(version);
		ImmutableList.Builder<FixedMetadata> builder = ImmutableList.builder();

		for (int index = 0; index < tableHdu.getNCols(); ++index)
		{
			builder.add(columnSummary(tableHdu, index));
		}
		metadata.put(NUMBER_RECORDS, tableHdu.getNRows());
		metadata.put(COLUMNS, builder.build());
		return FixedMetadata.of(metadata);
	}

	private FixedMetadata columnSummary(TableHDU<?> tableHdu, int index)
	{
		SettableMetadata metadata = SettableMetadata.of(version);
		metadata.put(COLUMN_NAME, tableHdu.getColumnMeta(index, "TTYPE"));
		metadata.put(UNITS, tableHdu.getColumnMeta(index, "TUNIT"));
		return FixedMetadata.of(metadata);
	}

	public static void main(String[] args)
	{
		FileMetadata fileMetadata = FileMetadata.of();
		File fitsFile = new File("/Users/peachjm1/Downloads/Slope_res0.fits");
		File metadataFile = new File("/Users/peachjm1/Downloads/pc-import.sbmt");
		try
		{
			FixedMetadata metadata = fileMetadata.summary(fitsFile);
			Serializers.serialize("Slope_res0.fits", metadata, metadataFile);
			metadata = Serializers.deserialize(metadataFile, "Slope_res0.fits");
			System.out.println(metadata);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
