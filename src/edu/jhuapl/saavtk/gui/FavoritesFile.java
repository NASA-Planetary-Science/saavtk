package edu.jhuapl.saavtk.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;

public class FavoritesFile
{
 //   ViewManager viewManager;
    final Path favoritesFilePath=Paths.get(Configuration.getApplicationDataDir()+File.separator+"favorites");
    //final static String defaultModelMarker="***";
    //String defaultModelName=null;

 //   public FavoritesManager() //ViewManager viewManager)
 //   {
//        super("\u2661");    // unicode heart
//        this.viewManager=viewManager;
//        rebuild();
//    }

/*    private class FavoritesMenuItem extends JRadioButtonMenuItem
    {
        public FavoritesMenuItem(final String string)
        {
            super(string);
            System.out.println(string);
            for (int i=0; i < viewManager.getNumberOfBuiltInViews(); ++i)
            {
                final int ifinal=i;
                if (viewManager.getBuiltInView(i).getUniqueName().equals(string))
                    this.setAction(new AbstractAction()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            viewManager.setCurrentView(viewManager.getBuiltInView(ifinal));
                        }
                    });
            }
        }
    }*/

    public List<String> getAllFavorites()
    {
        touchFavoritesFile();
        Scanner scanner;
        List<String> stringsOnFile=Lists.newArrayList();
        try
        {
            scanner = new Scanner(favoritesFilePath.toFile());
            while (scanner.hasNext())
                stringsOnFile.add(scanner.next());
            scanner.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return stringsOnFile;
    }

    public boolean hasFavorite(String string)
    {
        Scanner scanner;
        try
        {
            scanner = new Scanner(favoritesFilePath.toFile());
            while (scanner.hasNext())
                if (scanner.next().equals(string))
                {
                    scanner.close();
                    return true;
                }
            scanner.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void addFavorite(String string)
    {
        if (hasFavorite(string))
            return;
        //
        try
        {
            FileWriter writer=new FileWriter(favoritesFilePath.toFile(), true);
            writer.write(string+"\n");
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
 //       rebuild();
    }

    public void removeFavorite(String string)
    {
        List<String> stringsOnFile=getAllFavorites();
        if (stringsOnFile.contains(string))
        {
            stringsOnFile.remove(string);
            favoritesFilePath.toFile().delete();
            FileCache.getFileFromServer(FileCache.FILE_PREFIX+"favorites");
            try
            {
                FileWriter writer=new FileWriter(favoritesFilePath.toFile());

                for (String str : stringsOnFile)
                    writer.write(str+"\n");
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
//        rebuild();
    }

    private void touchFavoritesFile()
    {
        if (!favoritesFilePath.toFile().exists())
            try
            {
                favoritesFilePath.toFile().createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
}
