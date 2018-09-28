package edu.jhuapl.saavtk.gui.coloringData;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.Statistics;
import org.jfree.ui.RectangleEdge;

import edu.jhuapl.saavtk.model.FacetColoringData;

public class ColoringInfoPanel extends JPanel 
{
	FacetColoringData[] data;
	JPanel histogramPane;

	public ColoringInfoPanel(FacetColoringData[] data) throws IOException {
		this.data = data;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JComboBox<String> comboBox = new JComboBox<String>();
		DefaultComboBoxModel<String> sourceComboBoxModel = new DefaultComboBoxModel<String>(data[0].getAvailable1DColoringNames());
		comboBox.setModel(sourceComboBoxModel);
		
		comboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) 
            {
//            	String coloringName = comboBox.getSelectedItem().toString();
            	int index = comboBox.getSelectedIndex();
            	try 
            	{
            		remove(histogramPane);
            		String coloringName = data[0].getAvailable1DColoringNames()[index];
					histogramPane = setupHistogramPanel(coloringName, data[0].getAvailable1DColoringNameUnits()[index], coloringName, "Count");
					add(histogramPane);
            		
            		revalidate();
				} 
            	catch (IOException e) 
            	{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
		
		add(comboBox);
		histogramPane = new JPanel();
		
		String datasetName = data[0].getAvailable1DColoringNames()[0] ;
		String datasetNameUnits = data[0].getAvailable1DColoringNameUnits()[0];
		histogramPane = setupHistogramPanel(datasetName, datasetNameUnits, datasetName, "Count");
		
		add(histogramPane);
		setVisible(true);

	}

	private JPanel setupHistogramPanel(String coloringName, String coloringUnits, String xlabel, String ylabel) throws IOException 
	{
		double[] coloringData = new double[this.data.length]; //[this.data[0].getColoringValuesFor(coloringName).length];
		int i=0;
		for (FacetColoringData data : this.data)
		{
			coloringData[i++] = data.getColoringValuesFor(coloringName)[0];
		}
		
		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries(coloringName, coloringData, 20);

		JFreeChart chart = ChartFactory.createHistogram(null, xlabel + " (" + coloringUnits + ")", ylabel, dataset, PlotOrientation.VERTICAL, true,
				true, false);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseWheelEnabled(true);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		plot.getRenderer().setSeriesPaint(0, Color.BLACK); // change default bar color to black
		StandardXYBarPainter painter = new StandardXYBarPainter() // disable gradient rendering (by instantiating a
																	// standard painter) and disable shadow rendering
																	// (by overriding the method below)
		{
			@Override
			public void paintBarShadow(Graphics2D arg0, XYBarRenderer arg1, int arg2, int arg3, RectangularShape arg4,
					RectangleEdge arg5, boolean arg6) {
			}
		};
		((XYBarRenderer) plot.getRenderer()).setBarPainter(painter);

		Vector<Number> coloringNumbers = new Vector<Number>();
		for (double d : coloringData)
		{
			coloringNumbers.add(d);
		}
		Number[] coloringNumbersArray = new Number[coloringNumbers.size()];
		coloringNumbers.toArray(coloringNumbersArray);
		
		Object[][] data = new Object[4][2];
		data[0][0] = "Mean";
		data[1][0] = "Standard Deviation";
		data[2][0] = "Minimum";
		data[3][0] = "Maximum";
		data[0][1] = Statistics.calculateMean(coloringNumbers);
		data[1][1] = Statistics.getStdDev(coloringNumbersArray);
		data[2][1] = Collections.min(coloringNumbers, new Comparator<Number>() {

			@Override
			public int compare(Number o1, Number o2) {
				return Double.compare(o1.doubleValue(), o2.doubleValue());
			}
		});
		data[3][1] = Collections.max(coloringNumbers, new Comparator<Number>() {

			@Override
			public int compare(Number o1, Number o2) {
				return Double.compare(o1.doubleValue(), o2.doubleValue());
			}
		});

		String[] columns = new String[] { "Property", "Value" };

		JTable table = new JTable(data, columns) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setBorder(BorderFactory.createTitledBorder(""));
		table.setPreferredScrollableViewportSize(new Dimension(500, 130));
		JScrollPane scrollPane = new JScrollPane(table);

		JPanel momentsPanel = new JPanel();
		momentsPanel.setLayout(new BoxLayout(momentsPanel, BoxLayout.PAGE_AXIS));
		momentsPanel.add(Box.createVerticalStrut(10));
		momentsPanel.add(scrollPane);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(chartPanel, BorderLayout.CENTER);
		panel.add(momentsPanel, BorderLayout.EAST);

		return panel;
	}
}
