package eu.irreality.age.filemanagement;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import eu.irreality.age.InputOutputClient;
import eu.irreality.age.World;

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
				io.write("No puedo leer el fichero del mundo: " + inputAsFile + "\n"); 
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

				System.out.println(e);
				io.write("No puedo encontrar el mundo en el directorio " + moduledir + "\n");

				//buscar a ver si el mundo es un world.dat? no en este cliente.
			}

		}

		if ( theWorld == null )
		{
			io.write("No encontrado el fichero del mundo. Tal vez el directorio seleccionado [" + moduledir + "] no sea un directorio de mundo AGE válido.\n"); 
			return null; 
		}
		
		return theWorld;
		
	}
	
	public static World loadWorld ( String moduledir , Vector gameLog , InputOutputClient io , Object mundoSemaphore )
	{
		//tres posibilidades:
		//*nos han dado un nombre de fichero: mundo loquesea.xml
		//*nos han dado un directorio y el mundo es directorio/world.xml
		//*nos han dado un directorio y el mundo es directorio/world.dat <- NO LONGER SUPPORTED
		//*nos han dado una URL a un world.xml
		//*nos han dado una URL de un jar
		
		World theWorld = null;
		
		//probamos si la cadena es una URL
		try 
		{	
			URL url = null;
			try
			{
				url = new URL(moduledir);	
			}
			catch ( MalformedURLException mue )
			{
				//perhaps a resource in one of our jars?
				System.err.println("Checking if it's a resource!");
				url = WorldLoader.class.getClassLoader().getResource(moduledir);
				System.err.println("URL: " + url);
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
				if ( url.toString().endsWith("/") )
					url = new URL ( url.toString() + "world.xml" );

			
			theWorld = new World ( url , io , false );
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
		catch (MalformedURLException e1) 
		{
			//if it's not an URL, it should be a path
			return loadWorldFromPath ( moduledir , gameLog , io , mundoSemaphore );
		}
		catch ( IOException ioe )
		{
			io.write("No puedo leer el fichero del mundo: " + moduledir + "\n"); 
			ioe.printStackTrace();
			return null; 
		}

		

	}


}
