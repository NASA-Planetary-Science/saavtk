package edu.jhuapl.saavtk.gui.jogl;

import javax.swing.JFrame;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkUnsignedCharArray;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;

public class StereoRenderCanvas extends vtksbmtJoglCanvas
{

/*    vtkRenderer lftRenderer=new vtkRenderer();
    vtkRenderer rgtRenderer=new vtkRenderer();
    vtkRenderWindow lftRenderWindow=new vtkRenderWindow();
    vtkRenderWindow rgtRenderWindow=new vtkRenderWindow();*/
    vtksbmtJoglCanvas lft=new vtksbmtJoglCanvas();
    vtksbmtJoglCanvas rgt=new vtksbmtJoglCanvas();
    int w,h;

    vtkUnsignedCharArray lftData=new vtkUnsignedCharArray();
    vtkUnsignedCharArray rgtData=new vtkUnsignedCharArray();

    public StereoRenderCanvas(vtkActor testActor)
    {
/*        lftRenderer.SetRenderWindow(lftRenderWindow);
        rgtRenderer.SetRenderWindow(rgtRenderWindow);
        //lftRenderWindow.OffScreenRenderingOn();
        //rgtRenderWindow.OffScreenRenderingOn();
        lftRenderWindow.StereoCapableWindowOn();
        rgtRenderWindow.StereoCapableWindowOn();
        lftRenderWindow.SetStereoTypeToLeft();
        rgtRenderWindow.SetStereoTypeToRight();
        recomputeSize();
        //
        lftRenderer.AddActor(testActor);
        rgtRenderer.AddActor(testActor);
        lftRenderer.GetActiveCamera().ShallowCopy(getActiveCamera());
        rgtRenderer.GetActiveCamera().ShallowCopy(getActiveCamera());*/
        lft.getRenderer().AddActor(testActor);
        rgt.getRenderer().AddActor(testActor);
        lft.getRenderWindow().StereoCapableWindowOn();
        rgt.getRenderWindow().StereoCapableWindowOn();
        lft.getRenderWindow().SetStereoTypeToLeft();
        rgt.getRenderWindow().SetStereoTypeToRight();
    }

    @Override
    public void setSize(int w, int h)
    {
        super.setSize(w, h);
        this.w=w;
        this.h=h;
        //recomputeSize();
        lft.setSize(w/2, h);
        rgt.setSize(w/2, h);
    }


/*    private void recomputeSize()
    {
        this.w=getRenderWindow().GetSize()[0];
        this.h=getRenderWindow().GetSize()[1];
        lftRenderWindow.SetSize(w/2,h); // might need to adjust width to avoid buffer overflow
        rgtRenderWindow.SetSize(w/2,h);
    }*/

    @Override
    public void Render()
    {
/*        lftRenderWindow.Render();
        rgtRenderWindow.Render();
        System.out.println(lftRenderWindow.GetSize()[0]+" "+lftRenderWindow.GetSize()[1]);
        lftRenderWindow.GetRGBACharPixelData(0, 0, lftRenderWindow.GetSize()[0]-1, lftRenderWindow.GetSize()[1]-1, 1, lftData);
        rgtRenderWindow.GetRGBACharPixelData(0, 0, rgtRenderWindow.GetSize()[0]-1, rgtRenderWindow.GetSize()[1]-1, 1, rgtData);
        getRenderWindow().SetRGBACharPixelData(0, 0, w/2-1, h-1, lftData, 1, 0);
        getRenderWindow().SetRGBACharPixelData(w/2, 0, w-1, h-1, rgtData, 1, 0);
        getRenderWindow().Render();*/
        lft.Render();
        rgt.Render();
        lft.getRenderWindow().GetRGBACharPixelData(0, 0, lft.getRenderWindow().GetSize()[0]-1, lft.getRenderWindow().GetSize()[1]-1, 1, lftData);
        rgt.getRenderWindow().GetRGBACharPixelData(0, 0, rgt.getRenderWindow().GetSize()[0]-1, rgt.getRenderWindow().GetSize()[1]-1, 1, rgtData);
        getRenderWindow().SetRGBACharPixelData(0, 0, w/2-1, h-1, lftData, 1, 0);
        getRenderWindow().SetRGBACharPixelData(w/2, 0, w-1, h-1, rgtData, 1, 0);
    }

    public static void main(String[] args)
    {
        NativeLibraryLoader.loadVtkLibraries();

        vtkConeSource source=new vtkConeSource();
        source.Update();
        vtkPolyData polyData=source.GetOutput();
        vtkPolyDataMapper mapper=new vtkPolyDataMapper();
        mapper.SetInputData(polyData);
        vtkActor actor=new vtkActor();
        actor.SetMapper(mapper);

        final vtksbmtJoglCanvas canvas=new StereoRenderCanvas(actor);
        canvas.setInteractorStyle(new vtksbmtInteractorStyle(canvas));

        JFrame frame=new JFrame();
        frame.setSize(1200, 600);
        canvas.setSize(1200, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(canvas.getComponent());
        frame.setVisible(true);

        canvas.getRenderer().AddActor(actor);
    }

}
