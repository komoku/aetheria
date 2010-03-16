/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.net.*;

public class PluginMenu extends JMenu
{

	private final JDesktopPane dPane;


	public PluginMenu( final JDesktopPane dPane )
	{
		super("Plugins");
		this.dPane = dPane;
		final String[] names = getPluginClassNames();
		
		for ( int i = 0 ; i < names.length ; i++ )
		{
			final int ind = i;
			JMenuItem thisItem = new JMenuItem( names[i] );
			thisItem.addActionListener( new ActionListener() 
			{
				public void actionPerformed ( ActionEvent evt )
				{
					
					Class cl = loadPluginClass(names[ind]);
					JInternalFrame jif;
					try
					{
						jif = (JInternalFrame) cl.newInstance();
						dPane.add(jif);
						jif.setVisible(true);
					}
					catch ( ClassCastException cce )
					{
						System.out.println("Plugin error: plugin class must extend JInternalFrame.");
						cce.printStackTrace();
					}
					catch ( InstantiationException ie )
					{
						System.out.println("Plugin error: plugin class couldn't be instantiated.");
						ie.printStackTrace();
					}
					catch ( Exception e )
					{
						e.printStackTrace();
					}
				}
			} );
			this.add(thisItem);
		}
	}

	Class loadPluginClass ( String name )
	{

	// Create a File object on the root of the directory containing the class file
	    File file = new File("plugins/");
	    Class cls = null;
		
	    try 
		{
	        
			// Convert File to a URL
	        URL url = file.toURL();    
	        URL[] urls = new URL[]{url};
	    
	        // Create a new class loader with the directory
	        ClassLoader cl = new URLClassLoader(urls);
	    
	        cls = cl.loadClass(name);
	    	
		} 
		catch (MalformedURLException e) 
		{
	    		e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
				e.printStackTrace();
	    }
		
		return cls;

	}

	String[] getPluginClassNames ( )
	{
		
		File plugindir = new File("plugins/");
		
		System.out.println("Plugin directory: " + plugindir);
		
		File[] fList = plugindir.listFiles();
		
		if ( fList == null ) return ( new String[0] );
		
		System.out.println("" + fList.length + " plugins found.");
		
		String[] names = new String[fList.length];
		
		for ( int i = 0 ; i < fList.length ; i++ )
		{
			String fullFileName = fList[i].getName();
			StringTokenizer st = new StringTokenizer ( fullFileName,"." );
			names[i] = st.nextToken();
		}
		
		
		return names;
		
	}




}