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

import eu.irreality.age.i18n.UIMessages;


class AddIrcNetworkWindow extends JDialog
{

	public JTextField serverTextField = new JTextField("libres.irc-hispano.org");
	public JTextField portTextField = new JTextField("6667");
	public JTextField nickTextField = new JTextField("AGEserver");
	
	public JCheckBox cbQuery = new JCheckBox( UIMessages.getInstance().getMessage("server.irc.label.query") );
	public JCheckBox cbChannel = new JCheckBox( UIMessages.getInstance().getMessage("server.irc.label.channel") );
	public JCheckBox cbDCC = new JCheckBox("DCC");

	public Vector channelVector = new Vector();

	public JList channelList = new JList(channelVector);

	private JButton botonAceptar = new JButton( UIMessages.getInstance().getMessage("button.ok") );
	private JButton botonCancelar = new JButton( UIMessages.getInstance().getMessage("button.can") );

	private final ServerConfigurationWindow madre;
	
	private boolean editMode = false; //false:add, true:edit
	//private final IrcServerEntry aEditar;

	
	//refactoring of common code, 2012-04-18
	public void initComponents()
	{
		getContentPane().setLayout ( new BorderLayout() );
		JPanel panelPrincipal = new JPanel ( new GridLayout ( 2 , 1 ) );
			JPanel subPan1 = new JPanel();
				JPanel subPan11 = new JPanel();
					subPan11.add ( new JLabel( UIMessages.getInstance().getMessage("server.irc.label.server") ) );
					subPan11.add ( serverTextField );
				JPanel subPan12 = new JPanel();
					subPan12.add ( new JLabel( UIMessages.getInstance().getMessage("server.irc.label.port") ) );
					subPan12.add ( portTextField );
				JPanel subPan13 = new JPanel();
					subPan13.add ( new JLabel( UIMessages.getInstance().getMessage("server.irc.label.nick") ) );
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
	
	}
	
	public AddIrcNetworkWindow ( final ServerConfigurationWindow madre )
	{
	
		super ( madre , true );
		
		this.madre = madre;
	
		setTitle("Servidor IRC");
		
		fillComponents(null); //set component values to the defaults
		initComponents();
			
		pack();	
		this.setLocationRelativeTo(madre);
		
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
	
	
	
	/**
	 * Fills the components with the values associated with an IRC server entry.
	 * If aEditar is null, then it fills them with default values.
	 */
	public void fillComponents ( IrcServerEntry aEditar )
	{
		if ( aEditar == null )
		{
			cbQuery.setSelected(true);
			cbChannel.setSelected(true);
			cbDCC.setSelected(true);
			channelVector.add ( "#aetheria" );
		}
		else
		{
			cbQuery.setSelected(aEditar.respondeAPrivados());
			cbChannel.setSelected(aEditar.respondeACanales());
			cbDCC.setSelected(aEditar.respondeADCC());
			channelVector = aEditar.getChannels();
			serverTextField.setText(aEditar.getServer());
			portTextField.setText(String.valueOf(aEditar.getPort()));
			nickTextField.setText(String.valueOf(aEditar.getNick()));
		}
	}
	
	
	public AddIrcNetworkWindow ( final ServerConfigurationWindow madre , final IrcServerEntry aEditar )
	{
		
		super ( madre , true );
	
		this.madre = madre;
		//this.aEditar = aEditar;
	
		setTitle( UIMessages.getInstance().getMessage("server.irc.title") );

		editMode = true;

		fillComponents(aEditar);
		initComponents();
							
		pack();	
		this.setLocationRelativeTo(madre);
		
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