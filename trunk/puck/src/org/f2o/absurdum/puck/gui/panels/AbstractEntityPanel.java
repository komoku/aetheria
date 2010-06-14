/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 18:51:54
 * as file EntityPanel.java on package org.f2o.absurdum.puck.gui.panels
 */

//TODO Add spell-specifics

package org.f2o.absurdum.puck.gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.f2o.absurdum.puck.gui.graph.AbstractEntityNode;
import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
import org.f2o.absurdum.puck.gui.graph.SpellNode;
import org.f2o.absurdum.puck.i18n.Messages;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextField;
import org.f2o.absurdum.puck.util.xml.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 18:51:54
 */
public class AbstractEntityPanel extends EntityPanel 
{

	
	private EnhancedJTextField typeTextField = new EnhancedJTextField(20);
	
	private JComboBox extendsComboBox = new JComboBox();
	private JComboBox clonesComboBox = new JComboBox();
	

	/*
	private JCheckBox containerBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.container"));
	*/
	
	//privatize
	//public DescriptionListPanel dlp;
	//private ExtraDescriptionsPanel edp;
	private BSHCodePanel bcp;
	private PropertiesPanel pp;
	
	/*
	private DescriptionListPanel snp; //sing names
	private DescriptionListPanel pnp; //plur names
	*/
	
	/*
	private PathCommandsPanel srn; //sing refnames
	private PathCommandsPanel prn; //sing refnames
	*/
	
	//openable-closable support
	/*
	private DescriptionListPanel odp;
	private DescriptionListPanel cdp;
	private DescriptionListPanel ldp;
	private DescriptionListPanel udp;
	private JCheckBox oBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.openable"));
	private JCheckBox cBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.closeable"));
	private JCheckBox lBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.lockable"));
	private JCheckBox uBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.unlockable"));
	*/
	
	//the tabbed pane
	private JTabbedPane jtp;
	
	private AbstractEntityNode node;
	
	
	public AbstractEntityPanel( AbstractEntityNode node )
	{
		super();
		nameTextField.setText("Abstract Entity #"+getID());
		this.node = node;
		//add ( extendsComboBox );
	}
	
	public String toString()
	{
		return nameTextField.getText() + "##" + super.toString();
	}
	
	public String getName()
	{
		return nameTextField.getText();
	}
	
	public void linkWithGraph()
	{
		
		//no donut for ewe!
		this.removeAll();
		
		//we do need a tabbed pane!
		jtp = new JTabbedPane();
		this.add(jtp);
		

		
		JPanel firstTab = new JPanel();
		
		firstTab.setLayout(new BoxLayout(firstTab, BoxLayout.PAGE_AXIS));
		
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel(Messages.getInstance().getMessage("entity.uniquename")));
		namePanel.add(nameTextField);
		firstTab.add(namePanel);
		
		JPanel typePanel = new JPanel();
		typePanel.add(new JLabel(Messages.getInstance().getMessage("entity.type")));
		typePanel.add(typeTextField);
		firstTab.add(typePanel);
		
		//TODO: Maybe add effect nodes here
		/*
		Vector itemNodes =
			this.getGraphEditingPanel().getItemNodes();
		extendsComboBox = new JComboBox ( new DefaultComboBoxModel ( itemNodes ) );
		clonesComboBox = new JComboBox ( new DefaultComboBoxModel ( itemNodes ) );
		*/
		
		Vector abstractEntityNodes = this.getGraphEditingPanel().getNodes(AbstractEntityNode.class,true);
		extendsComboBox = new JComboBox ( new DefaultComboBoxModel ( abstractEntityNodes ) );
		clonesComboBox = new JComboBox ( new DefaultComboBoxModel ( abstractEntityNodes ) );
		
		JPanel extendsPanel = new JPanel();
		extendsPanel.add ( new JLabel(Messages.getInstance().getMessage("inherit.from")) );
		extendsPanel.add ( extendsComboBox );
		firstTab.add(extendsPanel);
		JPanel clonesPanel = new JPanel();
		clonesPanel.add ( new JLabel(Messages.getInstance().getMessage("clone.from")) );
		clonesPanel.add ( clonesComboBox );
		firstTab.add(clonesPanel);
		
		/*
		JPanel genderPanel = new JPanel();
		genderComboBox = new JComboBox ( new String[] { Messages.getInstance().getMessage("gender.m") , Messages.getInstance().getMessage("gender.f") } );
		genderPanel.add(new JLabel(Messages.getInstance().getMessage("gender")));
		genderPanel.add(genderComboBox);
		firstTab.add(genderPanel);
		*/
		
		/*
		JPanel weightVolPanel = new JPanel();
		weightVolPanel.add(new JLabel(Messages.getInstance().getMessage("item.weight")));
		weightVolPanel.add(tfWeight);
		weightVolPanel.add(new JLabel(Messages.getInstance().getMessage("item.volume")));
		weightVolPanel.add(tfVolume);
		firstTab.add(weightVolPanel);
		*/
		
		/*
		JPanel contPanel = new JPanel();
		contPanel.add(containerBox);
		firstTab.add(contPanel);
		
		dlp = new DescriptionListPanel();
		firstTab.add(dlp);
		
		edp = new ExtraDescriptionsPanel();
		firstTab.add(edp);
		 */
		
		jtp.add("General",firstTab);
		
		
		
		//JPanel thirdTab = new JPanel();
		
		/*
		thirdTab.setLayout(new BoxLayout(thirdTab, BoxLayout.PAGE_AXIS));
		
		snp = new DescriptionListPanel(Messages.getInstance().getMessage("label.singnames"),Messages.getInstance().getMessage("label.name"));
		thirdTab.add(snp);
		
		pnp = new DescriptionListPanel(Messages.getInstance().getMessage("label.plurnames"),Messages.getInstance().getMessage("label.name"));
		thirdTab.add(pnp);
		*/
		
		/*
		srn = new PathCommandsPanel(Messages.getInstance().getMessage("label.singrefnames"),Messages.getInstance().getMessage("label.name"),true);
		thirdTab.add(srn);
		
		prn = new PathCommandsPanel(Messages.getInstance().getMessage("label.plurrefnames"),Messages.getInstance().getMessage("label.name"),true);
		thirdTab.add(prn);
		*/
		
		//jtp.add(Messages.getInstance().getMessage("tab.names"),thirdTab);
		
		JPanel secondTab = new JPanel();
		
		secondTab.setLayout(new BoxLayout(secondTab, BoxLayout.PAGE_AXIS));
		
		bcp = new BSHCodePanel("abstract entity",this);
		secondTab.add(bcp);
		
		pp = new PropertiesPanel();
		secondTab.add(pp);
		
		jtp.add(Messages.getInstance().getMessage("tab.codeprop"),secondTab);
		
		
		//openable-closeable support
		
		/*
		JPanel fourthTab = new JPanel();
		
		//JScrollPane fourthTabScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel fourthTabReal = new JPanel();
		
		fourthTab.setLayout(new BorderLayout());
		fourthTabReal.setLayout(new BoxLayout(fourthTabReal, BoxLayout.PAGE_AXIS));
		
		JPanel openableFlagsPanel = new JPanel();
		openableFlagsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("flags.openable")));
		
		
		
		openableFlagsPanel.setLayout(new BoxLayout(openableFlagsPanel, BoxLayout.PAGE_AXIS));
		
		openableFlagsPanel.add(oBox);
		openableFlagsPanel.add(cBox);
		openableFlagsPanel.add(lBox);
		openableFlagsPanel.add(uBox);
		
		fourthTabReal.add(openableFlagsPanel);
				
		odp = new DescriptionListPanel(Messages.getInstance().getMessage("label.opendes"),Messages.getInstance().getMessage("label.description"),true);
		cdp = new DescriptionListPanel(Messages.getInstance().getMessage("label.closedes"),Messages.getInstance().getMessage("label.description"),true);
		ldp = new DescriptionListPanel(Messages.getInstance().getMessage("label.lockdes"),Messages.getInstance().getMessage("label.description"),true);
		udp = new DescriptionListPanel(Messages.getInstance().getMessage("label.unlockdes"),Messages.getInstance().getMessage("label.description"),true);
		
		JScrollPane innerScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout( new BoxLayout(innerPanel, BoxLayout.PAGE_AXIS) );
		
		innerPanel.add(odp);
		innerPanel.add(cdp);
		innerPanel.add(ldp);
		innerPanel.add(udp);
		
		innerScroll.setViewportView(innerPanel);
		innerScroll.setPreferredSize(new Dimension(0,400));
		
		fourthTabReal.add(innerScroll);
		

		
		//fourthTabScroll.setViewportView(fourthTabReal);
		
		//fourthTab.add(fourthTabScroll,BorderLayout.CENTER);
		
		//jtp.add(Messages.getInstance().getMessage("tab.openclose"),fourthTab);
		jtp.add(Messages.getInstance().getMessage("tab.openclose"),fourthTabReal);
		 */
		
	}
	

	
	
	public Node doGetXML ( Document d )
	{
		
		Element result = d.createElement("AbstractEntity");
		
		result.setAttribute("name",this.getName());
		if ( typeTextField.getText().length() > 0 )
			result.setAttribute("type",typeTextField.getText());
		
		/*
		result.setAttribute("weight",tfWeight.getText());
		result.setAttribute("volume",tfWeight.getText());
		
		String genderString = (String) genderComboBox.getSelectedItem();
		if (genderString.equals(Messages.getInstance().getMessage("gender.m")))
			result.setAttribute("gender","1");
		else
			result.setAttribute("gender","0");
		*/
		
		if ( !extendsComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("none") ))
			result.setAttribute("extends",extendsComboBox.getSelectedItem().toString());
		if ( !clonesComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("none") ))
			result.setAttribute("clones",clonesComboBox.getSelectedItem().toString());
		
		/*
		result.setAttribute("openable",String.valueOf(oBox.isSelected()));
		result.setAttribute("closeable",String.valueOf(cBox.isSelected()));
		result.setAttribute("lockable",String.valueOf(lBox.isSelected()));
		result.setAttribute("unlockable",String.valueOf(uBox.isSelected()));
		*/
		
		//names
		/*
		result.appendChild(snp.getXML(d,"SingularNames"));
		result.appendChild(pnp.getXML(d,"PluralNames"));
		*/
		/*
		result.appendChild(srn.getXMLForNames(d,"SingularReferenceNames"));
		result.appendChild(prn.getXMLForNames(d,"PluralReferenceNames"));
		*/
		/*
		result.appendChild((Element)dlp.getXML(d));		
		result.appendChild((Element)edp.getXML(d));
		*/
		
		//open-close-lock-unlock descriptions
		/*
		Element openDes = (Element)odp.getXML(d,"OpenDescriptionList");
		if ( openDes.hasChildNodes() || oBox.isSelected() )
			result.appendChild(openDes);
		Element closeDes = (Element)cdp.getXML(d,"CloseDescriptionList");
		if ( closeDes.hasChildNodes() || cBox.isSelected() )
			result.appendChild(closeDes);
		Element lockDes = (Element)ldp.getXML(d,"LockDescriptionList");
		if ( lockDes.hasChildNodes() || lBox.isSelected() )
			result.appendChild(lockDes);
		Element unlockDes = (Element)udp.getXML(d,"UnlockDescriptionList");
		if ( unlockDes.hasChildNodes() || uBox.isSelected() )
			result.appendChild(unlockDes);	
		*/
		
		//properties, code
		result.appendChild((Element)pp.getXML(d));
		Element codeElt = ((Element)bcp.getXML(d));
		if ( codeElt != null )
			result.appendChild(codeElt);
		
		List arrows = node.getArrows();
		
		/*
		Element partsElt = null;
		Element keysElt = null;
		*/
				
		//for ( int i = 0 ; i < arrows.size() ; i++ )
		//{
			
		//	GraphElementPanel gep = ((Arrow)arrows.get(i)).getAssociatedPanel();
			
			/*
			if ( gep instanceof ItemHasItemPanel )
			{
				ItemHasItemPanel relPanel = (ItemHasItemPanel) gep;
				String relType = relPanel.getRelationshipType();
				if ( relType.equals(Messages.getInstance().getMessage("structural.item.item.contain")) ) //containment relationship
				{
					if ( invElt == null ) invElt = d.createElement("Inventory");
					invElt.appendChild( relPanel.getXML(d) );
				}
				if ( relType.equals(Messages.getInstance().getMessage("structural.item.item.haspart")) ) //has-part relationship
				{
					if ( partsElt == null ) partsElt = d.createElement("Inventory");
					partsElt.appendChild( relPanel.getXML(d) );
				}
				if ( relType.equals(Messages.getInstance().getMessage("structural.item.item.haskey")) ) //has-key relationship
				{
					if ( keysElt == null ) keysElt = d.createElement("Inventory");
					keysElt.appendChild( relPanel.getXML(d) );
				}
			}
			*/
			
		//}
		
		Element relationshipsElt = (Element) getCustomRelationshipListXML ( d , node );
		

		if ( relationshipsElt != null )
		{
			result.appendChild(relationshipsElt);
		}
		
		/*
		if ( partsElt != null )
		{
			Element tempElt = d.createElement("Parts");
			tempElt.appendChild(partsElt);
			result.appendChild(tempElt);
		}
		if ( keysElt != null )
		{
			Element tempElt = d.createElement("KeyList");
			tempElt.appendChild(keysElt);
			result.appendChild(tempElt);
		}
		*/
				
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
		
		//type
		typeTextField.setText(e.getAttribute("type"));
		
		//extends, clones cb
		if ( e.hasAttribute("extends") )
		{
			int extendsInd = indexOf ( extendsComboBox.getModel() , e.getAttribute("extends") );
			if ( extendsInd >= 0 )
			{
				extendsComboBox.setSelectedIndex(extendsInd);
			}
		}
		if ( e.hasAttribute("clones") )
		{
			int clonesInd = indexOf ( clonesComboBox.getModel() , e.getAttribute("clones") );
			if ( clonesInd >= 0 )
			{
				clonesComboBox.setSelectedIndex(clonesInd);
			}
		}
		
		//openable, closeable, etc. flags
		/*
		if ( e.hasAttribute("openable") ) oBox.setSelected( Boolean.parseBoolean( e.getAttribute("openable") ) );
		if ( e.hasAttribute("closeable") ) cBox.setSelected( Boolean.parseBoolean( e.getAttribute("closeable") ) );
		if ( e.hasAttribute("lockable") ) lBox.setSelected( Boolean.parseBoolean( e.getAttribute("lockable") ) );
		if ( e.hasAttribute("unlockable") ) uBox.setSelected( Boolean.parseBoolean( e.getAttribute("unlockable") ) );
		*/
		
		/*
		//description list panel
		NodeList desNl = e.getElementsByTagName("DescriptionList");
		Element desListElt = (Element) desNl.item(0);
		dlp.initFromXML(desListElt);
		
		//extra descriptions panel
		NodeList edesNl = e.getElementsByTagName("ExtraDescriptionList");
		Element edesListElt = (Element) edesNl.item(0);
		edp.initFromXML(edesListElt);
		*/
		
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
		pp.initFromXML(plElt);
		
		//sing names panel
		/*
		desNl = e.getElementsByTagName("SingularNames");
		desListElt = (Element) desNl.item(0);
		snp.initFromXML(desListElt);
		
		//plur names panel
		desNl = e.getElementsByTagName("PluralNames");
		desListElt = (Element) desNl.item(0);
		pnp.initFromXML(desListElt);
		*/
		
		//sing ref names panel
		/*
		NodeList srnNl = e.getElementsByTagName("SingularReferenceNames");
		Element srnListElt = (Element) srnNl.item(0);
		srn.initFromXML(srnListElt);
		
		//plural ref names panel
		NodeList prnNl = e.getElementsByTagName("PluralReferenceNames");
		Element prnListElt = (Element) prnNl.item(0);
		prn.initFromXML(prnListElt);
		*/
		
		//open descriptions panel
		/*
		NodeList desOp = e.getElementsByTagName("OpenDescriptionList");
		if ( desOp.getLength() > 0 )
		{
			Element openDesListElt = (Element) desOp.item(0);
			odp.initFromXML(openDesListElt);
		}
		
		//close descriptions panel
		NodeList desCl = e.getElementsByTagName("CloseDescriptionList");
		if ( desCl.getLength() > 0 )
		{
			Element closeDesListElt = (Element) desCl.item(0);
			cdp.initFromXML(closeDesListElt);
		}
		
		//lock descriptions panel
		NodeList desLck = e.getElementsByTagName("LockDescriptionList");
		if ( desLck.getLength() > 0 )
		{
			Element lockDesListElt = (Element) desLck.item(0);
			ldp.initFromXML(lockDesListElt);
		}
		
		//unlock descriptions panel
		NodeList desUnl = e.getElementsByTagName("UnlockDescriptionList");
		if ( desUnl.getLength() > 0 )
		{
			Element unlockDesListElt = (Element) desUnl.item(0);
			udp.initFromXML(unlockDesListElt);
		}
		*/
		
		//if it has inventory, then it is a container. Inventory is not initted here.
		//All inventories are initted at WorldPanel.
		/*
		NodeList invs = DOMUtils.getDirectChildrenElementsByTagName(e,"Inventory");
		if ( invs.getLength() > 0 )
			containerBox.setSelected(true);
		
		//weight, volume textfields
		tfWeight.setText(e.getAttribute("weight"));
		tfVolume.setText(e.getAttribute("volume"));
		
		//gender combo box
		if ( e.getAttribute("gender").equals("0") )
			genderComboBox.setSelectedIndex(1);
		else
			genderComboBox.setSelectedIndex(0);
			*/
		
	}
	
	
}

