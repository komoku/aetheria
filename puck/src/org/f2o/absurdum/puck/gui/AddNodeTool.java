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

import org.f2o.absurdum.puck.gui.cursors.CursorHandler;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:51:01
 */
public class AddNodeTool extends AbstractAction 
{

	private Node prototype;
	private GraphEditingPanel panel;
	private MouseListener listener;
	private MouseMotionListener motionListener;
	
	public AddNodeTool ( Node prototype , GraphEditingPanel panel )
	{
		this.prototype = prototype;
		this.panel = panel;
		
		listener = new MouseListener()
		{
			public void mouseClicked(MouseEvent arg0) 
			{
				Node n = AddNodeTool.this.panel.getSpecialNode();
				/*
				AddNodeTool.this.panel.setToolListener(null);
				AddNodeTool.this.panel.setToolMotionListener(null);
				*/
				AddNodeTool.this.panel.resetToolListeners();
				AddNodeTool.this.panel.setSpecialNode(null);
				AddNodeTool.this.panel.addNode(n);
				AddNodeTool.this.panel.resetSelections();
				AddNodeTool.this.panel.selectNode(n);
				AddNodeTool.this.panel.getPropertiesPanel().show(n);
				AddNodeTool.this.panel.getPropertiesPanel().repaint();
				AddNodeTool.this.panel.repaint();
				AddNodeTool.this.panel.setCursor(CursorHandler.getInstance().getCursor("DEFAULT"));
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
				
				int newLocationX = AddNodeTool.this.panel.panelToMapX(arg0.getX());
				int newLocationY = AddNodeTool.this.panel.panelToMapY(arg0.getY());
				
				if ( AddNodeTool.this.panel.isSnapToGridEnabled( ))
				{
					newLocationX = (newLocationX/20)*20;
					newLocationY = (newLocationY/20)*20;
				}
				
				AddNodeTool.this.panel.getSpecialNode().setLocation(newLocationX,newLocationY);
				
				
				AddNodeTool.this.panel.repaint();
				
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

	public void actionPerformed(ActionEvent arg0) 
	{
		
		panel.setToolListener(listener);
		panel.setToolMotionListener(motionListener);
		panel.setSpecialNode((Node)prototype.clone());
		panel.setCursor(CursorHandler.getInstance().getCursor("ADD"));
		
	}

}
