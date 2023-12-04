package edu.jhuapl.saavtk.structure;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import glum.item.ItemEventListener;
import glum.item.ItemManager;
import glum.task.Task;

/**
 * Interface that defines a collection of methods to support management logic for a collection of {@link Structure}s.
 * <p>
 * The following features are supported:
 * <ul>
 * <li>Event handling via {@link ItemEventListener} mechanism
 * <li>Management of items
 * <li>Support for item selection
 * <li>Configuration of various rendering properties
 * </ul>
 *
 * @author lopeznr1
 */
public interface StructureManager<G1 extends Structure> extends ItemManager<G1>
{
	/**
	 * Returns the structure at the specified index.
	 * <p>
	 * Returns null if there are no items in the manager.
	 */
	public G1 getItem(int aIdx);

	/**
	 * Returns the center of the structure.
	 */
	public Vector3D getCenter(G1 aItem);

	/**
	 * Returns the centroid associated with the specified structure.
	 */
	public Vector3D getCentroid(G1 aItem);

	/**
	 * Returns the {@link RenderAttr}.
	 */
	public RenderAttr getRenderAttr();

	/**
	 * Returns the diameter of the structure.
	 */
	public double getDiameter(G1 aItem);

	/**
	 * Returns the normal corresponding to the structure's center.
	 *
	 * @see #getCenter(Structure)
	 */
	public Vector3D getNormal(G1 aItem);

	/**
	 * Installs the specified items into this manager.
	 */
	public void installItems(Task aTask, List<G1> aItemL);

	/**
	 * Notifies this manager that the specified items has been mutated.
	 */
	public void notifyItemsMutated(Collection<G1> aItemC);

	/**
	 * Sets the color of the specified structures.
	 *
	 * @param aItemC
	 *    The collection of structures to change.
	 * @param aColor
	 *    The color that the structures will be changed to.
	 */
	public void setColor(Collection<G1> aItemC, Color aColor);

	/**
	 * Sets the color of the specified structure.
	 */
	public void setColor(G1 aItem, Color aColor);

	/**
	 * Sets the label associated with the specified structure.
	 */
	public void setLabel(G1 aItem, String aLabel);

	/**
	 * Sets the color of the labels associated with the specified structures.
	 *
	 * @param aItemC
	 *    The collection of structures to change.
	 * @param aColor
	 *    The color that the labels will be changed to.
	 */
	public void setLabelColor(Collection<G1> aItemC, Color aColor);

	/**
	 * Sets the font face of the label associated with the specified structures.
	 *
	 * @param aItemC
	 *    The collection of structures to change.
	 * @param aFontFamily
	 *    The font family to switch to. Currently the only supported families are: [Times, Arial, Courier]. A
	 *    {@link RuntimeException} will be thrown if not supported.
	 */
	public void setLabelFontFamily(Collection<G1> aItemC, String aFontFamily);

	/**
	 * Sets the font size of the labels associated with the specified structures.
	 *
	 * @param aItemC
	 *    The collection of structures to change.
	 * @param aFontSize
	 */
	public void setLabelFontSize(Collection<G1> aItemC, int aFontSize);

	/**
	 * Sets the visibility of the labels associated with the specified structures.
	 *
	 * @param aItemC
	 *    The collection of structures to change.
	 * @param aBool
	 *    Flag which defines whether to show the labels or not.
	 */
	public void setLabelVisible(Collection<G1> aItemC, boolean aBool);

	/**
	 * Sets the visibility of the specified structures.
	 *
	 * @param aItemC
	 *    The collection of structures to change.
	 * @param aBool
	 *    Flag which defines whether to show the labels or not.
	 */
	public void setIsVisible(Collection<G1> aItemC, boolean aBool);

}
