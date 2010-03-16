/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:51:01
 * as file AddNodeTool.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.i18n.Messages;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:51:01
 */
public class DeleteArrowAction extends AbstractAction 
{

	private Arrow victim;
	private GraphEditingPanel panel;
	
	public DeleteArrowAction ( Arrow victim , GraphEditingPanel panel )
	{
		this.victim = victim;
		this.panel = panel;
		this.putValue(Action.NAME,Messages.getInstance().getMessage("menuaction.deleterel") + " " + victim.getName());		
		
	}

	public void actionPerformed(ActionEvent arg0) 
	{
		
		victim.getSource().removeArrow(victim);
		
	}

}
