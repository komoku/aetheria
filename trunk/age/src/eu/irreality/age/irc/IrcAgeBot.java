/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.irc;

import java.util.List;
import java.util.Vector;

import eu.irreality.age.World;
import eu.irreality.age.debug.Debug;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.server.ServerHandler;

public class IrcAgeBot extends IrcBot
{

	public IrcAgeBot ( String server , int port , String nick , List channels ) throws Exception
	{
	
		super ( server , port , nick, UIMessages.getInstance().getMessage("irc.bot.longname") , channels );
	
	}

	public void privateMsg ( String sender , String message )
	{
	
		if ( message.equalsIgnoreCase("DCC") )
		{
			IrcDccChatGameSelector idcsh = new IrcDccChatGameSelector (  );
			try
			{
				IrcDccChatSocket idcs = ircSocket.sendDccChatRequest ( sender , (short)0 , idcsh );
				idcs.setPriority(Thread.MIN_PRIORITY);
				idcsh.setSocket ( idcs );
				launchGame ( idcsh );
			}
			catch ( java.io.IOException ioe )
			{
				System.err.println("IO Exc.");
			}
		}		
		else if (!(sender.equals("NiCK")|sender.equals("agenda")|sender.equals("MeMo")|sender.equalsIgnoreCase("information")))
		{	
			ircSocket.sendPrivate(sender,UIMessages.getInstance().getMessage("irc.bot.intro.1"));
			ircSocket.sendPrivate(sender,
					UIMessages.getInstance().getMessage("irc.bot.intro.2"));
			ircSocket.sendPrivate(sender,UIMessages.getInstance().getMessage("irc.bot.intro.3", "$url", UIMessages.getInstance().getMessage("age.download.url")));
		}
		 
		System.out.println(sender+": "+message);
	
	}

	public void dccChatRequest( String nick , java.net.InetAddress ip , short port )
	{
			
		IrcDccChatGameSelector idcsh = new IrcDccChatGameSelector (  );
		try
		{
			IrcDccChatSocket idcs = ircSocket.acceptDccChatRequest ( nick , ip , port , idcsh );
			idcs.setPriority(Thread.MIN_PRIORITY);
			ircSocket.sendPrivate ( nick , UIMessages.getInstance().getMessage("irc.accepting.dcc") );
			idcsh.setSocket ( idcs );
			launchGame ( idcsh );
		}
		catch ( Exception e )
		{
			ircSocket.sendPrivate ( nick , UIMessages.getInstance().getMessage("irc.cannot.accept.dcc") );
		}
		
	}

	public void launchGame ( final IrcDccChatGameSelector idcgs )
	{
	
		//hacer un fork, porque la selección de mundo, etc. tiene esperas, y mientras tenemos que poder atender a otros clientes	
		Thread th = new Thread("IRC Game Launcher")
		{
			public void run()
			{

				Debug.println("Game launchin'");
				
				

				World mundo = idcgs.getPartidaSelection( getPartidasEnCurso() ).getMundo();
						
				new IrcDccChatClientProxy ( idcgs.getSocket() , mundo );		
						
				//SimpleTelnetClientProxy stpc = new SimpleTelnetClientProxy ( incoming , ccount , mundo );
				//activeClients.addElement ( stpc );
					
			}
		
		};
		
		th.start();
	
	}

/*
	ServerSocket s = new ServerSocket ( thePort );
			for (;;)
			{
				final Socket incoming = s.accept();
				
				//hacer un fork, porque la selección de mundo, etc. tiene esperas, y mientras tenemos que poder atender a otros clientes
				Thread th = new Thread()
				{
					public void run()
					{
						int ccount = getAndIncClientCount(); //synchronized
						Debug.println("Spawnin' client proxy " + ccount );
						
						SimpleTelnetClientSelector stcs = new SimpleTelnetClientSelector ( incoming , ccount );
						World mundo = stcs.getPartidaSelection( getPartidasEnCurso() ).getMundo();
						
						SimpleTelnetClientProxy stpc = new SimpleTelnetClientProxy ( incoming , ccount , mundo );
						activeClients.addElement ( stpc );
					}
				};
				th.start();
								
			}

*/

	public java.util.List getPartidasEnCurso()
	{
		return ServerHandler.getInstance().getPartidasIrc();
	}


}
