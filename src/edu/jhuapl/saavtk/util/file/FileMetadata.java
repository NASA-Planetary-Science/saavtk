package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.MetadataDisplayPanel;
import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.Serializers;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.util.file.FileReader.IncorrectFileFormatException;

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

	private static final FileMetadata INSTANCE = new FileMetadata();

	public static FileMetadata of()
	{
		return INSTANCE;
	}

	public FixedMetadata summary(File file) throws IOException
	{
		try
		{
			return FixedMetadata.of(FileReader.of().readMetadata(file));
		}
		catch (IncorrectFileFormatException e)
		{
			throw new IOException(e);
		}
	}

	// Test code below here.
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

	private static void display(Metadata fileMetadata)
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
