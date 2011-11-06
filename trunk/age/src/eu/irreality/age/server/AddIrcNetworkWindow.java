/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 06/11/2011 16:50:13
 */
package eu.irreality.age.server;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;


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