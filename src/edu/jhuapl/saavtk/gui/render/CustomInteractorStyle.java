package edu.jhuapl.saavtk.gui.render;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkCamera;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkRenderWindowInteractor;

public class CustomInteractorStyle extends vtkInteractorStyleTrackballCamera
{
	// State vars
	private Vector3D rotationConstraintAxis;
	private Vector3D rotationOrigin;
	private double motionFactor;

	public CustomInteractorStyle(vtkRenderWindowInteractor aInteractor)
	{
		rotationConstraintAxis = Vector3D.ZERO;
		rotationOrigin = Vector3D.ZERO;
		motionFactor = 1.0; // from vtkInteractorStyleTrackballCamera.cxx

		SetInteractor(aInteractor);
		aInteractor.SetInteractorStyle(this);

		AddObserver(VtkInteractorEvent.MouseMoveEvent.name(), this, "OnMouseMove");
		AddObserver(VtkInteractorEvent.LeftButtonPressEvent.name(), this, "OnLeftButtonDown");
	}

	/**
	 * Sets the axis and origin for which rotational motion will be constrained to.
	 * 
	 * @param aAxis
	 * @param aOrigin
	 */
	public void setRotationConstraint(Vector3D aAxis, Vector3D aOrigin)
	{
		// Transform non unit vectors into unit vectors
		if (aAxis.equals(Vector3D.ZERO) == false)
			aAxis = aAxis.normalize();
		else
			aAxis = Vector3D.ZERO;

		rotationConstraintAxis = aAxis;
		rotationOrigin = aOrigin;
	}

	@Override
	public void OnLeftButtonDown()
	{
		super.OnLeftButtonDown();
		vtkCamera activeCamera = GetCurrentRenderer().GetActiveCamera();
		rotationOrigin = new Vector3D(activeCamera.GetFocalPoint());
	}

	@Override
	public void OnMouseMove()
	{
		// Delegate to VTK if no valid constraint axis specified
		if (rotationConstraintAxis.equals(Vector3D.ZERO))
		{
			super.OnMouseMove();
			return;
		}

		// Restrict rotation to the rotationConstraintAxis
		if (GetState() == VtkInteractorStyleMotionFlags.VTKIS_ROTATE.getVtkCode())
		{
			// from vtkInteractorStyleTrackballCamera.cxx
			int dx = GetInteractor().GetEventPosition()[0] - GetInteractor().GetLastEventPosition()[0];
			// int dy = GetInteractor().GetEventPosition()[1] - GetInteractor().GetLastEventPosition()[1];

			int[] size = GetCurrentRenderer().GetRenderWindow().GetSize();

			// double delta_elevation = -20.0 / size[1];
			double delta_azimuth = -20.0 / size[0];

			double rxf = dx * delta_azimuth * motionFactor;
			// double ryf = dy * delta_elevation * motionFactor;

			vtkCamera activeCamera = GetCurrentRenderer().GetActiveCamera();
			Vector3D pos = new Vector3D(activeCamera.GetPosition());
			Rotation rot = new Rotation(rotationConstraintAxis, rxf);
			Vector3D newPos = rot.applyTo(pos.subtract(rotationOrigin)).add(rotationOrigin);
			activeCamera.SetPosition(newPos.toArray());
			activeCamera.SetViewUp(rot.applyTo(new Vector3D(activeCamera.GetViewUp())).toArray());
			activeCamera.OrthogonalizeViewUp();

			if (GetAutoAdjustCameraClippingRange() == 1)
			{
				GetCurrentRenderer().ResetCameraClippingRange();
			}

			if (GetInteractor().GetLightFollowCamera() == 1)
			{
				GetCurrentRenderer().UpdateLightsGeometryToFollowCamera();
			}

			GetInteractor().Render();
		}
		// Delegate to VTK
		else
		{
			super.OnMouseMove();
		}
	}

}
