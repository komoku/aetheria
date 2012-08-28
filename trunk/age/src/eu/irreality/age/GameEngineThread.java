/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import javax.swing.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.messages.Messages;
import eu.irreality.age.swing.applet.SwingSDIApplet;
import eu.irreality.age.swing.sdi.SwingSDIInterface;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.AGELoggingWindow;
import eu.irreality.age.windowing.MenuMnemonicOnTheFly;

import java.awt.event.*;
public class GameEngineThread extends Thread
{

	public static String getVersion() 
	{ 
		return "Aetheria Game Engine v " + getVersionNumber();
	}
	
	public static String getVersionNumber()
	{
		return UIMessages.getInstance().getMessage("age.version");
	}

	
	public static long DEFAULT_REAL_TIME_QUANTUM = 1500;
	
	//LA MÁQUINA DE ESTADOS

	/**
	* El contador de tiempo.
	*/
	protected long timeCount;
	/**
	* Si salimos del juego.
	*/
	protected boolean exitFlag;


	World theWorld;
	AGELoggingWindow ventana;
	
	/*¿Tiempo real?*/
	boolean realTimeEnabled;
	long realTimeQuantum = DEFAULT_REAL_TIME_QUANTUM;

	public GameEngineThread ( World theWorld , AGELoggingWindow ventana , boolean realTimeEnabled )
	{
		this.theWorld = theWorld;
		this.ventana = ventana;
		this.realTimeEnabled = realTimeEnabled;
		if ( ventana != null )
		{
			initServerMenu(ventana);
			ventana.repaint();
		}
	}
	
	public void setRealTimeQuantum ( long quantum )
	{
		realTimeQuantum = quantum;
	}
	
	public long getRealTimeQuantum ( )
	{
		return realTimeQuantum;
	}
	
	public boolean isRealTimeEnabled ( )
	{
		return realTimeEnabled;
	}
	
	public void setRealTimeEnabled ( boolean b )
	{
		realTimeEnabled = b;
	}
	
	public void initServerMenu ( final AGELoggingWindow window )
	{
		if ( SwingUtilities.isEventDispatchThread() )
			doInitServerMenu(window);
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
								doInitServerMenu(window);
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
	
	public void uninitServerMenu ( final AGELoggingWindow window )
	{
		if ( SwingUtilities.isEventDispatchThread() )
			doUninitServerMenu(window);
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
								doUninitServerMenu(window);
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
	
	public void doInitServerMenu ( final AGELoggingWindow window )
	{
		doInitServerMenu( window, UIMessages.getInstance().getMessage("servermenu.gameoptions") );
	}
	
	private JMenu serverConfigurationMenu;
	
	public void doUninitServerMenu ( final AGELoggingWindow window )
	{
		JMenuBar menubar = window.getTheJMenuBar();
		menubar.remove(serverConfigurationMenu);
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
	
	public void doInitServerMenu ( final AGELoggingWindow window , String menuName )
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
		if ( realTimeEnabled ) itemRealTime.setSelected(true);
		itemTurns.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				setRealTimeEnabled(false);
			}
		} );
		itemRealTime.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				setRealTimeEnabled(true);
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
	
	public void run ( )	
	{	
		int timeCount=0;
		exitFlag = false;
		
		//exec general server intro
		try
		{
			//System.err.println("Execcan serverintro for world " + theWorld);
			theWorld.execCode("serverintro",""); //EVA
			//System.err.println("Just singing in the rain...");
			theWorld.execCode("serverintro",new Object[0]); //bsh
			//System.err.println("BSH script code should have been exec'd just before THIS LINE");
		}
		catch (EVASemanticException esm) //EVA
		{
			theWorld.write("EVASemanticException found at serverintro routine" );
		}
		catch (bsh.TargetError bshte)
		{
			theWorld.write("bsh.TargetError found at serverintro routine" );
			theWorld.write ( bshte.printTargetError(bshte) + bshte.inNativeCode() );
			Debug.println ( bshte.printTargetError(bshte.getTarget()) );
			theWorld.write ( bshte.printTargetError(bshte.getTarget()) );
			//bshte.printStackTrace();
			//System.err.println("BINGO!!1");
		}
		catch ( Exception e )
		{
		    //theWorld.write("The followan has been thrown: " + e);
		    theWorld.writeError("Exception thrown by serverIntro routine:");
			theWorld.writeError(ExceptionPrinter.getExceptionReport(e));
		}
		finally
		{
			Debug.println("Gonna notify.");
			synchronized(theWorld.serverIntroSyncObject)
			{
				theWorld.serverIntroExeccedFlag=true;
				theWorld.serverIntroSyncObject.notifyAll();
			}
			Debug.println("Notified.");
		}
		
		//exec player intro foreach player
		//try
		//{
			Debug.println("Gonna exec player intros.");
			java.util.List l = theWorld.getPlayerList();
			Debug.println("List gotten: " + l);
			if ( l != null )
			{
				for ( int i = 0 ; i < l.size() ; i++ )
				{
					Debug.println("Intro " + i);
					Player p = (Player)l.get(i);
					/*
					theWorld.execCode("intro",""); //EVA (obsolete)
					theWorld.execCode("intro", new Object[] {p});
					*/
					theWorld.executePlayerIntro(p);
				}
			}
			Debug.println("Player intros execced.");
		//}
		/*
		catch (EVASemanticException esm) //EVA
		{
			theWorld.write("EVASemanticException found at intro routine" );
		}
		catch (bsh.TargetError bshte)
		{
			theWorld.write("bsh.TargetError found at intro routine" );
			bshte.printStackTrace();
		}
		*/
		
		while ( !exitFlag )
		{
			timeCount++;
			Debug.println("A world cycle.");
			
			try
			{
				//System.err.println("Going to update.");
				theWorld.update();
			}
			catch ( Exception e )
			{
				//the world creator messed something up, probably!
				if ( theWorld != null ) theWorld.writeError(""+e);
				System.err.println("Exception during world update:");
				e.printStackTrace();	
				theWorld.writeError(ExceptionPrinter.getExceptionReport(e));
			}
			
			if ( exitFlag ) 
			{
				//System.err.println("breakin'");
				break;
			}
				
			if ( realTimeEnabled && !theWorld.isLoadingLog() )
			{
				esperarCuanto();
			}
			
			if ( exitFlag ) break;
			
			//delete this?
			if ( theWorld.getNumberOfConnectedPlayers() <= 0 )
			{
				try
				{
					sleep(2000);
				}
				catch ( InterruptedException ie )
				{
					;
				}
			}
			
			Debug.println("Flag = " + exitFlag);
			
		}
		//exitNow();
		
	}
	
	public synchronized void esperarCuanto()
	{
				try
				{
					wait ( realTimeQuantum );
				}
				catch ( InterruptedException intex )
				{
					System.err.println(intex);
				}
	}

	public void exitNow ( )
	{
		Debug.println("Gonna x-it.");
		if ( ventana instanceof SwingAetheriaGameLoader )
		{
			((SwingAetheriaGameLoader)ventana).unlinkWorld(); //we are client and server so we can do this
			((SwingAetheriaGameLoader)ventana).saveAndFreeResources();
		}
		if ( ventana instanceof SwingSDIInterface )
		{
			((SwingSDIInterface)ventana).unlinkWorld(); //we are client and server so we can do this
			((SwingSDIInterface)ventana).saveAndFreeResources();
		}
		if ( ventana instanceof SwingSDIApplet )
		{
			((SwingSDIApplet)ventana).unlinkWorld(); //we are client and server so we can do this
			((SwingSDIApplet)ventana).saveAndFreeResources();
		}
		exitFlag = true;

		//if there are more deadlock problems with this, we could always create a "world end thread" with the following code (including join and all), and run it here,
		//rather than keep running in the event dispatching thread:
		try 
		{
			//wait for the world to end
			this.join();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		//System.err.println("BEFORE");
		//Messages.printReport();
		Messages.clearCache(theWorld);
		//System.err.println("Cache cleared for world " + theWorld + ".\n");
		//System.err.println("AFTER");
		//Messages.printReport();
		theWorld = null;
		ventana = null;
		serverConfigurationMenu = null;
		Debug.println("Flag set.");
	}
	
	public void exitForReinit()
	{
		Debug.println("Gonna x-it.");
		exitFlag = true;
		Debug.println("Flag set.");
	}

}
