/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.filemanagement.WorldLoader;
import eu.irreality.age.swing.config.AGEConfiguration;
import eu.irreality.age.util.VersionComparator;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.UpdatingRun;

import java.io.*; //savegame
//import javax.sound.*;



//ventana que carga el juego del directorio dado y llama al Game Engine, la m�quina de estados.

public class SwingAetheriaGameLoader extends JInternalFrame implements Informador, AGEClientWindow
{
	/**
	* El contador de tiempo.
	*/
	protected static long timeCount;
	/**
	* Si salimos del juego.
	*/
	protected static boolean exitFlag;
	/**
	* Realizar� E/S general y se pasar� a todos los informadores.
	*/
	protected InputOutputClient io;
	/**
	* Log para guardar partida.
	*/
	protected Vector gameLog;
	/**
	* Mundo.
	*/
	protected World mundo;
	
	
	/*Panel de cliente.*/
	private JPanel mainPanel;
	
	/*Frame auxiliar para full-screen mode.*/
	private JFrame fullScreenFrame;
	
	/*Nos dice si el modo full-screen est� activado o no.*/
	private boolean fullScreenMode;
	
	/*Barra de men�. Nos interesa tenerla aqu� aunque est� removed de la ventana. Por el fullscreen.*/
	private JMenuBar barraMenu;
	
	//
	private GameEngineThread maquinaEstados;
	
	public World getMundo()
	{
		return mundo;
	}


	public static String getVersion ( )
	{
		return "Swing-based MDI interface with colored text output, version 1.0";
	}



	protected SwingAetheriaGameLoader esto = this;




 	//final Runnable doUpdate = new Runnable() 
	//{
    //	public void run() 
	//	{
    //    	repaint();
    // 	}
 	//};
	
	protected Runnable updateCode = new UpdatingRun(this);
	
	public void updateNow()
	{	
		Thread c = new Thread ( updateCode );
		c.setPriority ( Thread.MAX_PRIORITY );
		c.start();
	}
	
/* Viejo constructor: todo funciona(ba cuando lo coment�); pero no se muestra el texto
de la ventana hasta acabar de cargar.
	public SwingAetheriaGameLoader ( String moduledir , JDesktopPane gui , boolean usarLog , String logFile )
	{
		//Create Window
		
		super(moduledir,true,true,true,true);
		
		//buff...
		
		//Runtime.getRuntime().setPriority(1);
		
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	
		System.out.println("Hmm.");
		
		setSize(600,600);
		if ( moduledir.equalsIgnoreCase("") )
		{
			setTitle("Aetheria Game Engine. M�dulo: aetherworld");
		}
		else
		{
			setTitle("Aetheria Game Engine. M�dulo: " + moduledir);
		}
		getContentPane().setLayout( new BorderLayout() );
		gui.add(this);
		setVisible(true);
		setVisible(true);
		update(getGraphics());
		repaint();
		updateNow();
		
		//UpdatingThread doUpdate2 = new UpdatingThread( this , doUpdate );
		//doUpdate2.start();
		
		Thread.yield();
		//Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	
		JTextArea areaTexto = new JTextArea( "Aetheria Game Engine v 1.0\n",0,0  );
		areaTexto.append( "Running on Aetheria Multiple Game Interface v 1.0 / swing-based MDI interface");
		//Thread.yield();
		updateNow();
		//doUpdate2 = new UpdatingThread( this , doUpdate );
		//doUpdate2.start();
		JScrollPane scrollAreaTexto = new JScrollPane ( areaTexto );
		//scrollAreaTexto.createVerticalScrollBar();
		scrollAreaTexto.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		areaTexto.setForeground(java.awt.Color.white);
		areaTexto.setBackground(java.awt.Color.black);
		
		//set areaTexto.font
		
		Font[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for ( int f = 0 ; f < fuentes.length ; f++ )
		{
			if ( fuentes[f].getFontName().equalsIgnoreCase("Courier New") )
			{
				areaTexto.setFont(fuentes[f].deriveFont((float)12.0));
				break;
			}
			//System.out.println("Fuente: " + fuentes[f]);
		}
		
		
		areaTexto.setVisible(true);
		scrollAreaTexto.setVisible(true);
		//scrollAreaTexto.getVerticalScrollBar().setVisible(true);
		//areaTexto.setEditable(false);
		//areaTexto.setBackground(Color.black);
		//areaTexto.setForeground(Color.white);
		areaTexto.setLineWrap(true);
		areaTexto.setWrapStyleWord(true);
		getContentPane().add(scrollAreaTexto,"Center");
		
		JTextField campoTexto = new JTextField(200);
		//campoTexto.setBounds(5,560,590,20);
		campoTexto.setVisible(true);
		getContentPane().add(campoTexto,"South");
		
		setJMenuBar( new SwingMenuAetheria(this) );
		
		//addWindowListener( new Cerrar() );
		
		setVisible(true);
		
		repaint();
		updateNow();
		//doUpdate2 = new UpdatingThread( this , doUpdate );
		//doUpdate2.start();
		Thread.yield();
		
		gameLog = new Vector(); //init game log
		
		io = new SwingEntradaSalida(campoTexto,scrollAreaTexto,areaTexto,gameLog);
		
		String worldName;
		World theWorld;
		
		if ( moduledir == null || moduledir.length() == 0 ) moduledir="aetherworld";
		
		repaint();
		updateNow();
		//doUpdate2 = new UpdatingThread( this , doUpdate );
		//doUpdate2.start();
		Thread.currentThread().yield();

		try 
		{ 
			System.out.println("World location: " +  maindir + moduledir + "/world.dat" );
			theWorld = new World ( maindir + moduledir + "/world.dat" , io );
		 }
		catch ( java.io.FileNotFoundException loadworldfileioerror )
		{ 
			escribir("No encontrado el fichero del mundo. Tal vez el directorio seleccionado no sea un directorio de mundo AGE v�lido.\n"); 
		  	return; 
		}
		catch ( java.io.IOException loadworldfileioerror2 )
		{
			escribir("No puedo leer el fichero del mundo. Tal vez el directorio seleccionado no sea un directorio de mundo AGE v�lido.\n"); 
		  	return; 
		}
		
		//Thread.yield();
		updateNow();
		//doUpdate2 = new UpdatingThread( this , doUpdate );
		//doUpdate2.start();
		
		gameLog.addElement(maindir + moduledir + "/world.dat"); //primera l�nea del log, fichero de mundo
		
		//Thread.currentThread().yield();
		
		//System.out.println("Construido el mundo. Vamos a por el jugata.\n");
	
		setTitle(theWorld.getModuleName());
		
		Player theProtagonist;
		
		try
		{
			theProtagonist = new Player ( theWorld , io );
		}
		catch ( java.io.IOException loadplayerfileioerror )
		{
			escribir("No puedo leer el fichero del jugador.\n");
			return;
		}
		
		if ( usarLog )
		{
			try
			{
				
				theProtagonist.prepareLog ( logFile ); //el jugador ejecutar� los comandos del log
			}
			catch ( Exception exc )
			{
				escribir("Excepci�n al leer el fichero de log.\n");
				return;
			}
		}
		
		//System.out.println("Jugata creado.\n");
		
		setVisible(true);
		
		timeCount=0;
		
		theWorld.escribir("\n\nAVISO IMPORTANTE:\n");
		theWorld.escribir("�sta es una versi�n Alpha del Aetheria Game Engine. Ello quiere decir que el programa no est� terminado, y si lo tienes es porque lo he difundido para que la gente vaya conoci�ndolo y para localizar los fallos y puntos d�biles. Por lo tanto:\n");
		theWorld.escribir("- Las funciones del programa est�n muy incompletas, faltando caracter�sticas que estar�n presentes en la versi�n final (personajes seudointeligentes, combate, etc�tera).\n");
		theWorld.escribir("- Se mostrar�n muchas veces mensajes que no son necesarios (debug) y no aparecer�n, en aras de una mayor simplicidad y manejabilidad, en el programa final.\n");
		theWorld.escribir("- Pueden aparecer errores, ya sea en forma de excepciones, mensajes de error o bloqueo del programa. Si esto sucede, te agradecer�a que me informaras del error (qu� estabas haciendo cuando apareci�, y mensajes de error que salieron, si es que salieron) en la direcci�n aetheria@irreality.org. As� podr� eliminarlo y mejorar el programa.\n");
		theWorld.escribir("La web del AGE es http://aetheria.irreality.org - ah� ir�n apareciendo las novedades y las nuevas versiones del engine.\n\n");
		
		
		GameEngineThread maquinaEstados =
			new GameEngineThread ( 
				theProtagonist,
				theWorld,
				this );
		
		maquinaEstados.start();		
		
		//Engine no multithread
		//exitFlag = false;
		//while ( !exitFlag )
		//{
		//	timeCount++;
		//	for ( int i = 0 ; i < theWorld.getMaxRoom() ; i++ )
		//	{
		//		if ( theWorld.getRoom(i) != null )
		//			theWorld.getRoom(i).update(theWorld);	
		//	}	
		//	theProtagonist.update(theWorld);
		//}
		//exitNow();
		
		//System.out.println("Laurel and hardy");
	}
*/

	private Object mundoSemaphore = new Object();

	public World waitForMundoToLoad() throws InterruptedException
	{

		System.out.println("Called waitForEtc");
		synchronized ( mundoSemaphore )
		{
			if ( mundo != null ) return mundo;
			while ( mundo == null )
			{
				mundoSemaphore.wait();
			}
		}	
		
		System.out.println("Semaphore return.");
		
		//mundo != null
		return mundo;
		
	}

	public void repaint()
	{
		super.repaint();
		if ( fullScreenMode ) fullScreenFrame.repaint();
	}

	public JPanel getMainPanel()
	{
		return mainPanel;
	}
	
	public void setMainPanel ( JPanel p )
	{
		Container relevantContentPane;
		if ( fullScreenMode ) relevantContentPane = fullScreenFrame.getContentPane();
		else relevantContentPane = getContentPane();
		if ( mainPanel != null ) relevantContentPane.remove(mainPanel);
		mainPanel = p;
		relevantContentPane.add ( p );
	}
	
	public JMenuBar getTheJMenuBar()
	{
		if ( barraMenu != null )
			return barraMenu;
		else
		{
			barraMenu = getJMenuBar();
			return barraMenu;
		}
	}
	
	public void setTheJMenuBar ( JMenuBar jmb )
	{
		barraMenu = jmb;
		if ( !fullScreenMode )
			setJMenuBar ( jmb );
		else
			fullScreenFrame.setJMenuBar( jmb );
	}

	public InputOutputClient getClient()
	{
		return io;
	}


	//remote init
	public SwingAetheriaGameLoader ( final String title , final JDesktopPane gui )
	{
		super ( title , true , true , true );
		
		try
		{
			Image iconito = this.getToolkit().getImage(this.getClass().getClassLoader().getResource("images/intficon.gif"));
			setFrameIcon ( new ImageIcon ( iconito ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		
		new SwingMenuAetheria(this).addToWindow();
		
	//	setDefaultCloseOperation ( JInternalFrame.DO_NOTHING_ON_CLOSE ); //se encarga el listener a continuaci�n
		addInternalFrameListener ( new InternalFrameAdapter()
		{ 
			public void internalFrameClosing ( InternalFrameEvent e )
			{
				System.out.println("Frame closed.");

					saveWindowCoordinates();
					exitNow(); //includes call to this.exitNow();
				//kldispose();
			}
		});
		
		gui.add(this);
		//this.getDesktopPane().getDesktopManager().maximizeFrame(this);
		
		setSize(AGEConfiguration.getInstance().getIntegerProperty("mdiSubwindowWidth"),AGEConfiguration.getInstance().getIntegerProperty("mdiSubwindowHeight"));
		//setLocation(AGEConfiguration.getInstance().getIntegerProperty("mdiWindowLocationX"),AGEConfiguration.getInstance().getIntegerProperty("mdiSubwindowLocationY"));
		if ( AGEConfiguration.getInstance().getBooleanProperty("mdiSubwindowMaximized") )
			this.getDesktopPane().getDesktopManager().maximizeFrame(this);
		
		setVisible(true);
		
		mainPanel = new JPanel(); //panel que contiene al cliente
		setMainPanel( mainPanel );
		
		io = new ColoredSwingClient(this,new Vector()); //components are added 'ere.
		
		//setSize(500,400);
		
	}
	
	
	
	private Thread loaderThread = null;
	
	
	private String moduledir;
	private JDesktopPane gui;
	private boolean usarLog;
	private String logFile;
	private String stateFile;
	private boolean noSerCliente;
	
	class LoaderThread extends Thread 
	{
	
		
					public void run ()
					{

						
						gameLog = new Vector(); //init game log
						
						//io = new SwingEntradaSalida(campoTexto,scrollAreaTexto,areaTexto,gameLog);
						//io = new ColoredSwingClient(esto,campoTexto,scrollAreaTexto,areaTexto,gameLog);
						
						System.out.println("1");
						
						
						try
						{
							SwingUtilities.invokeAndWait 
							( 
									new Runnable()
									{
										public void run()
										{
										
											getContentPane().removeAll();
											mainPanel = new JPanel(); //panel que contiene al cliente
											setMainPanel( mainPanel );
											io = new ColoredSwingClient(esto,gameLog); //components are added 'ere.
											
											if ( logFile != null )
											{
												if ( ((ColoredSwingClient)io).getSoundClient() instanceof AGESoundClient )
												{
													AGESoundClient asc = (AGESoundClient) ((ColoredSwingClient)io).getSoundClient();
													asc.deactivate(); //will be activated on log end (player:endOfLog()
												}
											}
											
											write("Aetheria Game Engine v 1.0.3\n");

											//areaTexto.setText("Aetheria Game Engine v 0.4.7b Beta Distribution\n");
											write("� 1999-2011 Carlos G�mez (solrac888@yahoo.com)\n");
											write("V�ase license.txt para consultar la licencia de AGE y del software de terceros incluido.\n");
											
											write("\n=============================================================");
											write("\n" + io.getColorCode("information") + "Engine-related Version Info:");
											write("\n" + io.getColorCode("information") + "[OS Layer]           " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + io.getColorCode("reset"));
											write("\n" + io.getColorCode("information") + "[Java Layer]         " + System.getProperty("java.version") + io.getColorCode("reset"));
											write("\n" + io.getColorCode("information") + "[Simulation Layer]   " + GameEngineThread.getVersion() + io.getColorCode("reset"));
											write("\n" + io.getColorCode("information") + "[Object Code Layer]  " + ObjectCode.getInterpreterVersion() + io.getColorCode("reset"));
											write("\n" + io.getColorCode("information") + "[UI Layer]           " + SwingAetheriaGameLoader.getVersion() + io.getColorCode("reset"));
											write("\n=============================================================\n");
											
										}
									}
							);
						}
						catch ( Exception e )
						{
							e.printStackTrace();
						}
						
						
						System.out.println("2");
					
						
						
						String worldName;
						World theWorld;
						
						if ( moduledir == null || moduledir.length() == 0 ) moduledir="aetherworld";
					
						
						try
						{
							SwingUtilities.invokeAndWait 
							( 
									new Runnable()
									{
										public void run()
										{
											repaint();
											updateNow();
										}
									}
							);
						}
						catch ( Exception e )
						{
							e.printStackTrace();
						}
						
						
						System.out.println("3");
					
						//doUpdate2 = new UpdatingThread( this , doUpdate );
						//doUpdate2.start();
						//Thread.currentThread().yield();



						//tres posibilidades:
						//*nos han dado un nombre de fichero: mundo loquesea.xml
						//*nos han dado un directorio y el mundo es directorio/world.xml
						//*nos han dado un directorio y el mundo es directorio/world.dat
						
						/*
						File inputAsFile = new File(moduledir);
						if ( inputAsFile.isFile() )
						{
							
							//nos han dado un fichero
							//eventualmente esto deber�a ser the way to go, y el else de este if ser eliminado por antiguo, pero de momento a�n se usa el else (TODO)
							
							System.out.println("Attempting world location: " +  inputAsFile );
							try
							{
								theWorld = new World ( moduledir , io , noSerCliente );
								mundo = theWorld;
								System.out.println("World generated.\n");
								synchronized ( mundoSemaphore )
								{
									mundoSemaphore.notifyAll();
								}
								gameLog.addElement( inputAsFile.getAbsolutePath() ); //primera l�nea del log, fichero de mundo
							}
							catch ( java.io.IOException ioe )
							{
								write("No puedo leer el fichero del mundo: " + inputAsFile + "\n"); 
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
								System.out.println("Attempting world location: " + moduledir + "/world.xml" );
								theWorld = new World ( moduledir + "/world.xml" , io , noSerCliente );
								mundo = theWorld;
								System.out.println("World generated.\n");
								synchronized ( mundoSemaphore )
								{
									mundoSemaphore.notifyAll();
								}
								gameLog.addElement(moduledir + "/world.xml"); //primera l�nea del log, fichero de mundo
							}
							catch ( java.io.IOException e )
							{
		
								System.out.println(e);
		
								//buscar a ver si el mundo es un world.dat
								try 
								{ 
									System.out.println("Attempting world location: " + moduledir + "/world.dat" );
									theWorld = new World ( moduledir + "/world.dat" , io , noSerCliente );
								 	mundo = theWorld;
									synchronized ( mundoSemaphore )
									{
										mundoSemaphore.notifyAll();
									}
									gameLog.addElement(moduledir + "/world.dat"); //primera l�nea del log, fichero de mundo
								 }
								catch ( java.io.FileNotFoundException loadworldfileioerror )
								{ 
									write("No encontrado el fichero del mundo. Tal vez el directorio seleccionado no sea un directorio de mundo AGE v�lido.\n"); 
									return; 
								}
								catch ( java.io.IOException loadworldfileioerror2 )
								{
									write("No puedo leer el fichero del mundo. Tal vez el directorio seleccionado no sea un directorio de mundo AGE v�lido.\n"); 
								  	return; 
								}
							}
						
						}
						*/
						
						theWorld = WorldLoader.loadWorld( moduledir , gameLog, io, mundoSemaphore);
						if ( theWorld == null ) return;
						mundo = theWorld;
						
						//{theWorld NOT null}
									
						final World theFinalWorld = theWorld;
						
						
						try
						{
							SwingUtilities.invokeAndWait 
							( 
									new Runnable()
									{
										public void run()
										{
										
										updateNow();
				
										//atender telnet
											//SimpleTelnetClientHandler stch = new SimpleTelnetClientHandler ( theWorld , 6 , (short)8010 );
											//No. Dar medios para meterla en partidas dedicadas en forma de PartidaEnCurso.
					
										if ( theFinalWorld.getModuleName() != null && theFinalWorld.getModuleName().length() > 0 )
											setTitle(theFinalWorld.getModuleName());
										}
									}
								);
								
						}
						catch ( Exception e )
						{
							e.printStackTrace();
						}
						
						
						//add player if there isn't any
						/*
						if ( theWorld.getPlayerList().size() < 1 && !noSerCliente ) //pues en XML ya se cargan solos los jugadores
						{
						
							Player theProtagonist;
							
							try
							{
								theProtagonist = new Player ( theWorld , io );
							}
							catch ( java.io.IOException loadplayerfileioerror )
							{
							
								try
								{
									//from template
									System.out.println("Adding player, since there isn't any.");
									theWorld.addNewPlayerASAP ( io );
								}
								catch ( XMLtoWorldException xml2we )
								{	
									escribir("No puedo leer el fichero del jugador ni usar plantillas.\n");
									return;
								}
							}
						
						}
						*/
						
						
						if ( new VersionComparator().compare(GameEngineThread.getVersionNumber(),theWorld.getRequiredAGEVersion()) < 0 )
						{
							String mess = "Est�s usando la versi�n " +
								GameEngineThread.getVersionNumber() + " de AGE; pero el mundo " + theWorld.getModuleName() +
								" requiere la versi�n " + theWorld.getRequiredAGEVersion() + " como m�nimo. Podr�a no funcionar " +
								" si no te bajas una nueva versi�n de AGE en http://code.google.com/p/aetheria";
							JOptionPane.showMessageDialog(SwingAetheriaGameLoader.this, mess, "Aviso", JOptionPane.WARNING_MESSAGE);
						}
						
						/*
						org.w3c.dom.Document d = null;
						try
						{
							d = theWorld.getXMLRepresentation();
							System.out.println("D=null?" + (d==null) );
						}
						catch ( javax.xml.parsers.ParserConfigurationException exc )
						{
							System.out.println(exc);
						}
						*/
						
						/*
						try
						{
							PrintStream ps = new PrintStream ( new FileOutputStream ( new File ( "elmundo.xml" ) ) );
							ps.println(d);
						}
						catch ( FileNotFoundException fnfe )
						{
							System.out.println(fnfe);
						}
						*/
						
						//xml printout begin
						
						if ( Debug.DEBUG_OUTPUT )
						{
							
							org.w3c.dom.Document d = null;
							try
							{
								d = theWorld.getXMLRepresentation();
							}
							catch ( javax.xml.parsers.ParserConfigurationException exc )
							{
								System.out.println(exc);
							}
						
							javax.xml.transform.stream.StreamResult sr = null;
						
							try
							{
								sr = new javax.xml.transform.stream.StreamResult ( new FileOutputStream ( "theworld.xml" ) );
							}
							catch ( FileNotFoundException fnfe ) //FileOutputStream <init>
							{
								System.out.println(fnfe);
							}
							
							//hace la transformacion identidad (copia), eso si, escribiendo en ISO.
							try
							{
								javax.xml.transform.Transformer tr = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
							
								tr.setOutputProperty ( javax.xml.transform.OutputKeys.ENCODING , "ISO-8859-1" );
							
								javax.xml.transform.Source s = new javax.xml.transform.dom.DOMSource ( d );
							
								Debug.println("Nodo:" + ((javax.xml.transform.dom.DOMSource)s).getNode());
							
								tr.transform(s,sr); //si esto tira un NullPointerException, la experiencia indica que puede ser por dejar alg�n atributo a null, y esto puede pasar en c�digo descuidado si no se ponen todos los atributos posibles en un tag XML.
								//nota: tambi�n puede ser porque falte un elemento.
								
							}
							catch ( javax.xml.transform.TransformerConfigurationException tfe ) //newTransformer()
							{
								System.out.println(tfe);
							}
							catch ( javax.xml.transform.TransformerException te ) //transform()
							{
								System.out.println(te);
							}
						
						}
						
						
						//xml printout end
						
						
						//usar estado si lo hay
						if ( stateFile != null )
						{
							try
							{
								theWorld.loadState ( stateFile );
							}
							catch ( Exception exc )
							{
								write("�No se ha podido cargar el estado!\n");
								write(exc.toString());
								exc.printStackTrace();
							}
						}
						
						
						if ( usarLog )
						{
							try
							{
							
								/**TEMPORAL. CAMBIAR ESTO.**/
								/**El log debe ser multiplayer.**/
								/*Quitar esta l�nea:*/
								//DONE!!
								//theWorld.getPlayer().prepareLog ( logFile ); //el jugador ejecutar� los comandos del log
								
								System.out.println("RLTLTL");
								System.out.println("Player list is " + theWorld.getPlayerList());
								//System.out.println("Single player is " + theWorld.getPlayer());
								System.out.println("PECADORL");
								
								theWorld.prepareLog(logFile);
								
								theWorld.setRandomNumberSeed( logFile );
							}
							catch ( Exception exc )
							{
								write("Excepci�n al leer el fichero de log: " + exc + "\n");
								exc.printStackTrace();
								return;
							}
						}
						else
						{
							theWorld.setRandomNumberSeed();
						}
						gameLog.addElement(String.valueOf(theWorld.getRandomNumberSeed())); //segunda l�nea, semilla
						
						//System.out.println("Jugata creado.\n");
						
						setVisible(true);
						
						timeCount=0;
						
						/*
						theWorld.escribir("\n\nAVISO IMPORTANTE:\n");
						theWorld.escribir("�sta es una versi�n Beta del Aetheria Game Engine. Ello quiere decir que el programa no est� terminado, y si lo tienes es porque lo he difundido para que la gente vaya conoci�ndolo y para localizar los fallos y puntos d�biles. Por lo tanto:\n");
						theWorld.escribir("- Las funciones del programa est�n muy incompletas, faltando caracter�sticas que estar�n presentes en la versi�n final (personajes seudointeligentes, combate, etc�tera).\n");
						theWorld.escribir("- Se mostrar�n muchas veces mensajes que no son necesarios (debug) y no aparecer�n, en aras de una mayor simplicidad y manejabilidad, en el programa final.\n");
						theWorld.escribir("- Pueden aparecer errores, ya sea en forma de excepciones, mensajes de error o bloqueo del programa. Si esto sucede, te agradecer�a que me informaras del error (qu� estabas haciendo cuando apareci�, y mensajes de error que salieron, si es que salieron) en la direcci�n aetheria@irreality.org. As� podr� eliminarlo y mejorar el programa.\n");
						theWorld.escribir("La web del AGE es http://www.irreality.org/aetheria - ah� ir�n apareciendo las novedades y las nuevas versiones del engine.\n\n");
						*/
						
						mundo = theWorld;
							synchronized ( mundoSemaphore )
							{
								mundoSemaphore.notifyAll();
							}
						
						/* UNCOMMENT THIS FOR REAL-TIME
						GameEngineThread maquinaEstados =
							new GameEngineThread ( 
								theWorld.getPlayer(),
								theWorld,
								esto , true );
						
						maquinaEstados.setRealTimeQuantum(100);
						*/
						
				

							
						maquinaEstados =
							new GameEngineThread ( 
								theWorld,
								esto , false );
						
						System.out.println("STARTING ENGINE THREAD");
						
						maquinaEstados.start();		
					
						System.out.println("ENGINE THREAD STARTED");
						
						System.out.println("noSerCliente = " + noSerCliente);
						
						//Esto enga�a con los estados, lo quitamos.
						/*
						if (noSerCliente)
							write("Este mundo se est� ejecutando en modo Dedicado. Por eso no ves nada aqu�: no eres jugador.");
						*/	
									
						try
						{
							SwingUtilities.invokeAndWait 
							( 
									new Runnable()
									{
										public void run()
										{
											repaint();
											updateNow();
											if ( !fullScreenMode )
											{
												setVisible(false);
												setVisible(true);
											}
											else
											{
												fullScreenFrame.setVisible(false);
												fullScreenFrame.setVisible(true);
											}
										}
									}
							);
						}
						catch ( Exception e )
						{
							e.printStackTrace();
						}
						
						if ( io instanceof ColoredSwingClient )
							((ColoredSwingClient)io).refreshFocus();

						
						
					} //end run
	}
	

	public boolean isFullScreenMode()
	{
		return fullScreenMode;
	}
	
	
	/**
	 * Saves this window's coordinates to the adequate properties file so next time a window from this class
	 * is constructed (i.e. next execution) it will have the same location and size.
	 */
	public void saveWindowCoordinates()
	{
		try
		{
			if ( !this.isMaximum() )
			{
				AGEConfiguration.getInstance().setProperty("mdiSubwindowWidth",String.valueOf(this.getWidth()));
				AGEConfiguration.getInstance().setProperty("mdiSubwindowHeight",String.valueOf(this.getHeight()));
				AGEConfiguration.getInstance().setProperty("mdiSubwindowMaximized","false");
				AGEConfiguration.getInstance().setProperty("mdiSubwindowLocationX",String.valueOf(this.getX()));
				AGEConfiguration.getInstance().setProperty("mdiSubwindowLocationY",String.valueOf(this.getY()));
			}
			else
			{
				AGEConfiguration.getInstance().setProperty("mdiSubwindowMaximized","true");
			};
			AGEConfiguration.getInstance().storeProperties();
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	
	
	//local init
	public SwingAetheriaGameLoader ( final String moduledir , final JDesktopPane gui , final boolean usarLog , final String logFile , final String stateFile , final boolean noSerCliente )
	{
		//Create Window
		
		super(moduledir,true,true,true,true);
		
		this.moduledir=moduledir;
		this.gui=gui;
		this.usarLog=usarLog;
		this.logFile=logFile;
		this.stateFile=stateFile;
		this.noSerCliente=noSerCliente;
		
		
		System.out.println("A");
		
		//System.out.println("noSerCliente = " + noSerCliente);
		//System.err.println("Bogus Stack Trace");
		//new Exception().printStackTrace();
		
		try
		{
			Image iconito = this.getToolkit().getImage(this.getClass().getClassLoader().getResource("images/intficon.gif"));
			setFrameIcon ( new ImageIcon ( iconito ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		
		new SwingMenuAetheria(this).addToWindow();
		
		addInternalFrameListener ( new InternalFrameAdapter()
		{ 
			public void internalFrameClosing ( InternalFrameEvent e )
			{
				System.out.println("Frame closed.");

				saveWindowCoordinates();
				exitNow(); //includes call to this.exitNow();
				//kldispose();
			}
		});
		
		System.out.println("B");
		
		//buff...
		
		//Runtime.getRuntime().setPriority(1);
					
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				
		//System.out.println("Hmm.");
					
		setSize(500,400);
		if ( moduledir.equalsIgnoreCase("") )
		{
			setTitle("Aetheria Game Engine. M�dulo: aetherworld");
		}
		else
		{
			setTitle("Aetheria Game Engine. M�dulo: " + moduledir);
		}
		//getContentPane().setLayout( new BorderLayout() );
		
		System.out.println("C");
		
		gui.add(this);
		//this.getDesktopPane().getDesktopManager().maximizeFrame(this);
		
		setSize(AGEConfiguration.getInstance().getIntegerProperty("mdiSubwindowWidth"),AGEConfiguration.getInstance().getIntegerProperty("mdiSubwindowHeight"));
		//setLocation(AGEConfiguration.getInstance().getIntegerProperty("mdiWindowLocationX"),AGEConfiguration.getInstance().getIntegerProperty("mdiSubwindowLocationY"));
		if ( AGEConfiguration.getInstance().getBooleanProperty("mdiSubwindowMaximized") )
			this.getDesktopPane().getDesktopManager().maximizeFrame(this);
		
		
		setVisible(true);
		final SwingAetheriaGameLoader esto = this;
		
		System.out.println("D");
		
		loaderThread = this.new LoaderThread( );
		
		loaderThread.start();
		

		
	}
	
	public void reinit()
	{
		if ( loaderThread != null )
		{
			final boolean fsm = fullScreenMode;
			setFullScreenMode(false);
			maquinaEstados.uninitServerMenu(this);
			maquinaEstados.exitForReinit();
			((ColoredSwingClient)io).uninitClientMenu(this);
			
			Thread thr = new Thread() {
				public void run()
				{
					loaderThread = SwingAetheriaGameLoader.this.new LoaderThread( );
					loaderThread.start();
					try
					{
						loaderThread.join();
					}
					catch ( InterruptedException ie )
					{
						ie.printStackTrace();
					}
					setFullScreenMode(fsm);
				}
			};
			thr.start();
			
		}
	}


	public void setFullScreenMode ( boolean onOrOff )
	{

		//una inicializaci�n que nunca va a hacer da�o, nos pidan lo que nos pidan		
		if ( fullScreenFrame == null ) 
			fullScreenFrame = new JFrame();
			
		if ( onOrOff ) //set full-screen ON
		{
		
			System.out.println("Setting full-screen dedicated mode ON");
		
			if ( fullScreenMode ) //ya estaba ON
				return;
				
			fullScreenMode = true;
			
			//darle el panel y el men� a la nueva ventana
			remove ( getMainPanel() );
			fullScreenFrame.getContentPane().add ( getMainPanel() );
			//mainPanel = null; <- no, sigue apuntando a lo mismo; aunque no est�.
			setJMenuBar ( new JMenuBar() ); //set j, no set the j
			fullScreenFrame.setJMenuBar ( barraMenu );
						
			GraphicsEnvironment env = GraphicsEnvironment.
				getLocalGraphicsEnvironment();
			GraphicsDevice[] devices = env.getScreenDevices();
			// REMIND : Multi-monitor full-screen mode not yet supported
				if ( !fullScreenFrame.isDisplayable() )
					fullScreenFrame.setUndecorated(true);
				fullScreenFrame.setResizable(false);
				//fullScreenFrame.setVisible(false);
				//fullScreenFrame.setVisible(true);
				devices[0].setFullScreenWindow ( fullScreenFrame );
				DisplayMode dm = devices[0].getDisplayMode();
				fullScreenFrame.setSize(new Dimension(dm.getWidth(), dm.getHeight()));
				fullScreenFrame.validate();
				fullScreenFrame.paintAll(fullScreenFrame.getGraphics());	
				fullScreenFrame.requestFocus();
				//fullScreenFrame.getContentPane().setVisible(true);
				Runnable updateCode = new UpdatingRun(fullScreenFrame);
				Thread c = new Thread ( updateCode );
				c.setPriority ( Thread.MAX_PRIORITY );
				c.start();
				//fullScreenFrame.setVisible(false);
				fullScreenFrame.setVisible(true);
				this.setVisible(false);
				
				/*
						Thread th = new Thread()
						{
							public void run()
							{
								try
								{
									sleep(15000);
									fullScreenFrame.dispose();
								}
								catch ( Throwable any )
								{
									;
								}
							}
						}; //end thread th declaration
						th.start();
				*/
				fullScreenFrame.requestFocus();
				if ( io instanceof ColoredSwingClient )
					((ColoredSwingClient)io).refreshFocus();
			
			
		}
		
		else //set full screen mode off
		{
		
			System.out.println("Setting full-screen dedicated mode OFF");
		
			if ( !fullScreenMode ) //ya estaba OFF
				return;
				
			fullScreenMode = false;
			
			//darle el panel y el men� a esta ventana
			fullScreenFrame.setJMenuBar ( new JMenuBar() );
			fullScreenFrame.remove ( mainPanel );
			setMainPanel ( mainPanel ); //this adds it
			setTheJMenuBar ( barraMenu );
						
			GraphicsEnvironment env = GraphicsEnvironment.
				getLocalGraphicsEnvironment();
			GraphicsDevice[] devices = env.getScreenDevices();
			// REMIND : Multi-monitor full-screen mode not yet supported
				devices[0].setFullScreenWindow ( null );
				DisplayMode dm = devices[0].getDisplayMode();
				//fullScreenFrame.setVisible(false);
				fullScreenFrame.setVisible(false);
				this.setVisible(true);
		
		}
					
		
	
	}



	public void exit ( )
	{
		exitFlag = true;	
	}
	
	public void exitNow()
	{
		if ( maquinaEstados != null )
			maquinaEstados.exitNow();
		else
			saveAndFreeResources();	
	}
	
	public void saveAndFreeResources ( )
	{
		//autosave
		io.write ("Guardando la partida...\n");
		try
		{
			CommonClientUtilities.guardarLog ( new File ( "autosave.alf" ) , gameLog );
		}
		catch (Exception exc)
		{
			io.write("�No se ha podido guardar la partida!\n");
		}
		io.write ("Tiempo del juego: " + timeCount + "\n" );
		io.write ("�Hasta la pr�xima!\n");
		//wait(2);
		//System.exit(0);
		if ( fullScreenMode )
			setFullScreenMode ( false );
		//this.getDesktopPane().remove(this);
		this.dispose();
		
		/*
		synchronized ( this.getClient() )
		{
			this.getClient().notifyAll(); //else thread will be waiting on the client and the GC won't collect it!
		}
		*/
		if ( this.getClient() instanceof ColoredSwingClient )
		{
			((ColoredSwingClient)this.getClient()).uninitClientMenu(this);
			((ColoredSwingClient)this.getClient()).exit(); //this also gets rid of threads waiting in the client
		}
			
		//this.getDesktopPane().remove(this);
		if ( fullScreenFrame != null )
		{
			fullScreenFrame.dispose();
			fullScreenFrame = null;
		}
		
		if ( maquinaEstados != null )
			maquinaEstados.uninitServerMenu(this);
			
		//very important to avoid memory leaks: unreference thread
		maquinaEstados = null;	
		Runtime.getRuntime().gc();
	}
	
	public void unlinkWorld ( )
	{
		mundo = null;
	}
	
	//de informador
	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public void escribir ( String s )
	{
		write(s);
	}


	//de informador
	public void write ( String s )
	{
		io.write(s);	
	}
	public void setIO ( InputOutputClient es )
	{
		io = es;
	}


	/*
	public void guardarLog ( File f ) throws java.io.IOException , java.io.FileNotFoundException
	{
		FileOutputStream fin = new FileOutputStream ( f );
		PrintWriter fwrite = new java.io.PrintWriter ( new java.io.BufferedWriter ( Utility.getBestOutputStreamWriter ( fin ) ) );
		
		for ( int i = 0 ; i < gameLog.size() ; i++ )
		{
			System.out.println("Savin': " + (String)gameLog.elementAt(i) );
			fwrite.println( (String)gameLog.elementAt(i) );
		}
		
		fwrite.flush();	
	}
	
	public void guardarEstado ( File f ) throws java.io.IOException , java.io.FileNotFoundException 
	{
		FileOutputStream fin = new FileOutputStream ( f );
		PrintWriter frwite = new java.io.PrintWriter ( new java.io.BufferedWriter ( Utility.getBestOutputStreamWriter ( fin ) ) );
		
		org.w3c.dom.Document d = null;
		try
		{
			d = mundo.getXMLRepresentation();
			System.out.println("D=null?" + (d==null) );
		}
		catch ( javax.xml.parsers.ParserConfigurationException exc )
		{
			System.out.println(exc);
		}
		
		javax.xml.transform.stream.StreamResult sr = null;
					
		sr = new javax.xml.transform.stream.StreamResult ( new FileOutputStream ( f ) );
			
		//hace la transformacion identidad (copia), eso si, escribiendo en ISO.
		try
		{
			javax.xml.transform.Transformer tr = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty ( javax.xml.transform.OutputKeys.ENCODING , "UTF-8" );
			javax.xml.transform.Source s = new javax.xml.transform.dom.DOMSource ( d );
			System.out.println("Nodo:" + ((javax.xml.transform.dom.DOMSource)s).getNode());
			tr.transform(s,sr);		
		}
		catch ( javax.xml.transform.TransformerConfigurationException tfe ) //newTransformer()
		{
			System.out.println(tfe);
		}
		catch ( javax.xml.transform.TransformerException te ) //transform()
		{
				System.out.println(te);
		}		
	}
	*/


	//funciones de la ventana
	public void guardarLog()
	{
	
		File elFichero = null;
	
		JFileChooser selectorFichero = new JFileChooser( Paths.SAVE_PATH );
		selectorFichero.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FiltroFicheroLog filtro = new FiltroFicheroLog();
		selectorFichero.setFileFilter(filtro);
		int returnVal = selectorFichero.showSaveDialog(this);
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			elFichero = selectorFichero.getSelectedFile();
			try
			{
				if ( !elFichero.toString().toLowerCase().endsWith(".alf") )
				{
					elFichero = new File ( elFichero.toString() + ".alf" );
				}  
				CommonClientUtilities.guardarLog ( elFichero , gameLog );
			}
			catch ( Exception exc )
			{
				write("No se ha podido guardar la partida...");
				write(exc.toString());
			}
		}		
	}
	
	public void guardarEstado()
	{
		File elFichero = null;
		
		JFileChooser selectorFichero = new JFileChooser( Paths.SAVE_PATH );
		selectorFichero.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FiltroFicheroEstado filtro = new FiltroFicheroEstado();
		selectorFichero.setFileFilter(filtro);
		int returnVal = selectorFichero.showSaveDialog(this);
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			elFichero = selectorFichero.getSelectedFile();
			try
			{
				if ( !elFichero.toString().toLowerCase().endsWith(".asf") )
				{
					elFichero = new File ( elFichero.toString() + ".asf" );
				}  
				CommonClientUtilities.guardarEstado ( elFichero , mundo );
			}
			catch ( Exception exc )
			{
				write("No se ha podido guardar la partida...");
				write(exc.toString());
			}
		}		
	}


	public boolean supportsFullScreen() 
	{
		return true;
	}


}
