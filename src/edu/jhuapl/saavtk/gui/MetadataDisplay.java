package edu.jhuapl.saavtk.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JTabbedPane;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.file.DataFileInfo;
import edu.jhuapl.saavtk.util.file.DataFileReader;
import edu.jhuapl.saavtk.util.file.DataFileReader.FileFormatException;
import edu.jhuapl.saavtk.util.file.DataObjectInfo;
import edu.jhuapl.saavtk.util.file.DataObjectInfo.Description;

public class MetadataDisplay
{
	public static JTabbedPane summary(File file) throws IOException
	{
		JTabbedPane jTabbedPane = new JTabbedPane();
		try
		{
			DataFileInfo fileInfo = DataFileReader.of().readFileInfo(file);
			for (DataObjectInfo dataObjectInfo : fileInfo.getDataObjectInfo())
			{
				Description description = dataObjectInfo.getDescription();
				ImmutableList<String> fields = description.getColumnTitles();

				// To pass to MetadataDisplayPanel, need to separate the first field,
				// which is the name of the "key" of the metadata.
				String keyField = "";
				if (fields.size() > 0)
				{
					keyField = fields.get(0);
					fields = fields.subList(1, fields.size());
				}

				MetadataDisplayPanel panel = MetadataDisplayPanel.of(description.getAsMetadata(), keyField, fields);
				jTabbedPane.add(dataObjectInfo.getTitle(), panel.getPanel());
			}
			return jTabbedPane;
		}
		catch (FileFormatException e)
		{
			throw new IOException(e);
		}

	}

	//	public static JTabbedPane summary(File file) throws IOException
	//	{
	//		JTabbedPane jTabbedPane = new JTabbedPane();
	//		FixedMetadata fileMetadata;
	//		try
	//		{
	//			fileMetadata = DataFileReader.of().readMetadata(file).getMetadata();
	//			List<FixedMetadata> dataObjects = fileMetadata.get(FileMetadata.DATA_OBJECTS);
	//			for (FixedMetadata metadata : dataObjects)
	//			{
	//				List<String> fields = metadata.get(FileMetadata.DESCRIPTION_FIELDS);
	//				FixedMetadata description = metadata.get(FileMetadata.DESCRIPTION);
	//				MetadataDisplayPanel panel = MetadataDisplayPanel.of(description, "Keyword", fields);
	//				jTabbedPane.add(metadata.get(FileMetadata.TITLE), panel.getPanel());
	//			}
	//			return jTabbedPane;
	//		}
	//		catch (IncorrectFileFormatException e)
	//		{
	//			throw new IOException(e);
	//		}
	//
	//	}

	private MetadataDisplay()
	{
		throw new AssertionError();
	}
}
