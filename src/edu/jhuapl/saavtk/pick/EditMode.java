package edu.jhuapl.saavtk.pick;

/**
 * Enum used to describe what edits are allowed.
 * <P>
 * This enum allows for Pickers to define their "edit" state.
 *
 * @author lopeznr1
 */
public enum EditMode
{
	/**
	 * Mode used when no action can be taken.
	 */
	NONE,

	/**
	 * Mode used when an item (vertex, ...) is ready to be clicked on or added.
	 */
	CLICKABLE,

	/**
	 * Mode used when an item (vertex, polygon, ...) is ready to be dragged (or in
	 * the state of being dragged).
	 */
	DRAGGABLE,

}
