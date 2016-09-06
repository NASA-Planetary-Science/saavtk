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
    private JEditorPane leftLabel;
    private JLabel rightLabel;

    public StatusBar()
    {
        setLayout(new BorderLayout());

        // The following snippet was taken from https://explodingpixels.wordpress.com/2008/10/28/make-jeditorpane-use-the-system-font/
        // which shows how to make a JEditorPane behave look like a JLabel but still be selectable.
        leftLabel = new JEditorPane(new HTMLEditorKit().getContentType(), "");
        leftLabel.setBorder(null);
        leftLabel.setOpaque(false);
        leftLabel.setEditable(false);
        leftLabel.setForeground(UIManager.getColor("Label.foreground"));
        // add a CSS rule to force body tags to use the default label font
        // instead of the value in javax.swing.text.html.default.csss
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)leftLabel.getDocument()).getStyleSheet().addRule(bodyRule);

        add(leftLabel, BorderLayout.CENTER);
        rightLabel = new JLabel(" ", SwingConstants.RIGHT);
        add(rightLabel, BorderLayout.EAST);

        font = new Font("Monospaced", Font.PLAIN, 13);
        rightLabel.setFont(font);

        setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    public void setLeftText(String text)
    {
        if (text.length() == 0)
            text = " ";
        leftLabel.setText(text);
    }

    public void setRightText(String text)
    {
        if (text.length() == 0)
            text = " ";
        rightLabel.setText(text);
    }
}
