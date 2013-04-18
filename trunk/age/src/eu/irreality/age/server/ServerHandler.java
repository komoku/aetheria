/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.server;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;

import eu.irreality.age.AGEClientHandler;
import eu.irreality.age.GameEngineThread;
import eu.irreality.age.InputOutputClient;
import eu.irreality.age.server.IrcServerEntry;
import eu.irreality.age.NullInputOutputClient;
import eu.irreality.age.PartidaEntry;
import eu.irreality.age.server.ServerConfigurationOptions;
import eu.irreality.age.swing.applet.SwingSDIApplet;
import eu.irreality.age.swing.menu.ServerMenuHandler;
import eu.irreality.age.telnet.SimpleTelnetClientHandler;
import eu.irreality.age.SwingAetheriaGameLoader;
import eu.irreality.age.World;
import eu.irreality.age.debug.Debug;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.irc.IrcAgeBot;
import eu.irreality.age.windowing.AGELoggingWindow;

import java.awt.*;
import java.util.*;



public class ServerHandler //Singleton!
{


	private SimpleTelnetClientHandler elServidorTelnet;
	//private PartidaEnCurso[] partidasTelnet;
	
	private AGEClientHandler elServidorAge;
	
	//sync-protected
	private java.util.List losBotsIrc = new Vector(); //of IrcAgeBot
	
	//sync-protected
	private java.util.List partidasIrc = new Vector(); //of PartidaEnCurso



	private static ServerHandler theInstance;
	
	private ServerConfigurationOptions opcionesServidor;
	
	private ServerLogWindow logWin;

	private ServerHandler ( JDesktopPane toAddLogWin )
	{
		this ( ServerConfigurationWindow.getInstance().getEntrada() , toAddLogWin );
	}
	
	private ServerHandler ( )
	{
		this ( ServerConfigurationWindow.getInstance().getEntrada() );
	}
	
	private ServerHandler ( ServerConfigurationOptions sco )
	{
		this ( sco , null );
	}
	
	public ServerLogWindow getLogWindow()
	{
		if ( logWin == null ) logWin = new ServerLogWindow();
		return logWin;
	}
	
	/**
	 * This initializes the servers specified in the ServerConfigurationOptions passed as a parameter.
	 * @param sco
	 */
	public void applyOptions ( ServerConfigurationOptions sco )
	{
		if ( opcionesServidor.sirveTelnet() )
		{
			if ( elServidorTelnet == null )
				elServidorTelnet = new SimpleTelnetClientHandler( (short)opcionesServidor.getPuertoTelnet() );
		}
		else
		{
			elServidorTelnet = null;
		}
		
		if ( opcionesServidor.sirveAge() )
		{
			if ( elServidorAge == null )
				elServidorAge = new AGEClientHandler ( (short)opcionesServidor.getPuertoAge() );
		}
		else
		{
			elServidorAge = null;
		}
		
		if ( opcionesServidor.sirveIrc() )
		{
		
			//inicializar bots IRC y añadirlos a la lista
			
			java.util.List ircServerEntryList = opcionesServidor.getListaServidoresIrc();
			
			for ( int i = 0 ; i < ircServerEntryList.size() ; i++ )
			{
				
				final IrcServerEntry ise = (IrcServerEntry)ircServerEntryList.get(i);
				

					Thread th = new Thread()
					{
						
						public void run ()
						{
							
							try
							{
								IrcAgeBot iab = new IrcAgeBot ( ise.getServer() , ise.getPort() , ise.getNick() , ise.getChannels() );
						 
								synchronized (theInstance) 
								{
									losBotsIrc.add ( iab );
								}
							}
							catch ( Exception e )
							{
								if ( logWin!=null )
								{
									logWin.writeGeneral("Exception found when trying to connect bot to server " + ise.getServer() + "\n");
									logWin.writeGeneral( e + ":" + e.getMessage() );
									e.printStackTrace();
									Debug.println("HALCYON\nAND ON\nAND ON\n");	
								}
							}


						}

					};
					th.setPriority(Thread.MIN_PRIORITY);
					th.start();
					//we don't join channels and so on at the moment.
				

			}
		
		}
		else
		{
			losBotsIrc.clear();
			partidasIrc.clear();
		}
	}
	
	private ServerHandler ( ServerConfigurationOptions sco , JDesktopPane toAddLogWin )
	{
		//init everythin'	
		opcionesServidor = sco;
		//logWin = new ServerLogWindow();
		
		if ( logWin == null && toAddLogWin != null )
		{
			logWin = new ServerLogWindow();
			toAddLogWin.add(logWin);
		}
		
		if ( sco.initOnStartup() )
			applyOptions(sco); //this inits the server (not the games, at the moment).
		
	}
	
	public ServerConfigurationOptions getServerConfigurationOptions ( )
	{
		return opcionesServidor;	
	}
	
	public void setServerConfigurationOptions ( ServerConfigurationOptions sco )
	{
		opcionesServidor = sco;
	}

	public static ServerHandler getInstance()
	{
		if ( theInstance == null )
			theInstance = new ServerHandler();
		return theInstance;	
	}
	
	public static ServerHandler getInstance ( JDesktopPane toAddLogWin )
	{
		if ( theInstance == null )
			theInstance = new ServerHandler ( toAddLogWin );
		return theInstance;	
	}
	

	public void addToCorrespondingServers ( PartidaEnCurso pec , PartidaEntry pe )
	{
		if ( opcionesServidor.sirveTelnet() && pe.sirveTelnet() )
				addPartidaToTelnetServer ( pec );
		if ( opcionesServidor.sirveAge() && pe.sirveAge() )
				addPartidaToAgeServer ( pec );	
		if ( opcionesServidor.sirveIrc() && pe.sirveIrc() )
				addPartidaToIrcServers ( pec );
	}

	public void initPartidasDedicadas ( JDesktopPane toAddLogWin )
	{
		if ( logWin == null )
		{
			logWin = new ServerLogWindow();
			toAddLogWin.add(logWin);
		}	
		java.util.List dedicadas = opcionesServidor.getListaPartidasDedicadas();
		for ( int i = 0 ; i < dedicadas.size () ; i++ )
		{
			Debug.println("GONNA ADD PARTIDA");
			PartidaEnCurso pec = initPartidaDedicada ( ((PartidaEntry)dedicadas.get(i)) , logWin , null , null );
			if ( opcionesServidor.sirveTelnet() && ((PartidaEntry)dedicadas.get(i)).sirveTelnet() )
				addPartidaToTelnetServer ( pec );
			if ( opcionesServidor.sirveAge() && ((PartidaEntry)dedicadas.get(i)).sirveAge() )
				addPartidaToAgeServer ( pec );	
			if ( opcionesServidor.sirveIrc() && ((PartidaEntry)dedicadas.get(i)).sirveIrc() )
				addPartidaToIrcServers ( pec );
		}
		
		//properly set menu names
		
		JMenuBar jmb = logWin.getJMenuBar();
		if ( jmb != null )
		{
			int nmenus = jmb.getMenuCount();
			int npartida = 1;
			for ( int i = 0 ; i < nmenus ; i++ )
			{
				JMenu cur = jmb.getMenu(i);
				if ( cur.getText().equalsIgnoreCase("Opciones de juego") )
				{
					cur.setText("Partida " + npartida);
					npartida++;
				}
			}
		}
		
	}

	public void addPartidaToTelnetServer ( PartidaEnCurso pec )
	{
		
		if ( elServidorTelnet != null )
			elServidorTelnet.addPartida(pec);	
	}
	
	public void addPartidaToAgeServer ( PartidaEnCurso pec )
	{
		if ( elServidorAge != null )
			elServidorAge.addPartida(pec);
	}
	
	public synchronized void addPartidaToIrcServers ( PartidaEnCurso pec )
	{
		partidasIrc.add(pec);
	}
	
	public synchronized java.util.List getPartidasIrc()
	{
		return partidasIrc;
	}

	private SwingAetheriaGameLoader tempSagl;
	
	public void initGameLoader( String ficheroMundo ,  JDesktopPane thePanel , String logFile , String stateFile )
	{
		tempSagl = new SwingAetheriaGameLoader( ficheroMundo , thePanel , (logFile!=null) , logFile , stateFile , (stateFile!=null) /*pues, si usamos estado, no seremos cliente porque el jugador se cargará al cargar el estado*/  );
	}
	
	public void initPartidaLocal ( final PartidaEntry pe , ServerLogWindow slw , /*nullable*/ final String stateFile , /*nullable*/ final String logFile , final JDesktopPane thePanel )
	{
	
		final String ficheroMundo = pe.getGameInfo().getFile();
		Debug.println("The world file: " + ficheroMundo);
		World theWorld;
		//InputOutputClient worldIO = slw.addTab();
		
		/*
		try
		{
			theWorld = new World ( ficheroMundo.toString() , worldIO  , true );
			gameLog.addElement ( ficheroMundo );
		}
		catch ( Exception e )
		{
			worldIO.escribir("Excepción al crear el mundo: " + e + "\n");
			e.printStackTrace();
			return null;
		}
		*/
		
		
		final SwingAetheriaGameLoader sagl1;
		
		//changed as of 2008-05-01
		try
		{
		//SwingUtilities.invokeAndWait ( 
			( new Thread() { 
				public void run()
				{
					Debug.println("b4 inigl");
					initGameLoader(ficheroMundo,thePanel,logFile,stateFile);
					Debug.println("af inigl");
					//SwingAetheriaGameLoader sagl = new SwingAetheriaGameLoader( ficheroMundo.getParent() , thePanel , (logFile!=null) , logFile , stateFile , (stateFile!=null) /*pues, si usamos estado, no seremos cliente porque el jugador se cargará al cargar el estado*/  );
				}
			} )
		//	);
			.start();

		}
		catch ( Exception intex )
		{
			intex.printStackTrace();
		}
		
		//SwingAetheriaGameLoader sagl = tempSagl;
		
			
		Debug.println("SAGL loaded. " + SwingUtilities.isEventDispatchThread());
		
		Thread thr = new Thread("ServerHandler Game Creation Thread")
		{
		
			public void run()
			{
			
				World mundo = null;
		
		
			try
			{
				while ( tempSagl == null )
				{
					synchronized(this)
					{
						this.wait(200);
					}
				}
				Debug.println("b4 wait");
				//synchronized(tempSagl)
				{
					mundo = tempSagl.waitForMundoToLoad();	
				}
				Debug.println("af wait");
			}
			catch ( InterruptedException intex )
			{
				intex.printStackTrace();
				//return null;
			}
			 //THIS CAUSED THREADAN BUGS. WE DON'T NEED IT.
				
			
			//return new PartidaEnCurso ( mundo , pe.getMaxPlayers() , pe.getName() , pe.getPassword()  );
			
			PartidaEnCurso pec = new PartidaEnCurso ( mundo , pe.getMaxPlayers() , pe.getName() , pe.getPassword()  );
			ServerHandler.getInstance().addToCorrespondingServers ( pec,pe );
			}
		
		};
		thr.start();
		
	}

	//Inicializa una partida y la devuelve, sin añadirla a ningún servidor. [pero dedicada]
	public PartidaEnCurso initPartidaDedicada ( PartidaEntry pe , ServerLogWindow slw , /*nullable*/ String stateFile , /*nullable*/ String logFile )
	{
		//do this! important!
		
		Debug.println(pe);
		Debug.println(pe.getGameInfo());
		Debug.println(pe.getGameInfo().getFile());
		String ficheroMundo = pe.getGameInfo().getFile();
		
		World theWorld;
		
		InputOutputClient worldIO = slw.addTab();
		
		Vector gameLog = new Vector();
		
		try
		{
			theWorld = new World ( ficheroMundo , worldIO  , true );
			gameLog.addElement ( ficheroMundo );
		}
		catch ( Exception e )
		{
			worldIO.write("Excepción al crear el mundo: " + e + "\n");
			e.printStackTrace();
			return null;
		}
		
		
		//pasted from SwingAetheriaGameLoader
		
					//usar estado si lo hay
					if ( stateFile != null )
					{
						try
						{
							theWorld.loadState ( stateFile );
						}
						catch ( Exception exc )
						{
							//worldIO.write("¡No se ha podido cargar el estado!\n");
							worldIO.write(UIMessages.getInstance().getMessage("swing.cannot.read.state","$file",stateFile));
							worldIO.write(exc.toString());
							exc.printStackTrace();
						}
					}
					
					
					if ( logFile != null )
					{
						try
						{
						
							/**TEMPORAL. CAMBIAR ESTO.**/
							/**El log debe ser multiplayer.**/
							/*Quitar esta línea:*/
							//theWorld.getPlayer().prepareLog ( logFile ); //el jugador ejecutará los comandos del log
							//DONE!!
							
							Debug.println("SHPL");
							Debug.println("Player list is " + theWorld.getPlayerList());
							theWorld.prepareLog(logFile);
							theWorld.setRandomNumberSeed( logFile );
						}
						catch ( Exception exc )
						{
							//worldIO.write("Excepción al leer el fichero de log (ServerHandler).\n");
							worldIO.write(UIMessages.getInstance().getMessage("swing.cannot.read.log","$exc",exc.toString()));
							return null;
						}
					}
					else
					{
						theWorld.setRandomNumberSeed();
					}
					
					gameLog.addElement(String.valueOf(theWorld.getRandomNumberSeed())); //segunda línea, semilla						
					
					//de momento pasamos slw para añadir servermenu.
					GameEngineThread maquinaEstados =
						new GameEngineThread ( 
							theWorld,
							slw , true ); //dedicated games are now real time by default
					
					maquinaEstados.attachObserver(new ServerMenuHandler(slw));
					
					maquinaEstados.start();	
		
		
		
		return new PartidaEnCurso ( theWorld , pe.getMaxPlayers() , pe.getName() , pe.getPassword()  );
		
	}



}

