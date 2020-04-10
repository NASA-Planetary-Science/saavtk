package edu.jhuapl.saavtk.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.text.DecimalFormat;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.DefaultPicker;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.status.StatusProvider;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import vtk.vtkCamera;
import vtk.vtkProp;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * PickListener that is responsible for updating the status bar whenever
 * something has been picked.
 * <P>
 * A portion of the logic in this class originated from the file (prior to
 * 2019Oct28): edu.jhuapl.saavtk.pick.DefaultPicker
 * 
 * @author lopeznr1
 */
public class StatusBarDefaultPickHandler implements PickListener
{
	// Ref vars
	private final DefaultPicker refDefaultPicker;
	private final StatusBar refStatusBar;
	private final vtkJoglPanelComponent refRenWin;
	private final ModelManager refModelManager;

	// Formatters
	private final DecimalFormat decimalFormatter;

	// State vars
	private String distanceStr;

	// Cache vars
	Vector3D cSurfacePos;

	/**
	 * Standard Constructor
	 * 
	 * @param aStatusBar
	 */
	public StatusBarDefaultPickHandler(DefaultPicker aDefaultPicker, StatusBar aStatusBar, Renderer aRenderer,
			ModelManager aModelManager)
	{
		refDefaultPicker = aDefaultPicker;
		refStatusBar = aStatusBar;
		refRenWin = aRenderer.getRenderWindowPanel();
		refModelManager = aModelManager;

		decimalFormatter = new DecimalFormat("##0.000");

		distanceStr = null;

		cSurfacePos = null;

		// We need to update the scale bar whenever there is a render or whenever
		// the window gets resized. Although resizing a window results in a render,
		// we still need to listen to a resize event since only listening to render
		// results in the scale bar not being positioned correctly when during the
		// resize for some reason. Thus we need to register a component
		// listener on the renderer panel as well to listen explicitly to resize events.
		// Note also that this functionality is in this class since picking is required
		// to compute the value of the scale bar.
		refRenWin.getRenderWindow().AddObserver("EndEvent", this, "updateScaleBarValue");
		refRenWin.getComponent().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent aEvent)
			{
				updateScaleBarValue();
				updateScaleBarPosition();
			}
		});
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Just update the status bar if not a primary action
		if (aMode != PickMode.ActivePri)
		{
			cSurfacePos = aSurfaceTarg.getPosition();
			showPositionInfoInStatusBar(cSurfacePos);
			return;
		}

		// Retrieve relevant primary target attributes
		vtkProp priActor = aPrimaryTarg.getActor();
		int priCellId = aPrimaryTarg.getCellId();

		// Just update the status bar's left size with status if
		// aPrimaryTarg is associated with a StatusProvider
		if (priActor instanceof SaavtkLODActor)
		{
			StatusProvider tmpSP = ((SaavtkLODActor) priActor).getAssocModel(StatusProvider.class);
			if (tmpSP != null)
			{
				String tmpStr = tmpSP.getDisplayInfo(priCellId);
				refStatusBar.setLeftText(tmpStr);
				return;
			}
		}

		// Retrieve the model associated with the actor
		Model tmpModel = refModelManager.getModel(priActor);

		// Otherwise, update the left side
		if (tmpModel == null)
			refStatusBar.setLeftText(" ");
		else
			refStatusBar.setLeftTextSource(tmpModel, priActor, priCellId, aPrimaryTarg.getPosition().toArray());
	}

	// TODO: Add Comments, needs to be reworked
	private void showPositionInfoInStatusBar(Vector3D aSurfacePos)
	{
		if (refRenWin.getRenderWindow().GetNeverRendered() > 0)
			return;

		vtkCamera activeCamera = refRenWin.getRenderer().GetActiveCamera();
		double[] cameraPos = activeCamera.GetPosition();
		double distance = Math
				.sqrt(cameraPos[0] * cameraPos[0] + cameraPos[1] * cameraPos[1] + cameraPos[2] * cameraPos[2]);
		distanceStr = decimalFormatter.format(distance);
		if (distanceStr.length() == 5)
			distanceStr = "  " + distanceStr;
		else if (distanceStr.length() == 6)
			distanceStr = " " + distanceStr;
		distanceStr += " km";

		updateStatusBar(refModelManager.getPolyhedralModel().getScaleBarWidthInKm(), distanceStr, aSurfacePos);
	}

	// TODO: Add Comments, needs to be reworked
	private void updateScaleBarPosition()
	{
		PolyhedralModel smallBodyModel = refModelManager.getPolyhedralModel();
		smallBodyModel.updateScaleBarPosition(refRenWin.getComponent().getWidth(), refRenWin.getComponent().getHeight());
	}

	// TODO: Add Comments, needs to be reworked
	private void updateScaleBarValue()
	{
		// if the body doesn't intercept all corners of the renderer, just go ahead and
		// return, leaving the scale bar value untouched
		double sizeOfPixel = refDefaultPicker.computeSizeOfPixel();
		if (sizeOfPixel == -1)
			return;

		PolyhedralModel smallBodyModel = refModelManager.getPolyhedralModel();
		smallBodyModel.updateScaleBarValue(sizeOfPixel, new Runnable() {

			@Override
			public void run()
			{
				updateStatusBar(refModelManager.getPolyhedralModel().getScaleBarWidthInKm(), distanceStr, cSurfacePos);
			}
		});
	}

	// TODO: Add Comments, needs to be reworked
	private void updateStatusBar(double scaleBarWidthInKm, String distanceStr, Vector3D aPosition)
	{
		DecimalFormat decimalFormatter = new DecimalFormat("##0.000");
		DecimalFormat decimalFormatter2 = new DecimalFormat("#0.000");

		if (distanceStr == null)
		{
			vtkCamera activeCamera = refRenWin.getRenderer().GetActiveCamera();
			double[] cameraPos = activeCamera.GetPosition();
			double distance = Math
					.sqrt(cameraPos[0] * cameraPos[0] + cameraPos[1] * cameraPos[1] + cameraPos[2] * cameraPos[2]);
			distanceStr = decimalFormatter.format(distance);
			if (distanceStr.length() == 5)
				distanceStr = "  " + distanceStr;
			else if (distanceStr.length() == 6)
				distanceStr = " " + distanceStr;
			distanceStr += " km";
		}

		String pixelResolutionString = "";
		if (refModelManager.getPolyhedralModel().getScaleBarWidthInKm() > 0)
		{
			if (refModelManager.getPolyhedralModel().getScaleBarWidthInKm() < 1.0)
				pixelResolutionString = String.format("%.2f m",
						1000.0 * refModelManager.getPolyhedralModel().getScaleBarWidthInKm());
			else
				pixelResolutionString = String.format("%.2f km",
						refModelManager.getPolyhedralModel().getScaleBarWidthInKm());
		}

		if (aPosition != null)
		{
			double[] pos = aPosition.toArray();
			LatLon llr = MathUtil.reclat(pos);

			// Note \u00B0 is the unicode degree symbol

			// double sign = 1.0;
			double lat = llr.lat * 180 / Math.PI;
			// if (lat < 0.0)
			// sign = -1.0;
			String latStr = decimalFormatter.format(lat);
			if (latStr.length() == 5)
				latStr = "  " + latStr;
			else if (latStr.length() == 6)
				latStr = " " + latStr;
			// if (lat >= 0.0)
			// latStr += "\u00B0N";
			// else
			// latStr += "\u00B0S";
			latStr += "\u00B0";

			// Note that the convention seems to be that longitude
			// is never negative and is shown as E. longitude.
			double lon = llr.lon * 180 / Math.PI;
			if (lon < 0.0)
				lon += 360.0;
			String lonStr = decimalFormatter.format(lon);
			if (lonStr.length() == 5)
				lonStr = "  " + lonStr;
			else if (lonStr.length() == 6)
				lonStr = " " + lonStr;
			// if (lon >= 0.0)
			// lonStr += "\u00B0E";
			// else
			// lonStr += "\u00B0W";
			lonStr += "\u00B0";

			double rad = llr.rad;
			String radStr = decimalFormatter2.format(rad);
			if (radStr.length() == 5)
				radStr = " " + radStr;
			radStr += " km";

			if (pixelResolutionString.equals(""))
				refStatusBar.setRightText(
						"Lat: " + latStr + "  Lon: " + lonStr + "  Radius: " + radStr + "  Range: " + distanceStr + " ");
			else
				refStatusBar.setRightText("Lat: " + latStr + "  Lon: " + lonStr + "  Radius: " + radStr + "  Range: "
						+ distanceStr + " " + " " + "Scalebar: " + pixelResolutionString);
		}
		else
		{
			if (pixelResolutionString.equals(""))
				refStatusBar.setRightText("Range: " + distanceStr + " ");
			else
				refStatusBar.setRightText("Range: " + distanceStr + " " + "Scalebar: " + pixelResolutionString);
		}
	}

}
