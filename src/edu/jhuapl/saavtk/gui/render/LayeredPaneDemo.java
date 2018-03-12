package edu.jhuapl.saavtk.gui.render;

import edu.jhuapl.saavtk.gui.render.axes.AxesPanel;
import vtk.vtkActor;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class LayeredPaneDemo extends JPanel {

	static {
		vtkNativeLibrary.LoadAllNativeLibraries();
	}

	static vtkJoglPanelComponent renderObject = new vtkJoglPanelComponent();
	static AxesPanel axesPanel = new AxesPanel(renderObject);

	private JLayeredPane layeredPane;

	public LayeredPaneDemo() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		// Create and set up the layered pane.
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(300, 310));
		layeredPane.setBorder(BorderFactory.createTitledBorder("(S)hooting (B)lanks at a (M)oving (T)arget"));

		layeredPane.add(renderObject.getComponent(), new Integer(0));
		renderObject.getComponent().setSize(layeredPane.getPreferredSize());
		renderObject.getComponent().setOpaque(true);
		//renderObject.getComponent().setBorder(BorderFactory.createLineBorder(Color.yellow, 10));
		vtkSphereSource source = new vtkSphereSource();
		source.Update();
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputData(source.GetOutput());
		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		renderObject.getRenderer().AddActor(actor);

		layeredPane.add(axesPanel.getComponent(), new Integer(1));
		axesPanel.getComponent().setSize(50, 50);
		axesPanel.getComponent().setOpaque(true);
		//
		axesPanel.getComponent().addMouseListener(new MouseAdapter() {
			int lastx,lasty;
			boolean dragging=false;
			
			@Override
			public void mouseEntered(MouseEvent e) {
				axesPanel.getComponent().setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
				}
			
			@Override
			public void mouseExited(MouseEvent e) {
				axesPanel.getComponent().setBorder(null);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				lastx=e.getX();
				lasty=e.getY();
				System.out.println(lastx+" "+lasty);
				dragging=true;
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				if (dragging)
					System.out.print("!");
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				dragging=false;
			}
		});
		
		
		renderObject.getComponent().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				System.out.println("renderObject mouse moved");
			}
		});

		renderObject.getRenderer().ResetCamera();

		add(layeredPane);
	}


	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("LayeredPaneDemo");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JComponent newContentPane = new LayeredPaneDemo();
				newContentPane.setOpaque(true); // content panes must be opaque
				frame.setContentPane(newContentPane);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
}
