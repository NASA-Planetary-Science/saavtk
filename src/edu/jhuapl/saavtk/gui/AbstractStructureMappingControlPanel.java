package edu.jhuapl.saavtk.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;
import edu.jhuapl.saavtk.model.LineModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.popupmenus.StructuresPopupMenu;
import edu.jhuapl.saavtk.util.Properties;

public abstract class AbstractStructureMappingControlPanel extends JPanel implements
    ActionListener,
    PropertyChangeListener,
    TableModelListener, ListSelectionListener
{
    private ModelManager modelManager;
    //private PickManager pickManager;
    private JButton loadStructuresButton;
    private JLabel structuresFileTextField;
    private JButton saveStructuresButton;
    private JButton saveAsStructuresButton;
    //private JList structuresList;
    private JTable structuresTable;
    private File structuresFile;
    private StructuresPopupMenu structuresPopupMenu;
    private JToggleButton editButton;
    private JButton hideAllButton;
    private JButton showAllButton;
    private JButton hideAllLabelsButton;
    private JButton showAllLabelsButton;
    //private JComboBox structureTypeComboBox;
    private StructureModel structureModel;
    private PickManager pickManager;
    private PickManager.PickMode pickMode;
    private JFrame profileWindow;

    public AbstractStructureMappingControlPanel(
            final ModelManager modelManager,
            final StructureModel structureModel,
            final PickManager pickManager,
            final PickManager.PickMode pickMode,
            StructuresPopupMenu structuresPopupMenu,
            boolean supportsLineWidth)
    {
        this.modelManager = modelManager;
        //this.pickManager = pickManager;
        this.structureModel = structureModel;
        this.pickManager = pickManager;
        this.pickMode = pickMode;
        this.structuresPopupMenu = structuresPopupMenu;

        structureModel.addPropertyChangeListener(this);
        this.addComponentListener(new ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                setEditingEnabled(false);
            }
        });

        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        setLayout(new MigLayout("wrap 3, insets 0"));

        this.loadStructuresButton = new JButton("Load...");
        this.loadStructuresButton.setEnabled(true);
        this.loadStructuresButton.addActionListener(this);

        this.saveStructuresButton= new JButton("Save");
        this.saveStructuresButton.setEnabled(true);
        this.saveStructuresButton.addActionListener(this);

        this.saveAsStructuresButton= new JButton("Save As...");
        this.saveAsStructuresButton.setEnabled(true);
        this.saveAsStructuresButton.addActionListener(this);

        this.structuresFileTextField = new JLabel("<no file loaded>");
        this.structuresFileTextField.setEnabled(true);
        this.structuresFileTextField.setPreferredSize(new java.awt.Dimension(150, 22));

        add(this.structuresFileTextField, "span");

        add(this.loadStructuresButton, "w 100!");
        add(this.saveStructuresButton, "w 100!");
        add(this.saveAsStructuresButton, "w 100!, wrap 15px");

        JLabel structureTypeText = new JLabel(" Structures");
        add(structureTypeText, "span");

        //String[] options = {LineModel.LINES, CircleModel.CIRCLES};
        //structureTypeComboBox = new JComboBox(options);

        //add(structureTypeComboBox, "wrap");


        String[] columnNames = {"Id",
                "Type",
                "Name",
                "Details",
                "Color",
                "Label"};
                //"Hide Label",
                //"Hide Structure"


        /*
        structuresList = new JList();
        structuresList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        structuresList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(structuresList);
        */
        //Object[][] data = new Object[0][7];
        Object[][] data = new Object[0][5];

        structuresTable = new JTable(new StructuresTableModel(data, columnNames));
        structuresTable.setBorder(BorderFactory.createTitledBorder(""));
        //table.setPreferredScrollableViewportSize(new Dimension(500, 130));
        structuresTable.setColumnSelectionAllowed(false);
        structuresTable.setRowSelectionAllowed(true);
        structuresTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        structuresTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        structuresTable.setDefaultRenderer(String.class, new StringRenderer());
        structuresTable.setDefaultRenderer(Color.class, new ColorRenderer());
//        structuresTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        structuresTable.getModel().addTableModelListener(this);
        structuresTable.getSelectionModel().addListSelectionListener(this);
        structuresTable.addMouseListener(new TableMouseHandler());
        /*structuresTable.getColumnModel().getColumn(6).setPreferredWidth(31);
        structuresTable.getColumnModel().getColumn(7).setPreferredWidth(31);
        structuresTable.getColumnModel().getColumn(6).setResizable(false);
        structuresTable.getColumnModel().getColumn(7).setResizable(false);*/
        JScrollPane tableScrollPane = new JScrollPane(structuresTable);
        tableScrollPane.setPreferredSize(new Dimension(10000, 10000));

        add(tableScrollPane, "span");


        if (structureModel.supportsActivation())
        {
            final JButton newButton = new JButton("New");
            newButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    structureModel.setVisible(true); // in case user hid everything, make it visible again
                    structureModel.addNewStructure();
                    pickManager.setPickMode(pickMode);
                    editButton.setSelected(true);
                    updateStructureTable();

                    int numStructures = structuresTable.getRowCount();
                    if (numStructures > 0)
                        structuresTable.setRowSelectionInterval(numStructures-1, numStructures-1);
                }
            });
            add(newButton, "w 100!");
        }

        // Show warning info about how to draw ellipses
        // for the benefit of user as this has changed. This warning
        // is only temporarily and should be removed after several months.
        if (pickMode == PickManager.PickMode.ELLIPSE_DRAW)
        {
            String text = "<html>" +
                    "Warning: Method to create new ellipses<br>" +
                    "has changed! Click for details..." +
                    "</html>";
            JButton ellipsesWarningButton = new JButton(text);
            ellipsesWarningButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(AbstractStructureMappingControlPanel.this),
                            "To create a new ellipse, click on 3 points on the shape model in the following manner:\n" +
                            "The first 2 points should lie on the endpoints of the major axis of the desired ellipse.\n" +
                            "The third point should lie on one of the endpoints of the minor axis of the desired ellipse.\n" +
                            "After clicking the third point, an ellipse is drawn that passes through the points.",
                            "How to Create a New Ellipse",
                            JOptionPane.PLAIN_MESSAGE);
                }
            });
            add(ellipsesWarningButton, "span 3, wrap");
        }

        editButton = new JToggleButton("Edit");
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setEditingEnabled(editButton.isSelected());
            }
        });
        editButton.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                hideAllButton.setEnabled(!editButton.isSelected());
                showAllButton.setEnabled(!editButton.isSelected());
                showAllLabelsButton.setEnabled(!editButton.isSelected());
                hideAllLabelsButton.setEnabled(!editButton.isSelected());
            }
        });
        add(editButton, "w 100!");

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteStructure();
            }
        });
        add(deleteButton, "w 100!, wrap");

        hideAllButton = new JButton("Hide All");
        hideAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                structureModel.setVisible(false);
            }
        });
        add(hideAllButton, "w 120!");

        showAllButton = new JButton("Show All");
        showAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                structureModel.setVisible(true);
            }
        });
        add(showAllButton, "w 120!, wrap");

        hideAllLabelsButton = new JButton("Hide Labels");
        hideAllLabelsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                structureModel.setLabelsVisible(false);
            }
        });
        add(hideAllLabelsButton, "w 120!");

        showAllLabelsButton = new JButton("Show Labels");
        showAllLabelsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                structureModel.setLabelsVisible(true);
            }
        });
        add(showAllLabelsButton, "w 120!, wrap");


        JButton deleteAllButton = new JButton("Delete All");
        deleteAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (structureModel.getNumberOfStructures() == 0)
                    return;

                int result = JOptionPane.showConfirmDialog(
                        JOptionPane.getFrameForComponent(AbstractStructureMappingControlPanel.this),
                        "Are you sure you want to delete all structures?",
                        "Confirm Delete All",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.NO_OPTION)
                    return;

                editButton.setSelected(false);

                structureModel.removeAllStructures();
                pickManager.setPickMode(PickManager.PickMode.DEFAULT);
                structureModel.activateStructure(-1);
            }
        });
        add(deleteAllButton, "w 120!, wrap");

        JButton changeOffsetButton = new JButton("Change Normal Offset...");
        changeOffsetButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(structureModel);
                changeOffsetDialog.setLocationRelativeTo(
                        JOptionPane.getFrameForComponent(AbstractStructureMappingControlPanel.this));
                changeOffsetDialog.setVisible(true);
            }
        });
        changeOffsetButton.setToolTipText(
                "<html>Structures displayed on a shape model need to be shifted slightly away from<br>" +
                "the shape model in the direction normal to the plates as otherwise they will<br>" +
                "interfere with the shape model itself and may not be visible. Click this<br>" +
                "button to show a dialog that will allow you to explicitely set the offset<br>" +
                "amount in meters.</html>");
        add(changeOffsetButton, "span 2, w 200!, wrap");

        if (supportsLineWidth)
        {
            JButton changeLineWidthButton = new JButton("Change Line Width...");
            changeLineWidthButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    SpinnerNumberModel sModel = new SpinnerNumberModel(structureModel.getLineWidth(), 1.0, 100.0, 1.0);
                    JSpinner spinner = new JSpinner(sModel);

                    int option = JOptionPane.showOptionDialog(
                            AbstractStructureMappingControlPanel.this,
                            spinner,
                            "Enter valid number",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, null, null);

                    if (option == JOptionPane.OK_OPTION)
                    {
                        structureModel.setLineWidth((Double)spinner.getValue());
                    }
                }
            });
            add(changeLineWidthButton, "span 2, w 200!, wrap");
        }

        structuresTable.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
                    deleteStructure();
            }
        });

        if (pickMode == PickManager.PickMode.LINE_DRAW)
        {
            // Only add profile plots for line structures
            ProfilePlot profilePlot = new ProfilePlot((LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES),
                    (PolyhedralModel)modelManager.getModel(ModelNames.SMALL_BODY));

            // Create a new frame/window with profile
            profileWindow = new JFrame();
            ImageIcon icon = new ImageIcon(getClass().getResource("/edu/jhuapl/saavtk/data/black-sphere.png"));
            profileWindow.setTitle("Profile Plot");
            profileWindow.setIconImage(icon.getImage());
            profileWindow.add(profilePlot.getChartPanel());
            profileWindow.setSize(600, 400);
            profileWindow.setVisible(false);

            JButton openProfilePlotButton = new JButton("Show Profile Plot...");
            openProfilePlotButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    profileWindow.setVisible(true);
                }
            });
            add(openProfilePlotButton, "span 2, w 200!, wrap");
        }
    }

    private void deleteStructure()
    {
        int numStructures = structuresTable.getRowCount();
        int idx = structuresTable.getSelectedRow();
        if (idx >= 0 && idx < numStructures)
        {
            structureModel.removeStructure(idx);
            structureModel.activateStructure(-1);
        }
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        Object source = actionEvent.getSource();

        if (source == this.loadStructuresButton)
        {
            File file = CustomFileChooser.showOpenDialog(this, "Select File");

            if (file != null)
            {
                try
                {
                    // If there are already structures, ask user if they want to
                    // append or overwrite them
                    boolean append = false;
                    if (structureModel.getNumberOfStructures() > 0)
                    {
                        Object[] options = {"Append", "Replace"};
                        int n = JOptionPane.showOptionDialog(this,
                                "Would you like to append to or replace the existing structures?",
                                "Append or Replace?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);
                        append = (n == 0 ? true : false);
                    }

                    structureModel.loadModel(file, append);
                    structuresFileTextField.setText(file.getAbsolutePath());
                    structuresFile = file;
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                            "There was an error reading the file.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e.printStackTrace();
                }
            }
        }
        else if (source == this.saveStructuresButton || source == this.saveAsStructuresButton)
        {
            File file = structuresFile;
            if (structuresFile == null || source == this.saveAsStructuresButton)
            {
                file = CustomFileChooser.showSaveDialog(this, "Select File");
            }

            if (file != null)
            {
                try
                {
                    structureModel.saveModel(file);
                    structuresFileTextField.setText(file.getAbsolutePath());
                    structuresFile = file;
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                            "There was an error saving the file.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e.printStackTrace();
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            updateStructureTable();

            if (structureModel.supportsActivation())
            {
                int idx = structureModel.getActivatedStructureIndex();
                if (idx >= 0)
                {
                    pickManager.setPickMode(pickMode);
                    if (!editButton.isSelected())
                        editButton.setSelected(true);
                    structuresTable.setRowSelectionInterval(idx, idx);
                    structuresTable.setEnabled(false);
                }
                else
                {
                    // Don't change the picker if this tab is not in view since
                    // it's possible we could be in the middle of drawing other
                    // objects.
                    if (isVisible())
                        pickManager.setPickMode(PickManager.PickMode.DEFAULT);
                    if (editButton.isSelected())
                        editButton.setSelected(false);
                    structuresTable.setEnabled(true);
                }
            }
        }
        else if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            // If we're editing, say, a path, return immediately.
            if (structureModel.supportsActivation() &&
                    editButton.isSelected())
            {
                return;
            }

            PickEvent e = (PickEvent)evt.getNewValue();
            if (modelManager.getModel(e.getPickedProp()) == structureModel)
            {
                int idx = structureModel.getStructureIndexFromCellId(e.getPickedCellId(), e.getPickedProp());

                if (Picker.isPopupTrigger(e.getMouseEvent()))
                {
                    // If the item right-clicked on is not selected, then deselect all the
                    // other items and select the item right-clicked on.
                    if (!structuresTable.isRowSelected(idx))
                    {
                        structuresTable.setRowSelectionInterval(idx, idx);
                    }
                }
                else
                {
                    int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
                    if (((e.getMouseEvent().getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) ||
                        ((e.getMouseEvent().getModifiers() & keyMask) == keyMask))
                        structuresTable.addRowSelectionInterval(idx, idx);
                    else
                        structuresTable.setRowSelectionInterval(idx, idx);
                }

                structuresTable.scrollRectToVisible(structuresTable.getCellRect(idx, 0, true));
            }
            else
            {
                int count = structuresTable.getRowCount();
                if (count > 0)
                    structuresTable.removeRowSelectionInterval(0, count-1);
            }
        }
        else if (Properties.STRUCTURE_ADDED.equals(evt.getPropertyName()))
        {
            int idx = structureModel.getNumberOfStructures() - 1;
            structuresTable.setRowSelectionInterval(idx, idx);
            structuresTable.scrollRectToVisible(structuresTable.getCellRect(idx, 0, true));
        }
        else if (Properties.STRUCTURE_REMOVED.equals(evt.getPropertyName()))
        {
            int idx = (Integer)evt.getNewValue();

            updateStructureTable();

            int numStructures = structuresTable.getRowCount();
            if (numStructures > 0)
            {
                if (idx > numStructures-1)
                {
                    structuresTable.setRowSelectionInterval(numStructures-1, numStructures-1);
                    structuresTable.scrollRectToVisible(structuresTable.getCellRect(numStructures-1, 0, true));
                }
                else
                {
                    structuresTable.setRowSelectionInterval(idx, idx);
                    structuresTable.scrollRectToVisible(structuresTable.getCellRect(idx, 0, true));
                }
            }
        }
        else if (Properties.ALL_STRUCTURES_REMOVED.equals(evt.getPropertyName()) ||
                 Properties.COLOR_CHANGED.equals(evt.getPropertyName()))
        {
            updateStructureTable();
        }
    }

    private void updateStructureTable()
    {
        int numStructures = structureModel.getNumberOfStructures();

        ((DefaultTableModel)structuresTable.getModel()).setRowCount(numStructures);
        for (int i=0; i<numStructures; ++i)
        {
            StructureModel.Structure structure = structureModel.getStructure(i);
            int[] c = structure.getColor();
            structuresTable.setValueAt(String.valueOf(structure.getId()), i, 0);
            structuresTable.setValueAt(structure.getType(), i, 1);
            structuresTable.setValueAt(structure.getName(), i, 2);
            structuresTable.setValueAt(structure.getInfo(), i, 3);
            structuresTable.setValueAt(new Color(c[0], c[1], c[2]), i, 4);
            structuresTable.setValueAt(structure.getLabel(), i, 5);
            //structuresTable.setValueAt(structure.getLabelHidden(), i, 6);
            //structuresTable.setValueAt(structure.getHidden(), i, 6);
        }
    }

    public void tableChanged(TableModelEvent e)
    {
        if (e.getColumn() == 2)
        {
            int row = e.getFirstRow();
            int col = e.getColumn();
            StructureModel.Structure structure = structureModel.getStructure(row);
            String name = (String)structuresTable.getValueAt(row, col);
            if (name != null && !name.equals(structure.getName()))
            {
                structure.setName(name);
            }
        }
        if(e.getColumn()==5)
        {
            int row = e.getFirstRow();
            int col = e.getColumn();
            StructureModel.Structure structure = structureModel.getStructure(row);
            String label = (String)structuresTable.getValueAt(row, col);
            if (label != null && !label.equals(structure.getLabel()))
            {
                structure.setLabel(label);
                boolean empty = structuresPopupMenu.updateLabel(label, row);
                if(!empty)
                {
                    label="";
                    structuresTable.setValueAt("",row, col);
                }
            }
        }
        /*if(e.getColumn()==6)
        {
            int row = e.getFirstRow();
            int col = e.getColumn();
            StructureModel.Structure structure = structureModel.getStructure(row);
            Boolean hidden = (Boolean)structuresTable.getValueAt(row, col);
            if(structuresTable.getValueAt(row, col-1)!=null&&!(structuresTable.getValueAt(row, col-1)).equals(""))
            {
                structure.setHidden(hidden);
                //structureModel.
            }
        }
        if(e.getColumn()==7)
        {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if(structuresTable.getValueAt(row, col-1)!=null&&!(structuresTable.getValueAt(row, col-2)).equals(""))
            {
                Boolean labelHidden = (Boolean)structuresTable.getValueAt(row, col-1);
                Boolean hidden = (Boolean)structuresTable.getValueAt(row, col);
                int [] loc = {row};
                structureModel.setStructuresHidden(loc, hidden, labelHidden, false);
            }
        }*/
    }

    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting() == false)
        {
            structureModel.selectStructures(structuresTable.getSelectedRows());
        }
    }

    public void setEditingEnabled(boolean enable)
    {
        if (enable)
        {
            structureModel.setVisible(true); // in case user hid everything, make it visible again

            if (!editButton.isSelected())
                editButton.setSelected(true);
        }
        else
        {
            if (editButton.isSelected())
                editButton.setSelected(false);
        }

        if (structureModel.supportsActivation())
        {
            int idx = structuresTable.getSelectedRow();

            if (enable)
            {
                if (idx >= 0)
                {
                    pickManager.setPickMode(pickMode);
                    structureModel.activateStructure(idx);
                }
                else
                {
                    editButton.setSelected(false);
                }
            }
            else
            {
                pickManager.setPickMode(PickManager.PickMode.DEFAULT);
                structureModel.activateStructure(-1);
            }

            // The item in the table might get deselected so select it again here.
            int numStructures = structuresTable.getRowCount();
            if (idx >= 0 && idx < numStructures)
                structuresTable.setRowSelectionInterval(idx, idx);
        }
        else
        {
            if (enable)
            {
                pickManager.setPickMode(pickMode);
            }
            else
            {
                pickManager.setPickMode(PickManager.PickMode.DEFAULT);
            }
        }
    }

    private void structuresTableMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger() && !editButton.isSelected())
        {
            int index = structuresTable.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
                if (!structuresTable.isRowSelected(index))
                {
                    structuresTable.setRowSelectionInterval(index, index);
                }

                structuresPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class StringRenderer extends DefaultTableCellRenderer
    {
        private Color selectionForeground;
        public StringRenderer()
        {
            UIDefaults defaults = UIManager.getDefaults();
            selectionForeground = defaults.getColor("Table.selectionForeground");
        }

        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (structureModel.isStructureHidden(row))
            {
                c.setForeground(Color.GRAY);
            }
            else
            {
                if (isSelected)
                    c.setForeground(selectionForeground);
                else
                    c.setForeground(Color.BLACK);
            }

            return c;
        }
    }

    class ColorRenderer extends JLabel implements TableCellRenderer
    {
        private Border unselectedBorder = null;
        private Border selectedBorder = null;

        public ColorRenderer()
        {
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            Color newColor = (Color)color;
            setBackground(newColor);

            if (isSelected)
            {
                if (selectedBorder == null)
                {
                    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            }
            else
            {
                if (unselectedBorder == null)
                {
                    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                              table.getBackground());
                }
                setBorder(unselectedBorder);
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", "
                    + newColor.getGreen() + ", "
                    + newColor.getBlue());

            return this;
        }
    }

    class StructuresTableModel extends DefaultTableModel
    {
        public StructuresTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            if (column == 2 || column ==5) //|| column ==6 || column ==7)
                return true;
            else
                return false;
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex == 4)
                return Color.class;
            //else if(columnIndex == 6||columnIndex == 7)
             //   return Boolean.class;
            else
                return String.class;
        }
    }

    class TableMouseHandler extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            int row = structuresTable.rowAtPoint(e.getPoint());
            int col = structuresTable.columnAtPoint(e.getPoint());

            if (e.getClickCount() == 2 && row >= 0 && col == 4)
            {
                Color color = ColorChooser.showColorChooser(
                        JOptionPane.getFrameForComponent(structuresTable),
                        structureModel.getStructure(row).getColor());

                if (color == null)
                    return;

                int[] c = new int[4];
                c[0] = color.getRed();
                c[1] = color.getGreen();
                c[2] = color.getBlue();
                c[3] = color.getAlpha();

                structureModel.setStructureColor(row, c);
            }
        }

        public void mousePressed(MouseEvent evt)
        {
            structuresTableMaybeShowPopup(evt);
        }

        public void mouseReleased(MouseEvent evt)
        {
            structuresTableMaybeShowPopup(evt);
        }
    }

}
