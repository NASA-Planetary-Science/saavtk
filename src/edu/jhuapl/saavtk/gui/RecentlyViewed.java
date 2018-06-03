package edu.jhuapl.saavtk.gui;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.SafePaths;

public class RecentlyViewed extends JMenu
{
	private static final int NUMBER_RECENTS_MAXIMUM = 10;
	private final ViewManager manager;
	private final List<View> recentViews;
	private final File recentsFile;
	private final JMenuItem clearAllMenuItem;

	public RecentlyViewed(ViewManager manager)
	{
		super("Recents");
		this.manager = manager;
		this.recentViews = new ArrayList<>();
		this.recentsFile = new File(SafePaths.getString(Configuration.getApplicationDataDir(), "recents.txt"));
		this.clearAllMenuItem = createClearAllMenuItem();
		if (this.recentsFile.exists())
		{
			// Read recents file and figure out which models it refers to.
			try (BufferedReader reader = new BufferedReader(new FileReader(this.recentsFile)))
			{
				final List<View> allViews = this.manager.getAllViews();
				while (reader.ready() && recentViews.size() <= NUMBER_RECENTS_MAXIMUM)
				{
					String line = reader.readLine();
					for (View view : allViews)
					{
						if (view.getUniqueName().equals(line) && !this.recentViews.contains(view))
						{
							recentViews.add(view);
							JMenuItem menuItem = createMenuItem(view);
							menuItem.setEnabled(view.isEnabled());
							add(menuItem);
							break;
						}
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}

		add(clearAllMenuItem);

		// Put the current view as the most recent.
		View view = this.manager.getCurrentView();
		if (view != null)
		{
			updateMenu(view);
		}

		if (recentViews.isEmpty())
		{
			clearAllMenuItem.setEnabled(false);
		}

	}

	public final void updateMenu(View view)
	{
		// If this view is already somewhere in the menu, remove it.
		// Then below we will add it in the 0th spot.
		for (int index = 0; index < recentViews.size(); ++index)
		{
			if (recentViews.get(index) == view)
			{
				recentViews.remove(index);
				remove(index);
				break;
			}
		}

		// Update both the collection and the view menu.
		recentViews.add(0, view);
		JMenuItem menuItem = createMenuItem(view);
		menuItem.setEnabled(view.isEnabled());
		add(menuItem, 0);
		clearAllMenuItem.setEnabled(true);

		// Enforce the limit on number of recents.
		if (recentViews.size() > NUMBER_RECENTS_MAXIMUM)
		{
			int lastViewIndex = recentViews.size() - 1;
			recentViews.remove(lastViewIndex);
			remove(lastViewIndex);
		}

		// Save the current recents list.
		writeRecentsFile();
	}

	protected final void writeRecentsFile()
	{
		try (FileWriter fileWriter = new FileWriter(recentsFile))
		{
			for (View view : recentViews)
			{
				fileWriter.write(view.getUniqueName() + "\n");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected final JMenuItem createMenuItem(View view)
	{
		JMenuItem item = new JMenuItem();
		item.setAction(new AbstractAction(view.getModelDisplayName()) {
			@Override
			public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
			{
				manager.setCurrentView(view);
			}
		});
		return item;
	}

	protected final JMenuItem createClearAllMenuItem()
	{
		JMenuItem item = new JMenuItem();
		item.setAction(new AbstractAction("Clear recents list") {
			@Override
			public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt)
			{
				if (recentsFile.exists())
				{
					try
					{
						Files.delete(recentsFile.toPath());
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				recentViews.clear();
				removeAll();
				clearAllMenuItem.setEnabled(false);
				add(clearAllMenuItem);
			}
		});
		return item;
	}
}
