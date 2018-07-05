package edu.jhuapl.saavtk.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.opengis.feature.simple.SimpleFeature;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk.popup.StructuresPopupMenu;

public class StructuresControlPanel extends JTabbedPane
{
    private boolean initialized = false;
    private AbstractStructureMappingControlPanel lineStructuresMapperPanel;
    private AbstractStructureMappingControlPanel polygonStructuresMapperPanel;
    private AbstractStructureMappingControlPanel circleStructuresMapperPanel;
    private AbstractStructureMappingControlPanel ellipseStructuresMapperPanel;
    private AbstractStructureMappingControlPanel pointsStructuresMapperPanel;

    public StructuresControlPanel(
            final ModelManager modelManager,
            final PickManager pickManager, final boolean supportsEsri)
    {
        // Delay initializing components until user explicitly makes this visible.
        // This may help in speeding up loading the view.
        this.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent arg0)
            {
                if (initialized)
                    return;

                PopupManager popupManager = pickManager.getPopupManager();

                StructureModel structureModel =
                        (StructureModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);
                lineStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                        modelManager,
                        structureModel,
                        pickManager,
                        PickManager.PickMode.LINE_DRAW,
                        (StructuresPopupMenu)popupManager.getPopup(structureModel),
                        true, supportsEsri) {});

                structureModel =
                        (StructureModel)modelManager.getModel(ModelNames.POLYGON_STRUCTURES);
                polygonStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                        modelManager,
                        structureModel,
                        pickManager,
                        PickManager.PickMode.POLYGON_DRAW,
                        (StructuresPopupMenu)popupManager.getPopup(structureModel),
                        true, supportsEsri) {});

                structureModel =
                        (StructureModel)modelManager.getModel(ModelNames.CIRCLE_STRUCTURES);
                circleStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                        modelManager,
                        structureModel,
                        pickManager,
                        PickManager.PickMode.CIRCLE_DRAW,
                        (StructuresPopupMenu)popupManager.getPopup(structureModel),
                        true, supportsEsri) {});

                structureModel =
                        (StructureModel)modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);
                ellipseStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                        modelManager,
                        structureModel,
                        pickManager,
                        PickManager.PickMode.ELLIPSE_DRAW,
                        (StructuresPopupMenu)popupManager.getPopup(structureModel),
                        true, supportsEsri) {});

                pointsStructuresMapperPanel = new PointsMappingControlPanel(
                        modelManager,
                        pickManager,
                        StructuresControlPanel.this, supportsEsri);

                addTab("Paths", lineStructuresMapperPanel);
                addTab("Polygons", polygonStructuresMapperPanel);
                addTab("Circles", circleStructuresMapperPanel);
                addTab("Ellipses", ellipseStructuresMapperPanel);
                addTab("Points", pointsStructuresMapperPanel);
                
/*                addTab("", new JPanel());
                
                class OpenEsriFrameAction extends AbstractAction
                {
                    public OpenEsriFrameAction()
                    {
                        super("ESRI");
                    }
                    
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        try
                        {
                            SwingUtilities.invokeAndWait(new Runnable()
                            {
                                
                                @Override
                                public void run()
                                {
                                    Collection<SimpleFeature> features=Lists.newArrayList();
                                    new ESRIStructuresFrame(features, (GenericPolyhedralModel)modelManager.getModel(ModelNames.SMALL_BODY));
                                }
                            });
                        }
                        catch (InvocationTargetException | InterruptedException e1)
                        {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                    
                }
                
                setTabComponentAt(getTabCount()-1, new JButton(new OpenEsriFrameAction()));*/

                initialized = true;
            }

            public void componentHidden(ComponentEvent e)
            {
                if (initialized)
                {
                    lineStructuresMapperPanel.setEditingEnabled(false);
                    polygonStructuresMapperPanel.setEditingEnabled(false);
                    circleStructuresMapperPanel.setEditingEnabled(false);
                    ellipseStructuresMapperPanel.setEditingEnabled(false);
                    pointsStructuresMapperPanel.setEditingEnabled(false);
                }
            }
        });
    }
}
