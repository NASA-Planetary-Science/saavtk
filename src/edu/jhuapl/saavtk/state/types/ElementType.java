package edu.jhuapl.saavtk.state.types;

public interface ElementType extends Comparable<ElementType>
{
	Class<?> getPrimaryType();

	HasSubTypes getSubTypes();
}
