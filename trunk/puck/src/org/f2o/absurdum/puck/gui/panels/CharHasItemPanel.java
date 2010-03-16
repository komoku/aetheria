/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 18:51:54
 * as file EntityPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.gui.graph.StructuralArrow;
import org.f2o.absurdum.puck.i18n.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 18:51:54
 */
public class CharHasItemPanel extends ArrowPanel 
{

	

	
	public CharHasItemPanel( Arrow theArrow )
	{
		
		super(theArrow);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		//this.theArrow = theArrow;
		
		//possible structural relationship types
		relTypes.clear();
		relTypes.add(Messages.getInstance().getMessage("structural.char.item.carry"));
		relTypes.add(Messages.getInstance().getMessage("structural.char.item.haspart"));
		relTypes.add(ArrowPanel.NO_STRUCTURAL_RELATIONSHIP);
		
		//default structural relationship type
		this.relationshipType = Messages.getInstance().getMessage("structural.char.item.carry"); //default relationship is carry
		
		add(new JLabel("Char to Item Relationship"));
		
	}
	
	public Vector getPossibleSourceNodes()
	{
		return this.getGraphEditingPanel().getCharNodes(false);
	}
	
	public Vector getPossibleDestinationNodes()
	{
		return this.getGraphEditingPanel().getItemNodes(false);
	}

	public String toString()
	{
		return "Yet Another Char to Item Panel";
	}	
	
	public void linkWithGraph()
	{
				
		super.linkWithGraph(); //this does a removeAll().
		
		//(new Exception()).printStackTrace();
		
		JPanel mainTab = new JPanel();
		mainTab.setLayout(new BoxLayout(mainTab,BoxLayout.PAGE_AXIS));
		
		JPanel srcPanel = new JPanel();
		srcPanel.add ( new JLabel(Messages.getInstance().getMessage("charitem.src")) );
		srcPanel.add ( srcComboBox );
		mainTab.add(srcPanel);
		
		JPanel relTypePanel = new JPanel();
		relTypePanel.add ( new JLabel(Messages.getInstance().getMessage("charitem.reltype")) );
		relTypePanel.add(relComboBox);
		mainTab.add(relTypePanel);
		
		JPanel dstPanel = new JPanel();
		dstPanel.add ( new JLabel(Messages.getInstance().getMessage("charitem.dst")) );
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
