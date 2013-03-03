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
	
	public static World loadWorldFromPath ( String moduledir , Vector gameLog , InputOutputClient io , Object mundoSemaphore )
	{
		//posibilidades:
		//*nos han dado un nombre de fichero: mundo loquesea.xml
		//*nos han dado un directorio y el mundo es directorio/world.xml
		//*nos han dado un directorio y el mundo es directorio/world.dat <- NO LONGER SUPPORTED
		
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

			//buscar a ver si el mundo es un world.xml
			try
			{
				System.out.println("Attempting world location: "  + moduledir + "/world.xml" );
				theWorld = new World (  moduledir + "/world.xml" , io , false );
				System.out.println("World generated.\n");
				if ( mundoSemaphore != null )
				{
					synchronized ( mundoSemaphore )
					{
						mundoSemaphore.notifyAll();
					}
				}
				gameLog.addElement( moduledir + "/world.xml"); //primera línea del log, fichero de mundo
			}
			catch ( java.io.IOException e )
			{

				io.write( UIMessages.getInstance().getMessage("load.world.cannot.read.world.ondir") + " " + moduledir + "\n");
				System.out.println(e);
				//io.write("No puedo encontrar el mundo en el directorio " + moduledir + "\n");

				
				//buscar a ver si el mundo es un world.dat? no en este cliente.
			}

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
	 * @param pathnameOrUrl
	 * @return
	 * @throws IOException 
	 */
	public static String goIntoFileIfCompressed ( String pathnameOrUrl )
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
				url = new URL( "jar", "" , url+"!/world.xml");
			} 
			catch (MalformedURLException e) 
			{
				
				e.printStackTrace();
			}
			return url.toString();
		}
	}
	
	
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
		
		//with this line cases 6 and 7 are collapsed into case 3:
		try
		{
			pathnameOrUrl = goIntoFileIfCompressed(pathnameOrUrl);
		}
		catch ( SecurityException se )
		{
			; //apparently not permitted in applet, but no problem, we don't support compressed files in applet.
		}
		
		//probamos si la cadena es una URL
		try 
		{	
			URL url = null;
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

			
			theWorld = new World ( url , io , false ); //cases 3, 4, 5 covered here
			System.out.println("World generated.\n");
			if ( mundoSemaphore != null )
			{
				synchronized ( mundoSemaphore )
				{
					mundoSemaphore.notifyAll();
				}
			}
			//gameLog.addElement( moduledir + "/world.xml"); //primera línea del log, fichero de mundo
			gameLog.addElement(theWorld.getResource("world.xml").toString()); //URL a fichero de mundo
			return theWorld;
		} 
		catch (MalformedURLException e1) //have to check for cases 1, 2 here
		{
			//if it's not an URL, it should be a path
			return loadWorldFromPath ( pathnameOrUrl , gameLog , io , mundoSemaphore );
		}
		catch ( IOException ioe )
		{
			io.write( UIMessages.getInstance().getMessage("load.world.cannot.read.world") + " " + pathnameOrUrl + "\n"); 
			ioe.printStackTrace();
			return null; 
		}

		

	}


}
