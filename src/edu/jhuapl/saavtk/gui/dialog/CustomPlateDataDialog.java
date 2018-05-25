/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomImageLoaderPanel.java
 *
 * Created on Jun 5, 2012, 3:56:56 PM
 */
package edu.jhuapl.saavtk.gui.dialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.CustomizableColoringDataManager;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.SafePaths;

public class CustomPlateDataDialog extends javax.swing.JDialog
{
	private final ModelManager modelManager;
	private final CustomizableColoringDataManager coloringDataManager;

	/** Creates new form CustomImageLoaderPanel */
	public CustomPlateDataDialog(ModelManager modelManager)
	{
		this.modelManager = modelManager;
		this.coloringDataManager = modelManager.getPolyhedralModel().getColoringDataManager();

		initComponents();

		DefaultListModel<ColoringData> model = new DefaultListModel<>();
		cellDataList.setModel(model);

		initializeList(model);

		pack();
	}

	private final void initializeList(DefaultListModel<ColoringData> model)
	{
		int resolution = modelManager.getPolyhedralModel().getModelResolution();
		int numberElements = coloringDataManager.getResolutions().get(resolution);
		for (ColoringData data : coloringDataManager.get(numberElements))
		{
			model.addElement(data);
		}
	}

	private String getCustomDataFolder()
	{
		return modelManager.getPolyhedralModel().getCustomDataFolder();
	}

	private String getConfigFilename()
	{
		return modelManager.getPolyhedralModel().getConfigFilename();
	}

	private void updateConfigFile()
	{
		MapUtil configMap = new MapUtil(getConfigFilename());

		// Load in the plate data
		String cellDataFilenames = "";
		String cellDataNames = "";
		String cellDataUnits = "";
		String cellDataHasNulls = "";
		String cellDataResolutionLevels = "";

		// We need to make sure to save out data from other resolutions without modification.
		int resolutionLevel = modelManager.getPolyhedralModel().getModelResolution();
		if (configMap.containsKey(PolyhedralModel.CELL_DATA_FILENAMES) && configMap.containsKey(PolyhedralModel.CELL_DATA_NAMES) && configMap.containsKey(PolyhedralModel.CELL_DATA_UNITS) && configMap.containsKey(PolyhedralModel.CELL_DATA_HAS_NULLS) && configMap.containsKey(PolyhedralModel.CELL_DATA_RESOLUTION_LEVEL))
		{
			String[] cellDataFilenamesArr = configMap.get(PolyhedralModel.CELL_DATA_FILENAMES).split(",", -1);
			String[] cellDataNamesArr = configMap.get(PolyhedralModel.CELL_DATA_NAMES).split(",", -1);
			String[] cellDataUnitsArr = configMap.get(PolyhedralModel.CELL_DATA_UNITS).split(",", -1);
			String[] cellDataHasNullsArr = configMap.get(PolyhedralModel.CELL_DATA_HAS_NULLS).split(",", -1);
			String[] cellDataResolutionLevelsArr = configMap.get(PolyhedralModel.CELL_DATA_RESOLUTION_LEVEL).split(",", -1);

			for (int i = 0; i < cellDataFilenamesArr.length; ++i)
			{
				if (!cellDataResolutionLevelsArr[i].trim().isEmpty() && Integer.parseInt(cellDataResolutionLevelsArr[i]) != resolutionLevel)
				{
					if (!cellDataFilenames.isEmpty())
					{
						cellDataFilenames += PolyhedralModel.LIST_SEPARATOR;
						cellDataNames += PolyhedralModel.LIST_SEPARATOR;
						cellDataUnits += PolyhedralModel.LIST_SEPARATOR;
						cellDataHasNulls += PolyhedralModel.LIST_SEPARATOR;
						cellDataResolutionLevels += PolyhedralModel.LIST_SEPARATOR;
					}
					cellDataFilenames += cellDataFilenamesArr[i];
					cellDataNames += cellDataNamesArr[i];
					cellDataUnits += cellDataUnitsArr[i];
					cellDataHasNulls += cellDataHasNullsArr[i];
					cellDataResolutionLevels += cellDataResolutionLevelsArr[i];
				}
			}
		}

		DefaultListModel<ColoringData> cellDataListModel = (DefaultListModel<ColoringData>) cellDataList.getModel();
		for (int i = 0; i < cellDataListModel.size(); ++i)
		{
			ColoringData coloringData = cellDataListModel.getElementAt(i);
			if (!coloringDataManager.isCustom(coloringData))
			{
				continue;
			}

			if (!cellDataFilenames.isEmpty())
			{
				cellDataFilenames += PolyhedralModel.LIST_SEPARATOR;
				cellDataNames += PolyhedralModel.LIST_SEPARATOR;
				cellDataUnits += PolyhedralModel.LIST_SEPARATOR;
				cellDataHasNulls += PolyhedralModel.LIST_SEPARATOR;
				cellDataResolutionLevels += PolyhedralModel.LIST_SEPARATOR;
			}

			cellDataFilenames += coloringData.getFileName().replaceFirst(".*/", "");
			cellDataNames += coloringData.getName();
			cellDataUnits += coloringData.getUnits();
			cellDataHasNulls += new Boolean(coloringData.hasNulls()).toString();
			cellDataResolutionLevels += new Integer(resolutionLevel).toString();
		}

		Map<String, String> newMap = new LinkedHashMap<>();

		newMap.put(PolyhedralModel.CELL_DATA_FILENAMES, cellDataFilenames);
		newMap.put(PolyhedralModel.CELL_DATA_NAMES, cellDataNames);
		newMap.put(PolyhedralModel.CELL_DATA_UNITS, cellDataUnits);
		newMap.put(PolyhedralModel.CELL_DATA_HAS_NULLS, cellDataHasNulls);
		newMap.put(PolyhedralModel.CELL_DATA_RESOLUTION_LEVEL, cellDataResolutionLevels);

		configMap.put(newMap);
	}

	private ColoringData copyCellData(int index, ColoringData source)
	{
		String sourceFilePath = source.getFileName();
		String sourceFileName = sourceFilePath.replaceFirst(".*[/\\\\]", "");
		String extension = sourceFileName.replaceFirst("[^\\.]*\\.", ".");
		String uuid = UUID.randomUUID().toString();
		String destFileName = "platedata-" + uuid + extension;
		String destFilePath = SafePaths.getString(getCustomDataFolder(), destFileName);

		// Copy the cell data file to the model directory
		try
		{
			FileUtil.copyFile(sourceFilePath, destFilePath);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), "An error occurred while trying to save the file " + source, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(e);
		}

		// After copying the file, convert the file path to a URL format.
		destFilePath = FileCache.FILE_PREFIX + getCustomDataFolder() + "/" + destFileName;
		ColoringData newColoringData = ColoringData.of(source.getName(), destFilePath, source.getElementNames(), source.getUnits(), source.getNumberElements(), source.hasNulls());

		DefaultListModel<ColoringData> model = (DefaultListModel<ColoringData>) cellDataList.getModel();
		if (index >= model.getSize())
		{
			model.addElement(newColoringData);
		}
		else
		{
			model.set(index, newColoringData);
		}

		return newColoringData;
	}

	private void removeCellData(int index)
	{
		try
		{
			DefaultListModel<ColoringData> model = (DefaultListModel<ColoringData>) cellDataList.getModel();
			ColoringData cellDataInfo = model.get(index);
			model.remove(index);
			coloringDataManager.removeCustom(cellDataInfo);

			Path fileName = SafePaths.get(getCustomDataFolder(), cellDataInfo.getFileName());
			Files.delete(fileName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{
		java.awt.GridBagConstraints gridBagConstraints;

		jScrollPane1 = new javax.swing.JScrollPane();
		cellDataList = new javax.swing.JList<>();
		newButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		editButton = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		closeButton = new javax.swing.JButton();

		setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new java.awt.GridBagLayout());

		cellDataList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			@Override
			public void valueChanged(javax.swing.event.ListSelectionEvent evt)
			{
				cellDataListValueChanged(evt);
			}
		});
		jScrollPane1.setViewportView(cellDataList);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.ipadx = 377;
		gridBagConstraints.ipady = 241;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(jScrollPane1, gridBagConstraints);

		newButton.setText("New...");
		newButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				newButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
		getContentPane().add(newButton, gridBagConstraints);

		deleteButton.setText("Remove");
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				deleteButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
		getContentPane().add(deleteButton, gridBagConstraints);

		editButton.setText("Edit...");
		editButton.setEnabled(false);
		editButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				editButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
		getContentPane().add(editButton, gridBagConstraints);

		jLabel1.setText("Plate Data");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
		getContentPane().add(jLabel1, gridBagConstraints);

		closeButton.setText("Close");
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				closeButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
		getContentPane().add(closeButton, gridBagConstraints);
	}// </editor-fold>//GEN-END:initComponents

	protected CustomPlateDataImporterDialog getPlateImporterDialog()
	{
		return new CustomPlateDataImporterDialog(JOptionPane.getFrameForComponent(this), false, modelManager.getPolyhedralModel().getSmallBodyPolyData().GetNumberOfCells());
	}

	private void newButtonActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_newButtonActionPerformed
		CustomPlateDataImporterDialog dialog = getPlateImporterDialog();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);

		// If user clicks okay add to list
		if (dialog.getOkayPressed())
		{
			ColoringData coloringData = copyCellData(cellDataList.getModel().getSize(), dialog.getColoringData());
			coloringDataManager.addCustom(coloringData);
			updateConfigFile();
		}
	}//GEN-LAST:event_newButtonActionPerformed

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_deleteButtonActionPerformed
		int selectedItem = cellDataList.getSelectedIndex();
		if (selectedItem >= 0)
		{
			removeCellData(selectedItem);
			updateConfigFile();
		}
	}//GEN-LAST:event_deleteButtonActionPerformed

	private void editButtonActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_editButtonActionPerformed
		int selectedItem = cellDataList.getSelectedIndex();
		if (selectedItem >= 0)
		{
			DefaultListModel<ColoringData> cellDataListModel = (DefaultListModel<ColoringData>) cellDataList.getModel();
			ColoringData oldColoringData = cellDataListModel.get(selectedItem);

			CustomPlateDataImporterDialog dialog = new CustomPlateDataImporterDialog(JOptionPane.getFrameForComponent(this), true, modelManager.getPolyhedralModel().getSmallBodyPolyData().GetNumberOfCells());
			dialog.setColoringData(oldColoringData);
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);

			// If user clicks okay replace item in list
			if (dialog.getOkayPressed())
			{
				ColoringData newColoringData = dialog.getColoringData();
				if (!oldColoringData.getFileName().equals(newColoringData.getFileName()))
				{
					newColoringData = copyCellData(selectedItem, newColoringData);
				}

				if (!oldColoringData.equals(newColoringData))
				{
					coloringDataManager.removeCustom(oldColoringData);
					coloringDataManager.addCustom(newColoringData);
					updateConfigFile();
				}
				cellDataListModel.set(selectedItem, newColoringData);
			}
		}
	}//GEN-LAST:event_editButtonActionPerformed

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_closeButtonActionPerformed
		setVisible(false);
	}//GEN-LAST:event_closeButtonActionPerformed

	private void cellDataListValueChanged(javax.swing.event.ListSelectionEvent evt)
	{//GEN-FIRST:event_cellDataListValueChanged
		int selectedItem = cellDataList.getSelectedIndex();
		if (selectedItem >= 0)
		{
			DefaultListModel<ColoringData> cellDataListModel = (DefaultListModel<ColoringData>) cellDataList.getModel();

			ColoringData coloringData = cellDataListModel.get(selectedItem);
			boolean builtIn = coloringDataManager.isBuiltIn(coloringData);
			editButton.setEnabled(!builtIn);
			deleteButton.setEnabled(!builtIn);
		}
		else
		{
			editButton.setEnabled(false);
			deleteButton.setEnabled(false);
		}
	}//GEN-LAST:event_cellDataListValueChanged

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JList<ColoringData> cellDataList;
	private javax.swing.JButton closeButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JButton editButton;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JButton newButton;
	// End of variables declaration//GEN-END:variables
}
