package eu.irreality.age.messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import eu.irreality.age.filemanagement.Paths;

public class DefaultMessages 
{
	
	private static DefaultMessages instance;
	
	private Properties properties;

	public static File messageFile = new File ( Paths.getWorkingDirectory() , Paths.LANG_FILES_PATH + File.separatorChar + "messages.lan" );
	
	private DefaultMessages()
	{	
		properties = new Properties();
		try
		{
			//InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/i18n/Messages.properties");
			InputStream is = new FileInputStream ( messageFile );
			if ( is == null ) throw new IOException("Could not read default message file " + messageFile);
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
	
	public static DefaultMessages getInstance()
	{
		if ( instance == null )
			instance = new DefaultMessages();
		return instance;
	}
	
	public void setMessage ( String key , String message )
	{
		properties.setProperty( key , message );
	}
	
	public String getMessage ( String key , String placeholder , String substitution )
	{
		return buildMessage ( getMessage(key) , placeholder , substitution );
	}
	
	public String getMessage ( String key , String p1 , String s1 , String p2 , String s2 )
	{
		return buildMessage ( getMessage(key) , p1 , s1 , p2 , s2 );
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
