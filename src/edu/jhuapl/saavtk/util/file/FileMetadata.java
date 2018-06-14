package edu.jhuapl.saavtk.util.file;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;

public class FileMetadata
{
	private static final Version VERSION = Version.of(0, 1);

	// Data object (FITS: list of HDUs in the file).
	public static final Key<List<FixedMetadata>> DATA_OBJECTS = Key.of("Data objects"); // Each has TITLE, DESCRIPTION_FIELDS, DESCRIPTION

	public static final Key<String> TITLE = Key.of("Title");

	// Fields used to describe the data object.
	public static final Key<List<String>> DESCRIPTION_FIELDS = Key.of("Fields"); // Meta-metadata that tells us what is in the description.

	// Key-value pairs in this metadata must be displayable, i.e., its keys are Key<List<?>>.
	public static final Key<FixedMetadata> DESCRIPTION = Key.of("Data object description");

	// Optional: files that contain tables (e.g., CSV or FITS tables) will have columns.
	public static final Key<List<FixedMetadata>> COLUMNS = Key.of("Columns");

	// Each column is required to have these attributes.
	public static final Key<String> COLUMN_NAME = Key.of("Column name");
	public static final Key<String> UNITS = Key.of("Units");
	public static final Key<Integer> NUMBER_RECORDS = Key.of("Number of records");

	public static SettableMetadata createColumnMetadata(String name, String units, int numberRecords)
	{
		validateColumnMetadata(name, units, numberRecords);
		SettableMetadata result = SettableMetadata.of(VERSION);
		result.put(COLUMN_NAME, name);
		result.put(UNITS, units);
		result.put(NUMBER_RECORDS, numberRecords);
		return result;
	}

	public static SettableMetadata createColumnsMetadata(List<? extends Metadata> columnsMetadata)
	{
		validateColumnsMetadata(columnsMetadata);
		SettableMetadata result = SettableMetadata.of(VERSION);
		ImmutableList.Builder<FixedMetadata> builder = ImmutableList.builder();
		for (Metadata metadata : columnsMetadata)
		{
			builder.add(FixedMetadata.of(metadata));
		}
		result.put(COLUMNS, builder.build());
		return result;
	}

	public static SettableMetadata createDataObjectMetadata(String title, List<String> descriptionFields, Metadata description)
	{
		validateDataObjectMetadata(title, descriptionFields, description);
		SettableMetadata result = SettableMetadata.of(VERSION);
		result.put(TITLE, title);
		result.put(DESCRIPTION_FIELDS, ImmutableList.copyOf(descriptionFields));
		result.put(DESCRIPTION, FixedMetadata.of(description));
		return result;
	}

	public static SettableMetadata createTableMetadata(Metadata dataObjectMetadata, Metadata columnsMetadata)
	{
		// Check first argument.
		Preconditions.checkNotNull(dataObjectMetadata);
		String title = dataObjectMetadata.get(TITLE);
		List<String> descriptionFields = dataObjectMetadata.get(DESCRIPTION_FIELDS);
		FixedMetadata description = dataObjectMetadata.get(DESCRIPTION);
		validateDataObjectMetadata(title, descriptionFields, description);

		// Check second argument, and extract columns in the process.
		Preconditions.checkNotNull(columnsMetadata);
		List<FixedMetadata> columnsList = columnsMetadata.get(COLUMNS);
		validateColumnsMetadata(columnsList);

		// Create the composite table object.
		SettableMetadata result = SettableMetadata.of(dataObjectMetadata);
		result.put(COLUMNS, columnsList);

		return result;
	}

	public static SettableMetadata createFileMetadata(List<? extends Metadata> dataObjectsMetadata)
	{
		validateFileMetadata(dataObjectsMetadata);
		SettableMetadata result = SettableMetadata.of(VERSION);
		ImmutableList.Builder<FixedMetadata> builder = ImmutableList.builder();
		for (Metadata metadata : dataObjectsMetadata)
		{
			builder.add(FixedMetadata.of(metadata));
		}
		result.put(DATA_OBJECTS, builder.build());
		return result;
	}

	private final FixedMetadata metadata;

	public static FileMetadata of(Metadata metadata)
	{
		return new FileMetadata(metadata);
	}

	public static FileMetadata of(List<? extends Metadata> dataObjectsMetadata)
	{
		return new FileMetadata(createFileMetadata(dataObjectsMetadata));
	}

	private FileMetadata(Metadata metadata)
	{
		Preconditions.checkNotNull(metadata);
		validateFileMetadata(metadata.get(DATA_OBJECTS));
		this.metadata = FixedMetadata.of(metadata);
	}

	public FixedMetadata getMetadata()
	{
		return metadata;
	}

	private static void validateName(String name)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(name.matches("^\\S.*"));
		Preconditions.checkArgument(name.matches(".*\\S$"));
	}

	private static void validateColumnMetadata(String name, String units, int numberRecords)
	{
		validateName(name);
		//		Preconditions.checkNotNull(units);
		Preconditions.checkArgument(numberRecords >= 0);
	}

	private static void validateColumnsMetadata(Iterable<? extends Metadata> columnsMetadata)
	{
		Preconditions.checkNotNull(columnsMetadata);
		for (Metadata metadata : columnsMetadata)
		{
			validateColumnMetadata(metadata.get(COLUMN_NAME), metadata.get(UNITS), metadata.get(NUMBER_RECORDS));
		}
	}

	private static void validateDataObjectMetadata(String title, Iterable<String> descriptionFields, Metadata description)
	{
		validateName(title);
		Preconditions.checkNotNull(descriptionFields);
		int numberFields = 0;
		for (String field : descriptionFields)
		{
			++numberFields;
			validateName(field);
		}
		Preconditions.checkNotNull(description);
		for (Key<?> key : description.getKeys())
		{
			Object value = description.get(key);
			Preconditions.checkArgument(value instanceof List);
			List<?> list = (List<?>) value;
			Preconditions.checkArgument(list.size() == numberFields - 1);
			for (Object cell : list)
			{
				Preconditions.checkArgument(cell == null || cell instanceof String);
			}
		}
	}

	private static void validateFileMetadata(List<? extends Metadata> metadata)
	{
		Preconditions.checkNotNull(metadata);
		for (Metadata objectMetadata : metadata)
		{
			validateDataObjectMetadata(objectMetadata.get(TITLE), objectMetadata.get(DESCRIPTION_FIELDS), objectMetadata.get(DESCRIPTION));
			if (objectMetadata.hasKey(COLUMNS))
			{
				validateColumnsMetadata(objectMetadata.get(COLUMNS));
			}
		}
	}

}
