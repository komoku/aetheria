/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.swing.mdi.gameloader;
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
import eu.irreality.age.i18n.UIMessages;

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
	
		super(UIMessages.getInstance().getMessage("gameloader.title"),true,true,true,true);
	
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
		theTabbedPane.addTab ( UIMessages.getInstance().getMessage("gameloader.new") , new GameChoosingPanel ( thePanel , this ) );
		theTabbedPane.addTab ( UIMessages.getInstance().getMessage("gameloader.saved") , new SaveChoosingPanel ( thePanel , this ) );
		theTabbedPane.addTab ( UIMessages.getInstance().getMessage("gameloader.options") , opciones = new OptionChoosingPanel (  this ) );
		theTabbedPane.addChangeListener ( new ChangeListener() { public void stateChanged(ChangeEvent evt) { opciones.updateServersAndPorts(); } } );
	}
}




