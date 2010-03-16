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

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 18:51:54
 */
public class RoomHasCharPanel extends ArrowPanel 
{
	
	
	public RoomHasCharPanel( Arrow theArrow )
	{
		
		super(theArrow);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		//possible structural relationship types
		relTypes.clear();
		relTypes.add(Messages.getInstance().getMessage("structural.room.char"));
		relTypes.add(ArrowPanel.NO_STRUCTURAL_RELATIONSHIP);
		
		//default structural relationship type
		this.relationshipType = Messages.getInstance().getMessage("structural.room.char"); //default relationship is this one (containment)
		
		add(new JLabel("Room to Character Relationship"));
		
	}
	
	public Vector getPossibleSourceNodes()
	{
		return this.getGraphEditingPanel().getRoomNodes(false);
	}
	
	public Vector getPossibleDestinationNodes()
	{
		return this.getGraphEditingPanel().getCharNodes(false);
	}
	
	public String toString()
	{
		return "Yet Another Room to Character Panel";
	}
	
	public void linkWithGraph()
	{
		
		super.linkWithGraph(); //this does a removeAll().
		
		JPanel mainTab = new JPanel();
		mainTab.setLayout(new BoxLayout(mainTab,BoxLayout.PAGE_AXIS));
		
		JPanel srcPanel = new JPanel();
		srcPanel.add ( new JLabel(Messages.getInstance().getMessage("roomchar.src")) );
		srcPanel.add ( srcComboBox );
		mainTab.add(srcPanel);
		
		JPanel relTypePanel = new JPanel();
		relTypePanel.add ( new JLabel(Messages.getInstance().getMessage("charitem.reltype")) );
		relTypePanel.add(relComboBox);
		mainTab.add(relTypePanel);
		
		JPanel dstPanel = new JPanel();
		dstPanel.add ( new JLabel(Messages.getInstance().getMessage("roomchar.dst")) );
		dstPanel.add ( dstComboBox );
		mainTab.add(dstPanel);
		
		//setVisible(true);
		
		jtp.add(mainTab,Messages.getInstance().getMessage("tab.structrel"),0);
		jtp.setSelectedIndex(0);	
		
	}
	
	public org.w3c.dom.Node doGetXML ( Document d )
	{
		
		Element result = d.createElement("MobRef");
		
		result.setAttribute("id",dstComboBox.getSelectedItem().toString());
										
		return result;
		
	}
	
	//from MobRef node
	public void doInitFromXML ( org.w3c.dom.Node n )
	{
		linkWithGraph(); //this should already fix the combo box values alright
		//if the arrow was created correctly between the corresponding nodes
	}
	
	
}
