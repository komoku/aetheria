/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import javax.swing.*;
import javax.swing.border.*;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.config.AGEConfiguration;

import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class SwingRemoteClientWindow extends JInternalFrame
{


	private JTextField tfIp = new JTextField("127.0.0.1",15);
	private JTextField tfPuerto = new JTextField("8009",5);
	private JButton botonConectar = new JButton( UIMessages.getInstance().getMessage("remote.connect") ); //Conectar
	private JTextPane logPane;
	private JList partidasList;
	private JButton botonJoinear = new JButton( UIMessages.getInstance().getMessage("remote.join") ); //Unirse
	private JPanel curGamesTab;
	
	private ARSPSocket arsps;
	
	private JDesktopPane escritorio;
	
	private JTabbedPane panelFichas;
	
	public JDesktopPane getEscritorio()
	{
		return escritorio;
	}

	public void initPartidasTab ( Vector partidas , Vector servicios )
	{
		partidasList.setListData(partidas);
		if ( partidas.size() > 0 )
			partidasList.setSelectedIndex(0);
		panelFichas.addTab ( UIMessages.getInstance().getMessage("remote.games") , curGamesTab ); //Partidas
		if ( servicios.contains ( "gamejoin" ) )
		{
			botonJoinear.setEnabled(true);
			this.getRootPane().setDefaultButton(botonJoinear);
		}
		else
		{
			botonJoinear.setEnabled(false);
		}
		panelFichas.setSelectedComponent(curGamesTab);
	}

	public SwingRemoteClientWindow ( JDesktopPane escritorio )
	{
	
		super ( UIMessages.getInstance().getMessage("remote.frame.title") , true , true , true , true ); //Partida remota
		
		this.escritorio = escritorio;
		
		setSize(550,550);
	
		//load last IP from properties
		String ip = AGEConfiguration.getInstance().getProperty("lastRemoteIp");
		if ( ip != null ) tfIp.setText(ip);
		
		
		panelFichas = new JTabbedPane();
		JPanel contentPane = new JPanel();
		setContentPane ( contentPane );
		contentPane.setLayout(new BorderLayout());
		contentPane.add(panelFichas,BorderLayout.CENTER);
		
		JPanel statusTab = new JPanel( new BorderLayout() /*new GridLayout ( 2 , 1 )*/ );
		
		JPanel connectSubPanel = new JPanel();
		JPanel con1 = new JPanel();
		JPanel con2 = new JPanel();
		JPanel con3 = new JPanel(new GridLayout(1,3));
		con1.add ( new JLabel( UIMessages.getInstance().getMessage("remote.ip.address") ) );
		con1.add ( tfIp );
		con2.add ( new JLabel( UIMessages.getInstance().getMessage("remote.port") ) );
		con2.add ( tfPuerto );
		con3.add ( new JPanel() );	con3.add ( new JPanel() );	
		con3.add ( botonConectar );	
		connectSubPanel.setLayout ( new GridLayout ( 3 , 1 ) );
		connectSubPanel.add ( con1 );
		connectSubPanel.add ( con2 );
		connectSubPanel.add ( con3 );
		connectSubPanel.setBorder ( BorderFactory.createTitledBorder( UIMessages.getInstance().getMessage("remote.connect") ) );
		statusTab.add(connectSubPanel,BorderLayout.NORTH);
		
		JPanel logSubPanel = new JPanel( new GridLayout(1,1) );
		logPane = new JTextPane();
		logSubPanel.add(new JScrollPane(logPane));
		statusTab.add(logSubPanel,BorderLayout.CENTER);
		
		panelFichas.addTab( UIMessages.getInstance().getMessage("remote.server") ,statusTab); //Servidor
	
		//tab de partidas. En principio, not visible, not added.
	
		curGamesTab = new JPanel ( new GridLayout ( 1 , 2 ) );
		
		JPanel listSubPanel = new JPanel( new GridLayout(1,1) );
		partidasList = new JList();
		listSubPanel.add ( new JScrollPane ( partidasList ) );
		JPanel actionsSubPanel = new JPanel();
		actionsSubPanel.setLayout ( new GridLayout ( 3 , 1 ) );
		actionsSubPanel.add ( botonJoinear );
		curGamesTab.add ( listSubPanel );
		curGamesTab.add ( actionsSubPanel );
		
		botonJoinear.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					if ( arsps != null )
					{
						if ( partidasList.getSelectedIndex() != -1 )
						{
							arsps.joinGame ( partidasList.getSelectedIndex() + 1 );							
							SwingRemoteClientWindow.this.dispose();
						}
					}
				}
			}
		);
		
	
		final SwingRemoteClientWindow esto = this;
	
	
		botonConectar.addActionListener 
		(
			new ActionListener() 
			{
				public void actionPerformed ( ActionEvent evt )
				{
					//connect to given IP, port
					;
				
					try
					{
				
						InetAddress dire = InetAddress.getByName ( tfIp.getText() );
						int port = Integer.valueOf ( tfPuerto.getText() ).intValue();
				
						Socket socket = new Socket(dire,port);
				
						arsps = new ARSPSocket ( socket , esto );
						
						arsps.start();
				
						/*
						
						logPane.setText ( logPane.getText() + "Socket connected.\n" );
						
						System.out.println("Socket connected.\n");
						
						//Initialization
						
						in = new BufferedReader ( new InputStreamReader ( socket.getInputStream() ) );
						out = new PrintWriter ( socket.getOutputStream() );
						
						writeSocket ( PROTOCOL_VERSION_STATEMENT + " 1.0" );
						String line = readSocket();
						StringTokenizer st = new StringTokenizer ( line );
						String tok = st.nextToken();
						if ( tok == PROTOCOL_VERSION_ACK )
						{
							String tok2 = st.nextToken();
							if ( !tok2.equalsIgnoreCase("OK") )	
							{
								logPane.setText ( logPane.getText() + "Protocol error.\n" );
								return;
							}
						}
						else
						{
							logPane.setText ( logPane.getText() + "Protocol error.\n" );
							return;
						}
						
						System.out.println("Handshake done.\n");
						
						*/
						
						//store ip for next time
						AGEConfiguration.getInstance().setProperty("lastRemoteIp",tfIp.getText());
						AGEConfiguration.getInstance().storeProperties();
						
				
					}
					catch ( UnknownHostException uoe )
					{
						logPane.setText ( logPane.getText() + uoe + "\n" );
					}
					
					catch ( IOException ioe )
					{
						logPane.setText ( logPane.getText() + ioe + "\n" );
					}
					
				
				}
			}
		);
	
	
		setVisible(true);
		this.getRootPane().setDefaultButton(botonConectar);
	
	
	}
	
	public JTextPane getLogPane()
	{
		return logPane;
	}
	




}


class ARSPSocket extends Thread implements ARSPConstants
{

	Socket socket;
	SwingRemoteClientWindow ventana;
	JTextPane logPane;
	
	private BufferedReader in; //for socket
	private PrintWriter out; //for socket
	

	public ARSPSocket ( Socket s , SwingRemoteClientWindow srcw )
	{
		this.socket = s;
		this.ventana = srcw;
		this.logPane = ventana.getLogPane();
	}
	
	public void run()
	{
	
	
		try
		{
				
						//InetAddress dire = InetAddress.getByName ( tfIp.getText() );
						//int port = Integer.valueOf ( tfPuerto.getText() ).intValue();
				
						//Socket socket = new Socket(dire,port);
				
			logPane.setText ( logPane.getText() + "Socket connected.\n" );
						
			System.out.println("Socket connected.\n");
						
			//Initialization
						
			in = new BufferedReader ( new InputStreamReader ( socket.getInputStream() ) );
			out = new PrintWriter ( socket.getOutputStream() )
{
    public void println(String linea)
    {
	print(linea);
	print("\r\n");
    }

};
						
			writeSocket ( PROTOCOL_VERSION_STATEMENT + " 1.0" );
			/*
			try
			{
				sleep(2000);
			}
			catch ( InterruptedException ie )
			{
				;
			}
			writeSocket ( PROTOCOL_VERSION_STATEMENT + " 1.0" );
			*/
			System.out.println("B4 readSocket()");
			String line = readSocket();
			System.out.println("After readSocket(). Line="+line);
			StringTokenizer st = new StringTokenizer ( line );
			String tok = st.nextToken();
			if ( tok.equalsIgnoreCase ( PROTOCOL_VERSION_ACK ) )
			{
				String tok2 = st.nextToken();
				if ( !tok2.equalsIgnoreCase("OK") )	
				{
					logPane.setText ( logPane.getText() + "Protocol error.\n" );
					return;
				}
			}
			else
			{
				logPane.setText ( logPane.getText() + "Protocol error.\n" );
				return;
			}
			
			//handshake done
			//get services and games
			
			Vector servicios = new Vector(); //list of services
			Vector partidas = new Vector(); //list of games
			writeSocket ( SERVICE_LIST_REQUEST );
			line = readSocket();
		    st = new StringTokenizer ( line );			
			tok = st.nextToken();
			if ( tok.equalsIgnoreCase ( SERVICE_LIST_BEGIN ) )
			{
				boolean terminamos = false;
				while ( !terminamos )
				{
					line = readSocket();
					st = new StringTokenizer ( line );
					tok = st.nextToken();
					if ( tok.equalsIgnoreCase ( SERVICE_LIST_LINE ) )
					{
						servicios.add ( st.nextToken() );
					}
					else if ( tok.equalsIgnoreCase ( SERVICE_LIST_END ) )
					{
						terminamos = true;
					}
					else
					{
						logPane.setText ( logPane.getText() + "Protocol error.\n" );
						return;
					}
				}
			
			}
			else
			{
				logPane.setText ( logPane.getText() + "Protocol error.\n" );
				return;
			}						
			System.out.println("Services gotten.");
			
			//get game list
			if ( servicios.contains ( "gamelist" ) )
			{
				writeSocket ( CALL_SERVICE + " gamelist" );
				line = readSocket();
			    st = new StringTokenizer ( line );			
				tok = st.nextToken();
				if ( tok.equalsIgnoreCase ( GAME_LIST_BEGIN ) )
				{
					boolean terminamos = false;
					while ( !terminamos )
					{
						line = readSocket();
						st = new StringTokenizer ( line );
						tok = st.nextToken();
						if ( tok.equalsIgnoreCase ( GAME_LIST_LINE ) )
						{
							partidas.add ( st.nextToken("") );
						}
						else if ( tok.equalsIgnoreCase ( GAME_LIST_END ) )
						{
							terminamos = true;
						}
						else
						{
							logPane.setText ( logPane.getText() + "Protocol error.\n" );
							return;
						}
					}
				
				} //end reading games
			} //end if gamelist service available
			System.out.println("Games gotten.\n");
			
			//create Partidas tab if possible
			if ( partidas.size() > 0 )
			{
				ventana.initPartidasTab ( partidas , servicios );
			}
						
				
		}

		catch ( IOException ioe )
		{
			logPane.setText ( logPane.getText() + ioe + "\n" );
		}
	
	
	
	
	
	}
	
	public void joinGame ( final int id )
	{
		writeSocket ( CALL_SERVICE + " gamejoin " + id );
		SwingAetheriaGameLoader sagl = new SwingAetheriaGameLoader ( UIMessages.getInstance().getMessage("remote.frame.title") , ventana.getEscritorio() ); //Partida remota
		ServerProxy sp = new ServerProxy ( socket , (MultimediaInputOutputClient) sagl.getClient() );
		sp.start();
	}
	
	/*
	public void joinGame ( final int id ) throws IOException
	{
	
		Thread thr = new Thread()
		{
		
			public void run()
			{
		
				try
				{
		
					writeSocket ( CALL_SERVICE + " gamejoin " + id );
					

					//atender requests del server
					
					boolean terminamos = false;
					
					while ( !terminamos )
					{
					
						String linea = readSocket();
						StringTokenizer st = new StringTokenizer(linea);
						String tok;
						if ( st.hasMoreTokens() ) tok = st.nextToken();
						else tok = "";
						if ( tok.equalsIgnoreCase ( CLIENT_TYPE_REQUEST ) )
						{
							writeSocket ( CLIENT_TYPE_REPLY + " sound images title color: Aetheria Game Engine 0.5.6b Embedded Client"); 
						}
						
						
						else
						{
							//init the server proxy! init the game!
						
							ServerProxy sp = new ServerProxy ( socket , MultimediaInputOutputClient );
						
						
						}
						
						
						
					}
					

				
				}
				catch ( IOException ioe )
				{
					System.out.println(ioe);ioe.printStackTrace();
				}
				
			}
		
		};
		
		thr.start();
			
	}
	*/

	private void writeSocket ( String s )
	{
		out.println ( s );
		out.flush();
		logPane.setText ( logPane.getText() + "[client] " + s + "\n" );
	}
	private String readSocket() throws IOException
	{
		String s = in.readLine();
		logPane.setText ( logPane.getText() + "[server] " + s + "\n" );
		return s;
	}

}
