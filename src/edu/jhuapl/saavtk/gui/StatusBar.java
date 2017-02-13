package edu.jhuapl.saavtk.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import vtk.vtkProp;
import edu.jhuapl.saavtk.model.Renderable;
import edu.jhuapl.saavtk.util.Properties;

public class StatusBar extends JPanel implements PropertyChangeListener
{
    private JLabel leftLabel;
    private JEditorPane leftEditorPane;
    private JLabel rightLabel;
    private boolean selectableLeftLabel;
    
    private Renderable leftModel = null;
    private vtkProp leftProp;
    private int leftCellId;
    private double[] leftPickPosition;
    
    private Renderable rightModel = null;
    private vtkProp rightProp;
    private int rightCellId;
    private double[] rightPickPosition;

    public StatusBar()
    {
    	this(true);
    }
    
    public StatusBar(boolean selectableLeftLabel)
    {
    	this.selectableLeftLabel = selectableLeftLabel;
        setLayout(new BorderLayout());
        Font font = UIManager.getFont("Label.font");
        font = new Font("Monospaced", Font.PLAIN, 13);
        // The following snippet was taken from https://explodingpixels.wordpress.com/2008/10/28/make-jeditorpane-use-the-system-font/
        // which shows how to make a JEditorPane behave look like a JLabel but still be selectable.
        if (selectableLeftLabel)
        {
        	 leftEditorPane = new JEditorPane(new HTMLEditorKit().getContentType(), "");
             leftEditorPane.setBorder(null);
             leftEditorPane.setOpaque(false);
             leftEditorPane.setEditable(false);
             leftEditorPane.setForeground(UIManager.getColor("Label.foreground"));
             
             // add a CSS rule to force body tags to use the default label font
             // instead of the value in javax.swing.text.html.default.csss
             
             String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                     "font-size: " + font.getSize() + "pt; }";
             ((HTMLDocument)leftEditorPane.getDocument()).getStyleSheet().addRule(bodyRule);
             add(leftEditorPane, BorderLayout.CENTER);
        }
        else
        {
        	 leftLabel = new JLabel(" ", SwingConstants.LEFT);
             leftLabel.setBorder(null);
             leftLabel.setOpaque(false);
             leftLabel.setForeground(UIManager.getColor("Label.foreground"));
             add(leftLabel, BorderLayout.CENTER);
        }
        
        
        rightLabel = new JLabel(" ", SwingConstants.RIGHT);
        add(rightLabel, BorderLayout.EAST);
        rightLabel.setFont(font);

        setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    public void setLeftText(String text)
    {
    	// Set the left text while removing any registered models
    	setLeftText(text, true);
    }
    
    private void setLeftText(String text, boolean removeModel)
    {
    	if(removeModel && leftModel != null)
    	{
    		leftModel.removePropertyChangeListener(this);
    		leftModel = null;
    	}
    	
        if (text.length() == 0)
            text = "Ready.";
//        System.out.println("StatusBar: setLeftText: left label is " + leftLabel + " and text is " + text);
        if (selectableLeftLabel)
        	leftEditorPane.setText(text);
        else
        	leftLabel.setText(text);
    }

    public void setRightText(String text)
    {
    	// Set the right text while removing any registered models
    	setRightText(text, true);
    }

    private void setRightText(String text, boolean removeModel)
    {
    	if(removeModel && rightModel != null)
    	{
    		rightModel.removePropertyChangeListener(this);
    		rightModel = null;
    	}
    	
        if (text.length() == 0)
            text = " ";
        rightLabel.setText(text);
    }
    
    // Sets up auto-refreshing left status text
    public void setLeftTextSource(Renderable model, vtkProp prop, int cellId, double[] pickPosition)
    {    	
    	// Determine if model has changed
    	boolean isNewModel = (leftModel != model);
    	if(isNewModel && leftModel != null)
    	{
    		// This status bar is no longer the property change listener for the old left model
    		leftModel.removePropertyChangeListener(this);
    	}

    	// Save references to arguments
    	leftModel = model;
    	leftProp = prop;
    	leftCellId = cellId;
    	leftPickPosition = pickPosition.clone();
    	if(isNewModel && leftModel != null)
    	{
        	// Set the status bar as the property change listener for the new left model
    		leftModel.addPropertyChangeListener(this);
    	}
    	
    	// Regenerate left status text
		setLeftText(leftModel.getClickStatusBarText(leftProp, leftCellId, leftPickPosition), false);
    }

    // Sets up auto-refreshing right status text
    public void setRightTextSource(Renderable model, vtkProp prop, int cellId, double[] pickPosition)
    {
	    // Determine if model has changed
    	boolean isNewModel = (rightModel != model);
    	if(isNewModel && rightModel != null)
    	{
    		// This status bar is no longer the property change listener for the old right model
    		rightModel.removePropertyChangeListener(this);
    	}

    	// Save references to arguments
    	rightModel = model;
    	rightProp = prop;
    	rightCellId = cellId;
    	rightPickPosition = pickPosition.clone();
    	if(isNewModel && rightModel != null)
    	{
        	// Set the status bar as the property change listener for the new right model
    		rightModel.addPropertyChangeListener(this);
    	}
    	
    	// Regenerate right status text
		setRightText(rightModel.getClickStatusBarText(rightProp, rightCellId, rightPickPosition), false);
    }

	@Override
	public void propertyChange(PropertyChangeEvent evt) 
	{
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
        	if(leftModel != null)
        	{
        		// Regenerate left status text
        		setLeftText(leftModel.getClickStatusBarText(leftProp, leftCellId, leftPickPosition), false);
        	}
        	
        	if(rightModel != null)
        	{
        		// Regenerate right status text
        		setRightText(rightModel.getClickStatusBarText(rightProp, rightCellId, rightPickPosition), false);        		
        	}
        }
    }
}
