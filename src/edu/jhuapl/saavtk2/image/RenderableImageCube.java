package edu.jhuapl.saavtk2.image;
//
//import java.util.List;
//
//import javax.swing.JFrame;
//import javax.swing.SwingUtilities;
//
//import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
//
//import com.google.common.collect.Lists;
//
//import edu.jhuapl.saavtk.gui.jogl.vtksbmtJoglCanvas;
//import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
//import edu.jhuapl.saavtk.polydata.clip.PolyDataClipWithCone;
//import edu.jhuapl.saavtk.util.Frustum;
//import edu.jhuapl.saavtk2.image.projection.PerspectiveProjection;
//import vtk.vtkImageData;
//import vtk.vtkNativeLibrary;
//import vtk.vtkPNGReader;
//import vtk.vtkPolyData;
//import vtk.vtkPolyDataWriter;
//import vtk.vtkSphereSource;
//import vtk.vtkTexture;
//import vtk.vtkXMLImageDataReader;
//
//public class RenderableImageCube extends RenderableImage
//{
//
//	public RenderableImageCube(ImageCube image)
//	{
//		super(image);
//		// TODO Auto-generated constructor stub
//	}
//	
//	@Override
//	protected void regenerateTexture()
//	{
//		baseImage.getSurfaceGeometry().GetPointData().SetActiveTCoords(((ImageCube)baseImage).generateTcoordsArrayName(activeBand));
//		super.regenerateTexture();
//	}
//
//	static ImageCube cube;
//	static RenderableImageCube renderableCube;
//	
//	public static void main(String[] args)
//	{
//		vtkNativeLibrary.LoadAllNativeLibraries();
//		
//		vtkSphereSource source=new vtkSphereSource();
//		source.SetThetaResolution(1440);
//		source.SetPhiResolution(720);
//		source.Update();
//		vtkPolyData polyData=source.GetOutput();
//		GenericPolyhedralModel bodyModel=new GenericPolyhedralModel(polyData);
//
//		vtkPNGReader reader=new vtkPNGReader();
//		reader.SetFileName("/Users/zimmemi1/Desktop/saavtk_small.png");
//		reader.Update();
//		vtkImageData imageData=reader.GetOutput();
//		
//		Vector3D viewpoint1=new Vector3D(2,0,0);
//		Vector3D lookat1=Vector3D.ZERO;
//		Frustum frustum1=new Frustum(viewpoint1, lookat1, Vector3D.PLUS_K, 10, 10);
//		PerspectiveImage image1=new PerspectiveImage(new PerspectiveProjection(frustum1), bodyModel, imageData, null);
//		
//		Vector3D viewpoint2=new Vector3D(2,1,0);
//		Vector3D lookat2=Vector3D.ZERO;
//		Frustum frustum2=new Frustum(viewpoint2, lookat2, Vector3D.PLUS_K, 10, 10);
//		PerspectiveImage image2=new PerspectiveImage(new PerspectiveProjection(frustum2), bodyModel, imageData, null);
//
//		cube=new ImageCube(bodyModel, Lists.newArrayList(image1,image2), Lists.newArrayList(0,0), 0);
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
//
//				//RenderableImage renderableImage1=new RenderableImage(image1);
//				//renderableImage1.setActiveBand(0);
//
//				//RenderableImage renderableImage2=new RenderableImage(image2);
//				//renderableImage2.setActiveBand(0);
//				
//				renderableCube=new RenderableImageCube(cube);
//				renderableCube.setActiveBand(1);
//
//				//canvas.getRenderer().AddActor(renderableImage1.surfaceActor);
//				//canvas.getRenderer().AddActor(renderableImage1.projectionActor);
//				
//				//canvas.getRenderer().AddActor(renderableImage2.surfaceActor);
//				//canvas.getRenderer().AddActor(renderableImage2.projectionActor);
//
//				canvas.getRenderer().AddActor(renderableCube.surfaceActor);
//				canvas.getRenderer().AddActor(renderableCube.projectionActor);
//			
//
//				frame.add(canvas.getComponent());
//			}
//		});
//
//
//	}
//
//	//		renderableCube.setActiveBand((renderableCube.activeBand+1)%cube.getNumberOfBands());
//
//}
