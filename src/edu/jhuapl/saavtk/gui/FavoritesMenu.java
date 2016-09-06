package edu.jhuapl.saavtk.gui;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class FavoritesMenu extends JMenu
{
    FavoritesFile favoritesFile;
    ViewManager manager;
    private static final char viewNameSpaceReplacementChar='-';

    public FavoritesMenu(FavoritesFile file, ViewManager manager)
    {
        super("Favorites");
        favoritesFile=file;
        this.manager=manager;
        rebuild();
    }

    private void rebuild()
    {
        removeAll();
        //
        JMenuItem add=new JMenuItem();
        JMenuItem rem=new JMenuItem();
        JMenuItem def=new JMenuItem();
        add.setAction(new AddFavoriteAction("Add current model to favorites"));
        rem.setAction(new RemoveFavoriteAction("Remove current model from favorites"));
        def.setAction(new SetDefaultModelAction("Set current model as default",manager));
        //

        // favorites

        JMenuItem favoritesItem=new JMenuItem("Favorite models:");
        favoritesItem.setEnabled(false);
        add(favoritesItem);

        List<String> stringsOnFile=favoritesFile.getAllFavorites();
        for (String viewName : stringsOnFile)
        {
            JMenuItem menuItem=new FavoritesMenuItem(viewName, manager);
            boolean isDefaultToLoad=unfilterViewName(viewName).equals(manager.getDefaultBodyToLoad());
            if (!isDefaultToLoad)
                add(menuItem);

        }

        // show default to load
        if (!stringsOnFile.isEmpty())
            add(new JSeparator());
        JMenuItem defaultItem=new JMenuItem("Default model:");
        defaultItem.setEnabled(false);
        add(defaultItem);

        String defaultToLoad = manager.getDefaultBodyToLoad();
        JMenuItem menuItem = new FavoritesMenuItem(defaultToLoad, manager);
        add(menuItem);

        //
        add(new JSeparator());
        add(add);
        add(rem);
        add(def);

    }

    private class FavoritesMenuItem extends JMenuItem
    {
        public FavoritesMenuItem(String viewName, ViewManager manager)
        {
            super(viewName);
            setAction(new ShowFavoriteAction(manager, viewName));
        }

    }

    private class ShowFavoriteAction extends AbstractAction
    {
        ViewManager manager;
        String viewName;

        public ShowFavoriteAction(ViewManager manager, String viewName)
        {
            super(viewName);
            this.manager=manager;
            this.viewName=viewName;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            for (int i=0; i<manager.getNumberOfBuiltInViews(); i++)
                if (manager.getBuiltInView(i).getUniqueName().equals(unfilterViewName(viewName)))
                    manager.setCurrentView(manager.getBuiltInView(i));
            for (int i=0; i<manager.getNumberOfCustomViews(); i++)
                if (manager.getCustomView(i).getUniqueName().equals(unfilterViewName(viewName)))
                    manager.setCurrentView(manager.getCustomView(i));
        }

    }

    private class AddFavoriteAction extends AbstractAction
    {
        public AddFavoriteAction(String desc)
        {
            super(desc);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            favoritesFile.addFavorite(filterViewName(manager.getCurrentView().getUniqueName()));
            rebuild();
        }
    }

    private class RemoveFavoriteAction extends AbstractAction
    {
        public RemoveFavoriteAction(String desc)
        {
            super(desc);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            favoritesFile.removeFavorite(filterViewName(manager.getCurrentView().getUniqueName()));
            if (manager.getDefaultBodyToLoad().equals(manager.getCurrentView().getUniqueName()))
                manager.resetDefaultBodyToLoad();
            rebuild();
        }
    }

    private class SetDefaultModelAction extends AbstractAction
    {

        ViewManager manager;

        public SetDefaultModelAction(String desc, ViewManager manager)
        {
            super(desc);
            this.manager=manager;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            manager.setDefaultBodyToLoad(manager.getCurrentView().getUniqueName());
            favoritesFile.addFavorite(filterViewName(manager.getCurrentView().getUniqueName()));    // automatically add current view to favorites if it already is
            rebuild();
        }

    }

    private String filterViewName(String str)   // JMenuItems don't display correctly if the text is too long and contains spaces, so replace spaces with |
    {
        return str.replace(' ',viewNameSpaceReplacementChar);
    }

    private String unfilterViewName(String str)
    {
        return str.replace(viewNameSpaceReplacementChar,' ');
    }

}
