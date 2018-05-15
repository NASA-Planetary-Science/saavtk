package edu.jhuapl.saavtk2.image.projection.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk2.image.projection.Projection;

public class LabelFileReader implements ProjectionReader
{
	String startTime,stopTime;
	int imageWidth,imageHeight;

    private String scTargetPositionString = null;
    private String targetSunPositionString = null;
    private String scOrientationString = null;
    private Rotation scOrientation = null;
    private double[] q = new double[4];
    private double[] cx = new double[3];
    private double[] cy = new double[3];
    private double[] cz = new double[3];

    private double focalLengthMillimeters = 100.0;
    private double npx = 4096.0;
    private double nln = 32.0;
    private double kmatrix00 = 1.0;
    private double kmatrix11 = 1.0;

    private String targetName = null;
    private String instrumentId = null;
    private String filterName = null;
    private String objectName = null;

    private String startTimeString = null;
    private String stopTimeString = null;
    private double exposureDuration = 0.0;
    private double numberOfPixels = 0.0;
    private double numberOfLines = 0.0;

	@Override
	public Projection read(File file)
	{
//      System.out.println("Loading label (.lbl) file...");
      String[] start = new String[1];
      String[] stop = new String[1];

  	
      double[][] spacecraftPositionOriginal;
      double[][] frustum1Original;
      double[][] frustum2Original;
      double[][] frustum3Original;
      double[][] frustum4Original;
      double[][] boresightDirectionOriginal;
      double[][] upVectorOriginal;
      double[][] sunPositionOriginal;

      spacecraftPositionOriginal = new double[1][3];
      frustum1Original = new double[1][3];
      frustum2Original = new double[1][3];
      frustum3Original = new double[1][3];
      frustum4Original = new double[1][3];
      boresightDirectionOriginal = new double[1][3];
      upVectorOriginal = new double[1][3];
      sunPositionOriginal = new double[1][3];

      try
	{
		loadLabelFile(
		          this.toString(),
		          start,
		          stop,
		          spacecraftPositionOriginal,
		          sunPositionOriginal,
		          frustum1Original,
		          frustum2Original,
		          frustum3Original,
		          frustum4Original,
		          boresightDirectionOriginal,
		          upVectorOriginal);
	}
	catch (IOException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

      startTime = start[0];
      stopTime = stop[0];
		return null;
	}

    protected void loadLabelFile(
            String labelFileName,
            String[] startTime,
            String[] stopTime,
            double[][] spacecraftPosition,
            double[][] sunVector,
            double[][] frustum1,
            double[][] frustum2,
            double[][] frustum3,
            double[][] frustum4,
            double[][] boresightDirection,
            double[][] upVector) throws IOException
    {
//        System.out.println(labelFileName);

        // for multispectral images, the image slice being currently parsed
        int slice = 0;

        // open a file input stream
        FileInputStream fs = new FileInputStream(labelFileName);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        //
        // Parse each line of the stream and process each key-value pair,
        // merging multiline numeric ("vector") values into a single-line
        // string. Multi-line quoted strings are ignored.
        //
        boolean inStringLiteral = false;
        boolean inVector = false;
        List<String> vector = new ArrayList<String>();
        String key = null;
        String value = null;
        String line = null;
        while ((line = in.readLine()) != null)
        {
            if (line.length() == 0)
                continue;

            // for now, multi-line quoted strings are ignored (i.e. treated as comments)
            if (line.trim().equals("\""))
            {
                inStringLiteral = false;
                continue;
            }

            if (inStringLiteral)
                continue;

            // terminate a multi-line numeric value (a "vector")
            if (line.trim().equals(")"))
            {
                inVector = false;
                value = "";
                for (String element : vector)
                    value = value + element;

                parseLabelKeyValuePair(
                        key,
                        value,
                        startTime,
                        stopTime,
                        spacecraftPosition[slice],
                        sunVector[slice],
                        frustum1[slice],
                        frustum2[slice],
                        frustum3[slice],
                        frustum4[slice],
                        boresightDirection[slice],
                        upVector[slice]);

                vector.clear();
                continue;
            }

            // add a line to the current vector
            if (inVector)
            {
                vector.add(line.trim());
                continue;
            }

            // extract key value pair
            String tokens[] = line.split("=");
            if (tokens.length < 2)
                continue;

            key = tokens[0].trim();
            value = tokens[1].trim();

            // detect and ignore comments
            if (value.equals("\""))
            {
                inStringLiteral = true;
                continue;
            }

            // start to accumulate numeric vector values
            if (value.equals("("))
            {
                inVector = true;
                continue;
            }

            if (value.startsWith("("))
                value = stripBraces(value);
            else
                value = stripQuotes(value);

            parseLabelKeyValuePair(
                    key,
                    value,
                    startTime,
                    stopTime,
                    spacecraftPosition[slice],
                    sunVector[slice],
                    frustum1[slice],
                    frustum2[slice],
                    frustum3[slice],
                    frustum4[slice],
                    boresightDirection[slice],
                    upVector[slice]);

        }

        in.close();

        //
        // calculate image projection from the parsed parameters
        //
        //this.focalLengthMillimeters = getFocalLength();
        //this.npx = getNumberOfPixels();
        //this.nln = getNumberOfLines();
        //this.kmatrix00 = 1.0 / getPixelWidth();
        //this.kmatrix11 = 1.0 / getPixelHeight();

        Vector3D boresightVector3D = scOrientation.applyTo(Vector3D.PLUS_I);
        boresightDirection[slice][0] = cz[0] = boresightVector3D.getX();
        boresightDirection[slice][1] = cz[1] = boresightVector3D.getY();
        boresightDirection[slice][2] = cz[2] = boresightVector3D.getZ();

        Vector3D upVector3D = scOrientation.applyTo(Vector3D.PLUS_J);
        upVector[slice][0] = cy[0] = upVector3D.getX();
        upVector[slice][1] = cy[1] = upVector3D.getY();
        upVector[slice][2] = cy[2] = upVector3D.getZ();

        Vector3D leftVector3D = scOrientation.applyTo(Vector3D.PLUS_K);
        cx[0] = -leftVector3D.getX();
        cx[1] = -leftVector3D.getY();
        cx[2] = -leftVector3D.getZ();

//      double kmatrix00 = Math.abs(Double.parseDouble(tmp[0]));
//      double kmatrix11 = Math.abs(Double.parseDouble(tmp[4]));

      // Here we calculate the image width and height using the K-matrix values.
      // This is used only when the constructor of this function was called with
      // loadPointingOnly set to true. When set to false, the image width and
      // and height is set in the loadImage function (after this function is called
      // and will overwrite these values here--though they should not be different).
      // But when in pointing-only mode, the loadImage function is not called so
      // we therefore set the image width and height here since some functions need it.
      imageWidth = (int)npx;
      imageHeight = (int)nln;
//      if (kmatrix00 > kmatrix11)
//          imageHeight = (int)Math.round(nln * (kmatrix00 / kmatrix11));
//      else if (kmatrix11 > kmatrix00)
//          imageWidth = (int)Math.round(npx * (kmatrix11 / kmatrix00));

      double[] cornerVector = new double[3];
      double fov1 = Math.atan(npx/(2.0*focalLengthMillimeters*kmatrix00));
      double fov2 = Math.atan(nln/(2.0*focalLengthMillimeters*kmatrix11));
      cornerVector[0] = -Math.tan(fov1);
      cornerVector[1] = -Math.tan(fov2);
      cornerVector[2] = 1.0;

      double fx = cornerVector[0];
      double fy = cornerVector[1];
      double fz = cornerVector[2];
      frustum3[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum3[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum3[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      fx = -cornerVector[0];
      fy = cornerVector[1];
      fz = cornerVector[2];
      frustum4[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum4[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum4[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      fx = cornerVector[0];
      fy = -cornerVector[1];
      fz = cornerVector[2];
      frustum1[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum1[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum1[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      fx = -cornerVector[0];
      fy = -cornerVector[1];
      fz = cornerVector[2];
      frustum2[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum2[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum2[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      MathUtil.vhat(frustum1[slice], frustum1[slice]);
      MathUtil.vhat(frustum2[slice], frustum2[slice]);
      MathUtil.vhat(frustum3[slice], frustum3[slice]);
      MathUtil.vhat(frustum4[slice], frustum4[slice]);


    }

    private String stripQuotes(String input)
    {
        String result = input;
        if (input.startsWith("\""))
            result = result.substring(1);
        if (input.endsWith("\""))
            result = result.substring(0, input.length()-2);
        return result;
    }

    private String stripBraces(String input)
    {
        String result = input;
        if (input.startsWith("("))
            result = result.substring(1);
        if (input.endsWith(")"))
            result = result.substring(0, input.length()-2);
        return result;
    }


    private void parseLabelKeyValuePair(
            String key,
            String value,
            String[] startTime,
            String[] stopTime,
            double[] spacecraftPosition,
            double[] sunVector,
            double[] frustum1,
            double[] frustum2,
            double[] frustum3,
            double[] frustum4,
            double[] boresightDirection,
            double[] upVector) throws IOException
    {
//        System.out.println("Label file key: " + key + " = " + value);

        if (key.equals("TARGET_NAME"))
            targetName = value;
        else if (key.equals("INSTRUMENT_ID"))
            instrumentId = value;
        else if (key.equals("FILTER_NAME"))
            filterName = value;
        else if (key.equals("OBJECT"))
            objectName = value;
        else if (key.equals("LINE_SAMPLES"))
        {
            if (objectName.equals("EXTENSION_CALGEOM_IMAGE"))
                numberOfPixels = Double.parseDouble(value);
        }
        else if (key.equals("LINES"))
        {
            if (objectName.equals("EXTENSION_CALGEOM_IMAGE"))
                numberOfLines = Double.parseDouble(value);
        }
        else if (key.equals("START_TIME"))
        {
            startTimeString = value;
            startTime[0] = startTimeString;
        }
        else if (key.equals("STOP_TIME"))
        {
            stopTimeString = value;
            stopTime[0] = stopTimeString;
        }
        else if (key.equals("SC_TARGET_POSITION_VECTOR"))
        {
            scTargetPositionString = value;
            String p[] = scTargetPositionString.split(",");
            spacecraftPosition[0] = Double.parseDouble(p[0].trim().split("\\s+")[0].trim());
            spacecraftPosition[1] = Double.parseDouble(p[1].trim().split("\\s+")[0].trim());
            spacecraftPosition[2] = Double.parseDouble(p[2].trim().split("\\s+")[0].trim());
        }
        else if (key.equals("TARGET_SUN_POSITION_VECTOR"))
        {
            targetSunPositionString = value;
            String p[] = targetSunPositionString.split(",");
            sunVector[0] = -Double.parseDouble(p[0].trim().split("\\s+")[0].trim());
            sunVector[1] = -Double.parseDouble(p[1].trim().split("\\s+")[0].trim());
            sunVector[2] = -Double.parseDouble(p[2].trim().split("\\s+")[0].trim());
        }
        else if (key.equals("QUATERNION"))
        {
            scOrientationString = value;
            String qstr[] = scOrientationString.split(",");
            q[0] = Double.parseDouble(qstr[0].trim().split("\\s+")[0].trim());
            q[1] = Double.parseDouble(qstr[1].trim().split("\\s+")[0].trim());
            q[2] = Double.parseDouble(qstr[2].trim().split("\\s+")[0].trim());
            q[3] = Double.parseDouble(qstr[3].trim().split("\\s+")[0].trim());
            scOrientation = new Rotation(q[0], q[1], q[2], q[3], false);
        }

    }

}
