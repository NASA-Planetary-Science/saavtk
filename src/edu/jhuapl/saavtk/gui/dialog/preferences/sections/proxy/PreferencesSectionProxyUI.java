package edu.jhuapl.saavtk.gui.dialog.preferences.sections.proxy;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

public class PreferencesSectionProxyUI extends JPanel
{
    private JPanel proxyTitlePanel;
    private JPanel proxyHostPanel;
    private JPanel proxyPortPanel;
    private JTextField proxyHostTextField;
    private JTextField proxyPortTextField;
    private JCheckBox proxyEnableCheckBox;
    private JLabel jLabel23;
    private JLabel jLabel24;
    private JLabel jLabel25;
    private JSeparator jSeparator10;

    public PreferencesSectionProxyUI()
	{
		initGUI();
	}
    
    
    private void initGUI() 
    {
    	setLayout(new GridBagLayout());
        proxyTitlePanel = new JPanel();
        proxyHostPanel = new JPanel();
        proxyPortPanel = new JPanel();
        jLabel23 = new JLabel();
        jSeparator10 = new JSeparator();
        jLabel24 = new JLabel();
        jLabel25 = new JLabel();
        proxyHostTextField = new JTextField(30);
        proxyPortTextField = new JTextField(30);
        proxyEnableCheckBox = new JCheckBox();
    	
    	GridBagConstraints gridBagConstraints = new GridBagConstraints();
    	// Start of configure proxy      
        proxyTitlePanel.setLayout(new java.awt.GridBagLayout());
        
        jLabel23.setText("Configure Proxy");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        proxyTitlePanel.add(jLabel23, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        proxyTitlePanel.add(jSeparator10, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 36;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 5, 0);
        add(proxyTitlePanel, gridBagConstraints);
        
        proxyHostPanel.setLayout(new java.awt.GridBagLayout());
        
        jLabel24.setText("http.proxyHost");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        proxyHostPanel.add(jLabel24, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        proxyHostPanel.add(proxyHostTextField);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 37;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(proxyHostPanel, gridBagConstraints);
        
        jLabel25.setText("http.proxyPort");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        proxyPortPanel.add(jLabel25);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        proxyPortPanel.add(proxyPortTextField);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 38;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(proxyPortPanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
		add(getProxyEnableCheckBox(), gridBagConstraints);
        
//		add(sectionPanel);
        // End of configure proxy
    }


	public JPanel getProxyTitlePanel()
	{
		return proxyTitlePanel;
	}


	public JPanel getProxyHostPanel()
	{
		return proxyHostPanel;
	}


	public JPanel getProxyPortPanel()
	{
		return proxyPortPanel;
	}


	public JTextField getProxyHostTextField()
	{
		return proxyHostTextField;
	}


	public JTextField getProxyPortTextField()
	{
		return proxyPortTextField;
	}


	public JCheckBox getProxyEnableCheckBox()
	{
		return proxyEnableCheckBox;
	}
}
