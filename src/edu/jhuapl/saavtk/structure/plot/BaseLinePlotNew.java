package edu.jhuapl.saavtk.structure.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.util.LatLon;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * Class that provides base functionality for line plots. Plots will be
 * generated via the JFree chart library.
 * <p>
 * The following features is supported:
 * <ul>
 * <li>Event handling via {@link ItemEventListener} mechanism
 * <li>Management of items
 * <li>Support for item selection
 * <li>Support for automatic updating of plots when corresponding line changes.
 * <li>Configuration of various rendering properties
 * </ul>
 *
 * @author lopeznr1
 */
public abstract class BaseLinePlotNew implements ItemEventListener
{
	// Ref vars
	private final AnyStructureManager refManager;

	// State vars
	private final Map<PolyLine, LineState> plotM;
	private ImmutableSet<PolyLine> currPickS;

	private final XYSeriesCollection plotDataSet;

	/** Standard Constructor */
	public BaseLinePlotNew(AnyStructureManager aManager)
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
		// Retrieve the full list of PolyLines
		var itemL = StructureMiscUtil.getPathsFrom(refManager.getAllItems());
		var fullS = ImmutableSet.copyOf(itemL);
		var origS = ImmutableSet.copyOf(plotM.keySet());

		// Remove lines (no longer in use)
		var diffDelS = Sets.difference(origS, fullS);
		for (var aItem : diffDelS)
		{
			var tmpState = plotM.remove(aItem);
			plotDataSet.removeSeries(tmpState.refSeries);
		}

		// Add lines (newly created)
		var diffAddS = Sets.difference(fullS, origS);
		for (var aItem : diffAddS)
		{
			var tmpSeries = new XYSeries(aItem.hashCode());
			var tmpState = new LineState(aItem, tmpSeries);

			plotDataSet.addSeries(tmpSeries);
			plotM.put(aItem, tmpState);

			updateDrawFor(aItem);
			updatePlotFor(aItem);
		}

		// Update draw attributes for all (since they are based on index)
		for (var aItem : fullS)
			updateDrawFor(aItem);
	}

	/**
	 * Helper method that handles the ItemsMutated event.
	 */
	private void doHandleItemsMutated()
	{
		for (var aItem : plotM.keySet())
		{
			var tmpState = plotM.get(aItem);

			// Determine what aspects of the plot are stale
			var isDrawStale = aItem.getColor() != tmpState.cColor;
			var isPlotStale = aItem.getControlPoints() != tmpState.cControlPointL;
			var isDataStale = refManager.getXyzPointsFor(aItem) != tmpState.cXyzPointL;

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
		var newPickS = ImmutableSet.copyOf(StructureMiscUtil.getPathsFrom(refManager.getSelectedItems()));

		var diffS = Sets.symmetricDifference(currPickS, newPickS);
		for (var aItem : diffS)
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
		var tmpState = plotM.get(aItem);
		if (tmpState == null)
			return;

		var tmpChartPanel = getChartPanel();
		var tmpPlot = (XYPlot) tmpChartPanel.getChart().getPlot();

		// Retrieve the plots index into the data series
		int tmpIdx = plotDataSet.indexOf(tmpState.refSeries);

		var color = aItem.getColor();
		tmpPlot.getRenderer().setSeriesPaint(tmpIdx, color);

		var strokeW = 2.0f;
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
