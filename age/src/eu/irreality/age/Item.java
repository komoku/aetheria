/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
//package AetheriaAWT;
import java.util.*;
import java.io.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
public class Item extends Entity implements Descriptible , SupportingCode , Nameable
{
	
	//////////////////////
	//INSTANCE VARIABLES//
	//////////////////////
	
	/**Tipo.*/
	/*00*/ private String itemType;
	
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
	
	/**Lista dinámica de descripciones.*/
	/*10*/ protected Description[] descriptionList;
	/**Lista dinámica de nombres en singular. (espada, espada oxidada... son la misma espada con varios estados)*/
	/*11*/ protected Description[] singNames;
	/**Lista dinámica de nombres en plural.*/
	/*12*/ protected Description[] plurNames;
	/**Género.*/
	/*13*/ protected boolean gender; //true=masculino
	/**Responder a... (comandos)*/
	/*14*/ protected String respondToSing;
	/*15*/ protected String respondToPlur;
	
	
	/**Volumen.*/
	/*17*/ protected int volume;
	/**Peso.*/
	/*16*/ protected int weight;
	
	/**Inventario de contenedor.*/
	/*20*/ protected String inventoryString;
		   protected Inventory inventory;
	/**Inventario de item compuesto.*/
		   protected String partsInventoryString;
		   protected Inventory partsInventory;
	
	/**Lista dinámica de personajes. ¿necesario?*/
	/*22*/ protected Vector characterRefs;
	/*21*/ protected Vector mobRefs;
	
	/**Descripciones de subcosas del objeto.*/
	/*30*/ protected String extraDescriptions; //OLD
	protected List extraDescriptionArrays = new ArrayList(); /*List of Description Arrays*/
	protected List extraDescriptionNameArrays = new ArrayList(); /*List of String Arrays*/
	/**Restricciones de acceso y otras.*/
	/*31*/ protected Vector onlyRestrictions;
	/**Lista dinámica de descripciones de apertura.*/
	/*32*/ protected Description[] openDescriptionList = null;
	/**Lista dinámica de descripciones de cierre.*/
	/*33*/ protected Description[] closeDescriptionList = null;
	/**Lista dinámica de descripciones de apertura con llave.*/
	/*34*/ protected Description[] unlockDescriptionList = null;
	/**Lista dinámica de descripciones de cierre con llave.*/
	/*35*/ protected Description[] lockDescriptionList = null;
	/**Lista dinámica de llaves que lo abren, si el item es abrible/cerrable.*/
	/**Ojo: la llave que abre un item tiene que ser anterior en número...*/
	/*36*/ protected Inventory keys;
	
	//Bitvectors
	/*40*/ protected boolean enabled;
	/*41*/ protected boolean isVirtual;
	/*42*/ protected boolean canGet;
	
	/**Código en Ensamblador Virtual Aetheria (EVA)*/
	/*80*/ protected ObjectCode itsCode;
	
	
	//emulador de numeros aleatorios
	private Random aleat;
	
	//mundo
	private World mundo;
	
	
	protected boolean properName = false; //marks names by proper as default
	
	
	protected transient List rooms = new ArrayList(); //habitaciones donde está el item.
	protected transient List mobiles = new ArrayList(); //mobiles donde está el item.
	//esto se irá rellenando al hacer addItem().
	
	public void addRoomReference ( Room r )
	{
		rooms.add(r);
	}	
	public void removeRoomReference ( Room r )
	{
		rooms.remove(r);
	}
	public List getRoomReferences ( )
	{
		return rooms;
	}
	public void addMobileReference ( Mobile m )
	{
		mobiles.add(m);
	}	
	public void removeMobileReference ( Mobile m )
	{
		mobiles.remove(m);
	}
	public List getMobileReferences ( )
	{
		return mobiles;
	}
	
	
	public boolean isGettable()
	{
		return canGet;
	}
	
	public void setGettable ( boolean gettable )
	{
		canGet = gettable;
	}
	
	
	//crea un nuevo Item que hereda de éste y lo añade al mundo
	public Item createNewInstance( World mundo , boolean cloneContents , boolean cloneParts ) 
	{
		Item it = (Item) this.clone();
		this.mundo=mundo;
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
		
		if ( cloneContents && it.inventory != null )
		{
			//poner inventario de copias
			it.inventory = it.inventory.cloneCopyingItems(mundo,cloneContents,cloneParts);
		}
		if ( cloneParts && it.inventory != null )
		{
			it.partsInventory = it.partsInventory.cloneCopyingItems(mundo,cloneContents,cloneParts);
		}
		
		mundo.addItemAssigningID ( it );
		
		return it;
	}
	
	public Object clone( )
	{
		//do it!
		
		Item it = new Item();
		
		copyItemFieldsTo(it);
		
		return it;
	}
	

	//notación un poco inconsistente con la de Entity::copyEntityFields; pero bueno
	public void copyItemFieldsTo ( Item it )
	{
	
		it.copyEntityFields(this); //estados, propiedades, etc.
	
		//Debug.println("Random from SOURCE item: " + getRandom());
	
		it.aleat = getRandom();
		it.canGet=canGet;
		it.enabled = enabled;
		it.extraDescriptions = extraDescriptions;
		it.gender = gender;
		it.idnumber = idnumber; //will have to change the ID later!
		//it.inheritsFrom = 0; //creates an identical (strong inherit) item -> No weak inherit
		//nay, identical copy
		it.inheritsFrom = inheritsFrom;
		
		if ( inventory != null )
			it.inventory = (Inventory) inventory.clone();
		else it.inventory = null;
		
		it.inventoryString = inventoryString;
		//it.isInstanceOf = idnumber; //creates an identical (strong inherit) item
		//nay, identical copy
		it.isInstanceOf = isInstanceOf;
		it.isVirtual = isVirtual;
		it.itemType = itemType;
		
		if ( itsCode != null )
			it.itsCode = //itsCode; //code is unmodifiable (shallow copy)
				itsCode.cloneIfNecessary();
		else
			it.itsCode = null;
		
		if ( keys != null )
			it.keys = (Inventory) keys.clone();
		else it.keys = null;
		
		
		if ( partsInventory != null )
			it.partsInventory = (Inventory) partsInventory.clone();
		else
			it.partsInventory = null;
		
		it.partsInventoryString = partsInventoryString;	

		it.respondToPlur = respondToPlur;
		it.respondToSing = respondToSing;
		it.title = title;
		it.volume = volume;
		it.weight = weight;
		
		it.properName = properName;
		
		if ( closeDescriptionList != null )
		{
			it.closeDescriptionList = new Description[closeDescriptionList.length];
			for ( int i = 0 ; i < it.closeDescriptionList.length ; i++ )
			{
				it.closeDescriptionList[i] = (Description) closeDescriptionList[i].clone();
			}
		}
		it.descriptionList = new Description[descriptionList.length];
		for ( int i = 0 ; i < it.descriptionList.length ; i++ )
		{
			it.descriptionList[i] = (Description) descriptionList[i].clone();
		}
		
		if ( lockDescriptionList != null )
		{
			it.lockDescriptionList = new Description[lockDescriptionList.length];
			for ( int i = 0 ; i < it.lockDescriptionList.length ; i++ )
			{
			it.openDescriptionList[i] = (Description) openDescriptionList[i].clone();
			}
		}
		
		if ( openDescriptionList != null )
		{				
			it.openDescriptionList = new Description[openDescriptionList.length];
			for ( int i = 0 ; i < it.lockDescriptionList.length ; i++ )
			{
				it.unlockDescriptionList[i] = (Description) unlockDescriptionList[i].clone();
			}
		}
		
		if ( unlockDescriptionList != null )
		{
			it.unlockDescriptionList = new Description[unlockDescriptionList.length];
			for ( int i = 0 ; i < it.lockDescriptionList.length ; i++ )
			{
				it.lockDescriptionList[i] = (Description) lockDescriptionList[i].clone();
			}
		}
		
		it.singNames = new Description[singNames.length];
		for ( int i = 0 ; i < it.singNames.length ; i++ )
		{
			it.singNames[i] = (Description) singNames[i].clone();
		}
		it.plurNames = new Description[plurNames.length];
		for ( int i = 0 ; i < it.plurNames.length ; i++ )
		{
			it.plurNames[i] = (Description) plurNames[i].clone();
		}
		
		//mob refs and only restrictions are obsolete, do not copy
	
	
	
	}
	
	
	
	///////////
	//METHODS//
	///////////
	
	public Item ( )
	{
	
	}
	
	/**
	* Este constructor solo llama a constructItem, que es el constructor de verdad. Esta dualidad se debe a las llamadas recursivas que debe hacer constructRoom para soportar herencia dinamica (items que copian datos de otros)
	*
	*/
	public Item ( World mundo , String itemfile ) throws IOException , FileNotFoundException
	{
		constructItem ( mundo , itemfile , true , "none" );
	}
	
	public Item ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		constructItem ( mundo , n , true , "none" );
	}
	
	/**
	* Constructor para ser llamado desde fuera, construye todas las subclases de Item según el fichero
	*
	*/
	public static Item getInstance ( World mundo , String itemfile ) throws FileNotFoundException, IOException
	{
		String linea;
		String id_linea;
		FileInputStream fp = new FileInputStream ( itemfile );
		BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( fp ) );
		//leer el tipo de item
		String itemtype = "none";
		for ( int line = 1 ; line < 100 ; line++ )
		{
			linea = filein.readLine();
			id_linea = StringMethods.getTok( linea , 1 , ' ' );
			linea = StringMethods.getToks( linea , 2 , StringMethods.numToks( linea , ' ' ) , ' ' );	
			try
			{
				if ( id_linea != null && Integer.valueOf(id_linea).intValue() == 0 )
					itemtype = linea;
			}
			catch ( NumberFormatException ex ) 
			{
				//no pasa nada, estaremos en una parte con codigo, etc.
			}	
		}
		//crear el objeto
		Item ourNewItem;
		if ( itemtype.equalsIgnoreCase("weapon") )
		{
			ourNewItem = new Weapon ( mundo , itemfile );
			//llama a ourNewItem.constructItem ( mundo , itemfile , true , "weapon" );
		}
		else if ( itemtype.equalsIgnoreCase("wearable") )
		{
			ourNewItem = new Wearable ( mundo , itemfile );
		}
		else
		{
			ourNewItem = new Item ( mundo , itemfile );
			//llama a ourNewItem.constructItem ( mundo , itemfile , true , "none" );
		}
		return ourNewItem;
	}
	
	public static Item getInstance ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException("Item node not Element") );
		}
		
		//{n is an Element}
		org.w3c.dom.Element e = (org.w3c.dom.Element)n;
		
		Item ourNewItem;
		
		if ( !e.hasAttribute("type") )
		{
			ourNewItem = new Item ( mundo , n );
		}
		else if ( e.getAttribute("type").equalsIgnoreCase("weapon") )
		{
			ourNewItem = new Weapon ( mundo , n );
		}
		else if ( e.getAttribute("type").equalsIgnoreCase("wearable") )
		{
			ourNewItem = new Wearable ( mundo , n );
		}
		else
		{
			ourNewItem = new Item ( mundo , n );
		}
		return ourNewItem;
	}
	
	/**
	* El pedazo de constructor que lee un item de un fichero.
	*
	*/
	public void constructItem ( World mundo , String itemfile , boolean allowInheritance , String itemtype ) throws IOException, FileNotFoundException
	{
		this.mundo=mundo;
		String linea;
		String id_linea;
		FileInputStream fp = new FileInputStream ( itemfile );
		BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( fp ) );
		itemType = itemtype;
		for ( int line = 1 ; line < 100 ; line++ )
		{
			linea = filein.readLine();
			id_linea = StringMethods.getTok( linea , 1 , ' ' );
			linea = StringMethods.getToks( linea , 2 , StringMethods.numToks( linea , ' ' ) , ' ' );
			if ( id_linea != null ) switch ( Integer.valueOf(id_linea).intValue() )
			{
			//	case 0:
			//		if ( linea.equalsIgnoreCase("weapon") )
			//			isweapon = true;
			//		break;	
				case 1:
					idnumber = Integer.valueOf(linea).intValue(); break;
				case 2:
					inheritsFrom = Integer.valueOf(linea).intValue(); 
					if ( inheritsFrom < idnumber && allowInheritance ) /*el item del que heredamos debe tener ID menor*/
					{
						/*construimos segun constructor de la habitacion de que heredamos*/
						constructItem ( mundo, Utility.itemFile(mundo,inheritsFrom) , true , itemtype ); 
						/*overrideamos lo que tengamos que overridear*/
						constructItem ( mundo, itemfile , false , itemtype );
						return; 
					}
					break;
				case 3:
					//state = Integer.valueOf(linea).intValue(); break;
					setNewState(Integer.valueOf(linea).intValue()); break;
				case 4:
					title = linea; break;	
				case 5:
					/*la herencia fuerte se hace exactamente igual que la débil, no pueden aparecer las dos (se ignoraría la 2a)*/
					isInstanceOf = Integer.valueOf(linea).intValue(); 
					if ( isInstanceOf < idnumber && allowInheritance ) /*el item del que heredamos debe tener ID menor*/
					{
						/*construimos segun constructor de la habitacion de que heredamos*/
						constructItem ( mundo, Utility.itemFile(mundo,isInstanceOf) , true , itemtype ); 
						/*overrideamos lo que tengamos que overridear*/
						constructItem ( mundo, itemfile , false , itemtype );
						return; 
					}
					break;
					
				case 10:
				//item description list line
				{
					descriptionList=Utility.loadDescriptionListFromString( linea );	
					break;
				}
				case 11:
				//singular names line
				{
					singNames = Utility.loadDescriptionListFromString( linea ); break;
				}
				case 12:
				//plural names line
				{
					plurNames = Utility.loadDescriptionListFromString( linea ); break;
				}
				case 13:
				//gender line
				{
					int temp = Integer.valueOf(linea).intValue();
					if ( temp == 0 ) gender = false;
					else gender = true;
					break;
				}
				case 14:
				//respondToSing
				{
					respondToSing = linea; break;
				}
				case 15:
				//respondToPlur
				{
					respondToPlur = linea; break;	
				}
				case 16:
				//weight line
				{
					weight = Integer.valueOf(linea).intValue(); break;
				}
				case 17:
				//gender line
				{
					volume = Integer.valueOf(linea).intValue(); break;
				}
				case 20:
				//container's inventory line
				{
					inventoryString = linea; break;
					//la carga del inventario está diferida, pues puede contener objetos
					//de número más alto que el actual, y que por lo tanto aún no están
					//cargados en memoria cuando se carga éste.
				}
				
				case 32:
				{
					//description of action of opening item
					openDescriptionList = Utility.loadDescriptionListFromString( linea );
					break;				
				}
				case 33:
				{
					//description of action of closing item
					closeDescriptionList = Utility.loadDescriptionListFromString( linea );
					break;
				}
				case 34:
				{
					//description of action of unlocking item
					unlockDescriptionList = Utility.loadDescriptionListFromString( linea );
					break;
				}
				case 35:
				{
					//description of action of locking item
					lockDescriptionList = Utility.loadDescriptionListFromString( linea );
					break;
				}
				case 36:
				{
					//list of keys which may unlock lockable/unlockable items
					int nkeys = StringMethods.numToks(linea,'&');
					keys = new Inventory ( 10000000 , 1000000 );
					for ( int i = 1 ; i <= nkeys ; i++ )
					{
						//primero, comprobar que la ID es menor que la del item actual
						int key_id = Integer.valueOf( StringMethods.getTok(linea,i,'&')  ).intValue();
						if ( key_id > idnumber )
						{
							mundo.write("Warning! La ID de una llave no puede ser mayor que la del objeto que abre (" + key_id + " > " + idnumber + ")\n");
						}
						else
						{
							try
							{
								keys.addItem ( mundo.getItem( key_id  ) );
							}
							catch (Exception exc)
							{
								mundo.write("Excepción absurda (llave pesada)" + exc);
							}
						}
					}
					break;	
				}
				case 80:
				//begin EVA code line
				{
					String EVACodeString = linea;
					boolean terminamos = false;
					
					while ( !terminamos )
					{
						linea = filein.readLine();
						id_linea = StringMethods.getTok(linea,1,' ');
						int intval;
						try
						{
							intval = Integer.valueOf(id_linea).intValue();
						}
						catch ( NumberFormatException e )
						{
							intval=0;
						}
						if ( intval == 81 ) terminamos=true; //EVA code termination line
						else
						{
							EVACodeString += "\n";
							EVACodeString += linea;
						}
					}	
					
					itsCode = new ObjectCode ( EVACodeString , "EVA" , mundo );
					
					break;

				} //end case 80
				
				case 84:
				//begin BeanShell code line
				{
					String bshCodeString = linea;
					boolean terminamos = false;
					
					while ( !terminamos )
					{
						linea = filein.readLine();
						id_linea = StringMethods.getTok(linea,1,' ');
						int intval;
						try
						{
							intval = Integer.valueOf(id_linea).intValue();
						}
						catch ( NumberFormatException e )
						{
							intval=0;
						}
						if ( intval == 85 ) terminamos=true; //EVA code termination line
						else
						{
							bshCodeString += "\n";
							bshCodeString += linea;
						}
					}	
					
					itsCode = new ObjectCode ( bshCodeString , "BeanShell" , mundo );

					break;

				} //end case 84
				
			} //end case switch	
		} //end for
		if ( itemtype.equalsIgnoreCase("weapon") )
		{
			((Weapon)this).readWeaponSpecifics ( mundo , itemfile );
		}
		/*
		else if ( itemtype.equalsIgnoreCase("wearable") )
		{
			((Wearable)this).readWearableSpecifics ( mundo , itemfile );
		}
		*/
		//poner bien la id
			if ( getID() < 10000000 )
			idnumber += 30000000; //prefijo de objeto	
	}
	
	public void constructItem ( World mundo , org.w3c.dom.Node n , boolean allowInheritance , String itemtype ) throws XMLtoWorldException
	{
	
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ( "Item node not Element" ) );
		}
		//{n is an Element}	
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;
	
		//type	
		itemType = itemtype;
		
		//world
		this.mundo=mundo;
	
		//default values
		canGet=true; isVirtual=false; enabled=true;
	
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
			
			constructItem ( mundo , mundo.getItemNode( e.getAttribute("extends") ) , true , itemtype );
			
			//2. overrideamos lo que debamos overridear
			
			constructItem ( mundo , n , false , itemtype );
			
			return;
			
		}
		
		//strong inheritance?
		if ( e.hasAttribute("clones") && !e.getAttribute("clones").equals("0") && allowInheritance )
		{
			//funciona igual que la weak inheritance a este nivel.
			//no deberian aparecer los dos; pero si asi fuera esta herencia (la fuerte) tendria precedencia.
			
			//1. overrideamos el super-item usando su associated node para construirlo
			
			constructItem ( mundo , mundo.getItemNode( e.getAttribute("clones") ) , true , itemtype );
			
			Debug.println("Overridden item gender is " + gender);
			
			//2. overrideamos lo que debamos overridear
			
			constructItem ( mundo , n , false , itemtype );
			
			Debug.println("Overridden item gender is now " + gender);
		
			return;
			
		}
		
		//Debug.println("IID = " + e.getAttribute("id") + ", " + allowInheritance);
		
		//mandatory XML-attribs exceptions
		//if ( !e.hasAttribute("id") )
		//	throw ( new XMLtoWorldException ( "Item node lacks attribute id" ) );
		if ( !e.hasAttribute("name") )
			throw ( new XMLtoWorldException ( "Item node lacks attribute name" ) );
		if ( !e.hasAttribute("volume") && allowInheritance )
			throw ( new XMLtoWorldException ( "Item node lacks attribute volume, id=" + e.getAttribute("id") ) );		
		if ( !e.hasAttribute("weight") && allowInheritance )
			throw ( new XMLtoWorldException ( "Item node lacks attribute weight, id=" + e.getAttribute("id") ) );
		if ( !e.hasAttribute("gender") && allowInheritance )
			throw ( new XMLtoWorldException ( "Item node lacks attribute gender, id=" + e.getAttribute("id") ) );	
	
		//mandatory XML-attribs parsing
		try
		{
			//id no longer mandatory
			if ( e.hasAttribute("id") )
				idnumber = Integer.valueOf ( e.getAttribute("id") ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			throw ( new XMLtoWorldException ( "Bad number format at attribute id in item node" ) );
		}
		title = e.getAttribute("name");
		
		if ( ! gender ) //si es true es porque lo hemos heredado true (!=default)
			gender = Boolean.valueOf ( e.getAttribute("gender") ).booleanValue();
		
		Debug.println("Gender has been set to " + gender + " for " + title);
		try
		{
			weight = Integer.valueOf ( e.getAttribute("weight") ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			if ( allowInheritance )
				throw ( new XMLtoWorldException ( "Bad number format at attribute weight in item node" ) );
		}
		try
		{
			volume = Integer.valueOf ( e.getAttribute("volume") ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			if ( allowInheritance )
				throw ( new XMLtoWorldException ( "Bad number format at attribute volume in item node" ) );
		}
		
		//non-mandatory attribs
		if ( e.hasAttribute("enabled") )
		{
			enabled = Boolean.valueOf ( e.getAttribute("enabled") ).booleanValue();
		}
		if ( e.hasAttribute("isVirtual") )
		{
			isVirtual = Boolean.valueOf ( e.getAttribute("isVirtual") ).booleanValue();
		}
		if ( e.hasAttribute("canGet") )
		{
			canGet = Boolean.valueOf ( e.getAttribute("canGet") ).booleanValue();
		}
		if ( e.hasAttribute("properName") )
		{
			properName = Boolean.valueOf ( e.getAttribute("properName") ).booleanValue();
		}
		
		//Entity parsing
		readPropListFromXML ( mundo , n );
		
		
		//de momento no leeremos openable,closeable,lockable,unlockable:
		//dependerán de si las description lists son o no nulas
		
		
		
		//description list (same as in Path)
		//WE CAN REWRITE ALL THESE USING "Utility.loadDescriptionListFromXML()" AND MAKE
		//THEM SHORTER. DO IT IF NEEDIN' TO MODIFY. 
		org.w3c.dom.NodeList descrListNodes = e.getElementsByTagName( "DescriptionList" );
		if ( descrListNodes.getLength() > 0 )
		{
			org.w3c.dom.Element descrListNode = (org.w3c.dom.Element) descrListNodes.item(0);
			org.w3c.dom.NodeList descrNodes = descrListNode.getElementsByTagName ( "Description" );
			descriptionList = new Description[descrNodes.getLength()];
			for ( int i = 0 ; i < descrNodes.getLength() ; i++ )
			{
				org.w3c.dom.Element descrNode = (org.w3c.dom.Element) descrNodes.item(i);
				try
				{
					descriptionList[i] = new Description(mundo,descrNode);
				}
				catch ( XMLtoWorldException xe )
				{
					throw ( new XMLtoWorldException ( "Error at item description: " + xe.getMessage()  ) );
				}
			}
		}
		//singNames (same)
		descrListNodes = e.getElementsByTagName( "SingularNames" );
		if ( descrListNodes.getLength() > 0 )
		{
			org.w3c.dom.Element descrListNode = (org.w3c.dom.Element) descrListNodes.item(0);
			org.w3c.dom.NodeList descrNodes = descrListNode.getElementsByTagName ( "Description" );
			singNames = new Description[descrNodes.getLength()];
			for ( int i = 0 ; i < descrNodes.getLength() ; i++ )
			{
				org.w3c.dom.Element descrNode = (org.w3c.dom.Element) descrNodes.item(i);
				try
				{
					singNames[i] = new Description(mundo,descrNode);
				}
				catch ( XMLtoWorldException xe )
				{
					throw ( new XMLtoWorldException ( "Error at item description: " + xe.getMessage()  ) );
				}
			}
		}
		//plurNames (same)
		descrListNodes = e.getElementsByTagName( "PluralNames" );
		if ( descrListNodes.getLength() > 0 )
		{
			org.w3c.dom.Element descrListNode = (org.w3c.dom.Element) descrListNodes.item(0);
			org.w3c.dom.NodeList descrNodes = descrListNode.getElementsByTagName ( "Description" );
			plurNames = new Description[descrNodes.getLength()];
			for ( int i = 0 ; i < descrNodes.getLength() ; i++ )
			{
				org.w3c.dom.Element descrNode = (org.w3c.dom.Element) descrNodes.item(i);
				try
				{
					plurNames[i] = new Description(mundo,descrNode);
				}
				catch ( XMLtoWorldException xe )
				{
					throw ( new XMLtoWorldException ( "Error at item description: " + xe.getMessage()  ) );
				}
			}
		}
		//singular reference names (respondToSing), XML format is:
		//<SingularReferenceNames><Name>nombre1</Name><Name>nombre2</Name>...</SingularReferenceNames>
		//while internal format is
		//nombre1$nombre2
		//WE CAN REWRITE ALL THESE USING "Utility.loadNameListFromXML()" AND MAKE
		//THEM SHORTER. DO IT IF NEEDIN' TO MODIFY. 
		org.w3c.dom.NodeList singRefNamesNodes = e.getElementsByTagName("SingularReferenceNames" );
		if ( singRefNamesNodes.getLength() > 0 )
		{
			org.w3c.dom.Element singRefNamesNode = (org.w3c.dom.Element)singRefNamesNodes.item(0);
			org.w3c.dom.NodeList nameNodes = singRefNamesNode.getElementsByTagName("Name");
			
			//init respondToSing
			respondToSing = "";
			
			for ( int i = 0 ; i < nameNodes.getLength() ; i++ )
			{
				//get this name node
				org.w3c.dom.Element nameNode = (org.w3c.dom.Element) nameNodes.item(i);
				
				//get first text node in this name node -- WE ASSUME THERE IS ONE!!
				org.w3c.dom.Node hijo = nameNode.getFirstChild();
				while ( !( hijo instanceof org.w3c.dom.Text ) )
					hijo = hijo.getNextSibling();
				
				//{hijo is an org.w3c.dom.Text}
				
				respondToSing += ( hijo.getNodeValue() );			
					
				if ( i < nameNodes.getLength()-1 ) //i.e. not last
					respondToSing += "$";	
			}
		}
		//plural reference names (respondToPlur) just same as singular reference names.
		
		respondToPlur = Utility.loadNameListFromXML ( mundo , e , "PluralReferenceNames" , true );
		
		//inventory:
		//NO lo hacemos ahora: es una carga DIFERIDA. Repito: DIFERIDA.
		//(para construir un inventory a partir de un Node estamos pidiendo que todos los items ya existan)
		//como guardamos el Node en el World, ya haremos esta carga.
		
		//keys inventory:
		//también es una carga DIFERIDA.
				
		//extra descriptions:
		//XML format is:
		//<ExtraDescriptionList> <ExtraDescription> <Name>cosa</Name> <Name>objeto</Name> la descripción de la cosa, también llamada objeto </ExtraDescription> <ExtraDescription> ... </ExtraDescription> </ExtraDescriptionList>
		//internal representation is:
		//cosa$objeto$la descripción de la cosa, también llamada objeto@[otra descripción]@...		
		
		//extraDescriptions = Utility.loadExtraDescriptionsFromXML ( e , "ExtraDescriptionList" , true );
		
		List temp = Utility.loadExtraDescriptionsFromXML ( mundo , e , "ExtraDescriptionList" , true );
		if ( temp == null || temp.size() < 2 )
		{
			extraDescriptionArrays = new ArrayList();
			extraDescriptionNameArrays = new ArrayList();
		}
		else
		{
			extraDescriptionArrays = (List) temp.get(1);
			extraDescriptionNameArrays = (List) temp.get(0);
		}
		
		
		
		//open, close, lock, unlock description lists.
		//code written in the new format, i.e. using the Utility function
		
		Description[] dl = Utility.loadDescriptionListFromXML ( mundo , e , "OpenDescriptionList" , true );
		if ( dl != null )
			openDescriptionList = dl; //if null, preserve inherited
		
		dl = Utility.loadDescriptionListFromXML ( mundo , e , "CloseDescriptionList" , true );
		if ( dl != null )
			closeDescriptionList = dl;
		
		dl = Utility.loadDescriptionListFromXML ( mundo , e , "LockDescriptionList" , true );
		if ( dl != null )
			lockDescriptionList = dl;
		
		dl = Utility.loadDescriptionListFromXML ( mundo , e , "UnlockDescriptionList" , true );	
		if ( dl != null )
			unlockDescriptionList = dl;
			
		//then do:
		// - World diferidas support (compile errors on this file)
	
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
		
		if ( itemtype.equalsIgnoreCase("weapon") )
		{
			((Weapon)this).readWeaponSpecifics ( mundo , e );
		}
		else if ( itemtype.equalsIgnoreCase("wearable") )
		{
			((Wearable)this).readWearableSpecifics ( mundo , e );
		}
		//poner bien la id
		if ( getID() < 10000000 )
			idnumber += 30000000; //prefijo de objeto	
	
		
		//eventos onInit()
		try
		{
			boolean ejecutado = execCode ( "onInit" ,
			new Object[]
			{
			}
			);
			
		}
		catch ( bsh.TargetError te )
		{
			te.printStackTrace();
			mundo.writeError("BeanShell error on initting item " + this + ": error was " + te);
			mundo.writeError(ExceptionPrinter.getExceptionReport(te));
		}
		
	}
	

	
	
	public int getID ( )
	{
		return idnumber;	
	}
	
	public String getTitle ( )
	{
		return title;	
	}
	
	public int getWeight()
	{
	    return weight;
	}
	
	public int getTotalWeight ( )
	{
	    int totalWeight = weight;
	    if ( inventory != null )
		totalWeight += inventory.getWeight();
	    return totalWeight;
	}
	
	public int getVolume ( )
	{
		return volume;
	}
	
	public void setVolume ( int volume )
	{
		this.volume = volume;
	}
	
	public String getDescription ( long comparand )
	{
		String desString="";
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			if ( descriptionList[i].matches(comparand) )
				desString += descriptionList[i].getText();
		}	
		if ( !isContainer() )
		{
			return desString;	
		}
		else if ( inventory.isEmpty() ) //puede contener items; pero no. Está vacío.
		{
			//si la descripción está construida con la conocida fórmula "hay x" o "lleva x", cambiamos por "no hay nada" o "no lleva nada".
			//si no, simplemente por "nada".
			return StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( desString , "lleva %INVENTORY" , "no lleva nada." ) , "hay %INVENTORY" , "no hay nada." ) , "%INVENTORY" , "nada." );
		}
		else
		{
			return StringMethods.textualSubstitution ( desString , "%INVENTORY" , inventory.toString() );
		}
	}
	
	//in order for bsh getDescription() to be able to use native getDescription()
	//without falling in infinite recursivity
	transient boolean getDescription_bsh_call = false;
	
	public String getDescription ( Entity viewer )
	{
	
		//bsh getDescription override support
		
		
		if ( !getDescription_bsh_call ) //avoid infinite recursivity if bsh calls this method
		{
			
			boolean ejecutado = false;
			ReturnValue retval = new ReturnValue ( null );
			try
			{
			
				getDescription_bsh_call = true;
			
				ejecutado = execCode ( "getDescription" ,
				new Object[]
				{
					viewer
				}
				, retval
				);
				
			}
			catch ( bsh.TargetError te )
			{
				te.printStackTrace();
			}
			finally
			{
				getDescription_bsh_call = false;
			}
			
			if ( retval.getRetVal() != null )
				return (String)retval.getRetVal();
			else if ( ejecutado ) //sobreescribe comportamiento por defecto
				return null;	
			
		}
	
		//normal (non-bsh) description generation
	
		String desString="";
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			if ( descriptionList[i].matchesConditions(this,viewer) )
				desString += descriptionList[i].getText();
		}	
		if ( !isContainer() )
		{
			return desString;	
		}
		else if ( inventory.isEmpty() ) //puede contener items; pero no. Está vacío.
		{
			//si la descripción está construida con la conocida fórmula "hay x" o "lleva x", cambiamos por "no hay nada" o "no lleva nada".
			//si no, simplemente por "nada".
			return StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( desString , "lleva %INVENTORY" , "no lleva nada." ) , "hay %INVENTORY" , "no hay nada." ) , "%INVENTORY" , "nada." );
		}
		else
		{
			return StringMethods.textualSubstitution ( desString , "%INVENTORY" , inventory.toString() );
		}
	}
	
	public String getName ( boolean s_p , int comparand )
	{
		Description[] theList;
		if ( s_p ) theList = singNames;
		else theList = plurNames;
		String desString="";
		for ( int i = 0 ; i < theList.length ; i++ )
		{
			if ( theList[i].matches(comparand) )
				desString += theList[i].getText();
		}	
		return desString;	
	}
	
	//new version supporting predicates
	
	public String getName ( boolean s_p , Entity viewer )
	{
		Description[] theList;
		if ( s_p ) theList = singNames;
		else theList = plurNames;
		String desString="";
		if ( theList == null ) return desString;
		for ( int i = 0 ; i < theList.length ; i++ )
		{
			if ( theList[i].matchesConditions(this,viewer) )
				desString += theList[i].getText();
		}	
		return desString;
	}
	
	public String getSingName ( int comparand )
	{
		return getName ( true , comparand );
	}
	
	public String getSingNameTrue ( int comparand )
	{
		String s = getName ( true , comparand );
		if ( s != null & s.length() > 0 )
			return s;
		else
			return Character.toLowerCase(title.charAt(0)) + title.substring(1);
	}
	
	//predicate-supporting
	public String getSingName ( Entity viewer )
	{
		return getName ( true , viewer );
	}
	
	//predicate-supporting
	public String getSingNameTrue ( Entity viewer )
	{
		String s = getName ( true , viewer );
		if ( s != null & s.length() > 0 )
			return s;
		else
			return Character.toLowerCase(title.charAt(0)) + title.substring(1);
	}
	
	public String getPlurName ( int comparand )
	{
		return getName ( false , comparand );	
	}
	
	//predicate-supporting
	public String getPlurName ( Entity viewer )
	{
		return getName ( false , viewer );
	}
	
	public String getPlurNameTrue ( int comparand )
	{
		String s = getName ( false , comparand );
		if ( s != null & s.length() > 0 )
			return s;
		else
			return Character.toLowerCase(title.charAt(0)) + title.substring(1);	
	}
	
	//predicate-supporting
	public String getPlurNameTrue ( Entity viewer )
	{
		String s = getName ( false , viewer );
		if ( s != null & s.length() > 0 )
			return s;
		else
			return Character.toLowerCase(title.charAt(0)) + title.substring(1);	
	}
	
	//devuelve "una espada", "dos hachas", etc.
	public String constructName ( int nItems , int comparand )
	{
		if ( nItems == 1 )
		{
			if ( gender )
				return ( "un " + getSingName( comparand ) );
			else
				return ( "una " + getSingName( comparand ) );	
		}
		else if ( nItems < 10 )
		{
			String str;
			switch ( nItems )
			{
				case 2: str="dos"; break;
				case 3: str="tres"; break;
				case 4: str="cuatro"; break;
				case 5: str="cinco"; break;
				case 6: str="seis"; break;
				case 7: str="siete"; break;
				case 8: str="ocho"; break;
				default: str="nueve"; break;
			}
			return ( str + " " + getPlurName ( comparand ) );
		}
		else
		{
			return ( nItems + " " + getPlurName( comparand ) );	
		}	
	}
	
	//predicate-supporting
	public String constructName ( int nItems , Entity viewer )
	{
		if ( nItems == 1 )
		{
			return presentName ( getSingName(viewer) , "un" , "una" );
			/*
			if ( gender )
				return ( "un " + getSingName( viewer ) );
			else
				return ( "una " + getSingName( viewer ) );	
		    */
		}
		else if ( nItems < 10 )
		{
			String str;
			switch ( nItems )
			{
				case 2: str="dos"; break;
				case 3: str="tres"; break;
				case 4: str="cuatro"; break;
				case 5: str="cinco"; break;
				case 6: str="seis"; break;
				case 7: str="siete"; break;
				case 8: str="ocho"; break;
				default: str="nueve"; break;
			}
			
			Debug.println("CONSNAME RETT'N' " + str + " " + getPlurName ( viewer ));
			
			return ( str + " " + getPlurName ( viewer ) );
		}
		else
		{
			return ( nItems + " " + getPlurName( viewer ) );	
		}
	}
	
	//devuelve "la espada", "dos hachas", etc.
	public String constructName2 ( int nItems , int comparand )
	{
		if ( nItems == 1 )
		{
			if ( gender )
				return ( "el " + getSingName( comparand ) );
			else
				return ( "la " + getSingName( comparand ) );	
		}	
		else
		{
			return ( nItems + " " + getPlurName( comparand ) );	
		}
	}
	
	//predicate-supporting
	public String constructName2 ( int nItems , Entity viewer )
	{
		if ( nItems == 1 )
		{
			return presentName ( getSingNameTrue(viewer) , "el" , "la" );	
			/*
			if ( gender )
				return ( "el " + getSingName( viewer ) );
			else
				return ( "la " + getSingName( viewer ) );
			*/	
		}	
		else
		{
			return ( nItems + " " + getPlurName( viewer ) );	
		}
	}
	
	//lo mismo que constructName2; pero si no hay ningún nombre válido en este estado,
	//se las arregla para conseguir uno: devuelve el título.
	public String constructName2True ( int nItems , int comparand )
	{
		
		if ( nItems == 1 )
		{
			if ( gender )
				return ( "el " + getSingNameTrue( comparand ) );
			else
				return ( "la " + getSingNameTrue( comparand ) );	
		}	
		else
		{
			return ( nItems + " " + getPlurNameTrue( comparand ) );	
		}
		
	}
	
	//predicate-supporting
	public String constructName2True ( int nItems , Entity viewer )
	{
		
		if ( nItems == 1 )
		{
			/*
			if ( gender )
				return ( "el " + getSingNameTrue( viewer ) );
			else
				return ( "la " + getSingNameTrue( viewer ) );	
			*/
			return presentName ( getSingNameTrue(viewer) , "el" , "la" );
		}	
		else
		{
			return ( nItems + " " + getPlurNameTrue( viewer ) );	
		}
		
	}
	
	
	public String constructName2OneItem ( )
	{
		return constructName2True ( 1 , getState() );
	}
	
	
	//predicate-supporting
	public String constructName2OneItem ( Entity viewer )
	{
		return constructName2True ( 1 , viewer ) ;
	}
	
	
	public String presentName ( String baseName , String mascArticle , String femArticle )
	{
		boolean properName = this.properName;
		if ( baseName.startsWith("P$")) //names starting with P$ are always treated as proper
		{
			properName = true;
			baseName = baseName.substring(2);
		}
		else if ( baseName.startsWith("N$")) //names starting with N$ are always treated as not proper
		{
			properName = false;
			baseName = baseName.substring(2);
		}
		//note that names not starting with P$ or N$ are treated as proper or not proper depending on
		//the properName variable of the mobile.
		if ( properName )
			return baseName;
		if ( gender )
			return ( mascArticle + " " + baseName );
		else
			return ( femArticle + " " + baseName );	
	}
	

	//as of 03.09.03, extra descriptions are regular descriptions.
	//Parallel lists of Description[] and String[].
	
	public String getExtraDescription ( String thingieName , Entity viewer )
	{
		if ( thingieName == null || thingieName.length() == 0 ) return null; 
		for ( int i = 0 ; i < extraDescriptionNameArrays.size() ; i++ )
		{
			String[] curNameArray = (String[]) extraDescriptionNameArrays.get(i);
			Description[] curDesArray = (Description[]) extraDescriptionArrays.get(i);
			
			//Modern Extra Description Support! As of 05-07-14! Let's Rock It!
			for ( int j = 0 ; j < curNameArray.length ; j++ )
			{
				if ( thingieName.toLowerCase().endsWith(curNameArray[j].toLowerCase()) )
				{
					String desString="";
					for ( int k = 0 ; k < curDesArray.length ; k++ )
					{
						if ( curDesArray[k].matchesConditions(this,viewer) ) //or general comparand?
						{
							desString += "\n";
							desString += curDesArray[k].getText();
						}
					}	
					if ( desString.length() > 0 )
						return desString.substring(1); //quitamos primer \n
					else
						return null;
				}
			}
			
			//Ancient Extra Description Support. Buuuh! Buuuh!
			for ( int j = 0 ; j < curNameArray.length ; j++ )
			{
				//si la ultima palabra de lo que pone despues de "mirar" coincide con el nombre actual
				if ( StringMethods.getTok(thingieName,StringMethods.numToks(thingieName,' '),' ').equalsIgnoreCase(curNameArray[j]) )
				{
					String desString="";
					for ( int k = 0 ; k < curDesArray.length ; k++ )
					{
						if ( curDesArray[k].matchesConditions(this,viewer) ) //or general comparand?
						{
							desString += "\n";
							desString += curDesArray[k].getText();
						}
					}	
					if ( desString.length() > 0 )
						return desString.substring(1); //quitamos primer \n
					else
						return null;
				}
			}
		}
		return null;
	}


	//as of 02.09.28, extra descriptions MAY be stated,
	//using dynlists as in std. descriptions, with room state.
	//in this case, no &'s in an extra description means 0&0&...
	/*
	public String getExtraDescription ( String thingieName , long comparand )
	{
		int nTokens = StringMethods.numToks(extraDescriptions,'@');
		for ( int i = 1 ; i <= nTokens ; i++ )
		{
			String curToken = StringMethods.getTok(extraDescriptions,i,'@');
			int nTokens2 = StringMethods.numToks(curToken,'$');
			for ( int j = 1 ; j < nTokens2 ; j++ )
			{
				//si la ultima palabra de lo que pone despues de "mirar" coincide con uno de los nTokens2-1 primeros tokens (es decir, comandos que activan extrades)
				if ( StringMethods.getTok(thingieName,StringMethods.numToks(thingieName,' '),' ').equalsIgnoreCase(StringMethods.getTok(curToken,j,'$')) )
				{
					String theDescription = StringMethods.getTok(curToken,nTokens2,'$');
					//puede ser stateful o stateless. Vamos a verlo.
					if ( theDescription.indexOf('&') < 0 )
						return theDescription; //sin depender de estados
					else
					{
						//depende de estados (es una description list con comparandos y máscaras)						
						//procesar comparandos y máscaras como en getDescription
						Description[] dList = Utility.loadDescriptionListFromString ( theDescription );
						String desString="";
						for ( int k = 0 ; k < dList.length ; k++ )
						{
							if ( dList[k].matches(comparand) ) //or general comparand?
							{
								desString += "\n";
								desString += dList[k].getText();
							}
						}	
						if ( desString.length() > 0 )
							return desString;
						else return null;	
					}
				}
			}
		}	
		return null;
	}
	*/
	
	/*
	
	obsolete public String getExtraDescription ( String thingieName )
	{
		int nTokens = StringMethods.numToks(extraDescriptions,'@');
		for ( int i = 1 ; i <= nTokens ; i++ )
		{
			String curToken = StringMethods.getTok(extraDescriptions,i,'@');
			int nTokens2 = StringMethods.numToks(curToken,'$');
			for ( int j = 1 ; j < nTokens2 ; j++ )
			{
				//si la ultima palabra de lo que pone despues de "mirar" coincide con uno de los nTokens2-1 primeros tokens (es decir, comandos que activan extrades)
				if ( StringMethods.getTok(thingieName,StringMethods.numToks(thingieName,' '),' ').equalsIgnoreCase(StringMethods.getTok(curToken,j,'$')) )
					return StringMethods.getTok(curToken,nTokens2,'$');	
			}
		}	
		return null;
	}

	*/
	
	/**
	* Nos dice si se da por aludido el objeto ante un comando (ej. "mirar piedra") -> se pasa "piedra")
	*
	* @param commandArgs Los argumentos del comando.
	* @param pluralOrSingular 1 si plural.
	* @return 0 si no se da por aludido, 1 si sí, y con qué prioridad. (+ nº = - prioridad). La prioridad es el orden que ocupa el nombre que se corresponde con el comando dado en la lista de nombres.
	*/
	public int matchesCommand ( String commandArgs , boolean pluralOrSingular )
	{
		String listaDeInteres;
		if ( pluralOrSingular ) // plural
			listaDeInteres = respondToPlur;
		else
			listaDeInteres = respondToSing;
		/*
		int nToksArg = StringMethods.numToks( commandArgs , ' ');
		int nToksList = StringMethods.numToks( listaDeInteres , '$');
		for ( int i = 1 ; i <= nToksArg ; i++ )
		{
			String currentToAnalyze = StringMethods.getToks( commandArgs , i , nToksArg , ' ' );
			//"mirar la piedra pequeña" -> commandArgs="la piedra pequeña" -> vamos analizando "la piedra pequeña", "piedra pequeña", ...
			for ( int j = 1 ; j <= nToksList ; j++ )
			{
				if ( StringMethods.getTok( listaDeInteres , j , '$' ) .equalsIgnoreCase(currentToAnalyze) ) 
				{
					return j;
				}
			}
			//TODO: here, add reverse analysis. gettoks from 1 to moving i.
		}
		return 0;
		*/
		return matchesCommand ( commandArgs , listaDeInteres );
	}
	
	public int getInstanceOf ( )
	{
		return isInstanceOf;
	}
	
	public void setInstanceOf ( int newid )
	{
		isInstanceOf = newid;
	}
	
	public boolean isSame ( Item other )
	{
	
		Debug.print ( "isSame " + this + "("+this.getTitle()+","+this.idnumber+", cloning "+this.getInstanceOf()+")" + " " + other + "("+other.getTitle()+","+other.idnumber+", cloning "+other.getInstanceOf()+")? "); 
	
		Debug.println ( "" + (  ( idnumber % 10000000 == other.getInstanceOf () % 10000000 ) 
		         || ( isInstanceOf % 10000000 == other.getID () % 10000000 )
				 || ( isInstanceOf % 10000000 == other.getInstanceOf() % 10000000 && isInstanceOf % 10000000 != 0 )  ) );
	
		//Debug.print("this.getInstanceOf = " + isInstanceOf);
		//Debug.print(" other.getInstanceOf = " + other.getInstanceOf());
		//Debug.print(" this.getID = " + idnumber);
		//Debug.println(" other.getID = " + other.getID());
		
		//fix for problem with first item, which always seems same as everything because
		//items which aren't instance of anything have instanceof 0, and that item
		//has id 0
		if ( isInstanceOf == 0 || other.getInstanceOf() == 0 ) return false;
		
		
		return (  ( idnumber % 10000000 == other.getInstanceOf () % 10000000 ) 
		         || ( isInstanceOf % 10000000 == other.getID () % 10000000 )
				 || ( isInstanceOf % 10000000 == other.getInstanceOf() % 10000000 && isInstanceOf % 10000000 != 0 )  );
		/*observemos que la herencia fuerte no producira los resultados
		deseados si se encadena (A hereda de B, B de C...). No se implementan
		las cadenas porque son inutiles y solo recargarian el juego. Las
		cadenas solo son utiles en herencia debil, en herencia fuerte se
		supone que las instancias son iguales y da igual que hereden todas
		de una que unas de otras, por lo tanto.*/ 
	}
	
	/*ejecuta el codigo EVA del objeto correspondiente a la rutina dada si existe.
	Si no existe, simplemente no ejecuta nada y devuelve false.*/
	public boolean execCode ( String routine , String dataSegment ) throws EVASemanticException
	{
		if ( itsCode != null )
			return itsCode.run ( routine , dataSegment );
		else return false;
	}
	
	/*
	public boolean isInvisible ( Entity viewer )
	{
		//if ( !properName )
			return ( StringMethods.numToks ( constructName(1,viewer) , ' ' ) < 2 );
		//else
		//	return ( StringMethods.numToks ( constructName(1,viewer) , ' ' ) < 1 );
	}
	*/
	
	public boolean isInvisible ( Entity viewer )
	{
		return !( getSingName(viewer).length() > 0 );
	}	
	
	/*ejecuta el codigo EVA del objeto correspondiente a la rutina dada si existe.
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
	
	//[old] Hay que definir el changeState.
	public void changeState ( World mundo ) 
	{ 
		try
		{
			execCode("event_endstate","this: "+ getID() + "state: " + getState() );
		}
		catch ( EVASemanticException exc ) 
		{
			mundo.write("EVASemanticException found at event_endstate , item number " + getID() );
		}
	}
	
	
	public boolean getGender()
	{
		return gender;
	}
	
	/**Devuelve si el item puede abrirse (no si el personaje puede hacerlo, sino si tiene sentido abrirlo)*/
	public boolean isOpenable()
	{
		return ( openDescriptionList != null && openDescriptionList.length > 0 );
	}
	
	/**Devuelve si el item puede cerrarse (no es lo mismo, un huevo es openable y no closeable)*/
	public boolean isCloseable()
	{
		return ( closeDescriptionList != null && closeDescriptionList.length > 0 );
	}

	//id. anteriores
	public boolean isUnlockable()
	{
		return ( unlockDescriptionList != null && unlockDescriptionList.length > 0 );
	}
	
	//id. anteriores
	public boolean isLockable()
	{
		return ( lockDescriptionList != null && lockDescriptionList.length > 0 );
	}
	
	public boolean isOpen()
	{
		return ( (256 & getState()) == 0 ) && ( !getPropertyValueAsBoolean("closed") );
	}
	public boolean isClosed()
	{
		return !isOpen();
	}
	public boolean isUnlocked()
	{
		return ( (512 & getState()) == 0 ) && ( !getPropertyValueAsBoolean("locked") );
	}
	public boolean isLocked()
	{
		return !isUnlocked();
	}
	
	public boolean unlocksWithKey( Item key )
	{
		if ( keys != null )
		{
			for ( int i = 0 ; i < keys.size() ; i++ )
			{
				if ( keys.elementAt(i).equals(key) ) return true;
			}
		}
			return false;
	}
	
	//intentar abrir sin llave. 
	public String abrir (  )
	{
		boolean exito = false; //si realmente abrimos el objeto
		String descriptionText = "";
		if ( !isOpenable() )
		{
			return //"Es absurdo abrir eso.";
				mundo.getMessages().getMessage("item.not.openable",new Object[]{this});
		}
		//else if ( !isUnlocked() ) 
		//{
			//corrección: quitamos esto. En realidad, se tratará como el resto.
			//return "\n¡No se abre!";
		//}
		else
		{
			//descripciones open
			for ( int i = 0 ; i < openDescriptionList.length ; i++ )
			{
				//Debug.print("A");
			    Description des_actual = openDescriptionList[i];
				if ( des_actual.matchesConditions ( this /*add viewer*/ ) )
				{
					//Debug.print("B");
					//Debug.print(des_actual.getText());
					String elTexto = des_actual.getText();
					StringTokenizer st = new StringTokenizer(elTexto,":");
					//mirar si antes de ":" sale éxito o fracaso (al abrir)
					String firstToken = st.nextToken();
					if ( firstToken.equalsIgnoreCase("SUCCESS") || firstToken.equalsIgnoreCase("EXITO") )
					{
						exito = true;
					}
					//coger el resto de la descripción y añadirla
					elTexto = "";
					while ( st.hasMoreTokens() ) elTexto+=st.nextToken();
					if ( !elTexto.equals("") )
					{
						descriptionText += elTexto;
						//descriptionText += "\n";
					}
				}
			} 
			//si ha tenido éxito (se cumplía alguno de los estados de éxito) abrimos
			if ( exito )
			{ 
				//old open 
					setNewState( getState() & (~256) );
				//new open
					setProperty("closed",false);
				//opened item inform
					List habitaciones = getRoomReferences();
					for ( int i = 0 ; i < habitaciones.size() ; i++ )
					{
						Room thisHabitacion = (Room) habitaciones.get(i);
						
						
						//@@UNCOMMENT
						//NO DE MOMENTO. PENSAR CÓMO HACER QUE ESTO NO DIGA "ALGO"
						//TAL VEZ NOMBRES CON PARÁMETRO SHOWONROOM, O ALGO ASÍ
						//thisHabitacion.informAction(this,null,null,"$1 se abre.\n","Te abres.\n","Abres.\n",false);
					}
					
				
			}
		}
		try
		{	
			execCode("onOpen" , new Object[] {new Boolean(exito)} );
		}
		catch ( bsh.TargetError bshte )
		{
				return( "bsh.TargetError found onOpen , item number " + getID() + ": " + bshte + "[description was: " + descriptionText + "]" );
		}
		return descriptionText;
	} //end function
	
	
	//intentar cerrar sin llave
	public String cerrar ( )
	{
		if ( !isCloseable() )
		{
			return //"Es absurdo cerrar eso.";
				mundo.getMessages().getMessage("item.not.closeable",new Object[]{this});
		}
		else
		{
			//descripciones close
			String descriptionText = "";
			boolean exito = false; //si realmente cerramos el objeto
			for ( int i = 0 ; i < closeDescriptionList.length ; i++ )
			{
			    Description des_actual = closeDescriptionList[i];
				if ( des_actual.matchesConditions ( this /*add viewer*/ ) )
				{
					String elTexto = des_actual.getText();
					StringTokenizer st = new StringTokenizer(elTexto,":");
					//mirar si antes de ":" sale éxito o fracaso (al cerrar)
					String firstToken = st.nextToken();
					if ( firstToken.equalsIgnoreCase("SUCCESS") || firstToken.equalsIgnoreCase("EXITO") )
					{
						exito = true;
					}
					//coger el resto de la descripción y añadirla
					elTexto = "";
					while ( st.hasMoreTokens() ) elTexto+=st.nextToken();
					if ( !elTexto.equals("") )
					{
						descriptionText += elTexto;
						//descriptionText += "\n";
					}
				}
			} 
			//si ha tenido éxito (se cumplía alguno de los estados de éxito) cerramos
			if ( exito ) 
			{
				//old close
					setNewState( getState() | 256 );
				//new close
					setProperty("closed",true);
				//closed item inform
					List habitaciones = getRoomReferences();
					for ( int i = 0 ; i < habitaciones.size() ; i++ )
					{
						Room thisHabitacion = (Room) habitaciones.get(i);
						
						//@@UNCOMMENT
						//NO DE MOMENTO. PENSAR CÓMO HACER QUE ESTO NO DIGA "ALGO"
						//TAL VEZ NOMBRES CON PARÁMETRO SHOWONROOM, O ALGO ASÍ
						//thisHabitacion.informAction(this,null,null,"$1 se cierra.\n","Te cierras.\n","Cierras.\n",false);
					}
			}
			try
			{	
				execCode("onClose" , new Object[] {new Boolean(exito)} );
			}
			catch ( bsh.TargetError bshte )
			{
				return( "bsh.TargetError found onOpen , item number " + getID() + ": " + bshte + "[description was: " + descriptionText + "]" );
			}
			return descriptionText;
		}
	
	}


	public String unlock ( Item key )
	{
		if ( !isUnlockable() )
		{
			return //"No parece que se pueda abrir eso de ese modo.";
				mundo.getMessages().getMessage("item.not.unlockable",new Object[]{this});
		}
		else
		{
			//descripciones unlock: a diferencia de las open, pueden tener dos diferentes
			//son de la forma SUCCESS:des1:FAIL:des2
			//(las open sólo tienen una de las dos partes que indica si abres o no
			//en ese estado; pero en las unlock el que abras o no viene dado por
			//la llave que uses aparte del estado)
			
			String descriptionText = "";
			
			if ( !unlocksWithKey ( key ) )
			{
				//consideramos sólo descripciones "fail"
				
				for ( int i = 0 ; i < unlockDescriptionList.length ; i++ )
				{
					Description des_actual = unlockDescriptionList[i];
					String elTexto2 = ""; //to append to description
					if ( des_actual.matchesConditions ( this /*add viewer*/ ) )
					{
						//buscar descripción fail (obligatoria) <- no oblig. (2008-04)
						String elTexto1 = des_actual.getText();
						StringTokenizer st = new StringTokenizer(elTexto1,":");
						String temp;
						while ( st.hasMoreTokens() && !((temp=st.nextToken()).equalsIgnoreCase("FAIL")) && !temp.equalsIgnoreCase("FRACASO") )
						{
							;
						}
						if ( st.hasMoreTokens() )
							elTexto2 = st.nextToken();
						else
							elTexto2 = "";
					} //end if matches getstate
					descriptionText += elTexto2;
				}
				
				if ( descriptionText.equals("") )
				{
					descriptionText = "No consigues abrir " + this.getTitle(); //TODO temp, modify
				}
				
			}
			
			else
			{
				//consideramos la descripción "success" y abrimos, o, si no hay
				//descripción "success", falla por el estado
			
				boolean unlocked = false;
				
				for ( int i = 0 ; i < unlockDescriptionList.length ; i++ )
				{
					Description des_actual = unlockDescriptionList[i];
					String elTexto2 = ""; //to append to description
					if ( des_actual.matchesConditions ( this /*add viewer*/ ) )
					{
						//buscar descripción success (puede no haberla)
						String elTexto1 = des_actual.getText();
						StringTokenizer st = new StringTokenizer(elTexto1,":");
						String temp = null;
						while ( st.hasMoreTokens() && !((temp=st.nextToken()).equalsIgnoreCase("SUCCESS")) && !temp.equalsIgnoreCase("EXITO") )
						{
							;
							
							//se abre
							
						}
						if ( temp.equalsIgnoreCase("SUCCESS") || temp.equalsIgnoreCase("EXITO") )
						{
							elTexto2 = st.nextToken();
							unlocked = true;
						}
						unlocked=true;
						/* commented 2008-04-27
						if ( elTexto2.equals("") )
						{
							//buscar descripción fail (obligatoria)
							StringTokenizer st2 = new StringTokenizer(elTexto1,":");
							String temp2 = null;
							Debug.println("Des_actual: " + des_actual.getText());
							StringTokenizer st3 = new StringTokenizer(elTexto1,":");
							Debug.println(st3.nextToken());
							Debug.println(st3.nextToken());
							while ( st2.hasMoreTokens() && !(  (temp2=st2.nextToken()).equalsIgnoreCase("FAIL")  ) && !temp2.equalsIgnoreCase("FRACASO")  )
							{
								Debug.println("TOK: " + temp2 ) ;
								Debug.println("des_actual: " + des_actual );
								;
							}
							elTexto2 = st.nextToken();
						}
						*/
					} //end if matches getstate
					if ( !elTexto2.equals("") )
					{
						descriptionText += "\n";
						descriptionText += elTexto2;
					}
				} //end for all possible descriptions in different states
				
				if ( unlocked )
				{
					//hemos conseguido abrirlo
					//cambiar el estado para que se refleje esto
					
					//old unlock
						setNewState( getState() & (~512) );
					//new unlock
						setProperty("locked",false);
						
					//unlocked item inform
					List habitaciones = getRoomReferences();
					for ( int i = 0 ; i < habitaciones.size() ; i++ )
					{
						Room thisHabitacion = (Room) habitaciones.get(i);
						thisHabitacion.reportAction(this,null,null,"$1 se abre con llave.\n","Te abres con llave.\n","Abres con llave.\n",false);
					}
				}
			
			
			} //end else (if unlocks with key) 
			
			return descriptionText;
		
		} //end else (if it's unlockable)
	
	} //end function
	
	
	public String lock ( Item key )
	//simétrico de unlock
	{
		if ( !isLockable() )
		{
			return //"No parece que se pueda cerrar eso de ese modo.";
			mundo.getMessages().getMessage("item.not.lockable",new Object[]{this});
		}
		else
		{
			//descripciones lock: igual que las de unlock, pueden tener dos diferentes
			//son de la forma SUCCESS:des1:FAIL:des2
			//(las open sólo tienen una de las dos partes que indica si abres o no
			//en ese estado; pero en las unlock/lock el que abras o no viene dado por
			//la llave que uses aparte del estado)
			
			String descriptionText = "";
			
			if ( !unlocksWithKey ( key ) )
			{
				//consideramos sólo descripciones "fail"
				
				for ( int i = 0 ; i < lockDescriptionList.length ; i++ )
				{
					Description des_actual = lockDescriptionList[i];
					String elTexto2 = ""; //to append to description
					if ( des_actual.matchesConditions ( this /*add viewer*/ ) )
					{
						//buscar descripción fail (obligatoria)
						String elTexto1 = des_actual.getText();
						StringTokenizer st = new StringTokenizer(elTexto1,":");
						String temp;
						while ( st.hasMoreTokens() && !((temp=st.nextToken()).equalsIgnoreCase("FAIL")) && !temp.equalsIgnoreCase("FRACASO") )
						{
							;
						}
						if ( st.hasMoreTokens() )
							elTexto2 = st.nextToken();
					} //end if matches getstate
					if ( !elTexto2.equals("") )
					{
						descriptionText += "\n";
						descriptionText += elTexto2;
					}
				}
				
			}
			
			else
			{
				//consideramos la descripción "success" y abrimos, o, si no hay
				//descripción "success", falla por el estado
			
				boolean locked = false;
				
				for ( int i = 0 ; i < lockDescriptionList.length ; i++ )
				{
					Description des_actual = lockDescriptionList[i];
					String elTexto2 = ""; //to append to description
					if ( des_actual.matches ( getState() ) )
					{
						//buscar descripción success (puede no haberla)
						String elTexto1 = des_actual.getText();
						StringTokenizer st = new StringTokenizer(elTexto1,":");
						String temp = null;
						while ( st.hasMoreTokens() && !((temp=st.nextToken()).equalsIgnoreCase("SUCCESS")) && !temp.equalsIgnoreCase("EXITO") )
						{
							;
							
							
							
						}
						//se cierra
						if ( temp.equalsIgnoreCase("SUCCESS") || temp.equalsIgnoreCase("EXITO") )
						{
							elTexto2 = st.nextToken();
							locked = true;
						}
						locked=true;
						/* removed 2008-04-07
						if ( elTexto2.equals("") )
						{
							//buscar descripción fail (obligatoria)
							StringTokenizer st2 = new StringTokenizer(elTexto1,":");
							String temp2=null;
							while ( st2.hasMoreTokens() && !((temp2=st2.nextToken()).equalsIgnoreCase("FAIL")) && !temp2.equalsIgnoreCase("FRACASO") )
							{
								;
							}
							elTexto2 = st2.nextToken();
						}
						*/
					} //end if matches getstate
					if ( !elTexto2.equals("") )
					{
						descriptionText += "\n";
						descriptionText += elTexto2;
					}
				} //end for all possible descriptions in different states
				
				if ( locked )
				{
					//hemos conseguido cerrarlo
					//cambiar el estado para que se refleje esto
					
					//old lock
						setNewState( getState() | 512 );
					//new lock
						setProperty("locked",true);
					//locked item inform
					List habitaciones = getRoomReferences();
					for ( int i = 0 ; i < habitaciones.size() ; i++ )
					{
						Room thisHabitacion = (Room) habitaciones.get(i);
						thisHabitacion.reportAction(this,null,null,"$1 se cierra con llave.\n","Te cierras con llave.\n","Cierras con llave.\n",false);
					}	
						
				}
			
			
			} //end else (if unlocks with key) 
			
			return descriptionText;
		
		} //end else (if it's unlockable)
	
	} //end function


	//nos devuelve el nombre más específico con el que nos podemos referir al objeto en
	//un comando. Útil para zonas de referencia.
	public String getBestReferenceName ( boolean pluralOrSingular )
	{
		String theList;
		if ( pluralOrSingular ) //true? plural
			theList = respondToPlur;
		else
			theList = respondToSing;
		String tmp = StringMethods.getTok(theList,1,'$');	
		return ( Character.toLowerCase( tmp.charAt(0) ) ) + tmp.substring(1);
	}
	
	/* Carga diferida del inventario (y también del parts-inventory o inventario de composite)*/
	/* (y del key list) <- 2008-04-27*/
	public void loadInventoryFromXML ( World mundo ) throws XMLtoWorldException
	{
	
		org.w3c.dom.Node n = mundo.getItemNode( String.valueOf(getID()) );
		
		org.w3c.dom.Element e = null;
		try
		{
			e = (org.w3c.dom.Element) n;
		}
		catch ( ClassCastException cce )
		{
			throw ( new XMLtoWorldException ( "Item node not Element" ) );
		}
		
		org.w3c.dom.NodeList inventoryNodes = e.getElementsByTagName ( "Inventory" );
		//solo nos interesan los hijos DIRECTOS
		List realInventoryNodes = new ArrayList();
		List partsInventoryNodes = new ArrayList();
		List keysInventoryNodes = new ArrayList();
		for ( int i = 0 ; i < inventoryNodes.getLength() ; i++ )
		{
			if ( inventoryNodes.item(i).getParentNode() == e ) realInventoryNodes.add ( inventoryNodes.item(i) );
			else if ( inventoryNodes.item(i).getParentNode() instanceof org.w3c.dom.Element && ((org.w3c.dom.Element)inventoryNodes.item(i).getParentNode()).getTagName().equalsIgnoreCase("parts") ) partsInventoryNodes.add ( inventoryNodes.item(i) );
			else if ( inventoryNodes.item(i).getParentNode() instanceof org.w3c.dom.Element && ((org.w3c.dom.Element)inventoryNodes.item(i).getParentNode()).getTagName().equalsIgnoreCase("keylist") ) keysInventoryNodes.add ( inventoryNodes.item(i) );
		}
		
		if ( realInventoryNodes.size() < 1 )
		{
			//Debug.println("No inventory nodes, inventory will be left null.");
			inventory = null;
		}
		else
		{
			inventory = new Inventory ( mundo , (org.w3c.dom.Node)realInventoryNodes.get(0) );
		}		
		
		if ( partsInventoryNodes.size ( ) < 1 )
		{
			partsInventory = null;
		}
		else
		{
			partsInventory = new Inventory ( mundo , (org.w3c.dom.Node)partsInventoryNodes.get(0) );
		}
		
		if ( keysInventoryNodes.size ( ) < 1 )
		{
			keys = null;
		}
		else
		{
			keys = new Inventory ( mundo , (org.w3c.dom.Node)keysInventoryNodes.get(0) );
		}
		
	
	}
	
	/*
		Carga del inventario para contenedores a partir de la línea correspondiente del fichero
		(inventoryString), que se difiere para poder soportar items de número mayor que éste.
	*/
	//I think this is unused and I don't have to repeat it for partsInventory. But not sure.
	public void loadInventory ( World mundo )
	{
		//item references line
		
		if ( inventoryString == null || inventoryString.equals("") ) 
		{
			inventory = null; return;
		}
		
		//Debug.println("Container item found. Performing delayed load.\n");
		
		int nObjects = StringMethods.numToks(inventoryString,'$') - 2;
		//Debug.println("nObjects is " + nObjects);
		//if ( nObjects < 1 ) 
		//{
		//	inventory = null; return;
		//}
		int maxweight,maxvol;
		try
		{
			maxweight = Integer.valueOf(StringMethods.getTok(inventoryString,1,'$')).intValue();
			maxvol = Integer.valueOf(StringMethods.getTok(inventoryString,2,'$')).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			inventory = null; return;
		}
		//Debug.println("Proceeding to item load.");
		inventory = new Inventory ( maxweight , maxvol, nObjects );
		for ( int i = 0 ; i < nObjects ; i++ )
		{
			//Debug.println("Adding item to container.");
			try
			{
				inventory.addItem ( mundo.getItem(StringMethods.getTok(inventoryString,i+3,'$')) );
			}
			catch (WeightLimitExceededException exc) 
			{
				mundo.write("Item too heavy for container, ID " + idnumber );
			}
			catch (VolumeLimitExceededException exc2)
			{
				mundo.write("Item too big for container , ID " + idnumber );
			}
		}
			
	} //end function load inventory
	
	public Inventory getContents ( )
	{
		return inventory;
	}
	
	public boolean isContainer ( )
	{
		return (inventory!=null);
	}
	
	public Inventory getParts ( )
	{
		return partsInventory;
	}
	
	public boolean isComposite ( )
	{
		return (partsInventory!=null);
	}
	
	
	
	
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
	
		org.w3c.dom.Element suElemento = doc.createElement( "Item" );
		
		suElemento.setAttribute ( "id" , String.valueOf( idnumber ) );
		suElemento.setAttribute ( "name" , String.valueOf ( title ) );
		suElemento.setAttribute ( "extends" , String.valueOf ( inheritsFrom ) );
		suElemento.setAttribute ( "clones" , String.valueOf ( isInstanceOf ) );
		suElemento.setAttribute ( "type" , String.valueOf ( itemType ) );
		suElemento.setAttribute ( "volume" , String.valueOf( volume ) );
		suElemento.setAttribute ( "weight" , String.valueOf( weight ) );
		suElemento.setAttribute ( "enabled" , String.valueOf( enabled ) );
		suElemento.setAttribute ( "isVirtual" , String.valueOf( isVirtual ) );
		suElemento.setAttribute ( "canGet" , String.valueOf( canGet ) );
		if ( properName ) suElemento.setAttribute ( "properName" , String.valueOf( properName ) );
		
		suElemento.setAttribute ( "openable" , String.valueOf( openDescriptionList != null ) );
		suElemento.setAttribute ( "closeable" , String.valueOf( closeDescriptionList != null ) );
		suElemento.setAttribute ( "lockable" , String.valueOf( lockDescriptionList != null ) );
		suElemento.setAttribute ( "unlockable" , String.valueOf( unlockDescriptionList != null ) );
		
		//temp gender representation
		suElemento.setAttribute ( "gender" , String.valueOf( gender ) );
		
		suElemento.appendChild ( getPropListXMLRepresentation(doc) );
		
		suElemento.appendChild ( getRelationshipListXMLRepresentation(doc) );
		
		org.w3c.dom.Element listaDesc;
		if ( descriptionList != null )
		{
			listaDesc = doc.createElement("DescriptionList");
			for ( int i = 0 ; i < descriptionList.length ; i++ )
			{
				Description nuestraDescripcion = descriptionList[i];
				listaDesc.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
			}
			suElemento.appendChild(listaDesc);
		}
		
		if ( singNames != null )
		{
			org.w3c.dom.Element listaSing = doc.createElement("SingularNames");
			for ( int i = 0 ; i < singNames.length ; i++ )
			{
				Description nuestraDescripcion = singNames[i];
				listaSing.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
			}
			suElemento.appendChild(listaSing);
		}
		
		if ( plurNames != null )
		{
			org.w3c.dom.Element listaPlur = doc.createElement("PluralNames");
			for ( int i = 0 ; i < plurNames.length ; i++ )
			{
				Description nuestraDescripcion = plurNames[i];
				listaPlur.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
			}
			suElemento.appendChild(listaPlur);
		}
		
		//"respond to" names (temp XML representation)
		if ( respondToSing != null )
		{
			org.w3c.dom.Element respTo = doc.createElement("SingularReferenceNames");
			StringTokenizer st = new StringTokenizer ( respondToSing , "$" );
			while ( st.hasMoreTokens() )
			{
				String tok = st.nextToken();
				org.w3c.dom.Element esteNombre = doc.createElement("Name");
				org.w3c.dom.Text elNombre = doc.createTextNode(tok);
				esteNombre.appendChild(elNombre);
				respTo.appendChild(esteNombre);
			}
			suElemento.appendChild(respTo);
		}
		if ( respondToPlur != null )
		{
			org.w3c.dom.Element respTo = doc.createElement("PluralReferenceNames");
			StringTokenizer st = new StringTokenizer ( respondToPlur , "$" );
			while ( st.hasMoreTokens() )
			{
				String tok = st.nextToken();
				org.w3c.dom.Element esteNombre = doc.createElement("Name");
				org.w3c.dom.Text elNombre = doc.createTextNode(tok);
				esteNombre.appendChild(elNombre);
				respTo.appendChild(esteNombre);
			}
			suElemento.appendChild(respTo);
		}

		
		//inventario
		if ( inventory != null )
			suElemento.appendChild(inventory.getXMLRepresentation(doc));
	
		if ( partsInventory != null )
		{
			org.w3c.dom.Element partsElt = doc.createElement("Parts");
			suElemento.appendChild(partsElt);
			partsElt.appendChild(partsInventory.getXMLRepresentation(doc));
		}
	
		//extra descriptions (temp XML representation)
		if ( extraDescriptions != null )
		{
			org.w3c.dom.Element extraDes = doc.createElement("ExtraDescriptionList");			
			StringTokenizer st1 = new StringTokenizer ( extraDescriptions, "@" );
			while ( st1.hasMoreTokens() )
			{
				String desActual = st1.nextToken();	
				org.w3c.dom.Element unaDescripcion = doc.createElement("ExtraDescription");
				StringTokenizer st2 = new StringTokenizer ( desActual , "$" );
				while ( st2.hasMoreTokens() )
				{
					String wordActual = st2.nextToken();
					if ( st2.hasMoreTokens() )
					{
						org.w3c.dom.Element comando = doc.createElement("Name");
						org.w3c.dom.Text contenido = doc.createTextNode(wordActual);
						comando.appendChild(contenido);
						unaDescripcion.appendChild(comando);
					}
					else
					{
						org.w3c.dom.Text texto = doc.createTextNode(wordActual);
						unaDescripcion.appendChild(texto);
					}
				}
				extraDes.appendChild ( unaDescripcion );
			}		
			//org.w3c.dom.Text extraDesContent = doc.createTextNode(extraDescriptions);
			//extraDes.appendChild(extraDesContent);
			suElemento.appendChild(extraDes);
		}
	
		//keys inventory
		if ( keys != null )
		{
			org.w3c.dom.Element keysel = doc.createElement("KeyList");
			keysel.appendChild(keys.getXMLRepresentation(doc));
			suElemento.appendChild(keysel);
		}
		
		//only restrictions: not at the moment
		
		//open, close, lock, unlock lists
		
		if ( openDescriptionList != null )
		{
			listaDesc = doc.createElement("OpenDescriptionList");
			for ( int i = 0 ; i < openDescriptionList.length ; i++ )
			{
				Description nuestraDescripcion = openDescriptionList[i];
				listaDesc.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
			}
			suElemento.appendChild(listaDesc);
		}
		if ( closeDescriptionList != null )
		{
			listaDesc = doc.createElement("CloseDescriptionList");
			for ( int i = 0 ; i < closeDescriptionList.length ; i++ )
			{
				Description nuestraDescripcion = closeDescriptionList[i];
				listaDesc.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
			}
			suElemento.appendChild(listaDesc);
		}
		if ( lockDescriptionList != null )
		{
			listaDesc = doc.createElement("LockDescriptionList");
			for ( int i = 0 ; i < lockDescriptionList.length ; i++ )
			{
				Description nuestraDescripcion = lockDescriptionList[i];
				listaDesc.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
			}
			suElemento.appendChild(listaDesc);
		}
		if ( unlockDescriptionList != null )
		{
			listaDesc = doc.createElement("UnlockDescriptionList");
			for ( int i = 0 ; i < unlockDescriptionList.length ; i++ )
			{
				Description nuestraDescripcion = unlockDescriptionList[i];
				listaDesc.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
			}
			suElemento.appendChild(listaDesc);
		}
		
		//object code
		if ( itsCode != null )
			suElemento.appendChild(itsCode.getXMLRepresentation(doc));

		return suElemento;
		
	}
	
	
	public void loadNumberGenerator ( World mundo )
	{
		aleat = mundo.getRandom();
		//Debug.println("I am " + this + "[IID:"+getID()+"][INAME"+title+"], and my Random has been set to " + aleat);
	}
	
	public java.util.Random getRandom()
	{
		return aleat;
	}
	
	public void setID ( int newid )
	{
		if ( newid < 30000000 )
			idnumber = newid + 30000000;
		else
			idnumber = newid;
	}
	
	//Crea el cadáver del bicho dado.
	public static Item initCorpse ( Mobile m )
	{
	
		Item it = new Item();
	
		//no fijamos una ID porque la fijará World::addItemDinamically()
	
		it.itemType = "corpse";
		it.inheritsFrom = 0;
		it.isInstanceOf = 0;	
		it.title = "cadáver de " + m.constructName(1);
	
		it.descriptionList = new Description[1];
		it.descriptionList[0] = ( new Description ( "Es un cadáver de " + m.constructName(1) + ", que lleva %INVENTORY" , 0 , 0 )  );
		it.singNames = new Description[1];
		it.singNames[0] = new Description ( it.title , 0 , 0 );
		it.plurNames = new Description[1];
		it.plurNames[0] = new Description ( "cadáveres de " + m.constructName(2) , 0 , 0 );
		
		it.gender = true; //masculino
	
		it.respondToSing = it.title + "$cadáver$cadaver$cuerpo$muerto";
		it.respondToPlur = it.title + "$cadáveres$cadaveres$cuerpos$muertos";
		
		//temp!
		it.volume = 1000;
		it.weight = 1000;
	
		//inventory
		
		it.inventory = m.getInventoryForCorpse();
		if ( it.inventory == null )
			it.inventory = new Inventory(10000,10000);
	
		it.mobRefs = null; //unnecessary, probably
	
		it.extraDescriptions = null;
		it.onlyRestrictions = null;
		it.openDescriptionList = null;
		it.closeDescriptionList = null;
		it.lockDescriptionList = null;
		it.unlockDescriptionList = null;
		it.keys = null;
		
		it.enabled = true;
		it.isVirtual = false;
		it.canGet = true;
		it.properName = false;
	
		it.itsCode = null;
	
		return it;
	
	}
	
	
	//devuelve un Inventory formado por las partes, las partes de partes, etc. etc.
	public Inventory getFlattenedPartsInventory()
	{
		
		if ( partsInventory == null ) return new Inventory(1,1);
		
		Inventory result = new Inventory(partsInventory.getWeightLimit(),partsInventory.getVolumeLimit());
		
		for ( int i = 0 ; i < partsInventory.size() ; i++ )
		{
			Item thisPart = partsInventory.elementAt(i);
			Inventory subInv = thisPart.getFlattenedPartsInventory();
		
			//add part's flattened parts inventory
			try
			{
				result.setVolumeLimit ( result.getVolumeLimit() + subInv.getVolumeLimit() );
				result.setWeightLimit ( result.getWeightLimit() + subInv.getWeightLimit() );
			}
			catch ( Exception e )
			{
				Debug.println("Impossible exception thrown: " + e);
				e.printStackTrace();
			}
			for ( int j = 0 ; j < subInv.size() ; j++ )
			{
				try
				{
					result.addItem ( subInv.elementAt(j) );
				}
				catch ( Exception e )
				{
					Debug.println("Impossible exception thrown: " + e);
					e.printStackTrace();
				}
			}
		
			//add this part
				try
				{
					result.addItem ( thisPart );
				}
				catch ( Exception e )
				{
					Debug.println("Impossible exception thrown: " + e);
					e.printStackTrace();
				}
		}
		
		return result;
		
	}
	
	
	
	
	//call Room::informActionAuto on all item's rooms
	/**
	 * @deprecated Use {@link #reportActionAuto(Entity,Entity,Entity[],String,boolean)} instead
	 */
	public void informActionAuto ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , boolean self_included )
	{
		reportActionAuto(source, target, objects, thirdPersonDes, self_included);
	}
	
	/**
	 * @deprecated Use {@link #reportAction(Entity,Entity,Entity[],String,String,String,boolean)} instead
	 */
	public void informAction ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , String sufferDes , String execDes , boolean self_included )
	{
		reportAction(source, target, objects, thirdPersonDes, sufferDes,
				execDes, self_included);
	}
	
	public void reportActionAuto ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes ,  boolean self_included )
	{
		reportActionAuto ( source , target , objects , thirdPersonDes , null , self_included );
	}
	
	public void reportAction ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , String sufferDes , String execDes  , boolean self_included )
	{
		reportAction ( source , target , objects , thirdPersonDes , sufferDes , execDes , null , self_included );
	}
	
	
	//call Room::informActionAuto on all item's rooms
	public void reportActionAuto ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , String style , boolean self_included )
	{
		List habitaciones = getRoomReferences();
		for ( int i = 0 ; i < habitaciones.size() ; i++ )
		{
			Room hab = (Room)habitaciones.get(i);
			hab.reportActionAuto ( source , target , objects , thirdPersonDes , style , self_included );
		}
	}
	
	public void reportAction ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , String sufferDes , String execDes , String style , boolean self_included )
	{
		List habitaciones = getRoomReferences();
		for ( int i = 0 ; i < habitaciones.size() ; i++ )
		{
			Room hab = (Room)habitaciones.get(i);
			hab.reportAction ( source , target , objects , thirdPersonDes , sufferDes , execDes , style , self_included );
		}
	}
	
	
	public void removeFromInventories (  )
	{
		
		for ( int i = 0 ; i < rooms.size() ; i++ )
		{
			Room r = (Room) rooms.get(i);
			r.removeItem(this);
		}
		for ( int i = 0 ; i < mobiles.size() ; i++ )
		{
			Mobile m = (Mobile) mobiles.get(i);
			m.removeItem(this);
		}
	}
	
	public void moveTo ( Room target ) throws WeightLimitExceededException,VolumeLimitExceededException
	{
		removeFromInventories();
		target.addItem(this);
	}
	
	public void moveTo ( Mobile target ) throws WeightLimitExceededException,VolumeLimitExceededException
	{
		removeFromInventories();
		target.addItem(this);
	}
	
	
	
	
	
	//devuelve un Inventory formado por el contenido, el contenido del contenido, etc. etc.
	public Inventory getFlattenedInventory()
	{
		
		if ( inventory == null ) return new Inventory(1,1);
		
		Inventory result = new Inventory(inventory.getWeightLimit(),inventory.getVolumeLimit());
		
		for ( int i = 0 ; i < inventory.size() ; i++ )
		{
			Item thisPart = inventory.elementAt(i);
			Inventory subInv = thisPart.getFlattenedInventory();
		
			//add part's flattened parts inventory
			try
			{
				result.setVolumeLimit ( result.getVolumeLimit() + subInv.getVolumeLimit() );
				result.setWeightLimit ( result.getWeightLimit() + subInv.getWeightLimit() );
			}
			catch ( Exception e )
			{
				Debug.println("Impossible exception thrown: " + e);
				e.printStackTrace();
			}
			for ( int j = 0 ; j < subInv.size() ; j++ )
			{
				try
				{
					result.addItem ( subInv.elementAt(j) );
				}
				catch ( Exception e )
				{
					Debug.println("Impossible exception thrown: " + e);
					e.printStackTrace();
				}
			}
		
			//add this part
				try
				{
					result.addItem ( thisPart );
				}
				catch ( Exception e )
				{
					Debug.println("Impossible exception thrown: " + e);
					e.printStackTrace();
				}
		}
		
		return result;
		
	}
	
	
	public boolean isWearable() { return ( this instanceof Wearable ); }
	public boolean isWeapon() { return ( this instanceof Weapon ); }
	
	

} //end class item