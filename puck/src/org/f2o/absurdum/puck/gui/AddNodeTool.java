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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

import org.f2o.absurdum.puck.gui.cursors.CursorHandler;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:51:01
 */
public class AddNodeTool extends ToolAction 
{

	private Node prototype;
	private MouseListener listener;
	private MouseMotionListener motionListener;
	
	private int lastSpecialNodeLocationX;
	private int lastSpecialNodeLocationY;
	
	public AddNodeTool ( Node prototype , GraphEditingPanel panel )
	{
		super(panel);
		
		this.prototype = prototype;
		
		listener = new MouseListener()
		{
			public void mouseClicked(MouseEvent arg0) 
			{
				Node n = getPanel().getSpecialNode();
				/*
				AddNodeTool.this.panel.setToolListener(null);
				AddNodeTool.this.panel.setToolMotionListener(null);
				*/
				//getPanel().resetToolListeners();
				getPanel().setSpecialNode(null);
				getPanel().addNode(n);
				getPanel().resetSelections();
				getPanel().selectNode(n);
				getPanel().getPropertiesPanel().show(n);
				getPanel().getPropertiesPanel().repaint();
				getPanel().repaint();
				//getPanel().setCursor(CursorHandler.getInstance().getCursor("DEFAULT"));
				toolDone();
			}
			public void mouseEntered(MouseEvent arg0) 
			{
				
			}
			public void mouseExited(MouseEvent arg0) 
			{
				
			}
			public void mousePressed(MouseEvent arg0) 
			{
				
			}
			public void mouseReleased(MouseEvent arg0) 
			{
				
			}
			
		};
		
		motionListener = new MouseMotionListener()
		{

			public void mouseDragged(MouseEvent arg0) 
			{
				
			}

			public void mouseMoved(MouseEvent arg0) 
			{
				/*
				AddNodeTool.this.prototype.paint(
						AddNodeTool.this.panel.getGraphics(),arg0.getX(),arg0.getY());
				*/
				
				int newLocationX = getPanel().panelToMapX(arg0.getX());
				int newLocationY = getPanel().panelToMapY(arg0.getY());
				
				if ( getPanel().isSnapToGridEnabled( ))
				{
					newLocationX = (newLocationX/20)*20;
					newLocationY = (newLocationY/20)*20;
				}
				
				lastSpecialNodeLocationX = newLocationX;
				lastSpecialNodeLocationY = newLocationY;
				
				getPanel().getSpecialNode().setLocation(newLocationX,newLocationY);
				
				
				getPanel().repaint();
				
				/*
				AddNodeTool.this.panel.repaint();
				SwingUtilities.invokeLater
				(
						new Runnable()
						{
							public void run()
							{
								AddNodeTool.this.panel.repaint();
								System.out.println("Nay.");
							}
						}
				);
				*/
			}
			
		};
		
	}
	
	public void loadTool()
	{
		getPanel().setToolListener(listener);
		getPanel().setToolMotionListener(motionListener);
		getPanel().setSpecialNode((Node)prototype.clone());
		getPanel().getSpecialNode().setLocation(lastSpecialNodeLocationX,lastSpecialNodeLocationY);
		getPanel().setCursor(CursorHandler.getInstance().getCursor("ADD"));
	}
	
	public void unloadTool()
	{
		getPanel().resetToolListeners();
		getPanel().setCursor(CursorHandler.getInstance().getCursor("DEFAULT"));
	}

}
