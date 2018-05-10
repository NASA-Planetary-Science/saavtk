package edu.jhuapl.saavtk2.image.projection.geom;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.LinearSpace;
import edu.jhuapl.saavtk2.geom.BasicGeometry;
import edu.jhuapl.saavtk2.image.projection.CylindricalMapCoordinates;
import edu.jhuapl.saavtk2.image.projection.CylindricalProjection;
import edu.jhuapl.saavtk2.image.projection.depthfunc.ConstantDepthFunction;
import edu.jhuapl.saavtk2.image.projection.depthfunc.DepthFunction;
import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkDataArray;
import vtk.vtkFloatArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyLine;
import vtk.vtkVertex;

public class CylindricalProjectionGeometry extends BasicGeometry {

	public CylindricalProjectionGeometry(CylindricalProjection projection, double radius, int nDivLon, int nDivLat) {
		super(generatePolyDataRepresentation(projection, radius, nDivLon, nDivLat));
	}

	public CylindricalProjectionGeometry(CylindricalProjection projection, double radius) {
		super(generatePolyDataRepresentation(projection, radius));
	}

	public CylindricalProjectionGeometry(CylindricalProjection projection, double radius, vtkPoints pointsToProject) {
		super(generatePolyDataRepresentation(projection, radius, pointsToProject));
	}

	public CylindricalProjectionGeometry(CylindricalProjection projection, double radius, vtkPolyData polyDataToProject) {
		super(generatePolyDataRepresentation(projection, radius, polyDataToProject));
	}

	public static vtkPolyData generatePolyDataRepresentation(CylindricalProjection projection, double rayDepth, int nDivLon, int nDivLat) {
		vtkPoints points = new vtkPoints();
		int oid = points.InsertNextPoint(Vector3D.ZERO.toArray());
		int ulid = points.InsertNextPoint(
				projection.getUpperLeftUnit().scalarMultiply(rayDepth).add(projection.getRayOrigin()).toArray());
		int urid = points.InsertNextPoint(
				projection.getUpperRightUnit().scalarMultiply(rayDepth).add(projection.getRayOrigin()).toArray());
		int llid = points.InsertNextPoint(
				projection.getLowerLeftUnit().scalarMultiply(rayDepth).add(projection.getRayOrigin()).toArray());
		int lrid = points.InsertNextPoint(
				projection.getLowerRightUnit().scalarMultiply(rayDepth).add(projection.getRayOrigin()).toArray());
		vtkLine ulline = new vtkLine();
		ulline.GetPointIds().SetId(0, oid);
		ulline.GetPointIds().SetId(1, ulid);
		vtkLine urline = new vtkLine();
		urline.GetPointIds().SetId(0, oid);
		urline.GetPointIds().SetId(1, urid);
		vtkLine llline = new vtkLine();
		llline.GetPointIds().SetId(0, oid);
		llline.GetPointIds().SetId(1, llid);
		vtkLine lrline = new vtkLine();
		lrline.GetPointIds().SetId(0, oid);
		lrline.GetPointIds().SetId(1, lrid);
		vtkCellArray cells = new vtkCellArray();
		cells.InsertNextCell(ulline);
		cells.InsertNextCell(urline);
		cells.InsertNextCell(llline);
		cells.InsertNextCell(lrline);

		if (projection.getHorizontalMin()>projection.getHorizontalMax() || projection.getVerticalMin()>projection.getVerticalMax())
			return new vtkPolyData();
		
		double[] lonDegRange = LinearSpace.create(projection.getHorizontalMin(), projection.getHorizontalMax(),
				nDivLon);
		double[] latDegRange = LinearSpace.create(projection.getVerticalMin(), projection.getVerticalMax(), nDivLat);

		double latDeg, lonDeg;
		int id;
		DepthFunction func = new ConstantDepthFunction(rayDepth);

		for (int i = 0; i < nDivLat; i++) {
			vtkPolyLine lonLine = new vtkPolyLine();
			latDeg = latDegRange[i];
			for (int j = 0; j < nDivLon; j++) {
				lonDeg = lonDegRange[j];
				id = points.InsertNextPoint(
						projection.unproject(new CylindricalMapCoordinates(lonDeg, latDeg), func).toArray());
				lonLine.GetPointIds().InsertNextId(id);
			}
			cells.InsertNextCell(lonLine);
		}

		for (int i = 0; i < nDivLon; i++) {
			vtkPolyLine latLine = new vtkPolyLine();
			lonDeg = lonDegRange[i];
			for (int j = 0; j < nDivLat; j++) {
				latDeg = latDegRange[j];
				id = points.InsertNextPoint(
						projection.unproject(new CylindricalMapCoordinates(lonDeg, latDeg), func).toArray());
				latLine.GetPointIds().InsertNextId(id);
			}
			cells.InsertNextCell(latLine);
		}

		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetLines(cells);
		return polyData;
	}

	public static vtkPolyData generatePolyDataRepresentation(CylindricalProjection projection, double rayDepth,
			vtkPoints pointsToProject) {
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();

		DepthFunction func = new ConstantDepthFunction(rayDepth);
		for (int i = 0; i < pointsToProject.GetNumberOfPoints(); i++) {
			int id = points.InsertNextPoint(
					projection.unproject(projection.project(new Vector3D(pointsToProject.GetPoint(i))), func).toArray());
			vtkVertex vert=new vtkVertex();
			vert.GetPointIds().SetId(0, id);
			cells.InsertNextCell(vert);
		}

		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetVerts(cells);
		return polyData;
	}

	public static vtkPolyData generatePolyDataRepresentation(CylindricalProjection projection, double rayDepth) {
		vtkCellArray cells = new vtkCellArray();
		vtkPoints points=new vtkPoints();
		int idor=points.InsertNextPoint(projection.getRayOrigin().toArray());
		int idll=points.InsertNextPoint(projection.getRayOrigin().add(projection.getLowerLeftUnit()).scalarMultiply(rayDepth).toArray());
		int idul=points.InsertNextPoint(projection.getRayOrigin().add(projection.getUpperLeftUnit()).scalarMultiply(rayDepth).toArray());
		int idlr=points.InsertNextPoint(projection.getRayOrigin().add(projection.getLowerRightUnit()).scalarMultiply(rayDepth).toArray());
		int idur=points.InsertNextPoint(projection.getRayOrigin().add(projection.getUpperRightUnit()).scalarMultiply(rayDepth).toArray());
		
		vtkLine ll=new vtkLine();
		ll.GetPointIds().SetId(0, idor);
		ll.GetPointIds().SetId(1, idll);
		
		vtkLine ul=new vtkLine();
		ul.GetPointIds().SetId(0, idor);
		ul.GetPointIds().SetId(1, idul);

		vtkLine lr=new vtkLine();
		lr.GetPointIds().SetId(0, idor);
		lr.GetPointIds().SetId(1, idlr);

		vtkLine ur=new vtkLine();
		ur.GetPointIds().SetId(0, idor);
		ur.GetPointIds().SetId(1, idur);
		
		vtkLine l=new vtkLine();
		l.GetPointIds().SetId(0, idll);
		l.GetPointIds().SetId(1, idul);

		vtkLine t=new vtkLine();
		t.GetPointIds().SetId(0, idul);
		t.GetPointIds().SetId(1, idur);

		vtkLine r=new vtkLine();
		r.GetPointIds().SetId(0, idur);
		r.GetPointIds().SetId(1, idlr);

		vtkLine b=new vtkLine();
		b.GetPointIds().SetId(0, idlr);
		b.GetPointIds().SetId(1, idll);

		cells.InsertNextCell(ll);
		cells.InsertNextCell(ul);
		cells.InsertNextCell(lr);
		cells.InsertNextCell(ur);
		cells.InsertNextCell(l);
		cells.InsertNextCell(t);
		cells.InsertNextCell(r);
		cells.InsertNextCell(b);
		
		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetLines(cells);
		return polyData;
	}

	public static vtkPolyData generatePolyDataRepresentation(CylindricalProjection projection, double rayDepth,
			vtkPolyData polyDataToProject) {
		vtkPolyData result=new vtkPolyData();
		result.DeepCopy(polyDataToProject);

		DepthFunction func = new ConstantDepthFunction(rayDepth);
		for (int i = 0; i < polyDataToProject.GetNumberOfPoints(); i++) {
			Vector3D projPt=projection.unproject(projection.project(new Vector3D(polyDataToProject.GetPoint(i))), func);
			result.GetPoints().SetPoint(i, projPt.toArray());
		}
		vtkDataArray tcoords=result.GetPointData().GetTCoords();
		if (tcoords!=null)
		{
			vtkFloatArray tcoordsCopy=new vtkFloatArray();
			tcoordsCopy.DeepCopy(tcoords);
			result.GetPointData().SetTCoords(tcoordsCopy);
		}
		return result;
	}

}
