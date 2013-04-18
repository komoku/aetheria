package eu.irreality.age.swing.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import eu.irreality.age.GameEngineThread;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.observer.GameThreadObserver;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.AGELoggingWindow;
import eu.irreality.age.windowing.MenuMnemonicOnTheFly;

/**
 * Handles the AGE server menu for a given AGE window and engine thread.
 * @author carlos
 *
 */
public class ServerMenuHandler implements GameThreadObserver
{
	
	private AGELoggingWindow window;
	
	private GameEngineThread thread;
	
	/**
	 * The server menu.
	 */
	private JMenu serverConfigurationMenu;
	
	public ServerMenuHandler ( AGELoggingWindow window )
	{
		this.window = window;
	}

	/**
	 * Initializes the server menu. Can be called from the event dispatch thread or from any other thread (in the latter
	 * case, it uses invokeAndWait()).
	 */
	public void initServerMenu ( )
	{
		if ( SwingUtilities.isEventDispatchThread() )
			doInitServerMenu( );
		else
		{
			try
			{
				SwingUtilities.invokeAndWait 
				( 
						new Runnable()
						{
							public void run()
							{
								doInitServerMenu( );
								window.repaint();
							}
						}
				);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Removes the server menu from the window. Can be invoked from the event dispatch thread or from any other thread.
	 */
	public void uninitServerMenu ( )
	{
		if ( SwingUtilities.isEventDispatchThread() )
			doUninitServerMenu();
		else
		{
			try
			{
				SwingUtilities.invokeAndWait 
				( 
						new Runnable()
						{
							public void run()
							{
								doUninitServerMenu();
								window.repaint();
							}
						}
				);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	private void doInitServerMenu ( )
	{
		doInitServerMenu( UIMessages.getInstance().getMessage("servermenu.gameoptions") );
	}
	
	private void setJMenuBarAsNeeded ( final AGELoggingWindow window , JMenuBar mb )
	{
	    /*
		if ( window instanceof SwingAetheriaGameLoader )
			((SwingAetheriaGameLoader)window).setTheJMenuBar(mb);
		else
			window.setJMenuBar(mb);
			*/
	    window.setTheJMenuBar(mb);
	}
	
	private JMenuBar getJMenuBarAsNeeded ( final AGELoggingWindow window )
	{
	    /*
		if ( window instanceof SwingAetheriaGameLoader )
			return ((SwingAetheriaGameLoader)window).getTheJMenuBar();
		else
			return window.getJMenuBar();
			*/
	    return window.getTheJMenuBar();
	}
	
	private void doUninitServerMenu ( )
	{
		JMenuBar menubar = window.getTheJMenuBar();
		menubar.remove(serverConfigurationMenu);
		serverConfigurationMenu = null;
	}
		
	private void doInitServerMenu ( String menuName )
	{
		serverConfigurationMenu = new JMenu( menuName );
		JMenu timeConfigurationMenu = new JMenu( UIMessages.getInstance().getMessage("servermenu.gamemode") );
		JMenuBar mb = getJMenuBarAsNeeded(window);
		if ( mb == null )
			setJMenuBarAsNeeded ( window , mb = new JMenuBar() );
		serverConfigurationMenu.add ( timeConfigurationMenu );
		JRadioButtonMenuItem itemTurns = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("servermenu.gamemode.sync"),true);
		JRadioButtonMenuItem itemRealTime = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("servermenu.gamemode.async"),false);
		ButtonGroup bg = new ButtonGroup();
		bg.add ( itemTurns );
		bg.add ( itemRealTime );
		if ( thread.isRealTimeEnabled() ) itemRealTime.setSelected(true);
		itemTurns.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				thread.setRealTimeEnabled(false);
			}
		} );
		itemRealTime.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				thread.setRealTimeEnabled(true);
				//setRealTimeQuantum(DEFAULT_REAL_TIME_QUANTUM);
			}
		} );
		timeConfigurationMenu.add ( itemTurns );
		timeConfigurationMenu.add ( itemRealTime );
		if ( window instanceof AGEClientWindow )
		{
			JMenuItem reinitItem = new JMenuItem( UIMessages.getInstance().getMessage("servermenu.restart") );
			reinitItem.addActionListener ( new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					((AGEClientWindow)window).reinit();
				}
			} );
			serverConfigurationMenu.add(reinitItem);
		}
		mb.add ( serverConfigurationMenu );
		MenuMnemonicOnTheFly.setMnemonics(mb);
		mb.revalidate();
		window.repaint();
	}

	public void onAttach(GameEngineThread thread) 
	{
		this.thread = thread;
		initServerMenu();
	}

	public void onDetach(GameEngineThread thread) 
	{
		uninitServerMenu();
	}
	
	
	
	
}
