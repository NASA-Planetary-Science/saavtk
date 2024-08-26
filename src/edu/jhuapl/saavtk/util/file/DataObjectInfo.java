package edu.jhuapl.saavtk.util.file;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.FixedMetadata;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

/**
 * Class that provides information about one data object (image or table) within
 * a data file.
 */
public class DataObjectInfo
{
	/**
	 * Textual metadata that describes the data object, for example key-value pairs,
	 * represented as a row-oriented table.
	 */
	public static class Description
	{
		public static Description of(List<String> fields, List<? extends InfoRow> elements)
		{
			return new Description(fields, elements);
		}

		private final ImmutableList<String> fields;
		private final ImmutableList<InfoRow> elements;

		protected Description(List<String> fields, List<? extends InfoRow> elements)
		{
			Preconditions.checkNotNull(fields);
			for (String field : fields)
			{
				checkStringBeginsAndEndsWithText(field);
			}
			Preconditions.checkNotNull(elements);
			for (InfoRow info : elements)
			{
				Preconditions.checkArgument(fields.size() == info.getNumberElements());
			}

			this.fields = ImmutableList.copyOf(fields);
			this.elements = ImmutableList.copyOf(elements);
		}

		/**
		 * Get a list of the column titles in order left-to-right of this description
		 * data.
		 * 
		 * @return the list of column titles
		 */
		public ImmutableList<String> getColumnTitles()
		{
			return fields;
		}

		/**
		 * Get the data object metadata as a list of rows.
		 * 
		 * @return
		 */
		public ImmutableList<InfoRow> get()
		{
			return elements;
		}

		/**
		 * Get the data object information in (storable) metadata form.
		 * 
		 * @return the data object info, as metadata
		 */
		public FixedMetadata getAsMetadata()
		{
			SettableMetadata result = SettableMetadata.of(Version.of(0, 1));
			for (InfoRow element : elements)
			{
				Iterator<String> iterator = element.get().iterator();
				if (iterator.hasNext())
				{
					Key<List<String>> key = Key.of(iterator.next());
					List<String> values = new ArrayList<>();
					while (iterator.hasNext())
					{
						values.add(iterator.next());
					}
					result.put(key, values);
				}
			}
			return FixedMetadata.of(result);
		}

		@Override
		public String toString()
		{
			return "Fields: " + getColumnTitles();
		}
	}

	/**
	 * One row of descriptive information in the data object metadata, e.g., a
	 * single key-value pair. The definition of the row titles is contained in the
	 * info table that contains this row.
	 * 
	 * Note that this represents only descriptive information, not rows of data from
	 * a table or pixels from an image.
	 */
	public static class InfoRow
	{
		public static InfoRow of(List<String> elements)
		{
			return new InfoRow(elements);
		}

		private final List<String> elements;

		protected InfoRow(List<String> elements)
		{
			Preconditions.checkNotNull(elements);
			this.elements = new ArrayList<>(elements);
		}

		/**
		 * Return the size of the row (number of columns).
		 * 
		 * @return the row size
		 */
		public int getNumberElements()
		{
			return elements.size();
		}

		/**
		 * Return the row content as list of strings in column-order.
		 * 
		 * @return the row content
		 */
		public List<String> get()
		{
			return new ArrayList<>(elements);
		}

		@Override
		public String toString()
		{
			return String.join(" ", elements);
		}
	}

	public static DataObjectInfo of(String title, Description description)
	{
		return new DataObjectInfo(title, description);
	}

	private final String title;
	private final Description description;

	protected DataObjectInfo(String title, Description description)
	{
		checkStringBeginsAndEndsWithText(title);
		Preconditions.checkNotNull(description);
		this.title = title;
		this.description = description;
	}

	public String getTitle()
	{
		return title;
	}

	public Description getDescription()
	{
		return description;
	}

	@Override
	public String toString()
	{
		return getTitle() + ": " + getDescription();
	}

	protected static void checkStringBeginsAndEndsWithText(String string)
	{
		Preconditions.checkNotNull(string);
		Preconditions.checkArgument(string.matches("^\\S.*"));
		Preconditions.checkArgument(string.matches(".*\\S$"));
	}
}
