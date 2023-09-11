package edu.jhuapl.saavtk.gui.render;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.axes.AxesPanel;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class LayeredPaneDemo extends JPanel
{

    static
    {
        NativeLibraryLoader.loadVtkLibraries();
    }

    static vtkJoglPanelComponent renderObject = new vtkJoglPanelComponent();
    static AxesPanel axesPanel = new AxesPanel(renderObject);

    private JLayeredPane layeredPane;

    public LayeredPaneDemo()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // Create and set up the layered pane.
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(300, 310));
        layeredPane.setBorder(BorderFactory.createTitledBorder("(S)hooting (B)lanks at a (M)oving (T)arget"));

        layeredPane.add(renderObject.getComponent(), new Integer(0));
        renderObject.getComponent().setSize(layeredPane.getPreferredSize());
        renderObject.getComponent().setOpaque(true);
        // renderObject.getComponent().setBorder(BorderFactory.createLineBorder(Color.yellow,
        // 10));
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

        /*
         * axesPanel.getInteractorForwarder().setEventInterceptor(new
         * vtkEventInterceptor() { int xold,yold; int mx0,my0; boolean dragging=false;
         * 
         * @Override public boolean mouseWheelMoved(MouseWheelEvent e) { // TODO
         * Auto-generated method stub return false; }
         * 
         * @Override public boolean mouseReleased(MouseEvent e) { dragging=false; return
         * true; }
         * 
         * @Override public boolean mousePressed(MouseEvent e) {
         * xold=axesPanel.getComponent().getLocation().x;
         * yold=axesPanel.getComponent().getLocation().y; mx0=e.getX(); my0=e.getY();
         * dragging=true; return true; }
         * 
         * @Override public boolean mouseMoved(MouseEvent e) {
         * System.out.println(e.getComponent()); if (dragging) {
         * axesPanel.getComponent().setLocation(xold+e.getX()-mx0,yold+e.getY()-my0);
         * axesPanel.getComponent().repaint();
         * System.out.println(e.getX()+" "+e.getY()); }
         * 
         * return false; }
         * 
         * @Override public boolean mouseExited(MouseEvent e) { // TODO Auto-generated
         * method stub return false; }
         * 
         * @Override public boolean mouseEntered(MouseEvent e) { // TODO Auto-generated
         * method stub return false; }
         * 
         * @Override public boolean mouseDragged(MouseEvent e) { return false; }
         * 
         * @Override public boolean mouseClicked(MouseEvent e) { // TODO Auto-generated
         * method stub return false; }
         * 
         * @Override public boolean keyTyped(KeyEvent e) { // TODO Auto-generated method
         * stub return false; }
         * 
         * @Override public boolean keyReleased(KeyEvent e) { // TODO Auto-generated
         * method stub return false; }
         * 
         * @Override public boolean keyPressed(KeyEvent e) { // TODO Auto-generated
         * method stub return false; } });
         */

        renderObject.getRenderer().ResetCamera();

        add(layeredPane);
    }

    void test()
    {
        System.out.println("!");
    }

    public static void main(String[] args)
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
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
