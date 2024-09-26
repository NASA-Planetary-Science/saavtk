package edu.jhuapl.saavtk.gui.render.axes;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkAxesActor;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class AxesPanel extends vtkJoglPanelComponent {

	vtkJoglPanelComponent sourceComponent;
	vtkAxesActor actor = new vtkAxesActor();

/*	protected static class Properties {
		static Color xColor = Color.RED;
		static Color yColor = Color.GREEN;
		static Color zColor = Color.BLUE;
		static Color fontColor = Color.WHITE;
		static boolean boldOn=true;
		static boolean italicOn=true;
		static boolean shadowOn=true;
		static int fontsize=25;
		static int offsetu=-10;
		static int offsetv=-10;
		static double labelpos=1.4;
		static double linewidth=3;
		static double conelength=0.25;
		static double coneradius=0.25;
		static double shaftlength=1;

	}*/

	public AxesPanel(vtkJoglPanelComponent sourceComponent) {
		this.sourceComponent = sourceComponent;
		sourceComponent.getActiveCamera().AddObserver("ModifiedEvent", this, "Render");
		getRenderer().AddActor(actor);
		//sync();

		actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().BoldOn();
		actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().BoldOn();
		actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().BoldOn();

		actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().ItalicOn();
		actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().ItalicOn();
		actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().ItalicOn();
		
		actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().ShadowOn();
		actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().ShadowOn();
		actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().ShadowOn();

		actor.GetXAxisCaptionActor2D().GetTextActor().GetScaledTextProperty().SetFontSize(15);
		actor.GetYAxisCaptionActor2D().GetTextActor().GetScaledTextProperty().SetFontSize(15);
		actor.GetZAxisCaptionActor2D().GetTextActor().GetScaledTextProperty().SetFontSize(15);

		actor.GetXAxisCaptionActor2D().SetDisplayPosition(-10, -10);
		actor.GetYAxisCaptionActor2D().SetDisplayPosition(-10, -10);
		actor.GetZAxisCaptionActor2D().SetDisplayPosition(-10, -10);

		actor.SetNormalizedLabelPosition(1.4, 1.4, 1.4);

		actor.GetXAxisShaftProperty().SetLineWidth(2);
		actor.GetYAxisShaftProperty().SetLineWidth(2);
		actor.GetZAxisShaftProperty().SetLineWidth(2);

		actor.GetXAxisCaptionActor2D().GetProperty().SetColor(colorToDoubleArray(Color.RED));
		actor.GetYAxisCaptionActor2D().GetProperty().SetColor(colorToDoubleArray(Color.GREEN));
		actor.GetZAxisCaptionActor2D().GetProperty().SetColor(colorToDoubleArray(Color.BLUE));

		actor.GetXAxisShaftProperty().SetColor(colorToDoubleArray(Color.RED));
		actor.GetYAxisShaftProperty().SetColor(colorToDoubleArray(Color.GREEN));
		actor.GetZAxisShaftProperty().SetColor(colorToDoubleArray(Color.BLUE));
		
		actor.GetXAxisTipProperty().SetColor(colorToDoubleArray(Color.RED));
		actor.GetYAxisTipProperty().SetColor(colorToDoubleArray(Color.GREEN));
		actor.GetZAxisTipProperty().SetColor(colorToDoubleArray(Color.BLUE));

		actor.SetConeRadius(actor.GetConeRadius()*2);

		resetCamera();
//		actor.SetConeRadius(Properties.coneradius);
//		actor.SetNormalizedShaftLength(Properties.shaftlength, Properties.shaftlength, Properties.shaftlength);
//		actor.SetNormalizedTipLength(Properties.conelength, Properties.conelength, Properties.conelength);

	}

/*	public void sync() {
		if (Properties.boldOn) {
			actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().BoldOn();
			actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().BoldOn();
			actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().BoldOn();
		} else {
			actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().BoldOff();
			actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().BoldOff();
			actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().BoldOff();

		}

		if (Properties.italicOn) {
			actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().ItalicOn();
			actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().ItalicOn();
			actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().ItalicOn();

		} else {
			actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().ItalicOff();
			actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().ItalicOff();
			actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().ItalicOff();
		}

		if (Properties.shadowOn) {
			actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().ShadowOn();
			actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().ShadowOn();
			actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().ShadowOn();
		} else {
			actor.GetXAxisCaptionActor2D().GetCaptionTextProperty().ShadowOff();
			actor.GetYAxisCaptionActor2D().GetCaptionTextProperty().ShadowOff();
			actor.GetZAxisCaptionActor2D().GetCaptionTextProperty().ShadowOff();

		}

		actor.GetXAxisCaptionActor2D().GetTextActor().GetScaledTextProperty().SetFontSize(Properties.fontsize);
		actor.GetYAxisCaptionActor2D().GetTextActor().GetScaledTextProperty().SetFontSize(Properties.fontsize);
		actor.GetZAxisCaptionActor2D().GetTextActor().GetScaledTextProperty().SetFontSize(Properties.fontsize);

		actor.GetXAxisCaptionActor2D().SetDisplayPosition(Properties.offsetu, Properties.offsetv);
		actor.GetYAxisCaptionActor2D().SetDisplayPosition(Properties.offsetu, Properties.offsetv);
		actor.GetZAxisCaptionActor2D().SetDisplayPosition(Properties.offsetu, Properties.offsetv);

		actor.SetNormalizedLabelPosition(Properties.labelpos, Properties.labelpos, Properties.labelpos);

		actor.GetXAxisShaftProperty().SetLineWidth(Properties.linewidth);
		actor.GetYAxisShaftProperty().SetLineWidth(Properties.linewidth);
		actor.GetZAxisShaftProperty().SetLineWidth(Properties.linewidth);

		actor.GetXAxisCaptionActor2D().GetProperty().SetColor(colorToDoubleArray(Properties.fontColor));
		actor.GetYAxisCaptionActor2D().GetProperty().SetColor(colorToDoubleArray(Properties.fontColor));
		actor.GetZAxisCaptionActor2D().GetProperty().SetColor(colorToDoubleArray(Properties.fontColor));

		actor.GetXAxisShaftProperty().SetColor(colorToDoubleArray(Properties.xColor));
		actor.GetYAxisShaftProperty().SetColor(colorToDoubleArray(Properties.yColor));
		actor.GetZAxisShaftProperty().SetColor(colorToDoubleArray(Properties.zColor));
		actor.GetXAxisTipProperty().SetColor(colorToDoubleArray(Properties.xColor));
		actor.GetYAxisTipProperty().SetColor(colorToDoubleArray(Properties.yColor));
		actor.GetZAxisTipProperty().SetColor(colorToDoubleArray(Properties.zColor));

		actor.SetConeRadius(Properties.coneradius);
		actor.SetNormalizedShaftLength(Properties.shaftlength, Properties.shaftlength, Properties.shaftlength);
		actor.SetNormalizedTipLength(Properties.conelength, Properties.conelength, Properties.conelength);
		
		actor.Modified();

	}
*/
	public static double[] colorToDoubleArray(Color color) {
		return new double[] { (double) color.getRed() / 255., (double) color.getGreen() / 255.,
				(double) color.getBlue() / 255. };
	}

	@Override
	public void Render() {
		Vector3D position = new Vector3D(sourceComponent.getActiveCamera().GetPosition());
		position = position.normalize();
		getActiveCamera().SetPosition(position.toArray());
		getActiveCamera().SetViewUp(sourceComponent.getActiveCamera().GetViewUp());
		getRenderer().ResetCamera();
		getActiveCamera().Dolly(1.3);
		getRenderer().ResetCameraClippingRange();
		super.Render();
	}

/*	public Color getxColor() {
		return Properties.xColor;
	}

	public void setxColor(Color xColor) {
		Properties.xColor = xColor;
		sync();
	}

	public Color getyColor() {
		return Properties.yColor;
	}

	public void setyColor(Color yColor) {
		Properties.yColor = yColor;
		sync();
	}

	public Color getzColor() {
		return Properties.zColor;
	}

	public void setzColor(Color zColor) {
		Properties.zColor = zColor;
		sync();
	}

	public Color getFontColor() {
		return Properties.fontColor;
	}

	public void setFontColor(Color fontColor) {
		Properties.fontColor = fontColor;
		sync();
	}

	public boolean isBoldOn() {
		return Properties.boldOn;
	}

	public void setBoldOn(boolean boldOn) {
		Properties.boldOn = boldOn;
		sync();
	}

	public boolean isItalicOn() {
		return Properties.italicOn;
	}

	public void setItalicOn(boolean italicOn) {
		Properties.italicOn = italicOn;
		sync();
	}

	public boolean isShadowOn() {
		return Properties.shadowOn;
	}

	public void setShadowOn(boolean shadowOn) {
		Properties.shadowOn = shadowOn;
		sync();
	}

	public int getFontsize() {
		return Properties.fontsize;
	}

	public void setFontsize(int fontsize) {
		Properties.fontsize = fontsize;
		sync();
	}

	public int getOffsetu() {
		return Properties.offsetu;
	}

	public void setOffsetu(int offsetu) {
		Properties.offsetu = offsetu;
		sync();
	}

	public int getOffsetv() {
		return Properties.offsetv;
	}

	public void setOffsetv(int offsetv) {
		Properties.offsetv = offsetv;
		sync();
	}

	public double getLabelpos() {
		return Properties.labelpos;
	}

	public void setLabelpos(double labelpos) {
		Properties.labelpos = labelpos;
		sync();
	}

	public double getLinewidth() {
		return Properties.linewidth;
	}

	public void setLinewidth(double linewidth) {
		Properties.linewidth = linewidth;
		sync();
	}

	public double getConelength() {
		return Properties.conelength;
	}

	public void setConelength(double conelength) {
		Properties.conelength = conelength;
		sync();
	}

	public double getConeradius() {
		return Properties.coneradius;
	}

	public void setConeradius(double coneradius) {
		Properties.coneradius = coneradius;
		sync();
	}

	public double getShaftlength() {
		return Properties.shaftlength;
	}

	public void setShaftlength(double shaftlength) {
		Properties.shaftlength = shaftlength;
		sync();
	}
*/
}
