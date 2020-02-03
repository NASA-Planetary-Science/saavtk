package edu.jhuapl.saavtk.structure;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.pick.DefaultPicker;
import glum.item.ItemEventListener;
import glum.item.ItemManager;
import glum.task.Task;

/**
 * Interface that provides defines methods to support management logic for a
 * collection of {@link Structure}s.
 * <P>
 * The following features are supported:
 * <UL>
 * <LI>Event handling via {@link ItemEventListener} mechanism
 * <LI>Management of items
 * <LI>Support for item selection
 * <LI>Configuration of various rendering properties
 * <LI>Support for editing structures via an activation mechanism.
 * </UL>
 * <P>
 * TODO: Improvements to be added:
 * <UL>
 * <LI>Add support for individual line widths
 * <LI>Decoupling VTK painting from structure management
 * </UL>
 *
 * @author lopeznr1
 */
public interface StructureManager<G1 extends Structure> extends ItemManager<G1>
{
	/**
	 * Returns the structure at the specified index.
	 * <P>
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
	 * Returns the color of the specified structure.
	 */
	public Color getColor(G1 aItem);

	/**
	 * Returns the diameter of the structure.
	 */
	public double getDiameter(G1 aItem);

	/**
	 * Returns true if the the specified structure is visible.
	 */
	public boolean getIsVisible(G1 aItem);

	/**
	 * Returns the line width associated with the manager.
	 * <P>
	 * TODO: This method will eventually be specific to a specific structure rather
	 * than all the structures associated with this manager.
	 */
	public double getLineWidth();

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
	 * Removes the specified items from this manager.
	 */
	public void removeItems(Collection<G1> aItemC);

	/**
	 * Sets the color of the specified structures.
	 *
	 * @param aItemC The collection of structures to change.
	 * @param aColor The color that the structures will be changed to.
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
	 * @param aItemC The collection of structures to change.
	 * @param aColor The color that the labels will be changed to.
	 */
	public void setLabelColor(Collection<G1> aItemC, Color aColor);

	/**
	 * Sets the font face of the label associated with the specified structures.
	 *
	 * @param aItemC      The collection of structures to change.
	 * @param aFontFamily The font family to switch to. Currently the only supported
	 *                    families are: [Times, Arial, Courier]. A
	 *                    {@link RuntimeException} will be thrown if not supported.
	 */
	public void setLabelFontFamily(Collection<G1> aItemC, String aFontFamily);

	/**
	 * Sets the font size of the labels associated with the specified structures.
	 *
	 * @param aItemC    The collection of structures to change.
	 * @param aFontSize
	 */
	public void setLabelFontSize(Collection<G1> aItemC, int aFontSize);

	/**
	 * Sets the visibility of the labels associated with the specified structures.
	 *
	 * @param aItemC The collection of structures to change.
	 * @param aBool  Flag which defines whether to show the labels or not.
	 */
	public void setLabelVisible(Collection<G1> aItemC, boolean aBool);

	/**
	 * Sets the line width associated with the manager.
	 * <P>
	 * TODO: This method will eventually be specific to a specific structure rather
	 * than all the structures associated with this manager.
	 */
	public void setLineWidth(double width);

	/**
	 * Sets the visibility of the specified structures.
	 *
	 * @param aItemC The collection of structures to change.
	 * @param aBool  Flag which defines whether to show the labels or not.
	 */
	public void setIsVisible(Collection<G1> aItemC, boolean aBool);

	// TODO: Bad design
	public boolean supportsActivation();

	/**
	 * TODO: We should be registered with the "DefaultPicker" through different
	 * means and not rely on unrelated third party registration...
	 */
	public void registerDefaultPickerHandler(DefaultPicker aDefaultPicker);

}
