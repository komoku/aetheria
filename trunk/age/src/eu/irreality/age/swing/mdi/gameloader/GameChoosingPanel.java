/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 06/11/2011 12:51:24
 */
package eu.irreality.age.swing.mdi.gameloader;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import eu.irreality.age.GameInfo;
import eu.irreality.age.PartidaEntry;
import eu.irreality.age.server.ServerHandler;

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