package edu.jhuapl.saavtk.scalebar;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.view.ViewActionListener;
import edu.jhuapl.saavtk.view.ViewChangeReason;
import edu.jhuapl.saavtk.vtk.VtkResource;
import vtk.vtkActor2D;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper2D;
import vtk.vtkProp;
import vtk.vtkTextActor;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Painter used to render a scale bar onto a vtk scene.
 * <P>
 * This painter will register as a {@link ViewActionListener} to provide
 * automatic updates.
 *
 * @author lopeznr1
 */
public class ScaleBarPainter implements ViewActionListener, VtkPropProvider, VtkResource
{
	// Reference vars
	private final Renderer refRenderer;

	// State vars
	private boolean isVisible;
	private double dimSizeX, dimSizeY;
	private NumberFormat dispNF;

	// VTK vars
	private vtkPolyData vScaleBarPD;
	private vtkPolyDataMapper2D vScaleBarM;
	private vtkActor2D vScaleBarA;
	private vtkTextActor vScaleBarTA;

	/**
	 * Standard Constructor
	 */
	public ScaleBarPainter(Renderer aRenderer)
	{
		refRenderer = aRenderer;

		isVisible = true;
		dimSizeX = 150;
		dimSizeY = 16;
		dispNF = new DecimalFormat("0.00");

		vScaleBarPD = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray polys = new vtkCellArray();
		vScaleBarPD.SetPoints(points);
		vScaleBarPD.SetPolys(polys);

		points.SetNumberOfPoints(4);

		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(4);
		for (int i = 0; i < 4; ++i)
			idList.SetId(i, i);
		polys.InsertNextCell(idList);

		vScaleBarM = new vtkPolyDataMapper2D();
		vScaleBarM.SetInputData(vScaleBarPD);

		vScaleBarA = new vtkActor2D();
		vScaleBarA.SetMapper(vScaleBarM);

		vScaleBarTA = new vtkTextActor();

		vScaleBarA.GetProperty().SetColor(1.0, 1.0, 1.0);
		vScaleBarA.GetProperty().SetOpacity(0.5);
		vScaleBarTA.GetTextProperty().SetColor(0.0, 0.0, 0.0);
		vScaleBarTA.GetTextProperty().SetJustificationToCentered();
		vScaleBarTA.GetTextProperty().BoldOn();

		// Register for events of interest
		refRenderer.addViewChangeListener(this);
	}

	/**
	 * Returns the number of decimal places that should be displayed.
	 */
	public NumberFormat getDispNumberFormat()
	{
		return dispNF;
	}

	/**
	 * Returns the scale bar's visibility.
	 */
	public boolean getIsVisible()
	{
		return isVisible;
	}

	/**
	 * Returns the scale bar's width in pixels.
	 */
	public double getSizeX()
	{
		return dimSizeX;
	}

	/**
	 * Returns the scale bar's height in pixels.
	 */
	public double getSizeY()
	{
		return dimSizeY;
	}

	/**
	 * Returns the number of decimal places that should be displayed.
	 */
	public void setDispNumberFormat(NumberFormat aDispNF)
	{
		dispNF = aDispNF;

		vtkUpdateState();
	}

	/**
	 * Sets the scale bar's visibility.
	 */
	public void setIsVisible(boolean aBool)
	{
		// Bail if no state change
		if (isVisible == aBool)
			return;
		isVisible = aBool;

		// Send out the update notification
		refRenderer.notifySceneChange();
	}

	/**
	 * Sets the scale bar's dimensions (in pixels).
	 */
	public void setSize(double aSizeX, double aSizeY)
	{
		// Bail if no state change
		boolean isChanged = false;
		isChanged |= Double.compare(dimSizeX, aSizeX) != 0;
		isChanged |= Double.compare(dimSizeY, aSizeY) != 0;
		if (isChanged == false)
			return;

		dimSizeX = aSizeX;
		dimSizeY = aSizeY;

		vtkUpdateState();
	}

	@Override
	public Collection<vtkProp> getProps()
	{
		// Bail if not visible
		if (isVisible == false)
			return ImmutableList.of();

		// Bail if no valid pixel size (span)
		double tmpPixelSpan = refRenderer.getNominalPixelSpan();
		if (Double.isNaN(tmpPixelSpan) == true)
			return ImmutableList.of();

		// Return the list of all vtkProps
		List<vtkProp> retL = new ArrayList<>();
		retL.add(vScaleBarA);
		retL.add(vScaleBarTA);
		return retL;
	}

	@Override
	public void handleViewAction(Object aSource, ViewChangeReason aReason)
	{
		vtkUpdateState();
	}

	@Override
	public void vtkDispose()
	{
		vScaleBarA.Delete();
		vScaleBarTA.Delete();
	}

	@Override
	public void vtkUpdateState()
	{
		updatePosition();
		updateInfoMsg();

		vScaleBarA.Modified();
		vScaleBarTA.Modified();

		// Send out the update notification
		refRenderer.notifySceneChange();
	}

	/**
	 * Helper method that will update the text component of the scale bar.
	 */
	private void updateInfoMsg()
	{
		// Bail if no valid span
		double nominalPixelSpan = refRenderer.getNominalPixelSpan();
		if (Double.isNaN(nominalPixelSpan) == true)
			return;

		// Update the scale bar's text
		double fullSpanKm = nominalPixelSpan * dimSizeX;
		String tmpMsg = dispNF.format(fullSpanKm) + " km";
		if (fullSpanKm < 1.0)
			tmpMsg = dispNF.format(fullSpanKm * 1000.0) + " m";
		vScaleBarTA.SetInput(tmpMsg);
	}

	/**
	 * Helper method that will update the position of scale bar (and associated
	 * text).
	 */
	private void updatePosition()
	{
		// Compute lower right corner
		vtkJoglPanelComponent tmpRenWin = refRenderer.getRenderWindowPanel();
		int compW = tmpRenWin.getComponent().getWidth();
		int padAmt = 7;
		double x = compW - dimSizeX - padAmt;
		double y = padAmt;

		// Update scale bar
		vtkPoints points = vScaleBarPD.GetPoints();
		points.SetPoint(0, x, y, 0.0);
		points.SetPoint(1, x + dimSizeX, y, 0.0);
		points.SetPoint(2, x + dimSizeX, y + dimSizeY, 0.0);
		points.SetPoint(3, x, y + dimSizeY, 0.0);

		// Update scale bar text
		int fontSize = (int) (dimSizeY - 4);
		vScaleBarTA.SetPosition(x + dimSizeX / 2, y + 2);
		vScaleBarTA.GetTextProperty().SetFontSize(fontSize);
	}

}
