package edu.jhuapl.saavtk.colormap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.collect.Lists;

public class ColormapController extends JPanel implements ActionListener, FocusListener, ChangeListener
{
	private final List<Component> componentTracker;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private final List<ActionListener> actionListeners = Lists.newArrayList();
	public static final String colormapChanged = "Colormap changed";
	public static final String colormapRangeChanged = "Colormap range changed";

	private double defaultMin, defaultMax;

	private Colormap colormap;
	private final JComboBox<Colormap> colormapComboBox = new JComboBox<>();
	private final JCheckBox logScaleCheckbox = new JCheckBox("Log scale");
	private final JTextField lowTextField = new JTextField("0");
	private final JTextField highTextField = new JTextField("1");
	private final JTextField nLevelsTextField = new JTextField("32");
	private final JButton resetButton = new JButton("Range Reset");
	private final JSpinner nLabelsSpinner = new JSpinner(new SpinnerNumberModel(4, 0, 20, 1));
	private final JToggleButton syncButton = new JToggleButton("Sync");
	private final JButton refreshButton = new JButton("Refresh");

	private final JPanel panel1 = new JPanel();
	private final JPanel panel2r = new JPanel();
	private final JPanel panel3 = new JPanel();
	private final JPanel panel2l = new JPanel();
	private final JPanel panel2 = new JPanel();

	public ColormapController()
	{
		this.componentTracker = new ArrayList<>();
		setLayout(new BorderLayout());
		colormapComboBox.setRenderer(new ColormapComboBoxRenderer());
		for (String str : Colormaps.getAllBuiltInColormapNames())
		{
			Colormap cmap = Colormaps.getNewInstanceOfBuiltInColormap(str);
			colormapComboBox.addItem(cmap);
			if (cmap.getName().equals(Colormaps.getDefaultColormapName()))
				colormapComboBox.setSelectedItem(cmap);
		}
		//
		colormap = Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());
		panel1.add(track(colormapComboBox));
		//
		panel2r.setLayout(new GridLayout(4, 2));
		panel2r.add(track(new JLabel("Min Value", JLabel.RIGHT)));
		panel2r.add(track(lowTextField));
		panel2r.add(track(new JLabel("Max Value", JLabel.RIGHT)));
		panel2r.add(track(highTextField));
		panel2r.add(track(new JLabel("# Color Levels", JLabel.RIGHT)));
		panel2r.add(track(nLevelsTextField));
		panel2r.add(track(new JLabel("# Ticks", JLabel.RIGHT)));
		panel2r.add(track(nLabelsSpinner));
		//
		//
		panel3.setLayout(new GridLayout(2, 1));
		panel3.add(track(logScaleCheckbox));
		panel3.add(track(resetButton));

		//
		panel2l.setLayout(new GridLayout(2, 1));
		panel2l.add(track(syncButton));
		panel2l.add(track(refreshButton));

		panel2.setLayout(new FlowLayout());
		panel2.add(track(panel2l));
		panel2.add(track(panel2r));

		this.add(track(panel1), BorderLayout.NORTH);
		this.add(track(panel2), BorderLayout.CENTER);
		this.add(track(panel3), BorderLayout.EAST);
		//

		colormapComboBox.addActionListener(this);
		nLevelsTextField.addActionListener(this);
		lowTextField.addActionListener(this);
		highTextField.addActionListener(this);
		logScaleCheckbox.addActionListener(this);
		resetButton.addActionListener(this);
		refreshButton.addActionListener(this);

		lowTextField.addFocusListener(this);
		highTextField.addFocusListener(this);
		nLevelsTextField.addFocusListener(this);
		nLabelsSpinner.addFocusListener(this);

		nLabelsSpinner.addChangeListener(this);

		syncButton.setSelected(true);
		lowTextField.setAlignmentX(TextField.LEFT_ALIGNMENT);
		highTextField.setAlignmentX(TextField.LEFT_ALIGNMENT);

		setDefaultRange(0, 1);
		refresh();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		for (Component component : componentTracker)
		{
			component.setEnabled(enabled);
		}
	}

	public void setMinMax(double min, double max)
	{
		lowTextField.setText(String.valueOf(min));
		highTextField.setText(String.valueOf(max));
		refresh();
	}

	public double[] getMinMax()
	{
		return new double[] { Double.valueOf(lowTextField.getText()), Double.valueOf(highTextField.getText()) };
	}

	public Colormap getColormap()
	{
		return colormap;
	}

	public boolean isLogScale()
	{
		return logScaleCheckbox.isSelected();
	}

	protected class ColormapComboBoxRenderer extends JLabel implements ListCellRenderer<Colormap>
	{

		@Override
		public Component getListCellRendererComponent(JList<? extends Colormap> list, Colormap value, int index, boolean isSelected, boolean cellHasFocus)
		{
			if (isSelected)
			{
				setBackground(Color.DARK_GRAY);
				setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
			}
			else
			{
				setBackground(list.getBackground());
				setBorder(null);
			}

			setIcon(createIcon(value));
			setText(value.getName());
			return this;
		}

	}

	private static ImageIcon createIcon(Colormap cmap)
	{
		int w = 100;
		int h = 30;
		cmap.setRangeMin(0);
		cmap.setRangeMax(1);
		BufferedImage image = new BufferedImage(w, h, java.awt.color.ColorSpace.TYPE_RGB);
		for (int i = 0; i < w; i++)
		{
			double val = (double) i / (double) (image.getWidth() - 1);
			for (int j = 0; j < h; j++)
				image.setRGB(i, j, cmap.getColor(val).getRGB());
		}
		return new ImageIcon(image);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		for (int i = 0; i < actionListeners.size(); i++)
			actionListeners.get(i).actionPerformed(e);
		//
		if (syncButton.isSelected())
		{
			if (e.getSource().equals(lowTextField) || e.getSource().equals(highTextField) || e.getSource().equals(nLevelsTextField) || e.getSource().equals(nLabelsSpinner))
			{
				refresh();
				pcs.firePropertyChange(colormapRangeChanged, null, null);
			}
		}
		//
		if (e.getSource().equals(resetButton))
		{
			setMinMax(defaultMin, defaultMax);
		}
		if (syncButton.isSelected() || e.getSource().equals(refreshButton))
			refresh();
		if (e.getSource().equals(refreshButton))
			pcs.firePropertyChange(colormapRangeChanged, null, null);
		if (syncButton.isSelected())
		{
			if (e.getSource().equals(colormapComboBox))
				pcs.firePropertyChange(colormapChanged, null, null);
			else if (e.getSource().equals(resetButton))
				pcs.firePropertyChange(colormapRangeChanged, null, null);
		}
	}

	public void refresh()
	{
		if (colormap != null)
		{
			String name = ((Colormap) colormapComboBox.getSelectedItem()).getName();
			colormap = Colormaps.getNewInstanceOfBuiltInColormap(name);
			colormap.setLogScale(logScaleCheckbox.isSelected());
			colormap.setRangeMin(Double.valueOf(lowTextField.getText()));
			colormap.setRangeMax(Double.valueOf(highTextField.getText()));
			colormap.setNumberOfLevels(Integer.valueOf(nLevelsTextField.getText()));
			colormap.setNumberOfLabels((Integer) nLabelsSpinner.getValue());
		}
	}

	public void setDefaultRange(double min, double max)
	{
		this.defaultMin = min;
		this.defaultMax = max;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener l)
	{
		pcs.addPropertyChangeListener(l);

	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener l)
	{
		pcs.removePropertyChangeListener(l);
	}

	public void addActionListener(ActionListener l)
	{
		actionListeners.add(l);
	}

	public void removeActionListener(ActionListener l)
	{
		actionListeners.remove(l);
	}

	@Override
	public void focusGained(@SuppressWarnings("unused") FocusEvent e)
	{

	}

	@Override
	public void focusLost(FocusEvent e)
	{
		if (!syncButton.isSelected())
			return;
		//
		if (e.getSource().equals(lowTextField) | e.getSource().equals(highTextField))
			pcs.firePropertyChange(colormapRangeChanged, null, null);
		else
			pcs.firePropertyChange(colormapChanged, null, null);
		refresh();
	}

	@Override
	public void stateChanged(@SuppressWarnings("unused") ChangeEvent e)
	{
		if (!syncButton.isSelected())
			return;
		//
		colormap.setNumberOfLabels((Integer) nLabelsSpinner.getValue());
		pcs.firePropertyChange(colormapChanged, null, null);
	}

	private Component track(Component c)
	{
		componentTracker.add(c);
		return c;
	}
}
