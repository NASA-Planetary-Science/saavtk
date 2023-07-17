package edu.jhuapl.saavtk.util;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import vtk.vtkAbstractPointLocator;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtksbCellLocator;

class ObscuredTask implements Callable<Void>
	{
		private vtkPoints points;
		private vtksbCellLocator locator;
		private vtkAbstractPointLocator pointLocator;
		private int i;
		private double[] origin;
		private vtkPolyData polyData;
		private int[] numberOfObscuredPointsPerCell;

		public ObscuredTask(vtkPolyData polydata, vtkPoints points, vtksbCellLocator locator, vtkAbstractPointLocator pointLocator, double[] origin, 
				int[] numberOfObscuredPointsPerCell, int i)
		{
			this.polyData = polydata;
			this.points = points;
			this.locator = locator;
			this.pointLocator = pointLocator;
			this.origin = origin;
			this.numberOfObscuredPointsPerCell = numberOfObscuredPointsPerCell;
			this.i = i;
		}

		@Override
		public Void call() throws Exception
		{
			double tol = 1e-6;
			double[] t = new double[1];
			double[] x = new double[3];
			double[] pcoords = new double[3];
			int[] subId = new int[1];
			long[] cell_id = new long[1];
			double[] sourcePnt = points.GetPoint(i);
			vtkGenericCell cell = new vtkGenericCell();
			vtkIdList idList = new vtkIdList();


//			if (i==10) Logger.getAnonymousLogger().log(Level.INFO, "Getting result ");
			int result = locator.IntersectWithLine(origin, sourcePnt, tol, t, x, pcoords, subId, cell_id, cell);
//			if (i==10) Logger.getAnonymousLogger().log(Level.INFO, "Got result ");
			
				if (result == 1)
				{
//					if (i==10) Logger.getAnonymousLogger().log(Level.INFO, "Getting point ");
					synchronized (ObscuredTask.class)
					{
						int ptid = (int)pointLocator.FindClosestPoint(sourcePnt);
						polyData.GetPointCells(ptid, idList);

					}
//					if (i==10) Logger.getAnonymousLogger().log(Level.INFO, "Getting cells ");
//					if (i==10) Logger.getAnonymousLogger().log(Level.INFO, "Checking ");
					// The following check makes sure we don't delete any cells
					// if the intersection point happens to coincides with sourcePnt.
					// To do this we test to see of the intersected cell
					// is one of the cells which share a point with sourcePnt.
					// If it is we skip to the next point.
					if (idList.IsId(cell_id[0]) >= 0)
					{
						//System.out.println("Too close  " + i);
						return null;
					}

					polyData.GetPointCells(i, idList);
					int numPtCells = (int)idList.GetNumberOfIds();
					for (int j = 0; j < numPtCells; ++j)
					{
						// The following makes sure that only cells for which ALL three of its
						// points are obscured get deleted
						int cellId = (int)idList.GetId(j);
						++numberOfObscuredPointsPerCell[cellId];
						if (numberOfObscuredPointsPerCell[cellId] == 3)
							polyData.DeleteCell(cellId);
					}
//				}
			}
			
			return null;
		}
	}