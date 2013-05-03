/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:38:28
 * as file Messages.java on package org.f2o.absurdum.puck.i18n
 */
package org.f2o.absurdum.puck.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;

import eu.irreality.age.swing.config.AGEConfiguration;
import eu.irreality.age.util.UTF8PropertiesLoader;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:38:28
 */
public /*Singleton*/ class UIMessages 
{

	private static UIMessages instance;
	
	private Properties properties;
	
	
	/**
	 * Obtains the language code for the user's preferred language as configured in the AGE configuration file.
	 * If no language is configured in the file, then if the JVM locale is Spanish or English, it returns that language.
	 * In other case, it returns English.
	 * @return
	 */
	public String getPreferredLanguage()
	{
		PuckConfiguration config = null;
		config = PuckConfiguration.getInstance();
		String configuredLanguage = config.getProperty("language");
		if ( configuredLanguage != null ) return configuredLanguage;
		String sysLanguage = Locale.getDefault().getLanguage();
		
		String[] supportedLanguages = getSupportedLanguages();
		for ( int i = 0 ; i < supportedLanguages.length ; i++ )
		{
			if ( sysLanguage.contains(supportedLanguages[i]) )
			{
				config.setProperty("language",supportedLanguages[i]);
				return supportedLanguages[i];
			}
		}
		/*
		if ( sysLanguage.contains("es") )
		{
			PuckConfiguration.getInstance().setProperty("language","es");
			return "es";
		}
		if ( sysLanguage.contains("en") )
		{
			PuckConfiguration.getInstance().setProperty("language","en");
			return "en";
		}
		*/
		return "en";
	}
	
	public void setPreferredLanguage ( String language )
	{
		String oldLanguage = getPreferredLanguage();
		if ( !oldLanguage.equals(language) )
		{
			PuckConfiguration.getInstance().setProperty("language",language);
			initForLanguage(language);
		}
	}
	
	private UIMessages()
	{
		String lang = getPreferredLanguage();
		try
		{
			initForLanguage(lang);
		}
		catch ( Exception exc )
		{
			System.err.println("Could not load PUCK UI message file for language " + lang + ", will try with the default message file.\n");
			init("org/f2o/absurdum/puck/i18n/Messages.properties");
		}
	}
	
	private void initForLanguage ( String lang )
	{
		init("org/f2o/absurdum/puck/i18n/Messages." + lang);
	}
	
	public void init ( String pathToMessageFile )
	{	
		properties = new Properties();
		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(pathToMessageFile);
			if ( is == null ) throw new IOException("getResourceAsStream returned null stream for " + pathToMessageFile);

			//properties.load( is );
		    //TODO: apply this cheap hack only if java version < 1.6
		    UTF8PropertiesLoader.loadProperties(properties,is,"UTF-8");
		    is.close();
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	public String getMessage ( String key )
	{
		String mess = properties.getProperty(key);
		return ( mess != null ? mess : "??" + key + "??" );
	}
	
	public static UIMessages getInstance()
	{
		if ( instance == null )
			instance = new UIMessages();
		return instance;
	}
	
	/**
	 * Return the language codes of the supported languages for the UI.
	 * At the moment, this is hardcoded.
	 * @return
	 */
	public String[] getSupportedLanguages()
	{
		return new String[] {"es","en","eo","gl"};
	}

}
