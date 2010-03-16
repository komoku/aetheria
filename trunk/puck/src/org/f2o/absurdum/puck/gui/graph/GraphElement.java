/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 22-jul-2005 19:17:52
 * as file GraphElement.java on package org.f2o.absurdum.puck.gui.graph
 */
package org.f2o.absurdum.puck.gui.graph;

import java.awt.Graphics;

import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;

/**
 * @author carlos
 *
 * Created at regulus, 22-jul-2005 19:17:52
 */
public interface GraphElement extends Cloneable
{

	public Object clone ();
	public void paint ( Graphics g );
	public GraphElementPanel getAssociatedPanel();
	
}
