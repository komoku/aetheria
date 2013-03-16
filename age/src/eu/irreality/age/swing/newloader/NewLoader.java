package eu.irreality.age.swing.newloader;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import eu.irreality.age.SwingAetheriaGameLoaderInterface;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.config.AGEConfiguration;
import eu.irreality.age.swing.sdi.SwingSDIInterface;

public class NewLoader extends JFrame 
{
	
	private NewLoaderGamePanel gamePanel;
	
	private JButton loadFromDiskButton;

	public NewLoader()
	{
		super(UIMessages.getInstance().getMessage("gameloader.title"));
		//setSize(400,400);
		getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.PAGE_AXIS));
		
		//the panel with the game catalog
		getContentPane().add(gamePanel = new NewLoaderGamePanel());
		
		JPanel fromDiskPanel = new JPanel();
		fromDiskPanel.setLayout(new BoxLayout(fromDiskPanel,BoxLayout.LINE_AXIS));
		fromDiskPanel.add ( Box.createHorizontalGlue() );
		fromDiskPanel.add( new JLabel( UIMessages.getInstance().getMessage("gameloader.fromdisk") ) );
		loadFromDiskButton = new JButton( UIMessages.getInstance().getMessage("gameloader.browse") );
		loadFromDiskButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				SwingSDIInterface.main( new String[0] );
			}
		});
		//TODO: Option to add the game loaded from disk to the catalog: a checkbox + a method to read a world XML and create a catalog entry (even w/o remote url)
		fromDiskPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		fromDiskPanel.add(Box.createRigidArea(new Dimension(5,5)));
		fromDiskPanel.add(loadFromDiskButton);
		getContentPane().add(new JSeparator());
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
	
	public static void main ( String[] args )
	{
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
