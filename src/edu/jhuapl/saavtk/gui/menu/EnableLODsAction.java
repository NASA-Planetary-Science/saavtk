package edu.jhuapl.saavtk.gui.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;

import edu.jhuapl.saavtk.gui.render.Renderer;

/**
 * {@link AbstractAction} that contains the logic for the 'Enable LODs' menu.
 */
public class EnableLODsAction extends AbstractAction
{
	/**
	 * Standard Constructor
	 */
	public EnableLODsAction()
	{
		super("Enable LODs");
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Renderer.enableLODs = ((AbstractButton) aEvent.getSource()).getModel().isSelected();
	}

}
