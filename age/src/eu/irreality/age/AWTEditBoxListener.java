/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
//package AetheriaAWT;
import java.awt.*;
import java.awt.event.*;
public class AWTEditBoxListener implements ActionListener
{

	TextField elCampoTexto;
	TextArea elAreaTexto;
	Player esperando;

	public AWTEditBoxListener ( TextField nCampoTexto , TextArea nAreaTexto )
	{
		elCampoTexto = nCampoTexto;
		elAreaTexto = nAreaTexto;
	}
	
	public void setWaitingPlayer ( Player p )
	{
		esperando = p;	
	}

	public void actionPerformed( ActionEvent e )
	{
		elAreaTexto.append("\n");
		elAreaTexto.append("\n" + "[COMANDO]  " + elCampoTexto.getText());
		esperando.setCommandString(elCampoTexto.getText());
		elCampoTexto.setText("");
		esperando.resumeExecution();
	}
	
}