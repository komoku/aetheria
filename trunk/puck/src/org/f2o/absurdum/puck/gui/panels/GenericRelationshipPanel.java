/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.gui.panels;

import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.i18n.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 Created 10/02/2008 18:12:13
 */

public class GenericRelationshipPanel extends ArrowPanel 
{

	private Class sourceClass;
	private Class destinationClass;
	
	
	public GenericRelationshipPanel( Arrow theArrow , Class sourceClass , Class destinationClass )
	{
		
		super(theArrow);
		
		this.sourceClass = sourceClass;
		this.destinationClass = destinationClass;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		//possible structural relationship types
		relTypes.clear();
		relTypes.add(ArrowPanel.NO_STRUCTURAL_RELATIONSHIP);
		
		//default structural relationship type
		this.relationshipType = ArrowPanel.NO_STRUCTURAL_RELATIONSHIP; //default relationship is this one (containment)
		
		add(new JLabel("Generic Relationship"));
		
	}
	
	public Vector getPossibleSourceNodes()
	{
		return this.getGraphEditingPanel().getNodes(sourceClass,false);
	}
	public Vector getPossibleDestinationNodes()
	{
		return this.getGraphEditingPanel().getNodes(destinationClass,false);
	}
	
	public String toString()
	{
		return "Yet Another Generic Relationship Panel";
	}
	
	public void linkWithGraph()
	{
		
		super.linkWithGraph(); //this does a removeAll().
		
		JPanel mainTab = new JPanel();
		mainTab.setLayout(new BoxLayout(mainTab,BoxLayout.PAGE_AXIS));
		
		JPanel srcPanel = new JPanel();
		srcPanel.add ( new JLabel(Messages.getInstance().getMessage("entityentity.src")) );
		srcPanel.add ( srcComboBox );
		mainTab.add(srcPanel);
		
		//unnecessary, only one rel. type
		/*
		JPanel relTypePanel = new JPanel();
		relTypePanel.add ( new JLabel(Messages.getInstance().getMessage("charitem.reltype")) );
		relTypePanel.add(relComboBox);
		mainTab.add(relTypePanel);
		*/
		
		JPanel dstPanel = new JPanel();
		dstPanel.add ( new JLabel(Messages.getInstance().getMessage("entityentity.dst")) );
		dstPanel.add ( dstComboBox );
		mainTab.add(dstPanel);
		
		//setVisible(true);
		
		jtp.add(mainTab,Messages.getInstance().getMessage("tab.structrel"),0);
		jtp.setSelectedIndex(0);
		
	}
	
	public org.w3c.dom.Node doGetXML ( Document d )
	{
		return null;
	}
	
	//from ItemRef node
	public void doInitFromXML ( org.w3c.dom.Node n )
	{
		linkWithGraph(); //this should already fix the combo box values alright
		//if the arrow was created 
	}
	
	
}
