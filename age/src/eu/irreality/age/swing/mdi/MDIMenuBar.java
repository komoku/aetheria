package eu.irreality.age.swing.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import eu.irreality.age.GameChoosingInternalFrame;
import eu.irreality.age.PluginMenu;
import eu.irreality.age.ServerConfigurationWindow;
import eu.irreality.age.SwingRemoteClientWindow;
import eu.irreality.age.server.ServerHandler;
import eu.irreality.age.server.ServerLogWindow;

public class MDIMenuBar extends JMenuBar 
{

	JDesktopPane thePanel;
	SwingAetheriaGUI window;

	public MDIMenuBar ( final JDesktopPane p , SwingAetheriaGUI w )
	{
		thePanel = p;
		window = w;
		JMenu menuArchivo = new JMenu("Archivo");
		JMenuItem itemNuevo = new JMenuItem("Nuevo");
		JMenuItem itemNuevo2 = new JMenuItem("Nuevo juego...");
		JMenuItem itemRemota = new JMenuItem("Conectar a partida remota...");
		JMenuItem itemNuevoLog = new JMenuItem("Cargar partida...");
		JMenuItem itemNuevoEstado = new JMenuItem("Cargar estado...");
		JMenuItem itemLoader = new JMenuItem("Cargador de juegos...");
		JMenuItem itemSalir = new JMenuItem("Salir");
		JMenu menuPresentacion = new JMenu("Presentación");
		JMenuItem itemIconificar = new JMenuItem("Iconificar todo");
		JMenu menuServidor = new JMenu("Servidor");
		JMenuItem itemConfigServidor = new JMenuItem("Configuración...");
		JMenuItem itemShowLogs = new JMenuItem("Mostrar logs");
		JMenu menuAyuda = new JMenu("Ayuda");
		JMenuItem itemAbout = new JMenuItem("Acerca de AGE...");
		EscuchadorMinimizarTodo escmin = new EscuchadorMinimizarTodo ( thePanel );
		EscuchadorAgregarVentanas esc = new EscuchadorAgregarVentanas ( thePanel );
		EscuchadorNuevoDesdeFichero esc2 = new EscuchadorNuevoDesdeFichero ( thePanel );
		EscuchadorCargarDesdeLog escCargar = new EscuchadorCargarDesdeLog ( thePanel );
		EscuchadorCargarDesdeEstado escCargar2 = new EscuchadorCargarDesdeEstado ( thePanel );
		itemNuevo.addActionListener ( esc );
		itemNuevo2.addActionListener ( esc2 );
		itemNuevoLog.addActionListener ( escCargar );
		itemNuevoEstado.addActionListener ( escCargar2 );
		itemIconificar.addActionListener ( escmin );
		itemSalir.addActionListener ( 

				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						/*
				if ( standalone == true )
					System.exit(0);
				else
					dispose();
						 */
						window.dispose();
					}
				}

		);

		itemLoader.addActionListener (

				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						p.add ( new GameChoosingInternalFrame ( p ) );
					}
				}

		);

		itemAbout.addActionListener (
				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						JInternalFrame ventanaAbout;
						ventanaAbout = new JInternalFrame("Acerca de AGE",true,true,true,true);
						p.add(ventanaAbout);
						ventanaAbout.setSize(500,500);
						//ventanaAbout.getContentPane().add ( new JLabel("Aetheria Game Engine") );

						final JTextPane tpAbout = new JTextPane();
						try
						{
							tpAbout.setPage(new File("doc/help/index.htm").toURL());
						}
						catch ( Exception exc )
						{
							exc.printStackTrace();
						}

						tpAbout.setEditable(false);

						tpAbout.addHyperlinkListener ( new HyperlinkListener ()
						{
							public void hyperlinkUpdate ( HyperlinkEvent evt )
							{
								if ( evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
								{
									try
									{
										System.out.println("Hyperlink Event.\n");
										tpAbout.setPage(evt.getURL());
									}
									catch ( java.io.IOException ioe )
									{
										ioe.printStackTrace();
									}
								}
							}
						} );

						ventanaAbout.getContentPane().add ( tpAbout );

						ventanaAbout.setVisible(true);
					}
				}
		);

		itemConfigServidor.addActionListener (
				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						ServerConfigurationWindow scw = ServerConfigurationWindow.getInstance();
						scw.setVisible(true);
					}
				}
		);
		
		itemShowLogs.addActionListener(
				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						ServerLogWindow slw = ServerHandler.getInstance(p).getLogWindow();
						if ( slw.getParent() == null )
							p.add(slw);
						slw.setVisible(true);
						slw.toFront();
						//slw.requestFocusInWindow();
						try {
							slw.setSelected(true);
						} catch (PropertyVetoException e) {
							e.printStackTrace();
						}
					}
				}
				);

		itemRemota.addActionListener (
				new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						SwingRemoteClientWindow srcw = new SwingRemoteClientWindow( thePanel );
						p.add(srcw);
						srcw.toFront();
					}
				}
		);



		//menuArchivo.add ( itemNuevo );
		menuArchivo.add ( itemNuevo2 );
		menuArchivo.add ( itemRemota );
		menuArchivo.add ( itemNuevoLog );
		menuArchivo.add ( itemNuevoEstado );
		menuArchivo.add ( itemLoader );
		menuArchivo.add ( new JSeparator() );
		menuArchivo.add ( itemSalir );
		menuPresentacion.add ( itemIconificar );
		menuServidor.add ( itemConfigServidor );
		menuServidor.add ( itemShowLogs );
		menuAyuda.add ( itemAbout );
		this.add ( menuArchivo );	
		this.add ( menuPresentacion );
		this.add ( menuServidor );
		this.add ( menuAyuda );
		this.add ( new PluginMenu(thePanel) );
	}
}
