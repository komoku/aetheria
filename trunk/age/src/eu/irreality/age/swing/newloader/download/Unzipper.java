package eu.irreality.age.swing.newloader.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzipper 
{


	/**
	 * Unzip a file into a given output folder.
	 * @param zipFile input zip file
	 * @param output zip file output folder
	 */
	public static void unzip(String zipFile, String outputFolder) throws IOException
	{
		
		//System.out.println("Unzipping " + zipFile + " to " + outputFolder + ":\n");
		
		byte[] buffer = new byte[1024];


		//create output directory if it doesn't exist
		File folder = new File(outputFolder);
		if(!folder.exists())
		{
			folder.mkdirs();
		}

		//get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			
		//get the zipped file list entry
		ZipEntry ze = zis.getNextEntry();

		while(ze!=null)
		{

			String fileName = ze.getName();
			File newFile = new File(outputFolder + File.separator + fileName);

			if ( ze.isDirectory() )
			{
				//create the directory.
				newFile.mkdirs();
			}
			
			else
			{
			
				//System.out.println("file unzip : "+ newFile.getAbsoluteFile());
	
				//create all non existent folders
				//(corresponding to compressed folders inside the zipfile)
				new File(newFile.getParent()).mkdirs();
				
				//System.out.println("*Made dirs to " + newFile.getParent() + "\n");
				//System.out.println("*Will create " + newFile + "\n");
				
				FileOutputStream fos = new FileOutputStream(newFile);             
	
				int len;
				while ((len = zis.read(buffer)) > 0) 
				{
					fos.write(buffer, 0, len);
				}
				fos.close();  
			
			}
	
 
			ze = zis.getNextEntry();
			
		}

		zis.closeEntry();
		zis.close();

		//System.out.println("Done");

	} 


}
