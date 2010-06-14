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
public class SpellPanel extends EntityPanel 
{

	
	private JComboBox extendsComboBox = new JComboBox();
	private JComboBox clonesComboBox = new JComboBox();
	
	/*
	private JComboBox genderComboBox = new JComboBox();
	
	private JTextField tfWeight = new EnhancedJTextField("0",5);
	private JTextField tfVolume = new EnhancedJTextField("0",5);
	*/
	/*
	private JCheckBox containerBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.container"));
	*/
	
	private JTextField tfCastProbSteepness = new EnhancedJTextField("0",5);
	private JTextField tfCastTimeStarting = new EnhancedJTextField("0",5);
	private JTextField tfCastTimeSteepness = new EnhancedJTextField("0",5);
	private JTextField tfDurationStarting = new EnhancedJTextField("0",5);
	private JTextField tfDurationSteepness = new EnhancedJTextField("0",5);
	private JTextField tfManaCostStarting = new EnhancedJTextField("0",5);
	private JTextField tfManaCostSteepness = new EnhancedJTextField("0",5);
	private JTextField tfIntensityStarting = new EnhancedJTextField("0",5);
	private JTextField tfIntensitySteepness = new EnhancedJTextField("0",5);
	
	
	private PathCommandsPanel involvedSkillsPanel; //involved skill
	

	
	//privatize
	//public DescriptionListPanel dlp;
	//private ExtraDescriptionsPanel edp;
	private BSHCodePanel bcp;
	private PropertiesPanel pp;
	
	/*
	private DescriptionListPanel snp; //sing names
	private DescriptionListPanel pnp; //plur names
	*/
	
	private PathCommandsPanel srn; //sing refnames
	private PathCommandsPanel prn; //sing refnames
	
	
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
	
	private org.f2o.absurdum.puck.gui.graph.Node node;
	
	
	public SpellPanel( org.f2o.absurdum.puck.gui.graph.Node node )
	{
		super();
		nameTextField.setText("Spell #"+getID());
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
		
		
		Vector spellNodes =
			this.getGraphEditingPanel().getNodes(SpellNode.class,true);
		extendsComboBox = new JComboBox ( new DefaultComboBoxModel ( spellNodes ) );
		clonesComboBox = new JComboBox ( new DefaultComboBoxModel ( spellNodes ) );
		
		
		JPanel extendsPanel = new JPanel();
		extendsPanel.add ( new JLabel(Messages.getInstance().getMessage("inherit.from")) );
		extendsPanel.add ( extendsComboBox );
		firstTab.add(extendsPanel);
		JPanel clonesPanel = new JPanel();
		clonesPanel.add ( new JLabel(Messages.getInstance().getMessage("clone.from")) );
		clonesPanel.add ( clonesComboBox );
		firstTab.add(clonesPanel);
		
		
		
		JPanel castProbPanel = new JPanel();
		castProbPanel.add(new JLabel(Messages.getInstance().getMessage("spell.castprob.steepness")));
		castProbPanel.add(tfCastProbSteepness);
		firstTab.add(castProbPanel);

		JPanel castTimePanel = new JPanel();
		castTimePanel.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("spell.casttime")));
		castTimePanel.add(new JLabel(Messages.getInstance().getMessage("spell.casttime.starting")));
		castTimePanel.add(tfCastTimeStarting);
		castTimePanel.add(new JLabel(Messages.getInstance().getMessage("spell.casttime.steepness")));
		castTimePanel.add(tfCastTimeSteepness);
		firstTab.add(castTimePanel);
		
		JPanel durationPanel = new JPanel();
		durationPanel.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("spell.duration")));
		durationPanel.add(new JLabel(Messages.getInstance().getMessage("spell.duration.starting")));
		durationPanel.add(tfDurationStarting);
		durationPanel.add(new JLabel(Messages.getInstance().getMessage("spell.duration.steepness")));
		durationPanel.add(tfDurationSteepness);
		firstTab.add(durationPanel);
		
		JPanel manaCostPanel = new JPanel();
		manaCostPanel.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("spell.manacost")));
		manaCostPanel.add(new JLabel(Messages.getInstance().getMessage("spell.manacost.starting")));
		manaCostPanel.add(tfManaCostStarting);
		manaCostPanel.add(new JLabel(Messages.getInstance().getMessage("spell.manacost.steepness")));
		manaCostPanel.add(tfManaCostSteepness);
		firstTab.add(manaCostPanel);
		
		JPanel intensityPanel = new JPanel();
		intensityPanel.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("spell.intensity")));
		intensityPanel.add(new JLabel(Messages.getInstance().getMessage("spell.intensity.starting")));
		intensityPanel.add(tfIntensityStarting);
		intensityPanel.add(new JLabel(Messages.getInstance().getMessage("spell.intensity.steepness")));
		intensityPanel.add(tfIntensitySteepness);
		firstTab.add(intensityPanel);
		
		involvedSkillsPanel = new PathCommandsPanel(Messages.getInstance().getMessage("spell.involvedskills"),Messages.getInstance().getMessage("label.skill"),true);
		firstTab.add(involvedSkillsPanel);
		
		
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
		
		
		
		JPanel thirdTab = new JPanel();
		
		
		thirdTab.setLayout(new BoxLayout(thirdTab, BoxLayout.PAGE_AXIS));
		
		/*
		snp = new DescriptionListPanel(Messages.getInstance().getMessage("label.singnames"),Messages.getInstance().getMessage("label.name"));
		thirdTab.add(snp);
		
		pnp = new DescriptionListPanel(Messages.getInstance().getMessage("label.plurnames"),Messages.getInstance().getMessage("label.name"));
		thirdTab.add(pnp);
		*/
		
		
		/**
		 * TODO Note:
		 * As of 2008-04-19, spells in AGE don't have reference names.
		 * But they should be implemented in the future, so I leave this panel here for when this functionality is added to AGE.
		 */
		
		srn = new PathCommandsPanel(Messages.getInstance().getMessage("label.singrefnames"),Messages.getInstance().getMessage("label.name"),true);
		thirdTab.add(srn);
		
		prn = new PathCommandsPanel(Messages.getInstance().getMessage("label.plurrefnames"),Messages.getInstance().getMessage("label.name"),true);
		thirdTab.add(prn);
		
		
		
		jtp.add(Messages.getInstance().getMessage("tab.names"),thirdTab);
		
		JPanel secondTab = new JPanel();
		
		secondTab.setLayout(new BoxLayout(secondTab, BoxLayout.PAGE_AXIS));
		
		bcp = new BSHCodePanel("spell",this);
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
		
		Element result = d.createElement("Spell");
		
		result.setAttribute("name",this.getName());
		
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
		
		result.appendChild(involvedSkillsPanel.getXML(d,"InvolvedSkills","Skill","name"));
		
		Element castProbElt  = d.createElement("CastProbability");
		castProbElt.setAttribute("steepness",tfCastProbSteepness.getText());
		
		Element castTimeElt = d.createElement("CastTime");
		castTimeElt.setAttribute("starting",tfCastTimeStarting.getText());
		castTimeElt.setAttribute("steepness",tfCastTimeSteepness.getText());
		
		Element durationElt = d.createElement("Duration");
		durationElt.setAttribute("starting",tfDurationStarting.getText());
		durationElt.setAttribute("steepness",tfDurationSteepness.getText());
		
		Element manaCostElt = d.createElement("ManaCost");
		manaCostElt.setAttribute("starting",tfManaCostStarting.getText());
		manaCostElt.setAttribute("steepness",tfManaCostSteepness.getText());
		
		Element intensityElt = d.createElement("Intensity");
		intensityElt.setAttribute("starting",tfIntensityStarting.getText());
		intensityElt.setAttribute("steepness",tfIntensitySteepness.getText());
		
		result.appendChild(castProbElt);
		result.appendChild(castTimeElt);
		result.appendChild(durationElt);
		result.appendChild(manaCostElt);
		result.appendChild(intensityElt);
		
		
		//names
		/*
		result.appendChild(snp.getXML(d,"SingularNames"));
		result.appendChild(pnp.getXML(d,"PluralNames"));
		*/
		result.appendChild(srn.getXMLForNames(d,"SingularReferenceNames"));
		result.appendChild(prn.getXMLForNames(d,"PluralReferenceNames"));
			
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
		
		
		//effects:
		Element effList = null;
		//if ( containerBox.isSelected() )
			effList = d.createElement("EffectList");
		
		/*
		Element partsElt = null;
		Element keysElt = null;
		*/
				
		for ( int i = 0 ; i < arrows.size() ; i++ )
		{
			
			GraphElementPanel gep = ((Arrow)arrows.get(i)).getAssociatedPanel();
			
			//TODO EffectList stuff
			
			if ( gep instanceof SpellHasEffectPanel )
			{
				SpellHasEffectPanel relPanel = (SpellHasEffectPanel) gep;
				String relType = relPanel.getRelationshipType();
				if ( relType.equals(Messages.getInstance().getMessage("structural.spell.abstractentity.haseffect")) ) //has-effect relationship
				{
					effList.appendChild( relPanel.getXML(d) );
				}
			}
			
		}
		
		Element relationshipsElt = (Element) getCustomRelationshipListXML ( d , node );
		
		
		if ( effList != null )
			result.appendChild(effList);
		
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
		
		//cast probability
		NodeList castProbNl = e.getElementsByTagName("CastProbability");
		if ( castProbNl.getLength() > 0 )
		{
			Element castProbElt = (Element) castProbNl.item(0);
			tfCastProbSteepness.setText(castProbElt.getAttribute("steepness"));
		}
		
		//cast time
		NodeList castTimeNl = e.getElementsByTagName("CastTime");
		if ( castTimeNl.getLength() > 0 )
		{
			Element castTimeElt = (Element) castTimeNl.item(0);
			tfCastTimeStarting.setText(castTimeElt.getAttribute("starting"));
			tfCastTimeSteepness.setText(castTimeElt.getAttribute("steepness"));
		}
		
		//duration
		NodeList durationNl = e.getElementsByTagName("Duration");
		if ( durationNl.getLength() > 0 )
		{
			Element durationElt = (Element) durationNl.item(0);
			tfDurationStarting.setText(durationElt.getAttribute("starting"));
			tfDurationSteepness.setText(durationElt.getAttribute("steepness"));
		}
		
		//mana cost
		NodeList manaCostNl = e.getElementsByTagName("ManaCost");
		if ( manaCostNl.getLength() > 0 )
		{
			Element manaCostElt = (Element) manaCostNl.item(0);
			tfManaCostStarting.setText(manaCostElt.getAttribute("starting"));
			tfManaCostSteepness.setText(manaCostElt.getAttribute("steepness"));
		}
		
		//intensity
		NodeList intensityNl = e.getElementsByTagName("Intensity");
		if ( intensityNl.getLength() > 0 )
		{
			Element intensityElt = (Element) intensityNl.item(0);
			tfIntensityStarting.setText(intensityElt.getAttribute("starting"));
			tfIntensitySteepness.setText(intensityElt.getAttribute("steepness"));
		}
		
		//involved skills
		NodeList skillsNl = e.getElementsByTagName("InvolvedSkills");
		if ( skillsNl.getLength() > 0 )
		{
			Element skillsElt = (Element) skillsNl.item(0);
			involvedSkillsPanel.initFromXML(skillsElt,"Skill","name");
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
		NodeList srnNl = e.getElementsByTagName("SingularReferenceNames");
		Element srnListElt = (Element) srnNl.item(0);
		if ( srnListElt != null )
			srn.initFromXML(srnListElt);
		
		//plural ref names panel
		NodeList prnNl = e.getElementsByTagName("PluralReferenceNames");
		Element prnListElt = (Element) prnNl.item(0);
		if ( srnListElt != null )
			prn.initFromXML(prnListElt);
		

		//set default values for text fields if they are empty
		setDefaultTextIfEmpty ( tfCastProbSteepness , "0" );
		setDefaultTextIfEmpty ( tfCastTimeStarting , "10" );
		setDefaultTextIfEmpty ( tfCastTimeSteepness , "0" );
		setDefaultTextIfEmpty ( tfManaCostStarting , "10" );
		setDefaultTextIfEmpty ( tfManaCostSteepness , "0" );
		setDefaultTextIfEmpty ( tfIntensityStarting , "10" );
		setDefaultTextIfEmpty ( tfIntensitySteepness , "0" );
		setDefaultTextIfEmpty ( tfDurationStarting , "300" );
		setDefaultTextIfEmpty ( tfDurationSteepness , "0" );
		
		
	}
	
	private void setDefaultTextIfEmpty ( JTextField textField , String value )
	{
		if ( textField.getText().trim().length() <= 0 )
			textField.setText(value);
	}
	
	
}

