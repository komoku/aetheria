/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 18:51:54
 * as file EntityPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
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

import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
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
public class ItemPanel extends EntityPanel 
{

	
	private JComboBox extendsComboBox = new JComboBox();
	private JComboBox clonesComboBox = new JComboBox();
	
	private JComboBox genderComboBox = new JComboBox();
	
	private JTextField tfWeight = new EnhancedJTextField("0",5);
	private JTextField tfVolume = new EnhancedJTextField("0",5);
	
	private JCheckBox containerBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.container"));
	
	private JCheckBox ungettableBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.ungettable"));
	
	//privatize
	public DescriptionListPanel dlp;
	private ExtraDescriptionsPanel edp;
	private BSHCodePanel bcp;
	private PropertiesPanel pp;
	
	private DescriptionListPanel snp; //sing names
	private DescriptionListPanel pnp; //plur names
	
	private PathCommandsPanel srn; //sing refnames
	private PathCommandsPanel prn; //sing refnames
	
	
	//openable-closable support
	private DescriptionListPanel odp;
	private DescriptionListPanel cdp;
	private DescriptionListPanel ldp;
	private DescriptionListPanel udp;
	private JCheckBox oBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.openable"));
	private JCheckBox cBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.closeable"));
	private JCheckBox lBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.lockable"));
	private JCheckBox uBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.unlockable"));
	
	//wearable support
	private JCheckBox wBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.wearable"));
	private List wearableLimbPanels = new ArrayList();
	
	private DamageListPanel damlp;
	
	
	//weapon support
	private JCheckBox weaponBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.weapon"));
	private List wieldableLimbPanels = new ArrayList();
	
	private AttDefSpecsPanel attackPanel;
	private AttDefSpecsPanel defensePanel;
	
	
	
	//the tabbed pane
	private JTabbedPane jtp;
	
	private ItemNode node;
	
	private JPanel innerPanelFifth;
	private JPanel fifthTab;
	private JButton buttonLess;
	
	private JPanel innerPanelSixth;
	private JPanel sixthTab;
	private JButton buttonLess2;
	
	
	public ItemPanel( ItemNode node )
	{
		super();
		nameTextField.setText("Item #"+getID());
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
	
	/*
	public void linkWithGraph()
	{
		;
	}
	*/
	
	//adds combo boxes with item nodes, etc.
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
		
		Vector itemNodes =
			this.getGraphEditingPanel().getItemNodes(true);
		extendsComboBox = new JComboBox ( new DefaultComboBoxModel ( itemNodes ) );
		clonesComboBox = new JComboBox ( new DefaultComboBoxModel ( itemNodes ) );
		
		JPanel extendsPanel = new JPanel();
		extendsPanel.add ( new JLabel(Messages.getInstance().getMessage("inherit.from")) );
		extendsPanel.add ( extendsComboBox );
		firstTab.add(extendsPanel);
		JPanel clonesPanel = new JPanel();
		clonesPanel.add ( new JLabel(Messages.getInstance().getMessage("clone.from")) );
		clonesPanel.add ( clonesComboBox );
		firstTab.add(clonesPanel);
		
		JPanel genderPanel = new JPanel();
		genderComboBox = new JComboBox ( new String[] { Messages.getInstance().getMessage("gender.m") , Messages.getInstance().getMessage("gender.f") } );
		genderPanel.add(new JLabel(Messages.getInstance().getMessage("gender")));
		genderPanel.add(genderComboBox);
		firstTab.add(genderPanel);
		
		JPanel weightVolPanel = new JPanel();
		weightVolPanel.add(new JLabel(Messages.getInstance().getMessage("item.weight")));
		weightVolPanel.add(tfWeight);
		weightVolPanel.add(new JLabel(Messages.getInstance().getMessage("item.volume")));
		weightVolPanel.add(tfVolume);
		firstTab.add(weightVolPanel);
		
		JPanel flagsPanel = new JPanel();
		flagsPanel.add(containerBox);
		flagsPanel.add(ungettableBox);
		firstTab.add(flagsPanel);
		
		dlp = new DescriptionListPanel(5);
		firstTab.add(dlp);
		
		edp = new ExtraDescriptionsPanel(5);
		firstTab.add(edp);

		jtp.add("General",firstTab);
		
		
		JPanel thirdTab = new JPanel();
		
		thirdTab.setLayout(new BoxLayout(thirdTab, BoxLayout.PAGE_AXIS));
		
		snp = new DescriptionListPanel(Messages.getInstance().getMessage("label.singnames"),Messages.getInstance().getMessage("label.name"),false,true,1);
		thirdTab.add(snp);
		
		pnp = new DescriptionListPanel(Messages.getInstance().getMessage("label.plurnames"),Messages.getInstance().getMessage("label.name"),1);
		thirdTab.add(pnp);
		
		srn = new PathCommandsPanel(Messages.getInstance().getMessage("label.singrefnames"),Messages.getInstance().getMessage("label.name"),true);
		thirdTab.add(srn);
		
		prn = new PathCommandsPanel(Messages.getInstance().getMessage("label.plurrefnames"),Messages.getInstance().getMessage("label.name"),true);
		thirdTab.add(prn);
		
		jtp.add(Messages.getInstance().getMessage("tab.names"),thirdTab);
		
		
		JPanel secondTab = new JPanel();
		
		secondTab.setLayout(new BoxLayout(secondTab, BoxLayout.PAGE_AXIS));
		
		bcp = new BSHCodePanel("item",this);
		secondTab.add(bcp);
		
		pp = new PropertiesPanel();
		secondTab.add(pp);
		
		jtp.add(Messages.getInstance().getMessage("tab.codeprop"),secondTab);
		
		
		//openable-closeable support
		
		
		//JScrollPane fourthTabScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel fourthTabReal = new JPanel();
		
		fourthTabReal.setLayout(new BoxLayout(fourthTabReal, BoxLayout.PAGE_AXIS));
		
		JPanel openableFlagsPanel = new JPanel();
		openableFlagsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("flags.openable")));
		
		
		
		openableFlagsPanel.setLayout(new BoxLayout(openableFlagsPanel, BoxLayout.PAGE_AXIS));
		
		openableFlagsPanel.add(oBox);
		openableFlagsPanel.add(cBox);
		openableFlagsPanel.add(lBox);
		openableFlagsPanel.add(uBox);
		
		fourthTabReal.add(openableFlagsPanel);
				
		odp = new DescriptionListPanel(Messages.getInstance().getMessage("label.opendes"),Messages.getInstance().getMessage("label.description"),true,2);
		cdp = new DescriptionListPanel(Messages.getInstance().getMessage("label.closedes"),Messages.getInstance().getMessage("label.description"),true,2);
		ldp = new DescriptionListPanel(Messages.getInstance().getMessage("label.lockdes"),Messages.getInstance().getMessage("label.description"),true,2);
		udp = new DescriptionListPanel(Messages.getInstance().getMessage("label.unlockdes"),Messages.getInstance().getMessage("label.description"),true,2);
		
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
		
		/*
		fourthTabReal.add(odp);
		fourthTabReal.add(cdp);
		fourthTabReal.add(ldp);
		fourthTabReal.add(udp);
		*/
		
		//fourthTabScroll.setViewportView(fourthTabReal);
		
		//fourthTab.add(fourthTabScroll,BorderLayout.CENTER);
		
		//jtp.add(Messages.getInstance().getMessage("tab.openclose"),fourthTab);
		jtp.add(Messages.getInstance().getMessage("tab.openclose"),fourthTabReal);
		
		
		
		fifthTab = new JPanel();
		fifthTab.setLayout(new BoxLayout(fifthTab, BoxLayout.PAGE_AXIS));
		
		fifthTab.add(wBox);
		
		JScrollPane innerScrollFifth = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		innerPanelFifth = new JPanel();
		innerPanelFifth.setLayout( new BoxLayout(innerPanelFifth, BoxLayout.PAGE_AXIS) );
		
		innerScrollFifth.setViewportView(innerPanelFifth);
		innerScrollFifth.setPreferredSize(new Dimension(0,400));
		
		final JPanel notSoInnerPanelFifth = new JPanel();
		notSoInnerPanelFifth.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("tab.wearable.requirements")));
		notSoInnerPanelFifth.setLayout(new BorderLayout());
		
		notSoInnerPanelFifth.add(innerScrollFifth,BorderLayout.CENTER);
		fifthTab.add(notSoInnerPanelFifth);	
		
		wearableLimbPanels = new ArrayList();
		for ( int i = 0 ; i < wearableLimbPanels.size() ; i++ )
		{
			DescriptionListPanel dlp = (DescriptionListPanel) wearableLimbPanels.get(i);
			fifthTab.add(dlp);
		}
		
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout( new BoxLayout(buttonsPanel , BoxLayout.LINE_AXIS) );
		buttonsPanel.add(Box.createHorizontalGlue());
		final JButton buttonMore = new JButton(Messages.getInstance().getMessage("tab.wearable.more"));
		buttonLess = new JButton(Messages.getInstance().getMessage("tab.wearable.less"));
		buttonLess.setEnabled(false);
		buttonsPanel.add(buttonMore);
		buttonsPanel.add(buttonLess);
		
		buttonMore.addActionListener ( new ActionListener() {
			public void actionPerformed ( ActionEvent evt )
			{
				PathCommandsPanel newPanel = new PathCommandsPanel(Messages.getInstance().getMessage("label.wearrequirement"),Messages.getInstance().getMessage("label.name"),true);
				addLimbRequirementPanel(newPanel);
			}
		});
		
		buttonLess.addActionListener ( new ActionListener() 
		{
			public void actionPerformed ( ActionEvent evt )
			{
				removeLimbRequirementPanel();
			}
		});
		
		damlp = new DamageListPanel(Messages.getInstance().getMessage("tab.wearable.damage"));
	
		notSoInnerPanelFifth.add(buttonsPanel,BorderLayout.SOUTH);
		
		fifthTab.add(damlp);

		jtp.add(Messages.getInstance().getMessage("tab.wearable"),fifthTab);
		
		
		
		
		
		
		sixthTab = new JPanel();
		sixthTab.setLayout(new BoxLayout(sixthTab, BoxLayout.PAGE_AXIS));
		
		sixthTab.add(weaponBox);
		
		JScrollPane innerScrollSixth = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		innerPanelSixth = new JPanel();
		innerPanelSixth.setLayout( new BoxLayout(innerPanelSixth, BoxLayout.PAGE_AXIS) );
		
		innerScrollSixth.setViewportView(innerPanelSixth);
		innerScrollSixth.setPreferredSize(new Dimension(0,150));
		
		final JPanel notSoInnerPanelSixth = new JPanel();
		notSoInnerPanelSixth.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("tab.weapon.requirements")));
		notSoInnerPanelSixth.setLayout(new BorderLayout());
		
		notSoInnerPanelSixth.add(innerScrollSixth,BorderLayout.CENTER);
		sixthTab.add(notSoInnerPanelSixth);	
		
		wieldableLimbPanels = new ArrayList();
		for ( int i = 0 ; i < wieldableLimbPanels.size() ; i++ )
		{
			DescriptionListPanel dlp = (DescriptionListPanel) wieldableLimbPanels.get(i);
			sixthTab.add(dlp);
		}
		
		final JPanel buttonsPanel2 = new JPanel();
		buttonsPanel2.setLayout( new BoxLayout(buttonsPanel2 , BoxLayout.LINE_AXIS) );
		buttonsPanel2.add(Box.createHorizontalGlue());
		final JButton buttonMore2 = new JButton(Messages.getInstance().getMessage("tab.weapon.more"));
		buttonLess2 = new JButton(Messages.getInstance().getMessage("tab.weapon.less"));
		buttonLess2.setEnabled(false);
		buttonsPanel2.add(buttonMore2);
		buttonsPanel2.add(buttonLess2);
		
		buttonMore2.addActionListener ( new ActionListener() {
			public void actionPerformed ( ActionEvent evt )
			{
				PathCommandsPanel newPanel = new PathCommandsPanel(Messages.getInstance().getMessage("label.weaponrequirement"),Messages.getInstance().getMessage("label.name"),true);
				addWeaponLimbRequirementPanel(newPanel);
			}
		});
		
		buttonLess2.addActionListener ( new ActionListener() 
		{
			public void actionPerformed ( ActionEvent evt )
			{
				removeWeaponLimbRequirementPanel();
			}
		});
		
		notSoInnerPanelSixth.add(buttonsPanel2,BorderLayout.SOUTH);
		
		attackPanel = new AttDefSpecsPanel(true);
		
		//sixthTab.add(attackPanel);
		
		defensePanel = new AttDefSpecsPanel(false);
		
		//sixthTab.add(defensePanel);
		
		JTabbedPane subJtp = new JTabbedPane();
		subJtp.add(attackPanel,Messages.getInstance().getMessage("weapon.attack"));
		subJtp.add(defensePanel,Messages.getInstance().getMessage("weapon.defense"));
		subJtp.setBorder(BorderFactory.createBevelBorder(2));
		
		//fifthTab.add(damlp);
		
		sixthTab.add(subJtp);

		jtp.add(Messages.getInstance().getMessage("tab.weapon"),sixthTab);
		
		
		
		
		

		
	}
	
	public void doInitMinimal( org.w3c.dom.Node e )
	{
		nameTextField.setText(((org.w3c.dom.Element)e).getAttribute("name"));
	}
	
	
	private void addLimbRequirementPanel ( PathCommandsPanel newPanel )
	{
		wearableLimbPanels.add(newPanel);
		innerPanelFifth.add(newPanel);
		fifthTab.setVisible(false);
		fifthTab.setVisible(true);
		buttonLess.setEnabled(true);
	}
	
	private void removeLimbRequirementPanel ( )
	{
		if ( wearableLimbPanels.size() < 1 ) return;
		PathCommandsPanel thePanel = (PathCommandsPanel) wearableLimbPanels.get(wearableLimbPanels.size()-1);
		wearableLimbPanels.remove(thePanel);
		innerPanelFifth.remove(thePanel);
		fifthTab.setVisible(false);
		fifthTab.setVisible(true);
		if ( wearableLimbPanels.size() < 1 ) buttonLess.setEnabled(false);
	}
	
	
	private void addWeaponLimbRequirementPanel ( PathCommandsPanel newPanel )
	{
		wieldableLimbPanels.add(newPanel);
		innerPanelSixth.add(newPanel);
		sixthTab.setVisible(false);
		sixthTab.setVisible(true);
		buttonLess2.setEnabled(true);
	}
	
	private void removeWeaponLimbRequirementPanel ( )
	{
		if ( wieldableLimbPanels.size() < 1 ) return;
		PathCommandsPanel thePanel = (PathCommandsPanel) wieldableLimbPanels.get(wieldableLimbPanels.size()-1);
		wieldableLimbPanels.remove(thePanel);
		innerPanelSixth.remove(thePanel);
		sixthTab.setVisible(false);
		sixthTab.setVisible(true);
		if ( wieldableLimbPanels.size() < 1 ) buttonLess2.setEnabled(false);
	}
	
	
	public Node doGetXML ( Document d )
	{
		
		Element result = d.createElement("Item");
		
		result.setAttribute("name",this.getName());
		
		//TODO: "type" attr would go here
		if ( wBox.isSelected() )
			result.setAttribute("type", "wearable");
		
		if ( weaponBox.isSelected() )
			result.setAttribute("type", "weapon");
		
		if ( !extendsComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("none") ))
			result.setAttribute("extends",extendsComboBox.getSelectedItem().toString());
		if ( !clonesComboBox.getSelectedItem().equals(Messages.getInstance().getMessage("none") ))
		{
			result.setAttribute("clones",clonesComboBox.getSelectedItem().toString());
			//identical clones can't have more features
			return result;
		}
			

		//result.setAttribute("weight",tfWeight.getText());
		result.setAttribute("weight",String.valueOf(getIntegerFromField(tfWeight,0)));
		
		//result.setAttribute("volume",tfVolume.getText());
		result.setAttribute("volume",String.valueOf(getIntegerFromField(tfVolume,0)));
		
		result.setAttribute("canGet",String.valueOf(!ungettableBox.isSelected()));
		
		
		String genderString = (String) genderComboBox.getSelectedItem();
		if (genderString.equals(Messages.getInstance().getMessage("gender.m")))
			result.setAttribute("gender","true");
		else
			result.setAttribute("gender","false");
		
		

		
		result.setAttribute("openable",String.valueOf(oBox.isSelected()));
		result.setAttribute("closeable",String.valueOf(cBox.isSelected()));
		result.setAttribute("lockable",String.valueOf(lBox.isSelected()));
		result.setAttribute("unlockable",String.valueOf(uBox.isSelected()));
		
		
		//names
		result.appendChild(snp.getXML(d,"SingularNames"));
		result.appendChild(pnp.getXML(d,"PluralNames"));
		result.appendChild(srn.getXMLForNames(d,"SingularReferenceNames"));
		result.appendChild(prn.getXMLForNames(d,"PluralReferenceNames"));
				
		result.appendChild((Element)dlp.getXML(d));		
		result.appendChild((Element)edp.getXML(d));
		
		//open-close-lock-unlock descriptions
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
		
		
		//wearable
		if ( wBox.isSelected() )
		{
			Element wearableSpecsElt = d.createElement("WearableSpecs"); 
			if ( wearableLimbPanels.size() > 0 )
			{
				Element wearableLimbsElt = d.createElement("WearableLimbs");
				for ( int i = 0 ; i < wearableLimbPanels.size() ; i++ )
				{
					PathCommandsPanel aPanel = (PathCommandsPanel) wearableLimbPanels.get(i);
					Element reqElt = (Element) aPanel.getXMLForNames(d,"Requirement");
					wearableLimbsElt.appendChild(reqElt);
				}
				wearableSpecsElt.appendChild(wearableLimbsElt);
			}
			//damage list
			wearableSpecsElt.appendChild((Element)damlp.getXML(d));
			
			result.appendChild(wearableSpecsElt);
		}
		
		//weapon
		if ( weaponBox.isSelected() )
		{
			Element weaponSpecsElt = d.createElement("WeaponSpecs");
			if ( wieldableLimbPanels.size() > 0 )
			{
				Element wieldableLimbsElt = d.createElement("WieldableLimbs");
				for ( int i = 0 ; i < wieldableLimbPanels.size() ; i++ )
				{
					PathCommandsPanel aPanel = (PathCommandsPanel) wieldableLimbPanels.get(i);
					Element reqElt = (Element) aPanel.getXMLForNames(d,"Requirement");
					wieldableLimbsElt.appendChild(reqElt);
				}
				weaponSpecsElt.appendChild(wieldableLimbsElt);
			}
			weaponSpecsElt.appendChild(attackPanel.getXML(d));
			weaponSpecsElt.appendChild(defensePanel.getXML(d));
			
			result.appendChild(weaponSpecsElt);
		}
		
		
		//properties, code
		result.appendChild((Element)pp.getXML(d));
		Element codeElt = ((Element)bcp.getXML(d));
		if ( codeElt != null )
			result.appendChild(codeElt);
		
		List arrows = node.getArrows();
		
		
		//items:
		Element invElt = null;
		if ( containerBox.isSelected() )
			invElt = d.createElement("Inventory");
		
		Element partsElt = null;
		Element keysElt = null;
		
		//custom relationships
		//Element relationshipsElt = d.createElement("RelationshipList");
				
		for ( int i = 0 ; i < arrows.size() ; i++ )
		{
			
			GraphElementPanel gep = ((Arrow)arrows.get(i)).getAssociatedPanel();
			
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
			/*
			 //refactored upwards
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
		
		
		if ( invElt != null )
			result.appendChild(invElt);
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
		if ( e.hasAttribute("clones") )
		{
			int clonesInd = indexOf ( clonesComboBox.getModel() , e.getAttribute("clones") );
			if ( clonesInd >= 0 )
			{
				clonesComboBox.setSelectedIndex(clonesInd);
			}
		}
		
		//openable, closeable, etc. flags
		if ( e.hasAttribute("openable") ) oBox.setSelected( Boolean.parseBoolean( e.getAttribute("openable") ) );
		if ( e.hasAttribute("closeable") ) cBox.setSelected( Boolean.parseBoolean( e.getAttribute("closeable") ) );
		if ( e.hasAttribute("lockable") ) lBox.setSelected( Boolean.parseBoolean( e.getAttribute("lockable") ) );
		if ( e.hasAttribute("unlockable") ) uBox.setSelected( Boolean.parseBoolean( e.getAttribute("unlockable") ) );
		
		//wearable
		wBox.setSelected("wearable".equals(e.getAttribute("type")));
		
		//weapon
		weaponBox.setSelected("weapon".equals(e.getAttribute("type")));
		
		//description list panel
		NodeList desNl = e.getElementsByTagName("DescriptionList");
		Element desListElt = (Element) desNl.item(0);
		if ( desListElt != null )
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
		
		//sing names panel
		desNl = e.getElementsByTagName("SingularNames");
		desListElt = (Element) desNl.item(0);
		if ( desListElt != null )
			snp.initFromXML(desListElt);
		
		//plur names panel
		desNl = e.getElementsByTagName("PluralNames");
		desListElt = (Element) desNl.item(0);
		if ( desListElt != null )
			pnp.initFromXML(desListElt);
		
		//sing ref names panel
		NodeList srnNl = e.getElementsByTagName("SingularReferenceNames");
		Element srnListElt = (Element) srnNl.item(0);
		if ( srnListElt != null )
			srn.initFromXML(srnListElt);
		
		//plural ref names panel
		NodeList prnNl = e.getElementsByTagName("PluralReferenceNames");
		Element prnListElt = (Element) prnNl.item(0);
		if ( prnListElt != null )
			prn.initFromXML(prnListElt);
		
		//open descriptions panel
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
		
		//if it has inventory, then it is a container. Inventory is not initted here.
		//All inventories are initted at WorldPanel.
		NodeList invs = DOMUtils.getDirectChildrenElementsByTagName(e,"Inventory");
		if ( invs.getLength() > 0 )
			containerBox.setSelected(true);
		
		//weight, volume textfields
		tfWeight.setText(e.getAttribute("weight"));
		tfVolume.setText(e.getAttribute("volume"));
		
		//ungettable flag checkbox
		if ( e.hasAttribute("canGet") ) ungettableBox.setSelected( !Boolean.parseBoolean(e.getAttribute("canGet") ) );
		
		//set default weights and volumes if not present
		if ( tfWeight.getText().trim().length() <= 0 )
			tfWeight.setText("0");
		if ( tfVolume.getText().trim().length() <= 0 )
			tfVolume.setText("0");
		
		//gender combo box
		if ( e.getAttribute("gender").equals("false") || e.getAttribute("gender").equals("0") )
			genderComboBox.setSelectedIndex(1);
		else
			genderComboBox.setSelectedIndex(0);
		
		//wearable
		if ( "wearable".equals(e.getAttribute("type")) )
		{
			NodeList wearableSpecs = e.getElementsByTagName("WearableSpecs");
			if ( wearableSpecs.getLength() > 0 )
			{
				Element wearableSpecsElt = (Element) wearableSpecs.item(0);
				NodeList wearableLimbs = wearableSpecsElt.getElementsByTagName("WearableLimbs");
				if ( wearableLimbs.getLength() > 0 )
				{
					Element wearableLimbsElt = (Element) wearableLimbs.item(0);
					NodeList requirements = wearableLimbsElt.getElementsByTagName("Requirement");
					for ( int i = 0 ; i < requirements.getLength() ; i++ )
					{
						Element requirement = (Element)requirements.item(i);
						PathCommandsPanel newPanel = new PathCommandsPanel(Messages.getInstance().getMessage("label.wearrequirement"),Messages.getInstance().getMessage("label.name"),true);
						newPanel.initFromXML(requirement);
						addLimbRequirementPanel(newPanel);		
					}
				}
				NodeList damageList = wearableSpecsElt.getElementsByTagName("DamageList");
				if ( damageList.getLength() > 0 )
				{
					Element damageListElt = (Element) damageList.item(0);
					damlp.initFromXML(damageListElt);
				}
			}
		}
		
		//weapon
		if ( "weapon".equals(e.getAttribute("type")) )
		{
			NodeList weaponSpecs = e.getElementsByTagName("WeaponSpecs");
			if ( weaponSpecs.getLength() > 0 )
			{
				Element weaponSpecsElt = (Element) weaponSpecs.item(0);
				NodeList wieldableLimbs = weaponSpecsElt.getElementsByTagName("WieldableLimbs");
				if ( wieldableLimbs.getLength() > 0 )
				{
					Element wieldableLimbsElt = (Element) wieldableLimbs.item(0);
					NodeList requirements = wieldableLimbsElt.getElementsByTagName("Requirement");
					for ( int i = 0 ; i < requirements.getLength() ; i++ )
					{
						Element requirement = (Element)requirements.item(i);
						PathCommandsPanel newPanel = new PathCommandsPanel(Messages.getInstance().getMessage("label.weaponrequirement"),Messages.getInstance().getMessage("label.name"),true);
						newPanel.initFromXML(requirement);
						addWeaponLimbRequirementPanel(newPanel);		
					}
				}
				NodeList attack = weaponSpecsElt.getElementsByTagName("Attack");
				if ( attack.getLength() > 0 )
				{
					attackPanel.initFromXML(attack.item(0));
				}
				NodeList defense = weaponSpecsElt.getElementsByTagName("Defense");
				if ( defense.getLength() > 0 )
				{
					defensePanel.initFromXML(defense.item(0));
				}
			}
		}
		
		
		
		
	}
	
	//nah, this is sz of the panel
	/*
	public void paintComponent ( Graphics g )
	{
		System.out.println(this.getParent().getParent());
		double splitsz = this.getParent().getParent().getSize().getWidth();
		if ( splitsz >= this.getMinimumSize().getWidth() )
			this.setPreferredSize(new Dimension((int)splitsz,(int)this.getPreferredSize().getHeight()));
		super.paintComponent(g);
	}
	*/
	
	
}
