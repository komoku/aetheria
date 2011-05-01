/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.filemanagement;

import java.io.File;

/*
 Created 17/11/2007 13:24:40
 */

public abstract class Paths 
{

	public static final String WORLD_PATH = "worlds";
	public static String SAVE_PATH = "saves";
	
	public static final String LANG_FILES_PATH = "lang";
	
	private static String workingDirectory = System.getProperty("user.dir");

	public static String getWorkingDirectory() 
	{
		return workingDirectory;
	}

	public static void setWorkingDirectory(String workingDirectory) 
	{
		Paths.workingDirectory = workingDirectory;
	}
	
	public static void setSaveDir ( String saveDirPath )
	{
    	File f = new File ( saveDirPath );
    	if ( !f.exists() )
    	{
    		if ( !f.mkdir() )
    		{
    			System.err.println("Could not create save directory: " + saveDirPath + ". Will ignore this setting.");
    		}
    	}
		SAVE_PATH = saveDirPath;
	}
	
	public static String getSaveDir ( )
	{
		return SAVE_PATH;
	}
	
}
