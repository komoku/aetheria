/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 04/02/2011 19:49:32
 */
package org.f2o.absurdum.puck.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

/**
 * @author carlos
 *
 * This is just a JPanel with border layout that adds some space at the sides of a component.
 */
public class SpacingPanel extends JPanel 
{

	public SpacingPanel ( Component child )
	{
		setLayout(new BorderLayout());
		add(child,BorderLayout.CENTER);
		add(new JPanel(),BorderLayout.WEST);
		add(new JPanel(),BorderLayout.EAST);
	}
	
	public SpacingPanel ( Component child , boolean west , boolean east , boolean north , boolean south )
	{
		setLayout(new BorderLayout());
		add(child,BorderLayout.CENTER);
		if ( west )
			add(new JPanel(),BorderLayout.WEST);
		if ( east )
			add(new JPanel(),BorderLayout.EAST);
		if ( north )
			add(new JPanel(),BorderLayout.NORTH);
		if ( south )
			add(new JPanel(),BorderLayout.SOUTH);
	}
	
}
