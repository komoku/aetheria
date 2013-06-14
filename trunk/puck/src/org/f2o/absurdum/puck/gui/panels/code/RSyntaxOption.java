package org.f2o.absurdum.puck.gui.panels.code;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

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
	
	private static String[] themeNames = new String[] { "default","default-alt","dark","eclipse","idea","vs" };
	private static Theme[] themes = new Theme[themeNames.length];
	private static String themePackage = "org/f2o/absurdum/puck/staticconf/rsthemes";
	
	private static boolean THEMES_COLOR_ONLY = true; //apply only color when applying themes (not font size).
	
	static
	{
		optionScopes.put("rsyntaxWordWrap", ALL_AREAS);
		//optionScopes.put("rsyntaxShowLineNumbers", ALL_AREAS); //scroll pane
		optionScopes.put("rsyntaxShowTabLines", ALL_AREAS);
		optionScopes.put("rsyntaxTabsEmulated", ALL_AREAS);
		
		optionTogglers.put("rsyntaxWordWrap", new RSyntaxOptionApplier()
		{
			public void setOptionEnabled(RSyntaxTextArea ta, boolean enabled) 
			{
				ta.setLineWrap(enabled);
			}
			
		});
		
		optionTogglers.put("rsyntaxShowTabLines", new RSyntaxOptionApplier()
		{
			public void setOptionEnabled(RSyntaxTextArea ta, boolean enabled) 
			{
				ta.setPaintTabLines(enabled);
			}
		});
		
		optionTogglers.put("rsyntaxTabsEmulated", new RSyntaxOptionApplier()
		{
			public void setOptionEnabled(RSyntaxTextArea ta, boolean enabled) 
			{
				if ( enabled ) ta.convertTabsToSpaces();
				else ta.convertSpacesToTabs();
				ta.setTabsEmulated(enabled);
			}
		});
		
		//load themes
		for ( int i = 0 ; i < themeNames.length ; i++ )
		{
			final String themeName = themeNames[i];
			try
			{
				Theme theme = Theme.load(RSyntaxOption.class.getClassLoader().getResourceAsStream(themePackage+"/"+themeName+".xml"));
				themes[i] = theme;
			}
			catch ( IOException ioe )
			{
				System.err.println("Could not load theme " + themePackage+"/"+themeName+".xml");
			}
		}
		
		//theme option togglers
		for ( int i = 0 ; i < themes.length ; i++ )
		{
			final String themeName = themeNames[i];
			final Theme theme = themes[i];
			if ( theme != null )
			{
				optionTogglers.put("rsyntaxTheme"+themeName, new RSyntaxOptionApplier()
				{
					public void setOptionEnabled(RSyntaxTextArea ta, boolean enabled)
					{
						if ( enabled )
						{
							applyTheme ( theme , ta );
						}
					}
				}
				);
			}
		}
		
		
	}
	
	private static void applyTheme ( Theme theme , RSyntaxTextArea ta )
	{
		RSyntaxTextAreaRegistry.getInstance().setThemeForNewAreas(theme); //so that new text areas are created with the current theme
		if ( THEMES_COLOR_ONLY )
		{
			int theFontSize = ta.getFont().getSize();
			theme.apply(ta);
			ta.setFont(ta.getFont().deriveFont(theFontSize));
		}
		else
		{
			theme.apply(ta);
		}
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

	
	/**
	 * Returns the supported theme names.
	 * @return
	 */
	public static String[] getThemeNames ()
	{
		return themeNames;
	}

	
}
