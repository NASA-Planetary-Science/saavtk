package edu.jhuapl.saavtk.animator;

import java.io.File;

/**
 * @author steelrj1
 *
 */
public class AnimationFrame
{
    /**
     * Boolean describing whether the frame is ready to be saved
     */
    public boolean staged;

    /**
     * Boolean describing whether the frame has been saved
     */
    public boolean saved;

    /**
     * Time to wait (in ms) before the frame is captured
     */
    public int delay;

    /**
     * Time fraction (between 0 and 1) along the displayed timeline for this frame
     */
    public double timeFraction;

    /**
     * The file to which this file is written
     */
    public File file;
}