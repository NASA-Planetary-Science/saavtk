package edu.jhuapl.saavtk.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.util.Configuration;


public class RecentlyViewed extends JMenu
{
    ViewManager manager;
    ArrayList<JMenuItem> items= new ArrayList<JMenuItem>();

    public RecentlyViewed(ViewManager m)
    {
        super("Recents");
        manager = m;
        Scanner scan = null;
        File read_file = new File(Configuration.getApplicationDataDir()+File.separator+"recents.txt");
        try
        {
            read_file.createNewFile();
        }
        catch (IOException e1)
        {
        }
        // Creating file object
        try{
            scan = new Scanner(read_file);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        while (scan.hasNextLine())
        {
            updateMenu(scan.nextLine());
        }
    }

    //Updates the menu and sets action
    public void updateMenu(String name)
    {
        if(items.size()>9)
        {
            items.remove(9);
            this.remove(9);
        }

        JMenuItem recentItem = new JMenuItem();
        items.add(recentItem);
        this.add(recentItem, 0);
        recentItem.setAction(new RecentAction(manager, name));
        try{
            FileWriter f_out=new FileWriter(Configuration.getApplicationDataDir()+File.separator+"recents.txt", false);
            f_out.write("");
            f_out.close();

        }catch(IOException e){
            System.exit(-1);
        }
        for(int i=0;i<items.size();i++)
        {
            try{
                FileWriter f_out=new FileWriter(Configuration.getApplicationDataDir()+File.separator+"recents.txt", true);
                f_out.write(items.get(i).getActionCommand()+"\n");
                f_out.close();

            }catch(IOException e){
                System.exit(-1);
            }
        }
    }

    //sets the action for the menu items
    private class RecentAction extends AbstractAction
    {
        ViewManager manager;
        String viewName;

        public RecentAction(ViewManager m, String n)
        {
            super(n);
            manager = m;
            viewName = n;
        }

        public void actionPerformed(ActionEvent e)
        {
            ArrayList<View> check = manager.getAllViews();
            for (View v : check)
            {
                if (v.getUniqueName().equals(viewName))
                {
                    manager.setCurrentView(v);
                }
            }

        }
    }
}
