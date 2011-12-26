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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.f2o.absurdum.puck.bsh.BeanShellCodeHolder;
import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.CharacterNode;
import org.f2o.absurdum.puck.i18n.UIMessages;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 18:51:54
 */
public class CharPanel extends EntityPanel implements BeanShellCodeHolder
{

	
	private JComboBox extendsComboBox = new JComboBox();
	private JComboBox clonesComboBox = new JComboBox();
	
	//privatize
	public DescriptionListPanel dlp;
	private ExtraDescriptionsPanel edp;
	private BSHCodePanel bcp;
	private PropertiesPanel pp;
	
	private DescriptionListPanel snp; //sing names
	private DescriptionListPanel pnp; //plur names
	
	private PathCommandsPanel srn; //sing refnames
	private PathCommandsPanel prn; //sing refnames
	
	private SkillsPanel sp; //teh 1337 5ki11z
	
	//the tabbed pane
	private JTabbedPane jtp;
	
	private CharacterNode node;
	
	
	private JTextField tfHP = new EnhancedJTextField("10",5);
	private JTextField tfMaxHP = new EnhancedJTextField("10",5);
	private JTextField tfMP = new EnhancedJTextField("10",5);
	private JTextField tfMaxMP = new EnhancedJTextField("10",5);
	private JComboBox genderComboBox = new JComboBox();
	
	private JCheckBox cbPlayer = new JCheckBox(UIMessages.getInstance().getMessage("char.isplayer"));
	
	
	public boolean isPlayer ( )
	{
		return cbPlayer.isSelected();
	}
	
	public void setPlayer ( boolean val )
	{
		cbPlayer.setSelected(val);
	}
	
	public CharPanel( CharacterNode node )
	{
		super();
		nameTextField.setText("Character #"+getID());
		this.node = node;
		//add ( extendsComboBox );
	}
	
	public String toString()
	{
		return nameTextField.getText() + "##" + super.toString();
	}
	
	public String getPanelName()
	{
		return nameTextField.getText();
	}
	
	public String getBSHCode()
	{
		if ( bcp == null ) forceRealInitFromXml(true); //code panel not yet initted.
		return bcp.getCode();
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
		namePanel.add(new JLabel(UIMessages.getInstance().getMessage("entity.uniquename")));
		namePanel.add(nameTextField);
		firstTab.add(namePanel);
		
		Vector charNodes =
			this.getGraphEditingPanel().getCharNodes(true);
		extendsComboBox = new JComboBox ( new DefaultComboBoxModel ( charNodes ) );
		clonesComboBox = new JComboBox ( new DefaultComboBoxModel ( charNodes ) );
		
		JPanel extendsPanel = new JPanel();
		extendsPanel.add ( new JLabel(UIMessages.getInstance().getMessage("inherit.from")) );
		extendsPanel.add ( extendsComboBox );
		firstTab.add(extendsPanel);
		JPanel clonesPanel = new JPanel();
		clonesPanel.add ( new JLabel(UIMessages.getInstance().getMessage("clone.from")) );
		clonesPanel.add ( clonesComboBox );
		firstTab.add(clonesPanel);
		
		JPanel genderPanel = new JPanel();
		genderComboBox = new JComboBox ( new String[] { UIMessages.getInstance().getMessage("gender.m") , UIMessages.getInstance().getMessage("gender.f") } );
		genderPanel.add(new JLabel(UIMessages.getInstance().getMessage("gender")));
		genderPanel.add(genderComboBox);
		firstTab.add(genderPanel);
		
		JPanel hpPanel = new JPanel();
		hpPanel.add(new JLabel(UIMessages.getInstance().getMessage("char.hp")));
		hpPanel.add(tfHP);
		hpPanel.add(new JLabel(UIMessages.getInstance().getMessage("char.hpmax")));
		hpPanel.add(tfMaxHP);
		firstTab.add(hpPanel);
		
		JPanel mpPanel = new JPanel();
		mpPanel.add(new JLabel(UIMessages.getInstance().getMessage("char.mp")));
		mpPanel.add(tfMP);
		mpPanel.add(new JLabel(UIMessages.getInstance().getMessage("char.mpmax")));
		mpPanel.add(tfMaxMP);
		firstTab.add(mpPanel);
		
		JPanel playerPanel = new JPanel();
		playerPanel.add(cbPlayer);
		firstTab.add(playerPanel);
		
		
		dlp = new DescriptionListPanel(5);
		firstTab.add(dlp);
		
		edp = new ExtraDescriptionsPanel(5);
		firstTab.add(edp);

		jtp.add("General",firstTab);
		
		
		JPanel thirdTab = new JPanel();
		
		thirdTab.setLayout(new BoxLayout(thirdTab, BoxLayout.PAGE_AXIS));
		
		snp = new DescriptionListPanel(UIMessages.getInstance().getMessage("label.singnames"),UIMessages.getInstance().getMessage("label.name"),false,true,1);
		thirdTab.add(snp);
		
		pnp = new DescriptionListPanel(UIMessages.getInstance().getMessage("label.plurnames"),UIMessages.getInstance().getMessage("label.name"),1);
		thirdTab.add(pnp);
		
		srn = new PathCommandsPanel(UIMessages.getInstance().getMessage("label.singrefnames"),UIMessages.getInstance().getMessage("label.name"),true);
		thirdTab.add(srn);
		
		prn = new PathCommandsPanel(UIMessages.getInstance().getMessage("label.plurrefnames"),UIMessages.getInstance().getMessage("label.name"),true);
		thirdTab.add(prn);
		
		jtp.add(UIMessages.getInstance().getMessage("tab.names"),thirdTab);
		
		
		JPanel secondTab = new JPanel();
		
		secondTab.setLayout(new BoxLayout(secondTab, BoxLayout.PAGE_AXIS));
		
		bcp = new BSHCodePanel("mobile",this);
		secondTab.add(bcp);
		
		pp = new PropertiesPanel();
		secondTab.add(pp);
		
		jtp.add(UIMessages.getInstance().getMessage("tab.codeprop"),secondTab);
		
		
		JPanel traitsTab = new JPanel();
		
		traitsTab.setLayout(new BoxLayout(traitsTab, BoxLayout.PAGE_AXIS));
		
		sp = new SkillsPanel();
		traitsTab.add(sp);
		
		jtp.add(UIMessages.getInstance().getMessage("tab.traits"),traitsTab);

		
	}
	
	
	public Node doGetXML ( Document d )
	{
		
		Element result = d.createElement("Mobile");
		
		result.setAttribute("name",this.getPanelName());
		
		//result.setAttribute("hp",tfHP.getText());
		result.setAttribute("hp",String.valueOf(getIntegerFromField(tfHP,20)));
		
		//result.setAttribute("mp",tfMP.getText());
		result.setAttribute("mp",String.valueOf(getIntegerFromField(tfMP,20)));
		
		//result.setAttribute("maxhp",tfMaxHP.getText());
		result.setAttribute("maxhp",String.valueOf(getIntegerFromField(tfMaxHP,20)));
		
		//result.setAttribute("maxmp",tfMaxMP.getText());
		result.setAttribute("maxmp",String.valueOf(getIntegerFromField(tfMaxMP,20)));
		
		String genderString = (String) genderComboBox.getSelectedItem();
		if (genderString.equals(UIMessages.getInstance().getMessage("gender.m")))
			result.setAttribute("gender","true");
		else
			result.setAttribute("gender","false");
		
		if ( !extendsComboBox.getSelectedItem().equals(UIMessages.getInstance().getMessage("none") ))
			result.setAttribute("extends",extendsComboBox.getSelectedItem().toString());
		if ( !clonesComboBox.getSelectedItem().equals(UIMessages.getInstance().getMessage("none") ))
			result.setAttribute("clones",clonesComboBox.getSelectedItem().toString());
		
		//names
		result.appendChild(snp.getXML(d,"SingularNames"));
		result.appendChild(pnp.getXML(d,"PluralNames"));
		result.appendChild(srn.getXMLForNames(d,"SingularReferenceNames"));
		result.appendChild(prn.getXMLForNames(d,"PluralReferenceNames"));
		
		
		
		result.appendChild((Element)dlp.getXML(d));
		result.appendChild((Element)edp.getXML(d));
		result.appendChild((Element)pp.getXML(d));
		Element codeElt = ((Element)bcp.getXML(d));
		if ( codeElt != null )
			result.appendChild(codeElt);
		
		Element traitsElt = d.createElement("Traits");
		traitsElt.appendChild(sp.getXML(d));
		result.appendChild(traitsElt);
		
		List arrows = node.getArrows();
		
		//items:
		Element invElt = d.createElement("Inventory");
		
		Element partsElt = null;
		
		Element spellsElt = null;
		
		//custom relationships
		//Element relationshipsElt = d.createElement("RelationshipList");
				
		for ( int i = 0 ; i < arrows.size() ; i++ )
		{
			
			GraphElementPanel gep = ((Arrow)arrows.get(i)).getAssociatedPanel();
			
			if ( gep instanceof CharHasItemPanel )
			{
				CharHasItemPanel relPanel = (CharHasItemPanel) gep;
				String relType = relPanel.getRelationshipType();
				if ( relType.equals(UIMessages.getInstance().getMessage("structural.char.item.carry")) ) //carry relationship
				{
					if ( invElt == null ) invElt = d.createElement("Inventory");
					invElt.appendChild( relPanel.getXML(d) );
				}
				if ( relType.equals(UIMessages.getInstance().getMessage("structural.char.item.haspart")) ) //has-part relationship
				{
					if ( partsElt == null ) partsElt = d.createElement("Inventory");
					partsElt.appendChild( relPanel.getXML(d) );
				}
			}
			
			if ( gep instanceof CharHasSpellPanel )
			{
				CharHasSpellPanel relPanel = (CharHasSpellPanel) gep;
				String relType = relPanel.getRelationshipType();
				if ( relType.equals(UIMessages.getInstance().getMessage("structural.char.spell.know")) ) //know relationship
				{
					if ( spellsElt == null ) spellsElt = d.createElement("SpellList");
					spellsElt.appendChild( relPanel.getXML(d) );
				}
			}
			
			
			/*
		    //refactored upwards to EntityPanel
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
		if ( spellsElt != null )
		{
			result.appendChild(spellsElt);
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
		if ( e.hasAttribute("clones") )
		{
			int clonesInd = indexOf ( clonesComboBox.getModel() , e.getAttribute("clones") );
			if ( clonesInd >= 0 )
			{
				clonesComboBox.setSelectedIndex(clonesInd);
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
		pp.initFromXML(plElt);
					
		//sing names panel
		desNl = e.getElementsByTagName("SingularNames");
		desListElt = (Element) desNl.item(0);
		snp.initFromXML(desListElt);
		
		//plur names panel
		desNl = e.getElementsByTagName("PluralNames");
		desListElt = (Element) desNl.item(0);
		pnp.initFromXML(desListElt);
		
		//sing ref names panel
		NodeList srnNl = e.getElementsByTagName("SingularReferenceNames");
		Element srnListElt = (Element) srnNl.item(0);
		srn.initFromXML(srnListElt);
		
		//plural ref names panel
		NodeList prnNl = e.getElementsByTagName("PluralReferenceNames");
		Element prnListElt = (Element) prnNl.item(0);
		prn.initFromXML(prnListElt);
		
		//HP, MP, maxHP, maxMP textfields
		tfHP.setText(e.getAttribute("hp"));
		tfMP.setText(e.getAttribute("mp"));
		tfMaxHP.setText(e.getAttribute("maxhp"));
		tfMaxMP.setText(e.getAttribute("maxmp"));
		
		//gender combo box
		if ( e.getAttribute("gender").equals("0") )
			genderComboBox.setSelectedIndex(1);
		else
			genderComboBox.setSelectedIndex(0);
		
		//skills panel
		NodeList traitsNl = e.getElementsByTagName("Traits");
		Element traitsElt = (Element) traitsNl.item(0);
		if ( traitsElt != null )
		{
			NodeList skillListNl = traitsElt.getElementsByTagName("SkillList");
			sp.initFromXML(skillListNl.item(0));
		}
		
		//TODO: initialize the cbPlayer.
		//already done for regular players, not for templates
		
	}
	
}
