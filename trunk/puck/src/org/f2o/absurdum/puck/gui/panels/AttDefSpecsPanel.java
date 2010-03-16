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
 * For attack/defense specs of weapons
 */
public class AttDefSpecsPanel extends JPanel 
{

	
	
	private JTextField tfMinUsage = new EnhancedJTextField("0",5);
	
	private JTextField tfProbabilitySteepness = new EnhancedJTextField("0",5);
	
	private JTextField tfTimeStarting = new EnhancedJTextField("0",5);
	private JTextField tfTimeSteepness = new EnhancedJTextField("0",5);
	private JTextField tfRecoverTimeStarting = new EnhancedJTextField("0",5);
	private JTextField tfRecoverTimeSteepness = new EnhancedJTextField("0",5);
	
	private DamageListPanel damagePanel;
	
	private SkillsPanel skillsPanel = new SkillsPanel("relevance");
	

	private boolean attack;

	
	public AttDefSpecsPanel( boolean attack ) //false? defense
	{
		this.attack = attack;
		
		String type = (attack)?"attack":"defense";
		
		String title = (attack)?
				Messages.getInstance().getMessage("weapon.attack")
				: 
				Messages.getInstance().getMessage("weapon.defense");
		
		setBorder(BorderFactory.createTitledBorder(title));		
				
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		JPanel minUsagePanel = new JPanel();
		minUsagePanel.add(new JLabel(Messages.getInstance().getMessage("weapon."+type+".minusage")));
		minUsagePanel.add(tfMinUsage);
		this.add(minUsagePanel);
		
		JPanel probPanel = new JPanel();
		probPanel.add(new JLabel(Messages.getInstance().getMessage("weapon."+type+".prob.steepness")));
		probPanel.add(tfProbabilitySteepness);
		this.add(probPanel);

		JPanel timePanel = new JPanel();
		timePanel.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("weapon."+type+".time")));
		timePanel.add(new JLabel(Messages.getInstance().getMessage("weapon."+type+".time.starting")));
		timePanel.add(tfTimeStarting);
		timePanel.add(new JLabel(Messages.getInstance().getMessage("weapon."+type+".time.steepness")));
		timePanel.add(tfTimeSteepness);
		this.add(timePanel);
		
		JPanel recoverPanel = new JPanel();
		recoverPanel.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("weapon."+type+".recovertime")));
		recoverPanel.add(new JLabel(Messages.getInstance().getMessage("weapon."+type+".recovertime.starting")));
		recoverPanel.add(tfRecoverTimeStarting);
		recoverPanel.add(new JLabel(Messages.getInstance().getMessage("weapon."+type+".recovertime.steepness")));
		recoverPanel.add(tfRecoverTimeSteepness);
		this.add(recoverPanel);
		
		damagePanel = new DamageListPanel(Messages.getInstance().getMessage("weapon."+type+".damage"));
		this.add(damagePanel);
		
		skillsPanel = new SkillsPanel("relevance");
		this.add(skillsPanel);
		
	}
	
	
	public Node getXML ( Document d )
	{
		
		Element result;
		if ( attack )
			result = d.createElement("Attack");
		else
			result = d.createElement("Defense");
		
	
		result.appendChild(skillsPanel.getListXML(d,"InvolvedSkills","Skill"));
		result.appendChild(damagePanel.getXML(d));
		
		Element probElt  = d.createElement("Probability");
		probElt.setAttribute("steepness",tfProbabilitySteepness.getText());
		
		Element timeElt = d.createElement("Time");
		timeElt.setAttribute("starting",tfTimeStarting.getText());
		timeElt.setAttribute("steepness",tfTimeSteepness.getText());
		
		Element recoverElt = d.createElement("RecoverTime");
		recoverElt.setAttribute("starting",tfRecoverTimeStarting.getText());
		recoverElt.setAttribute("steepness",tfRecoverTimeSteepness.getText());
		
		
		result.appendChild(probElt);
		result.appendChild(timeElt);
		result.appendChild(recoverElt);
		
		result.setAttribute("minUsage", tfMinUsage.getText());

				
		return result;
		
	}
	
	
	public void initFromXML ( org.w3c.dom.Node n )
	{
		
		Element e = (Element) n;
		
		//probability
		NodeList probNl = e.getElementsByTagName("Probability");
		if ( probNl.getLength() > 0 )
		{
			Element probElt = (Element) probNl.item(0);
			tfProbabilitySteepness.setText(probElt.getAttribute("steepness"));
		}
		
		//time
		NodeList timeNl = e.getElementsByTagName("Time");
		if ( timeNl.getLength() > 0 )
		{
			Element timeElt = (Element) timeNl.item(0);
			tfTimeStarting.setText(timeElt.getAttribute("starting"));
			tfTimeSteepness.setText(timeElt.getAttribute("steepness"));
		}
		
		//recover time
		NodeList recoverNl = e.getElementsByTagName("RecoverTime");
		if ( recoverNl.getLength() > 0 )
		{
			Element recoverElt = (Element) recoverNl.item(0);
			tfRecoverTimeStarting.setText(recoverElt.getAttribute("starting"));
			tfRecoverTimeSteepness.setText(recoverElt.getAttribute("steepness"));
		}
		
		//involved skills
		NodeList skillsNl = e.getElementsByTagName("InvolvedSkills");
		if ( skillsNl.getLength() > 0 )
		{
			Element skillsElt = (Element) skillsNl.item(0);
			skillsPanel.initFromXML(skillsElt,"Skill");
		}
		
		//damage
		NodeList damageList = e.getElementsByTagName("DamageList");
		if ( damageList.getLength() > 0 )
		{
			Element damageListElt = (Element) damageList.item(0);
			damagePanel.initFromXML(damageListElt);
		}
		
		//min usage
		if ( e.hasAttribute("minUsage") )
			tfMinUsage.setText(e.getAttribute("minUsage"));
		

		//set default values for text fields if they are empty
		setDefaultTextIfEmpty ( tfProbabilitySteepness , "0" );
		setDefaultTextIfEmpty ( tfTimeStarting , "10" );
		setDefaultTextIfEmpty ( tfTimeSteepness , "0" );
		setDefaultTextIfEmpty ( tfRecoverTimeStarting , "20" );
		setDefaultTextIfEmpty ( tfRecoverTimeSteepness , "0" );
		setDefaultTextIfEmpty ( tfMinUsage , "0" );
		
		
	}
	
	private void setDefaultTextIfEmpty ( JTextField textField , String value )
	{
		if ( textField.getText().trim().length() <= 0 )
			textField.setText(value);
	}
	
	
}

