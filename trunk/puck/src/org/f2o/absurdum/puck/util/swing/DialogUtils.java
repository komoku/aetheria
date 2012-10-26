/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 26/10/2012 17:14:36
 */
package org.f2o.absurdum.puck.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * @author carlos
 *
 */
public class DialogUtils 
{

	/**
	 * Registers the Escape key to close the given dialog.
	 * @param dialog
	 */
	public static void registerEscapeAction(final JDialog dialog) 
	{
		registerCloseAction(dialog,KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
	}
	
	/**
	 * Registers a keystroke to close the given dialog.
	 * @param dialog
	 * @param keyStroke
	 */
	public static void registerCloseAction(final JDialog dialog , KeyStroke keyStroke) 
	{
		ActionListener escListener = new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				dialog.dispose();
			}
		};

		dialog.getRootPane().registerKeyboardAction(escListener,
				keyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
	/**
	 * Registers the Escape key to close the given dialog.
	 * @param dialog
	 */
	public static void registerEscapeAction(final JFrame dialog) 
	{
		registerCloseAction(dialog,KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
	}
	
	/**
	 * Registers a keystroke to close the given dialog.
	 * @param dialog
	 * @param keyStroke
	 */
	public static void registerCloseAction(final JFrame dialog , KeyStroke keyStroke) 
	{
		ActionListener escListener = new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				dialog.dispose();
			}
		};

		dialog.getRootPane().registerKeyboardAction(escListener,
				keyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}



}
