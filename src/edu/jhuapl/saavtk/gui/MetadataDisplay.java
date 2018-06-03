package edu.jhuapl.saavtk.gui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.util.file.FileMetadata;

public class MetadataDisplay
{
	public static JTabbedPane summary(File file) throws IOException
	{
		JTabbedPane jTabbedPane = new JTabbedPane();
		FixedMetadata fileMetadata = FileMetadata.of().summary(file);

		List<FixedMetadata> dataObjects = fileMetadata.get(FileMetadata.DATA_OBJECTS);
		for (FixedMetadata metadata : dataObjects)
		{
			List<String> fields = metadata.get(FileMetadata.DESCRIPTION_FIELDS);
			FixedMetadata description = metadata.get(FileMetadata.DESCRIPTION);
			MetadataDisplayPanel panel = MetadataDisplayPanel.of(description, "Keyword", fields);
			jTabbedPane.add(metadata.get(FileMetadata.TITLE), panel.getPanel());
		}
		return jTabbedPane;
	}

	private MetadataDisplay()
	{
		throw new AssertionError();
	}
}
