package edu.jhuapl.saavtk.colormap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.jhuapl.saavtk.gui.Renderer;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkScalarBarActor;
import vtk.vtkScalarBarWidget;

public class Colorbar implements PropertyChangeListener
{
    // for interaction
	Renderer renderer;
    vtkRenderWindowInteractor interactor;

    vtkScalarBarWidget widget=new vtkScalarBarWidget();
	vtkScalarBarActor actor=new vtkScalarBarActor();
	boolean visible=false;
	boolean hovering=false;
	Colormap cmap;

	public Colorbar(Renderer renderer)
	{
/*		chooser.addPropertyChangeListener(new PropertyChangeListener()
		{

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (! (evt.getNewValue() instanceof String))
				{
					System.out.println(evt.getNewValue()+" "+evt.getPropertyName());
					return;
				}

				String cname=(String)evt.getNewValue();
				Colormap c=Colormaps.getNewInstanceOfBuiltInColormap(cname);
				c.setRangeMin(cmap.getRangeMin());
				c.setRangeMax(cmap.getRangeMax());
				c.setLog(cmap.getLog());
				c.setNumberOfLevels(cmap.getNumberOfLevels());
				setColormap(c);

	            chooser.setVisible(false);
			}
		});*/

		this.renderer=renderer;

        //vtkCoordinate coords=actor.GetPositionCoordinate();
        //coords.SetCoordinateSystemToNormalizedViewport();
        //coords.SetValue(0.2,0.01);
        //actor.SetOrientationToHorizontal();
        //actor.SetWidth(0.6);
        //actor.SetHeight(0.1275);
        //vtkTextProperty tp=new vtkTextProperty();
        //tp.SetFontSize(10);
        //actor.SetTitleTextProperty(tp);

        widget.CreateDefaultRepresentation();
        widget.GetScalarBarRepresentation().SetOrientation(0);
        widget.GetScalarBarRepresentation().SetPosition(0.2, 0.01);
        widget.GetScalarBarRepresentation().SetPosition2(0.6,0.1275);
        widget.ResizableOn();

        actor.SetOrientationToHorizontal();
        actor.SetTitle(" ");
        widget.SetScalarBarActor(actor);

        this.interactor=renderer.getRenderWindowPanel().getRenderWindowInteractor();
        interactor.AddObserver("RenderEvent", this, "synchronizationKludge");
        interactor.AddObserver("MouseMoveEvent", this, "interactionKludge");
        widget.SetInteractor(interactor);
        widget.EnabledOn();
	}



	public void synchronizationKludge()
	{
		if (actor.GetLookupTable()!=null && cmap!=null)
		{
	        actor.SetLookupTable(cmap.getLookupTable());
	        actor.GetLookupTable().SetRange(cmap.getRangeMin(), cmap.getRangeMax());
		}
	}

	public void interactionKludge()
	{
		if (widget.GetBorderRepresentation().GetInteractionState()>0 && visible)
		{
			hovering=true;
            actor.DrawBackgroundOn();
            if (interactor.GetControlKey()==1)
            	widget.GetScalarBarRepresentation().ProportionalResizeOn();
            else
            	widget.GetScalarBarRepresentation().ProportionalResizeOff();
		}
		else
		{
			hovering=false;
			actor.DrawBackgroundOff();
		}
	}

	public void setTitle(String str)
	{
		widget.GetScalarBarActor().SetTitle(str);
	}

	public vtkScalarBarWidget getWidget()
	{
		return widget;
	}

	public vtkScalarBarActor getActor()
	{
		return widget.GetScalarBarActor();
	}

	public void setColormap(Colormap cmap)
	{

		this.cmap=cmap;
		cmap.addPropertyChangeListener(this);
		widget.GetScalarBarActor().SetLookupTable(cmap.getLookupTable());
		widget.GetScalarBarActor().Modified();
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean flag)
	{
		if (flag)
		{
            actor.SetVisibility(1);
			visible=true;
		}
		else
		{
			widget.GetScalarBarActor().SetVisibility(0);
			visible=false;
		}
	}



    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(Colormap.colormapPropertyChanged))
            widget.GetScalarBarActor().SetLookupTable(cmap.getLookupTable());
    }


}
