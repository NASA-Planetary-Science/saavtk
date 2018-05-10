package edu.jhuapl.saavtk2.image.projection.geom;

import edu.jhuapl.saavtk.util.LinearSpace;
import edu.jhuapl.saavtk2.geom.BasicGeometry;
import edu.jhuapl.saavtk2.util.LatLon;
import edu.jhuapl.saavtk2.util.MathUtil;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkQuad;

public class LatLonGlobeGeometry extends BasicGeometry {

	public LatLonGlobeGeometry(double radius, int nlat, int nlon) {
		super(generatePolyDataRepresentation(radius, nlat, nlon));
		// TODO Auto-generated constructor stub
	}

	public static vtkPolyData generatePolyDataRepresentation(double radius, int nlat, int nlon) {
		double[] latVals = LinearSpace.create(-90, 90, nlat);
		double[] lonVals = LinearSpace.create(0, 360, nlon);

		vtkPoints points = new vtkPoints();
		int[][] ids = new int[nlon][nlat];
		for (int i = 0; i < nlon; i++) {
			for (int j = 0; j < nlat; j++) {
				int id = points.InsertNextPoint(
						MathUtil.latrec(new LatLon(Math.toRadians(latVals[j]), Math.toRadians(lonVals[i]), radius)));
				ids[i][j] = id;
			}
		}

		vtkCellArray polys = new vtkCellArray();
		for (int i = 0; i < nlon - 1; i++) {
			for (int j = 0; j < nlat - 1; j++) {

				vtkQuad quad = new vtkQuad();
				int id0 = ids[i][j];
				int id1 = ids[i][j + 1];
				int id2 = ids[i + 1][j];
				int id3 = ids[i + 1][j + 1];
				quad.GetPointIds().SetId(0, id0);
				quad.GetPointIds().SetId(1, id1);
				quad.GetPointIds().SetId(2, id3);
				quad.GetPointIds().SetId(3, id2);
				polys.InsertNextCell(quad);
			}
		}

		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetPolys(polys);
		return polyData;
	}

}
