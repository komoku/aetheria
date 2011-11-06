/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.irc;
//package irc;
import java.util.*;

class IrcCommand
	{
	

	
	static final int	PingType =			1;
	static final int	PrivMsgType =		2;
	static final int	AwayMsgType =		15;
	static final int	NickType =			3;
	static final int	QuitType =			4;
	static final int	PartType =			5;
	static final int	JoinType =			6;
	static final int	TopicType =			7;
	static final int	TopicChangeType =	8;
	static final int	NickListType =		9;
	static final int	ServerMsgType =		10;
	static final int	TopicSetByType =	11;
	static final int	NoticeType =		12;
	static final int	ModeType =			13;
	static final int	KickType =			14;
	static final int	NotChannelOpType =	16;
	static final int	NotOnChannelType =	17;

	static final int	UnknownType =		128;

	public	int		type;
	public	String	from, to, content, channel, command;

	public IrcCommand(String s)
		{
		StringTokenizer st = new StringTokenizer(s);
		String s1 = st.nextToken(), s2 = st.nextToken();
		
		System.out.println("s:" + s + "**" + s.indexOf((char)1) );
		System.out.println("s1:" + s1 + "**" + s1.indexOf((char)1) );
		System.out.println("s2:" + s2 + "**" + s2.indexOf((char)1) );
		for ( int i = 0 ; i < s.length() ; i++ )
		{
			System.out.print ( "##" + s.charAt(i) + "#" + (int)(s.charAt(i)) + "##" );
		}

		this.command = s;

		// =====================================================================================
		// commands of syntax "<COMMAND> :<content>"
		if (s1.equals("PING")) 
			{
			type = PingType;
			content = s2.substring(1);
			return;
			}

		// =====================================================================================
		// commands of syntax ":<nick>!<name>@<virtualip> <COMMAND> <origin>"
		if (s2.equals("PART"))
			{
			type = PartType;
			from = getNickFromIdString(s1.substring(1));
			channel = st.nextToken();
			return;
			}

		// =====================================================================================
		// commands of syntax ":<nick>!<name>@<virtualip> <COMMAND> <origin> :<content>"
		if (s2.equals("PRIVMSG"))
			{
			type = PrivMsgType;
			from = getNickFromIdString(s1.substring(1));
			to = st.nextToken(); //s3
			content = (concatTokenizedString(st)).substring(1); //id est, (s4..sN).substring(1)			

			
			System.out.println("content:" + content + "**" + content.indexOf((char)1) );
			
			return;
			}

		if (s2.equals("NOTICE"))
			{
			type = NoticeType;
			from = getNickFromIdString(s1.substring(1));
			to = st.nextToken();
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		if (s2.equals("TOPIC"))
			{
			type = TopicChangeType;
			from = getNickFromIdString(s1.substring(1));
			channel = st.nextToken();
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		if (s2.equals("372"))	// ServerMsg
			{
			type = ServerMsgType;
			from = getNickFromIdString(s1.substring(1));
			to = st.nextToken();
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		if (s2.equals("442"))	// Not on that channel
			{
			type = NotOnChannelType;
			to = st.nextToken();
			channel = st.nextToken();
			content = concatTokenizedString(st);
			return;
			}

		if (s2.equals("482"))	// Not a channel operator
			{
			type = NotChannelOpType;
			to = st.nextToken();
			channel = st.nextToken();
			content = concatTokenizedString(st);
			return;
			}

		// =====================================================================================
		// commands of type ":<nick>!<name>@<virtualip> <COMMAND> :<content>"
		if (s2.equals("NICK"))
			{
			type = NickType;
			from = getNickFromIdString(s1.substring(1));
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		if (s2.equals("QUIT"))
			{
			type = QuitType;
			from = getNickFromIdString(s1.substring(1));
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		if (s2.equals("JOIN"))
			{
			type = JoinType;
			from = getNickFromIdString(s1.substring(1));
			channel = (concatTokenizedString(st)).substring(1);
			return;
			}

		// =====================================================================================
		// commands of type ":<nick>!<name>@<virtualip> <COMMAND> <origin1> <origin2> :<content>"
		if (s2.equals("332"))	// TOPIC
			{
			type = TopicType;
			from = st.nextToken();
			channel = st.nextToken();
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		if (s2.equals("KICK"))
			{
			type = KickType;
			from = getNickFromIdString(s1.substring(1));
			channel = st.nextToken();
			to = st.nextToken();
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		if (s2.equals("301"))	// AWAY MSG
			{
			type = AwayMsgType;
			to = st.nextToken();
			from = st.nextToken();
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		if (s2.equals("332"))	// TOPIC
			{
			type = TopicType;
			from = st.nextToken();
			channel = st.nextToken();
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		// =====================================================================================
		// commands of type ":<nick>!<name>@<virtualip> <COMMAND> <nick> <channel> <nick> <date>"
		if (s2.equals("333"))	// TOPIC_SET_BY
			{
			type = TopicSetByType;
			to = st.nextToken();
			channel = st.nextToken();
			from = st.nextToken();
			content = st.nextToken();
			return;
			}

		// =====================================================================================
		// commands of type ":<nick>!<name>@<virtualip> <COMMAND> <origin1> = <origin2> :<content>"
		if (s2.equals("353"))	// LIST
			{
			type = NickListType;
			from = st.nextToken();
			st.nextToken();
			channel = st.nextToken();
			content = (concatTokenizedString(st)).substring(1);
			return;
			}

		// =====================================================================================
		// commands of type ":<nick>!<name>@<virtualip> <COMMAND> <origin> <mode> <destiny>"


		this.type = UnknownType;
		}

	String	getNickFromIdString (String s)
		{
		StringTokenizer st = new StringTokenizer(s,"!");
		return st.nextToken();
		}

	String	concatTokenizedString (StringTokenizer st)
		{
		String s = st.nextToken();
		while (st.hasMoreTokens()) s+=" "+st.nextToken();

		return s;
		}

	}

