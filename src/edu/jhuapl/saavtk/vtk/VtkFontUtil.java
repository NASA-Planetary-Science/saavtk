package edu.jhuapl.saavtk.vtk;

import java.awt.Color;

import edu.jhuapl.saavtk.vtk.font.FontAttr;
import vtk.vtkTextProperty;

/**
 * Collection of VTK routines associated with font functionality.
 *
 * @author lopeznr1
 */
public class VtkFontUtil
{
	/**
	 * Retrieves the {@link FontAttr} corresponding to the specified
	 * {@link vtkTextProperty}.
	 */
	public static FontAttr getFontAttr(vtkTextProperty aTmpTP)
	{
		String fontFace = aTmpTP.GetFontFamilyAsString();

		double[] colorArr = aTmpTP.GetColor();
		Color tmpColor = new Color((float) colorArr[0], (float) colorArr[1], (float) colorArr[2]);

		int tmpSize = aTmpTP.GetFontSize();

		return new FontAttr(fontFace, tmpColor, tmpSize, true);
	}

	/**
	 * Configures the {@link vtkTextProperty} to match the {@link FontAttr}.
	 */
	public static void setFontAttr(vtkTextProperty aTmpTP, FontAttr aFontAttr)
	{
		aTmpTP.SetFontFamilyAsString(aFontAttr.getFace());

		Color tmpColor = aFontAttr.getColor();
		aTmpTP.SetColor(tmpColor.getRed() / 255.0, tmpColor.getGreen() / 255.0, tmpColor.getBlue() / 255.0);

		aTmpTP.SetFontSize(aFontAttr.getSize());

		int iBool = aFontAttr.getIsBold() == false ? 0 : 1;
		aTmpTP.SetBold(iBool);

		iBool = aFontAttr.getIsItalic() == false ? 0 : 1;
		aTmpTP.SetItalic(iBool);

		aTmpTP.Modified();
	}

}
