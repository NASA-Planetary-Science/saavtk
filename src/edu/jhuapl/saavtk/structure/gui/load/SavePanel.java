package edu.jhuapl.saavtk.structure.gui.load;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.geotools.feature.DefaultFeatureCollection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import edu.jhuapl.saavtk.gui.util.Colors;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.StructuresExporter;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.esri.FeatureUtil;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Point;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureType;
import edu.jhuapl.saavtk.structure.io.BennuStructuresEsriIO;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.structure.io.StructureSaveUtil;
import edu.jhuapl.saavtk.structure.io.XmlLoadUtil;
import glum.gui.FocusUtil;
import glum.gui.GuiUtil;
import glum.gui.action.ClickAction;
import glum.gui.component.GComboBox;
import glum.gui.component.GTextField;
import glum.gui.panel.GlassPanel;
import glum.io.Loader;
import glum.io.LoaderInfo;
import glum.task.BufferTask;
import glum.task.NotifyTask;
import glum.task.Task;
import glum.task.TaskListener;
import glum.unit.NumberUnit;
import glum.unit.TimeCountUnit;
import glum.util.ThreadUtil;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to save a collection of {@link Structure}.
 * <p>
 * This panel provides the following features:
 * <ul>
 * <li>Ability to specify the save file format.
 * <li>Ability to specify the {@link Structure}s types to be saved.
 * <li>Responsive status feedback associated with user input.
 * </ul>
 *
 * @author lopeznr1
 */
public class SavePanel extends GlassPanel implements ActionListener, ItemListener, TaskListener
{
	// Constants
	private static final String ERR_MSG_NO_STRUCTURES_AVAILABLE = "There are no structures to save.";
	private static final String ERR_MSG_NO_STRUCTURES_SELECTED = "Please select at least one of the available structure types.";

	// Ref vars
	private final PolyhedralModel refSmallBody;
	private final AnyStructureManager refStructureManager;

	// State vars
	private ImmutableList<Structure> fullItemL;
	private ImmutableList<Structure> pickItemL;
	private ConcurrentHashMap<File, Result> fileResultM;
	private LoaderInfo locationLI;
	private String initMsg;

	// GUI vars
	private final GComboBox<FormatType> formatBox;
	private final JLabel targetItemsL;
	private final JRadioButton targetItemsAllRB, targetItemsPickRB;
	private final JButton locationB;
	private final GTextField locationTF;
	private final GTextField baseNameTF;

	private final JCheckBox circleCountCB;
	private final JCheckBox ellipseCountCB;
	private final JCheckBox polygonCountCB;
	private final JCheckBox pathCountCB;
	private final JCheckBox pointCountCB;

	private final NotifyTask workTask;
	private final JTextArea infoTA;
	private final JLabel statusL;

	private final JCheckBox allowOverwriteCB;
	private final JButton acceptB, cancelB, closeB;

	/** Standard Constructor */
	public SavePanel(Component aParent, PolyhedralModel aSmallBody, AnyStructureManager aStructureManager)
	{
		super(aParent);

		refSmallBody = aSmallBody;
		refStructureManager = aStructureManager;

		fullItemL = ImmutableList.of();
		pickItemL = ImmutableList.of();
		fileResultM = new ConcurrentHashMap<>();
		locationLI = new LoaderInfo();
		initMsg = "";

		// Form the GUI
		setLayout(new MigLayout("", "[]", "[]"));
		setBorder(new BevelBorder(BevelBorder.RAISED));

		var titleL = new JLabel("Save Structures", JLabel.CENTER);
		add(titleL, "growx,span,wrap");

		pathCountCB = GuiUtil.createJCheckBox("Paths: ", this);
		polygonCountCB = GuiUtil.createJCheckBox("Polygons: ", this);
		circleCountCB = GuiUtil.createJCheckBox("Circles: ", this);
		ellipseCountCB = GuiUtil.createJCheckBox("Ellipses: ", this);
		pointCountCB = GuiUtil.createJCheckBox("Points: ", this);
		var eastPanel = formPanelTypesAndCounts();

		formatBox = new GComboBox<>(this, FormatType.values());
		targetItemsL = new JLabel("Target Items:");
		targetItemsAllRB = GuiUtil.createJRadioButton(this, "All");
		targetItemsPickRB = GuiUtil.createJRadioButton(this, "Selected");
		GuiUtil.linkRadioButtons(targetItemsAllRB, targetItemsPickRB);
		locationB = GuiUtil.createJButton(IconUtil.getActionFolder(), this);
		locationTF = new GTextField(this);
		baseNameTF = new GTextField(this);
		allowOverwriteCB = GuiUtil.createJCheckBox("Allow Overwrites", this);
		var westPanel = formPanelLocationAndFormat();

		add(westPanel, "ay top,growx,pushx");
		add(GuiUtil.createDivider(), "growy,w 4!");
		add(eastPanel, "ay top,w :170:,wrap");

		// Info area
		infoTA = GuiUtil.createUneditableTextArea(2, 0);
		infoTA.setTabSize(3);
		infoTA.setLineWrap(false);
		var tmpPane = new JScrollPane(infoTA);
		add(tmpPane, "growx,growy,h :170:,w :570:,pushy,span,wrap");

		// Control area
		workTask = new NotifyTask(this);
		cancelB = GuiUtil.formButton(this, "Cancel");
		acceptB = GuiUtil.formButton(this, "Accept");
		closeB = GuiUtil.formButton(this, "Close");
		statusL = new JLabel();
		add(statusL, "span,split,growx");
		add(cancelB, "ax right");
		add(acceptB, "");
		add(closeB, "");

		// Set up keyboard short cuts
		FocusUtil.addAncestorKeyBinding(this, "ESCAPE", new ClickAction(cancelB));
		FocusUtil.addAncestorKeyBinding(this, "ENTER", new ClickAction(closeB));
	}

	/**
	 * Sets in the structures to be saved.
	 * <p>
	 * This will cause the panel to enter a reset state where all items are to be saved in the SBMT format.
	 */
	public void setStructuresToSave(Collection<Structure> aFullItemC, Collection<Structure> aPickItemC)
	{
		fullItemL = ImmutableList.copyOf(aFullItemC);
		pickItemL = ImmutableList.copyOf(aPickItemC);
		fileResultM = new ConcurrentHashMap<>();

		formatBox.setChosenItem(FormatType.SBMT);
		targetItemsAllRB.setSelected(true);
		allowOverwriteCB.setSelected(false);
		workTask.reset();

		GuiUtil.setEnabled(true, targetItemsL, targetItemsAllRB, targetItemsPickRB, formatBox, locationB, locationTF,
				baseNameTF);
		GuiUtil.setEnabled(true, circleCountCB, ellipseCountCB, pointCountCB, pathCountCB, polygonCountCB);

		updatePanelTypesAndCounts(true);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		var source = aEvent.getSource();
		if (source == acceptB)
			doActionAccept();
		else if (source == cancelB)
			doActionCancel();
		else if (source == closeB)
			doActionClose();
		else if (source == locationB)
			doActionLocation();

		updateGui();
	}

	@Override
	public void itemStateChanged(ItemEvent aEvent)
	{
		var source = aEvent.getSource();
		if (source == targetItemsAllRB || source == targetItemsPickRB)
			updatePanelTypesAndCounts(false);

		updateGui();
	}

	@Override
	public void taskUpdate(Task aTask)
	{
		var tmpMsg = formStatusMsg(workTask);
		SwingUtilities.invokeLater(() -> statusL.setText(tmpMsg));
	}

	/**
	 * Helper method to handle the accept action.
	 */
	private void doActionAccept()
	{
		// Progress the task out of the init state
		workTask.markInitDone();

		// Update UI to reflect the active save
		acceptB.setEnabled(false);
		GuiUtil.setEnabled(false, targetItemsL, targetItemsAllRB, targetItemsPickRB, formatBox, locationB, locationTF,
				baseNameTF, allowOverwriteCB);
		GuiUtil.setEnabled(false, circleCountCB, ellipseCountCB, pointCountCB, pathCountCB, polygonCountCB);

		// Save the various structures
		var fileSaveMM = getFileToStructsSaveMap();
		var tmpThread = new Thread(() -> saveStructuresToFilesMap(fileSaveMM, workTask));
		tmpThread.start();
	}

	/**
	 * Helper method to handle the cancel action.
	 */
	private void doActionCancel()
	{
		if (workTask.isInit() == true)
			setVisible(false);

		workTask.abort();
	}

	/**
	 * Helper method to handle the close action.
	 */
	private void doActionClose()
	{
		setVisible(false);

		workTask.reset();
		workTask.abort();
	}

	/**
	 * Helper method to process the "specify destination path" action.
	 */
	private void doActionLocation()
	{
		// Retrieve the path to load
		var tmpPath = Loader.queryUserForPath(locationLI, getParent(), "Specify the destination folder", true);
		if (tmpPath == null)
			return;

		locationTF.setValue(tmpPath.getAbsolutePath());
	}

	/**
	 * Helper method that allows the user to specify the file format and location.
	 */
	private JPanel formPanelLocationAndFormat()
	{
		var retPanel = new JPanel(new MigLayout("", "[]", "[]"));

		retPanel.add(targetItemsL, "ax right");
		retPanel.add(targetItemsAllRB, "span,split");
		retPanel.add(targetItemsPickRB, "wrap");

		var formatL = new JLabel("Save Format:");
		retPanel.add(formatL, "ax right");
		retPanel.add(formatBox, "wrap");

		var locationL = new JLabel("Location:");
		retPanel.add(locationL, "ax right");
		retPanel.add(locationTF, "split 2,growx,pushx,w 100::");
		retPanel.add(locationB, "w 24!,h 24!,wrap");

		var nameL = new JLabel("Base Name:");
		retPanel.add(nameL, "ax right");
		retPanel.add(baseNameTF, "growx,wrap");

		retPanel.add(allowOverwriteCB, "span,split");

		return retPanel;
	}

	/**
	 * Helper method to form the panel which details the structure types and counts.
	 */
	private JPanel formPanelTypesAndCounts()
	{
		var retPanel = new JPanel(new MigLayout("", "", "[]"));
		retPanel.add(pathCountCB, "wrap");
		retPanel.add(polygonCountCB, "wrap");
		retPanel.add(circleCountCB, "wrap");
		retPanel.add(ellipseCountCB, "wrap");
		retPanel.add(pointCountCB, "");

		return retPanel;
	}

	/**
	 * Helper method that returns the appropriate progress message.
	 */
	private String formStatusMsg(NotifyTask aTask)
	{
		// Display nothing if the task is in the init state
		if (aTask.isInit() == true)
			return "";

		if (aTask.isDone() == true)
		{
			var cntSaveFail = 0;
			for (var aResult : fileResultM.values())
			{
				if (aResult.error() != null)
					cntSaveFail++;
			}

			if (cntSaveFail > 0)
				return "Failure: " + cntSaveFail + " files";
		}

		var progress = aTask.getProgress();
		var perNU = new NumberUnit("", "", 1.0, "0.00 %");
		var retStr = "Progress: " + perNU.getString(progress);

		var runTime = (Long) null;
		if (aTask.isActive() == true || progress >= 0)
			runTime = aTask.getTimeEnd() - aTask.getTimeBeg();
		var tmpTCU = new TimeCountUnit(2);
		retStr += "    Time: " + tmpTCU.getString(runTime);

		return retStr;
	}

	/**
	 * Returns the file as specified in the location gui.
	 * <p>
	 * Returns null if the input is empty.
	 */
	private File getDestPath()
	{
		// Retrieve the folder
		var locationStr = locationTF.getText().strip();
		if (locationStr.isEmpty() == true)
			return null;

		if (locationStr.equals("~") == true)
			locationStr = System.getProperty("user.home");
		if (locationStr.startsWith("~/") == true)
			locationStr = new File(System.getProperty("user.home"), locationStr.substring(2)).getAbsolutePath();

		var retDestPath = new File(locationStr);
		return retDestPath;
	}

	/**
	 * Helper method that returns a mapping of File to the list of {@link Structure}s to be saved.
	 */
	private Multimap<File, Structure> getFileToStructsSaveMap()
	{
		var destPath = getDestPath();
		var baseName = baseNameTF.getText().strip();
		var formatType = formatBox.getChosenItem();

		var retFileMM = LinkedHashMultimap.<File, Structure>create();
		for (var aItem : getItemsToSave())
		{
			// Get the specific extension based on structure type
			var specExt = "";
			if (aItem instanceof Polygon)
				specExt = ".polygons";
			else if (aItem instanceof PolyLine)
				specExt = ".paths";
			else if (aItem instanceof Point)
				specExt = ".points";
			else if (aItem instanceof Ellipse aEllipse)
			{
				if (aEllipse.getType() == StructureType.Ellipse)
					specExt = ".ellipses";
				else if (aEllipse.getType() == StructureType.Circle)
					specExt = ".circles";
				else
					throw new Error("Unsupported mode: " + aEllipse.getType());
			}
			else
				throw new Error("Unsupported Structure: " + aItem.getClass() + " -> type: " + aItem.getType());

			var tmpFile = (File) null;
			if (formatType == FormatType.SBMT)
			{
				if (aItem instanceof Ellipse || aItem instanceof Point)
					tmpFile = new File(destPath, baseName + specExt + ".txt");
				else if (aItem instanceof PolyLine || aItem instanceof Polygon)
					tmpFile = new File(destPath, baseName + specExt + ".xml");
				else
					throw new Error("Unsupported Structure: " + aItem.getClass() + " -> type: " + aItem.getType());
			}
			else if (formatType == FormatType.ESRI)
			{
				tmpFile = new File(destPath, baseName + specExt + ".shp");
			}
			else if (formatType == FormatType.VTK_Polydata)
			{
				// Saving of VTK polydata only works with paths, polygons
				if (aItem instanceof PolyLine)
					tmpFile = new File(destPath, baseName + specExt + ".vtk");
				else
					continue;
			}

			retFileMM.put(tmpFile, aItem);
		}

		return retFileMM;
	}

	/**
	 * Helper method that returns a list of all the errors.
	 */
	private List<String> getIssueFailList()
	{
		var retIssueL = new ArrayList<String>();

		// Determine if there are structures available and chosen to save
		var tmpItemL = fullItemL;
		if (targetItemsPickRB.isSelected() == true)
			tmpItemL = pickItemL;

		var numItemsAvail = tmpItemL.size();
		var numItemsToSave = getItemsToSave().size();
		var fileSaveMM = getFileToStructsSaveMap();
		if (numItemsAvail == 0)
			retIssueL.add(ERR_MSG_NO_STRUCTURES_AVAILABLE);
		else if (numItemsAvail > 0 && numItemsToSave == 0)
			retIssueL.add(ERR_MSG_NO_STRUCTURES_SELECTED);
		else if (fileSaveMM.size() == 0)
			retIssueL.add("The save format is not compatible with the specified structure types.");

		// Determine if there are issues with the location
		var destPath = getDestPath();
		var rootPath = destPath;
		while (rootPath != null)
		{
			// Bail once we locate the first directory
			if (rootPath.isDirectory() == true)
				break;

			// Bail if the specified path exists (and is not a directory)
			if (rootPath.exists() == true)
				break;

			rootPath = rootPath.getParentFile();
		}

		// Determine the errors
		if (rootPath == null)
			retIssueL.add("Invalid location.");
		else if (rootPath.exists() == true && rootPath.isDirectory() == false)
			retIssueL.add("Specified location does not resolve to a folder.");
		else if (rootPath.canWrite() == false)
			retIssueL.add("No write permission at location.");
		else if (destPath.exists() == true && destPath.isDirectory() == false)
			retIssueL.add("Specified location is not a folder.");
		else if (destPath.exists() == false)
		{
			// Ensure there is not whitespace at front or end of any folder path we will create
			var tmpPath = destPath;
			while (tmpPath != null && tmpPath.equals(rootPath) == false)
			{
				var tmpName = tmpPath.getName();
				if (tmpName.stripLeading() != tmpName)
				{
					retIssueL.add("Whitespace is not allowed at the front of a folder name: " + destPath);
					break;
				}
				else if (tmpName.stripTrailing() != tmpName)
				{
					retIssueL.add("Whitespace is not allowed at the end of a folder name: " + destPath);
					break;
				}

				tmpPath = tmpPath.getParentFile();
			}
		}

		var baseName = baseNameTF.getText().strip();
		if (baseName.isEmpty() == true)
			retIssueL.add("Please specify a base name.");
		else if (baseName.matches("[\\/]") == true)
			retIssueL.add("The specified base name is not valid.");

		return retIssueL;
	}

	/**
	 * Helper method that returns a list of all the warnings.
	 */
	private List<String> getIssueWarnList()
	{
		var retIssueL = new ArrayList<String>();

		// Warn the user if the top-level-folder folder needs to be created
		var destPath = getDestPath();
		if (destPath != null && destPath.exists() == false)
			retIssueL.add("The location (folder) does not exist and will be created.");

		// Warn the user of the experimental SBMT ESRI format
		var formatType = formatBox.getChosenItem();
		if (formatType == FormatType.ESRI)
			retIssueL.add("SBMT ESRI structure format is experimental.");

		// Determine if the structure types and format are not compatible
		var nonVtkTypeL = new ArrayList<String>();
		if (circleCountCB.isEnabled() == true && circleCountCB.isSelected() == true)
			nonVtkTypeL.add("circle");
		if (ellipseCountCB.isEnabled() == true && ellipseCountCB.isSelected() == true)
			nonVtkTypeL.add("ellipse");
		if (pointCountCB.isEnabled() == true && pointCountCB.isSelected() == true)
			nonVtkTypeL.add("point");
		if (formatType == FormatType.VTK_Polydata && nonVtkTypeL.size() > 0)
			retIssueL.add("Format 'VTK Polydata' is not compatbile with structures of type: " //
					+ String.join(",", nonVtkTypeL));

		// Notify if files need to be overwritten
		var fileSaveMM = getFileToStructsSaveMap();
		var existFileS = new HashSet<File>();
		for (var aFile : fileSaveMM.keySet())
		{
			if (aFile.exists() == true)
				existFileS.add(aFile);
		}

		if (existFileS.size() > 0)
		{
			retIssueL.add("Files with an * will need to be overwritten. Count: " + existFileS.size());
			if (allowOverwriteCB.isSelected() == false)
				retIssueL.add("Please select 'Allow Overwrites' to proceed.");
		}

		return retIssueL;
	}

	/**
	 * Helper method that returns the {@link Structure}s to save.
	 */
	private List<Structure> getItemsToSave()
	{
		var tmpItemL = fullItemL;
		if (targetItemsPickRB.isSelected() == true)
			tmpItemL = pickItemL;

		var retItemL = new ArrayList<Structure>();
		if (pathCountCB.isSelected() == true)
			retItemL.addAll(StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Path));
		if (polygonCountCB.isSelected() == true)
			retItemL.addAll(StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Polygon));
		if (circleCountCB.isSelected() == true)
			retItemL.addAll(StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Circle));
		if (ellipseCountCB.isSelected() == true)
			retItemL.addAll(StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Ellipse));
		if (pointCountCB.isSelected() == true)
			retItemL.addAll(StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Point));

		return retItemL;
	}

	/**
	 * Helper method to save the list of {@link Structure}s to the specified file.
	 * <p>
	 * This method utilize the file extension to determine the output format. The following is handled:
	 * <ul>
	 * <li>.txt ===> Classic SBMT tab-separated-values (circles, ellipses, points)
	 * <li>.xml ===> Classic SBMT XML (polygons, paths)
	 * <li>.shp ===> ESRI shape file (any SBMT structure)
	 * <li>.paths.vtk ===> VTK polydata file (paths)
	 * </ul>
	 */
	private void saveStructuresToFile(Task aTask, File aFile, Collection<Structure> tmpItemL) throws Exception
	{
		// Ensure the top-level folder has been created
		var destPath = new File(locationTF.getText());
		destPath.mkdirs();
		if (destPath.exists() == false)
			throw new Error("Failed to create the locataion: " + destPath);
		if (destPath.isDirectory() == false)
			throw new Error("The specified destPath, " + destPath + ", is not a folder.");

		// Format is dictated by the file extension
		var name = aFile.getName();

		// Format: Classic SBMT: Circles, Ellipses, Points
		if (name.endsWith(".txt") == true)
		{
			var structureL = StructureMiscUtil.getItemsOfType(tmpItemL, null);
			StructureSaveUtil.saveModel(aTask, aFile, refStructureManager, structureL, refSmallBody);
		}

		// Format: Classic SBMT: PolyLines or Polygons
		else if (name.endsWith(".xml") == true)
		{
			if (name.endsWith(".paths.xml") == true)
			{
				var polylineL = StructureMiscUtil.getPathsFrom(tmpItemL);
				XmlLoadUtil.saveManager(aFile, polylineL, refSmallBody);
			}
			else if (name.endsWith(".polygons.xml") == true)
			{
				var polygonL = StructureMiscUtil.getPolygonsFrom(tmpItemL);
				XmlLoadUtil.saveManager(aFile, polygonL, refSmallBody);
			}
			else
				throw new Error("Invalid file name extension: " + name);
		}

		// Format: ESRI
		else if (name.endsWith(".shp") == true)
		{
			var ellipseDFC = new DefaultFeatureCollection();
			var pointDFC = new DefaultFeatureCollection();
			var lineDFC = new DefaultFeatureCollection();

			for (var aItem : tmpItemL)
			{
				if (aItem instanceof Ellipse aEllipse)
				{
					if (aEllipse.getType() == StructureType.Ellipse || aEllipse.getType() == StructureType.Circle)
					{
						var tmpEsriStruct = EllipseStructure.fromSbmtStructure(refStructureManager, aEllipse);
						ellipseDFC.add(FeatureUtil.createFeatureFrom(tmpEsriStruct));
					}
					else if (aEllipse.getType() == StructureType.Point)
						pointDFC.add(FeatureUtil.createFeatureFrom(new PointStructure(aEllipse.getCenter())));
					else
						throw new Error("Unsupported mode: " + aEllipse.getType());
				}
				else if (aItem instanceof Polygon aPolygon)
				{
					var tmpEsriStruct = LineStructure.fromSbmtStructure(refStructureManager, aPolygon);
					lineDFC.add(FeatureUtil.createFeatureFrom(tmpEsriStruct));
				}
				else if (aItem instanceof PolyLine aPolyLine)
				{
					var tmpEsriStruct = LineStructure.fromSbmtStructure(refStructureManager, aPolyLine);
					lineDFC.add(FeatureUtil.createFeatureFrom(tmpEsriStruct));
				}
				else
					throw new Error("Unsupported mode: " + aItem);
			}

			if (ellipseDFC.getCount() > 0)
				BennuStructuresEsriIO.write(aFile.toPath(), ellipseDFC, FeatureUtil.ellipseType);
			else if (pointDFC.getCount() > 0)
				BennuStructuresEsriIO.write(aFile.toPath(), pointDFC, FeatureUtil.pointType);
			else if (lineDFC.getCount() > 0)
				BennuStructuresEsriIO.write(aFile.toPath(), lineDFC, FeatureUtil.lineType);
		}

		// Format: VTK (paths, polygons)
		else if (name.endsWith(".paths.vtk") == true)
		{
			var saveAsMultipleFiles = false;
			var polylineL = StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Path);
			StructuresExporter.exportToVtkFile(refStructureManager, polylineL, aFile.toPath(), saveAsMultipleFiles);
		}
		else if (name.endsWith(".polygons.vtk") == true)
		{
			var saveAsMultipleFiles = false;
			var polygonL = StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Polygon);
			StructuresExporter.exportToVtkFile(refStructureManager, polygonL, aFile.toPath(), saveAsMultipleFiles);
		}

		else
			throw new Error("Invalid file name extension: " + name);
	}

	/**
	 * Helper method that save the {@link Structure}s associated with each file from the specified file-to-structure
	 * multimap.
	 */
	private void saveStructuresToFilesMap(Multimap<File, Structure> aFileSaveMM, Task aTask)
	{
		var fileL = new ArrayList<File>(aFileSaveMM.keySet());
		fileL.sort(null);

		var fullCnt = fileL.size() + 0.0;
		var progOff = 0;
		var progTot = 1.0 / fullCnt;

		for (var aFile : fileL)
		{
			var tmpItemL = aFileSaveMM.get(aFile);

			// Delegate the actual file save
			var tmpTask = new BufferTask();
			try
			{
				saveStructuresToFile(tmpTask, aFile, tmpItemL);
				fileResultM.put(aFile, new Result(null, tmpTask.getBuffer()));
			}
			catch (Throwable aExp)
			{
				fileResultM.put(aFile, new Result(aExp, tmpTask.getBuffer()));
			}

			if (aTask.isActive() == false)
				return;

			progOff += progTot;
			aTask.setProgress(progOff);
		}

		aTask.setProgress(1.0);
		aTask.abort();
		SwingUtilities.invokeLater(() -> updateGui());
	}

	/**
	 * Helper method that keeps the GUI synchronized with user input.
	 */
	private void updateGui()
	{
		// Update our internal LoaderInfo
		var destPath = getDestPath();
		if (destPath != null)
			locationLI.setPath(destPath);

		// Update logic dependent on the "init" state
		if (workTask.isInit() == true)
			updateGuiWhenInit();
		else
			updateGuiWhenDone();

		// Update the status label
		var cntSaveFail = 0;
		var cntSaveWarn = 0;
		for (var aResult : fileResultM.values())
		{
			if (aResult.error() != null)
				cntSaveFail++;
			else if (aResult.message().isBlank() == false)
				cntSaveWarn++;
		}
		var fgColor = Colors.getPassFG();
		if (cntSaveFail > 0 || cntSaveWarn > 0)
			fgColor = Colors.getFailFG();
		statusL.setForeground(fgColor);

		// Update action area
		var isEnabled = workTask.isDone() == false;
		cancelB.setEnabled(isEnabled);

		isEnabled = workTask.isDone() == true;
		closeB.setEnabled(isEnabled);

		workTask.forceUpdate();
	}

	/**
	 * Helper method to update the gui when the user is done setting up the {@link SavePanel}.
	 */
	private void updateGuiWhenDone()
	{
		var infoMsg = initMsg + "\n";
		for (var aFile : fileResultM.keySet())
		{
			var tmpResult = fileResultM.get(aFile);
			if (tmpResult.error() == null && tmpResult.message().isBlank() == true)
			{
				infoMsg += "Saved file: " + aFile.getName() + "\n";
			}
			else
			{
				if (tmpResult.error() != null)
					infoMsg += "Failure while saving file: " + aFile.getName() + "\n";
				else
					infoMsg += "Issue with saved file: " + aFile.getName() + "\n";

				var tmpMsg = tmpResult.message();
				if (tmpResult.error() != null)
					tmpMsg += ThreadUtil.getStackTraceClassic(tmpResult.error);
				infoMsg += "\t" + tmpMsg.replaceAll("\n", "\n\t") + "\n";
			}
		}

		infoMsg = infoMsg.trim() + "\n";
		infoTA.setText(infoMsg);
	}

	/**
	 * Helper method to update the gui when the user is setting up the {@link SavePanel} .
	 */
	private void updateGuiWhenInit()
	{
		if (workTask.isInit() == false)
			return;

		var issueFailL = getIssueFailList();
		var issueWarnL = getIssueWarnList();
		var existFileS = new HashSet<File>();

		// Update state var: initMsg
		initMsg = "";
		if (issueFailL.size() > 0)
		{
			initMsg += "Please correct the following issues:\n";
			for (var aMsg : issueFailL)
				initMsg += "\t - " + aMsg + "\n";
		}
		else
		{
			var saveItemL = getItemsToSave();
			initMsg += "Structures to save: " + saveItemL.size() + "\n\n";

			// Add any warnings
			if (issueWarnL.size() > 0)
			{
				initMsg += "Please note the following:\n";
				for (var aMsg : issueWarnL)
					initMsg += "\t - " + aMsg + "\n";
				initMsg += "\n";
			}

			// Determine the files that will be overwritten
			var fileSaveMM = getFileToStructsSaveMap();
			var fileL = new ArrayList<>(fileSaveMM.keySet());
			fileL.sort(null);

			for (var aFile : fileL)
			{
				if (aFile.exists() == true)
					existFileS.add(aFile);
			}

			// Log the files to be created or overwritten
			initMsg += "The following " + fileL.size() + " files will be written:\n";
			for (var aFile : fileL)
			{
				var flagStr = "";
				if (existFileS.contains(aFile) == true)
					flagStr = " * ";
				initMsg += "\t" + flagStr + aFile + "\n";
			}
			initMsg += "\n";
		}
		initMsg = initMsg.strip() + "\n";

		// Update the info area
		if (initMsg.equals(infoTA.getText()) == false)
		{
			infoTA.setText(initMsg);
			infoTA.setCaretPosition(0);
		}

		var isOverwriteNeeded = existFileS.size() > 0;
		var isEnabled = isOverwriteNeeded == true;
		isEnabled &= workTask.isInit() == true;
		allowOverwriteCB.setEnabled(isEnabled);

		// Update action area
		isEnabled = issueFailL.size() == 0;
		isEnabled &= allowOverwriteCB.isSelected() == true || isOverwriteNeeded == false;
		isEnabled &= workTask.isInit() == true;
		acceptB.setEnabled(isEnabled);
	}

	/**
	 * Updates the panel that lists the structures types and corresponding counts.
	 */
	private void updatePanelTypesAndCounts(boolean aIsReset)
	{
		var tmpItemL = fullItemL;
		if (targetItemsPickRB.isSelected() == true)
			tmpItemL = pickItemL;

		// Update GUI state of various tasks
		int cntPath = StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Path).size();
		var isEnabled = cntPath > 0;
		pathCountCB.setEnabled(isEnabled);
		if (aIsReset == true)
			pathCountCB.setSelected(isEnabled);
		pathCountCB.setText("Paths: " + cntPath);

		int cntPolygon = StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Polygon).size();
		isEnabled = cntPolygon > 0;
		polygonCountCB.setEnabled(isEnabled);
		if (aIsReset == true)
			polygonCountCB.setSelected(isEnabled);
		polygonCountCB.setText("Polygons: " + cntPolygon);

		int cntCircle = StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Circle).size();
		isEnabled = cntCircle > 0;
		circleCountCB.setEnabled(isEnabled);
		if (aIsReset == true)
			circleCountCB.setSelected(isEnabled);
		circleCountCB.setText("Circles: " + cntCircle);

		int cntEllipse = StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Ellipse).size();
		isEnabled = cntEllipse > 0;
		ellipseCountCB.setEnabled(isEnabled);
		if (aIsReset == true)
			ellipseCountCB.setSelected(isEnabled);
		ellipseCountCB.setText("Ellipses: " + cntEllipse);

		int cntPoint = StructureMiscUtil.getItemsOfType(tmpItemL, StructureType.Point).size();
		isEnabled = cntPoint > 0;
		pointCountCB.setEnabled(isEnabled);
		if (aIsReset == true)
			pointCountCB.setSelected(isEnabled);
		pointCountCB.setText("Points: " + cntPoint);

		updateGui();
	}

	/**
	 * Enumeration of supported SBMT structure file formats.
	 *
	 * @author lopeznr1
	 */
	enum FormatType
	{
		SBMT,

		ESRI,

		VTK_Polydata
	}

	/**
	 * Record to capture the results of an action.
	 *
	 * @author lopeznr1
	 */
	record Result(Throwable error, String message)
	{
	};

}
