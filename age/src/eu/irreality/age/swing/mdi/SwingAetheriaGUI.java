package eu.irreality.age.swing.mdi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import eu.irreality.age.ServerConfigurationWindow;
import eu.irreality.age.SwingAetheriaGameLoader;
import eu.irreality.age.SwingAetheriaGameLoaderInterface;
import eu.irreality.age.SwingImageDrawingThread;
import eu.irreality.age.server.ServerHandler;
import eu.irreality.age.swing.config.AGEConfiguration;
import eu.irreality.age.swing.mdi.gameloader.GameChoosingInternalFrame;

public class SwingAetheriaGUI extends JFrame
{
	JDesktopPane panel;
	JMenuBar menuBar;
	Vector padre1, padre2;
	int i=0,j=0;

	private static SwingAetheriaGUI instance; //not exactly a singleton, but at least the last instance is always cached (for IDE go button use)

	public static SwingAetheriaGUI getInstance() { return instance; }

	public JDesktopPane getPanel()
	{
		return panel;
	}
	
	public void closeSubWindows()
	{
		JInternalFrame[] frames = panel.getAllFrames();
		for ( int i = 0 ; i < frames.length ; i++ )
		{
			if ( frames[i] instanceof SwingAetheriaGameLoader )
			{
				((SwingAetheriaGameLoader)frames[i]).exitNow();
			}
		}
	}

	//unused
	/*
	public void setStandalone ( boolean standalone )
	{
		if ( standalone == true )
			this.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
		else
			this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	}
	*/


	/**
	 * Maximizes this frame if supported by the platform.
	 */
	private void maximizeIfPossible()
	{
		int state = getExtendedState();	    
		state |= Frame.MAXIMIZED_BOTH;    
		setExtendedState(state);
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
				AGEConfiguration.getInstance().setProperty("mdiWindowWidth",String.valueOf(this.getWidth()));
				AGEConfiguration.getInstance().setProperty("mdiWindowHeight",String.valueOf(this.getHeight()));
				AGEConfiguration.getInstance().setProperty("mdiWindowMaximized","false");
				AGEConfiguration.getInstance().setProperty("mdiWindowLocationX",String.valueOf(this.getX()));
				AGEConfiguration.getInstance().setProperty("mdiWindowLocationY",String.valueOf(this.getY()));
			}
			else
			{
				AGEConfiguration.getInstance().setProperty("mdiWindowMaximized","true");
			};
			AGEConfiguration.getInstance().storeProperties();
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}

	public static WindowListener standaloneWindowListener = new WindowAdapter() 
	{
		public void windowClosing ( WindowEvent e )
		{
			SwingAetheriaGUI.getInstance().saveWindowCoordinates();
			System.exit(0);
		}
	};
	
	public static WindowListener nonStandaloneWindowListener = new WindowAdapter() 
	{
		public void windowClosing ( WindowEvent e )
		{
			SwingAetheriaGUI.getInstance().saveWindowCoordinates();
			((SwingAetheriaGUI)e.getWindow()).setVisible(false);
			((SwingAetheriaGUI)e.getWindow()).closeSubWindows();
		}
	};
	
	
	public SwingAetheriaGUI ( )
	{	



		/* mod (was working)
		try
		{
		(new AGESoundClient()).playMOD ( new File ( "VIM-Needless.MOD" ) , 2 );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		 */

		super("Aetheria Game Engine, v 1.1.8");
		instance = this;
		
		//obsolete
		this.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
		//this:
		addWindowListener ( standaloneWindowListener ); //standalone by default
		
		
		setSize(AGEConfiguration.getInstance().getIntegerProperty("mdiWindowWidth"),AGEConfiguration.getInstance().getIntegerProperty("mdiWindowHeight"));
		setLocation(AGEConfiguration.getInstance().getIntegerProperty("mdiWindowLocationX"),AGEConfiguration.getInstance().getIntegerProperty("mdiWindowLocationY"));
		//setSize(600,600);
		if ( AGEConfiguration.getInstance().getBooleanProperty("mdiWindowMaximized") )
			maximizeIfPossible();
		
		

		try
		{
			Image iconito = this.getToolkit().getImage(this.getClass().getClassLoader().getResource("images/intficon.gif"));
			this.setIconImage ( iconito );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}


		/*
		Dimension tamanoVentana = this.getToolkit().getScreenSize();
		this.setSize ( tamanoVentana.width - 150 , tamanoVentana.height - 150 );
		this.setLocation ( 70 , 70 );

		this.maximizeIfPossible();
		*/

		/*
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		gd.setFullScreenWindow(mdi);
		 */

		SwingImageDrawingThread imthread = new SwingImageDrawingThread("images/agelogo.jpg" , 1000 , this );
		//imthread.setPriority(Thread.MAX_PRIORITY);
		imthread.start();


		//BEGIN SPLASH WINDOW RELATED CODE
		/*		
		MediaTracker mt = new MediaTracker(mdi);
       	Image splashIm = mdi.getToolkit().createImage("agelogo.jpg");
       	mt.addImage(splashIm,0);
       	try 
		{
         	mt.waitForID(0);
       	} 
		catch(InterruptedException ie){}

		//splash window (test)

		SplashWindow w = new SplashWindow( mdi , splashIm );
        w.setVisible(true);
        try {
	  		Thread.sleep(3000);
       	} catch(InterruptedException ie){}
       	w.dispose();

		 */			
		// give time to GUI thread
		/*try
        {
        	Thread.sleep(200);
            Thread.currentThread().yield(); // Give a chance to other threads.
        }  catch(InterruptedException e)  { }
        w.repaint();
        try
        {
        	Thread.sleep(200);
        	Thread.currentThread().yield(); // Give a chance to other threads.
        }  catch(InterruptedException e)  { }
		 */

		//END SPLASH WINDOW RELATED CODE


		ServerConfigurationWindow.setMadre(this);


		Image logo = this.getToolkit().createImage(this.getClass().getClassLoader().getResource("images/agelogo.jpg"));
		
		panel = new JDesktopPane( );
		panel.setBackground(Color.black);

		final JPanel general = new JPanel();
		general.setLayout ( new BorderLayout() );
		general.add ( panel , "Center" );
		JPanel botones = new JPanel();
		botones.setLayout ( new GridLayout(1,7 ) );

		JButton botonNuevo = new JButton ( "Nuevo" );

		botones.add ( botonNuevo );
		botones.add ( new JButton ( javax.swing.plaf.metal.MetalIconFactory.getFileChooserNewFolderIcon() ) );
		botones.add ( new JButton ( javax.swing.plaf.metal.MetalIconFactory.getFileChooserNewFolderIcon() ) );
		//general.add ( botones , "North" );

		//final JPanel panelNuevoJuego = new GameChoosingPanel(panel);
		//general.add ( panelNuevoJuego , "Center" );
		//panelNuevoJuego.setVisible(false);
		panel.setVisible(true);

		/*
		botonNuevo.addActionListener ( new ActionListener ()
		{
			public void actionPerformed ( ActionEvent evt )
			{	
				general.remove ( panelNuevoJuego );
				general.add ( panelNuevoJuego , "Center" );
				panelNuevoJuego.setVisible(true);	
				//panel.add ( panelNuevoJuego , "Center" );
			}
		} );
		 */

		this.setContentPane ( general );

		//general.add ( new JButton("Prueba") );


		this.setJMenuBar ( new MDIMenuBar( panel , this ) );
		this.setVisible ( true );


		//cargar configuración del ini
		/*
	String fontName = "Courier New";
	int fontSize = 12;
	try
	{
	    BufferedReader iniReader = new BufferedReader ( Utility.getBestInputStreamReader ( new FileInputStream( "age.cfg" ) ) );
	    String linea;
	    for ( int line = 1 ; line < 100 ; line++ )
	    {
		linea = iniReader.readLine();
		if ( linea != null )
		{
		    System.out.println("Linea " + linea );
		    String codigo = StringMethods.getTok(linea,1,'=').trim().toLowerCase();
		    if ( codigo.equals("font name") )
		    {
			System.out.println("Nombre: " + StringMethods.getTok(linea,2,'=').trim() );
			fontName = StringMethods.getTok(linea,2,'=').trim(); 
		    }
		    else if ( codigo.equals("font size" ) )
		    {
			fontSize = Integer.parseInt(StringMethods.getTok(linea,2,'=').trim());
		    }
		}
	    }
	}
	//las excepciones nos la sudan porque hay valores por defecto
	catch ( FileNotFoundException fnfe )
	{
	    ;
	}
	catch ( NumberFormatException nfe )
	{
	    ;
	}
	catch ( IOException ioe )
	{
	    ;
	}


	Font[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	Font fuenteElegida;
	for ( int f = 0 ; f < fuentes.length ; f++ )
	{
	    if ( fuentes[f].getFontName().equalsIgnoreCase(fontName) )
	    {
		SwingAetheriaGameLoaderInterface.font = fuentes[f].deriveFont((float)fontSize);
		break;
	    }
	    //System.out.println("Fuente: " + fuentes[f]);
	}

	//System.err.println("He seleccionado mi fuente, y es: " + SwingAetheriaGameLoaderInterface.font  );

	//font not selected? be less picky
	if ( SwingAetheriaGameLoaderInterface.font == null )
	{
	    String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	    Arrays.sort(fonts);
	    if (Arrays.binarySearch(fonts, "Courier New") >= 0) {
		SwingAetheriaGameLoaderInterface.font = new Font("Courier New", Font.PLAIN, 12);
	    } else if (Arrays.binarySearch(fonts, "Courier") >= 0) {
		SwingAetheriaGameLoaderInterface.font = new Font("Courier", Font.PLAIN, 12);
	    } else if (Arrays.binarySearch(fonts, "Monospaced") >= 0) {
		SwingAetheriaGameLoaderInterface.font = new Font("Monospaced", Font.PLAIN, 13);
	    }
	}

	//still not selected? well, in that case just default to font 0
	if ( SwingAetheriaGameLoaderInterface.font == null )
	    SwingAetheriaGameLoaderInterface.font=fuentes[0].deriveFont((float)fontSize);
		 */

		//new SwingAetheriaGameLoader ( "" , panel , false , null );


		SwingAetheriaGameLoaderInterface.loadFont();

		//el add ya lo hace solo.
		//panel.add ( new SwingAetheriaGameLoader ( "" , panel ) );
		//panel.add ( new SwingAetheriaGameLoader ( "" , panel ) );




		//poner el dialogo inicial

		//new GameChoosingDialog ( panel );

		panel.add ( new GameChoosingInternalFrame ( panel ) );




		//init partidas dedicadas si necesario

		ServerHandler sh = ServerHandler.getInstance( panel );
		if ( sh.getServerConfigurationOptions().initOnStartup() )
			sh.initPartidasDedicadas( panel );




	}

}
