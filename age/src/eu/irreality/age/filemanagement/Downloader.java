package eu.irreality.age.filemanagement;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class Downloader 
{

	/**
	 * Downloads file from an URL and saves it to a path in the local machine
	 * @throws FileNotFoundException, IOException 
	 */
	public static void urlToFile ( URL url , File path ) throws FileNotFoundException, IOException
	{
		
		java.io.BufferedInputStream in = new java.io.BufferedInputStream(url.openStream());
		java.io.FileOutputStream fos = new java.io.FileOutputStream(path);
		java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
		byte[] data = new byte[1024];
		int x=0;
		while((x=in.read(data,0,1024))>=0)
		{
			bout.write(data,0,x);
		}
		bout.close();
		in.close();
	}
	
}
