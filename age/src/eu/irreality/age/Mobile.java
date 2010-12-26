/*
 * (c) 2000-2009 Carlos Gï¿½mez Rodrï¿½guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
//package AetheriaAWT;
import java.util.*;

import bsh.*;
import java.io.*;
import java.net.URL;

import javax.sound.midi.Sequencer;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.messages.Messages;
public class Mobile extends Entity implements Descriptible , SupportingCode , Nameable
{


	/*constantes de "state"*/
	public static final int IDLE=1;
	public static final int MOVING=2;
	public static final int ATTACKING=3;
	public static final int BLOCKING=4;
	public static final int READY_TO_BLOCK=5;
	public static final int DODGING=6;
	public static final int READY_TO_DODGE=7;
	public static final int ATTACK_RECOVER=8;
	public static final int BLOCK_RECOVER=9;
	public static final int DAMAGE_RECOVER=10;
	public static final int DODGE_RECOVER=11;
	public static final int DYING=12; //just before death
	public static final int DEAD=13;
	public static final int SURPRISE_RECOVER=14;
	public static final int DISABLED=15; //disconnected players and suchlike. Don't do anything
	public static final int CASTING=16;




	//provisional
	boolean numeric_damage = false;




	/*end constantes de "state"







	//////////////////////
	//INSTANCE VARIABLES//
	//////////////////////

	/**Tipo de bicho.*/
	/*00*/ private String mobileType;

	/**ID del objeto.*/
	/*01*/ private int idnumber;

	/**Hereda de.*/
	/*02*/ private int inheritsFrom;

	/*03*/ // inherited protected int state;
	// inherited protected long timeunitsleft;



	/**Habitaciï¿½n actual*/
	protected Room habitacionActual;
	/**Habitaciï¿½n anterior*/
	protected Room habitacionAnterior;



	/**Nombre sintï¿½tico del bicho.*/
	/*04*/ protected String title;

	/**Es instancia de.*/
	/*05*/ private int isInstanceOf;

	/**Lista dinï¿½mica de descripciones.*/
	/*10*/ protected Description[] descriptionList;
	/**Lista dinï¿½mica de nombres en singular. (goblin, goblin herido... mismo goblin, varios estados)*/
	/*11*/ protected Description[] singNames;
	/**Lista dinï¿½mica de nombres en plural.*/
	/*12*/ protected Description[] plurNames;
	/**Gï¿½nero.*/
	/*13*/ protected boolean gender; //true=masculino
	/**Responder a... (comandos): listas de pattern-matching.*/
	/*14*/ protected String respondToSing;
	/*15*/ protected String respondToPlur;


	/*20*/ protected Inventory inventory;
	/*21*/ protected Inventory virtualInventory; //actually, should be called corpseInventory

	protected Inventory partsInventory;

	/*23*/ protected SpellList spellRefs;


	/*25*/ protected MobileList combatRefs;
	//protected ArrayList combatRefsMemory;
	/*26*/ protected Inventory wieldedWeapons;
	protected Inventory wornItems;
	/*27*/ protected Vector knownSpellRefs;


	/**Descripciones de subcosas del bicho.*/
	/*30*/ protected String extraDescriptions; //OLD
	protected List extraDescriptionArrays; /*List of Description Arrays*/
	protected List extraDescriptionNameArrays; /*List of String Arrays*/

	/**bit vector (o bits, a secas, mï¿½s bien)*/
	/*40*/ protected boolean properName = false; //no se le trata de "un" ni de "el".
	//por defecto, false.

	/**Lista de miembros*/
	/*50*/ protected Inventory wieldingLimbs;

	/**Traits*/
	/*60,61*/ protected Traits caracteristicas;
	/*62*/ protected int hp, mp, maxhp, maxmp;

	/**Bitvectors de estado de relaciones con entidades (variables de memoria)*/
	//	/*70*/ protected Vector relationships = new Vector(); //vector of Entity
	//		protected Vector relationship_properties = new Vector(); //vector of List of Property

	/**Cï¿½digo en Ensamblador Virtual Aetheria (EVA)*/
	/*80*/ protected ObjectCode itsCode;

	/**Especificaciones de habla PSI*/
	/*82*/
	protected Vector PSIanswers; //vector of Description[]s	
	protected Vector PSIkeywords; //vector or Strings


	//initted in constructor
	protected InputOutputClient io /*= new InputOutputClientNula()*/; //no hace nada para los bichos que no sean Informadores


	String exitname; //nombre de la salida hacia la que estï¿½ yendo, si estï¿½ en estado "go"

	//for combat
	//private Weapon usingWeapon; //weapon used for a given attack
	Spell usingSpell; //spell bein' used

	//aleatorios	
	private Random aleat;

	protected NaturalLanguage lenguaje;


	protected World mundo; //su mundo actual


	///////////
	//METHODS//
	///////////

	public Mobile ( )
	{

	}

	/**
	 * Este constructor solo llama a constructMobile, que es el constructor de verdad. Esta dualidad se debe a las llamadas recursivas que debe hacer constructRoom para soportar herencia dinamica (mobs que copian datos de otros)
	 *
	 */
	public Mobile ( World mundo , String mobfile ) throws IOException , FileNotFoundException
	{
		this.mundo = mundo;
		io = new NullInputOutputClient();
		lenguaje = mundo.getLanguage();
		constructMob ( mundo , mobfile , true , "none" );
		//el constructor de cada subclase de Mobile llamarï¿½ con esa subclase en vez de none:
		//public Daedra harï¿½
		//constructMob ( mundo , mobfile , true , "daedra" );
	}

	public Mobile ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		this.mundo = mundo;
		io = new NullInputOutputClient();
		lenguaje = mundo.getLanguage();
		constructMob ( mundo , n , true , "none" );
		//el constructor de cada subclase de Mobile llamarï¿½ con esa subclase en vez de none:
		//public Daedra harï¿½
		//constructMob ( mundo , mobfile , true , "daedra" );
	}

	/**
	 * Constructor para ser llamado desde fuera, construye todas las subclases de Mobile segï¿½n el fichero
	 *
	 */

	public static Mobile getInstance ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException("Mobile node not Element") );
		}

		//{n is an Element}
		org.w3c.dom.Element e = (org.w3c.dom.Element)n;

		Mobile ourNewMobile;

		if ( !e.hasAttribute("type") )
		{
			ourNewMobile = new Mobile ( mundo , n );
		}
		else if ( e.getAttribute("type").equalsIgnoreCase("daedra") )
		{
			// ourNewMobile = new Daedra ( mundo , n );
			ourNewMobile=null;
		}
		//else if...
		else
		{
			ourNewMobile = new Mobile ( mundo , n );
		}
		return ourNewMobile;

	}

	public static Mobile getInstance ( World mundo , String mobfile ) throws FileNotFoundException, IOException
	{
		String linea;
		String id_linea;
		FileInputStream fp = new FileInputStream ( mobfile );
		BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( fp ) );
		//leer el tipo de mob
		String mobtype = "none";
		for ( int line = 1 ; line < 100 ; line++ )
		{
			linea = filein.readLine();
			id_linea = StringMethods.getTok( linea , 1 , ' ' );
			linea = StringMethods.getToks( linea , 2 , StringMethods.numToks( linea , ' ' ) , ' ' );	
			try
			{
				if ( id_linea != null && Integer.valueOf(id_linea).intValue() == 0 )
					mobtype = linea;
			}
			catch ( NumberFormatException ex ) 
			{
				//no pasa nada, estaremos en una parte con codigo, etc.
			}	
		}
		//crear el bicho
		Mobile ourNewMob;
		if ( mobtype.equalsIgnoreCase("daedra") )
		{
			ourNewMob = new Mobile /*Daedra*/ ( mundo , mobfile );
			//llama a ourNewItem.constructItem ( mundo , itemfile , true , "weapon" );
		}
		else
		{
			ourNewMob = new Mobile ( mundo , mobfile );
			//llama a ourNewItem.constructItem ( mundo , itemfile , true , "none" );
		}
		return ourNewMob;
	}

	/**
	 * El pedazo de constructor que lee un bicho de un fichero.
	 *
	 */
	public void constructMob ( World mundo , String mobfile , boolean allowInheritance , String mobtype ) throws IOException, FileNotFoundException
	{
	    
	    this.setProperty("describeRoomsOnArrival" , true); //defaults to true
	    
		String linea;
		String id_linea;
		FileInputStream fp = new FileInputStream ( mobfile );
		BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( fp ) );
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
					/*construimos segun constructor del bicho de que heredamos*/
					constructMob ( mundo, Utility.mobFile(mundo,inheritsFrom) , true , mobtype ); 
					/*overrideamos lo que tengamos que overridear*/
					constructMob ( mundo, mobfile , false , mobtype );
					return; 
				}
				break;
			case 3:
				setNewState( Integer.valueOf(linea).intValue() ); break;
			case 4:
				title = linea; break;	
			case 5:
				/*la herencia fuerte se hace exactamente igual que la dï¿½bil, no pueden aparecer las dos (se ignorarï¿½a la 2a)*/
				isInstanceOf = Integer.valueOf(linea).intValue(); 
				if ( isInstanceOf < idnumber && allowInheritance ) /*el item del que heredamos debe tener ID menor*/
				{
					/*construimos segun constructor de la habitacion de que heredamos*/
					constructMob ( mundo, Utility.mobFile(mundo,isInstanceOf) , true , mobtype ); 
					/*overrideamos lo que tengamos que overridear*/
					constructMob ( mundo, mobfile , false , mobtype );
					return; 
				}
				break;

			case 10:
				//mob description list line
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

			case 20:
				//nonvirtual inventory line
			{
				int nObjects = StringMethods.numToks(linea,'$');
				inventory = new Inventory ( 10000,10000,nObjects );
				//Item[nObjects];
				for ( int i = 0 ; i < nObjects ; i++ )
				{
					try
					{
						inventory.addItem ( mundo.getItem(StringMethods.getTok(linea,i+1,'$')) );
					}
					catch (WeightLimitExceededException exc) 
					{
					}
					catch (VolumeLimitExceededException exc2)
					{
					}
				}
				break;
			}
			case 21:
				//virtual inventory line
			{
				int nObjects = StringMethods.numToks(linea,'$');
				virtualInventory = new Inventory ( 10000,10000,nObjects );
				//Item[nObjects];
				for ( int i = 0 ; i < nObjects ; i++ )
				{
					try
					{
						virtualInventory.addItem ( mundo.getItem(StringMethods.getTok(linea,i+1,'$')) );
					}
					catch (WeightLimitExceededException exc) 
					{
					}
					catch (VolumeLimitExceededException exc2)
					{
					}
				}
				break;
			}
			case 25:
				//combat opponents line
			{
				int nObjects = StringMethods.numToks(linea,'$');
				combatRefs = new MobileList ( nObjects );
				//Item[nObjects];
				for ( int i = 0 ; i < nObjects ; i++ )
				{
					combatRefs.addMobile ( mundo.getMob(StringMethods.getTok(linea,i+1,'$')) );
				}
				break;
			}
			case 26:
				//wielded weapons line
			{
				int nObjects = StringMethods.numToks(linea,'$');
				wieldedWeapons = new Inventory ( 10000,10000,nObjects );
				//Item[nObjects];
				for ( int i = 0 ; i < nObjects ; i++ )
				{
					try
					{
						wieldedWeapons.addItem ( mundo.getItem(StringMethods.getTok(linea,i+1,'$')) );
					}
					catch (WeightLimitExceededException exc) 
					{
					}
					catch (VolumeLimitExceededException exc2)
					{
					}
				}
				break;
			}
			case 40:
				//bitvectors area: proper name line (bicho con nombre propio)
			{
				try
				{
					if ( linea.equalsIgnoreCase("true") ||
							Integer.parseInt(linea) == 1 )
					{
						properName = true;
					}
				}
				catch ( NumberFormatException nfe )
				{
					;
				}
				break;
			}

			case 50:
				//limbs line
			{
				int nObjects = StringMethods.numToks(linea,'$');
				wieldingLimbs = new Inventory ( 10000,10000,nObjects );
				//Item[nObjects];
				for ( int i = 0 ; i < nObjects ; i++ )
				{
					try
					{
						wieldingLimbs.addItem ( mundo.getItem(StringMethods.getTok(linea,i+1,'$')) );
					}
					catch (WeightLimitExceededException exc) 
					{
					}
					catch (VolumeLimitExceededException exc2)
					{
					}
				}
				break;
			}

			case 70:
				//RSBL (Relationship State Bitvector List) line
			{
				//formato:
				// 0001$35@0003$32@0021$51
				// quiere decir: la relacion con 0001 esta en estado 35, la con 0003 en estado 32, y con 0021 en estado 51.	
				//Usa /*70*/ protected Vector relationships;
				//	         protected Vector relationship_states;

				relationships = new Vector(); //of Entity
				relationship_properties = new Vector(); //of List of PropertyEntry
				StringTokenizer st = new StringTokenizer ( linea , "@" );
				while ( st.hasMoreTokens() )
				{
					relationships.add ( mundo.getObject( st.nextToken()) );
					if ( st.hasMoreTokens() )
					{
						List l = new ArrayList();
						PropertyEntry pe = new PropertyEntry ( "state" , st.nextToken() , 0 );
						l.add(pe);
						relationship_properties.add(l);
					}
					//relationship_states.add ( Integer.valueOf(st.nextToken()) );
					else
					{
						List l = new ArrayList();
						PropertyEntry pe = new PropertyEntry ( "state" , "0" , 0 );
						l.add(pe);
						relationship_properties.add(l);
					}
					//relationship_states.add ( new Integer(0) );
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
			case 82:
				//begin PSI specs
			{
				String PSISpecString = linea;
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
						intval = 0;
					}
					if ( intval == 83 ) terminamos = true; //PSI spec termination line
					else
					{
						PSISpecString += "\n";
						PSISpecString += linea;
					}
				}
				loadPSISpecs ( PSISpecString );

				break;

			} //end case 82
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
			} //end for
			//poner bien la ID
			if ( getID() < 10000000 )
				idnumber += 20000000; //prefijo de bicho		
			if ( wieldingLimbs != null && wieldedWeapons == null )
			{
				//Debug.println("El conde Laurel.\n");
				//Debug.println(wieldingLimbs.size());
				wieldedWeapons = new Inventory(10000,10000,wieldingLimbs.size());
				wieldedWeapons.incrementSize(wieldingLimbs.size());
				//			try
				//			{
				//				for ( int k = 0 ; k < wieldingLimbs.size() ; k++ )
				//					wieldedWeapons.addItem(mundo.getItem(0));
				//Debug.println(wieldedWeapons.size());	
				//			}
				//			catch ( WeightLimitExceededException e1 )
				//			{
				//				Debug.println("Hmm... Exception.");
				//				//imposible weight/volume limit exceeded: el objeto nulo (0) no pesa.
				//			}	
				//			catch ( VolumeLimitExceededException e2 )
				//			{
				//				//idem
				//				Debug.println("Hmm... Exception2.");
				//			}
			}	
		}
		if ( mobtype.equalsIgnoreCase("daedra") )
		{
			/*((Daedra)this).readDaedraSpecifics ( mundo , mobfile )*/;
		}
	}


	public void constructMob ( World mundo , org.w3c.dom.Node n , boolean allowInheritance , String mobtype ) throws XMLtoWorldException
	{

	    this.setProperty("describeRoomsOnArrival" , true); //defaults to true
	    
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ( "Mobile node not Element" ) );
		}
		//{n is an Element}	
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;

		//default values
		mp = 0;
		maxmp = 0;
		properName = false;

		//weak inheritance?
		if ( e.hasAttribute("extends") && !e.getAttribute("extends").equals("0") && allowInheritance )
		{
			//item must extend from existing item.
			//clonamos ese item y overrideamos lo overrideable
			//(nï¿½tese que la ID del item extendido ha de ser menor).

			//por eso los associated nodes de los items quedan guardados [por ref] en el World hasta que
			//haya concluido la construccion del mundo

			//1. overrideamos el super-item usando su associated node para construirlo

			constructMob ( mundo , mundo.getMobileNode( e.getAttribute("extends") ) , true , mobtype );

			//2. overrideamos lo que debamos overridear

			constructMob ( mundo , n , false , mobtype );
		}

		//strong inheritance?
		if ( e.hasAttribute("clones") && !e.getAttribute("clones").equals("0") && allowInheritance )
		{
			//funciona igual que la weak inheritance a este nivel.
			//no deberian aparecer los dos; pero si asi fuera esta herencia (la fuerte) tendria precedencia.

			//1. overrideamos el super-item usando su associated node para construirlo

			constructMob ( mundo , mundo.getMobileNode( e.getAttribute("clones") ) , true , mobtype );

			//2. overrideamos lo que debamos overridear

			constructMob ( mundo , n , false , mobtype );
		}

		//mandatory XML-attribs exceptions
		//if ( !e.hasAttribute("id") )
		//	throw ( new XMLtoWorldException ( "Mobile node lacks attribute id" ) );
		if ( !e.hasAttribute("name") )
			throw ( new XMLtoWorldException ( "Mobile node lacks attribute name" ) );
		if ( !e.hasAttribute("hp") )
			throw ( new XMLtoWorldException ( "Mobile node lacks attribute hp" ) );		
		if ( !e.hasAttribute("maxhp") )
			throw ( new XMLtoWorldException ( "Item node lacks attribute maxhp" ) );
		if ( !e.hasAttribute("gender") )
			throw ( new XMLtoWorldException ( "Item node lacks attribute gender" ) );	

		//mandatory XML-attribs parsing
		try
		{
			//id no longer mandatory
			if ( e.hasAttribute("id") )
				idnumber = Integer.valueOf ( e.getAttribute("id") ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			throw ( new XMLtoWorldException ( "Bad number format at attribute id in mobile node" ) );
		}
		title = e.getAttribute("name");
		gender = Boolean.valueOf ( e.getAttribute("gender") ).booleanValue();
		try
		{
			hp = Integer.valueOf ( e.getAttribute("hp") ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			throw ( new XMLtoWorldException ( "Bad number format at attribute hp in item node" ) );
		}
		try
		{
			maxhp = Integer.valueOf ( e.getAttribute("maxhp") ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			throw ( new XMLtoWorldException ( "Bad number format at attribute maxhp in item node" ) );
		}
		if ( e.hasAttribute("mp") )
		{
			try
			{
				mp = Integer.valueOf ( e.getAttribute("mp") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				throw ( new XMLtoWorldException ( "Bad number format at attribute mp in item node" ) );
			}
		}
		if ( e.hasAttribute("maxmp") )
		{
			try
			{
				maxmp = Integer.valueOf ( e.getAttribute("maxmp") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				throw ( new XMLtoWorldException ( "Bad number format at attribute maxmp in item node" ) );
			}
		}

		//non-mandatory attribs
		if ( e.hasAttribute("properName") )
		{
			properName = Boolean.valueOf ( e.getAttribute("properName") ).booleanValue();
		}

		//Entity parsing
		readPropListFromXML ( mundo , n );

		//habitacion actual



		//PARA LOS BICHOS NO DAIOS,
		//NO SE NECESITA PONER UN ROOM. SE INICIALIZA AL METER AL BICHO EN LA HABITACIï¿½N (AL CARGAR HABITACIï¿½N)
		//(ROOMS SE CARGAN MAS TARDE QUE MOBILES)

		//A LOS BICHOS DAIO'S (DYNAMICALLY-ASSIGNED-ID OBJECTS) LOS PONEMOS EN LA HABITACION
		//QUE INDICA SU CAMPO HABITACIONACTUAL.

		boolean roomsdone=true;

		try
		{

			mundo.getRoom(0); //si esto da NullPointerException es que las habitaciones 
			//no estï¿½n inicializadas.
		}
		catch ( NullPointerException npe )
		{
			roomsdone = false;
		}


		if ( roomsdone )
		{

			org.w3c.dom.NodeList currentRoomNodes = e.getElementsByTagName("CurrentRoom");
			if ( currentRoomNodes.getLength() < 1 ) habitacionActual = null;
			else
			{
				org.w3c.dom.Element currentRoomNode = (org.w3c.dom.Element)currentRoomNodes.item(0);
				if ( !(currentRoomNode.hasAttribute("id")) )
					throw ( new XMLtoWorldException("CurrentRoom element lacking id at Mobile") );
				else
				{
					Room r = mundo.getRoom ( currentRoomNode.getAttribute("id") );
					if ( r == null )
						throw ( new XMLtoWorldException("CurrentRoom element pointing to unknown Room object") );
					habitacionActual = r;
				}				
			}

		}




		//habitacion anterior

		/*

		NOT LOADED AT THE MOMENT

		org.w3c.dom.NodeList lastRoomNodes = e.getElementsByTagName("LastRoom");
		if ( lastRoomNodes.getLength() < 1 ) habitacionActual = null;
		else
		{
			org.w3c.dom.Element lastRoomNode = (org.w3c.dom.Element)lastRoomNodes.item(0);
			if ( !(lastRoomNode.hasAttribute("id")) )
				throw ( new XMLtoWorldException("LastRoom element lacking id at Mobile") );
			else
			{
				Room r = mundo.getRoom ( lastRoomNode.getAttribute("id") );
				if ( r == null )
					throw ( new XMLtoWorldException("LastRoom element pointing to unknown Room object") );
				habitacionAnterior = r;
			}				
		}

		 */

		//description list
		descriptionList = Utility.loadDescriptionListFromXML ( mundo , e , "DescriptionList" , true );

		//singular name description list
		singNames = Utility.loadDescriptionListFromXML ( mundo , e , "SingularNames" , true );

		//plural name description list
		plurNames = Utility.loadDescriptionListFromXML ( mundo , e , "PluralNames" , true );

		//singular reference names (respondToSing)
		respondToSing = Utility.loadNameListFromXML ( mundo , e , "SingularReferenceNames" , true );

		//plural reference names (respondToPlur)
		respondToPlur = Utility.loadNameListFromXML ( mundo , e , "PluralReferenceNames" , true );

		//inventory, virtual inventory, wielded weapons inv, wielding limbs inv:
		//not DIFERIDA.

		org.w3c.dom.NodeList inventoryNodes = e.getElementsByTagName ( "Inventory" );
		//solo nos interesan los hijos DIRECTOS
		List realInventoryNodes = new ArrayList();
		for ( int i = 0 ; i < inventoryNodes.getLength() ; i++ )
			if ( inventoryNodes.item(i).getParentNode() == e ) realInventoryNodes.add ( inventoryNodes.item(i) );

		if ( realInventoryNodes.size() < 1 )
		{
			//Debug.println("No inventory nodes, inventory will be left null.");
			inventory = null;
		}
		else
		{
			//Debug.println("Inventory nodes present.");
			inventory = new Inventory ( mundo , (org.w3c.dom.Node)realInventoryNodes.get(0) );
			//Debug.println("Inventory size: " + inventory.size() );
			//Debug.println("Inventory node: " + inventoryNodes.item(0) );
			//Debug.println("Inventory's parent: " + inventoryNodes.item(0).getParentNode() );
			//Debug.println(inventory);
		}			

		//virtual inv
		org.w3c.dom.NodeList virtualInvNodes = e.getElementsByTagName ( "VirtualInventory" );
		if ( virtualInvNodes.getLength() < 1 )
			virtualInventory = null;
		else
		{
			org.w3c.dom.Element e2 = ( org.w3c.dom.Element ) virtualInvNodes.item(0);

			org.w3c.dom.NodeList invNodes = e2.getElementsByTagName("Inventory");
			if ( invNodes.getLength() < 1 )
				virtualInventory = null;
			else
				virtualInventory = new Inventory ( mundo , invNodes.item(0) );	
		}		

		//welded weapon inv
		org.w3c.dom.NodeList wieldedInvNodes = e.getElementsByTagName ( "WieldedWeaponsInventory" );
		if ( wieldedInvNodes.getLength() < 1 )
			wieldedWeapons = null;
		else
		{
			org.w3c.dom.Element e2 = ( org.w3c.dom.Element ) wieldedInvNodes.item(0);

			org.w3c.dom.NodeList invNodes = e2.getElementsByTagName("Inventory");
			if ( invNodes.getLength() < 1 )
				wieldedWeapons = null;
			else
				wieldedWeapons = new Inventory ( mundo , invNodes.item(0) );	
		}

		//worn items inv
		org.w3c.dom.NodeList wornItemNodes = e.getElementsByTagName ( "WornItemsInventory" );
		if ( wornItemNodes.getLength() < 1 )
			wornItems = null;
		else
		{
			org.w3c.dom.Element e2 = ( org.w3c.dom.Element ) wornItemNodes.item(0);

			org.w3c.dom.NodeList invNodes = e2.getElementsByTagName("Inventory");
			if ( invNodes.getLength() < 1 )
				wornItems = null;
			else
				wornItems = new Inventory ( mundo , invNodes.item(0) );	
		}

		//wielding limbs inv: obsolete
		/*
		org.w3c.dom.NodeList wieldingInvNodes = e.getElementsByTagName ( "WieldingLimbsInventory" );
		if ( wieldingInvNodes.getLength() < 1 )
			wieldingLimbs = null;
		else
		{
			org.w3c.dom.Element e2 = ( org.w3c.dom.Element ) wieldingInvNodes.item(0);

			org.w3c.dom.NodeList invNodes = e2.getElementsByTagName("Inventory");
			if ( invNodes.getLength() < 1 )
				wieldingLimbs = null;
			else
			{
				wieldingLimbs = new Inventory ( mundo , invNodes.item(0) );	
				Debug.println(this + "Wielding Limbs Inventory: " + wieldingLimbs);
			}
		}
		 */
		wieldingLimbs = null;

		//spell list

		org.w3c.dom.NodeList spellNodes = e.getElementsByTagName("SpellList");
		if ( spellNodes.getLength() < 1 )
			spellRefs = null;
		else
			spellRefs = new SpellList ( mundo , spellNodes.item(0) );	


		//parts inv
		org.w3c.dom.NodeList partsInvNodes = e.getElementsByTagName ( "Parts" );
		if ( partsInvNodes.getLength() < 1 )
			partsInventory = null;
		else
		{
			org.w3c.dom.Element e2 = ( org.w3c.dom.Element ) partsInvNodes.item(0);

			org.w3c.dom.NodeList invNodes = e2.getElementsByTagName("Inventory");
			if ( invNodes.getLength() < 1 )
				partsInventory = null;
			else
			{
				partsInventory = new Inventory ( mundo , invNodes.item(0) );	
				Debug.println(this + "Parts Inventory: " + wieldingLimbs);
			}
		}

		//caracteristicas (traits): not at the moment.
		//or... yeWwwW!
		org.w3c.dom.NodeList traitNodes = e.getElementsByTagName ( "Traits" );
		if ( traitNodes.getLength() > 0 )
		{
			try
			{
				caracteristicas = new Traits ( mundo , traitNodes.item(0) );
			}
			catch ( XMLtoWorldException ex )
			{
				throw ( new XMLtoWorldException ( "Exception at Traits node: " + ex.getMessage() ) );
			}
		}

		//if traits weren't specified, init them to default
		if ( caracteristicas == null ) caracteristicas = new Traits();

		//limit inventory according to strength
		long strength = Math.max( caracteristicas.getSkill("FUE") , caracteristicas.getSkill("STR") );
		try
		{
			inventory.setVolumeLimit((int)(2000+10*strength));
			inventory.setWeightLimit((int)(2000+10*strength));
		}
		catch ( Exception exc )
		{
			System.err.println("[Warning] Mobile " + this.getTitle() + " carrying items too heavy or big for its strength of " + strength);
		}

		//relationship-states: not at the moment

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

		//load PSI specifications

		loadPSISpecs ( mundo , e );

		//FINALLY... type-specifics!!

		if ( mobtype.equalsIgnoreCase("daedra") )
		{
			//((Daedra)this).readDaedraSpecifics ( mundo , e );
			;
		}
		//poner bien la id
		if ( getID() < 10000000 )
			idnumber += 20000000; //prefijo de bicho	

		//correcciï¿½n inventarios paralelos (copiada del otro, ï¿½dobla innecesariamente?)
		if ( wieldingLimbs != null && wieldedWeapons == null )
		{
			wieldedWeapons = new Inventory(10000,10000,wieldingLimbs.size());
			wieldedWeapons.incrementSize(wieldingLimbs.size());
		}

		if ( wornItems == null )
		{
			wornItems = new Inventory(10000,10000);
		}

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
			mundo.write("BeanShell error on initting mobile " + this + ": error was " + te);
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

	public String getDescription ( long comparand )
	{
		String desString="";
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			if ( descriptionList[i].matches(comparand) )
				desString += descriptionList[i].getText();
		}	
		return desString;	
	}

	public String getDescription ( Entity viewer )
	{
		String desString="";
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			if ( descriptionList[i].matchesConditions(this,viewer) )
				desString += descriptionList[i].getText();
		}	
		return desString;	
	}

	/*
	public boolean isInvisible ( int comparand )
	{
		if ( !properName )
			return ( StringMethods.numToks ( constructName(1,comparand) , ' ' ) < 2 );
		else
			return ( StringMethods.numToks ( constructName(1,comparand) , ' ' ) < 1 );
	}
	 */

	public boolean isInvisible ( Entity viewer )
	{
		return !( getSingName(viewer).length() > 0 );
		/*
		if ( !properName )
			return ( StringMethods.numToks ( constructName(1,viewer) , ' ' ) < 2 );
		else
			return ( StringMethods.numToks ( constructName(1,viewer) , ' ' ) < 1 );
		 */
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

	public String getName ( boolean s_p )
	{
		Description[] theList;
		if ( s_p ) theList = singNames;
		else theList = plurNames;
		String desString="";
		for ( int i = 0 ; i < theList.length ; i++ )
		{
			if ( theList[i].matchesConditions(this) )
				desString += theList[i].getText();
		}	
		return desString;
	}

	public String getSingName ( int comparand )
	{
		return getName ( true , comparand );
	}

	public String getPlurName ( int comparand )
	{
		return getName ( false , comparand );	
	}

	public String getSingName()
	{
		return getName ( true );
	}

	public String getPlurName()
	{
		return getName ( false );
	}

	//coge de comparando el estado del bicho
	public String constructName ( int nItems )
	{
		return constructName ( nItems , getState() );
	}

	//devuelve "una espada", "dos hachas", etc.
	public String constructName ( int nItems , int comparand )
	{
		boolean properName = this.properName;

		if ( nItems == 1 )
		{
			String baseName = getSingName( comparand );
			if ( baseName.startsWith("P$") )
			{
				properName = true;
				baseName = baseName.substring(2);
			}

			if ( properName )
				return baseName;
			else if ( gender )
				return ( "un " + baseName );
			else
				return ( "una " + baseName );	
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

	public String constructName2 ( int nItems )
	{
		return constructName2 ( nItems , getState() );
	}

	//devuelve "la espada", "dos hachas", etc.
	public String constructName2 ( int nItems , int comparand )
	{

		boolean properName = this.properName;

		if ( nItems == 1 )
		{

			String baseName = getSingName( comparand );
			if ( baseName.startsWith("$") )
			{
				properName = true;
				baseName = baseName.substring(1);
			}

			if ( properName )
				return baseName;
			if ( gender )
				return ( "el " + baseName );
			else
				return ( "la " + baseName );	
		}	
		else
		{
			return ( nItems + " " + getPlurName( comparand ) );	
		}
	}

	/*
	public String getExtraDescription ( String thingieName )
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

	public String getExtraDescription ( String thingieName , Entity viewer )
	{
		if ( thingieName == null || thingieName.length() == 0 ) return null; 
		for ( int i = 0 ; i < extraDescriptionNameArrays.size() ; i++ )
		{
			String[] curNameArray = (String[]) extraDescriptionNameArrays.get(i);
			Description[] curDesArray = (Description[]) extraDescriptionArrays.get(i);
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

	//nos devuelve el nombre mï¿½s especï¿½fico con el que nos podemos referir al objeto en
	//un comando. ï¿½til para zonas de referencia.
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


	/**
	 * Nos dice si se da por aludido el objeto ante un comando (ej. "mirar piedra") -> se pasa "piedra")
	 *
	 * @param commandArgs Los argumentos del comando.
	 * @param pluralOrSingular 1 si plural.
	 * @return 0 si no se da por aludido, 1 si sï¿½, y con quï¿½ prioridad. (+ nï¿½ = - prioridad). La prioridad es el orden que ocupa el nombre que se corresponde con el comando dado en la lista de nombres.
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
			//"mirar la piedra pequeï¿½a" -> commandArgs="la piedra pequeï¿½a" -> vamos analizando "la piedra pequeï¿½a", "piedra pequeï¿½a", ...
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

	public boolean isSame ( Mobile other )
	{
		return (  
				( idnumber == other.getID() )
				||	( idnumber == other.getInstanceOf () ) 
				|| ( isInstanceOf == other.getID () )
				|| ( isInstanceOf == other.getInstanceOf () && isInstanceOf != 0 )  );
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

	public long getSkill ( String s )
	{

		if ( caracteristicas == null )
			caracteristicas = new Traits();

		return caracteristicas.getSkill(s);

	}

	public int getStat ( String s )
	{
		//ej. getStat("FUE") , getStat("INT")

		int value = 0;

		if ( caracteristicas != null )
			value = (int)caracteristicas.getStat(s);
		if ( value == 0 )
			return 12;	
		else
			return value;	


		//this is the correct one!
		//return caracteristicas.getStat(s);

		//this is temporal garbage, remove it!
		//return 12;

	}

	public Weapon getCurrentWeapon()
	{
		return (Weapon) mundo.getItem(getPropertyValueAsInteger("usingWeapon"));
	}

	public void setCurrentWeapon( Weapon w )
	{
		setProperty("usingWeapon",w.getID());
	}

	public Spell getCurrentSpell()
	{
		return (Spell) mundo.getSpell(getPropertyValueAsInteger("usingSpell"));
	}

	public void setCurrentSpell( Spell s )
	{
		setProperty("usingSpell",s.getID());
	}


	public void setRoom ( Room nuevaHabitacion )
	{
		habitacionAnterior = habitacionActual;
		if ( habitacionActual != null )
		{
			habitacionActual.removeMob(this);
			habitacionActual = nuevaHabitacion;
			if ( habitacionActual != null )
				habitacionActual.addMob(this);
		}
		else
		{
			habitacionActual = nuevaHabitacion;
			if ( habitacionActual != null && !habitacionActual.hasMobile(this)  )
				habitacionActual.addMob(this);
		}
	}

	public void setInventory ( Inventory inv )
	{
		inventory = inv;
	}

	public Room getRoom ( )
	{
		return habitacionActual;
	}

	public Room getLastRoom ( )
	{
		return habitacionAnterior;
	}

	//begin
	//relationship-related functions

	/*
		Las relaciones con estado entre mï¿½viles y otros objetos (o tal vez, en el
		futuro, entre entidades en general, si hiciera falta?) resultan ï¿½tiles.
		Gracias a ellas, un jugador puede, por ejemplo, "recordar" lo que habï¿½a
		en una habitaciï¿½n: al mirarla se establece un estado 1 (por ejemplo) en su
		relaciï¿½n con esa entidad habitaciï¿½n, de tal modo que cuando vuelve a ella
		ya no tiene que "buscar" algo sino que lo ve directamente, por ejemplo.

		No para cambios objetivos en la habitaciï¿½n que verï¿½a cualquiera (como el
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
	//esto es para carga DIFERIDA!! Son entitys, no sabemos si de las que se cargan antes o despuï¿½s.
	public void readRelationshipListFromXML ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{

		if ( ! ( n instanceof org.w3c.dom.Element ) ) throw ( new XMLtoWorldException ( "Mobile node not Element" ) );
		org.w3c.dom.Element e = ( org.w3c.dom.Element) n;
		org.w3c.dom.NodeList nl = e.getElementsByTagName("RelationshipList");
		if ( nl.getLength() < 1 ) ; //no hacemos nada porque relationships y cia ya son un new Vector()
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


	/**Relationship functions yo-yo'd up to Entity*/
	/*
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
				//lista de propiedades de la relaciï¿½n
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

	public boolean getRelationshipPropertyValueAsBoolean ( Entity e , String propertyName )
	{
		int lim = relationships.size();
		for ( int k = 0 ; k < lim ; k++ )
		{
			if ( relationships.elementAt(k).equals(e) )
			{
				//lista de propiedades de la relaciï¿½n
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

	 */
	/*
	public void setRelationshipProperty ( Entity e , String propertyName , String propertyValue )
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
	 */
	/*
	public void setRelationshipProperty ( Entity e , String propertyName , int propertyValue )
	{
		setRelationshipProperty ( e , propertyName , String.valueOf ( propertyValue ) );
	}

	public void setRelationshipState ( Entity e , int newState )
	{
		setRelationshipProperty ( e , "state" , newState );

	}
	 */

	//end
	//relationship-related functions
	//
	//





	//public Entity getObject ( int objectid )


	private void loadPSISpecs ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{

		org.w3c.dom.Element e;
		try
		{
			e = (org.w3c.dom.Element) n;
		}
		catch ( ClassCastException cce )
		{
			throw ( new XMLtoWorldException ( "Mobile node not Element" ) );	
		}
		//{e is this mobile's Element}
		org.w3c.dom.NodeList convAINodes = e.getElementsByTagName( "ConversationalAI" );
		if ( convAINodes.getLength() > 0 )
		{
			//get the first node labelled <ConversationalAI>, in fact there should be only one
			org.w3c.dom.Element convAINode = (org.w3c.dom.Element) convAINodes.item(0);

			//now process all mappings
			org.w3c.dom.NodeList mappingNodes = convAINode.getElementsByTagName("Mapping");
			for ( int i = 0 ; i < mappingNodes.getLength() ; i++ )
			{
				org.w3c.dom.Element curMappingNode = (org.w3c.dom.Element) mappingNodes.item(i);
				if ( !curMappingNode.hasAttribute("command") )
					throw ( new XMLtoWorldException ( "Mapping node lacking attribute command" ) );
				else
				{
					//search for description node
					org.w3c.dom.NodeList desNodes = curMappingNode.getElementsByTagName("Description");
					if ( desNodes.getLength() < 1 )
						throw ( new XMLtoWorldException ( "Mapping node lacking Description element" ) );
					else
					{
						//org.w3c.dom.Element curDescriptionNode = (org.w3c.dom.Element) desNodes.item(0);
						Description[] answer = Utility.loadDescriptionListFromXML ( mundo , curMappingNode );
						if ( answer == null )
							continue;
						else
						{
							if ( PSIkeywords == null )
								PSIkeywords = new Vector();
							if ( PSIanswers == null )
								PSIanswers = new Vector();	
							//init this PSI question-answer pair	
							PSIkeywords.add ( curMappingNode.getAttribute("command") );
							PSIanswers.add ( answer );

							Debug.println("Adding answers to " + curMappingNode.getAttribute("command") );
							Debug.println("They are for example " + answer[0].getText() );

						}		
					}
				}				

			}

		}
		else
		{
			//let the PSI specs be null
			return;
		}
	}

	private void loadPSISpecs ( String PSISpecList )
	{

		int len = StringMethods.numToks(PSISpecList,'\n');

		PSIanswers = new Vector();	
		PSIkeywords = new Vector();

		StringTokenizer lineas = new StringTokenizer ( PSISpecList , "\n" );

		int nlinea = -1;
		while ( lineas.hasMoreTokens() )
		{
			nlinea++;
			String linea = lineas.nextToken();

			PSIanswers.addElement( Utility.loadDescriptionListFromString  //last token with $
					( StringMethods.getTok ( linea ,  StringMethods.numToks(linea,'$') , '$' ) ) );
			PSIkeywords.addElement( StringMethods.getToks( linea , 1 , StringMethods.numToks(linea,'$')-1 , '$' ).trim() );

		}

	}

	public String getPSIAnswer ( String question )
	{

		if ( PSIanswers == null || PSIanswers.size() == 0 ) return null;

		for ( int i = 0 ; i < PSIanswers.size() ; i++ )
		{

			//Aquï¿½ es donde hay que aï¿½adir el soporte de Regular Expressions.
			//En vez de isSubstringOf, matchesRegEx.

			//keywords separadas con "$"
			StringTokenizer st = new StringTokenizer ( (String)PSIkeywords.elementAt(i) , "$" );
			String tok;

			while ( st.hasMoreTokens() )
			{
				tok = st.nextToken();
				if ( StringMethods.isSubstringOf ( tok.toLowerCase() , question.toLowerCase() ) )
				{
					Description[] nuestraRespuesta = (Description[])PSIanswers.elementAt(i);
					String stringRespuesta = null;
					for ( int j = 0 ; j < nuestraRespuesta.length ; j++ )
					{
						if ( nuestraRespuesta[j].matches(getState()) )
						{
							if ( stringRespuesta == null ) stringRespuesta = nuestraRespuesta[j].getText();
							else stringRespuesta += nuestraRespuesta[j].getText();
						}
					}
					return stringRespuesta;
				}
			}

		}

		return null;

	}

	/*
	Una posible cosa a hacer con respecto a esta funciï¿½n es usar el "escribir" como caso particular
	de ella en los Informadores, (if instanceof informador then escribir, p.ej.) y de este modo
	cambiar las funciones informAction para que en realidad sï¿½lo hagan a los Mobiles ejecutar
	reactToRoomText, y que cada uno lo interprete como pueda.
	 */
	public void reactToRoomText ( String text )
	{

		boolean ejecutado = false;

		//room text reaction evt
		try
		{
			ejecutado = execCode ( "onRoomText" ,
					new Object[]
					           {
					text
					           }
			);
		}
		catch ( bsh.TargetError te )
		{
			write(""+te);
			writeError(ExceptionPrinter.getExceptionReport(te));
			te.printStackTrace();
		}

		if ( ejecutado ) return;


		if ( StringMethods.isSubstringOf ( " dice " , text.toLowerCase() ) )
		{
			//reaccionar a alguien que dice algo

			Debug.println("Full-string: " + text);

			Vector sepvector = new Vector();
			sepvector.addElement(" te dice "); //nï¿½tese que este token se mira antes.
			sepvector.addElement(" dice ");
			Vector analisis = StringMethods.tokenizeWithComplexSeparators ( text , sepvector , true );
			String elSujeto = ((String)analisis.elementAt(0)).trim();

			String resto;
			boolean vaPorTi=false;
			if ( analisis.size() < 2 )
			{
				resto = "";
			}
			else if (  ((String)analisis.elementAt(1)).equals( " te dice " )  )
			{
				vaPorTi=true;
				resto = ((String)analisis.elementAt(2)).trim();
			}
			else
			{
				resto = ((String)analisis.elementAt(2)).trim();
			}

			Debug.println("El sujeto's string: " + elSujeto);

			//Debug.println("EELLL SUJETOOO: " + elSujeto);

			Mobile elSujetoEnSi=null;
			Mobile elObjetoEnSi=null;



			Vector patternMatchVectorSing = habitacionActual.mobsInRoom.patternMatch ( elSujeto , false ); //en singular
			if ( patternMatchVectorSing != null && patternMatchVectorSing.size() > 0 )
			{
				elSujetoEnSi = (Mobile)patternMatchVectorSing.elementAt(0);
			}
			else elSujetoEnSi=null;
			String loQueDice=null;

			//quitar comillas

			StringTokenizer st = new StringTokenizer ( resto,"\"",false );
			if ( !st.hasMoreTokens() ) return;
			loQueDice = st.nextToken();

			if ( st.hasMoreTokens() )
			{
				resto = st.nextToken(); //esto puede ser el objeto.
				//por ejemplo, en "juan dice "hola" a pedro"
				//primera tokenizaciï¿½n: elSujeto juan, resto "hola" a pedro
				//segunda: loQueDice hola, resto a pedro

				Debug.println("Resto's string: " + resto);

				Vector patternMatchVectorSing2 = habitacionActual.mobsInRoom.patternMatch ( resto , false ); //en singular
				if ( patternMatchVectorSing2 != null && patternMatchVectorSing2.size() > 0 )
				{
					elObjetoEnSi = (Mobile)patternMatchVectorSing2.elementAt(0);
				}

				Debug.println("El objeto: " + elObjetoEnSi);

			}

			if ( vaPorTi ) 
			{
				elObjetoEnSi = this;
			}

			//quitar comillas

			/*
			if ( loQueDice.length() < 1 ) return;
			if ( loQueDice.charAt(0) == '\"' )
				loQueDice = loQueDice.substring(1);
			if ( loQueDice.length() < 1 ) return;	
			if ( loQueDice.charAt(loQueDice.length()-1) == '.' )
				loQueDice = loQueDice.substring(0,loQueDice.length()-2);
			if ( loQueDice.length() < 1 ) return;	
			if ( loQueDice.charAt(loQueDice.length()-1) == '\"' )
				loQueDice = loQueDice.substring(0,loQueDice.length()-2);
			if ( loQueDice.length() < 1 ) return;	
			 */

			try
			{

				Debug.println("Calling event say with " + elSujetoEnSi + "," + loQueDice);

				execCode ( "event_say",
						"this: " + getID() 
						+ "\n" + "speaker_id: " + ((elSujetoEnSi!=null)?String.valueOf(elSujetoEnSi.getID()):null)
						+ "\n" + "speaker_name: " + elSujeto
						+ "\n" + "sentence: " + loQueDice );

				//onSay con parï¿½metro objeto

				ejecutado=false;
				ejecutado = execCode ( "onSayTo" ,
						new Object[]
						           {
						elSujetoEnSi , loQueDice , elObjetoEnSi
						           }
				);

				if ( ! ejecutado )
				{
					//onSay sin parï¿½metro objeto

					execCode ( "onSay" ,
							new Object[]
							           {
							elSujetoEnSi , loQueDice
							           }
					);
				}

			}
			catch ( EVASemanticException exc ) 
			{
				write(io.getColorCode("error") + "EVASemanticException found at event_say, mob number " + getID() + io.getColorCode("reset") );
			}
			catch ( bsh.TargetError te )
			{
				write(io.getColorCode("error") + "bsh.TargetError found at event_say, mob number " + getID() + io.getColorCode("reset") );
				writeError(ExceptionPrinter.getExceptionReport(te));
			}

			String respuesta = getPSIAnswer ( loQueDice );
			if ( respuesta != null )
			{
				say ( respuesta );
			}
			else
			{
				respuesta = getPSIAnswer ( "default" );
				if ( respuesta != null )
					say ( respuesta );
			}
		}
	}


	public void say ( String text )
	{
		//escribir("\n");
		if ( text.trim().equals("") ) return;
		habitacionActual.reportActionAuto ( this , null , null , "$1 dice \"" + text + "\".\n" , true );
		//setNewState( 1 , 1 );
		//ZR_verbo = command;
		//return true;
		//for ( int i = 0 ; i < habitacionActual.mobsInRoom.size() ; i++ ) ;
	}
	
	public void say ( String text , String style )
	{
		if ( text.trim().equals("") ) return;
		habitacionActual.reportActionAuto ( this , null , null , "$1 dice \"" + text + "\".\n" , style , true );	
	}

	public void sayTo ( Mobile m , String text )
	{
		habitacionActual.reportAction ( this , m , null , "$1 dice \"" + text + "\" a $2.\n" , "$1 te dice \"" + text + "\".\n" , "dices \"" + text + "\" a $2.\n" , true );
	}
	
	public void sayTo ( Mobile m , String text , String style )
	{
		habitacionActual.reportAction ( this , m , null , "$1 dice \"" + text + "\" a $2.\n" , "$1 te dice \"" + text + "\".\n" , "dices \"" + text + "\" a $2.\n" , style , true );
	}

	//	procesa el comando decir
	protected void comandoDecir ( String args )
	{

		//si hay comillas, distinguimos lo de dentro de lo de fuera

		StringTokenizer st = new StringTokenizer ( " " + args , "\"" );
		//el espacio aï¿½adido a args en la lï¿½nea enterior es para que el primer token estï¿½ siempre fuera
		boolean tamosDentro = false;
		String dentro="";
		String fuera="";
		while ( st.hasMoreTokens() )
		{
			if ( tamosDentro )
				dentro+=st.nextToken();
			else
				fuera+=st.nextToken();
			tamosDentro=!tamosDentro;	
		}

		dentro = dentro.trim();
		fuera = fuera.trim();

		Debug.println("DENTRO:"+dentro);
		Debug.println("FUERA:"+fuera);

		if ( dentro.equalsIgnoreCase("") ) //no habï¿½a comillas
		{
			//interpretaciï¿½n en este caso:
			//decir hola a jorge -> hablas a todos
			//decir hola -> hablas a todos
			//ergo, no tenemos que buscar bichos (de momento)

			say ( fuera );

		}

		else //habï¿½a comillas
		{
			//interpretaciï¿½n:
			//decir "hola" -> a todos
			//decir "hola" a jorge, decir a jorge "hola" -> solo a jorge
			//ergo, tenemos que buscar en fuera a ver si hay un bicho y decirle dentro.

			MobileList ml = getRoom().getMobiles();
			Vector patternMatchVectorSing = ml.patternMatch ( fuera , false ); //en singular
			Vector patternMatchVectorPlur = ml.patternMatch ( fuera , true ); //en plural

			if ( patternMatchVectorSing.size() > 0 )
			{
				//decimos algo a un bicho
				sayTo ( ((Mobile)patternMatchVectorSing.elementAt(0)) , dentro  );
			}

			else if ( patternMatchVectorPlur.size() > 0 )
			{

				//no era en singular, probamos en plural.
				Mobile ourMob;
				for ( int i = 0 ; i < patternMatchVectorPlur.size() ; i++ )
				{
					ourMob = (Mobile)patternMatchVectorPlur.elementAt(i);
					sayTo ( ourMob , dentro );
				}	
			}
			else //no hay bicho que valga
			{
				say ( dentro );
			}

		}



	}

	private void defaultShowInventory( )
	{
		if ( inventory != null )
		{
			String str = inventory.toString(this);
			if ( str.equalsIgnoreCase("nada.") ) write( io.getColorCode("information") + mundo.getMessages().getMessage("you.have.nothing",new Object[]{this}) + io.getColorCode("reset") );
			else
			{
				write( io.getColorCode("information") + 
						/*"Tienes "*/  mundo.getMessages().getMessage("you.have.items","$inventory",str.substring(0,str.length()-1),new Object[]{this})
						//+ str 
						+ io.getColorCode("reset") );

				Inventory limbs = getFlattenedPartsInventory();
				//cosas que blandes
				Inventory wieldedWeapons = getWieldedWeapons(); //this shadows the homonymous attribute
				if ( wieldedWeapons != null )
					for ( int i = 0 ; i < wieldedWeapons.size() ; i++ )
					{
						if ( wieldedWeapons.elementAt(i) != null )
						{
							/*
							escribir( io.getColorCode("information") + "Llevas " + (wieldedWeapons.elementAt(i)).constructName2True(1,(wieldedWeapons.elementAt(i)).getState())  + " en " + (wieldingLimbs.elementAt(i)).constructName2True(1,(wieldingLimbs.elementAt(i)).getState()) + ".\n" + io.getColorCode("reset") );
							 */
							Item arma = wieldedWeapons.elementAt(i);
							//buscar miembro que blande el arma
							for ( int j = 0 ; j < limbs.size() ; j++ )
							{
								Item miembro = limbs.elementAt(j);
								if ( miembro.getRelationshipPropertyValueAsBoolean(arma,"wields") )
								{
									write( io.getColorCode("information") + 
									//"Blandes " + arma.constructName2OneItem(this)  + " en " + miembro.constructName2OneItem(this) + ".\n" 
									mundo.getMessages().getMessage("you.are.wielding.item","$item",arma.constructName2OneItem(this),"$limbs",miembro.constructName2OneItem(this),new Object[]{this,arma,miembro})
									+ io.getColorCode("reset") );
									//break;
								}
							}
						}
					}
				//cosas que llevas puestas
				Inventory wornItems = getWornItems(); //this shadows the homonymous attribute
				if ( wornItems != null )
					for ( int i = 0 ; i < wornItems.size() ; i++ )
					{
						if ( wornItems.elementAt(i) != null )
						{
							/*
							escribir( io.getColorCode("information") + "Llevas " + (wieldedWeapons.elementAt(i)).constructName2True(1,(wieldedWeapons.elementAt(i)).getState())  + " en " + (wieldingLimbs.elementAt(i)).constructName2True(1,(wieldingLimbs.elementAt(i)).getState()) + ".\n" + io.getColorCode("reset") );
							 */
							Item vestido = wornItems.elementAt(i);
							Vector miembrosOcupados = new Vector();
							//buscar miembros que visten el wearable
							for ( int j = 0 ; j < limbs.size() ; j++ )
							{
								Item miembro = limbs.elementAt(j);
								if ( miembro.getRelationshipPropertyValueAsBoolean(vestido,"wears") )
								{
									miembrosOcupados.add(miembro);
								}
							}
							//output
							String toOutput="";
							for ( int j = 0 ; j < miembrosOcupados.size() ; j++ )
							{
								Item limb = (Item)miembrosOcupados.get(j);
								if ( j == 0 )
									toOutput += limb.constructName2OneItem(this);
								else if ( j > 0 && j == miembrosOcupados.size() - 1 )
									toOutput += " y " + limb.constructName2OneItem(this);
								else
									toOutput += ", " + limb.constructName2OneItem(this);
							}
							write( io.getColorCode("information") + 
							//"Llevas " + vestido.constructName2OneItem(this)  + " en " + toOutput + ".\n"
							mundo.getMessages().getMessage("you.are.wearing.item","$item",vestido.constructName2OneItem(this),"$limbs",toOutput,new Object[]{this,vestido,toOutput})
							+ io.getColorCode("reset") );
						}
					}
			}
		}
		else write( io.getColorCode("information") + mundo.getMessages().getMessage("you.have.nothing",new Object[]{this}) + io.getColorCode("reset") );
	}


	public void showInventory()
	{

		//ejecutar descripciï¿½n (pesa poquito, etc. etc.)
		boolean execced = false;
		try
		{
			execced = this.execCode("showInventory",new Object[] {} );
		}
		catch ( bsh.EvalError te )
		{
			write( io.getColorCode("error") + "bsh.EvalError found at showInventory() , mobile " + this + io.getColorCode("reset") + "\n"  );
		}

		if ( !execced )
		{
			defaultShowInventory();
		}



	}



	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public void escribir ( String s )
	{
		write(s);
	}

	public void write ( String s )
	{
		//Escribir no hace nada para los bichos que no sean Informadores.
		//Los que sï¿½ lo sean ya lo overrridearï¿½n.
		//Lo ponemos como mï¿½todo de conveniencia, para poder llamarlo, por ejemplo,
		//cuando se mueva cualquier bicho; pero que sï¿½lo el jugador informe de ello.
		;
	}




	/**
	 * @deprecated Use {@link #writeAction(String)} instead
	 */
	public void escribirAccion ( String s )
	{
		writeAction(s);
	}


	public void writeWithTemplate ( String colorTemplate , String s )
	{
		write ( io.getColorCode(colorTemplate) + s + io.getColorCode("reset") );
	}


	public void writeImportant ( String s )
	{
		writeWithTemplate("important",s);
	}

	public void writeError ( String s )
	{
		writeWithTemplate("error",s);
	}

	public void writeStory ( String s )
	{
		writeWithTemplate("story",s);
	}


	public void writeAction ( String s )
	{
		write( io.getColorCode("action") + s + io.getColorCode("reset") );
	}
	/**
	 * @deprecated Use {@link #writeDescription(String)} instead
	 */
	public void escribirDescripcion ( String s )
	{
		writeDescription(s);
	}



	public void writeDescription ( String s )
	{
		write( io.getColorCode("description") + s + io.getColorCode("reset") );
	}
	/**
	 * @deprecated Use {@link #writeDenial(String)} instead
	 */
	public void escribirNegacion ( String s )
	{
		writeDenial(s);
	}



	public void writeInformation ( String s )
	{
		write( io.getColorCode("information") + s + io.getColorCode("reset") );
	}



	public void writeDenial ( String s )
	{
		write( io.getColorCode("denial") + s + io.getColorCode("reset") );
	}




	protected void escribirErrorNoEntiendo ( )
	{
		//write( io.getColorCode("denial") + "No entiendo...\n" + io.getColorCode("reset") );
		write( io.getColorCode("denial") + mundo.getMessages().getMessage("error.parsing") /*+ "\n"*/ + io.getColorCode("reset") );

	}



	public boolean makeRandomValidMove ( )
	{
		int nsal = habitacionActual.getRandomValidExitAsNumber();
		if ( nsal < 0 ) return false; //no hay salida vï¿½lida.
		if ( nsal < 10 )
		{
			//Debug.println ( "The movement eez true " + nsal );
			return go ( habitacionActual.getExit(true , nsal) );
		}
		else
		{
			//Debug.println ( "The movement eez false " + nsal );
			return go ( habitacionActual.getExit(false , 10-nsal) );	
		}
	}


	//ir a la habitaciï¿½n dada contigua a la actual
	public boolean goTo ( int roomid )
	{

		Debug.println("Called goTo with id: " + roomid);

		Path[] std = habitacionActual.getStandardExits();
		Path[] nonstd = habitacionActual.getNonStandardExits();

		for ( int i = 0 ; i < std.length ; i++ )
		{
			if ( std[i].getDestinationID() == roomid )
			{
				if ( go ( habitacionActual.getExit(true , i) ) )
					return true;
			}
		}
		for ( int i = 0 ; i < nonstd.length ; i++ )
		{
			if ( nonstd[i].getDestinationID() == roomid )
			{
				if ( go ( habitacionActual.getExit( false , i ) ) )
					return true;
			}
		}

		return false;
	}

	public boolean goTo ( Room r )
	{

		Debug.println("Called goTo with room: " + r);

		return goTo ( r.getID() );
	}

	//tempvars while go state is set
	protected Path movingState_Path;




	//ir por la salida dada. Legacy.
	public boolean go ( Path p ) //throws java.io.IOException
	{

		Debug.println("EXECCING GO " + p );

		if ( !p.isValid() )
		{

			//si no es un Informador, esto no hace nada.
			if ( p.isStandard() ) write( io.getColorCode("denial") + 
					mundo.getMessages().getMessage("go.noexit",new Object[]{this,p})  //"No parece haber salida en esa direcciï¿½n.\n" 
					+ io.getColorCode("reset") );
			else write( io.getColorCode("denial") + mundo.getMessages().getMessage("go.invalid",new Object[]{this,p}) + io.getColorCode("reset") );

			return false;	
		}	
		else
		{

			boolean endfound = false;

			Debug.println("EXECCING GO: ENDFOUND INIT");

			try
			{	
				endfound = habitacionActual.execCode("onWalkAway" , new Object[] {this,p} );
				Debug.println("EXECCING GO: ENDFOUND ASSIGN " + endfound);
			}
			catch ( bsh.TargetError bshte )
			{
				writeError( io.getColorCode("error") + "bsh.TargetError found onExitRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
				writeError(ExceptionPrinter.getExceptionReport(bshte));
			}

			if ( !endfound )
			{

				//sinonimo del anterior
				try
				{	
					endfound = habitacionActual.execCode("beforeExit" , new Object[] {this,p} );
					Debug.println("EXECCING GO: ENDFOUND ASSIGN " + endfound);
				}
				catch ( bsh.TargetError bshte )
				{
					writeError( io.getColorCode("error") + "bsh.TargetError found onExitRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
					writeError(ExceptionPrinter.getExceptionReport(bshte));
				}

			}

			//<antique>
			//int comparand=habitacionActual.getState();
			//comparand+=exitn*(2^8); //4-bit exit ID.
			//modify comparand more with character state (humor...)
			//el comparando es para la descripcion de la salida, asi que la verdad no haria muchafalta meterle la id.
			//</antique>
			//long comparand = habitacionActual.getExit(isStandard,exitn).getState(); //de momento

			//si no es un Informador, esto no hace nada.

			if ( !endfound )
			{

				//show path description
				//nay! passed to end of MOVING state, save for closed-path case.
				//now set path to describe.

				movingState_Path = p;

				//escribir( io.getColorCode("action")  + habitacionActual.getExit(isStandard,exitn).getDescription(this) + "\n" + io.getColorCode("reset") );

				//habitacionActual.getExit(isStandard,exitn)

				//use path if open, return false if closed.
				if ( p.isOpen() ) //puerta o salida abierta
				{
					exitname = habitacionActual.getExitName(p);

					// en chstate: habitacionActual.informAction(this,null,"$1 se va hacia " + habitacionActual.getExitName(isStandard,exitn) , null , null , false );
					p.go(this); //setea estado

					Debug.println("Path::go() called");

					return true;
				}	
				else //cerrado
				{

					//moving state will not be set, so we print movement description right now

					if ( movingState_Path != null )
						write( io.getColorCode("action")  + movingState_Path.getDescription(this) + "\n" + io.getColorCode("reset") );


					Debug.println("Closed path");

					return false; //se devuelve fracaso (el jugador no sale); la descripciï¿½n la darï¿½ la salida en funciï¿½n del estado cerrado.
				}

			}
			else
			{
				return true; //el cï¿½digo se encargï¿½ de todo.
			}


		}
	}





	public void changeState( World mundo )
	{

		Debug.println(this + " state " + getState() + "TUL" + this.getPropertyTimeLeft("state"));

		switch ( getState() )
		{
		case IDLE: //IDLE
			//if ( ! execCommand ( mundo ) )
			//	setNewState ( 1 /*IDLE*/, 1 /*penalizacion*/ );


			//Debug.println("Bicho's idle state.");

			//si tenemos enemigos, combatir como podamos

			if ( hasEnemies() )
			{

				Debug.println("Bicho has enemies.");

				//primero vemos si nos estan atacando para poder bloquear

				List atacantes = getAttackingEnemies();

				Debug.println("Bicho has " + atacantes.size() + " attacking enemies.");

				Debug.println("A " + this + " lo atacan " + atacantes);

				if ( atacantes.size() == 0 )
				{
					//no nos atacan.

					//primero vemos si algun enemigo huye para perseguirle.

					for ( int i = 0 ; i < getEnemies().size() ; i++ )
					{
						Mobile m = getEnemies().elementAt(i);	
						if ( habitacionActual.hasMobile ( m ) && m.getState() == MOVING ) //si el enemigo esta alcanzable y se estï¿½ yendo
						{
							int destRoomID = m.getTarget();

							//TODO: As of 2009-12-06, we don't pursue enemies by default (libraries will decide to do that).
							//if ( goTo ( destRoomID ) ) return;
							setNewState(IDLE,1);
							return;
						}				
					}

					//Ataquemos.

					boolean success = attackBestTarget();

					Debug.println("It's " + success + " to say that the bicho found targets.");

					if ( success ) return;

					else
					{

						boolean weHaveEnemy = false;
						for ( int i = 0 ; i < getEnemies().size() ; i++ )
						{
							//enemigo
							Mobile m = getEnemies().elementAt(i);
							if ( habitacionActual.hasMobile ( m ) )
							{
								weHaveEnemy = true;
								break;
							}
						}

						if ( !weHaveEnemy ) setNewState(IDLE,1); //we shrug. our enemy is not here.


						//seguramente la cagamos por no tener arma blandida

						Weapon w = bestNonWieldedWeapon( 5 /*inteligencia here*/ );

						Debug.println("Best non-wielded weapon set to " + w );

						if ( w != null )
						{

							boolean success2 = intentarBlandir ( w , true );

							Debug.println("Weapon wield success " + success2);

							if ( success2 )
								setNewState(IDLE,5); //wielding time: 5 units
							else
								setNewState(IDLE,1); //we shrug. we don't seem to be able to attack.

							return;

						}  

					}

				}
				else
				{
					//nos atacan. Bloqueemos o esquivemos.

					//de momento usamos un algoritmo random...
					//refinar esto.

					int aleat = getRandom().nextInt();

					boolean success = blockBestTarget();

					if ( !success ) //no era posible bloquear
						//aqui se puede crear una funcion similar dodgeBestTarget() y si no atacar,
						//y a estas funciones se le puede pasar parametro "risk" o "tolerance"
						dodge ( (Mobile)atacantes.get(0) );

				}

			}


			else //no enemies
				setNewState(1 /*IDLE*/,2); //fixes regression bug (enemies wouldn't attack you).


			break;
		case MOVING: //GO

			//escribir exit-description
			if ( movingState_Path != null )
				write( io.getColorCode("action")  + movingState_Path.getDescription(this) + "\n" + io.getColorCode("reset") );

			//has dejado la habitacion en que estabas (habitacionActual)
			//-> ejecutar eventos onExitRoom

			try
			{
				habitacionActual.execCode("event_exitroom","this: " + habitacionActual.getID() + "\n" + "player: " + getID() + "\n" + "dest: " + getTarget() );		
				habitacionActual.execCode("onExitRoom" , new Object[] {this} );
			}
			catch ( EVASemanticException exc ) 
			{
				write( io.getColorCode("denial") + "EVASemanticException found at event_exitroom , room number " + habitacionActual.getID() + io.getColorCode("reset") );
			}
			catch ( bsh.TargetError bshte )
			{
				writeError( io.getColorCode("error") + "bsh.TargetError found onExitRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
				writeError(ExceptionPrinter.getExceptionReport(bshte));
			}

			habitacionActual.reportAction(this,null,"$1 se va hacia " + exitname + ".\n" , null , null , false );	

			Debug.println("Trying room set.");	

			setRoom ( mundo.getRoom(getTarget()) );

			Debug.println("Trying invert inform.");

			habitacionActual.reportAction(this,null,"$1 llega desde " + Path.invert(exitname) + ".\n" , null , null , false );

			//Chequeo de cruces con enemigos.
			//Si hay un enemigo haciendo el camino opuesto al nuestro,
			//lo sorprendemos evitando su movimiento.
			Room destino = mundo.getRoom(getTarget());
			MobileList ml = destino.getMobiles();
			for ( int i = 0 ; i < ml.size() ; i++ )
			{
				Mobile current = (Mobile) ml.get(i);
				if ( this.hasEnemy(current) || current.hasEnemy(this) )
				{
					if ( current.getState() == Mobile.MOVING && mundo.getRoom(current.getTarget()) == this.getRoom() )
					{
						//this stuns current.
						current.setNewState(Mobile.SURPRISE_RECOVER,10);
						current.write("Ibas a dirigirte hacia " + current.exitname + "; pero te ves sorprendido por la apariciï¿½n de " + this.constructName2OneItem(current) + ", que te bloquea el paso.\n");
						this.write("Has sorprendido a " + current.constructName2OneItem(this) + ", que parecï¿½a querer dirigirse hacia " + current.exitname + ".\n");
					}
				}
			}

			//has entrado en nueva habitacion (habitacionActual)
			//-> ejecutar eventos onEnterRoom	

			try
			{
				habitacionActual.execCode("event_enterroom","this: " + habitacionActual.getID() + "\n" + "player: " + getID() + "\n" + "orig: " + habitacionAnterior );		
				habitacionActual.execCode("onEnterRoom" , new Object[] {this} );
			}
			catch ( EVASemanticException exc ) 
			{
				write( io.getColorCode("error") + "EVASemanticException found at event_enterroom , room number " + habitacionActual.getID() + io.getColorCode("reset") );
			}
			catch ( bsh.TargetError bshte )
			{
				write( io.getColorCode("error") + "bsh.TargetError found onEnterRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
				writeError(ExceptionPrinter.getExceptionReport(bshte));
			}

			//-> si hay Mobiles, pueden reaccionar tambiï¿½n a que entres (onEnterRoom de Mobile)
			MobileList mlist = habitacionActual.getMobiles(); //note that this includes itself!
			if ( mlist != null )
			{
				for ( int i = 0 ; i < mlist.size() ; i++ )
				{
					Mobile bichoActual = mlist.elementAt(i);

					try
					{
						//bichoActual.execCode("event_enterroom","this: " + habitacionActual.getID() + "\n" + "player: " + getID() + "\n" + "orig: " + habitacionAnterior );		
						boolean ejecutado = bichoActual.execCode("onEnterRoom" , new Object[] {this} );
						if ( ejecutado ) return;
					}
					catch ( bsh.TargetError bshte )
					{
						write( io.getColorCode("error") + "bsh.TargetError found onEnterRoom , mobile number " + bichoActual.getID() + ": " + bshte + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(bshte));
					}

				}
			}		



			//show_room ( mundo ); <- players only
			setNewState ( 1 /*IDLE*/, 0 );
			break;
		case ATTACKING:

			manageEndOfAttackState();

			break;

		case CASTING:

			Debug.println("Calling manageEndOfCastState()");

			manageEndOfCastState();

			break;

		case ATTACK_RECOVER:
			write("Mobile's attack-recover time is over.");
			setNewState ( IDLE , 0 );

			break;

		case DAMAGE_RECOVER:
			write("Mobile's damage-recover time is over.");
			setNewState ( IDLE , 0 );
			break;

		case BLOCK_RECOVER:
			write("Mobile's block-recover time is over.");
			setNewState ( IDLE , 0 );
			break;

		case DODGE_RECOVER:
			write("Mobile's dodge-recover time is over.");
			setNewState ( IDLE , 0 );
			break;

		case BLOCKING:
			write("");
			setNewState ( READY_TO_BLOCK , 0 );
			break;

		case READY_TO_BLOCK:
			//hold on that state
			setNewState ( READY_TO_BLOCK , 0 );
			break;

		case DODGING:
			write("");
			setNewState ( READY_TO_DODGE , 0 );
			break;

		case READY_TO_DODGE:
			//hold on that state
			setNewState ( READY_TO_DODGE , 0 );
			break;	

		case DYING:
			//die
			die();
			break;

		case DEAD:
			//hold on that state
			setNewState ( DEAD , 5 );
			break;		

		}
		//this is 4 the players
		//if ( ! execCommand ( mundo ) )
		//	setNewState ( 1 /*IDLE*/, 1 /*penalizacion*/ );
	}

	public void manageEndOfAttackState()
	{

		//llevar a cabo el ataque

		//buscar entre los bichos enemigos al que estamos atacando

		Mobile objetivo = null;

		for ( int i = 0 ; i < getEnemies().size() ; i++ )
		{
			if ( getEnemies().elementAt(i).getID() == getTarget() )
			{
				objetivo = getEnemies().elementAt(i);
				break;
			}
		}

		if ( objetivo == null )
		{
			Debug.println("Oops... El bicho atacado no estï¿½.");

			habitacionActual.reportAction(this, null, "$1 interrumpe su ataque ante la ausencia de su contrincante.\n", "$1 vacila ante tu ausencia.\n", "Te disponï¿½as a atacar; pero vacilas ante la ausencia de tu enemigo.\n", true);

			setNewState ( IDLE , 0 );


			return;
		}

		if ( objetivo.getState() != READY_TO_BLOCK /* && objetivo.getState() != BLOCKING */
				&& objetivo.getState() != READY_TO_DODGE /* && objetivo.getState() != DODGING */) 
		{

			//ataque no bloqueado

			if ( getAttackSuccessFromProbability(getCurrentWeapon()) )
			{

				//attack successful


				//solo hay un caso especial en que no se hace daï¿½o: choque de armas en el aire
				if ( objetivo.getState() == ATTACKING && objetivo.getPropertyTimeLeft("state") < 3 )
				{

					//choque de armas en el aire

					//informar de ello

					boolean ejec = false;
					try
					{
						//tiene prioridad el arma, luego el que la maneja.
						ejec = getCurrentWeapon().execCode ( "infoChoqueArmas" , new Object[] {this,objetivo} ); if ( !ejec )
							ejec = execCode ( "infoChoqueArmas" , new Object[] {objetivo} );
					}
					catch ( bsh.TargetError te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at infoChoqueArmas(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejec )
						habitacionActual.reportAction ( this , objetivo , "Las armas de $1 y $2 chocan en el aire...\n" , "Tu arma y la de $1 chocan en el aire...\n" , "Tu arma y la de $2 chocan en el aire...\n" , true );

					setNewState ( ATTACK_RECOVER , generateAttackRecoverTime(getCurrentWeapon()) );
					objetivo.setNewState ( ATTACK_RECOVER , objetivo.generateAttackRecoverTime(objetivo.getCurrentWeapon()) );

				}


				else
				{
					//se hace daï¿½o

					Item limbToHit = objetivo.getRandomLimbToHit(); //nullable

					int danyo = getCurrentWeapon().dealDamage ( this , objetivo , false , limbToHit );

					if ( objetivo.getState() == BLOCKING )
					{
						//informar de que no le ha dado tiempo a bloquear

						boolean ejec = false;
						try
						{
							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoNoTiempoBloquear" , new Object[] {this,objetivo} ); if ( !ejec )
								ejec = execCode ( "infoNoTiempoBloquear" , new Object[] {objetivo} );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoNoTiempoBloquear(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
							habitacionActual.reportAction ( this , objetivo , null , "No te da tiempo a bloquear el ataque de $1...\n" , "A $2 no le da tiempo a bloquear tu ataque...\n" , true );

					}

					if ( objetivo.getState() == DODGING )
					{

						//informar de que no le ha dado tiempo a esquivar
						boolean ejec = false;
						try
						{
							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoNoTiempoEsquivar" , new Object[] {this,objetivo} ); if ( !ejec )
								ejec = execCode ( "infoNoTiempoEsquivar" , new Object[] {objetivo} );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoNoTiempoEsquivar(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
							habitacionActual.reportAction ( this , objetivo , null , "No te da tiempo a esquivar el ataque de $1...\n" , "A $2 no le da tiempo a esquivar tu ataque...\n" , true );

					}

					if ( danyo > 0 )
					{

						//informar de que acierta
						boolean ejec = false;
						try
						{

							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoAcierto" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
								ejec = execCode ( "infoAcierto" , new Object[] {objetivo,new Integer(danyo)} ); if ( !ejec ) 
									ejec = getCurrentWeapon().execCode ( "infoOnHit" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
										ejec = execCode ( "infoOnHit" , new Object[] {objetivo,new Integer(danyo)} );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							if ( numeric_damage )
							{
								habitacionActual.reportAction ( this , objetivo ,
										mundo.getMessages().getMessage("someone.hits.someone.numeric","$weapon",getCurrentWeapon().constructName2OneItem(),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.you.numeric","$weapon",getCurrentWeapon().constructName2OneItem(),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.enemy.numeric","$weapon",getCurrentWeapon().constructName2OneItem(),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										//"$1 acierta a $2 con " + getCurrentWeapon().constructName2OneItem() + " infligiï¿½ndole " + danyo + " puntos de daï¿½o...\n" ,
										//"$1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligiï¿½ndote " + danyo + " puntos de daï¿½o...\n" ,
										//"Aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + " infligiï¿½ndole " + danyo + " puntos de daï¿½o...\n" ,
										true );
							}
							else
							{
								habitacionActual.reportAction ( this , objetivo ,
										mundo.getMessages().getMessage("someone.hits.someone","$weapon",getCurrentWeapon().constructName2OneItem(),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.you","$weapon",getCurrentWeapon().constructName2OneItem(),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.enemy","$weapon",getCurrentWeapon().constructName2OneItem(),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										//"$1 acierta a $2 con " + getCurrentWeapon().constructName2OneItem() + " infligiï¿½ndole " + objetivo.estimateDamage(danyo) + "...\n" ,
										//"$1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligiï¿½ndote " + objetivo.estimateDamage(danyo) + "...\n" ,
										//"Aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + " infligiï¿½ndole " + objetivo.estimateDamage(danyo) + "...\n" ,
										true );

								habitacionActual.reportActionAuto ( objetivo , null , null , "$1 " + objetivo.estimateStatus() + ".\n" , true );

							}
						}


					}
					else
					{

						//informar de que acierta
						boolean ejec = false;
						try
						{
							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoAcierto" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
								ejec = execCode ( "infoAcierto" , new Object[] {objetivo,new Integer(danyo)} ); if (!ejec)
									ejec = getCurrentWeapon().execCode ( "infoOnHit" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
										ejec = execCode ( "infoOnHit" , new Object[] {objetivo,new Integer(danyo)} );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
							habitacionActual.reportAction ( this , objetivo ,
									mundo.getMessages().getMessage("someone.hits.someone.nodamage","$weapon",getCurrentWeapon().constructName2OneItem(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
									mundo.getMessages().getMessage("enemy.hits.you.nodamage","$weapon",getCurrentWeapon().constructName2OneItem(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
									mundo.getMessages().getMessage("you.hit.enemy.nodamage","$weapon",getCurrentWeapon().constructName2OneItem(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
									//"$1 acierta a $2 con " + getCurrentWeapon().constructName2OneItem() + " pero no le hace daï¿½o...\n" ,
									//"$1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " pero no te hace daï¿½o...\n" ,
									//"Aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + " pero no le haces daï¿½o...\n" ,
									true );

					}

					setNewState ( ATTACK_RECOVER , generateAttackRecoverTime(getCurrentWeapon()) );

					if ( objetivo.getState() != DYING )
					{
						objetivo.interrupt ( "el golpe" );
						objetivo.setNewState ( DAMAGE_RECOVER , objetivo.generateRecoverFromUnblockedHitTime() );
					}

				}

			}
			else
			{

				//attack unsuccessful

				//informar de que falla
				boolean ejec = false;
				try
				{
					//tiene prioridad el arma, luego el que la maneja.
					ejec = getCurrentWeapon().execCode ( "infoFallo" , new Object[] {this,objetivo} ); if ( !ejec )
						ejec = execCode ( "infoFallo" , new Object[] {objetivo} );
				}
				catch ( bsh.TargetError te )
				{
					write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
					writeError(ExceptionPrinter.getExceptionReport(te));
				}
				if ( !ejec )
				{
					habitacionActual.reportAction ( this , objetivo , 
							mundo.getMessages().getMessage("someone.misses.someone","$weapon",getCurrentWeapon().constructName2OneItem(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
							mundo.getMessages().getMessage("enemy.misses.you","$weapon",getCurrentWeapon().constructName2OneItem(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
							mundo.getMessages().getMessage("you.miss.enemy","$weapon",getCurrentWeapon().constructName2OneItem(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
							//"El ataque de $1 falla a $2.\n" , 
							//"El ataque de $1 te falla.\n" , 
							//"Tu ataque falla a $2.\n" , 
							true );
				}

				setNewState ( ATTACK_RECOVER , generateAttackRecoverTime(getCurrentWeapon()) );

				if ( objetivo.getState() == BLOCKING )
				{
					//darle la iniciativa

					//o bien cambiar esto por, en cambio de estado de blocking
					//a ready to block de los mobiles, si ya no les estï¿½ atacando
					//el objetivo, pasar a idle (mï¿½s realista)

					objetivo.setNewState ( IDLE , 1 ); //objetivo has a chance to attack right now

					//informar de que tienes la iniciativa
					ejec = false;
					try
					{
						//tiene prioridad el arma, luego el que la maneja.
						ejec = getCurrentWeapon().execCode ( "infoIniciativaTrasBloquear" , new Object[] {this,objetivo} ); if ( !ejec )
							ejec = execCode ( "infoIniciativaTrasBloquear" , new Object[] {objetivo} );
					}
					catch ( bsh.TargetError te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejec )
					{
						habitacionActual.reportAction ( this , objetivo , null , "Tienes la iniciativa...\n" , "$2 interrumpe su intento de bloquear...\n" , true );
					}

				}

				if ( objetivo.getState() == DODGING )
				{

					//idem que el anterior

					objetivo.setNewState ( IDLE , 1 ); //chance to attack for objetivo

					//informar de que tienes la iniciativa
					ejec = false;
					try
					{
						//tiene prioridad el arma, luego el que la maneja.
						ejec = getCurrentWeapon().execCode ( "infoIniciativaTrasEsquivar" , new Object[] {this,objetivo} ); if ( !ejec )
							ejec = execCode ( "infoIniciativaTrasEsquivar" , new Object[] {objetivo} );
					}
					catch ( bsh.TargetError te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejec )
					{
						habitacionActual.reportAction ( this , objetivo , null , "Tienes la iniciativa...\n" , "$2 interrumpe su intento de esquivar...\n" , true );
					}

				}

			} //end attack unsucc

			getCurrentWeapon().incrementAttackUsage(this); //weapon skill increases					

		} //end not blocking, not dodging
		else if ( objetivo.getState() == READY_TO_BLOCK )
		{
			//ataque bloqueado

			if ( getAttackSuccessFromProbability(getCurrentWeapon()) )
			{

				//attack successful

				if ( objetivo.getBlockSuccessFromProbability( objetivo.getCurrentWeapon() ) )
				{

					//attack successful and block successful

					Item limbHit = objetivo.getRandomLimbToHit();

					int danyo = getCurrentWeapon().dealDamageDefended ( this , objetivo , false , limbHit );

					//informar de que bloqueas con ï¿½xito
					boolean ejec = false;
					try
					{
						//tiene prioridad el arma, luego el que la maneja.
						ejec = getCurrentWeapon().execCode ( "infoBloqueo" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
							ejec = execCode ( "infoBloqueo" , new Object[] {objetivo,new Integer(danyo)} );
					}
					catch ( bsh.TargetError te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at infoBloqueo(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejec )
					{
						if ( danyo > 0 )
						{
							if ( numeric_damage )
							{
								habitacionActual.reportAction ( this , objetivo ,
										"$2 se defiende de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem() + " recibiendo " + danyo + " puntos de daï¿½o...\n" ,
										"Te defiendes de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem(objetivo) + " recibiendo " + danyo + " puntos de daï¿½o...\n" ,
										"$2 se defiende con " + objetivo.getCurrentWeapon().constructName2OneItem(this) + " recibiendo " + danyo + " puntos de daï¿½o...\n" ,
										true );
							}
							else
							{
								habitacionActual.reportAction ( this , objetivo ,
										"$2 se defiende de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem() + " recibiendo " + objetivo.estimateDamage(danyo) + "...\n" ,
										"Te defiendes de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem(objetivo) + " recibiendo " + objetivo.estimateDamage(danyo) + "...\n" ,
										"$2 se defiende con " + objetivo.getCurrentWeapon().constructName2OneItem(this) + " recibiendo " + objetivo.estimateDamage(danyo) + "...\n" ,
										true );

								habitacionActual.reportActionAuto ( objetivo , null , null , "$1 " + objetivo.estimateStatus() + ".\n" , true );

							}
						}
						else
						{
							habitacionActual.reportAction ( this , objetivo ,
									"$2 se defiende de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem() + ", desviando el ataque...\n" ,
									"Te defiendes de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem(objetivo) + ", desviando el ataque...\n" ,
									"$2 se defiende con " + objetivo.getCurrentWeapon().constructName2OneItem(this) + ", desviando el ataque...\n" ,
									true );
						}
					}

					setNewState ( ATTACK_RECOVER , generateAttackRecoverTime ( getCurrentWeapon() ) );

					objetivo.setNewState ( BLOCK_RECOVER , objetivo.generateBlockRecoverTime ( objetivo.getCurrentWeapon() ) );

				}
				else
				{

					//attack successful but block unsuccessful.
					//damage is deal just as at an unblocked attack

					Item limbHit = objetivo.getRandomLimbToHit();

					int danyo = getCurrentWeapon().dealDamage ( this , objetivo , false , limbHit );

					if ( danyo > 0 )
					{

						//informar de que bloqueas sin ï¿½xito recibiendo daï¿½o
						boolean ejec = false;
						try
						{
							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoBloqueoFallido" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
								ejec = execCode ( "infoBloqueoFallido" , new Object[] {objetivo,new Integer(danyo)} );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoBloqueo(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							if ( numeric_damage )
							{
								habitacionActual.reportAction ( this , objetivo ,
										"$2 no consigue parar el ataque de $1, que le acierta con " + getCurrentWeapon().constructName2OneItem() + " infligiï¿½ndole " + danyo + " puntos de daï¿½o...\n" ,
										"No consigues parar el ataque de $1, que te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligiï¿½ndote " + danyo + " puntos de daï¿½o...\n" ,
										"$2 no consigue parar tu ataque, le aciertas con " + getCurrentWeapon().constructName2OneItem(this) + " infligiï¿½ndole " + danyo + " puntos de daï¿½o...\n" ,
										true );
							}
							else
							{
								habitacionActual.reportAction ( this , objetivo ,
										"$2 no consigue parar el ataque de $1, que le acierta con " + getCurrentWeapon().constructName2OneItem() + " infligiï¿½ndole " + objetivo.estimateDamage(danyo) + "...\n" ,
										"No consigues parar el ataque de $1, que te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligiï¿½ndote " + objetivo.estimateDamage(danyo) + "...\n" ,
										"$2 no consigue parar tu ataque, le aciertas con " + getCurrentWeapon().constructName2OneItem(this) + " infligiï¿½ndole " + objetivo.estimateDamage(danyo) + "...\n" ,
										true );

								habitacionActual.reportActionAuto ( objetivo , null , null , "$1 " + objetivo.estimateStatus() + ".\n" , true );

							}		
						}		

					}
					else
					{

						//informar de que bloqueas sin ï¿½xito pero no recibes daï¿½o
						boolean ejec = false;
						try
						{
							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoBloqueoFallido" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
								ejec = execCode ( "infoBloqueoFallido" , new Object[] {objetivo,new Integer(danyo)} );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoBloqueo(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							habitacionActual.reportAction ( this , objetivo ,
									"$2 no consigue parar el ataque de $1, que le acierta con " + getCurrentWeapon().constructName2OneItem() + " pero no le hace daï¿½o...\n" ,
									"No consigues parar el ataque de $1, que te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " pero no te hace daï¿½o...\n" ,
									"$2 no consigue parar tu ataque, le aciertas con " + getCurrentWeapon().constructName2OneItem(this) + " pero no le haces daï¿½o...\n" ,
									true );
						}

					}

					setNewState ( ATTACK_RECOVER , generateAttackRecoverTime(getCurrentWeapon()) );

					if ( objetivo.getState() != DYING )
					{
						objetivo.interrupt ( "el golpe" );
						objetivo.setNewState ( DAMAGE_RECOVER , objetivo.generateRecoverFromUnblockedHitTime() );
					}

				}

			}

			else
			{
				//estaban bloqueando y fallamos el ataque

				//informar de que fallas
				boolean ejec = false;
				try
				{
					//tiene prioridad el arma, luego el que la maneja.
					ejec = getCurrentWeapon().execCode ( "infoFallo" , new Object[] {this,objetivo} ); if ( !ejec )
						ejec = execCode ( "infoFallo" , new Object[] {objetivo} );
				}
				catch ( bsh.TargetError te )
				{
					write(io.getColorCode("error") + "bsh.TargetError found at infoFallo(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
					writeError(ExceptionPrinter.getExceptionReport(te));
				}
				if ( !ejec )
				{
					habitacionActual.reportAction ( this , objetivo , "El ataque de $1 falla a $2.\n" , "El ataque de $1 te falla.\n" , "Tu ataque falla a $2.\n" , true );
				}

				setNewState ( ATTACK_RECOVER , generateAttackRecoverTime(getCurrentWeapon()) );

				//darle la iniciativa

				//o bien cambiar esto por, en cambio de estado de
				//ready to block de los mobiles, si ya no les estï¿½ atacando
				//el objetivo, pasar a idle (mï¿½s realista)

				objetivo.setNewState ( IDLE , 1 ); //objetivo has a chance to attack right now

			}


		}
		else if ( objetivo.getState() == READY_TO_DODGE )
		{
			//ataque esquivado, posiblemente

			if ( getAttackSuccessFromProbability(getCurrentWeapon()) )
			{

				//attack successful

				if ( objetivo.getDodgeSuccessFromProbability() )
				{

					//attack successful and dodge successful

					//int danyo = getCurrentWeapon().dealDamageDefended ( this , objetivo , false );

					//informar de esquivada
					boolean ejec = false;
					try
					{
						//tiene prioridad el arma, luego el que la maneja.
						ejec = getCurrentWeapon().execCode ( "infoEsquivada" , new Object[] {this,objetivo} ); if ( !ejec )
							ejec = execCode ( "infoEsquivada" , new Object[] {objetivo} );
					}
					catch ( bsh.TargetError te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at infoEsquivada(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejec )
					{
						habitacionActual.reportAction ( this , objetivo ,
								"$2 esquiva hï¿½bilmente el ataque de $1.\n",
								"Esquivas el ataque de $1.\n" ,
								"$2 esquiva tu ataque.\n" ,
								true );
					}		

					setNewState ( ATTACK_RECOVER , generateAttackRecoverTime ( getCurrentWeapon() ) );

					objetivo.setNewState ( DODGE_RECOVER , objetivo.generateDodgeRecoverTime (  ) );

				}
				else
				{

					//attack successful but dodge unsuccessful.
					//damage is deal just as at an unblocked attack

					Item limbHit = objetivo.getRandomLimbToHit();

					int danyo = getCurrentWeapon().dealDamage ( this , objetivo , false , limbHit );

					if ( danyo > 0 )
					{


						//informar de esquivada fallida
						boolean ejec = false;
						try
						{
							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoEsquivadaFallida" , new Object[] {this,objetivo} ); if ( !ejec )
								ejec = execCode ( "infoEsquivadaFallida" , new Object[] {objetivo} );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoEsquivadaFallida(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							if ( numeric_damage )
							{
								habitacionActual.reportAction ( this , objetivo ,
										"$2 intenta esquivar el golpe de $1; pero no lo consigue, $1 le acierta con " + getCurrentWeapon().constructName2OneItem() + " infligiï¿½ndole " + danyo + " puntos de daï¿½o...\n" ,
										"Tu intento de esquivar falla, y $1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligiï¿½ndote " + danyo + " puntos de daï¿½o...\n" ,
										"A pesar de su intento de esquivar el ataque, aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + ", infligiï¿½ndole " + danyo + " puntos de daï¿½o...\n" ,
										true );
							}		
							else
							{
								habitacionActual.reportAction ( this , objetivo ,
										"$2 intenta esquivar el golpe de $1; pero no lo consigue, $1 le acierta con " + getCurrentWeapon().constructName2OneItem() + " infligiï¿½ndole " + objetivo.estimateDamage(danyo) + ".\n" ,
										"Tu intento de esquivar falla, y $1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligiï¿½ndote " + objetivo.estimateDamage(danyo) + ".\n"  ,
										"A pesar de su intento de esquivar el ataque, aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + ", infligiï¿½ndole " + objetivo.estimateDamage(danyo) + ".\n"  ,
										true );

								habitacionActual.reportActionAuto ( objetivo , null , null , "$1 " + objetivo.estimateStatus() + ".\n" , true );

							}	
						}

					}
					else
					{

						//informar de esquivada fallida
						boolean ejec = false;
						try
						{
							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoEsquivadaFallida" , new Object[] {this,objetivo} ); if ( !ejec )
								ejec = execCode ( "infoEsquivadaFallida" , new Object[] {objetivo} );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoEsquivadaFallida(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							habitacionActual.reportAction ( this , objetivo ,
									"$2 intenta esquivar el golpe de $1; pero no lo consigue, $1 le acierta con " + getCurrentWeapon().constructName2OneItem() + " pero no le hace daï¿½o.\n" ,
									"Tu intento de esquivar falla, y $1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " pero no te hace daï¿½o.\n" ,
									"A pesar de su intento de esquivar el ataque, aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + "; pero no le haces daï¿½o.\n" ,
									true );
						}		
					}

					setNewState ( ATTACK_RECOVER , generateAttackRecoverTime(getCurrentWeapon()) );

					if ( objetivo.getState() != DYING )
					{
						objetivo.interrupt ( "el golpe" );
						objetivo.setNewState ( DAMAGE_RECOVER , objetivo.generateRecoverFromUnblockedHitTime() );
					}

				}

			} //end if get attack success...

			else
			{

				//attack unsuccessful
				//defender has iniciativa


				//attack unsuccessful

				habitacionActual.reportAction ( this , objetivo , "El ataque de $1 falla a $2.\n" , "El ataque de $1 te falla. Tienes la iniciativa...\n" , "Tu ataque falla a $2. Te desequilibras...\n" , true );

				setNewState ( ATTACK_RECOVER , generateAttackRecoverTime(getCurrentWeapon()) );

				objetivo.setNewState ( IDLE , 1 ); //objetivo has a chance to attack right now

			} //end if not attack successful

		} //end if state was ready to dodge

	}


	public void addEnemy ( Mobile nuevo )
	{
		if ( combatRefs == null ) combatRefs = getEnemies();
		combatRefs.addMobile(nuevo);
		this.setRelationshipProperty(nuevo,"enemy",true);

		//ser enemigo es relaciï¿½n simï¿½trica
		if ( !nuevo.hasEnemy ( this ) )
			nuevo.addEnemy(this);
	}

	public boolean removeEnemy ( Mobile viejo )
	{
		this.setRelationshipProperty(viejo,"enemy",false);
		if ( combatRefs == null ) return false;
		else return combatRefs.removeElement(viejo);
	}

	public void addItem ( Item nuevo ) throws WeightLimitExceededException, VolumeLimitExceededException
	{
		if ( inventory == null ) inventory = new Inventory(50,50); //limite de peso y volumen que ponemos por defecto
		inventory.addItem(nuevo);
		nuevo.addMobileReference(this);
	}

	public void addSpell ( Spell nuevo )
	{
		if ( spellRefs == null ) spellRefs = new SpellList(); 
		spellRefs.addSpell(nuevo);
	}

	public boolean removeItem ( Item viejo )
	{
		if ( inventory == null ) return false;
		else
		{
			viejo.removeMobileReference(this);
			return inventory.removeItem(viejo);
		}
	}

	public boolean hasItem ( Item it )
	{
		return (inventory != null && inventory.contains(it) );
	}

	public boolean hasSpell ( Spell s )
	{
		return (spellRefs != null && spellRefs.contains(s) );
	}

	public boolean hasEnemy ( Mobile m )
	{
		//return (combatRefs != null && combatRefs.contains(m) );
		return getEnemies().contains(m);
	}

	public boolean hasEnemies ( )
	{
		//return ( combatRefs != null && combatRefs.size() > 0 );
		return getEnemies().size() > 0;
	}

	public Inventory getInventory ( )
	{
		if ( inventory == null ) inventory = new Inventory(50,50);
		return inventory;
	}

	public SpellList getSpells ( )
	{
		if ( spellRefs == null ) spellRefs = new SpellList();
		return spellRefs;
	}


	public Wearable getWornItem ( Item limb )
	{
		List l = limb.getRelatedEntitiesByValue ( "wears" , true );
		if ( l == null || l.size() < 1 )
			return null;
		else
			return (Wearable) l.get(0);	
	}

	public Inventory getWornItems ()
	{
		Inventory limbs = getFlattenedPartsInventory();
		Inventory result = new Inventory(10000,10000);
		if ( limbs == null ) return result;
		else
		{
			for ( int i = 0 ; i < limbs.size() ; i++ )
			{
				Item limb = limbs.elementAt(i);
				Item worn = getWornItem(limb);
				if ( worn != null ) 
				{
					try
					{
						result.addItem(worn);
					}
					catch ( WeightLimitExceededException wle )
					{
						wle.printStackTrace();
					}
					catch ( VolumeLimitExceededException  vle )
					{
						vle.printStackTrace();
					}
				}
			}
		}
		wornItems = result; //kinda cache
		return result;
	}

	public Weapon getWieldedItem ( Item limb )
	{
		List l = limb.getRelatedEntitiesByValue ( "wields" , true );
		if ( l == null || l.size() < 1 )
			return null;
		else
			return (Weapon) l.get(0);	
	}

	public Inventory getWieldedWeapons ()
	{
		Inventory limbs = getFlattenedPartsInventory();
		Inventory result = new Inventory(10000,10000);
		if ( limbs == null ) return result;
		else
		{
			for ( int i = 0 ; i < limbs.size() ; i++ )
			{
				Item limb = limbs.elementAt(i);
				Item worn = getWieldedItem(limb);
				if ( worn != null ) 
				{
					try
					{
						if ( !result.contains(worn) ) //two handed
							result.addItem(worn);
					}
					catch ( WeightLimitExceededException wle )
					{
						wle.printStackTrace();
					}
					catch ( VolumeLimitExceededException  vle )
					{
						vle.printStackTrace();
					}
				}
			}
		}
		wieldedWeapons = result; //kinda cache
		return result;
	}

	/*
	Intenta hacer a este bicho damage daï¿½os del tipo damtype: la cantidad de daï¿½o que haga
	en realidad se verï¿½ reducida por las resistencias, armaduras, etc. y es la cantidad que
	se devuelve. [el bloqueo, sin embargo, se resuelve antes]
	 */
	public int tryToDealDamage ( int damtype , int damage , boolean simulated /* , Item limb */ )
	{

		//resistencias, armaduras... here!



		if ( !simulated )
			decreaseHP ( damage );	

		return damage;

	}

	//limb is randomised
	public int tryToDealDamage ( List damList , boolean simulated )
	{
		Item limbToHit = getRandomLimbToHit(); 	
		return tryToDealDamage ( damList , simulated , limbToHit );
	}

	//despuï¿½s de bloquear; pero antes de armaduras (ï¿½stas se resuelven aquï¿½)
	public int tryToDealDamage ( List damList , boolean simulated , Item limb )
	{

		Wearable armadura = null;
		if ( limb != null ) armadura = getWornItem(limb); //can be null	
		List armorDamList = null;
		if ( armadura != null ) armorDamList = armadura.getDamageList(this);

		if ( armorDamList == null ) //no hay armadura
		{

			int damDealt = 0;

			for ( int i = 0 ; i < damList.size() ; i++ )
			{
				int thisDam = ((Integer)damList.get(i)).intValue();
				if ( !simulated )
					decreaseHP ( thisDam );
				damDealt+=thisDam;
			}

			return damDealt;

		}
		else
		{

			int damDealt = 0;

			for ( int i = 0 ; i < damList.size() ; i++ )
			{
				int thisDam;
				if ( i < armorDamList.size() )
					thisDam = ((Integer)damList.get(i)).intValue() - ((Integer)armorDamList.get(i)).intValue();
				else
					thisDam = ((Integer)damList.get(i)).intValue();
				if ( thisDam <= 0 )
				{
					thisDam = 0;
				}
				if ( !simulated && thisDam > 0 )
					decreaseHP ( thisDam );
				damDealt+=thisDam;
			}

			if ( damDealt == 0 )
			{

				//informar de que armadura absorbiï¿½ impacto

				habitacionActual.reportAction ( this , null , armadura.constructName2OneItem() + " de $1 absorbe totalmente el impacto.\n" , null, "Tu armadura absorbe totalmente el impacto.\n"  , true );


			}

			return damDealt;


		}


	}






	public int decreaseHP ( int amount )
	{
		hp -= amount;
		if ( hp <= 0 ) prepareToDie();
		return amount;
	}

	public void setHP ( int amt )
	{
		hp = amt;
	}
	public void setMP ( int amt )
	{
		mp = amt;
	}

	public void prepareToDie ( )
	{
		setNewState ( DYING , 1 );
	}


	public void die()
	{

		Debug.println( this + " estï¿½ mï¿½s que muerto." );


		boolean ejecutado = false;
		try
		{
			ejecutado = execCode( "beforeDie" , new Object[] {} );
		}
		catch (bsh.TargetError bshte)
		{
			write("bsh.TargetError found at die routine" );
			writeError(ExceptionPrinter.getExceptionReport(bshte));
		}
		if ( ejecutado ) return;		



		habitacionActual.reportAction ( this , null , mundo.getMessages().getMessage("someone.dies",new Object[]{this}) , null , mundo.getMessages().getMessage("you.die",new Object[]{this}) , true );

		Item cadaver = Item.initCorpse ( this );
		mundo.addItemAssigningID ( cadaver );
		try
		{
			habitacionActual.addItem ( cadaver );
			habitacionActual.removeMob ( this );
			if ( mundo.getLimbo() != null )
				mundo.getLimbo().addMob(this);
			setNewState ( DEAD , 1 );
		}
		catch ( WeightLimitExceededException wlee )
		{
			Debug.println("Can't add corpse to room: " + wlee);
		}
		catch ( VolumeLimitExceededException vlee )
		{
			Debug.println("Can't add corpse to room: " + vlee);
		}


		try
		{
			ejecutado = execCode( "afterDie" , new Object[] { cadaver } );
		}
		catch (bsh.TargetError bshte)
		{
			write("bsh.TargetError found at afterDie routine" );
			writeError(ExceptionPrinter.getExceptionReport(bshte));
		}


	}

	public void setID ( int newid )
	{
		if ( newid < 20000000 )
			idnumber = newid + 20000000;
		else
			idnumber = newid;
	}

	//o null si no llevamos armas encima
	public Weapon bestNonWieldedWeapon ( int numberOfSimulations )
	{
		if ( inventory == null ) return null;

		int max = -1; //maximo daï¿½o encontrado

		Weapon resultado = null;

		for ( int i = 0 ; i < inventory.size() ; i++ )
		{
			Weapon w;
			try
			{
				w = (Weapon) inventory.elementAt(i);
			}
			catch ( ClassCastException cce )
			{
				continue;
			}

			wieldedWeapons = getWieldedWeapons(); //shades attribute with the same name
			if ( wieldedWeapons != null && wieldedWeapons.contains(w) ) continue;

			//atacarme a mi mismo para ver el dano
			int acum = 0;
			for ( int k = 0 ; k < numberOfSimulations ; k++ )
			{
				acum += w.dealDamage ( this , this , true );
			}
			int media = acum / numberOfSimulations;

			if ( media > max )
				resultado = w;

		}

		return resultado;

	}

	//retval[0] mejor enemigo que bloquear, retval[1] la mejor arma para ello
	//null: nada que bloquear
	//SI EL BLOQUEO NO CONVENCE POR EL TIEMPO NECESARIO, DEVUELVE NULL
	public Object[] bestBlockTargetAndWeapon ( int numberOfSimulations )
	{

		List atacantes = getAttackingEnemies();

		Inventory usableWeapons = getUsableWeapons();

		if ( getEnemies() == null || usableWeapons == null ) return null;
		if ( atacantes.size() < 1 ) return null;

		Object[] resultado = new Object[2];

		Debug.println("Best block function result initted.");

		double max = -1; //maximo valor funcion de evaluacion hasta el momento

		//de momento, IA muy simple. Mas tarde, tener en cuenta para cada bicho y arma de bloqueo:
		//tiempo de bloqueo, probabilidad de que no la caguemos increiblemente
		//danyo que prevenimos, i.e. danyo sin bloqueo vs. danyo con el
		for ( int i = 0 ; i < atacantes.size() ; i++ )
		{
			//enemigo
			Mobile m =  (Mobile ) atacantes.get(i);
			if ( habitacionActual.hasMobile ( m ) ) //bicho aqui
			{

				Debug.println("Checking a mob.");

				for ( int j = 0 ; j < usableWeapons.size() ; j++ )
				{
					Weapon w = (Weapon) usableWeapons.elementAt(j);

					Debug.println("Checking a weapon.");

					if ( w != null )
					{
						//tenemos una combinacion: arma w , enemigo m.
						//aqui se calcularian medias, etc.
						//de momento no nos lo curramos mucho

						Debug.println("It's not null, this weapon.");

						double deftime = w.getTypicalDefenseTime(m);
						int attime = (int) m.getPropertyTimeLeft("state");

						if ( deftime < attime )
						{
							Debug.println("ME CONVENCE.");
							double evalf = 100 - deftime;
							if ( evalf > max )
							{
								max = evalf;
								resultado[0] = m;
								resultado[1] = w;
							} //end if combination maximizes function
						}
						else
						{
							Debug.println("NO ME CONVENCE.");
						}


					} //end if weapon not null
					else
					{
						Debug.println("It was null, the weapon.");
					}
				} //end foreach weapon
			} //end if bicho is here

		} //end for each bicho atacante



		if ( resultado[0] == null ) //nullified, no se le ha llegado a asignar nada
			//porque todas las wielded weapons eran null
			//o porque ninguna nos convencia en cuanto a tiempo de defensa
			return null;

		return resultado;

	}

	//retval[0] es el mejor objetivo de los enemigos para atacar, retval[1] la mejor arma para atacarlo.
	//o bien devuelve null si no hay nada que atacar.
	public Object[] bestAttackTargetAndWeapon ( int numberOfSimulations )
	{
		//hace las archiconocidas y famosï¿½simas simulaciones que caracterizan la IA del AGE.

		Inventory usableWeapons = getUsableWeapons();

		Debug.println("Usable weapons for " + getTitle() + ": " + usableWeapons );

		if ( getEnemies() == null || usableWeapons == null ) return null;

		int max = -1; //maximo daï¿½o encontrado hasta el momento
		Object[] resultado = new Object[2];

		for ( int i = 0 ; i < getEnemies().size() ; i++ )
		{
			//enemigo
			Mobile m = getEnemies().elementAt(i);
			if ( habitacionActual.hasMobile ( m ) ) //si el enemigo esta alcanzable
			{

				for ( int j = 0 ; j < usableWeapons.size() ; j++ )
				{
					Weapon w = (Weapon) usableWeapons.elementAt(j);

					if ( w != null )
					{

						//tenemos una combinacion: arma w , enemigo m.

						//calcular media del daï¿½o producido en n simulaciones
						int acum = 0;
						for ( int k = 0 ; k < numberOfSimulations ; k++ )
						{
							acum += w.dealDamage ( this , m , true );
						}
						int media = acum/numberOfSimulations;

						if ( media > max )
						{
							max = media;
							resultado[0] = m;
							resultado[1] = w;
						}

					}

				}
			}
		}

		if ( resultado[0] == null ) //nullified, no se le ha llegado a asignar nada
			//porque todas las wielded weapons eran null, seguramente
			return null;

		return resultado;

	}

	//returns true if successful
	public boolean attackBestTarget()
	{
		Object[] obj = bestAttackTargetAndWeapon ( 5 /*intelligence here*/ );
		Debug.println("Best attack target and weapon: " + obj);
		//Debug.println("That is: " + obj[0] + " and " + obj[1]); 
		if ( obj == null )
			return false;
		else
		{
			Mobile target = (Mobile)obj[0];
			Weapon w = (Weapon)obj[1];
			attack ( target , w ); 
			return true;
		}				
	}

	//returns true if actually blocked
	public boolean blockBestTarget()
	{
		Object[] obj = bestBlockTargetAndWeapon ( 5 /*intelligence here*/ );
		Debug.println("Best block target and weapon: " + obj);
		if ( obj == null )
			return false;
		else
		{
			Mobile target = (Mobile)obj[0];
			Weapon w = (Weapon)obj[1];

			block ( target , w );
			return true;
		}
	}

	public void attack ( Mobile target , Weapon w )
	{
		setNewTarget ( target.getID() );
		setCurrentWeapon(w);
		setNewState ( ATTACKING , generateAttackTime( w ) );

		//informar de ataque
		boolean ejec = false;
		try
		{
			//tiene prioridad el arma, luego el que la maneja.
			ejec = getCurrentWeapon().execCode ( "infoIntentoAtaque" , new Object[] {this,target} ); if ( !ejec )
				ejec = execCode ( "infoIntentoAtaque" , new Object[] {target} ); if (!ejec)
					ejec = getCurrentWeapon().execCode ( "infoOnAttack" , new Object[] {this,target} ); if ( !ejec )
						ejec = execCode ( "infoOnAttack" , new Object[] {target} );
		}
		catch ( bsh.TargetError te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at infoIntentoAtaque(), target id was " + target.getID() + ", error was " + te + io.getColorCode("reset") );					
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
		if ( !ejec )
		{
			habitacionActual.reportAction ( this , target , 
					"$1 ataca a $2 con " + w.constructName2OneItem() + ".\n" ,
					"$1 te ataca con " + w.constructName2OneItem(target) + ".\n",
					"Atacas a $2 con " + w.constructName2OneItem(this) + ".\n",
					true );
		}	 

	}

	public void block ( Mobile target , Weapon w )
	{

		setNewTarget ( target.getID() );
		setCurrentWeapon(w);
		setNewState ( BLOCKING , generateBlockTime( w ) );

		//informar de bloqueo
		boolean ejec = false;
		try
		{
			//tiene prioridad el arma, luego el que la maneja.
			ejec = getCurrentWeapon().execCode ( "infoIntentoBloqueo" , new Object[] {this,target} ); if ( !ejec )
				ejec = execCode ( "infoIntentoBloqueo" , new Object[] {target} );
		}
		catch ( bsh.TargetError te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at infoIntentoBloqueo(), target id was " + target.getID() + ", error was " + te + io.getColorCode("reset") );					
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
		if ( !ejec )
		{		

			habitacionActual.reportAction ( this , target ,
					"$1 intenta defenderse de $2 con " + w.constructName2OneItem()  + ".\n",
					"$1 intenta defenderse con " + w.constructName2OneItem(target) + ".\n" ,
					"Intentas defenderte de $2 con " + w.constructName2OneItem(this) + ".\n" ,
					true );

		}

	}

	//esquivar, en general
	protected boolean esquivar ( )
	{
		List l = getAttackingEnemies();
		if ( l == null || l.size() == 0 )
			return false;
		else
		{
			Mobile objetivo = (Mobile) l.get(0);
			dodge ( objetivo );
			return true;
		}		
	}

	public void dodge ( Mobile target )
	{

		setNewTarget ( target.getID() );
		setNewState ( DODGING , generateDodgeTime() );

		//informar de esquivada
		boolean ejec = false;
		try
		{
			ejec = execCode ( "infoIntentoEsquivada" , new Object[] {target} );
		}
		catch ( bsh.TargetError te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at infoIntentoEsquivada(), target id was " + target.getID() + ", error was " + te + io.getColorCode("reset") );					
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
		if ( !ejec )
		{	

			habitacionActual.reportAction ( this , target ,
					"$1 intenta esquivar el ataque de $2" + ".\n" ,
					"$1 intenta esquivarte" + ".\n" ,
					"Intentas esquivar el ataque de $2" + ".\n" ,
					true );
		}		
	}


	public MobileList getEnemies ( )
	{
		if ( combatRefs == null )
		{
			combatRefs = new MobileList();
			List el = this.getRelatedEntitiesByValue("enemy", true);
			for (Iterator iterator = el.iterator(); iterator.hasNext();) {
				Entity enemy = (Entity) iterator.next();
				combatRefs.addEntity(enemy);
			}
		}
		return combatRefs;
	}

	public void removeAllEnemies()
	{
		for ( int i = 0 ; i < combatRefs.size() ; i++ )
		{
			this.setRelationshipProperty(combatRefs.elementAt(i), "enemy", false);
			combatRefs.removeElement(combatRefs.elementAt(i));
		}
	}

	//devuelve la lista de todos los enemigos que nos estï¿½n atacando.
	//(si no hay ninguno, la lista vacia)
	public List getAttackingEnemies ( )
	{
		if ( !hasEnemies() ) return new ArrayList();
		else
		{
			List res = new ArrayList();
			for ( int i = 0 ; i < getEnemies().size() ; i++ )
			{
				Mobile enemigo = getEnemies().elementAt(i);
				if ( enemigo.getState() == ATTACKING && enemigo.getTarget() == getID() )
				{
					res.add(enemigo);
				}
			}
			return res;
		}
	}

	//freeLimbsNeeded == true -> sï¿½lo blandir en miembro libre
	//freeLimbsNeeded == false -> si los miembros estï¿½n ocupados, desblandir las armas
	//blandidas en ellos para poder blandir ï¿½sta
	//return: true if successfully welded

	//OBSOLETE

	/*

	public boolean wieldWeapon ( Weapon w , boolean freeLimbsNeeded )
	{

		//ver si ya la estamos blandiendo

		if ( wieldedWeapons != null && wieldedWeapons.contains ( w ) )
		{
			//{ya estamos blandiendo este arma}
			Debug.println("Already wielding that one.");
			return false;
		}

		//inspeccionar miembros

		Vector limbPatternMatchVector = new Vector();

		if ( wieldingLimbs != null )
		{
			if ( w.wieldableLimbs != null )
			{
				StringTokenizer st = new StringTokenizer ( w.wieldableLimbs , "$" );

				while ( st.hasMoreTokens() )
				{
					Vector temp = wieldingLimbs.patternMatch ( st.nextToken() , false );
					for ( int l = 0 ; l < temp.size() ; l++ )
						if ( ! ( limbPatternMatchVector.contains ( temp.elementAt(l) ) ) )
							limbPatternMatchVector.addElement ( temp.elementAt(l) );
				}

			}
		}

		if ( limbPatternMatchVector.size() < 1 )
		{
			//{el arma no es compatible con nuestros miembros}
			Debug.println("Weapon not compatible.");		
			return false;
		}

		//buscar un miembro libre entre los obtenidos
		int limbn = 0;
		int k;

		//para cada miembro obtenido (que patternmatchea con los wieldables del arma)
		for ( k = 0 ; k < limbPatternMatchVector.size() ; k++ )
		{
			//buscar el numero de ese miembro en nuestro bicho y, si no lleva arma, breakear
			for ( limbn = 0 ; ; limbn++ )
			{	
				if ( wieldingLimbs.elementAt(limbn) == limbPatternMatchVector.elementAt(k) ) break;
			}
			if ( wieldedWeapons.elementAt( limbn ) == null ) break;
		}

		if ( k == limbPatternMatchVector.size() )
		{

			//hay miembros wieldables; pero ocupados.
			//solo blandiremos si freeLimbsNeeded es false.

			if ( freeLimbsNeeded ) 
			{
				Debug.println("No free limbs to spare.");
				return false;
			}
			else
			{

				//blandimos, por ejemplo, en limbPatternMatchVector(0)
				//(es un hecho aqui {limbPatternMatchVector.size()>=1})

				for ( limbn = 0 ; ; limbn++ )
				{	
					if ( wieldingLimbs.elementAt(limbn) == limbPatternMatchVector.elementAt(0) ) break;
				}

				wieldedWeapons.removeItem(wieldedWeapons.elementAt(limbn));

				return doWield ( w , limbn );

			}

		}

		//si llegamos aqui, el miembro numero limbn vale para wieldear el item, y esta desocupado.

		Debug.println("Calling doWield.");

		return doWield ( w , limbn );

	}



	//pone un arma en el inventario de armas wieldeadas.	
	private boolean doWield ( Weapon w , int limbn )
	{
		if ( wieldedWeapons.elementAt ( limbn ) != null )		
			return false;
		try
		{
			wieldedWeapons.setElementAt ( w , limbn );


			//informar de que hemos blandido

			habitacionActual.informActionAuto ( this , null , "$1 blande " + w.constructName2OneItem() + " en " + wieldingLimbs.elementAt(limbn).constructName2OneItem() + "\n" , true );			

			return true;

		}
		catch ( WeightLimitExceededException wle )
		{
			return false;
		}
		catch ( VolumeLimitExceededException vle )
		{
			return false;
		}
	}

	 */








	/******STUBS******/


	public boolean getAttackSuccessFromProbability ( Weapon w )
	{
		double dado = getRandom().nextDouble();
		double prob = generateAttackProbability(w);
		return ( dado < prob );
	}	

	public boolean getSuccessFromProbability ( Spell s )
	{
		double dado = getRandom().nextDouble();
		double prob = generateCastProbability(s);
		return ( dado < prob );
	}

	public boolean getBlockSuccessFromProbability ( Weapon w )
	{
		double dado = getRandom().nextDouble();
		double prob = generateDefenseProbability(w);
		return ( dado < prob );
	}

	public boolean getDodgeSuccessFromProbability ( )
	{
		double dado = getRandom().nextDouble();
		double prob = generateDodgeProbability();
		return ( dado < prob );
	}

	public double generateCastProbability ( Spell s )
	{
		double basicProb = s.getTypicalCastProbability(this);
		Debug.println("Basic probability: " + basicProb);
		//no introducimos factor gaussiano, la propia probabilidad ya implica influencia del azar
		return basicProb;
	}

	public double generateAttackProbability ( Weapon w )
	{
		double basicProb = w.getTypicalAttackProbability(this);
		//no introducimos factor gaussiano, la propia probabilidad ya implica influencia del azar
		return basicProb;
	}

	public double generateDefenseProbability ( Weapon w )
	{
		double basicProb = w.getTypicalDefenseProbability(this);
		//no introducimos factor gaussiano, la propia probabilidad ya implica influencia del azar
		return basicProb;
	}

	public double generateDodgeProbability ( )
	{
		return 0.2; //stub
	}

	//Be careful with negatives. Not treated. Candidato a explicaciï¿½n de posteriores fallos chungos?
	//(la gaussiana suma negativos)

	public int generateAttackTime( Weapon w )
	{
		double basicTime = w.getTypicalAttackTime ( this );
		double variedTime = Utility.applyGaussianVariation ( basicTime , getRandom() , 1.0/3.0 );
		Debug.println("Attack time with " + w + ": " + variedTime);
		if ( (int) variedTime < 1 )
			return 1;
		else	
			return (int) variedTime;		
	}
	public int generateCastTime( Spell s )
	{
		double basicTime = s.getTypicalCastTime ( this );
		double variedTime = Utility.applyGaussianVariation ( basicTime , getRandom() , 1.0/3.0 );
		Debug.println("Cast time with " + s + ": " + variedTime);
		if ( (int) variedTime < 1 )
			return 1;
		else	
			return (int) variedTime;		
	}
	public int generateBlockTime( Weapon w )
	{
		double basicTime = w.getTypicalDefenseTime ( this );
		double variedTime = Utility.applyGaussianVariation ( basicTime , getRandom() , 1.0/3.0 );
		Debug.println("Defense time with " + w + ": " + variedTime);
		if ( (int) variedTime < 1 )
			return 1;
		else	
			return (int) variedTime;
	}
	public int generateAttackRecoverTime( Weapon w )
	{
		double basicTime = w.getTypicalAttackRecoverTime ( this );
		double variedTime = Utility.applyGaussianVariation ( basicTime , getRandom() , 1.0/3.0 );
		Debug.println("Attack recover time with " + w + ": " + (int)variedTime);
		if ( (int) variedTime < 1 )
			return 1;
		else	
			return (int) variedTime;		
	}
	public int generateBlockRecoverTime( Weapon w )
	{
		double basicTime = w.getTypicalDefenseRecoverTime ( this );
		double variedTime = Utility.applyGaussianVariation ( basicTime , getRandom() , 1.0/3.0 );
		Debug.println("Defense recover time with " + w + ": " + variedTime);
		if ( (int) variedTime < 1 )
			return 1;
		else	
			return (int) variedTime;
	}
	public int generateRecoverFromUnblockedHitTime()
	{
		//varï¿½a mucho
		//later, recover usage?
		double basicTime = 30.0;
		double variedTime = Utility.applyGaussianVariation ( basicTime , getRandom() , 2.0/3.0 );
		Debug.println("Recover from unblocked hit time generated: " + variedTime);
		if ( (int) variedTime < 1 )
			return 1;
		else	
			return (int) variedTime;
	}
	public int generateDodgeTime()
	{
		return 15;
	}
	public int generateDodgeRecoverTime()
	{
		return 15;
	}



	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{

		org.w3c.dom.Element suElemento = doc.createElement( "Mobile" );

		suElemento.setAttribute ( "id" , String.valueOf( idnumber ) );
		suElemento.setAttribute ( "name" , String.valueOf ( title ) );
		suElemento.setAttribute ( "extends" , String.valueOf ( inheritsFrom ) );
		suElemento.setAttribute ( "clones" , String.valueOf ( isInstanceOf ) );
		suElemento.setAttribute ( "type" , String.valueOf ( mobileType ) );
		suElemento.setAttribute ( "properName" , String.valueOf ( properName ) );
		suElemento.setAttribute ( "hp" , String.valueOf ( hp ) );
		suElemento.setAttribute ( "mp" , String.valueOf ( mp ) );
		suElemento.setAttribute ( "maxhp" , String.valueOf ( maxhp ) );
		suElemento.setAttribute ( "maxmp" , String.valueOf ( maxmp ) );

		suElemento.appendChild ( getPropListXMLRepresentation(doc) );

		suElemento.appendChild ( getRelationshipListXMLRepresentation(doc) );

		org.w3c.dom.Element habitacAct = doc.createElement( "CurrentRoom" );
		if ( habitacionActual != null )
		{
			habitacAct.setAttribute ( "id" , String.valueOf(habitacionActual.getID()) );
			suElemento.appendChild(habitacAct);		
		}		

		habitacAct = doc.createElement( "LastRoom" );
		if ( habitacionAnterior != null )
		{
			habitacAct.setAttribute ( "id" , String.valueOf(habitacionAnterior.getID()) );
			suElemento.appendChild(habitacAct);
		}

		//temp gender representation
		suElemento.setAttribute ( "gender" , String.valueOf( gender ) );

		org.w3c.dom.Element listaDesc = doc.createElement("DescriptionList");
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			Description nuestraDescripcion = descriptionList[i];
			listaDesc.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
		}
		suElemento.appendChild(listaDesc);

		org.w3c.dom.Element listaSing = doc.createElement("SingularNames");
		for ( int i = 0 ; i < singNames.length ; i++ )
		{
			Description nuestraDescripcion = singNames[i];
			listaSing.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
		}
		suElemento.appendChild(listaSing);

		org.w3c.dom.Element listaPlur = doc.createElement("PluralNames");
		for ( int i = 0 ; i < plurNames.length ; i++ )
		{
			Description nuestraDescripcion = plurNames[i];
			listaPlur.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
		}
		suElemento.appendChild(listaPlur);

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

		//hechizos
		if ( spellRefs != null )
			suElemento.appendChild(spellRefs.getXMLRepresentation(doc));

		//virtual inventory
		if ( virtualInventory != null )
		{
			org.w3c.dom.Element vi = doc.createElement("VirtualInventory");
			vi.appendChild(virtualInventory.getXMLRepresentation(doc));
			suElemento.appendChild(vi);
		}

		//welded weapons inventory
		if ( wieldedWeapons != null )
		{
			org.w3c.dom.Element ww = doc.createElement("WieldedWeaponsInventory");
			ww.appendChild(wieldedWeapons.getXMLRepresentation(doc));
			suElemento.appendChild(ww);
		}

		//worn items inventory
		if ( wornItems != null )
		{
			org.w3c.dom.Element ww = doc.createElement("WornItemsInventory");
			ww.appendChild(wornItems.getXMLRepresentation(doc));
			suElemento.appendChild(ww);
		}

		//wielding limbs inventory
		if ( wieldingLimbs != null )
		{
			org.w3c.dom.Element wl = doc.createElement("WieldingLimbsInventory");
			wl.appendChild(wieldingLimbs.getXMLRepresentation(doc));
			suElemento.appendChild(wl);
		}

		//parts inventory
		if ( partsInventory != null )
		{
			org.w3c.dom.Element p = doc.createElement("Parts");
			p.appendChild(partsInventory.getXMLRepresentation(doc));
			suElemento.appendChild(p);
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

		//caracterï¿½sticas (traits): not at the moment
		//yeW! let's doo-eet!

		if ( caracteristicas != null )
		{
			suElemento.appendChild ( caracteristicas.getXMLRepresentation(doc));
		}

		//relationships and states: not at the moment

		//object code
		if ( itsCode != null )
			suElemento.appendChild(itsCode.getXMLRepresentation(doc));

		//PSI keywords and answers

		if ( PSIkeywords != null )
		{
			org.w3c.dom.Element PSISpecElement = doc.createElement("ConversationalAI");
			for ( int i = 0 ; i < PSIkeywords.size() ; i++ )
			{
				org.w3c.dom.Element mappingElement = doc.createElement("Mapping");
				mappingElement.setAttribute ( "command" , (String)PSIkeywords.elementAt(i) );

				Description[] respuesta = (Description[]) PSIanswers.elementAt(i);
				for ( int j = 0 ; j < respuesta.length ; j++ )
				{
					Description nuestraDescripcion = respuesta[j];
					mappingElement.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
				}

				PSISpecElement.appendChild(mappingElement);

			}
			suElemento.appendChild(PSISpecElement);
		}


		return suElemento;

	}


	public void loadNumberGenerator ( World mundo )
	{
		aleat = mundo.getRandom();
	}

	public java.util.Random getRandom()
	{
		return aleat;
	}

	public void incSkill ( String skillName )
	{
		caracteristicas.incSkill ( skillName );
	}	

	public void setSkill ( String skillN , long val )
	{
		caracteristicas.setSkill ( skillN , val );
	}

	public void setStat ( String stat , int val )
	{
		caracteristicas.setStat ( stat , val );
	}

	public Inventory getInventoryForCorpse ( )
	{
		if ( this instanceof Player ) return inventory;
		else return virtualInventory;
	}


	/* Carga diferida de las relationships */
	public void loadRelationshipsFromXML ( World mundo ) throws XMLtoWorldException
	{

		relationships = new Vector();
		relationship_properties = new Vector();

		org.w3c.dom.Node n = mundo.getMobileNode( String.valueOf(getID()) );

		org.w3c.dom.Element e = null;
		try
		{
			e = (org.w3c.dom.Element) n;
		}
		catch ( ClassCastException cce )
		{
			throw ( new XMLtoWorldException ( "Mobile node not Element" ) );
		}

		org.w3c.dom.NodeList relListNodes = e.getElementsByTagName ( "RelationshipList" );

		if ( relListNodes.getLength() > 0 )
		{
			org.w3c.dom.NodeList relationshipNodes;
			try
			{
				relationshipNodes = ( (org.w3c.dom.Element) relListNodes.item(0) ).getElementsByTagName ( "Relationship" );
			}
			catch ( ClassCastException cce )
			{
				throw ( new XMLtoWorldException ( "Relationship list node not Element" ) );
			}
			if ( relationshipNodes.getLength() > 0 )
			{
				for ( int i = 0 ; i < relationshipNodes.getLength() ; i++ )
				{

					//process a relationship

					List thisRelPropList = new ArrayList();
					Entity thisRelEntity = null;

					org.w3c.dom.Element relationshipNode = (org.w3c.dom.Element) relationshipNodes.item(i);
					org.w3c.dom.NodeList propListNodes = relationshipNode.getElementsByTagName("PropertyList");

					if ( propListNodes.getLength() > 0 )
					{
						org.w3c.dom.Element relationshipPropertyList = (org.w3c.dom.Element) propListNodes.item(i);
						org.w3c.dom.NodeList propEntryNodes = relationshipPropertyList.getElementsByTagName("PropertyEntry");

						if ( propEntryNodes.getLength() > 0 )
						{
							for ( int j = 0 ; j < propEntryNodes.getLength() ; j++ )
							{
								org.w3c.dom.Node nod =  propEntryNodes.item(j);
								PropertyEntry pe = new PropertyEntry ( mundo , nod );
								thisRelPropList.add(pe);
							}	
						}

						if ( relationshipNode.hasAttribute("id") )
						{
							thisRelEntity = mundo.getObject ( relationshipNode.getAttribute("id") );
							if ( thisRelEntity == null )
								throw ( new XMLtoWorldException ( "Entity referenced at Relationship node's ID attribute is nonexistent or null" ) );


							//finally, put the relationship into the object.
							relationships.add ( thisRelEntity );
							relationship_properties.add ( thisRelPropList );		
						}
						else
						{
							throw ( new XMLtoWorldException ( "Relationship node lacks attribute id" ) );
						}

					}




				}
			}
		}



	} //end method loadRel(...)



	//armas blandidas y armas naturales que no estï¿½n blandiendo nada.
	public Inventory getUsableWeapons()
	{

		Inventory result = new Inventory(1000000,1000000); //for example

		//Inventory result = getNaturalWeapons();
		for ( int i = result.size()-1 ; i>=0 ; i-- )
		{
			Item current = result.elementAt(i);
			List blandidos = current.getRelatedEntitiesByValue("wields",true);
			if ( blandidos.size() > 0 )
				result.removeItem(current);
		}

		Inventory wieldedWeapons = getWieldedWeapons();

		if ( wieldedWeapons != null )
		{
			for ( int i = 0 ; i < wieldedWeapons.size() ; i++ )
			{
				Item arma = wieldedWeapons.elementAt(i);
				if ( arma != null ) //old wielded weapon format permitted nulls
				{
					try
					{
						result.addItem(arma);
					}
					catch ( WeightLimitExceededException wle )
					{
						wle.printStackTrace();
					}
					catch ( VolumeLimitExceededException vle )
					{
						vle.printStackTrace();
					}
				}
			}
		}

		Inventory nat = getNaturalWeapons();

		for ( int i = 0 ; i < nat.size() ; i++ )
		{
			try
			{
				result.addItem(nat.elementAt(i));
			}
			catch ( WeightLimitExceededException wle )
			{
				wle.printStackTrace();
			}
			catch ( VolumeLimitExceededException vle )
			{
				vle.printStackTrace();
			}
		}

		Debug.println("Usable weapons: " + result);
		return result;
	}


	//devuelve las armas naturales (puï¿½os, etc... i.e. miembros que atacan) del bicho.
	public Inventory getNaturalWeapons()
	{
		Inventory miembros = getFlattenedPartsInventory();
		if ( miembros == null )
			return new Inventory(10000,10000);
		for ( int i = miembros.size()-1 ; i >= 0 ; i-- )
		{
			Item it = miembros.elementAt(i);
			if ( !(it instanceof Weapon ) )
			{
				miembros.removeItem(it);
			}
		}
		return miembros;
	}

	public Inventory getPartsInventory()
	{
		return partsInventory;
	}


	//devuelve un Inventory formado por las partes, las partes de partes, etc. etc.
	public Inventory getFlattenedPartsInventory()
	{

		//if ( partsInventory == null ) return null;
		if ( partsInventory == null )
			partsInventory = new Inventory(10000,10000);

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




	/*
	 * freeLimbsNeeded = true: si no hay miembros libres, fracasa la funciï¿½n (dev. false)
	 * freeLimbsNeeded = false: si no hay miembros libres, nos quitamos lo que llevemos en ellos para vestirnos
	 */

	public boolean intentarVestir ( Item it , boolean freeLimbsNeeded )
	{ 

		if ( ! ( it instanceof Wearable ) )
		{
			if ( it instanceof Weapon )
			{
				write( io.getColorCode("denial") + "ï¿½Vestir " + it.constructName2OneItem(this) + "? Parece mï¿½s adecuado blandir" + ((it.getGender())?"lo":"la") + ".\n" );
			}
			else
			{
				write( io.getColorCode("denial") + "No parece que " + it.constructName2OneItem(this) + " sea algo que se pueda vestir." + "\n" );
			}
			return false;
		}  

		Inventory ourLimbs = getFlattenedPartsInventory();

		if ( ourLimbs.size() < 1 ) write ( io.getColorCode("error") + "No puedes ponerte ropa si no tienes ningï¿½n miembro." );

		//ver si ya estamos blandiendo el item
		for ( int i = 0 ; i < ourLimbs.size() ; i++ )
		{
			Item limb = ourLimbs.elementAt(i);
			if ( limb.getRelationshipPropertyValueAsBoolean ( it , "wears" ) )
			{
				//write ( io.getColorCode("denial") + "Ya llevas puesto " + it.constructName2OneItem(this) + ".\n" );
				writeDenial(mundo.getMessages().getMessage("wear.already.worn","$item",it.constructName2OneItem(this),new Object[]{this,it}));
				return false;
			}		
		}

		//intentar cumplir todos los limbs requeridos por el Wearable.
		List requirements = ((Wearable)it).getLimbRequirementsList();

		Vector usedLimbs = new Vector(); //los iremos marcando como usados

		for ( int i = 0 ; i < requirements.size() ; i++ )
		{

			String requirementString = (String) requirements.get(i);			
			StringTokenizer st = new StringTokenizer ( requirementString , "$" );
			Vector matchingLimbs = new Vector();

			//determinar que miembros cumplen el requerimiento, poniendolos en matchingLimbs	
			while ( st.hasMoreTokens() )
			{
				Vector temp = ourLimbs.patternMatch ( st.nextToken() , false );
				for ( int l = 0 ; l < temp.size() ; l++ )
					if ( !matchingLimbs.contains( temp.elementAt(l) ) )
						matchingLimbs.add(temp.elementAt(l) );
			}

			//si no hay ninguno que lo cumpla, nada, no podemos vestir.
			if ( matchingLimbs.size() < 1 )
			{
				//write( io.getColorCode("denial")  + "No parece adecuado para los de tu especie." + io.getColorCode("reset")  + "\n"  );
				writeDenial(mundo.getMessages().getMessage("wear.no.suitable.limbs","$item",it.constructName2OneItem(this),new Object[]{this,it}));
				return false;
			}

			int k;

			//entre los miembros que cumplen el requerimiento, vemos si hay alguno libre
			for ( k = 0 ; k < matchingLimbs.size() ; k++ )
			{
				Item thisLimb = (Item) matchingLimbs.get(k);
				List vestidos = thisLimb.getRelatedEntitiesByValue("wears",true);	
				if ( vestidos.size() < 1 && !usedLimbs.contains(thisLimb) )
				{
					usedLimbs.add(thisLimb);
					break;
				}
			}

			if ( k == matchingLimbs.size() ) //no fuimos por el break, i.e. miembros ocupados
			{

				if ( !freeLimbsNeeded ) //Mobile auto-remove feature
				{

					//nos quitamos lo que llevemos en matchingLimbs(0)

					Item busyLimb = (Item) matchingLimbs.get(0);
					List vestidos = busyLimb.getRelatedEntitiesByValue("wears",true);	
					for ( int z = 0 ; z < vestidos.size() ; z++ )
					{
						if ( desvestir ( (Item) vestidos.get(z) ) == false )
							return false;
					}

				}

				else //Usual for Players. Fracasa la funciï¿½n.
				{

					for ( int n = 0 ; n < matchingLimbs.size() ; n++ )
					{
						Item current = ( Item ) matchingLimbs.get(n);
						List vestidos = current.getRelatedEntitiesByValue("wears",true);
						if ( vestidos.size() > 0 )
						{
							Item vestido = ( Item ) vestidos.get(0);
							write( io.getColorCode("information") + "Llevas puesto " + vestido.constructName2OneItem(this)  + " en " + current.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n"  );			
						}
						else
						{
							write( io.getColorCode("information") + "Tienes libre " + current.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n" );
						}

					}
					//write( io.getColorCode("denial") + "Tienes que quitarte algo para poder vestir " + it.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n"  );
					writeDenial(mundo.getMessages().getMessage("wear.busy.limbs","$item",it.constructName2OneItem(this),new Object[]{this,it}));
					return false;

				}

			}

		}

		//{si llegamos hasta aquï¿½, se dan todas las condiciones para vestir el item.}
		//{sï¿½lo tenemos que marcar el item como vestido y poner la relaciï¿½n "wears" con el item en todos los miembros de usedLimbs.}

		String toOutput = "";

		for ( int i = 0 ; i < usedLimbs.size() ; i++ )
		{
			Item limb = (Item) usedLimbs.get(i);
			limb.setRelationshipProperty ( it , "wears" , true );

			if ( i == 0 )
				toOutput += limb.constructName2OneItem(this);
			else if ( i > 0 && i == usedLimbs.size() - 1 )
				toOutput += " y " + limb.constructName2OneItem(this);
			else
				toOutput += ", " + limb.constructName2OneItem(this);	

		}

		//write( io.getColorCode("action") + "Te pones " + it.constructName2OneItem(this) + " en " + toOutput + ".\n" + io.getColorCode("reset") );
		writeAction( mundo.getMessages().getMessage("you.wear.item","$item",it.constructName2OneItem(this),"$limbs",toOutput,new Object[]{this,it,usedLimbs})  );
		
		//habitacionActual.reportActionAuto ( this , null , "$1 se pone " + it.constructName2OneItem() + ".\n" , false );	
		habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.wears.item","$item",it.constructName2OneItem(this),"$limbs",toOutput,new Object[]{this,it,usedLimbs}) , false );
		
		if ( wornItems == null ) wornItems = new Inventory(10000,10000);
		try
		{
			wornItems.addItem(it);	
		}
		catch ( WeightLimitExceededException wlee )
		{
			write( io.getColorCode("denial") + "No puedes vestir eso, pesa demasiado." + ".\n" + io.getColorCode("reset") );	
		}
		catch ( VolumeLimitExceededException vlee )
		{
			write( io.getColorCode("denial") + "No puedes blandir eso, pesa demasiado." + ".\n" + io.getColorCode("reset") );
		}

		return true;
	}




	/*
	 * freeLimbsNeeded = true: si no hay miembros libres, fracasa la funciï¿½n (dev. false)
	 * freeLimbsNeeded = false: si no hay miembros libres, nos quitamos lo que llevemos en ellos para vestirnos
	 */

	public boolean intentarBlandir ( Item it , boolean freeLimbsNeeded )
	{

		if ( ! ( it instanceof Weapon ) )
		{
			if ( it instanceof Wearable )
			{
				write( io.getColorCode("denial") + "ï¿½Blandir " + it.constructName2OneItem(this) + "? Parece mï¿½s adecuado vestir" + ((it.getGender())?"lo":"la") + ".\n" );
			}
			else
			{
				//write( io.getColorCode("denial") + "No parece que " + it.constructName2OneItem(this) + " sea un arma." + "\n" );
				writeDenial(mundo.getMessages().getMessage("wield.non.weapon","$item",it.constructName2OneItem(this),new Object[]{this,it}));
			}
			return false;
		}  

		Inventory ourLimbs = getFlattenedPartsInventory();

		//ver si ya estamos blandiendo el item
		for ( int i = 0 ; i < ourLimbs.size() ; i++ )
		{
			Item limb = ourLimbs.elementAt(i);
			if ( limb.getRelationshipPropertyValueAsBoolean ( it , "wields" ) )
			{
				//write ( io.getColorCode("denial") + "Ya estï¿½s blandiendo " + it.constructName2OneItem(this) + ".\n" );
				writeDenial(mundo.getMessages().getMessage("wield.already.wielded","$item",it.constructName2OneItem(this),new Object[]{this,it}));
				return false;
			}		
		}

		//intentar cumplir todos los limbs requeridos por el Weapon.
		List requirements = ((Weapon)it).getLimbRequirementsList();

		Vector usedLimbs = new Vector(); //los iremos marcando como usados

		for ( int i = 0 ; i < requirements.size() ; i++ )
		{

			String requirementString = (String) requirements.get(i);			
			StringTokenizer st = new StringTokenizer ( requirementString , "$" );
			Vector matchingLimbs = new Vector();

			//determinar que miembros cumplen el requerimiento, poniendolos en matchingLimbs	
			while ( st.hasMoreTokens() )
			{
				Vector temp = ourLimbs.patternMatch ( st.nextToken() , false );
				for ( int l = 0 ; l < temp.size() ; l++ )
					if ( !matchingLimbs.contains( temp.elementAt(l) ) )
						matchingLimbs.add(temp.elementAt(l) );
			}

			//si no hay ninguno que lo cumpla, nada, no podemos blandir.
			if ( matchingLimbs.size() < 1 )
			{
				//write( io.getColorCode("denial")  + "No parece adecuado para los de tu especie." + io.getColorCode("reset")  + "\n"  );
				writeDenial(mundo.getMessages().getMessage("wield.no.suitable.limbs","$item",it.constructName2OneItem(this),new Object[]{this,it}));
				return false;
			}

			int k;

			//entre los miembros que cumplen el requerimiento, vemos si hay alguno libre
			for ( k = 0 ; k < matchingLimbs.size() ; k++ )
			{
				Item thisLimb = (Item) matchingLimbs.get(k);
				List vestidos = thisLimb.getRelatedEntitiesByValue("wields",true);	
				if ( vestidos.size() < 1 && !usedLimbs.contains(thisLimb) )
				{
					usedLimbs.add(thisLimb);
					break;
				}
			}

			if ( k == matchingLimbs.size() ) //no fuimos por el break, i.e. miembros ocupados
			{

				if ( !freeLimbsNeeded ) //Mobile auto-remove feature
				{

					//nos quitamos lo que llevemos en matchingLimbs(0)

					Item busyLimb = (Item) matchingLimbs.get(0);
					List vestidos = busyLimb.getRelatedEntitiesByValue("wields",true);	
					for ( int z = 0 ; z < vestidos.size() ; z++ )
					{
						if ( guardarArma ( (Item) vestidos.get(z) ) == false )
							return false;
					}

				}

				else //Usual for Players. Fracasa la funciï¿½n.
				{

					for ( int n = 0 ; n < matchingLimbs.size() ; n++ )
					{
						Item current = ( Item ) matchingLimbs.get(n);
						List vestidos = current.getRelatedEntitiesByValue("wields",true);
						if ( vestidos.size() > 0 )
						{
							Item vestido = ( Item ) vestidos.get(0);
							write( io.getColorCode("information") + "Estï¿½s blandiendo " + vestido.constructName2OneItem(this)  + " en " + current.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n"  );			
						}
						else
						{
							write( io.getColorCode("information") + "Tienes libre " + current.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n" );
						}

					}
					write( io.getColorCode("denial") + "Tienes que guardar algï¿½n arma para poder blandir " + it.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n"  );
					return false;

				}

			}

		}

		//{si llegamos hasta aquï¿½, se dan todas las condiciones para blandir el item.}
		//{sï¿½lo tenemos que marcar el item como blandido y poner la relaciï¿½n "wields" con el item en todos los miembros de usedLimbs.}

		String toOutput = "";

		for ( int i = 0 ; i < usedLimbs.size() ; i++ )
		{
			Item limb = (Item) usedLimbs.get(i);
			limb.setRelationshipProperty ( it , "wields" , true );

			if ( i == 0 )
				toOutput += limb.constructName2OneItem(this);
			else if ( i > 0 && i == usedLimbs.size() - 1 )
				toOutput += " y " + limb.constructName2OneItem(this);
			else
				toOutput += ", " + limb.constructName2OneItem(this);	

		}

		//write( io.getColorCode("action") + "Blandes " + it.constructName2OneItem(this) + " en " + toOutput + ".\n" + io.getColorCode("reset") );
		writeAction( mundo.getMessages().getMessage("you.wield.item","$item",it.constructName2OneItem(this),"$limbs",toOutput,new Object[]{this,it,usedLimbs})  );
		
		//habitacionActual.reportActionAuto ( this , null , "$1 blande " + it.constructName2OneItem() + ".\n" , false );	
		habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.wields.item","$item",it.constructName2OneItem(this),"$limbs",toOutput,new Object[]{this,it,usedLimbs}) , false );
		
		
		
		
		
		
		
		if ( wieldedWeapons == null ) wieldedWeapons = new Inventory(10000,10000);
		try
		{
			wieldedWeapons.addItem(it);	
		}
		catch ( WeightLimitExceededException wlee )
		{
			write( io.getColorCode("denial") + "No puedes blandir eso, pesa demasiado." + ".\n" + io.getColorCode("reset") );	
		}
		catch ( VolumeLimitExceededException vlee )
		{
			write( io.getColorCode("denial") + "No puedes blandir eso, pesa demasiado." + ".\n" + io.getColorCode("reset") );
		}

		return true;
	}







	/*Comando para guardar armas y quitarse armaduras.*/
	public boolean intentarGuardar ( Item it )
	{

		//1. guardar armaduras
		if (  it instanceof Wearable  )
		{

			;


		}  
		//end guardar armaduras

		return false;

	}


	public boolean desvestir ( Item it )
	{

		if ( wornItems == null ) return false;

		boolean success = wornItems.removeItem(it);

		if ( success )
		{

			Inventory ourLimbs = getFlattenedPartsInventory();
			for ( int i = 0 ; i < ourLimbs.size() ; i++ )
			{
				Item curLimb = ourLimbs.elementAt(i);
				if ( curLimb.getRelationshipPropertyValueAsBoolean(it,"wears") )
				{
					curLimb.setRelationshipProperty(it,"wears",false);
				}
			}

			//write ( io.getColorCode("action") + "Te quitas " + it.constructName2OneItem() + ".\n" + io.getColorCode("reset") );
			writeAction ( mundo.getMessages().getMessage("you.unwear.item","$item",it.constructName2OneItem(),new Object[]{this,it}) );
						
			//habitacionActual.reportActionAuto ( this , null , "$1 se quita " + it.constructName2OneItem() + ".\n" , false );
			habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.unwears.item","$item",it.constructName2OneItem(),new Object[]{this,it}) , false );

			return true;

		}
		else
		{
			return false;
		}

	}

	public boolean guardarArma ( Item it )
	{

		if ( wieldedWeapons == null ) return false;

		boolean success = wieldedWeapons.removeItem(it);

		if ( success )
		{

			Inventory ourLimbs = getFlattenedPartsInventory();
			for ( int i = 0 ; i < ourLimbs.size() ; i++ )
			{
				Item curLimb = ourLimbs.elementAt(i);
				if ( curLimb.getRelationshipPropertyValueAsBoolean(it,"wields") )
				{
					curLimb.setRelationshipProperty(it,"wields",false);
				}
			}

			//write ( io.getColorCode("action") + "Dejas de blandir " + it.constructName2OneItem() + ".\n" + io.getColorCode("reset") );
			//habitacionActual.reportActionAuto ( this , null , "$1 deja de blandir " + it.constructName2OneItem() + ".\n" , false );

			writeAction ( mundo.getMessages().getMessage("you.unwield.item","$item",it.constructName2OneItem(),new Object[]{this,it}) );
			habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.unwields.item","$item",it.constructName2OneItem(),new Object[]{this,it}) , false );
			
			return true;

		}
		else
		{
			return false;
		}

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

	//predicate-supporting
	public String getPlurName ( Entity viewer )
	{
		return getName ( false , viewer );
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

	//predicate-supporting
	public String constructName ( int nItems , Entity viewer )
	{

		if ( nItems == 1 )
		{
			return presentName ( getSingName(viewer) , "un" , "una" );
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
			return ( str + " " + getPlurName ( viewer ) );
		}
		else
		{
			return ( nItems + " " + getPlurName( viewer ) );	
		}
	}

	//predicate-supporting
	public String constructName2 ( int nItems , Entity viewer )
	{

		if ( nItems == 1 )
		{
			return presentName ( getSingNameTrue(viewer) , "el" , "la" );	
		}	
		else
		{
			return ( nItems + " " + getPlurName( viewer ) );	
		}
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

	//predicate-supporting
	public String constructName2True ( int nItems , Entity viewer )
	{

		if ( nItems == 1 )
		{

			return presentName ( getSingNameTrue(viewer) , "el" , "la" );
			/*
			String baseName = getSingNameTrue( viewer );

			if ( baseName.startsWith("$"))
			{
				properName = true;
				baseName = baseName.substring(1);
			}

			if ( properName )
				return baseName;
			if ( gender )
				return ( "el " + baseName );
			else
				return ( "la " + baseName );
			 */	

		}	
		else
		{
			return ( nItems + " " + getPlurNameTrue( viewer ) );	
		}

	}

	//predicate-supporting
	public String constructName2OneItem ( Entity viewer )
	{
		return constructName2True ( 1 , viewer ) ;
	}


	//devuelve un miembro al azar para ser golpeado por un arma, teniendo en cuenta el volumen de los diferentes miembros.
	//los de volumen cero no seran golpeados (dedos, etc.)
	public Item getRandomLimbToHit ( )
	{

		Inventory limbs = getFlattenedPartsInventory();

		if ( limbs == null || limbs.size() == 0 ) return null; //no se consideran miembros para este bicho

		int volTot = limbs.getVolume();

		if ( volTot == 0 ) return null; //no hay miembros golpeables

		int volElegido = Math.abs ( aleat.nextInt() ) % volTot;

		Item chosen = null;
		int volsum = 0;
		int j = 0;
		while ( volsum <= volElegido )			
		{
			chosen = limbs.elementAt(j);
			volsum += chosen.getVolume();
			j++;
		}
		return chosen;	

	}


	//devuelve lo grave que es el daï¿½o
	public String estimateDamage ( int damAmt )
	{

		double percent = (double)damAmt / (double)maxhp * 100.0;

		String result;

		if ( percent < 5 )
			result = "muy ligeros daños";
		else if ( percent < 10 )
			result = "ligeros daños";	
		else if ( percent < 18 )
			result = "algún daño";	
		else if ( percent < 25 )
			result = "un daño significativo";
		else if ( percent < 33 )
			result = "daños moderados";
		else if ( percent < 40 )
			result = "bastante daño";
		else if ( percent < 50 )
			result = "severos daños";
		else if ( percent < 60 )
			result = "mucho daño";
		else if ( percent < 70 )
			result = "gran daño";
		else if ( percent < 80 )
			result = "enorme daño";
		else if ( percent < 90 )
			result = "terribles heridas";	
		else
			result = "daño mortal";	

		return result;

	}

	public String estimateStatus (  )
	{

		//percent = % de daï¿½o recibido
		double percent = (double)(maxhp-hp) / (double)maxhp * 100.0;

		String result;

		if ( percent <= 0.1 )
			result = "está intacto";
		else if ( percent < 5 )
			result = "tiene algún rasguño";
		else if ( percent < 10 )
			result = "tiene ligeras heridas";	
		else if ( percent < 18 )
			result = "tiene heridas poco graves";	
		else if ( percent < 25 )
			result = "tiene heridas de cierta consideración";
		else if ( percent < 33 )
			result = "tiene heridas importantes";
		else if ( percent < 40 )
			result = "tiene heridas graves";
		else if ( percent < 50 )
			result = "tiene heridas preocupantes";
		else if ( percent < 60 )
			result = "está gravemente herido";
		else if ( percent < 70 )
			result = "está muy gravemente herido";
		else if ( percent < 80 )
			result = "está críticamente herido";
		else if ( percent < 90 )
			result = "está en las últimas";	
		else if ( percent < 100 )
			result = "está a un paso de la tumba";	
		else
			result = "está mortalmente herido";

		return result;

	}

	public int getHP()
	{
		return hp;
	}
	public int getMaxHP()
	{
		return maxhp;
	}
	public int getMP()
	{
		return mp;
	}
	public int getMaxMP()
	{
		return maxmp;
	}


	public Object clone( )
	{
		//do it!

		Mobile it = new Mobile();

		copyMobileFieldsTo(it);

		return it;
	}



	public void cast ( Spell s , Entity target )
	{

		Debug.println( this + " is actually tryin' to cast " + s );

		int manaCost = (int) s.getTypicalManaCost ( this );
		Debug.println("Mana Cost: " + manaCost);
		if ( manaCost > mp )
		{
			Debug.println("Mana Cost Flubbed!");
			write("No tienes suficiente manï¿½ para ejecutar ese hechizo.\n");
			return;
		}  
		else
		{
			mp -= manaCost;
			if ( target != null )
				setNewTarget ( target.getID() );
			else
				setNewTarget(-1);

			boolean ejecutado = false;
			try
			{
				ejecutado = s.execCode( "prepare" , new Object[] { this , target  } );
			}
			catch (bsh.TargetError bshte)
			{
				//escribir("bsh.TargetError found at fail routine" );
				;
				writeError(ExceptionPrinter.getExceptionReport(bshte));
			}
			if ( ejecutado ) return;


			setNewState ( CASTING , generateCastTime( s ) );
			setCurrentSpell(s);

			getCurrentSpell().incrementUsage(this);

			Debug.println("Cast state set!");

			return;
		}
	}

	public void manageEndOfCastState()
	{

		Debug.println("End of cast state!");


		if ( getSuccessFromProbability ( getCurrentSpell() ) )
		{
			if ( getTarget() == -1 )
				getCurrentSpell().cast ( this , null ) ;
			else
				getCurrentSpell().cast ( this , mundo.getObject ( getTarget() ) );
			//return true;
		}
		else
		{
			if ( getTarget() == -1 )
				getCurrentSpell().fail ( this , null ) ;
			else
				getCurrentSpell().fail ( this , mundo.getObject ( getTarget() ) );
			//return false;
		}

		setNewState ( IDLE , 1 );

	}

	void interrupt ( String cause )
	{

		Debug.println("INTERRUPT!!!!!!!!!!!!");

		switch ( getState() )
		{
		case MOVING:
			write( Character.toUpperCase(cause.charAt(0)) + cause.substring(1) + " interrumpe tu intento de irte.\n" );
			break;
		case ATTACKING:
			write( Character.toUpperCase(cause.charAt(0)) + cause.substring(1) + " interrumpe tu intento de ataque.\n" );
			break;
		case CASTING:
			write( Character.toUpperCase(cause.charAt(0)) + cause.substring(1) + " interrumpe tu intento de hacer magia.\n" );
			break;
		}



	}


	//try to "poner (cosa) en (otracosa)" with two inventories (one for cosa and the other for otracosa).
	//returns whether there has been a match.
	public boolean putInside ( Inventory inv1 , Inventory inv2 , String arguments )
	{

		Vector[] patternMatchVectorSingSing = null;
		//Vector[] patternMatchVectorSingPlur = null; //no podemos poner una cosa en varios sitios
		Vector[] patternMatchVectorPlurSing = null;
		//Vector[] patternMatchVectorPlurPlur = null;

		boolean mirado = false;

		patternMatchVectorSingSing = inv1.patternMatchTwo ( inv2 , arguments , false , false ); //en singular y singular
		patternMatchVectorPlurSing = inv1.patternMatchTwo ( inv2 , arguments , true , false ); //en plural y singular



		if ( patternMatchVectorSingSing != null && patternMatchVectorSingSing[0].size() > 0 ) //ponemos una cosa en un sitio
		{
			mirado = true;
			Vector[] theVectors = patternMatchVectorSingSing;
			Item ourContainer = (Item)(theVectors[1].elementAt(0));
			Item ourItem = (Item)(theVectors[0].elementAt(0));

			if ( checkPutInside(ourContainer) )
				putInside(ourItem,ourContainer);
		}
		else if ( patternMatchVectorPlurSing != null && patternMatchVectorPlurSing[0].size() > 0 )
		{
			mirado = true;
			Vector[] theVectors = patternMatchVectorPlurSing;
			Item ourContainer = (Item)theVectors[1].elementAt(0);
			if ( checkPutInside(ourContainer) )
			{
				for ( int i=0 ; i < theVectors[0].size() ; i++ )
				{
					Item ourItem = (Item)theVectors[0].elementAt(i);		

					putInside(ourItem,ourContainer);
				} //end for
			}
		}
		return mirado;

	}




	//check if we can put stuff inside the container
	public boolean checkPutInside ( Item ourContainer )
	{
		if ( !ourContainer.isContainer() )
		{
			write ( io.getColorCode("denial") + 
					//"No parece muy ï¿½til poner cosas en " + ourContainer.constructName2True(1,this) + "." + io.getColorCode("reset") 
					mundo.getMessages().getMessage("cant.put.into.noncontainer","$container",ourContainer.constructName2True(1,this),new Object[]{this,ourContainer})
					+ io.getColorCode("reset") );
			return false;
		}
		else if ( ourContainer.isCloseable() && !ourContainer.isOpen() )
		{
			writeDenial (
			mundo.getMessages().getMessage("cant.put.into.closed","$container",ourContainer.constructName2True(1,this),"$oa",((ourContainer.getGender())?"o":"a"),new Object[]{this,ourContainer})
			);
			//write ( io.getColorCode("denial")  + ourContainer.constructName2True(1,this) + " estï¿½ cerrad" + ((ourContainer.getGender())?"o.":"a.") + io.getColorCode("reset") + "\n"  );
			return false;
		}
		return true;
	}

	//put inside action

	public void putInside ( Item ourItem , Item ourContainer )
	{



		try
		{
			ourContainer.getContents().addItem(ourItem);
			removeItem(ourItem);

			boolean eventEnded = false;
			//ejecutar eventos onPutInside
			try
			{		
				eventEnded = ourItem.execCode("onPutInside",new Object[] {this,ourItem,ourContainer} );
				if ( !eventEnded )
					eventEnded = ourContainer.execCode("onPutInside",new Object[] {this,ourItem,ourContainer} );
				if ( !eventEnded ) 
					eventEnded = this.execCode("onPutInside",new Object[] {this,ourItem,ourContainer} );
			}
			catch ( bsh.TargetError te )
			{
				write( io.getColorCode("error") + "bsh.TargetError found at event onGet , item number " + ourItem.getID() + io.getColorCode("reset") + "\n"  );
				writeError(ExceptionPrinter.getExceptionReport(te));
			}

			if ( !eventEnded )
			{
				//write( io.getColorCode("action")  + "Pones " + ourItem.constructName2True(1,this) + " en " + ourContainer.constructName2True(1,this) + "." + io.getColorCode("reset")  + "\n"  );
				writeAction ( mundo.getMessages().getMessage("you.put.into","$item",ourItem.constructName2True(1,this),"$container",ourContainer.constructName2True(1,this),new Object[]{this,ourItem,ourContainer}) );
			}
		}
		catch ( WeightLimitExceededException wle )
		{
			//write ( io.getColorCode("denial") + ourContainer.constructName2True(1,this) + " no puede soportar tanto peso." + io.getColorCode("reset")  + "\n"  );
			writeDenial ( mundo.getMessages().getMessage("cant.put.into.weight","$item",ourItem.constructName2True(1,this),"$container",ourContainer.constructName2True(1,this),new Object[]{this,ourItem,ourContainer}));
		}
		catch ( VolumeLimitExceededException vle )
		{
			//write ( io.getColorCode("denial") + ourItem.constructName2True(1,this) + " no cabe en " +  ourContainer.constructName2True(1,this) + io.getColorCode("reset")  + "\n"  );
			writeDenial ( mundo.getMessages().getMessage("cant.put.into.weight","$item",ourItem.constructName2True(1,this),"$container",ourContainer.constructName2True(1,this),new Object[]{this,ourItem,ourContainer}));
		}

	}




	//action method

	boolean getItem ( Item ourItem , Inventory inv , String toAppend )
	{

		try
		{

			if ( !ourItem.isGettable() )
			{
				//write( io.getColorCode("denial")  + "No tiene mucho sentido intentar coger "+ ourItem.constructName2True(1,this) + ".\n" + io.getColorCode("reset") );
				write( io.getColorCode("denial")  + mundo.getMessages().getMessage("denial.get.ungettable","$item",ourItem.constructName2True(1,this) , new Object[]{this,ourItem} ) /*+ "\n"*/ + io.getColorCode("reset") );
				return true;
			}

			//agregar item a inventario
			addItem ( ourItem );
			//Debug.println("Added: " + ourItem.constructName2(1,ourItem.getState()) );
			//decir que lo ha cogido


			//write( io.getColorCode("action")  + lenguaje.gramaticalizar (  "Coges " + ourItem.constructName2True(1,this) + toAppend + "." ) + "\n" + io.getColorCode("reset") );
			if ( toAppend == null || toAppend.length() < 1 )
				write( io.getColorCode("action")  + lenguaje.gramaticalizar ( mundo.getMessages().getMessage("you.get.item","$item",ourItem.constructName2True(1,this)+toAppend,new Object[]{this,ourItem})) + "\n" + io.getColorCode("reset") );
			else
				write( io.getColorCode("action")  + lenguaje.gramaticalizar ( mundo.getMessages().getMessage("you.get.item.from.location","$item",ourItem.constructName2True(1,this),"$location",toAppend.trim(),new Object[]{this,ourItem,toAppend})) + "\n" + io.getColorCode("reset") );
			
			//habitacionActual.informActionAuto ( this , null , "$1 coge " + ourItem.constructName2OneItem() + toAppend + ".\n" , false );
			if ( toAppend == null || toAppend.length() < 1 )
				habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.gets.item","$item", ourItem.constructName2OneItem(),new Object[]{this,ourItem} ) /*+ "\n"*/ , false );
			else
				habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.gets.item.from.location","$item", ourItem.constructName2OneItem(),"$location",toAppend.trim(),new Object[]{this,ourItem} ) /*+ "\n"*/ , false );
			
			//ejecutar eventos onGet
			try
			{
				ourItem.execCode("event_get","this: "+	ourItem.getID() + "\n" + "player: " + getID() );		
				ourItem.execCode("onGet",new Object[] {this} );
			}
			catch ( EVASemanticException exc ) 
			{
				write( io.getColorCode("error") + "EVASemanticException found at event_get , item number " + ourItem.getID() + io.getColorCode("reset") + "\n"  );
			}
			catch ( bsh.TargetError te )
			{
				write( io.getColorCode("error") + "bsh.TargetError found at event onGet , item number " + ourItem.getID() + io.getColorCode("reset") + "\n"  );
				writeError(ExceptionPrinter.getExceptionReport(te));
			}

			//velar por el principio de conservacion de la masa
			inv.removeItem ( ourItem );
			if ( inv == habitacionActual.getInventory() ) ourItem.removeRoomReference(habitacionActual);

			//ejecutar descripciï¿½n (pesa poquito, etc. etc.)
			boolean execced = false;
			try
			{
				execced = mundo.execCode("messageAfterGet",new Object[] {this,ourItem} );
			}
			catch ( bsh.TargetError te )
			{
				write( io.getColorCode("error") + "bsh.TargetError found at messageAfterGet , item number " + ourItem.getID() + io.getColorCode("reset") + "\n"  );
				writeError(ExceptionPrinter.getExceptionReport(te));
			}

			if ( !execced )
			{
				//afterGet por defecto: descripciï¿½n del ï¿½tem.
				writeDescription(ourItem.getDescription(this) + "\n");
			}


			/*
			 * this has been externalised to the desconget.bsh library as of 2009-04-27
			 * 

				//describirlo
				write( io.getColorCode("description") + ourItem.getDescription(this) + '\n' + io.getColorCode("reset")  );

				//dar una aproximacion del peso
				double factorApr = 0.9+Math.random()/5;
				int pesoApr;
				pesoApr = (int)Math.round( factorApr*ourItem.getWeight() );

				int pesoApr_kilos = pesoApr / 8;
				int pesoApr_cuartos = (pesoApr % 8) /2 ;
				String pesoDescription = "";

				if ( pesoApr_kilos == 0 )
				{
					if ( pesoApr_cuartos == 0 ) 
					{
						pesoDescription = "Pesa muy poquito.";
					}
					else if ( pesoApr_cuartos == 2 )
					{
						pesoDescription = "Pesarï¿½ medio kilo.";
					}
					else if ( pesoApr_cuartos == 1 )
					{
						pesoDescription = "Pesarï¿½ un cuarto kilo, mï¿½s o menos.";
					}
					else if ( pesoApr_cuartos == 3 )
					{
						pesoDescription = "Pesarï¿½ cerca de un kilo.";
					}
				}
				else
				{
					if ( pesoApr_cuartos == 0 || pesoApr_cuartos == 1 )
					{
						pesoDescription = "Pesarï¿½ aproximadamente " + pesoApr_kilos + " kilo" + (pesoApr_kilos>1?"s":"") + ".";
					}
					else if ( pesoApr_cuartos == 2 || pesoApr_cuartos == 3 )
					{
						pesoDescription = "Debe de pesar algo mï¿½s de " + pesoApr_kilos + " kilo" + (pesoApr_kilos>1?"s":"") + ".";
					}
				}

				//write( io.getColorCode("description") + pesoDescription + io.getColorCode("reset") + "\n" );
				writeDescription(pesoDescription+"\n");	
			 */


		}
		catch ( WeightLimitExceededException we )
		{
			//write( io.getColorCode("denial")  + "ï¿½Llevas demasiado peso para coger "+ ourItem.constructName2True(1,this) + "!\n" + io.getColorCode("reset") );
			write( io.getColorCode("denial") + mundo.getMessages().getMessage("cant.get.item.weight","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem} ) /*+ "\n"*/ + io.getColorCode("reset"));
		}
		catch ( VolumeLimitExceededException ve )
		{
			//write( io.getColorCode("denial")  + "ï¿½Llevas objetos demasiado voluminosos para coger "+ ourItem.constructName2True(1,this) + "!\n" + io.getColorCode("reset") );
			writeDenial( mundo.getMessages().getMessage("cant.get.item.volume","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem}) /*+ "\n"*/ );
		}

		return true;

	}



	protected boolean cogerItem ( String args , Inventory inv , String extraInfo )
	{
		boolean mirado = false;

		String toAppend;
		if ( extraInfo == null ) toAppend = "";
		else toAppend = extraInfo;

		if ( inv == null || inv.isEmpty() ) return false;

		//Debug.println("cogerItem args: " + args + "Inv " + inv );

		Vector patternMatchVectorSing = inv.patternMatch ( args , false ); //en singular
		Vector patternMatchVectorPlur = inv.patternMatch ( args , true ); //en plural

		if ( patternMatchVectorSing.size() > 0 ) //cogemos un objeto
		{

			Item ourItem = (Item)patternMatchVectorSing.elementAt(0);
			mirado = true;

			executeAction ( "get" , new Object[] 
			                                   { ourItem , inv , toAppend 
			                                   } );

			

		}
		else if ( patternMatchVectorPlur.size() > 0 )
		{
			//no era en singular, probamos en plural.
			Item ourItem;
			mirado = true;
			//coger todos los items
			for ( int i = 0 ; i < patternMatchVectorPlur.size() ; i++ )
			{

				ourItem = (Item)patternMatchVectorPlur.elementAt(i);

				executeAction ( "get" , new Object[] 
				                                   { ourItem , inv , toAppend 
				                                   } );

				
			}

		}

		return mirado;
	}








	boolean executeAction ( String actionName , Object[] actionArgs )
	{

		//ejecutado = execCode( "before" , new Object[] { io } , retval );

		boolean ejecutado_algo = false;

		//Preparar argumentos para mï¿½todo de acciï¿½n general

		//Object[] generalArgs = new Object[actionArgs.length+2];
		Object[] generalArgs = new Object[3];
		generalArgs[0] = actionName;
		generalArgs[1] = this;
		//for ( int i = 0 ; i < actionArgs.length ; i++ )
		//	generalArgs[i+2] = actionArgs[i];
		generalArgs[2] = actionArgs;

		try
		{
			ejecutado_algo = mundo.execCode ( "before" , generalArgs );
		}
		catch (bsh.TargetError bshte)
		{
			write("bsh.TargetError found at general before method" );
			bshte.printStackTrace();
			writeError(ExceptionPrinter.getExceptionReport(bshte));
		}

		if ( ejecutado_algo ) return true;

		//Argumentos para mï¿½todo del sujeto: actionArgs

		try
		{
			ejecutado_algo =  this.execCode ( "before_" + actionName , actionArgs );
		}
		catch (bsh.TargetError bshte)
		{
			write("bsh.TargetError found at subject before method" );
			bshte.printStackTrace();
			writeError(ExceptionPrinter.getExceptionReport(bshte));
		}

		if ( ejecutado_algo ) return true;

		Object[] do_args = new Object[actionArgs.length];

		if ( actionArgs.length > 0 && actionArgs[0] instanceof SupportingCode )
		{
			//hay objeto directo
			SupportingCode directObject = (SupportingCode) actionArgs[0];
			do_args[0] = this;
			for ( int i = 1 ; i < actionArgs.length ; i++ )
				do_args[i] = actionArgs[i];

			try
			{
				ejecutado_algo =  directObject.execCode ( "before_do_" + actionName , do_args );
			}
			catch (bsh.TargetError bshte)
			{
				write("bsh.TargetError found at direct object before method" );
				bshte.printStackTrace();
				writeError(ExceptionPrinter.getExceptionReport(bshte));
			}

			if ( ejecutado_algo ) return true;

		}

		if ( actionArgs.length > 1 && actionArgs[1] instanceof SupportingCode )
		{
			//hay objeto indirecto, su mï¿½todo sï¿½lo sustituye el argumento 1.
			do_args[1] = actionArgs[0];
			SupportingCode indirectObject = (SupportingCode) actionArgs[1];
			try
			{
				ejecutado_algo =  indirectObject.execCode ( "before_io_" + actionName , do_args );
			}
			catch (bsh.TargetError bshte)
			{
				write("bsh.TargetError found at indirect object before method" );
				bshte.printStackTrace();
				writeError(ExceptionPrinter.getExceptionReport(bshte));
			}

			if ( ejecutado_algo ) return true;

		}

		//action actual execution

		if ( actionName.equalsIgnoreCase("go") )
		{
			return go ( (Path) actionArgs[0] );
		}

		else if ( actionName.equalsIgnoreCase("get") )
		{
			return getItem ( (Item) actionArgs[0] , (Inventory) actionArgs[1] , (String) actionArgs[2] );
		}

		//se puede invocar multiples veces si el comando es de desvestir varias cosas
		else if ( actionName.equalsIgnoreCase("unwear") )
		{
			boolean retval = desvestir ( (Item) actionArgs[0] );
			if ( retval == true )
				setNewState(1,5);
			return retval;	
		}

		//se puede invocar multiples veces si el comando es de vestir varias cosas
		else if ( actionName.equalsIgnoreCase("wear") )
		{
			boolean retval = intentarVestir ( (Item) actionArgs[0] , true );
			if ( retval == true )
				setNewState(1,5);
			return retval;	
		}

		//se puede invocar multiples veces si el comando es de blandir varias cosas
		else if ( actionName.equalsIgnoreCase("wield") )
		{
			boolean retval = intentarBlandir ( (Item) actionArgs[0] , true );
			if ( retval == true )
				setNewState(1,5);
			return retval;	
		}

		//se puede invocar multiples veces si el comando es de desenfundar varias armas
		else if ( actionName.equalsIgnoreCase("unwield") )
		{
			boolean retval = guardarArma ( (Item) actionArgs[0] );
			if ( retval == true )
				setNewState(1,5);
			return retval;	
		}

		//se puede invocar multiples veces si el comando es de dejar varios objetos
		else if ( actionName.equalsIgnoreCase("drop") )
		{
			boolean retval = dejarItem ( (Item) actionArgs[0] );
			if ( retval == true )
				setNewState(1,1);
			return retval;
		}


		return false;

	}



	boolean dejarItem ( Item ourItem )
	{
		try
		{
			//Aquï¿½ irï¿½ toda la parafernalia de mirar si estï¿½ maldito, etecepunto, etecepunto.
			habitacionActual.addItem ( ourItem );
			removeItem ( ourItem );

			//habitacionActual.reportActionAuto ( this , null , "$1 deja " + ourItem.constructName2OneItem() + ".\n" , false );
			habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.drops.item","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem}) , false );
			
			
			
			//si es un arma que blandimos, dejar tambien de blandirla
			if ( wieldedWeapons != null && wieldedWeapons.contains(ourItem) )
			{
				/*
				wieldedWeapons.removeItem(ourItem);
				escribir( io.getColorCode("action")  + "Dejas de blandir " + ourItem.constructName2True(1,ourItem.getState()) + ".\n" + io.getColorCode("reset") );
				 */
				guardarArma(ourItem);
			}
			//si es un wearable que llevamos, dejar tambien de llevarlo
			if ( wornItems != null && wornItems.contains(ourItem) )
			{
				desvestir(ourItem);
			}

			//write( io.getColorCode("action")  + "Dejas " + ourItem.constructName2True(1,this) + ".\n" + io.getColorCode("reset") );
			writeAction ( mundo.getMessages().getMessage("you.drop.item","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem}));
			
		}
		catch ( WeightLimitExceededException wle )
		{
			/*esta excepciï¿½n es absurda en este caso, nunca saldrï¿½ (lï¿½mite de peso de habitaciï¿½n, enorme)*/
			//write( io.getColorCode("denial") + "No puedes dejar aquï¿½ " + ourItem.constructName2True(1,this) + ", hay demasiado peso.\n" + io.getColorCode("reset") );
			write( io.getColorCode("denial") + mundo.getMessages().getMessage("cant.drop.item.weight","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem} ) /*+ "\n"*/ + io.getColorCode("reset"));
			return false;
		}
		catch ( VolumeLimitExceededException vle )
		{
			//write( io.getColorCode("denial") + "No puedes dejar aquï¿½ " + ourItem.constructName2True(1,this) + ", no hay espacio suficiente.\n" + io.getColorCode("reset") );
			write( io.getColorCode("denial") + mundo.getMessages().getMessage("cant.drop.item.volume","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem} ) /*+ "\n"*/ + io.getColorCode("reset"));
			return false;
		}
		//setNewState( 1 /*IDLE*/, 1 );
		//ZR_verbo = command;
		return true;
	}



	protected boolean mirarItem ( String arguments , Inventory inv ) 
	{

		boolean mirado = false;

		if ( inv == null || inv.isEmpty() ) return false;

		Vector patternMatchVectorSing = inv.patternMatch ( arguments , false ); //en singular
		Vector patternMatchVectorPlur = inv.patternMatch ( arguments , true ); //en plural


		if ( patternMatchVectorSing.size() > 0 && !(((Item)patternMatchVectorSing.elementAt(0)).getDescription(this).equals("") ) ) //miramos un objeto
		{
			write(  io.getColorCode("description") + ((Item)patternMatchVectorSing.elementAt(0)).getDescription(this) + io.getColorCode("reset") + "\n" );
			mirado=true;
		}	
		else if ( patternMatchVectorPlur.size() > 0 )
		{
			mirado=true;
			//no era en singular, probamos en plural.
			Item ourItem;
			//mirar todos los items
			for ( int i = 0 ; i < patternMatchVectorPlur.size() ; i++ )
			{
				ourItem = (Item)patternMatchVectorPlur.elementAt(i);
				if ( !((Item)ourItem).getDescription(this).equals("")  )
				{
					write( '\n' + "Miras " + ourItem.constructName2True ( 1 , this ) + ": " );
					write( io.getColorCode("description") + ((Item)ourItem).getDescription(this) + io.getColorCode("reset") + "\n"   );
				}
			}	
		}

		return mirado;

	} //end method mirar item

	protected boolean mirarExtrasItems ( String arguments , Inventory inv )
	{

		boolean mirado = false;
		String s;
		if ( inv == null || inv.isEmpty() ) return false;

		for ( int i = 0 ; i < inv.size() ; i++ ) //mirar item i
		{
			Debug.println("Seein' if item " + inv.elementAt(i) + " has extra.");
			if ( ( s = inv.elementAt(i).getExtraDescription( arguments , this ) ) != null )
			{
				write(io.getColorCode("description")+s+io.getColorCode("reset")+"\n");
				return true;
			}
		}

		return false;

	}

	protected boolean mirarExtrasBichos ( String arguments , MobileList inv )
	{

		boolean mirado = false;
		String s;
		if ( inv == null || inv.isEmpty() ) return false;

		for ( int i = 0 ; i < inv.size() ; i++ ) //mirar item i
		{
			Debug.println("Seein' if item " + inv.elementAt(i) + " has extra.");
			if ( ( s = inv.elementAt(i).getExtraDescription( arguments , this ) ) != null )
			{
				write(io.getColorCode("description")+s+io.getColorCode("reset")+"\n");
				return true;
			}
		}

		return false;

	}

	protected boolean mirarBicho ( String arguments , MobileList ml ) //en realidad, lo mismo que el anterior.
	//ï¿½Interfaz Referenciable para estas cosas [constructname...], y ReferenciableList en vez de EntityList?
	{

		boolean mirado = false;

		if ( ml == null || ml.isEmpty() ) return false;

		Vector patternMatchVectorSing = ml.patternMatch ( arguments , false ); //en singular
		Vector patternMatchVectorPlur = ml.patternMatch ( arguments , true ); //en plural


		if ( patternMatchVectorSing.size() > 0 && !(((Mobile)patternMatchVectorSing.elementAt(0)).getDescription(this)).equals("") ) //miramos un objeto
		{
			write( io.getColorCode("description") + ((Mobile)patternMatchVectorSing.elementAt(0)).getDescription(this) + io.getColorCode("reset") + '\n' );
			mirado=true;
		}	
		else if ( patternMatchVectorPlur.size() > 0 )
		{
			mirado=true;
			//no era en singular, probamos en plural.
			Mobile ourMob;
			//mirar todos los items
			for ( int i = 0 ; i < patternMatchVectorPlur.size() ; i++ )
			{
				ourMob = (Mobile)patternMatchVectorPlur.elementAt(i);
				if ( !((Mobile)ourMob).getDescription(this).equals("")  )
				{
					write( '\n' + "Miras " + ourMob.constructName2 ( 1 , this ) + ": " );
					write( io.getColorCode("description") + ((Mobile)ourMob).getDescription(this) + io.getColorCode("reset") + "\n"   );
				}
			}	
		}

		return mirado;

	} //end method mirar item

	//el contenido se mira recursivamente.
	protected boolean mirarContenido ( String arguments , Inventory inv )
	{

		boolean mirado = false;

		if ( inv == null || inv.isEmpty() ) return false;

		for ( int i = 0 ; i < inv.size() ; i++ )
		{
			if ( inv.elementAt(i).isContainer() )
			{
				mirado = mirarItem ( arguments , inv.elementAt(i).getContents() ); //mirar el contenido
				if ( mirado ) break;
			}
		}
		if ( !mirado )
		{
			for ( int i = 0 ; i < inv.size() ; i++ )
			{
				if ( inv.elementAt(i).isContainer() )
				{
					mirado = mirarContenido ( arguments , inv.elementAt(i).getContents() ); //llamada recursiva
					if ( mirado ) break;
				}
			}
		}

		return mirado;

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












	//notaciï¿½n un poco inconsistente con la de Entity::copyEntityFields; pero bueno
	public void copyMobileFieldsTo ( Mobile m )
	{

		m.copyEntityFields(this); //estados, propiedades, etc.

		//Debug.println("Random from SOURCE item: " + getRandom());

		m.aleat = getRandom();
		m.habitacionActual = habitacionActual;
		m.habitacionAnterior = habitacionAnterior;
		m.lenguaje = lenguaje;
		m.movingState_Path = movingState_Path;
		m.mundo = mundo;
		m.PSIanswers = PSIanswers;
		m.PSIkeywords = PSIkeywords;
		m.io = io;
		m.caracteristicas = (Traits) caracteristicas.clone();
		m.exitname = exitname;
		m.extraDescriptions = extraDescriptions;
		m.gender = gender;
		m.idnumber = idnumber; //will have to change the ID later!
		//it.inheritsFrom = 0; //creates an identical (strong inherit) item -> No weak inherit
		//nay, identical copy
		m.inheritsFrom = inheritsFrom;

		if ( inventory != null )
			m.inventory = (Inventory) inventory.clone();
		else m.inventory = null;

		//m.inventoryString = inventoryString;
		//it.isInstanceOf = idnumber; //creates an identical (strong inherit) item
		//nay, identical copy
		m.isInstanceOf = isInstanceOf;
		//m.isVirtual = isVirtual;
		m.mobileType = mobileType;
		m.itsCode = itsCode.cloneIfNecessary(); 

		if ( partsInventory != null )
			m.partsInventory = (Inventory) partsInventory.clone();
		else m.partsInventory = null;

		if ( wieldedWeapons != null )
			m.wieldedWeapons = (Inventory) wieldedWeapons.clone();
		else m.wieldedWeapons = null;

		if ( wieldingLimbs != null )
			m.wieldingLimbs = (Inventory) wieldingLimbs.clone();
		else
			m.wieldingLimbs = null;

		if ( wornItems != null )
			m.wornItems = (Inventory) wornItems.clone();
		else
			m.wornItems = null;	

		if ( virtualInventory != null )
			m.virtualInventory = (Inventory) virtualInventory.clone();
		else
			m.virtualInventory = null;		


		m.respondToPlur = respondToPlur;
		m.respondToSing = respondToSing;
		m.title = title;
		m.hp = hp;
		m.mp = mp;
		m.maxhp = maxhp;
		m.maxmp = maxmp;


		m.descriptionList = new Description[descriptionList.length];
		for ( int i = 0 ; i < m.descriptionList.length ; i++ )
		{
			m.descriptionList[i] = (Description) descriptionList[i].clone();
		}


		m.singNames = new Description[singNames.length];
		for ( int i = 0 ; i < m.singNames.length ; i++ )
		{
			m.singNames[i] = (Description) singNames[i].clone();
		}
		m.plurNames = new Description[plurNames.length];
		for ( int i = 0 ; i < m.plurNames.length ; i++ )
		{
			m.plurNames[i] = (Description) plurNames[i].clone();
		}

		//mob refs and only restrictions are obsolete, do not copy



	}



	//crea un nuevo Mobile que hereda de ï¿½ste y lo aï¿½ade al mundo
	public Mobile createNewInstance( World mundo , boolean cloneInventory , boolean cloneParts , boolean cloneVirtual  ) 
	{
		Mobile it = (Mobile) this.clone();
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



		boolean [][] limb_wielded_relationship_map = null;
		boolean [][] limb_worn_relationship_map = null;

		if ( (cloneParts || cloneInventory)  )
		{
			//make relationship map to solve limbs/weapons cloning problem

			Inventory parts = it.getFlattenedPartsInventory();
			Inventory inv = it.getInventory();

			if ( parts != null && inv != null )
			{

				limb_wielded_relationship_map = new boolean [parts.size()][inv.size()];
				limb_worn_relationship_map = new boolean [parts.size()][inv.size()];

				for ( int i = 0 ; i < parts.size() ; i++ )
				{
					Weapon w = it.getWieldedItem( parts.elementAt(i) );
					//get index of w in inv
					for ( int j = 0 ; j < inv.size() ; j++ )
					{
						if ( inv.elementAt(j).equals(w) )
						{
							limb_wielded_relationship_map[i][j]=true;
						}
					}

					Wearable w2 = it.getWornItem( parts.elementAt(i) );
					//get index of w in inv
					for ( int j = 0 ; j < inv.size() ; j++ )
					{
						if ( inv.elementAt(j).equals(w2) )
						{
							limb_worn_relationship_map[i][j]=true;
						}
					}
				}
			}
		}







		if ( cloneInventory && it.inventory != null )
		{
			//poner inventario de copias
			it.inventory = it.inventory.cloneCopyingItems(mundo,cloneInventory,cloneParts);
		}
		if ( cloneVirtual && it.virtualInventory != null )
		{
			it.virtualInventory = it.virtualInventory.cloneCopyingItems(mundo,cloneVirtual,cloneParts);
		}



		if ( cloneParts && it.partsInventory != null )
		{
			it.partsInventory = it.partsInventory.cloneCopyingItems(mundo,cloneInventory,cloneParts);
		}

		//wearable and wieldable stuff

		/*
		if ( cloneInventory && it.wieldedWeapons != null )
		{
			it.wieldedWeapons = it.wieldedWeapons.cloneCopyingItems(mundo,cloneInventory,cloneParts);
		}
		if ( cloneInventory && it.wornItems != null )
		{
			it.wornItems = it.wornItems.cloneCopyingItems(mundo,cloneInventory,cloneParts);
		}
		 */
		//just clone them, will be emptied and refilled

		it.wieldedWeapons = (Inventory) it.wieldedWeapons.clone();
		it.wornItems = (Inventory) it.wornItems.clone();


		if ( cloneParts || cloneInventory )
		{
			//load relationship map to solve limbs/weapons cloning problem

			Inventory parts = it.getFlattenedPartsInventory();
			Inventory inv = it.getInventory();

			it.wieldedWeapons.empty();
			it.wornItems.empty();

			for ( int i = 0 ; i < parts.size() ; i++ )
			{
				for ( int j = 0 ; j < inv.size() ; j++ )
				{
					//reset wieldings

					try
					{

						Weapon w = it.getWieldedItem ( parts.elementAt(i) );
						if ( w != null )
							parts.elementAt(i).setRelationshipProperty(w,"wields",false);
						if ( limb_wielded_relationship_map[i][j] )
						{
							parts.elementAt(i).setRelationshipProperty(inv.elementAt(j),"wields",true);
							it.wieldedWeapons.addItem(inv.elementAt(j));
							Debug.println("Adding wielded item " + inv.elementAt(j) + " to " + it);
						}
						Wearable w2 = it.getWornItem ( parts.elementAt(i) );
						if ( w2 != null )
							parts.elementAt(i).setRelationshipProperty(w2,"wears",false);
						if ( limb_worn_relationship_map[i][j] )
						{
							parts.elementAt(i).setRelationshipProperty(inv.elementAt(j),"wears",true);
							it.wornItems.addItem(inv.elementAt(j));
							Debug.println("Adding worn item " + inv.elementAt(j) + " to " + it);
						}

					}
					catch ( WeightLimitExceededException wlee )
					{
						wlee.printStackTrace();
					}
					catch ( VolumeLimitExceededException vlee )
					{
						vlee.printStackTrace();
					}

				}
			}
		}

		mundo.addMobileAssigningID ( it );

		return it;
	}

	//respuesta al comando suicidarse
	public void suicide()
	{

		Inventory weaponInv = getUsableWeapons();
		Inventory natInv = getNaturalWeapons();

		for ( int i = 0 ; i < weaponInv.size() ; i++ )
		{
			Weapon current = (Weapon) weaponInv.elementAt(i);
			if ( ! ( natInv.contains(current) ) )
			{
				suicideWith ( current );
				return;
			}
		}
		
		write("No estás blandiendo ningún arma útil para suicidarte.\n");

	}

	public void suicideWith ( Weapon w )
	{
		habitacionActual.reportAction ( this,null,new Entity[]{w} , "$1 se suicida con $3.\n", "Te suicidas con $3.\n", "Te suicidas con $3.\n", true );
		decreaseHP ( getHP() );
	}

	public EntityList getReachableEntities()
	{
		return getReachableEntities(false);
	}

	public EntityList getReachableEntities( boolean includeContainedItems )
	{

		EntityList possibleSpellTargets = new EntityList();

		for ( int i = 0 ; i < habitacionActual.mobsInRoom.size() ; i++ )
		{
			possibleSpellTargets.addEntity ( habitacionActual.mobsInRoom.elementAt(i) );
		}

		Inventory myInv;
		if ( includeContainedItems ) myInv = getFlattenedInventory();
		else myInv = getInventory();
		for ( int i = 0 ; i < myInv.size() ; i++ )
		{
			possibleSpellTargets.addEntity ( myInv.elementAt(i) );
		}

		Inventory roomInv;
		if ( includeContainedItems ) roomInv = habitacionActual.getFlattenedInventory();
		else roomInv = habitacionActual.getInventory();
		for ( int i = 0 ; i < roomInv.size() ; i++ )
		{
			possibleSpellTargets.addEntity ( roomInv.elementAt(i) );
		}

		//add this mobile's parts as well
		Inventory parts = this.getFlattenedPartsInventory();
		for ( int i = 0 ; i < parts.size(); i++ )
		{
			possibleSpellTargets.addEntity( parts.elementAt(i) );
		}

		return possibleSpellTargets;

	}

	/**
	 * 
	 * @param el1 An entity list.
	 * @param el2 Another entity list.
	 * @param arguments Arguments of a string command to be executed.
	 * @return Two parallel vectors containing the pairs of objects (o1,o2) such that o1 belongs to el1, o2 belongs to el2, and the objects pattern-match
	 * with the two arguments in order.
	 */
	protected List[] patternMatchPairs(EntityList el1, EntityList el2, String arguments) {

		List[] result = new ArrayList[2];
		ArrayList list1 = new ArrayList();
		ArrayList list2 = new ArrayList();
		result[0] = list1;
		result[1] = list2;

		if ( el1 == null || el1.isEmpty() || el2 == null || el2.isEmpty() ) return result;


		Vector[] patternMatchVectorSingSing = el1.patternMatchTwo ( el2 , arguments , false , false ); //en singular y singular
		Vector[] patternMatchVectorSingPlur = el1.patternMatchTwo ( el2 , arguments , false , true ); //en singular y plural
		Vector[] patternMatchVectorPlurSing = el1.patternMatchTwo ( el2 , arguments , true , false ); //en plural y singular
		Vector[] patternMatchVectorPlurPlur = el1.patternMatchTwo ( el2 , arguments , true , true ); //en plural y plural

		if ( patternMatchVectorSingSing != null && patternMatchVectorSingSing[0].size() > 0 )
		{
			list1.add( patternMatchVectorSingSing[0].get(0) );
			list2.add( patternMatchVectorSingSing[1].get(0) );
		}

		else if ( patternMatchVectorSingPlur != null && patternMatchVectorSingPlur[0].size() > 0 )
		{
			for ( int i=0 ; i < patternMatchVectorSingPlur[1].size() ; i++ )
			{
				list1.add( patternMatchVectorSingPlur[0].get(0) );
				list2.add( patternMatchVectorSingPlur[1].get(i) );
			}
		}
		else if ( patternMatchVectorPlurSing != null && patternMatchVectorPlurSing[0].size() > 0 )
		{
			for ( int j=0 ; j < patternMatchVectorPlurSing[0].size() ; j++ )
			{
				list1.add( patternMatchVectorPlurSing[0].get(j) );
				list2.add( patternMatchVectorPlurSing[1].get(0) );
			}
		}
		else if ( patternMatchVectorPlurPlur != null && patternMatchVectorPlurPlur[0].size() > 0 )
		{
			for ( int i=0 ; i < patternMatchVectorPlurPlur[0].size() ; i++ )
			{
				for ( int j=0 ; j < patternMatchVectorPlurPlur[1].size() ; j++ )
				{
					list1.add( patternMatchVectorPlurPlur[0].get(i) );
					list2.add( patternMatchVectorPlurPlur[1].get(j) );
				}
			}
		}

		return result;

	}

	/**
	 * @return The SoundClient associated with this Mobile if it's a Player and it has a sound-enabled client. Else, it returns null.
	 */
	private SoundClient getSoundClientIfAvailable()
	{
		if ( !(this instanceof Player) ) return null; //not player, doesn't have an input-output client to play midi
		Player pl = (Player) this;
		InputOutputClient io = pl.getIO();
		if ( !(io instanceof MultimediaInputOutputClient) ) return null; //player is using client without multimedia capabilities
		MultimediaInputOutputClient mio = (MultimediaInputOutputClient) io;
		if ( !mio.isSoundEnabled() ) return null; //sound is disabled in the client
		SoundClient sc = mio.getSoundClient();
		return sc;
	}




	//multimedia
	//TODO: version with exceptions (playMidi) and without (playMidiIfAvailable)
	public boolean playMidiIfAvailable ( String midiFileName , int loopCount )
	{
		SoundClient sc = getSoundClientIfAvailable();
		if ( sc == null ) return false;
		try
		{
			sc.midiInit();
			sc.midiPreload(midiFileName);
			sc.midiOpen(midiFileName);
			if ( loopCount != 0 )
				sc.midiLoop( loopCount );
			else
				sc.midiStart();
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}

	//old version of this method
	private boolean playMidiIfAvailable ( String midiFileName , boolean loop )
	{
		if ( loop )
			return playMidiIfAvailable(midiFileName,-1);
		else
			return playMidiIfAvailable(midiFileName);
	}

	public boolean playMidiIfAvailable ( String midiFileName )
	{
		return playMidiIfAvailable(midiFileName,0);
	}

	public boolean loopMidiIfAvailable ( String midiFileName )
	{
		return playMidiIfAvailable(midiFileName,Sequencer.LOOP_CONTINUOUSLY);
	}

	//multimedia
	public boolean stopMidiIfAvailable (  )
	{
		SoundClient sc = getSoundClientIfAvailable();
		if ( sc == null ) return false;
		try
		{
			sc.midiInit();
			sc.midiStop();
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}


	public boolean playAudioIfAvailable ( String audioFileName )
	{
		SoundClient sc = getSoundClientIfAvailable();
		if ( sc == null ) return false;
		try
		{
			sc.audioStart(audioFileName);
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}

	public boolean playAudioIfAvailable ( String audioFileName , int loopTimes )
	{
		SoundClient sc = getSoundClientIfAvailable();
		if ( sc == null ) return false;
		try
		{
			sc.audioStart(audioFileName,loopTimes);
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}

	public boolean stopAudioIfAvailable ( String audioFileName )
	{
		SoundClient sc = getSoundClientIfAvailable();
		if ( sc == null ) return false;
		try
		{
			sc.audioStop(audioFileName);
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}

	/*
	public boolean playMODIfAvailable ( URL modURL )
	{
	    	SoundClient sc = getSoundClientIfAvailable();
		if ( sc == null ) return false;
		try
		{
			sc.playMOD(modURL,1);
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}
	 */





}