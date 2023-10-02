package edu.jhuapl.saavtk.gui.dialog.preferences.sections.font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PreferencesSectionFontUI extends JPanel
{
    private JButton fontColorButton;
    private JLabel fontColorLabel;
    
    public PreferencesSectionFontUI()
	{
		initGUI();
	}
    
    private void initGUI()
    {
        fontColorLabel = new JLabel();
        fontColorButton = new JButton();
    }

	public JLabel getFontColorLabel()
	{
		return fontColorLabel;
	}

	public void setFontColorLabel(JLabel fontColorLabel)
	{
		this.fontColorLabel = fontColorLabel;
	}
}
