package edu.jhuapl.saavtk.state.types;

/**
 * 2018-04-24. This package was part of the initial "save state" effort (redmine
 * 1206). This package was a partly-successful attempt to keep track of the full
 * type of generics despite type erasure, similar to Guava/Gson's TypeToken
 * classes. It worked fine and was even better in some ways because it allowed
 * one to reconstruct a complicated object piece-by-piece, but was too clunky to
 * be practical.
 * 
 * Leaving it in the project for the time being in case we need it after all or
 * want to refer to it in the near future, but marking it deprecated, not so
 * much because of its flaws but to prevent its accidental use.
 * 
 * After a suitable time, if no use presents itself, it would be safe to remove
 * this whole package.
 */
@Deprecated
public interface ElementType extends Comparable<ElementType>
{
	Class<?> getPrimaryType();

	HasSubTypes getSubTypes();
}
