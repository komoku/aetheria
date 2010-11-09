package eu.irreality.age.swing.applet;

import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import eu.irreality.age.ColoredSwingClient;
import eu.irreality.age.CommonClientUtilities;
import eu.irreality.age.FiltroFicheroEstado;
import eu.irreality.age.FiltroFicheroLog;
import eu.irreality.age.FiltroFicheroMundo;
import eu.irreality.age.GameEngineThread;
import eu.irreality.age.InputOutputClient;
import eu.irreality.age.ObjectCode;
import eu.irreality.age.SwingAetheriaGameLoaderInterface;
import eu.irreality.age.SwingMenuAetheria;
import eu.irreality.age.World;
import eu.irreality.age.debug.Debug;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.filemanagement.WorldLoader;
import eu.irreality.age.swing.sdi.NewFromFileListener;
import eu.irreality.age.swing.sdi.SwingSDIInterface;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.UpdatingRun;

public class SwingSDIApplet extends JApplet implements AGEClientWindow
{


	private World mundo;
	private boolean fullScreenMode;
	private InputOutputClient io;
	private JPanel mainPanel;

	private Vector gameLog;


	private Thread loaderThread = null;

	private GameEngineThread maquinaEstados;


	private String moduledir;
	private boolean usarLog;
	private String logFile;
	private String stateFile;

	private Object mundoSemaphore = new Object();

	public void setMainPanel ( JPanel panel )
	{
		if ( mainPanel != null ) getContentPane().remove(mainPanel);
		mainPanel = panel;
		getContentPane().add ( panel );
	}

	public void write ( String s )
	{
		io.write(s);	
	}

	public static String getVersion ( )
	{
		return "Swing Applet AGE client, v0.1";
	}

	class LoaderThread extends Thread 
	{


		public void run ()
		{


			gameLog = new Vector(); //init game log
			
			Debug.println("1");

			try
			{
				SwingUtilities.invokeAndWait 
				( 
						new Runnable()
						{
							public void run()
							{

								//setVisible(false);
								getContentPane().removeAll();
								mainPanel = new JPanel(); //panel que contiene al cliente
								setMainPanel( mainPanel );
								io = new ColoredSwingClient(SwingSDIApplet.this,gameLog); //components are added 'ere.
								//setVisible(true);

								write("Aetheria Game Engine v 0.8 Beta Distribution\n");

								//areaTexto.setText("Aetheria Game Engine v 0.4.7b Beta Distribution\n");
								write("� 1999-2010 Carlos G�mez (solrac888@yahoo.com)\n");
								write("Biblioteca BeanShell creada por Pat Niemeyer (pat@pat.net) y bajo licencia LGPL (ver docs/LGPL.txt, c�digo fuente en www.beanshell.org)\n");


								write("\n=============================================================");
								write("\n" + io.getColorCode("information") + "Engine-related Version Info:");
								write("\n" + io.getColorCode("information") + "[OS Layer]           " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + io.getColorCode("reset"));
								write("\n" + io.getColorCode("information") + "[Java Layer]         " + System.getProperty("java.version") + io.getColorCode("reset"));
								write("\n" + io.getColorCode("information") + "[Simulation Layer]   " + GameEngineThread.getVersion() + io.getColorCode("reset"));
								write("\n" + io.getColorCode("information") + "[Object Code Layer]  " + ObjectCode.getInterpreterVersion() + io.getColorCode("reset"));
								write("\n" + io.getColorCode("information") + "[UI Layer]           " + SwingSDIApplet.getVersion() + io.getColorCode("reset"));
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

			//areaTexto.setText(areaTexto.getText() + "Running on Aetheria Multiple Game Interface v 1.0 / swing-based MDI interface" );


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
				e.printStackTrace();
			}


			System.out.println("3");

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

			setVisible(true);

			mundo = theWorld;
			synchronized ( mundoSemaphore )
			{
				mundoSemaphore.notifyAll();
			}

			maquinaEstados =
				new GameEngineThread ( 
						theWorld,
						SwingSDIApplet.this , false );

			System.out.println("STARTING ENGINE THREAD");

			maquinaEstados.start();		

			System.out.println("ENGINE THREAD STARTED");

			System.out.println("noSerCliente = " + false);

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
								setVisible(false);
								setVisible(true);

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

	public void start()
	{
		setVisible(true);
		this.requestFocus();
		this.requestFocusInWindow();
		this.requestFocus();
		if ( io != null && io instanceof ColoredSwingClient )
			((ColoredSwingClient)io).refreshFocus();
	}

	private void stopGameSaveAndUnlink()
	{
		if ( maquinaEstados != null )
			maquinaEstados.exitNow();
		else
			saveAndFreeResources();
	}

	public void exitNow()
	{
		write("Para salir de este applet, simplemente cierra la pesta�a del navegador :)\n");
		//stopGameSaveAndUnlink();
		//this.destroy();

		//if ( !standalone )
		//	System.exit(0);
	}

	public void destroy()
	{
		this.exitNow();
	}


	public void saveAndFreeResources ( )
	{
		/*
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
		*/
		io.write ("�Hasta la pr�xima!\n");
		//wait(2);
		//System.exit(0);
		//if ( fullScreenMode )
		//	setFullScreenMode ( false );
		//this.getDesktopPane().remove(this);

		if ( this.getClient() instanceof ColoredSwingClient )
		{
			((ColoredSwingClient)this.getClient()).uninitClientMenu(this);
			((ColoredSwingClient)this.getClient()).exit(); //this also gets rid of threads waiting in the client
		}

		if ( maquinaEstados != null )
			maquinaEstados.uninitServerMenu(this);

		//very important to avoid memory leaks: unreference thread
		maquinaEstados = null;	
		Runtime.getRuntime().gc();
	}

	public JPanel getMainPanel()
	{
		return mainPanel;
	}

	public void unlinkWorld ( )
	{
		mundo = null;
	}

	public InputOutputClient getClient()
	{
		return io;
	}

	public World getMundo()
	{
		return mundo;
	}

	public JMenuBar getTheJMenuBar()
	{
		return getJMenuBar();
	}

	public void setTheJMenuBar(JMenuBar jmb)
	{
		setJMenuBar(jmb);
	}


	public SwingSDIApplet()
	{
		this("AGE Applet");
	}

	public SwingSDIApplet(String title)
	{
		super();
		this.title = title;

		//Image iconito = getToolkit().getImage("images" + File.separatorChar + "intficon.gif");
		/*
			try
			{
				Image iconito = this.getToolkit().getImage(this.getClass().getClassLoader().getResource("images/intficon.gif"));
				this.setIconImage ( iconito );
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}*/

		new SwingMenuAetheria(this).addToWindow();

		JMenu menuArchivo = getTheJMenuBar().getMenu(0);

		//JMenuItem itemNuevo = new JMenuItem("Nuevo juego...");
		//menuArchivo.add(itemNuevo,0);
		//itemNuevo.addActionListener(new NewFromFileListener(this));

		//JMenuItem itemLoadLog = new JMenuItem("Cargar partida...");
		//menuArchivo.add(itemLoadLog,1);
		//itemLoadLog.addActionListener(new LoadFromLogListener(this));

		//JMenuItem itemLoadState = new JMenuItem("Cargar estado...");
		//menuArchivo.add(itemLoadState,2);
		//itemLoadState.addActionListener(new LoadFromStateListener(this));

		menuArchivo.add(new JSeparator(),3);

		setSize(500,400);
	}


	public void startGame ( final String moduledir , final boolean usarLog , final String logFile , final String stateFile )
	{

		if ( loaderThread != null ) //a game is started already
		{
			stopGameSaveAndUnlink();
		}

		this.moduledir=moduledir;
		this.usarLog=usarLog;
		this.logFile=logFile;
		this.stateFile=stateFile;

		System.out.println("B");

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		//setSize(500,400);
		if ( moduledir.equalsIgnoreCase("") )
		{
			setTitle("Aetheria Game Engine. M�dulo: aetherworld");
		}
		else
		{
			setTitle("Aetheria Game Engine. M�dulo: " + moduledir);
		}

		setVisible(true);

		loaderThread = this.new LoaderThread( );

		loaderThread.start();
	}

	//local init
	public SwingSDIApplet ( final String moduledir , final boolean usarLog , final String logFile , final String stateFile  )
	{
		//Create Window

		this(moduledir);

		startGame ( moduledir , usarLog , logFile , stateFile );

	}

	public void reinit()
	{
		if ( loaderThread != null )
		{
			//final boolean fsm = fullScreenMode;
			//setFullScreenMode(false);
			maquinaEstados.uninitServerMenu(this);
			maquinaEstados.exitForReinit();
			((ColoredSwingClient)io).uninitClientMenu(this);

			Thread thr = new Thread() {
				public void run()
				{
					loaderThread = SwingSDIApplet.this.new LoaderThread( );
					loaderThread.start();
					try
					{
						loaderThread.join();
					}
					catch ( InterruptedException ie )
					{
						ie.printStackTrace();
					}
					//setFullScreenMode(fsm);
				}
			};
			thr.start();

		}
	}


	protected Runnable updateCode = new UpdatingRun(this);

	public void updateNow()
	{	
		Thread c = new Thread ( updateCode );
		c.setPriority ( Thread.MAX_PRIORITY );
		c.start();
	}


	//funciones de la ventana copypasteadas de SwingAetheriaGameLoader
	
	public void guardarLog()
	{
		write("AVISO: Desde el navegador no se pueden guardar partidas. Pero puedes guardar partidas si te descargas el Aetheria Game Engine, que permite jugar a aventuras como �sta con plena funcionalidad. B�jalo en http://code.google.com/p/aetheria/downloads/list\n");
		/*
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
		*/	
	}
	

	
	public void guardarEstado()
	{
		write("AVISO: Desde el navegador no se pueden guardar partidas. Pero puedes guardar partidas si te descargas el Aetheria Game Engine, que permite jugar a aventuras como �sta con plena funcionalidad. B�jalo en http://code.google.com/p/aetheria/downloads/list\n");
		/*
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
		*/	
	}
	
	
	public void init()
	{


		System.err.println(this.getClass().getResource("worlds/Vampiro/world.xml"));
		System.err.println(this.getClass().getResource("libinvoke.bsh"));
		SwingAetheriaGameLoaderInterface.loadFont();
		startGame ( this.getParameter("worldUrl") , usarLog , logFile , stateFile );


	}

	private String title = "AGE Applet";

	public String getTitle() 
	{
		return title;
	}

	public boolean isFullScreenMode() 
	{
		return false;
	}

	public void setFullScreenMode(boolean b) 
	{
		; //unsupported
	}

	public void setTitle(String s) 
	{
		title = s;
	}

	public boolean supportsFullScreen()
	{
		return false;
	}

	public void repaint()
	{
		validate();
		super.repaint(100);
	}


}