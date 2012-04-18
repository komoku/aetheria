/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.server;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import eu.irreality.age.PartidaEntry;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.mdi.SwingAetheriaGUI;

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

	private JCheckBox cbTel // = new JCheckBox("Servir por Telnet, puerto:");
		= new JCheckBox(UIMessages.getInstance().getMessage("serveroptions.serve.telnet"));
	private JCheckBox cbAge // = new JCheckBox("Servir por AGE, puerto:");
		= new JCheckBox(UIMessages.getInstance().getMessage("serveroptions.serve.age"));
	private JCheckBox cbIrc // = new JCheckBox("Servir por IRC");
		= new JCheckBox(UIMessages.getInstance().getMessage("serveroptions.serve.irc"));
	private JCheckBox cbInitOnOK // = new JCheckBox("Arrancar las partidas dedicadas al pulsar Aceptar");
		= new JCheckBox(UIMessages.getInstance().getMessage("serveroptions.init.on.ok"));
	private JCheckBox cbInitOnStartup // = new JCheckBox("Arrancar las partidas dedicadas al iniciar AGE");
		= new JCheckBox(UIMessages.getInstance().getMessage("serveroptions.init.on.startup"));
	private JTextField tfTPort = new JTextField("8010") , tfAPort = new JTextField("8009") ;
	
	private DefaultListModel ircServerVector = new DefaultListModel();
	private JList ircServerList = new JList(ircServerVector);	
	private JButton islAdd // = new JButton("Agregar...");
		= new JButton( UIMessages.getInstance().getMessage("serveroptions.ircserver.add") );
	private JButton islRemove // = new JButton("Quitar");
		= new JButton( UIMessages.getInstance().getMessage("serveroptions.ircserver.del") );
	private JButton islConfig // = new JButton("Configurar");
		= new JButton( UIMessages.getInstance().getMessage("serveroptions.ircserver.conf") );
	
	private DefaultListModel gameVector = new DefaultListModel();
	private JList gameList = new JList(gameVector);
	private JButton gameAdd // = new JButton("Agregar...");
		= new JButton( UIMessages.getInstance().getMessage("serveroptions.game.add") );
	private JButton gameRemove // = new JButton("Quitar");
		= new JButton( UIMessages.getInstance().getMessage("serveroptions.game.del") );
	private JButton gameConfig //= new JButton("Configurar");
		= new JButton( UIMessages.getInstance().getMessage("serveroptions.game.conf") );
	
	private JButton botonAceptar = new JButton(UIMessages.getInstance().getMessage("button.ok"));
	private JButton botonCancelar = new JButton(UIMessages.getInstance().getMessage("button.can"));
	
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
	
		setTitle( UIMessages.getInstance().getMessage("serveroptions.title") );
		
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
				subPan1.setBorder ( BorderFactory.createTitledBorder( UIMessages.getInstance().getMessage("serveroptions.servers") ) );
			final JPanel subPan2 = new JPanel();
				subPan2.setBorder ( BorderFactory.createTitledBorder( UIMessages.getInstance().getMessage("serveroptions.irc") ) );
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
				subPan3.setBorder ( BorderFactory.createTitledBorder( UIMessages.getInstance().getMessage("serveroptions.dedicated") ) );
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
		setLocationRelativeTo(madre);
		
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
					try
					{
						saveConfiguration();
						if ( cbInitOnOK.isSelected() )
						{
							ServerHandler.getInstance().applyOptions(getEntrada());
							ServerHandler.getInstance().initPartidasDedicadas(SwingAetheriaGUI.getInstance().getPanel());
						}
					}
					catch ( Exception e )
					{
						JOptionPane.showMessageDialog ( null , UIMessages.getInstance().getMessage("serveroptions.cannot.save") + " " + e , "¡Oops!" , JOptionPane.ERROR_MESSAGE );
					}
					finally
					{
						dispose();
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
