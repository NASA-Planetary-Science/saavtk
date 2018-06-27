package edu.jhuapl.saavtk.util.file;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;

public class DataObjectInfo
{
	public static class InfoElements
	{
		public static InfoElements of(List<String> elements)
		{
			return new InfoElements(elements);
		}

		private final List<String> elements;

		protected InfoElements(List<String> elements)
		{
			Preconditions.checkNotNull(elements);
			this.elements = new ArrayList<>(elements);
		}

		public int getNumberElements()
		{
			return elements.size();
		}

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

	public static class Description
	{
		public static Description of(List<String> fields, List<? extends InfoElements> elements)
		{
			return new Description(fields, elements);
		}

		private final ImmutableList<String> fields;
		private final ImmutableList<InfoElements> elements;

		protected Description(List<String> fields, List<? extends InfoElements> elements)
		{
			Preconditions.checkNotNull(fields);
			for (String field : fields)
			{
				checkStringBeginsAndEndsWithText(field);
			}
			Preconditions.checkNotNull(elements);
			for (InfoElements info : elements)
			{
				Preconditions.checkArgument(fields.size() == info.getNumberElements());
			}

			this.fields = ImmutableList.copyOf(fields);
			this.elements = ImmutableList.copyOf(elements);
		}

		public ImmutableList<String> getFields()
		{
			return fields;
		}

		public ImmutableList<InfoElements> get()
		{
			return elements;
		}

		public FixedMetadata getAsMetadata()
		{
			SettableMetadata result = SettableMetadata.of(Version.of(0, 1));
			for (InfoElements element : elements)
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
			return "Fields: " + getFields();
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
