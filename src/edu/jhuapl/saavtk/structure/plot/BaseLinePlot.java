package edu.jhuapl.saavtk.structure.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.util.LatLon;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * Class that provides base functionality for line plots. Plots will be
 * generated via the JFree chart library.
 * <P>
 * The following features is supported:
 * <UL>
 * <LI>Event handling via {@link ItemEventListener} mechanism
 * <LI>Management of items
 * <LI>Support for item selection
 * <LI>Support for automatic updating of plots when corresponding line changes.
 * <LI>Configuration of various rendering properties
 * </UL>
 *
 * Developers that extend this class are required to implement the following
 * methods:
 * <UL>
 * <LI>{@link #getChartPanel()}
 * <LI>{@link #getPlotPoints(Line, List, List)}
 * </UL>
 *
 * @author lopeznr1
 */
public abstract class BaseLinePlot implements ItemEventListener
{
	// Ref vars
	private final LineModel<PolyLine> refManager;

	// State vars
	private final Map<PolyLine, LineState> plotM;
	private ImmutableSet<PolyLine> currPickS;

	private final XYSeriesCollection plotDataSet;

	/**
	 * Standard Constructor
	 */
	public BaseLinePlot(LineModel<PolyLine> aManager)
	{
		refManager = aManager;

		plotM = new HashMap<>();
		currPickS = ImmutableSet.of();

		plotDataSet = new XYSeriesCollection();

		// Register for events of interest
		refManager.addListener(this);
	}

	/**
	 * Method that returns the associated {@link ChartPanel}.
	 */
	public abstract ChartPanel getChartPanel();

	/**
	 * Returns the values to be plotted for the given line.
	 * <P>
	 * This method is called when it is time for the plot to be updated.
	 *
	 * @param aItem
	 * @param xValueL The values associated with the x-axis
	 * @param yValueL The values associated with the y-axis
	 */
	protected abstract void getPlotPoints(PolyLine aItem, List<Double> xValueL, List<Double> yValueL);

	/**
	 * Returns the series associated with the specified line.
	 * <P>
	 * The returned object is managed by this class and should be used in a read
	 * only fashion.
	 */
	public XYSeries getSeriesFor(PolyLine aItem)
	{
		return plotM.get(aItem).refSeries;
	}

	/**
	 * Returns the associated JFree charts dataset.
	 * <P>
	 * The returned object is managed by this class and should be used in a read
	 * only fashion.
	 */
	public XYDataset getXYDataSet()
	{
		return plotDataSet;
	}

	/**
	 * Method to notify that all plots are stale.
	 */
	public void notifyAllStale()
	{
		for (PolyLine aItem : plotM.keySet())
			updatePlotFor(aItem);
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		if (aEventType == ItemEventType.ItemsChanged)
			doHandleItemsChanged();
		else if (aEventType == ItemEventType.ItemsMutated)
			doHandleItemsMutated();
		else if (aEventType == ItemEventType.ItemsSelected)
			doHandleItemsSelected();
	}

	/**
	 * Helper method that handles the ItemsChanged event.
	 */
	private void doHandleItemsChanged()
	{
		List<PolyLine> itemL = refManager.getAllItems();
		Set<PolyLine> fullS = ImmutableSet.copyOf(itemL);
		Set<PolyLine> origS = ImmutableSet.copyOf(plotM.keySet());

		// Remove lines (no longer in use)
		Set<PolyLine> diffDelS = Sets.difference(origS, fullS);
		for (PolyLine aItem : diffDelS)
		{
			LineState tmpState = plotM.remove(aItem);
			plotDataSet.removeSeries(tmpState.refSeries);
		}

		// Add lines (newly created)
		Set<PolyLine> diffAddS = Sets.difference(fullS, origS);
		for (PolyLine aItem : diffAddS)
		{
			XYSeries tmpSeries = new XYSeries(aItem.hashCode());
			LineState tmpState = new LineState(aItem, tmpSeries);

			plotDataSet.addSeries(tmpSeries);
			plotM.put(aItem, tmpState);

			updateDrawFor(aItem);
			updatePlotFor(aItem);
		}

		// Update draw attributes for all (since they are based on index)
		for (PolyLine aItem : fullS)
			updateDrawFor(aItem);
	}

	/**
	 * Helper method that handles the ItemsMutated event.
	 */
	private void doHandleItemsMutated()
	{
		for (PolyLine aItem : plotM.keySet())
		{
			LineState tmpState = plotM.get(aItem);

			// Determine what aspects of the plot are stale
			boolean isDrawStale = aItem.getColor() != tmpState.cColor;
			boolean isPlotStale = aItem.getControlPoints() != tmpState.cControlPointL;
			boolean isDataStale = refManager.getXyzPointsFor(aItem) != tmpState.cXyzPointL;

			// Update the actual plot state
			if (isDataStale == true)
			{
				tmpState = new LineState(aItem, tmpState.refSeries);
				plotM.put(aItem, tmpState);
			}

			// Update the draw attributes
			if (isDrawStale == true || isDataStale == true)
				updateDrawFor(aItem);

			// Update the plot points
			if (isPlotStale == true || isDataStale == true)
				updatePlotFor(aItem);
		}
	}

	/**
	 * Helper method that handles the ItemsSelected event.
	 */
	private void doHandleItemsSelected()
	{
		ImmutableSet<PolyLine> newPickS = refManager.getSelectedItems();

		Set<PolyLine> diffS = Sets.symmetricDifference(currPickS, newPickS);
		for (PolyLine aItem : diffS)
			updateDrawFor(aItem);

		currPickS = newPickS;
	}

	/**
	 * Helper method that will update the draw attributes associated with the
	 * specified line.
	 */
	private void updateDrawFor(PolyLine aItem)
	{
		// Bail if we are no longer tracking the item
		LineState tmpState = plotM.get(aItem);
		if (tmpState == null)
			return;

		ChartPanel tmpChartPanel = getChartPanel();
		XYPlot tmpPlot = (XYPlot) tmpChartPanel.getChart().getPlot();

		// Retrieve the plots index into the data series
		int tmpIdx = plotDataSet.indexOf(tmpState.refSeries);

		Color color = aItem.getColor();
		tmpPlot.getRenderer().setSeriesPaint(tmpIdx, color);

		float strokeW = 2.0f;
		if (refManager.getSelectedItems().contains(aItem) == true)
			strokeW = 4.0f;
		tmpPlot.getRenderer().setSeriesStroke(tmpIdx, new BasicStroke(strokeW));
	}

	/**
	 * Helper method that will update the plot associated with the specified line.
	 */
	private void updatePlotFor(PolyLine aItem)
	{
		// Retrieve values to plot
		List<Double> xValueL = new ArrayList<>();
		List<Double> yValueL = new ArrayList<>();
		getPlotPoints(aItem, xValueL, yValueL);

		// Update the plot
		LineState tmpState = plotM.get(aItem);
		XYSeries tmpSeries = tmpState.refSeries;

		tmpSeries.clear();
		for (int aIdx = 0; aIdx < yValueL.size(); aIdx++)
			tmpSeries.add(xValueL.get(aIdx), yValueL.get(aIdx), false);
		tmpSeries.fireSeriesChanged();
	}

	/**
	 * Private class used to keep track of state associated with a line's plot.
	 *
	 * @author lopeznr1
	 */
	class LineState
	{
		// Ref vars
		final XYSeries refSeries;

		// State vars
		final ImmutableList<LatLon> cControlPointL;
		final ImmutableList<Vector3D> cXyzPointL;
		final Color cColor;

		LineState(PolyLine aItem, XYSeries aSeries)
		{
			refSeries = aSeries;

			cControlPointL = aItem.getControlPoints();
			cXyzPointL = refManager.getXyzPointsFor(aItem);
			cColor = aItem.getColor();
		}

	}

}
