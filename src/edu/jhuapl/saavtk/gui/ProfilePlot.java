package edu.jhuapl.saavtk.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.util.Properties;

public class ProfilePlot implements ChartMouseListener, PropertyChangeListener
{
	// Ref vars
	private final LineModel<Line> refLineModel;
	private final PolyhedralModel refPolyhedralModel;

	private XYSeriesCollection valueDistanceDataset;
	private ChartPanel chartPanel;
	private int coloringIndex;

	private int numberOfProfilesCreated = 0;

	public ProfilePlot(LineModel<Line> aLineModel, PolyhedralModel aPolyhedralModel)
	{
		refLineModel = aLineModel;
		refPolyhedralModel = aPolyhedralModel;

		aLineModel.addPropertyChangeListener(this);
		aPolyhedralModel.addPropertyChangeListener(this); // twupy1

		valueDistanceDataset = new XYSeriesCollection();

		JFreeChart chart1 = ChartFactory.createXYLineChart("", "", "", valueDistanceDataset, PlotOrientation.VERTICAL,
				false, true, false);

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
		setColoringIndex(aPolyhedralModel.getColoringIndex());
	}

	public JPanel getChartPanel()
	{
		return chartPanel;
	}

	private void setSeriesColor(int lineId)
	{
		if (lineId == -1 || lineId >= valueDistanceDataset.getSeriesCount())
			return;

		Line line = refLineModel.getStructure(lineId);
		Color tmpColor = line.getColor();
		((XYPlot) chartPanel.getChart().getPlot()).getRenderer().setSeriesPaint(lineId, tmpColor);
	}

	private void addProfile()
	{
		int lineId = refLineModel.getNumItems() - 1;
		XYSeries series = new XYSeries("Profile " + numberOfProfilesCreated++);
		valueDistanceDataset.addSeries(series);
		setSeriesColor(lineId);

		// set line thickness
		((XYPlot) chartPanel.getChart().getPlot()).getRenderer().setSeriesStroke(lineId, new BasicStroke(2.0f));

		updateProfile(lineId);
	}

	private void updateProfile(int lineId)
	{
		if (lineId == -1 || lineId >= valueDistanceDataset.getSeriesCount() || lineId >= refLineModel.getNumItems())
			return;

		Line line = refLineModel.getStructure(lineId);
		List<Double> value = new ArrayList<Double>();
		List<Double> distance = new ArrayList<Double>();
		try
		{
			if (line.getVisible() == true && line.getControlPoints().size() == 2)
			{
				if (coloringIndex >= 0 && coloringIndex < refPolyhedralModel.getNumberOfColors())
				{
					// Get value of plate coloring "coloringIndex" along the profile specified in
					// line.xyzPointList
					refLineModel.generateProfile(line.xyzPointList, value, distance, coloringIndex);
				}
				else
				{
					// Default is to create a profile of the radius
					refLineModel.generateProfile(line.xyzPointList, value, distance, -1);
				}

				XYSeries series = valueDistanceDataset.getSeries(lineId);
				series.clear();
				int N = value.size();
				for (int i = 0; i < N; ++i)
				{
					series.add((double) distance.get(i), value.get(i) / 1000, false);
				}
				series.fireSeriesChanged();
			}
		}
		catch (Exception e)
		{
			System.err.println("ProfilePlot.updateProfile() exception:\n" + e);
		}
	}

	private void updateChartLabels()
	{
		// Figure out labels to use
		String title, domainLabel, rangeLabel;

		if (coloringIndex >= 0 && coloringIndex < refPolyhedralModel.getNumberOfColors())
		{
			title = refPolyhedralModel.getColoringName(coloringIndex);
			rangeLabel = refPolyhedralModel.getColoringName(coloringIndex) + " ("
					+ refPolyhedralModel.getColoringUnits(coloringIndex) + ")";
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
		;
		chartPanel.getChart().getXYPlot().getDomainAxis().setLabel(domainLabel);
		chartPanel.getChart().getXYPlot().getRangeAxis().setLabel(rangeLabel);
	}

	private void removeProfile(int lineId)
	{
		if (lineId == -1 || lineId >= valueDistanceDataset.getSeriesCount())
			return;

		valueDistanceDataset.removeSeries(lineId);
	}

	public void setColoringIndex(int index)
	{
		// Save value of index
		int numColoringIndices = refPolyhedralModel.getNumberOfColors();

		// Use the index, even if it is invalid
		coloringIndex = index;

		// Update chart labels
		updateChartLabels();

		// Update all the profiles
		updateAllProfiles();
	}

	private void updateAllProfiles()
	{
		// Update all profiles
		int numLines = valueDistanceDataset.getSeriesCount();
		for (int i = 0; i < numLines; i++)
		{
			updateProfile(i);
		}
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
		Line line = null;
		if (evt.getNewValue() instanceof Line)
			line = (Line) evt.getNewValue();
		int lineId = refLineModel.getAllItems().indexOf(line);

		if (Properties.VERTEX_INSERTED_INTO_LINE.equals(evt.getPropertyName()))
		{
			if (line != null && line.controlPointIds.size() == 2)
			{
				// Two clicks establishes a profile
				addProfile();
			}
			else if (line != null && line.controlPointIds.size() > 2)
			{
				// Main window differs from DEM view in that we can choose more than 2 control
				// points for a piecewise linear profile.
				updateProfile(lineId);
			}
		}
		else if (Properties.VERTEX_POSITION_CHANGED.equals(evt.getPropertyName()))
		{
			updateProfile(lineId);
		}
		else if (Properties.VERTEX_REMOVED_FROM_LINE.equals(evt.getPropertyName()))
		{
			updateProfile(lineId);
		}
		else if (Properties.STRUCTURE_REMOVED.equals(evt.getPropertyName()))
		{
			// TODO: This is not function properly
			removeProfile(lineId);
		}
		else if (Properties.ALL_STRUCTURES_REMOVED.equals(evt.getPropertyName()))
		{
			valueDistanceDataset.removeAllSeries();
		}
		else if (Properties.COLOR_CHANGED.equals(evt.getPropertyName()))
		{
			setSeriesColor(lineId);
		}
		else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			if (refPolyhedralModel.getColoringIndex() != coloringIndex)
			{
				setColoringIndex(refPolyhedralModel.getColoringIndex());
			}
			else
			{
				updateAllProfiles();
			}
		}
	}
}
