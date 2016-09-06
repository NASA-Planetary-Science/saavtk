/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomImageLoaderPanel.java
 *
 * Created on Jun 5, 2012, 3:56:56 PM
 */
package edu.jhuapl.saavtk.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.model.ColoringInfo;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.ShapeModel;
import edu.jhuapl.saavtk.model.PolyhedralModel.Format;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MapUtil;


public class CustomPlateDataDialog extends javax.swing.JDialog {

    private ModelManager modelManager;

    /** Creates new form CustomImageLoaderPanel */
    public CustomPlateDataDialog(
            ModelManager modelManager)
    {
        this.modelManager = modelManager;

        initComponents();

        cellDataList.setModel(new DefaultListModel());

        initializeList();

        pack();
    }

    private void initializeList()
    {
        ((DefaultListModel)cellDataList.getModel()).clear();
        ArrayList<ColoringInfo> list = modelManager.getPolyhedralModel().getColoringInfoList();
        for (ColoringInfo info : list)
            ((DefaultListModel)cellDataList.getModel()).addElement(info);
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
        if (configMap.containsKey(PolyhedralModel.CELL_DATA_FILENAMES) &&
                configMap.containsKey(PolyhedralModel.CELL_DATA_NAMES) &&
                configMap.containsKey(PolyhedralModel.CELL_DATA_UNITS) &&
                configMap.containsKey(PolyhedralModel.CELL_DATA_HAS_NULLS) &&
                configMap.containsKey(PolyhedralModel.CELL_DATA_RESOLUTION_LEVEL))
        {
            String[] cellDataFilenamesArr = configMap.get(PolyhedralModel.CELL_DATA_FILENAMES).split(",", -1);
            String[] cellDataNamesArr = configMap.get(PolyhedralModel.CELL_DATA_NAMES).split(",", -1);
            String[] cellDataUnitsArr = configMap.get(PolyhedralModel.CELL_DATA_UNITS).split(",", -1);
            String[] cellDataHasNullsArr = configMap.get(PolyhedralModel.CELL_DATA_HAS_NULLS).split(",", -1);
            String[] cellDataResolutionLevelsArr = configMap.get(PolyhedralModel.CELL_DATA_RESOLUTION_LEVEL).split(",", -1);

            int resolution = modelManager.getPolyhedralModel().getModelResolution();
            for (int i=0; i<cellDataFilenamesArr.length; ++i)
            {
                if (!cellDataResolutionLevelsArr[i].trim().isEmpty() &&
                        Integer.parseInt(cellDataResolutionLevelsArr[i]) != resolution)
                {
                    cellDataFilenames += cellDataFilenamesArr[i];
                    cellDataNames += cellDataNamesArr[i];
                    cellDataUnits += cellDataUnitsArr[i];
                    cellDataHasNulls += cellDataHasNullsArr[i];
                    cellDataResolutionLevels += cellDataResolutionLevelsArr[i];

                    if (i < cellDataFilenamesArr.length-1 || modelManager.getPolyhedralModel().getNumberOfCustomColors() > 0)
                    {
                        cellDataFilenames += PolyhedralModel.LIST_SEPARATOR;
                        cellDataNames += PolyhedralModel.LIST_SEPARATOR;
                        cellDataUnits += PolyhedralModel.LIST_SEPARATOR;
                        cellDataHasNulls += PolyhedralModel.LIST_SEPARATOR;
                        cellDataResolutionLevels += PolyhedralModel.LIST_SEPARATOR;
                    }
                }
            }
        }


        DefaultListModel cellDataListModel = (DefaultListModel)cellDataList.getModel();
        for (int i=0; i<cellDataListModel.size(); ++i)
        {
            ColoringInfo cellDataInfo = (ColoringInfo)cellDataListModel.get(i);

            if (cellDataInfo.builtIn)
                continue;

            cellDataFilenames += cellDataInfo.coloringFile;
            cellDataNames += cellDataInfo.coloringName;
            cellDataUnits += cellDataInfo.coloringUnits;
            cellDataHasNulls += new Boolean(cellDataInfo.coloringHasNulls).toString();
            cellDataResolutionLevels += new Integer(cellDataInfo.resolutionLevel).toString();

            if (i < cellDataListModel.size()-1)
            {
                cellDataFilenames += PolyhedralModel.LIST_SEPARATOR;
                cellDataNames += PolyhedralModel.LIST_SEPARATOR;
                cellDataUnits += PolyhedralModel.LIST_SEPARATOR;
                cellDataHasNulls += PolyhedralModel.LIST_SEPARATOR;
                cellDataResolutionLevels += PolyhedralModel.LIST_SEPARATOR;
            }
        }

        Map<String, String> newMap = new LinkedHashMap<String, String>();

        newMap.put(PolyhedralModel.CELL_DATA_FILENAMES, cellDataFilenames);
        newMap.put(PolyhedralModel.CELL_DATA_NAMES, cellDataNames);
        newMap.put(PolyhedralModel.CELL_DATA_UNITS, cellDataUnits);
        newMap.put(PolyhedralModel.CELL_DATA_HAS_NULLS, cellDataHasNulls);
        newMap.put(PolyhedralModel.CELL_DATA_RESOLUTION_LEVEL, cellDataResolutionLevels);

        configMap.put(newMap);
    }

    private void saveCellData(int index, ColoringInfo oldCellDataInfo, ColoringInfo newCellDataInfo)
    {
        String uuid = UUID.randomUUID().toString();
        Format format = newCellDataInfo.format;

        // If newCellDataInfo.coloringFile is the same as the oldCellDataInfo.coloringFile,
        // that means we are in edit mode and and the user did not change to a new coloring file.
        if (oldCellDataInfo == null || !newCellDataInfo.coloringFile.equals(oldCellDataInfo.coloringFile))
        {
            // Copy the cell data file to the model directory
            try
            {
                String newFilename = "platedata-" + uuid + "." + format.toString().toLowerCase();
                String newFilepath = getCustomDataFolder() + File.separator + newFilename;
                FileUtil.copyFile(newCellDataInfo.coloringFile, newFilepath);
                // Change coloringFile to the new filename
                newCellDataInfo.coloringFile = newFilename;
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "An error occurred loading the file",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        DefaultListModel model = (DefaultListModel)cellDataList.getModel();
        if (index >= model.getSize())
        {
            model.addElement(newCellDataInfo);
        }
        else
        {
            model.set(index, newCellDataInfo);
        }

        updateConfigFile();
    }

    private void removeCellData(int index)
    {
        try
        {
            ColoringInfo cellDataInfo = (ColoringInfo)((DefaultListModel)cellDataList.getModel()).get(index);
            String filename = getCustomDataFolder() + File.separator + cellDataInfo.coloringFile;
            new File(filename).delete();

            modelManager.getPolyhedralModel().removeCustomPlateData(index);
            ((DefaultListModel)cellDataList.getModel()).remove(index);
            updateConfigFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        cellDataList = new javax.swing.JList();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();

        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        cellDataList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
        return new CustomPlateDataImporterDialog(JOptionPane.getFrameForComponent(this), false);
    }

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        try
        {
            ColoringInfo cellDataInfo = new ColoringInfo();
            CustomPlateDataImporterDialog dialog = getPlateImporterDialog();
            dialog.setCellDataInfo(cellDataInfo, modelManager.getPolyhedralModel().getSmallBodyPolyData().GetNumberOfCells());
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            // If user clicks okay add to list
            if (dialog.getOkayPressed())
            {
                cellDataInfo = dialog.getCellDataInfo();
                if (cellDataInfo.coloringFile.toLowerCase().endsWith(".fit") || cellDataInfo.coloringFile.toLowerCase().endsWith(".fits"))
                    cellDataInfo.format = Format.FIT;
                saveCellData(((DefaultListModel)cellDataList.getModel()).getSize(), null, cellDataInfo);
                modelManager.getPolyhedralModel().addCustomPlateData(cellDataInfo);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int selectedItem = cellDataList.getSelectedIndex();
        if (selectedItem >= 0)
        {
            removeCellData(selectedItem);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        try
        {
            int selectedItem = cellDataList.getSelectedIndex();
            if (selectedItem >= 0)
            {
                DefaultListModel cellDataListModel = (DefaultListModel)cellDataList.getModel();
                ColoringInfo oldCellDataInfo = (ColoringInfo)cellDataListModel.get(selectedItem);

                CustomPlateDataImporterDialog dialog = new CustomPlateDataImporterDialog(JOptionPane.getFrameForComponent(this), true);
                dialog.setCellDataInfo(oldCellDataInfo, modelManager.getPolyhedralModel().getSmallBodyPolyData().GetNumberOfCells());
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);

                // If user clicks okay replace item in list
                if (dialog.getOkayPressed())
                {
                    ColoringInfo cellDataInfo = dialog.getCellDataInfo();
                    saveCellData(selectedItem, oldCellDataInfo, cellDataInfo);
                    modelManager.getPolyhedralModel().setCustomPlateData(selectedItem, cellDataInfo);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void cellDataListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_cellDataListValueChanged
        int selectedItem = cellDataList.getSelectedIndex();
        if (selectedItem >= 0)
        {
            DefaultListModel cellDataListModel = (DefaultListModel)cellDataList.getModel();
            ColoringInfo cellDataInfo = (ColoringInfo)cellDataListModel.get(selectedItem);
            editButton.setEnabled(!cellDataInfo.builtIn);
            deleteButton.setEnabled(!cellDataInfo.builtIn);
        }
        else
        {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }//GEN-LAST:event_cellDataListValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList cellDataList;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton newButton;
    // End of variables declaration//GEN-END:variables
}
