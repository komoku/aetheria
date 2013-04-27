package eu.irreality.age.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;

public class FileUtils 
{
	
	//nothing to see here, move along

	/*
	
	//tries to open the file named by given path.
	//If impossible, tries similar files.
	public static PrintStream openLogFile ( String path )
	{
		
		File f = new File(path);
		if ( !f.exists() )
		{
			if ( !f.getParentFile().exists() )
			{
				if ( !f.getParentFile().mkdirs() )
				{
					throw new FileNotFoundException("Could not open log file " + path + ": unable to create directories.");
				}
			}
			//{f.getParentFile().exists()
			

			FileOutputStream fos;
			fos = new FileOutputStream(f,true);
			
			try 
			{
				System.setErr ( new PrintStream ( new FileOutputStream(f,true) );
			} 
			catch (FileNotFoundException e) 
			{
				System.err.println("Could not redirect standard error to " + file + ":");
				e.printStackTrace();
			}
			
		}
		
	}
	
	private static FileOutputStream streamForFile ( File f )
	{
		FileOutputStream fos = null;
		int tries = 10;
		while ( fos == null )
		
		
	}
	
	*/
	
	public static void copyFile(File sourceFile, File destFile) throws IOException 
	{
		
	    if (!destFile.exists() )
	    {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;
	    try 
	    {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();

	        // previous code: destination.transferFrom(source, 0, source.size());
	        // to avoid infinite loops, should be:
	        long count = 0;
	        long size = source.size();              
	        while((count += destination.transferFrom(source, count, size-count))<size);
	    }
	    finally 
	    {
	        if(source != null) 
	        {
	            source.close();
	        }
	        if(destination != null) 
	        {
	            destination.close();
	        }
	    }
	}
	
	
	
	
}
