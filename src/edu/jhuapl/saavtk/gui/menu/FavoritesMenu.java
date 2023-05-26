package edu.jhuapl.saavtk.gui.menu;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.model.DefaultModelIdentifier;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileStateListenerTracker;
import edu.jhuapl.saavtk.util.SafeURLPaths;

public class FavoritesMenu extends JMenu
{
    private final FavoritesFile favoritesFile;
    private final ViewManager manager;
    private final FileStateListenerTracker fileStateTracker;

    public FavoritesMenu(ViewManager manager)
    {
        super("Favorites");
        this.favoritesFile = new FavoritesFile(manager);
        this.manager = manager;
        this.fileStateTracker = FileStateListenerTracker.of(FileCache.instance());
        rebuild();
    }

    private final void rebuild()
    {
        removeAll();
        fileStateTracker.removeAllStateChangeListeners();

        //
        JMenuItem add = new JMenuItem();
        JMenuItem rem = new JMenuItem();
        JMenuItem def = new JMenuItem();
        JMenuItem remall = new JMenuItem();
        add.setAction(new AddFavoriteAction("Add current model to favorites"));
        rem.setAction(new RemoveFavoriteAction("Remove current model from favorites"));
        def.setAction(new SetDefaultModelAction("Set current model as default", manager));
        remall.setAction(new ClearFavoritesAction("Clear all favorites"));

        //

        // favorites

        JMenuItem favoritesItem = new JMenuItem("Favorite models:");
        favoritesItem.setEnabled(false);
        add(favoritesItem);

        String defaultModelId = DefaultModelIdentifier.getDefaultModel();

        List<View> favoriteViews = favoritesFile.getAllFavorites();
        for (View view : favoriteViews)
        {
            if (!view.getUniqueName().equals(defaultModelId))
            {
                JMenuItem menuItem = new FavoritesMenuItem(view);
                String urlString = (view.getConfigURL() == null) ? view.getShapeModelName() : view.getConfigURL();
            	if (view.getUniqueName().contains("Custom"))
            	{
            		SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
            		urlString = safeUrlPaths.getUrl(safeUrlPaths.getString(Configuration.getImportedShapeModelsDir(), view.getShapeModelName()));
            	}
//                String urlString = view.getConfig().getShapeModelFileNames()[0];
                fileStateTracker.addStateChangeListener(urlString, state -> {
                    menuItem.setEnabled(state.isAccessible());
                });
                add(menuItem);
            }
        }

        // show default to load
        if (!favoriteViews.isEmpty())
            add(new JSeparator());
        JMenuItem defaultItem = new JMenuItem("Default model:");
        add(defaultItem);

        View defaultToLoad;
        try
        {
            defaultToLoad = manager.getView(defaultModelId);
        }
        catch (Exception e)
        {
            defaultToLoad = null;
        }

        if (defaultToLoad != null)
        {            
            try
            {
                JMenuItem menuItem = new FavoritesMenuItem(defaultToLoad);
//            String urlString = defaultToLoad.getShapeModelName();
                String urlString = (defaultToLoad.getConfigURL() == null) ? defaultToLoad.getShapeModelName() : defaultToLoad.getConfigURL();
                if (defaultToLoad.getUniqueName().contains("Custom"))
                {
                    SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
                    urlString = safeUrlPaths.getUrl(safeUrlPaths.getString(Configuration.getImportedShapeModelsDir(), defaultToLoad.getShapeModelName()));
                }
                fileStateTracker.addStateChangeListener(urlString, state -> {
                    menuItem.setEnabled(state.isAccessible());
                });
                add(menuItem);
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

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
            String defaultModelName = DefaultModelIdentifier.getDefaultModel();
            String currentModelName = manager.getCurrentView().getUniqueName();
            if (defaultModelName == currentModelName || (defaultModelName != null && defaultModelName.equals(currentModelName)))
            {
                DefaultModelIdentifier.factoryReset();
            }
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
            DefaultModelIdentifier.factoryReset();
            rebuild();
        }
    }

    private class SetDefaultModelAction extends AbstractAction
    {

        ViewManager manager;

        public SetDefaultModelAction(String desc, ViewManager manager)
        {
            super(desc);
            this.manager = manager;
        }

        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
        {
            DefaultModelIdentifier.setUserDefaultModel(manager.getCurrentView().getUniqueName());
            favoritesFile.addFavorite(manager.getCurrentView()); // automatically add current view to favorites if it already is
            rebuild();
        }

    }

}
