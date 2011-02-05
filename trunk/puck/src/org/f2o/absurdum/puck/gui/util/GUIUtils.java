/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 05/02/2011 11:25:28
 */
package org.f2o.absurdum.puck.gui.util;

import java.awt.Dimension;

import javax.swing.JComponent;

/**
 * @author carlos
 *
 */
public class GUIUtils 
{

	public static JComponent limitVertically ( JComponent comp )
	{
		int prefH = (int) comp.getPreferredSize().getHeight();
		int maxW = (int) comp.getMaximumSize().getWidth();
		comp.setMaximumSize(new Dimension(maxW,prefH));
		return comp;
	}
	
	public static JComponent limitHorizontally ( JComponent comp )
	{
		int prefW = (int) comp.getPreferredSize().getWidth();
		int maxH = (int) comp.getMaximumSize().getHeight();
		comp.setMaximumSize(new Dimension(prefW,maxH));
		return comp;
	}
	
}
