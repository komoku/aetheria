/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import eu.irreality.age.windowing.AGEClientWindow;
public class SwingMenuAetheria extends JMenuBar
{

	AGEClientWindow win;
	
	JMenuItem itemSalir;
	JMenuItem itemGuardar;
	JMenuItem itemGuardar2;
	
	public void addToMenu ( JMenu theMenu )
	{
		theMenu.add(itemGuardar);
		theMenu.add(itemGuardar2);
		theMenu.add(new JSeparator());
		theMenu.add(itemSalir);
	}
	
	public SwingMenuAetheria ( AGEClientWindow nwin )
	{
		this.win = nwin;
		JMenu menuArchivo = new JMenu( "Archivo" );
		itemSalir = new JMenuItem( "Salir" );
		itemGuardar = new JMenuItem( "Guardar partida..." );
		itemGuardar2 = new JMenuItem( "Guardar estado..." );
		
		addToMenu(menuArchivo);
		
		itemSalir.addActionListener( new SwingCerrarInterna(win) );
		itemGuardar.addActionListener ( new ActionListener() 
		{
			public void actionPerformed ( ActionEvent evt )
			{
				win.guardarLog();
			}
		}
		);
		itemGuardar2.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				win.guardarEstado();
			}
		}
		);
		this.add(menuArchivo);
	}
	
	
	public void addToWindow ()
	{
		AGEClientWindow laVentana = win;
		if ( laVentana.getTheJMenuBar() == null )
			laVentana.setTheJMenuBar( this );
		else
		{
			SwingMenuAetheria source = this;
			JMenuBar targetBar = laVentana.getTheJMenuBar();
			JMenu targetMenu;
			JMenu theMenuToAdd = source.getMenu(0);
			if ( targetBar.getMenu(0).getText().equals("Archivo") )
				targetMenu = targetBar.getMenu(0);
			else
			{
				targetMenu = new JMenu("Archivo");
				targetBar.add(targetMenu);
			}
			source.addToMenu(targetMenu);
		}
	}

}


