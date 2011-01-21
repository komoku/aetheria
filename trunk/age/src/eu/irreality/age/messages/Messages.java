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

import eu.irreality.age.ReturnValue;
import eu.irreality.age.World;
import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.filemanagement.Paths;

public class Messages 
{
	
	private static Messages defaultInstance;
	
	private Properties properties;
	
	//this map stores messages that are modified temporarily (for one use only).
	private Map tempChanged = new HashMap();

	public static String defaultMessagePath = Paths.LANG_FILES_PATH + "/messages.lan";
	
	/**
	 * If a Messages instance is associated to a particular world, then the getMessage() method will attempt to call scripting code
	 * in the world to generate custom messages.
	 */
	private World world = null; 
	
	public void setWorld ( World w )
	{
		this.world = w;
	}
	
	public Messages(String path)
	{	
		properties = new Properties();
		try
		{
			//InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/i18n/Messages.properties");
			URL u = this.getClass().getClassLoader().getResource(path);
			InputStream is = u.openStream();
			if ( is == null ) throw new IOException("Could not read message file " + u);
			properties.load( new InputStreamReader ( is , "UTF-8" ) );
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
	    properties.load( new InputStreamReader ( is , "UTF-8" ) );
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
		if ( mess == null && !(this == getDefaultInstance()) ) mess = getDefaultInstance().getMessage(key); //fallback to default instance
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
		if ( mess == null && !(this == getDefaultInstance()) ) mess = getDefaultInstance().getMessage(key,argumentsForScriptCode); //fallback
		return ( mess != null ? mess : "??" + key + "??" );
	}
	
	public static Messages getDefaultInstance()
	{
		if ( defaultInstance == null )
			defaultInstance = new Messages(defaultMessagePath);
		return defaultInstance;
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
	
	public String getMessage ( String key , String placeholder , String substitution , Object[] argumentsForScriptCode )
	{
	    return buildMessage ( getMessage(key,argumentsForScriptCode) , placeholder , substitution );
	}
	
	public String getMessage ( String key , String p1 , String s1 , String p2 , String s2 , Object[] argumentsForScriptCode  )
	{
		return buildMessage ( getMessage(key,argumentsForScriptCode) , p1 , s1 , p2 , s2 );
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
	
}