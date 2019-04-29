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
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.util.Properties;

public class ProfilePlot implements ChartMouseListener, PropertyChangeListener
{
	private XYDataset valueDistanceDataset;
	private LineModel lineModel;
	private ChartPanel chartPanel;
	private PolyhedralModel smallBodyModel;
	private int coloringIndex;

	private int numberOfProfilesCreated = 0;

	public ProfilePlot(LineModel lineModel, PolyhedralModel smallBodyModel)
	{
		this.lineModel = lineModel;
		this.smallBodyModel = smallBodyModel;

		lineModel.addPropertyChangeListener(this);
		smallBodyModel.addPropertyChangeListener(this); // twupy1

		valueDistanceDataset = new XYSeriesCollection();

		JFreeChart chart1 = ChartFactory.createXYLineChart("", "", "", valueDistanceDataset, PlotOrientation.VERTICAL, false, true, false);

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
		setColoringIndex(smallBodyModel.getColoringIndex());
	}

	public JPanel getChartPanel()
	{
		return chartPanel;
	}

	private void setSeriesColor(int lineId)
	{
		Line line = (Line) lineModel.getStructure(lineId);
		int[] c = line.getColor();
		((XYPlot) chartPanel.getChart().getPlot()).getRenderer().setSeriesPaint(lineId, new Color(c[0], c[1], c[2], c[3]));
	}

	private void addProfile()
	{
		int lineId = lineModel.getNumberOfStructures() - 1;
		XYSeries series = new XYSeries("Profile " + numberOfProfilesCreated++);
		((XYSeriesCollection) valueDistanceDataset).addSeries(series);
		setSeriesColor(lineId);
		((XYPlot) chartPanel.getChart().getPlot()).getRenderer().setSeriesStroke(lineId, new BasicStroke(2.0f)); // set line thickness
		updateProfile(lineId);
	}

	private void updateProfile(int lineId)
	{
		if (lineId >= ((XYSeriesCollection) valueDistanceDataset).getSeriesCount() || lineId >= lineModel.getNumberOfStructures())
			return;

		Line line = (Line) lineModel.getStructure(lineId);
		List<Double> value = new ArrayList<Double>();
		List<Double> distance = new ArrayList<Double>();
		try
		{
			if (!line.getHidden() && line.getControlPoints().size() == 2)
			{
				if (coloringIndex >= 0 && coloringIndex < smallBodyModel.getNumberOfColors())
				{
					// Get value of plate coloring "coloringIndex" along the profile specified in line.xyzPointList
					lineModel.generateProfile(line.xyzPointList, value, distance, coloringIndex);
				}
				else
				{
					// Default is to create a profile of the radius
					lineModel.generateProfile(line.xyzPointList, value, distance, -1);
				}

				XYSeries series = ((XYSeriesCollection) valueDistanceDataset).getSeries(lineId);
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

		if (coloringIndex >= 0 && coloringIndex < smallBodyModel.getNumberOfColors())
		{
			title = smallBodyModel.getColoringName(coloringIndex);
			rangeLabel = smallBodyModel.getColoringName(coloringIndex) + " (" + smallBodyModel.getColoringUnits(coloringIndex) + ")";
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
		if (lineId < ((XYSeriesCollection) valueDistanceDataset).getSeriesCount())
		{
			((XYSeriesCollection) valueDistanceDataset).removeSeries(lineId);
		}
	}

	public void setColoringIndex(int index)
	{
		// Save value of index
		int numColoringIndices = smallBodyModel.getNumberOfColors();

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
		int numLines = ((XYSeriesCollection) valueDistanceDataset).getSeriesCount();
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
			//int id = ((XYItemEntity)entity).getItem();
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent arg0)
	{}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.VERTEX_INSERTED_INTO_LINE.equals(evt.getPropertyName()))
		{
			int lineId = (Integer) evt.getNewValue();
			Line line = (Line) lineModel.getStructure(lineId);

			if (line.controlPointIds.size() == 2)
			{
				// Two clicks establishes a profile
				addProfile();
			}
			else if (line.controlPointIds.size() > 2)
			{
				// Main window differs from DEM view in that we can choose more than 2 control
				// points for a piecewise linear profile.
				updateProfile(lineId);
			}
		}
		else if (Properties.VERTEX_POSITION_CHANGED.equals(evt.getPropertyName()))
		{
			int lineId = (Integer) evt.getNewValue();
			updateProfile(lineId);
		}
		else if (Properties.VERTEX_REMOVED_FROM_LINE.equals(evt.getPropertyName()))
		{
			int lineId = (Integer) evt.getNewValue();
			updateProfile(lineId);
		}
		else if (Properties.STRUCTURE_REMOVED.equals(evt.getPropertyName()))
		{
			int lineId = (Integer) evt.getNewValue();
			removeProfile(lineId);
		}
		else if (Properties.ALL_STRUCTURES_REMOVED.equals(evt.getPropertyName()))
		{
			((XYSeriesCollection) valueDistanceDataset).removeAllSeries();
		}
		else if (Properties.COLOR_CHANGED.equals(evt.getPropertyName()))
		{
			int lineId = (Integer) evt.getNewValue();
			setSeriesColor(lineId);
		}
		else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			if (smallBodyModel.getColoringIndex() != coloringIndex)
			{
				setColoringIndex(smallBodyModel.getColoringIndex());
			}
			else
			{
				updateAllProfiles();
			}
		}
	}
}
