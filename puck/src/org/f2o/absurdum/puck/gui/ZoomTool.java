/*
 * (c) 2005-2006 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 16-dic-2006 11:53:27
 * as file ZoomTool.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;

import org.f2o.absurdum.puck.gui.cursors.CursorHandler;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;

/**
 * @author carlos
 *
 * Created at regulus, 16-dic-2006 11:53:27
 */
public class ZoomTool extends ToolAction
{

	private MouseListener listener;
	private MouseMotionListener motionListener;
	
	private boolean enabled = false;
	private boolean justEnabled = true;
	private int lastX = 0;
	private int lastY = 0;
	
	public ZoomTool ( GraphEditingPanel panel )
	{
		
		super(panel);
		
		listener = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent arg0) 
			{
				enabled = !enabled;
				if ( enabled ) justEnabled = true;
				else
				{
					/*
					ZoomTool.this.panel.setToolListener(null);
					ZoomTool.this.panel.setToolMotionListener(null);
					*/
					
					toolDone();
					
					//ZoomTool.this.panel.setSpecialNode(null);
					getPanel().getPropertiesPanel().repaint();
					getPanel().repaint();
					

				}
			}
		};
		
		motionListener = new MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent arg0)
			{
				if ( enabled )
				{
					if ( justEnabled )
						justEnabled = false;
					else
					{
						int diff = arg0.getY()-lastY;
						double factor = Math.pow(2.0,diff/100.0);
						getPanel().multiplyZoom(factor);
						getPanel().repaint();
					}
					lastX = arg0.getX();
					lastY = arg0.getY();
				}
			}
			/*
			public void mouseDragged(MouseEvent arg0)
			{
				if ( justEnabled )
					justEnabled = false;
				else
				{
					int diff = arg0.getY()-lastY;
					double factor = Math.pow(2.0,diff/100.0);
					ZoomTool.this.panel.multiplyZoom(factor);
					ZoomTool.this.panel.repaint();
				}
				lastX = arg0.getX();
				lastY = arg0.getY();
			}
			*/
		};
	
	}
	
	
	public void loadTool()
	{
		getPanel().setToolListener(listener);
		getPanel().setToolMotionListener(motionListener);
		getPanel().setCursor(CursorHandler.getInstance().getCursor("ZOOM"));
	}
	
	public void unloadTool()
	{
		getPanel().resetToolListeners();
		getPanel().setCursor(CursorHandler.getInstance().getCursor("DEFAULT"));
	}

	
}
