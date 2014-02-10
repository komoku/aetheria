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
 * This tool does the same than no tool loaded at all.
 * No special listeners are loaded, only the default responses to mouse events from the panel happen.
 *
 * Created 2014-02-10
 */
public class SelectTool extends ToolAction
{
	
	public SelectTool ( GraphEditingPanel panel )
	{
		super(panel);
	}
	
	
	public void loadTool()
	{
		getPanel().resetToolListeners();
		getPanel().setCursor(CursorHandler.getInstance().getCursor("DEFAULT"));
		
	}
	
	public void unloadTool()
	{
		getPanel().resetToolListeners();
		getPanel().setCursor(CursorHandler.getInstance().getCursor("DEFAULT"));
	}
	
}
