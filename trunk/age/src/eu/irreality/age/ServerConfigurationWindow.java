/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import eu.irreality.age.server.IrcServerEntry;
import eu.irreality.age.server.ServerConfigurationOptions;
import eu.irreality.age.server.ServerHandler;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;



//Singleton
public class ServerConfigurationWindow extends JDialog
{

	public ServerConfigurationOptions getEntrada ( )
	{
		int tPort,aPort;
		try
		{
			tPort = Integer.valueOf ( tfTPort.getText() ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			tPort = 8010;
		}
		try
		{
			aPort = Integer.valueOf ( tfAPort.getText() ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			aPort = 8009;
		}
		//build irc server list, dedicated game list
		java.util.List servList = new ArrayList();
		java.util.List gList = new ArrayList();
		for ( int i = 0 ; i < ircServerVector.size() ; i++ )
			servList.add ( ircServerVector.get(i) );
		for ( int i = 0 ; i < gameVector.size() ; i++ )
			gList.add ( gameVector.get(i) );	
		return new ServerConfigurationOptions (
			cbAge.isSelected(), cbTel.isSelected() , cbIrc.isSelected() , cbInitOnStartup.isSelected(),
			tPort ,	aPort ,	servList , gList );
	}
	
	public void saveConfiguration() throws FileNotFoundException , IOException
	{
		ObjectOutputStream oos = new ObjectOutputStream ( new FileOutputStream ( configurationFile ) );
		oos.writeObject ( getEntrada() );
		ServerHandler.getInstance().setServerConfigurationOptions(getEntrada());
	}
	
	public static File configurationFile = new File("server.opt");

	private JCheckBox cbTel = new JCheckBox("Servir por Telnet, puerto:");
	private JCheckBox cbAge = new JCheckBox("Servir por AGE, puerto:");
	private JCheckBox cbIrc = new JCheckBox("Servir por IRC");
	private JCheckBox cbInitOnOK = new JCheckBox("Arrancar las partidas dedicadas al pulsar Aceptar");
	private JCheckBox cbInitOnStartup = new JCheckBox("Arrancar las partidas dedicadas al iniciar AGE");
	private JTextField tfTPort = new JTextField("8010") , tfAPort = new JTextField("8009") ;
	
	private DefaultListModel ircServerVector = new DefaultListModel();
	private JList ircServerList = new JList(ircServerVector);	
	private JButton islAdd = new JButton("Agregar...");
	private JButton islRemove = new JButton("Quitar");
	private JButton islConfig = new JButton("Configurar");
	
	private DefaultListModel gameVector = new DefaultListModel();
	private JList gameList = new JList(gameVector);
	private JButton gameAdd = new JButton("Agregar...");
	private JButton gameRemove = new JButton("Quitar");
	private JButton gameConfig = new JButton("Configurar");
	
	private JButton botonAceptar = new JButton("Aceptar");
	private JButton botonCancelar = new JButton("Cancelar");
	
	private static ServerConfigurationWindow theInstance;
	
	private static JFrame madre;
	
	public static void setMadre ( JFrame parent )
	{
		madre = parent;
	}
	
	public static ServerConfigurationWindow getInstance()
	{
		if ( theInstance == null )
		{
			try
			{
				ObjectInputStream ois = new ObjectInputStream ( new FileInputStream ( configurationFile ) );
				ServerConfigurationOptions sco = (ServerConfigurationOptions) ois.readObject();
				theInstance = new ServerConfigurationWindow ( sco );
			}
			catch ( Exception e )
			{
				theInstance = new ServerConfigurationWindow();
			}
		}
		return theInstance;	
	}
	
	private ServerConfigurationWindow ( ServerConfigurationOptions sco )
	{
		this();
		cbAge.setSelected ( sco.sirveAge() );
		cbTel.setSelected ( sco.sirveTelnet() );
		cbIrc.setSelected ( sco.sirveIrc() );
		cbInitOnStartup.setSelected ( sco.initOnStartup() );
		cbInitOnOK.setSelected ( false ); //this is not an option, we don't init on OK unless explicitly told to do so. 
		tfTPort.setText( String.valueOf(sco.getPuertoTelnet()) );
		tfAPort.setText( String.valueOf(sco.getPuertoAge()) );
		java.util.List lIrc = sco.getListaServidoresIrc();
		java.util.List lDed = sco.getListaPartidasDedicadas();
		for ( int i = 0 ; i < lIrc.size() ; i++ )
			ircServerVector.addElement ( lIrc.get(i) );
		for ( int i = 0 ; i < lDed.size() ; i++ )
			gameVector.addElement ( lDed.get(i) );	
	}
	
	public void setVisible(boolean visible)
	{
		if(visible) cbInitOnOK.setSelected(false);
		super.setVisible(visible);
	}
	
	private ServerConfigurationWindow ( )
	{
	
		super(madre,true);
	
		setTitle("Configuración del servidor");
		
		getContentPane().setLayout ( new BorderLayout() );
		JPanel panelPrincipal = new JPanel ( new GridLayout ( 3 , 1 ) );
			JPanel subPan1 = new JPanel();
				subPan1.setLayout ( new GridLayout ( 6 , 1 ) );
				JPanel subPan11 = new JPanel();
					subPan11.add ( cbTel );
					//subPan11.add ( new JLabel("Servir por Telnet, puerto:") );
					subPan11.add ( tfTPort );
				JPanel subPan12 = new JPanel();
					subPan12.add ( cbAge );
					//subPan12.add ( new JLabel("Servir por AGE, puerto:") );
					subPan12.add ( tfAPort );
				JPanel subPan13 = new JPanel();
					subPan13.add ( cbIrc );
					//subPan13.add ( new JLabel("Servir por IRC") );
				JPanel subPan15 = new JPanel();
					subPan15.add ( cbInitOnStartup );
				JPanel subPan16 = new JPanel();
					subPan16.add ( cbInitOnOK );
				subPan1.add(subPan12);
				subPan1.add(subPan11);
				subPan1.add(subPan13);
				subPan1.add(new JPanel());
				subPan1.add(subPan15);
				subPan1.add(subPan16);
				subPan1.setBorder ( BorderFactory.createTitledBorder("Activar/Desactivar Servidores") );
			final JPanel subPan2 = new JPanel();
				subPan2.setBorder ( BorderFactory.createTitledBorder("Servidor IRC") );
				subPan2.setLayout ( new BorderLayout() );
				JPanel subPan21 = new JPanel();
					subPan21.add ( new JScrollPane(ircServerList) ); //lista de IrcServerEntry
				JPanel subPan22 = new JPanel();
					subPan22.add ( islAdd );
					subPan22.add ( islRemove );
					subPan22.add ( islConfig );
				subPan2.add ( subPan21 , BorderLayout.CENTER );
				subPan2.add ( subPan22 , BorderLayout.SOUTH );
			JPanel subPan3 = new JPanel();
				subPan3.setBorder ( BorderFactory.createTitledBorder("Partidas dedicadas") );
				subPan3.setLayout ( new BorderLayout() );
				JPanel subPan31 = new JPanel();
					subPan31.add ( new JScrollPane(gameList) );
				JPanel subPan32 = new JPanel();
					subPan32.add ( gameAdd );
					subPan32.add ( gameRemove );
					subPan32.add ( gameConfig );
				subPan3.add ( subPan31 , BorderLayout.CENTER );
				subPan3.add ( subPan32 , BorderLayout.SOUTH );
			panelPrincipal.add ( subPan1 );
			panelPrincipal.add ( subPan2 );
			panelPrincipal.add ( subPan3 );
		JPanel panelBotones = new JPanel ( new GridLayout ( 1 , 4 ) );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( botonAceptar );
			panelBotones.add ( botonCancelar );
		panelPrincipal.setBorder ( BorderFactory.createEtchedBorder() );
		getContentPane().add(panelPrincipal , BorderLayout.CENTER );
		getContentPane().add(panelBotones , BorderLayout.SOUTH );
		pack();
		
		islAdd.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					AddIrcNetworkWindow ainw = new AddIrcNetworkWindow( getInstance());
					ainw.setVisible(true);
				}
			}
		);
		
		islConfig.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					IrcServerEntry ise = removeAndGetSelectedIrcServerEntry();
					if ( ise == null ) return;
					AddIrcNetworkWindow ainw = new AddIrcNetworkWindow( getInstance() , ise );
					ainw.setVisible(true);
				}
			}
		);
		
		islRemove.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					removeSelectedIrcServerEntries();
				}
			}
		);
		
		gameAdd.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					AddGameWindow agw = new AddGameWindow( getInstance() );
					agw.setVisible(true);
				}
			}
		);
		
		gameConfig.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					PartidaEntry pe = removeAndGetSelectedPartidaEntry();
					if ( pe == null ) return;
					AddGameWindow ainw = new AddGameWindow( getInstance() , pe );
					ainw.setVisible(true);
				}
			}
		);
		
		gameRemove.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					removeSelectedPartidaEntries();
				}
			}
		);
		
		botonCancelar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					dispose();
					theInstance = new ServerConfigurationWindow ( ServerHandler.getInstance().getServerConfigurationOptions() );
				}
			}
		);
		
		botonAceptar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					dispose();
					try
					{
						saveConfiguration();
						if ( cbInitOnOK.isSelected() )
							ServerHandler.getInstance().initPartidasDedicadas(SwingAetheriaGUI.getInstance().panel);
					}
					catch ( Exception e )
					{
						JOptionPane.showMessageDialog ( null , "No se ha podido guardar la configuración del servidor a fichero: " + e , "¡Oops!" , JOptionPane.ERROR_MESSAGE );
					}
				}
			}
		);
		
		cbIrc.addItemListener
		(
			new ItemListener()
			{
				public void itemStateChanged ( ItemEvent evt )
				{
					if ( evt.getStateChange() == ItemEvent.SELECTED )
					{
						subPan2.setEnabled(true);
						islAdd.setEnabled(true);
						islRemove.setEnabled(true);
						islConfig.setEnabled(true);
					}
					else if ( evt.getStateChange() == ItemEvent.DESELECTED )
					{
						subPan2.setEnabled(false);
						islAdd.setEnabled(false);
						islRemove.setEnabled(false);
						islConfig.setEnabled(false);
					}
				}
			}
		);
		//init button state
		islAdd.setEnabled(cbIrc.isSelected());
		islRemove.setEnabled(cbIrc.isSelected());
		islConfig.setEnabled(cbIrc.isSelected());
	
	}
	
	public void addIrcServerEntry ( IrcServerEntry ise )
	{
		ircServerVector.addElement ( ise );
		ircServerList.repaint();
	}
	public void removeSelectedIrcServerEntries ( )
	{
		int[] indices = ircServerList.getSelectedIndices();
		for ( int i = indices.length-1 ; i >= 0 ; i-- )
		{
			ircServerVector.removeElementAt(indices[i]);
		}
	}
	
	public IrcServerEntry removeAndGetSelectedIrcServerEntry() //just one
	{
		int[] indices = ircServerList.getSelectedIndices();
		if ( indices.length < 1 ) return null;
		IrcServerEntry ise = (IrcServerEntry) ircServerVector.get(indices[0]);
		ircServerVector.removeElementAt(indices[0]);
		return ise;
	}
	
	public void addPartidaEntry ( PartidaEntry pe )
	{
		gameVector.addElement ( pe );
		gameList.repaint();
	}
	public void removeSelectedPartidaEntries ( )
	{
		int[] indices = gameList.getSelectedIndices();
		for ( int i = indices.length-1 ; i >= 0 ; i-- )
		{
			gameVector.removeElementAt(indices[i]);
		}
	}
	
	public PartidaEntry removeAndGetSelectedPartidaEntry() //just one
	{
		int[] indices = gameList.getSelectedIndices();
		if ( indices.length < 1 ) return null;
		PartidaEntry pe = (PartidaEntry) gameVector.get(indices[0]);
		gameVector.removeElementAt(indices[0]);
		return pe;
	}
	



}





class AddGameWindow extends JDialog
{

	private DefaultListModel gamesVector = new DefaultListModel();
	private JList gamesList = new JList ( gamesVector );
	private JTextPane gameInfoArea = new JTextPane();
	
	private JTextField gameNameTextField = new JTextField("Partida");
	private JTextField maxPlayersTextField = new JTextField("9999");
	
	//maybe login-pass thingies? password requirement?
	
	private JCheckBox passwordRequiredBox = new JCheckBox ("Contraseña de servidor:");
	private JTextField passwordTextField = new JTextField ( "alohomora" );
	
	private JCheckBox cbAge = new JCheckBox("AGE");
	private JCheckBox cbTelnet = new JCheckBox("Telnet");
	private JCheckBox cbIrc = new JCheckBox("IRC");
	
	private JButton botonAceptar = new JButton("Aceptar");
	private JButton botonCancelar = new JButton("Cancelar");

	private ServerConfigurationWindow madre;
	
	private GameInfo informacionJuego;

	private void updateGameInfoArea()
	{
		gameInfoArea.setText( informacionJuego.toLongString() );
		gameNameTextField.setText("Partida de " + gamesList.getSelectedValue().toString() );
	}

	public AddGameWindow ( final ServerConfigurationWindow madre )
	{
		
		super ( madre , true );
		
		this.madre = madre;
	
		setTitle("Partida dedicada");
		
		setSize ( 600 , 400 );
		
		//get game info list
		GameInfo[] gi = GameInfo.getListOfGames();
		for ( int i = 0 ; i < gi.length ; i++ )
			gamesVector.addElement ( gi[i] );		
		
		getContentPane().setLayout ( new BorderLayout() );
		JPanel panelPrincipal = new JPanel ( new GridLayout(3,1)  );
			JPanel subPan1 = new JPanel(); //parecido a un GameChoosingPanel
				gamesList.addListSelectionListener ( new ListSelectionListener()
				{
					public void valueChanged ( ListSelectionEvent evt )
					{
							informacionJuego = (GameInfo)gamesList.getSelectedValue();
							updateGameInfoArea();

					}
				} );
				
					gamesList.setSelectedIndex(0);
						
				subPan1.setLayout ( new GridLayout ( 1 , 2 ) );
					/*
					JPanel p1 = new JPanel( new BorderLayout( ));
					p1.setSize ( 300 , 200  );
					subPan1.add ( p1 );
					p1.add ( new JScrollPane ( gamesList , JScrollPane.VERTICAL_SCROLLBAR_ALWAYS , JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED  ) , BorderLayout.CENTER );
					JPanel p2 = new JPanel( new BorderLayout (  ) );
					p2.setSize ( 300 , 200 );
					subPan1.add ( p2 );
					p2.add ( new JScrollPane ( gameInfoArea , JScrollPane.VERTICAL_SCROLLBAR_ALWAYS , JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED  ) , BorderLayout.CENTER );
					*/
					//JScrollPane jsp1 = new JScrollPane ( gamesList , JScrollPane.VERTICAL_SCROLLBAR_ALWAYS , JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED  );
					//JScrollPane jsp2 = new JScrollPane ( gameInfoArea , JScrollPane.VERTICAL_SCROLLBAR_ALWAYS , JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED  );
					//jsp1.setMaximumSize ( new Dimension ( 2 , 2 ) );
					//gamesList.setSize(2,2);
					//subPan1.add ( jsp1 );
					//JPanel panInfAr = new JPanel ( new BorderLayout() );
					//panInfAr.add ( gameInfoArea , BorderLayout.CENTER );
					//subPan1.add ( new JScrollPane ( panInfAr ) );
					//gameInfoArea.setSize(2,2);
					//subPan1.add ( jsp2 );
					//subPan1.add ( gameInfoArea );
					subPan1.add ( new JScrollPane ( gamesList , JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED , JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) );
					JPanel ctl = new JPanel ( new BorderLayout() );
					gamesList.setBackground ( Color.black );
					gamesList.setForeground ( Color.white );
					gameInfoArea.setBackground ( Color.black );
					gameInfoArea.setForeground ( Color.white );
					//ctl.setSize(200,200);
					//ctl.add ( gameInfoArea );
					//ctl.setSize(200,200);
					//subPan1.add(ctl);
					subPan1.add ( new JScrollPane ( gameInfoArea , JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED , JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) );
			JPanel subPan2 = new JPanel ( new GridLayout ( 3 , 1 ) );
				JPanel subPan21 = new JPanel();
					subPan21.add ( new JLabel("Nombre de la partida:") );
					subPan21.add ( gameNameTextField );
				JPanel subPan22 = new JPanel();
					subPan22.add ( new JLabel("Máximo de jugadores:") );
					subPan22.add ( maxPlayersTextField );
				JPanel subPan23 = new JPanel();
					subPan23.add ( passwordRequiredBox );
					subPan23.add ( passwordTextField );
				subPan2.add ( subPan21 );
				subPan2.add ( subPan22 );
				subPan2.add ( subPan23 );
			JPanel subPan3 = new JPanel();
				subPan3.setLayout ( new GridLayout( 2 , 1 ) );
				JPanel subPan31 = new JPanel();
				subPan3.setBorder ( BorderFactory.createTitledBorder("Servir por:") );
				subPan31.add ( cbAge );
				subPan31.add ( cbTelnet );
				subPan31.add ( cbIrc );
					//JTextPane jtp = new JTextPane();
					//jtp.setText("(nótese que, para que se sirva la partida, es necesario seleccionar AGE, telnet y/o IRC también en la ventana de configuración de servidor, junto con un puerto cuando corresponda)");
					//subPan24.add ( jtp );
				subPan3.add ( subPan31 );
				subPan3.add ( new JLabel("(sólo si dichos servidores están activos)" , JLabel.CENTER ) );
				//subPan2.add ( subPan24 );
			panelPrincipal.add ( subPan1 );
			panelPrincipal.add ( subPan2 );
			panelPrincipal.add ( subPan3 );
		JPanel panelBotones = new JPanel ( new GridLayout ( 1 , 4 ) );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( botonAceptar );
			panelBotones.add ( botonCancelar );
		panelPrincipal.setBorder ( BorderFactory.createEtchedBorder() );
		getContentPane().add(panelPrincipal , BorderLayout.CENTER );
		getContentPane().add(panelBotones , BorderLayout.SOUTH );	
		//pack();
		
		botonCancelar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					dispose();
				}
			}
		);
		
		botonAceptar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					PartidaEntry pe = getEntrada();
					madre.addPartidaEntry ( pe );
					dispose();
				}
			}
		);
		
		
	}
	
	
	boolean editMode = false;
	
	//for editing
	public AddGameWindow ( final ServerConfigurationWindow madre , final PartidaEntry aEditar )
	{
		
		super ( madre , true );
		
		this.madre = madre;
	
		editMode = true;
	
		setTitle("Partida dedicada");
		
		setSize ( 600 , 400 );
		
		//get game info list
		GameInfo[] gi = GameInfo.getListOfGames();
		for ( int i = 0 ; i < gi.length ; i++ )
			gamesVector.addElement ( gi[i] );		
		
		getContentPane().setLayout ( new BorderLayout() );
		JPanel panelPrincipal = new JPanel ( new GridLayout(3,1)  );
			JPanel subPan1 = new JPanel(); //parecido a un GameChoosingPanel
				gamesList.addListSelectionListener ( new ListSelectionListener()
				{
					public void valueChanged ( ListSelectionEvent evt )
					{
							informacionJuego = (GameInfo)gamesList.getSelectedValue();
							updateGameInfoArea();

					}
				} );
				
				//choose selected index
				int i;
				for ( i = 0 ; i < gamesVector.size() ; i++ )
				{
					System.out.println("Infos for index " + i + ": ");
					System.out.println( "" + ((GameInfo)gamesVector.get(i)) );
					System.out.println( "" + (aEditar.getGameInfo()) );
					if ( ((GameInfo)gamesVector.get(i)).equals(aEditar.getGameInfo()) )
						break;
				}
				
				if ( i < gamesVector.size() )
					gamesList.setSelectedIndex(i);
				else
					gamesList.setSelectedIndex(0);
				
				
				subPan1.setLayout ( new GridLayout ( 1 , 2 ) );


					subPan1.add ( new JScrollPane ( gamesList , JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED , JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) );
					JPanel ctl = new JPanel ( new BorderLayout() );
					gamesList.setBackground ( Color.black );
					gamesList.setForeground ( Color.white );
					gameInfoArea.setBackground ( Color.black );
					gameInfoArea.setForeground ( Color.white );
					//ctl.setSize(200,200);
					//ctl.add ( gameInfoArea );
					//ctl.setSize(200,200);
					//subPan1.add(ctl);
					subPan1.add ( new JScrollPane ( gameInfoArea , JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED , JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) );
			JPanel subPan2 = new JPanel ( new GridLayout ( 3 , 1 ) );
				JPanel subPan21 = new JPanel();
					subPan21.add ( new JLabel("Nombre de la partida:") );
					gameNameTextField.setText(aEditar.getName());
					subPan21.add ( gameNameTextField );
				JPanel subPan22 = new JPanel();
					subPan22.add ( new JLabel("Máximo de jugadores:") );
					maxPlayersTextField.setText(String.valueOf(aEditar.getMaxPlayers()));
					subPan22.add ( maxPlayersTextField );
				JPanel subPan23 = new JPanel();
					subPan23.add ( passwordRequiredBox );
					passwordTextField.setText(String.valueOf(aEditar.getPassword()));
					subPan23.add ( passwordTextField );
				subPan2.add ( subPan21 );
				subPan2.add ( subPan22 );
				subPan2.add ( subPan23 );
			JPanel subPan3 = new JPanel();
				subPan3.setLayout ( new GridLayout( 2 , 1 ) );
				JPanel subPan31 = new JPanel();
				subPan3.setBorder ( BorderFactory.createTitledBorder("Servir por:") );
				cbAge.setSelected(aEditar.sirveAge());
				cbTelnet.setSelected(aEditar.sirveTelnet());
				cbIrc.setSelected(aEditar.sirveIrc());
				subPan31.add ( cbAge );
				subPan31.add ( cbTelnet );
				subPan31.add ( cbIrc );
					//JTextPane jtp = new JTextPane();
					//jtp.setText("(nótese que, para que se sirva la partida, es necesario seleccionar AGE, telnet y/o IRC también en la ventana de configuración de servidor, junto con un puerto cuando corresponda)");
					//subPan24.add ( jtp );
				subPan3.add ( subPan31 );
				subPan3.add ( new JLabel("(sólo si dichos servidores están activos)" , JLabel.CENTER ) );
				//subPan2.add ( subPan24 );
			panelPrincipal.add ( subPan1 );
			panelPrincipal.add ( subPan2 );
			panelPrincipal.add ( subPan3 );
		JPanel panelBotones = new JPanel ( new GridLayout ( 1 , 4 ) );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( botonAceptar );
			panelBotones.add ( botonCancelar );
		panelPrincipal.setBorder ( BorderFactory.createEtchedBorder() );
		getContentPane().add(panelPrincipal , BorderLayout.CENTER );
		getContentPane().add(panelBotones , BorderLayout.SOUTH );	
		//pack();
		
		botonCancelar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					if ( editMode ) //cancelled edit: re-add it
					madre.addPartidaEntry ( aEditar );
					dispose();
				}
			}
		);
		
		botonAceptar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					PartidaEntry pe = getEntrada();
					madre.addPartidaEntry ( pe );
					dispose();
				}
			}
		);
		
		
	}
	
	
	
	
	public PartidaEntry getEntrada()
	{
		try
		{
			return new PartidaEntry ( (GameInfo) gamesList.getSelectedValue() , gameNameTextField.getText() , maxPlayersTextField.getText() , passwordRequiredBox.isSelected()?passwordTextField.getText():null , cbAge.isSelected() , cbTelnet.isSelected() , cbIrc.isSelected() ); 
		}
		catch ( NumberFormatException nfe )
		{
			//defaultear el maxplayers a 9999
			return new PartidaEntry ( (GameInfo) gamesList.getSelectedValue() , gameNameTextField.getText() , 9999 , passwordRequiredBox.isSelected()?passwordTextField.getText():null , cbAge.isSelected() , cbTelnet.isSelected() , cbIrc.isSelected() ); 
		}
	}
	

}

//vale para add y edit
class AddIrcNetworkWindow extends JDialog
{

	public JTextField serverTextField = new JTextField("libres.irc-hispano.org");
	public JTextField portTextField = new JTextField("6667");
	public JTextField nickTextField = new JTextField("AGEserver");
	
	public JCheckBox cbQuery = new JCheckBox("Privado");
	public JCheckBox cbChannel = new JCheckBox("Canal");
	public JCheckBox cbDCC = new JCheckBox("DCC");

	public Vector channelVector = new Vector();

	public JList channelList = new JList(channelVector);

	private JButton botonAceptar = new JButton("Aceptar");
	private JButton botonCancelar = new JButton("Cancelar");

	private final ServerConfigurationWindow madre;
	
	private boolean editMode = false; //false:add, true:edit
	//private final IrcServerEntry aEditar;

	public AddIrcNetworkWindow ( final ServerConfigurationWindow madre )
	{
	
		super ( madre , true );
		
		this.madre = madre;
	
		setTitle("Servidor IRC");


		getContentPane().setLayout ( new BorderLayout() );
		JPanel panelPrincipal = new JPanel ( new GridLayout ( 2 , 1 ) );
			JPanel subPan1 = new JPanel();
				JPanel subPan11 = new JPanel();
					subPan11.add ( new JLabel("Servidor IRC:") );
					subPan11.add ( serverTextField );
				JPanel subPan12 = new JPanel();
					subPan12.add ( new JLabel("Puerto:") );
					subPan12.add ( portTextField );
				JPanel subPan13 = new JPanel();
					subPan13.add ( new JLabel("Nick:") );
					subPan13.add ( nickTextField ); 
				subPan1.add ( subPan11 );
				subPan1.add ( subPan12 );
				subPan1.add ( subPan13 );
			JPanel subPan2 = new JPanel();
				subPan2.setLayout ( new GridLayout ( 1 , 2 ) );
				JPanel subPan21 = new JPanel();
					subPan21.add ( cbQuery );
					subPan21.add ( cbChannel );
					subPan21.add ( cbDCC );
				JPanel subPan22 = new JPanel();
					subPan22.add ( channelList );
					channelVector.add ( "#aetheria" );
				subPan2.add ( subPan21 );
				subPan2.add ( subPan22 );
			panelPrincipal.add(subPan1);
			panelPrincipal.add(subPan2);
		JPanel panelBotones = new JPanel ( new GridLayout ( 1 , 4 ) );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( botonAceptar );
			panelBotones.add ( botonCancelar );
		panelPrincipal.setBorder ( BorderFactory.createEtchedBorder() );
		getContentPane().add(panelPrincipal , BorderLayout.CENTER );
		getContentPane().add(panelBotones , BorderLayout.SOUTH );	
				
			
		pack();	
		
		botonCancelar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					dispose();
				}
			}
		);
		
		botonAceptar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					IrcServerEntry ise = getEntrada();
					madre.addIrcServerEntry ( ise );
					dispose();
				}
			}
		);
	}
	
	
	
	
	
	
	
	
	
	public AddIrcNetworkWindow ( final ServerConfigurationWindow madre , final IrcServerEntry aEditar )
	{
		
		super ( madre , true );
	
		this.madre = madre;
		//this.aEditar = aEditar;
	
		setTitle("Servidor IRC");

		editMode = true;

		getContentPane().setLayout ( new BorderLayout() );
		JPanel panelPrincipal = new JPanel ( new GridLayout ( 2 , 1 ) );
			JPanel subPan1 = new JPanel();
				JPanel subPan11 = new JPanel();
					subPan11.add ( new JLabel("Servidor IRC:") );
					serverTextField.setText(aEditar.getServer());
					subPan11.add ( serverTextField );
				JPanel subPan12 = new JPanel();
					subPan12.add ( new JLabel("Puerto:") );
					portTextField.setText(String.valueOf(aEditar.getPort()));
					subPan12.add ( portTextField );
				JPanel subPan13 = new JPanel();
					subPan13.add ( new JLabel("Nick:") );
					nickTextField.setText(aEditar.getNick());
					subPan13.add ( nickTextField ); 
				subPan1.add ( subPan11 );
				subPan1.add ( subPan12 );
				subPan1.add ( subPan13 );
			JPanel subPan2 = new JPanel();
				subPan2.setLayout ( new GridLayout ( 1 , 2 ) );
				JPanel subPan21 = new JPanel();
					cbQuery.setSelected(aEditar.respondeAPrivados());
					cbChannel.setSelected(aEditar.respondeACanales());
					cbDCC.setSelected(aEditar.respondeADCC());
					subPan21.add ( cbQuery );
					subPan21.add ( cbChannel );
					subPan21.add ( cbDCC );
				JPanel subPan22 = new JPanel();
					subPan22.add ( channelList );
					channelVector = aEditar.getChannels();
				subPan2.add ( subPan21 );
				subPan2.add ( subPan22 );
			panelPrincipal.add(subPan1);
			panelPrincipal.add(subPan2);
		JPanel panelBotones = new JPanel ( new GridLayout ( 1 , 4 ) );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( new JPanel() );
			panelBotones.add ( botonAceptar );
			panelBotones.add ( botonCancelar );
		panelPrincipal.setBorder ( BorderFactory.createEtchedBorder() );
		getContentPane().add(panelPrincipal , BorderLayout.CENTER );
		getContentPane().add(panelBotones , BorderLayout.SOUTH );	
				
			
		pack();	
		
		botonCancelar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					if ( editMode = true )
						madre.addIrcServerEntry ( aEditar ); //pues se quita al abrir la ventana de editar
					dispose();
				}
			}
		);
		
		botonAceptar.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					IrcServerEntry ise = getEntrada();
					madre.addIrcServerEntry ( ise );
					dispose();
				}
			}
		);
	}
	
	
	
	
	
	

	
	public IrcServerEntry getEntrada()
	{
		try
		{
			return new IrcServerEntry ( serverTextField.getText() , portTextField.getText() , nickTextField.getText() , cbQuery.isSelected() , cbChannel.isSelected() , cbDCC.isSelected() , channelVector ); 
		}
		catch ( NumberFormatException nfe )
		{
			//defaultear el puerto a 6667
			return new IrcServerEntry ( serverTextField.getText() , 6667 , nickTextField.getText() , cbQuery.isSelected() , cbChannel.isSelected() , cbDCC.isSelected() , channelVector ); 
		}
	}

}
