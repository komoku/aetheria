/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.f2o.absurdum.puck.gui.cursors.CursorHandler;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;

/*
 Created 21/11/2007 20:23:43
 */

public class ExecAgeTool extends AbstractAction 
{
	
	private PuckFrame frame;
	
	public ExecAgeTool ( PuckFrame frame )
	{
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent arg0) 
	{
		
		frame.runCurrentFileInAge();
		
	}
	
}
