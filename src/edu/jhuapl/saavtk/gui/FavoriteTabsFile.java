package edu.jhuapl.saavtk.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.util.Configuration;

public final class FavoriteTabsFile
{
    final private static Path favoriteTabsFilePath=Paths.get(Configuration.getApplicationDataDir()+File.separator+"favoriteTabs");
    private static FavoriteTabsFile instance=new FavoriteTabsFile();

    private FavoriteTabsFile()
    {
    }

    static public FavoriteTabsFile getInstance()
    {
        return instance;
    }

    public void setFavoriteTab(String uniqueBodyName, int tab)
    {
        Map<String, Integer> bodyNameToTabIndexMap=this.readAllFavoriteTabs();
        bodyNameToTabIndexMap.put(uniqueBodyName, tab);
        rewriteFavoritesFile(bodyNameToTabIndexMap);

    }

    public int getFavoriteTab(String uniqueBodyName)
    {
        Map<String, Integer> bodyNameToTabIndexMap=this.readAllFavoriteTabs();
        if (bodyNameToTabIndexMap.containsKey(uniqueBodyName))
            return bodyNameToTabIndexMap.get(uniqueBodyName);
        else
            return 0;
    }

    private void rewriteFavoritesFile(Map<String, Integer> bodyNameToTabIndexMap)
    {
        if (favoriteTabsFilePath.toFile().exists())
            favoriteTabsFilePath.toFile().delete();
        //
        try
        {
            FileWriter writer=new FileWriter(favoriteTabsFilePath.toFile());
            for (String bodyName : bodyNameToTabIndexMap.keySet())
                writer.write(bodyName+" tab="+bodyNameToTabIndexMap.get(bodyName)+"\n");
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private Map<String,Integer> readAllFavoriteTabs()
    {
        touchFavoriteTabsFile();
        Map<String,Integer> bodyNameToTabIndexMap=Maps.newHashMap();
        //
        Scanner scanner;
        try
        {
            scanner = new Scanner(favoriteTabsFilePath.toFile());
            while (scanner.hasNextLine())
            {
                String line=scanner.nextLine();
                int idx=line.indexOf("tab=");
                String bodyName=line.substring(0, idx-1);
                int tab=Integer.valueOf(line.substring(idx+4,line.length()));
                bodyNameToTabIndexMap.put(bodyName,tab);
            }
            scanner.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return bodyNameToTabIndexMap;
    }

    private void touchFavoriteTabsFile()
    {
        if (!favoriteTabsFilePath.toFile().exists())
            try
            {
                favoriteTabsFilePath.toFile().createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

}
