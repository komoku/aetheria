/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.irc;


/*Uses the IRC/DCC Chat Listener interface to build a SYNCHRONOUS dcc interface.*/
/*Don't forget to setSocket().*/
public class IrcDccChatSynchronousHandler implements IrcDccListener
{


	IrcDccChatSocket s;
	
	String curNick;
	String curText;
	
	//disconnection flag
	protected boolean clientHasDisconnected = false;
	
	public IrcDccChatSynchronousHandler ( )
	{
		s = null;
	}
	
	public IrcDccChatSynchronousHandler ( IrcDccChatSocket s )
	{
		this.s = s;
		s.setDccListener ( this );
	}
	
	public synchronized void setSocket ( IrcDccChatSocket s )
	{
		this.s = s;
		s.setDccListener ( this );
	}
	
	public synchronized IrcDccChatSocket getSocket()
	{
		return s;
	}

	//blocking call!
	public synchronized String getInput()
	{
		try
		{
			wait();
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);
			intex.printStackTrace();
			return null;
		}
		//notified by dccMsg() [or by dccDisconnection, we return null in that case]
		String temp = curText;
		curText = null;
		return temp;
	}
	
	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public void escribir ( String str )
	{
		write(str);
	}

	public void write ( String str )
	{
		if ( s != null )
			s.sendMessage ( str );
	}
	
	public synchronized void dccMsg ( String nick , String message )
	{
		curNick = nick;
		curText = message;
		System.out.println("Input gotten: " + message);
		notify();
	}

	public synchronized void dccConnection ( String nick )
	{
		System.out.println("Connected to " + nick);	
	}
	
	public synchronized void dccDisconnection ( String nick )
	{
		System.out.println("Disconnected from " + nick);
		clientHasDisconnected=true;
		curText = null;
		notify();
	}


}