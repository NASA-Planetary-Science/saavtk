package edu.jhuapl.saavtk.gui.renderer;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkCamera;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkInteractorStyleUser;
import vtk.vtkRenderWindowInteractor;

public class CustomInteractorStyle extends vtkInteractorStyleTrackballCamera
{
	CartesianAxis	rotationConstraint	= CartesianAxis.NONE;
	Vector3D		rotationOrigin		= Vector3D.ZERO;
	double			motionFactor		= 1.0;												// from vtkInteractorStyleTrackballCamera.cxx

	public CustomInteractorStyle(vtkRenderWindowInteractor interactor)
	{
		this.SetInteractor(interactor);
		interactor.SetInteractorStyle(this);

		this.AddObserver(VtkInteractorEvent.MouseMoveEvent.name(), this, "OnMouseMove");
		this.AddObserver(VtkInteractorEvent.LeftButtonPressEvent.name(), this, "OnLeftButtonDown");
	}

	public void setRotationConstraint(CartesianAxis rotationConstraint)
	{
		this.rotationConstraint = rotationConstraint;
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
		if (rotationConstraint == CartesianAxis.NONE)
			super.OnMouseMove();
		else
		{
			if (GetState() == VtkInteractorStyleMotionFlags.VTKIS_ROTATE.getVtkCode())
			{
				// from vtkInteractorStyleTrackballCamera.cxx
				int dx = GetInteractor().GetEventPosition()[0] - GetInteractor().GetLastEventPosition()[0];
				//	  int dy = GetInteractor().GetEventPosition()[1] - GetInteractor().GetLastEventPosition()[1];

				int[] size = GetCurrentRenderer().GetRenderWindow().GetSize();

				//  double delta_elevation = -20.0 / size[1];
				double delta_azimuth = -20.0 / size[0];

				double rxf = dx * delta_azimuth * motionFactor;
				//		double ryf = dy * delta_elevation * motionFactor;

				vtkCamera activeCamera = GetCurrentRenderer().GetActiveCamera();
				Vector3D pos = new Vector3D(activeCamera.GetPosition());
				Rotation rot = new Rotation(getRotationConstraintAxis(), rxf);
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
			else
			{
				super.OnMouseMove();
			}
		}
	}

	protected Vector3D getRotationConstraintAxis()
	{
		switch (rotationConstraint)
		{
		case X:
			return Vector3D.PLUS_I;
		case Y:
			return  Vector3D.PLUS_J;
		case Z:
			return Vector3D.PLUS_K;
		}
		return null;
	}

	protected Vector3D getRotationOrigin()
	{
		return rotationOrigin;
	}
}
