package edu.jhuapl.saavtk.main.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import edu.jhuapl.saavtk.main.MainWinCfg;
import edu.jhuapl.saavtk.util.Configuration;
import glum.gui.info.WindowCfg;
import glum.io.ParseUtil;
import glum.task.Task;
import glum.util.ThreadUtil;
import glum.version.PlainVersion;
import glum.version.VersionUtils;

/**
 * Collection of utility methods used to access configuration associated with
 * the main application window.
 *
 * @author lopeznr1
 */
public class MainWinConfigUtil
{
	// Constants
	private static final PlainVersion RefVersion = new PlainVersion(2021, 10, 0);

	/**
	 * Utility method that returns the main window configuration file.
	 */
	public static File getConfigurationFile()
	{
		return new File(Configuration.getApplicationDataDir(), "MainWin.cfg.csv");
	}

	/**
	 * Utility method to load the main application configuration.
	 *
	 * @param aTask       The {@link Task} used for logging.
	 * @param aDefaultMAC The default {@link MainWinCfg} used to populate missing
	 *                    values.
	 * @return
	 */
	public static MainWinCfg loadConfiguration(Task aTask, MainWinCfg aDefaultMAC)
	{
		// Bail if no configuration file
		var tmpFile = getConfigurationFile();
		if (tmpFile.exists() == false)
			return aDefaultMAC;

		// Load the configuration
		var mainWC = aDefaultMAC.mainWC();
		var mainSplitSize = aDefaultMAC.mainSplitSize();
		try (var tmpBR = new BufferedReader(new FileReader(tmpFile)))
		{
			int lineCnt = 0;
			while (true)
			{
				lineCnt++;

				// Bail at EOF
				var tmpLine = tmpBR.readLine();
				if (tmpLine == null)
					break;

				// Skip empty comments / empty lines
				var tmpStr = tmpLine.trim();
				if (tmpStr.isEmpty() == true || tmpStr.startsWith("#") == true)
					continue;

				// Tokenize
				var strArr = tmpStr.split(",");
				var tagStr = strArr[0];

				// Read the version
				if (tagStr.equals("ver") == true && strArr.length >= 2)
				{
					var tmpVersion = PlainVersion.parse(strArr[1]);
					if (tmpVersion == null)
					{
						aTask.logRegln("Unrecognized version. Input: " + strArr[1]);
						aTask.logRegln("Aborting...\n");
						break;
					}
					if (VersionUtils.isAfter(tmpVersion, RefVersion) == true)
						aTask.logRegln("Future version encountered. Configuration may not be supported. Ver: " + strArr[1]);
					continue;
				}

				// Read the window config
				if (tagStr.equals("win") == true && strArr.length >= 6)
				{
					var isShown = ParseUtil.readBoolean(strArr[1], false);
					var posX = ParseUtil.readInt(strArr[2], mainWC.posX());
					var posY = ParseUtil.readInt(strArr[3], mainWC.posY());
					var dimX = ParseUtil.readInt(strArr[4], mainWC.dimX());
					var dimY = ParseUtil.readInt(strArr[5], mainWC.dimY());
					mainWC = new WindowCfg(isShown, posX, posY, dimX, dimY);
					continue;
				}

				// Read the main window split size
				if (tagStr.equals("mainSplit") == true && strArr.length >= 2)
				{
					mainSplitSize = ParseUtil.readInt(strArr[1], mainSplitSize);
					continue;
				}

				// Log the unrecognized line
				aTask.logRegln("\t[L:" + lineCnt + "] Skipping unrecognized line: ");
				aTask.logRegln("\t" + tmpLine);
			}
		}
		catch (IOException aExp)
		{
			aExp.printStackTrace();
		}

		return new MainWinCfg(mainWC, mainSplitSize);
	}

	/**
	 * Utility method to save the main application configuration.
	 */
	public static void saveConfiguration(Task aTask, MainWinCfg aMainAppCfg)
	{
		// Save the configuration
		var tmpFile = getConfigurationFile();
		try (var tmpBW = new BufferedWriter(new FileWriter(tmpFile)))
		{
			// Header
			writeHeader(tmpBW, false, true);

			// Content
			var tmpMainWC = aMainAppCfg.mainWC();
			var isShown = tmpMainWC.isShown();
			var posX = tmpMainWC.posX();
			var posY = tmpMainWC.posY();
			var dimX = tmpMainWC.dimX();
			var dimY = tmpMainWC.dimY();
			tmpBW.write("win," + isShown + "," + posX + "," + posY + "," + dimX + "," + dimY + "\n\n");

			var tmpMainSplitSize = aMainAppCfg.mainSplitSize();
			tmpBW.write("mainSplit," + tmpMainSplitSize + "\n\n");
		}
		catch (IOException aExp)
		{
			aTask.logRegln("Failed to save file: " + tmpFile);
			aTask.logRegln(ThreadUtil.getStackTraceClassic(aExp));
			return;
		}
	}

	/**
	 * Utility helper method that will write out the (appropriate) header for the
	 * main window configuration file.
	 */
	private static void writeHeader(BufferedWriter aBW, boolean aIsCatalog, boolean aIsPainter) throws IOException
	{
		var alwaysTrue = true;
		writeLine(aBW, alwaysTrue, "# SBMT Main Window Configuration File");
		writeLine(aBW, alwaysTrue, "# ------------------------------------------------------------------------------");
		writeLine(aBW, alwaysTrue, "# File consists of a list of instructions.");
		writeLine(aBW, alwaysTrue, "# <aInstr>,<...>*");
		writeLine(aBW, alwaysTrue, "# where <aInstr> can be one of the following:");
		writeLine(aBW, alwaysTrue, "#   ver:       Specifies the version of this configuration file.");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "#   win:       Specifies the main window configuration.");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "#   mainSplit: Specifies the x-size of the main window's control panel");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "# Listed below are the available instructions and details on the associated");
		writeLine(aBW, alwaysTrue, "# parameters:");
		writeLine(aBW, alwaysTrue, "#   ver,<aVerStr>");
		writeLine(aBW, alwaysTrue, "#      aVerStr:   The version of the file. The expected format is: yyyy.mm");
		writeLine(aBW, alwaysTrue, "#                 The current supported version is: " + RefVersion);
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "#   win,<aIsVisible>,<aPosX>,<aPosY>,<aDimX>,<aDimY>");
		writeLine(aBW, alwaysTrue, "#      aVisible:  Whether this window will be shown. This parameter is ignored.");
		writeLine(aBW, alwaysTrue, "#      aPosX:     X-Position of the main window.");
		writeLine(aBW, alwaysTrue, "#      aPosY:     Y-Position of the main window.");
		writeLine(aBW, alwaysTrue, "#      aDimX:     X-Dimension of the main window.");
		writeLine(aBW, alwaysTrue, "#      aDimY:     Y-Dimension of the main window.");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "#   mainSplit,<aSize>");
		writeLine(aBW, alwaysTrue, "#      aSize:     The size of the left panel.");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "#");
		aBW.write("ver," + RefVersion + "\n\n");
	}

	/**
	 * Helper method that optionally writes out the specified line.
	 *
	 * @param aBool If true then aMsg will be output to the {@link BufferedWriter}.
	 */
	private static void writeLine(BufferedWriter aBW, boolean aBool, String aMsg) throws IOException
	{
		if (aBool == false)
			return;

		aBW.write(aMsg + "\n");
	}

}
