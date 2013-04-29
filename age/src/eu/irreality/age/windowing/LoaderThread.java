package eu.irreality.age.windowing;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import eu.irreality.age.AGESoundClient;
import eu.irreality.age.ColoredSwingClient;
import eu.irreality.age.GameEngineThread;
import eu.irreality.age.World;
import eu.irreality.age.filemanagement.WorldLoader;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.CommonSwingFunctions;
import eu.irreality.age.swing.menu.ServerMenuHandler;
import eu.irreality.age.swing.sdi.SwingSDIInterface;
import eu.irreality.age.util.VersionComparator;

public class LoaderThread extends Thread
{

	private String moduledir;
	private boolean usarLog;
	private String logFile;
	private String stateFile;
	private boolean noSerCliente;
	
	private Object mundoSemaphore;
	
	private AGEClientWindow window;

	public LoaderThread ( String moduledir, boolean usarLog, String logFile, String stateFile, boolean noSerCliente, AGEClientWindow window , Object mundoSemaphore ) 
	{
		super("Loader Thread: " + moduledir);
		this.moduledir = moduledir;
		this.usarLog = usarLog;
		this.logFile = logFile;
		this.stateFile = stateFile;
		this.noSerCliente = noSerCliente;
		this.window = window;
		this.mundoSemaphore = mundoSemaphore;
	}
		
	public void run()
	{
		
		final ColoredSwingClient io;
		final Vector gameLog;
		
		try
		{
			SwingUtilities.invokeAndWait 
			( 
					new Runnable()
					{
						public void run()
						{

							window.initClient();
							if ( logFile != null )
							{
								((ColoredSwingClient)window.getIO()).hideForLogLoad();
								if ( ((ColoredSwingClient)window.getIO()).getSoundClient() instanceof AGESoundClient )
								{
									AGESoundClient asc = (AGESoundClient) ((ColoredSwingClient)window.getIO()).getSoundClient();
									asc.deactivate(); //will be activated on log end (player:endOfLog()
								}
							}
							
							CommonSwingFunctions.writeIntroductoryInfo(window);

						}
					}
			);
		}
		catch ( Exception e )
		{
			if ( window.getIO() != null ) ((ColoredSwingClient)window.getIO()).showAfterLogLoad();
			e.printStackTrace();
		}
		
		io = (ColoredSwingClient) window.getIO();
		gameLog = window.getGameLog();

		//System.out.println("2");

		String worldName;
		World theWorld = null;

		if ( moduledir == null || moduledir.length() == 0 ) moduledir="aetherworld";


		try
		{
			SwingUtilities.invokeAndWait 
			( 
					new Runnable()
					{
						public void run()
						{
							window.repaint();
							window.updateNow();
						}
					}
			);
		}
		catch ( Exception e )
		{
			((ColoredSwingClient)io).showAfterLogLoad();
			e.printStackTrace();
		}

		//System.out.println("3");
		
		try
		{
			theWorld = WorldLoader.loadWorld( moduledir , gameLog, io, mundoSemaphore );
		}
		catch ( Exception e )
		/*
		 * This shouldn't happen, because unchecked exceptions in world initialization scripts are caught before reaching this level,
		 * and the loadWorld method doesn't throw its own exceptions (it returns null if the world cannot be loaded). But it's defensive
		 * programming in case AGE forgets to catch some unchecked exception, which has happened in the past.
		 */
		{
			if ( io != null ) ((ColoredSwingClient)io).showAfterLogLoad();
			if ( io != null ) window.write ( "Exception on loading world: " + e );
			e.printStackTrace();
		}
		if ( theWorld == null || io.isDisconnected() ) //io could be disconnected due to closing the window before assigning player 
		{
			((ColoredSwingClient)io).showAfterLogLoad();
			return;
		}
		window.setWorld(theWorld);

		//{theWorld NOT null}

		final World theFinalWorld = theWorld;


		try
		{
			SwingUtilities.invokeAndWait 
			( 
					new Runnable()
					{
						public void run()
						{

							window.updateNow();

							//atender telnet
							//SimpleTelnetClientHandler stch = new SimpleTelnetClientHandler ( theWorld , 6 , (short)8010 );
							//No. Dar medios para meterla en partidas dedicadas en forma de PartidaEnCurso.

							if ( theFinalWorld.getModuleName() != null && theFinalWorld.getModuleName().length() > 0 )
								window.setTitle(theFinalWorld.getModuleName());
						}
					}
			);

		}
		catch ( Exception e )
		{
			((ColoredSwingClient)io).showAfterLogLoad();
			e.printStackTrace();
		}
		
		if ( new VersionComparator().compare(GameEngineThread.getVersionNumber(),theWorld.getRequiredAGEVersion()) < 0 )
		{
			String mess = UIMessages.getInstance().getMessage("age.version.warning",
					"$curversion",GameEngineThread.getVersionNumber(),"$reqversion",theWorld.getRequiredAGEVersion(),
					"$world",theWorld.getModuleName());
			mess = mess + " " + UIMessages.getInstance().getMessage("age.download.url");
			JOptionPane.showMessageDialog( (Component)window , mess, UIMessages.getInstance().getMessage("age.version.warning.title"), JOptionPane.WARNING_MESSAGE);
		}

		//usar estado si lo hay
		if ( stateFile != null )
		{
			try
			{
				theWorld.loadState ( stateFile );
			}
			catch ( Exception exc )
			{
				((ColoredSwingClient)io).showAfterLogLoad();
				window.write(UIMessages.getInstance().getMessage("swing.cannot.read.state","$file",stateFile));
				window.write(exc.toString());
				exc.printStackTrace();
			}
		}


		if ( usarLog )
		{
			try
			{
				theWorld.prepareLog(logFile);
				theWorld.setRandomNumberSeed( logFile );
			}
			catch ( Exception exc )
			{
				((ColoredSwingClient)io).showAfterLogLoad();
				window.write(UIMessages.getInstance().getMessage("swing.cannot.read.log","$exc",exc.toString()));
				exc.printStackTrace();
				return;
			}
		}
		else
		{
			theWorld.setRandomNumberSeed();
		}
		gameLog.addElement(String.valueOf(theWorld.getRandomNumberSeed())); //segunda línea, semilla

		//TODO use invoke method for this to avoid deadlocks:
		try 
		{
			SwingUtilities.invokeAndWait( new Runnable() { public void run() { window.setVisible(true); } } );
		} 
		catch (InvocationTargetException e1) 
		{
			e1.printStackTrace();
		} 
		catch (InterruptedException e1) 
		{
			e1.printStackTrace();
		}
		

		window.setWorld(theWorld);
		synchronized ( mundoSemaphore )
		{
			mundoSemaphore.notifyAll();
		}

		GameEngineThread maquinaEstados =
			new GameEngineThread ( 
					theWorld, false );
		
		window.setEngineThread(maquinaEstados);
		
		maquinaEstados.attachObserver(window);
		maquinaEstados.attachObserver(new ServerMenuHandler(window));

		//System.out.println("STARTING ENGINE THREAD");

		maquinaEstados.start();		

		//System.out.println("ENGINE THREAD STARTED");

		//Esto engaña con los estados, lo quitamos.
		/*
					if (noSerCliente)
						write("Este mundo se está ejecutando en modo Dedicado. Por eso no ves nada aquí: no eres jugador.");
		 */	

		try
		{
			SwingUtilities.invokeAndWait 
			( 
					new Runnable()
					{
						public void run()
						{
							window.repaint();
							window.updateNow();
							//setVisible(false);
							//setVisible(true);

						}
					}
			);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		if ( io instanceof ColoredSwingClient )
			((ColoredSwingClient)io).refreshFocus();


		
		
		
	}
	
	
	
	
}
