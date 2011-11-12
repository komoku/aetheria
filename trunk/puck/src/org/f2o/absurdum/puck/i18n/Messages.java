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

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:38:28
 */
public /*Singleton*/ class Messages 
{

	private static Messages instance;
	
	private Properties properties;
	
	/**
	 * Obtains the language code for the user's preferred language as configured in the AGE configuration file.
	 * If no language is configured in the file, then if the JVM locale is Spanish or English, it returns that language.
	 * In other case, it returns Spanish.
	 * @return
	 */
	private String getPreferredLanguage()
	{
		String configuredLanguage = PuckConfiguration.getInstance().getProperty("language");
		if ( configuredLanguage != null ) return configuredLanguage;
		String sysLanguage = Locale.getDefault().getLanguage();
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
		return "es";
	}
	
	private Messages()
	{
		String lang = getPreferredLanguage();
		try
		{
			init("org/f2o/absurdum/puck/i18n/Messages."+lang);
		}
		catch ( Exception exc )
		{
			System.err.println("Could not load PUCK UI message file for language " + lang + ", will try with the default message file.\n");
			init("org/f2o/absurdum/puck/i18n/Messages.properties");
		}
	}
	
	public void init ( String pathToMessageFile )
	{	
		properties = new Properties();
		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(pathToMessageFile);
			if ( is == null ) throw new IOException("getResourceAsStream returned null stream for " + pathToMessageFile);
			properties.load( is );
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
	
	public static Messages getInstance()
	{
		if ( instance == null )
			instance = new Messages();
		return instance;
	}

}
