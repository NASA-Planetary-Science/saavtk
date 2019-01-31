package edu.jhuapl.saavtk.gui.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
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
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.opengis.feature.simple.SimpleFeature;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.GNumberFieldSlider;
import edu.jhuapl.saavtk.gui.ProfilePlot;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.NormalOffsetChangerDialog;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.geotools.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.geotools.FeatureUtil;
import edu.jhuapl.saavtk.model.structure.geotools.LineStructure;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.popup.StructuresPopupMenu;
import edu.jhuapl.saavtk.util.ColorIcon;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.ProgressListener;
import edu.jhuapl.saavtk.util.Properties;
import net.miginfocom.swing.MigLayout;

public class AbstractStructureMappingControlPanel extends JPanel implements ActionListener, PropertyChangeListener, TableModelListener, ListSelectionListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// State vars
	private ModelManager modelManager;
	private File structuresFile;
	private StructureModel structureModel;
	private PickManager pickManager;
	private PickManager.PickMode pickMode;
	private boolean supportsEsri=false;

	// GUI vars
	private JButton loadStructuresButton;
	private JLabel structuresFileL;
	// private JButton saveStructuresButton;
	private JButton saveAsStructuresButton;
	private JTable structuresTable;
	private StructuresPopupMenu structuresPopupMenu;
	private JFrame profileWindow;

	// GUI vars
	private NormalOffsetChangerDialog changeOffsetDialog;
	private JButton createB, deleteB;
	private JToggleButton editB;
	private JLabel labelTitleL, structTitleL;
	private JButton selectAllB, selectNoneB;
	private JButton structColorB, structHideB, structShowB;
	private JButton labelColorB, labelHideB, labelShowB;
	private JButton changeOffsetB;
	private GNumberFieldSlider fontSizeNFS;
	private GNumberFieldSlider lineWidthNFS;

	private JPopupMenu saveAsPopupMenu = new JPopupMenu();
	private PopupListener saveAsPopupListener = new PopupListener(saveAsPopupMenu);
	
	private StructuresLoadingTask task;
	private ProgressMonitor structuresLoadingProgressMonitor;
	
	public AbstractStructureMappingControlPanel(final ModelManager modelManager, ModelNames aModelType, final PickManager pickManager, final PickManager.PickMode pickMode, boolean supportsEsri)
	{
		this.modelManager = modelManager;
		structureModel = (StructureModel) modelManager.getModel(aModelType);
		this.pickManager = pickManager;
		this.pickMode = pickMode;
		structuresPopupMenu = (StructuresPopupMenu)pickManager.getPopupManager().getPopup(structureModel);
this.supportsEsri=supportsEsri;

		changeOffsetDialog = null;
		saveAsPopupMenu.add(new JMenuItem(new SaveSbmtStructuresFileAction()));
		saveAsPopupMenu.add(new JMenuItem(new SaveEsriShapeFileAction()));

		structureModel.addPropertyChangeListener(this);
		this.addComponentListener(new ComponentAdapter()
		{
			public void componentHidden(ComponentEvent e)
			{
				setEditingEnabled(false);
			}
		});

		pickManager.getDefaultPicker().addPropertyChangeListener(this);

		setLayout(new MigLayout("", "", "[]"));

		loadStructuresButton = new JButton("Load...");
		loadStructuresButton.addActionListener(this);

		// twupy1: Getting rid of "Save" feature at request of Carolyn since we don't
		// have an undo button yet
		// this.saveStructuresButton= new JButton("Save");
		// this.saveStructuresButton.setEnabled(true);
		// this.saveStructuresButton.addActionListener(this);

		if (supportsEsri)
		{
			saveAsStructuresButton = new JButton(new SaveAsPopupAction());
			saveAsStructuresButton.addMouseListener(saveAsPopupListener);
		}
		else
		{
			saveAsStructuresButton = new JButton("Save...");
			saveAsStructuresButton.addActionListener(this);
		}

		JLabel fileNameL = new JLabel("File: ");
		structuresFileL = new JLabel("<no file loaded>");
		add(fileNameL, "span,split");
		add(structuresFileL, "growx,pushx,w 100:100:,wrap");

		JLabel tableHeadL = new JLabel(" Structures");
		add(tableHeadL, "span,split,growx,pushx");
		add(loadStructuresButton, "sg g0");
		add(saveAsStructuresButton, "sg g0,wrap");

		String[] columnNames = { "Id", "Type", "Name", "Details", "Color", "Label" };
		// "Hide Label",
		// "Hide Structure"

		structuresTable = new JTable(new StructuresTableModel(columnNames));
		structuresTable.setBorder(BorderFactory.createTitledBorder(""));
		structuresTable.setColumnSelectionAllowed(false);
		structuresTable.setRowSelectionAllowed(true);
		structuresTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		structuresTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		structuresTable.setDefaultRenderer(String.class, new StringRenderer());
		structuresTable.setDefaultRenderer(Color.class, new ColorRenderer());
		structuresTable.getColumnModel().getColumn(5).setCellRenderer(new StructureLabelRenderer());
		// structuresTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		structuresTable.getModel().addTableModelListener(this);
		structuresTable.getSelectionModel().addListSelectionListener(this);
		structuresTable.addMouseListener(new TableMouseHandler());
		structuresTable.getTableHeader().setReorderingAllowed(false);

		structuresTable.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
					doDeleteSelectedStructures();
			}
		});

		/*structuresTable.getColumnModel().getColumn(6).setPreferredWidth(31);
		structuresTable.getColumnModel().getColumn(7).setPreferredWidth(31);
		structuresTable.getColumnModel().getColumn(6).setResizable(false);
		structuresTable.getColumnModel().getColumn(7).setResizable(false);*/

		JScrollPane tableScrollPane = new JScrollPane(structuresTable);
		add(tableScrollPane, "growx,growy,pushy,span,wrap");

		createB = new JButton("New");
		createB.addActionListener(this);
		createB.setVisible(structureModel.supportsActivation());
		deleteB = new JButton("Delete");
		deleteB.addActionListener(this);
		editB = new JToggleButton("Edit");
		editB.addActionListener(this);
		add(createB, "sg g1,span,split");
		add(editB, "sg g1");
		add(deleteB, "sg g1,wrap");
		
		labelTitleL = new JLabel("Labels:", JLabel.RIGHT);
		labelColorB = new JButton("");
		labelColorB.addActionListener(this);
		labelHideB = new JButton("Hide");
		labelHideB.addActionListener(this);
		labelShowB = new JButton("Show");
		labelShowB.addActionListener(this);
		add(labelTitleL, "span,split,sg g2");
		add(labelHideB, "gapright 0,sg g3");
		add(labelShowB, "gapleft 0,sg g3");
		add(labelColorB, "gapy 0,sg g3,wrap");

		structTitleL = new JLabel("Structs:", JLabel.RIGHT);
		structColorB = new JButton("");
		structColorB.addActionListener(this);
		structHideB = new JButton("Hide");
		structHideB.addActionListener(this);
		structShowB = new JButton("Show");
		structShowB.addActionListener(this);
		add(structTitleL, "span,split,sg g2");
		add(structHideB, "gapright 0,sg g3");
		add(structShowB, "gapleft 0,sg g3");
		add(structColorB, "gapy 0,sg g3,wrap 2");

		selectAllB = new JButton("Select All");
		selectAllB.addActionListener(this);
		selectNoneB = new JButton("Select None");
		selectNoneB.addActionListener(this);
		add(selectAllB, "sg g4,span,split");
		add(selectNoneB, "sg g4,wrap");

		changeOffsetB = new JButton("Change Normal Offset...");
		changeOffsetB.addActionListener(this);
		changeOffsetB.setToolTipText("<html>Structures displayed on a shape model need to be shifted slightly away from<br>" + "the shape model in the direction normal to the plates as otherwise they will<br>" + "interfere with the shape model itself and may not be visible. Click this<br>" + "button to show a dialog that will allow you to explicitely set the offset<br>" + "amount in meters.</html>");
		add(changeOffsetB, "sg g5,span,split");

		JButton openProfilePlotButton = new JButton("Show Profile Plot...");
		if (pickMode == PickManager.PickMode.LINE_DRAW)
		{
			// Only add profile plots for line structures
			ProfilePlot profilePlot = new ProfilePlot((LineModel) modelManager.getModel(ModelNames.LINE_STRUCTURES), (PolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));

			// Create a new frame/window with profile
			profileWindow = new JFrame();
			ImageIcon icon = new ImageIcon(getClass().getResource("/edu/jhuapl/saavtk/data/black-sphere.png"));
			profileWindow.setTitle("Profile Plot");
			profileWindow.setIconImage(icon.getImage());
			profileWindow.getContentPane().add(profilePlot.getChartPanel());
			profileWindow.setSize(600, 400);
			profileWindow.setVisible(false);

			openProfilePlotButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					profileWindow.setVisible(true);
				}
			});
		} 
		else
		{
			openProfilePlotButton.setVisible(false);
		}
		add(openProfilePlotButton, "sg g5,wrap");

		fontSizeNFS = new GNumberFieldSlider(this, "Font Size:", 8, 120);
		fontSizeNFS.setNumSteps(67);
		fontSizeNFS.setNumColumns(3);
		add(fontSizeNFS, "growx,sg g6,span,wrap");

		lineWidthNFS = new GNumberFieldSlider(this, "Line Width:", 1, 100);
		lineWidthNFS.setNumSteps(100);
		lineWidthNFS.setNumColumns(3);

		// Create the pointDiameterPanel if the PickMode == POINT_DRAW
		PointDiameterPanel pointDiameterPanel = null;
		if (pickMode == PickMode.POINT_DRAW)
		{
			PointModel pointModel = (PointModel)modelManager.getModel(ModelNames.POINT_STRUCTURES);
			pointDiameterPanel = new PointDiameterPanel(pointModel);
		}

		// Either the lineWidthNFS or the pointDiameterPanel will be visible
		if (pointDiameterPanel != null)
			add(pointDiameterPanel, "span");
//			add(pointDiameterPanel, "growx,sg g5,span,wrap");
		else
			add(lineWidthNFS, "growx,sg g6,span");

		// Hack to force fontSizeNFS label to have the same size as lineWidthNFS
		fontSizeNFS.getLabelComponent().setPreferredSize(lineWidthNFS.getLabelComponent().getPreferredSize());

		updateControlGui();
	}
	
	class StructuresLoadingTask extends SwingWorker<Void, Void>
    {
    	File file;
    	boolean append;

    	public StructuresLoadingTask(File file, boolean append)
    	{
    		this.file = file;
    		this.append = append;
    	}

    	@Override
    	protected Void doInBackground() throws Exception
    	{
    		structureModel.loadModel(file, append, new ProgressListener()
    		{
				@Override
				public void setProgress(int progress)
				{
					task.setProgress(progress);
				}

    		});
    		structuresFileL.setText(file.getAbsolutePath());
			structuresFile = file;
    		return null;
    	}

    	@Override
    	protected void done()
    	{
    		// TODO Auto-generated method stub
    		super.done();

    	}

    }

	@Override
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
						Object[] options = { "Append", "Replace" };
						int n = JOptionPane.showOptionDialog(this,
								"Would you like to append to or replace the existing structures?", "Append or Replace?",
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						append = (n == 0 ? true : false);
					}
					List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
					structuresLoadingProgressMonitor = new ProgressMonitor(null, "Loading Structures...", "", 0, 100);
			        structuresLoadingProgressMonitor.setProgress(0);

					task = new StructuresLoadingTask(file, append);
			        task.addPropertyChangeListener(this);
			        task.execute();
					
//					structureModel.loadModel(file, append);
//					structuresFileL.setText(file.getAbsolutePath());
//					structuresFile = file;
				} 
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
							"There was an error reading the file.", "Error", JOptionPane.ERROR_MESSAGE);

					e.printStackTrace();
				}
			}
		} 
		else if (/* source == this.saveStructuresButton || */source == this.saveAsStructuresButton && !supportsEsri)
		{
			File file = structuresFile;
			if (structuresFile == null || source == this.saveAsStructuresButton)
			{
				if (file != null)
				{
					// File already exists, use it as the default filename
					file = CustomFileChooser.showSaveDialog(this, "Select File", file.getName());
				} 
				else
				{
					// We don't have a default filename to provide
					file = CustomFileChooser.showSaveDialog(this, "Select File");
				}
			}

			if (file != null)
			{
				try
				{
					structureModel.saveModel(file);
					structuresFileL.setText(file.getAbsolutePath());
					structuresFile = file;
				} 
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
							"There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

					e.printStackTrace();
				}
			}
		}
		else if (source == createB)
		{
			// Ensure all structures are visible (in case any are hidden)
			structureModel.setVisible(true);
			
			structureModel.addNewStructure();
			pickManager.setPickMode(pickMode);
			editB.setSelected(true);
			updateStructureTable();

			int numStructures = structuresTable.getRowCount();
			if (numStructures > 0)
				structuresTable.setRowSelectionInterval(numStructures - 1, numStructures - 1);
		}
		else if (source == editB)
		{
			setEditingEnabled(editB.isSelected());
		}
		else if (source == selectAllB)
		{
			int numRows = structuresTable.getRowCount();
			if (numRows == 0)
				return;
			structuresTable.setRowSelectionInterval(0, numRows - 1);
		}
		else if (source == selectNoneB)
		{
			structuresTable.clearSelection();
		}
		else if (source == deleteB)
		{
			doDeleteSelectedStructures();
		}
		else if (source == labelColorB)
		{
			int[] idxArr = structuresTable.getSelectedRows();
			
			Color defColor = structureModel.getLabelColor(idxArr[0]);
			Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", defColor);
			if (tmpColor == null)
				return;
			
			structureModel.setLabelColor(idxArr, tmpColor);
		}
		else if (source == labelHideB)
		{
			int[] idxArr = structuresTable.getSelectedRows();
			structureModel.setLabelVisible(idxArr, false);
		}
		else if (source == labelShowB)
		{
			int[] idxArr = structuresTable.getSelectedRows();
			structureModel.setLabelVisible(idxArr, true);
		}
		else if (source == structColorB)
		{
			int[] idxArr = structuresTable.getSelectedRows();
			
			Color defColor = structureModel.getStructureColor(idxArr[0]);
			Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", defColor);
			if (tmpColor == null)
				return;
			
			structureModel.setStructureColor(idxArr, tmpColor);
		}
		else if (source == structHideB)
		{
			int[] idxArr = structuresTable.getSelectedRows();
			structureModel.setStructureVisible(idxArr, false);
		}
		else if (source == structShowB)
		{
			int[] idxArr = structuresTable.getSelectedRows();
			structureModel.setStructureVisible(idxArr, true);
		}
		else if (source == changeOffsetB)
		{
			// Lazy init
			if (changeOffsetDialog == null)
			{
				changeOffsetDialog = new NormalOffsetChangerDialog(structureModel);
				changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(this));
			}

			changeOffsetDialog.setVisible(true);
		}
		else if (source == fontSizeNFS)
		{
			doUpdateFontSize();
		}
		else if (source == lineWidthNFS)
		{
			doUpdateLineWidth();
		}
		
		updateControlGui();
	}
	
	/**
	 * Helper method to delete the selected structures
	 */
	private void doDeleteSelectedStructures()
	{
		int[] idxArr = structuresTable.getSelectedRows();

		// Request confirmation when deleting multiple items
		if (idxArr.length > 0)
		{
			String countStr = "" + idxArr.length;
			String infoMsg = "Are you sure you want to delete " + countStr + " structures?";
			int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(this), infoMsg, "Confirm Deletion",
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.NO_OPTION)
				return;
		}

		// Remove the structures
		structureModel.removeStructures(idxArr);

		// Update internal state vars
		pickManager.setPickMode(PickManager.PickMode.DEFAULT);
		structureModel.activateStructure(-1);

		// Update GUI
		updateControlGui();
		editB.setSelected(false);
	}
	
	/**
	 * Helper method to update the (selected) models to reflect the user selected font size. 
	 */
	private void doUpdateFontSize()
	{
		if (fontSizeNFS.isValidInput() == false)
			return;

		// Retrieve the fontSize
		int fontSize = (int)fontSizeNFS.getValue();

		// Update the relevant structures
		int[] idxArr = structuresTable.getSelectedRows();
		structureModel.setLabelFontSize(idxArr, fontSize);
	}
	
	/**
	 * Helper method to update the specified structures to reflect the user selected line width.
	 */
	private void doUpdateLineWidth()
	{
		if (lineWidthNFS.isValidInput() == false)
			return;

		// Retrieve the lineWidth
		int lineWidth = (int)lineWidthNFS.getValue();

		// TODO: Currently individual (sub)structures can not have different line widths from
		// TODO: their parent (super) structure. In the future that may be a request
		// Update the relevant structures
//		int[] idxArr = structuresTable.getSelectedRows();
//		for (int aIdx : idxArr)
//			structureModel.setLineWidth(aIdx, lineWidth);
		structureModel.setLineWidth(lineWidth);
	}

	/**
	 * Helper method to update the colored icons/text for labelIconB and structIconB
	 */
	private void updateColoredButtons()
	{
		// Update the label / struct colors
		Icon labelIcon = null;
		Icon structIcon = null;

		int[] idxArr = structureModel.getSelectedStructures();
		boolean isEnabled = idxArr.length > 0;
		if (isEnabled == true)
		{
			// These values may need to be fiddled with if there are sizing issues
			int iconW = (int) (structColorB.getWidth() * 0.60);
			int iconH = (int) (structColorB.getHeight() * 0.50);

			// Determine label color attributes
			boolean isMixed = false;
			Color tmpColor = structureModel.getLabelColor(idxArr[0]);
			for (int aIdx : idxArr)
				isMixed |= Objects.equals(tmpColor, structureModel.getLabelColor(aIdx)) == false;

			if (isMixed == false)
				labelIcon = new ColorIcon(tmpColor, Color.BLACK, iconW, iconH);
			else
				labelIcon = new ColorIcon(Color.LIGHT_GRAY, Color.GRAY, iconW, iconH);

			// Determine structure color attributes
			isMixed = false;
			tmpColor = structureModel.getStructureColor(idxArr[0]);
			for (int aIdx : idxArr)
				isMixed |= Objects.equals(tmpColor, structureModel.getStructureColor(aIdx)) == false;

			if (isMixed == false)
				structIcon = new ColorIcon(tmpColor, Color.BLACK, iconW, iconH);
			else
				structIcon = new ColorIcon(Color.LIGHT_GRAY, Color.GRAY, iconW, iconH);
		}

		labelColorB.setIcon(labelIcon);
		structColorB.setIcon(structIcon);
	}

	/**
	 * Helper method to update control UI. Triggered when selection has changed.
	 */
	public void updateControlGui()
	{
		boolean isEnabled;

		int[] idxArr = structureModel.getSelectedStructures();

		isEnabled = idxArr.length < structuresTable.getRowCount();
		selectAllB.setEnabled(isEnabled);

		isEnabled = idxArr.length > 0;
		selectNoneB.setEnabled(isEnabled);
		labelColorB.setEnabled(isEnabled);
		structColorB.setEnabled(isEnabled);

		isEnabled = idxArr.length > 0 && editB.isSelected() == false;
		deleteB.setEnabled(isEnabled);
		labelTitleL.setEnabled(isEnabled);
		labelHideB.setEnabled(isEnabled);
		labelShowB.setEnabled(isEnabled);
//		labelColorB.setEnabled(isEnabled);
		structTitleL.setEnabled(isEnabled);
		structHideB.setEnabled(isEnabled);
		structShowB.setEnabled(isEnabled);
//		structColorB.setEnabled(isEnabled);
		updateColoredButtons();

		isEnabled = idxArr.length > 0;
		fontSizeNFS.setEnabled(isEnabled);

		int fontSize = -1;
		if (idxArr.length > 0)
			fontSize = structureModel.getLabelFontSize(idxArr[0]);
		fontSizeNFS.setValue(fontSize);

		double lineWidth = -1;
		lineWidth = structureModel.getLineWidth();
		// TODO: Enable if lineWidths are customizable on a per (sub)structure basis
//		if (idxArr.length > 0)
//			lineWidth = structureModel.getLineWidth();
		lineWidthNFS.setValue(lineWidth);
	}

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
    		((DefaultTableModel) structuresTable.getModel()).setRowCount(structureModel.getNumberOfStructures());

            updateStructureTable();

            if (structureModel.supportsActivation())
            {
                int idx = structureModel.getActivatedStructureIndex();
                if (idx >= 0)
                {
                    pickManager.setPickMode(pickMode);
                    if (!editB.isSelected())
                        editB.setSelected(true);
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
                    if (editB.isSelected())
                        editB.setSelected(false);
                    structuresTable.setEnabled(true);
                }
            }
        }
        else if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            // If we're editing, say, a path, return immediately.
            if (structureModel.supportsActivation() && editB.isSelected())
            {
                return;
            }

            PickEvent e = (PickEvent) evt.getNewValue();
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
                    if (((e.getMouseEvent().getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) || ((e.getMouseEvent().getModifiers() & keyMask) == keyMask))
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
                    structuresTable.removeRowSelectionInterval(0, count - 1);
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
            int idx = (Integer) evt.getNewValue();

            updateStructureTable();

            int numStructures = structuresTable.getRowCount();
            if (numStructures > 0)
            {
                if (idx > numStructures - 1)
                {
                    structuresTable.setRowSelectionInterval(numStructures - 1, numStructures - 1);
                    structuresTable.scrollRectToVisible(structuresTable.getCellRect(numStructures - 1, 0, true));
                }
                else
                {
                    structuresTable.setRowSelectionInterval(idx, idx);
                    structuresTable.scrollRectToVisible(structuresTable.getCellRect(idx, 0, true));
                }
            }
        }
        else if (Properties.ALL_STRUCTURES_REMOVED.equals(evt.getPropertyName()) || Properties.COLOR_CHANGED.equals(evt.getPropertyName()))
        {
            updateStructureTable();
        }
        else if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();
            structuresLoadingProgressMonitor.setProgress(progress);
            String message =
                String.format("Completed %d%%.\n", progress);
            structuresLoadingProgressMonitor.setNote(message);
            if (structuresLoadingProgressMonitor.isCanceled() || task.isDone()) {
                if (structuresLoadingProgressMonitor.isCanceled()) {
                    task.cancel(true);
                } else {
//                    taskOutput.append("Task completed.\n");
                }
            }
//            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    private void updateStructureTable()
    {
        int numStructures = structureModel.getNumberOfStructures();

        ((DefaultTableModel) structuresTable.getModel()).setRowCount(numStructures);
        for (int i = 0; i < numStructures; ++i)
        {
            StructureModel.Structure structure = structureModel.getStructure(i);
            int[] c = structure.getColor();
            ((DefaultTableModel) structuresTable.getModel()).setValueAt(String.valueOf(structure.getId()), i, 0);
            ((DefaultTableModel) structuresTable.getModel()).setValueAt(structure.getType(), i, 1);
            ((DefaultTableModel) structuresTable.getModel()).setValueAt(structure.getName(), i, 2);
            ((DefaultTableModel) structuresTable.getModel()).setValueAt(structure.getInfo(), i, 3);
            ((DefaultTableModel) structuresTable.getModel()).setValueAt(new Color(c[0], c[1], c[2]), i, 4);
            ((DefaultTableModel) structuresTable.getModel()).setValueAt(structure.getLabel(), i, 5);
            //structuresTable.setValueAt(structure.getLabelHidden(), i, 6);
            //structuresTable.setValueAt(structure.getHidden(), i, 6);
        }

        updateColoredButtons();
    }

    public void tableChanged(TableModelEvent e)
    {
   	 // Retrieve the model colum index
   	 int colNum = e.getColumn();
   	 
        if (colNum == 2)
        {
            int row = e.getFirstRow();
            int col = e.getColumn();
            StructureModel.Structure structure = structureModel.getStructure(row);
            String name = (String) structuresTable.getValueAt(row, col);
            if (name != null && !name.equals(structure.getName()))
            {
                structure.setName(name);
            }
        }
        if (colNum == 5)
        {
            int row = e.getFirstRow();
            int col = e.getColumn();
            StructureModel.Structure structure = structureModel.getStructure(row);
            String label = (String) structuresTable.getValueAt(row, col);
            if (label != null && !label.equals(structure.getLabel()))
            {
                structure.setLabel(label);
                structureModel.setStructureLabel(row, label);
            }
        }
        /*if(colNum ==6)
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
        if(colNum==7)
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
            //        		if (structuresTable.getSelectedRows().length == 0) structuresTable.clearSelection();
            structureModel.selectStructures(structuresTable.getSelectedRows());
            updateControlGui();
        }
    }

    public void setEditingEnabled(boolean enable)
    {
        if (enable)
        {
            structureModel.setVisible(true); // in case user hid everything, make it visible again

            if (!editB.isSelected())
                editB.setSelected(true);
        }
        else
        {
            if (editB.isSelected())
                editB.setSelected(false);
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
                    editB.setSelected(false);
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
        
        updateControlGui();
    }

    private void structuresTableMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger() && !editB.isSelected())
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

    class PopupListener extends MouseAdapter     // only if esri support is enabled
    {
        JPopupMenu menu;

        public PopupListener(JPopupMenu menu)
        {
            this.menu = menu;
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            maybeShowPopup(e);
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            if (e.getSource() == menu)
                menu.setVisible(false);
        }

        private void maybeShowPopup(MouseEvent e)
        {
            if (e.isPopupTrigger())
                menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    class SaveAsPopupAction extends AbstractAction  // only if esri support is enabled
    {
        public SaveAsPopupAction()
        {
            super("Save as...");
        }

        @Override
        public void actionPerformed(ActionEvent e) // convert the button press into a mouse event so popup is properly shown
        {
            Point p = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(p, (Component) e.getSource());
            saveAsPopupListener.mousePressed(new MouseEvent(AbstractStructureMappingControlPanel.this, MouseEvent.MOUSE_PRESSED, e.getWhen(), e.getModifiers(), p.x, p.y, 1, true));
        }
    }

    class SaveSbmtStructuresFileAction extends AbstractAction // only if esri support is enabled
    {
        public SaveSbmtStructuresFileAction()
        {
            super("SBMT Structures File...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            File file = structuresFile;
            if (file != null)
            {
                // File already exists, use it as the default filename
                file = CustomFileChooser.showSaveDialog(AbstractStructureMappingControlPanel.this, "Select File", file.getName());
            }
            else
            {
                // We don't have a default filename to provide
                file = CustomFileChooser.showSaveDialog(AbstractStructureMappingControlPanel.this, "Select File");
            }

            if (file != null)
            {
                try
                {
                    structureModel.saveModel(file);
                    structuresFileL.setText(file.getAbsolutePath());
                    structuresFile = file;
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(AbstractStructureMappingControlPanel.this), "There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

                    ex.printStackTrace();
                }
            }

        }
    }

    class SaveEsriShapeFileAction extends AbstractAction  // only if esri support is enabled
    {
        public SaveEsriShapeFileAction()
        {
            super("ESRI Shapefile Datastore...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            List<SimpleFeature> features = Lists.newArrayList();
            List<EllipseStructure> ellipses = EllipseStructure.fromSbmtStructure((AbstractEllipsePolygonModel) modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES));
            List<EllipseStructure> circles = EllipseStructure.fromSbmtStructure((AbstractEllipsePolygonModel) modelManager.getModel(ModelNames.CIRCLE_STRUCTURES));
            List<EllipseStructure> points = EllipseStructure.fromSbmtStructure((AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.POINT_STRUCTURES));
            List<LineStructure> polygons = LineStructure.fromSbmtStructure((LineModel) modelManager.getModel(ModelNames.POLYGON_STRUCTURES));
            List<LineStructure> lines = LineStructure.fromSbmtStructure((LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES));
            for (int i = 0; i < ellipses.size(); i++)
                features.add(FeatureUtil.createFeatureFrom(ellipses.get(i)));
            for (int i = 0; i < circles.size(); i++)
                features.add(FeatureUtil.createFeatureFrom(circles.get(i)));
            for (int i=0; i<points.size(); i++)
                features.add(FeatureUtil.createFeatureFrom(points.get(i)));
            for (int i=0; i<polygons.size(); i++)
                features.add(FeatureUtil.createFeatureFrom(polygons.get(i)));
            for (int i=0; i<lines.size(); i++)
                features.add(FeatureUtil.createFeatureFrom(lines.get(i)));
            File file=CustomFileChooser.showSaveDialog(AbstractStructureMappingControlPanel.this, "Datastore filename", "myDataStore.shp", "shp");
            if (file!=null)
                new HeterogeneousShapefileDatastoreDumper(features).write(file.toPath());
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

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (row >= structureModel.getNumberOfStructures()) return c;
            if (structureModel.isStructureVisible(row) == false)
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

	class StructureLabelRenderer extends DefaultTableCellRenderer
	{
		private Color selectionForeground;

		public StructureLabelRenderer()
		{
			UIDefaults defaults = UIManager.getDefaults();
			selectionForeground = defaults.getColor("Table.selectionForeground");
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (row >= structureModel.getNumberOfStructures()) return c;
			boolean isVisible = structureModel.isStructureVisible(row) && structureModel.isLabelVisible(row);
			if (isVisible == false)
				c.setForeground(Color.GRAY);
			else if (isSelected == true)
				c.setForeground(selectionForeground);
			else
				c.setForeground(Color.BLACK);

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

        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column)
        {
            Color newColor = (Color) color;
            setBackground(newColor);

            if (isSelected)
            {
                if (selectedBorder == null)
                {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            }
            else
            {
                if (unselectedBorder == null)
                {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                }
                setBorder(unselectedBorder);
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue());

            return this;
        }
    }

    class StructuresTableModel extends DefaultTableModel
    {
        public StructuresTableModel(String[] columnNames)
        {
            super(columnNames, 0);
        }

        public boolean isCellEditable(int row, int column)
        {
            if (column == 2 || column == 5) //|| column ==6 || column ==7)
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
                Color color = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(structuresTable), structureModel.getStructure(row).getColor());

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
