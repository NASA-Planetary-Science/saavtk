package edu.jhuapl.saavtk.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import vtk.vtkAbstractPointLocator;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkPointLocator;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtksbCellLocator;

class FilterTask implements Callable<List<Integer>>
	{

		private vtkPolyData tmpPolyData;
		private vtksbCellLocator locator;
		private vtkAbstractPointLocator pointLocator;
		private double[] origin;
		private int i;
		private List<Integer> obscuredIds = Lists.newArrayList();

		public FilterTask(vtkPolyData passInPolyData, vtksbCellLocator locator, double[] origin, int i)
		{
			
//			this.tmpPolyData = new vtkPolyData();
//			tmpPolyData.DeepCopy(passInPolyData);
			this.tmpPolyData = passInPolyData;
			this.locator = locator;
			
			this.origin = origin;
			this.i = i;
		}

		@Override
		public List<Integer> call() throws Exception
		{
			pointLocator.BuildLocator();
			vtkPoints points = tmpPolyData.GetPoints();
			vtkGenericCell cell = new vtkGenericCell();
			double[] sourcePnt = points.GetPoint(i);
			vtkIdList idList = new vtkIdList();
			double tol = 1e-6;
			double[] t = new double[1];
			double[] x = new double[3];
			double[] pcoords = new double[3];
			int[] subId = new int[1];
			int[] cell_id = new int[1];
			Logger.getAnonymousLogger().log(Level.INFO, "Getting result ");
			int result = locator.IntersectWithLine(origin, sourcePnt, tol, t, x, pcoords, subId, cell_id, cell);
//			Logger.getAnonymousLogger().log(Level.INFO, "Got result ");
			if (result == 1)
			{
//				System.out.println("FilterTask: call: source " + new Vector3D(sourcePnt));
				int ptid = pointLocator.FindClosestPoint(sourcePnt);
//				System.out.println("FilterTask: call: pt id " + ptid);
//				System.out.println("FilterTask: call: idlist " + idList);
				tmpPolyData.GetPointCells(ptid, idList);

				// The following check makes sure we don't delete any cells
				// if the intersection point happens to coincides with sourcePnt.
				// To do this we test to see of the intersected cell
				// is one of the cells which share a point with sourcePnt.
				// If it is we skip to the next point.
				if (idList.IsId(cell_id[0]) >= 0)
				{
					//System.out.println("Too close  " + i);
//					continue;
					return obscuredIds;
				}

				tmpPolyData.GetPointCells(i, idList);
				int numPtCells = idList.GetNumberOfIds();
				for (int j = 0; j < numPtCells; ++j)
				{
					// The following makes sure that only cells for which ALL three of its
					// points are obscured get deleted
					int cellId = idList.GetId(j);
					obscuredIds.add(cellId);
//					++numberOfObscuredPointsPerCell[cellId];
//					if (numberOfObscuredPointsPerCell[cellId] == 3)
//						tmpPolyData.DeleteCell(cellId);
				}
				
			}
			return obscuredIds;
		}
	}