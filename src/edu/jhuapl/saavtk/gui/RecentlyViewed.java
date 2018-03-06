package edu.jhuapl.saavtk.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import edu.jhuapl.saavtk.util.Configuration;


public class RecentlyViewed extends JMenu
{
    ViewManager manager;
    List<JMenuItem> items= new ArrayList<JMenuItem>();
    JMenuItem clrItems = new JMenuItem();
    String names;

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
    	names=name;
        if(items.size()>9)
        {
            items.remove(9);
            this.remove(9);
        }

        JMenuItem recentItem = new JMenuItem();
        items.add(recentItem);
        this.add(recentItem, 0);
        recentItem.setAction(new RecentAction(manager, name));
        clrItems.setAction(new ClearRecentsAction(manager, "Remove all recents"));
        add(clrItems);
        try (FileWriter f_out=new FileWriter(Configuration.getApplicationDataDir()+File.separator+"recents.txt", false))
        {
        	for(int i=0;i<items.size();i++)
        	{
        		f_out.write(items.get(i).getText()+"\n");
        	}
        } catch (IOException e) {
        	e.printStackTrace();
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
            List<View> check = manager.getAllViews();
            for (View v : check)
            {
                if (v.getUniqueName().equals(viewName))
                {
                    manager.setCurrentView(v);
                }
            }

        }
    }
    
    private class ClearRecentsAction extends AbstractAction
    {
    	
    	public ClearRecentsAction(ViewManager m, String desc)
        {
            super(desc);
        }
		
		public void actionPerformed(ActionEvent e) 
		{	
			File read_file = new File(Configuration.getApplicationDataDir()+File.separator+"recents.txt");
			//read_file.delete();
	        try{
	            FileWriter f_out=new FileWriter(Configuration.getApplicationDataDir()+File.separator+"recents.txt", false);
	            f_out.write("");
	            f_out.close();

	        }catch(IOException ex){
	        	ex.getStackTrace();
	        }
	        items.clear();
	        
	        
		}
   
    }
    
}
