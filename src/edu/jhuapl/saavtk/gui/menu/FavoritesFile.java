package edu.jhuapl.saavtk.gui.menu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.SafeURLPaths;

class FavoritesFile
{
	private final Path favoritesFilePath;
	private final List<View> favorites;

	public FavoritesFile(ViewManager viewManager)
	{
		this.favoritesFilePath = SafeURLPaths.instance().get(Configuration.getApplicationDataDir(), "favorites");
		this.favorites = Lists.newArrayList();

		if (this.favoritesFilePath.toFile().exists())
		{
			List<View> allViews = viewManager.getAllViews();

			try (BufferedReader reader = new BufferedReader(new FileReader(favoritesFilePath.toFile())))
			{
				while (reader.ready())
				{
					final String line = reader.readLine();
					for (View view : allViews)
					{
						if (view.getUniqueName().equals(line) && !favorites.contains(view))
						{
							favorites.add(view);
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
	}

	//final static String defaultModelMarker="***";
	//String defaultModelName=null;

	//   public FavoritesManager() //ViewManager viewManager)
	//   {
	//        super("\u2661");    // unicode heart
	//        this.viewManager=viewManager;
	//        rebuild();
	//    }

	/*
	 * private class FavoritesMenuItem extends JRadioButtonMenuItem { public
	 * FavoritesMenuItem(final String string) { super(string);
	 * System.out.println(string); for (int i=0; i <
	 * viewManager.getNumberOfBuiltInViews(); ++i) { final int ifinal=i; if
	 * (viewManager.getBuiltInView(i).getUniqueName().equals(string))
	 * this.setAction(new AbstractAction() {
	 * 
	 * @Override public void actionPerformed(ActionEvent e) {
	 * viewManager.setCurrentView(viewManager.getBuiltInView(ifinal)); } }); } } }
	 */

	public List<View> getAllFavorites()
	{
		return ImmutableList.copyOf(favorites);
	}

	public void addFavorite(View view)
	{
		if (!favorites.contains(view))
		{
			favorites.add(view);
			writeFavoritesFile();
		}
	}

	public void removeFavorite(View view)
	{
		favorites.remove(view);
		writeFavoritesFile();
	}

	public void clear()
	{
		if (favoritesFilePath.toFile().exists())
			favoritesFilePath.toFile().delete();
		favorites.clear();
	}

	private void writeFavoritesFile()
	{
		try (FileWriter writer = new FileWriter(favoritesFilePath.toFile()))
		{
			for (View view : favorites)
			{
				writer.write(view.getUniqueName() + "\n");
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
