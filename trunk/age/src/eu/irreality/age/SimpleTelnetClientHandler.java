/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;
import java.net.*;
import java.io.*;

import eu.irreality.age.server.PartidaEnCurso;

public class SimpleTelnetClientHandler extends Thread {

 
 
	private static Vector activeClients;
	private int maxClients;
	
	//private World mundo; 
	
	private List /*of PartidaEnCurso*/ partidas;
	
	int clientCount;
	
	private short thePort;

	public SimpleTelnetClientHandler( short port )
	{
		System.out.println("SimpleTelnetClientHandler started.");
		partidas = new Vector();
		thePort = port;
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

  	public SimpleTelnetClientHandler ( World w , int maxPlayers , short port )
  	{
		
		System.out.println("SimpleTelnetClientHandler started.");
	
		thePort = port;
	
		//mundo = w;
		
		partidas = new Vector();
		addPartida ( new PartidaEnCurso ( w , maxPlayers , "Nombre de Partida" , null ) );
		
		maxClients = maxPlayers;

		setPriority(Thread.MIN_PRIORITY);
		start();
	
  	}
	
	public synchronized void addPartida ( PartidaEnCurso pec )
	{
		System.out.println("ADDDDDDDDDDDDING GAME");
		partidas.add ( pec );
	}
	
	public synchronized int getClientCount()
	{
		return clientCount;
	}
	public synchronized void incClientCount()
	{
		clientCount++;
	}
	public synchronized int getAndIncClientCount()
	{
		clientCount++;
		return ( clientCount-1 );
	}
	//coge las partidas en curso, protegida de accesos concurrentes
	public synchronized List getPartidasEnCurso()
	{
		Vector result = new Vector();
		for ( int i = 0 ; i < partidas.size() ; i++ )
		{
			result.add (  partidas.get(i) );
		}
		return result;
	}
	
	public void run()
	{
	
		activeClients = new Vector();
		clientCount = 1;		
		
		try
		{
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
						System.out.println("Spawnin' client proxy " + ccount );
						
						SimpleTelnetClientSelector stcs = new SimpleTelnetClientSelector ( incoming , ccount );
						
						PartidaEnCurso pec =  stcs.getPartidaSelection( getPartidasEnCurso() ); 
						
						World mundo = pec.getMundo();
						
						if ( mundo == null ) return;
						
						SimpleTelnetClientProxy stpc = new SimpleTelnetClientProxy ( incoming , ccount , mundo );
						activeClients.addElement ( stpc );
					}
				};
				th.start();
								
			}
		
		}
		catch (IOException ioe )
		{
			System.out.println("Exception on accepting socket.\n");
			ioe.printStackTrace();
		}
		
		
		
  	}
	
	
}


//obtiene las opciones del cliente.
class SimpleTelnetClientSelector implements TelnetConstants
{

	private InputStream is;
	private OutputStream os;

	private BufferedReader in;
	private PrintWriter out;
	
	private Socket incoming;
	private int id;
	
	public SimpleTelnetClientSelector ( java.net.Socket s , int id )
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
	}
	
	public boolean checkForANSISupport ( ) throws IOException //BLOCKING CALL
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
							escribir("Your telnet client refuses negotiating terminal types.\n");
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
								escribir("ANSI support detected (" + termTypeString + ")\n");
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
	
	//do
	public void escribir ( String s )
	{

		
		if ( out != null )
		{
			System.out.print("Systemoutprinting:"+s);
			//print with LF -> CRLF substitution!
			out.print( StringMethods.textualSubstitution ( s , "\n" , "\n\r" ) );
			out.flush();
		}
	}
	
	//selector input is always synchronous.
	public String getInput (  )
	{
		try
		{
			//escribir("> ");
			String str = in.readLine();
			System.out.println("INPUT GOTTEN: " + str );
			return str;
		}
		catch ( IOException ioe )
		{
			System.out.println("Oh yeppie, en Ái Ou Excepxen.\n");
			return null;
		}
	}
	
	public PartidaEnCurso getPartidaSelection( List partidas )
	{
		//aquí parafernalia del menú
		
		if ( partidas.size() < 1 )
		{
			escribir("¡No hay partidas en curso!\n");
			return null;
		}
		if ( partidas.size() == 1 )
		{
			//autoseleccionar la única que hay
			PartidaEnCurso pec = (PartidaEnCurso) partidas.get(0);
			if ( pec.getPlayers() >= pec.getMaxPlayers() )
			{
				; //como si hubiera varias partidas, mostrar menú		
			}
			else
			{
				return pec;
			}
		}
		
		//código para más de una partida en curso:
		
		boolean done = false;
		
		while ( !done )
		{

			escribir("PARTIDAS EN CURSO:\n");

			for ( int i = 0 ; i < partidas.size() ; i++ )
			{
				PartidaEnCurso pec = (PartidaEnCurso) partidas.get(i);
				escribir ( (i+1) + ". " + pec.getNombre() + " " + "(" + pec.getPlayers() + "/" + pec.getMaxPlayers() + ")" ); 
				escribir ( "\n" );
			}

			escribir("Introduzca el número de la partida a la que desea jugar: ");

			String linea = getInput();
			if ( linea == null ) return null; //exc
			int part = -1;
			try
			{
				part = Integer.valueOf ( linea ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				;
			}
			if ( part > 0 && part <= partidas.size() )
			{
				PartidaEnCurso p = (PartidaEnCurso) partidas.get(part-1);
				if ( p.getPlayers() < p.getMaxPlayers() )
					return p;
				else
					escribir("Esa partida ha alcanzado su número máximo de usuarios, no puedes entrar.\n");
			}


		}
		
		return null;
	}

}
