package edu.jhuapl.saavtk.state.types;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class ElementTypes
{
	public static ElementType of(Class<?> primaryType)
	{
		return new SimpleElementType(primaryType);
	}

	public static ElementType ofCompositeTypes(Class<?> primaryType, ElementType secondaryType, ElementType... otherSecondaryTypes)
	{
		Preconditions.checkNotNull(primaryType);
		Preconditions.checkNotNull(secondaryType);

		HasSubTypes subTypes = ofSubTypes(secondaryType, otherSecondaryTypes);

		return new CompositeType(primaryType, subTypes);
	}

	public static ElementType ofCompositeTypes(Class<?> primaryType, HasSubTypes subTypes)
	{
		return new CompositeType(primaryType, subTypes);
	}

	public static HasSubTypes ofSubTypes(ElementType primaryType, ElementType... secondaryTypes)
	{
		Preconditions.checkNotNull(primaryType);

		ImmutableList.Builder<ElementType> builder = ImmutableList.builder();
		builder.add(primaryType);
		for (ElementType type : secondaryTypes)
		{
			builder.add(type);
		}

		return new BasicSubTypes(builder.build());

	}

	private static final class SimpleElementType extends BasicElementType
	{
		private static final HasSubTypes EMPTY_SUB_TYPES = new BasicSubTypes(ImmutableList.of());

		SimpleElementType(Class<?> primaryType)
		{
			super(primaryType, EMPTY_SUB_TYPES);
		}

		@Override
		public String toString()
		{
			return getPrimaryType().getSimpleName();
		}

	}

	private static final class CompositeType extends BasicElementType
	{
		protected CompositeType(Class<?> primaryType, HasSubTypes secondaryTypes)
		{
			super(primaryType, secondaryTypes);
		}

		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder(getPrimaryType().getSimpleName());
			//			Iterator<ElementType> iterator = getSubTypes().getSubTypes().iterator();
			//			if (iterator.hasNext())
			//			{
			builder.append("<");
			//				builder.append(iterator.next());
			//				while (iterator.hasNext())
			//				{
			//					builder.append("<");
			//					builder.append(iterator.next());
			//					builder.append(">");
			//				}
			//				while (iterator.hasNext())
			//				{
			//					builder.append(", ");
			//					builder.append(iterator.next());
			//				}
			builder.append(getSubTypes());
			builder.append(">");
			//			}
			return builder.toString();
		}

	}

	private ElementTypes()
	{
		throw new AssertionError();
	}

	public static void main(String[] args)
	{
		ElementType stringType = of(String.class);
		ElementType stringType2 = of(String.class);
		ElementType listType = of(List.class);
		System.out.println(stringType);

		if (!stringType.equals(stringType2))
		{
			System.err.println("String types are not equal");
		}
		if (stringType.equals(listType))
		{
			System.err.println("String type equals List type");
		}

		ElementType listType2 = ofCompositeTypes(List.class, listType, stringType);
		System.out.println(listType2);
		if (listType.equals(listType2))
		{
			System.err.println("List types are equal");
		}

		ElementType mapType = ofCompositeTypes(Map.class, ofSubTypes(stringType, listType2));
		System.out.println(mapType);

		ElementType mapType2 = ofCompositeTypes(Map.class, ofSubTypes(stringType2, listType2));
		if (!mapType.equals(mapType2))
		{
			System.err.println("Map types are not equal");
		}

		SortedSet<ElementType> elements = new TreeSet<>();
		elements.add(stringType);
		elements.add(listType);
		elements.add(stringType2);
		elements.add(listType2);
		elements.add(mapType2);
		elements.add(mapType2);
		System.out.println(elements);
	}
}
