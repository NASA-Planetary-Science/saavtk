package edu.jhuapl.saavtk.gui.render.axes;

import java.awt.Color;
import java.util.Arrays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkAxes;
import vtk.vtkCaptionActor2D;
import vtk.vtkFloatArray;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkTextActor;
import vtk.vtkTextActor3D;
import vtk.vtkUnsignedCharArray;

public class Axes
{
	Color						polyColorX		= Color.RED;
	Color						polyColorY		= Color.GREEN;
	Color						polyColorZ		= Color.BLUE;
	Color						originColor		= Color.GRAY;

	String						labelXtext		= "X";
	String						labelYtext		= "Y";
	String						labelZtext		= "Z";

	Color						textColorX		= Color.RED;
	Color						textColorY		= Color.GREEN;
	Color						textColorZ		= Color.BLUE;

	double						viewSize		= 0.25;
	double						viewX			= -0.75;
	double						viewY			= -0.75;

	double						lineWidth		= 3;

	final vtkPolyData			polyData		= new vtkPolyData();
	final vtkPolyDataMapper		mapper			= new vtkPolyDataMapper();
	final vtkActor				actor			= new vtkActor();
	final vtkUnsignedCharArray	pointColors		= new vtkUnsignedCharArray();

	vtkCaptionActor2D			labelX			= new vtkCaptionActor2D();
	vtkCaptionActor2D			labelY			= new vtkCaptionActor2D();
	vtkCaptionActor2D			labelZ			= new vtkCaptionActor2D();

	double						labelViewSize	= 0.01;

	boolean						visible			= true;

	public Axes()
	{
		vtkAxes axes = new vtkAxes();
		axes.Update();
		axes.ComputeNormalsOff();
		polyData.DeepCopy(axes.GetOutput());
		//
		pointColors.SetNumberOfComponents(3);
		pointColors.SetNumberOfTuples(6);
		pointColors.SetName("Colors");
		//
		labelX.BorderOff();
		labelY.BorderOff();
		labelZ.BorderOff();
		labelX.LeaderOff();
		labelY.LeaderOff();
		labelZ.LeaderOff();
		//
		update();
	}

	protected void update()
	{
		labelX.SetCaption(labelXtext);
		labelY.SetCaption(labelYtext);
		labelZ.SetCaption(labelZtext);
		labelX.GetTextActor().GetProperty().SetColor(textColorX.getRed() / 255., textColorX.getGreen() / 255.,
				textColorX.getBlue() / 255.);
		labelY.GetTextActor().GetProperty().SetColor(textColorY.getRed() / 255., textColorY.getGreen() / 255.,
				textColorY.getBlue() / 255.);
		labelZ.GetTextActor().GetProperty().SetColor(textColorZ.getRed() / 255., textColorZ.getGreen() / 255.,
				textColorZ.getBlue() / 255.);
		pointColors.SetTuple3(0, originColor.getRed(), originColor.getGreen(), originColor.getBlue());
		pointColors.SetTuple3(1, polyColorX.getRed(), polyColorX.getGreen(), polyColorX.getBlue());
		pointColors.SetTuple3(2, originColor.getRed(), originColor.getGreen(), originColor.getBlue());
		pointColors.SetTuple3(3, polyColorY.getRed(), polyColorY.getGreen(), polyColorY.getBlue());
		pointColors.SetTuple3(4, originColor.getRed(), originColor.getGreen(), originColor.getBlue());
		pointColors.SetTuple3(5, polyColorZ.getRed(), polyColorZ.getGreen(), polyColorZ.getBlue());
		polyData.GetPointData().SetScalars(pointColors);
		mapper.SetScalarModeToUsePointData();
		mapper.SetInputData(polyData);
		actor.GetProperty().LightingOff();
		actor.SetMapper(mapper);
		actor.GetProperty().SetLineWidth((float)lineWidth);
		labelX.SetPosition(polyData.GetPoint(1));
		labelY.SetPosition(polyData.GetPoint(3));
		labelZ.SetPosition(polyData.GetPoint(5));
		setVisible(visible);
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
		int flag = visible ? 1 : 0;
		actor.SetVisibility(flag);
		labelX.SetVisibility(flag);
		labelY.SetVisibility(flag);
		labelZ.SetVisibility(flag);
	}

	public boolean isVisible()
	{
		return visible;
	}

	public vtkActor getActor()
	{
		return actor;
	}

	public vtkCaptionActor2D getLabelActorX()
	{
		return labelX;
	}

	public vtkCaptionActor2D getLabelActorY()
	{
		return labelY;
	}

	public vtkCaptionActor2D getLabelActorZ()
	{
		return labelZ;
	}

	public Vector3D getTipCoordinatesX()
	{
		return new Vector3D(polyData.GetPoint(1));
	}

	public Vector3D getTipCoordinatesY()
	{
		return new Vector3D(polyData.GetPoint(3));
	}

	public Vector3D getTipCoordinatesZ()
	{
		return new Vector3D(polyData.GetPoint(5));
	}

	public void setPolyColorX(Color polyColorX)
	{
		this.polyColorX = polyColorX;
		update();
	}

	public void setPolyColorY(Color polyColorY)
	{
		this.polyColorY = polyColorY;
		update();
	}

	public void setPolyColorZ(Color polyColorZ)
	{
		this.polyColorZ = polyColorZ;
		update();
	}

	public void setOriginColor(Color originColor)
	{
		this.originColor = originColor;
		update();
	}

	public void setLabelX(String labelX)
	{
		this.labelXtext = labelX;
		update();
	}

	public void setLabelY(String labelY)
	{
		this.labelYtext = labelY;
		update();
	}

	public void setLabelZ(String labelZ)
	{
		this.labelZtext = labelZ;
		update();
	}

	public void setTextColorX(Color textColorX)
	{
		this.textColorX = textColorX;
		update();
	}

	public void setTextColorY(Color textColorY)
	{
		this.textColorY = textColorY;
		update();
	}

	public void setTextColorZ(Color textColorZ)
	{
		this.textColorZ = textColorZ;
		update();
	}

	public void setViewSize(double viewSize)
	{
		this.viewSize = viewSize;
		update();
	}

	public void setLineWidth(double lineWidth)
	{
		this.lineWidth = lineWidth;
		update();
	}

	public void setViewX(double viewX)
	{
		this.viewX = viewX;
		update();
	}

	public void setViewY(double viewY)
	{
		this.viewY = viewY;
		update();
	}

}
