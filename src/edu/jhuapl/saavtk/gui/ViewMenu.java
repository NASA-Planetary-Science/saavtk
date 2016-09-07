package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.dialog.ShapeModelImporterManagerDialog;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.Properties;

public class ViewMenu extends JMenu implements PropertyChangeListener
{
    private ViewManager rootPanel;
    private RecentlyViewed viewed;
    private JMenu customImageMenu;
    private ShapeModelImporterManagerDialog shapeModelImportedDialog;

    public ViewManager getRootPanel()
    {
        return rootPanel;
    }

    public void setRootPanel(ViewManager rootPanel)
    {
        this.rootPanel = rootPanel;
    }

    public RecentlyViewed getViewed()
    {
        return viewed;
    }

    public void setViewed(RecentlyViewed viewed)
    {
        this.viewed = viewed;
    }

    public ViewMenu(ViewManager rootPanel, RecentlyViewed viewed)
    {
        super("View");

        this.rootPanel = rootPanel;
        this.viewed=viewed;

        initialize();

        // Enable LODs checkbox menu item
        this.addSeparator();
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(new EnableLODsAction());
        cbmi.setSelected(true);
        this.add(cbmi);
    }

    protected void initialize()
    {
        customImageMenu = new JMenu("Custom Shape Models");
        this.add(customImageMenu);
        // Import shape models
//        if (Configuration.isAPLVersion())
//        {
        //this.addSeparator();

        JMenuItem mi = new JMenuItem(new ImportShapeModelsAction());
        customImageMenu.add(mi);

        if (rootPanel.getNumberOfCustomViews() > 0)
            customImageMenu.addSeparator();

        for (int i=0; i < rootPanel.getNumberOfCustomViews(); ++i)
        {
            View view = rootPanel.getCustomView(i);
            mi = new JMenuItem(new ShowBodyAction(view));
            mi.setText(view.getDisplayName());
            if (i==0)
                mi.setSelected(true);
//                group.add(mi);
            customImageMenu.add(mi);
//            }
        }

        for (int i=0; i < getRootPanel().getNumberOfBuiltInViews(); ++i)
        {
            View view = getRootPanel().getBuiltInView(i);
            mi = new JMenuItem(new ShowBodyAction(view));
            mi.setText(view.getDisplayName());
            if (i==0)
                mi.setSelected(true);

                ViewConfig smallBodyConfig = view.getConfig();

                addMenuItem(mi, smallBodyConfig);
        }
    }

    protected void addMenuItem(JMenuItem mi, ViewConfig config)
    {
         add(mi);
    }

    private void sortCustomMenuItems()
    {
        // First create a list of the custom menu items and remove them
        // from the menu.
        List<JMenuItem> customMenuItems = new ArrayList<JMenuItem>();
        int numberItems = customImageMenu.getItemCount();
        for (int i=numberItems-1; i>=0; --i)
        {
            JMenuItem item = customImageMenu.getItem(i);
            if (item != null)
            {
                Action action = item.getAction();
                if (action instanceof ShowBodyAction)
                {
                    customMenuItems.add(item);
                    customImageMenu.remove(item);
                }
            }
        }

        // Then sort them
        Collections.sort(customMenuItems, new Comparator<JMenuItem>()
        {
            @Override
            public int compare(JMenuItem o1, JMenuItem o2)
            {
                return o1.getText().compareTo(o2.getText());
            }
        });


        // Now add back in the items
        numberItems = customMenuItems.size();
        for (int i=0; i<numberItems; ++i)
        {
            JMenuItem item = customMenuItems.get(i);
            customImageMenu.add(item);
        }
    }

    public void addCustomMenuItem(View view)
    {
        if (getRootPanel().getNumberOfCustomViews() == 1)
            this.addSeparator();

        JMenuItem mi = new JRadioButtonMenuItem(new ShowBodyAction(view));
        mi.setText(view.getDisplayName());
        customImageMenu.add(mi);

        sortCustomMenuItems();
    }

    public void removeCustomMenuItem(View view)
    {
        int numberItems = customImageMenu.getItemCount();
        for (int i=2; i<numberItems; ++i)
        {
            JMenuItem item = customImageMenu.getItem(i);
            if (item != null)
            {
                Action action = item.getAction();
                if (action instanceof ShowBodyAction)
                {
                    ShowBodyAction showBodyAction = (ShowBodyAction) action;
                    if (view == showBodyAction.getView())
                    {
                        customImageMenu.remove(item);

                        // Remove the final separator if no custom models remain
                        if (getRootPanel().getNumberOfCustomViews() == 0)
                            removeFinalSeparator();

                        return;
                    }
                }
            }
        }
    }

    public void removeFinalSeparator()
    {
        int numberItems = this.getItemCount();
        for (int i=numberItems-1; i>=0; --i)
        {
            JMenuItem item = this.getItem(i);
            if (item == null)
            {
                this.remove(i);
                return;
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.CUSTOM_MODEL_ADDED.equals(evt.getPropertyName()))
        {
            String name = (String) evt.getNewValue();
            View view = getRootPanel().addCustomView(name);
            addCustomMenuItem(view);
        }
        else if (Properties.CUSTOM_MODEL_DELETED.equals(evt.getPropertyName()))
        {
            String name = (String) evt.getNewValue();
            View view = getRootPanel().removeCustomView(name);
            removeCustomMenuItem(view);
        }
        else if (Properties.CUSTOM_MODEL_EDITED.equals(evt.getPropertyName()))
        {
            String name = (String) evt.getNewValue();
            View view = getRootPanel().getCustomView("Custom/"+name);

            ModelManager modelManager = view.getModelManager();

            // If model manager is null, it means the model has not been displayed yet,
            // so no need to reset anything.
            if (modelManager != null)
            {
                PolyhedralModel smallBodyModel =
                        (PolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY);
                try
                {
                    smallBodyModel.reloadShapeModel();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    class ImportShapeModelsAction extends AbstractAction
    {
        public ImportShapeModelsAction()
        {
            super("Import Shape Models...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            if (shapeModelImportedDialog == null)
            {
                shapeModelImportedDialog = new ShapeModelImporterManagerDialog(null);
                shapeModelImportedDialog.addPropertyChangeListener(ViewMenu.this);
            }

            shapeModelImportedDialog.setLocationRelativeTo(getRootPanel());
            shapeModelImportedDialog.setVisible(true);
        }
    }


    class ShowBodyAction extends AbstractAction
    {
        private View view;
        public View getView() { return view; }

        public ShowBodyAction(View view)
        {
            super(view.getUniqueName());
            this.view = view;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            viewed.updateMenu(view.getUniqueName());
            getRootPanel().setCurrentView(view);
        }
    }

    class EnableLODsAction extends AbstractAction
    {
        public EnableLODsAction()
        {
            super("Enable LODs");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            Renderer.enableLODs = ((AbstractButton)actionEvent.getSource()).getModel().isSelected();
        }
    }

    protected JMenu getChildMenu(JMenu menu, String childName)
    {
        Component[] components = menu.getMenuComponents();
        for (Component comp : components)
        {
            if (comp instanceof JMenu)
            {
                if (((JMenu)comp).getText().equals(childName))
                    return (JMenu)comp;
            }
        }

        return null;
    }


}
