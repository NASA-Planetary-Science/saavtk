package edu.jhuapl.saavtk.camera.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.view.ViewActionListener;
import edu.jhuapl.saavtk.view.ViewChangeReason;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class CameraRecorderPanelOLD extends JPanel implements ActionListener, ViewActionListener, MouseListener, MouseMotionListener, PropertyChangeListener
{
	private vtkJoglPanelComponent renWin;
	private StatusBar statusBar;
	private static final String TITLE = "Renderer Recorder";

	public CameraRecorderPanelOLD(Renderer renderer) throws HeadlessException, CloneNotSupportedException
	{
//		super(TITLE);
		initComponents();

//        this.statusBar = statusBar;

        renWin = new vtkJoglPanelComponent();
        renWin.getComponent().setPreferredSize(new Dimension(550, 550));

//        vtkInteractorStyleImage style =
//            new vtkInteractorStyleImage();
//        renWin.setInteractorStyle(style);

//        renWin.getRenderWindow().GetInteractor().GetInteractorStyle().AddObserver("WindowLevelEvent",this,"levelsChanged");
        renWin.getRenderWindow().AddRenderer(renderer.getRenderWindowPanel().getRenderer());

        renWin.getComponent().addMouseListener(this);
        renWin.getComponent().addMouseMotionListener(this);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(renWin.getComponent(), gridBagConstraints);

//        createMenus();
//        pack();
//        setVisible(true);

        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                renWin.resetCamera();
                renWin.Render();
            }
        });
	}

//	private void createMenus()
//    {
//        JMenuBar menuBar = new JMenuBar();
//
//        JMenu fileMenu = new JMenu("File");
//        JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                File file = CustomFileChooser.showSaveDialog(renWin.getComponent(), "Export to PNG Image...", "image.png", "png");
//                RenderIoUtil.saveToFile(file, renWin, null);
//            }
//        });
//        fileMenu.add(mi);
//        fileMenu.setMnemonic('F');
//        menuBar.add(fileMenu);
//
//        setJMenuBar(menuBar);
//    }

	@Override
    public void mouseClicked(MouseEvent e)
    {
//        if (centerFrustumMode && e.getButton() == 1)
//        {
//            if (e.isAltDown())
//            {
////                System.out.println("Resetting pointing...");
////                ((PerspectiveImage)image).resetSpacecraftState();
//            }
//            else
//            {
//                centerFrustumOnPixel(e);
//
//                ((PerspectiveImage)image).loadFootprint();
////                ((PerspectiveImage)image).calculateFrustum();
//            }
////            PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(image.getKey());
////            boundary.update();
////            ((PerspectiveImageBoundary)boundary).firePropertyChange();
//
//            ((PerspectiveImage)image).firePropertyChange();
//        }
//
//      int pickSucceeded = doPick(e, imagePicker, renWin);
//      if (pickSucceeded == 1)
//      {
//          double[] p = imagePicker.GetPickPosition();
//
//          // Display selected pixel coordinates in console output
//          // Note we reverse x and y so that the pixel is in the form the camera
//          // position/orientation program expects.
//          System.out.println(p[1] + " " + p[0]);
//
//          // Display status bar message upon being picked
//          statusBar.setLeftTextSource(image, null, 0, p);
//      }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
//        if (centerFrustumMode && !e.isAltDown())
//        {
//            ((PerspectiveImage)image).calculateFrustum();
//            ((PerspectiveImage)image).firePropertyChange();
//        }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
//        if (centerFrustumMode && e.getButton() == 1)
//        {
//            if (!e.isAltDown())
//            {
//                centerFrustumOnPixel(e);
//                ((PerspectiveImage)image).loadFootprint();
//            }
//
//            ((PerspectiveImage)image).firePropertyChange();
//
//        }
//        else
//            updateSpectrumRegion(e);
    }

    private void centerFrustumOnPixel(MouseEvent e)
    {
//        int pickSucceeded = doPick(e, imagePicker, renWin);
//        if (pickSucceeded == 1)
//        {
//            double[] pickPosition = imagePicker.GetPickPosition();
//            // Note we reverse x and y so that the pixel is in the form the camera
//            // position/orientation program expects.
//            if (image instanceof PerspectiveImage)
//            {
//                PerspectiveImage pi = (PerspectiveImage)image;
//                pi.setTargetPixelCoordinates(pickPosition);
//            }
//        }
    }

    @Override
    public void mouseMoved(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    public void propertyChange(PropertyChangeEvent arg0)
    {
        if (renWin.getRenderWindow().GetNeverRendered() > 0)
            return;
        renWin.Render();
    }

    private void initComponents()
    {


    }

	@Override
	public void handleViewAction(Object aSource, ViewChangeReason aReason)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}
}