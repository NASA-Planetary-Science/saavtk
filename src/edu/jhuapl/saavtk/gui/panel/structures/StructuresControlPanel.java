package edu.jhuapl.saavtk.gui.panel.structures;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;

public class StructuresControlPanel extends JTabbedPane implements ComponentListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// State vars
	private final ModelManager modelManager;
	private final PickManager pickManager;
	private final boolean supportsEsri;

	// Gui vars
	private AbstractStructureMappingControlPanel linePanel;
	private AbstractStructureMappingControlPanel polygonPanel;
	private AbstractStructureMappingControlPanel circlePanel;
	private AbstractStructureMappingControlPanel ellipsePanel;
	private AbstractStructureMappingControlPanel pointsPanel;

	public StructuresControlPanel(final ModelManager modelManager, final PickManager pickManager,
			final boolean supportsEsri)
	{
		this.modelManager = modelManager;
		this.pickManager = pickManager;
		this.supportsEsri = supportsEsri;

		// Register for events of interest
		addComponentListener(this);
	}

	@Override
	public void componentResized(ComponentEvent aEvent)
	{
		; // Nothing to do
	}

	@Override
	public void componentMoved(ComponentEvent aEvent)
	{
		; // Nothing to do
	}

	@Override
	public void componentShown(ComponentEvent aEvent)
	{
		// TODO: Check that this added complexity actually results in substantial
		// TODO: performance improvements.
		// TODO: This is not the place to be doing optimization.
		//
		// Delay initializing components until user explicitly makes this visible.
		// This may help in speeding up loading the view.
		if (getTabCount() > 0)
			return;

		linePanel = (new AbstractStructureMappingControlPanel(modelManager, ModelNames.LINE_STRUCTURES, pickManager,
				PickManager.PickMode.LINE_DRAW, supportsEsri));

		polygonPanel = (new AbstractStructureMappingControlPanel(modelManager, ModelNames.POLYGON_STRUCTURES, pickManager,
				PickManager.PickMode.POLYGON_DRAW, supportsEsri));

		circlePanel = (new AbstractStructureMappingControlPanel(modelManager, ModelNames.CIRCLE_STRUCTURES, pickManager,
				PickManager.PickMode.CIRCLE_DRAW, supportsEsri));

		ellipsePanel = (new AbstractStructureMappingControlPanel(modelManager, ModelNames.ELLIPSE_STRUCTURES, pickManager,
				PickManager.PickMode.ELLIPSE_DRAW, supportsEsri));

		pointsPanel = (new AbstractStructureMappingControlPanel(modelManager, ModelNames.POINT_STRUCTURES, pickManager,
				PickManager.PickMode.POINT_DRAW, supportsEsri));

		addTab("Paths", linePanel);
		addTab("Polygons", polygonPanel);
		addTab("Circles", circlePanel);
		addTab("Ellipses", ellipsePanel);
		addTab("Points", pointsPanel);

//		addTab("", new JPanel());
//
//		class OpenEsriFrameAction extends AbstractAction
//		{
//			public OpenEsriFrameAction()
//			{
//				super("ESRI");
//			}
//
//			@Override
//			public void actionPerformed(ActionEvent e)
//			{
//				try
//				{
//					SwingUtilities.invokeAndWait(new Runnable()
//					{
//
//						@Override
//						public void run()
//						{
//							Collection<SimpleFeature> features = Lists.newArrayList();
//							new ESRIStructuresFrame(features,
//									(GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));
//						}
//					});
//				} 
//				catch (InvocationTargetException | InterruptedException e1)
//				{
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
//
//		}
//     
//     setTabComponentAt(getTabCount()-1, new JButton(new OpenEsriFrameAction()));
	}

	@Override
	public void componentHidden(ComponentEvent aEvent)
	{
		// Bail if not initialized
		if (getTabCount() == 0)
			return;

		// Disable editing on all StructureMappingControlPanels
		linePanel.setEditingEnabled(false);
		polygonPanel.setEditingEnabled(false);
		circlePanel.setEditingEnabled(false);
		ellipsePanel.setEditingEnabled(false);
		pointsPanel.setEditingEnabled(false);
	}
}
