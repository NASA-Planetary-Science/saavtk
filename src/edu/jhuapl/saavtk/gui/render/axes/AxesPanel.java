package edu.jhuapl.saavtk.gui.render.axes;

import java.awt.Color;
import java.awt.Dimension;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkAxesActor;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class AxesPanel extends vtkJoglPanelComponent {

	vtkJoglPanelComponent sourceComponent;
	vtkAxesActor actor=new vtkAxesActor();
	
	public AxesPanel(vtkJoglPanelComponent sourceComponent) {
		this.sourceComponent=sourceComponent;
		sourceComponent.getActiveCamera().AddObserver("ModifiedEvent", this, "Render");
		getRenderer().AddActor(actor);
		actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().BoldOn();
		actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().BoldOn();
		actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().BoldOn();
		actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().ItalicOff();
		actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().ItalicOff();
		actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().ItalicOff();
		actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().ShadowOn();
		actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().ShadowOn();
		actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().ShadowOn();
		//int fontSize=actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().GetFontSize();
		//int deltaFontSize=10;
		//actor.GetXAxisCaptionActor2D().GetTextActor().GetScaledTextProperty().SetFontSize(fontSize+deltaFontSize);
		//actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().SetFontSize(fontSize+deltaFontSize);
		//actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().SetFontSize(fontSize+deltaFontSize);
		
/*		actor.GetXAxisCaptionActor2D().SetAttachmentPoint(1,0,0);
		actor.GetYAxisCaptionActor2D().SetAttachmentPoint(0,1,0);
		actor.GetZAxisCaptionActor2D().SetAttachmentPoint(0,0,1);*/
		

		actor.GetXAxisShaftProperty().SetLineWidth(2);
		actor.GetYAxisShaftProperty().SetLineWidth(2);
		actor.GetZAxisShaftProperty().SetLineWidth(2);
		
		//printModeOff();
	
	}
	
	@Override
	public void Render() {
		Vector3D position=new Vector3D(sourceComponent.getActiveCamera().GetPosition());
		position=position.normalize();//.scalarMultiply(5);
		getActiveCamera().SetPosition(position.toArray());
		getActiveCamera().SetViewUp(sourceComponent.getActiveCamera().GetViewUp());
/*		double w=actor.GetXAxisCaptionActor2D().GetTextActor().GetWidth();
		double h=actor.GetXAxisCaptionActor2D().GetTextActor().GetHeight();
		actor.GetXAxisCaptionActor2D().GetTextActor().SetPosition(-w/2.,-h/2.);
		actor.GetXAxisCaptionActor2D().GetTextActor().SetPosition2(w/2.,h/2.);*/
		getRenderer().ResetCamera();
		super.Render();
	}
	
	/*public void printModeOn()
	{
		
		sourceComponent.getRenderer().SetBackground(0, 0, 0);
		actor.GetXAxisShaftProperty().SetColor(1,1,1);
		actor.GetYAxisShaftProperty().SetColor(1,1,1);
		actor.GetZAxisShaftProperty().SetColor(1,1,1);
		actor.GetXAxisTipProperty().SetColor(1,1,1);
		actor.GetYAxisTipProperty().SetColor(1,1,1);
		actor.GetZAxisTipProperty().SetColor(1,1,1);
		actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().SetColor(1,1,1);
		actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().SetColor(1,1,1);
		actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().SetColor(1,1,1);
		Render();
	}
	
	public void printModeOff()
	{
		Color color=this.getComponent().getBackground();
		getRenderer().SetBackground((double)color.getRed()/255., (double)color.getGreen()/255., (double)color.getBlue()/255.);
		actor.GetXAxisShaftProperty().SetColor(0,0,0);
		actor.GetYAxisShaftProperty().SetColor(0,0,0);
		actor.GetZAxisShaftProperty().SetColor(0,0,0);
		actor.GetXAxisTipProperty().SetColor(0,0,0);
		actor.GetYAxisTipProperty().SetColor(0,0,0);
		actor.GetZAxisTipProperty().SetColor(0,0,0);
		actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().SetColor(0,0,0);
		actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().SetColor(0,0,0);
		actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().SetColor(0,0,0);
		Render();
	}
	*/
}
