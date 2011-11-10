/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.telnet;
import java.util.*;
import java.io.*;
import java.net.*;

import eu.irreality.age.InputOutputClient;
import eu.irreality.age.Player;
import eu.irreality.age.StringMethods;
import eu.irreality.age.World;
import eu.irreality.age.XMLtoWorldException;
import eu.irreality.age.i18n.UIMessages;

public class SimpleTelnetClientProxy implements InputOutputClient , TelnetConstants
{

	private Socket incoming;
	private int id;
	private InputStream is;
	private OutputStream os;
	private BufferedReader in;
	private PrintWriter out;
	
	private boolean asynchronous; //synchronous or asynchronous mode
	
	private LinkedList inputQueue = new LinkedList(); //for asynchronous command queueing

	private Vector gameLog = new Vector();
	
	
	private boolean clientHasDisconnected=false;



	public boolean isDisconnected()
	{
		return clientHasDisconnected;
	}

	public boolean checkForANSISupport ( ) throws IOException
	{
		//the telnet negotiations which check for ANSI support!!
		boolean terminamos = false;
		int tries = 10;
		
		//ask for terminal-type negotiation
		os.write ( IAC );
		os.write ( DO );
		os.write ( TELOPT_TTYPE );
		
		for(;!terminamos;)
		{
			System.out.println("Iteration.");
			int abyte = is.read();
			if ( tries == 0 ) terminamos = true;
			if ( abyte == -1 ) break;
			if ( abyte == IAC )
			{
				System.out.println("IAC.");
				int bbyte = is.read();
				switch ( bbyte )
				{
					case WILL:
						System.out.println("WILL.");
						int cbyte = is.read();
						if ( cbyte == TELOPT_TTYPE )
						{
							//aceptan negociar terminal type!
							
							//pedimos tipos de terminal...
							os.write ( IAC );
							os.write ( SB );
							os.write ( TELOPT_TTYPE );
							os.write ( TEL_QUAL_SEND );
							os.write ( IAC );
							os.write ( SE );
							
							//otra iteración para SB
							
						}
						break;
					case WONT:
						System.out.println("WONT.");
						int cbyte2 = is.read();
						if ( cbyte2 == TELOPT_TTYPE )
						{
							System.out.println("TELOPT_TTYPE.");
							write("Your telnet client refuses negotiating terminal types.\n");
							terminamos = true;
						}
						break;	
						
					case SB: //subnegotiation begin
						System.out.println("SB.");
						int cbyte3 = is.read();
						if ( cbyte3 == TELOPT_TTYPE )
						{
							byte[] buf = new byte[100];
							int i = 0;
							int c;
							is.skip(1); //IS
							while ( (c=is.read()) != IAC && i < 99 )
							{
								buf[i] = (byte)c;
								i++;
							}
							buf[i] = '\0';
							String termTypeString = new String ( buf );
							System.out.println("TERMTYPESTRING:"+termTypeString+"\n");
							
							if ( termTypeString.equalsIgnoreCase("ansi") || termTypeString.equalsIgnoreCase("vt100") || termTypeString.equalsIgnoreCase("xterm") || termTypeString.equalsIgnoreCase("vt320") || termTypeString.equalsIgnoreCase("mushclient") || termTypeString.equalsIgnoreCase("zmud") )
							{
								write("ANSI support detected (" + termTypeString + ")\n");
								return true;
							}
							
							else
							{
								//pedir otra terminal
								os.write ( IAC );
								os.write ( SB );
								os.write ( TELOPT_TTYPE );
								os.write ( TEL_QUAL_SEND );
								os.write ( IAC );
								os.write ( SE );
							
								//otra iteración para SB y procesar subnegotiation
							
								tries--;
							
							}
							
							
						}
						
					default: break;
				}

			}  
		}
		return false;
	}


	public SimpleTelnetClientProxy ( java.net.Socket s , int id , World mundo )
	{
		this.incoming = s;
		this.id = id;
		
		
	   	try 
		{
      		if (incoming != null) 
			{
        		in = new BufferedReader(new InputStreamReader((is=incoming.getInputStream())));         
        		out = new PrintWriter(new OutputStreamWriter((os=incoming.getOutputStream())));         
      		}
    	} 
		catch (Exception e) 
		{
      		System.out.println("Error: " + e);
    	}
		
		
		try
		{
			boolean ANSIsupport = checkForANSISupport();
			write("Welcome to " +  s.getLocalAddress() + " [port " + s.getLocalPort() + "] running the Aetheria Game Engine.\n");
			write("Your client ID is " + id + ".\n");
			mundo.addNewPlayerASAP ( this );
		}
		catch ( XMLtoWorldException e )
		{
			System.out.println("Couldn't: XMLtoWorldException " + e );
		}
		catch ( IOException e )
		{
			System.out.println("I/O Exception: " + e);
		}
		
	}

	public boolean isColorEnabled()
	{
		return false;
	}
	public boolean isMemoryEnabled()
	{
		return false;
	}
	public boolean isLoggingEnabled()
	{
		return true;
	}
	public boolean isTitleEnabled()
	{
		return false;
	}

	private void setSynchronousMode()
	{
		asynchronous = false;	
	}
	private void setAsynchronousMode()
	{
		if ( !asynchronous )
		{
			asynchronous = true;	
			inputQueue = new LinkedList();
			Thread t = new Thread ( )
			{
				public void run ( )
				{
					try
					{
						for(;;)
						{
							String str = in.readLine();
							if ( str == null )
							{
								System.out.println("Nullified input.\n");
								clientHasDisconnected=true;
								break;
							}
							System.out.println("Adding 2 Que: " + str);
							inputQueue.addLast(str);
						}
					}
					catch ( IOException ioe )
					{
						clientHasDisconnected=true;
						System.out.println("IO Exception.\n");
					}
				}
			};
			t.start();
		}
	}

	//bloqueante.
	public String getInput ( Player pl )
	{
		setSynchronousMode();
		try
		{
			write("SynchronousInput> ");
			String str = in.readLine();
			if ( str == null )
			{
				clientHasDisconnected=true;
				return null;
			}
			System.out.println("INPUT GOTTEN: " + str );
			return str;
		}
		catch ( IOException ioe )
		{
			clientHasDisconnected=true;
			System.out.println("Oh yeppie, en Ái Ou Excepxen.\n");
			//maybe set an exception flag and ask world to remove player ASAP.
			return null;
		}
	}
	
	//no bloqueante
	public String getRealTimeInput ( Player pl )
	{
		setAsynchronousMode();
		if ( inputQueue.isEmpty() )
			return null;
		String str = (String) inputQueue.removeFirst();	
		return str;
	}
	
	//bloqueante
	public void waitKeyPress()
	{
		setSynchronousMode();
		write("[Pulse ENTER]\n");
		try
		{
			String str = in.readLine();
			System.out.println("INPUT GOTTEN (line)\n");
		}
		catch ( IOException ioe )
		{
		
		}
	}
	
	/**
	 * @deprecated Use {@link #writeTitle(String)} instead
	 */
	public void escribirTitulo(String s) {
		writeTitle(s);
	}

	public void writeTitle(String s) { return; /*disabled*/ }
	/**
	 * @deprecated Use {@link #writeTitle(String,int)} instead
	 */
	public void escribirTitulo(String s , int pos) {
		writeTitle(s, pos);
	}

	public void writeTitle(String s , int pos) { return; /*disabled*/ }

	//do
	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public void escribir ( String s )
	{
		write(s);
	}

	//do
	public void write ( String s )
	{

		
		if ( out != null )
		{
			System.out.print("Systemoutprinting:"+s);
			//print with LF -> CRLF substitution!
			out.print( StringMethods.textualSubstitution ( s , "\n" , "\n\r" ) );
			out.flush();
		}
	}

	public void loguear ( String s )
	{
		gameLog.add ( s );
	}

	/**
	 * @deprecated Use {@link #forceInput(String,boolean)} instead
	 */
	public void forzarEntrada ( String s , boolean output_enabled )
	{
		forceInput(s, output_enabled);
	}

	public void forceInput ( String s , boolean output_enabled )
	{
		gameLog.addElement(s);
		write("\n");
		write(" > " + s.trim() );
	}

	/**
	 * @deprecated Use {@link #clearScreen()} instead
	 */
	public void borrarPantalla ( )
	{
		clearScreen();
	}

	public void clearScreen ( )
	{
		//write(UIMessages.getInstance().getMessage("irc.clear.placeholder")+"\n");
		write("\n\n");
	}

	
	public String getColorCode ( String colorKey )
	{
		return "";
	}


}
