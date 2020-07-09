package edu.jhuapl.saavtk.view.lod;

/**
 * Defines the available modes for which to set the LOD (level-of-detail).
 * <P>
 * LOD modes define how to prioritize speed vs quality.
 *
 * @author lopeznr1
 */
public enum LodMode
{
	/**
	 * The LOD mode will be automatically selected by the software.
	 */
	Auto,

	/**
	 * The LOD mode will be selected to maximize rendering speed.
	 */
	MaxSpeed,

	/**
	 * The LOD mode will be selected to maximize rendering quality.
	 */
	MaxQuality,

}
