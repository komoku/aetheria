/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

/*
 * Created at regulus on 10-dic-2008 19:15:29
 * as file Skin.java on package org.f2o.absurdum.puck.gui.skin
 */
package org.f2o.absurdum.puck.gui.skin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.f2o.absurdum.puck.gui.graph.CharacterNode;

/**
 * @author carlos
 *
 * Created at regulus, 10-dic-2008 19:15:29
 */
public class Skin 
{

	/**Path where images are located, including final bar.*/
	private String basePath;
	
	/**Skin properties*/
	private Properties properties;

	public Skin ( String skinName )
	{
		
		System.out.println("Constructor of " + skinName + " skin");

		basePath = "skins" + "/" + skinName + "/";
 		
		String pathToProperties = basePath+"skin.properties";
		
		System.out.println("Properties at " + pathToProperties);
		
		properties = new Properties();
		
		try
		{
			properties.load( getClass().getClassLoader().getResourceAsStream(pathToProperties) );
			System.out.println("Properties loaded");
		}
		catch ( FileNotFoundException fnfe )
		{
			System.out.println("Skin configuration file not found.");
		}
		catch ( NullPointerException npe )
		{
			System.out.println("Skin configuration file not found.");
		}
		catch ( IOException ioe )
		{
			System.out.println("Error reading skin configuration file.");
		}
		
	}
	
	public String getImagePath ( String imageCode )
	{
		return basePath + properties.getProperty(imageCode);
	}
	
}
