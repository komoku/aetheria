/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:51:01
 * as file AddNodeTool.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.f2o.absurdum.puck.gui.cursors.CursorHandler;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.gui.panels.EntityPanel;
import org.f2o.absurdum.puck.gui.panels.WorldPanel;
import org.f2o.absurdum.puck.i18n.Messages;
import org.f2o.absurdum.puck.util.debug.Debug;
import org.f2o.absurdum.puck.util.xml.DOMUtils;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:51:01
 */
public class PasteNodeAction extends AbstractAction 
{

	//private Node prototype;
	private GraphEditingPanel panel;
	
	public PasteNodeAction ( GraphEditingPanel panel )
	{
		this.panel = panel;
		this.putValue(Action.NAME,Messages.getInstance().getMessage("menuaction.paste"));
	}

	public void actionPerformed(ActionEvent arg0) 
	{
		Clipboard clipboard = panel.getToolkit().getSystemClipboard();
		
		try
		{
			String theString = (String) clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor);
			org.w3c.dom.Element e = (org.w3c.dom.Element) DOMUtils.stringToNode(theString);
			e.setAttribute("name",e.getAttribute("name")+"(copy)");
			WorldPanel wp = (WorldPanel) panel.getWorldNode().getAssociatedPanel();
			while(wp.nameToNode(e.getAttribute("name"))!=null)
			{
				e.setAttribute("name",e.getAttribute("name")+"(otra)");
			}
			Point coords = null;
			Point mousePos = panel.getMousePosition();
			if ( mousePos != null ) //entity is pasted at mouse position if possible
			{
				coords = new Point(panel.panelToMapX(mousePos.x),panel.panelToMapY(mousePos.y));
			}
			wp.addEntityFromXML(e,null,-1,coords);
		}
		catch ( Exception e )
		{
			JOptionPane.showMessageDialog(panel,Messages.getInstance().getMessage("error.clip.nonode"),"Whoops!",JOptionPane.ERROR_MESSAGE);
			Debug.println("The clipboard does not contain a valid node to paste.");
			e.printStackTrace();
		}
	}

}
