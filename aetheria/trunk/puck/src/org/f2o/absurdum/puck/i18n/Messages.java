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
import java.util.Properties;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:38:28
 */
public /*Singleton*/ class Messages 
{

	private static Messages instance;
	
	private Properties properties;
	
	private Messages()
	{	
		properties = new Properties();
		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/i18n/Messages.properties");
			if ( is == null ) throw new IOException("getResourceAsStream returned null stream for Messages.properties");
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
