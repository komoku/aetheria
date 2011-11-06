/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 06/11/2011 12:51:16
 */
package eu.irreality.age.swing.mdi.gameloader;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import eu.irreality.age.PartidaEntry;
import eu.irreality.age.SaveInfo;
import eu.irreality.age.server.ServerHandler;

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