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
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import eu.irreality.age.i18n.UIMessages;


class AddIrcNetworkWindow extends JDialog
{

	public JTextField serverTextField = new JTextField("libres.irc-hispano.org");
	public JTextField portTextField = new JTextField("6667");
	public JTextField nickTextField = new JTextField("AGEserver");
	
	public JCheckBox cbQuery = new JCheckBox( UIMessages.getInstance().getMessage("server.irc.label.query") );
	public JCheckBox cbChannel = new JCheckBox( UIMessages.getInstance().getMessage("server.irc.label.channel") );
	public JCheckBox cbDCC = new JCheckBox("DCC");

	public DefaultListModel channelVector = new DefaultListModel();

	public JList channelList = new JList(channelVector);

	private JButton botonAceptar = new JButton( UIMessages.getInstance().getMessage("button.ok") );
	private JButton botonCancelar = new JButton( UIMessages.getInstance().getMessage("button.can") );

	private final ServerConfigurationWindow madre;
	
	private boolean editMode = false; //false:add, true:edit
	//private final IrcServerEntry aEditar;

	
	//refactoring of common code, 2012-04-18
	public void initComponents()
	{
		
		getContentPane().setLayout ( new BoxLayout ( getContentPane() , BoxLayout.PAGE_AXIS ) );
		
		JPanel connectionDataPanel = new JPanel();
		connectionDataPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getInstance().getMessage("server.irc.conndata")));
		connectionDataPanel.setLayout ( new BoxLayout ( connectionDataPanel , BoxLayout.PAGE_AXIS ) );
		
		JPanel subPan11 = new JPanel();
		subPan11.add ( new JLabel( UIMessages.getInstance().getMessage("server.irc.label.server") ) );
		subPan11.add ( serverTextField );
		JPanel subPan12 = new JPanel();
		subPan12.add ( new JLabel( UIMessages.getInstance().getMessage("server.irc.label.port") ) );
		subPan12.add ( portTextField );
		JPanel subPan13 = new JPanel();
		subPan13.add ( new JLabel( UIMessages.getInstance().getMessage("server.irc.label.nick") ) );
		subPan13.add ( nickTextField ); 
		connectionDataPanel.add ( subPan11 );
		connectionDataPanel.add ( subPan12 );
		connectionDataPanel.add ( subPan13 );
		
		JPanel channelListPanel = new JPanel();
		channelListPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getInstance().getMessage("server.irc.channels")));
		channelListPanel.setLayout ( new BoxLayout ( channelListPanel , BoxLayout.PAGE_AXIS ) );
		
		JScrollPane jsp = new JScrollPane(channelList);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		channelListPanel.add(jsp);
		JPanel channelButtonPanel = new JPanel();
		channelButtonPanel.setLayout ( new BoxLayout ( channelButtonPanel , BoxLayout.LINE_AXIS) );
		final JTextField editChanTextField = new JTextField(8);
		JButton addChanButton = new JButton(UIMessages.getInstance().getMessage("server.irc.chan.add"));
		JButton deleteChanButton = new JButton(UIMessages.getInstance().getMessage("server.irc.chan.del"));
		JButton editChanButton = new JButton(UIMessages.getInstance().getMessage("server.irc.chan.edit"));
		channelButtonPanel.add(editChanTextField);
		channelButtonPanel.add(addChanButton);
		channelButtonPanel.add(deleteChanButton);
		channelButtonPanel.add(editChanButton);
		channelListPanel.add(channelButtonPanel);
		
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout( new BoxLayout ( buttonsPanel , BoxLayout.LINE_AXIS ) );
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add ( botonAceptar );
		buttonsPanel.add ( botonCancelar );
		
		getContentPane().add(connectionDataPanel);
		getContentPane().add(Box.createVerticalStrut(10));
		getContentPane().add(channelListPanel);
		getContentPane().add(Box.createVerticalStrut(10));
		getContentPane().add(buttonsPanel);
		
		addChanButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				String toAdd = editChanTextField.getText();
				if ( toAdd != null && toAdd.length() > 0 && !channelVector.contains(toAdd) )
				{
					channelVector.addElement(toAdd);
					editChanTextField.setText("");
				}
			}
		}
		);
		
		deleteChanButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				int selInd = channelList.getSelectedIndex();
				if ( selInd >= 0 )
					channelVector.removeElementAt(channelList.getSelectedIndex());
				if ( channelVector.size() > selInd )
					channelList.setSelectedIndex(selInd);
			}
		}
		);
		
		editChanButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				int selInd = channelList.getSelectedIndex();
				String newText = editChanTextField.getText();
				if ( selInd >= 0 && newText != null && newText.length() > 0 )
					channelVector.setElementAt(newText, selInd);
			}
		}
		);
		
		channelList.addListSelectionListener( new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent arg0) 
			{
				if ( channelList.getSelectedIndex() >= 0 )
					editChanTextField.setText((String)channelVector.elementAt(channelList.getSelectedIndex()));
			}
			
		}
		);
		
		
		
		
		
		/*
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
					channelList.setVisibleRowCount(4);
					JScrollPane jsp = new JScrollPane(channelList);
					jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					subPan22.add ( jsp );	
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
			*/
			
	
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
			channelVector.addElement( "#aetheria" );
		}
		else
		{
			cbQuery.setSelected(aEditar.respondeAPrivados());
			cbChannel.setSelected(aEditar.respondeACanales());
			cbDCC.setSelected(aEditar.respondeADCC());
			
			for ( int i = 0 ; i < aEditar.getChannels().size() ; i++ )
				channelVector.addElement(aEditar.getChannels().get(i));
			
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
			return new IrcServerEntry ( serverTextField.getText() , portTextField.getText() , nickTextField.getText() , cbQuery.isSelected() , cbChannel.isSelected() , cbDCC.isSelected() , Arrays.asList(channelVector.toArray()) ); 
		}
		catch ( NumberFormatException nfe )
		{
			//defaultear el puerto a 6667
			return new IrcServerEntry ( serverTextField.getText() , 6667 , nickTextField.getText() , cbQuery.isSelected() , cbChannel.isSelected() , cbDCC.isSelected() , Arrays.asList(channelVector.toArray()) ); 
		}
	}

}