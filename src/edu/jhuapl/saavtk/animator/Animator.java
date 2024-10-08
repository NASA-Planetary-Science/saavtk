package edu.jhuapl.saavtk.animator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Timer;

import edu.jhuapl.saavtk.gui.render.Renderer;
import vtk.vtkBMPWriter;
import vtk.vtkJPEGWriter;
import vtk.vtkPNGWriter;
import vtk.vtkPNMWriter;
import vtk.vtkPostScriptWriter;
import vtk.vtkTIFFWriter;
import vtk.vtkWindowToImageFilter;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * @author steelrj1
 *
 */
public class Animator implements ActionListener
{
	/**
	 *
	 */
	private BlockingQueue<AnimationFrame> animationFrameQueue;
    /**
     *
     */
    private Renderer renderer;
    /**
     *
     */
    private AnimatorFrameRunnable completionBlock;
    /**
     *
     */
    private Runnable movieBlock;
    
    private boolean isCancelled = false;
    
    private ArrayList<String> filenames = new ArrayList<String>();

    private File file;

	/**
	 * @param renderer
	 * @param runs
	 */
	public Animator(Renderer renderer)
	{
		this.renderer = renderer;
	}

	// creates animation frame with data to move the camera
    /**
     * @param tf
     * @param file
     * @param delay
     * @return
     */
    public AnimationFrame createAnimationFrameWithTimeFraction(double tf, File file, int delay)
    {
        AnimationFrame result = new AnimationFrame();
        result.timeFraction = tf;
        result.file = file;
        result.delay = delay;

        return result;
    }

    // saves a view to a file
    /**
     * @param file
     * @param renWin
     */
    public static void saveToFile(File file, vtkJoglPanelComponent renWin)
    {
        if (file != null)
        {
            try
            {
                // The following line is needed due to some weird threading
                // issue with JOGL when saving out the pixel buffer. Note release
                // needs to be called at the end.

                renWin.getComponent().getContext().makeCurrent();
                renWin.getVTKLock().lock();
                vtkWindowToImageFilter windowToImage = new vtkWindowToImageFilter();
                windowToImage.SetInput(renWin.getRenderWindow());

                String filename = file.getAbsolutePath();
                if (filename.toLowerCase().endsWith("bmp"))
                {
                    vtkBMPWriter writer = new vtkBMPWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("jpg") ||
                        filename.toLowerCase().endsWith("jpeg"))
                {
                    vtkJPEGWriter writer = new vtkJPEGWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("png"))
                {
                    vtkPNGWriter writer = new vtkPNGWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("pnm"))
                {
                    vtkPNMWriter writer = new vtkPNMWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("ps"))
                {
                    vtkPostScriptWriter writer = new vtkPostScriptWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("tif") ||
                        filename.toLowerCase().endsWith("tiff"))
                {
                    vtkTIFFWriter writer = new vtkTIFFWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.SetCompressionToNoCompression();
                    writer.Write();
                }
                renWin.getVTKLock().unlock();
            }
            finally
            {
                renWin.getComponent().getContext().release();
            }
        }
    }

 // called repeatedly to update the renderer and take the picture
    /**
     *
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
    	if (renderer.getRenderWindowPanel().getComponent().getWidth() %2 != 0) renderer.getRenderWindowPanel().getComponent().setSize(renderer.getRenderWindowPanel().getComponent().getWidth() + 1, renderer.getRenderWindowPanel().getComponent().getHeight());
        AnimationFrame frame = animationFrameQueue.peek();
        if (isCancelled) { cleanup(); return; }
        if (frame != null)
        {
            if (frame.staged && frame.file != null)
            {
                saveToFile(frame.file, renderer.getRenderWindowPanel());
                animationFrameQueue.remove();

            }
            else
            {
            	completionBlock.run(frame);
                frame.staged = true;

            }

            Timer timer = new Timer(frame.delay, this);
            timer.setRepeats(false);
            timer.start();
        }
        else
        {
        	new Thread(movieBlock).start();
        }

    }

    /**
     * @param frameNum
     * @param file
     * @param completionBlock
     * @param movieBlock
     */
    public void saveAnimation(int frameNum, File file, AnimatorFrameRunnable completionBlock, Runnable movieBlock)
    {
    	animationFrameQueue = new LinkedBlockingQueue<AnimationFrame>();
    	this.file = file;
        // creates the frames with the data necessary to take the images
    	String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
        String base = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator));
        String ext = ".png";
        new File(path+File.separator + ".movieCreate").mkdirs();
        for(double i = 0; i <= frameNum; i++)
        {
            String index = String.format("%03d",  (int)i);
            File f = new File(path+File.separator + ".movieCreate" + File.separator + base +"_Frame_"+index+ext);
            filenames.add(f.getAbsolutePath());
            double tf = i/(double)frameNum;
            AnimationFrame frame = createAnimationFrameWithTimeFraction(tf, f, 250);
            animationFrameQueue.add(frame);
        }
        this.completionBlock = completionBlock;
        this.movieBlock = movieBlock;
        this.actionPerformed(null);
    }
    
    public void cleanup()
    {
    	for (String filename : filenames)
		{
			new File(filename).delete();
		}
    	String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
		new File(path+File.separator + ".movieCreate").delete();
		this.isCancelled = false;
    }

	/**
	 * @param isCancelled the isCancelled to set
	 */
	public void setCancelled(boolean isCancelled)
	{
		this.isCancelled = isCancelled;
	}

}
