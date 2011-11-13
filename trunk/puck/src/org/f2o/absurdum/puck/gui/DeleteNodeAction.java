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

import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.i18n.UIMessages;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:51:01
 */
public class DeleteNodeAction extends AbstractAction 
{

	private Node victim;
	private GraphEditingPanel panel;
	
	public DeleteNodeAction ( Node victim , GraphEditingPanel panel )
	{
		this.victim = victim;
		this.panel = panel;
		this.putValue(Action.NAME,UIMessages.getInstance().getMessage("menuaction.delete") + " " + victim.getName());		
		
	}

	public void actionPerformed(ActionEvent arg0) 
	{
		
		panel.totallyRemoveNode(victim);
		
	}

}
