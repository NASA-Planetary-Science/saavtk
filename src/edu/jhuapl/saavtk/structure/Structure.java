package edu.jhuapl.saavtk.structure;

import java.awt.Color;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import vtk.vtkCaptionActor2D;

/**
 * Interface that defines a structure.
 * <P>
 * TODO: Issues to correct:
 * <UL>
 * <LI>Add comments for all methods.
 * <LI>Remove VTK specific logic
 * <LI>Reduce / eliminate ability to mutate (public scope)
 * </UL>
 */
public interface Structure
{
	public String getClickStatusBarText();

	public int getId();

	public String getName();

	public void setName(String name);

	public String getType();

	/**
	 * Provides a short description of the structure's various attributes.
	 */
	public String getInfo();

	public Color getColor();

	public void setColor(Color aColor);

	public void setLabel(String label);

	public String getLabel();

	public boolean getVisible();

	public boolean getLabelVisible();

	public Color getLabelColor();

	public void setLabelColor(Color aColor);

	public int getLabelFontSize();

	public void setLabelFontSize(int fontSize);

	public void setVisible(boolean b);

	public void setLabelVisible(boolean aBool);

	public double[] getCentroid(PolyhedralModel smallBodyModel);

	public vtkCaptionActor2D getCaption();

	public void setCaption(vtkCaptionActor2D caption);
}