/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.saavtk.gui.dialog;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.TableHDU;

@SuppressWarnings("serial")
public class CustomPlateDataImporterDialog extends javax.swing.JDialog
{
	private boolean okayPressed = false;
	private final int numCells;
	private boolean isEditMode;
	private static final String LEAVE_UNMODIFIED = "<leave unmodified or empty to use existing plate data>";
	private String origColoringFile; // used in Edit mode only to store original filename

	/** Creates new form ShapeModelImporterDialog */
	public CustomPlateDataImporterDialog(java.awt.Window parent, boolean isEditMode, int numCells)
	{
		super(parent, "Import Plate Data", Dialog.ModalityType.DOCUMENT_MODAL);
		initComponents();
		this.isEditMode = isEditMode;
		this.numCells = numCells;
	}

	/**
	 * Set the cell data info
	 */
	public void setColoringData(ColoringData data)
	{
		if (isEditMode)
		{
			cellDataPathTextField.setText(LEAVE_UNMODIFIED);
			origColoringFile = data.getFileName();
		}

		nameTextField.setText(data.getName());
		unitsTextField.setText(data.getUnits());
		hasNullsCheckBox.setSelected(data.hasNulls());
	}

	/**
	 * @return
	 */
	public ColoringData getColoringData()
	{
		String errorString = validateInput();
		if (errorString != null)
		{
			throw new RuntimeException(errorString);
		}

		String fileName = cellDataPathTextField.getText();

		if (isEditMode && (LEAVE_UNMODIFIED.equals(fileName) || fileName == null || fileName.isEmpty()))
			fileName = origColoringFile;

		return ColoringData.of(nameTextField.getText(), fileName, ImmutableList.of(), unitsTextField.getText(), numCells, hasNullsCheckBox.isSelected());
	}

	private String validateInput()
	{
		String result = null;

		String cellDataPath = cellDataPathTextField.getText();
		if (cellDataPath == null)
			cellDataPath = "";

		if (!isEditMode || (!cellDataPath.isEmpty() && !cellDataPath.equals(LEAVE_UNMODIFIED)))
		{
			if (cellDataPath.isEmpty())
				return "Please enter the path to the plate data file.";

			File file = new File(cellDataPath);
			if (!file.exists() || !file.canRead() || !file.isFile())
				return cellDataPath + " does not exist or is not readable.";

			if (cellDataPath.contains(","))
				return "Plate data path may not contain commas.";

			if (cellDataPath.toLowerCase().endsWith(".fit") || cellDataPath.toLowerCase().endsWith(".fits"))
				result = validateFitsFile(cellDataPath);
			else
				result = validateTxtFile(cellDataPath);

			if (result != null)
				return result;
		}

		String name = nameTextField.getText();
		if (name == null)
			name = "";
		name = name.trim();
		nameTextField.setText(name);
		if (name.isEmpty())
			return "Please enter a name for the plate data.";

		String units = unitsTextField.getText();
		if (units == null)
			units = "";
		units = units.trim();
		unitsTextField.setText(units);
		if (name.contains(",") || units.contains(","))
			return "Fields may not contain commas.";

		return null;
	}

	private String validateTxtFile(String cellDataPath)
	{
		InputStream fs;
		try
		{
			fs = new FileInputStream(cellDataPath);
		}
		catch (FileNotFoundException e)
		{
			return "The file '" + cellDataPath + "' does not exist or is not readable.";
		}

		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		String line;
		int lineCount = 0;
		try
		{
			while ((line = in.readLine()) != null)
			{
				Double.parseDouble(line);
				++lineCount;
			}

			in.close();
		}
		catch (NumberFormatException e)
		{
			return "Numbers in file '" + cellDataPath + "' are malformatted.";
		}
		catch (IOException e)
		{
			return "An error occurred reading the file '" + cellDataPath + "'.";
		}

		if (lineCount != numCells)
		{
			return "Number of lines in file '" + cellDataPath + "' must equal number of plates in shape model.";
		}

		return null;
	}

	protected String validateFitsFile(String filename)
	{
		String result = null;

		try (Fits fits = new Fits(filename))
		{
			BasicHDU<?>[] hdus = fits.read();
			if (hdus.length < 2)
			{
				return "FITS ancillary file has too few HDUs";
			}

			if (hdus[1] instanceof TableHDU)
			{
				TableHDU<?> athdu = (TableHDU<?>) hdus[1];
				int ncols = athdu.getNCols();
				if (ncols <= PolyhedralModel.FITS_SCALAR_COLUMN_INDEX)
					return "FITS ancillary table has too few olumns";
			}
			else
			{
				return "FITS ancillary file doesn't have a table HDU";
			}

		}
		catch (@SuppressWarnings("unused") IOException | FitsException e)
		{
			return "Error reading FITS ancillary file";
		}

		return result;
	}

	public boolean getOkayPressed()
	{
		return okayPressed;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private final void initComponents()
	{
		java.awt.GridBagConstraints gridBagConstraints;

		pathLabel2 = new javax.swing.JLabel();
		cellDataPathTextField = new javax.swing.JTextField();
		browsePlateDataButton = new javax.swing.JButton();
		nameLabel = new javax.swing.JLabel();
		unitsLabel = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		cancelButton = new javax.swing.JButton();
		okButton = new javax.swing.JButton();
		nameTextField = new javax.swing.JTextField();
		unitsTextField = new javax.swing.JTextField();
		hasNullsCheckBox = new javax.swing.JCheckBox();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new java.awt.Dimension(600, 167));
		setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new java.awt.GridBagLayout());

		pathLabel2.setText("Path");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
		getContentPane().add(pathLabel2, gridBagConstraints);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
		getContentPane().add(cellDataPathTextField, gridBagConstraints);

		browsePlateDataButton.setText("Browse...");
		browsePlateDataButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				browsePlateDataButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
		getContentPane().add(browsePlateDataButton, gridBagConstraints);

		nameLabel.setText("Name");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
		getContentPane().add(nameLabel, gridBagConstraints);

		unitsLabel.setText("Units (optional)");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
		getContentPane().add(unitsLabel, gridBagConstraints);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				cancelButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
		jPanel1.add(cancelButton, gridBagConstraints);

		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				okButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		jPanel1.add(okButton, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
		getContentPane().add(jPanel1, gridBagConstraints);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
		getContentPane().add(nameTextField, gridBagConstraints);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
		getContentPane().add(unitsTextField, gridBagConstraints);

		hasNullsCheckBox.setText("Contains Invalid Data");
		hasNullsCheckBox.setToolTipText("If checked, then the smallest value in the file is assumed to represent invalid data and is not displayed on the shape model.");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
		getContentPane().add(hasNullsCheckBox, gridBagConstraints);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void browsePlateDataButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browsePlateDataButtonActionPerformed
	{//GEN-HEADEREND:event_browsePlateDataButtonActionPerformed
		File file = CustomFileChooser.showOpenDialog(this, "Select Plate Data");
		if (file == null)
		{
			return;
		}

		String filename = file.getAbsolutePath();
		cellDataPathTextField.setText(filename);
	}//GEN-LAST:event_browsePlateDataButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
	{//GEN-HEADEREND:event_cancelButtonActionPerformed
		setVisible(false);
	}//GEN-LAST:event_cancelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
	{//GEN-HEADEREND:event_okButtonActionPerformed
		String errorString = validateInput();
		if (errorString != null)
		{
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), errorString, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		okayPressed = true;
		setVisible(false);
	}//GEN-LAST:event_okButtonActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton browsePlateDataButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JTextField cellDataPathTextField;
	private javax.swing.JCheckBox hasNullsCheckBox;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JLabel pathLabel2;
	private javax.swing.JLabel unitsLabel;
	private javax.swing.JTextField unitsTextField;
	// End of variables declaration//GEN-END:variables
}
