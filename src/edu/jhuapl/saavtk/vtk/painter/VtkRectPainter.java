package edu.jhuapl.saavtk.vtk.painter;

import java.awt.Color;
import java.util.Collection;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.vtk.Location;
import edu.jhuapl.saavtk.vtk.VtkResource;
import vtk.vtkActor2D;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper2D;
import vtk.vtkProp;
import vtk.vtkProperty2D;

/**
 * Painter used to render a simple 2 dimension rectangle into a VTK scene.
 *
 * @author lopeznr1
 */
public class VtkRectPainter implements VtkPropProvider, VtkResource
{
	// State vars
	private boolean isVisible;

	// VTK vars
	private vtkActor2D vBarA;
	private vtkPolyData vBarPD;

	/** Standard Constructor */
	public VtkRectPainter()
	{
		isVisible = true;

		vBarPD = new vtkPolyData();
		vtkPoints vTmpP = new vtkPoints();
		vtkCellArray vTmpCA = new vtkCellArray();
		vBarPD.SetPoints(vTmpP);
		vBarPD.SetPolys(vTmpCA);

		vTmpP.SetNumberOfPoints(4);

		vtkIdList vTmpIL = new vtkIdList();
		vTmpIL.SetNumberOfIds(4);
		for (int i = 0; i < 4; ++i)
			vTmpIL.SetId(i, i);
		vTmpCA.InsertNextCell(vTmpIL);

		vtkPolyDataMapper2D vTmpPDM = new vtkPolyDataMapper2D();
		vTmpPDM.SetInputData(vBarPD);

		vBarA = new vtkActor2D();
		vBarA.GetProperty().SetColor(1.0, 1.0, 1.0);
		vBarA.GetProperty().SetOpacity(0.5);
		vBarA.SetMapper(vTmpPDM);
	}

	/**
	 * Returns the painter's visibility.
	 */
	public boolean getIsVisible()
	{
		return isVisible;
	}

	/**
	 * Sets in the color of this bar.
	 */
	public void setColor(Color aColor)
	{
		vtkProperty2D vTmpP2D = vBarA.GetProperty();
		vTmpP2D.SetColor(aColor.getRed() / 255.0, aColor.getGreen() / 255.0, aColor.getBlue() / 255.0);
		vTmpP2D.SetOpacity(aColor.getAlpha() / 255.0);
		vTmpP2D.Modified();
	}

	/**
	 * Sets this painter's visibility.
	 */
	public void setIsVisible(boolean aBool)
	{
		// Bail if nothing has changed
		if (isVisible == aBool)
			return;
		isVisible = aBool;
	}

	/**
	 * Sets this painter's location
	 */
	public void setLocation(Location aLocation)
	{
		double posX = aLocation.getPosX();
		double posY = aLocation.getPosY();
		double dimX = aLocation.getDimX();
		double dimY = aLocation.getDimY();

		vtkPoints vTmpP = vBarPD.GetPoints();
		vTmpP.SetPoint(0, posX, posY, 0.0);
		vTmpP.SetPoint(1, posX + dimX, posY, 0.0);
		vTmpP.SetPoint(2, posX + dimX, posY + dimY, 0.0);
		vTmpP.SetPoint(3, posX, posY + dimY, 0.0);
		vBarA.Modified();
	}

	@Override
	public Collection<vtkProp> getProps()
	{
		// Bail if not visible
		if (isVisible == false)
			return ImmutableList.of();

		return ImmutableList.of(vBarA);
	}

	@Override
	public void vtkDispose()
	{
		vBarA.Delete();
	}

	@Override
	public void vtkUpdateState()
	{
		; // Nothing to do
	}

}
