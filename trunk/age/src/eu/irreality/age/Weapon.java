/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.io.*;
import java.util.*;

import eu.irreality.age.debug.Debug;

public class Weapon extends Item
{

	private static int USAGE_CORRECTOR = 100;

	
	String weapon_type;
	double minskill_twohand;
	double minskill_onehand;
	double maxskill;
	private String attack_damage_formula;
	private String defense_damage_formula;
	//obsolete String damage_type;
	String[] attack_names_list;
	
	
	
	//obsolete String wieldableLimbs;
	
	List /*of dollar-separated String*/ wieldableLimbs = new Vector();
	
	

	//modern skill system vars
	
	//String wieldableLimbs: already present (namelist)
	private long attackMinimalUsage;
	private String[] attackInvolvedSkills;
	private double[] attackInvolvedSkillRelevance;
	private double attackProbabilitySteepness;
	private double attackTimeSteepness;
	private double attackStartingTime;
	private double attackRecoverTimeSteepness;
	private double attackRecoverStartingTime;
	
	private long defenseMinimalUsage;
	private String[] defenseInvolvedSkills;
	private double[] defenseInvolvedSkillRelevance;
	private double defenseProbabilitySteepness;
	private double defenseTimeSteepness;
	private double defenseStartingTime;
	private double defenseRecoverTimeSteepness;
	private double defenseRecoverStartingTime;	
	

	public void addDamageBonus(int amt)
	{
		if ( amt > 0 )
			attack_damage_formula = attack_damage_formula + "+" + amt;
		else if ( amt < 0 )
			attack_damage_formula = attack_damage_formula + "-" + amt;
	}


	public Weapon ( World mundo , String itemfile ) throws IOException, FileNotFoundException
	{
		constructItem ( mundo , itemfile , true , "weapon" );
	}
	
	public Weapon  ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		constructItem ( mundo , n , true , "weapon" );
	}
	
	
	//le pasamos el nodo general de la Entity
	public void readWeaponSpecifics ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
	
		if ( ! ( n instanceof org.w3c.dom.Element ) )
			throw ( new XMLtoWorldException("Weapon node not Element") );
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;
		
		//{e is an Element representing this Weapon}
			
		//WieldableLimbs: a dollar-separated names list.
		//XML parsing copy-pasted from parsing of SingularReferenceNames at Item.
		
		
		/*
		org.w3c.dom.NodeList singRefNamesNodes = e.getElementsByTagName("WieldableLimbs" );
		if ( singRefNamesNodes.getLength() > 0 )
		{
			org.w3c.dom.Element singRefNamesNode = (org.w3c.dom.Element)singRefNamesNodes.item(0);
			org.w3c.dom.NodeList nameNodes = singRefNamesNode.getElementsByTagName("Name");
			
			//init wieldableLimbs
			wieldableLimbs = "";
			
			for ( int i = 0 ; i < nameNodes.getLength() ; i++ )
			{
				//get this name node
				org.w3c.dom.Element nameNode = (org.w3c.dom.Element) nameNodes.item(i);
				
				//get first text node in this name node -- WE ASSUME THERE IS ONE!!
				org.w3c.dom.Node hijo = nameNode.getFirstChild();
				while ( !( hijo instanceof org.w3c.dom.Text ) )
					hijo = hijo.getNextSibling();
				
				//{hijo is an org.w3c.dom.Text}
				
				wieldableLimbs += ( hijo.getNodeValue() );			
					
				if ( i < nameNodes.getLength()-1 ) //i.e. not last
					wieldableLimbs += "$";	
			}
		}
		*/
		
			//wieldableLimbs is a List of dollar-separated string lists.
		org.w3c.dom.NodeList singRefNamesNodes = e.getElementsByTagName("WieldableLimbs" );
		if ( singRefNamesNodes.getLength() > 0 )
		{
			org.w3c.dom.Element singRefNamesNode = (org.w3c.dom.Element)singRefNamesNodes.item(0);
			
			org.w3c.dom.NodeList reqNodes = singRefNamesNode.getElementsByTagName("Requirement");
			
			for ( int k = 0 ; k < reqNodes.getLength() ; k++ )
			{
				//get this req node
				org.w3c.dom.Element reqNode = (org.w3c.dom.Element)reqNodes.item(k);
				
				org.w3c.dom.NodeList nameNodes = reqNode.getElementsByTagName("Name");
				
				//init curLimbNames
				String curLimbNames = "";
				
				for ( int i = 0 ; i < nameNodes.getLength() ; i++ )
				{
					//get this name node
					org.w3c.dom.Element nameNode = (org.w3c.dom.Element) nameNodes.item(i);
					
					//get first text node in this name node -- WE ASSUME THERE IS ONE!!
					org.w3c.dom.Node hijo = nameNode.getFirstChild();
					while ( !( hijo instanceof org.w3c.dom.Text ) )
						hijo = hijo.getNextSibling();
					
					//{hijo is an org.w3c.dom.Text}
					
					curLimbNames += ( hijo.getNodeValue() );			
						
					if ( i < nameNodes.getLength()-1 ) //i.e. not last
						curLimbNames += "$";	
				}
				
				wieldableLimbs.add ( curLimbNames );
				
			}
			
			
		}
		
		//attack-related info
		org.w3c.dom.NodeList attackNodes = e.getElementsByTagName("Attack");
		if ( attackNodes.getLength() > 0 )
		{
			org.w3c.dom.Element attackNode = (org.w3c.dom.Element)attackNodes.item(0);
			
			//OK, we have attack node, now parse different sub-nodes.
			
			//attack minimal usage
			if ( attackNode.hasAttribute("minUsage") )
			{
				try
				{
					attackMinimalUsage = Long.valueOf ( attackNode.getAttribute("minUsage") ).longValue();
				}
				catch ( NumberFormatException nfe )
				{
					throw ( new XMLtoWorldException("Number format error at attackMinimalUsage") );
				}
			}
			else
				attackMinimalUsage = 0; //default value	
			
			//attack involved skills
			org.w3c.dom.NodeList nl1 = attackNode.getElementsByTagName("InvolvedSkills");
			if ( nl1.getLength() > 0 )
			{
				org.w3c.dom.Element invsk = (org.w3c.dom.Element) nl1.item(0);
				org.w3c.dom.NodeList skillnodes = invsk.getElementsByTagName("Skill");
				attackInvolvedSkills = new String[skillnodes.getLength()];
				attackInvolvedSkillRelevance = new double [skillnodes.getLength()];
				for ( int i = 0 ; i < skillnodes.getLength() ; i++ )
				{
					org.w3c.dom.Element skillNode = (org.w3c.dom.Element) skillnodes.item(i);
					if ( !skillNode.hasAttribute("name") )
						throw ( new XMLtoWorldException("Skill node lacking attribute name") );
					if ( !skillNode.hasAttribute("relevance") )
						//throw ( new XMLtoWorldException("Skill node lacking attribute relevance") );
						attackInvolvedSkillRelevance[i] = 1.0;
					try
					{
						if ( skillNode.hasAttribute("relevance") )
							attackInvolvedSkillRelevance[i] = Double.valueOf ( skillNode.getAttribute("relevance") ).doubleValue();
						attackInvolvedSkills[i] = skillNode.getAttribute("name");
					}	
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Wrong number format for relevance attribute at weapon's attack skill node") );
					}
					
				}
			}
			
			//attack probability
			org.w3c.dom.NodeList nl = attackNode.getElementsByTagName("Probability");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						attackProbabilitySteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at attackProbabilitySteepness") );
					}
				}
				else
					attackProbabilitySteepness = 0; //default value	
			}
			
			//attack time
			nl = attackNode.getElementsByTagName("Time");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						attackTimeSteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at attackTimeSteepness") );
					}
				}
				else
					attackTimeSteepness = 0; //default value	
				if ( atProb.hasAttribute("starting") )
				{
					try
					{
						attackStartingTime = Double.valueOf ( atProb.getAttribute("starting") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at attackStartingTime") );
					}
				}
				else
					attackStartingTime = 30; //default value	
			}
			
			//attack recover time
			nl = attackNode.getElementsByTagName("RecoverTime");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						attackRecoverTimeSteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at attackRecoverTimeSteepness") );
					}
				}
				else
					attackRecoverTimeSteepness = 0; //default value	
				if ( atProb.hasAttribute("starting") )
				{
					try
					{
						attackRecoverStartingTime = Double.valueOf ( atProb.getAttribute("starting") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at attackRecoverStartingTime") );
					}
				}
				else
					attackRecoverStartingTime = 0; //default value	
			}
			
			//attack damage (formulae)
			nl = attackNode.getElementsByTagName("DamageList");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atDam = (org.w3c.dom.Element) nl.item(0);
				org.w3c.dom.NodeList damageElements = atDam.getElementsByTagName("Damage");
				for ( int i = 0 ; i < damageElements.getLength() ; i++ )
				{
				
					org.w3c.dom.Element curDamageElement = (org.w3c.dom.Element) damageElements.item(i);
					if ( !curDamageElement.hasAttribute("type") )
						throw ( new XMLtoWorldException("Damage element lacking type attribute") );
					if ( !curDamageElement.hasAttribute("formula") )
						throw ( new XMLtoWorldException("Damage element lacking formula attribute") );
					if ( attack_damage_formula == null )
						attack_damage_formula = "";
					//append this damtype
					if ( i > 0 )
						attack_damage_formula += "$";
					attack_damage_formula += curDamageElement.getAttribute("type");
					attack_damage_formula += ":";
					attack_damage_formula += curDamageElement.getAttribute("formula");					
				}
			}		
			
		}
				
		
		//defense-related info
		//copy-pasted from attack-related info replacing attack by defense
		org.w3c.dom.NodeList defenseNodes = e.getElementsByTagName("Defense");
		if ( defenseNodes.getLength() > 0 )
		{
			org.w3c.dom.Element defenseNode = (org.w3c.dom.Element)defenseNodes.item(0);
			
			//OK, we have defense node, now parse different sub-nodes.
			
			//defense minimal usage
			if ( defenseNode.hasAttribute("minUsage") )
			{
				try
				{
					defenseMinimalUsage = Long.valueOf ( defenseNode.getAttribute("minUsage") ).longValue();
				}
				catch ( NumberFormatException nfe )
				{
					throw ( new XMLtoWorldException("Number format error at defenseMinimalUsage") );
				}
			}
			else
				defenseMinimalUsage = 0; //default value	
			
			//defense involved skills
			org.w3c.dom.NodeList nl1 = defenseNode.getElementsByTagName("InvolvedSkills");
			if ( nl1.getLength() > 0 )
			{
				org.w3c.dom.Element invsk = (org.w3c.dom.Element) nl1.item(0);
				org.w3c.dom.NodeList skillnodes = invsk.getElementsByTagName("Skill");
				defenseInvolvedSkills = new String[skillnodes.getLength()];
				defenseInvolvedSkillRelevance = new double [skillnodes.getLength()];
				for ( int i = 0 ; i < skillnodes.getLength() ; i++ )
				{
					org.w3c.dom.Element skillNode = (org.w3c.dom.Element) skillnodes.item(i);
					if ( !skillNode.hasAttribute("name") )
						throw ( new XMLtoWorldException("Skill node lacking attribute name") );
					if ( !skillNode.hasAttribute("relevance") )
						//throw ( new XMLtoWorldException("Skill node lacking attribute relevance") );
						defenseInvolvedSkillRelevance[i] = 1.0;
					try
					{
						if ( skillNode.hasAttribute("relevance") )
							defenseInvolvedSkillRelevance[i] = Double.valueOf ( skillNode.getAttribute("relevance") ).doubleValue();
						defenseInvolvedSkills[i] = skillNode.getAttribute("name");
					}	
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Wrong number format for relevance attribute at weapon's defense skill node") );
					}
					
				}
			}
			
			//defense probability
			org.w3c.dom.NodeList nl = defenseNode.getElementsByTagName("Probability");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						defenseProbabilitySteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at defenseProbabilitySteepness") );
					}
				}
				else
					defenseProbabilitySteepness = 0; //default value	
			}
			
			//defense time
			nl = defenseNode.getElementsByTagName("Time");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						defenseTimeSteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at defenseTimeSteepness") );
					}
				}
				else
					defenseTimeSteepness = 0; //default value	
				if ( atProb.hasAttribute("starting") )
				{
					try
					{
						defenseStartingTime = Double.valueOf ( atProb.getAttribute("starting") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at defenseStartingTime") );
					}
				}
				else
					defenseStartingTime = 0; //default value	
			}
			
			//defense recover time
			nl = defenseNode.getElementsByTagName("RecoverTime");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						defenseRecoverTimeSteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at defenseRecoverTimeSteepness") );
					}
				}
				else
					defenseRecoverTimeSteepness = 0; //default value	
				if ( atProb.hasAttribute("starting") )
				{
					try
					{
						defenseRecoverStartingTime = Double.valueOf ( atProb.getAttribute("starting") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at defenseRecoverStartingTime") );
					}
				}
				else
					defenseRecoverStartingTime = 0; //default value	
			}
			
			//defense damage (formulae)
			nl = defenseNode.getElementsByTagName("DamageList");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atDam = (org.w3c.dom.Element) nl.item(0);
				org.w3c.dom.NodeList damageElements = atDam.getElementsByTagName("Damage");
				for ( int i = 0 ; i < damageElements.getLength() ; i++ )
				{
				
					org.w3c.dom.Element curDamageElement = (org.w3c.dom.Element) damageElements.item(i);
					if ( !curDamageElement.hasAttribute("type") )
						throw ( new XMLtoWorldException("Damage element lacking type attribute") );
					if ( !curDamageElement.hasAttribute("formula") )
						throw ( new XMLtoWorldException("Damage element lacking formula attribute") );
					if ( defense_damage_formula == null )
						defense_damage_formula = "";
					//append this damtype
					if ( i > 0 )
						defense_damage_formula += "$";
					defense_damage_formula += curDamageElement.getAttribute("type");
					defense_damage_formula += ":";
					defense_damage_formula += curDamageElement.getAttribute("formula");					
				}
			}
			
		}
		
		//parsing and initting done.
	
	}
	
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
		org.w3c.dom.Node nodoItem = super.getXMLRepresentation(doc);
		nodoItem.appendChild ( getWeaponSpecificsXMLRepresentation ( doc ) );
		return nodoItem;
	}
	
	public org.w3c.dom.Node getWeaponSpecificsXMLRepresentation ( org.w3c.dom.Document doc )
	{

		org.w3c.dom.Element suElemento = doc.createElement("WeaponSpecs");
		
		/*
		//wieldable limbs
		if ( wieldableLimbs != null )
		{
			org.w3c.dom.Element elt = doc.createElement("WieldableLimbs");
			StringTokenizer st = new StringTokenizer ( wieldableLimbs , "$" );
			while ( st.hasMoreTokens() )
			{
				String tok = st.nextToken();
				org.w3c.dom.Element esteNombre = doc.createElement("Name");
				org.w3c.dom.Text elNombre = doc.createTextNode(tok);
				esteNombre.appendChild(elNombre);
				elt.appendChild(esteNombre);
			}
			suElemento.appendChild( elt );
		}
		*/
		//wieldable limbs
		if ( wieldableLimbs != null )
		{
			org.w3c.dom.Element elt = doc.createElement("WieldableLimbs");
		
			for ( int i = 0 ; i < wieldableLimbs.size() ; i++ )
			{
			
				String curString = (String) wieldableLimbs.get(i);
			
				org.w3c.dom.Element reqNode = doc.createElement("Requirement");
			
				StringTokenizer st = new StringTokenizer ( curString , "$" );
				while ( st.hasMoreTokens() )
				{
					String tok = st.nextToken();
					org.w3c.dom.Element esteNombre = doc.createElement("Name");
					org.w3c.dom.Text elNombre = doc.createTextNode(tok);
					esteNombre.appendChild(elNombre);
					reqNode.appendChild(esteNombre);
				}
				elt.appendChild( reqNode );
		
			}
			
			suElemento.appendChild(elt);
		
		}
		
		//attack
		
		org.w3c.dom.Element attackElement = doc.createElement("Attack");
		{
			attackElement.setAttribute("minUsage" , String.valueOf(attackMinimalUsage));
			org.w3c.dom.Element attackSkillsElement = doc.createElement("InvolvedSkills");
			{
				if ( attackInvolvedSkills != null )
				{
					for ( int i = 0 ; i < attackInvolvedSkills.length ; i++ )
					{
						org.w3c.dom.Element thisSkillElement = doc.createElement("Skill");
						thisSkillElement.setAttribute("name",attackInvolvedSkills[i]);
						thisSkillElement.setAttribute("relevance",String.valueOf(attackInvolvedSkillRelevance[i]));
						attackSkillsElement.appendChild(thisSkillElement);
					}
				}
				attackElement.appendChild(attackSkillsElement);
			}
			org.w3c.dom.Element attackProbabilityElement = doc.createElement("Probability");
			{
				attackProbabilityElement.setAttribute("steepness",String.valueOf(attackProbabilitySteepness));
				attackElement.appendChild(attackProbabilityElement);
			}
			org.w3c.dom.Element attackTimeElement = doc.createElement("Time");
			{
				attackTimeElement.setAttribute("steepness",String.valueOf(attackTimeSteepness));
				attackTimeElement.setAttribute("starting",String.valueOf(attackStartingTime));
				attackElement.appendChild(attackTimeElement);
			}
			org.w3c.dom.Element attackRecoverTimeElement = doc.createElement("RecoverTime");
			{
				attackRecoverTimeElement.setAttribute("steepness",String.valueOf(attackRecoverTimeSteepness));
				attackRecoverTimeElement.setAttribute("starting",String.valueOf(attackRecoverStartingTime));
				attackElement.appendChild(attackRecoverTimeElement);
			}
			org.w3c.dom.Element attackDamageElement = doc.createElement("DamageList");
			{
				if ( attack_damage_formula != null )
				{
					StringTokenizer st = new StringTokenizer(attack_damage_formula,"$");
					while ( st.hasMoreTokens() )
					{
						String curToken = st.nextToken();
						StringTokenizer st2 = new StringTokenizer ( curToken , ":" ); //separa el tipo de daño del daño
						
						String damTypeString = st2.nextToken().trim();
						String damFormulaString = st2.nextToken().trim();
						
						org.w3c.dom.Element curDamageElement = doc.createElement("Damage");
						curDamageElement.setAttribute("type",damTypeString);
						curDamageElement.setAttribute("formula",damFormulaString);
						attackDamageElement.appendChild(curDamageElement);
					}
				}
				attackElement.appendChild(attackDamageElement);
			}				
				
			suElemento.appendChild(attackElement);
		}
		
		//defense
		
		org.w3c.dom.Element defenseElement = doc.createElement("Defense");
		{
			defenseElement.setAttribute("minUsage" , String.valueOf(defenseMinimalUsage));
			org.w3c.dom.Element defenseSkillsElement = doc.createElement("InvolvedSkills");
			{
				if ( defenseInvolvedSkills != null )
				{
					for ( int i = 0 ; i < defenseInvolvedSkills.length ; i++ )
					{
						org.w3c.dom.Element thisSkillElement = doc.createElement("Skill");
						thisSkillElement.setAttribute("name",defenseInvolvedSkills[i]);
						thisSkillElement.setAttribute("relevance",String.valueOf(defenseInvolvedSkillRelevance[i]));
						defenseSkillsElement.appendChild(thisSkillElement);
					}
				}
				defenseElement.appendChild(defenseSkillsElement);
			}
			org.w3c.dom.Element defenseProbabilityElement = doc.createElement("Probability");
			{
				defenseProbabilityElement.setAttribute("steepness",String.valueOf(defenseProbabilitySteepness));
				defenseElement.appendChild(defenseProbabilityElement);
			}
			org.w3c.dom.Element defenseTimeElement = doc.createElement("Time");
			{
				defenseTimeElement.setAttribute("steepness",String.valueOf(defenseTimeSteepness));
				defenseTimeElement.setAttribute("starting",String.valueOf(defenseStartingTime));
				defenseElement.appendChild(defenseTimeElement);
			}
			org.w3c.dom.Element defenseRecoverTimeElement = doc.createElement("RecoverTime");
			{
				defenseRecoverTimeElement.setAttribute("steepness",String.valueOf(defenseRecoverTimeSteepness));
				defenseRecoverTimeElement.setAttribute("starting",String.valueOf(defenseRecoverStartingTime));
				defenseElement.appendChild(defenseRecoverTimeElement);
			}
			org.w3c.dom.Element defenseDamageElement = doc.createElement("DamageList");
			{
				if ( defense_damage_formula != null )
				{
					StringTokenizer st = new StringTokenizer(defense_damage_formula,"$");
					while ( st.hasMoreTokens() )
					{
						String curToken = st.nextToken();
						StringTokenizer st2 = new StringTokenizer ( curToken , ":" ); //separa el tipo de daño del daño
						
						String damTypeString = st2.nextToken().trim();
						String damFormulaString = st2.nextToken().trim();
						
						org.w3c.dom.Element curDamageElement = doc.createElement("Damage");
						curDamageElement.setAttribute("type",damTypeString);
						curDamageElement.setAttribute("formula",damFormulaString);
						defenseDamageElement.appendChild(curDamageElement);
					}
				}
				defenseElement.appendChild(defenseDamageElement);
			}				
				
			
			suElemento.appendChild(defenseElement);
		}
		
		return suElemento;
	
	}
	
	public void readWeaponSpecifics ( World mundo , String itemfile ) throws IOException, FileNotFoundException
	{

		String linea;
		String id_linea;
		FileInputStream fp = new FileInputStream ( itemfile );
		BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( fp ) );
		for ( int line = 1 ; line < 100 ; line++ )
		{
			linea = filein.readLine();
			id_linea = StringMethods.getTok( linea , 1 , ' ' );
			linea = StringMethods.getToks( linea , 2 , StringMethods.numToks( linea , ' ' ) , ' ' );
			if ( id_linea != null ) switch ( Integer.valueOf(id_linea).intValue() )
			{	
				case 50:
					break;
				case 51:
					minskill_twohand = Double.valueOf(linea).doubleValue();
					break;	
				case 52:
					minskill_onehand = Double.valueOf(linea).doubleValue();
					break;		
				case 53:
					maxskill = Double.valueOf(linea).doubleValue();
					break;
				case 54:
					attack_damage_formula = linea;
					break;
				case 55:
					//obsolete damage_type = linea;
					break;
				case 60:
					break;	
				case 61:
					//wieldable limbs list line
					wieldableLimbs = new Vector();
					wieldableLimbs.add(  linea  );
					break;
			}
		}
	}
	
	//el valor de retorno es el daño hecho
	public int dealDamage ( Mobile atacante , Mobile defensor , boolean simulated )
	{
		List listaDanos = getDamagesListFromFormula ( attack_damage_formula , atacante );
		int danoTotal = 0;
		for ( int i = 0 ; i < listaDanos.size() ; i++ )
		{
			danoTotal += defensor.tryToDealDamage ( i , ((Integer)listaDanos.get(i)).intValue() , simulated );
		}
		return danoTotal;
	}
	
	public int dealDamageDefended ( Mobile atacante , Mobile defensor , boolean simulated )
	{
		List listaDanosAtaque = getDamagesListFromFormula ( attack_damage_formula , atacante );
		List listaDanosDefensa = getDamagesListFromFormula ( defensor.getCurrentWeapon().defense_damage_formula , defensor );
		int danoTotal = 0;
		for ( int i = 0 ; i < listaDanosAtaque.size() ; i++ )
		{
			int danoAHacer;
			if ( i < listaDanosDefensa.size() )
				danoAHacer = ((Integer)listaDanosAtaque.get(i)).intValue() - ((Integer)listaDanosDefensa.get(i)).intValue(); 
			else
				danoAHacer = ((Integer)listaDanosAtaque.get(i)).intValue();
			if ( danoAHacer < 0 ) danoAHacer = 0;
			danoTotal += defensor.tryToDealDamage ( i , danoAHacer , simulated );
		}
		return danoTotal;
	}
	
	public int dealDamage ( Mobile atacante , Mobile defensor , boolean simulated , /*nullable*/ Item limb )
	{
	
		Debug.println("Dealing damage with " + this + attack_damage_formula);
		List listaDanos = getDamagesListFromFormula ( attack_damage_formula , atacante );
		Debug.println(""+listaDanos);
		return defensor.tryToDealDamage ( listaDanos , simulated , limb );
	}
	
	
	public int dealDamageDefended ( Mobile atacante , Mobile defensor , boolean simulated , /*nullable*/ Item limb )
	{
		List listaDanosAtaque = getDamagesListFromFormula ( attack_damage_formula , atacante );
		List listaDanosDefensa = getDamagesListFromFormula ( defensor.getCurrentWeapon().defense_damage_formula , defensor );
		List listaDanosTotal = new ArrayList();
		for ( int i = 0 ; i < listaDanosAtaque.size() ; i++ )
		{
			int danoAHacer;
			if ( i < listaDanosDefensa.size() )
				danoAHacer = ((Integer)listaDanosAtaque.get(i)).intValue() - ((Integer)listaDanosDefensa.get(i)).intValue(); 
			else
				danoAHacer = ((Integer)listaDanosAtaque.get(i)).intValue();
			if ( danoAHacer < 0 ) danoAHacer = 0;
			listaDanosTotal.add(new Integer(danoAHacer));
		}
		return defensor.tryToDealDamage ( listaDanosTotal , simulated , limb );
	}
	
	
	//lista de daños según tipo (físico, fuego...)
	public List getDamagesListFromFormula ( String damage_formula , Mobile atacanteODefensor )
	{
		List listaDanos = new ArrayList();
		
		String ourFormula = getDamageFromFormula ( damage_formula , atacanteODefensor );
		
		StringTokenizer st = new StringTokenizer ( ourFormula , "$" );
		while ( st.hasMoreTokens ( ) )
		{
			String curToken = st.nextToken();
			StringTokenizer st2 = new StringTokenizer ( curToken , ":" ); //separa el tipo de daño del daño
			int damtype = Integer.valueOf( st2.nextToken().trim() ).intValue();  
			int damamount = Integer.valueOf( st2.nextToken().trim() ).intValue();
			
			while ( listaDanos.size() <= damtype )
				listaDanos.add(null);
			
			listaDanos.set(damtype, new Integer(damamount));
		}
		//cambiar nulos por ceros
		for ( int i = 0 ; i < listaDanos.size() ; i++ )
		{
			if ( listaDanos.get(i) == null ) 
				listaDanos.set( i , new Integer(0) );
		}
		return listaDanos;
	}
	
	public String getDamageFromFormula ( String damage_formula , Mobile atacanteODefensor )
	{
	
		//Debug.println("Formula is : " + damage_formula);
		
	
		//coge: damage_formula de esta instancia
		//formula del tipo:
		// 0: 3D6 - 2 + 3FUE + 0N4 $ 1: 2D6
		//devuelve: daño
		//formula del tipo:
		// 0: 24 $ 1: 5
		//dependera del bicho atacanteODefensor (parametros como FUE, etc.)
		
		
		String result = "";
		
		if ( damage_formula == null ) return "";
		
		StringTokenizer st = new StringTokenizer ( damage_formula , "$" );
		
		while ( st.hasMoreTokens ( ) )
		{
			String curToken = st.nextToken();
			StringTokenizer st2 = new StringTokenizer ( curToken , ":" ); //separa el tipo de daño del daño
			result += st2.nextToken().trim(); //tipo de daño
			result += ":";
			
			String laformula = st2.nextToken().trim(); //formula que indica el daño de ese tipo
			
			//calcular el numero que resulta de la formula y concatenarlo
				StringTokenizer stFormula = new StringTokenizer ( laformula , "+-" , true ); //true para que devuelva tambien los delimitadores
				boolean sumaOResta = true; //suma
				int elResultado = 0; //empieza en la suma vacia, formula es suma de cosas.
				while ( stFormula.hasMoreTokens ( ) )
				{
					String curFormulaToken = stFormula.nextToken().trim();
					if ( curFormulaToken.equals("+") ) sumaOResta = true;
					else if ( curFormulaToken.equals("-") ) sumaOResta = false;
					else //sumar o restar el resultado de este token al numero
					{
						int sumando = processAtomicFormula ( curFormulaToken , atacanteODefensor );
						if ( sumaOResta ) elResultado += sumando;
						else elResultado -= sumando;
					}
				} 
				result += String.valueOf(elResultado);
			
			
			result += "$";
		}
		
		//quitar el dólar que se añadió de más, si string no "".
			if ( result.length() > 0 )
				result = result.substring ( 0 , result.length() - 1 );
		
		Debug.println(getTitle() + " drawing damage from " + damage_formula + " resulted " + result);
		
		
		return result;
	}
	
	private int processAtomicFormula ( String formula , Mobile atacanteODefensor )
	{
		//auxiliar para getDamageFromFormula
		//procesa una formula de daño elemental, ej. 4D6FUE o 4
		if ( 
				formula.endsWith("FUE") || formula.endsWith("STR") 
				|| formula.endsWith("CON") 
				|| formula.endsWith("INT") 
				|| formula.endsWith("SAB") || formula.endsWith("WIS")  
				|| formula.endsWith("DES") || formula.endsWith("DEX")  
				|| formula.endsWith("CHA") || formula.endsWith("CAR") 
				|| formula.endsWith("POD") || formula.endsWith("POW") 
		)
		{
			return processAtomicFormula(formula.substring(0,formula.length()-3), atacanteODefensor ) * atacanteODefensor.getStat(formula.substring(formula.length()-3,formula.length())) ;
		}
		else if ( formula.equals( "" ) )
		{
			return 0;
		}
		else if ( StringMethods.numToks ( formula , 'D' ) > 1 )
		{
			Random rand = getRandom();
			int numDados = Integer.valueOf (  StringMethods.getTok ( formula, 1, 'D' )  ) . intValue() ;
			int cantDado = Integer.valueOf (  StringMethods.getTok ( formula, 2, 'D' )  ) . intValue() ;
			int tirada = 0;
			Debug.print("-ndados" + numDados + "-");
			for ( int i = 0 ; i < numDados ; i++ )
			{
				//Debug.println("I am " + this + "[IID:"+getID()+"][INAME"+title+"], and my Random is actually " + getRandom());
				int temp = rand.nextInt();
				tirada += ( Math.abs( temp%cantDado ) + 1 );
				Debug.print("<tir" + tirada + ">");
			} 
			return tirada;
		}
		else
		{
			return Integer.valueOf ( formula ) . intValue() ;
		}
	}
	
	//typical según skill (variará further según su destreza y el factor aleatorio)
	public double getTypicalAttackTime ( long usage )
	{
		return getTypicalTime ( usage , attackStartingTime , attackMinimalUsage , attackTimeSteepness );
	}
	public double getTypicalAttackRecoverTime ( long usage )
	{
		return getTypicalTime ( usage , attackRecoverStartingTime , attackMinimalUsage , attackRecoverTimeSteepness );
	}
	public double getTypicalAttackProbability ( long usage )
	{
		return 1 - getTypicalTime ( usage , 1.0 , attackMinimalUsage , attackProbabilitySteepness );
	}
	
	public double getTypicalDefenseTime ( long usage )
	{
		return getTypicalTime ( usage , defenseStartingTime , defenseMinimalUsage , defenseTimeSteepness );
	}
	public double getTypicalDefenseRecoverTime ( long usage )
	{
		return getTypicalTime ( usage , defenseRecoverStartingTime , defenseMinimalUsage , defenseRecoverTimeSteepness );
	}
	public double getTypicalDefenseProbability ( long usage )
	{
		return 1 - getTypicalTime ( usage , 1.0 , defenseMinimalUsage , defenseProbabilitySteepness );
	}



	public double getTypicalAttackTime ( Mobile m )
	{
		return getTypicalTime ( getAttackUsage(m) , attackStartingTime , attackMinimalUsage , attackTimeSteepness );
	}
	public double getTypicalAttackRecoverTime ( Mobile m )
	{
		return getTypicalTime ( getAttackUsage(m) , attackRecoverStartingTime , attackMinimalUsage , attackRecoverTimeSteepness );
	}
	public double getTypicalAttackProbability ( Mobile m )
	{
		return 1 - getTypicalTime ( getAttackUsage(m) , 1.0 , attackMinimalUsage , attackProbabilitySteepness );
	}
	
	public double getTypicalDefenseTime ( Mobile m )
	{
		return getTypicalTime ( getDefenseUsage(m) , defenseStartingTime , defenseMinimalUsage , defenseTimeSteepness );
	}
	public double getTypicalDefenseRecoverTime ( Mobile m )
	{
		return getTypicalTime ( getDefenseUsage(m)  , defenseRecoverStartingTime , defenseMinimalUsage , defenseRecoverTimeSteepness );
	}
	public double getTypicalDefenseProbability ( Mobile m )
	{
		return 1 - getTypicalTime ( getDefenseUsage(m)  , 1.0 , defenseMinimalUsage , defenseProbabilitySteepness );
	}



	
	//typical según skill (variará further según su destreza y el factor aleatorio)
	private double getTypicalTime ( long usage , double starting , long minusage , double steepness )
	{
	
		if ( usage < minusage ) return 0;
		else
			return
			( 
				(double)starting   /   
					( 
						Math.pow 
						( 
							( 
								(double)usage - (double)minusage
							) 
							/ 
							(double)USAGE_CORRECTOR 
							+ 1
						 , 
						 	( 
								Math.exp(steepness) 
							)
						) 
					)
			 );
	
	}
	

	//uses involved skills and relevance to draw this weapon's specific usage skill.
	public long getAttackUsage ( Mobile m )
	{
	
		double theUsage = 0;
	
		for ( int i = 0 ; i < attackInvolvedSkills.length ; i++ )
		{
			theUsage += attackInvolvedSkillRelevance[i] * m.getSkill(attackInvolvedSkills[i]);
		}
	
		return (long)theUsage;	
	
	}
	
	//uses involved skills and relevance to draw this weapon's specific usage skill.
	public long getDefenseUsage ( Mobile m )
	{
	
		double theUsage = 0;
	
		for ( int i = 0 ; i < defenseInvolvedSkills.length ; i++ )
		{
			theUsage += defenseInvolvedSkillRelevance[i] * m.getSkill(defenseInvolvedSkills[i]);
		}
	
		return (long)theUsage;	
	
	}
	
	
	public void incrementAttackUsage ( Mobile m )
	{
		
		for ( int i = 0 ; i < attackInvolvedSkills.length ; i++ )
		{
			if ( m.getRandom().nextDouble() < attackInvolvedSkillRelevance[i] )
				m.incSkill ( attackInvolvedSkills[i] );
		}
		
	}
		
	public void incrementDefenseUsage ( Mobile m )
	{
		
		for ( int i = 0 ; i < defenseInvolvedSkills.length ; i++ )
		{
			if ( m.getRandom().nextDouble() < defenseInvolvedSkillRelevance[i] )
				m.incSkill ( defenseInvolvedSkills[i] );
		}
		
	}
	
	//de momento, a la espera de generalizar a armas de dos manos y que haya auténtica lista de requerimientos
	//Generalizado.
	public List getLimbRequirementsList()
	{
		/*
		Vector temp = new Vector();
		temp.add ( wieldableLimbs );
		return temp;
		*/
		return wieldableLimbs;
	}









	//notación un poco inconsistente con la de Entity::copyEntityFields; pero bueno
	public void copyWeaponFieldsTo ( Weapon w )
	{
	
		copyItemFieldsTo(w); //Entity+Weapon
	
		//scalars
		w.attack_damage_formula = attack_damage_formula;
		w.attackMinimalUsage = attackMinimalUsage;
		w.attackProbabilitySteepness = attackProbabilitySteepness;
		w.attackRecoverStartingTime = attackRecoverStartingTime;
		w.attackRecoverTimeSteepness = attackRecoverTimeSteepness;
		w.attackStartingTime = attackStartingTime;
		w.attackTimeSteepness = attackTimeSteepness;
		w.defense_damage_formula = defense_damage_formula;
		w.defenseMinimalUsage = defenseMinimalUsage;
		w.defenseProbabilitySteepness = defenseProbabilitySteepness;
		w.defenseRecoverStartingTime = defenseRecoverStartingTime;
		w.defenseStartingTime = defenseStartingTime;
		w.defenseTimeSteepness = defenseTimeSteepness;
		w.maxskill = maxskill;
		w.minskill_onehand = minskill_onehand;
		w.minskill_twohand = minskill_twohand;
		w.weapon_type = weapon_type;
		
		if ( attack_names_list != null )
		{
			w.attack_names_list = new String[ attack_names_list.length ];
			for ( int i = 0 ; i < w.attack_names_list.length ; i++ )
			{
				w.attack_names_list[i] = attack_names_list[i];
			}
		}
		
		if ( attackInvolvedSkillRelevance != null )
		{
			w.attackInvolvedSkillRelevance = new double[ attackInvolvedSkillRelevance.length ];
			for ( int i = 0 ; i < w.attackInvolvedSkillRelevance.length ; i++ )
			{
				w.attackInvolvedSkillRelevance[i] = attackInvolvedSkillRelevance[i];
			}
		}
		
		if ( attackInvolvedSkills != null )
		{
			w.attackInvolvedSkills = new String[ attackInvolvedSkills.length ];
			for ( int i = 0 ; i < w.attackInvolvedSkills.length ; i++ )
			{
				w.attackInvolvedSkills[i] = attackInvolvedSkills[i];
			}
		}
		
		if ( defenseInvolvedSkillRelevance != null )
		{
			w.defenseInvolvedSkillRelevance = new double[ defenseInvolvedSkillRelevance.length ];
			for ( int i = 0 ; i < w.defenseInvolvedSkillRelevance.length ; i++ )
			{
				w.defenseInvolvedSkillRelevance[i] = defenseInvolvedSkillRelevance[i];
			}
		}
		
		if ( defenseInvolvedSkills != null )
		{
			w.defenseInvolvedSkills = new String[ defenseInvolvedSkills.length ];
			for ( int i = 0 ; i < w.defenseInvolvedSkills.length ; i++ )
			{
				w.defenseInvolvedSkills[i] = defenseInvolvedSkills[i];
			}
		}
		
		if ( wieldableLimbs != null )
		{
			w.wieldableLimbs = new ArrayList();
			for ( int i = 0 ; i < wieldableLimbs.size() ; i++ )
			{
				w.wieldableLimbs.add ( wieldableLimbs.get(i) );
			}
		}
		
	}


	public Object clone( )
	{
		//do it!
		
		Weapon w = new Weapon();
		
		copyWeaponFieldsTo(w);
		
		return w;
	}

	//for clones
	public Weapon()
	{
		;
	}


}
