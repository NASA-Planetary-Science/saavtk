package edu.jhuapl.saavtk.colormap;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

public class ColormapControllerWithContouring extends ColormapController implements ChangeListener
{

	JToggleButton showAsContourLinesButton=new JToggleButton("Enable Contours");
	JSpinner contourLineWidthSpinner=new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
	
	public ColormapControllerWithContouring()
	{
		super();
		
		JPanel subPanel=new JPanel(new GridLayout(1, 2));
		subPanel.add(new JLabel("Line width", JLabel.RIGHT));
		subPanel.add(contourLineWidthSpinner);
		contourLineWidthSpinner.addFocusListener(this);
		
		JPanel panel=new JPanel(new GridLayout(1, 2));
		panel.add(showAsContourLinesButton);
		panel.add(subPanel);
		this.add(panel,BorderLayout.SOUTH);
		
		contourLineWidthSpinner.setEnabled(false);
		contourLineWidthSpinner.addChangeListener(this);
		showAsContourLinesButton.addActionListener(this);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource().equals(showAsContourLinesButton))
		{
			contourLineWidthSpinner.setEnabled(showAsContourLinesButton.isSelected());
		}
		super.actionPerformed(e);
	}
	

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		showAsContourLinesButton.setEnabled(enabled);
		contourLineWidthSpinner.setEnabled(showAsContourLinesButton.isSelected());
	}
	
	public double getLineWidth()
	{
		return (Integer)contourLineWidthSpinner.getValue();
	}
	
	public boolean getContourLinesRequested()
	{
		return showAsContourLinesButton.isSelected();
	}
	
}
