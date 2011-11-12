/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:38:28
 * as file Messages.java on package org.f2o.absurdum.puck.i18n
 */
package eu.irreality.age.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import eu.irreality.age.swing.config.AGEConfiguration;

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
	 * Obtains the language code for the user's preferred language as configured in the AGE configuration (gotten from file).
	 * If no language is configured in the file, then if the JVM locale is Spanish or English, it returns that language.
	 * In other case, it returns Spanish.
	 * @return
	 */
	public String getPreferredLanguage()
	{
		String configuredLanguage = AGEConfiguration.getInstance().getProperty("language");
		if ( configuredLanguage != null ) return configuredLanguage;
		String sysLanguage = Locale.getDefault().getLanguage();
		if ( sysLanguage.contains("es") )
		{
			AGEConfiguration.getInstance().setProperty("language","es");
			return "es";
		}
		if ( sysLanguage.contains("en") )
		{
			AGEConfiguration.getInstance().setProperty("language","en");
			return "en";
		}
		return "es";
	}
	
	public void setPreferredLanguage ( String language )
	{
		String oldLanguage = getPreferredLanguage();
		if ( !oldLanguage.equals(language) )
		{
			AGEConfiguration.getInstance().setProperty("language",language);
			initForLanguage(language);
		}
	}
	
	private void init ( String pathToMessageFile )
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
	
	private void initForLanguage ( String lang )
	{
		init("eu/irreality/age/i18n/UIMessages." + lang);
	}
	
	private UIMessages()
	{	
		String lang = getPreferredLanguage();
		try
		{
			initForLanguage(lang);
		}
		catch (Exception exc)
		{
			System.err.println("Could not load AGE UI message file for language " + lang + ", will try with the default message file.\n");
			init("eu/irreality/age/i18n/UIMessages.properties");
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
	 * buildMessage ( "Coges $object", "$object", "la espada" ) returns "Coges la espada".
	 * @param messString
	 * @param placeholder
	 * @return
	 */
	private static String buildMessage ( String messString , String placeholder , String substitution )
	{
		return messString.replace(placeholder,substitution);
	}
	
	private static String buildMessage ( String messString , String p1 , String s1 , String p2 , String s2 )
	{
		return buildMessage ( buildMessage ( messString,p1,s1 ) , p2 , s2 );
	}
	
	private static String buildMessage ( String messString , String p1 , String s1 , String p2 , String s2 , String p3 , String s3 )
	{
		return buildMessage ( buildMessage ( messString,p1,s1,p2,s2 ) , p3 , s3 );
	}
	
	public String getMessage ( String key , String placeholder , String substitution )
	{
		return buildMessage ( getMessage(key) , placeholder , substitution );
	}
	
	public String getMessage ( String key , String p1 , String s1 , String p2 , String s2 )
	{
		return buildMessage ( getMessage(key) , p1 , s1 , p2 , s2 );
	}
	
	public String getMessage ( String key , String p1 , String s1 , String p2 , String s2 , String p3 , String s3 )
	{
		return buildMessage ( getMessage(key) , p1 , s1 , p2 , s2 , p3 , s3 );
	}

}
