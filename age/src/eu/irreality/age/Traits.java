/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import java.util.*;

import eu.irreality.age.debug.Debug;

public class Traits implements Cloneable
{

	/*ATRIBUTOS*/
	
	/*all deprecated!
	private int fuerza;
	private int constitucion;
	private int inteligencia;
	private int sabiduria;
	private int destreza;
	private int carisma;
	private int poder;
	*/
	
	/*HABILIDADES [USAGE]*/
	
	private Hashtable habilidades;
	
	public String toString()
	{
		return "<Traits: " + habilidades.toString()+">";
	}
	                  
	
	public Object clone()
	{
		Traits t = null;
		try
		{
			t  =  (Traits) super.clone();
		}
		catch ( CloneNotSupportedException cnse )
		{
			cnse.printStackTrace();
		}	
		t.habilidades = (Hashtable) habilidades.clone();
		return t;
	}
	
	/* OBSOLETE!! HASHTABLE USED!!
	
	//armas
	
	private float ataque;
	private float defensa;
	private float unamano;
	private float dosmanos;
	
	private float roma;
	private float espada;
	private float daga;
	private float hacha;
	private float arco;

	//magia
	
	private float fuego;
	private float tierra;
	private float aire;
	private float agua;
	private float brujeria;
	private float caos;
	private float naturaleza;
	private float vida;
	private float muerte;
	private float poder_arcano;
	
	//otras
	
	//instrumentos
	//idiomas
	//(...)
	private float seduccion;
	private float percepcion;
	private float regateo;
	private float robo;	

	*/

	//getStat(String) y getSkill(String) para obtener los datos.


	/*
	public void setStat ( String s , int val )
	{
	
		if ( s.equalsIgnoreCase("FUE") || s.equalsIgnoreCase("STR") )
			fuerza=val;
		else if ( s.equalsIgnoreCase("CON") )
			constitucion=val;
		else if ( s.equalsIgnoreCase("INT") )
			inteligencia=val;
		else if ( s.equalsIgnoreCase("SAB") || s.equalsIgnoreCase("WIS") )
			sabiduria=val;	
		else if ( s.equalsIgnoreCase("DES") || s.equalsIgnoreCase("DEX") )
			destreza=val;	
		else if ( s.equalsIgnoreCase("CHA") || s.equalsIgnoreCase("CAR") )	
			carisma=val;
		else if ( s.equalsIgnoreCase("POD") || s.equalsIgnoreCase("POW") )
			poder=val;
	
	}
	

	public int getStat ( String s )
	{
		if ( s.equalsIgnoreCase("FUE") || s.equalsIgnoreCase("STR") )
			return fuerza;
		else if ( s.equalsIgnoreCase("CON") )
			return constitucion;
		else if ( s.equalsIgnoreCase("INT") )
			return inteligencia;
		else if ( s.equalsIgnoreCase("SAB") || s.equalsIgnoreCase("WIS") )
			return sabiduria;	
		else if ( s.equalsIgnoreCase("DES") || s.equalsIgnoreCase("DEX") )
			return destreza;	
		else if ( s.equalsIgnoreCase("CHA") || s.equalsIgnoreCase("CAR") )	
			return carisma;
		else if ( s.equalsIgnoreCase("POD") || s.equalsIgnoreCase("POW") )
			return poder;
		return 0;	
	}
	*/

	public long getStat ( String s )
	{
	
	
		if ( s.equalsIgnoreCase("FUE") || s.equalsIgnoreCase("STR") )
			return getStat("fuerza");
		else if ( s.equalsIgnoreCase("CON") )
			return getStat("constitucion");
		else if ( s.equalsIgnoreCase("INT") )
			return getStat("inteligencia");
		else if ( s.equalsIgnoreCase("SAB") || s.equalsIgnoreCase("WIS") )
			return getStat("sabiduria");	
		else if ( s.equalsIgnoreCase("DES") || s.equalsIgnoreCase("DEX") )
			return getStat("destreza");	
		else if ( s.equalsIgnoreCase("CHA") || s.equalsIgnoreCase("CAR") )	
			return getStat("carisma");
		else if ( s.equalsIgnoreCase("POD") || s.equalsIgnoreCase("POW") )
			return getStat("poder");;	
	
		
	
		return getSkill(s);
	}
	
	public void setStat ( String s , long l )
	{
	
	
	
		if ( s.equalsIgnoreCase("FUE") || s.equalsIgnoreCase("STR") )
			setStat("fuerza",l);
		else if ( s.equalsIgnoreCase("CON") )
			setStat("constitucion",l);
		else if ( s.equalsIgnoreCase("INT") )
			setStat("inteligencia",l);
		else if ( s.equalsIgnoreCase("SAB") || s.equalsIgnoreCase("WIS") )
			setStat("sabiduria",l);	
		else if ( s.equalsIgnoreCase("DES") || s.equalsIgnoreCase("DEX") )
			setStat("destreza",l);	
		else if ( s.equalsIgnoreCase("CHA") || s.equalsIgnoreCase("CAR") )	
			setStat("carisma",l);
		else if ( s.equalsIgnoreCase("POD") || s.equalsIgnoreCase("POW") )
			setStat("poder",l);
	
	
	
	
		setSkill ( s , l );
	}

	public long getSkill ( String s )
	{
		try
		{
			Long l = (Long) habilidades.get(s);
			if ( l != null )
				return l.longValue();
			else
				return 0;
		}
		catch ( ClassCastException cce )
		{
			//lo que hay no es un Long
			return 0;
		}
	}
	
	public void setSkill ( String name , long value )
	{
		habilidades.put ( name , new Long(value) );
	}
	
	public void incSkill ( String name )
	{
		setSkill ( name , getSkill(name) + 1 );
	}


	public Traits ( )
	{
		/*
		fuerza=12;
		inteligencia=12;
		destreza=12;
		constitucion=12;
		sabiduria=12;
		poder=12;
		*/
		habilidades = new Hashtable();
		
		setStat ( "fuerza" , 12 );
		setStat ( "inteligencia" , 12 );
		setStat ( "sabiduria" , 12 );
		setStat ( "destreza" , 12 );
		setStat ( "velocidad" , 12 );
		setStat ( "carisma" , 12 );
		
	}
	
	public Traits ( int skillHashtableInitSize )
	{
		/*
		fuerza=12;
		inteligencia=12;
		destreza=12;
		constitucion=12;
		sabiduria=12;
		poder=12;
		*/
		habilidades = new Hashtable ( skillHashtableInitSize );
		
				
		setStat ( "fuerza" , 12 );
		setStat ( "inteligencia" , 12 );
		setStat ( "sabiduria" , 12 );
		setStat ( "destreza" , 12 );
		setStat ( "velocidad" , 12 );
		setStat ( "carisma" , 12 );
		
		
	}



	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
	
		org.w3c.dom.Element traitsElt = doc.createElement("Traits");
	
		//only skill representation at the moment
		
		if ( habilidades != null )
		{
			org.w3c.dom.Element skillsElt = doc.createElement ( "SkillList" );
		
			Enumeration nombres = habilidades.keys();
			
			while ( nombres.hasMoreElements() )
			{
				String nombre = (String)nombres.nextElement();
				long valor = ((Long)habilidades.get(nombre)).longValue();
				
				org.w3c.dom.Element skillElt = doc.createElement ( "Skill" );
				skillElt.setAttribute("name" , nombre);
				skillElt.setAttribute("value" , String.valueOf(valor));
				
				skillsElt.appendChild(skillElt);
			}	
			
			traitsElt.appendChild(skillsElt);
		}
		
		return traitsElt;
	
	}

	public Traits ( World mundo , org.w3c.dom.Node traitsNode ) throws XMLtoWorldException
	{
	
		if ( !(traitsNode instanceof org.w3c.dom.Element) )
			throw ( new XMLtoWorldException ( "Traits node not Element" ) );
		org.w3c.dom.Element e = (org.w3c.dom.Element) traitsNode;
		
		//{e is an Element, the <Traits> node}
		
		//read skills 
		
		org.w3c.dom.NodeList skillsElts = e.getElementsByTagName ( "SkillList" );
		if ( skillsElts.getLength() > 0 )
		{
			Debug.println("Processing TRAITS NODE");
			
			org.w3c.dom.NodeList skillElts = ((org.w3c.dom.Element)skillsElts.item(0)).getElementsByTagName("Skill");
			
			habilidades = new Hashtable ( (int)(skillElts.getLength() / 0.75) + 1 , (float)0.75 );
			
			for ( int i = 0 ; i < skillElts.getLength() ; i++ )
			{
			
				org.w3c.dom.Element skillElt = (org.w3c.dom.Element) skillElts.item(i);
				
				if ( !skillElt.hasAttribute("name") )
					throw ( new XMLtoWorldException ( "Skill element lacking name attribute") );
				if ( !skillElt.hasAttribute("value") )
					throw ( new XMLtoWorldException ( "Skill element lacking value attribute") );
						
				long val;	
				try
				{
					val = Long.valueOf( skillElt.getAttribute("value") ).longValue();
				}
				catch ( NumberFormatException nfe )
				{
					throw ( new XMLtoWorldException ( "Bad number format for Skill element's value attribute" ) );
				}
				
				Debug.println("Hey! Skill " + skillElt.getAttribute("name") + " has value " + val + "!");
				
				habilidades.put ( skillElt.getAttribute("name") , new Long ( val ) );
			
			}				
		}
		else //no skills
			habilidades = new Hashtable();
	
	}



}
