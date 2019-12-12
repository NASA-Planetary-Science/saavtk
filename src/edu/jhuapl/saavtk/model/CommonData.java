package edu.jhuapl.saavtk.model;

import java.awt.Color;

import edu.jhuapl.saavtk.structure.io.StructureLegacyUtil;
import edu.jhuapl.saavtk.util.Preferences;

public class CommonData
{
	private Color selectionColor;

	public CommonData()
	{
		int[] rgbArr = Preferences.getInstance().getAsIntArray(Preferences.SELECTION_COLOR, new int[] { 0, 0, 255 });

		selectionColor = StructureLegacyUtil.convertRgbaToColor(rgbArr);
	}

	public Color getSelectionColor()
	{
		return selectionColor;
	}

	public void setSelectionColor(Color aColor)
	{
		selectionColor = aColor;
	}
}
