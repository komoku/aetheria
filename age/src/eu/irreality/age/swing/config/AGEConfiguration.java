package eu.irreality.age.swing.config;

/*
 * (c) 2005-2010 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/*
 Created 2010-11-09
 */

public class AGEConfiguration 
{

	private Properties properties = null;
	
	private static String FILENAME = "age.properties";
	
	private static Properties defaultProperties;
	
	static
	{
		defaultProperties = new Properties();
	
		defaultProperties.setProperty("nRecentFiles", "5");
	
		defaultProperties.setProperty("sdiWindowWidth", "600");
		defaultProperties.setProperty("sdiWindowHeight", "440");
		defaultProperties.setProperty("sdiWindowMaximized", "false");
		defaultProperties.setProperty("sdiWindowLocationX","100");
		defaultProperties.setProperty("sdiWindowLocationY","100");
		
		//these'll be 0 by default and sorted out by the window itself
		//defaultProperties.setProperty("loaderWindowWidth", "770");
		//defaultProperties.setProperty("loaderWindowHeight", "570");
		defaultProperties.setProperty("loaderWindowMaximized", "false");
		//defaultProperties.setProperty("loaderWindowLocationX","50");
		//defaultProperties.setProperty("loaderWindowLocationY","50");
		defaultProperties.setProperty("catalogURL", "http://www.caad.es/aetheria/online-catalog.xml");
		
		defaultProperties.setProperty("mdiWindowWidth", "800");
		defaultProperties.setProperty("mdiWindowHeight", "600");
		defaultProperties.setProperty("mdiWindowMaximized", "true");
		defaultProperties.setProperty("mdiWindowLocationX","0");
		defaultProperties.setProperty("mdiWindowLocationY","0");
		
		defaultProperties.setProperty("mdiSubwindowWidth", "600");
		defaultProperties.setProperty("mdiSubwindowHeight", "400");
		defaultProperties.setProperty("mdiSubwindowMaximized", "true");
		defaultProperties.setProperty("mdiSubwindowLocationX","0");
		defaultProperties.setProperty("mdiSubwindowLocationY","0");
		
		//defaultProperties.setProperty("cscSound","true"); //not saved at the moment
		defaultProperties.setProperty("cscTextFx","true");
		defaultProperties.setProperty("cscBlindAcc","false");
		
		defaultProperties.setProperty("cscDefaultFontName","Lucida Sans Typewriter");
		defaultProperties.setProperty("cscDefaultFontSize","16");
		
		defaultProperties.setProperty("lastRemoteIp","127.0.0.1");
		
	}
	
	private static AGEConfiguration instance;
	
	public static AGEConfiguration getInstance() 
	{
		if ( instance == null )
				instance = new AGEConfiguration();
		return instance;
	}
		
	private AGEConfiguration()
	{
		System.out.println("AGEConfiguration constructor");
		properties = new Properties(defaultProperties);
		try
		{
			properties.load ( new FileInputStream(new File(FILENAME)) );
		}
		catch ( FileNotFoundException fnfe )
		{
			System.out.println("AGE configuration file not found.");
		}
		catch ( NullPointerException npe )
		{
			System.out.println("AGE configuration file not found.");
		}
		catch ( IOException ioe )
		{
			System.out.println("Error reading AGE configuration file.");
		}
		updateRecentFilesListFromProperties();
	}
	
	public int getIntegerProperty ( String key , int defaultVal )
	{
		try
		{
			return Integer.valueOf(properties.getProperty(key)).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			return defaultVal;
		}
	}
	
	public int getIntegerProperty ( String key ) { return getIntegerProperty(key,0); }
	
	public boolean getBooleanProperty ( String key , boolean defaultVal )
	{
		try
		{
			return Boolean.valueOf(properties.getProperty(key)).booleanValue();
		}
		catch ( NumberFormatException nfe )
		{
			return defaultVal;
		}
	}
	
	public boolean getBooleanProperty ( String key ) { return getBooleanProperty(key,false); }
	
	public double getDoubleProperty ( String key , double defaultVal )
	{
		try
		{
			return Double.valueOf(properties.getProperty(key)).doubleValue();
		}
		catch ( NumberFormatException nfe )
		{
			return defaultVal;
		}
	}
	
	public double getDoubleProperty ( String key ) { return getDoubleProperty(key,0.0);} 
	
	public String getProperty ( String key )
	{
		return properties.getProperty(key);
	}
	
	public void setProperty ( String key , String value )
	{
		properties.setProperty(key,value);
	}
	
	public void storeProperties() throws FileNotFoundException, IOException
	{
		updateRecentFilesPropertiesFromList();
		File f = new File(FILENAME);
		FileOutputStream fos = new FileOutputStream(f);
		System.out.println(f.getAbsolutePath());
		properties.store( fos , "Written by AGE" );
	}
	
	private LinkedList recentFiles = new LinkedList();
	
	private void updateRecentFilesListFromProperties()
	{
		synchronized(recentFiles)
		{
			recentFiles.clear();
			int nRecentFiles = Integer.parseInt(properties.getProperty("nRecentFiles"));
			for ( int i = 0 ; i < nRecentFiles ; i++ )
			{
				String path = (String) properties.getProperty("recentFile."+i);
				if ( path != null )
					recentFiles.addLast(path);
			}
		}
	}
	
	private void updateRecentFilesPropertiesFromList()
	{
		int i = 0;
		for (Iterator iter = recentFiles.iterator(); iter.hasNext(); ) 
		{
			String element = (String) iter.next();
			properties.setProperty("recentFile."+i,element);
			i++;
		}
	}
	
	public void addRecentFile ( File f )
	{
		int nRecentFiles = Integer.parseInt(properties.getProperty("nRecentFiles"));
		synchronized(recentFiles)
		{
			String path = f.getAbsolutePath();
			if ( !recentFiles.remove(path) && recentFiles.size() == nRecentFiles )
				recentFiles.removeLast();
			recentFiles.addFirst(path);
		}
	}
	
	public List getRecentFiles ( )
	{
		return recentFiles;
	}
	
	
}