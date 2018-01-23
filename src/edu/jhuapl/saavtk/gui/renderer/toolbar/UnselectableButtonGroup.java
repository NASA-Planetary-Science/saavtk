package edu.jhuapl.saavtk.gui.renderer.toolbar;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

@SuppressWarnings("serial")
public class UnselectableButtonGroup extends ButtonGroup
{
	@Override
	public void setSelected(ButtonModel m, boolean b)
	{
		if (b)
			super.setSelected(m, b);
		else
			clearSelection();
	}
}
