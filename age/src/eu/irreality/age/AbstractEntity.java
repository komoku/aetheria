/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package eu.irreality.age;
import java.util.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.scripting.ScriptException;

public class AbstractEntity extends Entity implements SupportingCode, UniqueNamed
{


	/*00*/ private String absentType;

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
	
	
	public Object clone( )
	{
		//do it!
		
		AbstractEntity ae = new AbstractEntity();
		
		copyAbstractEntityFieldsTo(ae);
		
		return ae;
	}
	
	
	public void copyAbstractEntityFieldsTo ( AbstractEntity ae )
	{
		Debug.println("Copying fields.");
		ae.copyEntityFields(this); //estados, propiedades, etc.
		if ( itsCode != null ) ae.itsCode = itsCode.cloneIfNecessary();
		ae.aleat = getRandom();
		ae.absentType = absentType;
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
	public boolean execCode ( String routine , Object[] args ) throws ScriptException
	{
		if ( itsCode != null )
			return itsCode.run ( routine , this , args );
		else return false;
	}
	
	/*ejecuta el codigo bsh del objeto correspondiente a la rutina dada si existe.
	Si no existe, simplemente no ejecuta nada y devuelve false.*/
	public boolean execCode ( String routine , Object[] args , ReturnValue retval ) throws ScriptException
	{
		//S/ystem.out.println("Mobile code runnin'.");
		//System.out.println("Its Code: " + itsCode);
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
	//public /*abstract*/ boolean update ( PropertyEntry pe , World mundo )
	//{
	//	return true;
	//}
	//done @ Entity
	
	public void setID ( int newid )
	{
		if ( newid < Utility.absent_summand )
			idnumber = newid + Utility.absent_summand;
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
	
		org.w3c.dom.Element suElemento = doc.createElement( "AbstractEntity" );
		
		suElemento.setAttribute ( "id" , String.valueOf( idnumber ) );
		suElemento.setAttribute ( "name" , String.valueOf ( title ) );
		suElemento.setAttribute ( "extends" , String.valueOf ( inheritsFrom ) );
		suElemento.setAttribute ( "clones" , String.valueOf ( isInstanceOf ) );
		suElemento.setAttribute ( "type" , String.valueOf ( absentType ) );
		
		suElemento.appendChild ( getPropListXMLRepresentation(doc) );
		
		suElemento.appendChild ( getRelationshipListXMLRepresentation(doc) );
		
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
	
		
	public AbstractEntity ( )
	{
	
	}
	
	public static AbstractEntity getInstance ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException("AbstractEntity node not Element") );
		}
		
		//{n is an Element}
		org.w3c.dom.Element e = (org.w3c.dom.Element)n;
		
		AbstractEntity ourNewAbstractEntity;
		
		if ( !e.hasAttribute("type") )
		{
			ourNewAbstractEntity = new AbstractEntity ( mundo , n );
		}
		else if ( e.getAttribute("type").equalsIgnoreCase("effect") )
		{
			ourNewAbstractEntity = new Effect ( mundo , n );
		}
		else
		{
			ourNewAbstractEntity = new AbstractEntity ( mundo , n );
		}
		return ourNewAbstractEntity;
	}
	
	public AbstractEntity ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		constructAbstractEntity ( mundo , n , true , "none" );
	}
	
	public void constructAbstractEntity ( World mundo , org.w3c.dom.Node n , boolean allowInheritance , String absenttype ) throws XMLtoWorldException
	{
	
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ( "AbstractEntity node not Element" ) );
		}
		//{n is an Element}	
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;
	
		//type	
		absentType = absenttype;
	
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
			
			constructAbstractEntity ( mundo , mundo.getAbstractEntityNode( e.getAttribute("extends") ) , true , absenttype );
			
			//2. overrideamos lo que debamos overridear
			
			constructAbstractEntity ( mundo , n , false , absenttype );
			
			return;
			
		}
		
		//strong inheritance?
		if ( e.hasAttribute("clones") && !e.getAttribute("clones").equals("0") && allowInheritance )
		{
			//funciona igual que la weak inheritance a este nivel.
			//no deberian aparecer los dos; pero si asi fuera esta herencia (la fuerte) tendria precedencia.
			
			//1. overrideamos el super-item usando su associated node para construirlo
			
			constructAbstractEntity ( mundo , mundo.getAbstractEntityNode( e.getAttribute("clones") ) , true , absenttype );
			
			//2. overrideamos lo que debamos overridear
			
			constructAbstractEntity ( mundo , n , false , absenttype );
		
			return;
			
		}
		
		//mandatory XML-attribs exceptions
		if ( !e.hasAttribute("name") )
			throw ( new XMLtoWorldException ( "Item node lacks attribute name" ) );
	
		//mandatory XML-attribs parsing
		
		//id no longer mandatory
		try
		{
			if ( e.hasAttribute("id") )
				idnumber = Integer.valueOf ( e.getAttribute("id") ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			throw ( new XMLtoWorldException ( "Bad number format at attribute id in abstract entity node" ) );
		}
		
		title = e.getAttribute("name");
		
		//Entity parsing
		readPropListFromXML ( mundo , n );
	
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
		
		if ( absentType.equalsIgnoreCase("effect") )
		{
			((Effect)this).readEffectSpecifics ( mundo , e );
		}

		//poner bien la id
		if ( getID() < 10000000 )
			idnumber += 40000000; //prefijo de abstr. ent.	
		
		
		
		//eventos onInit()
		try
		{
			boolean ejecutado = execCode ( "onInit" ,
			new Object[]
			{
			}
			);
			
		}
		catch ( ScriptException te )
		{
			te.printStackTrace();
			mundo.write("BeanShell error on initting abstract entity " + this + ": error was " + te);
		}
		
	
	}
	
	public AbstractEntity createNewInstance ( World mundo )
	{
		return createNewInstance(mundo,null);
	}
	
	public AbstractEntity createNewInstance( World mundo , String uniqueName ) 
	{
	
		Debug.println("Creatin' new instance.");
	
		AbstractEntity it = (AbstractEntity) this.clone();
		
		Debug.println("Clone made.");
		
		it.inheritsFrom = 0;
		
		if ( this.isInstanceOf == 0 )
		{
			it.isInstanceOf = idnumber;
			Debug.println("1) instanceOf set to " + idnumber);
		}
		else
		{
			it.isInstanceOf = this.isInstanceOf;	
			Debug.println("2) instanceOf set to " + this.isInstanceOf);
		}
		
		if ( uniqueName == null ) it.title = mundo.generateUnusedUniqueName(this.getUniqueName()); 
		else it.title = uniqueName;
		
		mundo.addAbstractEntityAssigningID ( it );
		
		return it;
	}
	
	public ObjectCode getAssociatedCode() 
	{
		return itsCode;
	}
	
	
	public String toString()
	{
	
		String s = ("[ " + getClass().getName() + ":" + getID() );
		//if ( this instanceof Nameable )
		{
			s += ":";
			s += this.getUniqueName();
		}
		s+=" ]";
		return s;
	}
	

}
