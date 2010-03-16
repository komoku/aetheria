/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 19:05:17
 * as file PropertiesPanel.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JPanel;

import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.GraphElement;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.gui.graph.PanelsTable;
import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;
import org.f2o.absurdum.puck.gui.panels.RoomPanel;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 19:05:17
 */
public class PropertiesPanel extends JPanel 
{

	private CardLayout cl = new CardLayout();
	
	private Set addedPanels = new LinkedHashSet();
	
	private GraphEditingPanel gep;
	
	
	

	
	
	
	public PropertiesPanel()
	{
		super();
		setLayout ( cl );
	}
	
	public void clear()
	{
		addedPanels = new LinkedHashSet();
		this.removeAll();
	}
	
	
	public void setGraphEditingPanel ( GraphEditingPanel gep )
	{
		this.gep = gep;
	}
	
	//al final es casi como show. No diff'ce. Sin show() no tiraba. (why?)
	public void loadWithoutShowing ( GraphElement n )
	{
		//introducir en Maps y tal. Útil al cargar (load).
		GraphElementPanel np = n.getAssociatedPanel();
		if ( np != null )
		{
			if ( !addedPanels.contains(np) )
			{
				np.setGraphEditingPanel(gep);
				np.initMinimal();
				addedPanels.add(np);
				
				np.setVisible(false);
				this.add(np,np.getID());
				np.refresh();			
				
				//cl.show(this,np.getID());
				
				//np.setVisible(true);
				this.revalidate();
				
				//this.repaint();			
			
			}
			else
			{
				np.refresh();
				
				
				//cl.show(this,np.getID());
				
				//np.setVisible(false);
				//this.validate();
				
				//this.repaint();
			}
		}
	}
	
	//this is called by mouse events (clicking on a graph node, etc.)
	//and shows the panel associated to that node in the PropertiesPanel's card layout
	public void show ( GraphElement n )
	{
		//JPanel jp = PanelsTable.getInstance().getPanel(n);
		GraphElementPanel np = n.getAssociatedPanel();
		
		if ( np != null )
		{
			if ( !addedPanels.contains(np) )
			{
				np.setGraphEditingPanel(gep);
				np.linkWithGraph();
				addedPanels.add(np);
				this.add(np,np.getID());
				np.refresh();			
				cl.show(this,np.getID());
				this.repaint();			
			
			}
			else
			{
				np.refresh();
				cl.show(this,np.getID());
				this.repaint();
			}
		}
	}
	
}
