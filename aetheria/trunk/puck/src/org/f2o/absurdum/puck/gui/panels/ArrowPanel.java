/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.gui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.i18n.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/*
 Created 08/02/2008 20:13:12
 When it is done, this should be the class from where all panels associated to arrows
 should inherit. It should add a PropertiesPanel with the custom relationship properties.
 */

public class ArrowPanel extends GraphElementPanel
{

	protected Arrow theArrow;
	
	protected Vector relTypes = new Vector(); //structural relationship types
	
	protected String relationshipType; //type of structural relationship or NO_STRUCTURAL_RELATIONSHIP
	
	//we don't know where they will be added but these combo boxes must exist
	protected JComboBox srcComboBox = new JComboBox();
	protected JComboBox dstComboBox = new JComboBox();
	protected JComboBox relComboBox = new JComboBox(); //this one may be invisible, but it should exist too
	
	//possible destinations, these nodes will be referenced by the combo boxes
	protected Vector srcNodes;
	protected Vector dstNodes;
	
	private PropertiesPanel customRelationshipsPanel;
	
	//the tabbed pane
	protected JTabbedPane jtp = new JTabbedPane();
	

	
	//String denoting that there is no structural relationship associated to an arrow (only, maybe, custom relationships)
	public static final String NO_STRUCTURAL_RELATIONSHIP = Messages.getInstance().getMessage("structural.none");
	
	
	public String getNameForElement()
	{
		if ( customRelationshipsPanel != null && relationshipType == NO_STRUCTURAL_RELATIONSHIP )
		{
			String report = customRelationshipsPanel.getReport();
			if ( report.length() > 0 ) return report;
		}		
		return relationshipType;
	}
	
	public void setRelationshipType( String relType )
	{
		this.relationshipType = relType;
	}
	
	public String getRelationshipType()
	{
		return relationshipType;
	}
	
	public Vector getPossibleSourceNodes()
	{
		return new Vector();
		//return this.getGraphEditingPanel().getCharNodes();
	}
	
	public Vector getPossibleDestinationNodes()
	{
		return new Vector();
		//return this.getGraphEditingPanel().getItemNodes();
	}
	
	//returns the index of the object in v whose name is s.
	protected int indexOf ( Vector v , String s )
	{
		for ( int i = 0 ; i < v.size() ; i++ )
		{
			if ( s.equals(v.get(i).toString()) )
				return i;
		}
		return -1;
	}
	
	public void doInitMinimal()
	{
		addCustomRelationshipsTab();
		srcNodes =
			getPossibleSourceNodes();
		dstNodes =
			getPossibleDestinationNodes();
	}
	
	public void linkWithGraph()
	{
		
		/*
		private PropertiesPanel temp = null;
		if ( customRelationshipsPanel != null )
			temp = customRelationshipsPanel;
		*/
		
		removeAll();
		jtp.removeAll();
		this.add(jtp);
				
		
		initMinimal(); //init srcNodes, dstNodes

		srcComboBox = new JComboBox ( new DefaultComboBoxModel ( srcNodes ) );
		dstComboBox = new JComboBox ( new DefaultComboBoxModel ( dstNodes ) );
		relComboBox = new JComboBox ( new DefaultComboBoxModel ( relTypes) );
		
		srcComboBox.addActionListener ( new ActionListener() 
		{
			public void actionPerformed ( ActionEvent evt )
			{
				Node n = (Node) srcNodes.get(srcComboBox.getSelectedIndex());
				theArrow.setSource(n);
			}
		}
		);

		dstComboBox.addActionListener ( new ActionListener() 
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( dstComboBox.getSelectedIndex() >= 0 )
				{
					Node n = (Node) dstNodes.get(dstComboBox.getSelectedIndex());
					theArrow.setDestination(n);
				}
			}
		}
		);
		
		relComboBox.addActionListener ( new ActionListener() 
		{
			public void actionPerformed ( ActionEvent evt )
			{
				relationshipType = (String) relTypes.get(relComboBox.getSelectedIndex());
			}
		}
		);
		
		srcComboBox.setSelectedIndex( indexOf(srcNodes,theArrow.getSource().getName()) );
		dstComboBox.setSelectedIndex( indexOf(dstNodes,theArrow.getDestination().getName()) );
		relComboBox.setSelectedIndex( indexOf(relTypes,relationshipType) );
		
	}
	
	public void refresh()
	{
		
			srcComboBox.setModel(new DefaultComboBoxModel(getPossibleSourceNodes()));
			dstComboBox.setModel(new DefaultComboBoxModel(getPossibleDestinationNodes()));
			relComboBox.setModel( new DefaultComboBoxModel ( relTypes) );
			srcComboBox.setSelectedIndex( indexOf(srcNodes,theArrow.getSource().getName()) );
			dstComboBox.setSelectedIndex( indexOf(dstNodes,theArrow.getDestination().getName()) );
			relComboBox.setSelectedIndex( indexOf(relTypes,relationshipType) );
	}
	
	public ArrowPanel( Arrow theArrow )
	{
		super();
		this.theArrow = theArrow;
	}
	
	public void addCustomRelationshipsTab()
	{	
		if ( customRelationshipsPanel == null ) //it could be != null and relevant if a save calls initMinimal and then the delayed load calls it too
			customRelationshipsPanel = new PropertiesPanel( Messages.getInstance().getMessage("label.relationships"));	
		JPanel customRelationshipsTab = new JPanel();
		customRelationshipsTab.setLayout(new BoxLayout(customRelationshipsTab, BoxLayout.PAGE_AXIS));
		customRelationshipsTab.add(customRelationshipsPanel);
		jtp.add(customRelationshipsTab,Messages.getInstance().getMessage("tab.customrel"));
	}
	
	private String getDestinationName()
	{
		return dstComboBox.getSelectedItem().toString();
	}

	public org.w3c.dom.Node getCustomRelationshipXML ( Document d )
	{
		forceRealCustomRelationshipsInitFromXML(); 
		
		Element result = d.createElement("Relationship");
		
		result.setAttribute("id",getDestinationName());
		
		Element elt = (Element) customRelationshipsPanel.getXML(d);
		
		result.appendChild(elt);
		
		return result;
		
	}
	
	private org.w3c.dom.Node cachedRelationshipsNode = null; 
	boolean inittedRels = false;
	
	synchronized public final void forceRealCustomRelationshipsInitFromXML ( )
	{
		if ( isCacheEnabled() && !inittedRels && cachedRelationshipsNode != null )
		{
			doInitCustomRelationshipsFromXML(cachedRelationshipsNode);
			inittedRels = true;
			cachedRelationshipsNode = null;
		}
	}
	
	public final void initCustomRelationshipsFromXML ( org.w3c.dom.Node n )
	{
		if ( isCacheEnabled() )
		{
			cachedRelationshipsNode = n;
			/*
			synchronized(cachedNotInitted)
			{
				cachedNotInitted.offer(this);
			}
			*/
		}
		else
			doInitCustomRelationshipsFromXML ( n );
	}
	
	
	
	public void doInitCustomRelationshipsFromXML ( org.w3c.dom.Node n )
	{
		
		Element e = (Element) n;
		
		NodeList nl1 = e.getElementsByTagName("PropertyList");
		
		if ( nl1.getLength() > 0 )
		{
			Element plElt = (Element) nl1.item(0);
			customRelationshipsPanel.initFromXML(plElt);
		}
		 
	}
	
}
