/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
//package AetheriaAWT;
/**
* Entidades que pueblan el mundo.
* Definimos "entidad" como "objeto del mundo de Aetheria que es afectado por el paso del tiempo".
* Cada entidad está, en cada momento, en un estado, y puede cambiar de estado según las circunstancias.
* Entidades son las habitaciones, los items, los enemigos y el personaje.
*
* @author Carlos Gómez
*/
import java.util.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
public abstract class Entity
{


	List propertiesList = new ArrayList(); //of PropertyEntry
	
	protected Vector relationships = new Vector(); //vector of Entity
	protected Vector relationship_properties = new Vector(); //vector of List of Property
	

	/**Estado actual*/
	//protected int state; 
	/**Unidades restantes para cambiar de estado*/
	//protected long timeUnitsLeft;
	/**Blanco de los estados*/
	//protected int target;
	
	
	//para los diferentes clone() de las subclases de Entity. Coge los campos de e.
	public void copyEntityFields(Entity e)
	{
	
		//Debug.println("CLONING ENTITY FIELDS from Source " + (((Item)e).getTitle()) + ":" + e.getID());
		//Debug.println("SOURCE HAS " + e.propertiesList.size() + " properties, " + e.relationships.size() + " relationships" );
	
		propertiesList = new ArrayList ( e.propertiesList.size() );
		for ( int i = 0 ; i < e.propertiesList.size() ; i++ )
		{
			propertiesList.add ( ((PropertyEntry)e.propertiesList.get(i)).clone() );
		}
		relationships = new Vector( e.relationships.size() );
		for ( int i = 0 ; i < e.relationships.size() ; i++ )
		{
			relationships.add ( ((Entity)e.relationships.get(i)) );
		}
		relationship_properties = new Vector ( e.relationship_properties.size() );
		for ( int i = 0 ; i < e.relationship_properties.size() ; i++ )
		{
			List ofProperties = new ArrayList( ((List)e.relationship_properties.get(i)).size()   );
			relationship_properties.add(ofProperties);
			for ( int j = 0 ; j < ofProperties.size() ; j++ )
			{
				ofProperties.add (   ((PropertyEntry)((List)e.relationship_properties.get(i)).get(j)).clone()     );
			}
		}
	}
	
	
	/**
	* update: función genérica que hace pasar una unidad de tiempo y actualiza el estado si es necesario.
	*
	* @return true si sale bien el update.
	*/
	public boolean update(World mundo)
	{
	
		//si se ha solicitado que alguna propiedad sea puesta delante en la lista
		if ( pushToFront.size() > 0 )
			pushRequestedPropertiesToFront ();
	
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			//if ( this instanceof Player ) Debug.println(pe.getName()+" isat " +i);
			pe.decreaseTime();
			if ( pe.needsUpdate() )
			{
				
				//if ( pe.getName().equalsIgnoreCase("state") )
				//{
					//Debug.println("changeState called by " + this);
				//	changeState( mundo ); //legacy!!
				//}
				 //nah, changeState called by update.
				//else
				//{
					update ( pe , mundo );
				//}
			}
		}
		
		for ( int i = 0 ; i < relationships.size() ; i++ )
		{
			Entity ent = (Entity) relationships.get(i);
			List propertyEntries = (List) relationship_properties.get(i);
			for ( int j = 0 ; j < propertyEntries.size() ; j++ )
			{
				PropertyEntry entrada = (PropertyEntry) propertyEntries.get(j);
				entrada.decreaseTime();
				if ( entrada.needsUpdate() )
				{
					updateRelationship ( ent , entrada , mundo );
				}
			}
		}
		
		return true;
	}
	
	
	
	
	//important
	
	/*
	public boolean update ( PropertyEntry pe , World mundo )
	{
	
		boolean ejecutado = false;
	
	
		//try to exec entity's update function
		if ( this instanceof SupportingCode )
		{
		
			SupportingCode esto = (SupportingCode) this;
	
			try
			{
				ejecutado = esto.execCode( "update" , new Object[] { pe , mundo } );
			}
			catch (bsh.TargetError bshte)
			{
				mundo.escribir("bsh.TargetError found at update routine" );
				bshte.printStackTrace();
			}
			if ( ejecutado ) return true; //end() found
			
		}
					
		if ( this instanceof Player )			
			Debug.println("Checkpoint1");			
					
		//try to exec world's update function
		try
		{
			ejecutado = mundo.execCode( "update" , new Object[] { pe , this } );
		}
		catch (bsh.TargetError bshte)
		{
			mundo.escribir("bsh.TargetError found at update routine" );
			bshte.printStackTrace();
		}
		if ( ejecutado ) return true; //end() found
				
	
	
		if ( pe.getName().equalsIgnoreCase("state") )
		{
			if ( this instanceof Player )
				Debug.println(this + "Changestate");
			changeState( mundo ); //legacy!!
		}
		
		//else, should be overridden if it should do somethin'
	
		return true;
	}
	*/
	
	
	
	public boolean update ( PropertyEntry pe , World mundo )
	{
	
	
	/*
		if ( this instanceof Player )
		{
			Debug.println("Player update: " + pe.getName() + ":" + pe.getValue() + ":" + pe.getTimeLeft() );
		}
	*/
		//Debug.println("UPDATE " + pe.getName());
		
		/*
		if ( pe.getName().equalsIgnoreCase("state") )
		{
			if ( this instanceof Player )
				Debug.println(this + "Changestate");
			Debug.println("'NNA CALL CHANGESTATE for " + this);	
			changeState( mundo ); //legacy!!
		}
		*/
		
		/*
			if ( getID() == 20000007 )
		{
			Mobile m = (Mobile)this;
			if ( m.getTitle().equalsIgnoreCase("Zunius") )
			{
				Debug.println("Zun1");
			}
		}
		*/
	
		
		//ON HANG, USE THIS CHSTATE INSTD. OF LAST ONE
		
		/*
		
				if ( pe.getName().equalsIgnoreCase("state") )
				{
					//Debug.println("changeState called by " + this);
					changeState( mundo ); //legacy!!
				}	
		
		*/
		
				
				/*
		if ( getID() == 20000007 )
		{
			Mobile m = (Mobile)this;
			if ( m.getTitle().equalsIgnoreCase("Zunius") )
			{
				Debug.println("Zun2");
			}
		}
		*/
	
		
		if ( pe.getTimeLeft() < -1 )
			return false;
		
		/*
			if ( getID() == 20000007 )
		{
			Mobile m = (Mobile)this;
			if ( m.getTitle().equalsIgnoreCase("Zunius") )
			{
				Debug.println("Zun3");
			}
		}
		*/	
		
		//else
		//{
		
				
				//temp code copied from above
		
		
						boolean ejecutado = false;
				
				
					//try to exec entity's update function
					if ( this instanceof SupportingCode )
					{
					
						SupportingCode esto = (SupportingCode) this;
				
						try
						{
							ejecutado = esto.execCode( "update" , new Object[] { pe , mundo } );
						}
						catch (bsh.TargetError bshte)
						{
							mundo.write(mundo.getIO().getColorCode("error"));
							mundo.write("bsh.TargetError found at update routine, entity is " + this + ", property entry is " + pe.getName() + "\n"  );
							mundo.write("Target exception: " + bshte.printTargetError(bshte.getTarget()) + "\n"  );
							mundo.writeError(ExceptionPrinter.getExceptionReport(bshte));
							mundo.write(mundo.getIO().getColorCode("reset"));
							bshte.printStackTrace();
						}
						if ( ejecutado ) return true; //end() found
						
					}
								
					//if ( this instanceof Player )			
					//	Debug.println("Checkpoint1");			
				
					//Debug.println("Ejecutado at cp1 is " + ejecutado);
								
					//try to exec world's update function
					try
					{
						ejecutado = mundo.execCode( "update" , new Object[] { pe , this } );
					}
					catch (bsh.TargetError bshte)
					{
						mundo.write("bsh.TargetError found at world's update routine, property entry is " + pe.getName() );
						mundo.writeError(ExceptionPrinter.getExceptionReport(bshte));
						bshte.printStackTrace();
					}
					if ( ejecutado ) return true; //end() found
		
		
			//Debug.println("Ejecutado is " + ejecutado);
		
				if ( pe.getName().equalsIgnoreCase("state") )
				{
					//Debug.println("changeState called by " + this);
					changeState( mundo ); //legacy!!
				}	
		
		
		//}
		
	
		return true;
	}
	
	
	public /*abstract*/ boolean updateRelationship ( Entity e , PropertyEntry pe , World mundo )
	{
		return true;
	}







	public List getProperties ()
	{
		return propertiesList;
	}
	
	public String getPropertyValue ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValue();
		}
		return null;
	}
	
	public String getPropertyValueAsString ( String propertyName )
	{
		return getPropertyValue ( propertyName );
	}
	
	private List pushToFront = new ArrayList(); //propiedades que tenemos que poner delante.
	//No se ponen inmediatamente con la función pushPropertyToFront() porque ésta
	//tenderá a ser llamada desde cosas que se hagan dentro de un update() [que es
	//donde normalmente se actualizan propiedades] y no nos interesa que el orden
	//de la lista cambie en medio del update, sino al final.
	
	//En ocasiones, nos interesará que unas prioridades se actualicen antes que otras.
	//Con esto, damos a una máxima prioridad.
	public void pushPropertyToFront ( String propertyName )
	{
		pushToFront.add ( propertyName );
	}
	
	private void pushRequestedPropertiesToFront ( )
	{
		for ( int i = 0 ; i < pushToFront.size() ; i++ )
		{
			pushPropertyToFrontReally ( (String)pushToFront.get(i) );
		}
		pushToFront = new ArrayList();
	}
	
	private void pushPropertyToFrontReally ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
			{
				propertiesList.remove(pe);
				propertiesList.add(0,pe);
			}
		}
	}
	
	public Object getPropertyValueAsObject ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValueAsObject();
		}
		return null;
	}
	
	public boolean getPropertyValueAsBoolean ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValueAsBoolean();
		}
		return false;
	}
	
	public int getPropertyValueAsInteger ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValueAsInteger();
		}
		return 0;
	}
	
	public double getPropertyValueAsDouble ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValueAsDouble();
		}
		return 0;
	}
	
	public float getPropertyValueAsFloat ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValueAsFloat();
		}
		return 0;
	}
	
	public Object getPropertyValueAsWrapper ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValueAsWrapper();
		}
		return null;
	}
	
	public long getPropertyTimeLeft ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getTimeLeft();
		}
		return 0;
	}
	
	public void setPropertyTimeLeft ( String propertyName , long newTime )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					pe.setTime(newTime);
		}
	}
	
	public PropertyEntry getPropertyEntry ( String propertyName )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe;
		}
		return null;
	}
	
	public void setProperty ( String propertyName , String propertyValue )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
			{
				pe.setValue ( propertyValue );
				return;
			}	
		}
		//property not found, add to list
		propertiesList.add ( new PropertyEntry ( propertyName , propertyValue , 0 ) );
	}
	
	//notese que si se pasa como segundo parametro un String no se llamara
	//esta funcion, sino setProperty ( String , String ) por ser mas especifica.
	public void setProperty ( String propertyName , Object propertyValue )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
			{
				pe.setValue ( propertyValue.toString() );
				pe.setObjectValue ( propertyValue );
				return;
			}
		}
		PropertyEntry pe = new PropertyEntry ( propertyName , propertyValue.toString() , 0 );
		pe.setObjectValue ( propertyValue );
		propertiesList.add ( pe );
	}
	
	public void setProperty ( String propertyName , int propertyValue )
	{
		setProperty ( propertyName , String.valueOf(propertyValue) );
	}
	
	public void setProperty ( String propertyName , double propertyValue )
	{
		setProperty ( propertyName , String.valueOf(propertyValue) );
	}
	
	public void setProperty ( String propertyName , float propertyValue )
	{
		setProperty ( propertyName , String.valueOf(propertyValue) );
	}
	
	public void setProperty ( String propertyName , boolean propertyValue )
	{
		setProperty ( propertyName , String.valueOf(propertyValue) );
	}
	
	public void setProperty ( String propertyName , String propertyValue , long timeLeft )
	{
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			if ( pe.getName().equalsIgnoreCase(propertyName) )
			{
				pe.setValueAndTime ( propertyValue , timeLeft );
				return;
			}	
		}
		//property not found, add to list
		propertiesList.add ( new PropertyEntry ( propertyName , propertyValue , timeLeft ) );
	}
	
	public void setProperty ( String propertyName , int propertyValue , long timeLeft )
	{
		setProperty ( propertyName , String.valueOf(propertyValue) , timeLeft );
	}
	
	public void setProperty ( String propertyName , boolean propertyValue , long timeLeft )
	{
		setProperty ( propertyName , String.valueOf(propertyValue) , timeLeft );
	}
	


	public boolean hasBooleanProperty ( String propertyName )
	{
		return getPropertyValueAsBoolean ( propertyName ); 
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
		return 0; //sólo algunas clases de Entity, las que se puedan agrupar en listas, necesitarán overridear este método.
	}
	
	/**
	 * This implements one of the modes of matchesCommand ( String , String )
	 * @param commandArgs arguments of a command that might refer to this object
	 * @param referenceList ordered list of reference names
	 * @return 0 si no se da por aludido, 1 si sí, y con qué prioridad. (+ nº = - prioridad). La prioridad es el orden que ocupa el nombre que se corresponde con el comando dado en la lista de nombres.
	 */
	protected int legacyMatchesCommand ( String commandArgs , String referenceNameList )
	{
		int nToksArg = StringMethods.numToks( commandArgs , ' ');
		int nToksList = StringMethods.numToks( referenceNameList , '$');
		for ( int i = 1 ; i <= nToksArg ; i++ )
		{
			String currentToAnalyze = StringMethods.getToks( commandArgs , i , nToksArg , ' ' );
			//"mirar la piedra pequeña" -> commandArgs="la piedra pequeña" -> vamos analizando "la piedra pequeña", "piedra pequeña", ...
			for ( int j = 1 ; j <= nToksList ; j++ )
			{
				if ( StringMethods.getTok( referenceNameList , j , '$' ) .equalsIgnoreCase(currentToAnalyze) ) 
				{
					return j;
				}
			}
			//TODO: here, add reverse analysis. gettoks from 1 to moving i.
		}
		return 0;
	}
	
	/**
	 * New matchesCommand mode implemented 2010-06-25, more robust than the "legacy" mode that wouldn't accept anything
	 * to the right of entities.
	 * @param commandArgs
	 * @param referenceNameList
	 * @return
	 */
	protected int lenientMatchesCommand ( String commandArgs , String referenceNameList )
	{
		int nToksList = StringMethods.numToks( referenceNameList , '$');
		for ( int j = 1 ; j <= nToksList ; j++ )
		{
			String currentReferenceName = StringMethods.getTok( referenceNameList , j , '$' );
			if ( commandArgs.toLowerCase().contains(currentReferenceName.toLowerCase()) )
				return j;
		}
		return 0;
	}
	
	
	/**
	 * New matchesCommand mode implemented 2011-05-01, a bit less lenient than the lenient matchesCommand. Will search for
	 * matches with words.
	 * @param commandArgs
	 * @param referenceNameList
	 * @return
	 */
	protected int moderateMatchesCommand ( String commandArgs , String referenceNameList )
	{
		int nToksList = StringMethods.numToks( referenceNameList , '$');
		for ( int j = 1 ; j <= nToksList ; j++ )
		{
			String currentReferenceName = StringMethods.getTok( referenceNameList , j , '$' );
			int position = commandArgs.toLowerCase().indexOf(currentReferenceName.toLowerCase());
			if ( position < 0 ) //does not match
				continue;
			if ( position != 0 && !Character.isWhitespace(commandArgs.charAt(position-1)) ) //matches but starts at a place other than beginning/whitespace
				continue;
			if ( position+currentReferenceName.length() != commandArgs.length() && !Character.isWhitespace(commandArgs.charAt(position+currentReferenceName.length())) ) //matches but ends at a place other than end/whitespace
				continue;
			//if we have reached this point, the match is acceptable
			return j;
		}
		return 0;
	}
	
	/**
	 * Note: takes a legacy, $-separated list of reference names
	 * This method may be called by subclass-specific implementations of the matchesCommand ( String , boolean ) method.
	 * @param commandArgs arguments of a command that might refer to this object
	 * @param referenceList ordered list of reference names
	 * @return 0 si no se da por aludido, 1 si sí, y con qué prioridad. (+ nº = - prioridad). La prioridad es el orden que ocupa el nombre que se corresponde con el comando dado en la lista de nombres.
	 */
	protected int matchesCommand ( String commandArgs , String referenceNameList , int commandMatchingMode )
	{
		if ( commandMatchingMode == LEGACY_COMMAND_MATCHING )
			return legacyMatchesCommand ( commandArgs , referenceNameList );
		else if ( commandMatchingMode == LENIENT_COMMAND_MATCHING )
			return lenientMatchesCommand ( commandArgs , referenceNameList );
		else
			return moderateMatchesCommand ( commandArgs , referenceNameList );
	}
	
	//command matching modes
	public static final int LEGACY_COMMAND_MATCHING = 0;
	public static final int LENIENT_COMMAND_MATCHING = 1;
	public static final int MODERATE_COMMAND_MATCHING = 2;
	
	
	
	
	/**Funciones que manejan esa propiedad tan especial que es el estado, que antes era la única que había.**/
	

	
	public void setNewTarget ( int ntarget )
	{
		setProperty ( "target" , ntarget );	
	}
	
	public int getTarget ( )
	{
		return getPropertyValueAsInteger("target");
	}
	
	public void setNewState ( int nstate , long TUs )
	{
	
		if ( getPropertyValueAsInteger ("state") == Mobile.CASTING )
		{
			//Debug.println("setNewState: ");
			//(new Exception()).printStackTrace();
		}
	
		setProperty ( "state" , nstate , TUs );
	}
	
	protected void setNewState ( int nstate )
	{
		setProperty ( "state" , nstate );
	}
	
	public int getState ( )
	{
		return getPropertyValueAsInteger("state");	
	}
	
	/**
	* Hace todo lo necesario para el cambio de estado.
	*
	*/
	public abstract void changeState ( World mundo );

	/**
	* Devuelve true si el estado tiene los bits a 1 en la máscara iguales que el comparando.
	*
	*/
	public boolean stateMatches ( int comparand , int mask )
	{
		return (( mask & ( getPropertyValueAsInteger("state") ^ comparand ) ) == 0 ); 
	}
	
	
	
	
	
	public org.w3c.dom.Node getPropListXMLRepresentation ( org.w3c.dom.Document doc )	
	{
		org.w3c.dom.Element e = doc.createElement("PropertyList");
		for ( int i = 0 ; i < propertiesList.size() ; i++ )
		{
			PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
			org.w3c.dom.Node nodoProp = pe.getXMLRepresentation(doc);
			e.appendChild (nodoProp);
		}
		return e;
	}
	
	//n es el nodo asociado a la entidad.
	public void readPropListFromXML ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		org.w3c.dom.Element e = null;
		try
		{
			e = (org.w3c.dom.Element) n;
		}
		catch ( ClassCastException cce )
		{
			throw ( new XMLtoWorldException ( "Entity node not Element" ) );
		}
		
		org.w3c.dom.NodeList nl = e.getElementsByTagName("PropertyList");
		
		if ( nl.getLength() <= 0 )
			//propertiesList = new ArrayList();
			; //esto ya está por defecto (inicialización atr)
		else
		{
			
			org.w3c.dom.Element elementoPropertyList = (org.w3c.dom.Element) nl.item(0);
			
			org.w3c.dom.NodeList listaPropertyEntries = elementoPropertyList.getElementsByTagName("PropertyEntry");
			
			for ( int i = 0 ; i < listaPropertyEntries.getLength() ; i++ )
			{
				org.w3c.dom.Node nod = listaPropertyEntries.item(i);
				PropertyEntry pe = new PropertyEntry ( mundo , nod );
				
				//si la propiedad ya estaba presente, quitar la copia presente.
				//esto puede suceder porque esta funcion se llame mas de una vez
				//debido a la herencia, y nos interesa que las propiedades se sobreescriban.
				String nombre = pe.getName();
				for ( int j = propertiesList.size()-1 ; j >= 0 ; j-- )
				{
					PropertyEntry actual = (PropertyEntry) propertiesList.get(j);
					if ( actual.getName().equals(nombre) )
						propertiesList.remove(actual);
				}
				
				propertiesList.add(pe);
			}
			
			
		}
			
	}
	
	
	
	public abstract int getID();
	
	
	
	
	
	
	//begin
	//relationship-related functions
	
	/*
		Las relaciones con estado entre móviles y otros objetos (o tal vez, en el
		futuro, entre entidades en general, si hiciera falta?) resultan útiles.
		Gracias a ellas, un jugador puede, por ejemplo, "recordar" lo que había
		en una habitación: al mirarla se establece un estado 1 (por ejemplo) en su
		relación con esa entidad habitación, de tal modo que cuando vuelve a ella
		ya no tiene que "buscar" algo sino que lo ve directamente, por ejemplo.
		
		No para cambios objetivos en la habitación que vería cualquiera (como el
		que se abra una puerta)
	*/
	
	public org.w3c.dom.Node getRelationshipListXMLRepresentation ( org.w3c.dom.Document doc )
	{
	
		org.w3c.dom.Element e = doc.createElement ( "RelationshipList" );
		
		for ( int i = 0 ; i < relationships.size() ; i++ )
		{
			
			org.w3c.dom.Element eRel = doc.createElement ( "Relationship" );
			
			org.w3c.dom.Element ePropList = doc.createElement ( "PropertyList" );
			
			Entity objetivo = (Entity) relationships.elementAt(i);
			
			eRel.setAttribute ( "id" , String.valueOf( objetivo.getID() ) );
			
			List propertiesList = (List) relationship_properties.elementAt(i);
			
			for ( int j = 0 ; j < propertiesList.size() ; j++ )
			{
				PropertyEntry pe = (PropertyEntry) propertiesList.get(j);
				org.w3c.dom.Node nodoProp = pe.getXMLRepresentation(doc);
				ePropList.appendChild (nodoProp);
			}
			
			eRel.appendChild ( ePropList );
			
			e.appendChild ( eRel );
		
		}
	
		return e;
	
	}
	
	//pasar directamente como parametro el nodo del Mobile.
	//esto es para carga DIFERIDA!! Son entitys, no sabemos si de las que se cargan antes o después.
	public void readRelationshipListFromXML ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		
		if ( ! ( n instanceof org.w3c.dom.Element ) ) throw ( new XMLtoWorldException ( "Mobile node not Element" ) );
		org.w3c.dom.Element e = ( org.w3c.dom.Element) n;
		org.w3c.dom.NodeList nl = e.getElementsByTagName("RelationshipList");
		if ( nl.getLength() <= 0 ) ; //no hacemos nada porque relationships y cia ya son un new Vector()
		else
		{
			org.w3c.dom.Element relationshipListNode = (org.w3c.dom.Element) nl.item(0);
			org.w3c.dom.NodeList relaciones = relationshipListNode.getElementsByTagName("Relationship");
			for ( int i = 0 ; i < relaciones.getLength() ; i++ )
			{
				
				org.w3c.dom.Element thisRelationshipNode = (org.w3c.dom.Element)relaciones.item(i);
				
				//get relationship target (id)
				if ( ! (thisRelationshipNode.hasAttribute("id")) )
					throw ( new XMLtoWorldException ( "Relationship node lacking attribute id" ) );
				relationships.add ( mundo.getObject ( thisRelationshipNode.getAttribute("id") ) );
				
				//get relationship property list
				org.w3c.dom.NodeList pListNodes = thisRelationshipNode.getElementsByTagName("PropertyList");
				List propertiesList;
				if ( pListNodes.getLength() < 0 )
					propertiesList = new ArrayList();
				else
				{
					propertiesList = new ArrayList();
					org.w3c.dom.Element elementoPropertyList = (org.w3c.dom.Element) pListNodes.item(0);
					org.w3c.dom.NodeList listaPropertyEntries = elementoPropertyList.getElementsByTagName("PropertyEntry");
					for ( int k = 0 ; k < listaPropertyEntries.getLength() ; k++ )
					{
						org.w3c.dom.Node nod = listaPropertyEntries.item(k);
						PropertyEntry pe = new PropertyEntry ( mundo , nod );
						propertiesList.add(pe);
					}			
				}
				relationship_properties.add ( propertiesList );
			}
		} //end else if length > 0 [has relationships]
	} //end method
	
	public int getRelationshipState ( Entity e )
	{
		return getRelationshipPropertyValueAsInteger ( e , "state" );
	}
	
	public int getRelationshipPropertyValueAsInteger ( Entity e , String propertyName )
	{
		int lim = relationships.size();
		for ( int k = 0 ; k < lim ; k++ )
		{
			if ( relationships.elementAt(k).equals(e) )
			{
				//lista de propiedades de la relación
				List propertiesList = (List) relationship_properties.elementAt(k);
				for ( int i = 0 ; i < propertiesList.size() ; i++ )
				{
					PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
					if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValueAsInteger();
				}
			}
		}
		return 0; //by default
	}
	
	public Object getRelationshipPropertyValueAsWrapper ( Entity e , String propertyName )
	{
		int lim = relationships.size();
		for ( int k = 0 ; k < lim ; k++ )
		{
			if ( relationships.elementAt(k).equals(e) )
			{
				//lista de propiedades de la relación
				List propertiesList = (List) relationship_properties.elementAt(k);
				for ( int i = 0 ; i < propertiesList.size() ; i++ )
				{
					PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
					if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValueAsWrapper();
				}
			}
		}
		return null; //by default
	}
	
	public String getRelationshipPropertyValueAsString ( Entity e , String propertyName )
	{
		Debug.println("Relationships size is " + relationships.size() );
		int lim = relationships.size();
		for ( int k = 0 ; k < lim ; k++ )
		{
			Debug.println("Checking a relationship. Entity: " + e.getID());
			if ( relationships.elementAt(k).equals(e) )
			{
				Debug.println("Entity found.");
				//lista de propiedades de la relación
				List propertiesList = (List) relationship_properties.elementAt(k);
				for ( int i = 0 ; i < propertiesList.size() ; i++ )
				{
					Debug.println("Name: " + ((PropertyEntry) propertiesList.get(i)).getName());
					PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
					if ( pe.getName().equalsIgnoreCase(propertyName) )
						return pe.getValue();
				}
			}
		}
		return null; //by default
	}
	
	public List getRelatedEntities ( String propertyName )
	{
		int lim = relationships.size();
		List result = new ArrayList();
		for ( int k = 0 ; k < lim ; k++ )
		{
			Entity other = (Entity) relationships.elementAt(k);
			List propertiesList = (List) relationship_properties.elementAt(k);
			for ( int i = 0 ; i < propertiesList.size() ; i++ )
			{
				PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
				if ( pe.getName().equalsIgnoreCase(propertyName) )
				{
					result.add ( other );
					break;
				}
			}
		}
		return result;
	}
	
	
	public List getRelatedEntitiesByValue ( String propertyName , boolean boolVal )
	{
		int lim = relationships.size();
		List result = new ArrayList();
		for ( int k = 0 ; k < lim ; k++ )
		{
			Entity other = (Entity) relationships.elementAt(k);
			List propertiesList = (List) relationship_properties.elementAt(k);
			for ( int i = 0 ; i < propertiesList.size() ; i++ )
			{
				PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
				if ( pe.getName().equalsIgnoreCase(propertyName) && pe.getValueAsBoolean() == boolVal )
				{
					result.add ( other );
					break;
				}
			}
		}
		return result;
	}
	
	public List getRelatedEntitiesByValue ( String propertyName , int intVal )
	{
		int lim = relationships.size();
		List result = new ArrayList();
		for ( int k = 0 ; k < lim ; k++ )
		{
			Entity other = (Entity) relationships.elementAt(k);
			List propertiesList = (List) relationship_properties.elementAt(k);
			for ( int i = 0 ; i < propertiesList.size() ; i++ )
			{
				PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
				if ( pe.getName().equalsIgnoreCase(propertyName) && pe.getValueAsInteger() == intVal )
				{
					result.add ( other );
					break;
				}
			}
		}
		return result;
	}
	
	public boolean getRelationshipPropertyValueAsBoolean ( Entity e , String propertyName )
	{
		int lim = relationships.size();
		for ( int k = 0 ; k < lim ; k++ )
		{
			if ( relationships.elementAt(k).equals(e) )
			{
				//lista de propiedades de la relación
				List propertiesList = (List) relationship_properties.elementAt(k);
				for ( int i = 0 ; i < propertiesList.size() ; i++ )
				{
					PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
					if ( pe.getName().equalsIgnoreCase(propertyName) )
					return pe.getValueAsBoolean();
				}
			}
		}
		return false; //by default
	}
	
	public void setRelationshipProperty ( Entity e , String propertyName , String propertyValue )
	{
		int lim = relationships.size();
		//Debug.println("On setting string relationship property " + propertyName + ", size is " + relationships.size());
		for ( int k = 0 ; k < lim ; k++ )
		{
			if ( relationships.elementAt(k).equals(e) )
			{
				List propertiesList = (List) relationship_properties.elementAt(k);
				for ( int i = 0 ; i < propertiesList.size() ; i++ )
				{
					PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
					if ( pe.getName().equalsIgnoreCase(propertyName) )
					{
						pe.setValue ( propertyValue );
						return;
					}	
				}
				//property not found, add to list
				propertiesList.add ( new PropertyEntry ( propertyName , propertyValue , 0 ) );
				return;
			}
		}
		//relationship not found, add to relationships
		relationships.addElement ( e );
		List nuevaPropEntryList = new ArrayList();
		PropertyEntry pe = new PropertyEntry( propertyName , propertyValue , 0 );
		nuevaPropEntryList.add(pe);
		relationship_properties.addElement(nuevaPropEntryList);
		return;
	}
	
	
	//creo que se puede eliminar y dejar sólo el de String, hacen the same
	public void setRelationshipProperty ( Entity e , String propertyName , Object propertyValue )
	{
		int lim = relationships.size();
		for ( int k = 0 ; k < lim ; k++ )
		{
			if ( relationships.elementAt(k).equals(e) )
			{
				List propertiesList = (List) relationship_properties.elementAt(k);
				for ( int i = 0 ; i < propertiesList.size() ; i++ )
				{
					PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
					if ( pe.getName().equalsIgnoreCase(propertyName) )
					{
						pe.setValue ( propertyValue );
						return;
					}	
				}
				//property not found, add to list
				PropertyEntry pe = new PropertyEntry ( propertyName , propertyValue.toString() , 0 );
				pe.setObjectValue(propertyValue);
				propertiesList.add ( pe );
				return;
			}
		}
		//relationship not found, add to relationships
		relationships.addElement ( e );
		List nuevaPropEntryList = new ArrayList();
		PropertyEntry pe = new PropertyEntry( propertyName , propertyValue.toString() , 0 );
		pe.setObjectValue(propertyValue);
		nuevaPropEntryList.add(pe);
		relationship_properties.addElement(nuevaPropEntryList);
		return;
	}
	
	
	
	public void setRelationshipProperty ( Entity e , String propertyName , int propertyValue )
	{
		//Debug.println("On setting int relationship property " + propertyName + ", size is " + relationships.size());
		setRelationshipProperty ( e , propertyName , String.valueOf ( propertyValue ) );
		//Debug.println("After setting int relationship property " + propertyName + ", size is " + relationships.size());
	}
	
	public void setRelationshipProperty ( Entity e , String propertyName , boolean propertyValue )
	{
		//Debug.println("On setting bool relationship property " + propertyName + ", size is " + relationships.size());
		setRelationshipProperty ( e , propertyName , String.valueOf ( propertyValue ) );
		//Debug.println("After setting bool relationship property " + propertyName + ", size is " + relationships.size());
	}
	
	public void setRelationshipPropertyTimeLeft ( Entity e  , String propertyName , long newtime )
	{
		int lim = relationships.size();
		//Debug.println("On setting string relationship property " + propertyName + ", size is " + relationships.size());
		for ( int k = 0 ; k < lim ; k++ )
		{
			if ( relationships.elementAt(k).equals(e) )
			{
				List propertiesList = (List) relationship_properties.elementAt(k);
				for ( int i = 0 ; i < propertiesList.size() ; i++ )
				{
					PropertyEntry pe = (PropertyEntry) propertiesList.get(i);
					if ( pe.getName().equalsIgnoreCase(propertyName) )
					{
						pe.setTime ( newtime );
						return;
					}	
				}
				//property not found, add to list
				propertiesList.add ( new PropertyEntry ( propertyName , "false" , newtime ) );
				return;
			}
		}
		//relationship not found, add to relationships
		relationships.addElement ( e );
		List nuevaPropEntryList = new ArrayList();
		PropertyEntry pe = new PropertyEntry( propertyName , "false" , newtime );
		nuevaPropEntryList.add(pe);
		relationship_properties.addElement(nuevaPropEntryList);
		return;
	}
	
	public void setRelationshipState ( Entity e , int newState )
	{
		setRelationshipProperty ( e , "state" , newState );
	
	}
	
	
	public String toString()
	{
	
		String s = ("[ " + getClass().getName() + ":" + getID() );
		if ( this instanceof Nameable )
		{
			s += ":";
			s += ((Nameable)this).constructName2(1,null);
		}
		s+=" ]";
		return s;
	}
	
	
	
	//end
	//relationship-related functions
	//
	//
	
	
	
	
	
	
	
	
}