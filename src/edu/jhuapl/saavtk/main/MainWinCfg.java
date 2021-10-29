package edu.jhuapl.saavtk.main;

import java.awt.Toolkit;

import glum.gui.info.WindowCfg;

/**
 * Record used to store the configuration of the main application window.
 *
 * @author lopeznr1
 */
public record MainWinCfg(WindowCfg mainWC, int mainSplitSize)
{
	/**
	 * Utility helper method that creates the default {@link MainWinCfg}.
	 * <p>
	 * The default configuration will position the window at the center and take up
	 * 75% of the width and height.
	 */
	public static MainWinCfg formDefaultConfiguration()
	{
		var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		var dimX = (int) (screenSize.width * 0.75);
		if (dimX < 800)
			dimX = 800;
		var dimY = (int) (screenSize.height * 0.75);
		if (dimY < 600)
			dimY = 600;

		var posX = (int) (screenSize.width / 2.0 - dimX / 2.0);
		if (posX < 0)
			posX = 0;
		var posY = (int) (screenSize.height / 2.0 - dimY / 2.0);
		if (posY < 0)
			posY = 0;

		var mainWC = new WindowCfg(true, posX, posY, dimX, dimY);
		var mainSplitSize = 320;
		return new MainWinCfg(mainWC, mainSplitSize);
	}
}
