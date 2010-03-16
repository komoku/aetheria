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
import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.StructuralArrow;
import org.f2o.absurdum.puck.gui.graph.RoomNode;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.InvisibleNode;
import org.f2o.absurdum.puck.gui.graph.Node;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:51:01
 */
public class AddArrowTool extends AbstractAction 
{

	private Arrow prototype;
	private GraphEditingPanel panel;
	private MouseListener listener;
	private MouseMotionListener motionListener;
	
	private Node source;
	
	public AddArrowTool ( Arrow prototype , GraphEditingPanel panel )
	{
		this.prototype = prototype;
		this.panel = panel;
		
		listener = new MouseListener()
		{
			public void mouseClicked(MouseEvent arg0) 
			{
				if ( source == null )
				{
					Node n = AddArrowTool.this.panel.nodeAt(AddArrowTool.this.panel.panelToMapX(arg0.getX()),AddArrowTool.this.panel.panelToMapY(arg0.getY()));
					
					//structural arrows only allowed from rooms
					//nay! chars have items, etc.
					//if ( AddArrowTool.this.prototype instanceof StructuralArrow && !(n instanceof RoomNode ) )
					//	return;
					
					source = n;
					if ( n != null )
					{
						InvisibleNode in = new InvisibleNode(AddArrowTool.this.panel.panelToMapX(arg0.getX()),AddArrowTool.this.panel.panelToMapY(arg0.getY()));
						AddArrowTool.this.panel.setSpecialNode(in);
						Arrow a = (Arrow) AddArrowTool.this.prototype.clone();
						a.setSource(source);
						a.setDestination(in);
						AddArrowTool.this.panel.setSpecialArrow(a);
					}
					
				}
				else
				{
					Node n = AddArrowTool.this.panel.nodeAt(AddArrowTool.this.panel.panelToMapX(arg0.getX()),AddArrowTool.this.panel.panelToMapY(arg0.getY()));
					if ( n != null )
					{
						Arrow a = (Arrow) AddArrowTool.this.prototype.clone();
						a.setSource(source);
						a.setDestination(n);
						AddArrowTool.this.panel.setSpecialNode(null);
						AddArrowTool.this.panel.setSpecialArrow(null);
						source.addArrow(a); //this adds it to the panel
						AddArrowTool.this.panel.resetSelections();
						AddArrowTool.this.panel.selectArrow(a);
						AddArrowTool.this.panel.getPropertiesPanel().show(a);
						AddArrowTool.this.panel.getPropertiesPanel().repaint();
						source = null;
						/*
						AddArrowTool.this.panel.setToolListener(null);
						AddArrowTool.this.panel.setToolMotionListener(new DefaultMouseMotionListener(AddArrowTool.this.panel));
						*/
						AddArrowTool.this.panel.resetToolListeners();
						AddArrowTool.this.panel.setCursor(CursorHandler.getInstance().getCursor("DEFAULT"));
						
					}
				}
				AddArrowTool.this.panel.repaint();

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
				if ( AddArrowTool.this.panel.getSpecialNode() != null )
				{
					AddArrowTool.this.panel.getSpecialNode().setLocation(AddArrowTool.this.panel.panelToMapX(arg0.getX()),AddArrowTool.this.panel.panelToMapY(arg0.getY()));
					AddArrowTool.this.panel.repaint();
				}
				
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

	private void cleanup()
	{
		//cleanup arrows without destination from prior uses of this tool
		source=null;
		panel.setSpecialNode(null);
		if ( panel.getSpecialArrow() != null )
		{
			Arrow a = panel.getSpecialArrow();
			a.getSource().removeArrow(a);
		}
		panel.setSpecialArrow(null);
	}
	
	public void actionPerformed(ActionEvent arg0) 
	{
		
		cleanup();
		panel.setToolListener(listener);
		panel.setToolMotionListener(motionListener);
		panel.setCursor(CursorHandler.getInstance().getCursor("ADDARROW"));
		
	}

}
