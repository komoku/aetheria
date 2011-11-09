/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.irc;
//package irc;
import java.net.*;
import java.util.*;
import java.io.*;

import eu.irreality.age.util.networking.LocalIPObtainer;



public class IrcSocket extends Thread
{
	BufferedReader	fromServer;
	PrintStream		toServer;
	IrcListener		ircListener;
	Socket socket;

	static final char X_DELIM = (char) 1; //extended content delimiter

	public IrcSocket(String server, int port, IrcListener ircListener) throws Exception
	{
		socket = new Socket(InetAddress.getByName(server),port);
		toServer = new PrintStream(socket.getOutputStream());
		fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.ircListener = ircListener;
		this.start();
	}

	public void joinChannel(String channel)
	{ toServer.println("JOIN "+channel); 
	};
	public void	partChannel(String channel)
	{ toServer.println("PART "+channel); 
	};
	public void	sendChannel(String channel, String line)
	{ toServer.println("PRIVMSG "+channel+" :"+line); 
	};
	public void	setChannelTopic(String channel, String topic)
	{ toServer.println("TOPIC "+channel+" :"+topic); 
	};

	public void	sendPrivate(String nick, String line)
	{ toServer.println("PRIVMSG "+nick+" :"+line); 
	};
	public void	setNick(String nick)
	{ toServer.println("NICK "+nick); 
	};
	public void	sendPing(String nick)
	{ toServer.println("PING "+nick); 
	};
	public void	sendPong(String nick)
	{ toServer.println("PONG :"+nick); 
	};
	public void sendCtcpPing(String nick , String args)
	{
		toServer.println("PRIVMSG "+nick+" :"+(char)1+"PING "+args+(char)1); 
	}
	public void login(String nick, String realName)
	{ this.setNick(nick); toServer.println("USER "+nick+" <host> <server> :"+realName); 
	}

	public void setMode(String channel, String target, String mode)
	{ toServer.println("MODE "+channel+" "+mode+" "+target); 
	};

	public void	setAway(String reason)
	{ toServer.println("AWAY "+reason); 
	};

	public void	quit(String reason)
	{ toServer.println("QUIT :"+reason); 
	};


	/*DCC SUPPORT BEGIN*/

		final static short DCC_ANY = 0; //pased to sendDccChatRequest, will open the server on any free port.

	/*
	This is a nonblocking call.
	Returns an IrcDccChatSocket not yet connected. Will notify the listener when connected.
	*/
	public IrcDccChatSocket sendDccChatRequest ( String nick , short port /*or DCC_ANY*/, IrcDccListener idl ) throws java.io.IOException
	{
		//System.out.println("Going to send DCC chat request\n");
		long addressAsLong = 0;
		ServerSocket dccSocket = new ServerSocket ( port );
		//System.out.println("Socket created at port " + dccSocket.getLocalPort() + "\n"); 
		
		//InetAddress ia = /*dccSocket.getInetAddress();*/InetAddress.getLocalHost();
		
		//InetAddress ia = LocalIPObtainer.getLocalHost(); //testing as of 2011-11-08
		
		//TODO: Let the user select the external IP address to receive DCC's on
		//Or maybe we'll need to use whatismyip because the machine doesn't know its external ip!
		InetAddress ia = socket.getLocalAddress();  //testing as of 2011-11-08
		/*
		System.err.println("Addr: " + ia);
		if ( ia.isLoopbackAddress() || ia.isLinkLocalAddress() || LocalIPObtainer.isIPv4Private(ia) )
		{
			ia = LocalIPObtainer.getLocalHost();
			System.err.println("Correction: " + ia);
		}
		*/
			
		byte[] b = ia.getAddress();
		//System.out.println("Converting address " + ia + "(" + b + ")" + "\n");
		//System.out.println("Host addr: " + ia.getHostAddress() );
		//System.out.println("Local socket addr: " + dccSocket.getLocalSocketAddress() );
		//System.out.println("Local host addr: " + InetAddress.getLocalHost() );
		for ( int i = 0 ; i < b.length ; i++ )
		{
			addressAsLong = (addressAsLong << 8);
			addressAsLong +=  (b[i]>=0)?(b[i]):(256+b[i]) ; System.out.println(" " + (int)b[i]);
			//System.out.println("addressAsLong " + addressAsLong);
		}

		//System.out.println("Sending privmsg:\n");

		//System.out.println ( "PRIVMSG "+nick+" :"+((char)1)+"DCC CHAT CHAT "+ addressAsLong + " " + dccSocket.getLocalPort() + ((char)1));

		toServer.println ( "PRIVMSG "+nick+" :"+((char)1)+"DCC CHAT CHAT "+ addressAsLong + " " + dccSocket.getLocalPort() + ((char)1));

		return new IrcDccChatSocket ( dccSocket , idl , nick );
	}
	
	public IrcDccChatSocket acceptDccChatRequest ( String nick , InetAddress addr , short port , IrcDccListener idl ) throws java.io.IOException
	{
		return new IrcDccChatSocket ( addr , port , idl , nick );
	}

	/*DCC SUPPORT END*/





	public void	run()
	{
		try
		{
			while(true)
			{
				IrcCommand c = new IrcCommand(fromServer.readLine());
				switch(c.type)
				{
					case IrcCommand.NoticeType:	ircListener.noticeMsg(c.from, c.content);			break;
					case IrcCommand.ModeType:	ircListener.changeMode(c.from, c.to, c.content);	break;
					case IrcCommand.QuitType:	ircListener.quitMsg(c.from, c.content);				break;
					case IrcCommand.TopicType:	ircListener.channelTopic(c.channel, c.content);		break;
					case IrcCommand.JoinType:	ircListener.channelJoin(c.channel, c.from);			break;
					case IrcCommand.PartType:	ircListener.channelPart(c.channel, c.from);			break;
					case IrcCommand.NickType:	ircListener.nickChange(c.from, c.content);			break;
					case IrcCommand.PingType:	ircListener.ping(c.content); sendPong(c.content);	break;
					case IrcCommand.KickType:	ircListener.kick(c.channel,c.from,c.to,c.content);	break;
					case IrcCommand.NickListType:	ircListener.nickList(c.channel, c.content);		break;
					case IrcCommand.UnknownType:	ircListener.unknownMsg(c.command);				break;
					case IrcCommand.ServerMsgType:	ircListener.serverMsg(c.content);				break;
					case IrcCommand.NotOnChannelType:	ircListener.notOnChannel(c.channel);		break;
					case IrcCommand.NotChannelOpType:	ircListener.notChannelOp(c.channel);		break;

					case IrcCommand.TopicChangeType:
					ircListener.topicChange(c.channel,c.from,c.content);	break;

					case IrcCommand.TopicSetByType:
					ircListener.topicSetBy(c.channel, c.from, c.content);	break;

					case IrcCommand.PrivMsgType:			


					//parse extended content according to X-DELIM
					StringTokenizer st = new StringTokenizer ( c.content , ""+X_DELIM , true );
					boolean terminamos = false;
					boolean extended = false;
					for (;;)
					{
						if ( st.hasMoreTokens() )
						{
							String tok = st.nextToken();
							if ( tok.equals(""+X_DELIM) )
							{		
								extended = !extended; //extended content delimiter
							}
							else
							{
								if ( !extended )
								{
									if (c.to.charAt(0) == '#')
										ircListener.channelMsg(c.from, c.to, tok);
									else
										ircListener.privateMsg(c.from, tok);
								}
								else
								{

									String even_content = tok;
									//extended content!
									StringTokenizer extTok = new StringTokenizer (even_content);
									if ( !extTok.hasMoreTokens() )
									{
										continue;
									}
									else
									{
										String command = extTok.nextToken();
										String arguments = concatTokenizedString(extTok);
										if ( command.equalsIgnoreCase("ACTION") )
										{
											if (c.to.charAt(0) == '#')
												ircListener.channelAction(c.from, c.to, arguments);
											else
												ircListener.privateAction(c.from, arguments);
										}
										else if ( command.equalsIgnoreCase("DCC") )
										{
											//DCC request.
											StringTokenizer argTok = new StringTokenizer ( arguments );
											if ( !argTok.hasMoreTokens() )
											{
												continue;
											}
											else
											{
												try
												{
													String type = argTok.nextToken();
													if ( type.equalsIgnoreCase("CHAT") )
													{
														//DCC chat request.
														String arg = argTok.nextToken();
														String addr = argTok.nextToken();
														String port = argTok.nextToken();
														
														System.out.println("Argument: " + arg);
														System.out.println("Address: " + addr);
														System.out.println("Port: " + port);

														//build InetAddress and port
														long iaddr = Long.valueOf ( addr ).longValue();
														
														System.out.println("Iaddr is " + iaddr);
														
														//byte[] b = new byte[4];
														String s = ""; //string representation of the IP address
														for ( int i = 0 ; i < 4 ; i++ )
														{
															//host byte order
															System.out.println("Byte: " + ( new Long( iaddr%256 ) ) );
															System.out.println("Casted byte: " + ( new Long( iaddr%256 ) ).byteValue() );
															//b[i] = ( new Integer( iaddr%256 ) ).byteValue();
															s="."+s;
															s = String.valueOf ( (iaddr%256>=0)?(iaddr%256):(256+iaddr%256) ) + s;
															iaddr = iaddr >> 8;
														}
														System.out.println("Stringed address: " + s.substring(0,s.length()-1) );
														InetAddress ia = InetAddress.getByName( s.substring(0,s.length()-1) );
														short dccport = Short.valueOf(port).shortValue();
														ircListener.dccChatRequest ( c.from , ia , dccport );	
														
														System.out.println("Converted address: " + ia);
																	
													}
													else
													{
														//we don't treat sends
														System.out.println("Type is unknown");
														ircListener.unknownMsg(even_content);
														continue;
													} //end else
												} //end try
												catch ( Exception e )
												{
													//wrong format dcc message
													System.out.println("Wrong format");
													ircListener.unknownMsg(even_content);
													continue;
												} //end catch
											} //end else (if argtok has no more tokens)
										}
										else if ( command.equalsIgnoreCase("PING") )
										{
											ircListener.ctcpPing ( c.from , arguments );
											sendCtcpPing ( c.from , arguments );
										} //end else if (command is...)	
									} //end else (if extd. text has tokens)
								} //end else (text is extended)

							} //end if (not x_delim)
							
						} //end if (more tokens exist)	
						else
						{
							break;
						}




					} //end for [infinite]







						break;

					case IrcCommand.AwayMsgType:
						if (c.to.charAt(0) == '#')
							ircListener.channelAwayMsg(c.to, c.from, c.content);
						else
							ircListener.privateAwayMsg(c.from, c.content);
						break;

					default: System.out.println("*** Not catched event type "+c.type+":"+c.command);
				} //end case switch	
			} //end infinite loop
		} //end try
		catch (Exception e)
		{
			;
		}
	} //end method run()


	String	concatTokenizedString (StringTokenizer st)
	{
		String s = st.nextToken();
		while (st.hasMoreTokens()) s+=" "+st.nextToken();
			return s;
	}
	
} //end class


