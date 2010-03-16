/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.io.*;
import java.util.*;

import eu.irreality.age.debug.Debug;

public class Wearable extends Item
{


	List /*of dollar-separated String*/ wearableLimbs = new Vector();



	private String defense_damage_formula; //protección que nos da como armadura




	/*
	private static int USAGE_CORRECTOR = 100;

	
	String weapon_type;
	double minskill_twohand;
	double minskill_onehand;
	double maxskill;
	private String attack_damage_formula;
	private String defense_damage_formula;
	//obsolete String damage_type;
	String[] attack_names_list;
	String wieldableLimbs;
	

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
	*/


	public Wearable ( World mundo , String itemfile ) throws IOException, FileNotFoundException
	{
		constructItem ( mundo , itemfile , true , "wearable" );
	}
	
	public Wearable  ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		constructItem ( mundo , n , true , "wearable" );
	}
	
	public List getDamageList ( Mobile defensor )
	{
		return getDamagesListFromFormula ( defense_damage_formula , defensor );
	}
	
	
	//le pasamos el nodo general de la Entity
	public void readWearableSpecifics ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
	
		if ( ! ( n instanceof org.w3c.dom.Element ) )
			throw ( new XMLtoWorldException("Wearable node not Element") );
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;
		
		//{e is an Element representing this Wearable}
			
		//WearableLimbs: adapted for weapons' WieldableLimbs deprecated XML load.
		
		//WearableLimbs is a List of dollar-separated string lists.
		org.w3c.dom.NodeList singRefNamesNodes = e.getElementsByTagName("WearableLimbs" );
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
				
				wearableLimbs.add ( curLimbNames );
				
			}
			
			
		}
		
		//do here armor's properties (defensive power, armor class, etc.) load.
		//MAYBE use the same format as weapons' defense thingies (dice throws, etc.)
	
		org.w3c.dom.NodeList nl = e.getElementsByTagName("DamageList");
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
	
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
		org.w3c.dom.Node nodoItem = super.getXMLRepresentation(doc);
		nodoItem.appendChild ( getWearableSpecificsXMLRepresentation ( doc ) );
		return nodoItem;
	}
	
	public org.w3c.dom.Node getWearableSpecificsXMLRepresentation ( org.w3c.dom.Document doc )
	{

		org.w3c.dom.Element suElemento = doc.createElement("WearableSpecs");
		
		//wieldable limbs
		if ( wearableLimbs != null )
		{
			org.w3c.dom.Element elt = doc.createElement("WearableLimbs");
		
			for ( int i = 0 ; i < wearableLimbs.size() ; i++ )
			{
			
				String curString = (String) wearableLimbs.get(i);
			
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
				suElemento.appendChild(defenseDamageElement);
		}	
		
		return suElemento;
	
	}

	public List getLimbRequirementsList ( )
	{
		return wearableLimbs;
	}



	/**Tres funciones idénticas a las mismas en Weapon:**/
	
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
				tirada += ( Math.abs( rand.nextInt()%cantDado ) + 1 );
				Debug.print("<tir" + tirada + ">");
			} 
			return tirada;
		}
		else
		{
			return Integer.valueOf ( formula ) . intValue() ;
		}
	}




	//notación un poco inconsistente con la de Entity::copyEntityFields; pero bueno
	public void copyWearableFieldsTo ( Wearable w )
	{
	
		copyItemFieldsTo(w); //Entity+Wearable
	
		//scalars
		w.defense_damage_formula = defense_damage_formula;
		
		if ( wearableLimbs != null )
		{
			w.wearableLimbs = new ArrayList();
			for ( int i = 0 ; i < wearableLimbs.size() ; i++ )
			{
				w.wearableLimbs.add ( wearableLimbs.get(i) );
			}
		}
		
	}


	public Object clone( )
	{
		//do it!
		
		Wearable w = new Wearable();
		
		copyWearableFieldsTo(w);
		
		return w;
	}

	//for clones
	public Wearable()
	{
		;
	}


}
