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
import edu.jhuapl.saavtk.vtk.Location;
import edu.jhuapl.saavtk.vtk.VtkResource;
import edu.jhuapl.saavtk.vtk.painter.VtkRectPainter;
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
	private double dimX, dimY;
	private NumberFormat dispNF;

	// VTK vars
	private VtkRectPainter vScaleBarRP;
	private vtkTextActor vScaleBarTA;

	/** Standard Constructor */
	public ScaleBarPainter(Renderer aRenderer)
	{
		refRenderer = aRenderer;

		isVisible = true;
		dimX = 150;
		dimY = 16;
		dispNF = new DecimalFormat("0.00");

		vScaleBarRP = new VtkRectPainter();

		vScaleBarTA = new vtkTextActor();
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
		return dimX;
	}

	/**
	 * Returns the scale bar's height in pixels.
	 */
	public double getSizeY()
	{
		return dimY;
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
		isChanged |= Double.compare(dimX, aSizeX) != 0;
		isChanged |= Double.compare(dimY, aSizeY) != 0;
		if (isChanged == false)
			return;

		dimX = aSizeX;
		dimY = aSizeY;

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
		retL.addAll(vScaleBarRP.getProps());
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
		vScaleBarRP.vtkDispose();
		vScaleBarTA.Delete();
	}

	@Override
	public void vtkUpdateState()
	{
		updatePosition();
		updateInfoMsg();

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
		double fullSpanKm = nominalPixelSpan * dimX;
		String tmpMsg = dispNF.format(fullSpanKm) + " km";
		if (fullSpanKm < 1.0)
			tmpMsg = dispNF.format(fullSpanKm * 1000.0) + " m";
		vScaleBarTA.SetInput(tmpMsg);
		vScaleBarTA.Modified();
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
		double x = compW - dimX - padAmt;
		double y = padAmt;

		// Update scale bar
		vScaleBarRP.setLocation(new Location(x, y, dimX, dimY));

		// Update scale bar text
		int fontSize = (int) (dimY - 4);
		vScaleBarTA.SetPosition(x + dimX / 2, y + 2);
		vScaleBarTA.GetTextProperty().SetFontSize(fontSize);
		vScaleBarTA.Modified();
	}

}
