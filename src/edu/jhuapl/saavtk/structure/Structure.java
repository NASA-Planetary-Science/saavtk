package edu.jhuapl.saavtk.structure;

import java.awt.Color;

import edu.jhuapl.saavtk.structure.vtk.RenderState;
import edu.jhuapl.saavtk.vtk.font.FontAttr;

/**
 * Interface that defines a structure.
 * <p>
 * A structure should provide minimal functionality relating to it's attributes.
 *
 * @author lopeznr1
 */
public interface Structure
{
	/**
	 * Returns the structures id.
	 * <p>
	 * This value may NOT be unique.
	 */
	public int getId();

	/**
	 * Returns the structure's color.
	 */
	public Color getColor();

	/**
	 * Returns the structure's label.
	 * <p>
	 * The label may optionally be displayed next to visual representations of this structure.
	 */
	public String getLabel();

	/**
	 * Returns the {@link FontAttr} associated with the label.
	 */
	public FontAttr getLabelFontAttr();

	/**
	 * Returns the "official" name of the structure. May be null.
	 */
	public String getName();

	/**
	 * Returns the {@link RenderState} associated with the structure.
	 */
	public RenderState getRenderState();

	/**
	 * Returns a unique identifier of the associated shape model.
	 */
	public String getShapeModelId();

	/**
	 * Returns the type of structure.
	 */
	public StructureType getType();

	/**
	 * Returns the source of the structure. May be null.
	 */
	public Object getSource();

	/**
	 * Returns true if the structure should be rendered.
	 */
	public boolean getVisible();

	/**
	 * Sets the structure's color.
	 */
	public void setColor(Color aColor);

	/**
	 * Sets the structure's label.
	 * <p>
	 * The label may optionally be displayed next to visual representations of this structure.
	 */
	public void setLabel(String aLabel);

	/**
	 * Sets the {@link FontAttr} associated with the label.
	 */
	public void setLabelFontAttr(FontAttr aAttr);

	/**
	 * Sets the "official" name of the structure. May be null.
	 */
	public void setName(String aName);

	/**
	 * Sets the {@link RenderState} associated with this structure.
	 */
	public void setRenderState(RenderState aRenderState);

	/**
	 * Sets in the unique identifier of the associated shape model.
	 */
	public void setShapeModelId(String aShapeModelId);

	/**
	 * Sets whether the structure should be rendered.
	 */
	public void setVisible(boolean aBool);

}