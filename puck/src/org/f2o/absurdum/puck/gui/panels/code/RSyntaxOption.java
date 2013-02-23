package org.f2o.absurdum.puck.gui.panels.code;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**A configuration option that applies to a range of RSyntaxTextAreas.*/
public class RSyntaxOption 
{
	
	private String configOptionName;
	
	private boolean enabled;
	
	/**This is the object that actually knows how to make the option take real effect in a text area.*/
	private RSyntaxOptionApplier toggler;
	
	//Possible scopes of options (which text areas they affect)
	public static int ALL_AREAS = 0;
	public static int SMALL_AREAS = 1;
	public static int LARGE_AREAS = 2;
	
	private int optionScope;
	
	private static Map instances = Collections.synchronizedMap( new HashMap() );
	
	private static Map optionScopes = Collections.synchronizedMap( new HashMap() );
	private static Map optionTogglers = Collections.synchronizedMap( new HashMap() );
	
	static
	{
		optionScopes.put("rsyntaxWordWrap", ALL_AREAS);
		
		optionTogglers.put("rsyntaxWordWrap", new RSyntaxOptionApplier()
		{
			public void setOptionEnabled(RSyntaxTextArea ta, boolean enabled) 
			{
				ta.setLineWrap(enabled);
			}
			
		});
	}
	
	private RSyntaxOption ( String configOptionName , int optionScope , RSyntaxOptionApplier toggler )
	{
		this.toggler = toggler;
		this.configOptionName = configOptionName;
		this.optionScope = optionScope;
		enabled = PuckConfiguration.getInstance().getBooleanProperty(configOptionName);
	}
	
	public static RSyntaxOption getInstanceFor ( String configOptionName )
	{
		RSyntaxOption instance;
		instance = (RSyntaxOption) instances.get(configOptionName);
		if ( instance != null ) return instance;
		//no instance yet for this config property: create and register it
		int optionScope;
		Integer optionScopeInteger = (Integer)optionScopes.get(configOptionName);
		if ( optionScopeInteger == null ) optionScope = ALL_AREAS;
		else optionScope = optionScopeInteger.intValue();
		RSyntaxOptionApplier toggler = (RSyntaxOptionApplier) optionTogglers.get(configOptionName);
		instance = new RSyntaxOption ( configOptionName , optionScope , toggler );
		instances.put(configOptionName, instance);
		return instance;
	}
	
	/**
	 * Returns whether the option is currently enabled.
	 * @return
	 */
	public boolean isOptionEnabled()
	{
		return enabled;
	}

	
	/**
	 * Sets the option as enabled or disabled in the text areas..
	 */
	public void setOptionEnabled(boolean enabled)
	{
		if ( optionScope == SMALL_AREAS || optionScope == ALL_AREAS )
		{
			List smallAreas = RSyntaxTextAreaRegistry.getInstance().getSmallTextAreas();
			for ( Iterator it = smallAreas.iterator() ; it.hasNext(); )
			{
				RSyntaxTextArea area = (RSyntaxTextArea) it.next();
				toggler.setOptionEnabled(area,enabled);
			}
		}
		if ( optionScope == LARGE_AREAS || optionScope == ALL_AREAS )
		{
			List largeAreas = RSyntaxTextAreaRegistry.getInstance().getLargeTextAreas();
			for ( Iterator it = largeAreas.iterator() ; it.hasNext(); )
			{
				RSyntaxTextArea area = (RSyntaxTextArea) it.next();
				toggler.setOptionEnabled(area,enabled);
			}
		}
		this.enabled = enabled;
	}

	

	
}
