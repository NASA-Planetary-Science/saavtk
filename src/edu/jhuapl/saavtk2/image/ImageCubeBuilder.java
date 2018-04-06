package edu.jhuapl.saavtk2.image;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk2.image.projection.Projection;
import edu.jhuapl.saavtk2.image.projection.io.InfoFileReader;
import edu.jhuapl.saavtk2.image.projection.io.ProjFileReader;
import edu.jhuapl.saavtk2.image.projection.io.SumFileReader;
import vtk.vtkPolyData;

@Deprecated
public class ImageCubeBuilder
{
	Projection		projection		= null;
	List<Image> images=Lists.newArrayList();
	List<Integer> bands=Lists.newArrayList();
	GenericPolyhedralModel		bodyModel			= null;

	public ImageCubeBuilder projection(Projection projection)
	{
		this.projection = projection;
		return this;
	}

	public ImageCubeBuilder sumProjection(File file)
	{
		projection = new SumFileReader().read(file);	// sum files always contain a perspective projection
		return this;
	}

	public ImageCubeBuilder infoProjection(File file)
	{
		projection = new InfoFileReader().read(file);
		return this;
	}

	public ImageCubeBuilder projProjection(File file)
	{
		projection = new ProjFileReader().read(file);
		return this;
	}
	
	public ImageCubeBuilder addBand(Image image, int band)
	{
		images.add(image);
		bands.add(band);
		return this;
	}
	
	public ImageCubeBuilder bodyModel(GenericPolyhedralModel model)
	{
		this.bodyModel=model;
		return this;
	}
	
	public Image build()
	{
		//return new PerspectiveImage(projection, bodyModel, imageData, key)
		//return new PerspectiveImage(projection, bodyModel, imageData, key, preFilter)
		
		vtkPolyData footprint=bodyModel.getSmallBodyPolyData();
		for (int i=0; i<images.size(); i++)
			footprint=images.get(i).getProjection().clipVisibleGeometry(footprint);

		
		
		return null;
	}
}
