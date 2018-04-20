package edu.jhuapl.saavtk.state.types;

import com.google.common.collect.ImmutableList;

public interface HasSubTypes extends Comparable<HasSubTypes>
{
	ImmutableList<ElementType> getSubTypes();
}
