package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class ConvertResourceToFile
{
    private static boolean resourceEqualsFile(Object o, String resource, File file)
    {
        if (!file.exists())
            return false;

        boolean equals = false;

        try
        {
            InputStream ris = o.getClass().getResourceAsStream(resource);
            FileInputStream fis = new FileInputStream(file);

            equals = IOUtils.contentEquals(ris, fis);

            ris.close();
            fis.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return equals;
    }

    /**
     * Convert a specified resource to a real file to be placed in a certain directory.
     * Only write out the file if it does not already exist or is different from the resource.
     * @param o
     * @param resource
     * @param parentDir
     * @return
     */
    public static File convertResourceToRealFile(Object o, String resource, String parentDir)
    {
        // Get the name of the resource after the last slash
        File tmp = new File(resource);
        String name = tmp.getName();

        File parent = new File(parentDir);
        if (!parent.exists())
            parent.mkdirs();

        File file = new File(parentDir + File.separator + name);

        if (resourceEqualsFile(o, resource, file))
            return file;

        try
        {
            InputStream is = o.getClass().getResourceAsStream(resource);

            FileOutputStream os = new FileOutputStream(file);

            byte[] buff = new byte[2048];
            int len;
            while((len = is.read(buff)) > 0)
            {
                os.write(buff, 0, len);
            }

            os.close();
            is.close();
        }
        catch (IOException e)
        {
            file = null;
            e.printStackTrace();
        }

        return file;
    }

    public static File convertResourceToTempFile(Object o, String resource)
    {
        File temp = null;
        try
        {
            temp = File.createTempFile("resource-", null);
            temp.deleteOnExit();

            InputStream is = o.getClass().getResourceAsStream(resource);

            FileOutputStream os = new FileOutputStream(temp);

            byte[] buff = new byte[2048];
            int len;
            while((len = is.read(buff)) > 0)
            {
                os.write(buff, 0, len);
            }

            os.close();
            is.close();
        }
        catch (IOException e)
        {
            temp = null;
            e.printStackTrace();
        }

        return temp;
    }
}
