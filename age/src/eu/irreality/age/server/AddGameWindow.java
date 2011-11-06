/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 06/11/2011 16:49:20
 */
package eu.irreality.age.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import eu.irreality.age.GameInfo;
import eu.irreality.age.PartidaEntry;
import eu.irreality.age.i18n.UIMessages;

class AddGameWindow extends JDialog
{

	private DefaultListModel gamesVector = new DefaultListModel();
	private JList gamesList = new JList ( gamesVector );
	private JTextPane gameInfoArea = new JTextPane();
	
	private JTextField gameNameTextField = new JTextField( UIMessages.getInstance().getMessage("server.addgame.game") );
	private JTextField maxPlayersTextField = new JTextField("9999");
	
	//maybe login-pass thingies? password requirement?
	
	private JCheckBox passwordRequiredBox = new JCheckBox ( UIMessages.getInstance().getMessage("server.addgame.label.password") );
	private JTextField passwordTextField = new JTextField ( "alohomora" );
	
	private JCheckBox cbAge = new JCheckBox("AGE");
	private JCheckBox cbTelnet = new JCheckBox("Telnet");
	private JCheckBox cbIrc = new JCheckBox("IRC");
	
	private JButton botonAceptar = new JButton( UIMessages.getInstance().getMessage("button.ok") );
	private JButton botonCancelar = new JButton( UIMessages.getInstance().getMessage("button.can") );

	private ServerConfigurationWindow madre;
	
	private GameInfo informacionJuego;

	private void updateGameInfoArea()
	{
		gameInfoArea.setText( informacionJuego.toLongString() );
		//gameNameTextField.setText("Partida de " + gamesList.getSelectedValue().toString() );
		gameNameTextField.setText( UIMessages.getInstance().getMessage("server.addgame.gameinfo","$game",gamesList.getSelectedValue().toString()) );
	}

	public AddGameWindow ( final ServerConfigurationWindow madre )
	{
		
		super ( madre , true );
		
		this.madre = madre;
	
		setTitle( UIMessages.getInstance().getMessage("server.addgame.title") );
		
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
					subPan21.add ( new JLabel( UIMessages.getInstance().getMessage("server.addgame.label.gamename") ) );
					subPan21.add ( gameNameTextField );
				JPanel subPan22 = new JPanel();
					subPan22.add ( new JLabel( UIMessages.getInstance().getMessage("server.addgame.label.maxplayers") ) );
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
				subPan3.setBorder ( BorderFactory.createTitledBorder( UIMessages.getInstance().getMessage("server.addgame.protocols") ) );
				subPan31.add ( cbAge );
				subPan31.add ( cbTelnet );
				subPan31.add ( cbIrc );
					//JTextPane jtp = new JTextPane();
					//jtp.setText("(nótese que, para que se sirva la partida, es necesario seleccionar AGE, telnet y/o IRC también en la ventana de configuración de servidor, junto con un puerto cuando corresponda)");
					//subPan24.add ( jtp );
				subPan3.add ( subPan31 );
				subPan3.add ( new JLabel( UIMessages.getInstance().getMessage("server.addgame.onlyactive") , JLabel.CENTER ) );
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
	
		setTitle( UIMessages.getInstance().getMessage("server.addgame.title") );
		
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
					subPan21.add ( new JLabel( UIMessages.getInstance().getMessage("server.addgame.label.gamename") ) );
					gameNameTextField.setText(aEditar.getName());
					subPan21.add ( gameNameTextField );
				JPanel subPan22 = new JPanel();
					subPan22.add ( new JLabel( UIMessages.getInstance().getMessage("server.addgame.label.maxplayers") ) );
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
				subPan3.add ( new JLabel( UIMessages.getInstance().getMessage("server.addgame.onlyactive") , JLabel.CENTER ) );
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