package eu.irreality.age.messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import bsh.This;

import eu.irreality.age.NaturalLanguage;
import eu.irreality.age.ReturnValue;
import eu.irreality.age.World;
import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.util.UTF8PropertiesLoader;

public class Messages 
{
	
	private static Map defaultInstances = new HashMap(); //String (language code) -> Messages & World -> Messages
	
	private Properties properties;
	
	//this map stores messages that are modified temporarily (for one use only).
	private Map tempChanged = new HashMap();

	public static String defaultMessagePath = Paths.LANG_FILES_PATH + "/messages.lan";
	
	public static String getPathForLanguage ( String languageCode )
	{
		return Paths.LANG_FILES_PATH + "/" + languageCode + "/messages.lan";
	}
	
	/**
	 * If a Messages instance is associated to a particular world, then the getMessage() method will attempt to call scripting code
	 * in the world to generate custom messages.
	 */
	private World world = null; 
	
	public void setWorld ( World w )
	{
		this.world = w;
	}
	
	/*
	public static void printReport()
	{
		System.err.println(defaultInstances);
		System.err.println(defaultInstances.keySet());
		System.err.println(defaultInstances.values());
	}
	*/
	
	public Messages(String path)
	{	
		properties = new Properties();
		try
		{
			//InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/i18n/Messages.properties");
			URL u = this.getClass().getClassLoader().getResource(path);
			InputStream is = u.openStream();
			if ( is == null ) throw new IOException("Could not read message file " + u);
			//properties.load( new InputStreamReader ( is , "UTF-8" ) );
		    //1.5 compatible:
			//TODO: apply this cheap hack only if java version < 1.6
		    UTF8PropertiesLoader.loadProperties(properties,is,"UTF-8");
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	public Messages(URL u) throws IOException
	{
	    properties = new Properties();
	    InputStream is = u.openStream();
	    if ( is == null ) throw new IOException("Could not read message file " + u);
	    //properties.load( new InputStreamReader ( is , "UTF-8" ) );
	    //1.5 compatible:
	    //TODO: apply this cheap hack only if java version < 1.6
	    UTF8PropertiesLoader.loadProperties(properties,is,"UTF-8");
	    is.close();
	}
	
	private String getEntryForKey ( String key )
	{
		String tempChangedEntry = (String) tempChanged.get(key);
		if ( tempChangedEntry != null )
		{
			tempChanged.remove(key); //remove temporary mapping
			return tempChangedEntry;
		}
		return properties.getProperty(key);
	}
	
	public String getMessage ( String key )
	{
		String mess = getEntryForKey ( key );
		if ( mess == null && !(this == getDefaultInstance(world)) ) mess = getDefaultInstance(world).getMessage(key); //fallback to default instance
		return ( mess != null ? mess : "??" + key + "??" );
	}
	
	/**
	 * The parameter argumentsForScriptCode is passed to the beanshell code so the user can employ it to generate custom messages
	 * if necessary.
	 */
	public String getMessage ( String key , Object[] argumentsForScriptCode )
	{
		if ( world != null )
		{
			try
			{
				ReturnValue retval = new ReturnValue(null);
				world.execCode( "getMessage" , new Object[] { key , argumentsForScriptCode } , retval );
				if ( retval.getRetVal() != null ) return (String)retval.getRetVal();
			}
			catch (bsh.TargetError bshte)
			{
				world.writeError("bsh.TargetError found at getMessage routine\n" );
				world.writeError(ExceptionPrinter.getExceptionReport(bshte));
				world.writeError( bshte.printTargetError(bshte) );
			}
		}
		
		String mess = getEntryForKey ( key );
		if ( mess == null && !(this == getDefaultInstance(world)) ) mess = getDefaultInstance(world).getMessage(key,argumentsForScriptCode); //fallback
		return ( mess != null ? mess : "??" + key + "??" );
	}
	
	/**
	 * Returns the default repository of messages for a given language code. The same instance is always returned for the same language code.
	 * @param languageCode
	 * @return
	 */
	public static Messages getDefaultInstance(String languageCode)
	{
		Messages cached = (Messages) defaultInstances.get(languageCode);
		if ( cached == null )
		{
			cached = new Messages(getPathForLanguage(languageCode));
			defaultInstances.put(languageCode,cached);
		}
		return cached;
	}
	
	/**
	 * Returns the default repository of messages provided by AGE for a given world, i.e., the repository of messages for the language code associated with the
	 * language of that world, and with a pointer to the world.
	 * @param w
	 * @return
	 */
	public static Messages getDefaultInstance(World w)
	{
		Messages cached = (Messages) defaultInstances.get(w);
		if ( cached != null ) return cached;
		
		String languageCode = null;
		if ( w != null ) languageCode = w.getLanguage().getLanguageCode();
		if ( languageCode == null ) languageCode = NaturalLanguage.DEFAULT_LANGUAGE_CODE;
		//return getDefaultInstance ( languageCode );
		Messages theInstance = new Messages(getPathForLanguage(languageCode));
		theInstance.setWorld(w);
		defaultInstances.put(w,theInstance);
		//System.err.println("Caching: " + w + "->" + theInstance);
		//new Throwable().printStackTrace();
		return theInstance;
	}
	
	/**
	 * Removes entry for world w from the cache if it is present.
	 * @param w
	 */
	public static void clearCache ( World w )
	{
		Messages cached = (Messages) defaultInstances.get(w);
		if ( cached != null )
		{
			//System.err.println("Found cached entry: " + cached + " for world " + w);
			cached.setWorld(null);
		}
		//else
		//	System.err.println("Not found cached entry for world " + w);
		defaultInstances.remove(w);
	}
	
	public void setMessage ( String key , String message )
	{
		properties.setProperty( key , message );
	}
	
	public void setNextMessage ( String key , String message )
	{
		tempChanged.put(key,message);
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
	
	public String getMessage ( String key , String p1 , String s1 , String p2 , String s2 , String p3 , String s3 , String p4 , String s4 )
	{
		return buildMessage ( getMessage(key) , p1 , s1 , p2 , s2 , p3 , s3 , p4 , s4 );
	}
	
	public String getMessage ( String key , String placeholder , String substitution , Object[] argumentsForScriptCode )
	{
	    return buildMessage ( getMessage(key,argumentsForScriptCode) , placeholder , substitution );
	}
	
	public String getMessage ( String key , String p1 , String s1 , String p2 , String s2 , Object[] argumentsForScriptCode  )
	{
		return buildMessage ( getMessage(key,argumentsForScriptCode) , p1 , s1 , p2 , s2 );
	}
	
	public String getMessage ( String key , String p1 , String s1 , String p2 , String s2 , String p3 , String s3 , Object[] argumentsForScriptCode  )
	{
		return buildMessage ( getMessage(key,argumentsForScriptCode) , p1 , s1 , p2 , s2 , p3 , s3 );
	}
	
	public String getMessage ( String key , String p1 , String s1 , String p2 , String s2 , String p3 , String s3 , String p4 , String s4 , Object[] argumentsForScriptCode  )
	{
		return buildMessage ( getMessage(key,argumentsForScriptCode) , p1 , s1 , p2 , s2 , p3 , s3 , p4 , s4 );
	}
	
	/**
	 * buildMessage ( "Coges $object", "$object", "la espada" ) returns "Coges la espada".
	 * @param messString
	 * @param placeholder
	 * @return
	 */
	public static String buildMessage ( String messString , String placeholder , String substitution )
	{
		return messString.replace(placeholder,substitution);
	}

	public static String buildMessage ( String messString , String p1 , String s1 , String p2 , String s2 )
	{
		return buildMessage ( buildMessage ( messString,p1,s1 ) , p2 , s2 );
	}
	
	public static String buildMessage ( String messString , String p1 , String s1 , String p2 , String s2 , String p3 , String s3 )
	{
		return buildMessage ( buildMessage ( messString,p1,s1,p2,s2 ) , p3 , s3 );
	}
	
	public static String buildMessage ( String messString , String p1 , String s1 , String p2 , String s2 , String p3 , String s3 , String p4 , String s4 )
	{
		return buildMessage ( buildMessage ( messString,p1,s1,p2,s2,p3,s3 ) , p4 , s4 );
	}
	
}
