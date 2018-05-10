package edu.jhuapl.saavtk2.image;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.keys.MultiBandImageKey;
import edu.jhuapl.saavtk2.image.projection.MapCoordinates;
import edu.jhuapl.saavtk2.image.projection.Projection;
import jogamp.opengl.GLBufferObjectTracker.CreateStorageDispatch;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkPolyData;

/*public class ImageCube implements Image
{

	int primaryImage;
	List<Image> images;
	List<Integer> imageBandIds;
	
	GenericPolyhedralModel bodyModel;
	MultiBandImageKey key;
	vtkPolyData footprint;
	
	public ImageCube(MultiBandImageKey key, List<Image> images, List<Integer> imageBandIds, int primaryImage)
	{
		this.images=images;
		this.imageBandIds=imageBandIds;
		this.key=key;
		bodyModel=key.getBodyModel();
		build();
	}
	
	public ImageCube(GenericPolyhedralModel bodyModel, List<Image> images, List<Integer> imageBandIds, int primaryImage)
	{
		this.images=images;
		this.imageBandIds=imageBandIds;
		this.bodyModel=bodyModel;
		build();
	}

	public static String generateTcoordsArrayName(int band)
	{
		return "TCOORDS_BAND_"+band;
	}
	
	protected void build()
	{
		footprint=bodyModel.getSmallBodyPolyData();
		for (int i=0; i<images.size(); i++)
			footprint=images.get(i).getProjection().clipVisibleGeometry(footprint);
		for (int i=0; i<images.size(); i++)
		{
			vtkFloatArray tcoords=new vtkFloatArray();
			tcoords.SetNumberOfComponents(2);
			tcoords.SetName(generateTcoordsArrayName(i));
			for (int c=0; c<footprint.GetNumberOfPoints(); c++)
			{
				double[] uv=images.get(i).getTCoordsFromMapCoords(images.get(i).getProjection().project(new Vector3D(footprint.GetPoint(c))));
				tcoords.InsertNextTuple2(uv[0], uv[1]);
			}
			footprint.GetPointData().AddArray(tcoords);
		}
	}

	@Override
	public Projection getProjection()
	{
		return images.get(primaryImage).getProjection();
	}

	@Override
	public vtkPolyData getSurfaceGeometry()
	{
		return footprint;
	}

	@Override
	public vtkPolyData getProjectionGeometry()
	{
		return images.get(primaryImage).getProjectionGeometry();
	}

	@Override
	public vtkPolyData getOffLimbGeometry()
	{
		return new vtkPolyData();
	}

	@Override
	public int getNumberOfBands()
	{
		return images.size();
	}

	@Override
	public vtkImageData getBand(int i)
	{
		return images.get(primaryImage).getBand(imageBandIds.get(primaryImage));
	}

	@Override
	public MapCoordinates getMapCoordsFromPixel(int i, int j)
	{
		return images.get(primaryImage).getMapCoordsFromPixel(i, j);
	}

	@Override
	public int[] getPixelFromMapCoords(MapCoordinates mapCoords)
	{
		return images.get(primaryImage).getPixelFromMapCoords(mapCoords);
	}

	@Override
	public MapCoordinates getMapCoordsFromTCoords(double tx, double ty)
	{
		return images.get(primaryImage).getMapCoordsFromTCoords(tx, ty);
	}

	@Override
	public double[] getTCoordsFromMapCoords(MapCoordinates mapCoords)
	{
		return images.get(primaryImage).getTCoordsFromMapCoords(mapCoords);
	}

	@Override
	public Vector3D getPositionOnSurface(MapCoordinates mapCoords)
	{
		return images.get(primaryImage).getPositionOnSurface(mapCoords);
	}

//	@Override
//	public ImageKey getKey()
//	{
//		return key;
//	}
	
}
*/