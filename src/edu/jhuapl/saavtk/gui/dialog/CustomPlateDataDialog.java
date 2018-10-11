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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.MetadataDisplay;
import edu.jhuapl.saavtk.gui.panel.PolyhedralModelControlPanel;
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
	private static final long serialVersionUID = 1L;
	private final PolyhedralModelControlPanel controlPanel;
	private final ModelManager modelManager;
	private final CustomizableColoringDataManager coloringDataManager;
	private final Map<File, MetadataDialog> metadataDialogs;

	/** Creates new form CustomImageLoaderPanel */
	public CustomPlateDataDialog(PolyhedralModelControlPanel controlPanel)
	{
		this.controlPanel = controlPanel;
		this.modelManager = controlPanel.getModelManager();
		this.coloringDataManager = modelManager.getPolyhedralModel().getColoringDataManager();
		this.metadataDialogs = new HashMap<>();

		initComponents();

		DefaultListModel<ColoringData> model = new DefaultListModel<>();
		cellDataList.setModel(model);

		initializeList(model);

		pack();
	}

	private final void initializeList(DefaultListModel<ColoringData> model)
	{
		int resolution = modelManager.getPolyhedralModel().getModelResolution();
		ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();
		if (resolutions.size() > resolution)
		{
			int numberElements = resolutions.get(resolution);
			for (ColoringData data : coloringDataManager.get(numberElements))
			{
				model.addElement(data);
			}
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

		ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();
		for (int index = 0; index < resolutions.size(); ++index)
		{
			for (ColoringData coloringData : coloringDataManager.get(resolutions.get(index)))
			{
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
				cellDataResolutionLevels += new Integer(index).toString();
			}
		}

		Map<String, String> newMap = new LinkedHashMap<>();

		newMap.put(PolyhedralModel.CELL_DATA_FILENAMES, cellDataFilenames);
		newMap.put(PolyhedralModel.CELL_DATA_NAMES, cellDataNames);
		newMap.put(PolyhedralModel.CELL_DATA_UNITS, cellDataUnits);
		newMap.put(PolyhedralModel.CELL_DATA_HAS_NULLS, cellDataHasNulls);
		newMap.put(PolyhedralModel.CELL_DATA_RESOLUTION_LEVEL, cellDataResolutionLevels);

		configMap.put(newMap);
		try
		{
			coloringDataManager.saveCustomMetadata(modelManager.getPolyhedralModel().getCustomDataFolder());
		}
		catch (IOException e)
		{
			// This should not fail, but if it does it should not disrupt what the user is doing.
			// Thus in this case it is appropriate to log the problem and then continue.
			e.printStackTrace();
		}
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
		destFilePath = FileCache.createFileURL(getCustomDataFolder(), destFileName).toString();
		ColoringData newColoringData = ColoringData.of(source.getName(), destFilePath, source.getElementNames(), source.getColumnIdentifiers(), source.getUnits(), source.getNumberElements(), source.hasNulls());

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

			File file = FileCache.getFileFromServer(cellDataInfo.getFileName());
			JDialog dialog = metadataDialogs.get(file);
			if (dialog != null)
			{
				dialog.setVisible(false);
			}
			metadataDialogs.remove(file);
			Files.delete(file.toPath());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		controlPanel.updateColoringOptions();
	}

	@SuppressWarnings("serial")
	private final class MetadataDialog extends JDialog
	{
		private final JPopupMenu jPopupMenu;

		MetadataDialog(File file)
		{
			setModal(false);
			setTitle(file.getName());

			JTabbedPane jTabbedPane = null;
			try
			{
				jTabbedPane = MetadataDisplay.summary(file);
				if (jTabbedPane.getTabCount() > 0)
				{
					add(jTabbedPane);
				}
				else
				{
					// Don't bother displaying empty metadata.
					jTabbedPane = null;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			JPopupMenu popup = new JPopupMenu();
			JMenuItem menuItem = null;
			if (jTabbedPane != null)
			{
				menuItem = new JMenuItem("Show metadata");
				final MetadataDialog dialog = this;
				menuItem.addActionListener((e) -> {
					dialog.pack();
					dialog.validate();
					dialog.setVisible(true);
				});
			}
			else
			{
				menuItem = new JMenuItem("No metadata available");
				menuItem.setEnabled(false);
			}
			popup.add(menuItem);
			this.jPopupMenu = popup;
		}

		public void showPopupMenu(MouseEvent event)
		{
			jPopupMenu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	private void showMetadataPopup(MouseEvent event)
	{
		if (event.isPopupTrigger())
		{
			// First make a right click do what a left click does as well.
			int row = cellDataList.locationToIndex(event.getPoint());
			cellDataList.setSelectedIndex(row);

			ColoringData coloringData = cellDataList.getSelectedValue();
			if (coloringData != null)
			{
				File file = FileCache.getFileFromServer(coloringData.getFileName());
				MetadataDialog metadataDialog = metadataDialogs.get(file);
				if (metadataDialog == null)
				{
					metadataDialog = new MetadataDialog(file);
					metadataDialogs.put(file, metadataDialog);
				}
				metadataDialog.showPopupMenu(event);
			}
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

		cellDataList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e)
			{
				showMetadataPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				showMetadataPopup(e);
			}
		});

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
		return new CustomPlateDataImporterDialog(JOptionPane.getFrameForComponent(this), coloringDataManager, false, modelManager.getPolyhedralModel().getSmallBodyPolyData().GetNumberOfCells());
	}

	private void newButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt)
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
		controlPanel.updateColoringOptions();
	}//GEN-LAST:event_newButtonActionPerformed

	private void deleteButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_deleteButtonActionPerformed
		int selectedItem = cellDataList.getSelectedIndex();
		if (selectedItem >= 0)
		{
			removeCellData(selectedItem);
			updateConfigFile();
		}
	}//GEN-LAST:event_deleteButtonActionPerformed

	private void editButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_editButtonActionPerformed
		int selectedItem = cellDataList.getSelectedIndex();
		if (selectedItem >= 0)
		{
			DefaultListModel<ColoringData> cellDataListModel = (DefaultListModel<ColoringData>) cellDataList.getModel();
			ColoringData oldColoringData = cellDataListModel.get(selectedItem);

			CustomPlateDataImporterDialog dialog = new CustomPlateDataImporterDialog(JOptionPane.getFrameForComponent(this), coloringDataManager, true, modelManager.getPolyhedralModel().getSmallBodyPolyData().GetNumberOfCells());
			dialog.setColoringData(oldColoringData);
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);

			// If user clicks okay replace item in list
			if (dialog.getOkayPressed())
			{
				ColoringData newColoringData = dialog.getColoringData();
				if (!oldColoringData.equals(newColoringData))
				{
					cellDataListModel.set(selectedItem, newColoringData);
					coloringDataManager.replaceCustom(oldColoringData.getName(), newColoringData);
					updateConfigFile();
				}
				controlPanel.updateColoringOptions();
			}
		}
	}//GEN-LAST:event_editButtonActionPerformed

	private void closeButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt)
	{//GEN-FIRST:event_closeButtonActionPerformed
		setVisible(false);
	}//GEN-LAST:event_closeButtonActionPerformed

	private void cellDataListValueChanged(@SuppressWarnings("unused") javax.swing.event.ListSelectionEvent evt)
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
