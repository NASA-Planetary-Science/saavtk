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
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.ShapeModelImporter;
import edu.jhuapl.saavtk.gui.ShapeModelImporter.FormatType;
import edu.jhuapl.saavtk.gui.ShapeModelImporter.ShapeModelType;
import edu.jhuapl.saavtk.model.ShapeModel;
import edu.jhuapl.saavtk.util.MapUtil;


public class ShapeModelImporterDialog extends javax.swing.JDialog
{
    // True if we're editing an existing model rather than creating a new one.
    private boolean editMode = false;

    private boolean okayPressed = false;

    /** Creates new form ShapeModelImporterDialog */
    public ShapeModelImporterDialog(java.awt.Window parent)
    {
        super(parent, "Import New Shape Model", Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
    }

    public String getNameOfImportedShapeModel()
    {
        return nameTextField.getText();
    }

    public void setName(String name)
    {
        nameTextField.setText(name);
    }

    public void setEditMode(boolean b)
    {
        editMode = b;
        nameLabel.setEnabled(!b);
        nameTextField.setEnabled(!b);
    }

    public boolean getOkayPressed()
    {
        return okayPressed;
    }

    public void loadConfig(String configFilename) throws IOException
    {
        MapUtil configMap = new MapUtil(configFilename);
        nameTextField.setText(configMap.get(ShapeModel.NAME));
        boolean isEllipsoid = ShapeModel.ELLIPSOID.equals(configMap.get(ShapeModel.TYPE));
        ellipsoidRadioButton.setSelected(isEllipsoid);
        customShapeModelRadioButton.setSelected(!isEllipsoid);

        if (isEllipsoid)
        {
            equatorialRadiusXFormattedTextField.setText(configMap.get(ShapeModel.EQUATORIAL_RADIUS_X));
            equatorialRadiusYFormattedTextField.setText(configMap.get(ShapeModel.EQUATORIAL_RADIUS_Y));
            polarRadiusFormattedTextField1.setText(configMap.get(ShapeModel.POLAR_RADIUS));
            resolutionFormattedTextField.setText(configMap.get(ShapeModel.RESOLUTION));
        }
        else
        {
            shapeModelPathTextField.setText(configMap.get(ShapeModel.CUSTOM_SHAPE_MODEL_PATH));
            String format = configMap.get(ShapeModel.CUSTOM_SHAPE_MODEL_FORMAT);
            shapeModelFormatComboBox.setSelectedItem(format);
        }


        updateEnabledState();
    }

    private void updateEnabledState()
    {
        boolean enabled = ellipsoidRadioButton.isSelected();
        shapeModelPathTextField.setEnabled(!enabled);
        browseShapeModelButton.setEnabled(!enabled);
        pathLabel.setEnabled(!enabled);
        shapeModelFormatLabel.setEnabled(!enabled);
        shapeModelFormatComboBox.setEnabled(!enabled);
        equatorialRadiusXLabel.setEnabled(enabled);
        equatorialRadiusXFormattedTextField.setEnabled(enabled);
        equatorialRadiusYLabel.setEnabled(enabled);
        equatorialRadiusYFormattedTextField.setEnabled(enabled);
        polarRadiusLabel1.setEnabled(enabled);
        polarRadiusFormattedTextField1.setEnabled(enabled);
        resolutionLabel.setEnabled(enabled);
        resolutionFormattedTextField.setEnabled(enabled);
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

        shapeModelSourceButtonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        customShapeModelRadioButton = new javax.swing.JRadioButton();
        ellipsoidRadioButton = new javax.swing.JRadioButton();
        shapeModelPathTextField = new javax.swing.JTextField();
        browseShapeModelButton = new javax.swing.JButton();
        equatorialRadiusXLabel = new javax.swing.JLabel();
        equatorialRadiusYLabel = new javax.swing.JLabel();
        resolutionLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        equatorialRadiusXFormattedTextField = new javax.swing.JFormattedTextField();
        equatorialRadiusYFormattedTextField = new javax.swing.JFormattedTextField();
        resolutionFormattedTextField = new javax.swing.JFormattedTextField();
        shapeModelFormatLabel = new javax.swing.JLabel();
        shapeModelFormatComboBox = new javax.swing.JComboBox();
        pathLabel = new javax.swing.JLabel();
        polarRadiusLabel1 = new javax.swing.JLabel();
        polarRadiusFormattedTextField1 = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        nameLabel.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 0);
        getContentPane().add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 300;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 0);
        getContentPane().add(nameTextField, gridBagConstraints);

        shapeModelSourceButtonGroup.add(customShapeModelRadioButton);
        customShapeModelRadioButton.setText("Custom Shape Model");
        customShapeModelRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customShapeModelRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(customShapeModelRadioButton, gridBagConstraints);

        shapeModelSourceButtonGroup.add(ellipsoidRadioButton);
        ellipsoidRadioButton.setSelected(true);
        ellipsoidRadioButton.setText("Ellipsoid Shape Model");
        ellipsoidRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ellipsoidRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(ellipsoidRadioButton, gridBagConstraints);

        shapeModelPathTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(shapeModelPathTextField, gridBagConstraints);

        browseShapeModelButton.setText("Browse...");
        browseShapeModelButton.setEnabled(false);
        browseShapeModelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseShapeModelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        getContentPane().add(browseShapeModelButton, gridBagConstraints);

        equatorialRadiusXLabel.setText("Equatorial Radius - X (km)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(equatorialRadiusXLabel, gridBagConstraints);

        equatorialRadiusYLabel.setText("Equatorial Radius - Y (km)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(equatorialRadiusYLabel, gridBagConstraints);

        resolutionLabel.setText("Resolution");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(resolutionLabel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(cancelButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        jPanel1.add(okButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        equatorialRadiusXFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        equatorialRadiusXFormattedTextField.setText("1000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(equatorialRadiusXFormattedTextField, gridBagConstraints);

        equatorialRadiusYFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        equatorialRadiusYFormattedTextField.setText("1000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(equatorialRadiusYFormattedTextField, gridBagConstraints);

        resolutionFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        resolutionFormattedTextField.setText("360");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(resolutionFormattedTextField, gridBagConstraints);

        shapeModelFormatLabel.setText("Format");
        shapeModelFormatLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(shapeModelFormatLabel, gridBagConstraints);

        shapeModelFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PDS", "OBJ", "VTK" }));
        shapeModelFormatComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(shapeModelFormatComboBox, gridBagConstraints);

        pathLabel.setText("Path");
        pathLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(pathLabel, gridBagConstraints);

        polarRadiusLabel1.setText("Polar Radius - Z (km)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(polarRadiusLabel1, gridBagConstraints);

        polarRadiusFormattedTextField1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        polarRadiusFormattedTextField1.setText("1000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(polarRadiusFormattedTextField1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void customShapeModelRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_customShapeModelRadioButtonActionPerformed
    {//GEN-HEADEREND:event_customShapeModelRadioButtonActionPerformed
        if(!editMode)
        {
            updateEnabledState();
        }
        else
        {
            customShapeModelRadioButton.setSelected(false);
            ellipsoidRadioButton.setSelected(true);
            customShapeModelRadioButton.setEnabled(false);
        }

    }//GEN-LAST:event_customShapeModelRadioButtonActionPerformed

    private void ellipsoidRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ellipsoidRadioButtonActionPerformed
    {//GEN-HEADEREND:event_ellipsoidRadioButtonActionPerformed
        if(!editMode)
        {
            updateEnabledState();
        }
        else
        {
            customShapeModelRadioButton.setSelected(true);
            ellipsoidRadioButton.setSelected(false);
            ellipsoidRadioButton.setEnabled(false);
        }
    }//GEN-LAST:event_ellipsoidRadioButtonActionPerformed

    private void browseShapeModelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseShapeModelButtonActionPerformed
    {//GEN-HEADEREND:event_browseShapeModelButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Shape Model");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        shapeModelPathTextField.setText(filename);
    }//GEN-LAST:event_browseShapeModelButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed

        ShapeModelImporter importer = new ShapeModelImporter();

        ShapeModelType shapeModelType = ellipsoidRadioButton.isSelected() ? ShapeModelType.ELLIPSOID : ShapeModelType.FILE;
        importer.setShapeModelType(shapeModelType);

        String name = nameTextField.getText();
        importer.setName(name);

        if (shapeModelType == ShapeModelType.ELLIPSOID)
        {
            double equRadiusX = Double.parseDouble(equatorialRadiusXFormattedTextField.getText());
            double equRadiusY = Double.parseDouble(equatorialRadiusYFormattedTextField.getText());
            double polarRadius = Double.parseDouble(polarRadiusFormattedTextField1.getText());
            int resolution = Integer.parseInt(resolutionFormattedTextField.getText());
            importer.setEquRadiusX(equRadiusX);
            importer.setEquRadiusY(equRadiusY);
            importer.setPolarRadius(polarRadius);
            importer.setResolution(resolution);
        }
        else
        {
            String format = (String) shapeModelFormatComboBox.getSelectedItem();
            String modelPath = shapeModelPathTextField.getText();
            importer.setFormat(ShapeModelImporter.FormatType.valueOf(format));
            importer.setModelPath(modelPath);
        }

        String[] errorMessage = new String[1];
        boolean success = importer.importShapeModel(errorMessage,editMode);

        if (!success)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    errorMessage,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        okayPressed = true;
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseShapeModelButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton customShapeModelRadioButton;
    private javax.swing.JRadioButton ellipsoidRadioButton;
    private javax.swing.JFormattedTextField equatorialRadiusXFormattedTextField;
    private javax.swing.JLabel equatorialRadiusXLabel;
    private javax.swing.JFormattedTextField equatorialRadiusYFormattedTextField;
    private javax.swing.JLabel equatorialRadiusYLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JFormattedTextField polarRadiusFormattedTextField1;
    private javax.swing.JLabel polarRadiusLabel1;
    private javax.swing.JFormattedTextField resolutionFormattedTextField;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JComboBox shapeModelFormatComboBox;
    private javax.swing.JLabel shapeModelFormatLabel;
    private javax.swing.JTextField shapeModelPathTextField;
    private javax.swing.ButtonGroup shapeModelSourceButtonGroup;
    // End of variables declaration//GEN-END:variables
}
