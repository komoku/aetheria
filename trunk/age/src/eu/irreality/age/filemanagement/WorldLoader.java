package eu.irreality.age.filemanagement;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import eu.irreality.age.InputOutputClient;
import eu.irreality.age.World;
import eu.irreality.age.i18n.UIMessages;

public class WorldLoader 
{
	
	/**
	 * As far as I know, unused from 2013-03-16.
	 * Lol, and I changed it on 2014-02-20, despite it definitely being unused. That's what I get for not deleting old code.
	 * @param moduledir
	 * @param gameLog
	 * @param io
	 * @param mundoSemaphore
	 * @return
	 */
	public static World loadWorldFromPath ( String moduledir , Vector gameLog , InputOutputClient io , Object mundoSemaphore )
	{
		//posibilidades:
		//*nos han dado un nombre de fichero: mundo loquesea.xml
		//*nos han dado un directorio y el mundo es directorio/world.xml
		//*nos han dado un directorio y el mundo es directorio/world.dat <- NO LONGER SUPPORTED
		//*nos han dado un directorio y el mundo es directorio/world.agw
		
		World theWorld = null;
		
		File inputAsFile = new File(moduledir);
		if ( inputAsFile.isFile() )
		{

			//nos han dado un fichero
			//eventualmente esto debería ser the way to go, y el else de este if ser eliminado por antiguo, pero de momento aún se usa el else (TODO)

			System.out.println("Attempting world location: " +  inputAsFile );
			try
			{
				theWorld = new World ( moduledir , io , false );
				System.out.println("World generated.\n");
				if ( mundoSemaphore != null )
				{
					synchronized ( mundoSemaphore )
					{
						mundoSemaphore.notifyAll();
					}
				}
				gameLog.addElement( inputAsFile.getAbsolutePath() ); //primera línea del log, fichero de mundo
			}
			catch ( java.io.IOException ioe )
			{
				//io.write("No puedo leer el fichero del mundo: " + inputAsFile + "\n"); 
				io.write( UIMessages.getInstance().getMessage("load.world.cannot.read.world") + " " + inputAsFile + "\n"); 
				ioe.printStackTrace();
				return null; 
			}

		}
		else
		{

			//nos han dado un directorio

			//buscar a ver si el mundo es un world.agw
			try
			{
				theWorld = new World (  moduledir + "/world.agw" , io , false );
				if ( mundoSemaphore != null )
				{
					synchronized ( mundoSemaphore )
					{
						mundoSemaphore.notifyAll();
					}
				}
				gameLog.addElement( moduledir + "/world.agw"); //primera línea del log, fichero de mundo
			}
			catch ( java.io.IOException e )
			{
				//no era un world.agw. Probar a ver si es entonces un world.xml
				try
				{
					theWorld = new World (  moduledir + "/world.xml" , io , false );
					if ( mundoSemaphore != null )
					{
						synchronized ( mundoSemaphore )
						{
							mundoSemaphore.notifyAll();
						}
					}
					gameLog.addElement( moduledir + "/world.xml"); //primera línea del log, fichero de mundo
				}
				catch ( java.io.IOException e2 )
				{
					io.write( UIMessages.getInstance().getMessage("load.world.cannot.read.world.ondir") + " " + moduledir + "\n");
					System.out.println(e2);
				} //inner catch
			} //outer catch

		}

		if ( theWorld == null )
		{
			io.write( UIMessages.getInstance().getMessage("load.world.invalid.dir","$dir",moduledir) ); 
			//io.write("No encontrado el fichero del mundo. Tal vez el directorio seleccionado [" + moduledir + "] no sea un directorio de mundo AGE válido.\n"); 
			return null; 
		}
		
		return theWorld;
		
	}
	
	
	/**
	 * If the given pathname or URL points to a compressed file that can contain a world (jar, zip, agz); then this method returns
	 * the pathname that points inside the file to recover world.xml.
	 * If the given pathname does not point to a compressed file, then this method returns the same string that was passed as a parameter.
	 * Warning: this version of the method only supports world.xml, not world.agz, right now.
	 * @param pathnameOrUrl
	 * @return
	 * @throws IOException 
	 */
	public static String goIntoFileIfCompressed ( String pathnameOrUrl )
	{
		return goIntoFileIfCompressed ( pathnameOrUrl , "world.xml" );
	}
	
	public static String goIntoFileIfCompressed ( String pathnameOrUrl , String worldFileName )
	{
		if ( !pathnameOrUrl.endsWith(".jar") && !pathnameOrUrl.endsWith(".zip") && !pathnameOrUrl.endsWith(".agz") )
			return pathnameOrUrl; //this url does not point to a compressed file
		if ( pathnameOrUrl.startsWith("zip:") || pathnameOrUrl.startsWith("jar:") )
			return pathnameOrUrl; //this url is already pointing to inside the file, no need to do anything
		else
		{
			URL url = URLUtils.stringToURL(pathnameOrUrl);
			try 
			{
				url = new URL( "jar", "" , url+"!/" + worldFileName);
			} 
			catch (MalformedURLException e) 
			{		
				e.printStackTrace();
			}
			return url.toString();
		}
	}
	
	/**
	 * This method converts a path to a world given as path to directory, path to zipped file, path to world.xml, remote URL to one of those, etc.
	 * into an URL to the actual world.xml file (be it local or remote) so that we can load the world.
	 * @param pathnameOrUrl
	 * @return
	 * @throws MalformedURLException
	 */
	public static URL getURLForWorldLoad ( String pathnameOrUrl ) throws MalformedURLException
	{
		//Updated possibilities:
		//1. pathname to worldname.xml file
		//2. pathname to directoryname (and world is directoryname/world.xml)
		//3. url to worldname.xml file [3a. to world not inside anything, 3b. to world inside a jar file]
		//4. url to directoryname [4a. not inside anything, 4b. inside a jar file]
		//5. resource pathname (could be to a world inside applet jar, for example)
		//6. pathname to agz, zip, jar compressed file (which should have world.xml inside)
		//7. url to agz, zip, jar compressed file (which should have world.xml inside)
		
		//with this line cases 6 and 7 are collapsed into case 3:
		try
		{
			pathnameOrUrl = goIntoFileIfCompressed(pathnameOrUrl);
		}
		catch ( SecurityException se )
		{
			; //apparently not permitted in applet, but no problem, we don't support compressed files in applet.
			//now it does work in applet (tested that we can load an AGZ from applet, even though not recommended in large AGZ's due to slowness). This exception should not be thrown in general.
		}
		
		URL url = null;
		//probamos si la cadena ya es una URL
		try 
		{	
			
			try
			{
				url = new URL(pathnameOrUrl); 	//cases 3, 4?	
			}
			catch ( MalformedURLException mue )
			{
				//perhaps a resource in one of our jars?
				//System.err.println("Checking if it's a resource!");
				url = WorldLoader.class.getClassLoader().getResource(pathnameOrUrl); //case 5
				//System.err.println("URL: " + url);
				if ( url == null ) throw mue;
			}
				//process url
				/*//world constructor already does this.
				if ( url.toString().endsWith(".jar"))
				{
					URLClassLoader ucl = new URLClassLoader ( new URL[] { url } , WorldLoader.class.getClassLoader() );
					url = ucl.getResource("world.xml");
				}
				*/
				if ( url.toString().endsWith("/") ) //case 4
					url = new URL ( url.toString() + "world.xml" );
		}
		catch (MalformedURLException e1) //have to check for cases 1, 2 here
		{
			//if it's not an URL, it should be a path
			
			File f = new File(pathnameOrUrl);
			if ( !f.isFile() )
				f = new File ( f , "world.xml" ); //check for case 2
			try
			{
				url = f.toURI().toURL(); //cases 1 and 2
			}
			catch (MalformedURLException e2)
			{
				//this is hopeless. Probably the given string was neither an URL nor a pathname.
				throw e2;
			}
		}
		
		return url;
	}
	
	/**
	 * Called by loadWorld (URL, Vector, InputOutputClient, Object) when a concrete URL has been obtained from the pathname.
	 * @param url
	 * @param gameLog
	 * @param io
	 * @param mundoSemaphore
	 * @return
	 */
	private static World loadWorldFromURL ( URL url , Vector gameLog , InputOutputClient io , Object mundoSemaphore ) throws IOException
	{
		World theWorld = new World ( url , io , false ); //cases 3, 4, 5 covered here (apart from 6, 7 which were collapsed into 3 before)
		System.out.println("World generated.\n");
		if ( mundoSemaphore != null )
		{
			synchronized ( mundoSemaphore )
			{
				mundoSemaphore.notifyAll();
			}
		}
		//gameLog.addElement( moduledir + "/world.xml"); //primera línea del log, fichero de mundo
		//gameLog.addElement(theWorld.getResource("world.xml").toString()); //URL a fichero de mundo
		gameLog.addElement(url.toString()); //above line didn't work if name of the world wasn't world.xml!
		return theWorld;
	}
	
	/**
	 * Multi-use world loader method that can take pathnames or URLs to directories, plain world files or zipped world files.
	 * All in one.
	 * @param pathnameOrUrl
	 * @param gameLog
	 * @param io
	 * @param mundoSemaphore
	 * @return
	 */
	public static World loadWorld ( String pathnameOrUrl , Vector gameLog , InputOutputClient io , Object mundoSemaphore )
	{
		//tres posibilidades:
		//*nos han dado un nombre de fichero: mundo loquesea.xml
		//*nos han dado un directorio y el mundo es directorio/world.xml
		//*nos han dado un directorio y el mundo es directorio/world.dat <- NO LONGER SUPPORTED
		//*nos han dado una URL a un world.xml
		//*nos han dado una URL de un jar
		
		//Updated possibilities:
		//1. pathname to worldname.xml file
		//2. pathname to directoryname (and world is directoryname/world.xml)
		//3. url to worldname.xml file [3a. to world not inside anything, 3b. to world inside a jar file]
		//4. url to directoryname [4a. not inside anything, 4b. inside a jar file]
		//5. resource pathname (could be to a world inside applet jar, for example)
		//6. pathname to agz, zip, jar compressed file (which should have world.xml inside)
		//7. url to agz, zip, jar compressed file (which should have world.xml inside)
		
		
		World theWorld = null;
		
		URL url = null;
		
		try
		{
			try
			{
				url = getURLForWorldLoad (pathnameOrUrl);
			}
			catch ( MalformedURLException mue ) //path given was not a correct URL/pathname
			{
				io.write( UIMessages.getInstance().getMessage("load.world.cannot.read.world") + " " + pathnameOrUrl + "\n"); 
				mue.printStackTrace();
				return null; 
			}
			return loadWorldFromURL ( url , gameLog , io , mundoSemaphore );
		} 
		catch ( IOException ioe )
		{
			//if we tried with .xml, now try with the new extension .agw. Note that we only do this if we added a world.xml that wasn't originally there in the pathnameOrUrl parameter.
			if ( url.toString().endsWith(".xml") && !pathnameOrUrl.endsWith(".xml") )
			{
				try
				{
					url = new URL( url.toString().substring(0,url.toString().length()-3) + "agw" );
				}
				catch ( MalformedURLException mue ) //this shouldn't really happen if it didn't happen above
				{
					io.write( UIMessages.getInstance().getMessage("load.world.cannot.read.world") + " " + pathnameOrUrl + "\n"); 
					mue.printStackTrace();
					return null; 
				}
				try
				{
					return loadWorldFromURL ( url , gameLog , io , mundoSemaphore );
				}
				catch ( IOException ioe2 )
				{
					; //we will report the first exception, not this one
				}
			}
			io.write( UIMessages.getInstance().getMessage("load.world.cannot.read.world") + " " + pathnameOrUrl + "\n"); 
			ioe.printStackTrace();
			return null; 
		}

		

	}


}
