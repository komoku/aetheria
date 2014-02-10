package org.f2o.absurdum.puck.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.f2o.absurdum.puck.gui.cursors.CursorHandler;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;

public abstract class ToolAction extends AbstractAction 
{
	
	private static boolean toolSelectionPersistent = false;
	
	private GraphEditingPanel panel;
	
	/**
	 * Returns whether the tool should remain selected after use.
	 */
	public boolean isToolSelectionPersistent()
	{
		return toolSelectionPersistent;
	}
	
	public ToolAction ( GraphEditingPanel panel )
	{
		this.panel = panel;
	}
	
	/**
	 * Deselects this tool if tool selection is not configured to be persistent.
	 * Else, re-prepares this action for execution.
	 */
	public void toolDone()
	{
		unloadTool();
		if ( isToolSelectionPersistent() )
		{
			loadTool();
		}
		else
		{
			this.putValue(SELECTED_KEY, Boolean.FALSE);
		}
		
	}
	
	/**When tool has been selected*/
	public void actionPerformed(ActionEvent arg0) 
	{
		//if tool needs special node/arrow, it will init them in loadTool. But clear those from previously-used tools.
		if ( panel.getSpecialNode() != null ) panel.setSpecialNode(null);
		if ( panel.getSpecialArrow() != null ) panel.setSpecialArrow(null);
		panel.repaint();
		
		//template method that loads the tool
		loadTool();
	}

	/**Does the necessary processing to select and ready the tool for usage.*/
	public abstract void loadTool();
	
	/**Does the necessary processing to unselect and unready the tool for usage.*/
	public abstract void unloadTool();

	public GraphEditingPanel getPanel() 
	{
		return panel;
	}

	public void setPanel(GraphEditingPanel panel) 
	{
		this.panel = panel;
	}
	
}
