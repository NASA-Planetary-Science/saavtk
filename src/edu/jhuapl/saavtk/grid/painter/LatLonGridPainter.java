package edu.jhuapl.saavtk.grid.painter;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.grid.GridAttr;
import edu.jhuapl.saavtk.grid.LatLonSpacing;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.vtk.VtkFontUtil;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import vtk.vtkActor;
import vtk.vtkCaptionActor2D;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;

/**
 * Painter used to render a LatLon grid into a VTK scene.
 *
 * @author lopeznr1
 */
public class LatLonGridPainter implements PropertyChangeListener, VtkPropProvider
{
	// Constants
	private final FontAttr DefaultFontAttr = new FontAttr(FontAttr.DefaultFace, Color.WHITE, 12, false);

	// Reference vars
	private final Renderer refRenderer;
	private final PolyhedralModel refSmallBody;

	// State vars
	private List<GridChangeListener> listenerL;
	private LatLonSpacing mainLLS;
	private GridAttr mainGA;
	private FontAttr labelFA;
	private boolean isStale;
	private double prevShiftAmt;

	// VTK vars
	private final vtkActor vActor;
	private final vtkPolyDataMapper vMapper;
	private final vtkPolyData vGridPD;
	private final List<vtkCaptionActor2D> vLabelActorL;

	/** Standard Constructor */
	public LatLonGridPainter(Renderer aRenderer, PolyhedralModel aSmallBody)
	{
		refRenderer = aRenderer;
		refSmallBody = aSmallBody;

		listenerL = new ArrayList<>();

		vActor = new vtkActor();
		vMapper = new vtkPolyDataMapper();
		vGridPD = new vtkPolyData();
		vLabelActorL = new ArrayList<>();

		mainLLS = LatLonSpacing.Default;
		mainGA = GridAttr.Default;
		labelFA = DefaultFontAttr;
		isStale = true;
		prevShiftAmt = 0;

		// Register for events of interest
		refSmallBody.addPropertyChangeListener(this);
	}

	/**
	 * Registers a {@link GridChangeListener} with this painter.
	 */
	public void addListener(GridChangeListener aListener)
	{
		listenerL.add(aListener);
	}

	/**
	 * Deregisters a {@link GridChangeListener} with this painter.
	 */
	public void delListener(GridChangeListener aListener)
	{
		listenerL.remove(aListener);
	}

	/**
	 * Gets the {@link FontAttr} utilized for labels.
	 */
	public FontAttr getFontAttr()
	{
		return labelFA;
	}

	/**
	 * Gets the {@link GridAttr} associated with the actual grid.
	 */
	public GridAttr getGridAttr()
	{
		return mainGA;
	}

	/**
	 * Gets the {@link LatLonSpacing} associated with the actual grid.
	 */
	public LatLonSpacing getLatLonSpacing()
	{
		return mainLLS;
	}

	/**
	 * Returns true if the VTK vars are out of date and need to be rerendered.
	 */
	public boolean isStale()
	{
		return isStale;
	}

	/**
	 * Sets in the {@link FontAttr} to be utilized for labels.
	 */
	public void setFontAttr(FontAttr aLabelFA)
	{
		// Bail if nothing has changed
		if (labelFA.equals(aLabelFA) == true)
			return;

		labelFA = aLabelFA;
		for (var aActor : vLabelActorL)
			VtkFontUtil.setFontAttr(aActor.GetTextActor().GetTextProperty(), labelFA);

		refRenderer.notifySceneChange();
		notifyListeners(GridChangeType.Label);
	}

	/**
	 * Sets in the {@link GridAttr} associated with the actual grid.
	 */
	public void setGridAttr(GridAttr aMainGA)
	{
		// Bail if nothing has changed
		if (mainGA.equals(aMainGA) == true)
			return;
		mainGA = aMainGA;

		// Update state
		updateVtkState();

		refRenderer.notifySceneChange();
		notifyListeners(GridChangeType.Grid);
	}

	/**
	 * Sets in the {@link LatLonSpacing} associated with the actual grid.
	 */
	public void setLatLonSpacing(LatLonSpacing aMainLLS)
	{
		// Bail if nothing has changed
		if (mainLLS.equals(aMainLLS) == true)
			return;
		mainLLS = aMainLLS;

		// Update state
		isStale = true;
		updateVtkState();

		refRenderer.notifySceneChange();
		notifyListeners(GridChangeType.Label);
	}

	/**
	 * Sets in the {@link LatLonSpacing} associated with the actual grid.
	 */
	public void setGridAttr(LatLonSpacing aMainLLS)
	{
		// Bail if nothing has changed
		if (mainLLS.equals(aMainLLS) == true)
			return;
		mainLLS = aMainLLS;

		// A full regeneration of the grid will be necessary
		isStale = true;

		// Update state
		updateVtkState();

		refRenderer.notifySceneChange();
		notifyListeners(GridChangeType.Grid);
	}

	@Override
	public List<vtkProp> getProps()
	{
		if (mainGA.isVisible() == false)
			return ImmutableList.of();

		var retPropL = new ArrayList<vtkProp>();
		retPropL.add(vActor);
		if (labelFA.getIsVisible() == true)
			retPropL.addAll(vLabelActorL);

		return retPropL;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		// Respond only to changes in resolution
		if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()) == false && (Properties.MODEL_POSITION_CHANGED.equals(evt.getPropertyName()) == false))
			return;

		isStale = true;
		updateVtkState();
		refRenderer.notifySceneChange();
	}

	/**
	 * Helper method to send out notification of configuration change to listeners.
	 */
	private void notifyListeners(GridChangeType aType)
	{
		for (var aListener : listenerL)
			aListener.handleGridChanged(this, aType);
	}

	/**
	 * Helper method to update the painter's VTK state.
	 */
	private void updateVtkState()
	{
		// Nothing to do if not shown
		if (mainGA.isVisible() == false)
			return;

		// Apply the line width
		vActor.GetProperty().SetLineWidth((float)mainGA.lineWidth());
		VtkUtil.setColorOnProperty(vActor.GetProperty(), mainGA.mainColor());

		// Generate the grid only if we are stale
		if (isStale == true)
		{
			var latSpacing = mainLLS.latSpacing();
			var lonSpacing = mainLLS.lonSpacing();
			VtkGridUtil.generateLatLonGrid(refSmallBody, vGridPD, vLabelActorL, latSpacing, lonSpacing);

			vMapper.ScalarVisibilityOff();
			vMapper.SetInputData(vGridPD);

			for (var aActor : vLabelActorL)
				VtkFontUtil.setFontAttr(aActor.GetTextActor().GetTextProperty(), labelFA);

			isStale = false;
			prevShiftAmt = 0.0;
		}

		// Apply the shiftFactor
		var nextShiftAmt = mainGA.shiftFactor() * refSmallBody.getMinShiftAmount();
		var deltaShiftAmt = nextShiftAmt - prevShiftAmt;
		if (deltaShiftAmt != 0)
		{
			refSmallBody.shiftPolyLineInNormalDirection(vGridPD, deltaShiftAmt);
			prevShiftAmt = nextShiftAmt;
		}

		vMapper.Update();
		vActor.SetMapper(vMapper);
	}

}
