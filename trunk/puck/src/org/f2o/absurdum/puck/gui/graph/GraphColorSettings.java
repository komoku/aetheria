package org.f2o.absurdum.puck.gui.graph;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.util.ColorUtils;

public class GraphColorSettings 
{

	private static GraphColorSettings instance;
	
	/**
	 * Map of color settings, containing things like the background color, grid color, etc.
	 */
	private Map colorSettings = Collections.synchronizedMap ( new HashMap() );
	
	private GraphColorSettings()
	{
		;
	}
	
	public static GraphColorSettings getInstance()
	{
		if ( instance == null ) instance = new GraphColorSettings();
		return instance;
	}
	
	/**
	 * Obtains the color setting with the given name.
	 * @param name
	 * @return
	 */
	public Color getColorSetting ( String name )
	{
		Color result;
		result = (Color) colorSettings.get(name);
		if ( result != null ) return result;
		String configString = PuckConfiguration.getInstance().getProperty("mapColor."+name);
		if ( configString != null )
		{
			result = ColorUtils.stringToColor(configString);
			colorSettings.put(name, result);
			return result;
		}
		return null;
	}
	
	/**
	 * Sets the given color setting. Also sets it in the configuration file.
	 * @param name
	 * @param color
	 */
	public void setColorSetting ( String name , Color color )
	{
		colorSettings.put(name,color);
		PuckConfiguration.getInstance().setProperty("mapColor."+name,ColorUtils.colorToString(color));
	}
	
	
}
