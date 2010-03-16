/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
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
	
		Image iconito = getToolkit().getImage("images" + File.separatorChar + "llama.gif");
		setFrameIcon ( new ImageIcon ( iconito ) );
	
		this.thePanel = thePanel;
		theTabbedPane = new JTabbedPane();
		setSize(600,500);
		setVisible(true);
		//setTitle("Cargador de juegos");
		getContentPane().add ( theTabbedPane );
		theTabbedPane.addTab ( "Nuevo" , new GameChoosingPanel ( thePanel , this ) );
		theTabbedPane.addTab ( "Salvados" , new SaveChoosingPanel ( thePanel , this ) );
		theTabbedPane.addTab ( "Opciones" , opciones = new OptionChoosingPanel (  this ) );
		theTabbedPane.addChangeListener ( new ChangeListener() { public void stateChanged(ChangeEvent evt) { opciones.updPorts(); } } );
	}
}


class OptionChoosingPanel extends JPanel
{
	JDesktopPane thePanel;
	
	JCheckBox cb1 = new JCheckBox();
	JCheckBox cb2 = new JCheckBox();
	JCheckBox cb3 = new JCheckBox();
	
	JTextField tf1 = null;
	JTextField tf2 = null;
	
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
	
	public void updPorts()
	{
		tf1.setText(String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoAge()));
		tf2.setText(String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoTelnet()));
	}
	
	public OptionChoosingPanel ( final JInternalFrame madre )
	{
		JPanel pan1 = new JPanel();
		JPanel pan2 = new JPanel();
		JPanel pan3 = new JPanel();
		JPanel pan4 = new JPanel();
		JLabel l1 = new JLabel("Ser servidor de AGE");
		JLabel l2 = new JLabel("Ser servidor de Telnet");
		JLabel l3 = new JLabel("Ser servidor de IRC");
		JLabel l4 = new JLabel("<html><p>Nota: se pueden cambiar los puertos telnet y AGE en las opciones</p><p>del servidor dedicado (Servidor/Configuración...)</p></html>");
		tf1 = new JTextField(String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoAge()));
		tf2 = new JTextField(String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoTelnet()));
		setLayout ( new GridLayout ( 4 , 1 ) );
		
		setBackground ( GameChoosingInternalFrame.BACKGROUND_COLOR );
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
		
		tf1.setEditable(false);
		tf2.setEditable(false);
		
		pan1.add ( cb1 );
		pan1.add ( l1 );
		pan1.add ( tf1 );
		pan2.add ( cb2 );
		pan2.add ( l2 );
		pan2.add ( tf2 );
		pan3.add ( cb3 );
		pan3.add ( l3 );
		pan4.add ( l4 );
		
		add(pan1);add(pan2);add(pan3);add(pan4);
		
		
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




