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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.gui.graph.StructuralArrow;
import org.f2o.absurdum.puck.i18n.Messages;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 18:51:54
 */
public class PathPanel extends ArrowPanel 
{

	//private JTextField nameTextField = new EnhancedJTextField(20);
	
	//private JComboBox extendsComboBox = new JComboBox();
	
		
	/*
	private JComboBox srcComboBox = new JComboBox();
	private JComboBox dstComboBox = new JComboBox();
	*/
	
	private JComboBox dirComboBox;
	
	private JComboBox doorComboBox = new JComboBox();
	
	private JTextField lengthTextField = new EnhancedJTextField("0",5);
	
	//private Arrow pathArrow;
	
	/*
	private Vector roomNodes;
	private Vector itemNodes; //used for paths with doors
	*/
	
	private ItemNode door;
	
	private DescriptionListPanel dlp;
	private PathCommandsPanel pcp;
	private PropertiesPanel pp;
	
	public void setDoor ( ItemNode door )
	{
		this.door = door;
	}
	
	public ItemNode getDoor ( )
	{
		return door;
	}
	
	public String getDirectionString()
	{
		return (String) dirComboBox.getSelectedItem();
	}
	
	public Vector getPossibleSourceNodes()
	{
		return this.getGraphEditingPanel().getRoomNodes(false);
	}
	
	public Vector getPossibleDestinationNodes()
	{
		return this.getGraphEditingPanel().getRoomNodes(false);
	}
	
	public String getNameForElement()
	{
		String result;
		if ( !getDirectionString().equals(Messages.getInstance().getMessage("dir.none")) )
			result = getDirectionString();
		else result = "";
		
		if ( pcp != null ) //will be null if panel not fully initted (just minimally initted)
		{
			DefaultListModel dlm = pcp.getListModel();
			if ( dlm.size() > 0 )
			{
				result += " (" + dlm.get(0);
				if ( dlm.size() > 1 )
				{
					result += ", ...)";
				}
				else result += ")";
			}
		}
	
		return result;
	}
	
	public PathPanel( Arrow pathArrow )
	{
		
		super(pathArrow);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
//		possible structural relationship types
		relTypes.clear();
		relTypes.add(Messages.getInstance().getMessage("structural.room.room"));
		relTypes.add(ArrowPanel.NO_STRUCTURAL_RELATIONSHIP);
		
		//default structural relationship type
		this.relationshipType = Messages.getInstance().getMessage("structural.room.room"); //default relationship is this one (path)
		
		add(new JLabel("Path"));
	}
	
	public String toString()
	{
		return "Yet Another Path Panel";
	}
	
	public String getName()
	{
		return "Yet Another Path Panel";
	}
	
	
	//initialise the minimum to be able to access name, etc. in the graph display
	public void doInitMinimal()
	{
		
		super.doInitMinimal();
		
		//std. direction
		Vector directions = new Vector();
		directions.add(Messages.getInstance().getMessage("dir.none"));
		directions.add(Messages.getInstance().getMessage("dir.n"));
		directions.add(Messages.getInstance().getMessage("dir.s"));
		directions.add(Messages.getInstance().getMessage("dir.e"));
		directions.add(Messages.getInstance().getMessage("dir.w"));
		directions.add(Messages.getInstance().getMessage("dir.u"));
		directions.add(Messages.getInstance().getMessage("dir.d"));
		directions.add(Messages.getInstance().getMessage("dir.nw"));
		directions.add(Messages.getInstance().getMessage("dir.sw"));
		directions.add(Messages.getInstance().getMessage("dir.ne"));
		directions.add(Messages.getInstance().getMessage("dir.se"));
		
		dirComboBox = new JComboBox ( new DefaultComboBoxModel ( directions ) );
		dirComboBox.setSelectedItem( ((StructuralArrow)theArrow).getMostLikelyDirection() );
		
	}
	
	
	public void linkWithGraph()
	{
		
		super.linkWithGraph(); //this does a removeAll().
		
		/*
		roomNodes =
			this.getGraphEditingPanel().getRoomNodes();
		srcComboBox = new JComboBox ( new DefaultComboBoxModel ( roomNodes ) );
		dstComboBox = new JComboBox ( new DefaultComboBoxModel ( roomNodes ) );
				
		srcComboBox.setSelectedIndex( indexOf(roomNodes,pathArrow.getSource().getName()) );
		dstComboBox.setSelectedIndex( indexOf(roomNodes,pathArrow.getDestination().getName()) );
		*/
		
		Vector itemNodes =
			this.getGraphEditingPanel().getItemNodes(true);
		doorComboBox = new JComboBox ( new DefaultComboBoxModel ( itemNodes ) );
		if ( door != null )
			doorComboBox.setSelectedIndex( indexOf(itemNodes,door.getName()) );
		else
			doorComboBox.setSelectedIndex(0);
		
		//listener just to update the door attribute when door combo box selection
		//changes, so that this change is immediately reflected on the graph
		doorComboBox.addActionListener( new ActionListener() {
			public void actionPerformed ( ActionEvent evt )
			{
				door = (ItemNode) doorComboBox.getSelectedItem();
			}
		});
		
		JPanel mainTab = new JPanel();
		mainTab.setLayout(new BoxLayout(mainTab,BoxLayout.PAGE_AXIS));
		
		JPanel srcPanel = new JPanel();
		srcPanel.add ( new JLabel(Messages.getInstance().getMessage("path.src")) );
		srcPanel.add ( srcComboBox );
		mainTab.add(srcPanel);
		
		JPanel dstPanel = new JPanel();
		dstPanel.add ( new JLabel(Messages.getInstance().getMessage("path.dst")) );
		dstPanel.add ( dstComboBox );
		mainTab.add(dstPanel);
		
		JPanel enablePathPanel = new JPanel();
		final JCheckBox enablePathCb = new JCheckBox(Messages.getInstance().getMessage("path.enable"));
		enablePathPanel.add(enablePathCb);
		mainTab.add(enablePathPanel);
		if ( relationshipType.equals ( ArrowPanel.NO_STRUCTURAL_RELATIONSHIP ) )
			enablePathCb.setSelected(false);
		else
			enablePathCb.setSelected(true);
			
		
		enablePathCb.addActionListener ( new ActionListener() {
			public void actionPerformed ( ActionEvent evt )
			{
				if ( enablePathCb.isSelected() )
					relationshipType = Messages.getInstance().getMessage("structural.room.room");
				else
					relationshipType = ArrowPanel.NO_STRUCTURAL_RELATIONSHIP;
			}
		});
		
		//setVisible(true);
		
		
		//initMinimal(); //direction //already done, not needed
		
		JPanel dirPanel = new JPanel();
		dirPanel.add ( new JLabel(Messages.getInstance().getMessage("path.dir")) );
		dirPanel.add ( dirComboBox );
		mainTab.add(dirPanel);
		
		JPanel doorPanel = new JPanel();
		doorPanel.add( new JLabel(Messages.getInstance().getMessage("path.door")) );
		doorPanel.add( doorComboBox );
		mainTab.add (doorPanel);
		
		JPanel lengthPanel = new JPanel();
		lengthPanel.add( new JLabel(Messages.getInstance().getMessage("path.length")) );
		lengthPanel.add( lengthTextField );
		mainTab.add ( lengthPanel );
		
		//commands
		pcp = new PathCommandsPanel();
		mainTab.add(pcp);
		
		//descriptions
		dlp = new DescriptionListPanel();
		mainTab.add(dlp);
		
		//properties
		pp = new PropertiesPanel();
		mainTab.add(pp);
		
		//setVisible(true);
		
		jtp.add(mainTab,Messages.getInstance().getMessage("tab.structrel"),0);
		jtp.setSelectedIndex(0);	
		
		
	}
	
	
	public org.w3c.dom.Node doGetXML ( Document d )
	{
		
		Element result = d.createElement("Path");
		
		result.setAttribute("destination",dstComboBox.getSelectedItem().toString());
		
		if ( !dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("none")) )
		{
			result.setAttribute("standard","true");
			
			String directionString = "???";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.n")) )
				directionString="norte";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.s")) )
				directionString="sur";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.e")) )
				directionString="este";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.w")) )
				directionString="oeste";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.n")) )
				directionString="norte";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.nw")) )
				directionString="noroeste";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.sw")) )
				directionString="suroeste";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.ne")) )
				directionString="nordeste";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.se")) )
				directionString="sudeste";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.u")) )
				directionString="arriba";
			if ( dirComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("dir.d")) )
				directionString="abajo";
			
			result.setAttribute("direction",directionString);
		}
		
		//path length (exit time)
		if ( lengthTextField.getText().length() > 0 )
			result.setAttribute("exitTime",lengthTextField.getText());
			
		//door (associated item)
		if ( doorComboBox.getSelectedIndex() > 0 ) //0 = "nada"
		{
			this.door = (ItemNode) doorComboBox.getSelectedItem();
			String doorStr = this.door.toString();
			Element e = d.createElement("AssociatedItem");
			e.setAttribute("id",doorStr);
			result.appendChild(e);
		}
			
		result.appendChild(pp.getXML(d));
		result.appendChild(dlp.getXML(d));
		result.appendChild(pcp.getXML(d));
		
		return result;
		
	}
	
	
	//from Path node
	public void doInitFromXML ( org.w3c.dom.Node n )
	{
		
		linkWithGraph(); refresh(); //this should already fix the combo box values alright
		//if the arrow was created correctly between the corresponding nodes
		
		Element e = (Element) n;
		
		if ( e.getTagName().equals("Relationship") ) return; //we're not initting the path 
		
		//direction combo box init
		if ( e.getAttribute("standard").equals("true") )
		{
			String dirAttr = e.getAttribute("direction");
			if ( "norte".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.n"));
			else if ( "sur".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.s"));
			else if ( "este".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.e"));
			else if ( "oeste".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.w"));
			else if ( "noroeste".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.nw"));
			else if ( "suroeste".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.sw"));
			else if ( "nordeste".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.ne"));
			else if ( "sudeste".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.se"));
			else if ( "arriba".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.u"));
			else if ( "abajo".equals(dirAttr) )
				dirComboBox.setSelectedItem(Messages.getInstance().getMessage("dir.d"));
		}
		else
		{
			dirComboBox.setSelectedItem(Messages.getInstance().getMessage("none"));
		}
				
		//description list panel init
		NodeList desNl = e.getElementsByTagName("DescriptionList");
		Element desListElt = (Element) desNl.item(0);
		dlp.initFromXML(desListElt);
		
		//path commands panel init
		pcp.initFromXML(e);
		
		//properties panel init
		NodeList plNl = e.getElementsByTagName("PropertyList");
		Element plElt = (Element) plNl.item(0);
		if ( plElt != null )
			pp.initFromXML(plElt);
		
		//length text field
		lengthTextField.setText(e.getAttribute("exitTime"));
		
		//set default length if not present
		if ( lengthTextField.getText().trim().length() <= 0 )
			lengthTextField.setText("0");
		
	}
	
	
}
