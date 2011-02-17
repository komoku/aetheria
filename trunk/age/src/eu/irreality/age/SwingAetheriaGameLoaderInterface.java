/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;

import javax.imageio.ImageIO;
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
import eu.irreality.age.server.ServerHandler;
import eu.irreality.age.server.ServerLogWindow;
import eu.irreality.age.swing.mdi.MDIMenuBar;
import eu.irreality.age.swing.mdi.SwingAetheriaGUI;
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
		catch ( SecurityException se ) //applet mode
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
			SwingAetheriaGUI.getInstance().removeWindowListener ( SwingAetheriaGUI.nonStandaloneWindowListener );
			SwingAetheriaGUI.getInstance().addWindowListener ( SwingAetheriaGUI.standaloneWindowListener );

		}
		else
		{
			//obsolete
			SwingAetheriaGUI.getInstance().setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
			//this:
			SwingAetheriaGUI.getInstance().removeWindowListener ( SwingAetheriaGUI.standaloneWindowListener );
			SwingAetheriaGUI.getInstance().addWindowListener ( SwingAetheriaGUI.nonStandaloneWindowListener );
		}
	}

	public static void showIfAlreadyOpen()
	{
		if ( SwingAetheriaGUI.getInstance() != null )
		{
			System.err.println("Showing previous instance...");
			SwingAetheriaGUI.getInstance().setVisible(true);
			System.err.flush();
		}
	}
	
	public static void setLookAndFeel ( )
	{
		try
		{

			boolean setLAF = true;

			
			setLAF = false; //pasamos del native look and feel.
			
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
					
					/*
					if ( firstNum <= 1 && secondNum <= 5 )
						setLAF = false;
					*/
					//don't use GTK l&f at all
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
            .withDescription(  "The world file or URL to play" )
            .create( "worldfile" );
			/*
			Option worldUrl = OptionBuilder.withArgName( "url" )
            .hasArg()
            .withDescription(  "The world URL to play" )
            .create( "worldurl" );	*/
			Option logFile   = OptionBuilder.withArgName( "file" )
            .hasArg()
            .withDescription(  "Log file to load the game from (requires a world file)" )
            .create( "logfile" );
			Option stateFile   = OptionBuilder.withArgName( "file" )
            .hasArg()
            .withDescription(  "State file to load the game from (requires a world file)" )
            .create( "statefile" );
			Option errorLog = OptionBuilder.withArgName( "errorlog" )
            .hasArg()
            .withDescription(  "A file to append the error output to" )
            .create( "errorlog" );
			
			Options options = new Options();

			options.addOption( sdi );
			options.addOption( worldFile );
			//options.addOption( worldUrl );
			options.addOption( logFile );
			options.addOption( stateFile );
			options.addOption( errorLog );
			
			CommandLineParser parser = new GnuParser();
		    try 
		    {
		        // parse the command line arguments
		        CommandLine line = parser.parse( options, args );
		        
		        String desiredWorldFile = null;
		        //String desiredWorldUrl = null;
		        String desiredLogFile = null;
		        String desiredStateFile = null;
		        
		        String errorLogFile = null;
		        
		        if ( line.hasOption("errorlog") ) errorLogFile = line.getOptionValue("errorlog");
		        
		        if ( line.hasOption("statefile") ) desiredStateFile = line.getOptionValue("statefile");
		        if ( line.hasOption("logfile") ) desiredLogFile = line.getOptionValue("logfile");
		        if ( line.hasOption("worldfile") ) desiredWorldFile = line.getOptionValue("worldfile");
		        
		        //first, redirect std. error if necessary
		        redirectStandardError(errorLogFile);
		        
		        //if ( line.hasOption("worldurl") ) desiredWorldFile = line.getOptionValue("worldurl");
		        if ( desiredWorldFile == null /*&& desiredWorldUrl == null*/ && line.getArgs().length > 0 ) desiredWorldFile = line.getArgs()[0];
		        boolean desiredSdi = line.hasOption("sdi");  //Boolean.valueOf( line.getOptionValue("sdi") ).booleanValue();	    
		        
		        if ( SwingAetheriaGUI.getInstance() != null && !desiredSdi )
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
		

		/*
		if ( args.length > 0 )
		{
			System.out.println("Opening file in newly created instance...");
			createLocalGameFromFile(args[0]);
		}
		*/


	}
	
	public static void redirectStandardError ( String file )
	{
		File f = new File(file);
		if ( !f.exists() )
		{
			if ( !f.getParentFile().exists() )
			{
				if ( !f.getParentFile().mkdirs() )
				{
					System.err.println("Could not redirect standard error to " + file + ": unable to create directories.");
					return;
				}
			}
			//{f.getParentFile().exists()
			try 
			{
				System.setErr ( new PrintStream ( new FileOutputStream(f) ) );
			} 
			catch (FileNotFoundException e) 
			{
				System.err.println("Could not redirect standard error to " + file + ":");
				e.printStackTrace();
			}
			
		}
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
			( file  , SwingAetheriaGUI.getInstance().getPanel() , false , null , null, false );
		else
			new SwingSDIInterface(file,false,null,null);
	}
	
	public static void createLocalGameFromFile ( String file , boolean mdi , boolean usarLog , String logFile , String stateFile )
	{
		if ( mdi )
			new SwingAetheriaGameLoader
			( file  , SwingAetheriaGUI.getInstance().getPanel() , usarLog , logFile , stateFile , false );
		else
		{
		    SwingAetheriaGameLoaderInterface.loadFont();
		    new SwingSDIInterface(file,usarLog,logFile,stateFile);
		}
	}
	
	
}

