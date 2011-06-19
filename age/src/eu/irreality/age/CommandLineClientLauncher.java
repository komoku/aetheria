package eu.irreality.age;

import java.io.File;
import java.util.Vector;

import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.filemanagement.URLUtils;
import eu.irreality.age.filemanagement.WorldLoader;

public class CommandLineClientLauncher 
{
	
	private static String worldPath = null;
	private static String logPath = null;
	private static String statePath = null;
	private static String saveDirPath = null;
	private static boolean rebotFriendly = false;
	private static boolean unstrict = false;
	
	public static void showSyntax (  )
	{
		System.out.println("Syntax: ");
		System.out.println("CheapAGE" + " < -w worldfile > [ -l logfile | -s statefile ] [ -r ] [ -u ]");
		System.out.println("-w worldfile: path to the world file that is to be loaded by CheapAGE.");
		System.out.println("-l logfile: path to a save file in log form, if one is to be used.");
		System.out.println("-s statefile: path to a save file in state form, if one is to be used.");
		System.out.println("-r: rebot-friendly options, disables the prompt and changes some default texts to better accomodate to" +
				" multiprotocol networking via rebot.");
		System.out.println("-u: unstrict metacommands, makes it possible to save/quit without using commands starting with a slash.");
		System.out.println("-sd savedir: path to a directory where savegames will be stored by default.");
	}

	public static void main ( String[] args )
	{
		
		if ( args.length <= 0 )
		{
			showSyntax();
			System.exit(0);
		}
		
		for ( int i = 0 ; i < args.length ; i++ )
		{
			
			if ( args[i].startsWith("-") )
			{
				if ( args[i].equalsIgnoreCase("-world") || args[i].equalsIgnoreCase("-w") )
				{
					if ( i+1 < args.length )
					{
						worldPath = args[i+1];
						i++;
					}
					else
					{
						System.out.println("No world specified after option -world");
					}
				}
				else if ( args[i].equalsIgnoreCase("-log") || args[i].equalsIgnoreCase("-l") )
				{
					if ( i+1 < args.length )
					{
						logPath = args[i+1];
						i++;
					}
					else
					{
						System.out.println("No save file specified after option -log");
					}
				}
				else if ( args[i].equalsIgnoreCase("-state") || args[i].equalsIgnoreCase("-s") )
				{
					if ( i+1 < args.length )
					{
						statePath = args[i+1];
						i++;
					}
					else
					{
						System.out.println("No world specified after option -state");
					}
				}
				else if ( args[i].equalsIgnoreCase("-savedir") || args[i].equalsIgnoreCase("-sd") )
				{
					if ( i+1 < args.length )
					{
						saveDirPath = args[i+1];
						i++;
					}
					else
					{
						System.out.println("No path to saves directory specified after option -savedir");
					}
				}
				else if ( args[i].equalsIgnoreCase("-r") )
				{
					rebotFriendly = true;
				}
				else if ( args[i].equalsIgnoreCase("-u") )
				{
					unstrict = true;
				}
				else
				{
					System.out.println("Unknown option " + args[i]);
					showSyntax();
					System.exit(0);
				}
			}
			else
			{
				System.out.println("Wrong syntax, expected some option, found " + args[i]);
				showSyntax();
				System.exit(0);
			}
			
			
		}
		
		if ( worldPath != null )
		{
			
			//TODO this code duplicates functionality in WorldLoader.
			
			Vector gameLog = new Vector(); //init game log
			InputOutputClient io = new CommandLineClient(gameLog,rebotFriendly,unstrict); //init client
			File inputAsFile = new File(worldPath);
			World theWorld = null;
			
			
			if ( inputAsFile.isFile() )
			{
				
				//nos han dado un fichero
				//eventualmente esto debería ser the way to go, y el else de este if ser eliminado por antiguo, pero de momento aún se usa el else (TODO)
				
				System.out.println("Attempting world location: " +  inputAsFile );
				try
				{
					theWorld = new World ( URLUtils.stringToURL(WorldLoader.goIntoFileIfCompressed(worldPath)) , io , false );
					System.out.println("World generated.\n");
					gameLog.addElement( inputAsFile.getAbsolutePath() ); //primera línea del log, fichero de mundo
				}
				catch ( java.io.IOException ioe )
				{
					io.write("No puedo leer el fichero del mundo: " + inputAsFile + "\n"); 
					ioe.printStackTrace();
					return; 
				}
				
			}
			else
			{
				//nos han dado un directorio
			
				//buscar a ver si el mundo es un world.xml
				//new World tanto si es xml como dat
				try
				{
					System.out.println("Attempting world location: " + worldPath + "/world.xml" );
					theWorld = new World ( worldPath + "/world.xml" , io , false );
					System.out.println("World generated.\n");
					gameLog.addElement(worldPath + "/world.xml"); //primera línea del log, fichero de mundo
				}
				catch ( java.io.IOException e )
				{
					e.printStackTrace();
					return;
				}
				
			}
			
			//{theWorld NOT null}
			
			
	        //set save dir if requested
	        if ( saveDirPath != null )
	        {
	        	Paths.setSaveDir(saveDirPath);
	        }
	        
	
			//usar estado si lo hay
			if ( statePath != null )
			{
				try
				{
					theWorld.loadState ( statePath );
				}
				catch ( Exception exc )
				{
					io.write("¡No se ha podido cargar el estado!\n");
					io.write(exc.toString());
					exc.printStackTrace();
				}
			}
	
			
			if ( logPath != null )
			{
				try
				{
					theWorld.prepareLog(logPath);
					theWorld.setRandomNumberSeed( logPath );
				}
				catch ( Exception exc )
				{
					io.write("Excepción al leer el fichero de log: " + exc + "\n");
					exc.printStackTrace();
					return;
				}
			}
			else
			{
				theWorld.setRandomNumberSeed();
			}
			
			gameLog.addElement(String.valueOf(theWorld.getRandomNumberSeed())); //segunda línea, semilla
			
			GameEngineThread maquinaEstados =
				new GameEngineThread ( 
					theWorld,
					null , false );
			
			maquinaEstados.start();		
			
		}
		
	}
	
	
}
