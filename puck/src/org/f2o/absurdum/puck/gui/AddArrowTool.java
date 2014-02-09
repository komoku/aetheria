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
public class AddArrowTool extends ToolAction 
{

	private Arrow prototype;
	private MouseListener listener;
	private MouseMotionListener motionListener;
	
	private Node source;
	
	public AddArrowTool ( Arrow prototype , GraphEditingPanel panel )
	{
		super(panel);
		this.prototype = prototype;
		
		listener = new MouseListener()
		{
			public void mouseClicked(MouseEvent arg0) 
			{
				if ( source == null )
				{
					Node n = getPanel().nodeAt(getPanel().panelToMapX(arg0.getX()),getPanel().panelToMapY(arg0.getY()));
					
					//structural arrows only allowed from rooms
					//nay! chars have items, etc.
					//if ( AddArrowTool.this.prototype instanceof StructuralArrow && !(n instanceof RoomNode ) )
					//	return;
					
					source = n;
					if ( n != null )
					{
						InvisibleNode in = new InvisibleNode(getPanel().panelToMapX(arg0.getX()),getPanel().panelToMapY(arg0.getY()));
						getPanel().setSpecialNode(in);
						Arrow a = (Arrow) AddArrowTool.this.prototype.clone();
						a.setSource(source);
						a.setDestination(in);
						getPanel().setSpecialArrow(a);
					}
					
				}
				else
				{
					Node n = getPanel().nodeAt(getPanel().panelToMapX(arg0.getX()),getPanel().panelToMapY(arg0.getY()));
					if ( n != null )
					{
						Arrow a = (Arrow) AddArrowTool.this.prototype.clone();
						a.setSource(source);
						a.setDestination(n);
						getPanel().setSpecialNode(null);
						getPanel().setSpecialArrow(null);
						source.addArrow(a); //this adds it to the panel
						getPanel().resetSelections();
						getPanel().selectArrow(a);
						getPanel().getPropertiesPanel().show(a);
						getPanel().getPropertiesPanel().repaint();
						source = null;
						/*
						AddArrowTool.this.panel.setToolListener(null);
						AddArrowTool.this.panel.setToolMotionListener(new DefaultMouseMotionListener(AddArrowTool.this.panel));
						*/			
						toolDone();
					}
				}
				getPanel().repaint();

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
				if ( getPanel().getSpecialNode() != null )
				{
					getPanel().getSpecialNode().setLocation(getPanel().panelToMapX(arg0.getX()),getPanel().panelToMapY(arg0.getY()));
					getPanel().repaint();
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
		getPanel().setSpecialNode(null);
		if ( getPanel().getSpecialArrow() != null )
		{
			Arrow a = getPanel().getSpecialArrow();
			a.getSource().removeArrow(a);
		}
		getPanel().setSpecialArrow(null);
	}
	
	public void loadTool() 
	{
		cleanup();
		getPanel().setToolListener(listener);
		getPanel().setToolMotionListener(motionListener);
		getPanel().setCursor(CursorHandler.getInstance().getCursor("ADDARROW"));	
	}

	public void unloadTool()
	{
		cleanup();
		getPanel().resetToolListeners();
		getPanel().setCursor(CursorHandler.getInstance().getCursor("DEFAULT"));
	}
	
}
