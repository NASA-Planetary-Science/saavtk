package edu.jhuapl.saavtk.camera.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.render.Renderer;
import glum.gui.action.CloseDialog;

/**
 * {@link AbstractAction} that contains the logic for the "Config Camera:
 * Quaternion" action.
 *
 * @author lopeznr1
 */
public class CameraQuaternionAction extends AbstractAction implements HierarchyListener
{
	// Constants
	private static final String Title = "Camera: Quaternion";

	// State vars
	private final Map<Renderer, JDialog> viewM;

	// Ref vars
	private final ViewManager refViewManager;

	/**
	 * Standard Constructor
	 */
	public CameraQuaternionAction(ViewManager aViewManager)
	{
		super(Title);

		refViewManager = aViewManager;

		viewM = new HashMap<>();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Renderer tmpRenderer = refViewManager.getCurrentView().getRenderer();

		JDialog tmpDialog = viewM.get(tmpRenderer);
		if (tmpDialog == null)
		{
			CameraQuaternionPanel tmpPanel = new CameraQuaternionPanel(tmpRenderer);

			Frame tmpFrame = JOptionPane.getFrameForComponent(refViewManager.getCurrentView());
			tmpDialog = new CloseDialog(tmpFrame, tmpPanel);
			tmpDialog.setTitle(Title);
			tmpDialog.setLocationRelativeTo(tmpRenderer);

			tmpRenderer.addHierarchyListener(this);

			viewM.put(tmpRenderer, tmpDialog);
		}
		tmpDialog.setVisible(true);
	}

	@Override
	public void hierarchyChanged(HierarchyEvent aEvent)
	{
		Component tmpComp = aEvent.getComponent();
		JDialog tmpDialog = viewM.get(tmpComp);
		if (tmpDialog != null && tmpDialog.isShowing() == true && tmpComp.isShowing() == false)
			tmpDialog.setVisible(false);
	}

}
