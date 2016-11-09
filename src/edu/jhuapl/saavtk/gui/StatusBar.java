package edu.jhuapl.saavtk.gui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class StatusBar extends JPanel
{
    private JLabel leftLabel;
    private JEditorPane leftEditorPane;
    private JLabel rightLabel;
    private boolean selectableLeftLabel;

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
        if (text.length() == 0)
            text = " ";
        rightLabel.setText(text);
    }
}
