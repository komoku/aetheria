/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.io.*;
import java.net.*;

public class IrcDccChatSocket extends Thread //analogo a IrcSocket pero para DCC Chat
{

	//for server
	private ServerSocket ss;
	
	//for both
	private boolean isServer;
	private BufferedReader br = null; 
	private PrintWriter pw = null ;
	private IrcDccListener il;
	private String nick; 
	
	//for client
	private InetAddress addr;
	private short port;
	
	
	private /*critical section*/ boolean connected;
	
	private Object connectionWaitSemaphore = new Object();
	
	public boolean isConnected()
	{
		return true;
	}
	
	public void waitUntilConnected() throws InterruptedException
	{
		synchronized ( connectionWaitSemaphore )
		{
			System.out.println("Connected value " + connected);
			if ( connected ) return;
			while ( !connected )
			{
				connectionWaitSemaphore.wait();
				System.out.println("Connected value " + connected);
			}
		}	
		
	}
	
		
	//when we send a request and are the server
	public IrcDccChatSocket ( ServerSocket ss , IrcDccListener il , String nick ) throws IOException
	{
		this.il = il;
		this.nick = nick;
		this.ss = ss;
		isServer = true;
		setPriority(Thread.MIN_PRIORITY);
		System.out.println("Gonna start IRC/DCC chat socket");
		start();
	}
	
	//when we receive a request and are the client
	public IrcDccChatSocket ( InetAddress addr , short port , IrcDccListener il , String nick ) throws IOException
	{
		this.il = il;
		this.nick = nick;
		this.port = port;
		this.addr = addr;
		isServer = false;
		setPriority(Thread.MIN_PRIORITY);
		System.out.println("Gonna start IRC/DCC chat socket");
		start();
	}
	
	public void setDccListener ( IrcDccListener idcl )
	{
		this.il = idcl;
	}
	
	//public IrcDccChatSocket ( Socket s , IrcDccListener il , 

	public void run ( )
	{

			Socket s;
			System.out.println("Entering try.");
			try
			{
				System.out.println("Entering if-else part.");
				if ( isServer )
				{
					System.out.println("Entering socket-synchronized block");
					
					//synchronized(this)
					//{
						System.out.println("Accepting");
						s = ss.accept();
						
						/* later (after streams)
						synchronized(connectionWaitSemaphore)
						{
							connectionWaitSemaphore.notifyAll();
							connected=true;
						}
						*/
						
						System.out.println("Accepted");
					//}
				}
				else
				{
					//synchronized(this)
					//{
					
						System.out.println("Connecting");
					
						s = new Socket ( addr , port );
					
						System.out.println("Connected");
					
					
					//}
				}
				
				if ( s == null )
				{
					il.dccDisconnection(nick);
					synchronized(this)
					{
						connected=false;
					}
					return;
				}
				System.out.println("DCC connection established.\n");
				il.dccConnection(nick);
				br = new BufferedReader ( new InputStreamReader ( s.getInputStream() ) );
				pw = new PrintWriter ( new OutputStreamWriter ( s.getOutputStream() ) );
			
					synchronized(connectionWaitSemaphore)
					{
						connectionWaitSemaphore.notifyAll();
						connected=true;
					}
			
			}
			catch ( Exception e )
			{
				il.dccDisconnection(nick);
				return;
			}
			for ( ;; )
			{
			
				try
				{
					String received = br.readLine();
					if ( received == null ) //null <=> DCC chat session closed	
						break;
					if ( il != null )
						il.dccMsg ( nick , received );				
				}
				catch ( Exception e )
				{
					break;
				}
				
			}
			
			il.dccDisconnection ( nick );

		
	}
	
	public void sendMessage ( String mesg )
	{
		System.out.println("Message to send: " + mesg);
		synchronized ( this )
		{
			if ( pw != null )
			{
				pw.print(mesg);
				
				//medida especial: en IRC, no se manda realmente nada sin \n.
				if ( ( mesg.charAt(mesg.length()-1) != '\n' ) && ( mesg.charAt(mesg.length()-1) != '\r' ) )
				{
					pw.print("\n");
				}
				
				
				pw.flush();
			}
		}
		System.out.println("Message sent: " + mesg);
	}

}
