package eu.irreality.age.swing.newloader;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import eu.irreality.age.FiltroFicheroMundo;
import eu.irreality.age.SwingAetheriaGameLoaderInterface;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.config.AGEConfiguration;
import eu.irreality.age.swing.sdi.SwingSDIInterface;

public class NewLoader extends JFrame 
{
	
	private NewLoaderGamePanel gamePanel;
	
	private JButton loadFromDiskButton;
	private JCheckBox addLoadedGameCheckBox = new JCheckBox(UIMessages.getInstance().getMessage("gameloader.addonload"),true);

	private static Border addSpaceToBorder ( Border b )
	{
		Border b1 = new CompoundBorder ( BorderFactory.createEmptyBorder(5,5,5,5) , b );
		Border b2 = new CompoundBorder ( b1, BorderFactory.createEmptyBorder(5,5,5,5) );
		return b2;
	}
	
	public NewLoader()
	{
		super(UIMessages.getInstance().getMessage("gameloader.title"));
		//setSize(400,400);
		getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.PAGE_AXIS));
		
		//the panel with the game catalog
		gamePanel = new NewLoaderGamePanel(this);
		TitledBorder catalogBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"  " + UIMessages.getInstance().getMessage("gameloader.catalog") + "  ");
		catalogBorder.setTitleJustification(TitledBorder.CENTER);
		gamePanel.setBorder( addSpaceToBorder( catalogBorder ) );
		getContentPane().add(Box.createVerticalStrut(8));
		//getContentPane().add(new JLabel(UIMessages.getInstance().getMessage("gameloader.catalog")+":"));
		getContentPane().add(gamePanel);
		//gamePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		
		JPanel fromDiskPanel = new JPanel();
		fromDiskPanel.setLayout(new BoxLayout(fromDiskPanel,BoxLayout.LINE_AXIS));
		fromDiskPanel.add ( Box.createHorizontalGlue() );
		fromDiskPanel.add( new JLabel( UIMessages.getInstance().getMessage("gameloader.fromdisk") ) );
		loadFromDiskButton = new JButton( UIMessages.getInstance().getMessage("gameloader.browse") );
		loadFromDiskButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				//SwingSDIInterface.main( new String[0] );
				SwingAetheriaGameLoaderInterface.loadFont();
				JFileChooser selector = new JFileChooser( Paths.WORLD_PATH );
				selector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				selector.setDialogTitle( UIMessages.getInstance().getMessage("dialog.new.title") );
				selector.setFileFilter ( new FiltroFicheroMundo() );
				int returnVal = selector.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) 
				{
					if ( addLoadedGameCheckBox.isSelected() )
					{
						GameEntry ge = new GameEntry();
						boolean success = ge.obtainFromWorld(selector.getSelectedFile().getAbsolutePath());
						ge.setDownloaded(true);
						if ( success ) gamePanel.addGameEntry(ge,false);
					}
					new SwingSDIInterface(selector.getSelectedFile().getAbsolutePath(),false,null,null);
					
				}
			}
		});
		//TODO: Option to add the game loaded from disk to the catalog: a checkbox + a method to read a world XML and create a catalog entry (even w/o remote url)
		fromDiskPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		fromDiskPanel.add(Box.createRigidArea(new Dimension(5,5)));
		fromDiskPanel.add(loadFromDiskButton);
		fromDiskPanel.add(addLoadedGameCheckBox);
		
		//getContentPane().add(new JSeparator());
		
		
		getContentPane().add(fromDiskPanel);
		
		loadWindowCoordinates(); //if no coordinates stored, this does pack() and setLocationRelativeTo(null).
		setVisible(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener ( new WindowAdapter()
		{
			public void windowClosing ( WindowEvent e )
			{
				saveWindowCoordinates();
				gamePanel.writeData();
				NewLoader.this.dispose();
			}
		}
		);
		JOptionPane.showMessageDialog(this, UIMessages.getInstance().getMessage("gameloader.beta.message") , UIMessages.getInstance().getMessage("gameloader.beta.title") , JOptionPane.INFORMATION_MESSAGE );
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
		}
		//{f.getParentFile().exists()
		try 
		{
			System.setErr ( new PrintStream ( new FileOutputStream(f,true) ) );
			System.err.println("[" + new Date() + "]");
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("Could not redirect standard error to " + file + ":");
			e.printStackTrace();
		}
	}
	
	
	public static void main ( String[] args )
	{
		
		if ( args.length > 0 )
		{
			//parse command line
			Option errorLog = OptionBuilder.withArgName( "errorlog" )
		            .hasArg()
		            .withDescription(  "A file to append the error output to" )
		            .withLongOpt( "errorlog" )
		            .create( "e" );
			
			Options options = new Options();
			options.addOption( errorLog );
			CommandLineParser parser = new GnuParser();
			
			 try 
			 {
			        // parse the command line arguments
			        CommandLine line = parser.parse( options, args );
			        
			        String errorLogFile = null;
			        
			        if ( line.hasOption("e") ) errorLogFile = line.getOptionValue("e");
			        
			        if ( errorLogFile != null ) redirectStandardError(errorLogFile);
			 }
			 catch( ParseException exp ) 
			 {
			        // oops, something went wrong
			        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
			 }
				
		}
		
		
		
		SwingUtilities.invokeLater ( new Runnable()
		{
			public void run() 
			{
				new NewLoader();	
			}	
		});
		
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
				AGEConfiguration.getInstance().setProperty("loaderWindowWidth",String.valueOf(this.getWidth()));
				AGEConfiguration.getInstance().setProperty("loaderWindowHeight",String.valueOf(this.getHeight()));
				AGEConfiguration.getInstance().setProperty("loaderWindowMaximized","false");
				AGEConfiguration.getInstance().setProperty("loaderWindowLocationX",String.valueOf(this.getX()));
				AGEConfiguration.getInstance().setProperty("loaderWindowLocationY",String.valueOf(this.getY()));
			}
			else
			{
				AGEConfiguration.getInstance().setProperty("loaderWindowMaximized","true");
			};
			AGEConfiguration.getInstance().storeProperties();
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	public void loadWindowCoordinates()
	{
		int w = AGEConfiguration.getInstance().getIntegerProperty("loaderWindowWidth");
		int h = AGEConfiguration.getInstance().getIntegerProperty("loaderWindowHeight");
		if ( w != 0 && h != 0 )
			setSize(w,h);
		else
			pack();
		int x = AGEConfiguration.getInstance().getIntegerProperty("loaderWindowLocationX");
		int y = AGEConfiguration.getInstance().getIntegerProperty("loaderWindowLocationY");
		if ( x != 0 && y != 0 )
			setLocation(x,y);
		else
			setLocationRelativeTo(null);
		if ( AGEConfiguration.getInstance().getBooleanProperty("loaderWindowMaximized") )
			maximizeIfPossible();
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
	
}
