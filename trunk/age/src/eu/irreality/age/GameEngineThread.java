/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import javax.swing.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.swing.applet.SwingSDIApplet;
import eu.irreality.age.swing.sdi.SwingSDIInterface;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.AGELoggingWindow;

import java.awt.event.*;
public class GameEngineThread extends Thread
{

	public static String getVersion() 
	{ 
		return "Aetheria Game Engine v 1.0";
	}

	
	public static long DEFAULT_REAL_TIME_QUANTUM = 1500;
	
	//LA MÁQUINA DE ESTADOS

	/**
	* El contador de tiempo.
	*/
	protected static long timeCount;
	/**
	* Si salimos del juego.
	*/
	protected static boolean exitFlag;


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
		doInitServerMenu(window,"Opciones de juego");
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
		JMenu timeConfigurationMenu = new JMenu("Modo de juego");
		JMenuBar mb = getJMenuBarAsNeeded(window);
		if ( mb == null )
			setJMenuBarAsNeeded ( window , mb = new JMenuBar() );
		serverConfigurationMenu.add ( timeConfigurationMenu );
		JRadioButtonMenuItem itemTurns = new JRadioButtonMenuItem("Turnos",true);
		JRadioButtonMenuItem itemRealTime = new JRadioButtonMenuItem("Tiempo real",false);
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
			JMenuItem reinitItem = new JMenuItem("Reiniciar juego");
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
				
			if ( realTimeEnabled )
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
