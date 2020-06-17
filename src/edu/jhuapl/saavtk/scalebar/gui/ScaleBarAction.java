package edu.jhuapl.saavtk.scalebar.gui;

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

import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.scalebar.ScaleBarPainter;
import glum.gui.GuiUtil;
import glum.gui.action.CloseDialog;
import glum.misc.InitListener;

/**
 * {@link AbstractAction} that contains the logic for the 'Config Scale Bar'
 * menu.
 *
 * @author lopeznr1
 */
public class ScaleBarAction extends AbstractAction implements HierarchyListener, InitListener
{
	// Constants
	private static final String Title = "Scale Bar";

	// Ref vars
	private final ViewManager refViewManager;

	// State vars
	private final Map<View, JDialog> initM;

	/**
	 * Standard Constructor
	 */
	public ScaleBarAction(ViewManager aViewManager)
	{
		super(Title);

		refViewManager = aViewManager;

		initM = new HashMap<>();

		// Register for events of interest
		refViewManager.addInitListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		View tmpView = refViewManager.getCurrentView();

		// Locate and show the corresponding dialog
		JDialog tmpDialog = initM.get(tmpView);
		if (tmpDialog == null)
			return;

		tmpDialog.setVisible(true);
	}

	@Override
	public void handleInitAction(Object aSource)
	{
		// Ignore if the source is not a View
		if (aSource instanceof View == false)
			return;

		// Execute on the AWT
		if (GuiUtil.redispatchOnAwtIfNeeded(() -> handleInitAction(aSource)) == true)
			return;

		View tmpView = (View) aSource;
		Renderer tmpRenderer = tmpView.getRenderer();
		SceneChangeNotifier tmpSceneChangeNotifier = tmpView.getModelManager();

		// Bail if we have already seen this source
		JDialog tmpDialog = initM.get(tmpView);
		if (tmpDialog != null)
			return;

		// Create our UI
		ScaleBarPainter tmpPainter = new ScaleBarPainter(tmpRenderer, tmpSceneChangeNotifier);
		tmpRenderer.addVtkPropProvider(tmpPainter);
		ScaleBarPanel tmpPanel = new ScaleBarPanel(tmpRenderer, tmpPainter);

		Frame tmpFrame = JOptionPane.getFrameForComponent(tmpView);
		tmpDialog = new CloseDialog(tmpFrame, tmpPanel);
		tmpDialog.setTitle(Title);
		tmpDialog.setLocationRelativeTo(tmpRenderer);
		initM.put(tmpView, tmpDialog);

		// Register for events of interest
		tmpRenderer.addHierarchyListener(this);
	}

	@Override
	public void hierarchyChanged(HierarchyEvent aEvent)
	{
		Component tmpComp = aEvent.getComponent();
		JDialog tmpDialog = initM.get(tmpComp);
		if (tmpDialog != null && tmpDialog.isShowing() == true && tmpComp.isShowing() == false)
			tmpDialog.setVisible(false);
	}

}