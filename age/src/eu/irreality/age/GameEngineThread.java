/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import javax.swing.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.messages.Messages;
import eu.irreality.age.observer.GameThreadObserver;
import eu.irreality.age.scripting.ScriptException;
import eu.irreality.age.swing.applet.SwingSDIApplet;
import eu.irreality.age.swing.sdi.SwingSDIInterface;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.AGELoggingWindow;
import eu.irreality.age.windowing.MenuMnemonicOnTheFly;

import java.awt.event.*;
import java.util.List;
import java.util.Vector;
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
	
	//LA M�QUINA DE ESTADOS

	/**
	* El contador de tiempo.
	*/
	protected long timeCount;
	/**
	* Si salimos del juego.
	*/
	protected boolean exitFlag;


	World theWorld;
	
	/*�Tiempo real?*/
	boolean realTimeEnabled;
	long realTimeQuantum = DEFAULT_REAL_TIME_QUANTUM;

	public GameEngineThread ( World theWorld  , boolean realTimeEnabled )
	{
		setName(getName()+": AGE Game Engine Thread");
		this.theWorld = theWorld;
		this.realTimeEnabled = realTimeEnabled;
		
		//now done from the outside by attaching a ServerMenuHandler observer:
		//if ( ventana != null )
		//{
		//	initServerMenu(ventana);
		//	ventana.repaint();
		//}
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
		catch (ScriptException bshte)
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
		
		//now this is done when detaching the observers
		/*
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
		*/
		detachAllObservers();
		
		
		exitFlag = true;

		//if there are more deadlock problems with this, we could always create a "world end thread" with the following code (including join and all), and run it here,
		//rather than keep running in the event dispatching thread:
		
		
		//this thread waits for the game engine thread to end, and then clears things like the message cache
		//which cannot be cleared before the game engine thread ends (since the game engine thread would replace
		//the Messages instance in the cache).
		Thread worldEndThread = new Thread("World End Thread")
		{
			public void run()
			{
				try 
				{
					//wait for the world to end
					GameEngineThread.this.join();
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				Messages.clearCache(theWorld);
				theWorld = null;
				//serverConfigurationMenu = null; //the equivalent to this now done by detachAllObservers() above.
				Debug.println("World ended.");
			}
		};
		worldEndThread.start(); //wait for the game engine thread to end, then clean up
		
		/*
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
		*/
		
	}
	
	public void exitForReinit()
	{
		Debug.println("Gonna x-it.");
		exitFlag = true;
		Debug.println("Flag set.");
	}
	
	private List observers = new Vector();
	
	public void attachObserver ( GameThreadObserver obs )
	{
		observers.add(obs);
		obs.onAttach(this);
	}
	
	public boolean hasObserver ( GameThreadObserver obs )
	{
		return observers.contains(this);
	}
	
	public void detachObserver ( GameThreadObserver obs )
	{
		observers.remove(obs);
		obs.onDetach(this);
	}
	
	public void detachAllObservers()
	{
		while ( !observers.isEmpty() )
		{
			detachObserver((GameThreadObserver)observers.get(0));
		}
	}

}
