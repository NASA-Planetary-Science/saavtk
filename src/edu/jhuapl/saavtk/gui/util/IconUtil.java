package edu.jhuapl.saavtk.gui.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Collection of utility methods that provide a singular location for the
 * definition, management, and retrieval of icons.
 *
 * @author lopeznr1
 */
public class IconUtil
{
	// Cache vars
	private static Icon cIconAppMain = null;

	private static Icon cIconActionAbort = null;
	private static Icon cIconActionCenter = null;
	private static Icon cIconActionConfig = null;
	private static Icon cIconActionInfo = null;
	private static Icon cIconActionReset = null;

	private static Icon cIconItemAdd = null;
	private static Icon cIconItemDel = null;
	private static Icon cIconItemEdit = null;
	private static Icon cIconItemEditTrue = null;
	private static Icon cIconItemSpawn = null;
	private static Icon cIconItemSync = null;
	private static Icon cIconItemSyncTrue = null;

	private static Icon cIconSelectAll = null;
	private static Icon cIconSelectNone = null;
	private static Icon cIconSelectInvert = null;
	
	private static Icon cIconShow = null;
	private static Icon cIconHide = null;
	
	private static Icon cIconRecord = null;
	private static Icon cIconStop = null;
	
	private static Icon cIconFont = null;

	/**
	 * Returns the icon that should be used for "Abort" action buttons
	 */
	public static Icon getActionAbort()
	{
		if (cIconActionAbort == null)
			cIconActionAbort = IconUtil.loadIcon("resources/icons/ActionAbort.24.png");

		return cIconActionAbort;
	}

	/**
	 * Returns the icon that should be used for "Center" action buttons
	 */
	public static Icon getActionCenter()
	{
		if (cIconActionCenter == null)
			cIconActionCenter = IconUtil.loadIcon("resources/icons/ActionCenter.24.png");

		return cIconActionCenter;
	}

	/**
	 * Returns the icon that should be used for "Configure" action buttons
	 */
	public static Icon getActionConfig()
	{
		if (cIconActionConfig == null)
			cIconActionConfig = IconUtil.loadIcon("resources/icons/ActionConfig.24.png");

		return cIconActionConfig;
	}

	/**
	 * Returns the icon that should be used for "Information" action buttons
	 */
	public static Icon getActionInfo()
	{
		if (cIconActionInfo == null)
			cIconActionInfo = IconUtil.loadIcon("resources/icons/ActionInfo.24.png");

		return cIconActionInfo;
	}

	/**
	 * Returns the icon that should be used for "Reset" action buttons
	 */
	public static Icon getActionReset()
	{
		if (cIconActionReset == null)
			cIconActionReset = IconUtil.loadIcon("resources/icons/ActionReset.24.png");

		return cIconActionReset;
	}

	/**
	 * Returns the icon that should be used as the application icon.
	 */
	public static Icon getAppMain()
	{
		if (cIconAppMain == null)
			cIconAppMain = IconUtil.loadIcon("resources/icons/MainApp.Eros.png");

		return cIconAppMain;
	}

	/**
	 * Returns the icon image that should be used as the application icon.
	 * <P>
	 * Note this method does not utilize any caching mechanism.
	 */
	public static ImageIcon getAppMainImage()
	{
		return IconUtil.loadIcon("resources/icons/MainApp.Eros.png");
	}

	/**
	 * Returns the icon that should be used for "Add Items" buttons
	 */
	public static Icon getItemAdd()
	{
		if (cIconItemAdd == null)
			cIconItemAdd = IconUtil.loadIcon("resources/icons/ItemAdd.24.png");

		return cIconItemAdd;
	}

	/**
	 * Returns the icon that should be used for "Delete Items" buttons
	 */
	public static Icon getItemDel()
	{
		if (cIconItemDel == null)
			cIconItemDel = IconUtil.loadIcon("resources/icons/ItemDelete.24.png");

		return cIconItemDel;
	}

	/**
	 * Returns the icon that should be used for "Edit Items" buttons
	 */
	public static Icon getItemEdit()
	{
		if (cIconItemEdit == null)
			cIconItemEdit = IconUtil.loadIcon("resources/icons/ItemEdit.False.24.png");

		return cIconItemEdit;
	}

	/**
	 * Returns the icon that should be used for "Edit Items" toggle == false button.
	 */
	public static Icon getItemEditFalse()
	{
		// Delegate
		return getItemEdit();
	}

	/**
	 * Returns the icon that should be used for "Edit Items" toggle == true button.
	 */
	public static Icon getItemEditTrue()
	{
		if (cIconItemEditTrue == null)
			cIconItemEditTrue = IconUtil.loadIcon("resources/icons/ItemEdit.True.24.png");

		return cIconItemEditTrue;
	}

	/**
	 * Returns the icon that should be used for "Spawn Items" buttons
	 */
	public static Icon getItemSpawn()
	{
		if (cIconItemSpawn == null)
			cIconItemSpawn = IconUtil.loadIcon("resources/icons/ItemSpawn.24.png");

		return cIconItemSpawn;
	}

	/**
	 * Returns the icon that should be used for "Sync Items" buttons
	 */
	public static Icon getItemSync()
	{
		if (cIconItemSync == null)
			cIconItemSync = IconUtil.loadIcon("resources/icons/ItemSync.False.24.png");

		return cIconItemSync;
	}

	/**
	 * Returns the icon that should be used for "Sync Items" toggle == false button.
	 */
	public static Icon getItemSyncFalse()
	{
		// Delegate
		return getItemSync();
	}

	/**
	 * Returns the icon that should be used for "Sync Items" toggle == true button.
	 */
	public static Icon getItemSyncTrue()
	{
		if (cIconItemSyncTrue == null)
			cIconItemSyncTrue = IconUtil.loadIcon("resources/icons/ItemSync.True.24.png");

		return cIconItemSyncTrue;
	}

	/**
	 * Returns the icon that should be used for "Select All" buttons
	 */
	public static Icon getSelectAll()
	{
		if (cIconSelectAll == null)
			cIconSelectAll = IconUtil.loadIcon("resources/icons/ItemSelectAll.24.png");

		return cIconSelectAll;
	}

	/**
	 * Returns the icon that should be used for "Select Invert" buttons
	 */
	public static Icon getSelectInvert()
	{
		if (cIconSelectInvert == null)
			cIconSelectInvert = IconUtil.loadIcon("resources/icons/ItemSelectInvert.24.png");

		return cIconSelectInvert;
	}

	/**
	 * Returns the icon that should be used for "Select None" buttons
	 */
	public static Icon getSelectNone()
	{
		if (cIconSelectNone == null)
			cIconSelectNone = IconUtil.loadIcon("resources/icons/ItemSelectNone.24.png");

		return cIconSelectNone;
	}
	
	//Icons made by <a href="https://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
	/**
	 * Returns the icon that should be used for "Show" buttons
	 */
	public static Icon getItemShow()
	{
		if (cIconShow == null)
			cIconShow = IconUtil.loadIcon("resources/icons/show.png");

		return cIconShow;
	}
	
	public static Icon getItemHide()
	{
		if (cIconHide == null)
			cIconHide = IconUtil.loadIcon("resources/icons/hide.png");

		return cIconHide;
	}
	
	//Icons made by <a href="https://www.flaticon.com/authors/google" title="Google">Google</a> from www.flaticon.com
	public static Icon getRecord()
	{
		if (cIconRecord == null)
			cIconRecord = IconUtil.loadIcon("resources/icons/record.png");

		return cIconRecord;
	}
	
	//Icons made by <a href="https://www.flaticon.com/authors/pixel-perfect" title="Pixel perfect">Pixel perfect</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>
	public static Icon getStop()
	{
		if (cIconStop == null)
			cIconStop = IconUtil.loadIcon("resources/icons/stop.png");

		return cIconStop;
	}
	
	//<div>Icons made by <a href="https://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>
	public static Icon getFont()
	{
		if (cIconFont == null)
			cIconFont = IconUtil.loadIcon("resources/icons/font.png");

		return cIconFont;
	}
	
	/**
	 * Utility helper method to load an icon from the specified resource.
	 */
	private static ImageIcon loadIcon(String aIconPath)
	{
		return new ImageIcon(ClassLoader.getSystemResource(aIconPath));
	}

}
