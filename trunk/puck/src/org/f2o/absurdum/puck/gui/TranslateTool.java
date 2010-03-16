/*
 * (c) 2005-2006 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 16-dic-2006 13:59:55
 * as file TranslateTool.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;


import javax.swing.AbstractAction;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.cursors.CursorHandler;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;

/**
 * @author carlos
 *
 * Created at regulus, 16-dic-2006 13:59:55
 */
public class TranslateTool extends AbstractAction
{

	private GraphEditingPanel panel;
	private MouseListener listener;
	private MouseMotionListener motionListener;
	
	private boolean enabled = false;
	private boolean justEnabled = true;
	private int lastX = 0;
	private int lastY = 0;
	
	private double speedFactor = 5.0;
	
	public TranslateTool ( GraphEditingPanel panel )
	{
		
		this.panel = panel;
		
		listener = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent arg0) 
			{
				enabled = !enabled;
				if ( enabled ) justEnabled = true;
				else
				{
					/*
					TranslateTool.this.panel.setToolListener(null);
					TranslateTool.this.panel.setToolMotionListener(null);
					*/
					TranslateTool.this.panel.resetToolListeners();
					//ZoomTool.this.panel.setSpecialNode(null);
					TranslateTool.this.panel.getPropertiesPanel().repaint();
					TranslateTool.this.panel.repaint();
					TranslateTool.this.panel.setCursor(CursorHandler.getInstance().getCursor("DEFAULT"));

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
						int diffY = arg0.getY()-lastY;
						int diffX = arg0.getX()-lastX;
						
						if ( "push".equals(PuckConfiguration.getInstance().getProperty("translateMode")) )
						{
							TranslateTool.this.panel.setViewXOffset( TranslateTool.this.panel.getViewXOffset() + Math.ceil(diffX / TranslateTool.this.panel.getViewZoom() * speedFactor));
							TranslateTool.this.panel.setViewYOffset( TranslateTool.this.panel.getViewYOffset() + Math.ceil(diffY / TranslateTool.this.panel.getViewZoom() * speedFactor));
						}
						else //"hold"
						{
							System.out.println("HOLD IT!");
							TranslateTool.this.panel.setViewXOffset( TranslateTool.this.panel.getViewXOffset() - Math.ceil(diffX / TranslateTool.this.panel.getViewZoom() * speedFactor));
							TranslateTool.this.panel.setViewYOffset( TranslateTool.this.panel.getViewYOffset() - Math.ceil(diffY / TranslateTool.this.panel.getViewZoom() * speedFactor));
						}
						
						TranslateTool.this.panel.repaint();
					}
					lastX = arg0.getX();
					lastY = arg0.getY();
				}
			}
		};
	
	}
	
	
	public void actionPerformed(ActionEvent arg0) 
	{
		
		panel.setToolListener(listener);
		panel.setToolMotionListener(motionListener);
		panel.setCursor(CursorHandler.getInstance().getCursor("MOVE"));
		
	}


	
}
