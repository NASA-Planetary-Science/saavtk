package edu.jhuapl.saavtk.colormap;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

public class ColormapControllerWithContouring extends ColormapController
{

	JToggleButton showAsContourLinesButton=new JToggleButton("Enable Contours");
	JTextField contourLineWidthTextBox=new JTextField();
	
	public ColormapControllerWithContouring()
	{
		super();
		
		JPanel subPanel=new JPanel(new GridLayout(1, 2));
		subPanel.add(new JLabel("Line width", JLabel.RIGHT));
		subPanel.add(contourLineWidthTextBox);
		contourLineWidthTextBox.addFocusListener(this);
		
		JPanel panel=new JPanel(new GridLayout(1, 2));
		panel.add(subPanel);
		panel.add(showAsContourLinesButton);
		this.add(panel,BorderLayout.SOUTH);
		
		contourLineWidthTextBox.setText("1");
		
		showAsContourLinesButton.addActionListener(this);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource().equals(showAsContourLinesButton))
		{
			contourLineWidthTextBox.setEnabled(showAsContourLinesButton.isSelected());
			
		}
		super.actionPerformed(e);
	}
	

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		contourLineWidthTextBox.setEnabled(enabled);
		showAsContourLinesButton.setEnabled(enabled);
	}
	
	public double getLineWidth()
	{
		if (contourLineWidthTextBox.getText().isEmpty())
			return 1;
		else
			return Double.valueOf(contourLineWidthTextBox.getText());
	}
	
	public boolean getContourLinesRequested()
	{
		return showAsContourLinesButton.isSelected();
	}
	
}
