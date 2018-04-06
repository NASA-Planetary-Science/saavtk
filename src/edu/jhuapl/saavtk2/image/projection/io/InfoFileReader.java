package edu.jhuapl.saavtk2.image.projection.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk2.util.Frustum;
import edu.jhuapl.saavtk2.image.projection.PerspectiveProjection;
import edu.jhuapl.saavtk2.image.projection.Projection;

public class InfoFileReader implements ProjectionReader
{
	String						start, stop;
	boolean						ato;
	private double[]			zoomFactor			= { 1.0 };
	private double[]			rotationOffset		= { 0.0 };

	public static final float	PDS_NA				= -1.e32f;
	public static final String	FRUSTUM1			= "FRUSTUM1";
	public static final String	FRUSTUM2			= "FRUSTUM2";
	public static final String	FRUSTUM3			= "FRUSTUM3";
	public static final String	FRUSTUM4			= "FRUSTUM4";
	public static final String	BORESIGHT_DIRECTION	= "BORESIGHT_DIRECTION";
	public static final String	UP_DIRECTION		= "UP_DIRECTION";
	public static final String	NUMBER_EXPOSURES	= "NUMBER_EXPOSURES";
	public static final String	START_TIME			= "START_TIME";
	public static final String	STOP_TIME			= "STOP_TIME";
	public static final String	SPACECRAFT_POSITION	= "SPACECRAFT_POSITION";
	public static final String	SUN_POSITION_LT		= "SUN_POSITION_LT";
	public static final String	TARGET_PIXEL_COORD	= "TARGET_PIXEL_COORD";
	public static final String	TARGET_ROTATION		= "TARGET_ROTATION";
	public static final String	TARGET_ZOOM_FACTOR	= "TARGET_ZOOM_FACTOR";
	public static final String	APPLY_ADJUSTMENTS	= "APPLY_ADJUSTMENTS";
	
	int slice;
	
	public static boolean extensionIsSupported(String ext)
	{
		return ext.toLowerCase().equals("info");
	}
	
	public void setSlice(int slice)
	{
		this.slice=slice;
	}

	@Override
	public Projection read(File file)
	{
		int nfiles = 1;
		boolean pad = nfiles > 1;

		//      for (int k=0; k<nfiles; k++)
		//      {
		String[] start = new String[1];
		String[] stop = new String[1];
		boolean[] ato = new boolean[1];
		ato[0] = true;

		//          System.out.println("Loading image: " + infoFileNames[k]);

		double[][] spacecraftPositionOriginal;
		double[][] frustum1Original;
		double[][] frustum2Original;
		double[][] frustum3Original;
		double[][] frustum4Original;
		double[][] boresightDirectionOriginal;
		double[][] upVectorOriginal;
		double[][] sunPositionOriginal;


		double[] targetPixelCoordinates = { Double.MAX_VALUE, Double.MAX_VALUE };

		List<double[]> spacecraftPosition=Lists.newArrayList();;
		List<double[]> frustum1=Lists.newArrayList();
		List<double[]> frustum2=Lists.newArrayList();
		List<double[]> frustum3=Lists.newArrayList();
		List<double[]> frustum4=Lists.newArrayList();
		List<double[]> boresightDirection=Lists.newArrayList();
		List<double[]> upVector=Lists.newArrayList();
		List<double[]> sunPosition=Lists.newArrayList();
		
		
		
		try
		{
			loadImageInfo(
					this.toString(),
					0,
					pad,
					start,
					stop,
					spacecraftPosition,
					sunPosition,
					frustum1,
					frustum2,
					frustum3,
					frustum4,
					boresightDirection,
					upVector,
					targetPixelCoordinates,
					ato);
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int n=spacecraftPosition.size();
		
		spacecraftPositionOriginal = new double[n][3];
		frustum1Original = new double[n][3];
		frustum2Original = new double[n][3];
		frustum3Original = new double[n][3];
		frustum4Original = new double[n][3];
		boresightDirectionOriginal = new double[n][3];
		upVectorOriginal = new double[n][3];
		sunPositionOriginal = new double[n][3];
		
		for (int i=0; i<n; i++)
		{
			spacecraftPositionOriginal[i]=spacecraftPosition.get(i);
			frustum1Original[i]=frustum1.get(i);
			frustum2Original[i]=frustum2.get(i);
			frustum3Original[i]=frustum3.get(i);
			frustum4Original[i]=frustum4.get(i);
			boresightDirectionOriginal[i]=boresightDirection.get(i);
			upVectorOriginal[i]=upVector.get(i);
			sunPositionOriginal[i]=sunPosition.get(i);
		}
		
		// should startTime and stopTime be an array? -turnerj1
		this.start = start[0];
		this.stop = stop[0];
		this.ato = ato[0];
        
        double[] origin=spacecraftPositionOriginal[0];
        double[] ul=frustum3Original[0];
        double[] ur=frustum4Original[0];
        double[] ll=frustum1Original[0];
        double[] lr=frustum2Original[0];

		return new PerspectiveProjection(new Frustum(origin, ul, ur, ll, lr));
	}

	protected void loadImageInfo(
			String infoFilename,
			int startSlice,        // for loading multiple info files, the starting array index to put the info into
			boolean pad,           // if true, will pad out the rest of the array with the same info
			String[] startTime,
			String[] stopTime,
			List<double[]> spacecraftPosition,
			List<double[]> sunPosition,
			List<double[]> frustum1,
			List<double[]> frustum2,
			List<double[]> frustum3,
			List<double[]> frustum4,
			List<double[]> boresightDirection,
			List<double[]> upVector,
			double[] targetPixelCoordinates,
			boolean[] applyFrameAdjustments) throws NumberFormatException, IOException, FileNotFoundException
	{
		if (infoFilename == null || infoFilename.endsWith("null"))
			throw new FileNotFoundException();

		boolean offset = true;

		FileInputStream fs = null;

		// look for an adjusted file first
		try
		{
			fs = new FileInputStream(infoFilename + ".adjusted");
		}
		catch (FileNotFoundException e)
		{
			fs = null;
		}

		// if no adjusted file exists, then load in the original unadjusted file
		if (fs == null)
		{
			//              try {
			fs = new FileInputStream(infoFilename);
			//              } catch (FileNotFoundException e) {
			//                  e.printStackTrace();
			//              }
		}

		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		
		
		// for multispectral images, the image slice being currently parsed
		int slice = startSlice - 1;

		String str;
		while ((str = in.readLine()) != null)
		{
			StringTokenizer st = new StringTokenizer(str);
			while (st.hasMoreTokens())
			{
				String token = st.nextToken();
				if (token == null)
					continue;

				if (START_TIME.equals(token))
				{
					st.nextToken();
					startTime[0] = st.nextToken();
				}
				if (STOP_TIME.equals(token))
				{
					st.nextToken();
					stopTime[0] = st.nextToken();
				}
				// eventually, we should parse the number of exposures from the INFO file, for now it is hard-coded -turnerj1
				//                  if (NUMBER_EXPOSURES.equals(token))
				//                  {
				//                      numberExposures = Integer.parseInt(st.nextToken());
				//                      if (numberExposures > 1)
				//                      {
				//                          spacecraftPosition = new double[numberExposures][3];
				//                          frustum1 = new double[numberExposures][3];
				//                          frustum2 = new double[numberExposures][3];
				//                          frustum3 = new double[numberExposures][3];
				//                          frustum4 = new double[numberExposures][3];
				//                          sunVector = new double[numberExposures][3];
				//                          boresightDirection = new double[numberExposures][3];
				//                          upVector = new double[numberExposures][3];
				//                          frusta = new Frustum[numberExposures];
				//                          footprint = new vtkPolyData[numberExposures];
				//                          footprintCreated = new boolean[numberExposures];
				//                          shiftedFootprint = new vtkPolyData[numberExposures];
				//                      }
				//                  }
				// For backwards compatibility with MSI images we use the endsWith function
				// rather than equals for FRUSTUM1, FRUSTUM2, FRUSTUM3, FRUSTUM4, BORESIGHT_DIRECTION
				// and UP_DIRECTION since these are all prefixed with MSI_ in the info file.
				if (token.equals(TARGET_PIXEL_COORD))
				{
					st.nextToken();
					st.nextToken();
					double x = Double.parseDouble(st.nextToken());
					st.nextToken();
					double y = Double.parseDouble(st.nextToken());
					targetPixelCoordinates[0] = x;
					targetPixelCoordinates[1] = y;
				}
				if (token.equals(TARGET_ROTATION))
				{
					st.nextToken();
					double x = Double.parseDouble(st.nextToken());
					rotationOffset[0] = x;
				}
				if (token.equals(TARGET_ZOOM_FACTOR))
				{
					st.nextToken();
					double x = Double.parseDouble(st.nextToken());
					zoomFactor[0] = x;
				}
				if (token.equals(APPLY_ADJUSTMENTS))
				{
					st.nextToken();
					offset = Boolean.parseBoolean(st.nextToken());
					applyFrameAdjustments[0] = offset;
				}

				if (SPACECRAFT_POSITION.equals(token) ||
						SUN_POSITION_LT.equals(token) ||
						token.endsWith(FRUSTUM1) ||
						token.endsWith(FRUSTUM2) ||
						token.endsWith(FRUSTUM3) ||
						token.endsWith(FRUSTUM4) ||
						token.endsWith(BORESIGHT_DIRECTION) ||
						token.endsWith(UP_DIRECTION))
				{
					st.nextToken();
					st.nextToken();
					double x = Double.parseDouble(st.nextToken());
					st.nextToken();
					double y = Double.parseDouble(st.nextToken());
					st.nextToken();
					double z = Double.parseDouble(st.nextToken());
					if (SPACECRAFT_POSITION.equals(token))
					{
						// SPACECRAFT_POSITION is assumed to be at the start of a frame, so increment slice count
						slice++;
						/*spacecraftPosition[slice][0] = x;
						spacecraftPosition[slice][1] = y;
						spacecraftPosition[slice][2] = z;*/
						spacecraftPosition.add(new double[]{x,y,z});
					}
					if (SUN_POSITION_LT.equals(token))
					{
						/*sunPosition[slice][0] = x;
						sunPosition[slice][1] = y;
						sunPosition[slice][2] = z;*/
						//                          MathUtil.vhat(sunPosition[slice], sunPosition[slice]);
						sunPosition.add(new double[]{x,y,z});
					}
					else if (token.endsWith(FRUSTUM1))
					{
						/*frustum1[slice][0] = x;
						frustum1[slice][1] = y;
						frustum1[slice][2] = z;
						MathUtil.vhat(frustum1[slice], frustum1[slice]);*/
						frustum1.add(new double[]{x,y,z});
					}
					else if (token.endsWith(FRUSTUM2))
					{
						/*frustum2[slice][0] = x;
						frustum2[slice][1] = y;
						frustum2[slice][2] = z;
						MathUtil.vhat(frustum2[slice], frustum2[slice]);*/
						frustum2.add(new double[]{x,y,z});
					}
					else if (token.endsWith(FRUSTUM3))
					{
						/*frustum3[slice][0] = x;
						frustum3[slice][1] = y;
						frustum3[slice][2] = z;
						MathUtil.vhat(frustum3[slice], frustum3[slice]);*/
						frustum3.add(new double[]{x,y,z});
					}
					else if (token.endsWith(FRUSTUM4))
					{
						/*frustum4[slice][0] = x;
						frustum4[slice][1] = y;
						frustum4[slice][2] = z;
						MathUtil.vhat(frustum4[slice], frustum4[slice]);*/
						frustum4.add(new double[]{x,y,z});
					}
					if (token.endsWith(BORESIGHT_DIRECTION))
					{
						/*boresightDirection[slice][0] = x;
						boresightDirection[slice][1] = y;
						boresightDirection[slice][2] = z;*/
						boresightDirection.add(new double[]{x,y,z});
					}
					if (token.endsWith(UP_DIRECTION))
					{
						/*upVector[slice][0] = x;
						upVector[slice][1] = y;
						upVector[slice][2] = z;*/
						upVector.add(new double[]{x,y,z});
					}
				}
			}
		}

		/*          // once we've read in all the frames, pad out any additional missing frames
		  if (pad)
		  {
		      int nslices = getNumberBands();
		      for (int i=slice+1; i<nslices; i++)
		      {
		          spacecraftPosition[i][0] = spacecraftPosition[slice][0];
		          spacecraftPosition[i][1] = spacecraftPosition[slice][1];
		          spacecraftPosition[i][2] = spacecraftPosition[slice][2];
		
		          sunPosition[i][0] = sunPosition[slice][0];
		          sunPosition[i][1] = sunPosition[slice][1];
		          sunPosition[i][2] = sunPosition[slice][2];
		
		          frustum1[i][0] = frustum1[slice][0];
		          frustum1[i][1] = frustum1[slice][1];
		          frustum1[i][2] = frustum1[slice][2];
		
		          frustum2[i][0] = frustum2[slice][0];
		          frustum2[i][1] = frustum2[slice][1];
		          frustum2[i][2] = frustum2[slice][2];
		
		          frustum3[i][0] = frustum3[slice][0];
		          frustum3[i][1] = frustum3[slice][1];
		          frustum3[i][2] = frustum3[slice][2];
		
		          frustum4[i][0] = frustum4[slice][0];
		          frustum4[i][1] = frustum4[slice][1];
		          frustum4[i][2] = frustum4[slice][2];
		
		          boresightDirection[i][0] = boresightDirection[slice][0];
		          boresightDirection[i][1] = boresightDirection[slice][1];
		          boresightDirection[i][2] = boresightDirection[slice][2];
		
		          upVector[slice][0] = upVector[slice][0];
		          upVector[slice][1] = upVector[slice][1];
		          upVector[slice][2] = upVector[slice][2];
		      }
		  }
		
		    
		  */ in.close();
		  
		  
		  
	}
	
}
