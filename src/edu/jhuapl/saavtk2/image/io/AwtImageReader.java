package edu.jhuapl.saavtk2.image.io;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.jhuapl.saavtk2.util.VtkDataTypes;
import vtk.vtkImageData;

public class AwtImageReader implements ImageDataReader
{

	@Override
	public vtkImageData read(File file)
	{
		try
		{
			BufferedImage image = ImageIO.read(file.getAbsoluteFile());
			vtkImageData imageData = new vtkImageData();
			imageData.SetDimensions(image.getWidth(), image.getHeight(), 1);
			imageData.AllocateScalars(VtkDataTypes.VTK_UNSIGNED_CHAR.ordinal(), 4);
			//final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			int[] data=image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
			for (int j=0; j<image.getHeight(); j++)
				for (int i=0; i<image.getWidth(); i++)
				{
					Color c=new Color(data[j*image.getWidth()+i]);
					imageData.SetScalarComponentFromDouble(i, image.getHeight()-1-j, 0, 0, c.getRed());
					imageData.SetScalarComponentFromDouble(i, image.getHeight()-1-j, 0, 1, c.getGreen());
					imageData.SetScalarComponentFromDouble(i, image.getHeight()-1-j, 0, 2, c.getBlue());
					imageData.SetScalarComponentFromDouble(i, image.getHeight()-1-j, 0, 3, c.getAlpha());
				}
			return imageData;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean extensionIsSupported(String ext)
	{
		return ImageIO.getImageReadersBySuffix(ext.toLowerCase()) != null;
	}

}
