package edu.jhuapl.saavtk2.image.boundary;

import java.io.File;


import edu.jhuapl.saavtk2.io.InfoFileReader;
import edu.jhuapl.saavtk2.io.SumFileReader;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.projection.Projection;

public class ImageBoundaryBuilder
{
	Projection		projection		= null;
	GenericPolyhedralModel bodyModel=null;
	ImageKey key=null;
	

	class ImageBoundaryBuilderException extends Exception
	{
		public ImageBoundaryBuilderException(String message)
		{
			super(message);
		}
	};
	
	public ImageBoundaryBuilder key(ImageKey key)
	{
		this.key=key;
		return this;
	}

	public ImageBoundaryBuilder projection(Projection projection)
	{
		this.projection = projection;
		return this;
	}

	public ImageBoundaryBuilder sumProjection(File file)
	{
		projection = new SumFileReader().read(file);	// sum files always contain a perspective projection
		return this;
	}

	public ImageBoundaryBuilder infoProjection(File file)
	{
		projection = new InfoFileReader().read(file);
		return this;
	}


	public ImageBoundaryBuilder bodyModel(GenericPolyhedralModel model)
	{
		this.bodyModel=model;
		return this;
	}
	

	public ImageBoundary build()
	{
		try
		{
			if (projection == null)
				throw new ImageBoundaryBuilderException("Projection not specified");
			if (bodyModel == null)
				throw new ImageBoundaryBuilderException("Body model not specified");
			if (key == null)
				throw new ImageBoundaryBuilderException("Image key not specified");
			//
			return new GenericImageBoundary(projection, bodyModel, key);
		}
		catch (ImageBoundaryBuilderException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
}
