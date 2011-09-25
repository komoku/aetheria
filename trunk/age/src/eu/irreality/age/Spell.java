/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;

import eu.irreality.age.debug.Debug;


public class Spell extends Entity implements SupportingCode, UniqueNamed
{


	/*00*/ private String spellType;

	/**ID del objeto.*/
	/*01*/ private int idnumber;
	
	/**Hereda de.*/
	/*02*/ private int inheritsFrom;
	
	/*03*/ // inherited protected int state;
	// inherited protected long timeunitsleft;
	
	/**Nombre sintético del objeto.*/
	/*04*/ protected String title;
	
	/**Es instancia de.*/
	/*05*/ private int isInstanceOf;


	/**Código en Ensamblador Virtual Aetheria (EVA)*/
	/*80*/ protected ObjectCode itsCode;
	
	
	
	//generador de numeros aleatorios
	private Random aleat;
	
	
	
	
	
	
			long minimalUsage;
			
			String[] involvedSkills;
			double[] involvedSkillRelevance;
			
			double castProbabilitySteepness;
			
			double castStartingTime;
			double castTimeSteepness;
			
			double durationStartingTime;
			double durationTimeSteepness;
			
			double manaCostSteepness;
			double startingManaCost;
			
			double intensitySteepness;
			double startingIntensity;
	
	
			EffectList effects;
			List intensities; //lista paralela de intensidades de efectos
	
	
		private static int USAGE_CORRECTOR = 100;
	
	
	
	public void cast ( Entity caster , Entity target )
	{
		boolean ejecutado = false;
		try
		{
			ejecutado = execCode( "beforeCast" , new Object[] { caster , target  } );
		}
		catch (bsh.TargetError bshte)
		{
			//escribir("bsh.TargetError found at fail routine" );
			;
		}
		if ( ejecutado ) return;
		if ( effects != null )
		{
			for ( int i = 0 ; i < effects.size() ; i++ )
			{
				Effect ef = (Effect) effects.elementAt(i);
				ef.enable ( caster , target , ((Integer)intensities.get(i)).intValue() , (int)getTypicalDuration((Mobile)caster) );
			}
		}	
		try
		{
			ejecutado = execCode( "afterCast" , new Object[] { caster , target  } );
		}
		catch (bsh.TargetError bshte)
		{
			//escribir("bsh.TargetError found at fail routine" );
			;
		}	
	}
	
	public void fail ( Entity caster , Entity target )
	{
		boolean ejecutado = false;
		try
		{
			ejecutado = execCode( "beforeFail" , new Object[] { caster , target  } );
		}
		catch (bsh.TargetError bshte)
		{
			//escribir("bsh.TargetError found at fail routine" );
			;
		}
		if ( ejecutado ) return;
		if ( effects != null )
		{
			for ( int i = 0 ; i < effects.size() ; i++ )
			{
				Effect ef = (Effect) effects.elementAt(i);
				ef.fail ( caster , target , ((Integer)intensities.get(i)).intValue() );
			}
		}	
		try
		{
			ejecutado = execCode( "afterFail" , new Object[] { caster , target  } );
		}
		catch (bsh.TargetError bshte)
		{
			//escribir("bsh.TargetError found at fail routine" );
			;
		}	
	}
	
	
	
	//copypasted from Weapon class
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
	
	
	
	//uses involved skills and relevance to draw this spell's specific usage skill.
	public long getUsage ( Mobile m )
	{
		double theUsage = 0;
		for ( int i = 0 ; i < involvedSkills.length ; i++ )
		{
			Debug.println("Skill: " + involvedSkills[i]);
			Debug.println("Relevance: " + involvedSkillRelevance[i]);
			Debug.println("Mobile: " + m);
			Debug.println("Traits: " + m.caracteristicas);
			Debug.println("Skill for mobile: " + m.getSkill(involvedSkills[i]));
			theUsage += involvedSkillRelevance[i] * m.getSkill(involvedSkills[i]);
		}
		Debug.println("Usage: " + theUsage);
		return (long)theUsage;	
	}
	
	
	//casting time
	//typical steepness is 1
	public double getTypicalCastTime ( Mobile m )
	{
		return getTypicalTime ( getUsage(m) , castStartingTime , minimalUsage , castTimeSteepness );
	}
	
	//casting success probability
	//typical steepness is 0
	public double getTypicalCastProbability ( Mobile m )
	{
		return 1 - getTypicalTime ( getUsage(m) , 1.0 , minimalUsage , castProbabilitySteepness );
	}
	
	//casting mana cost
	//typical steepness is 1
	public double getTypicalManaCost ( Mobile m )
	{
		return getTypicalTime ( getUsage(m) , startingManaCost , minimalUsage , manaCostSteepness );
	}
	
	//casting duration
	//inverted law!
	public double getTypicalDuration ( Mobile m )
	{
		Debug.println("STARTING: " + durationStartingTime);
		if ( durationStartingTime == 0.0 )
		{
			Debug.println("RET 0");
			return 0.0;
		}
		else
		{
			Debug.println("RET 1.0/"+getTypicalTime ( getUsage(m) , 1.0/durationStartingTime , minimalUsage , durationTimeSteepness ));
			return 1.0/getTypicalTime ( getUsage(m) , 1.0/durationStartingTime , minimalUsage , durationTimeSteepness );
		}
	}
	
	//casting intensity
	//inverted law!
	public double getTypicalIntensity ( Mobile m )
	{
		if ( startingIntensity == 0.0 )
			return 0.0;
		else	
			return 1.0/getTypicalTime ( getUsage(m) , 1.0/startingIntensity , minimalUsage , intensitySteepness );
	}

	
	

	public int getID ( )
	{
		return idnumber;	
	}
	
	/**
	 * @deprecated Use {@link #getUniqueName()} instead
	 */
	public String getTitle ( )
	{
		return getUniqueName();
	}

	public String getUniqueName ( )
	{
		return title;	
	}

	/*ejecuta el codigo bsh del objeto correspondiente a la rutina dada si existe.
	Si no existe, simplemente no ejecuta nada y devuelve false.*/
	public boolean execCode ( String routine , Object[] args ) throws bsh.TargetError 
	{
		if ( itsCode != null )
			return itsCode.run ( routine , this , args );
		else return false;
	}
	
	/*ejecuta el codigo bsh del objeto correspondiente a la rutina dada si existe.
	Si no existe, simplemente no ejecuta nada y devuelve false.*/
	public boolean execCode ( String routine , Object[] args , ReturnValue retval ) throws bsh.TargetError 
	{
		//S/ystem.out.println("Mobile code runnin'.");
		//Debug.println("Its Code: " + itsCode);
		if ( itsCode != null )
			return itsCode.run ( routine , this , args , retval );
		else return false;
	}

	//legacy change-state
	//[old!!]
	public void changeState ( World mundo ) 
	{ 
		;
		/*
		try
		{
			execCode("event_endstate","this: "+ getID() + "state: " + getState() );
		}
		catch ( EVASemanticException exc ) 
		{
			mundo.escribir("EVASemanticException found at event_endstate , item number " + getID() );
		}
		*/
	}
	
	//important
	//[define event here!! and do it for other entity types, too!]
	public /*abstract*/ boolean update ( PropertyEntry pe , World mundo )
	{
		return true;
	}
	
	public void setID ( int newid )
	{
		if ( newid < Utility.spell_summand )
			idnumber = newid + Utility.spell_summand;
		else
			idnumber = newid;
	}

	public void loadNumberGenerator ( World mundo )
	{
		aleat = mundo.getRandom();
	}
	
	public java.util.Random getRandom()
	{
		return aleat;
	}
	
	
	
	
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
	
		org.w3c.dom.Element suElemento = doc.createElement( "Spell" );
		
		suElemento.setAttribute ( "id" , String.valueOf( idnumber ) );
		suElemento.setAttribute ( "name" , String.valueOf ( title ) );
		suElemento.setAttribute ( "extends" , String.valueOf ( inheritsFrom ) );
		suElemento.setAttribute ( "clones" , String.valueOf ( isInstanceOf ) );
		suElemento.setAttribute ( "type" , String.valueOf ( spellType ) );
		
		suElemento.setAttribute ( "minUsage" , String.valueOf ( minimalUsage ) ); 
		
		suElemento.appendChild ( getPropListXMLRepresentation(doc) );
		
		suElemento.appendChild ( getRelationshipListXMLRepresentation(doc) );
		
		
		
		
		

		

		org.w3c.dom.Element attackElement = suElemento; //for copy-paste purposes
			
			
			org.w3c.dom.Element castProbabilityElement = doc.createElement("CastProbability");
			{
				castProbabilityElement.setAttribute("steepness",String.valueOf(castProbabilitySteepness));
				attackElement.appendChild(castProbabilityElement);
			}
			
			org.w3c.dom.Element castTimeElement = doc.createElement("CastTime");
			{
				castTimeElement.setAttribute("steepness",String.valueOf(castTimeSteepness));
				castTimeElement.setAttribute("starting",String.valueOf(castStartingTime));
				attackElement.appendChild(castTimeElement);
			}
			org.w3c.dom.Element durationTimeElement = doc.createElement("Duration");
			{
				durationTimeElement.setAttribute("steepness",String.valueOf(durationTimeSteepness));
				durationTimeElement.setAttribute("starting",String.valueOf(durationStartingTime));
				attackElement.appendChild(durationTimeElement);
			}
			org.w3c.dom.Element attackSkillsElement = doc.createElement("InvolvedSkills");
			{
				if ( involvedSkills != null )
				{
					for ( int i = 0 ; i < involvedSkills.length ; i++ )
					{
						org.w3c.dom.Element thisSkillElement = doc.createElement("Skill");
						thisSkillElement.setAttribute("name",involvedSkills[i]);
						thisSkillElement.setAttribute("relevance",String.valueOf(involvedSkillRelevance[i]));
						attackSkillsElement.appendChild(thisSkillElement);
					}
				}
				attackElement.appendChild(attackSkillsElement);
			}
			org.w3c.dom.Element manaCostElement = doc.createElement("ManaCost");
			{
				manaCostElement.setAttribute("steepness",String.valueOf(manaCostSteepness));
				manaCostElement.setAttribute("starting",String.valueOf(startingManaCost));
				attackElement.appendChild(manaCostElement);
			}
			org.w3c.dom.Element intensityElement = doc.createElement("Intensity");
			{
				intensityElement.setAttribute("steepness",String.valueOf(intensitySteepness));
				intensityElement.setAttribute("starting",String.valueOf(startingIntensity));
				attackElement.appendChild(intensityElement);
			}
			
			/*
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
			*/	

		if ( effects != null )
			suElemento.appendChild ( effects.getXMLRepresentation ( doc , intensities ) );
		
		
		
		
		//object code
		if ( itsCode != null )
			suElemento.appendChild(itsCode.getXMLRepresentation(doc));

		return suElemento;
		
	}
	
	
	
	
	
	
	
	/**
	**
	
	CONSTRUCTING CODE
	
	**
	**/
	
		
	public Spell ( )
	{
	
	}
	
	public static Spell getInstance ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException("Spell node not Element") );
		}
		
		//{n is an Element}
		org.w3c.dom.Element e = (org.w3c.dom.Element)n;
		
		Spell ourNewSpell;
		
		if ( !e.hasAttribute("type") )
		{
			ourNewSpell = new Spell ( mundo , n );
		}
		/*
		else if ( e.getAttribute("type").equalsIgnoreCase("effect") )
		{
			ourNewAbstractEntity = new Effect ( mundo , n );
		}
		*/
		else
		{
			ourNewSpell = new Spell ( mundo , n );
		}
		return ourNewSpell;
	}
	
	public Spell ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		constructSpell ( mundo , n , true , "none" );
	}
	
	public void constructSpell ( World mundo , org.w3c.dom.Node n , boolean allowInheritance , String spelltype ) throws XMLtoWorldException
	{
	
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ( "Spell node not Element" ) );
		}
		//{n is an Element}	
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;
	
		//type	
		spellType = spelltype;
	
		//attribs
		
		//weak inheritance?
		if ( e.hasAttribute("extends") && !e.getAttribute("extends").equals("0") && allowInheritance )
		{
			//item must extend from existing item.
			//clonamos ese item y overrideamos lo overrideable
			//(nótese que la ID del item extendido ha de ser menor).
			
			//por eso los associated nodes de los items quedan guardados [por ref] en el World hasta que
			//haya concluido la construccion del mundo
			
			//1. overrideamos el super-item usando su associated node para construirlo
			
			constructSpell ( mundo , mundo.getSpellNode( e.getAttribute("extends") ) , true , spelltype );
			
			//2. overrideamos lo que debamos overridear
			
			constructSpell ( mundo , n , false , spelltype );
			
			return;
			
		}
		
		//strong inheritance?
		if ( e.hasAttribute("clones") && !e.getAttribute("clones").equals("0") && allowInheritance )
		{
			//funciona igual que la weak inheritance a este nivel.
			//no deberian aparecer los dos; pero si asi fuera esta herencia (la fuerte) tendria precedencia.
			
			//1. overrideamos el super-item usando su associated node para construirlo
			
			constructSpell ( mundo , mundo.getSpellNode( e.getAttribute("clones") ) , true , spelltype );
			
			//2. overrideamos lo que debamos overridear
			
			constructSpell ( mundo , n , false , spelltype );
		
			return;
			
		}
		
		//mandatory XML-attribs exceptions
		if ( !e.hasAttribute("name") )
			throw ( new XMLtoWorldException ( "Spell node lacks attribute name" ) );
	
		//mandatory XML-attribs parsing
		
		//id no longer mandatory
		try
		{
			if ( e.hasAttribute("id") )
				idnumber = Integer.valueOf ( e.getAttribute("id") ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			throw ( new XMLtoWorldException ( "Bad number format at attribute id in spell node" ) );
		}
		
		title = e.getAttribute("name");
		
		//Entity parsing
		readPropListFromXML ( mundo , n );
		
		
		//spell-specifics
		
		
		/*

			
		*/
		
			
			//spell minimal usage
			if ( e.hasAttribute("minUsage") )
			{
				try
				{
					minimalUsage = Long.valueOf ( e.getAttribute("minUsage") ).longValue();
				}
				catch ( NumberFormatException nfe )
				{
					throw ( new XMLtoWorldException("Number format error at minimalUsage") );
				}
			}
			else
				minimalUsage = 0; //default value	
			
			//spell involved skills
			org.w3c.dom.NodeList nl1 = e.getElementsByTagName("InvolvedSkills");
			if ( nl1.getLength() > 0 )
			{
				org.w3c.dom.Element invsk = (org.w3c.dom.Element) nl1.item(0);
				org.w3c.dom.NodeList skillnodes = invsk.getElementsByTagName("Skill");
				involvedSkills = new String[skillnodes.getLength()];
				involvedSkillRelevance = new double [skillnodes.getLength()];
				for ( int i = 0 ; i < skillnodes.getLength() ; i++ )
				{
					org.w3c.dom.Element skillNode = (org.w3c.dom.Element) skillnodes.item(i);
					if ( !skillNode.hasAttribute("name") )
						throw ( new XMLtoWorldException("Skill node lacking attribute name") );
					if ( !skillNode.hasAttribute("relevance") )
						//throw ( new XMLtoWorldException("Skill node lacking attribute relevance") );
						involvedSkillRelevance[i] = 1.0;
					try
					{
						if ( skillNode.hasAttribute("relevance") )
							involvedSkillRelevance[i] = Double.valueOf ( skillNode.getAttribute("relevance") ).doubleValue();
						involvedSkills[i] = skillNode.getAttribute("name");
					}	
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Wrong number format for relevance attribute at weapon's attack skill node") );
					}
					
				}
			}
			
			//cast probability
			org.w3c.dom.NodeList nl = e.getElementsByTagName("CastProbability");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						castProbabilitySteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at attackProbabilitySteepness") );
					}
				}
				else
					castProbabilitySteepness = 0; //default value	
			}
			
			//cast time
			nl = e.getElementsByTagName("CastTime");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						castTimeSteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at castTimeSteepness") );
					}
				}
				else
					castTimeSteepness = 0; //default value	
				if ( atProb.hasAttribute("starting") )
				{
					try
					{
						castStartingTime = Double.valueOf ( atProb.getAttribute("starting") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at castStartingTime") );
					}
				}
				else
					castStartingTime = 30; //default value	
			}
			
			//duration time
			nl = e.getElementsByTagName("Duration");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						durationTimeSteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at durationTimeSteepness") );
					}
				}
				else
					durationTimeSteepness = 0; //default value	
				if ( atProb.hasAttribute("starting") )
				{
					try
					{
						durationStartingTime = Double.valueOf ( atProb.getAttribute("starting") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at durationStartingTime") );
					}
				}
				else
					durationStartingTime = 0; //default value	
			}
			
			//mana cost
			nl = e.getElementsByTagName("ManaCost");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						manaCostSteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at manaCostSteepness") );
					}
				}
				else
					manaCostSteepness = 0; //default value	
				if ( atProb.hasAttribute("starting") )
				{
					try
					{
						startingManaCost = Double.valueOf ( atProb.getAttribute("starting") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at startingManaCost") );
					}
				}
				else
					startingManaCost = 30; //default value	
			}
			
			//intensity
			nl = e.getElementsByTagName("Intensity");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element atProb = (org.w3c.dom.Element) nl.item(0);
				if ( atProb.hasAttribute("steepness") )
				{
					try
					{
						intensitySteepness = Double.valueOf ( atProb.getAttribute("steepness") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at intensitySteepness") );
					}
				}
				else
					intensitySteepness = 0; //default value	
				if ( atProb.hasAttribute("starting") )
				{
					try
					{
						startingIntensity = Double.valueOf ( atProb.getAttribute("starting") ).doubleValue();
					}
					catch ( NumberFormatException nfe )
					{
						throw ( new XMLtoWorldException("Number format error at startingManaCost") );
					}
				}
				else
					startingIntensity = 30; //default value	
			}
				
			//spell effects	
			nl = e.getElementsByTagName("EffectList");
			if ( nl.getLength() > 0 )
			{
				org.w3c.dom.Element effectListNode = (org.w3c.dom.Element) nl.item(0);
				
				effects = new EffectList ( mundo , effectListNode );
				
				
				org.w3c.dom.NodeList nl2 = effectListNode.getElementsByTagName("EffectRef");
				
				intensities = new Vector();
				
				for ( int r = 0 ; r < nl2.getLength() ; r++ )
				{
					org.w3c.dom.Element h = (org.w3c.dom.Element) nl2.item(r);

						if ( h.hasAttribute("intensity") )
							intensities.add ( new Integer ( h.getAttribute("intensity") ) );
						else
							intensities.add(new Integer(0));
				
				}
				
			}
		

	
		org.w3c.dom.NodeList codeNodes = e.getElementsByTagName ( "Code" );
		if ( codeNodes.getLength() > 0 )
		{
			try
			{
				itsCode = new ObjectCode ( mundo , codeNodes.item(0) );
			}
			catch ( XMLtoWorldException ex )
			{
				throw ( new XMLtoWorldException ( "Exception at Code node: " + ex.getMessage() ) );
			}
		}	
		
		//FINALLY... type-specifics!!
		
		/*
		if ( absentType.equalsIgnoreCase("effect") )
		{
			((Effect)this).readEffectSpecifics ( mundo , e );
		}
		*/

		//poner bien la id
		if ( getID() < 10000000 )
			idnumber += 50000000; //prefijo de spell.	
	
	}
	
	
	/**
	* Nos dice si se da por aludido el objeto ante un comando (ej. "mirar piedra") -> se pasa "piedra")
	*
	* @param commandArgs Los argumentos del comando.
	* @param pluralOrSingular 1 si plural.
	* @return 0 si no se da por aludido, 1 si sí, y con qué prioridad. (+ nº = - prioridad). La prioridad es el orden que ocupa el nombre que se corresponde con el comando dado en la lista de nombres.
	*/
	public int matchesCommand ( String commandArgs , boolean pluralOrSingular )
	{
		return commandArgs.trim().equalsIgnoreCase(getUniqueName())?1:0;
	}
	

	public void incrementUsage ( Mobile m )
	{
		
		for ( int i = 0 ; i < involvedSkills.length ; i++ )
		{
			if ( m.getRandom().nextDouble() < involvedSkillRelevance[i] )
				m.incSkill ( involvedSkills[i] );
		}
		
	}

	
	
	public ObjectCode getAssociatedCode() 
	{
		return itsCode;
	}

}