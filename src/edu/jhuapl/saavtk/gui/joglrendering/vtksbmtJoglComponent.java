package edu.jhuapl.saavtk.gui.joglrendering;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkObject;
import vtk.vtkRenderWindow;
import vtk.rendering.vtkAbstractComponent;
import vtk.rendering.vtkInteractorForwarder;

/**
 * Provide JOGL based rendering component for VTK
 *
 * @author Sebastien Jourdain - sebastien.jourdain@kitware.com
 */
class vtksbmtJoglComponent<T extends java.awt.Component> extends vtkAbstractComponent<T> {

  protected T uiComponent;
  protected boolean isWindowCreated;
  protected GLEventListener glEventListener;
  protected vtkGenericOpenGLRenderWindow glRenderWindow;

  public static vtksbmtJoglComponent createGL()
  {
      vtksbmtJoglComponent<GLCanvas> component=new vtksbmtJoglComponent<GLCanvas>(new vtkGenericOpenGLRenderWindow(), new GLCanvas());
      component.uiComponent.addGLEventListener(component.glEventListener);
      return component;
  }

  public vtksbmtJoglComponent(vtkRenderWindow renderWindowToUse, T glContainer) {
    super(renderWindowToUse);
    this.isWindowCreated = false;
    this.uiComponent = glContainer;
    this.glRenderWindow = (vtkGenericOpenGLRenderWindow) renderWindowToUse;
    this.glRenderWindow.SetIsDirect(1);
    this.glRenderWindow.SetSupportsOpenGL(1);
    this.glRenderWindow.SetIsCurrent(true);

    // Create the JOGL Event Listener
    this.glEventListener = new GLEventListener() {
      public void init(GLAutoDrawable drawable) {
        vtksbmtJoglComponent.this.isWindowCreated = true;

        // Make sure the JOGL Context is current
        GLContext ctx = drawable.getContext();
        if (!ctx.isCurrent()) {
          ctx.makeCurrent();
        }

        // Init VTK OpenGL RenderWindow
        vtksbmtJoglComponent.this.glRenderWindow.SetMapped(1);
        vtksbmtJoglComponent.this.glRenderWindow.SetPosition(0, 0);
        vtksbmtJoglComponent.this.setSize(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
//        vtksbmtJoglComponent.this.setSize(drawable.getHeight(),drawable.getWidth());
        vtksbmtJoglComponent.this.glRenderWindow.OpenGLInit();
      }

      public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        vtksbmtJoglComponent.this.setSize(width, height);
      }

      public void display(GLAutoDrawable drawable) {
        vtksbmtJoglComponent.this.inRenderCall = true;
        getVTKLock().lock();
        vtksbmtJoglComponent.this.glRenderWindow.Render();
        getVTKLock().unlock();
        vtksbmtJoglComponent.this.inRenderCall = false;
      }

      public void dispose(GLAutoDrawable drawable) {
        vtksbmtJoglComponent.this.Delete();
        vtkObject.JAVA_OBJECT_MANAGER.gc(false);
      }
    };

    // Bind the interactor forwarder
    vtkInteractorForwarder eventForwarder = this.getInteractorForwarder();
    this.uiComponent.addMouseListener(eventForwarder);
    this.uiComponent.addMouseMotionListener(eventForwarder);
    this.uiComponent.addMouseWheelListener(eventForwarder);
    this.uiComponent.addKeyListener(eventForwarder);

    // Make sure when VTK internaly request a Render, the Render get
    // properly triggered
    renderWindowToUse.AddObserver("WindowFrameEvent", this, "Render");
    renderWindowToUse.GetInteractor().AddObserver("RenderEvent", this, "Render");
    renderWindowToUse.GetInteractor().SetEnableRender(false);
  }

  public T getComponent() {
    return this.uiComponent;
  }

  /**
   * Render the internal component
   */
  public void Render() {
    // Make sure we can render
    if (!inRenderCall) {
        uiComponent.repaint();
    }
  }

  /**
   * @return true if the graphical component has been properly set and
   * operation can be performed on it.
   */
  public boolean isWindowSet() {
    return this.isWindowCreated;
  }

}
