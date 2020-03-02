package edu.jhuapl.saavtk.structure.gui.load;

/**
 * Enum that defines the way structures should be installed into the system.
 *
 * @author lopeznr1
 */
public enum InstallMode
{
	/** All items will be replaced with the newly loaded items. */
	ReplaceAll,

	/** Only items with colliding ids will be replaced with newly loaded items. */
	ReplaceCollidingId,

	/** Append the newly loaded items and keep the original id. */
	AppendWithOriginalId,

	/** Append the newly loaded items and ensure they are assigned a unique id. */
	AppendWithUniqueId
}
