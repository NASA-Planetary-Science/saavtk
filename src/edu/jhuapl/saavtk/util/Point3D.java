package edu.jhuapl.saavtk.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class Point3D
{
    public double[] xyz = new double[3];

    public Point3D(double[] pt)
    {
        xyz[0] = pt[0];
        xyz[1] = pt[1];
        xyz[2] = pt[2];
    }

    public String toString()
    {
        return "(" + xyz[0] + "," + xyz[1] + "," + xyz[2] + ")";
    }

    public double distanceTo(Point3D pt)
    {
        return MathUtil.distanceBetween(xyz, pt.xyz);
    }

    static public double distanceBetween(Point3D pt1, Point3D pt2)
    {
        return MathUtil.distanceBetween(pt1.xyz, pt2.xyz);
    }

    static public void savePointArray(List<Point3D> pointArray, File file) throws IOException
    {
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);
        for (Point3D p : pointArray)
        {
            out.write(p.xyz[0] + " " +
                    p.xyz[1] + " " +
                    p.xyz[2] + "\n");
        }
        out.close();
    }

    public static List<Point3D> loadPointArray(String filename) throws IOException
    {
        InputStream fs = new FileInputStream(filename);
        if (filename.toLowerCase().endsWith(".gz"))
            fs = new GZIPInputStream(fs);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        List<Point3D> values = new ArrayList<Point3D>();
        String line;

        while ((line = in.readLine()) != null)
        {
            String [] tokens = line.trim().split("\\s+");

            values.add(new Point3D(new double[]{
                    Double.parseDouble(tokens[0]),
                    Double.parseDouble(tokens[1]),
                    Double.parseDouble(tokens[2])}));
        }

        in.close();

        return values;
    }
}
