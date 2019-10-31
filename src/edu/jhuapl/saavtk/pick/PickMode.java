package edu.jhuapl.saavtk.pick;

/**
 * Enum that defines the state of the picker during a "picker event".
 * 
 * @author lopeznr1
 */
public enum PickMode
{
	/** No-operation mode. */
	NONE,

	/** Mode associated with a specific action. */
	Active,

	/** Mode associated with a passive activity. */
	Passive;

}
