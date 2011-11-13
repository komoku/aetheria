/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 18:51:54
 * as file EntityPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.f2o.absurdum.puck.bsh.BeanShellCodeHolder;
import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.RoomNode;
import org.f2o.absurdum.puck.i18n.UIMessages;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 18:51:54
 */
public class RoomPanel extends EntityPanel implements BeanShellCodeHolder
{

	//private JTextField nameTextField = new EnhancedJTextField(20);
	
	private JComboBox extendsComboBox = new JComboBox();
	
	//privatize
	public DescriptionListPanel dlp;
	private ExtraDescriptionsPanel edp;
	private BSHCodePanel bcp;
	private PropertiesPanel pp;
	
	//the tabbed pane
	private JTabbedPane jtp;
	
	private RoomNode node;
	
	public RoomPanel(RoomNode node)
	{
		super();
		nameTextField.setText("Room #"+getID());
		this.node = node;
		//add ( extendsComboBox );
	}
	
	public String toString()
	{
		return nameTextField.getText() + "##" + super.toString();
	}
	
	public String getNameForElement()
	{
		return nameTextField.getText();
	}
	
	public void linkWithGraph()
	{
		
		//System.out.println("Aaah. Refreshing.");
		//(new Throwable()).printStackTrace();
		
		//no donut for ewe!
		this.removeAll();
		
		//we do need a tabbed pane!
		jtp = new JTabbedPane();
		this.add(jtp);
		
		JPanel firstTab = new JPanel();
		
		firstTab.setLayout(new BoxLayout(firstTab, BoxLayout.PAGE_AXIS));
		
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel(UIMessages.getInstance().getMessage("entity.uniquename")));
		namePanel.add(nameTextField);
		firstTab.add(namePanel);
		
		Vector roomNodes =
			this.getGraphEditingPanel().getRoomNodes(true);
		extendsComboBox = new JComboBox ( new DefaultComboBoxModel ( roomNodes ) );
		
		JPanel extendsPanel = new JPanel();
		extendsPanel.add ( new JLabel(UIMessages.getInstance().getMessage("inherit.from")) );
		extendsPanel.add( extendsComboBox );
		firstTab.add(extendsPanel);
		
		dlp = new DescriptionListPanel(5);
		firstTab.add(dlp);
		
		edp = new ExtraDescriptionsPanel(5);
		firstTab.add(edp);
		
		jtp.add("General",firstTab);
		
		JPanel secondTab = new JPanel();
		
		secondTab.setLayout(new BoxLayout(secondTab, BoxLayout.PAGE_AXIS));
		
		bcp = new BSHCodePanel("room",this);
		//System.out.println("Setting " + this.hashCode() + "'s panel to " + bcp.hashCode());
		secondTab.add(bcp);
		
		pp = new PropertiesPanel();
		secondTab.add(pp);
		
		jtp.add(UIMessages.getInstance().getMessage("tab.codeprop"),secondTab);
		
	}
	
	
	public Node doGetXML ( Document d )
	{
		
		Element result = d.createElement("Room");
		
		result.setAttribute("name",this.getName());
		
		if ( !extendsComboBox.getSelectedItem().equals(UIMessages.getInstance().getMessage("none") ))
			result.setAttribute("extends",extendsComboBox.getSelectedItem().toString());
		
		result.appendChild((Element)dlp.getXML(d));
		result.appendChild((Element)edp.getXML(d));
		result.appendChild((Element)pp.getXML(d));
		Element codeElt = ((Element)bcp.getXML(d));
		if ( codeElt != null )
			result.appendChild(codeElt);
		
		
		//check arrows & gen. path XML, inventory XML and suchlike.
		List arrows = node.getArrows();
		
		//paths:
		Element pathElt = d.createElement("PathList");
		
		//items:
		Element invElt = d.createElement("Inventory");
		
		//moblist:
		Element mobsElt = d.createElement("MobileList");
		
		//custom relationships
		//Element relationshipsElt = d.createElement("RelationshipList");
		
		for ( int i = 0 ; i < arrows.size() ; i++ )
		{
			GraphElementPanel gep = ((Arrow)arrows.get(i)).getAssociatedPanel();
			if ( gep instanceof PathPanel )
			{
				if ( ((PathPanel)gep).getRelationshipType().equals(UIMessages.getInstance().getMessage("structural.room.room")) ) //there is a path
				{
					pathElt.appendChild(((PathPanel)gep).getXML(d));
				}
			}
			else if ( gep instanceof RoomHasCharPanel )
			{
				mobsElt.appendChild(((RoomHasCharPanel)gep).getXML(d));
			}
			else if ( gep instanceof RoomHasItemPanel )
			{
				invElt.appendChild(((RoomHasItemPanel)gep).getXML(d));
			}
			/*
			 * refactored up in hierarchy
			if ( gep instanceof ArrowPanel ) //this if check will become unnecessary when custom relationship implementation is complete
			{
				ArrowPanel relPanel = (ArrowPanel) gep;
				Node n = relPanel.getCustomRelationshipXML(d);
				if ( n.hasChildNodes() ) //if it doesn't have children there are no custom relationships, would be worthless to append it
					relationshipsElt.appendChild(n);
			}
			*/
		}
		
		Element relationshipsElt = (Element) getCustomRelationshipListXML(d,node);
		
		result.appendChild(pathElt);
		result.appendChild(invElt);
		result.appendChild(mobsElt);
		
		if ( relationshipsElt != null )
		{
			result.appendChild(relationshipsElt);
		}
		
				
		return result;
		
	}
	
	
	//returns the index of the object in v whose name is s.
	private int indexOf ( ComboBoxModel v , String s )
	{
		for ( int i = 0 ; i < v.getSize() ; i++ )
		{
			if ( s.equals(v.getElementAt(i).toString()) )
				return i;
		}
		return -1;
	}
	
	
	public String getBSHCode()
	{
		if ( bcp == null ) forceRealInitFromXml(true); //code panel not yet initted.
		return bcp.getCode();
	}
	
	/*
	//begin cache stuff
	
	private boolean initted = false;
	private org.w3c.dom.Node cachedNode = null;
	
	public void forceRealInitFromXml ( )
	{
		if ( !initted && cachedNode != null )
		{
			doInitFromXML(cachedNode);
			initted = true;
			cachedNode = null;
		}
	}
	
	public void setVisible( boolean visible )
	{
		if ( visible ) forceRealInitFromXml();
		super.setVisible(visible);
	}
	
	//rename to queueInitFromXML
	public void initFromXML ( org.w3c.dom.Node n )
	{
		cachedNode = n;
	}
	
	//end cache stuff
	*/
	
	public void doInitMinimal( org.w3c.dom.Node e )
	{
		nameTextField.setText(((org.w3c.dom.Element)e).getAttribute("name"));
	}
	
	
//	from Mobile node
	public void doInitFromXML ( org.w3c.dom.Node n )
	{
		
		linkWithGraph();
		
		Element e = (Element) n;
		
		//name
		nameTextField.setText(e.getAttribute("name"));
		
		//extends, clones cb
		if ( e.hasAttribute("extends") )
		{
			int extendsInd = indexOf ( extendsComboBox.getModel() , e.getAttribute("extends") );
			if ( extendsInd >= 0 )
			{
				extendsComboBox.setSelectedIndex(extendsInd);
			}
		}
		
		//description list panel
		NodeList desNl = e.getElementsByTagName("DescriptionList");
		Element desListElt = (Element) desNl.item(0);
		dlp.initFromXML(desListElt);
		
		//extra descriptions panel
		NodeList edesNl = e.getElementsByTagName("ExtraDescriptionList");
		Element edesListElt = (Element) edesNl.item(0);
		if ( edesListElt != null )
			edp.initFromXML(edesListElt);
		
		//bsh code panel
		NodeList codeNl = e.getElementsByTagName("Code");
		if ( codeNl.getLength() > 0 )
		{
			Element codeListElt = (Element) codeNl.item(0);
			bcp.initFromXML(codeListElt);
		}
		
		//properties panel
		NodeList plNl = e.getElementsByTagName("PropertyList");
		Element plElt = (Element) plNl.item(0);
		if ( plElt != null )
			pp.initFromXML(plElt);
		
		
	}
	

	
}
