/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.xml.sax.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.*;

import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.swing.sdi.SwingSDIInterface;

import javax.xml.parsers.*;


/*class SplashWindow extends Window {
    Image splashIm;

    SplashWindow(Frame parent, Image splashIm) {
        super(parent);
        this.splashIm = splashIm;
        setSize(splashIm.getWidth(null),splashIm.getHeight(null));

        // Center the window
        Dimension screenDim = 
             Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle winDim = getBounds();
        setLocation((screenDim.width - winDim.width) / 2,
		(screenDim.height - winDim.height) / 2);
        setVisible(true);
    }

    public void paint(Graphics g) {
       if (splashIm != null) {
           g.drawImage(splashIm,0,0,this);
       }
    }
}
 */


//this runs the GUI (but doesn't represent the GUI)
public class SwingAetheriaGameLoaderInterface
{

	public static Font font;

	public static void loadFont()
	{

		//cargar configuración del ini

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
	}

	public static void setStandalone ( boolean standalone )
	{
		if ( standalone == true )
		{
			//obsolete
			SwingAetheriaGUI.getInstance().setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
			//this:
			SwingAetheriaGUI.getInstance().addWindowListener ( new WindowAdapter() 
			{
				public void windowClosing ( WindowEvent e )
				{
					System.exit(0);
				}
			}
			);

		}
		else
		{
			//obsolete
			SwingAetheriaGUI.getInstance().setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
			//this:
			SwingAetheriaGUI.getInstance().addWindowListener ( new WindowAdapter() 
			{
				public void windowClosing ( WindowEvent e )
				{
					((SwingAetheriaGUI)e.getWindow()).setVisible(false);
					((SwingAetheriaGUI)e.getWindow()).closeSubWindows();
				}
			}
			);
		}
	}

	public static void showIfAlreadyOpen()
	{
		if ( SwingAetheriaGUI.getInstance() != null )
		{
			System.err.println("2 Showing previous instance...");
			SwingAetheriaGUI.getInstance().setVisible(true);
			System.err.println("2 Done.");
			System.err.flush();
		}
	}
	
	public static void setLookAndFeel ( )
	{
		try
		{

			boolean setLAF = true;

			//check java version and native look and feel: seems that GTK look and feel is broken for versions < 1.6

			if ( UIManager.getSystemLookAndFeelClassName().indexOf("gtk") >= 0 )
			{
				String javaVersion = System.getProperty("java.version");
				StringTokenizer st = new StringTokenizer(javaVersion,".-_");
				int firstNum;
				int secondNum;
				try
				{
					firstNum = Integer.valueOf(st.nextToken()).intValue();
					secondNum = Integer.valueOf(st.nextToken()).intValue();
					if ( firstNum <= 1 && secondNum <= 5 )
						setLAF = false;
				}
				catch ( Exception exc )
				{
					; //version unreadable: bah, set the look and feel and pray.
				}

			}

			if ( setLAF )
			{
				UIManager.setLookAndFeel ( UIManager.getSystemLookAndFeelClassName() );
			}

		}
		catch ( Exception ulafe )
		{
			ulafe.printStackTrace();
		}
	}

	public static void main ( String[] args )
	{


		
		if ( args.length > 0 )
		{
			//parse command line
			
			Option sdi = new Option( "sdi", "use single-document interface" );
			Option worldFile = OptionBuilder.withArgName( "file" )
            .hasArg()
            .withDescription(  "The world file to play" )
            .create( "worldfile" );	
			Option logFile   = OptionBuilder.withArgName( "file" )
            .hasArg()
            .withDescription(  "Log file to load the game from (requires a world file)" )
            .create( "logfile" );
			Option stateFile   = OptionBuilder.withArgName( "file" )
            .hasArg()
            .withDescription(  "State file to load the game from (requires a world file)" )
            .create( "statefile" );
			
			Options options = new Options();

			options.addOption( sdi );
			options.addOption( worldFile );
			options.addOption( logFile );
			options.addOption( stateFile );
			
			CommandLineParser parser = new GnuParser();
		    try 
		    {
		        // parse the command line arguments
		        CommandLine line = parser.parse( options, args );
		        
		        String desiredWorldFile = null;
		        String desiredLogFile = null;
		        String desiredStateFile = null;
		        
		        if ( line.hasOption("statefile") ) desiredStateFile = line.getOptionValue("statefile");
		        if ( line.hasOption("logfile") ) desiredLogFile = line.getOptionValue("logfile");
		        if ( line.hasOption("worldfile") ) desiredWorldFile = line.getOptionValue("worldfile");
		        if ( desiredWorldFile == null && line.getArgs().length > 0 ) desiredWorldFile = line.getArgs()[0];
		        boolean desiredSdi = Boolean.valueOf( line.getOptionValue("sdi") ).booleanValue();
		        
		        if ( SwingAetheriaGUI.getInstance() != null )
		        {
		        	//abrir un fichero en una instancia de AGE ya abierta
					System.out.println("Opening file in existing instance...");
					createLocalGameFromFile(desiredWorldFile,true,desiredLogFile!=null,desiredLogFile,desiredStateFile);
					return;
		        }
		        else
		        {
		        	
		        	System.out.println("Working directory: " + Paths.getWorkingDirectory() );
		    		setLookAndFeel();
		    		
		    		if ( !desiredSdi )
		    			new SwingAetheriaGUI();
		        	
		        	createLocalGameFromFile(desiredWorldFile,!desiredSdi,desiredLogFile!=null,desiredLogFile,desiredStateFile);

		        }
		        
		    }
		    catch( ParseException exp ) {
		        // oops, something went wrong
		        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
		    }
			
		    
		}
		else
		{
			setLookAndFeel();
    		new SwingAetheriaGUI();
		}
		

		/*
		if ( args.length > 0 && SwingAetheriaGUI.getInstance() != null )
		{
			//abrir un fichero en una instancia de AGE ya abierta
			System.out.println("Opening file in existing instance...");
			createLocalGameFromFile(args[0]);
			return;
		}
		*/




		//System.out.println("Arguments to main(): " + args.length );
		


		//prueba MIDI
		/*
		try
		{
			javax.sound.midi.Sequencer seqr = javax.sound.midi.MidiSystem.getSequencer();
			javax.sound.midi.Sequence seq = javax.sound.midi.MidiSystem.getSequence ( new java.io.File ( "prueba.mid" ) );
			seqr.open();
			seqr.setSequence(seq);
			//seqr.setTempoFactor((float)20.0);
			//seqr.start();
		}
		catch ( Exception exc )
		{	
			System.out.println(exc);
		}

		//prueba WAV

		try
		{
			javax.sound.sampled.AudioInputStream aii = javax.sound.sampled.AudioSystem.getAudioInputStream ( new java.io.File("prueba.wav") );
			javax.sound.sampled.AudioFormat af = aii.getFormat();
			javax.sound.sampled.Clip cl = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine ( new javax.sound.sampled.DataLine.Info ( javax.sound.sampled.Clip.class , af ) ); 
			cl.open ( aii );
		}
		catch ( Exception exc )
		{
			System.out.println(exc);
		}
		 */



		

		/*
		if ( args.length > 0 )
		{
			System.out.println("Opening file in newly created instance...");
			createLocalGameFromFile(args[0]);
		}
		*/


	}
	
	
	/**
	 * This is a simple game creation function prepared to call from the outside without needing any data except for the game file.
	 * This can be used for the PUCK IDE's "go" button.
	 *
	 */
	public static void createLocalGameFromFile( String file )
	{
		createLocalGameFromFile ( file , true );
		
	}
	
	public static void createLocalGameFromFile ( String file , boolean mdi )
	{
		if ( mdi )
			new SwingAetheriaGameLoader
			( file  , SwingAetheriaGUI.getInstance().panel , false , null , null, false );
		else
			new SwingSDIInterface(file,false,null,null);
	}
	
	public static void createLocalGameFromFile ( String file , boolean mdi , boolean usarLog , String logFile , String stateFile )
	{
		if ( mdi )
			new SwingAetheriaGameLoader
			( file  , SwingAetheriaGUI.getInstance().panel , usarLog , logFile , stateFile , false );
		else
			new SwingSDIInterface(file,usarLog,logFile,stateFile);
	}
	
	
}






class SwingAetheriaGUI extends JFrame
{
	JDesktopPane panel;
	JMenuBar menuBar;
	Vector padre1, padre2;
	int i=0,j=0;

	private static SwingAetheriaGUI instance; //not exactly a singleton, but at least the last instance is always cached (for IDE go button use)

	public static SwingAetheriaGUI getInstance() { return instance; }

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


	public void setStandalone ( boolean standalone )
	{
		if ( standalone == true )
			this.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
		else
			this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
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

		super("Aetheria Game Engine, Beta v0.7");
		instance = this;
		this.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
		Image iconito = this.getToolkit().getImage("images" + File.separatorChar + "intficon.gif");
		this.setIconImage ( iconito );


		Dimension tamanoVentana = this.getToolkit().getScreenSize();
		this.setSize ( tamanoVentana.width - 150 , tamanoVentana.height - 150 );
		this.setLocation ( 70 , 70 );

		this.maximizeIfPossible();

		/*
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		gd.setFullScreenWindow(mdi);
		 */

		SwingImageDrawingThread imthread = new SwingImageDrawingThread("images" + File.separatorChar + "agelogo.jpg" , 4000 , this );
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


		Image logo = this.getToolkit().createImage("agelogo.jpg");
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

class EscuchadorAgregarVentanas implements ActionListener 
{

	JDesktopPane thePanel;

	public EscuchadorAgregarVentanas ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		new SwingAetheriaGameLoader ( "" , thePanel , false , null , null , false );
		thePanel.setVisible(true);
	}
}

class EscuchadorNuevoDesdeFichero implements ActionListener
{
	JDesktopPane thePanel;

	public EscuchadorNuevoDesdeFichero ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		JFileChooser selector = new JFileChooser( Paths.WORLD_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		selector.setDialogTitle("Selecciona el directorio del juego o el fichero world.dat");
		selector.setFileFilter ( new FiltroFicheroMundo() );

		int returnVal = selector.showOpenDialog(thePanel);
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			System.out.println("Nombre: " + selector.getSelectedFile().getAbsolutePath() );

			//if ( selector.getSelectedFile().isFile() )
			//{
			//new SwingAetheriaGameLoader( selector.getSelectedFile().getParent() , thePanel , false ,null , null, true );
			//}
			//else
			//{
			//Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			new SwingAetheriaGameLoader
			(selector.getSelectedFile().getAbsolutePath() , thePanel , false , null , null, false );
			//}
		}
	}

}

class EscuchadorCargarDesdeLog implements ActionListener
{
	JDesktopPane thePanel;

	public EscuchadorCargarDesdeLog ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		final JFileChooser selector = new JFileChooser( Paths.SAVE_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
		selector.setDialogTitle("Selecciona el fichero de log");
		selector.setFileFilter ( new FiltroFicheroLog() ); 

		int returnVal = selector.showOpenDialog(thePanel);

		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			System.out.println("Nombre: " + selector.getSelectedFile().getAbsolutePath() );

			String worldFile;
			try
			{
				FileInputStream fis = new FileInputStream ( selector.getSelectedFile() );
				BufferedReader br = new BufferedReader ( Utility.getBestInputStreamReader ( fis ) );
				//primera linea del fichero de log: fichero de mundo
				worldFile = br.readLine(); 
			}
			catch ( Exception fnfe )
			{
				worldFile = "";
			}

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);


			//test:
			GameInfo gi = GameInfo.getGameInfoFromFile ( new File ( worldFile ) );
			final PartidaEntry pe = new PartidaEntry ( gi , "noname" , 200 , null , true , true , true );

			Thread thr = new Thread()
			{
				public void run()
				{
					ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null ,selector.getSelectedFile().getAbsolutePath() , thePanel );
				}
			};
			thr.start();

			//new SwingAetheriaGameLoader( new File(worldFile).getParent() , thePanel , true , selector.getSelectedFile().getAbsolutePath() , null, true );

		}
	}

}

class EscuchadorCargarDesdeEstado implements ActionListener
{
	JDesktopPane thePanel;

	public EscuchadorCargarDesdeEstado ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		final JFileChooser selector = new JFileChooser( Paths.SAVE_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
		selector.setDialogTitle("Selecciona el fichero de estado");
		selector.setFileFilter ( new FiltroFicheroEstado() ); 

		int returnVal = selector.showOpenDialog(thePanel);

		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			System.out.println("Nombre: " + selector.getSelectedFile().getAbsolutePath() );

			String moduledir;
			try
			{

				//Obtener fichero de módulo.
				//Warning: hacemos todo el árbol XML para obtener una chorrada.
				//LENTÍSIMO.
				//Menos mal que, en el futuro, diff y al c..ajo...

				org.w3c.dom.Document d = null;


				BufferedReader br = new BufferedReader ( new InputStreamReader ( new FileInputStream (  selector.getSelectedFile()  ) , "ISO-8859-1" ) );
				InputSource is = new InputSource(br);
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				//io.escribir(io.getColorCode("information") + "Obteniendo árbol DOM de los datos XML...\n" + io.getColorCode("reset") );
				d = db.parse(is);

				//obtain the DOM tree root
				org.w3c.dom.Element n = d.getDocumentElement();
				//obtain the information
				if ( n.hasAttribute("worldDir") )
					moduledir = n.getAttribute("worldDir");
				else  throw ( new Exception() ) ;			
			}

			catch ( Exception fnfe )
			{
				moduledir = "";
			}

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			//String worldFile = moduledir + "/world.xml"; //TODO Doesn't have to be world.xml now!
			String worldFile = selector.getSelectedFile().getAbsolutePath();

			//test:
			GameInfo gi = GameInfo.getGameInfoFromFile ( new File ( worldFile ) );
			final PartidaEntry pe = new PartidaEntry ( gi , "noname" , 200 , null , true , true , true );

			Thread thr = new Thread() {
				public void run()
				{
					ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() ,selector.getSelectedFile().getAbsolutePath() , null , thePanel );
				}
			};
			thr.start();

			//working:
			//new SwingAetheriaGameLoader( moduledir , thePanel , false , null , selector.getSelectedFile().getAbsolutePath(), true ); //not client is true? yep: don't assign players until second load, stateload (will change w/diff)

		}
	}

}




class EscuchadorMinimizarTodo implements ActionListener
{

	JDesktopPane thePanel;

	public EscuchadorMinimizarTodo ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt ) 
	{
		JInternalFrame[] lasVentanas = thePanel.getAllFrames();
		try
		{
			for ( int i = 0 ; i < lasVentanas.length ; i++ )
				lasVentanas[i].setIcon(true);
		}
		catch ( java.beans.PropertyVetoException excest )
		{
		}
	}

}

class MDIMenuBar extends JMenuBar 
{

	JDesktopPane thePanel;
	SwingAetheriaGUI window;

	public MDIMenuBar ( final JDesktopPane p , SwingAetheriaGUI w )
	{
		thePanel = p;
		window = w;
		JMenu menuArchivo = new JMenu("Archivo");
		JMenuItem itemNuevo = new JMenuItem("Nuevo");
		JMenuItem itemNuevo2 = new JMenuItem("Nuevo juego...");
		JMenuItem itemRemota = new JMenuItem("Conectar a partida remota...");
		JMenuItem itemNuevoLog = new JMenuItem("Cargar partida...");
		JMenuItem itemNuevoEstado = new JMenuItem("Cargar estado...");
		JMenuItem itemLoader = new JMenuItem("Cargador de juegos...");
		JMenuItem itemSalir = new JMenuItem("Salir");
		JMenu menuPresentacion = new JMenu("Presentación");
		JMenuItem itemIconificar = new JMenuItem("Iconificar todo");
		JMenu menuServidor = new JMenu("Servidor");
		JMenuItem itemConfigServidor = new JMenuItem("Configuración...");
		JMenu menuAyuda = new JMenu("Ayuda");
		JMenuItem itemAbout = new JMenuItem("Acerca de AGE...");
		EscuchadorMinimizarTodo escmin = new EscuchadorMinimizarTodo ( thePanel );
		EscuchadorAgregarVentanas esc = new EscuchadorAgregarVentanas ( thePanel );
		EscuchadorNuevoDesdeFichero esc2 = new EscuchadorNuevoDesdeFichero ( thePanel );
		EscuchadorCargarDesdeLog escCargar = new EscuchadorCargarDesdeLog ( thePanel );
		EscuchadorCargarDesdeEstado escCargar2 = new EscuchadorCargarDesdeEstado ( thePanel );
		itemNuevo.addActionListener ( esc );
		itemNuevo2.addActionListener ( esc2 );
		itemNuevoLog.addActionListener ( escCargar );
		itemNuevoEstado.addActionListener ( escCargar2 );
		itemIconificar.addActionListener ( escmin );
		itemSalir.addActionListener ( 

				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						/*
				if ( standalone == true )
					System.exit(0);
				else
					dispose();
						 */
						window.dispose();
					}
				}

		);

		itemLoader.addActionListener (

				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						p.add ( new GameChoosingInternalFrame ( p ) );
					}
				}

		);

		itemAbout.addActionListener (
				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						JInternalFrame ventanaAbout;
						ventanaAbout = new JInternalFrame("Acerca de AGE",true,true,true,true);
						p.add(ventanaAbout);
						ventanaAbout.setSize(500,500);
						//ventanaAbout.getContentPane().add ( new JLabel("Aetheria Game Engine") );

						final JTextPane tpAbout = new JTextPane();
						try
						{
							tpAbout.setPage(new File("doc/help/index.htm").toURL());
						}
						catch ( Exception exc )
						{
							exc.printStackTrace();
						}

						tpAbout.setEditable(false);

						tpAbout.addHyperlinkListener ( new HyperlinkListener ()
						{
							public void hyperlinkUpdate ( HyperlinkEvent evt )
							{
								if ( evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
								{
									try
									{
										System.out.println("Hyperlink Event.\n");
										tpAbout.setPage(evt.getURL());
									}
									catch ( java.io.IOException ioe )
									{
										ioe.printStackTrace();
									}
								}
							}
						} );

						ventanaAbout.getContentPane().add ( tpAbout );

						ventanaAbout.setVisible(true);
					}
				}
		);

		itemConfigServidor.addActionListener (
				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						ServerConfigurationWindow scw = ServerConfigurationWindow.getInstance();
						scw.setVisible(true);
					}
				}
		);

		itemRemota.addActionListener (
				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						SwingRemoteClientWindow srcw = new SwingRemoteClientWindow( thePanel );
						p.add(srcw);

					}
				}
		);



		//menuArchivo.add ( itemNuevo );
		menuArchivo.add ( itemNuevo2 );
		menuArchivo.add ( itemRemota );
		menuArchivo.add ( itemNuevoLog );
		menuArchivo.add ( itemNuevoEstado );
		menuArchivo.add ( itemLoader );
		menuArchivo.add ( new JSeparator() );
		menuArchivo.add ( itemSalir );
		menuPresentacion.add ( itemIconificar );
		menuServidor.add ( itemConfigServidor );
		menuAyuda.add ( itemAbout );
		this.add ( menuArchivo );	
		this.add ( menuPresentacion );
		this.add ( menuServidor );
		this.add ( menuAyuda );
		this.add ( new PluginMenu(thePanel) );
	}
}




