package edu.jhuapl.saavtk.gui.render.toolbar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.xml.transform.Source;

public class ImageUtil
{
	/**
	 * Utility method to colorize the image, aImage, with the specified color.
	 * <P>
	 * {@link Source
	 * https://stackoverflow.com/questions/21382966/colorize-a-picture-in-java}
	 * 
	 * @param aImage
	 * @param aColor
	 * @return
	 */
	public static BufferedImage colorize(BufferedImage aImage, Color aColor)
	{
		int w = aImage.getWidth();
		int h = aImage.getHeight();
		BufferedImage retImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = retImage.createGraphics();

		g.drawImage(aImage, 0, 0, null);
		g.setComposite(AlphaComposite.SrcAtop);
		g.setColor(aColor);
		g.fillRect(0, 0, w, h);
		g.dispose();
		return retImage;
	}

	/**
	 * Utility method to darken the specified image by the specified factor amount.
	 * <P>
	 * {@link Source https://stackoverflow.com/questions/10333387/darkening-image}
	 * 
	 * @param aImage
	 * @param aFactor
	 * @return
	 */
	public static BufferedImage darker(BufferedImage aImage, float aFactor)
	{
		float[] elements = { aFactor };

		Kernel kernel = new Kernel(1, 1, elements);
		ConvolveOp op = new ConvolveOp(kernel);

		BufferedImage retImage = new BufferedImage(aImage.getWidth(), aImage.getHeight(), aImage.getType());
		op.filter(aImage, retImage);

		return retImage;
	}

	/**
	 * Utility method to load the image located at the specified URL and scale to
	 * the specified dimensions (if necessary).
	 * 
	 * @param aResourceName The URL which corresponds to the image of interest.
	 * @param aImageW       The width for which the returned image will be scaled
	 *                      to. Set to negative to skip scaling.
	 * @param aImageH       The height for which the returned image will be scaled
	 *                      to. Set to negative to skip scaling.
	 */
	public static BufferedImage loadImage(URL aURL, int aImageW, int aImageH)
	{
		BufferedImage retImage;

		try
		{
			retImage = ImageIO.read(aURL);
		}
		catch (Throwable aThrowable)
		{
			throw new RuntimeException(aThrowable);
		}

		// Scale the image if necessary
		if (aImageW > 0 && aImageH > 0 && (retImage.getWidth() != aImageW || retImage.getHeight() != aImageH))
		{
			Image tmpImage = retImage.getScaledInstance(aImageW, aImageH, Image.SCALE_SMOOTH);
			retImage = ImageUtil.toBufferedImage(tmpImage);
		}

		return retImage;
	}

	/**
	 * Returns an image where any pixel with an alpha value == aAlpha is replaced
	 * with the specified color.
	 * 
	 * @param aImage The source image
	 * @param aColor The replacement color for any matching pixel
	 * @param aAlpha The alpha value that will be replaced.
	 * @return
	 */
	public static BufferedImage replaceAlpha(BufferedImage aImage, Color aColor, int aAlpha)
	{
		int w = aImage.getWidth();
		int h = aImage.getHeight();
		BufferedImage retImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = retImage.createGraphics();

		g.drawImage(aImage, 0, 0, null);
		g.dispose();

		int argb = aColor.getRGB();
		for (int i = 0; i < retImage.getWidth(); i++)
		{
			for (int j = 0; j < retImage.getWidth(); j++)
			{
				if ((retImage.getRGB(i, j) & 0xFF000000) == aAlpha)
					retImage.setRGB(i, j, argb);
			}
		}

		return retImage;
	}

	/**
	 * Utility method to convert the given Image into a BufferedImage
	 * <P>
	 * {@link Source
	 * https://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage}
	 * 
	 * @param aImage The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image aImage)
	{
		if (aImage instanceof BufferedImage)
			return (BufferedImage) aImage;

		// Create a buffered image with transparency
		BufferedImage retImage = new BufferedImage(aImage.getWidth(null), aImage.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D g2d = retImage.createGraphics();
		g2d.drawImage(aImage, 0, 0, null);
		g2d.dispose();

		// Return the buffered image
		return retImage;
	}

}
