/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 09-mar-2009 15:44:45
 * as file DefaultMouseMotionListener.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.gui.graph.RoomNode;

/**
 * @author carlos
 *
 * Created at regulus, 09-mar-2009 15:44:45
 */
public class DefaultMouseMotionListener implements MouseMotionListener 
{
	
	//UNUSED

	private GraphEditingPanel gep;
	
	private Node currentNode;
	private Arrow currentArrow;
	
	public DefaultMouseMotionListener ( GraphEditingPanel gep )
	{
		this.gep = gep;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) 
	{
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) 
	{
		
		Node n = gep.nodeAt(gep.panelToMapX(e.getX()),gep.panelToMapY(e.getY()));
		
		if ( currentNode != null && currentNode != n )
		{
			currentNode.setHighlighted ( false );
			currentNode = null;
		}
		
		if ( n != null && currentNode != n )
		{
			n.setHighlighted ( true );
			currentNode = n;
		}
		

		
	}
	
	//motion motion listener to use when no tool is selected
	
	

}
