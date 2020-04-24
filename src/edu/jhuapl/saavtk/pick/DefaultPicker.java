package edu.jhuapl.saavtk.pick;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * This class is provided to support legacy support for the former "default
 * picker" (via {@link PropertyChangeListener}) notification mechanism. This
 * class will eventually be removed so new code should be developed via the
 * {@link #addListener(PickListener)} instead.
 * <P>
 * The method {@link #computeSizeOfPixel()} is included in this class, but this
 * functionality should be moved elsewhere as it is not related specifically to
 * the picker.
 * 
 * @author lopeznr1
 */
public class DefaultPicker extends BaseDefaultPicker implements PickListener, PropertyChangeListener
{
	// Ref vars
	private final vtkJoglPanelComponent refRenWin;

	// State vars
	private final PropertyChangeSupport pcs;

	/**
	 * Standard Constructor
	 */
	public DefaultPicker(Renderer aRenderer, ModelManager aModelManager)
	{
		super(aRenderer, aModelManager);

		refRenWin = aRenderer.getRenderWindowPanel();

		pcs = new PropertyChangeSupport(this);

		// Register ourself as a listener so we can propagate relevant events
		// to the registered PropertyChangeListeners
		addListener(this);
	}

	/**
	 * Notification of default pick events via this method are deprecated.
	 * <P>
	 * Please use {@link #addListener(PickListener)} instead.
	 */
	@Deprecated
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Notification of default pick events via this method are deprecated.
	 * <P>
	 * Please use {@link #delListener(PickListener)} instead.
	 */
	@Deprecated
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail since only MouseEvents are propagated (via the former design)
		if (aEvent instanceof MouseEvent == false)
			return;

		// Bail since only primary actions are propagated (via the former design)
		if (aMode != PickMode.ActivePri)
			return;

		// Bail if no valid target position
		Vector3D targetPos = aPrimaryTarg.getPosition();
		if (targetPos == null)
			return;

		pcs.firePropertyChange(Properties.MODEL_PICKED, null,
				new PickEvent((MouseEvent) aEvent, aPrimaryTarg.getActor(), aPrimaryTarg.getCellId(), targetPos.toArray()));
	}

	/**
	 * Computes the size of a pixel in body fixed coordinates. This is only
	 * meaningful when the user is zoomed in a lot. To compute a result all 4
	 * corners of the view window must intersect the asteroid.
	 *
	 * @return
	 */
	public double computeSizeOfPixel()
	{
		// Do a pick at each of the 4 corners of the renderer
		long currentTime = System.currentTimeMillis();
		int width = refRenWin.getComponent().getWidth();
		int height = refRenWin.getComponent().getHeight();

		vtkCellPicker vSmallBodyCP = getSmallBodyPicker();
		int[][] corners = { { 0, 0 }, { width - 1, 0 }, { width - 1, height - 1 }, { 0, height - 1 } };
		double[][] points = new double[4][3];
		for (int i = 0; i < 4; ++i)
		{
			int pickSucceeded = doPick(currentTime, corners[i][0], corners[i][1], vSmallBodyCP, refRenWin);
			if (pickSucceeded == 1)
			{
				points[i] = vSmallBodyCP.GetPickPosition();
			}
			else
			{
				return -1.0;
			}
		}

		// Compute the scale if all 4 points intersect by averaging the distance of all
		// 4 sides
		double bottom = MathUtil.distanceBetweenFast(points[0], points[1]);
		double right = MathUtil.distanceBetweenFast(points[1], points[2]);
		double top = MathUtil.distanceBetweenFast(points[2], points[3]);
		double left = MathUtil.distanceBetweenFast(points[3], points[0]);

		double sizeOfPixel = (bottom / (width - 1) + right / (height - 1) + top / (width - 1) + left / (height - 1))
				/ 4.0;

		return sizeOfPixel;
	}

}
