/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import java.util.StringTokenizer;

import eu.irreality.age.scripting.ScriptException;

//package AetheriaAWT;
/**
* Descripción con comparando y máscara, devuelve un texto u otro según el estado.
*
*/
public class Description
{
	
	//INSTANCE VARIABLES
	
	/**Texto de la descripción.*/
	protected String text;
	/**Comparando.*/
	protected long comparand; //pasar a long para comparar teniendo en cuenta estados relativos
	/**Máscara.*/
	protected long mask; //idem que la anterior
	
	//Description class 2.0: Condition as predicate support
	protected java.util.List conditions; //list of ObjectCode
	
	//CLONE
	
	public Object clone()
	{
	
		//Debug.println("CLONING: " + text);
	
		Description nueva = new Description(text,comparand,mask);	
		java.util.List nuevasCondiciones = new java.util.ArrayList();
		if ( conditions != null )
		{
			for ( int i = 0 ; i < conditions.size() ; i++ )
				nuevasCondiciones.add ( ((ObjectCode)conditions.get(i)).cloneIfNecessary() );
		}
			//java.util.Collections.copy ( nuevasCondiciones , conditions ); //nótese que el clon apunta al mismo object code (object code NO es modificable)
			//nuevasCondiciones.addAll(conditions);
		nueva.conditions = nuevasCondiciones;
		return nueva;	
	}	
	
	//CONSTRUCTOR
	
	public Description ( String ntext , long ncomparand , long nmask )
	{
		text = ntext;
		comparand = ncomparand;
		mask = nmask;
	}
	
	//METHODS
	
	/**
	* Nos dice si la descripción corresponde a un estado determinado, es decir, si se debe dar o no para dicho estado.
	*
	* @return Si la descripción corresponde al estado.
	*/
	public boolean matches ( long state )
	{
		//Debug.println("COMPARISON AHEAD");
		//Debug.println("Outer comparand: " + state);
		//Debug.println("Description comparand: " + comparand );
		//Debug.println("Description mask: " + mask );
		//Debug.println("Comparison result: " + (( mask & ( state ^ comparand ) ) == 0 ) );
		return (( mask & ( state ^ comparand ) ) == 0 ); // si true, entonces es la descripción buscada (o una de ellas).
	}
	
	
	/**
	* Nos dice si la descripción cumple las condiciones para ser mostrada,
	* permitiendo utilizar variables en dichas condiciones, cuyas inicializaciones
	* se pasan en variableInitialization.
	* Por ejemplo, este array podría ser: { { viewer , mobile("fulano") , actor , mobile("mengano") } }.
	*
	* @return Si la descripción cumple las condiciones para ser mostrada.
	*/
	public boolean matchesConditions ( Entity context , Object[][] variableInitializations )
	{
						
		if ( conditions == null ) return true;
		
		//{conditions not null}
		for ( int i = 0 ; i < conditions.size() ; i++ )
		{
			
			ReturnValue rv = new ReturnValue(null);
			
			try
			{
			
				//have to add additional code to set viewer
				((ObjectCode)conditions.get(i)).run ( null , context , null , rv , 
					variableInitializations
				);	
				
				//Debug.println("Code ran.");
				
			}
			catch ( ScriptException te )
			{
				//error al evaluar condición.
				te.printStackTrace();
				continue;
			}
			
			if ( rv.getRetVal() instanceof Boolean )
			{
				//Debug.println("Boolean return value: " + ((Boolean)rv.getRetVal()).booleanValue() );
				if ( ((Boolean)rv.getRetVal()).booleanValue() == false )
					return false;
			}
			
		}
		//{all conditions checked: true, not boolean or condition checking error}
		return true;
		
	}
	
	/**
	* Nos dice si la descripción cumple las condiciones para ser mostrada,
	* incluyendo cualquiera relacionada con viewer (relationship-states)
	* [parte del 2.0 description support]
	* El viewer puede ser null.
	*
	* @return Si la descripción cumple las condiciones para ser mostrada.
	*/
	public boolean matchesConditions( Entity context , Entity viewer )
	{
	
		//Debug.println("Called matchesConditions( " + context + ", " + viewer + " )");
	
		//viewer can be nullified if convenient
		//removed 2011-05-14 (redundant):
		//if ( viewer == null ) return matchesConditions ( context );
	
		//old support		
		if ( viewer != null )
		{
			long comparand = (long)((Mobile)viewer).getRelationshipState( context )*((long)Math.pow(2,32)) + context.getState();
			if ( !matches( comparand ) ) return false;
		}
		
		//Debug.println("Comparand matches.");
		
		//new support
		
		if ( conditions == null ) return true;
		
		//Debug.println("Conditions not null. Size " + conditions.size());
		
		//{conditions not null}
		for ( int i = 0 ; i < conditions.size() ; i++ )
		{
			
			//Debug.println("Checking condition " + (i+1));
			//Debug.println((ObjectCode)conditions.get(i));
			
			ReturnValue rv = new ReturnValue(null);
			
			try
			{
			
				//have to add additional code to set viewer
				((ObjectCode)conditions.get(i)).run ( null , context , null , rv , 
					new Object[][] 
					{ 
						new Object[] 
						{
							"viewer", viewer
						}
					} 
				);	
				
				//Debug.println("Code ran.");
				
			}
			catch ( ScriptException te )
			{
				//error al evaluar condición.
				te.printStackTrace();
				continue;
			}
			
			if ( rv.getRetVal() instanceof Boolean )
			{
				//Debug.println("Boolean return value: " + ((Boolean)rv.getRetVal()).booleanValue() );
				if ( ((Boolean)rv.getRetVal()).booleanValue() == false )
					return false;
			}
			
		}
		//{all conditions checked: true, not boolean or condition checking error}
		return true;
	
	}
	
	/**
	* Nos dice si la descripción cumple las condiciones para ser mostrada.
	* [parte del 2.0 description support]
	*
	* @return Si la descripción cumple las condiciones para ser mostrada.
	*/
	/*
	public boolean matchesConditions( Entity context )
	{
		
		//old support
		if ( !matches( context.getState() ) ) return false;
		
		//new support
		
		if ( conditions == null ) return true;
		
		//{conditions not null}
		for ( int i = 0 ; i < conditions.size() ; i++ )
		{
			
			ReturnValue rv = new ReturnValue(null);
			
			try
			{
				((ObjectCode)conditions.get(i)).run ( null , context , null , rv ,
						new Object[][]
						{
							new Object[]
							{
							"viewer", null
							}
						}
				 );	
			}
			catch ( bsh.TargetError te )
			{
				//error al evaluar condición.
				te.printStackTrace();
				continue;
			}
			
			if ( rv.getRetVal() instanceof Boolean )
			{
				if ( ((Boolean)rv.getRetVal()).booleanValue() == false )
					return false;
			}
			
		}
		//{all conditions checked: true, not boolean or condition checking error}
		return true;
		
	}
	*/
	
	/**
	* Método de acceso al texto de la descripción.
	*
	* @return Texto de la descripción.
	*/
	public String getText (  )
	{
		return text;	
	}
		
		
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
	
		org.w3c.dom.Element suElemento = doc.createElement( "Description" );
		
		String theText = text;
		if ( theText.startsWith("P$") )
		{
			theText = theText.substring(2);
			suElemento.setAttribute ( "properName" , "true" );
		}
		else if ( theText.startsWith("N$") )
		{
			theText = theText.substring(2);
			suElemento.setAttribute ( "properName" , "false" );
		}
		
		org.w3c.dom.Text t = doc.createTextNode(theText);
		
		suElemento.appendChild(t);
		
		suElemento.setAttribute ( "stateComparand" , String.valueOf( comparand ) );
		suElemento.setAttribute ( "stateMask" , String.valueOf ( mask ) );
		
		if ( conditions != null )
		{
			for ( int i = 0 ; i < conditions.size() ; i++ )
			{
			
				ObjectCode oc = (ObjectCode)conditions.get(i);
				org.w3c.dom.Element el = (org.w3c.dom.Element) oc.getXMLRepresentation(doc,"Condition");
				suElemento.appendChild(el);
				
			}
		}
		
		return suElemento;
		
	}
	
	public Description ( World w , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
	
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException("Description node not element") );
		}
		else
		{
			org.w3c.dom.Element e = (org.w3c.dom.Element) n;
			
			long comparand=0,mask=0; //si no se especifica en atributos stateComparand y stateMask, comparando y mascara valdran cero
			
			try
			{
				if ( e.hasAttribute ( "stateComparand" ) )
					comparand = Long.valueOf ( e.getAttribute( "stateComparand" ) ).longValue();
				if ( e.hasAttribute ( "stateMask" ) )
					mask = Long.valueOf ( e.getAttribute( "stateMask" ) ).longValue();	
			}
			catch ( NumberFormatException nfe )
			{
				throw ( new XMLtoWorldException("Bad number format at attribute stateComparand or stateMask") );
			}
			
			//cogemos el texto
			
			//block
			{
				boolean terminamos = false;
				while ( !terminamos )
				{
					try
					{
					
						org.w3c.dom.Node hijo = n.getFirstChild();
						while ( !(hijo instanceof org.w3c.dom.Text ) || hijo.getNodeValue().trim().length()==0 )
						{
							if ( hijo == null )
								break;
							hijo = hijo.getNextSibling();
						}
					
						org.w3c.dom.Text t = (org.w3c.dom.Text) (hijo);
						if ( t == null )
							throw ( new XMLtoWorldException("Description node containing no text node") );
						//return ( new Description ( t.getData() , comparand , mask ) );
						this.comparand=comparand;
						this.mask=mask;
						this.text=t.getData().trim(); //necessary because XML saving adds whitespace to text nodes
						this.text = t.getData();
						this.text = StringMethods.textualSubstitution( this.text, "\\n" , "\n" );
						this.text = StringMethods.textualSubstitution( this.text, "\\t" , "\t" );
						this.text = StringMethods.textualSubstitution( this.text, "\\s" , " " ); //escape character for space
						terminamos = true;
					}
					catch ( ClassCastException cce )
					{
						continue;
					}
				}

			}

			
			//2.0 Description: predicate support begin
			
			org.w3c.dom.NodeList nl = (org.w3c.dom.NodeList) e.getElementsByTagName("Condition");
			for ( int i = 0 ; i < nl.getLength() ; i++ )
			{
				
				ObjectCode oc = new ObjectCode ( w, (org.w3c.dom.Element)nl.item(i) , true ); 
				
				if ( conditions == null )
					conditions = new java.util.ArrayList();
				conditions.add(oc);	
				
			}
			
			//2.0 Description: predicate support end
			
			//proper name support: convert properName flag true to P$ and properName flag false to N$
			
			//<Description properName=true>tal</Description>
			
			if ( e.hasAttribute ( "properName" ) )
			{
				boolean isProper = Boolean.valueOf ( e.getAttribute( "properName" ) ).booleanValue();
				if ( isProper ) text = "P$" + text;
				else text = "N$" + text;
			}
		
		}
	
	}
	
	
	//metodos especificos para open y close descriptions
	
	/**
	 * Returns whether this description is marked as a success or failure description
	 * (for open/close descriptions)
	 */
	public boolean hasSuccessMark()
	{
		return text.startsWith("SUCCESS:") || text.startsWith("EXITO:") || text.startsWith("FAIL:") || text.startsWith("SUCCESS:") 
		|| text.startsWith("FRACASO:");
	}
	
	/**
	 * Returns whether this description is marked as a success description (for
	 * open/close descriptions).
	 * @return
	 */
	public boolean isSuccessDescription()
	{
		return text.startsWith("SUCCESS:") || text.startsWith("EXITO:");
	}
	
	/**
	 * Returns whether this description is marked as a failure description (for
	 * open/close descriptions).
	 * @return
	 */
	public boolean isFailDescription()
	{
		return text.startsWith("FAIL:") || text.startsWith("FRACASO:");
	}
	
	/**
	 * Returns the pure text of the description without the success/fail mark.
	 * Transitional method. When the open/close/lock/unlock system is further
	 * modernised, the success/fail status should not be saved directly in the
	 * description text and therefore this should be equivalent to getText(),
	 * but at the moment that doesn't hold.
	 * @return
	 */
	public String getTextWithoutSuccessMark()
	{
		if ( hasSuccessMark() )
		{
			StringTokenizer st = new StringTokenizer(text,":");
			st.nextToken();
			return st.nextToken("").substring(1); //the 1 is to remove the : delimiter from the beginning.
		}
		else return text;
	}
	
}