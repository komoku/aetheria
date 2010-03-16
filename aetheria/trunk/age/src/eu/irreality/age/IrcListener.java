/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
//package irc;

import java.util.*;

public interface IrcListener extends EventListener
	{
	public void	channelMsg (String sender, String channel, String message);
	public void	privateMsg (String sender, String message);
	public void noticeMsg (String sender, String message);
	public void serverMsg (String message);
	public void privateAwayMsg (String from, String reason);
	public void channelAwayMsg (String channel, String from, String reason);
	public void	changeMode (String from, String target, String mode);
	public void quitMsg	(String who, String reason);
	public void channelTopic (String channel, String topic);
	public void topicSetBy (String channel, String nick, String date);
	public void topicChange (String channel, String nick, String newTopic);
	public void channelJoin (String channel, String nick);
	public void	channelPart (String channel, String nick);
	public void nickChange (String oldNick, String newNick);
	public void ping (String from);
	public void kick (String channel, String who, String target, String reason);
	public void notOnChannel (String channel);
	public void notChannelOp (String channel);
	public void nickList (String channel, String nicks);
	public void unknownMsg (String message);
	
	public void channelAction ( String sender , String channel , String text );
	public void privateAction ( String sender , String text );
	public void dccChatRequest ( String sender , java.net.InetAddress ip , short port );
	public void ctcpPing ( String sender , String argument );
	
	}

