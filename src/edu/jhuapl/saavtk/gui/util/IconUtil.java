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
	private static Icon cIconActionAbort = null;
	private static Icon cIconActionConfig = null;
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
	 * Returns the icon that should be used for "Configure" action buttons
	 */
	public static Icon getActionConfig()
	{
		if (cIconActionConfig == null)
			cIconActionConfig = IconUtil.loadIcon("resources/icons/ActionConfig.24.png");

		return cIconActionConfig;
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

	/**
	 * Utility helper method to load an icon from the specified resource.
	 */
	private static ImageIcon loadIcon(String aIconPath)
	{
		return new ImageIcon(ClassLoader.getSystemResource(aIconPath));
	}

}
