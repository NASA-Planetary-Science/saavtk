package edu.jhuapl.saavtk.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.plot.BaseLinePlot;
import edu.jhuapl.saavtk.util.Properties;

public class ProfilePlot extends BaseLinePlot implements ChartMouseListener, PropertyChangeListener
{
	// Ref vars
	private final LineModel<PolyLine> refManager;
	private final PolyhedralModel refSmallBody;

	// State vars
	private ChartPanel chartPanel;
	private int coloringIndex;

	public ProfilePlot(LineModel<PolyLine> aManager, PolyhedralModel aSmallBody)
	{
		super(aManager);

		refManager = aManager;
		refSmallBody = aSmallBody;

		JFreeChart chart1 = ChartFactory.createXYLineChart("", "", "", getXYDataSet(), PlotOrientation.VERTICAL, false,
				true, false);

		// add the jfreechart graph
		chartPanel = new ChartPanel(chart1);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.addChartMouseListener(this);
		updateChartLabels();

		XYPlot plot = (XYPlot) chart1.getPlot();
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer)
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(false);
			renderer.setBaseShapesFilled(true);
		}

		// Set the coloring index last
		setColoringIndex(aSmallBody.getColoringIndex());

		// Register for events of interest
		aSmallBody.addPropertyChangeListener(this); // twupy1
	}

	@Override
	public ChartPanel getChartPanel()
	{
		return chartPanel;
	}

	@Override
	protected void getPlotPoints(PolyLine aItem, List<Double> xValueL, List<Double> yValueL)
	{
		// Bail if the line is not visible or valid
		if (aItem.getVisible() == false || aItem.getControlPoints().size() < 2)
			return;

		// Determine the plate coloring (index) to be utilized
		// Default index, -1, corresponds to radius
		int tmpIdx = -1;
		if (coloringIndex >= 0 && coloringIndex < refSmallBody.getNumberOfColors())
			tmpIdx = coloringIndex;

		List<Vector3D> xyzPointL = refManager.getXyzPointsFor(aItem);
		if (xyzPointL.size() == 0)
			return;
		try
		{
			refManager.generateProfile(xyzPointL, yValueL, xValueL, tmpIdx);
		}
		catch (Exception aExp)
		{
//			System.err.println("ProfilePlot.updateProfile() exception:\n" + aExp);
			aExp.printStackTrace();
		}

		// Values need to be scaled by 1/1000
		for (int aIdx = 0; aIdx < yValueL.size(); aIdx++)
			yValueL.set(aIdx, yValueL.get(aIdx) / 1000.0);
	}

	private void updateChartLabels()
	{
		// Figure out labels to use
		String title, domainLabel, rangeLabel;

		if (coloringIndex >= 0 && coloringIndex < refSmallBody.getNumberOfColors())
		{
			title = refSmallBody.getColoringName(coloringIndex);
			rangeLabel = refSmallBody.getColoringName(coloringIndex) + " (" + refSmallBody.getColoringUnits(coloringIndex)
					+ ")";
		}
		else
		{
			title = "Radius";
			rangeLabel = "Radius (m)";
			domainLabel = "";
		}

		// Common elements in labels
		title += " vs. Distance";
		domainLabel = "Distance (m)";

		// Apply the labels to the chart
		chartPanel.getChart().setTitle(title);
		chartPanel.getChart().getXYPlot().getDomainAxis().setLabel(domainLabel);
		chartPanel.getChart().getXYPlot().getRangeAxis().setLabel(rangeLabel);
	}

	public void setColoringIndex(int index)
	{
		// Save value of index
		coloringIndex = index;

		// Update chart labels
		updateChartLabels();

		// Update all the profiles
		notifyAllStale();
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent arg0)
	{
		ChartEntity entity = arg0.getEntity();
		if (entity instanceof XYItemEntity)
		{
			// int id = ((XYItemEntity)entity).getItem();
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent arg0)
	{
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			if (refSmallBody.getColoringIndex() != coloringIndex)
				setColoringIndex(refSmallBody.getColoringIndex());
		}
	}

}
