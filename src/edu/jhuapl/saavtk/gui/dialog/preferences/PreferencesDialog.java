/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SettingsDialog.java
 *
 * Created on Mar 27, 2012, 9:37:47 PM
 */
package edu.jhuapl.saavtk.gui.dialog.preferences;

import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class PreferencesDialog extends JDialog
{
    private JButton applyButton;
   
    private JButton closeButton;
    private JPanel buttonsPanel;

    private JPanel mainPanel;
    private JPanel sectionPanel;
    private JPanel prefPanel;
    private JPanel sideListPanel;

    private JScrollPane jScrollPane1;

	private JList<String> sectionList;
    private String[] prefSectionNames = {};
    private IPreferencesController selectedController;
    private CardLayout cardLayout = new CardLayout();
    private List<IPreferencesController> controllers;

    public PreferencesDialog(Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
        setTitle("Settings");
    }
    
    public void setPreferenceSections(List<IPreferencesController> controllers)
    {
    	this.controllers = controllers;
    	prefSectionNames = controllers.stream().map( contr -> contr.getPreferenceName()).toArray(String[] :: new);
    	DefaultListModel<String> listModel = new DefaultListModel<String>();
    	for (String name : prefSectionNames) listModel.addElement(name);
    	sectionList.setModel(listModel);
    	
    	for (IPreferencesController controller : controllers)
    	{
    		prefPanel.add(controller.getView(), controller.getPreferenceName());
    	}
    	
    	sectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionList.setSelectedIndex(0);
    }
    
    public IPreferencesController getSelectedController()
    {
    	return selectedController;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents()
    {
        GridBagConstraints gridBagConstraints;
        
//        displayedSection = "Pick Tolerance";

        jScrollPane1 = new JScrollPane();
        mainPanel = new JPanel();
        sectionPanel = new JPanel();
        prefPanel = new JPanel();
        buttonsPanel = new JPanel();
        applyButton = new JButton();
        closeButton = new JButton();
               
        sideListPanel = new JPanel();
       
        sectionList = new JList<String>(prefSectionNames);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridLayout());
        
        NumberFormat dimsFormat = NumberFormat.getIntegerInstance();
        dimsFormat.setGroupingUsed(false);

        mainPanel.setLayout(new GridBagLayout());

        sectionPanel.setLayout(new GridBagLayout());
        prefPanel.setLayout(cardLayout);

        sideListPanel.setLayout(new GridBagLayout());
        
     // Adds preference section labels to left side menu
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        sectionList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				String section = sectionList.getSelectedValue();
				cardLayout.show(prefPanel, section);
				selectedController = PreferencesDialog.this.controllers.stream().filter(cont -> cont.getPreferenceName().equals(section)).collect(Collectors.toList()).get(0);
			}
        });


        sideListPanel.add(sectionList, gridBagConstraints);
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(sideListPanel, gridBagConstraints);
        
        // Beginning of buttons section at bottom of preference changes section panel
        buttonsPanel.setLayout(new GridBagLayout());

        applyButton.setText("Apply & Save Settings");
       
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        buttonsPanel.add(applyButton, gridBagConstraints);

        closeButton.setText("Close");
       
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        buttonsPanel.add(closeButton, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
//        gridBagConstraints.insets = new Insets(15, 0, 0, 0);
        sectionPanel.add(prefPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 43;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(15, 0, 0, 0);
        sectionPanel.add(buttonsPanel, gridBagConstraints);

        // Adds the section panel to the full-dialog main panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 0, 5, 10);
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        mainPanel.add(sectionPanel, gridBagConstraints);
        
        jScrollPane1.setViewportView(mainPanel);
        getContentPane().add(jScrollPane1);

        pack();
        
        setSize(800, 300);
        
        
    }

	public JButton getApplyButton()
	{
		return applyButton;
	}

	public JButton getCloseButton()
	{
		return closeButton;
	}
}
