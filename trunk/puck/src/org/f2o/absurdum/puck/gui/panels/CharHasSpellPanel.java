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
import org.f2o.absurdum.puck.gui.graph.SpellNode;
import org.f2o.absurdum.puck.gui.graph.StructuralArrow;
import org.f2o.absurdum.puck.i18n.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author carlos
 *
 * Created at regulus2, 9 nov 2009 2:15 AM
 */
public class CharHasSpellPanel extends ArrowPanel 
{

	

	
	public CharHasSpellPanel( Arrow theArrow )
	{
		
		super(theArrow);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		//this.theArrow = theArrow;
		
		//possible structural relationship types
		relTypes.clear();
		relTypes.add(Messages.getInstance().getMessage("structural.char.spell.know"));
		relTypes.add(ArrowPanel.NO_STRUCTURAL_RELATIONSHIP);
		
		//default structural relationship type
		this.relationshipType = Messages.getInstance().getMessage("structural.char.spell.know"); //default relationship is know
		
		add(new JLabel("Char to Spell Relationship"));
		
	}
	
	public Vector getPossibleSourceNodes()
	{
		return this.getGraphEditingPanel().getCharNodes(false);
	}
	
	public Vector getPossibleDestinationNodes()
	{
		return this.getGraphEditingPanel().getNodes(SpellNode.class,false);
	}

	public String toString()
	{
		return "Yet Another Char to Spell Panel";
	}	
	
	public void linkWithGraph()
	{
				
		super.linkWithGraph(); //this does a removeAll().
		
		//(new Exception()).printStackTrace();
		
		JPanel mainTab = new JPanel();
		mainTab.setLayout(new BoxLayout(mainTab,BoxLayout.PAGE_AXIS));
		
		JPanel srcPanel = new JPanel();
		srcPanel.add ( new JLabel(Messages.getInstance().getMessage("charspell.src")) );
		srcPanel.add ( srcComboBox );
		mainTab.add(srcPanel);
		
		JPanel relTypePanel = new JPanel();
		relTypePanel.add ( new JLabel(Messages.getInstance().getMessage("charspell.reltype")) );
		relTypePanel.add(relComboBox);
		mainTab.add(relTypePanel);
		
		JPanel dstPanel = new JPanel();
		dstPanel.add ( new JLabel(Messages.getInstance().getMessage("charspell.dst")) );
		dstPanel.add ( dstComboBox );
		mainTab.add(dstPanel);
		
		//setVisible(true);
				
		jtp.add(mainTab,Messages.getInstance().getMessage("tab.structrel"),0);
		jtp.setSelectedIndex(0);
		
	}
	
	public org.w3c.dom.Node doGetXML ( Document d )
	{
		
		Element result = d.createElement("SpellRef");
		
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
