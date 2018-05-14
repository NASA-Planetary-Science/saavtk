package edu.jhuapl.saavtk2.table.search;

import java.awt.BorderLayout;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class BasicFrame extends JFrame {
	
	public BasicFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		setSize(600,600);
		setVisible(true);
	}
}
