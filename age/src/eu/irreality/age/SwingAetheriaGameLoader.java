/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
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
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.observer.GameThreadObserver;
import eu.irreality.age.swing.CommonSwingFunctions;
import eu.irreality.age.swing.SwingMenuAetheria;
import eu.irreality.age.swing.applet.SwingSDIApplet;
import eu.irreality.age.swing.config.AGEConfiguration;
import eu.irreality.age.swing.menu.ServerMenuHandler;
import eu.irreality.age.util.VersionComparator;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.UpdatingRun;

import java.io.*; //savegame
//import javax.sound.*;
import java.lang.reflect.InvocationTargetException;



//ventana que carga el juego del directorio dado y llama al Game Engine, la máquina de estados.

public class SwingAetheriaGameLoader extends JInternalFrame implements Informador, AGEClientWindow, GameThreadObserver
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
	* Realizará E/S general y se pasará a todos los informadores.
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
	
	/*Nos dice si el modo full-screen está activado o no.*/
	private boolean fullScreenMode;
	
	/*Barra de menú. Nos interesa tenerla aquí aunque esté removed de la ventana. Por el fullscreen.*/
	private JMenuBar barraMenu;
	
	//
	private GameEngineThread maquinaEstados;
	
	public World getMundo()
	{
		return mundo;
	}


	public String getVersion ( )
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
	
/* Viejo constructor: todo funciona(ba cuando lo comenté); pero no se muestra el texto
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
			setTitle("Aetheria Game Engine. Módulo: aetherworld");
		}
		else
		{
			setTitle("Aetheria Game Engine. Módulo: " + moduledir);
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
			escribir("No encontrado el fichero del mundo. Tal vez el directorio seleccionado no sea un directorio de mundo AGE válido.\n"); 
		  	return; 
		}
		catch ( java.io.IOException loadworldfileioerror2 )
		{
			escribir("No puedo leer el fichero del mundo. Tal vez el directorio seleccionado no sea un directorio de mundo AGE válido.\n"); 
		  	return; 
		}
		
		//Thread.yield();
		updateNow();
		//doUpdate2 = new UpdatingThread( this , doUpdate );
		//doUpdate2.start();
		
		gameLog.addElement(maindir + moduledir + "/world.dat"); //primera línea del log, fichero de mundo
		
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
				
				theProtagonist.prepareLog ( logFile ); //el jugador ejecutará los comandos del log
			}
			catch ( Exception exc )
			{
				escribir("Excepción al leer el fichero de log.\n");
				return;
			}
		}
		
		//System.out.println("Jugata creado.\n");
		
		setVisible(true);
		
		timeCount=0;
		
		theWorld.escribir("\n\nAVISO IMPORTANTE:\n");
		theWorld.escribir("Ésta es una versión Alpha del Aetheria Game Engine. Ello quiere decir que el programa no está terminado, y si lo tienes es porque lo he difundido para que la gente vaya conociéndolo y para localizar los fallos y puntos débiles. Por lo tanto:\n");
		theWorld.escribir("- Las funciones del programa están muy incompletas, faltando características que estarán presentes en la versión final (personajes seudointeligentes, combate, etcétera).\n");
		theWorld.escribir("- Se mostrarán muchas veces mensajes que no son necesarios (debug) y no aparecerán, en aras de una mayor simplicidad y manejabilidad, en el programa final.\n");
		theWorld.escribir("- Pueden aparecer errores, ya sea en forma de excepciones, mensajes de error o bloqueo del programa. Si esto sucede, te agradecería que me informaras del error (qué estabas haciendo cuando apareció, y mensajes de error que salieron, si es que salieron) en la dirección aetheria@irreality.org. Así podré eliminarlo y mejorar el programa.\n");
		theWorld.escribir("La web del AGE es http://aetheria.irreality.org - ahí irán apareciendo las novedades y las nuevas versiones del engine.\n\n");
		
		
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

		//System.out.println("Called waitForEtc");
		synchronized ( mundoSemaphore )
		{
			if ( mundo != null ) return mundo;
			while ( mundo == null )
			{
				mundoSemaphore.wait();
			}
		}	
		
		//System.out.println("Semaphore return.");
		
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
		
	//	setDefaultCloseOperation ( JInternalFrame.DO_NOTHING_ON_CLOSE ); //se encarga el listener a continuación
		addInternalFrameListener ( new InternalFrameAdapter()
		{ 
			public void internalFrameClosing ( InternalFrameEvent e )
			{

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
	
	/**
	 * Removes whatever was on the window and changes it to a new ColoredSwingClient.
	 */
	private void initClient()
	{
		gameLog = new Vector(); //init game log
		getContentPane().removeAll();
		mainPanel = new JPanel(); //panel que contiene al cliente
		setMainPanel( mainPanel );
		io = new ColoredSwingClient(esto,gameLog); //components are added 'ere.
	}
	
	class LoaderThread extends Thread 
	{
	
					public void run ()
					{
						
						//io = new SwingEntradaSalida(campoTexto,scrollAreaTexto,areaTexto,gameLog);
						//io = new ColoredSwingClient(esto,campoTexto,scrollAreaTexto,areaTexto,gameLog);
						
						
						//System.out.println("1");
						
						
						try
						{
							SwingUtilities.invokeAndWait 
							( 
									new Runnable()
									{
										public void run()
										{
										
											initClient();
											
											if ( logFile != null )
											{
												((ColoredSwingClient)io).hideForLogLoad();
												if ( ((ColoredSwingClient)io).getSoundClient() instanceof AGESoundClient )
												{
													AGESoundClient asc = (AGESoundClient) ((ColoredSwingClient)io).getSoundClient();
													asc.deactivate(); //will be activated on log end (player:endOfLog()
												}
											}
											
											CommonSwingFunctions.writeIntroductoryInfo(SwingAetheriaGameLoader.this);
											
										}
									}
							);
						}
						catch ( Exception e )
						{
							if ( io != null ) ((ColoredSwingClient)io).showAfterLogLoad();
							e.printStackTrace();
						}
							
						//System.out.println("2");
					
						String worldName;
						World theWorld = null;
						
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
							((ColoredSwingClient)io).showAfterLogLoad();
							e.printStackTrace();
						}
							
						//System.out.println("3");
						
						try
						{
							theWorld = WorldLoader.loadWorld( moduledir , gameLog, io, mundoSemaphore);
						}
						catch ( Exception e )
						/*
						 * This shouldn't happen, because unchecked exceptions in world initialization scripts are caught before reaching this level,
						 * and the loadWorld method doesn't throw its own exceptions (it returns null if the world cannot be loaded). But it's defensive
						 * programming in case AGE forgets to catch some unchecked exception, which has happened in the past.
						 */
						{
							if ( io != null ) ((ColoredSwingClient)io).showAfterLogLoad();
							if ( io != null ) write ( "Exception on loading world: " + e );
							e.printStackTrace();
						}
						if ( theWorld == null || io.isDisconnected() ) //io could be disconnected due to closing the window before assigning player 
						{
							((ColoredSwingClient)io).showAfterLogLoad();
							return;
						}
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
							((ColoredSwingClient)io).showAfterLogLoad();
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
							String mess = UIMessages.getInstance().getMessage("age.version.warning",
									"$curversion",GameEngineThread.getVersionNumber(),"$reqversion",theWorld.getRequiredAGEVersion(),
									"$world",theWorld.getModuleName());
							mess = mess + " " + UIMessages.getInstance().getMessage("age.download.url");
							/*
							String mess = "Estás usando la versión " +
								GameEngineThread.getVersionNumber() + " de AGE; pero el mundo " + theWorld.getModuleName() +
								" requiere la versión " + theWorld.getRequiredAGEVersion() + " como mínimo. Podría no funcionar " +
								" si no te bajas una nueva versión de AGE en http://code.google.com/p/aetheria";
							*/
							JOptionPane.showMessageDialog(SwingAetheriaGameLoader.this, mess, UIMessages.getInstance().getMessage("age.version.warning.title"), JOptionPane.WARNING_MESSAGE);
						}
						
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
							
								//Debug.println("Nodo:" + ((javax.xml.transform.dom.DOMSource)s).getNode());
							
								tr.transform(s,sr); //si esto tira un NullPointerException, la experiencia indica que puede ser por dejar algún atributo a null, y esto puede pasar en código descuidado si no se ponen todos los atributos posibles en un tag XML.
								//nota: también puede ser porque falte un elemento.
								
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
								((ColoredSwingClient)io).showAfterLogLoad();
								write(UIMessages.getInstance().getMessage("swing.cannot.read.state","$file",stateFile));
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
								/*Quitar esta línea:*/
								//DONE!!
								//theWorld.getPlayer().prepareLog ( logFile ); //el jugador ejecutará los comandos del log
								
								theWorld.prepareLog(logFile);								
								theWorld.setRandomNumberSeed( logFile );
							}
							catch ( Exception exc )
							{
								((ColoredSwingClient)io).showAfterLogLoad();
								//write("Excepción al leer el fichero de log: " + exc + "\n");
								write(UIMessages.getInstance().getMessage("swing.cannot.read.log","$exc",exc.toString()));
								exc.printStackTrace();
								return;
							}
						}
						else
						{
							theWorld.setRandomNumberSeed();
						}
						gameLog.addElement(String.valueOf(theWorld.getRandomNumberSeed())); //segunda línea, semilla
						
						//TODO use invoke method for this to avoid deadlocks:
						try 
						{
							SwingUtilities.invokeAndWait( new Runnable() { public void run() { setVisible(true); } } );
						} 
						catch (InvocationTargetException e1) 
						{
							e1.printStackTrace();
						} 
						catch (InterruptedException e1) 
						{
							e1.printStackTrace();
						}
						
						timeCount=0;
						
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
								theWorld, false );
						
						maquinaEstados.attachObserver(SwingAetheriaGameLoader.this);
						maquinaEstados.attachObserver(new ServerMenuHandler(SwingAetheriaGameLoader.this));
						
						
						//System.out.println("STARTING ENGINE THREAD");
						
						maquinaEstados.start();		
					
						//System.out.println("ENGINE THREAD STARTED");
						
						//Esto engaña con los estados, lo quitamos.
						/*
						if (noSerCliente)
							write("Este mundo se está ejecutando en modo Dedicado. Por eso no ves nada aquí: no eres jugador.");
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
											/*
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
											*/
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
			setTitle("Aetheria Game Engine. " + UIMessages.getInstance().getMessage("swing.default.title.module") + " (sin nombre)");
		}
		else
		{
			setTitle("Aetheria Game Engine. " + UIMessages.getInstance().getMessage("swing.default.title.module") + " " + moduledir);
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
			
			//dejemonos de finuras, total, vamos a recargar el mundo. [2011-05-01]
			/*
			maquinaEstados.uninitServerMenu(this);
			maquinaEstados.exitForReinit();
			((ColoredSwingClient)io).uninitClientMenu(this);
			*/
			maquinaEstados.exitNow();
			
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

		//una inicialización que nunca va a hacer daño, nos pidan lo que nos pidan		
		if ( fullScreenFrame == null ) 
			fullScreenFrame = new JFrame();
			
		if ( onOrOff ) //set full-screen ON
		{
		
			System.out.println("Setting full-screen dedicated mode ON");
		
			if ( fullScreenMode ) //ya estaba ON
				return;
			
			GraphicsEnvironment env = GraphicsEnvironment.
				getLocalGraphicsEnvironment();
			GraphicsDevice device = env.getDefaultScreenDevice(); //devices[0];
			
			if ( !device.isFullScreenSupported() )
			{
				JOptionPane.showMessageDialog(this, UIMessages.getInstance().getMessage("dialog.fullscreen.error"), UIMessages.getInstance().getMessage("dialog.fullscreen.error.not.supported") , JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if ( device.getDisplayMode() == null )
			{
				JOptionPane.showMessageDialog(this, UIMessages.getInstance().getMessage("dialog.fullscreen.error"), UIMessages.getInstance().getMessage("dialog.fullscreen.error.null.display") , JOptionPane.ERROR_MESSAGE);
				return;
			}
				
			fullScreenMode = true;
			
			//darle el panel y el menú a la nueva ventana
			remove ( getMainPanel() );
			fullScreenFrame.getContentPane().add ( getMainPanel() );
			//mainPanel = null; <- no, sigue apuntando a lo mismo; aunque no esté.
			setJMenuBar ( new JMenuBar() ); //set j, no set the j
			fullScreenFrame.setJMenuBar ( barraMenu );
						


			// REMIND : Multi-monitor full-screen mode not yet supported
				if ( !fullScreenFrame.isDisplayable() )
					fullScreenFrame.setUndecorated(true);
				fullScreenFrame.setResizable(false);
				//fullScreenFrame.setVisible(false);
				//fullScreenFrame.setVisible(true);

				
				DisplayMode dm = device.getDisplayMode();
				fullScreenFrame.setSize(new Dimension(dm.getWidth(), dm.getHeight()));
				fullScreenFrame.validate();
				fullScreenFrame.paintAll(fullScreenFrame.getGraphics());
				device.setFullScreenWindow ( fullScreenFrame );
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
			
			//darle el panel y el menú a esta ventana
			fullScreenFrame.setJMenuBar ( new JMenuBar() );
			fullScreenFrame.remove ( mainPanel );
			setMainPanel ( mainPanel ); //this adds it
			setTheJMenuBar ( barraMenu );
						
			GraphicsEnvironment env = GraphicsEnvironment.
				getLocalGraphicsEnvironment();
			GraphicsDevice[] devices = env.getScreenDevices();
			// REMIND : Multi-monitor full-screen mode not yet supported
				GraphicsDevice device = env.getDefaultScreenDevice(); //devices[0];
				device.setFullScreenWindow ( null );
				DisplayMode dm = device.getDisplayMode();
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
		saveWindowCoordinates();
		stopGameSaveAndUnlink();
		this.dispose();
	}
	
	public void stopGameSaveAndUnlink()
	{
		if ( maquinaEstados != null )
			maquinaEstados.exitNow();
		else
			saveAndFreeResources();	
	}
	
	public void saveAndFreeResources ( )
	{
		//autosave
		io.write ( UIMessages.getInstance().getMessage("swing.saving") + "\n");
		try
		{
			CommonClientUtilities.guardarLog ( new File ( "autosave.alf" ) , gameLog );
		}
		catch (Exception exc)
		{
			io.write( UIMessages.getInstance().getMessage("swing.cannot.save.log") + "\n");
		}
		//io.write ("Tiempo del juego: " + timeCount + "\n" );
		io.write ( UIMessages.getInstance().getMessage("swing.bye") + "\n");
		//wait(2);
		//System.exit(0);
		if ( fullScreenMode )
			setFullScreenMode ( false );
		//this.getDesktopPane().remove(this);
		//this.dispose();
		
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
			maquinaEstados.detachAllObservers();
			//maquinaEstados.uninitServerMenu(this);
			
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
	public InputOutputClient getIO()
	{
		return io;
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
				write( UIMessages.getInstance().getMessage("swing.cannot.save.log") + "\n" );
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
				write( UIMessages.getInstance().getMessage("swing.cannot.save.state") + "\n" );
				write(exc.toString());
			}
		}		
	}


	public boolean supportsFullScreen() 
	{
		return true;
	}
	
	public Dimension getScreenSize()
	{
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public void onAttach(GameEngineThread thread) 
	{
	}

	public void onDetach(GameEngineThread thread) 
	{
		unlinkWorld();
		saveAndFreeResources();
	}


}

