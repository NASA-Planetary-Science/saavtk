package edu.jhuapl.saavtk2.image.projection.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.toolbar.RenderToolbar;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk2.image.CylindricalImage;
import edu.jhuapl.saavtk2.image.projection.CylindricalProjection;
import edu.jhuapl.saavtk2.util.PolyDataUtil;
import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataNormals;
import vtk.vtkSphereSource;

public class CylindricalProjectionControlPanel extends JPanel implements ChangeListener {

	RenderToolbar toolbar = new RenderToolbar();
	RenderPanel renderObject = new RenderPanel(toolbar);

	JPanel sliderPanel = new JPanel();
	JSlider midLatSlider = new JSlider(-90, 90);
	JSlider midLonSlider = new JSlider(0, 360);
	JSlider deltaLatSlider = new JSlider(0, 180);
	JSlider deltaLonSlider = new JSlider(0, 360);

	vtkPolyData bodyPolyData;
	vtkPolyDataMapper bodyMapper = new vtkPolyDataMapper();
	vtkActor bodyActor = new vtkActor();

	vtkPolyData projectionPolyData;
	vtkPolyDataMapper projectionMapper = new vtkPolyDataMapper();
	vtkActor projectionActor = new vtkActor();

	vtkPolyData footprintPolyData;
	vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
	vtkActor footprintActor = new vtkActor();

	GenericPolyhedralModel model;
	CylindricalProjection projection;
	
	vtkPolyData graticulePolyData=new vtkPolyData();
	vtkPolyDataMapper graticuleMapper=new vtkPolyDataMapper();
	vtkActor graticuleActor=new vtkActor();
	
	public CylindricalProjectionControlPanel(GenericPolyhedralModel model) {
		this.model = model;

		bodyPolyData = model.getSmallBodyPolyData();
		bodyMapper.SetInputData(bodyPolyData);
		bodyMapper.Update();
		bodyActor.SetMapper(bodyMapper);

		setLayout(new BorderLayout());
		add(renderObject.getComponent(), BorderLayout.CENTER);

		JPanel midLatPanel = new JPanel();
		JPanel midLonPanel = new JPanel();
		JPanel deltaLatPanel = new JPanel();
		JPanel deltaLonPanel = new JPanel();

		midLatPanel.add(midLatSlider);
		midLatPanel.add(new JLabel("Lat Center", JLabel.CENTER));
		deltaLatPanel.add(deltaLatSlider);
		deltaLatPanel.add(new JLabel("Lat Spread", JLabel.CENTER));

		midLonPanel.add(midLonSlider);
		midLonPanel.add(new JLabel("Lon Center", JLabel.CENTER));
		deltaLonPanel.add(deltaLonSlider);
		deltaLonPanel.add(new JLabel("Lon Spread", JLabel.CENTER));

		midLatPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		midLonPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		deltaLatPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		deltaLatPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		midLatSlider.setPaintLabels(true);
		midLonSlider.setPaintLabels(true);
		deltaLatSlider.setPaintLabels(true);
		deltaLonSlider.setPaintLabels(true);
		midLatSlider.setPaintTicks(true);
		midLonSlider.setPaintTicks(true);
		deltaLatSlider.setPaintTicks(true);
		deltaLonSlider.setPaintTicks(true);
		midLatSlider.setMajorTickSpacing(30);
		midLonSlider.setMajorTickSpacing(90);
		deltaLatSlider.setMajorTickSpacing(30);
		deltaLonSlider.setMajorTickSpacing(90);

		JPanel southPanel = new JPanel(new GridLayout(2, 2));
		southPanel.add(midLatPanel);
		southPanel.add(deltaLatPanel);
		southPanel.add(midLonPanel);
		southPanel.add(deltaLonPanel);
		add(southPanel, BorderLayout.SOUTH);

		regenerateProjectionGeometry();
		projectionActor.SetMapper(projectionMapper);
		renderObject.getRenderer().AddActor(bodyActor);
		renderObject.getRenderer().AddActor(projectionActor);
		renderObject.getRenderer().AddActor(footprintActor);
		renderObject.resetCamera();

		midLatSlider.addChangeListener(this);
		midLonSlider.addChangeListener(this);
		deltaLatSlider.addChangeListener(this);
		deltaLonSlider.addChangeListener(this);

		/*vtkPolyData graticules = CylindricalProjection.generateGraticules(36, 10, bodyPolyData);
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputData(graticules);
		mapper.Update();
		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		actor.GetProperty().SetRepresentationToWireframe();
		actor.GetProperty().SetColor(0.25, 0, 0.5);
		actor.GetProperty().SetLineWidth(3);
		renderObject.getRenderer().AddActor(actor);*/
		renderObject.getRenderer().AddActor(graticuleActor);

/*		List<vtkActor2D> longitudeLabels = CylindricalProjection.generateLongitudeLabels(36, 0, model);
		for (int i = 0; i < longitudeLabels.size(); i++)
			renderObject.getRenderer().AddActor(longitudeLabels.get(i));

		int nlatBands = 3;
		for (int j = 0; j < 3; j++) {
			double lonDeg = 360. / nlatBands *j;
			List<vtkActor2D> latitudeLabels = CylindricalProjection.generateLatitudeLabels(10, lonDeg, model, j==0);
			for (int i = 0; i < latitudeLabels.size(); i++)
				renderObject.getRenderer().AddActor(latitudeLabels.get(i));
		}*/
		
		
	}

	protected void regenerateProjectionGeometry() {
		double minLat = Math.max(-90, midLatSlider.getValue() - deltaLatSlider.getValue() / 2.);
		double maxLat = Math.min(90, midLatSlider.getValue() + deltaLatSlider.getValue() / 2.);
		double minLon = midLonSlider.getValue() - deltaLonSlider.getValue() / 2.;
		double maxLon = midLonSlider.getValue() + deltaLonSlider.getValue() / 2.;

		projection = new CylindricalProjection(minLat, maxLat, minLon, maxLon);
		projectionPolyData = CylindricalImage.createProjectionGeometry(bodyPolyData.GetLength(), projection);
		projectionMapper.SetInputData(projectionPolyData);
		projectionMapper.Update();
		projectionActor.GetProperty().SetRepresentationToWireframe();
		projectionActor.GetProperty().SetEdgeColor(0, 1, 0);

		vtkPolyData tempPolyData = CylindricalImage.createSurfaceGeometry(bodyPolyData, projection);
		vtkPolyDataNormals normalFilter = new vtkPolyDataNormals();
		normalFilter.SetInputData(tempPolyData);
		normalFilter.ComputePointNormalsOn();
		normalFilter.ComputeCellNormalsOff();
		normalFilter.Update();
		PolyDataUtil.shiftPolyDataInNormalDirection(normalFilter.GetOutput(), bodyPolyData.GetLength() / 100.);
		footprintMapper.SetInputData(normalFilter.GetOutput());
		footprintMapper.Update();
		footprintActor.SetMapper(footprintMapper);
		footprintActor.GetProperty().SetColor(1, 1, 0);
		
		vtkAppendPolyData appendFilter=new vtkAppendPolyData();
		appendFilter.AddInputData(CylindricalProjection.generateLatitudeLine(maxLat, bodyPolyData));
		appendFilter.AddInputData(CylindricalProjection.generateLatitudeLine(minLat, bodyPolyData));
		appendFilter.AddInputData(CylindricalProjection.generateLongitudeLine(maxLon, bodyPolyData));
		appendFilter.AddInputData(CylindricalProjection.generateLongitudeLine(minLon, bodyPolyData));
		appendFilter.Update();
		
		graticulePolyData=appendFilter.GetOutput();
		graticuleMapper.SetInputData(graticulePolyData);
		graticuleMapper.Update();
		graticuleActor.SetMapper(graticuleMapper);
		graticuleActor.GetProperty().SetRepresentationToWireframe();
		graticuleActor.GetProperty().SetLineWidth(3);
		graticuleActor.GetProperty().SetColor(0.25,0,0.75);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == midLatSlider || e.getSource() == midLonSlider || e.getSource() == deltaLatSlider
				|| e.getSource() == deltaLonSlider) {
			regenerateProjectionGeometry();
			renderObject.Render();
		}

	}

	public static void main(String[] args) {
		vtkNativeLibrary.LoadAllNativeLibraries();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				vtkSphereSource source = new vtkSphereSource();
				source.SetPhiResolution(18);
				source.SetThetaResolution(36);
				source.Update();
				GenericPolyhedralModel model = new GenericPolyhedralModel(source.GetOutput());
				CylindricalProjectionControlPanel controlPanel = new CylindricalProjectionControlPanel(model);

				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				controlPanel.renderObject.setSize(300, 300);
				frame.add(controlPanel);
				frame.setSize(600, 600);
				controlPanel.setSize(new Dimension(600, 600));
				frame.setVisible(true);

			}
		});
	}

}
