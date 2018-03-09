package edu.jhuapl.saavtk.gui.menu;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;

public class FavoritesMenu extends JMenu
{
    FavoritesFile favoritesFile;
    ViewManager manager;

    public FavoritesMenu(ViewManager manager)
    {
        super("Favorites");
        this.favoritesFile = new FavoritesFile(manager);
        this.manager=manager;
        rebuild();
    }

    private final void rebuild()
    {
        removeAll();
        //
        JMenuItem add=new JMenuItem();
        JMenuItem rem=new JMenuItem();
        JMenuItem def=new JMenuItem();
        JMenuItem remall=new JMenuItem();
        add.setAction(new AddFavoriteAction("Add current model to favorites"));
        rem.setAction(new RemoveFavoriteAction("Remove current model from favorites"));
        def.setAction(new SetDefaultModelAction("Set current model as default",manager));
        remall.setAction(new ClearFavoritesAction("Clear all favorites"));
        
        //

        // favorites

        JMenuItem favoritesItem=new JMenuItem("Favorite models:");
        favoritesItem.setEnabled(false);
        add(favoritesItem);

        List<View> favoriteViews = favoritesFile.getAllFavorites();
        for (View view : favoriteViews)
        {
            JMenuItem menuItem = new FavoritesMenuItem(view);
            if (!view.getUniqueName().equals(manager.getDefaultBodyToLoad()))
                add(menuItem);
        }

        // show default to load
        if (!favoriteViews.isEmpty())
            add(new JSeparator());
        JMenuItem defaultItem=new JMenuItem("Default model:");
        defaultItem.setEnabled(false);
        add(defaultItem);

        String defaultToLoad = manager.getDefaultBodyToLoad();
        View defaultView = manager.getView(defaultToLoad);
        JMenuItem menuItem = new FavoritesMenuItem(defaultView);
        add(menuItem);

        //
        add(new JSeparator());
        add(add);
        add(rem);
        add(remall);
        add(def);

    }

    private class FavoritesMenuItem extends JMenuItem
    {
        public FavoritesMenuItem(View view)
        {
            super(view.getModelDisplayName());
            setAction(new ShowFavoriteAction(view));
        }

    }

    private class ShowFavoriteAction extends AbstractAction
    {
        private final View view;

        public ShowFavoriteAction(View view)
        {
            super(view.getModelDisplayName());
            this.view = view;
        }

        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
        {
        	manager.setCurrentView(view);
        }

    }

    private class AddFavoriteAction extends AbstractAction
    {
        public AddFavoriteAction(String desc)
        {
            super(desc);
        }

        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
        {
            favoritesFile.addFavorite(manager.getCurrentView());
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
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
        {
            favoritesFile.removeFavorite(manager.getCurrentView());
            if (manager.getDefaultBodyToLoad().equals(manager.getCurrentView().getUniqueName()))
                manager.resetDefaultBodyToLoad();
            rebuild();
        }
    }


    private class ClearFavoritesAction extends AbstractAction
    {
        public ClearFavoritesAction(String desc)
        {
            super(desc);
        }

        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
        {
            favoritesFile.clear();
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
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
        {
            manager.setDefaultBodyToLoad(manager.getCurrentView().getUniqueName());
            favoritesFile.addFavorite(manager.getCurrentView());    // automatically add current view to favorites if it already is
            rebuild();
        }

    }

}
