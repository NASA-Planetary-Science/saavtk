package edu.jhuapl.saavtk.view.lod.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.LodUtil;

/**
 * ListCellRenderer used to render custom labels corresponding to
 * {@link LodMode}.
 *
 * @author lopeznr1
 */
public class LodModeRenderer extends DefaultListCellRenderer
{
	/** Standard Constructor */
	public LodModeRenderer()
	{
		; // Nothing to do
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object aObj, int index, boolean isSelected,
			boolean hasFocus)
	{
		JLabel retL = (JLabel) super.getListCellRendererComponent(list, aObj, index, isSelected, hasFocus);

		String tmpStr = "---";
		if (aObj instanceof LodMode)
		{
			LodMode mode = (LodMode) aObj;
			tmpStr = LodUtil.getDisplayString(mode);
		}

		retL.setText(tmpStr);
		return retL;
	}

}
