package edu.jhuapl.saavtk2.image;

import vtk.vtkImageData;
import vtk.vtkPolyData;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.google.common.hash.HashCode;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk2.image.boundary.ImageBoundaryBuilder;
import edu.jhuapl.saavtk2.image.filters.ImageDataFilter;
import edu.jhuapl.saavtk2.image.filters.PassThroughImageDataFilter;
import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.projection.CylindricalProjection;
import edu.jhuapl.saavtk2.image.projection.PerspectiveProjection;
import edu.jhuapl.saavtk2.image.projection.Projection;
import edu.jhuapl.saavtk2.io.AwtImageReader;
import edu.jhuapl.saavtk2.io.EnviFileReader;
import edu.jhuapl.saavtk2.io.FitsImageFileReader;
import edu.jhuapl.saavtk2.io.InfoFileReader;
import edu.jhuapl.saavtk2.io.SumFileReader;

public class ImageBuilder
{
	Projection		projection		= null;
	vtkImageData	imageData		= null;
	GenericPolyhedralModel		bodyModel			= null;
	ImageKey key = null;
	int[] mask=new int[]{0,0,0,0};
	ImageDataFilter preFilter=null;

	class ImageBuilderException extends Exception
	{
		public ImageBuilderException(String message)
		{
			super(message);
		}
	};

	public ImageBuilder key(ImageKey key)
	{
		this.key=key;
		return this;
	}
	
	public ImageBuilder preFilter(ImageDataFilter filter)
	{
		this.preFilter=filter;
		return this;
	}
	
	public ImageBuilder projection(Projection projection)
	{
		this.projection = projection;
		return this;
	}

	public ImageBuilder sumProjection(File file)
	{
		projection = new SumFileReader().read(file);	// sum files always contain a perspective projection
		return this;
	}

	public ImageBuilder infoProjection(File file)
	{
		projection = new InfoFileReader().read(file);
		return this;
	}

	public ImageBuilder imageData(vtkImageData imageData)
	{
		this.imageData = imageData;
		return this;
	}

	public ImageBuilder awtImageData(File file)
	{
		imageData = new AwtImageReader().read(file);
		return this;
	}

	public ImageBuilder enviImageData(File file)
	{
		imageData = new EnviFileReader().read(file);
		return this;
	}

	public ImageBuilder fitsImageData(File file)
	{
		imageData = new FitsImageFileReader(false).read(file);
		return this;
	}

	public ImageBuilder bodyModel(GenericPolyhedralModel model)
	{
		this.bodyModel=model;
		return this;
	}
	
	public ImageBuilder leftMask(int m)
	{
		mask[0]=m;
		return this;
	}

	public ImageBuilder rightMask(int m)
	{
		mask[1]=m;
		return this;
	}

	public ImageBuilder topMask(int m)
	{
		mask[2]=m;
		return this;
	}

	public ImageBuilder bottomMask(int m)
	{
		mask[3]=m;
		return this;
	}

	public Image build()
	{
		try
		{
			if (projection == null)
				throw new ImageBuilderException("Projection not specified");
			if (bodyModel == null)
				throw new ImageBuilderException("Body model not specified");
			if (imageData == null)
				throw new ImageBuilderException("Image data not specified");
			if (key == null)
				throw new ImageBuilderException("Image key not specified");
			if (preFilter==null)
				preFilter=new PassThroughImageDataFilter();
			//
			if (projection instanceof PerspectiveProjection)
				return new PerspectiveImage((PerspectiveProjection) projection, bodyModel, imageData, /*key,*/ preFilter);
			else if (projection instanceof CylindricalProjection)
				return new CylindricalImage((CylindricalProjection) projection, bodyModel, imageData, /*key,*/ preFilter);
		}
		catch (ImageBuilderException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
}
