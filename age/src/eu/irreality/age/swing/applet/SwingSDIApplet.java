package eu.irreality.age.swing.applet;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.zip.DataFormatException;

import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

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
import eu.irreality.age.Utility;
import eu.irreality.age.World;
import eu.irreality.age.debug.Debug;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.filemanagement.WorldLoader;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.observer.GameThreadObserver;
import eu.irreality.age.swing.CommonSwingFunctions;
import eu.irreality.age.swing.FileSelectorDialogs;
import eu.irreality.age.swing.SwingMenuAetheria;
import eu.irreality.age.swing.menu.ServerMenuHandler;
import eu.irreality.age.swing.sdi.NewFromFileListener;
import eu.irreality.age.swing.sdi.SwingSDIInterface;
import eu.irreality.age.util.VersionComparator;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.LoaderThread;
import eu.irreality.age.windowing.UpdatingRun;

public class SwingSDIApplet extends JApplet implements AGEClientWindow, GameThreadObserver
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
	private InputStream logStream;
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
	
	public void setIO ( InputOutputClient io )
	{
		this.io = io;
	}
	
	public GameEngineThread getEngineThread ( )
	{
		return maquinaEstados;
	}

	public void setEngineThread ( GameEngineThread get )
	{
		maquinaEstados = get;
	}
	
	public Vector getGameLog ( )
	{
		return gameLog;
	}

	public String getVersion ( )
	{
		return "Swing Applet AGE client, v0.1";
	}

	/**
	 * Removes whatever was on the applet and changes it to a new ColoredSwingClient.
	 */
	public void initClient()
	{
		gameLog = new Vector(); //init game log
		//setVisible(false);
		getContentPane().removeAll();
		mainPanel = new JPanel(); //panel que contiene al cliente
		setMainPanel( mainPanel );
		io = new ColoredSwingClient(SwingSDIApplet.this,gameLog); //components are added 'ere.
		//setVisible(true);
	}
	
	public void prepareLog( World theWorld ) throws Exception
	{
		logStream.mark(100000);
		theWorld.prepareLog(logStream);
		logStream.reset();
		theWorld.setRandomNumberSeed( logStream );
	}
	
//	class LoaderThread extends Thread 
//	{
//
//		public void run ()
//		{
//
//			try
//			{
//				SwingUtilities.invokeAndWait 
//				( 
//						new Runnable()
//						{
//							public void run()
//							{
//
//								initClient();
//
//								if ( usarLog )
//								{
//									((ColoredSwingClient)io).hideForLogLoad();
//									if ( ((ColoredSwingClient)io).getSoundClient() instanceof AGESoundClient )
//									{
//										AGESoundClient asc = (AGESoundClient) ((ColoredSwingClient)io).getSoundClient();
//										asc.deactivate(); //will be activated on log end (player:endOfLog()
//									}
//								}
//								
//								CommonSwingFunctions.writeIntroductoryInfo(SwingSDIApplet.this);
//
//							}
//						}
//				);
//			}
//			catch ( Exception e )
//			{
//				if ( io != null ) ((ColoredSwingClient)io).showAfterLogLoad();
//				e.printStackTrace();
//			}
//
//			//System.out.println("2");
//
//			String worldName;
//			World theWorld = null;
//
//			if ( moduledir == null || moduledir.length() == 0 ) moduledir="aetherworld";
//
//
//			try
//			{
//				SwingUtilities.invokeAndWait 
//				( 
//						new Runnable()
//						{
//							public void run()
//							{
//								repaint();
//								updateNow();
//							}
//						}
//				);
//			}
//			catch ( Exception e )
//			{
//				((ColoredSwingClient)io).showAfterLogLoad();
//				e.printStackTrace();
//			}
//
//			//System.out.println("3");
//
//			try
//			{
//				theWorld = WorldLoader.loadWorld( moduledir , gameLog, io, mundoSemaphore);
//			}
//			catch ( Exception e )
//			/*
//			 * This shouldn't happen, because unchecked exceptions in world initialization scripts are caught before reaching this level,
//			 * and the loadWorld method doesn't throw its own exceptions (it returns null if the world cannot be loaded). But it's defensive
//			 * programming in case AGE forgets to catch some unchecked exception, which has happened in the past.
//			 */
//			{
//				if ( io != null ) ((ColoredSwingClient)io).showAfterLogLoad();
//				if ( io != null ) write ( "Exception on loading world: " + e );
//				e.printStackTrace();
//			}
//			if ( theWorld == null || io.isDisconnected() )
//			{
//				((ColoredSwingClient)io).showAfterLogLoad();
//				return;
//			}
//			mundo = theWorld;
//
//			//{theWorld NOT null}
//
//			final World theFinalWorld = theWorld;
//
//
//			try
//			{
//				SwingUtilities.invokeAndWait 
//				( 
//						new Runnable()
//						{
//							public void run()
//							{
//
//								updateNow();
//
//								//atender telnet
//								//SimpleTelnetClientHandler stch = new SimpleTelnetClientHandler ( theWorld , 6 , (short)8010 );
//								//No. Dar medios para meterla en partidas dedicadas en forma de PartidaEnCurso.
//
//								if ( theFinalWorld.getModuleName() != null && theFinalWorld.getModuleName().length() > 0 )
//									setTitle(theFinalWorld.getModuleName());
//							}
//						}
//				);
//
//			}
//			catch ( Exception e )
//			{
//				((ColoredSwingClient)io).showAfterLogLoad();
//				e.printStackTrace();
//			}
//
//			if ( new VersionComparator().compare(GameEngineThread.getVersionNumber(),theWorld.getRequiredAGEVersion()) < 0 )
//			{
//				String mess = UIMessages.getInstance().getMessage("age.version.warning",
//						"$curversion",GameEngineThread.getVersionNumber(),"$reqversion",theWorld.getRequiredAGEVersion(),
//						"$world",theWorld.getModuleName());
//				mess = mess + " " + UIMessages.getInstance().getMessage("age.download.url");
//				JOptionPane.showMessageDialog(SwingSDIApplet.this, mess, UIMessages.getInstance().getMessage("age.version.warning.title"), JOptionPane.WARNING_MESSAGE);
//			}
//			
//			//usar estado si lo hay
//			if ( stateFile != null )
//			{
//				try
//				{
//					theWorld.loadState ( stateFile );
//				}
//				catch ( Exception exc )
//				{
//					((ColoredSwingClient)io).showAfterLogLoad();
//					write(UIMessages.getInstance().getMessage("swing.cannot.read.state","$file",stateFile));
//					write(exc.toString());
//					exc.printStackTrace();
//				}
//			}
//
//
//			if ( usarLog )
//			{
//				try //different on applet
//				{
//					prepareLog( theWorld );
//				}
//				catch ( Exception exc )
//				{
//					((ColoredSwingClient)io).showAfterLogLoad();
//					write(UIMessages.getInstance().getMessage("swing.cannot.read.log","$exc",exc.toString()));
//					exc.printStackTrace();
//					return;
//				}
//			}
//			else
//			{
//				theWorld.setRandomNumberSeed();
//			}
//			gameLog.addElement(String.valueOf(theWorld.getRandomNumberSeed())); //segunda l�nea, semilla
//
//			//TODO use invoke method for this to avoid deadlocks:
//			try 
//			{
//				SwingUtilities.invokeAndWait( new Runnable() { public void run() { setVisible(true); } } );
//			} 
//			catch (InvocationTargetException e1) 
//			{
//				e1.printStackTrace();
//			} 
//			catch (InterruptedException e1) 
//			{
//				e1.printStackTrace();
//			}
//
//			
//			mundo = theWorld;
//			synchronized ( mundoSemaphore )
//			{
//				mundoSemaphore.notifyAll();
//			}
//
//			maquinaEstados =
//				new GameEngineThread ( 
//						theWorld, false );
//			
//			maquinaEstados.attachObserver(SwingSDIApplet.this);
//			maquinaEstados.attachObserver(new ServerMenuHandler(SwingSDIApplet.this));
//
//			//System.out.println("STARTING ENGINE THREAD");
//
//			maquinaEstados.start();		
//
//			//System.out.println("ENGINE THREAD STARTED");
//
//			//Esto enga�a con los estados, lo quitamos.
//			/*
//							if (noSerCliente)
//								write("Este mundo se est� ejecutando en modo Dedicado. Por eso no ves nada aqu�: no eres jugador.");
//			 */	
//
//			try
//			{
//				SwingUtilities.invokeAndWait 
//				( 
//						new Runnable()
//						{
//							public void run()
//							{
//								repaint();
//								updateNow();
//								setVisible(false);
//								setVisible(true);
//
//							}
//						}
//				);
//			}
//			catch ( Exception e )
//			{
//				e.printStackTrace();
//			}
//
//			if ( io instanceof ColoredSwingClient )
//				((ColoredSwingClient)io).refreshFocus();
//
//
//
//		} //end run
//	}

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
		write( UIMessages.getInstance().getMessage("applet.exit") + "\n" );
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
		io.write ( UIMessages.getInstance().getMessage("swing.bye") + "\n" );
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
			maquinaEstados.detachAllObservers();
			//maquinaEstados.uninitServerMenu(this);

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

	public World getWorld()
	{
		return mundo;
	}
	
	public void setWorld ( World w )
	{
		mundo = w;
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

		JMenuItem itemLoadLog = new JMenuItem( UIMessages.getInstance().getMessage("menu.load.log") );
		menuArchivo.add(itemLoadLog,1);
		itemLoadLog.addActionListener(new ActionListener()
		{
			public void actionPerformed( ActionEvent evt )
			{
				loadLogFromCookie();
			}
		});

		//JMenuItem itemLoadState = new JMenuItem("Cargar estado...");
		//menuArchivo.add(itemLoadState,2);
		//itemLoadState.addActionListener(new LoadFromStateListener(this));

		menuArchivo.add(new JSeparator(),3);

		setSize(500,400);
	}

	
	public void loadLogFromCookie()
	{
		//String logAsString = CookieUtils.readCookie(SwingSDIApplet.this,"log");
		String logAsString = "";
		
		if ( LocalStorageUtils.isLocalStorageSupported(this) )
		{
			try {
				logAsString = LocalStorageUtils.loadCompressedData(SwingSDIApplet.this,"log","UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
		}
		else //use plain good ol' cookie
		{
			try {
				logAsString = CookieUtils.readCompressedCookie(SwingSDIApplet.this,"log","UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
		}
		
		if ( logAsString == null )
		{
			write(UIMessages.getInstance().getMessage("applet.load.notsaved")+"\n");
			return;
		}
		
		try
		{
			logStream = new ByteArrayInputStream(logAsString.getBytes("UTF-8"));
		}
		catch ( UnsupportedEncodingException uee )
		{
			uee.printStackTrace();
			write(uee.toString());
		}
		usarLog = true;
		reinit();
	}

	public void startGame ( final String moduledir , final boolean usarLog , final InputStream logStream , final String stateFile )
	{
		
		if ( loaderThread != null ) //a game is started already
		{
			stopGameSaveAndUnlink();
		}

		this.moduledir=moduledir;
		this.usarLog=usarLog;
		this.logStream=logStream;
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

		loaderThread //= this.new LoaderThread( );
			= new LoaderThread ( moduledir, usarLog, stateFile, this , mundoSemaphore );
		
		loaderThread.start();
	}

	//local init
	public SwingSDIApplet ( final String moduledir , final boolean usarLog , final InputStream logStream , final String stateFile  )
	{
		//Create Window

		this(moduledir);

		startGame ( moduledir , usarLog , logStream , stateFile );

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
					loaderThread //= SwingSDIApplet.this.new LoaderThread( );
						= new LoaderThread ( moduledir, usarLog, stateFile, SwingSDIApplet.this , mundoSemaphore );
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
		//write("AVISO: La funcionalidad de guardar partidas es EXPERIMENTAL y es posible que tenga errores. Adem�s, s�lo se puede guardar una partida a la vez, requiere que tu navegador acepte cookies, y el juego salvado no es permanente sino que pod�a borrarse si tu navegador elimina las cookies cada cierto tiempo.\n");
		//write("Puedes guardar partidas de forma totalmente fiable (y tener guardadas m�ltiples partidas a la vez) si te descargas el Aetheria Game Engine, que permite jugar a aventuras como �sta con plena funcionalidad. B�jalo en http://code.google.com/p/aetheria/\n");
		write(UIMessages.getInstance().getMessage("applet.save.warning"));
		
		String logToString = "";
		
		for ( int i = 0 ; i < gameLog.size() ; i++ )
		{
			logToString += gameLog.get(i);
			//logToString +="\\n"; //con \n no se ejecuta bien el js
			logToString +="\n"; //lo de arriba pierde el sentido con base64
		}
			
		//write("Guardando: "  + logToString);
		
		
		boolean localStorageSuccess = false;
		boolean cookieSuccess = false;
		if ( LocalStorageUtils.isLocalStorageSupported(this))
		{
			LocalStorageUtils.eraseData(this,"log");
			try {
				localStorageSuccess = LocalStorageUtils.saveAndValidateCompressedData(this, "log", logToString, "UTF-8");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if ( !localStorageSuccess )
		{
			CookieUtils.eraseCookie(this,"log");
			try {
				cookieSuccess = CookieUtils.createAndValidateCompressedCookie(this,"log",logToString,"UTF-8",100);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if ( localStorageSuccess || cookieSuccess )
			write(UIMessages.getInstance().getMessage("applet.save.done") + "\n");
		else
			write(UIMessages.getInstance().getMessage("applet.save.failed") + "\n");
		
		
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
		//write("AVISO: Desde el navegador no se pueden guardar partidas. Pero puedes guardar partidas si te descargas el Aetheria Game Engine, que permite jugar a aventuras como �sta con plena funcionalidad. B�jalo en http://code.google.com/p/aetheria/\n");
		write(UIMessages.getInstance().getMessage("applet.state.warning") + "\n");
		guardarLog();
		
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


		//System.err.println(this.getClass().getResource("worlds/Vampiro/world.xml"));
		System.err.println(this.getClass().getResource("libinvoke.bsh"));
		SwingAetheriaGameLoaderInterface.loadFont();
		startGame ( this.getParameter("worldUrl") , usarLog , logStream , stateFile );


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

	/**
	 * Returns screen size based on the values of screenwidth and screenheight parameters if set
	 */
	public Dimension getScreenSize()
	{
		int width;
		int height;
		String sw = this.getParameter("screenwidth");
		String sh = this.getParameter("screenheight");
		if ( sw == null || sh == null ) return null;
		try
		{
			return new Dimension ( Integer.valueOf(sw).intValue() , Integer.valueOf(sh).intValue() );
		}
		catch ( NumberFormatException nfe )
		{
			return null;
		}
	}
	
	public void onAttach(GameEngineThread thread) 
	{
	}

	public void onDetach(GameEngineThread thread) 
	{
		unlinkWorld();
		saveAndFreeResources();
	}
	
	public String recoverMissingWorldPath()
	{
		return null;
	}

}
