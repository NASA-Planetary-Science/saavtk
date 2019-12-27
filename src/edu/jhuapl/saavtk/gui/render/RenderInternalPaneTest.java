package edu.jhuapl.saavtk.gui.render;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import edu.jhuapl.saavtk.gui.render.axes.AxesPanel;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class RenderInternalPaneTest
{

    static
    {
        NativeLibraryLoader.loadAllVtkLibraries();
    }

    static vtkJoglPanelComponent renderObject = new vtkJoglPanelComponent();
    static AxesPanel axesPanel = new AxesPanel(renderObject);

    public static void main(String[] args)
    {
        vtkSphereSource source = new vtkSphereSource();
        source.Update();
        vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        mapper.SetInputData(source.GetOutput());
        vtkActor actor = new vtkActor();
        actor.SetMapper(mapper);
        renderObject.getRenderer().AddActor(actor);
        renderObject.getRenderer().ResetCamera();

        JFrame frame = new JFrame("JFrame");
        JDesktopPane desktop = new JDesktopPane();
        JInternalFrame internalFrame1 = new JInternalFrame("JInternalFrame1");
        JInternalFrame internalFrame2 = new JInternalFrame("JInternalFrame2");
        internalFrame1.add(renderObject.getComponent());
        internalFrame2.add(axesPanel.getComponent());

        frame.setContentPane(desktop);
        desktop.add(internalFrame1);
        desktop.add(internalFrame2, 10);

        frame.setVisible(true);
        desktop.setVisible(true);
        internalFrame1.setVisible(true);
        internalFrame2.setVisible(true);

        frame.setSize(1000, 1000);
        internalFrame1.setSize(600, 600);
        internalFrame1.setResizable(true);
        internalFrame2.setSize(600, 600);
        internalFrame2.setResizable(true);

    }

}
