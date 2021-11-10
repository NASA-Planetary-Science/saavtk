package edu.jhuapl.saavtk.coloring.legacy;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.MetadataDisplay;
import edu.jhuapl.saavtk.gui.panel.PolyhedralModelControlPanel;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.plateColoring.ColoringData;
import edu.jhuapl.saavtk.model.plateColoring.LoadableColoringData;
import edu.jhuapl.saavtk.util.FileCache;

/**
 * Collection of legacy utility methods for working with the legacy design
 * associated with {@link PolyhedralModelControlPanel}.
 *
 * @author lopeznr1
 */
public class LegacyUtil
{
	/**
	 * Utility method to show a window that contains the properties of the
	 * (installed) coloring as configured by the specified small body.
	 * <p>
	 * This method was taken from {@link PolyhedralModelControlPanel}.
	 */
	public static void showColoringProperties(PolyhedralModel aSmallBody)
	{
		try
		{
			int index = aSmallBody.getColoringIndex();
			
			ColoringData coloringData = aSmallBody.getAllColoringData().get(index);
            if (coloringData instanceof LoadableColoringData)
            {
                // Force data to load.
                coloringData.getData();
                
                File file = ((LoadableColoringData) coloringData).getFile();
                
                JTabbedPane jTabbedPane = MetadataDisplay.summary(file);
                int tabCount = jTabbedPane.getTabCount();
                if (tabCount > 0)
                {
                    JFrame jFrame = new JFrame("Coloring File Properties");
                    
                    jFrame.add(jTabbedPane);
                    jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    jFrame.pack();
                    jFrame.setVisible(true);
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "No properties available for file " + file, "Coloring File Properties", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            else
            {
                JOptionPane.showMessageDialog(null, "No properties available for coloring " + coloringData.getName(), "Coloring Properties", JOptionPane.INFORMATION_MESSAGE);
            }
			
//			File file = FileCache.getFileFromServer(aSmallBody.getAllColoringData().get(index).getFileName());
//
//			JTabbedPane jTabbedPane = MetadataDisplay.summary(file);
//			int tabCount = jTabbedPane.getTabCount();
//			if (tabCount > 0)
//			{
//				JFrame jFrame = new JFrame("Coloring File Properties");
//
//				jFrame.add(jTabbedPane);
//				jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//				jFrame.pack();
//				jFrame.setVisible(true);
//			}
//			else
//			{
//				JOptionPane.showMessageDialog(null, "No properties available for file " + file, "Coloring File Properties",
//						JOptionPane.INFORMATION_MESSAGE);
//			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
