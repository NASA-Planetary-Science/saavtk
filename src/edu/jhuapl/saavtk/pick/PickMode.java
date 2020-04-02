package edu.jhuapl.saavtk.pick;

/**
 * Enum that defines the state of the picker during a "picker event".
 * 
 * @author lopeznr1
 */
public enum PickMode
{
	/** No-operation mode. */
	None,

	/** Mode associated with a (primary) action. */
	ActivePri,

	/** Mode associated with a (secondary) action. */
	ActiveSec,

	/** Mode associated with a passive activity. */
	Passive;

}
