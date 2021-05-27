package edu.jhuapl.saavtk.view.light;

import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.Preferences;

/**
 * Collection of utility methods associated with handling the configuration of
 * light (source) as used in SBMT.
 *
 * @author lopeznr1
 */
public class LightUtil
{
	// Cache vars
	private static LightCfg cLightCfg = null;

	/**
	 * Utility method that returns the "system" {@link LightCfg}.
	 */
	public static synchronized LightCfg getSystemLightCfg()
	{
		if (cLightCfg != null)
			return cLightCfg;

		cLightCfg = loadLightCfgFromPreferences();
		return cLightCfg;
	}

	/**
	 * Utility method that sets the "system" {@link LightCfg}.
	 * <p>
	 * This method will have the following effects:
	 * <ul>
	 * <li>All top level {@link Renderer} lighting configuration being updated.
	 * <li>Preferences being updated via the {@link Preferences} mechanism.
	 * </ul>
	 */
	public static synchronized void setSystemLightCfg(LightCfg aLightCfg)
	{
		cLightCfg = aLightCfg;

		// Update the lighting configuration for all top level Renderers
		ViewManager tmpViewManager = ViewManager.getGlobalViewManager();
		if (tmpViewManager != null)
		{
			for (View aView : tmpViewManager.getAllViews())
			{
				Renderer tmpRenderer = aView.getRenderer();
				if (tmpRenderer != null)
					tmpRenderer.setLightCfg(aLightCfg);
			}
		}

		// Delegate serialization to Preferences object
		Preferences.getInstance().put(Preferences.LIGHTING_TYPE, aLightCfg.getType().toString());
		Preferences.getInstance().put(Preferences.LIGHT_INTENSITY, aLightCfg.getIntensity());
		Preferences.getInstance().put(Preferences.FIXEDLIGHT_LATITUDE, aLightCfg.getPositionLL().toDegrees().lat);
		Preferences.getInstance().put(Preferences.FIXEDLIGHT_LONGITUDE, aLightCfg.getPositionLL().toDegrees().lon);
		Preferences.getInstance().put(Preferences.FIXEDLIGHT_DISTANCE, aLightCfg.getPositionLL().rad);
	}

	/**
	 * Utility method that switches the specified {@link Renderer} to use the
	 * {@link LightingType#LIGHT_KIT} source.
	 * <p>
	 * Note that this method will preserve other state associated with other
	 * (unused) light sources.
	 */
	public static void switchToLightKit(Renderer aRenderer)
	{
		// Delegate
		LightCfg tmpLightCfg = aRenderer.getLightCfg();
		tmpLightCfg = new LightCfg(LightingType.LIGHT_KIT, tmpLightCfg.getPositionLL(), tmpLightCfg.getIntensity());

		aRenderer.setLightCfg(tmpLightCfg);
	}

	/**
	 * Utility helper method that loads a {@link LightCfg} from {@link Preferences}.
	 * <p>
	 * This basis of this method was originally in:
	 * edu.jhuapl.saavtk.gui.render.Renderer
	 */
	private static LightCfg loadLightCfgFromPreferences()
	{
		LatLon tmpPositionLL = new LatLon(Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LATITUDE, 90.0),
				Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LONGITUDE, 0.0),
				Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_DISTANCE, 1.0e8)).toRadians();

		double tmpIntensityPer = Preferences.getInstance().getAsDouble(Preferences.LIGHT_INTENSITY, 1.0);

		LightingType tmpType = LightingType
				.valueOf(Preferences.getInstance().get(Preferences.LIGHTING_TYPE, LightingType.LIGHT_KIT.toString()));

		return new LightCfg(tmpType, tmpPositionLL, tmpIntensityPer);
	}
}
