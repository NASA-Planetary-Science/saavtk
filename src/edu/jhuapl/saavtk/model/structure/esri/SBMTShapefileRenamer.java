package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FilenameUtils;

public class SBMTShapefileRenamer extends JPanel implements ActionListener, DocumentListener
{

    String filename;
    String origprefix;
    File[] files;

    JLabel origlabel;
    JTextField prefixField;
    JComboBox<String> shapetypes = new JComboBox<>(new String[] { "Points", "Paths", "Polygons", "Circles", "Ellipses" });
    String[] filetypestr = new String[] { "points", "paths-ctrlpts", "polygons-ctrlpts", "circles", "ellipses" };
    JLabel newlabel;

    //   JButton renameButton=new JButton("Rename");
    //  JButton cancelButton=new JButton("Cancel");

    public SBMTShapefileRenamer(String filename)
    {
        this.filename = filename;
        origprefix = FilenameUtils.getBaseName(filename);
        files = new File(filename).getParentFile().listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                // explicitly look for prefix + one of the 7 file extensions, instead of using name.startsWith(prefix), since other, longer variants of prefix will also be picked up (incorrectly) 
                return name.equals(origprefix + ".cpg") || name.equals(origprefix + ".dbf") || name.equals(origprefix + ".prj") || name.equals(origprefix + ".sbn") || name.equals(origprefix + ".sbx") || name.equals(origprefix + ".shp") || name.equals(origprefix + ".shx");
            }
        });
        //
        prefixField = new JTextField(origprefix);
        origlabel = new JLabel(createOrigString());
        origlabel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Original file names"));
        newlabel = new JLabel(createNewString());
        newlabel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "New file names"));
        //
        add(origlabel, BorderLayout.WEST);
        JPanel ctrPanel = new JPanel(new BorderLayout());
        JPanel prefixPanel = new JPanel();
        prefixPanel.add(new JLabel("File prefix"));
        prefixPanel.add(prefixField);
        JPanel shapePanel = new JPanel();
        shapePanel.add(new JLabel("Shapefile content"));
        shapePanel.add(shapetypes);
        JPanel buttonPanel = new JPanel();
        //       buttonPanel.add(renameButton);
        //       buttonPanel.add(cancelButton);
        ctrPanel.add(prefixPanel, BorderLayout.NORTH);
        ctrPanel.add(shapePanel, BorderLayout.CENTER);
        ctrPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(ctrPanel, BorderLayout.CENTER);
        add(newlabel, BorderLayout.EAST);
        //
        prefixField.addActionListener(this);
        prefixField.getDocument().addDocumentListener(this);
        shapetypes.addActionListener(this);
        //     renameButton.addActionListener(this);
        //     cancelButton.addActionListener(this);
    }

    protected String createOrigString()
    {
        String origstr = "<html>";
        for (int i = 0; i < files.length; i++)
            origstr += files[i].getName() + "<br>";
        origstr += "<html>";
        return origstr;
    }

    protected String getNewFilename(File file)
    {
        return file.getParent() + "/" + prefixField.getText() + "." + filetypestr[shapetypes.getSelectedIndex()] + "." + FilenameUtils.getExtension(file.getName());
    }

    protected String createNewString()
    {
        String newstr = "<html>";
        for (int i = 0; i < files.length; i++)
            newstr += getNewFilename(files[i]) + "<br>";
        newstr += "<html>";
        return newstr;
    }

    public String rename()
    {
        String result=null;
        try
        {
            for (int i = 0; i < files.length; i++)
            {
                File newFile = new File(getNewFilename(files[i]));
                System.out.println("renamed " + files[i].getAbsolutePath() + " to " + newFile.getAbsolutePath());
                boolean ok = files[i].renameTo(newFile);
                if (newFile.getAbsolutePath().endsWith("shp"))
                    result=newFile.getAbsolutePath();
                if (!ok)
                    throw new IOException("File rename failed for " + files[i].toString());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        //        if (e.getSource().equals(renameButton))
        //            try
        //            {
        //                rename();
        //            }
        //            catch (IOException e1)
        //            {
        //                // TODO Auto-generated catch block
        //                e1.printStackTrace();
        //            }
        //        else if (e.getSource().equals(cancelButton))
        //            dispose();
        //        else
        newlabel.setText(createNewString());
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        newlabel.setText(createNewString());
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        newlabel.setText(createNewString());
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        newlabel.setText(createNewString());
    }

}
