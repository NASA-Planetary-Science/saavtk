package edu.jhuapl.saavtk.gui.render.camera;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import vtk.vtkCamera;
import vtk.vtkFloatArray;
import vtk.vtkRenderer;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Standard camera that is associated with the specified panel and is intended
 * to have a view relative to the specified PolyhedralModel.
 * <P>
 * By default the refPolyModel is assumed to be centered over the origin with a
 * standard coordinate system (X-axis: [1, 0, 0]; Y-axis: [0, 1, 0], Z-axis: [0,
 * 0, 1]). If this is not the case then the method {@link #setLockToModelCenter}
 * should be called so that the proper origin and corresponding coordinate
 * system can be computed.
 */
public class StandardCamera implements Camera
{
	// Ref vars
	private final vtkJoglPanelComponent refPanel;
	private final PolyhedralModel refPolyModel;

	// State vars
	private List<CameraListener> myListenerL;
	private boolean lockToModelCenter;

	// Logic vars
	private Vector3D logicFocusPt;
	private Vector3D logicAxisX;
	private Vector3D logicAxisY;
	private Vector3D logicAxisZ;

	// Cache vars
	private Vector3D cCenterVect;
	private Vector3D cNormalVect;

	/**
	 * Standard constructor
	 * 
	 * @param aPanel     The panel associated with this camera.
	 * @param aPolyModel The primary model associated with this panel. This camera
	 *                   can be configured to make this model the focus of the
	 *                   camera via the method {@link #setLockToModelCenter}
	 */
	public StandardCamera(vtkJoglPanelComponent aPanel, PolyhedralModel aPolyModel)
	{
		refPanel = aPanel;
		refPolyModel = aPolyModel;

		myListenerL = new ArrayList<>();
		lockToModelCenter = false;

		logicFocusPt = Vector3D.ZERO;
		logicAxisX = Vector3D.PLUS_I;
		logicAxisY = Vector3D.PLUS_J;
		logicAxisZ = Vector3D.PLUS_K;

		cCenterVect = null;
		cNormalVect = null;
	}

	/**
	 * Method which provides configuration of the camera so that the refPolyModel
	 * will be the focus. If the refPolyModel is the focus then the camera's logical
	 * coordinate system will be oriented relative to the model.
	 * <P>
	 * TODO: Consider making this part of the interface or only allowing
	 * configuration at construction time.
	 * 
	 * @param aBool Defines if the camera will use refPolyModel as the focus rather
	 *              than the origin and a standard coordinate system.
	 */
	public void setLockToModelCenter(boolean aBool)
	{
		lockToModelCenter = aBool;

		// Time to update the logical vars
		configureLogicAxis();
	}

	@Override
	public void addListener(CameraListener listener)
	{
		myListenerL.add(listener);
	}

	@Override
	public void removeListener(CameraListener listener)
	{
		myListenerL.remove(listener);
	}

	@Override
	public Vector3D getPosition()
	{
		vtkCamera vCamera = getVtkCamera();
		return new Vector3D(vCamera.GetPosition());
	}

	@Override
	public Vector3D getFocalPoint()
	{
		vtkCamera vCamera = getVtkCamera();
		return new Vector3D(vCamera.GetFocalPoint());
	}

	@Override
	public Vector3D getLookUnit()
	{
		return getFocalPoint().subtract(getPosition()).normalize();
	}

	@Override
	public Vector3D getRightUnit()
	{
		return getLookUnit().crossProduct(getUpUnit()).normalize();
	}

	@Override
	public Vector3D getUpUnit()
	{
		vtkCamera vCamera = getVtkCamera();
		return new Vector3D(vCamera.GetViewUp()).normalize();
	}

	@Override
	public void setPosition(Vector3D point)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.SetPosition(point.toArray());
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void setFocalPoint(Vector3D point)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.SetFocalPoint(point.toArray());
		vCamera.OrthogonalizeViewUp();
	}

	@Override
	public void setLookUnit(Vector3D look)
	{
		double dist = getFocalPoint().subtract(getPosition()).getNorm();
		setFocalPoint(getPosition().add(look.normalize().scalarMultiply(dist)));

		vtkCamera vCamera = getVtkCamera();
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void setUpUnit(Vector3D up)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.SetViewUp(up.toArray());
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void dolly(double distance)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Dolly(distance);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void pan(double dx, double dy)
	{
		Vector3D deltaVec = getRightUnit().scalarMultiply(dx).add(getUpUnit().scalarMultiply(dy));
		setPosition(getPosition().add(deltaVec));
		setFocalPoint(getFocalPoint().add(deltaVec));
		fireCameraEvent();
	}

	@Override
	public void zoom(double factor)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Zoom(factor);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void roll(double angleDeg)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Roll(angleDeg);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void pitch(double angleDeg)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Pitch(angleDeg);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void yaw(double angleDeg)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Yaw(angleDeg);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public Vector3D getLogicalAxis(AxisType aAxisType)
	{
		switch (aAxisType)
		{
			case NONE:
				return Vector3D.ZERO;

			case NEGATIVE_X:
				return logicAxisX.negate();
			case NEGATIVE_Y:
				return logicAxisY.negate();
			case NEGATIVE_Z:
				return logicAxisZ.negate();

			case POSITIVE_X:
				return logicAxisX;
			case POSITIVE_Y:
				return logicAxisY;
			case POSITIVE_Z:
				return logicAxisZ;

			default:
				throw new RuntimeException("Unrecognized AxisType: " + aAxisType);
		}
	}

	@Override
	public void reset()
	{
//		setOrientationInDirectionOfAxis(AxisType.NEGATIVE_Z, true);
		setOrientationInDirectionOfAxis(AxisType.NEGATIVE_Z, false);
//		refRenderPanel.resetCamera();

		// Send out notification of the configuration change
		fireCameraEvent();
	}

	@Override
	public void setOrientationInDirectionOfAxis(AxisType aAxisType, boolean aPreserveCurrentDistance)
	{
		vtkRenderer tmpRenderer = refPanel.getRenderer();
		if (tmpRenderer.VisibleActorCount() == 0)
			return;

		refPanel.getVTKLock().lock();

		// Retrieve the bounding box of refPolyModel
		double[] bounds = refPolyModel.getBoundingBox().getBounds();
		double xSize = Math.abs(bounds[1] - bounds[0]);
		double ySize = Math.abs(bounds[3] - bounds[2]);
		double zSize = Math.abs(bounds[5] - bounds[4]);
		double maxSize = Math.max(Math.max(xSize, ySize), zSize);

		// Form local copies of vars of interest
		Vector3D focalVect = this.logicFocusPt;
		Vector3D xAxisVect = logicAxisX;
		Vector3D yAxisVect = logicAxisY;
		Vector3D zAxisVect = logicAxisZ;

		double cameraDistance = calcCameraDistance();

		Vector3D viewVect;
		Vector3D targVect;
		if (aAxisType == AxisType.NEGATIVE_X)
		{
			double xMag = xSize / Math.tan(Math.PI / 6.0) + 2.0 * maxSize;
			targVect = xAxisVect.scalarMultiply(xMag).add(focalVect);
			viewVect = zAxisVect;
		}
		else if (aAxisType == AxisType.POSITIVE_X)
		{
			double xMag = -xSize / Math.tan(Math.PI / 6.0) - 2.0 * maxSize;
			targVect = xAxisVect.scalarMultiply(xMag).add(focalVect);
			viewVect = zAxisVect;
		}
		else if (aAxisType == AxisType.NEGATIVE_Y)
		{
			double yMag = ySize / Math.tan(Math.PI / 6.0) + 2.0 * maxSize;
			targVect = yAxisVect.scalarMultiply(yMag).add(focalVect);
			viewVect = zAxisVect;
		}
		else if (aAxisType == AxisType.POSITIVE_Y)
		{
			double yMag = -ySize / Math.tan(Math.PI / 6.0) - 2.0 * maxSize;
			targVect = yAxisVect.scalarMultiply(yMag).add(focalVect);
			viewVect = zAxisVect;
		}
		else if (aAxisType == AxisType.NEGATIVE_Z)
		{
			double zMag = zSize / Math.tan(Math.PI / 6.0) + 2.0 * maxSize;
			targVect = zAxisVect.scalarMultiply(zMag).add(focalVect);
			viewVect = yAxisVect;
		}
		else if (aAxisType == AxisType.POSITIVE_Z)
		{
			double zMag = -zSize / Math.tan(Math.PI / 6.0) - 2.0 * maxSize;
			targVect = zAxisVect.scalarMultiply(zMag).add(focalVect);
			viewVect = yAxisVect;
		}
		else
		{
			throw new RuntimeException("Unsupported AxisType: " + aAxisType);
		}

		// Orient the camera to reflect the new configuration
		vtkCamera tmpVtkCamera = getVtkCamera();
		tmpVtkCamera.SetFocalPoint(focalVect.toArray());
		tmpVtkCamera.SetPosition(targVect.toArray());
		tmpVtkCamera.SetViewUp(viewVect.toArray());

		// Maintain prior "viewer's distance" if requested
		if (aPreserveCurrentDistance)
		{
			Vector3D dTargVect = targVect.subtract(focalVect);
			dTargVect = dTargVect.normalize();
			dTargVect = dTargVect.scalarMultiply(cameraDistance);
			dTargVect = dTargVect.add(focalVect);

			tmpVtkCamera.SetPosition(dTargVect.toArray());
		}

		refPanel.getVTKLock().unlock();

		refPanel.resetCameraClippingRange();

		// Send out notification of the configuration change
		fireCameraEvent();
	}

	/**
	 * Helper method to send out notification that the "camera" state has changed.
	 */
	protected void fireCameraEvent()
	{
		for (CameraListener aListener : myListenerL)
			aListener.handle(new CameraEvent(this));
	}

	/**
	 * Helper method to compute and return the camera distance. The camera distance
	 * is defined as the distance between the camera's location and the focal point.
	 */
	private double calcCameraDistance()
	{
		Vector3D posVect = getPosition();
		Vector3D focalVect = logicFocusPt;
		double retDist = posVect.distance(focalVect);

		return retDist;
	}

	/**
	 * Helper method to properly set up the (X, Y, and Z) logical axis.
	 */
	private void configureLogicAxis()
	{
		// Switch the logical vars to the defaults if appropriate
		if (lockToModelCenter == false)
		{
			logicFocusPt = Vector3D.ZERO;
			logicAxisX = Vector3D.PLUS_I;
			logicAxisY = Vector3D.PLUS_J;
			logicAxisZ = Vector3D.PLUS_K;
			return;
		}

		// Ensure that the cache vars are calculated
		initCacheVars();

		// Switch the logical vars to reflect the refPolyModel
		logicFocusPt = cCenterVect;

		// Bail if z-axis is aligned with the standard unit z-axis
		logicAxisZ = cNormalVect;
		if (logicAxisZ.getX() == 0 && logicAxisY.getY() == 0)
		{
			logicAxisX = Vector3D.PLUS_I;
			logicAxisY = Vector3D.PLUS_J;
			return;
		}

		// Calculate the 2nd arbitrary (orthogonal) axis
		// Solve the equation: (a * d) + (b * e) + (c * f) = 0
		//
		// Due to our check that the normal is not alignment with
		// the (unit) z-axis we can be guaranteed that both getX()
		// and getY() will not equal zero (at the same time)!
		//
		// Source: https://en.wikipedia.org/wiki/Cross_product
		// Source: https://stackoverflow.com/questions/3049509
		double d, e, f;
		if (logicAxisZ.getY() != 0)
		{
			// Let d = 0.50; f = 0.00; and solve for e
			d = 0.50;
			f = 0.00;
			e = -(logicAxisZ.getX() * d) / logicAxisZ.getY();
		}
		else
		{
			// Let e = 0.00; f = 0.50; and solve for d
			e = 0.00;
			f = 0.50;
			d = -(logicAxisZ.getZ() * f) / logicAxisZ.getX();
		}
		logicAxisY = new Vector3D(d, e, f).normalize();

		// Calculate the 3rd (orthogonal) axis
		logicAxisX = logicAxisZ.crossProduct(logicAxisY);
		logicAxisX = logicAxisX.normalize();
	}

	/**
	 * Helper method that returns the associated VTkCamera
	 */
	private vtkCamera getVtkCamera()
	{
		return refPanel.getActiveCamera();
	}

	/**
	 * Helper method to initialize computationally expensive variables.
	 * <P>
	 * These computations will be cached for performance reasons.
	 */
	private void initCacheVars()
	{
		// Bail if the cache vars have been initialized
		boolean isInit = true;
		isInit &= cCenterVect != null;
		isInit &= cNormalVect != null;
		if (isInit == true)
			return;

		// Debug
		if (isDebug() == true)
			System.err.println("initCacheVars -> bounds: " + refPolyModel.getBoundingBox());

		// Retrieve the geometric center of the refPolyModel
		double[] centerPt = refPolyModel.getSmallBodyPolyData().GetCenter();

		// Locate a point on the surface closest to the geometric center of the model
		centerPt = refPolyModel.findClosestPoint(centerPt);
		cCenterVect = new Vector3D(centerPt);

		// Overly simplistic computation of normal
		// Locate the normal at the located centerPt
//		double[] normalPt = refPolyModel.getClosestNormal(centerPt);
		double[] normalPt = refPolyModel.getNormalAtPoint(centerPt);
		cNormalVect = new Vector3D(normalPt);

		// Debug
		if (isDebug() == true)
			System.err.println("   centerPt: " + cCenterVect + "  norm: " + cNormalVect);

		// Cast to a GenericPolyhedralModel
		// TODO: Should the method getCellNormals be part of PolyhedralModel interface?
		GenericPolyhedralModel tmpPolyModel = (GenericPolyhedralModel) refPolyModel;

		// Calculate the average normal vector (composed of all cells)
		vtkFloatArray tmpVFA = tmpPolyModel.getCellNormals();
		int numNorms = tmpVFA.GetNumberOfTuples();
		double sumX = 0.0, sumY = 0.0, sumZ = 0.0;
		for (int c1 = 0; c1 < numNorms; c1++)
		{
			double[] tmp = tmpVFA.GetTuple3(c1);
			sumX += tmp[0];
			sumY += tmp[1];
			sumZ += tmp[2];
		}
		cNormalVect = new Vector3D(sumX / numNorms, sumY / numNorms, sumZ / numNorms);

		// Normalize the average normal vector
		if (cNormalVect.equals(Vector3D.ZERO) == true)
			cNormalVect = Vector3D.PLUS_K;
		else
			cNormalVect = cNormalVect.normalize();

		// Debug
		if (isDebug() == true)
			System.err.println("   centerPt: " + cCenterVect + "  norm: " + cNormalVect + " numNorms: " + numNorms);
	}

	/**
	 * Helper method used for debugging
	 */
	private boolean isDebug()
	{
		return false;
	}

}
