package edu.jhuapl.saavtk.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.popupmenus.PopupManager;
import edu.jhuapl.saavtk.popupmenus.StructuresPopupMenu;

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
            final PickManager pickManager)
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
                        true) {});

                structureModel =
                        (StructureModel)modelManager.getModel(ModelNames.POLYGON_STRUCTURES);
                polygonStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                        modelManager,
                        structureModel,
                        pickManager,
                        PickManager.PickMode.POLYGON_DRAW,
                        (StructuresPopupMenu)popupManager.getPopup(structureModel),
                        true) {});

                structureModel =
                        (StructureModel)modelManager.getModel(ModelNames.CIRCLE_STRUCTURES);
                circleStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                        modelManager,
                        structureModel,
                        pickManager,
                        PickManager.PickMode.CIRCLE_DRAW,
                        (StructuresPopupMenu)popupManager.getPopup(structureModel),
                        true) {});

                structureModel =
                        (StructureModel)modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);
                ellipseStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                        modelManager,
                        structureModel,
                        pickManager,
                        PickManager.PickMode.ELLIPSE_DRAW,
                        (StructuresPopupMenu)popupManager.getPopup(structureModel),
                        true) {});

                pointsStructuresMapperPanel = new PointsMappingControlPanel(
                        modelManager,
                        pickManager,
                        StructuresControlPanel.this);

                addTab("Paths", lineStructuresMapperPanel);
                addTab("Polygons", polygonStructuresMapperPanel);
                addTab("Circles", circleStructuresMapperPanel);
                addTab("Ellipses", ellipseStructuresMapperPanel);
                addTab("Points", pointsStructuresMapperPanel);

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
