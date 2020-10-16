/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
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
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.saavtk.model.plateColoring.FileBasedColoringData;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.file.DataFileReader;
import edu.jhuapl.saavtk.util.file.DataFileReader.FileFormatException;
import edu.jhuapl.saavtk.util.file.DataFileReader.IncorrectFileFormatException;
import edu.jhuapl.saavtk.util.file.DataObjectInfo;
import edu.jhuapl.saavtk.util.file.TableInfo;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.TableHDU;

@SuppressWarnings("serial")
public class CustomPlateDataImporterDialog extends javax.swing.JDialog
{
    private static final String LEAVE_UNMODIFIED = "<leave unmodified or empty to use existing plate data>";

    private final ColoringDataManager coloringDataManager;
    private final int numCells;
    private final boolean isEditMode;
    private boolean okayPressed;
    private FileBasedColoringData origData; // Used in edit mode.
    private FileBasedColoringData currentData;

    /** Creates new form ShapeModelImporterDialog */
    public CustomPlateDataImporterDialog(java.awt.Window parent, ColoringDataManager coloringDataManager, boolean isEditMode, int numCells)
    {
        super(parent, "Import Plate Data", Dialog.ModalityType.DOCUMENT_MODAL);
        this.coloringDataManager = coloringDataManager;
        this.numCells = numCells;
        this.isEditMode = isEditMode;
        this.okayPressed = false;
        this.origData = null;
        this.currentData = null;
        initComponents();
    }

    public FileBasedColoringData getColoringData()
    {
        return currentData;
    }

    public void setColoringData(FileBasedColoringData data)
    {
        nameTextField.setText(data.getName());
        unitsTextField.setText(data.getUnits());
        hasNullsCheckBox.setSelected(data.hasNulls());

        if (isEditMode)
        {
            cellDataPathTextField.setText(LEAVE_UNMODIFIED);
            origData = data;
            List<String> elementNames = data.getTupleNames();
            int numberColumns = elementNames.size();

            if (numberColumns == 1)
            {
                scalarRadioButton.setSelected(true);
            }
            else if (numberColumns == 3)
            {
                vectorRadioButton.setSelected(true);
            }

            updateImportOptions(urlToFileName(data.getFileName()));

            if (numberColumns == 1)
            {
                select(comboBox, elementNames.get(0));
            }
            else if (numberColumns == 3)
            {
                select(xComboBox, elementNames.get(0));
                select(yComboBox, elementNames.get(1));
                select(zComboBox, elementNames.get(2));
            }
        }
    }

    private void select(JComboBox<String> comboBox, String string)
    {
        for (int index = 0; index < comboBox.getItemCount(); ++index)
        {
            if (comboBox.getItemAt(index).toString().equals(string))
            {
                comboBox.setSelectedIndex(index);
                break;
            }
        }
    }

    private String validateInput()
    {
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

            // if (cellDataPath.toLowerCase().endsWith(".fit") ||
            // cellDataPath.toLowerCase().endsWith(".fits"))
            // result = validateFitsFile(cellDataPath);
            // else
            // result = validateTxtFile(cellDataPath);

            try
            {
                @SuppressWarnings("unused")
                ImmutableList<String> columnTitles = getColumnTitles(cellDataPath);

            }
            catch (IOException | FileFormatException e)
            {
                return e.getMessage();
            }
        }

        String name = nameTextField.getText();
        if (name == null)
            name = "";
        name = name.trim();
        nameTextField.setText(name);
        if (name.isEmpty())
            return "Please enter a name for the plate data.";

        if (!isEditMode && coloringDataManager.has(name, numCells))
            return "Duplicated coloring name: " + name + " already exists";

        String units = unitsTextField.getText();
        if (units == null)
            units = "";
        units = units.trim();
        unitsTextField.setText(units);
        if (name.contains(",") || units.contains(","))
            return "Fields may not contain commas.";

        return null;
    }

    private ImmutableList<String> getColumnTitles(String cellDataPath) throws IOException, FileFormatException
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        File file = new File(cellDataPath);
        ImmutableList<DataObjectInfo> dataInfoList;
        dataInfoList = DataFileReader.of().readFileInfo(file).getDataObjectInfo();
        for (DataObjectInfo dataObjectInfo : dataInfoList)
        {
            if (dataObjectInfo instanceof TableInfo)
            {
                TableInfo tableInfo = (TableInfo) dataObjectInfo;
                for (int i = 0; i < tableInfo.getNumberColumns(); i++)
                {
                    String title = tableInfo.getColumnInfo(i).getName();
                    if (title.matches(".*\\S.*"))
                    {
                        builder.add(title);
                    }
                    else
                    {
                        builder.add("Column " + Integer.toString(i));
                    }
                }
            }
        }

        return builder.build();
    }

    // TODO redmine 1339: eventually this method should be superseded by a method
    // that returns the
    // information from within the text file (e.g. "getColumnTitlesCsv").
    private String validateTxtFile(String cellDataPath)
    {
        InputStream fs;
        try
        {
            fs = new FileInputStream(cellDataPath);
        }
        catch (@SuppressWarnings("unused") FileNotFoundException e)
        {
            return "The file '" + cellDataPath + "' does not exist or is not readable.";
        }

        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        int lineCount = 0;
        try
        {
            while (in.readLine() != null)
            {
                // This check would need to be generalized to handle
                // the case of multiple CSV separated values. Not bothering
                // to do this because file checking will be redone soon.
                // Double.parseDouble(line);
                ++lineCount;
            }

            in.close();
        }
        catch (@SuppressWarnings("unused") NumberFormatException e)
        {
            return "Numbers in file '" + cellDataPath + "' are malformatted.";
        }
        catch (@SuppressWarnings("unused") IOException e)
        {
            return "An error occurred reading the file '" + cellDataPath + "'.";
        }

        if (lineCount != numCells)
        {
            return "Number of lines in file '" + cellDataPath + "' must equal number of plates in shape model.";
        }

        return null;
    }

    // TODO redmine 1339: eventually this method should be superseded by a method
    // that returns the
    // information from within the Fits file (e.g. "getColumnTitlesFits").
    private String validateFitsFile(String filename)
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
                    return "FITS ancillary table has too few columns";
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

    private String urlToFileName(String urlString)
    {
        String fileName = null;
        if (urlString != null)
        {
            fileName = FileCache.instance().getFile(urlString).toString();
        }

        return fileName;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * Note: the content of this method was originally regenerated by the Form
     * Editor.
     * 
     * @throws IOException
     * @throws IncorrectFileFormatException
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
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
        setMinimumSize(new java.awt.Dimension(600, 300));
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

        importLabel = new javax.swing.JLabel();
        importLabel.setText("Import Data As:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(importLabel, gridBagConstraints);
        importLabel.setVisible(false);

        scalarRadioButton = new javax.swing.JRadioButton();
        scalarRadioButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                scalarRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        getContentPane().add(scalarRadioButton, gridBagConstraints);
        scalarRadioButton.setVisible(false);

        scalarRadioLabel = new javax.swing.JLabel();
        scalarRadioLabel.setText("Scalar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        getContentPane().add(scalarRadioLabel, gridBagConstraints);
        scalarRadioLabel.setVisible(false);

        vectorRadioButton = new javax.swing.JRadioButton();
        vectorRadioButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                vectorRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 100, 0, 0);
        getContentPane().add(vectorRadioButton, gridBagConstraints);
        vectorRadioButton.setVisible(false);

        vectorRadioLabel = new javax.swing.JLabel();
        vectorRadioLabel.setText("Vector");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 120, 0, 0);
        getContentPane().add(vectorRadioLabel, gridBagConstraints);
        vectorRadioLabel.setVisible(false);

        buttonGroup = new javax.swing.ButtonGroup();
        buttonGroup.add(scalarRadioButton);
        buttonGroup.add(vectorRadioButton);
        buttonGroup.clearSelection();

        scalarLabel = new javax.swing.JLabel();
        scalarLabel.setText("Choose Column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(scalarLabel, gridBagConstraints);
        scalarLabel.setVisible(false);

        xLabel = new javax.swing.JLabel();
        xLabel.setText("Choose 'X' Column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(xLabel, gridBagConstraints);
        xLabel.setVisible(false);

        yLabel = new javax.swing.JLabel();
        yLabel.setText("Choose 'Y' Column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 0, 0);
        getContentPane().add(yLabel, gridBagConstraints);
        yLabel.setVisible(false);

        zLabel = new javax.swing.JLabel();
        zLabel.setText("Choose 'Z' Column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 0, 0);
        getContentPane().add(zLabel, gridBagConstraints);
        zLabel.setVisible(false);

        comboBox = new JComboBox<>();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(comboBox, gridBagConstraints);
        comboBox.setVisible(false);

        xComboBox = new JComboBox<>();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(xComboBox, gridBagConstraints);
        xComboBox.setVisible(false);

        yComboBox = new JComboBox<>();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 0, 0);
        getContentPane().add(yComboBox, gridBagConstraints);
        yComboBox.setVisible(false);

        zComboBox = new JComboBox<>();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 0, 0);
        getContentPane().add(zComboBox, gridBagConstraints);
        zComboBox.setVisible(false);

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
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 25, 5, 0);
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
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(hasNullsCheckBox, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void browsePlateDataButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) // GEN-FIRST:event_browsePlateDataButtonActionPerformed

    {// GEN-HEADEREND:event_browsePlateDataButtonActionPerformed

        File file = CustomFileChooser.showOpenDialog(this, "Select Plate Data");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        cellDataPathTextField.setText(filename);

        updateImportOptions(filename);

    }// GEN-LAST:event_browsePlateDataButtonActionPerformed

    private void updateImportOptions(String filename)
    {
        ImmutableList<String> columnTitles;
        try
        {
            columnTitles = getColumnTitles(filename);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // buttonGroup.clearSelection();
        if (buttonGroup.getSelection() == null)
        {
            scalarRadioButton.setSelected(true);
        }
        boolean scalarMode = scalarRadioButton.isSelected();

        if (columnTitles.size() >= 3)
        {
            importLabel.setVisible(true);
            scalarRadioButton.setVisible(true);
            scalarRadioLabel.setVisible(true);
            vectorRadioButton.setVisible(true);
            vectorRadioLabel.setVisible(true);

            scalarLabel.setVisible(scalarMode);
            xLabel.setVisible(!scalarMode);
            yLabel.setVisible(!scalarMode);
            zLabel.setVisible(!scalarMode);
            comboBox.setVisible(scalarMode);
            xComboBox.setVisible(!scalarMode);
            yComboBox.setVisible(!scalarMode);
            zComboBox.setVisible(!scalarMode);
        }
        else if (columnTitles.size() == 2)
        {
            importLabel.setVisible(false);
            scalarRadioButton.setVisible(false);
            scalarRadioLabel.setVisible(false);
            vectorRadioButton.setVisible(false);
            vectorRadioLabel.setVisible(false);

            scalarLabel.setVisible(true);
            xLabel.setVisible(false);
            yLabel.setVisible(false);
            zLabel.setVisible(false);
            comboBox.setVisible(true);
            xComboBox.setVisible(false);
            yComboBox.setVisible(false);
            zComboBox.setVisible(false);
        }
        else
        {
            importLabel.setVisible(false);
            scalarRadioButton.setVisible(false);
            scalarRadioLabel.setVisible(false);
            vectorRadioButton.setVisible(false);
            vectorRadioLabel.setVisible(false);
            scalarLabel.setVisible(false);
            xLabel.setVisible(false);
            yLabel.setVisible(false);
            zLabel.setVisible(false);
            comboBox.setVisible(false);
            xComboBox.setVisible(false);
            yComboBox.setVisible(false);
            zComboBox.setVisible(false);
        }
        comboBox.removeAllItems();
        xComboBox.removeAllItems();
        yComboBox.removeAllItems();
        zComboBox.removeAllItems();

        if (columnTitles.size() == 1)
        {
            xComboBox.addItem(null);
            yComboBox.addItem(null);
            zComboBox.addItem(null);
        }

        for (String item : columnTitles)
        {
            comboBox.addItem(item);
            xComboBox.addItem(item);
            yComboBox.addItem(item);
            zComboBox.addItem(item);
        }
        if (!columnTitles.isEmpty())
        {
            comboBox.setSelectedIndex(0);
        }

    }

    private void scalarRadioButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt)
    {
        if (scalarRadioButton.isSelected())
        {
            scalarLabel.setVisible(true);
            comboBox.setVisible(true);
            xLabel.setVisible(false);
            yLabel.setVisible(false);
            zLabel.setVisible(false);
            xComboBox.setVisible(false);
            yComboBox.setVisible(false);
            zComboBox.setVisible(false);
        }
    }

    private void vectorRadioButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt)
    {
        if (vectorRadioButton.isSelected())
        {
            scalarLabel.setVisible(false);
            comboBox.setVisible(false);
            xLabel.setVisible(true);
            yLabel.setVisible(true);
            zLabel.setVisible(true);
            xComboBox.setVisible(true);
            yComboBox.setVisible(true);
            zComboBox.setVisible(true);
        }
    }

    private void cancelButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt)// GEN-FIRST:event_cancelButtonActionPerformed
    {// GEN-HEADEREND:event_cancelButtonActionPerformed
        setVisible(false);
    }// GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) // GEN-FIRST:event_okButtonActionPerformed
    {// GEN-HEADEREND:event_okButtonActionPerformed
        String errorString = validateInput();
        if (errorString != null)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), errorString, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fileName = cellDataPathTextField.getText();

        if (isEditMode && (LEAVE_UNMODIFIED.equals(fileName) || fileName == null || fileName.isEmpty()))
            fileName = origData != null ? urlToFileName(origData.getFileName()) : null;
        if (fileName == null)
            throw new AssertionError();

        ImmutableList<String> elementNames;
        ImmutableList<Integer> columnIdentifiers;
        if (scalarRadioButton.isSelected())
        {
            String selected = (String) comboBox.getSelectedItem();
            elementNames = selected != null ? ImmutableList.of(selected) : ImmutableList.of();
            columnIdentifiers = selected != null ? ImmutableList.of(comboBox.getSelectedIndex()) : null;
        }
        else
        {
            String xSelected = (String) xComboBox.getSelectedItem();
            String ySelected = (String) yComboBox.getSelectedItem();
            String zSelected = (String) zComboBox.getSelectedItem();
            ImmutableList.Builder<String> nameBuilder = ImmutableList.builder();
            ImmutableList.Builder<Integer> columnBuilder = ImmutableList.builder();
            if (xSelected != null)
            {
                nameBuilder.add(xSelected);
                columnBuilder.add(xComboBox.getSelectedIndex());
            }
            if (ySelected != null)
            {
                nameBuilder.add(ySelected);
                columnBuilder.add(yComboBox.getSelectedIndex());
            }
            if (zSelected != null)
            {
                nameBuilder.add(zSelected);
                columnBuilder.add(zComboBox.getSelectedIndex());
            }
            elementNames = nameBuilder.build();
            columnIdentifiers = columnBuilder.build();
        }

        // The commented out code below avoids reloading plate data if the data are
        // already loaded, and
        // if it appears the reload would result in the same data being (unnecessarily)
        // reloaded. Not clear
        // what the risks are of doing this, so leaving it commented out. But if users
        // should become impatient
        // with the unneeded reloads, this could be used.
        // // If the original coloring (being edited) was already loaded,
        // vtkFloatArray origVtkArray = null;
        // if (origData != null && origData.isLoaded())
        // {
        // // If the same file and same columns are identified, save the already-loaded
        // data.
        // String origFileName = origData.getFileName();
        // List<?> origColumnIds = origData.getColumnIdentifiers();
        // if (fileName == origFileName || (fileName != null &&
        // fileName.equals(origFileName))
        // && (columnIdentifiers == origColumnIds || (columnIdentifiers != null &&
        // columnIdentifiers.equals(origColumnIds))))
        // {
        // origVtkArray = origData.getData();
        // }
        // }
        //
        // if (origVtkArray == null)
        // {
        currentData = FileBasedColoringData.of(nameTextField.getText(), SafeURLPaths.instance().getUrl(fileName), elementNames, columnIdentifiers, unitsTextField.getText(), numCells, hasNullsCheckBox.isSelected());
        try
        {
            currentData.getData();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // }
        // else
        // {
        // // Preserve any loaded vtk data by constructing the new coloring data in two
        // steps.
        // BasicColoringData withoutFileName = BasicColoringData.of(nameTextField.getText(),
        // elementNames, columnIdentifiers, unitsTextField.getText(), numCells,
        // hasNullsCheckBox.isSelected(), origVtkArray);
        // currentData = BasicColoringData.renameFile(withoutFileName, fileName);
        // }

        okayPressed = true;
        setVisible(false);
    }// GEN-LAST:event_okButtonActionPerformed

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
    private javax.swing.JLabel importLabel;
    private javax.swing.JRadioButton scalarRadioButton;
    private javax.swing.JLabel scalarRadioLabel;
    private javax.swing.JRadioButton vectorRadioButton;
    private javax.swing.JLabel vectorRadioLabel;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel scalarLabel;
    private javax.swing.JLabel xLabel;
    private javax.swing.JLabel yLabel;
    private javax.swing.JLabel zLabel;
    private javax.swing.JComboBox<String> comboBox;
    private javax.swing.JComboBox<String> xComboBox;
    private javax.swing.JComboBox<String> yComboBox;
    private javax.swing.JComboBox<String> zComboBox;
    // End of variables declaration//GEN-END:variables
}
