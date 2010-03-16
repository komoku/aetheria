/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

/*
 * Created at regulus on 08-abr-2007 11:09:09
 * as file ItemHasItemPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.i18n.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author carlos
 *
 * Created at regulus, 08-abr-2007 11:09:09
 */
public class ItemHasItemPanel extends ArrowPanel 
{
	

	
	public ItemHasItemPanel( Arrow theArrow )
	{
		
		super(theArrow);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		//possible structural relationship types
		relTypes.clear();
		relTypes.add(Messages.getInstance().getMessage("structural.item.item.contain"));
		relTypes.add(Messages.getInstance().getMessage("structural.item.item.haspart"));
		relTypes.add(Messages.getInstance().getMessage("structural.item.item.haskey"));
		relTypes.add(ArrowPanel.NO_STRUCTURAL_RELATIONSHIP);
		
		//default structural relationship type
		this.relationshipType = Messages.getInstance().getMessage("structural.item.item.contain"); //default relationship is containment
		
		add(new JLabel("Item to Item Relationship"));
	
	}
	
	public Vector getPossibleSourceNodes()
	{
		return this.getGraphEditingPanel().getItemNodes(false);
	}
	
	public Vector getPossibleDestinationNodes()
	{
		return this.getGraphEditingPanel().getItemNodes(false);
	}
	
	public void setRelationshipType( String relType )
	{
		this.relationshipType = relType;
	}
	
	public String toString()
	{
		return "Yet Another Item to Item Panel (rel = " + relationshipType + ")";
	}
	
	public void linkWithGraph()
	{
		
		super.linkWithGraph(); //this does a removeAll().
		
		JPanel mainTab = new JPanel();
		mainTab.setLayout(new BoxLayout(mainTab,BoxLayout.PAGE_AXIS));
		
		JPanel srcPanel = new JPanel();
		srcPanel.add ( new JLabel(Messages.getInstance().getMessage("itemitem.src")) );
		srcPanel.add ( srcComboBox );
		mainTab.add(srcPanel);
		
		JPanel relTypePanel = new JPanel();
		relTypePanel.add ( new JLabel(Messages.getInstance().getMessage("itemitem.reltype")) );
		relTypePanel.add(relComboBox);
		mainTab.add(relTypePanel);
		
		JPanel dstPanel = new JPanel();
		dstPanel.add ( new JLabel(Messages.getInstance().getMessage("itemitem.dst")) );
		dstPanel.add ( dstComboBox );
		mainTab.add(dstPanel);
		
		//setVisible(true);
		
		jtp.add(mainTab,Messages.getInstance().getMessage("tab.structrel"),0);
		jtp.setSelectedIndex(0);
		
	}
	
	public org.w3c.dom.Node doGetXML ( Document d )
	{
		
		Element result = d.createElement("ItemRef");
		
		result.setAttribute("id",dstComboBox.getSelectedItem().toString());
										
		return result;
		
	}
	

	//from ItemRef node
	public void doInitFromXML ( org.w3c.dom.Node n )
	{
		linkWithGraph(); //this should already fix the combo box values alright
		//if the arrow was created correctly between the corresponding nodes
	}
	
	
}

