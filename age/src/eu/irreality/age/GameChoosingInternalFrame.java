/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;

import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.server.ServerHandler;

public class GameChoosingInternalFrame extends JInternalFrame
{
	JDesktopPane thePanel;
	JTabbedPane theTabbedPane;
	OptionChoosingPanel opciones;
	
	public static Color BACKGROUND_COLOR = new Color(255,255,204);
	public static Color FOREGROUND_COLOR = new Color(0,0,51);
	
	public OptionChoosingPanel getOptionChoosingPanel()
	{
		return opciones;
	}
	
	public GameChoosingInternalFrame ( JDesktopPane thePanel )
	{
	
		super("Cargador de juegos",true,true,true,true);
	
		try
		{
			Image iconito = this.getToolkit().getImage(this.getClass().getClassLoader().getResource("images/llama.gif"));
			setFrameIcon ( new ImageIcon ( iconito ) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		
		this.thePanel = thePanel;
		theTabbedPane = new JTabbedPane();
		setSize(600,500);
		setVisible(true);
		//setTitle("Cargador de juegos");
		getContentPane().add ( theTabbedPane );
		theTabbedPane.addTab ( "Nuevo" , new GameChoosingPanel ( thePanel , this ) );
		theTabbedPane.addTab ( "Salvados" , new SaveChoosingPanel ( thePanel , this ) );
		theTabbedPane.addTab ( "Opciones" , opciones = new OptionChoosingPanel (  this ) );
		theTabbedPane.addChangeListener ( new ChangeListener() { public void stateChanged(ChangeEvent evt) { opciones.updateServersAndPorts(); } } );
	}
}


class OptionChoosingPanel extends JPanel
{
	JDesktopPane thePanel;
	
	JCheckBox cb1 = new JCheckBox();
	JCheckBox cb2 = new JCheckBox();
	JCheckBox cb3 = new JCheckBox();
	
	//JTextField tf1 = null;
	//JTextField tf2 = null;
	
	JLabel ageLabel = new JLabel("Servidor de AGE");
	JLabel telnetLabel = new JLabel("Servidor de telnet");
	JLabel ircLabel = new JLabel("Servidor de IRC");
	
	JLabel infoLabel = new JLabel("<html><p>Nota: se pueden cambiar los puertos telnet y AGE en las opciones</p><p>del servidor dedicado (Servidor/Configuraci�n...)</p></html>");
	
	public boolean servirAGE()
	{
		return cb1.isSelected();
	}
	public boolean servirTelnet()
	{
		return cb2.isSelected();
	}
	public boolean servirIRC()
	{
		return cb3.isSelected();
	}
	
	public void updateServersAndPorts()
	{
		String agePort = String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoAge());
		String telnetPort = String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoTelnet());
		ageLabel.setText("Servidor de AGE (puerto " + agePort + ")");
		telnetLabel.setText("Servidor de telnet (puerto " + telnetPort + ")");
		//tf1.setText(agePort);
		//tf2.setText(telnetPort);
		boolean age = ServerHandler.getInstance().getServerConfigurationOptions().sirveAge();
		boolean telnet = ServerHandler.getInstance().getServerConfigurationOptions().sirveTelnet();
		boolean irc = ServerHandler.getInstance().getServerConfigurationOptions().sirveIrc();
		cb1.setEnabled(age);
		cb2.setEnabled(telnet);
		cb3.setEnabled(irc);
		ageLabel.setEnabled(age);
		telnetLabel.setEnabled(telnet);
		ircLabel.setEnabled(irc);
		if ( !age ) cb1.setSelected(false);
		if ( !telnet ) cb2.setSelected(false);
		if ( !irc ) cb3.setSelected(false);
		//build info label text
		infoLabel.setText(getInfoLabelText(age,telnet,irc));
	}
	
	/**
	 * Returns a text for the info label, which depends on which servers are enabled on the server configuration options.
	 */
	private String getInfoLabelText ( boolean age , boolean telnet , boolean irc )
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<html><p>");
		if ( !age || !telnet || !irc )
		{
			//informar de servidores deshabilitados
			boolean plur = false;
			if ( (!age && !telnet) || (!age && !irc) || (!telnet && !irc) ) plur = true;
			if ( plur ) 
			{
				sb.append("Los servidores de ");
			}
			else
			{
				sb.append("El servidor de ");
			}
			sb.append(getDisabledServerList(age,telnet,irc));
			if ( plur )
				sb.append("	est�n deshabilitados.</p><p>Para utilizarlos, ");
			else
				sb.append(" est� deshabilitado.</p><p>Para utilizarlo, ");
			sb.append("deben habilitarse primero</p><p>en las opciones de servidor (Servidor/Configuraci�n...).</p>");
			sb.append("<p>En dichas opciones tambi�n se pueden</p><p>configurar los correspondientes puertos.</p>");
		}
		else
		{
			sb.append("<p>Se pueden cambiar los puertos telnet y AGE</p><p>en las opciones de servidor (Servidor/Configuraci�n...).</p>");
		}
		sb.append("</html>");
		return sb.toString();
	}
	
	/**
	 * Returns a string with a comma-separated list of disabled servers to use in the info label.
	 */
	private String getDisabledServerList ( boolean age , boolean telnet , boolean irc )
	{
		StringBuffer sb = new StringBuffer();
		if ( !age ) sb.append("AGE");
		if ( !telnet )
		{
			if ( sb.length() > 0 ) sb.append(", ");
			sb.append("telnet");
		}
		if ( !irc )
		{
			if ( sb.length() > 0 ) sb.append(", ");
			sb.append("IRC");
		}
		return sb.toString();
	}
	
	
	public OptionChoosingPanel ( final JInternalFrame madre )
	{
		JPanel pan0 = new JPanel();
		JPanel pan1 = new JPanel();
		JPanel pan2 = new JPanel();
		JPanel pan3 = new JPanel();
		JPanel pan4 = new JPanel();
		JLabel l0 = new JLabel("<html><p>Las partidas lanzadas desde esta ventana</p><p>ser�n accesibles remotamente a trav�s de:</p>");
		JLabel l1 = ageLabel;
		JLabel l2 = telnetLabel;
		JLabel l3 = ircLabel;
		JLabel l4 = infoLabel;
		//tf1 = new JTextField(String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoAge()));
		//tf2 = new JTextField(String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoTelnet()));
		//setLayout ( new GridLayout ( 5 , 1 ) );
		setLayout ( new BoxLayout(this,BoxLayout.PAGE_AXIS) );
		
		setBackground ( GameChoosingInternalFrame.BACKGROUND_COLOR );
		pan0.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan0.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l0.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l0.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		pan1.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan1.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l1.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l1.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		pan2.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan2.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l2.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l2.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		pan3.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan3.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l3.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l3.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		pan4.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan4.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l4.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l4.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		
		//tf1.setEditable(false);
		//tf2.setEditable(false);
		//tf1.setEnabled(false);
		//tf2.setEnabled(false);
		
		pan0.add(l0);
		
		pan1.add ( cb1 );
		pan1.add ( l1 );
		//pan1.add ( tf1 );
		pan2.add ( cb2 );
		pan2.add ( l2 );
		//pan2.add ( tf2 );
		pan3.add ( cb3 );
		pan3.add ( l3 );
		pan4.add ( l4 );
		
		add(pan0);add(pan1);add(pan2);add(pan3);add(pan4);
		
		
	}
	
}


class SaveChoosingPanel extends JPanel
{
	JDesktopPane thePanel;
	
	JTextPane saveInfoArea = new JTextPane();
	MutableAttributeSet atributos = new SimpleAttributeSet();
	
	SaveInfo informacionSalvado = new SaveInfo();
	
	private void updateSaveInfoArea()
	{
	//	try
	//	{
	//		//gameInfoArea.getDocument().insertString(gameInfoArea.getText().length(),informacionJuego.toLongString(),atributos);
			saveInfoArea.setText( informacionSalvado.toLongString() );
	//	}
	//	catch ( BadLocationException ble )
	//	{
	//		System.out.println(ble);
	//	}
	}
	
	public SaveChoosingPanel( JDesktopPane p , final JInternalFrame madre )
	{

		thePanel = p;
		final JList lista = new JList ( SaveInfo.getListOfSaves() );
		lista.addListSelectionListener ( new ListSelectionListener()
		{
			public void valueChanged ( ListSelectionEvent evt )
			{
					informacionSalvado = (SaveInfo)lista.getSelectedValue();
					//updateLabels();
					updateSaveInfoArea();
			}
		} );
		if ( lista.getLastVisibleIndex() >= 0 ) //puede no haber ninguno
			lista.setSelectedIndex(0);  // seleccionar el primer salvado
		
		/*getContentPane().*/setBackground ( GameChoosingInternalFrame.BACKGROUND_COLOR );
		/*getContentPane().*/setLayout ( new GridLayout ( 1 , 2 ) );
		
		JScrollPane jsp = new JScrollPane ( lista );		
		
		/*getContentPane().*/add(jsp);
		jsp.setBorder(null);
		
		lista.setBackground ( GameChoosingInternalFrame.BACKGROUND_COLOR );
		lista.setForeground ( GameChoosingInternalFrame.FOREGROUND_COLOR );
		saveInfoArea.setBackground ( GameChoosingInternalFrame.BACKGROUND_COLOR );
		saveInfoArea.setForeground ( GameChoosingInternalFrame.FOREGROUND_COLOR );
		
		JPanel subp = new JPanel();
		JButton botonJugar = new JButton("Jugar");
		JButton botonCancelar = new JButton("Cerrar");
		subp.setLayout(new BorderLayout());
		/*
		subp.add ( etiquetas[0] );
		subp.add ( etiquetas[1] );
		subp.add ( etiquetas[2] );
		subp.add ( etiquetas[3] );
		subp.add ( etiquetas[4] );
		subp.add ( etiquetas[5] );
		*/
		subp.add ( saveInfoArea , BorderLayout.CENTER );
		JPanel pBotones = new JPanel();
		pBotones.add ( botonJugar );
		pBotones.add ( botonCancelar );
		
		pBotones.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pBotones.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		
		subp.add ( pBotones , BorderLayout.SOUTH );
		/*getContentPane().*/add(subp);
		
		setVisible(true);
		
		botonCancelar.addActionListener (
			new ActionListener ()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					//dispose();
					//setVisible(false);
					madre.dispose();
				}
			}
		); 
		botonJugar.addActionListener (
			new ActionListener ()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					//setVisible(false);
					//System.out.println("SAVEFILE: " + ((SaveInfo)lista.getSelectedValue()).getFile().toString() );
					
					Thread thr = new Thread()
					{
						public void run()
						{
					//test:
					PartidaEntry pe = new PartidaEntry ( (((SaveInfo)lista.getSelectedValue()).getGameInfo()) , "noname" , 200 , null , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirAGE() , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirTelnet() , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirIRC() );
					
					ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null , ((SaveInfo)lista.getSelectedValue()).getFile().toString() , thePanel );
					//PartidaEnCurso pec = ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null , ((SaveInfo)lista.getSelectedValue()).getFile().toString() , thePanel );
					//ServerHandler.getInstance().addToCorrespondingServers ( pec,pe );
						}
					};
					thr.start();
					
					//working:
					//new SwingAetheriaGameLoader( ((SaveInfo)lista.getSelectedValue()).getGameFile().getParent() , thePanel , true ,((SaveInfo)lista.getSelectedValue()).getFile().toString() , null , false );
				}
			}
		);
		
		lista.addMouseListener ( 
			new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					//System.out.println("Clickeado " + e.getClickCount() + " veces\n");
					if ( e.getClickCount() >= 2 )
					{
					
						Thread thr = new Thread()
						{
							public void run()
							{
						//test:
						PartidaEntry pe = new PartidaEntry ( (((SaveInfo)lista.getSelectedValue()).getGameInfo()) , "noname" , 200 , null , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirAGE() , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirTelnet() , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirIRC() );
						
						ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null , ((SaveInfo)lista.getSelectedValue()).getFile().toString() , thePanel );
						//PartidaEnCurso pec = ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null , ((SaveInfo)lista.getSelectedValue()).getFile().toString() , thePanel );
						//ServerHandler.getInstance().addToCorrespondingServers ( pec,pe );
							}
						};
						thr.start();
						
						//working:
						//new SwingAetheriaGameLoader( ((SaveInfo)lista.getSelectedValue()).getGameFile().getParent() , thePanel , true ,((SaveInfo)lista.getSelectedValue()).getFile().toString() , null ,  false );
					}
				}
			
			} );		
		
	}
	
	
	
	
}

class GameChoosingPanel extends JPanel
{

	JDesktopPane thePanel;
	
	JTextPane gameInfoArea = new JTextPane();
	MutableAttributeSet atributos = new SimpleAttributeSet();

	GameInfo informacionJuego = new GameInfo();
	
	
	private void updateGameInfoArea()
	{
	//	try
	//	{
	//		//gameInfoArea.getDocument().insertString(gameInfoArea.getText().length(),informacionJuego.toLongString(),atributos);
			gameInfoArea.setText( informacionJuego.toLongString() );
	//	}
	//	catch ( BadLocationException ble )
	//	{
	//		System.out.println(ble);
	//	}
	}
	
	
	
	
	public GameChoosingPanel( JDesktopPane p , final JInternalFrame madre )
	{
		//setVisible(true);
		//setSize(400,400);
		//setTitle("Nueva partida");
		thePanel = p;
		final JList lista = new JList ( GameInfo.getListOfGames() );
		lista.setFont(new Font("Serif",Font.ITALIC,24));
		lista.addListSelectionListener ( new ListSelectionListener()
		{
			public void valueChanged ( ListSelectionEvent evt )
			{

					//System.out.println("Settin' game info to " + informacionJuego);
					informacionJuego = (GameInfo)lista.getSelectedValue();
					//System.out.println("Settin' game info to " + informacionJuego);
					//updateLabels();
					updateGameInfoArea();

			}
		} );
		//lista.setSelectedIndex(0);  // seleccionar el primer juego
		
		/*getContentPane().*/setBackground ( GameChoosingInternalFrame.BACKGROUND_COLOR );
		/*getContentPane().*/setLayout ( new GridLayout ( 1 , 2 ) );
		lista.setBorder(BorderFactory.createTitledBorder("Selecciona un juego:"));		
		/*getContentPane().*/add(lista);
		
		lista.setBackground ( GameChoosingInternalFrame.BACKGROUND_COLOR );
		lista.setForeground ( GameChoosingInternalFrame.FOREGROUND_COLOR );
		gameInfoArea.setBackground ( GameChoosingInternalFrame.BACKGROUND_COLOR );
		gameInfoArea.setForeground ( GameChoosingInternalFrame.FOREGROUND_COLOR );
		
		JPanel subp = new JPanel();
		JButton botonJugar = new JButton("Jugar");
		JButton botonCancelar = new JButton("Cerrar");
		subp.setLayout(new BorderLayout());
		/*
		subp.add ( etiquetas[0] );
		subp.add ( etiquetas[1] );
		subp.add ( etiquetas[2] );
		subp.add ( etiquetas[3] );
		subp.add ( etiquetas[4] );
		subp.add ( etiquetas[5] );
		*/
		subp.add ( gameInfoArea , BorderLayout.CENTER );
		JPanel pBotones = new JPanel();
		pBotones.add ( botonJugar );
		pBotones.add ( botonCancelar );
		
		pBotones.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pBotones.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		
		subp.add ( pBotones , BorderLayout.SOUTH );
		/*getContentPane().*/add(subp);
		
		setVisible(true);
		
		botonCancelar.addActionListener (
			new ActionListener ()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					//dispose();
					//setVisible(false);
					madre.dispose();
				}
			}
		); 
		botonJugar.addActionListener (
			new ActionListener ()
			{
				public void actionPerformed ( ActionEvent evt )
				{
					//setVisible(false);
					
					if ( (GameInfo)lista.getSelectedValue() == null ) return;
					
					Thread thr = new Thread()
					{
						public void run()
						{
					//test:
					PartidaEntry pe = new PartidaEntry ( ((GameInfo)lista.getSelectedValue()) , "noname" , 200 , null , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirAGE() , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirTelnet() , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirIRC() );
					
					//PartidaEnCurso pec = ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null , null , thePanel );
					ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null , null , thePanel );
					//ServerHandler.getInstance().addToCorrespondingServers ( pec,pe );
						}
					};
					thr.start();
					
					//public PartidaEntry ( GameInfo juego , String nombrePartida , int maxPlayers , String passwordPartida , boolean servirAge , boolean servirTelnet , boolean servirIrc )
					
					
					//working:
					//new SwingAetheriaGameLoader( (((GameInfo)lista.getSelectedValue()).getFile()).getParent() , thePanel , false ,null , null , false );
				}
			}
		);
		
		lista.addMouseListener ( 
			new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					//System.out.println("Clickeado " + e.getClickCount() + " veces\n");
					if ( e.getClickCount() >= 2 )
					{
					
						Thread thr = new Thread()
						{
							public void run()
							{
								PartidaEntry pe = new PartidaEntry ( ((GameInfo)lista.getSelectedValue()) , "noname" , 200 , null , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirAGE() , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirTelnet() , ((GameChoosingInternalFrame)madre).getOptionChoosingPanel().servirIRC() );
								//PartidaEnCurso pec = ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null , null , thePanel );
								//ServerHandler.getInstance().addToCorrespondingServers ( pec,pe );
								ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null , null , thePanel );
							}
						};
						thr.start();
						
						//new SwingAetheriaGameLoader( (((GameInfo)lista.getSelectedValue()).getFile()).getParent() , thePanel , false ,null , null, false );
					}
				}
			
			} );
		
	}
	
	
	

}




