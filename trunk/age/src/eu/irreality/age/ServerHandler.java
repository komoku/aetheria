/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.windowing.AGELoggingWindow;

import java.awt.*;
import java.util.*;

class ServerLogWindow extends JInternalFrame implements AGELoggingWindow
{

	java.util.List panesPartidas = new ArrayList();

	JTabbedPane tabbed;
	
	JTextPane tpGeneral;

	public ServerLogWindow()
	{
	
		super("Logs del servidor",true,true,true,true);
	
		tabbed = new JTabbedPane();
		JPanel tabGeneral = new JPanel();
		tabGeneral.setLayout ( new java.awt.GridLayout(1,1) );
		tpGeneral = new JTextPane();
		tpGeneral.setBackground(java.awt.Color.black);
		tpGeneral.setForeground(java.awt.Color.white);
		tabGeneral.add(new JScrollPane(tpGeneral));
		tpGeneral.setText("Log Global:");
	
		tabbed.addTab ( "General" , tabGeneral );
	
		getContentPane().add(tabbed);
		
		
		setSize(400,400);
		setVisible(true);
	
	}
	
	//(los logs de partida escriben mediante InputOutputClients)
	public void writeGeneral ( String s ) //escribe sólo en el general
	{
		//prefijar líneas con "general" y colorearlas de amarillo
		MutableAttributeSet atributos = new SimpleAttributeSet();
		try
		{
			StyleConstants.setForeground( atributos , new Color ( Integer.parseInt("FFFF00",16 ) ) );
		}
		catch ( NumberFormatException nfe )
		{
						//unrecognized
					
		}

		
		/*
		String newText = tpGeneral.getText();
		if ( newText.length() > 0 && ( newText.charAt ( newText.length() - 1 ) != '\n' ) )
			newText += "\n";
		*/
		String toAppend = "[General] " + s.trim();
		//newText = newText + toAppend;		
		//tpGeneral.setText( newText ); 		
	
		try
		{
			String curText = tpGeneral.getText();
			if ( curText.length() > 0 && ( curText.charAt( curText.length()-1 ) != '\n' ) )
				tpGeneral.getDocument().insertString(tpGeneral.getText().length(),"\n",null);
			tpGeneral.getDocument().insertString(tpGeneral.getText().length(),toAppend,atributos);
			Debug.println("BY ORBITAL\n");
		}
		catch ( BadLocationException ble )
		{
			System.err.println(ble);
		}
		
	}
	
	public InputOutputClient addTab (  ) //devuelve una E/S para esa partida
	{
		final JTextPane panePartida = new JTextPane();
		panePartida.setBackground(java.awt.Color.black);
		panePartida.setForeground(java.awt.Color.white);
		panesPartidas.add ( panePartida );
		JPanel tabPartida = new JPanel();
		tabPartida.setLayout ( new java.awt.GridLayout(1,1) );
		tabPartida.add ( new JScrollPane(panePartida) );
		tabbed.add ( "Partida " + panesPartidas.size() , tabPartida );
		
		return ( new NullInputOutputClient()
		{
			int id = panesPartidas.size();
			/**
			 * @deprecated Use {@link #write(String)} instead
			 */
			public void escribir ( String s )
			{
				write(s);
			}
			public void write ( String s )
			{
				panePartida.setText( panePartida.getText() + s );
				
				//text pane general: prefijar líneas con la partida
			
				/*
				String newText = tpGeneral.getText();
				if ( newText.charAt ( newText.length() - 1 ) != '\n' )
					newText += "\n";
				
				String toAppend = "[Partida " + id + "] " + s.trim();
				
				newText = newText + toAppend;
				
				tpGeneral.setText( newText ); 
				*/
				
				String toAppend = "[Partida " + id + "] " + s.trim();
				try
				{
					String curText = tpGeneral.getText();
					if ( curText.length() > 0 && ( curText.charAt( curText.length()-1 ) != '\n' ) )
						tpGeneral.getDocument().insertString(tpGeneral.getText().length(),"\n",null);
					tpGeneral.getDocument().insertString(tpGeneral.getText().length(),toAppend,null);
				}
				catch ( BadLocationException ble )
				{
					System.err.println(ble);
				}
			}
		} );
		
	}

	public JMenuBar getTheJMenuBar()
	{
	    return this.getJMenuBar();
	}

	public void setTheJMenuBar(JMenuBar jmb)
	{
	    this.setJMenuBar(jmb);
	}


}

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
		return logWin;
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
		
		if ( opcionesServidor.sirveTelnet() )
		{
			if ( elServidorTelnet == null )
				elServidorTelnet = new SimpleTelnetClientHandler( (short)opcionesServidor.getPuertoTelnet() );
		}
		
		if ( opcionesServidor.sirveAge() )
		{
			if ( elServidorAge == null )
				elServidorAge = new AGEClientHandler ( (short)opcionesServidor.getPuertoAge() );
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
								IrcAgeBot iab = new IrcAgeBot ( ise.getServer() , ise.getPort() , ise.getNick() );
						 
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
				if ( cur.getText().equalsIgnoreCase("Servidor") )
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
		
		Thread thr = new Thread()
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
							worldIO.write("¡No se ha podido cargar el estado!\n");
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
							worldIO.write("Excepción al leer el fichero de log (ServerHandler).\n");
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
					
					maquinaEstados.start();	
		
		
		
		return new PartidaEnCurso ( theWorld , pe.getMaxPlayers() , pe.getName() , pe.getPassword()  );
		
	}



}

//se crea a partir de una PartidaEntry
class PartidaEnCurso
{
	
	private World mundo;
	private int maxPlayers;
	private String nombrePartida;
	private String passwordPartida;
	
	public PartidaEnCurso ( World mundo , int maxPlayers , String nombrePartida , String passwordPartida )
	{
		this.mundo = mundo;
		this.maxPlayers = maxPlayers;
		this.nombrePartida = nombrePartida;
		this.passwordPartida = passwordPartida;
	}
	
	public int getMaxPlayers()
	{
		return maxPlayers;
	}
	public int getPlayers()
	{
			return mundo.getNumberOfConnectedPlayers();
	}
	public World getMundo()
	{
		return mundo;
	}
	public String getNombre()
	{
		return nombrePartida;
	}
	public String getPass()
	{
		return passwordPartida;
	}
	
}
