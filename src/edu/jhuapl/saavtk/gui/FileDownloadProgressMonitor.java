package edu.jhuapl.saavtk.gui;

import java.awt.Component;

import edu.jhuapl.saavtk.util.FileDownloader;

public class FileDownloadProgressMonitor
{
    public static ProgressMonitor of(Component c, String title, FileDownloader fileDownloader)
    {
        return new ProgressMonitor(c, title, fileDownloader, false) {
            @Override
            protected String createProgressMessage()
            {
                return fileDownloader.createProgressMessage();
            }
        };
    }

}
