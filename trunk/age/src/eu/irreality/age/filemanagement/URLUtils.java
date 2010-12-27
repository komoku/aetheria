package eu.irreality.age.filemanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils 
{

	public static InputStream openFileOrURL ( String fileNameOrUrl ) throws IOException
	{
		try
		{
			URL url = new URL(fileNameOrUrl);
			return url.openStream();
		}
		catch ( MalformedURLException mfe )
		{
			InputStream str = new FileInputStream ( fileNameOrUrl );
			return str;
		} 	
	}
	
	public static URL fileToURL ( String filename )
	{
		try 
		{
			return new File(filename).toURI().toURL();
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static URL stringToURL ( String s )
	{
		try
		{
			return new URL(s);
		}
		catch ( MalformedURLException e )
		{
			return fileToURL(s);
		}
	}
	
}
