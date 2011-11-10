/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.irc;
import java.util.*;
//import irc.*;

import eu.irreality.age.i18n.UIMessages;

public class IrcBot implements IrcListener , IrcDccListener
	{
		protected IrcSocket	ircSocket;
	
	private final boolean ACCEPT_DCC_CHAT = true;

	public IrcBot( String server, int port, String nick, String name , Vector channels ) throws Exception
		{

			ircSocket = new IrcSocket(server,port,this); ircSocket.login(nick,name);


		Thread.sleep(2000);
		ircSocket.setNick(nick);
		Thread.sleep(1000);

		for ( int i = 0 ; i < channels.size() ; i++ )
		{
			String channel = (String) channels.get(i);
			
			//ircSocket.joinChannel("#aetheria"); 
			//ircSocket.setChannelTopic("#aetheria","buenas"); 
			//ircSocket.setMode("#p","[\\AxMan\\]","+o");
			//ircSocket.sendChannel("#aetheria", UIMessages.getInstance().getMessage("irc.bot.intro.channel") ); 
			
			ircSocket.joinChannel(channel);
			ircSocket.sendChannel(channel, UIMessages.getInstance().getMessage("irc.bot.intro.channel") ); 
		}
		
		//for ( ; ; )
		{
		Thread.sleep(120000);
			try
			{
				//ircSocket.sendPrivate ( "EthErwAvE" , "re1" );
				IrcDccChatSocket idcs = ircSocket.sendDccChatRequest ( "EthErwAvE" , IrcSocket.DCC_ANY , this );
				//ircSocket.sendPrivate ( "EthErwAvE" , "re2" );
			}
			catch ( Exception e )
			{
				System.out.println("Exc " + e);
				e.printStackTrace();
			}
		}

		//ircSocket.joinChannel("#murcia"); 
		//ircSocket.joinChannel("#cieza"); 
		//ircSocket.joinChannel("#escalada"); 
		//ircSocket.setAway("estudiando"); 
		//ircSocket.setMode("#ia","iSotOpe","+o");
		}

	public void	channelMsg (String sender, String channel, String message)
		{ System.out.println("("+channel+") "+sender+": "+message); };
	
	public void	channelAction (String sender, String channel, String message)
		{ System.out.println("("+channel+") "+sender+" [action]: "+"message"); };	

	public void	serverMsg (String message)
		{ System.out.println("MOTD: "+message); };

	public void	privateMsg (String sender, String message)
		{
		
		/* 
			na, en extension
		
			if ( message.equalsIgnoreCase("DCC") )
			{
				send
			}
		*/
		
		if (!(sender.equals("NiCK")|sender.equals("agenda")|sender.equals("MeMo")))
			{	
			ircSocket.sendPrivate(sender,"Soy un servidor del Aetheria Game Engine.");
			ircSocket.sendPrivate(sender,
					"Para jugar una partida, ábreme un DCC chat.");
			ircSocket.sendPrivate(sender,"Para más información, visita http://www.irreality.org/aetheria/conv.htm y http://absurdum.f2o.org/aetheria");
			}
		System.out.println(sender+": "+message);
		};
		
	public void	privateAction (String sender, String message)
		{
		System.out.println(sender+" [action]: "+message);
		};

	public void dccMsg ( String sender , String message )
	{
		System.out.println("[DCC] "+sender+": "+message);
	}
	public void dccDisconnection ( String sender )
	{
		System.out.println("Desconectado de " + sender);
	}
	public void dccConnection ( String sender )
	{
		System.out.println("Conectado a " + sender);
	}

	public void	privateAwayMsg (String sender, String reason)
		{ System.out.println(sender+" is away: "+reason); }

	public void	channelAwayMsg (String channel, String from, String reason)
		{ System.out.println("("+channel+") "+from+" is away: "+reason); }

	public void noticeMsg (String sender, String message) 
		{ System.out.println(sender+" (notice): "+message); };

	public void	changeMode (String from, String target, String mode) 
		{ System.out.println(from+" sets mode "+mode+" to "+target); };

	public void quitMsg	(String who, String reason) 
		{ System.out.println(who+" quits: "+reason); };

	public void channelTopic (String channel, String topic) 
		{ System.out.println("Topic "+channel+": "+topic); };

	public void notOnChannel (String channel) 
		{ System.out.println("not on channel "+channel); };

	public void notChannelOp (String channel) 
		{ System.out.println("not a channel operator of "+channel); };

	public void topicSetBy (String channel, String nick, String date) 
		{ System.out.println("Topic of "+channel+" set by "+nick+" on "+date); };

	public void kick (String channel, String who, String target, String reason) 
		{ System.out.println(who+" kicks "+target+" from channel "+channel+" because :"+reason); };

	public void topicChange (String channel, String nick, String newTopic) 
		{ System.out.println(nick+" sets "+channel+" topic: "+newTopic); };

	public void channelJoin (String channel, String nick) 
		{
		if (channel.equals("#ia")&&nick.toLowerCase().equals("isotope"))
				ircSocket.setMode(channel, nick,"+o");
		System.out.println(nick+" joins channel "+channel);
		};

	public void	channelPart (String channel, String nick) 
		{ System.out.println(nick+" parts from channel "+channel); };

	public void nickChange (String oldNick, String newNick) 
		{ System.out.println(oldNick+" changes nick to "+newNick); };

	public void ping(String from) 
		{ System.out.println("ping: "+from); };

	public void ctcpPing(String from,String arg) 
		{ System.out.println("ctcp ping: "+from+"["+arg+"]"); };

	public void nickList(String channel, String nicks) 
		{ System.out.println("nicks in "+channel+": "+nicks); };

	public void unknownMsg(String message) 
		{ System.out.println("Unknown message: "+message); };
	
	public void dccChatRequest(String nick , java.net.InetAddress ip , short port )
	{
		if ( ACCEPT_DCC_CHAT )
		{
			try
			{
				ircSocket.acceptDccChatRequest ( nick , ip , port , this );
				ircSocket.sendPrivate ( nick , "Aceptando la conexión DCC Chat (si la conexión no se establece correctamente, pruebe a ponerme en privado la palabra DCC" );
			}
			catch ( Exception e )
			{
				ircSocket.sendPrivate ( nick , "No se ha podido aceptar la conexión DCC Chat. Pruebe a poner en este privado la palabra DCC.");
			}
		}
		
	}

	// main method
	/*
	public static void main(String [] args)
		{
		try { IrcBot ircBot = new IrcBot("denebola",6667,"Jerk0","TestBot for Irc written in Java"); }
		catch(Exception e) { System.out.println(e); e.printStackTrace(); }
		}
	*/

	}
