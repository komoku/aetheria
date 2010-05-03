/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;


import java.util.*;
import java.net.*;
import java.io.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.server.PartidaEnCurso;

//<extends ClientHandler extends Thread?>
public class AGEClientHandler extends Thread {

 
 
	private static Vector activeClients;
	private int maxClients;
	
	private List /*of PartidaEnCurso*/ partidas;
	
	int clientCount;
	
	private short thePort;

	public AGEClientHandler( short port )
	{
		Debug.println("AGEClientHandler started.");
		partidas = new Vector();
		thePort = port;
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

  	public AGEClientHandler ( World w , int maxPlayers , short port )
  	{
		
		Debug.println("AGEClientHandler started.");
	
		thePort = port;
		
		
		partidas = new Vector();
		addPartida ( new PartidaEnCurso ( w , maxPlayers , "Nombre de Partida" , null ) );
		
		maxClients = maxPlayers;

		setPriority(Thread.MIN_PRIORITY);
		start();
	
  	}
	
	//begin: from ClientHandler <create?>
	public synchronized void addPartida ( PartidaEnCurso pec )
	{
		Debug.println("ADDDDDDDDDDDDING GAME");
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
	//end: from ClientHandler <create?>
	
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
						Debug.println("Spawnin' client proxy " + ccount );
						
						AGEClientSelector acs = new AGEClientSelector ( incoming , ccount );
						World mundo = acs.getPartidaSelection( getPartidasEnCurso() ).getMundo();
						
						AGEClientProxy stpc = new AGEClientProxy ( incoming ); //world passed @ bindToWorld()
						stpc.bindToWorld ( mundo );
						activeClients.addElement ( stpc );
					}
				};
				th.start();
								
			}
		
		}
		catch (IOException ioe )
		{
			System.err.println("Exception on accepting socket.\n");
			ioe.printStackTrace();
		}
		
		
		
  	}
	
	
}


//obtiene las opciones del cliente. <extends ClientSelector?>
class AGEClientSelector implements ARSPConstants
{

	private InputStream is;
	private OutputStream os;

	private BufferedReader in;
	private PrintWriter out;
	
	private Socket incoming;
	private int id;
	
	public AGEClientSelector ( java.net.Socket s , int id )
	{
		this.incoming = s;
		this.id = id;	
	   	try 
		{
      		if (incoming != null) 
			{
        		in = new BufferedReader(new InputStreamReader((is=incoming.getInputStream())));         
        		out = new PrintWriter(new OutputStreamWriter((os=incoming.getOutputStream())))
			    {
				public void println(String linea)
				{
				    print(linea);
				    print("\r\n");
				}
			    }
;         
      		}
    	} 
		catch (Exception e) 
		{
      		System.err.println("Error: " + e);
    	}	
	}
	
	public void escribir ( String s )
	{

		if ( out != null )
		{
			Debug.print("Systemoutprinting:"+s);
			//print with LF -> CRLF substitution!
			//not in AGE client.
			//out.print( StringMethods.textualSubstitution ( s , "\n" , "\r\n" ) );
			out.print ( s );
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
			Debug.println("INPUT GOTTEN: " + str );
			return str;
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
			return null;
		}
	}
	
	
	//Do all the protocol stuff until player selects a game.
	//Null if failed.
	public PartidaEnCurso getPartidaSelection( List partidas )
	{
	
		String str;
		try
		{
			str = in.readLine();
		}
		catch ( IOException ioe )
		{
			System.err.println(ioe);ioe.printStackTrace();
			return null;
		}
		StringTokenizer st = new StringTokenizer ( str );
		String head = st.nextToken();
		if ( head.equalsIgnoreCase ( PROTOCOL_VERSION_STATEMENT ) )
		{
			Debug.println("Version stated.");
			String tail = st.nextToken("").trim();
			Debug.println("Cola: " + tail);
			if ( tail.equalsIgnoreCase("1.0") )
			{
				out.println ( PROTOCOL_VERSION_ACK + " OK" );
				out.flush();
			}	  
			else
			{
				out.println ( PROTOCOL_VERSION_ACK + " NOK" );
				return null;
			}
		}
		else
		{
			out.println( UNRECOGNIZED_MESSAGE + str );
			out.println( SERVER_STATE + " expecting " + PROTOCOL_VERSION_STATEMENT );
			return null;
		}
		//Protocol version recognized.
		//Now expect commands.
		
		boolean terminamos = false;
		while ( !terminamos )
		{
			try
			{
				str = in.readLine();
                                if ( str == null )
                                {
                                    (new Exception("Received null input.")).printStackTrace();
                                    return null;
                                }
			}
			catch ( IOException ioe )
			{
				Debug.println(ioe);ioe.printStackTrace();
				return null;
			}
			st = new StringTokenizer ( str );
			String cmd=null;
			if ( st.hasMoreTokens() ) cmd = st.nextToken();
			String args = null;
			if ( st.hasMoreTokens() ) args = st.nextToken("").trim();
		
			if ( cmd == null )
			{
			    //out.println( UNRECOGNIZED_MESSAGE + " " + "Null command received.");
			    continue;
			}
			else if ( cmd.equalsIgnoreCase( GOODBYE ) )
			{
				out.println ( GOODBYE );
				return null;
			}
			else if ( cmd.equalsIgnoreCase ( SERVICE_LIST_REQUEST ) )
			{
				out.println ( SERVICE_LIST_BEGIN );
				out.println ( SERVICE_LIST_LINE + " gamelist" );
				out.println ( SERVICE_LIST_LINE + " gamejoin" );
				out.println ( SERVICE_LIST_END );
			}
			else if ( cmd.equalsIgnoreCase ( CALL_SERVICE ) )
			{
				StringTokenizer st2 = new StringTokenizer ( args );
				String servName = st2.nextToken();
				if ( servName.equalsIgnoreCase ( "gamelist" ) )
				{
					//output game list
					out.println ( GAME_LIST_BEGIN );
					for ( int i = 0 ; i < partidas.size() ; i++ )
					{
						PartidaEnCurso pec = (PartidaEnCurso) partidas.get(i);
						//current format: id. name (pl/pl)
						out.println ( GAME_LIST_LINE + " " + (i+1) + ". " + pec.getNombre() + " " + "(" + pec.getPlayers() + "/" + pec.getMaxPlayers() + ")" ); 
					}
					out.println ( GAME_LIST_END );
				}
				else if ( servName.equalsIgnoreCase ( "gamejoin" ) )
				{
					String id = st2.nextToken();
					int part = -1;
					try
					{
						part = Integer.valueOf ( id ).intValue();
						Debug.println("Part: " + part);
					}
					catch ( NumberFormatException nfe )
					{
						;
					}
					if ( part > 0 && part <= partidas.size() )
					{
						PartidaEnCurso p = (PartidaEnCurso) partidas.get(part-1);
						if ( p.getPlayers() < p.getMaxPlayers() )
						{
		
							Debug.println("Returning game.");
							return p;		
						}
						else
							out.println( ERRORMSG + " Esa partida ha alcanzado su número máximo de usuarios, no puedes entrar.\n");
					}
					else
					{
						out.println( ERRORMSG + " ID incorrecta" );
					}
					
				} //end service gamejoin
				else
				{
					out.println( UNSUPPORTED_SERVICE + " " + servName );
				} //end unknown service
			} //end if callservice
			else
			{
				out.println ( UNRECOGNIZED_MESSAGE + " " + str );
			}
		
		
			out.flush();
		
		} //end while (command-processing) loop		
	
		return null;
	
	} //end function

}



