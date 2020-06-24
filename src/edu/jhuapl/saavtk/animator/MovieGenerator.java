package edu.jhuapl.saavtk.animator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.jcodec.api.awt.AWTSequenceEncoder;

/**
 * Helper class that takes in a series of captured frames (png format) to generate an mp4 movie
 * @author steelrj1
 *
 */
public class MovieGenerator
{
    /**
     * Generates an mp4 movie based on passed in list of filenames representing png frames
     * @param frames		List of filenames (string) for the frames that make up the movie
     * @param outputFile	The output filename (string) of the movie
     * @param width			The width of the movie (in pixels)
     * @param height		The height of the movie (in pixels)
     * @throws IOException	Throws an IOException if problems occur when reading the input files or outputting the final product
     */
    public static void create(List<String> frames, File outputFile, int width, int height) throws IOException
    {
		AWTSequenceEncoder enc = AWTSequenceEncoder.create30Fps(outputFile);
		for (String filename : frames)
		{
			BufferedImage image = ImageIO.read(new File(filename));
			enc.encodeImage(image);
		}
		enc.finish();
    }
}