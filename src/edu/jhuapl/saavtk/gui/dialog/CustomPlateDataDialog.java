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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.panel.PolyhedralModelControlPanel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.plateColoring.ColoringData;
import edu.jhuapl.saavtk.model.plateColoring.CustomizableColoringDataManager;
import edu.jhuapl.saavtk.model.plateColoring.FileBasedColoringData;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.SafeURLPaths;

public class CustomPlateDataDialog extends javax.swing.JDialog
{
	private static final long serialVersionUID = 1L;
	private final PolyhedralModelControlPanel controlPanel;
	private final ModelManager modelManager;
	private final CustomizableColoringDataManager coloringDataManager;

	/** Creates new form CustomImageLoaderPanel */
	public CustomPlateDataDialog(PolyhedralModelControlPanel controlPanel)
	{
		this.controlPanel = controlPanel;
		this.modelManager = controlPanel.getModelManager();
		this.coloringDataManager = modelManager.getPolyhedralModel().getColoringDataManager();

		initComponents();

		DefaultListModel<FileBasedColoringData> model = new DefaultListModel<>();
		cellDataList.setModel(model);

		initializeList(model);

		pack();
	}

	private final void initializeList(DefaultListModel<FileBasedColoringData> model)
	{
		int resolution = modelManager.getPolyhedralModel().getModelResolution();
		ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();
		if (resolutions.size() > resolution)
		{
			int numberElements = resolutions.get(resolution);
			for (ColoringData data : coloringDataManager.get(numberElements))
			{
				model.addElement(FileBasedColoringData.of(data));
			}
		}
	}

	private String getCustomDataFolder()
	{
		return modelManager.getPolyhedralModel().getCustomDataFolder();
	}

	private String getConfigFilename()
	{
		return modelManager.getPolyhedralModel().getPlateConfigFilename();
	}

	private void updateConfigFile()
	{
		// It's unclear whether this code needs to continue to write the config file.
		// The call at the end to "saveCustomMetadata" saves the colorings using the new
		// metadata format, which should be all that is needed. But it still falls back
		// on this old config file, so for now, keep writing both of them.
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

				String fileName = FileBasedColoringData.of(coloringData).getFileName();
				if (fileName == null)
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

				cellDataFilenames += fileName.replaceFirst(".*/", "");
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

    /**
     * Copy the source data file into a stored location under the custom data area.
     * This method assumes the source's {@link FileBasedColoringData#getFileName()}
     * method returns a valid file name. It is up to the caller to ensure this.
     * <p>
     * The stored location is randomly generated using {@link UUID}.
     *
     * @param index of the plate coloring in the list model
     * @param source the input coloring data object
     * @return output coloring data object.
     */
    private ColoringData copyCellData(int index, FileBasedColoringData source)
    {
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();

        // Copy the cell data file to the model directory
        try
        {
            String sourceFilePath = source.getFileName();
            Preconditions.checkState(sourceFilePath != null, "cannot copy coloring data with no associated source file");

            String sourceFileName = sourceFilePath.replaceFirst(".*[/\\\\]", "");
            String extension = sourceFileName.replaceFirst("[^\\.]*\\.", ".");
            String uuid = UUID.randomUUID().toString();
            String destFileName = "platedata-" + uuid + extension;
            String destFilePath = safeUrlPaths.getString(getCustomDataFolder(), destFileName);

            File fileCache = FileCache.getFileFromServer(sourceFilePath);
            FileUtil.copyFile(fileCache.getAbsolutePath(), destFilePath);

            // After copying the file, convert the file path to a URL format.
            destFilePath = safeUrlPaths.getUrl(safeUrlPaths.getString(getCustomDataFolder(), destFileName));
            FileBasedColoringData newColoringData = FileBasedColoringData.of(source.getName(), destFilePath, source.getTupleNames(), source.getColumnIdentifiers(), source.getUnits(), source.getNumberElements(), source.hasNulls());

            DefaultListModel<FileBasedColoringData> model = (DefaultListModel<FileBasedColoringData>) cellDataList.getModel();
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
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), "An I/O error occurred while trying to save the coloring data " + source, "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), "An error occurred while trying to save the coloring data " + source, "Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

	private void removeCellData(int index)
	{
		try
		{
			DefaultListModel<FileBasedColoringData> model = (DefaultListModel<FileBasedColoringData>) cellDataList.getModel();
			FileBasedColoringData cellDataInfo = model.get(index);
			model.remove(index);
			coloringDataManager.removeCustom(cellDataInfo);

			File file = FileCache.getFileFromServer(cellDataInfo.getFileName());
			Files.delete(file.toPath());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		controlPanel.updateColoringOptions();
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
			DefaultListModel<FileBasedColoringData> cellDataListModel = (DefaultListModel<FileBasedColoringData>) cellDataList.getModel();
			FileBasedColoringData oldColoringData = cellDataListModel.get(selectedItem);

			CustomPlateDataImporterDialog dialog = new CustomPlateDataImporterDialog(JOptionPane.getFrameForComponent(this), coloringDataManager, true, modelManager.getPolyhedralModel().getSmallBodyPolyData().GetNumberOfCells());
			dialog.setColoringData(oldColoringData);
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);

			// If user clicks okay replace item in list
			if (dialog.getOkayPressed())
			{
				FileBasedColoringData newColoringData = dialog.getColoringData();
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
			DefaultListModel<FileBasedColoringData> cellDataListModel = (DefaultListModel<FileBasedColoringData>) cellDataList.getModel();

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
	private javax.swing.JList<FileBasedColoringData> cellDataList;
	private javax.swing.JButton closeButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JButton editButton;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JButton newButton;
	// End of variables declaration//GEN-END:variables
}
