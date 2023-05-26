package edu.jhuapl.saavtk.gui.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import vtk.vtkGenericOpenGLRenderWindow;

@Deprecated
public class vtksbmtJoglCanvas extends vtksbmtJoglComponent<GLCanvas>
{

    // this class has the default vtk interaction behavior, which can be overridden using vtksbmtInteractorStyle and its subclasses (e.g. by calling setInteractorStyle(...))
    public vtksbmtJoglCanvas()
    {
        super(new vtkGenericOpenGLRenderWindow(), new vtksbmtGLCanvas());
        uiComponent.addGLEventListener(this.glEventListener);
    }

    // this nested class provides the (future) ability to override methods of the GLCanvas Component
    private static class vtksbmtGLCanvas extends GLCanvas
    {
        public vtksbmtGLCanvas()
        {
            super(new GLCapabilities(GLProfile.getDefault()));
        }

/*        @Override
        public void paint(Graphics arg0)
        {
            // TODO Auto-generated method stub
            super.paint(arg0);
            arg0.setColor(Color.white);
            arg0.drawString("Test", 100, 100);
        }

        @Override
        public void display()
        {
            // TODO Auto-generated method stub
            super.display();
        }*/
    }



}
