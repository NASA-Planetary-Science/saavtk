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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
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
    PropertyChangeSupport pcs=new PropertyChangeSupport(this);
    List<ActionListener> actionListeners=Lists.newArrayList();
    public static final String colormapChanged="Colormap changed";
    public static final String colormapRangeChanged="Colormap range changed";

    double defaultMin,defaultMax;
    
    Colormap colormap=Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());
    JComboBox colormapComboBox=new JComboBox<>();
    JCheckBox logScaleCheckbox=new JCheckBox("Log scale");
    JTextField lowTextField=new JTextField("0");
    JTextField highTextField=new JTextField("1");
    JTextField nLevelsTextField=new JTextField("32");
    JButton resetButton=new JButton("Range Reset");
    JSpinner nLabelsSpinner=new JSpinner(new SpinnerNumberModel(4, 0, 20, 1));
    JToggleButton syncButton=new JToggleButton("Sync");
    JButton refreshButton=new JButton("Refresh");
    
    JPanel panel1=new JPanel();
    JPanel panel2r=new JPanel();
    JPanel panel3=new JPanel();
    JPanel panel2l=new JPanel();
    JPanel panel2=new JPanel();
    
    public ColormapController()
    {
        setLayout(new BorderLayout());
        colormapComboBox.setRenderer(new ColormapComboBoxRenderer());
        for (String str : Colormaps.getAllBuiltInColormapNames())
        {
            Colormap cmap=Colormaps.getNewInstanceOfBuiltInColormap(str);
            colormapComboBox.addItem(cmap);
            if (cmap.getName().equals(Colormaps.getDefaultColormapName()))
                colormapComboBox.setSelectedItem(cmap);
        }
        //
        colormap=Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());
        panel1.add(colormapComboBox);
        //
        panel2r.setLayout(new GridLayout(4, 2));
        panel2r.add(new JLabel("Min Value", JLabel.RIGHT));
        panel2r.add(lowTextField);
        panel2r.add(new JLabel("Max Value", JLabel.RIGHT));
        panel2r.add(highTextField);
        panel2r.add(new JLabel("# Color Levels", JLabel.RIGHT));
        panel2r.add(nLevelsTextField);
        panel2r.add(new JLabel("# Ticks", JLabel.RIGHT));
        panel2r.add(nLabelsSpinner);
        //
        //
        panel3.setLayout(new GridLayout(2, 1));
        panel3.add(logScaleCheckbox);
        panel3.add(resetButton);
        
        //
        panel2l.setLayout(new GridLayout(2, 1));
        panel2l.add(syncButton);
        panel2l.add(refreshButton);
        
        panel2.setLayout(new FlowLayout());
        panel2.add(panel2l);
        panel2.add(panel2r);
        
        this.add(panel1,BorderLayout.NORTH);
        this.add(panel2,BorderLayout.CENTER);
        this.add(panel3,BorderLayout.EAST);
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
        colormapComboBox.setEnabled(enabled);
        lowTextField.setEnabled(enabled);
        highTextField.setEnabled(enabled);
        logScaleCheckbox.setEnabled(enabled);
        nLevelsTextField.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        syncButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
    }

    public void setMinMax(double min, double max)
    {
        lowTextField.setText(String.valueOf(min));
        highTextField.setText(String.valueOf(max));
        refresh();
    }
    
    public double[] getMinMax()
    {
    	return new double[]{Double.valueOf(lowTextField.getText()),Double.valueOf(highTextField.getText())};
    }

    public Colormap getColormap()
    {
        return colormap;
    }

    public boolean isLogScale()
    {
        return logScaleCheckbox.isSelected();
    }

    protected class ColormapComboBoxRenderer extends JLabel implements ListCellRenderer
    {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus)
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

            setIcon(createIcon((Colormap)value));
            setText(((Colormap)value).getName());
            return this;
        }

    }

    private static ImageIcon createIcon(Colormap cmap)
    {
        int w=100;
        int h=30;
        cmap.setRangeMin(0);
        cmap.setRangeMax(1);
        BufferedImage image=new BufferedImage(w, h, java.awt.color.ColorSpace.TYPE_RGB);
        for (int i=0; i<w; i++)
        {
            double val=(double)i/(double)(image.getWidth()-1);
            for (int j=0; j<h; j++)
                image.setRGB(i, j, cmap.getColor(val).getRGB());
        }
        return new ImageIcon(image);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
    	for (int i=0; i<actionListeners.size(); i++)
    		actionListeners.get(i).actionPerformed(e);
    	//
        if (e.getSource().equals(colormapComboBox))
        {
            String name=((Colormap)colormapComboBox.getSelectedItem()).getName();
            colormap=Colormaps.getNewInstanceOfBuiltInColormap(name);
        }
        if (e.getSource().equals(resetButton))
        {
        	setMinMax(defaultMin, defaultMax);
        }
        refresh();
        if (e.getSource().equals(colormapComboBox))
        	pcs.firePropertyChange(colormapChanged, null, null);
        else if (e.getSource().equals(resetButton) || e.getSource().equals(refreshButton))
        	pcs.firePropertyChange(colormapRangeChanged, null, null);
    }

    public void refresh()
    {
        if (colormap!=null)
        {
            colormap.setLogScale(logScaleCheckbox.isSelected());
            colormap.setRangeMin(Double.valueOf(lowTextField.getText()));
            colormap.setRangeMax(Double.valueOf(highTextField.getText()));
            colormap.setNumberOfLevels(Integer.valueOf(nLevelsTextField.getText()));
            colormap.setNumberOfLabels((Integer)nLabelsSpinner.getValue());
        }
    }
    
    public void setDefaultRange(double min, double max)
    {
    	this.defaultMin=min;
    	this.defaultMax=max;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);

    }

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
	public void focusGained(FocusEvent e)
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
	public void stateChanged(ChangeEvent e)
	{
		if (!syncButton.isSelected())
			return;
		//
		colormap.setNumberOfLabels((Integer)nLabelsSpinner.getValue());
        pcs.firePropertyChange(colormapChanged, null, null);
	}


}
