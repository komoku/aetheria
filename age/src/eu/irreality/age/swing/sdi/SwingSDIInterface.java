package eu.irreality.age.swing.sdi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import eu.irreality.age.AGESoundClient;
import eu.irreality.age.ColoredSwingClient;
import eu.irreality.age.CommonClientUtilities;
import eu.irreality.age.FiltroFicheroEstado;
import eu.irreality.age.FiltroFicheroLog;
import eu.irreality.age.FiltroFicheroMundo;
import eu.irreality.age.GameEngineThread;
import eu.irreality.age.InputOutputClient;
import eu.irreality.age.ObjectCode;
import eu.irreality.age.SwingAetheriaGameLoader;
import eu.irreality.age.SwingAetheriaGameLoaderInterface;
import eu.irreality.age.World;
import eu.irreality.age.debug.Debug;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.filemanagement.WorldLoader;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.CommonSwingFunctions;
import eu.irreality.age.swing.SwingMenuAetheria;
import eu.irreality.age.swing.UILanguageSelectionMenu;
import eu.irreality.age.swing.config.AGEConfiguration;
import eu.irreality.age.swing.mdi.SwingAetheriaGUI;
import eu.irreality.age.util.VersionComparator;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.UpdatingRun;

public class SwingSDIInterface extends JFrame implements AGEClientWindow
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
	
	public InputOutputClient getIO()
	{
		return io;
	}

	public String getVersion ( )
	{
		return "Swing-based simplified SDI client, v1.0";
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
								//cover();
								getContentPane().removeAll();
								mainPanel = new JPanel(); //panel que contiene al cliente
								setMainPanel( mainPanel );
								io = new ColoredSwingClient(SwingSDIInterface.this,gameLog); //components are added 'ere.
								//setVisible(true);
								//uncover();
								
								if ( logFile != null )
								{
									((ColoredSwingClient)io).hideForLogLoad();
									if ( ((ColoredSwingClient)io).getSoundClient() instanceof AGESoundClient )
									{
										AGESoundClient asc = (AGESoundClient) ((ColoredSwingClient)io).getSoundClient();
										asc.deactivate(); //will be activated on log end (player:endOfLog()
									}
								}
								
								CommonSwingFunctions.writeIntroductoryInfo(SwingSDIInterface.this);
								
								/*
								write("Aetheria Game Engine v " + UIMessages.getInstance().getMessage("age.version") + "\n");

								write( UIMessages.getInstance().getMessage("age.copyright") + "\n" );
								write( UIMessages.getInstance().getMessage("intro.legal") + "\n" );

								write("\n=============================================================");
								write("\n" + io.getColorCode("information") + "Engine-related Version Info:");
								write("\n" + io.getColorCode("information") + "[OS Layer]           " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + io.getColorCode("reset"));
								write("\n" + io.getColorCode("information") + "[Java Layer]         " + System.getProperty("java.version") + io.getColorCode("reset"));
								write("\n" + io.getColorCode("information") + "[Simulation Layer]   " + GameEngineThread.getVersion() + io.getColorCode("reset"));
								write("\n" + io.getColorCode("information") + "[Object Code Layer]  " + ObjectCode.getInterpreterVersion() + io.getColorCode("reset"));
								write("\n" + io.getColorCode("information") + "[UI Layer]           " + SwingSDIInterface.getVersion() + io.getColorCode("reset"));
								write("\n=============================================================\n");
								*/

							}
						}
				);
			}
			catch ( Exception e )
			{
				((ColoredSwingClient)io).showAfterLogLoad();
				e.printStackTrace();
			}


			//System.out.println("2");

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
				((ColoredSwingClient)io).showAfterLogLoad();
				e.printStackTrace();
			}


			//System.out.println("3");

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
				//eventualmente esto debería ser the way to go, y el else de este if ser eliminado por antiguo, pero de momento aún se usa el else (TODO)

				System.out.println("Attempting world location: " +  inputAsFile );
				try
				{
					theWorld = new World ( moduledir , io , false );
					mundo = theWorld;
					System.out.println("World generated.\n");
					synchronized ( mundoSemaphore )
					{
						mundoSemaphore.notifyAll();
					}
					gameLog.addElement( inputAsFile.getAbsolutePath() ); //primera línea del log, fichero de mundo
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
					System.out.println("Attempting world location: "  + moduledir + "/world.xml" );
					theWorld = new World (  moduledir + "/world.xml" , io , false );
					mundo = theWorld;
					System.out.println("World generated.\n");
					synchronized ( mundoSemaphore )
					{
						mundoSemaphore.notifyAll();
					}
					gameLog.addElement( moduledir + "/world.xml"); //primera línea del log, fichero de mundo
				}
				catch ( java.io.IOException e )
				{

					System.out.println(e);

					//buscar a ver si el mundo es un world.dat? no en este cliente.
				}

			}

			if ( theWorld == null )
			{
				write("No encontrado el fichero del mundo. Tal vez el directorio seleccionado no sea un directorio de mundo AGE válido.\n"); 
				return; 
			}*/
			
			try
			{
				theWorld = WorldLoader.loadWorld( moduledir , gameLog, io, mundoSemaphore);
			}
			catch ( Exception e )
			{
				((ColoredSwingClient)io).showAfterLogLoad();
				write ( "Exception on loading world: " + e );
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
			
			if ( new VersionComparator().compare(GameEngineThread.getVersionNumber(),theWorld.getRequiredAGEVersion()) < 0 )
			{
				String mess = UIMessages.getInstance().getMessage("age.version.warning",
						"$curversion",GameEngineThread.getVersionNumber(),"$reqversion",theWorld.getRequiredAGEVersion(),
						"$world",theWorld.getModuleName());
				mess = mess + " " + UIMessages.getInstance().getMessage("age.download.url");
				JOptionPane.showMessageDialog(SwingSDIInterface.this, mess, UIMessages.getInstance().getMessage("age.version.warning.title"), JOptionPane.WARNING_MESSAGE);
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
					theWorld.prepareLog(logFile);
					theWorld.setRandomNumberSeed( logFile );
				}
				catch ( Exception exc )
				{
					((ColoredSwingClient)io).showAfterLogLoad();
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

			setVisible(true);

			mundo = theWorld;
			synchronized ( mundoSemaphore )
			{
				mundoSemaphore.notifyAll();
			}

			maquinaEstados =
				new GameEngineThread ( 
						theWorld,
						SwingSDIInterface.this , false );

			//System.out.println("STARTING ENGINE THREAD");

			maquinaEstados.start();		

			//System.out.println("ENGINE THREAD STARTED");

			System.out.println("noSerCliente = " + false);

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
								//setVisible(false);
								//setVisible(true);

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



	private void stopGameSaveAndUnlink()
	{
		if ( maquinaEstados != null )
			maquinaEstados.exitNow();
		else
			saveAndFreeResources();
	}

	/**
	 * Saves this window's coordinates to the adequate properties file so next time a window from this class
	 * is constructed (i.e. next execution) it will have the same location and size.
	 */
	public void saveWindowCoordinates()
	{
		try
		{
			if ( (this.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH )
			{
				AGEConfiguration.getInstance().setProperty("sdiWindowWidth",String.valueOf(this.getWidth()));
				AGEConfiguration.getInstance().setProperty("sdiWindowHeight",String.valueOf(this.getHeight()));
				AGEConfiguration.getInstance().setProperty("sdiWindowMaximized","false");
				AGEConfiguration.getInstance().setProperty("sdiWindowLocationX",String.valueOf(this.getX()));
				AGEConfiguration.getInstance().setProperty("sdiWindowLocationY",String.valueOf(this.getY()));
			}
			else
			{
				AGEConfiguration.getInstance().setProperty("sdiWindowMaximized","true");
			};
			AGEConfiguration.getInstance().storeProperties();
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	public void exitNow()
	{
		saveWindowCoordinates();
		stopGameSaveAndUnlink();
		this.dispose();
		//if ( !standalone )
		//	System.exit(0);
	}


	private boolean standalone = false;
	
	public void setStandalone ( boolean standalone )
	{
		this.standalone = standalone;
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
		io.write ( UIMessages.getInstance().getMessage("swing.bye") + "\n");
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



	public boolean isFullScreenMode()
	{
		return fullScreenMode;
	}

	public void setFullScreenMode(boolean onOrOff)
	{
		if ( onOrOff ) //set full-screen ON
		{

			System.out.println("Setting full-screen dedicated mode ON");

			if ( fullScreenMode ) //ya estaba ON
				return;

			fullScreenMode = true;

			GraphicsEnvironment env = GraphicsEnvironment.
			getLocalGraphicsEnvironment();
			GraphicsDevice[] devices = env.getScreenDevices();
			// REMIND : Multi-monitor full-screen mode not yet supported

			dispose();
			//if ( !isDisplayable() )
			setUndecorated(true);
			setResizable(false);
			devices[0].setFullScreenWindow ( this );
			DisplayMode dm = devices[0].getDisplayMode();
			setSize(new Dimension(dm.getWidth(), dm.getHeight()));
			validate();
			paintAll(getGraphics());	
			requestFocus();

			Runnable updateCode = new UpdatingRun(this);
			Thread c = new Thread ( updateCode );
			c.setPriority ( Thread.MAX_PRIORITY );
			c.start();

			setVisible(true);


			requestFocus();
			if ( io instanceof ColoredSwingClient )
				((ColoredSwingClient)io).refreshFocus();


		}

		else //set full screen mode off
		{

			System.out.println("Setting full-screen dedicated mode OFF");

			if ( !fullScreenMode ) //ya estaba OFF
				return;

			fullScreenMode = false;

			dispose();
			setUndecorated(false);
			setResizable(true);
			GraphicsEnvironment env = GraphicsEnvironment.
			getLocalGraphicsEnvironment();
			GraphicsDevice[] devices = env.getScreenDevices();
			// REMIND : Multi-monitor full-screen mode not yet supported
			devices[0].setFullScreenWindow ( null );
			DisplayMode dm = devices[0].getDisplayMode();
			setVisible(true);

		}

	}

	public void setTheJMenuBar(JMenuBar jmb)
	{
		setJMenuBar(jmb);

	}

	/**
	 * Maximizes this frame if supported by the platform.
	 */
	private void maximizeIfPossible()
	{
		int state = getExtendedState();	    
	    state |= Frame.MAXIMIZED_BOTH;    
	    setExtendedState(state);
	}

	
	public SwingSDIInterface(String title)
	{
		super(title);
		
		setSize(AGEConfiguration.getInstance().getIntegerProperty("sdiWindowWidth"),AGEConfiguration.getInstance().getIntegerProperty("sdiWindowHeight"));
		setLocation(AGEConfiguration.getInstance().getIntegerProperty("sdiWindowLocationX"),AGEConfiguration.getInstance().getIntegerProperty("sdiWindowLocationY"));
		//setSize(600,600);
		if ( AGEConfiguration.getInstance().getBooleanProperty("sdiWindowMaximized") )
			maximizeIfPossible();
		//setTitle(Messages.getInstance().getMessage("frame.title"));
		
		//Image iconito = getToolkit().getImage("images" + File.separatorChar + "intficon.gif");
		try
		{
			Image iconito = this.getToolkit().getImage(this.getClass().getClassLoader().getResource("images/intficon.gif"));
			this.setIconImage ( iconito );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		
		new SwingMenuAetheria(this).addToWindow();

		addWindowListener ( new WindowAdapter()
		{ 
			public void windowClosing ( WindowEvent e )
			{
				System.out.println("Frame closed.");

				exitNow(); //includes call to this.exitNow();
				//kldispose();
			}
		});
		
		
		JMenu menuArchivo = getTheJMenuBar().getMenu(0);
		
		JMenuItem itemNuevo = new JMenuItem( UIMessages.getInstance().getMessage("menu.new.game") );
		menuArchivo.add(itemNuevo,0);
		itemNuevo.addActionListener(new NewFromFileListener(this));
		
		JMenuItem itemLoadLog = new JMenuItem( UIMessages.getInstance().getMessage("menu.load.game") );
		menuArchivo.add(itemLoadLog,1);
		itemLoadLog.addActionListener(new LoadFromLogListener(this));
		
		JMenuItem itemLoadState = new JMenuItem( UIMessages.getInstance().getMessage("menu.load.state") );
		menuArchivo.add(itemLoadState,2);
		itemLoadState.addActionListener(new LoadFromStateListener(this));
		
		menuArchivo.add(new JSeparator(),3);
		
		//setSize(600,440);
		
		getTheJMenuBar().add( new UILanguageSelectionMenu(this) );
		
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
			setTitle("Aetheria Game Engine. " + UIMessages.getInstance().getMessage("swing.default.title.module") + " (sin nombre)");
		}
		else
		{
			setTitle("Aetheria Game Engine. " + UIMessages.getInstance().getMessage("swing.default.title.module") + " " + moduledir);
		}

		setVisible(true);

		loaderThread = this.new LoaderThread( );

		loaderThread.start();
	}

	//local init
	public SwingSDIInterface ( final String moduledir , final boolean usarLog , final String logFile , final String stateFile  )
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
					loaderThread = SwingSDIInterface.this.new LoaderThread( );
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


	public static void main ( String[] args )
	{
		SwingAetheriaGameLoaderInterface.loadFont();
		JFileChooser selector = new JFileChooser( Paths.WORLD_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		selector.setDialogTitle( UIMessages.getInstance().getMessage("dialog.new.title") );
		selector.setFileFilter ( new FiltroFicheroMundo() );


		int returnVal = selector.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			//System.out.println("Nombre: " + selector.getSelectedFile().getAbsolutePath() );

			//if ( selector.getSelectedFile().isFile() )
			//{
			//new SwingAetheriaGameLoader( selector.getSelectedFile().getParent() , thePanel , false ,null , null, true );
			//}
			//else
			//{
			//Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			new SwingSDIInterface(selector.getSelectedFile().getAbsolutePath(),false,null,null);

			//}
		}
	}

	public boolean supportsFullScreen() 
	{
		return true;
	}
	
	
	
	public void cover()
	{
		JPanel glass = new JPanel();
		glass.setBackground(Color.WHITE);
		glass.setOpaque(true);
		setGlassPane(glass);
		glass.setVisible(true);
	}
	
	public void uncover()
	{
		JPanel glass = (JPanel) getGlassPane();
		glass.setVisible(false);
	}

}
