/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RadialOffsetChanger.java
 *
 * Created on Apr 5, 2011, 4:36:57 PM
 */

package edu.jhuapl.saavtk.gui;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.Model;

public class RadialOffsetChanger extends JPanel
{
	private Model model;
	private double offsetScale = 0.025;
	private int defaultValue = 15;

	/** Creates new form RadialOffsetChanger */
	public RadialOffsetChanger()
	{
		initComponents();
		reset();
	}

	public void setModel(Model model)
	{
		this.model = model;
	}

	public void reset()
	{
		slider.setValue(defaultValue);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{

		slider = new javax.swing.JSlider();
		jLabel1 = new javax.swing.JLabel();
		jSeparator1 = new javax.swing.JSeparator();
		resetButton = new javax.swing.JButton();

		setPreferredSize(new java.awt.Dimension(300, 87));

		slider.setMajorTickSpacing(5);
		slider.setMaximum(30);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);
		slider.setValue(15);
		slider.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt)
			{
				sliderStateChanged(evt);
			}
		});

		jLabel1.setText("Radial Offset");

		resetButton.setText("Reset");
		resetButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				resetButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(slider, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(resetButton))
						.addGroup(layout.createSequentialGroup().addComponent(jLabel1)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)))
				.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(15, 15, 15)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jSeparator1,
										javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup().addGap(3, 3, 3).addComponent(resetButton))
								.addGroup(layout.createSequentialGroup().addGap(6, 6, 6).addComponent(slider,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(16, Short.MAX_VALUE)));
	}// </editor-fold>//GEN-END:initComponents

	private void sliderStateChanged(javax.swing.event.ChangeEvent evt)
	{// GEN-FIRST:event_sliderStateChanged

		if (!slider.getValueIsAdjusting())
		{
			double offset = getOffset();
			model.setOffset(offset);
		}
	}// GEN-LAST:event_sliderStateChanged

	private void resetButtonActionPerformed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_resetButtonActionPerformed
		reset();
	}// GEN-LAST:event_resetButtonActionPerformed

	public void setOffsetScale(double scale)
	{
		this.offsetScale = scale;
	}

	/**
	 * Returns the installed offset value.
	 */
	public double getOffset()
	{
		int val = slider.getValue();
		int max = slider.getMaximum();
		int min = slider.getMinimum();
		double offset = (val - (max - min) / 2.0) * offsetScale;
		return offset;
	}

	/**
	 * Sets in the installed offset value.
	 */
	public void setOffset(double aValue)
	{
		int max = slider.getMaximum();
		int min = slider.getMinimum();
		int val = (int) (aValue / offsetScale + ((max - min) / 2.0));
		slider.setValue(val);
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel jLabel1;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JButton resetButton;
	private javax.swing.JSlider slider;
	// End of variables declaration//GEN-END:variables

}
