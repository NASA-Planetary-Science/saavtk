package edu.jhuapl.saavtk2.io;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.saavtk.util.ImageDataUtil;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import vtk.vtkImageData;

public class FitsImageFileReader implements ImageDataReader
{
	boolean shiftBands;

	public FitsImageFileReader()
	{
		this(false);
	}
	
	public FitsImageFileReader(boolean shiftBands)
	{
		this.shiftBands=shiftBands;
	}
	
	public static boolean extensionIsSupported(String ext)
	{
		return (ext.toLowerCase().equals("fit") || ext.toLowerCase().equals("fits"));
	}
	
	@Override
	public vtkImageData read(File file)
	{
        // TODO: maybe make this more efficient if possible

        String filename = file.getAbsolutePath();

        float[][] array2D = null;
        float[][][] array3D = null;
        double[][][] array3Ddouble = null;

        int[] fitsAxes = null;
        int fitsNAxes = 0;
        // height is axis 0
        int fitsHeight = 0;
        // for 2D pixel arrays, width is axis 1, for 3D pixel arrays, width axis is 2
        int fitsWidth = 0;
        // for 2D pixel arrays, depth is 0, for 3D pixel arrays, depth axis is 1
        int fitsDepth = 0;

        try
        {
        // single file images (e.g. LORRI and LEISA)
        if (true)
        {
            Fits f = new Fits(filename);
            BasicHDU<?> h = f.getHDU(0);

            fitsAxes = h.getAxes();
            fitsNAxes = fitsAxes.length;
            fitsHeight = fitsAxes[0];
            fitsWidth = fitsNAxes == 3 ? fitsAxes[2] : fitsAxes[1];
            fitsDepth = fitsNAxes == 3 ? fitsAxes[1] : 1;

            Object data = h.getData().getData();

            // for 3D arrays we consider the second axis the "spectral" axis
            if (data instanceof float[][][])
            {
                if (shiftBands)
                {
                    array3D = new float[fitsHeight][fitsWidth][fitsDepth];
                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                            for (int k=0; k<fitsDepth; ++k)
                            {
                                int w = i + j - fitsDepth / 2;
                                if (w >= 0 && w < fitsHeight)
                                    array3D[w][j][k] = ((float[][][])data)[i][j][k];
                            }

                }
                else
                    array3D = (float[][][])data;

//               System.out.println("3D pixel array detected: " + array3D.length + "x" + array3D[0].length + "x" + array3D[0][0].length);
            }
            else if (data instanceof double[][][])
            {
                array3Ddouble = new double[fitsHeight][fitsWidth][fitsDepth];
                if (shiftBands)
                {
                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                            for (int k=0; k<fitsDepth; ++k)
                            {
                                int w = i + j - fitsDepth / 2;
                                if (w >= 0 && w < fitsHeight)
                                    array3Ddouble[w][j][k] = ((double[][][])data)[i][j][k];
                            }

                }
                else
                {
                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                            for (int k=0; k<fitsDepth; ++k)
                            {
                                array3Ddouble[i][j][k] = ((double[][][])data)[i][j][k];
                            }

                }

//               System.out.println("3D pixel array detected: " + array3D.length + "x" + array3D[0].length + "x" + array3D[0][0].length);
            }
            else if (data instanceof float[][])
            {
                array2D = (float[][])data;
            }
            else if (data instanceof short[][])
            {
                short[][] arrayS = (short[][])data;
                array2D = new float[fitsHeight][fitsWidth];

                for (int i=0; i<fitsHeight; ++i)
                    for (int j=0; j<fitsWidth; ++j)
                    {
                        array2D[i][j] = arrayS[i][j];
                    }
            }
            else if (data instanceof double[][])
            {
                double[][] arrayDouble = (double[][])data;
                array2D = new float[fitsHeight][fitsWidth];

                for (int i=0; i<fitsHeight; ++i)
                    for (int j=0; j<fitsWidth; ++j)
                    {
                        array2D[i][j] = (float)arrayDouble[i][j];
                    }
            }
            else if (data instanceof byte[][])
            {
                byte[][] arrayB = (byte[][])data;
                array2D = new float[fitsHeight][fitsWidth];

                for (int i=0; i<fitsHeight; ++i)
                    for (int j=0; j<fitsWidth; ++j)
                    {
                        array2D[i][j] = arrayB[i][j] & 0xFF;
                    }
            }
            else
            {
//                System.out.println("Data type not supported: " + data.getClass().getCanonicalName());
                f.close();
                return null;
            }

            // load in calibration info
            loadImageCalibrationData(f);
            array3D=null;

            f.getStream().close();
            f.close();

        }
/*        // for multi-file images (e.g. MVIC)
        else if (filenames.length > 1)
        {
            fitsDepth = filenames.length;
            fitsAxes = new int[3];
            fitsAxes[2] = fitsDepth;
            fitsNAxes = 3;

            for (int k=0; k<fitsDepth; k++)
            {
                Fits f = new Fits(filenames[k]);
                BasicHDU h = f.getHDU(0);

                int[] multiImageAxes = h.getAxes();
                int multiImageNAxes = multiImageAxes.length;

                if (multiImageNAxes > 2)
                {
//                    System.out.println("Multi-file images must be 2D.");
                    return null;
                }

                // height is axis 0, width is axis 1
                fitsHeight = fitsAxes[0] = multiImageAxes[0];
                fitsWidth = fitsAxes[2] = multiImageAxes[1];

                if (array3D == null)
                    array3D = new float[fitsHeight][fitsDepth][fitsWidth];


                Object data = h.getData().getData();

                if (data instanceof float[][])
                {
                    // NOTE: could performance be improved if depth was the first index and the entire 2D array could be assigned to a each slice? -turnerj1
                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                        {
                            array3D[i][k][j] = ((float[][])data)[i][j];
                        }
                }
                else if (data instanceof short[][])
                {
                    short[][] arrayS = (short[][])data;

                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                        {
                            array3D[i][k][j] = arrayS[i][j];
                        }
                }
                else if (data instanceof byte[][])
                {
                    byte[][] arrayB = (byte[][])data;

                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                        {
                            array3D[i][k][j] = arrayB[i][j] & 0xFF;
                        }
                }
                else
                {
//                    System.out.println("Data type not supported!");
                    return null;
                }

                f.getStream().close();
            }*/
        //}
        }
        catch (IOException | FitsException ex)
        {
        	ex.printStackTrace();
        }
        return createRawImage(fitsHeight, fitsWidth, fitsDepth, array2D, array3D);
	}

    protected vtkImageData createRawImage(int height, int width, int depth, float[][] array2D, float[][][] array3D)
    {
        return createRawImage(height, width, depth, true, array2D, array3D);
    }

    protected vtkImageData createRawImage(int height, int width, int depth, boolean transpose, float[][] array2D, float[][][] array3D)
    {
        // Allocate enough room to store min/max value at each layer
        float[] maxValue = new float[depth];
        float[] minValue = new float[depth];

        // Call
        return ImageDataUtil.createRawImage(height, width, depth, transpose, array2D, array3D, minValue, maxValue);
    }
	
    protected void loadImageCalibrationData(Fits f) throws FitsException, IOException
    {
    	
    }


}
