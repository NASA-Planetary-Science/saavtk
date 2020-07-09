package edu.jhuapl.saavtk.view;

/**
 * Enum which defines the various reasons why a {@link View} was changed.
 *
 * @author lopeznr1
 */
public enum ViewChangeReason
{
	/**
	 * The camera's configuration changed. This will typically result in a change in
	 * the rendered scene.
	 */
	Camera,

	/**
	 * The LOD (level-of-detail) was changed.
	 */
	Lod,

	/**
	 * Other unspecified reason.
	 */
	Other,
}
