package edu.jhuapl.saavtk2.image;
/*
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.jogl.vtksbmtJoglCanvas;
import edu.jhuapl.saavtk.model.Renderable;
import edu.jhuapl.saavtk.polydata.PolyDataComputeMeanNormal;
import edu.jhuapl.saavtk.polydata.PolyDataComputeMeanPosition;
import edu.jhuapl.saavtk.polydata.VtkDataAssociation;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk2.image.filters.ImageDataFilter;
import edu.jhuapl.saavtk2.image.filters.PassThroughImageDataFilter;
import edu.jhuapl.saavtk2.image.io.AwtImageReader;
import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkImageData;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkSphereSource;
import vtk.vtkTexture;
import vtk.vtkXMLImageDataWriter;

public class RenderableImage implements Renderable
{

	
	Image baseImage;
	int activeBand=0;
	
	boolean masterVisibility=true;
	boolean showSurface=true;
	boolean showProjection=true;
	boolean showOffLimb=true;
	
	double masterOpacity=1;
	double surfaceOpacity=1;
	double projectionOpacity=1;
	double offLimbOpacity=1;

	static final double defaultOffset=0.001;
	double offset;
	Vector3D offsetUnitVector;
	Vector3D initialCentroid;
	
	vtkPolyDataMapper surfaceMapper=new vtkPolyDataMapper();
	vtkPolyDataMapper projectionMapper=new vtkPolyDataMapper();
	vtkPolyDataMapper offLimbMapper=new vtkPolyDataMapper();
	
	vtkActor surfaceActor=new vtkActor();
	vtkActor projectionActor=new vtkActor();
	vtkActor offLimbActor=new vtkActor();
	
	ImageDataFilter coloringStrategy=new PassThroughImageDataFilter();
	
	PropertyChangeSupport pcs=new PropertyChangeSupport(this);
	
	public RenderableImage(Image image)
	{
		baseImage=image;
		//
		surfaceMapper.SetInputData(image.getSurfaceGeometry());
		projectionMapper.SetInputData(image.getProjectionGeometry());
		offLimbMapper.SetInputData(image.getOffLimbGeometry());
		//
		surfaceMapper.SetColorModeToDirectScalars();
		projectionMapper.SetColorModeToDirectScalars();
		offLimbMapper.SetColorModeToDirectScalars();
		//
		surfaceActor.SetMapper(surfaceMapper);
		projectionActor.SetMapper(projectionMapper);
		offLimbActor.SetMapper(offLimbMapper);
		//
		offsetUnitVector=new PolyDataComputeMeanNormal(VtkDataAssociation.CELLS).apply(image.getSurfaceGeometry());
		initialCentroid=new PolyDataComputeMeanPosition(VtkDataAssociation.POINTS).apply(image.getSurfaceGeometry());
		setOffset(0);
		//
		setActiveBand(0);
	}
	
	public boolean getSurfaceVisibility()
	{
		return showSurface && masterVisibility;
	}
	
	public boolean getProjectionVisibility()
	{
		return showProjection && masterVisibility;
	}
	
	public boolean getOffLimbVisibility()
	{
		return showOffLimb && masterVisibility;
	}
	
	public void setSurfaceVisibility(boolean show)
	{
		showSurface=show;
		surfaceActor.SetVisibility(getSurfaceVisibility()?1:0);
		fireModelChangedEvent();
	}
	
	public void setProjectionVisibility(boolean show)
	{
		showProjection=show;
		projectionActor.SetVisibility(getProjectionVisibility()?1:0);
		fireModelChangedEvent();
	}
	
	public void setOffLimbVisibility(boolean show)
	{
		showOffLimb=show;
		fireModelChangedEvent();
	}
	
	@Override
	public void setVisible(boolean b)
	{
		masterVisibility=b;
		fireModelChangedEvent();
	}
	
	@Override
	public boolean isVisible()
	{
		return masterVisibility;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);		
	}

	@Override
	public List<vtkProp> getProps()
	{
		surfaceActor.GetProperty().SetOpacity(getSurfaceOpacity());
		projectionActor.GetProperty().SetOpacity(getProjectionOpacity());
		offLimbActor.GetProperty().SetOpacity(getOffLimbOpacity());
		//
		surfaceActor.SetVisibility(getSurfaceVisibility()?1:0);
		projectionActor.SetVisibility(getProjectionVisibility()?1:0);
		offLimbActor.SetVisibility(getOffLimbVisibility()?1:0);
		//
		return Lists.newArrayList(surfaceActor,projectionActor,offLimbActor);
	}

	@Override
	public boolean isBuiltIn()
	{
		return false;
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		return "";
	}
	
	public double getSurfaceOpacity()
	{
		return surfaceOpacity*masterOpacity;
	}


	public double getProjectionOpacity()
	{
		return projectionOpacity*masterOpacity;
	}

	public double getOffLimbOpacity()
	{
		return offLimbOpacity*masterOpacity;
	}

	public void setSurfaceOpacity(double surfaceOpacity)
	{
		this.surfaceOpacity = surfaceOpacity;
		fireModelChangedEvent();
	}

	public void setProjectionOpacity(double projectionOpacity)
	{
		this.projectionOpacity = projectionOpacity;
		fireModelChangedEvent();
	}


	public void setOffLimbOpacity(double offLimbOpacity)
	{
		this.offLimbOpacity = offLimbOpacity;
		fireModelChangedEvent();
	}

	@Override
	public void setOpacity(double opacity)
	{
		masterOpacity=opacity;
		fireModelChangedEvent();
	}

	@Override
	public double getOpacity()
	{
		return masterOpacity;
	}
	

	@Override
	public void setOffset(double offset)
	{
		this.offset=offset;
		surfaceActor.SetPosition(initialCentroid.add(offsetUnitVector.scalarMultiply(offset)).toArray());
		fireModelChangedEvent();
	}

	@Override
	public double getOffset()
	{
		return offset;
	}

	@Override
	public double getDefaultOffset()
	{
		return defaultOffset;
	}

	@Override
	public void delete()
	{
	}

	@Override
	public void set2DMode(boolean enable)
	{
	}

	@Override
	public boolean supports2DMode()
	{
		return false;
	}

	
	protected void fireModelChangedEvent()
	{
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	protected void setActiveBand(int i)
	{
		this.activeBand=i;
		regenerateTexture();
	}
	
	protected void regenerateTexture()
	{
		vtkTexture texture=new vtkTexture();
		texture.SetInputData(coloringStrategy.apply(baseImage.getBand(activeBand)));
		texture.Update();
		surfaceActor.SetTexture(texture);
		surfaceActor.GetMapper().ScalarVisibilityOff();
	}
	
	public void setColoringStrategy(ImageDataFilter strategy)
	{
		this.coloringStrategy=strategy;
		regenerateTexture();
	}
	
	public vtkImageData getTextureData()
	{
		return surfaceActor.GetTexture().GetInput();
	}
	
//	public static void main(String[] args)
//	{
//		vtkNativeLibrary.LoadAllNativeLibraries();
//		
//		vtkSphereSource source=new vtkSphereSource();
//		source.SetThetaResolution(36);
//		source.SetPhiResolution(18);
//		source.Update();
//		vtkPolyData surface=source.GetOutput();
//			
//		Vector3D origin=Vector3D.PLUS_K.scalarMultiply(2);
//		Vector3D lookAt=Vector3D.ZERO;
//		Vector3D up=Vector3D.PLUS_J;
//		double fov=10;
//		Frustum frustum=new Frustum(origin, lookAt, up, fov, fov);
//		
//		vtkImageData imageData=AwtImageReader.read(new File("/Users/zimmemi1/Desktop/megaman.jpg"));
//		vtkXMLImageDataWriter iwriter=new vtkXMLImageDataWriter();
//		iwriter.SetFileName("/Users/zimmemi1/Desktop/itest.vtk");
//		iwriter.SetDataModeToBinary();
//		iwriter.SetInputData(imageData);
//		iwriter.Update();
//		
//		PerspectiveImage image=new PerspectiveImage(frustum, surface, imageData);
//		RenderableImage renderable=new RenderableImage(image);
//
//		vtkAppendPolyData appendFilter=new vtkAppendPolyData();
//		List<vtkProp> props=renderable.getProps();
//		for (int i=0; i<props.size(); i++)
//		{
//			vtkProp prop=props.get(i);
//			vtkPolyData polyData=((vtkPolyDataMapper)((vtkActor)prop).GetMapper()).GetInput();
//			appendFilter.AddInputData(polyData);
//		}
//		appendFilter.Update();
//		
//		SwingUtilities.invokeLater(new Runnable()
//		{
//			
//			@Override
//			public void run()
//			{
//				JFrame frame=new JFrame();
//				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//				frame.setSize(600, 600);
//				frame.setVisible(true);
//				
//				vtksbmtJoglCanvas canvas=new vtksbmtJoglCanvas();
//				canvas.setSize(600, 600);
//				canvas.getRenderer().AddActor(renderable.surfaceActor);
//				canvas.getRenderer().AddActor(renderable.boundaryActor);
//				canvas.getRenderer().AddActor(renderable.projectionActor);
//				canvas.getRenderer().AddActor(renderable.offLimbActor);
//				
//				frame.add(canvas.getComponent());
//			}
//		});
//		
//		
//	
//	}
	
}
*/