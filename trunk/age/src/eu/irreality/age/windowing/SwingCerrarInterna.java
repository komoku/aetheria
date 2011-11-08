/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.windowing;
import java.awt.*;
import java.awt.event.*;

public class SwingCerrarInterna extends WindowAdapter implements ActionListener
{

	AGEClientWindow win;

	public SwingCerrarInterna ( AGEClientWindow win )
	{
		this.win = win;
	}

	public void windowClosing ( WindowEvent evt )
	{
		win.exitNow();
	}

	//para el menú "salir".
	public void actionPerformed ( ActionEvent evt )
	{
		win.exitNow();
	}

}
