/*
 * (c) 2005-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-abr-2009 16:40:01
 * as file CutNodeAction.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.i18n.UIMessages;
import org.f2o.absurdum.puck.util.xml.DOMUtils;
import org.w3c.dom.Element;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:51:01
 */
public class CutNodeAction extends AbstractAction 
{

	private Node victim;
	private GraphEditingPanel panel;
	
	public CutNodeAction ( Node victim , GraphEditingPanel panel )
	{
		this.victim = victim;
		this.panel = panel;
		this.putValue(Action.NAME,UIMessages.getInstance().getMessage("menuaction.cut") + " " + victim.getName());		
		
	}

	public void actionPerformed(ActionEvent arg0) 
	{
		org.w3c.dom.Node n = victim.getAssociatedPanel().getXML(DOMUtils.getXMLClipboard());
		Clipboard clipboard = panel.getToolkit().getSystemClipboard();
		clipboard.setContents( new StringSelection(DOMUtils.nodeToString(n)) , GlobalAgent.getInstance() );
		panel.totallyRemoveNode(victim);
	}

}
