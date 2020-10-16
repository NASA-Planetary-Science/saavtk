package edu.jhuapl.saavtk.gui.render;

import java.io.File;

import com.jogamp.opengl.GLContext;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.camera.CameraFrame;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;
import edu.jhuapl.saavtk.gui.render.axes.AxesPanel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.MathUtil;
import vtk.vtkBMPWriter;
import vtk.vtkCamera;
import vtk.vtkJPEGWriter;
import vtk.vtkPNGWriter;
import vtk.vtkPNMWriter;
import vtk.vtkPostScriptWriter;
import vtk.vtkTIFFWriter;
import vtk.vtkWindowToImageFilter;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Class that provides a collection of utility methods associated with
 * serializations associated with {@link Renderer}s.
 * <P>
 * The class contains methods that originated from the Renderer class.
 *
 * @author lopeznr1
 */
public class RenderIoUtil
{
	public static File createAxesFile(File aFile)
	{
		String extension = FilenameUtils.getExtension(aFile.getName());
		return new File(aFile.getAbsolutePath().replaceAll("." + extension, ".axes." + extension));
	}

	public static CameraFrame createCameraFrameInDirectionOfAxis(ModelManager aModelManager, RenderPanel aRenderPanel,
			AxisType aAxisType, boolean preserveCurrentDistance, File aFile, int aDelayMS)
	{
		CameraFrame result = new CameraFrame();
		result.file = aFile;
		result.delay = aDelayMS;

		double[] bounds = aModelManager.getPolyhedralModel().getBoundingBox().getBounds();
		double xSize = Math.abs(bounds[1] - bounds[0]);
		double ySize = Math.abs(bounds[3] - bounds[2]);
		double zSize = Math.abs(bounds[5] - bounds[4]);
		double maxSize = Math.max(Math.max(xSize, ySize), zSize);

		vtkCamera cam = aRenderPanel.getRenderer().GetActiveCamera();
		double[] posArr = cam.GetPosition();
		double cameraDistance = MathUtil.vnorm(posArr);

		result.focalPoint = new double[] { 0.0, 0.0, 0.0 };

		if (aAxisType == AxisType.NEGATIVE_X)
		{
			double xpos = xSize / Math.tan(Math.PI / 6.0) + 2.0 * maxSize;
			result.position = new double[] { xpos, 0.0, 0.0 };
			result.upDirection = new double[] { 0.0, 0.0, 1.0 };
		}
		else if (aAxisType == AxisType.POSITIVE_X)
		{
			double xpos = -xSize / Math.tan(Math.PI / 6.0) - 2.0 * maxSize;
			result.position = new double[] { xpos, 0.0, 0.0 };
			result.upDirection = new double[] { 0.0, 0.0, 1.0 };
		}
		else if (aAxisType == AxisType.NEGATIVE_Y)
		{
			double ypos = ySize / Math.tan(Math.PI / 6.0) + 2.0 * maxSize;
			result.position = new double[] { 0.0, ypos, 0.0 };
			result.upDirection = new double[] { 0.0, 0.0, 1.0 };
		}
		else if (aAxisType == AxisType.POSITIVE_Y)
		{
			double ypos = -ySize / Math.tan(Math.PI / 6.0) - 2.0 * maxSize;
			result.position = new double[] { 0.0, ypos, 0.0 };
			result.upDirection = new double[] { 0.0, 0.0, 1.0 };
		}
		else if (aAxisType == AxisType.NEGATIVE_Z)
		{
			double zpos = zSize / Math.tan(Math.PI / 6.0) + 2.0 * maxSize;
			result.position = new double[] { 0.0, 0.0, zpos };
			result.upDirection = new double[] { 0.0, 1.0, 0.0 };
		}
		else if (aAxisType == AxisType.POSITIVE_Z)
		{
			double zpos = -zSize / Math.tan(Math.PI / 6.0) - 2.0 * maxSize;
			result.position = new double[] { 0.0, 0.0, zpos };
			result.upDirection = new double[] { 0.0, 1.0, 0.0 };
		}

		if (preserveCurrentDistance)
		{
			double[] poshat = new double[3];

			MathUtil.unorm(result.position, poshat);

			result.position[0] = poshat[0] * cameraDistance;
			result.position[1] = poshat[1] * cameraDistance;
			result.position[2] = poshat[2] * cameraDistance;
		}

		return result;
	}

	public static void saveToFile(Renderer aRenderer)
	{
		RenderPanel tmpPanel = aRenderer.getRenderWindowPanel();

		tmpPanel.Render();
		File file = CustomFileChooser.showSaveDialog(aRenderer, "Export to PNG Image", "image.png", "png");
		saveToFile(file, tmpPanel, tmpPanel.getAxesPanel());
	}

	public static void saveToFile(File aFile, vtkJoglPanelComponent aRenWin, AxesPanel aAxesWin)
	{
		saveToFile(aFile, aRenWin);
		if (aAxesWin != null && ((RenderPanel) aRenWin).isAxesPanelVisible())
		{
			// axesWin.printModeOn();
			// axesWin.setSize(200, 200);
			RenderPanel renderPanel = (RenderPanel) aRenWin;
			// boolean visible=renderPanel.axesFrame.isVisible();
			// if (!visible)
			// renderPanel.axesFrame.setVisible(true);
			saveToFile(createAxesFile(aFile), aAxesWin);
			aAxesWin.Render();
			// if (!visible)
			// renderPanel.axesFrame.setVisible(false);
			// axesWin.printModeOff();
		}
	}

	protected static void saveToFile(File aFile, vtkJoglPanelComponent aRenWin)
	{
		if (aFile == null)
			return;

		GLContext glContext = null;
		try
		{
			glContext = aRenWin.getComponent().getContext();
			if (glContext != null)
			{
				// The following line is needed due to some weird threading
				// issue with JOGL when saving out the pixel buffer. Note release
				// needs to be called at the end.
				glContext.makeCurrent();
			}

			aRenWin.getVTKLock().lock();
			vtkWindowToImageFilter windowToImage = new vtkWindowToImageFilter();
			windowToImage.SetInput(aRenWin.getRenderWindow());
			windowToImage.ShouldRerenderOn();

			String filename = aFile.getAbsolutePath();
			if (filename.toLowerCase().endsWith("bmp"))
			{
				vtkBMPWriter writer = new vtkBMPWriter();
				writer.SetFileName(filename);
				writer.SetInputConnection(windowToImage.GetOutputPort());
				writer.Write();
			}
			else if (filename.toLowerCase().endsWith("jpg") || filename.toLowerCase().endsWith("jpeg"))
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
			else if (filename.toLowerCase().endsWith("tif") || filename.toLowerCase().endsWith("tiff"))
			{
				vtkTIFFWriter writer = new vtkTIFFWriter();
				writer.SetFileName(filename);
				writer.SetInputConnection(windowToImage.GetOutputPort());
				writer.SetCompressionToNoCompression();
				writer.Write();
			}
			aRenWin.getVTKLock().unlock();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		finally
		{
			if (glContext != null)
			{
				glContext.release();
			}
		}
	}

}
