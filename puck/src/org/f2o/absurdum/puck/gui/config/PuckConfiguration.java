/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.gui.config;

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
 Created 19/01/2008 16:47:25
 */

public class PuckConfiguration 
{

	private Properties properties = null;
	
	private static String FILENAME = "puck.properties";
	
	private static Properties defaultProperties;
	
	static
	{
		defaultProperties = new Properties();
		defaultProperties.setProperty("nRecentFiles", "5");
		defaultProperties.setProperty("skin", "jenverde");
		defaultProperties.setProperty("look", "default");
		
		defaultProperties.setProperty("roomDisplaySize", "20");
		defaultProperties.setProperty("itemDisplaySize", "15");
		defaultProperties.setProperty("spellDisplaySize", "20");
		defaultProperties.setProperty("abstractEntityDisplaySize", "20");
		defaultProperties.setProperty("characterDisplaySize", "20");
		
		defaultProperties.setProperty("showGrid","true");
		defaultProperties.setProperty("snapToGrid","true");
		
		defaultProperties.setProperty("showRoomNames","true");
		defaultProperties.setProperty("showRoomNodes","true");
		defaultProperties.setProperty("showItemNames","true");
		defaultProperties.setProperty("showItemNodes","true");
		defaultProperties.setProperty("showSpellNames","true");
		defaultProperties.setProperty("showSpellNodes","true");
		defaultProperties.setProperty("showCharacterNames","true");
		defaultProperties.setProperty("showCharacterNodes","true");
		defaultProperties.setProperty("showAbstractEntityNames","true");
		defaultProperties.setProperty("showAbstractEntityNodes","true");
		
		defaultProperties.setProperty("showArrows","true");
		defaultProperties.setProperty("showArrowNames","true");
		
		defaultProperties.setProperty("codeFrameFontSize", "18.0");
		defaultProperties.setProperty("graphArrowFontSize", "10.0");
		defaultProperties.setProperty("graphNodeFontSize", "11.0");
		
		defaultProperties.setProperty("runInSDI", "false");
		
		defaultProperties.setProperty("windowWidth", "800");
		defaultProperties.setProperty("windowHeight", "600");
		defaultProperties.setProperty("windowMaximized", "true");
		defaultProperties.setProperty("windowLocationX","0");
		defaultProperties.setProperty("windowLocationY","0");
		
		defaultProperties.setProperty("dynamicFormResizing","true");
		
		defaultProperties.setProperty("dividerLocation","0.60");
		
		defaultProperties.setProperty("availableSkins","naive,urba1,orco,jenverde");
		defaultProperties.setProperty("additionalLooks","Nimbus"); //L&F's available apart from default and system
		
		defaultProperties.setProperty("rsyntaxWordWrap","true");
		defaultProperties.setProperty("rsyntaxShowLineNumbers","true");
		defaultProperties.setProperty("rsyntaxShowTabLines","false");
		defaultProperties.setProperty("rsyntaxTabsEmulated","true");
		
		defaultProperties.setProperty("mapColor.background", "FFFFFF");
		defaultProperties.setProperty("mapColor.grid", "EEEEEE");
		defaultProperties.setProperty("mapColor.text", "000000");
		
	}
	
	private static PuckConfiguration instance;
	
	public static PuckConfiguration getInstance() 
	{
		if ( instance == null )
				instance = new PuckConfiguration();
		return instance;
	}
		
	private PuckConfiguration()
	{
		System.out.println("PuckConfiguration constructor");
		properties = new Properties(defaultProperties);
		try
		{
			//properties.load ( new InputStreamReader(new FileInputStream(new File(FILENAME))) );
			//properties.load( getClass().getClassLoader().getResourceAsStream(FILENAME) );
			properties.load ( new FileInputStream(new File(FILENAME)) );
		}
		catch ( FileNotFoundException fnfe )
		{
			System.out.println("PUCK configuration file not found.");
		}
		catch ( NullPointerException npe )
		{
			System.out.println("PUCK configuration file not found.");
		}
		catch ( IOException ioe )
		{
			System.out.println("Error reading PUCK configuration file.");
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
		properties.store( fos , "Written by PUCK" );
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
