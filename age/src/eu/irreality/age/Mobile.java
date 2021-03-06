/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
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
import eu.irreality.age.language.Mentions;
import eu.irreality.age.matching.Matches;
import eu.irreality.age.messages.Messages;
import eu.irreality.age.util.Conversions;
import eu.irreality.age.scripting.ScriptException;
public class Mobile extends Entity implements Descriptible , SupportingCode , Nameable, UniqueNamed
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



	/**Habitaci�n actual*/
	protected Room habitacionActual;
	/**Habitaci�n anterior*/
	protected Room habitacionAnterior;



	/**Nombre sint�tico del bicho.*/
	/*04*/ protected String title;

	/**Es instancia de.*/
	/*05*/ private String isInstanceOf;

	/**Lista din�mica de descripciones.*/
	/*10*/ protected Description[] descriptionList;
	/**Lista din�mica de nombres en singular. (goblin, goblin herido... mismo goblin, varios estados)*/
	/*11*/ protected Description[] singNames;
	/**Lista din�mica de nombres en plural.*/
	/*12*/ protected Description[] plurNames;
	/**G�nero.*/
	/*13*/ protected boolean gender; //true=masculino
	/**Responder a... (comandos): listas de pattern-matching.*/
	/*14*/ protected List respondToSing = new ArrayList();
	/*15*/ protected List respondToPlur = new ArrayList();


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

	/**bit vector (o bits, a secas, m�s bien)*/
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

	/**C�digo en Ensamblador Virtual Aetheria (EVA)*/
	/*80*/ protected ObjectCode itsCode;

	/**Especificaciones de habla PSI*/
	/*82*/
	protected Vector PSIanswers; //vector of Description[]s	
	protected Vector PSIkeywords; //vector or Strings


	//initted in constructor
	protected InputOutputClient io /*= new InputOutputClientNula()*/; //no hace nada para los bichos que no sean Informadores


	String exitname; //nombre de la salida hacia la que est� yendo, si est� en estado "go"

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
		mentions.setLastMentionedVerb(lenguaje.getDefaultVerb());
		constructMob ( mundo , mobfile , true , "none" );
		//el constructor de cada subclase de Mobile llamar� con esa subclase en vez de none:
		//public Daedra har�
		//constructMob ( mundo , mobfile , true , "daedra" );
	}

	public Mobile ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		this.mundo = mundo;
		io = new NullInputOutputClient();
		lenguaje = mundo.getLanguage();
		mentions.setLastMentionedVerb(lenguaje.getDefaultVerb());
		constructMob ( mundo , n , true , "none" );
		//el constructor de cada subclase de Mobile llamar� con esa subclase en vez de none:
		//public Daedra har�
		//constructMob ( mundo , mobfile , true , "daedra" );
	}

	/**
	 * Constructor para ser llamado desde fuera, construye todas las subclases de Mobile seg�n el fichero
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

	protected static String firstWord(String s) {
	    StringTokenizer st = new StringTokenizer(s);
	    if ( st.hasMoreTokens() ) return st.nextToken();
	    else return "";
	}

	private static String restWords(String s) {
	    StringTokenizer st = new StringTokenizer(s);
	    if ( !st.hasMoreTokens() ) return "";
	    else
	    {
		st.nextToken();
		if ( !st.hasMoreTokens() ) return "";
		else return st.nextToken("");
	    }
	}

	/**
	 * El pedazo de constructor que lee un bicho de un fichero.
	 *
	 */
	public void constructMob ( World mundo , String mobfile , boolean allowInheritance , String mobtype ) throws IOException, FileNotFoundException
	{
	    
	    this.setProperty("describeRoomsOnArrival" , true); //defaults to true
	    this.setPropertyTimeLeft("describeRoomsOnArrival", -1);
	    
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
				/*la herencia fuerte se hace exactamente igual que la d�bil, no pueden aparecer las dos (se ignorar�a la 2a)*/
				isInstanceOf = linea; 
				if ( Integer.valueOf(isInstanceOf).intValue() < idnumber && allowInheritance ) /*el item del que heredamos debe tener ID menor*/
				{
					/*construimos segun constructor de la habitacion de que heredamos*/
					constructMob ( mundo, Utility.mobFile(mundo,Integer.valueOf(isInstanceOf).intValue()) , true , mobtype ); 
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
				respondToSing = Conversions.getReferenceNameList(linea); break;
			}
			case 15:
				//respondToPlur
			{
				respondToPlur = Conversions.getReferenceNameList(linea); break;	
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
	    this.setPropertyTimeLeft("describeRoomsOnArrival", -1);
	    
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
		if ( e.hasAttribute("extends") && !e.getAttribute("extends").equals("0") && !e.getAttribute("extends").equals("null") && allowInheritance )
		{
			//item must extend from existing item.
			//clonamos ese item y overrideamos lo overrideable
			//(n�tese que la ID del item extendido ha de ser menor).

			//por eso los associated nodes de los items quedan guardados [por ref] en el World hasta que
			//haya concluido la construccion del mundo

			//1. overrideamos el super-item usando su associated node para construirlo

			constructMob ( mundo , mundo.getMobileNode( e.getAttribute("extends") ) , true , mobtype );

			//2. overrideamos lo que debamos overridear

			constructMob ( mundo , n , false , mobtype );
		}

		//strong inheritance?
		if ( e.hasAttribute("clones") && !e.getAttribute("clones").equals("0") && !e.getAttribute("clones").equals("null") && allowInheritance )
		{
			//funciona igual que la weak inheritance a este nivel.
			//no deberian aparecer los dos; pero si asi fuera esta herencia (la fuerte) tendria precedencia.

			//1. overrideamos el super-item usando su associated node para construirlo

			constructMob ( mundo , mundo.getMobileNode( e.getAttribute("clones") ) , true , mobtype );

			isInstanceOf = e.getAttribute("clones");
			
			return;
			
			//2. overrideamos lo que debamos overridear
			//constructMob ( mundo , n , false , mobtype );
		}

		//mandatory XML-attribs exceptions
		//if ( !e.hasAttribute("id") )
		//	throw ( new XMLtoWorldException ( "Mobile node lacks attribute id" ) );
		if ( !e.hasAttribute("name") )
			throw ( new XMLtoWorldException ( "Mobile node lacks attribute name" ) );
		if ( !e.hasAttribute("hp") )
			throw ( new XMLtoWorldException ( "Mobile node lacks attribute hp" ) );		
		if ( !e.hasAttribute("maxhp") )
			throw ( new XMLtoWorldException ( "Mobile node lacks attribute maxhp" ) );
		if ( !e.hasAttribute("gender") )
			throw ( new XMLtoWorldException ( "Mobile node lacks attribute gender" ) );	

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
			throw ( new XMLtoWorldException ( "Bad number format at attribute hp in mobile node" ) );
		}
		try
		{
			maxhp = Integer.valueOf ( e.getAttribute("maxhp") ).intValue();
		}
		catch ( NumberFormatException nfe )
		{
			throw ( new XMLtoWorldException ( "Bad number format at attribute maxhp in mobile node" ) );
		}
		if ( e.hasAttribute("mp") )
		{
			try
			{
				mp = Integer.valueOf ( e.getAttribute("mp") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				throw ( new XMLtoWorldException ( "Bad number format at attribute mp in mobile node" ) );
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
				throw ( new XMLtoWorldException ( "Bad number format at attribute maxmp in mobile node" ) );
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
		//NO SE NECESITA PONER UN ROOM. SE INICIALIZA AL METER AL BICHO EN LA HABITACI�N (AL CARGAR HABITACI�N)
		//(ROOMS SE CARGAN MAS TARDE QUE MOBILES)

		//A LOS BICHOS DAIO'S (DYNAMICALLY-ASSIGNED-ID OBJECTS) LOS PONEMOS EN LA HABITACION
		//QUE INDICA SU CAMPO HABITACIONACTUAL.

		boolean roomsdone=true;

		try
		{

			mundo.getRoom(0); //si esto da NullPointerException es que las habitaciones 
			//no est�n inicializadas.
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
			createReferencesInInventoryItems();
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
			System.err.println("[Warning] Mobile " + this.getUniqueName() + " carrying items too heavy or big for its strength of " + strength);
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

		//correcci�n inventarios paralelos (copiada del otro, �dobla innecesariamente?)
		if ( wieldingLimbs != null && wieldedWeapons == null )
		{
			wieldedWeapons = new Inventory(10000,10000,wieldingLimbs.size());
			wieldedWeapons.incrementSize(wieldingLimbs.size());
		}

		if ( wornItems == null )
		{
			wornItems = new Inventory(10000,10000);
		}

		//from 2014-10-22, onInit() is not executed when loading states, see issue #310
		if ( !mundo.comesFromLoadedState() )
		{
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
				mundo.write("BeanShell error on initting mobile " + this + ": error was " + te);
				mundo.writeError(ExceptionPrinter.getExceptionReport(te));
			}
		}

	}
	
	
	/**
	 * Used when contained items are put inside of a container on world load, to set the references from those items to
	 * their container.
	 */
	private void createReferencesInInventoryItems()
	{
		for ( int i = 0 ; i < inventory.size() ; i++ )
		{
			((Item)inventory.get(i)).addMobileReference(this);
		}
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
	

	/**
	 * @deprecated Use {@link #getClient()} instead
	 */
	public InputOutputClient getIO ( )
	{
		return getClient();
	}

	public InputOutputClient getClient ( )
	{
		return io;
	}
	
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

	/**
	 * @deprecated Use getName ( boolean s_p , Entity viewer ) instead.
	 * @param s_p
	 * @return
	 */
	public String getName ( boolean s_p )
	{
		Description[] theList;
		if ( s_p ) theList = singNames;
		else theList = plurNames;
		String desString="";
		for ( int i = 0 ; i < theList.length ; i++ )
		{
			if ( theList[i].matchesConditions(this,(Entity)null) ) //oh wow, casting null
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
	
	
	/**
	 * Obtains an extra description, representing a description of a particular component, feature or aspect of this Item.
	 * This method's implementation has been changed as of 2012-12-01 to match in a more useful way, similar to the Entity moderateMatchesCommand() method.
	 * @param requestedName The name to match against descriptions (e.g. typed by the player)
	 * @param viewer The Entity for which the description will be customized.
	 * @return A description for the component/feature/aspect named requestedName, or null if no such component exists.
	 */
	public String getExtraDescription ( String requestedName , Entity viewer )
	{
		if ( requestedName == null || requestedName.length() == 0 ) return null; 
		for ( int i = 0 ; i < extraDescriptionNameArrays.size() ; i++ )
		{
			String[] curNameArray = (String[]) extraDescriptionNameArrays.get(i);
			Description[] curDesArray = (Description[]) extraDescriptionArrays.get(i);
			for ( int j = 0 ; j < curNameArray.length ; j++ )
			{
				String currentReferenceName = curNameArray[j];
				int position = requestedName.toLowerCase().indexOf(currentReferenceName.toLowerCase());
				if ( position < 0 ) //does not match
					continue;
				if ( position != 0 && !Character.isWhitespace(requestedName.charAt(position-1)) ) //matches but starts at a place other than beginning/whitespace
					continue;
				if ( position+currentReferenceName.length() != requestedName.length() && !Character.isWhitespace(requestedName.charAt(position+currentReferenceName.length())) ) //matches but ends at a place other than end/whitespace
					continue;
				//if we have reached this point, the match is acceptable
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
		//nothing found
		return null;
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

	/*
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
	*/

	/*
	 * Returns the most specific name with which we can refer to an object in a command.
	 * Useful for pronoun substitution.
	 */
	public String getBestReferenceName ( boolean pluralOrSingular )
	{
		List theList;
		if ( pluralOrSingular ) //true? plural
			theList = respondToPlur;
		else
			theList = respondToSing;
		if ( theList.size() == 0 ) return null;
		String tmp = (String) theList.get(0);
		return ( Character.toLowerCase( tmp.charAt(0) ) ) + tmp.substring(1);	
	}


	/**
	 * Nos dice si se da por aludido el objeto ante un comando (ej. "mirar piedra") -> se pasa "piedra")
	 *
	 * @param commandArgs Los argumentos del comando.
	 * @param pluralOrSingular 1 si plural.
	 * @return 0 si no se da por aludido, 1 si s�, y con qu� prioridad. (+ n� = - prioridad). La prioridad es el orden que ocupa el nombre que se corresponde con el comando dado en la lista de nombres.
	 */
	public int matchesCommand ( String commandArgs , boolean pluralOrSingular )
	{
		List listaDeInteres;
		if ( pluralOrSingular ) // plural
			listaDeInteres = respondToPlur;
		else
			listaDeInteres = respondToSing;
		return matchesCommand ( commandArgs , listaDeInteres , mundo.getCommandMatchingMode() );
	}

	public String getInstanceOf ( )
	{
		return isInstanceOf;
	}

	
	/**
	 * Returns true if viewer cannot distinguish this mobile from other.
	 * This is used to collapse descriptions, e.g. to show "two dogs" rather than "a dog and a dog".
	 * @param other The mobile that we want to know whether it is indistinguishable or not from this one.
	 * @param viewer The viewer for which we want to know if mobiles are indistinguishable.
	 * @return
	 */
	public boolean isIndistinguishableFrom ( Mobile other , Entity viewer )
	{
		String plurName1 = this.getPlurName(viewer);
		String plurName2 = other.getPlurName(viewer);
		if ( plurName1 != null && plurName1.trim().length() > 0 
			&& plurName2 != null && plurName2.trim().length() > 0 )
			return plurName1.equals(plurName2);
		else
			return isSame ( other );
	}
	
	public boolean isSame ( Mobile other )
	{
		//old
		/*
		return (  
				( idnumber == other.getID() )
				||	( idnumber == other.getInstanceOf () ) 
				|| ( isInstanceOf == other.getID () )
				|| ( isInstanceOf == other.getInstanceOf () && isInstanceOf != 0 )  );
		*/
		
		//new
		return ( 
				this.getUniqueName().equals(other.getInstanceOf()) 
			||	other.getUniqueName().equals(this.getInstanceOf())
			||  (
					this.getInstanceOf() != null &&
					this.getInstanceOf().equals(other.getInstanceOf()) &&
					!StringMethods.isStringOfZeroes(this.getInstanceOf())
				) );
		
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
		Las relaciones con estado entre m�viles y otros objetos (o tal vez, en el
		futuro, entre entidades en general, si hiciera falta?) resultan �tiles.
		Gracias a ellas, un jugador puede, por ejemplo, "recordar" lo que hab�a
		en una habitaci�n: al mirarla se establece un estado 1 (por ejemplo) en su
		relaci�n con esa entidad habitaci�n, de tal modo que cuando vuelve a ella
		ya no tiene que "buscar" algo sino que lo ve directamente, por ejemplo.

		No para cambios objetivos en la habitaci�n que ver�a cualquiera (como el
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
	//esto es para carga DIFERIDA!! Son entitys, no sabemos si de las que se cargan antes o despu�s.
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
				//lista de propiedades de la relaci�n
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
				//lista de propiedades de la relaci�n
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

			//Aqu� es donde hay que a�adir el soporte de Regular Expressions.
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
	Una posible cosa a hacer con respecto a esta funci�n es usar el "escribir" como caso particular
	de ella en los Informadores, (if instanceof informador then escribir, p.ej.) y de este modo
	cambiar las funciones informAction para que en realidad s�lo hagan a los Mobiles ejecutar
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
		catch ( ScriptException te )
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
			sepvector.addElement(" te dice "); //n�tese que este token se mira antes.
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



			Vector patternMatchVectorSing = habitacionActual.mobsInRoom.patternMatch ( elSujeto , false ).toEntityVector(); //en singular
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
				//primera tokenizaci�n: elSujeto juan, resto "hola" a pedro
				//segunda: loQueDice hola, resto a pedro

				Debug.println("Resto's string: " + resto);

				Vector patternMatchVectorSing2 = habitacionActual.mobsInRoom.patternMatch ( resto , false ).toEntityVector(); //en singular
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

				//onSay con par�metro objeto

				ejecutado=false;
				ejecutado = execCode ( "onSayTo" ,
						new Object[]
						           {
						elSujetoEnSi , loQueDice , elObjetoEnSi
						           }
				);

				if ( ! ejecutado )
				{
					//onSay sin par�metro objeto

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
				mundo.write(io.getColorCode("error") + "EVASemanticException found at event_say, mob number " + getID() + io.getColorCode("reset") );
			}
			catch ( ScriptException te )
			{
				//mundo.write(io.getColorCode("error") + "bsh.TargetError found at event_say, mob number " + getID() + io.getColorCode("reset") );
				mundo.writeError(ExceptionPrinter.getExceptionReport(te,"onSay, mobile " + this));
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
		if ( text.trim().equals("") ) return;
		//habitacionActual.reportActionAuto ( this , null , null , "$1 dice \"" + text + "\".\n" , true );
		habitacionActual.reportAction ( this , null , null , mundo.getMessages().getMessage("someone.says.something","$text",text) , mundo.getMessages().getMessage("someone.says.something","$text",text) , mundo.getMessages().getMessage("you.say.something","$text",text)  , true );
	}
	
	public void say ( String text , String style )
	{
		if ( text.trim().equals("") ) return;
		//habitacionActual.reportActionAuto ( this , null , null , "$1 dice \"" + text + "\".\n" , style , true );
		habitacionActual.reportAction ( this , null , null , mundo.getMessages().getMessage("someone.says.something","$text",text) , mundo.getMessages().getMessage("someone.says.something","$text",text) , mundo.getMessages().getMessage("you.say.something","$text",text)  ,  style , true );
	}

	public void sayTo ( Mobile m , String text )
	{
		//habitacionActual.reportAction ( this , m , null , "$1 dice \"" + text + "\" a $2.\n" , "$1 te dice \"" + text + "\".\n" , "dices \"" + text + "\" a $2.\n" , true );
		habitacionActual.reportAction ( this , m , null , mundo.getMessages().getMessage("someone.tells.someone.something","$text",text) , mundo.getMessages().getMessage("someone.tells.you.something","$text",text) , mundo.getMessages().getMessage("you.tell.someone.something","$text",text) , true );
	}
	
	public void sayTo ( Mobile m , String text , String style )
	{
		//habitacionActual.reportAction ( this , m , null , "$1 dice \"" + text + "\" a $2.\n" , "$1 te dice \"" + text + "\".\n" , "dices \"" + text + "\" a $2.\n" , style , true );
		habitacionActual.reportAction ( this , m , null , mundo.getMessages().getMessage("someone.tells.someone.something","$text",text) , mundo.getMessages().getMessage("someone.tells.you.something","$text",text) , mundo.getMessages().getMessage("you.tell.someone.something","$text",text) , style , true );
	}

	//	procesa el comando decir
	protected void comandoDecir ( String args )
	{

		//si hay comillas, distinguimos lo de dentro de lo de fuera

		StringTokenizer st = new StringTokenizer ( " " + args , "\"" );
		//el espacio a�adido a args en la l�nea enterior es para que el primer token est� siempre fuera
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

		if ( dentro.equalsIgnoreCase("") ) //no hab�a comillas
		{
			//interpretaci�n en este caso:
			//decir hola a jorge -> hablas a todos
			//decir hola -> hablas a todos
			//ergo, no tenemos que buscar bichos (de momento)

			say ( fuera );

		}

		else //hab�a comillas
		{
			//interpretaci�n:
			//decir "hola" -> a todos
			//decir "hola" a jorge, decir a jorge "hola" -> solo a jorge
			//ergo, tenemos que buscar en fuera a ver si hay un bicho y decirle dentro.

			MobileList ml = getRoom().getMobiles();
			Vector patternMatchVectorSing = ml.patternMatch ( fuera , false ).toEntityVector(); //en singular
			Vector patternMatchVectorPlur = ml.patternMatch ( fuera , true ).toEntityVector(); //en plural

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
			String str = inventory.toString(this,mundo);
			if ( str.equalsIgnoreCase(mundo.getMessages().getMessage("nothing")+".") ) write( io.getColorCode("information") + mundo.getMessages().getMessage("you.have.nothing",new Object[]{this}) + io.getColorCode("reset") );
			else
			{
				write( io.getColorCode("information") + 
						/*"Tienes "*/  mundo.getMessages().getMessage("you.have.items","$inventory",str.substring(0,str.length()-1),new Object[]{this})
						//+ str 
						+ io.getColorCode("reset") );

				Inventory limbs = getFlattenedPartsInventory();
				//cosas que blandes
				Inventory wieldedWeapons = getWieldedWeapons(); //this shadows the homonymous attribute
				Set alreadyShown = new HashSet(); //para prevenir que armas se muestren dos veces si se llevan en varios miembros (is this really necessary?)
				if ( wieldedWeapons != null )
					for ( int i = 0 ; i < wieldedWeapons.size() ; i++ )
					{
						if ( wieldedWeapons.elementAt(i) != null )
						{
							Item arma = wieldedWeapons.elementAt(i);
							
							if ( alreadyShown.contains(arma) ) continue; //this wielded item was already shown (worn in several limbs)
							else alreadyShown.add(arma);
							
							Vector miembrosOcupados = new Vector();
							//buscar miembros que blanden el arma
							for ( int j = 0 ; j < limbs.size() ; j++ )
							{
								Item miembro = limbs.elementAt(j);
								if ( miembro.getRelationshipPropertyValueAsBoolean(arma,"wields") )
								{
									miembrosOcupados.add(miembro);
									
								//	write( io.getColorCode("information") + 
									//"Blandes " + arma.constructName2OneItem(this)  + " en " + miembro.constructName2OneItem(this) + ".\n" 
								//	mundo.getMessages().getMessage("you.are.wielding.item","$item",arma.constructName2OneItem(this),"$limbs",miembro.constructName2OneItem(this),new Object[]{this,arma,miembro})
								//	+ io.getColorCode("reset") );
									//break;
								}
							}
							//output
							String toOutput="";
							for ( int j = 0 ; j < miembrosOcupados.size() ; j++ )
							{
								Item limb = (Item)miembrosOcupados.get(j);
								if ( j == 0 )
									toOutput += limb.getOutputNameThe(this);
								else if ( j > 0 && j == miembrosOcupados.size() - 1 )
									toOutput += " y " + limb.getOutputNameThe(this);
								else
									toOutput += ", " + limb.getOutputNameThe(this);
							}
							write( io.getColorCode("information") + 
							mundo.getMessages().getMessage("you.are.wielding.item","$item",arma.getOutputNameThe(this),"$limbs",toOutput,new Object[]{this,arma,toOutput})
							+ io.getColorCode("reset") );
							
							//old:
							/*
							write( io.getColorCode("information") + 
									//"Blandes " + arma.constructName2OneItem(this)  + " en " + miembro.constructName2OneItem(this) + ".\n" 
									mundo.getMessages().getMessage("you.are.wielding.item","$item",arma.constructName2OneItem(this),"$limbs",miembro.constructName2OneItem(this),new Object[]{this,arma,miembro})
									+ io.getColorCode("reset") );
								//break;
							 */
						}
					}
				//cosas que llevas puestas
				Inventory wornItems = getWornItems(); //this shadows the homonymous attribute
				alreadyShown = new HashSet(); //para prevenir que prendas se muestren dos veces si se llevan en varios miembros
				if ( wornItems != null )
					for ( int i = 0 ; i < wornItems.size() ; i++ )
					{
						if ( wornItems.elementAt(i) != null )
						{
							Item vestido = wornItems.elementAt(i);
							
							if ( alreadyShown.contains(vestido) ) continue; //this worn item was already shown (worn in several limbs)
							else alreadyShown.add(vestido);
							
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
									toOutput += limb.getOutputNameThe(this);
								else if ( j > 0 && j == miembrosOcupados.size() - 1 )
									toOutput += " y " + limb.getOutputNameThe(this);
								else
									toOutput += ", " + limb.getOutputNameThe(this);
							}
							write( io.getColorCode("information") + 
							//"Llevas " + vestido.constructName2OneItem(this)  + " en " + toOutput + ".\n"
							lenguaje.correctMorphology(
							mundo.getMessages().getMessage("you.are.wearing.item","$item",vestido.getOutputNameThe(this),"$limbs",toOutput,new Object[]{this,vestido,toOutput})
							)
							+ "\n" + io.getColorCode("reset") );
						}
					}
			}
		}
		else write( io.getColorCode("information") + mundo.getMessages().getMessage("you.have.nothing",new Object[]{this}) + io.getColorCode("reset") );
	}


	public void showInventory()
	{

		//ejecutar descripci�n (pesa poquito, etc. etc.)
		boolean execced = false;
		try
		{
			execced = this.execCode("showInventory",new Object[] {} );
		}
		catch ( ScriptException te )
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
		//Los que s� lo sean ya lo overrridear�n.
		//Lo ponemos como m�todo de conveniencia, para poder llamarlo, por ejemplo,
		//cuando se mueva cualquier bicho; pero que s�lo el jugador informe de ello.
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
		if ( mundo.isDebugMode() )
		{
			System.err.print(s);
		}
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
		if ( nsal < 0 ) return false; //no hay salida v�lida.
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


	//ir a la habitaci�n dada contigua a la actual
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
	/**Comando actual*/
	protected String commandstring;
	/**Comando actual*/
	protected String command;
	/**Comando actual*/
	protected String arguments;
	/**Cola para los comandos*/
	protected Vector commandQueue = new Vector();
	/**Variables del parser de strings: zonas de referencia*/
	protected Mentions mentions = new Mentions();
	/**�Estamos ejecutando un comando forzado?*/
	protected boolean forced;
	protected String force_string;
	/**�Estamos en la segunda oportunidad? (nos hemos improvisado el verbo anterior tras no reconocer el primero)*/
	protected boolean secondChance;
	/**Flag de control para indicar que tenemos que activar el second-chance al leer de la cola*/
	protected boolean nextCommandSecondChance;
	protected boolean matchedOneEntity = false;
	protected boolean matchedTwoEntities = false;
	protected boolean matchedOneEntityPermissive = false;
	protected boolean matchedTwoEntitiesPermissive = false;
	protected int oneEntityPriority = 0;
	Weapon lastAttackWeapon = null;
	Mobile lastAttackedEnemy = null;
	Weapon lastBlockWeapon = null;
	Mobile lastBlockedEnemy = null;
	protected Vector finalExecutedCommandLog = new Vector();
	private EntityList mobilesCache;




	//ir por la salida dada. Legacy.
	public boolean go ( Path p ) //throws java.io.IOException
	{

		Debug.println("EXECCING GO " + p );

		if ( !p.isValid() )
		{

			//si no es un Informador, esto no hace nada.
			if ( p.isStandard() ) write( io.getColorCode("denial") + 
					mundo.getMessages().getMessage("go.noexit",new Object[]{this,p})  //"No parece haber salida en esa direcci�n.\n" 
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
			catch ( ScriptException bshte )
			{
				//writeError( io.getColorCode("error") + "bsh.TargetError found onExitRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
				//writeError(ExceptionPrinter.getExceptionReport(bshte));
				writeError(ExceptionPrinter.getExceptionReport(bshte,"onExitRoom , room " + habitacionActual));
			}

			if ( !endfound )
			{

				//sinonimo del anterior
				try
				{	
					endfound = habitacionActual.execCode("beforeExit" , new Object[] {this,p} );
					Debug.println("EXECCING GO: ENDFOUND ASSIGN " + endfound);
				}
				catch ( ScriptException bshte )
				{
					//writeError( io.getColorCode("error") + "bsh.TargetError found onExitRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
					//writeError(ExceptionPrinter.getExceptionReport(bshte));
					writeError(ExceptionPrinter.getExceptionReport(bshte,"beforeExit , room " + habitacionActual));
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

					return false; //se devuelve fracaso (el jugador no sale); la descripci�n la dar� la salida en funci�n del estado cerrado.
				}

			}
			else
			{
				return true; //el c�digo se encarg� de todo.
			}


		}
	}


	private static boolean isDecisionState ( int state )
	{
		return ( state == IDLE || state == MOVING || state == ATTACK_RECOVER || 
				state == DAMAGE_RECOVER || state == BLOCK_RECOVER || state == DODGE_RECOVER || state == SURPRISE_RECOVER );
	}
	
	/**
	 * Returns true if this creature is in a state where it can make a decision and execute a command.
	 * @return
	 */
	public boolean isInDecisionState()
	{
		return isDecisionState(getState());
	}


	public void changeState( World mundo )
	{

		Debug.println(this + " state " + getState() + "TUL" + this.getPropertyTimeLeft("state"));

		//if there is a command enqueued, execute it
		if ( isInDecisionState() )
		{
			try
			{
				if ( obtainAndExecCommand(mundo) ) return;
			}
			catch ( Exception e )
			{
				writeError("I/O Exception on Mobile changeState(). This shouldn't happen. Only players should throw such an exception!");
			}
		}
		
		//else, the AI acts
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
						if ( habitacionActual.hasMobile ( m ) && m.getState() == MOVING ) //si el enemigo esta alcanzable y se est� yendo
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

							boolean success2 = doWield ( w , true );

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
			{
				setNewState(1 /*IDLE*/,2); //fixes regression bug (enemies wouldn't attack you).
			}

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
				write( io.getColorCode("error") + "EVASemanticException found at event_exitroom , room number " + habitacionActual.getID() + io.getColorCode("reset") );
			}
			catch ( ScriptException bshte )
			{
				//writeError( io.getColorCode("error") + "bsh.TargetError found onExitRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
				//writeError(ExceptionPrinter.getExceptionReport(bshte));
				writeError(ExceptionPrinter.getExceptionReport(bshte,"onExitRoom , room " + habitacionActual));
			}

			habitacionActual.reportAction(this,null,"$1 se va hacia " + exitname + ".\n" , null , null , false );	

			setRoom ( mundo.getRoom(getTarget()) );

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
						current.write("Ibas a dirigirte hacia " + current.exitname + "; pero te ves sorprendido por la aparici�n de " + this.constructName2OneItem(current) + ", que te bloquea el paso.\n");
						this.write("Has sorprendido a " + current.constructName2OneItem(this) + ", que parec�a querer dirigirse hacia " + current.exitname + ".\n");
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
			catch ( ScriptException bshte )
			{
				//write( io.getColorCode("error") + "bsh.TargetError found onEnterRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
				writeError(ExceptionPrinter.getExceptionReport(bshte,"onEnterRoom, room " + habitacionActual));
			}

			//-> si hay Mobiles, pueden reaccionar tambi�n a que entres (onEnterRoom de Mobile)
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
					catch ( ScriptException bshte )
					{
						//write( io.getColorCode("error") + "bsh.TargetError found onEnterRoom , mobile number " + bichoActual.getID() + ": " + bshte + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(bshte,"onEnterRoom, mobile " + bichoActual));
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

		/*
		for ( int i = 0 ; i < getEnemies().size() ; i++ )
		{
			if ( getEnemies().elementAt(i).getID() == getTarget() )
			{
				objetivo = getEnemies().elementAt(i);
				break;
			}
		}
		*/
		//as of 2013-02-28, this method doesn't require that the target is an enemy.
		//we are managing an attack that has started, so such checks should have been made when the attack was started, if relevant.
		//sometimes they might not even be relevant, if the game programmers launch a single attack (which doesn't create an enmity) from code. 
		objetivo = mundo.getMobile( getTarget() );

		if ( objetivo == null )
		{
			//An attack has been launched on a Mobile that does not exist in the world - this is either a world programmer error, or an internal error.
			mundo.writeError( "Error: " + this + " was attacking, but has no target to attack - attack() method invoked with invalid arguments?\n" );
		}
		
		if ( !objetivo.getRoom().equals(this.getRoom()) )
		{
			//The attacked Mobile exists, but is not present in this room. This may be because it moved or because it's dead (and has been moved to Limbo).

			if ( objetivo.getState() == Mobile.DEAD )
			{
				//The attacked Mobile isn't here because he's dead. This can happen e.g. because two opponents
				//were attacking him, the first attack killed him and the second attack (this one) lands when
				//he's already dead.
				habitacionActual.reportAction(this, null, null,
						mundo.getMessages().getMessage("someone.attacks.dead",new Object[]{this} ) , 
						mundo.getMessages().getMessage("someone.attacks.you.dead",new Object[]{this} ) , 
						mundo.getMessages().getMessage("you.attack.dead",new Object[]{this} ) , 
						true);
			}
			else
			{
				//The attacked Mobile isn't here due to another reason (e.g. fleed like a chicken).
				habitacionActual.reportAction(this, null, null,
						//"$1 interrumpe su ataque ante la ausencia de su contrincante.\n", 
						//"$1 vacila ante tu ausencia.\n", 
						//"Te dispon�as a atacar; pero vacilas ante la ausencia de tu enemigo.\n", 
						mundo.getMessages().getMessage("someone.attacks.absent",new Object[]{this} ) , 
						mundo.getMessages().getMessage("someone.attacks.you.absent",new Object[]{this} ) , 
						mundo.getMessages().getMessage("you.attack.absent",new Object[]{this} ) , 
						true);
			}
			
			setNewState ( IDLE , 1 );


			return;
		}

		if ( objetivo.getState() != READY_TO_BLOCK /* && objetivo.getState() != BLOCKING */
				&& objetivo.getState() != READY_TO_DODGE /* && objetivo.getState() != DODGING */) 
		{

			//ataque no bloqueado

			if ( getAttackSuccessFromProbability(getCurrentWeapon()) )
			{

				//attack successful


				//solo hay un caso especial en que no se hace da�o: choque de armas en el aire
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
					catch ( ScriptException te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at infoChoqueArmas(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejec )
						habitacionActual.reportAction ( this , objetivo , null , 
								//"Las armas de $1 y $2 chocan en el aire...\n" , 
								//"Tu arma y la de $1 chocan en el aire...\n" , 
								//"Tu arma y la de $2 chocan en el aire...\n" , 
								mundo.getMessages().getMessage("someone.clashes.someone",new Object[]{this,objetivo} ) , 
								mundo.getMessages().getMessage("enemy.clashes.you",new Object[]{this,objetivo} ) , 
								mundo.getMessages().getMessage("you.clash.enemy",new Object[]{this,objetivo} ) , 
								true );

					setNewState ( ATTACK_RECOVER , generateAttackRecoverTime(getCurrentWeapon()) );
					objetivo.setNewState ( ATTACK_RECOVER , objetivo.generateAttackRecoverTime(objetivo.getCurrentWeapon()) );

				}


				else
				{
					//se hace da�o

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
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoNoTiempoBloquear(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
							habitacionActual.reportAction ( this , objetivo , null ,
									null , 
									//"No te da tiempo a bloquear el ataque de $1...\n" , 
									//"A $2 no le da tiempo a bloquear tu ataque...\n" , 
									mundo.getMessages().getMessage("you.fail.block.time.someone",new Object[]{this,objetivo} ) , 
									mundo.getMessages().getMessage("someone.fails.block.time.you",new Object[]{this,objetivo} ) , 
									true );

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
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoNoTiempoEsquivar(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
							habitacionActual.reportAction ( this , objetivo , null ,
									null , 
									//"No te da tiempo a esquivar el ataque de $1...\n" , 
									//"A $2 no le da tiempo a esquivar tu ataque...\n" , 
									mundo.getMessages().getMessage("you.fail.dodge.time.someone",new Object[]{this,objetivo} ) , 
									mundo.getMessages().getMessage("someone.fails.dodge.time.you",new Object[]{this,objetivo} ) , 
									true );

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
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							if ( numeric_damage )
							{
								habitacionActual.reportAction ( this , objetivo ,
										mundo.getMessages().getMessage("someone.hits.someone.numeric","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.you.numeric","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.enemy.numeric","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										//"$1 acierta a $2 con " + getCurrentWeapon().constructName2OneItem() + " infligi�ndole " + danyo + " puntos de da�o...\n" ,
										//"$1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligi�ndote " + danyo + " puntos de da�o...\n" ,
										//"Aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + " infligi�ndole " + danyo + " puntos de da�o...\n" ,
										true );
							}
							else
							{
								habitacionActual.reportAction ( this , objetivo ,
										mundo.getMessages().getMessage("someone.hits.someone","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.you","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.enemy","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										//"$1 acierta a $2 con " + getCurrentWeapon().constructName2OneItem() + " infligi�ndole " + objetivo.estimateDamage(danyo) + "...\n" ,
										//"$1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligi�ndote " + objetivo.estimateDamage(danyo) + "...\n" ,
										//"Aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + " infligi�ndole " + objetivo.estimateDamage(danyo) + "...\n" ,
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
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
							habitacionActual.reportAction ( this , objetivo ,
									mundo.getMessages().getMessage("someone.hits.someone.nodamage","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
									mundo.getMessages().getMessage("enemy.hits.you.nodamage","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
									mundo.getMessages().getMessage("you.hit.enemy.nodamage","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
									//"$1 acierta a $2 con " + getCurrentWeapon().constructName2OneItem() + " pero no le hace da�o...\n" ,
									//"$1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " pero no te hace da�o...\n" ,
									//"Aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + " pero no le haces da�o...\n" ,
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
				catch ( ScriptException te )
				{
					write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
					writeError(ExceptionPrinter.getExceptionReport(te));
				}
				if ( !ejec )
				{
					habitacionActual.reportAction ( this , objetivo , 
							mundo.getMessages().getMessage("someone.misses.someone","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
							mundo.getMessages().getMessage("enemy.misses.you","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
							mundo.getMessages().getMessage("you.miss.enemy","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
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
					//a ready to block de los mobiles, si ya no les est� atacando
					//el objetivo, pasar a idle (m�s realista)

					objetivo.setNewState ( IDLE , 1 ); //objetivo has a chance to attack right now

					//informar de que tienes la iniciativa
					ejec = false;
					try
					{
						//tiene prioridad el arma, luego el que la maneja.
						ejec = getCurrentWeapon().execCode ( "infoIniciativaTrasBloquear" , new Object[] {this,objetivo} ); if ( !ejec )
							ejec = execCode ( "infoIniciativaTrasBloquear" , new Object[] {objetivo} );
					}
					catch ( ScriptException te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejec )
					{
						habitacionActual.reportAction ( this , objetivo , null ,
								null , 
								//"Tienes la iniciativa...\n" , 
								//"$2 interrumpe su intento de bloquear...\n" , 
								mundo.getMessages().getMessage("someone.fails.blocking.you",new Object[]{this,objetivo} ) , 
								mundo.getMessages().getMessage("you.fail.blocking.someone",new Object[]{this,objetivo} ) , 
								true );
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
					catch ( ScriptException te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at infoAcierto(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejec )
					{
						habitacionActual.reportAction ( this , objetivo , null ,
								null , 
								//"Tienes la iniciativa...\n" , 
								//"$2 interrumpe su intento de esquivar...\n" , 
								mundo.getMessages().getMessage("someone.fails.dodging.you",new Object[]{this,objetivo} ) , 
								mundo.getMessages().getMessage("you.fail.dodging.someone",new Object[]{this,objetivo} ) , 
								true );
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

					//informar de que bloqueas con �xito
					boolean ejec = false;
					try
					{
						//tiene prioridad el arma, luego el que la maneja.
						ejec = getCurrentWeapon().execCode ( "infoBloqueo" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
							ejec = execCode ( "infoBloqueo" , new Object[] {objetivo,new Integer(danyo)} );
					}
					catch ( ScriptException te )
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
								habitacionActual.reportAction ( this , objetivo , null ,
										mundo.getMessages().getMessage("someone.hits.blocked.someone.numeric","$weapon",objetivo.getCurrentWeapon().getOutputNameThe(),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,objetivo.getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.blocked.you.numeric","$weapon",objetivo.getCurrentWeapon().getOutputNameThe(objetivo),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,objetivo.getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.blocked.enemy.numeric","$weapon",objetivo.getCurrentWeapon().getOutputNameThe(this),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,objetivo.getCurrentWeapon(),new Integer(danyo)} ) , 					
										//"$2 se defiende de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem() + " recibiendo " + danyo + " puntos de da�o...\n" ,
										//"Te defiendes de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem(objetivo) + " recibiendo " + danyo + " puntos de da�o...\n" ,
										//"$2 se defiende con " + objetivo.getCurrentWeapon().constructName2OneItem(this) + " recibiendo " + danyo + " puntos de da�o...\n" ,	
										true );
							}
							else
							{
								habitacionActual.reportAction ( this , objetivo , null ,
										mundo.getMessages().getMessage("someone.hits.blocked.someone","$weapon",objetivo.getCurrentWeapon().getOutputNameThe(),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,objetivo.getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.blocked.you","$weapon",objetivo.getCurrentWeapon().getOutputNameThe(objetivo),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,objetivo.getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.blocked.enemy","$weapon",objetivo.getCurrentWeapon().getOutputNameThe(this),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,objetivo.getCurrentWeapon(),new Integer(danyo)} ) , 						
										//"$2 se defiende de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem() + " recibiendo " + objetivo.estimateDamage(danyo) + "...\n" ,
										//"Te defiendes de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem(objetivo) + " recibiendo " + objetivo.estimateDamage(danyo) + "...\n" ,
										//"$2 se defiende con " + objetivo.getCurrentWeapon().constructName2OneItem(this) + " recibiendo " + objetivo.estimateDamage(danyo) + "...\n" ,
										true );

								habitacionActual.reportActionAuto ( objetivo , null , null , "$1 " + objetivo.estimateStatus() + ".\n" , true );

							}
						}
						else
						{
							habitacionActual.reportAction ( this , objetivo , null ,
									mundo.getMessages().getMessage("someone.hits.blocked.someone.nodamage","$weapon",objetivo.getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,objetivo.getCurrentWeapon()} ) , 
									mundo.getMessages().getMessage("enemy.hits.blocked.you.nodamage","$weapon",objetivo.getCurrentWeapon().getOutputNameThe(objetivo),new Object[]{this,objetivo,objetivo.getCurrentWeapon()} ) , 
									mundo.getMessages().getMessage("you.hit.blocked.enemy.nodamage","$weapon",objetivo.getCurrentWeapon().getOutputNameThe(this),new Object[]{this,objetivo,objetivo.getCurrentWeapon()} ) , 						
									//"$2 se defiende de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem() + ", desviando el ataque...\n" ,
									//"Te defiendes de $1 con " + objetivo.getCurrentWeapon().constructName2OneItem(objetivo) + ", desviando el ataque...\n" ,
									//"$2 se defiende con " + objetivo.getCurrentWeapon().constructName2OneItem(this) + ", desviando el ataque...\n" ,
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

						//informar de que bloqueas sin �xito recibiendo da�o
						boolean ejec = false;
						try
						{
							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoBloqueoFallido" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
								ejec = execCode ( "infoBloqueoFallido" , new Object[] {objetivo,new Integer(danyo)} );
						}
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoBloqueo(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							if ( numeric_damage )
							{
								habitacionActual.reportAction ( this , objetivo , null ,
										mundo.getMessages().getMessage("someone.hits.block.failed.someone.numeric","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.block.failed.you.numeric","$weapon",getCurrentWeapon().getOutputNameThe(objetivo),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.block.failed.enemy.numeric","$weapon",getCurrentWeapon().getOutputNameThe(this),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 								
										//"$2 no consigue parar el ataque de $1, que le acierta con " + getCurrentWeapon().constructName2OneItem() + " infligi�ndole " + danyo + " puntos de da�o...\n" ,
										//"No consigues parar el ataque de $1, que te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligi�ndote " + danyo + " puntos de da�o...\n" ,
										//"$2 no consigue parar tu ataque, le aciertas con " + getCurrentWeapon().constructName2OneItem(this) + " infligi�ndole " + danyo + " puntos de da�o...\n" ,
										true );
							}
							else
							{
								habitacionActual.reportAction ( this , objetivo , null ,
										mundo.getMessages().getMessage("someone.hits.block.failed.someone","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.block.failed.you","$weapon",getCurrentWeapon().getOutputNameThe(objetivo),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.block.failed.enemy","$weapon",getCurrentWeapon().getOutputNameThe(this),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 								
										//"$2 no consigue parar el ataque de $1, que le acierta con " + getCurrentWeapon().constructName2OneItem() + " infligi�ndole " + objetivo.estimateDamage(danyo) + "...\n" ,
										//"No consigues parar el ataque de $1, que te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligi�ndote " + objetivo.estimateDamage(danyo) + "...\n" ,
										//"$2 no consigue parar tu ataque, le aciertas con " + getCurrentWeapon().constructName2OneItem(this) + " infligi�ndole " + objetivo.estimateDamage(danyo) + "...\n" ,
										true );

								habitacionActual.reportActionAuto ( objetivo , null , null , "$1 " + objetivo.estimateStatus() + ".\n" , true );

							}		
						}		

					}
					else
					{

						//informar de que bloqueas sin �xito pero no recibes da�o
						boolean ejec = false;
						try
						{
							//tiene prioridad el arma, luego el que la maneja.
							ejec = getCurrentWeapon().execCode ( "infoBloqueoFallido" , new Object[] {this,objetivo,new Integer(danyo)} ); if ( !ejec )
								ejec = execCode ( "infoBloqueoFallido" , new Object[] {objetivo,new Integer(danyo)} );
						}
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoBloqueo(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							habitacionActual.reportAction ( this , objetivo , null ,
									mundo.getMessages().getMessage("someone.hits.block.failed.someone.nodamage","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
									mundo.getMessages().getMessage("enemy.hits.block.failed.you.nodamage","$weapon",getCurrentWeapon().getOutputNameThe(objetivo),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
									mundo.getMessages().getMessage("you.hit.block.failed.enemy.nodamage","$weapon",getCurrentWeapon().getOutputNameThe(this),new Object[]{this,objetivo,getCurrentWeapon()} ) , 								
									//"$2 no consigue parar el ataque de $1, que le acierta con " + getCurrentWeapon().constructName2OneItem() + " pero no le hace da�o...\n" ,
									//"No consigues parar el ataque de $1, que te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " pero no te hace da�o...\n" ,
									//"$2 no consigue parar tu ataque, le aciertas con " + getCurrentWeapon().constructName2OneItem(this) + " pero no le haces da�o...\n" ,
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
				catch ( ScriptException te )
				{
					write(io.getColorCode("error") + "bsh.TargetError found at infoFallo(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
					writeError(ExceptionPrinter.getExceptionReport(te));
				}
				if ( !ejec )
				{
					habitacionActual.reportAction ( this , objetivo , null ,
							mundo.getMessages().getMessage("someone.misses.blocked.someone","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
							mundo.getMessages().getMessage("enemy.misses.blocked.you","$weapon",getCurrentWeapon().getOutputNameThe(objetivo),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
							mundo.getMessages().getMessage("you.miss.blocked.enemy","$weapon",getCurrentWeapon().getOutputNameThe(this),new Object[]{this,objetivo,getCurrentWeapon()} ) , 								
							//"El ataque de $1 falla a $2.\n" , 
							//"El ataque de $1 te falla.\n" , 
							//"Tu ataque falla a $2.\n" , 
							true );
				}

				setNewState ( ATTACK_RECOVER , generateAttackRecoverTime(getCurrentWeapon()) );

				//darle la iniciativa

				//o bien cambiar esto por, en cambio de estado de
				//ready to block de los mobiles, si ya no les est� atacando
				//el objetivo, pasar a idle (m�s realista)

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
					catch ( ScriptException te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at infoEsquivada(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejec )
					{
						habitacionActual.reportAction ( this , objetivo , null ,
								mundo.getMessages().getMessage("someone.dodged.by.someone","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
								mundo.getMessages().getMessage("enemy.dodged.by.you","$weapon",getCurrentWeapon().getOutputNameThe(objetivo),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
								mundo.getMessages().getMessage("you.dodged.by.enemy","$weapon",getCurrentWeapon().getOutputNameThe(this),new Object[]{this,objetivo,getCurrentWeapon()} ) , 								
								
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
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoEsquivadaFallida(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							if ( numeric_damage )
							{
								habitacionActual.reportAction ( this , objetivo , null ,
										mundo.getMessages().getMessage("someone.hits.dodge.failed.someone.numeric","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.dodge.failed.you.numeric","$weapon",getCurrentWeapon().getOutputNameThe(objetivo),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.dodge.failed.enemy.numeric","$weapon",getCurrentWeapon().getOutputNameThe(this),"$damage",String.valueOf(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 								
										true );
							}		
							else
							{
								habitacionActual.reportAction ( this , objetivo , null ,
										mundo.getMessages().getMessage("someone.hits.dodge.failed.someone","$weapon",getCurrentWeapon().getOutputNameThe(),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("enemy.hits.dodge.failed.you","$weapon",getCurrentWeapon().getOutputNameThe(objetivo),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
										mundo.getMessages().getMessage("you.hit.dodge.failed.enemy","$weapon",getCurrentWeapon().getOutputNameThe(this),"$damage",objetivo.estimateDamage(danyo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 								
										//"$2 intenta esquivar el golpe de $1; pero no lo consigue, $1 le acierta con " + getCurrentWeapon().constructName2OneItem() + " infligi�ndole " + objetivo.estimateDamage(danyo) + ".\n" ,
										//"Tu intento de esquivar falla, y $1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " infligi�ndote " + objetivo.estimateDamage(danyo) + ".\n"  ,
										//"A pesar de su intento de esquivar el ataque, aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + ", infligi�ndole " + objetivo.estimateDamage(danyo) + ".\n"  ,
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
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at infoEsquivadaFallida(), target id was " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );					
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
						if ( !ejec )
						{
							habitacionActual.reportAction ( this , objetivo , null ,
									mundo.getMessages().getMessage("someone.hits.dodge.failed.someone.nodamage","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
									mundo.getMessages().getMessage("enemy.hits.dodge.failed.you.nodamage","$weapon",getCurrentWeapon().getOutputNameThe(objetivo),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 
									mundo.getMessages().getMessage("you.hit.dodge.failed.enemy.nodamage","$weapon",getCurrentWeapon().getOutputNameThe(this),new Object[]{this,objetivo,getCurrentWeapon(),new Integer(danyo)} ) , 								
									//"$2 intenta esquivar el golpe de $1; pero no lo consigue, $1 le acierta con " + getCurrentWeapon().constructName2OneItem() + " pero no le hace da�o.\n" ,
									//"Tu intento de esquivar falla, y $1 te acierta con " + getCurrentWeapon().constructName2OneItem(objetivo) + " pero no te hace da�o.\n" ,
									//"A pesar de su intento de esquivar el ataque, aciertas a $2 con " + getCurrentWeapon().constructName2OneItem(this) + "; pero no le haces da�o.\n" ,
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

				habitacionActual.reportAction ( this , objetivo , null ,
						mundo.getMessages().getMessage("someone.misses.dodged.someone","$weapon",getCurrentWeapon().getOutputNameThe(),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
						mundo.getMessages().getMessage("enemy.misses.dodged.you","$weapon",getCurrentWeapon().getOutputNameThe(objetivo),new Object[]{this,objetivo,getCurrentWeapon()} ) , 
						mundo.getMessages().getMessage("you.miss.dodged.enemy","$weapon",getCurrentWeapon().getOutputNameThe(this),new Object[]{this,objetivo,getCurrentWeapon()} ) , 								
						//"El ataque de $1 falla a $2.\n" , 
						//"El ataque de $1 te falla. Tienes la iniciativa...\n" , 
						//"Tu ataque falla a $2. Te desequilibras...\n" , 
						true );

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

		//ser enemigo es relaci�n sim�trica
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
	
	public boolean removeSpell ( Spell viejo )
	{
		if ( spellRefs == null ) return false;
		else return spellRefs.removeElement(viejo);
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
	
	public boolean wieldsItem ( Item item )
	{
		return getWieldedWeapons().contains(item);
	}
	
	public boolean wearsItem ( Item item )
	{
		return getWornItems().contains(item);
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
	Intenta hacer a este bicho damage da�os del tipo damtype: la cantidad de da�o que haga
	en realidad se ver� reducida por las resistencias, armaduras, etc. y es la cantidad que
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

	//despu�s de bloquear; pero antes de armaduras (�stas se resuelven aqu�)
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

				//informar de que armadura absorbi� impacto

				habitacionActual.reportAction ( this , null , null ,
						mundo.getMessages().getMessage("someones.armor.absorbed","$armor",armadura.getOutputNameThe(),new Object[]{this,armadura} ) , 
						null,
						mundo.getMessages().getMessage("your.armor.absorbed","$armor",armadura.getOutputNameThe(this),new Object[]{this,armadura} ) , 								
						
						//armadura.constructName2OneItem() + " de $1 absorbe totalmente el impacto.\n" , 
						//null, 
						//"Tu armadura absorbe totalmente el impacto.\n"  , 
						true );


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
	public void setMaxHP ( int maxHP )
	{
		maxhp = maxHP;
	}
	public void setMaxMP ( int maxMP )
	{
		maxmp = maxMP;
	}

	public void prepareToDie ( )
	{
		setNewState ( DYING , 1 );
	}


	public void die()
	{

		Debug.println( this + " est� m�s que muerto." );


		boolean ejecutado = false;
		try
		{
			ejecutado = execCode( "beforeDie" , new Object[] {} );
		}
		catch ( ScriptException bshte)
		{
			//write("bsh.TargetError found at die routine" );
			writeError(ExceptionPrinter.getExceptionReport(bshte,"beforeDie() of mobile " + this));
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
				this.setRoom(mundo.getLimbo());
				//mundo.getLimbo().addMob(this);
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
		catch ( ScriptException bshte)
		{
			//write("bsh.TargetError found at afterDie routine" );
			writeError(ExceptionPrinter.getExceptionReport(bshte,"afterDie() of mobile " + this));
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

		int max = -1; //maximo da�o encontrado

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
		//hace las archiconocidas y famos�simas simulaciones que caracterizan la IA del AGE.

		Inventory usableWeapons = getUsableWeapons();

		Debug.println("Usable weapons for " + getUniqueName() + ": " + usableWeapons );

		if ( getEnemies() == null || usableWeapons == null ) return null;

		int max = -1; //maximo da�o encontrado hasta el momento
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

						//calcular media del da�o producido en n simulaciones
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
		catch ( ScriptException te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at infoIntentoAtaque(), target id was " + target.getID() + ", error was " + te + io.getColorCode("reset") );					
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
		if ( !ejec )
		{
			habitacionActual.reportAction ( this , target , null ,
					mundo.getMessages().getMessage("someone.attacks.someone","$weapon",w.getOutputNameThe(),new Object[]{this,target,w} ) , 
					mundo.getMessages().getMessage("enemy.attacks.you","$weapon",w.getOutputNameThe(target),new Object[]{this,target,w} ) , 
					mundo.getMessages().getMessage("you.attack.enemy","$weapon",w.getOutputNameThe(this),new Object[]{this,target,w} ) , 					
					//"$1 ataca a $2 con " + w.constructName2OneItem() + ".\n" ,
					//"$1 te ataca con " + w.constructName2OneItem(target) + ".\n",
					//"Atacas a $2 con " + w.constructName2OneItem(this) + ".\n",
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
		catch ( ScriptException te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at infoIntentoBloqueo(), target id was " + target.getID() + ", error was " + te + io.getColorCode("reset") );					
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
		if ( !ejec )
		{		

			habitacionActual.reportAction ( this , target , null ,
					mundo.getMessages().getMessage("someone.tries.to.block.someone","$weapon",w.getOutputNameThe(),new Object[]{this,target,w} ) , 
					mundo.getMessages().getMessage("enemy.tries.to.block.you","$weapon",w.getOutputNameThe(target),new Object[]{this,target,w} ) , 
					mundo.getMessages().getMessage("you.try.to.block.enemy","$weapon",w.getOutputNameThe(this),new Object[]{this,target,w} ) , 					
					//"$1 intenta defenderse de $2 con " + w.constructName2OneItem()  + ".\n",
					//"$1 intenta defenderse con " + w.constructName2OneItem(target) + ".\n" ,
					//"Intentas defenderte de $2 con " + w.constructName2OneItem(this) + ".\n" ,
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
		catch ( ScriptException te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at infoIntentoEsquivada(), target id was " + target.getID() + ", error was " + te + io.getColorCode("reset") );					
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
		if ( !ejec )
		{	

			habitacionActual.reportAction ( this , target , null ,
					mundo.getMessages().getMessage("someone.tries.to.dodge.someone",new Object[]{this,target} ) , 
					mundo.getMessages().getMessage("enemy.tries.to.dodge.you",new Object[]{this,target} ) , 
					mundo.getMessages().getMessage("you.try.to.dodge.enemy",new Object[]{this,target} ) , 					
					//"$1 intenta esquivar el ataque de $2" + ".\n" ,
					//"$1 intenta esquivarte" + ".\n" ,
					//"Intentas esquivar el ataque de $2" + ".\n" ,
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

	//devuelve la lista de todos los enemigos que nos est�n atacando.
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

	//freeLimbsNeeded == true -> s�lo blandir en miembro libre
	//freeLimbsNeeded == false -> si los miembros est�n ocupados, desblandir las armas
	//blandidas en ellos para poder blandir �sta
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

	//Be careful with negatives. Not treated. Candidato a explicaci�n de posteriores fallos chungos?
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
		//var�a mucho
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
			org.w3c.dom.Element respTo = getNameListXMLRepresentation(doc,respondToSing,"SingularReferenceNames");
			suElemento.appendChild(respTo);
		}
		if ( respondToPlur != null )
		{
			org.w3c.dom.Element respTo = getNameListXMLRepresentation(doc,respondToPlur,"PluralReferenceNames");
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
		
		//new extra description support
		if ( extraDescriptionNameArrays != null && extraDescriptionArrays != null )
		{
			suElemento.appendChild(Utility.getExtraDescriptionXMLRepresentation(extraDescriptionNameArrays, extraDescriptionArrays, doc));
		}

		//caracter�sticas (traits): not at the moment
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



	//armas blandidas y armas naturales que no est�n blandiendo nada.
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


	//devuelve las armas naturales (pu�os, etc... i.e. miembros que atacan) del bicho.
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



	/**
	 * @deprecated Use {@link #doWear(Item,boolean)} instead
	 */
	public boolean intentarVestir ( Item it , boolean freeLimbsNeeded )
	{
		return doWear(it, freeLimbsNeeded);
	}

	/**
	 * Tries to wear the given item.
	 * Does not refresh the mobile's state to reflect the time taken to wear the item - use the higher-level method wear() for that.
	 * @param it
	 * @param freeLimbsNeeded If true, the method fails (returning false) if no limbs are free. If false, we automatically remove whatever
	 * is worn on needed limbs in order to wear the item.
	 * @return
	 */
	public boolean doWear ( Item it , boolean freeLimbsNeeded )
	{ 

		if ( ! ( it instanceof Wearable ) )
		{
			if ( it instanceof Weapon )
			{
				//write( io.getColorCode("denial") + "�Vestir " + it.constructName2OneItem(this) + "? Parece m�s adecuado blandir" + ((it.getGender())?"lo":"la") + ".\n" );
				writeDenial(mundo.getMessages().getMessage("cant.wear.weapon","$item",it.getOutputNameThe(this),"$oa",((it.getGender())?"o":"a"),new Object[]{this,it}));
			}
			else
			{
				//write( io.getColorCode("denial") + "No parece que " + it.constructName2OneItem(this) + " sea algo que se pueda vestir." + "\n" );
				writeDenial(mundo.getMessages().getMessage("item.not.wearable","$item",it.getOutputNameThe(this),"$oa",((it.getGender())?"o":"a"),new Object[]{this,it}));
			}
			return false;
		}  

		Inventory ourLimbs = getFlattenedPartsInventory();

		if ( ourLimbs.size() < 1 ) 
		{
			//write ( io.getColorCode("error") + "No puedes ponerte ropa si no tienes ning�n miembro." );
			writeError(mundo.getMessages().getMessage("cant.wear.without.limbs","$item",it.getOutputNameThe(this),"$oa",((it.getGender())?"o":"a"),new Object[]{this,it}));
			
		}
			
		//ver si ya estamos blandiendo el item
		for ( int i = 0 ; i < ourLimbs.size() ; i++ )
		{
			Item limb = ourLimbs.elementAt(i);
			if ( limb.getRelationshipPropertyValueAsBoolean ( it , "wears" ) )
			{
				//write ( io.getColorCode("denial") + "Ya llevas puesto " + it.constructName2OneItem(this) + ".\n" );
				writeDenial(mundo.getMessages().getMessage("wear.already.worn","$item",it.getOutputNameThe(this),new Object[]{this,it}));
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
				Vector temp = ourLimbs.patternMatch ( st.nextToken() , false ).toEntityVector();
				for ( int l = 0 ; l < temp.size() ; l++ )
					if ( !matchingLimbs.contains( temp.elementAt(l) ) )
						matchingLimbs.add(temp.elementAt(l) );
			}

			//si no hay ninguno que lo cumpla, nada, no podemos vestir.
			if ( matchingLimbs.size() < 1 )
			{
				//write( io.getColorCode("denial")  + "No parece adecuado para los de tu especie." + io.getColorCode("reset")  + "\n"  );
				writeDenial(mundo.getMessages().getMessage("wear.no.suitable.limbs","$item",it.getOutputNameThe(this),new Object[]{this,it}));
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
						if ( doUnwear ( (Item) vestidos.get(z) ) == false )
							return false;
					}

				}

				else //Usual for Players. Fracasa la funci�n.
				{

					for ( int n = 0 ; n < matchingLimbs.size() ; n++ )
					{
						Item current = ( Item ) matchingLimbs.get(n);
						List vestidos = current.getRelatedEntitiesByValue("wears",true);
						if ( vestidos.size() > 0 )
						{
							Item vestido = ( Item ) vestidos.get(0);
							//write( io.getColorCode("information") + "Llevas puesto " + vestido.constructName2OneItem(this)  + " en " + current.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n"  );			
							writeInformation(mundo.getMessages().getMessage("you.use.limb.wearing","$item",vestido.getOutputNameThe(this),"$limb",current.getOutputNameThe(this),new Object[]{this,vestido,current.getOutputNameThe(this)}));
						}
						else
						{
							//write( io.getColorCode("information") + "Tienes libre " + current.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n" );
							writeInformation(mundo.getMessages().getMessage("you.have.free.limb","$limb",current.getOutputNameThe(this),new Object[]{this,current}));
						}

					}
					//write( io.getColorCode("denial") + "Tienes que quitarte algo para poder vestir " + it.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n"  );
					writeDenial(mundo.getMessages().getMessage("wear.busy.limbs","$item",it.getOutputNameThe(this),new Object[]{this,it}));
					return false;

				}

			}

		}

		//{si llegamos hasta aqu�, se dan todas las condiciones para vestir el item.}
		//{s�lo tenemos que marcar el item como vestido y poner la relaci�n "wears" con el item en todos los miembros de usedLimbs.}

		String toOutput = "";

		for ( int i = 0 ; i < usedLimbs.size() ; i++ )
		{
			Item limb = (Item) usedLimbs.get(i);
			limb.setRelationshipProperty ( it , "wears" , true );

			if ( i == 0 )
				toOutput += limb.getOutputNameThe(this);
			else if ( i > 0 && i == usedLimbs.size() - 1 )
				toOutput += " y " + limb.getOutputNameThe(this);
			else
				toOutput += ", " + limb.getOutputNameThe(this);	

		}

		//write( io.getColorCode("action") + "Te pones " + it.constructName2OneItem(this) + " en " + toOutput + ".\n" + io.getColorCode("reset") );
		writeAction( mundo.getMessages().getMessage("you.wear.item","$item",it.getOutputNameThe(this),"$limbs",toOutput,new Object[]{this,it,usedLimbs})  );
		
		//habitacionActual.reportActionAuto ( this , null , "$1 se pone " + it.constructName2OneItem() + ".\n" , false );	
		habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.wears.item","$item",it.getOutputNameThe(this),"$limbs",toOutput,new Object[]{this,it,usedLimbs}) , false );
		
		if ( wornItems == null ) wornItems = new Inventory(10000,10000);
		try
		{
			wornItems.addItem(it);	
		}
		catch ( WeightLimitExceededException wlee )
		{
			writeDenial(mundo.getMessages().getMessage("cant.wear.item.weight","$item",it.getOutputNameThe(this),new Object[]{this,it}));
			//write( io.getColorCode("denial") + "No puedes vestir eso, pesa demasiado." + ".\n" + io.getColorCode("reset") );	
			return false;
		}
		catch ( VolumeLimitExceededException vlee )
		{
			writeDenial(mundo.getMessages().getMessage("cant.wear.item.volume","$item",it.getOutputNameThe(this),new Object[]{this,it}));
			//write( io.getColorCode("denial") + "No puedes blandir eso, pesa demasiado." + ".\n" + io.getColorCode("reset") );
			return false;
		}
		
		//ejecutar eventos onWear
		try
		{	
			it.execCode("onWear",new Object[] {this,usedLimbs} );
		}
		catch ( ScriptException te )
		{
			write( io.getColorCode("error") + "bsh.TargetError found at event onWear , item " + it + io.getColorCode("reset") + "\n"  );
			writeError(ExceptionPrinter.getExceptionReport(te));
		}

		return true;
	}



	/**
	 * @deprecated Use {@link #doWield(Item,boolean)} instead
	 */
	public boolean intentarBlandir ( Item it , boolean freeLimbsNeeded )
	{
		return doWield(it, freeLimbsNeeded);
	}

	/**
	 * Try to wield the given item instantly.
	 * Does not set the state to reflect the time taken to wield a weapon - use the higher level method wield() instead for that.
	 * @param it
	 * @param freeLimbsNeeded If true, the method fails if the limbs required to wield the weapon are not free. If false, we automatically
	 * remove items wielded in those limbs in order to wield the weapon.
	 * @return
	 */
	public boolean doWield ( Item it , boolean freeLimbsNeeded )
	{

		if ( ! ( it instanceof Weapon ) )
		{
			if ( it instanceof Wearable )
			{
				writeDenial(mundo.getMessages().getMessage("cant.wield.wearable","$item",it.getOutputNameThe(this),"$oa",((it.getGender())?"o":"a"),new Object[]{this,it}));
				//write( io.getColorCode("denial") + "�Blandir " + it.constructName2OneItem(this) + "? Parece m�s adecuado vestir" + ((it.getGender())?"lo":"la") + ".\n" );
			}
			else
			{
				//write( io.getColorCode("denial") + "No parece que " + it.constructName2OneItem(this) + " sea un arma." + "\n" );
				writeDenial(mundo.getMessages().getMessage("wield.non.weapon","$item",it.getOutputNameThe(this),new Object[]{this,it}));
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
				//write ( io.getColorCode("denial") + "Ya est�s blandiendo " + it.constructName2OneItem(this) + ".\n" );
				writeDenial(mundo.getMessages().getMessage("wield.already.wielded","$item",it.getOutputNameThe(this),new Object[]{this,it}));
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
				Vector temp = ourLimbs.patternMatch ( st.nextToken() , false ).toEntityVector();
				for ( int l = 0 ; l < temp.size() ; l++ )
					if ( !matchingLimbs.contains( temp.elementAt(l) ) )
						matchingLimbs.add(temp.elementAt(l) );
			}

			//si no hay ninguno que lo cumpla, nada, no podemos blandir.
			if ( matchingLimbs.size() < 1 )
			{
				//write( io.getColorCode("denial")  + "No parece adecuado para los de tu especie." + io.getColorCode("reset")  + "\n"  );
				writeDenial(mundo.getMessages().getMessage("wield.no.suitable.limbs","$item",it.getOutputNameThe(this),new Object[]{this,it}));
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
						if ( doUnwield ( (Item) vestidos.get(z) ) == false )
							return false;
					}

				}

				else //Usual for Players. Fracasa la funci�n.
				{

					for ( int n = 0 ; n < matchingLimbs.size() ; n++ )
					{
						Item current = ( Item ) matchingLimbs.get(n);
						List vestidos = current.getRelatedEntitiesByValue("wields",true);
						if ( vestidos.size() > 0 )
						{
							Item vestido = ( Item ) vestidos.get(0);
							//write( io.getColorCode("information") + "Est�s blandiendo " + vestido.constructName2OneItem(this)  + " en " + current.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n"  );			
							writeInformation(mundo.getMessages().getMessage("you.use.limb.wielding","$item",vestido.getOutputNameThe(this),"$limb",current.getOutputNameThe(this),new Object[]{this,vestido,current.getOutputNameThe(this)}));
						}
						else
						{
							//write( io.getColorCode("information") + "Tienes libre " + current.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n" );
							writeInformation(mundo.getMessages().getMessage("you.have.free.limb","$limb",current.getOutputNameThe(this),new Object[]{this,current}));
						}

					}
					//write( io.getColorCode("denial") + "Tienes que guardar alg�n arma para poder blandir " + it.constructName2OneItem(this) + "." + io.getColorCode("reset")  + "\n"  );
					writeDenial(mundo.getMessages().getMessage("wield.busy.limbs","$item",it.getOutputNameThe(this),new Object[]{this,it}));
					return false;

				}

			}

		}

		//{si llegamos hasta aqu�, se dan todas las condiciones para blandir el item.}
		//{s�lo tenemos que marcar el item como blandido y poner la relaci�n "wields" con el item en todos los miembros de usedLimbs.}

		String toOutput = "";

		for ( int i = 0 ; i < usedLimbs.size() ; i++ )
		{
			Item limb = (Item) usedLimbs.get(i);
			limb.setRelationshipProperty ( it , "wields" , true );

			if ( i == 0 )
				toOutput += limb.getOutputNameThe(this);
			else if ( i > 0 && i == usedLimbs.size() - 1 )
				toOutput += " y " + limb.getOutputNameThe(this);
			else
				toOutput += ", " + limb.getOutputNameThe(this);	

		}

		//write( io.getColorCode("action") + "Blandes " + it.constructName2OneItem(this) + " en " + toOutput + ".\n" + io.getColorCode("reset") );
		writeAction( mundo.getMessages().getMessage("you.wield.item","$item",it.getOutputNameThe(this),"$limbs",toOutput,new Object[]{this,it,usedLimbs})  );
		
		//habitacionActual.reportActionAuto ( this , null , "$1 blande " + it.constructName2OneItem() + ".\n" , false );	
		habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.wields.item","$item",it.getOutputNameThe(this),"$limbs",toOutput,new Object[]{this,it,usedLimbs}) , false );
		
		
		
		
		
		
		
		if ( wieldedWeapons == null ) wieldedWeapons = new Inventory(10000,10000);
		try
		{
			wieldedWeapons.addItem(it);	
		}
		catch ( WeightLimitExceededException wlee )
		{
			//write( io.getColorCode("denial") + "No puedes blandir eso, pesa demasiado." + ".\n" + io.getColorCode("reset") );	
			writeDenial(mundo.getMessages().getMessage("cant.wield.item.weight","$item",it.getOutputNameThe(this),new Object[]{this,it}));
		}
		catch ( VolumeLimitExceededException vlee )
		{
			//write( io.getColorCode("denial") + "No puedes blandir eso, pesa demasiado." + ".\n" + io.getColorCode("reset") );
			writeDenial(mundo.getMessages().getMessage("cant.wield.item.volume","$item",it.getOutputNameThe(this),new Object[]{this,it}));
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


	/**
	 * @deprecated Use {@link #doUnwear(Item)} instead
	 */
	public boolean desvestir ( Item it )
	{
		return doUnwear(it);
	}

	/**
	 * Unwears the given item, but doesn't change the state to reflect the time taken to unwear (use unwear() for that).
	 * @param it
	 * @return
	 */
	public boolean doUnwear ( Item it )
	{

		if ( wornItems == null ) return false;

		boolean success = wornItems.removeItem(it);
		
		if ( success )
		{
			
			List usedLimbs = new Vector();

			Inventory ourLimbs = getFlattenedPartsInventory();
			for ( int i = 0 ; i < ourLimbs.size() ; i++ )
			{
				Item curLimb = ourLimbs.elementAt(i);
				if ( curLimb.getRelationshipPropertyValueAsBoolean(it,"wears") )
				{
					curLimb.setRelationshipProperty(it,"wears",false);
					usedLimbs.add(curLimb);
				}
			}

			//write ( io.getColorCode("action") + "Te quitas " + it.constructName2OneItem() + ".\n" + io.getColorCode("reset") );
			writeAction ( mundo.getMessages().getMessage("you.unwear.item","$item",it.getOutputNameThe(this),new Object[]{this,it}) );
						
			//habitacionActual.reportActionAuto ( this , null , "$1 se quita " + it.constructName2OneItem() + ".\n" , false );
			habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.unwears.item","$item",it.getOutputNameThe(),new Object[]{this,it}) , false );

			//ejecutar eventos onUnwear
			try
			{	
				it.execCode("onUnwear",new Object[] {this,usedLimbs} );
			}
			catch ( ScriptException te )
			{
				//write( io.getColorCode("error") + "bsh.TargetError found at event onUnwear , item " + it + io.getColorCode("reset") + "\n"  );
				writeError(ExceptionPrinter.getExceptionReport(te,"onUnwear, item " + it));
			}
			
			return true;

		}
		else
		{
			return false;
		}

	}

	/**
	 * @deprecated Use {@link #doUnwield(Item)} instead
	 */
	public boolean guardarArma ( Item it )
	{
		return doUnwield(it);
	}

	/**
	 * Tries to unwield an item instantly.
	 * Does not reflect the state and its timer to represent the time taken to unwield.
	 * Use the higher-level method unwield(Item) for that.
	 * @param it
	 * @return
	 */
	public boolean doUnwield ( Item it )
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

			writeAction ( mundo.getMessages().getMessage("you.unwield.item","$item",it.getOutputNameThe(),new Object[]{this,it}) );
			habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.unwields.item","$item",it.getOutputNameThe(),new Object[]{this,it}) , false );
			
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
			return presentName ( getSingName(viewer) , mundo.getMessages().getMessage("art.ind.m") , mundo.getMessages().getMessage("art.ind.f") );
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
			return presentName ( getSingNameTrue(viewer) , mundo.getMessages().getMessage("art.def.m") , mundo.getMessages().getMessage("art.def.f") );	
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

			return presentName ( getSingNameTrue(viewer) , mundo.getMessages().getMessage("art.def.m") , mundo.getMessages().getMessage("art.def.f") );
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
	
	//convert numbers to their word representation
	public String numberToWord ( int number )
	{
		switch ( number )
		{
			case 1: return "uno";
			case 2: return "dos";
			case 3: return "tres";
			case 4: return "cuatro";
			case 5: return "cinco";
			case 6: return "seis";
			case 7: return "siete";
			case 8: return "ocho";
			case 9: return "nueve";
			default: return String.valueOf(number);
		}
	}

	//master method for getting output names
	/**
	 * Gets the output name of this entity, given
	 * @param nItems The number of copies of this entity that are being shown.
	 * @viewer The entity who is viewing this entity, if any.
	 * @mascArt Article to be used for showing a single instance if the item is masculine.
	 * @femArt Article to be used for showing a single instance if the item is feminine.
	 * @evenIfInvisible If true, return a nonempty string even if the entity is invisible for the viewer (fallback to getTitle()).
	 * @numberToWord If nItems > 1, show the number of items in word form (eight) rather than numerical form (8).
	 */
	public String getOutputName ( int nItems , Entity viewer , String mascArt , String femArt , boolean evenIfInvisible , boolean numberToWord )
	{
		if ( nItems == 1 )
		{
			String baseName;
			if ( evenIfInvisible ) baseName = getSingNameTrue(viewer);
			else baseName = getSingName(viewer);
			return presentName ( baseName , mascArt , femArt );
		}
		else
		{
			String baseName;
			if ( evenIfInvisible ) baseName = getPlurNameTrue(viewer);
			else baseName = getPlurName(viewer);
			if ( numberToWord ) return presentName ( baseName , numberToWord(nItems) , numberToWord(nItems) );
			else return presentName ( baseName , String.valueOf(nItems) , String.valueOf(nItems) );
		}
	}
	
	/**
	 * Gets the output name to show nItems copies of this entity to viewer, without any article.
	 * @param nItems The number of copies of this entity that are being showed.
	 * @param viewer The entity who is viewing this entity.
	 * @return String with the output name to show to viewer.
	 */
	public String getOutputNameOnly ( int nItems , Entity viewer )
	{
		return getOutputName ( nItems , viewer , "" , "" , false , true );
	}
	
	/**
	 * Gets the output name to show one instance of this entity to viewer, without any article.
	 * @return String with the output name to show to viewer, without using any article.
	 */
	public String getOutputNameOnly ( Entity viewer )
	{
		return getOutputName ( 1 , viewer , "" , "" , true , true );
	}
	
	/**
	 * Gets the output name to show nItems copies of this entity, without parameterising it for any viewer in particular, without any article.
	 * @param nItems The number of copies of this entity that are being showed.
	 * @return String with the output name to show to viewer, without using any article for a single item.
	 */
	public String getOutputNameOnly ( int nItems )
	{
		return getOutputName ( nItems , null , "" , "" , true , true );
	}
	
	/**
	 * Gets the output name to show one instance of this entity, without parameterising the name for any viewer in particular, without any article.
	 * @return String with the output name to show to viewer, without using any article.
	 */
	public String getOutputNameOnly ( )
	{
		return getOutputName ( 1 , null , "" , "" , true , true );
	}
	
	/**
	 * Gets the output name to show nItems copies of this entity to viewer, with a definite article if applicable.
	 * @param nItems The number of copies of this entity that are being showed.
	 * @param viewer The entity who is viewing this entity.
	 * @return String with the output name to show to viewer, with a definite article or the number of items.
	 */
	public String getOutputNameThe ( int nItems , Entity viewer )
	{
		return getOutputName ( nItems , viewer , mundo.getMessages().getMessage("art.def.m") , mundo.getMessages().getMessage("art.def.f") , false , true );
	}
	
	/**
	 * Gets the output name to show one instance of this entity to viewer, using a definite article.
	 * @return String with the output name to show to viewer, using a definite article
	 */
	public String getOutputNameThe ( Entity viewer )
	{
		return getOutputName ( 1 , viewer , mundo.getMessages().getMessage("art.def.m") , mundo.getMessages().getMessage("art.def.f") , true , true );
	}
	
	/**
	 * Gets the output name to show nItems copies of this entity, without parameterising them for any viewer in particular, with a definite article if applicable.
	 * @param nItems The number of copies of this entity that are being showed.
	 * @return String with the output name to show to viewer, with a definite article or the number of items.
	 */
	public String getOutputNameThe ( int nItems )
	{
		return getOutputName ( nItems , null , mundo.getMessages().getMessage("art.def.m") , mundo.getMessages().getMessage("art.def.f") , true , true );
	}
	
	/**
	 * Gets the output name to show one instance of this entity, without parameterising the name for any viewer in particular, using a definite article.
	 * @return String with the output name to show to viewer, using a definite article
	 */
	public String getOutputNameThe ( )
	{
		return getOutputName ( 1 , null , mundo.getMessages().getMessage("art.def.m") , mundo.getMessages().getMessage("art.def.f") , true , true );
	}
	
	/**
	 * Gets the output name to show nItems copies of this entity to viewer, with an indefinite article if applicable.
	 * @param nItems The number of copies of this entity that are being showed.
	 * @param viewer The entity who is viewing this entity.
	 * @return String with the output name to show to viewer, with an indefinite article or the number of items.
	 */
	public String getOutputNameA ( int nItems , Entity viewer )
	{
		return getOutputName ( nItems , viewer , mundo.getMessages().getMessage("art.ind.m") , mundo.getMessages().getMessage("art.ind.f") , false , true );
	}
	
	/**
	 * Gets the output name to show one instance of this entity to a given viewer, using an indefinite article.
	 * @return String with the output name to show to viewer, using an indefinite article.
	 */
	public String getOutputNameA ( Entity viewer )
	{
		return getOutputName ( 1 , viewer , mundo.getMessages().getMessage("art.ind.m") , mundo.getMessages().getMessage("art.ind.f") , true , true );
	}
	
	/**
	 * Gets the output name to show nItems copies of this entity, without parameterising them for any viewer in particular, with an indefinite article if applicable.
	 * @param nItems The number of copies of this entity that are being showed.
	 * @return String with the output name to show to viewer, with an indefinite article or the number of items.
	 */
	public String getOutputNameA ( int nItems )
	{
		return getOutputName ( nItems , null , mundo.getMessages().getMessage("art.ind.m") , mundo.getMessages().getMessage("art.ind.f") , true , true );
	}
	
	/**
	 * Gets the output name to show one instance of this entity, without parameterising the name for any viewer in particular, using an indefinite article.
	 * @return String with the output name to show to viewer, using an indefinite article.
	 */
	public String getOutputNameA ( )
	{
		return getOutputName ( 1 , null , mundo.getMessages().getMessage("art.ind.m") , mundo.getMessages().getMessage("art.ind.f") , true , true );
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


	//devuelve lo grave que es el da�o
	public String estimateDamage ( int damAmt )
	{

		double percent = (double)damAmt / (double)maxhp * 100.0;

		String result;

		if ( percent < 5 )
			result = mundo.getMessages().getMessage("damage.5",new Object[]{this});
		else if ( percent < 10 )
			result = mundo.getMessages().getMessage("damage.10",new Object[]{this});	
		else if ( percent < 18 )
			result = mundo.getMessages().getMessage("damage.18",new Object[]{this});
		else if ( percent < 25 )
			result = mundo.getMessages().getMessage("damage.25",new Object[]{this});
		else if ( percent < 33 )
			result = mundo.getMessages().getMessage("damage.33",new Object[]{this});
		else if ( percent < 40 )
			result = mundo.getMessages().getMessage("damage.40",new Object[]{this});
		else if ( percent < 50 )
			result = mundo.getMessages().getMessage("damage.50",new Object[]{this});
		else if ( percent < 60 )
			result = mundo.getMessages().getMessage("damage.60",new Object[]{this});
		else if ( percent < 70 )
			result = mundo.getMessages().getMessage("damage.70",new Object[]{this});
		else if ( percent < 80 )
			result = mundo.getMessages().getMessage("damage.80",new Object[]{this});
		else if ( percent < 90 )
			result = mundo.getMessages().getMessage("damage.90",new Object[]{this});
		else
			result = mundo.getMessages().getMessage("damage.lethal",new Object[]{this});

		return result;

	}

	public String estimateStatus (  )
	{

		//percent = % de da�o recibido
		double percent = (double)(maxhp-hp) / (double)maxhp * 100.0;

		String result;

		if ( percent <= 0.1 )
			result = mundo.getMessages().getMessage("woundstatus.1","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 5 )
			result = mundo.getMessages().getMessage("woundstatus.5","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 10 )
			result = mundo.getMessages().getMessage("woundstatus.10","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 18 )
			result = mundo.getMessages().getMessage("woundstatus.18","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 25 )
			result = mundo.getMessages().getMessage("woundstatus.25","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 33 )
			result = mundo.getMessages().getMessage("woundstatus.33","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 40 )
			result = mundo.getMessages().getMessage("woundstatus.40","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 50 )
			result = mundo.getMessages().getMessage("woundstatus.50","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 60 )
			result = mundo.getMessages().getMessage("woundstatus.60","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 70 )
			result = mundo.getMessages().getMessage("woundstatus.70","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 80 )
			result = mundo.getMessages().getMessage("woundstatus.80","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 90 )
			result = mundo.getMessages().getMessage("woundstatus.90","$oa",(getGender()?"o":"a"),new Object[]{this});
		else if ( percent < 100 )
			result = mundo.getMessages().getMessage("woundstatus.100","$oa",(getGender()?"o":"a"),new Object[]{this});
		else
			result = mundo.getMessages().getMessage("woundstatus.lethal","$oa",(getGender()?"o":"a"),new Object[]{this});

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
	
	public boolean getGender()
	{
		return gender;
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
			//Debug.println("Mana Cost Flubbed!");
			//write("No tienes suficiente man� para ejecutar ese hechizo.\n");
			writeDenial(mundo.getMessages().getMessage("not.enough.mana","$spell",s.getUniqueName(),new Object[]{this,s,target}));
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
			catch ( ScriptException bshte)
			{
				//escribir("bsh.TargetError found at fail routine" );
				;
				writeError(ExceptionPrinter.getExceptionReport(bshte,"prepare()"));
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

		int origState = getState();
		long origTimer = getPropertyTimeLeft("state");

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

		/*
		 * if the spell casting hasn't changed the state/timer (it could change it, for example because the spell killed the caster!), then we change the state to
		 * IDLE so more commands can be accepted.
		 */
		if ( getState() == origState && getPropertyTimeLeft("state") == origTimer )
			setNewState ( IDLE , 1 );

	}

	void interrupt ( String cause )
	{

		Debug.println("INTERRUPT!!!!!!!!!!!!");

		switch ( getState() )
		{
		case MOVING:
			write(mundo.getMessages().getMessage("interrupted.move","$cause",Character.toUpperCase(cause.charAt(0)) + cause.substring(1),new Object[]{this,cause}));
			//write( Character.toUpperCase(cause.charAt(0)) + cause.substring(1) + " interrumpe tu intento de irte.\n" );
			break;
		case ATTACKING:
			write(mundo.getMessages().getMessage("interrupted.attack","$cause",Character.toUpperCase(cause.charAt(0)) + cause.substring(1),new Object[]{this,cause}));
			//write( Character.toUpperCase(cause.charAt(0)) + cause.substring(1) + " interrumpe tu intento de ataque.\n" );
			break;
		case CASTING:
			write(mundo.getMessages().getMessage("interrupted.cast","$cause",Character.toUpperCase(cause.charAt(0)) + cause.substring(1),new Object[]{this,cause}));
			//write( Character.toUpperCase(cause.charAt(0)) + cause.substring(1) + " interrumpe tu intento de hacer magia.\n" );
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

		patternMatchVectorSingSing = Matches.toEntityVectors(inv1.patternMatchTwo ( inv2 , arguments , false , false )); //en singular y singular
		patternMatchVectorPlurSing = Matches.toEntityVectors(inv1.patternMatchTwo ( inv2 , arguments , true , false )); //en plural y singular



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
					//"No parece muy �til poner cosas en " + ourContainer.constructName2True(1,this) + "." + io.getColorCode("reset") 
					mundo.getMessages().getMessage("cant.put.into.noncontainer","$container",ourContainer.constructName2True(1,this),new Object[]{this,ourContainer})
					+ io.getColorCode("reset") );
			return false;
		}
		else if ( ourContainer.isCloseable() && !ourContainer.isOpen() )
		{
			writeDenial (
			mundo.getMessages().getMessage("cant.put.into.closed","$container",ourContainer.constructName2True(1,this),"$oa",((ourContainer.getGender())?"o":"a"),new Object[]{this,ourContainer})
			);
			//write ( io.getColorCode("denial")  + ourContainer.constructName2True(1,this) + " est� cerrad" + ((ourContainer.getGender())?"o.":"a.") + io.getColorCode("reset") + "\n"  );
			return false;
		}
		return true;
	}

	//put inside action

	public void putInside ( Item ourItem , Item ourContainer )
	{

		if ( ourItem == ourContainer )
		{
			//we can't put a container into itself!
			writeDenial ( lenguaje.correctMorphologyWithoutTrimming ( mundo.getMessages().getMessage("cant.put.into.itself","$item",ourItem.getOutputNameThe(this),"$oa",((ourItem.getGender())?"o":"a"))));
			return;
		}

		try
		{
			ourContainer.addItem(ourItem);
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
			catch ( ScriptException te )
			{
				//write( io.getColorCode("error") + "bsh.TargetError found at event onPutInside , item number " + ourItem.getID() + io.getColorCode("reset") + "\n"  );
				writeError(ExceptionPrinter.getExceptionReport(te,"onPutInside, item " + ourItem));
			}

			if ( !eventEnded )
			{
				//write( io.getColorCode("action")  + "Pones " + ourItem.constructName2True(1,this) + " en " + ourContainer.constructName2True(1,this) + "." + io.getColorCode("reset")  + "\n"  );
				writeAction ( lenguaje.correctMorphologyWithoutTrimming( mundo.getMessages().getMessage("you.put.into","$item",ourItem.constructName2True(1,this),"$container",ourContainer.constructName2True(1,this),new Object[]{this,ourItem,ourContainer}) ) );
			}
		}
		catch ( WeightLimitExceededException wle )
		{
			//write ( io.getColorCode("denial") + ourContainer.constructName2True(1,this) + " no puede soportar tanto peso." + io.getColorCode("reset")  + "\n"  );
			writeDenial ( lenguaje.correctMorphologyWithoutTrimming ( mundo.getMessages().getMessage("cant.put.into.weight","$item",ourItem.constructName2True(1,this),"$container",ourContainer.constructName2True(1,this),new Object[]{this,ourItem,ourContainer})));
		}
		catch ( VolumeLimitExceededException vle )
		{
			//write ( io.getColorCode("denial") + ourItem.constructName2True(1,this) + " no cabe en " +  ourContainer.constructName2True(1,this) + io.getColorCode("reset")  + "\n"  );
			writeDenial ( lenguaje.correctMorphologyWithoutTrimming( mundo.getMessages().getMessage("cant.put.into.weight","$item",ourItem.constructName2True(1,this),"$container",ourContainer.constructName2True(1,this),new Object[]{this,ourItem,ourContainer})));
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
				write( io.getColorCode("action")  + lenguaje.correctMorphology ( mundo.getMessages().getMessage("you.get.item","$item",ourItem.constructName2True(1,this)+toAppend,new Object[]{this,ourItem})) + "\n" + io.getColorCode("reset") );
			else
				write( io.getColorCode("action")  + lenguaje.correctMorphology ( mundo.getMessages().getMessage("you.get.item.from.location","$item",ourItem.constructName2True(1,this),"$location",toAppend.trim(),new Object[]{this,ourItem,toAppend})) + "\n" + io.getColorCode("reset") );
			
			//habitacionActual.informActionAuto ( this , null , "$1 coge " + ourItem.constructName2OneItem() + toAppend + ".\n" , false );
			if ( toAppend == null || toAppend.length() < 1 )
				habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.gets.item","$item", ourItem.getOutputNameThe(),new Object[]{this,ourItem} ) /*+ "\n"*/ , false );
			else
				habitacionActual.reportActionAuto ( this , null , mundo.getMessages().getMessage("someone.gets.item.from.location","$item", ourItem.getOutputNameThe(),"$location",toAppend.trim(),new Object[]{this,ourItem} ) /*+ "\n"*/ , false );
			
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
			catch ( ScriptException te )
			{
				//write( io.getColorCode("error") + "bsh.TargetError found at event onGet , item number " + ourItem.getID() + io.getColorCode("reset") + "\n"  );
				writeError(ExceptionPrinter.getExceptionReport(te,"onGet, item " + ourItem));
			}

			//velar por el principio de conservacion de la masa
			inv.removeItem ( ourItem );
			if ( inv == habitacionActual.getInventory() ) ourItem.removeRoomReference(habitacionActual);
			ourItem.removeFromContainers(); //si estaba en un contenedor
			
			//ejecutar descripci�n (pesa poquito, etc. etc.)
			boolean execced = false;
			try
			{
				execced = mundo.execCode("messageAfterGet",new Object[] {this,ourItem} );
			}
			catch ( ScriptException te )
			{
				//write( io.getColorCode("error") + "bsh.TargetError found at messageAfterGet , item number " + ourItem.getID() + io.getColorCode("reset") + "\n"  );
				writeError(ExceptionPrinter.getExceptionReport(te,"messageAfterGet, item " + ourItem));
			}

			if ( !execced )
			{
				//afterGet por defecto: descripci�n del �tem.
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
						pesoDescription = "Pesar� medio kilo.";
					}
					else if ( pesoApr_cuartos == 1 )
					{
						pesoDescription = "Pesar� un cuarto kilo, m�s o menos.";
					}
					else if ( pesoApr_cuartos == 3 )
					{
						pesoDescription = "Pesar� cerca de un kilo.";
					}
				}
				else
				{
					if ( pesoApr_cuartos == 0 || pesoApr_cuartos == 1 )
					{
						pesoDescription = "Pesar� aproximadamente " + pesoApr_kilos + " kilo" + (pesoApr_kilos>1?"s":"") + ".";
					}
					else if ( pesoApr_cuartos == 2 || pesoApr_cuartos == 3 )
					{
						pesoDescription = "Debe de pesar algo m�s de " + pesoApr_kilos + " kilo" + (pesoApr_kilos>1?"s":"") + ".";
					}
				}

				//write( io.getColorCode("description") + pesoDescription + io.getColorCode("reset") + "\n" );
				writeDescription(pesoDescription+"\n");	
			 */


		}
		catch ( WeightLimitExceededException we )
		{
			//write( io.getColorCode("denial")  + "�Llevas demasiado peso para coger "+ ourItem.constructName2True(1,this) + "!\n" + io.getColorCode("reset") );
			write( io.getColorCode("denial") + mundo.getMessages().getMessage("cant.get.item.weight","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem} ) /*+ "\n"*/ + io.getColorCode("reset"));
		}
		catch ( VolumeLimitExceededException ve )
		{
			//write( io.getColorCode("denial")  + "�Llevas objetos demasiado voluminosos para coger "+ ourItem.constructName2True(1,this) + "!\n" + io.getColorCode("reset") );
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

		Vector patternMatchVectorSing = inv.patternMatch ( args , false ).toEntityVector(); //en singular
		Vector patternMatchVectorPlur = inv.patternMatch ( args , true ).toEntityVector(); //en plural

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







	/**
	 * Note: this method sets the state adequately if the action succeeds.
	 * It doesn't guarantee that it will be set if the action fails (although in some cases it does set it).
	 * @param actionName
	 * @param actionArgs
	 * @return
	 */
	boolean executeAction ( String actionName , Object[] actionArgs )
	{

		//ejecutado = execCode( "before" , new Object[] { io } , retval );

		boolean ejecutado_algo = false;

		//Preparar argumentos para m�todo de acci�n general

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
		catch ( ScriptException bshte)
		{
			write("bsh.TargetError found at general before method" );
			bshte.printStackTrace();
			writeError(ExceptionPrinter.getExceptionReport(bshte,"general before method"));
		}

		if ( ejecutado_algo ) return true;

		//Argumentos para m�todo del sujeto: actionArgs

		try
		{
			ejecutado_algo =  this.execCode ( "before_" + actionName , actionArgs );
		}
		catch ( ScriptException bshte)
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
			catch ( ScriptException bshte)
			{
				write("bsh.TargetError found at direct object before method" );
				bshte.printStackTrace();
				writeError(ExceptionPrinter.getExceptionReport(bshte));
			}

			if ( ejecutado_algo ) return true;

		}

		if ( actionArgs.length > 1 && actionArgs[1] instanceof SupportingCode )
		{
			//hay objeto indirecto, su m�todo s�lo sustituye el argumento 1.
			do_args[1] = actionArgs[0];
			SupportingCode indirectObject = (SupportingCode) actionArgs[1];
			try
			{
				ejecutado_algo =  indirectObject.execCode ( "before_io_" + actionName , do_args );
			}
			catch ( ScriptException bshte)
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
			setNewState(IDLE,5);
			boolean retval = doUnwear ( (Item) actionArgs[0] );
			if ( retval == false )
				setNewState(IDLE,1);
			return retval;	
		}

		//se puede invocar multiples veces si el comando es de vestir varias cosas
		else if ( actionName.equalsIgnoreCase("wear") )
		{
			setNewState(IDLE,5);
			boolean retval = doWear ( (Item) actionArgs[0] , true );
			if ( retval == false )
				setNewState(IDLE,1);
			return retval;	
		}

		//se puede invocar multiples veces si el comando es de blandir varias cosas
		else if ( actionName.equalsIgnoreCase("wield") )
		{
			setNewState(IDLE,5);
			boolean retval = doWield ( (Item) actionArgs[0] , true );
			if ( retval == false )
				setNewState(IDLE,1);
			return retval;	
		}

		//se puede invocar multiples veces si el comando es de desenfundar varias armas
		else if ( actionName.equalsIgnoreCase("unwield") )
		{
			setNewState(IDLE,5);
			boolean retval = doUnwield ( (Item) actionArgs[0] );
			if ( retval == false )
				setNewState(IDLE,1);
			return retval;	
		}

		//se puede invocar multiples veces si el comando es de dejar varios objetos
		else if ( actionName.equalsIgnoreCase("drop") )
		{
			setNewState(IDLE,1);
			boolean retval = doDrop ( (Item) actionArgs[0] );
			if ( retval == false )
				setNewState(IDLE,1);
			return retval;
		}


		return false;

	}
	
	//action wrappers

	public boolean drop ( Item it )
	{
		boolean success = executeAction ( "drop" , new Object[]{it} );
		if ( !success ) setNewState ( 1 /*IDLE*/ , 1 );
		return success;
	}
	
	public boolean wear ( Item it )
	{
		boolean success = executeAction ( "wear" , new Object[]{it} );
		if ( !success ) setNewState ( 1 /*IDLE*/ , 1 );
		return success;
	}
	
	public boolean unwear ( Item it )
	{
		boolean success = executeAction ( "unwear" , new Object[]{it} );
		if ( !success ) setNewState ( 1 /*IDLE*/ , 1 );
		return success;
	}
	
	public boolean wield ( Item it )
	{
		boolean success = executeAction ( "wield" , new Object[]{it} );
		if ( !success ) setNewState ( 1 /*IDLE*/ , 1 );
		return success;
	}
	
	public boolean unwield ( Item it )
	{
		boolean success = executeAction ( "unwield" , new Object[]{it} );
		if ( !success ) setNewState ( 1 /*IDLE*/ , 1 );
		return success;
	}
	
	public boolean take ( Item it )
	{
		Inventory inv = null;
		Entity location = it.getLocation();
		if ( location instanceof Room ) inv = ((Room)location).getInventory();
		else if ( location instanceof Item ) inv = ((Item)location).getContents();
		if ( inv != null && inv.contains(it) )
			return executeAction ( "get" , new Object[]{it,inv,""} );
		else
			return false;
	}



	/**
	 * @deprecated Use {@link #doDrop(Item)} instead
	 */
	boolean dejarItem ( Item ourItem )
	{
		return doDrop(ourItem);
	}

	/**
	 * Tries to drop an item instantly. This also includes unwearing/unwielding worn/wielded items.
	 * @param ourItem
	 * @return
	 */
	boolean doDrop ( Item ourItem )
	{
		try
		{
			//Aqu� ir� toda la parafernalia de mirar si est� maldito, etecepunto, etecepunto.
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
				doUnwield(ourItem);
			}
			//si es un wearable que llevamos, dejar tambien de llevarlo
			if ( wornItems != null && wornItems.contains(ourItem) )
			{
				doUnwear(ourItem);
			}

			//write( io.getColorCode("action")  + "Dejas " + ourItem.constructName2True(1,this) + ".\n" + io.getColorCode("reset") );
			writeAction ( mundo.getMessages().getMessage("you.drop.item","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem}));
			
		}
		catch ( WeightLimitExceededException wle )
		{
			/*esta excepci�n es absurda en este caso, nunca saldr� (l�mite de peso de habitaci�n, enorme)*/
			//write( io.getColorCode("denial") + "No puedes dejar aqu� " + ourItem.constructName2True(1,this) + ", hay demasiado peso.\n" + io.getColorCode("reset") );
			write( io.getColorCode("denial") + mundo.getMessages().getMessage("cant.drop.item.weight","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem} ) /*+ "\n"*/ + io.getColorCode("reset"));
			return false;
		}
		catch ( VolumeLimitExceededException vle )
		{
			//write( io.getColorCode("denial") + "No puedes dejar aqu� " + ourItem.constructName2True(1,this) + ", no hay espacio suficiente.\n" + io.getColorCode("reset") );
			write( io.getColorCode("denial") + mundo.getMessages().getMessage("cant.drop.item.volume","$item",ourItem.constructName2True(1,this),new Object[]{this,ourItem} ) /*+ "\n"*/ + io.getColorCode("reset"));
			return false;
		}
		
		//ejecutar eventos onDrop
		try
		{	
			ourItem.execCode("onDrop",new Object[] {this} );
		}
		catch ( ScriptException te )
		{
			//write( io.getColorCode("error") + "bsh.TargetError found at event onDrop , item " + ourItem + io.getColorCode("reset") + "\n"  );
			writeError(ExceptionPrinter.getExceptionReport(te,"onDrop, item " + ourItem));
		}
		
		//setNewState( 1 /*IDLE*/, 1 );
		//ZR_verbo = command;
		return true;
	}



	protected boolean mirarItem ( String arguments , Inventory inv ) 
	{

		boolean mirado = false;

		if ( inv == null || inv.isEmpty() ) return false;

		Vector patternMatchVectorSing = inv.patternMatch ( arguments , false ).toEntityVector(); //en singular
		Vector patternMatchVectorPlur = inv.patternMatch ( arguments , true ).toEntityVector(); //en plural


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
	//�Interfaz Referenciable para estas cosas [constructname...], y ReferenciableList en vez de EntityList?
	{

		boolean mirado = false;

		if ( ml == null || ml.isEmpty() ) return false;

		Vector patternMatchVectorSing = ml.patternMatch ( arguments , false ).toEntityVector(); //en singular
		Vector patternMatchVectorPlur = ml.patternMatch ( arguments , true ).toEntityVector(); //en plural


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
			//open condition added 2011-03-11
			if ( inv.elementAt(i).isContainer() && inv.elementAt(i).isOpen() )
			{
				mirado = mirarItem ( arguments , inv.elementAt(i).getContents() ); //mirar el contenido
				if ( mirado ) break;
			}
		}
		if ( !mirado )
		{
			for ( int i = 0 ; i < inv.size() ; i++ )
			{
				//open condition added 2011-03-11
				if ( inv.elementAt(i).isContainer() && inv.elementAt(i).isOpen() )
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












	//notaci�n un poco inconsistente con la de Entity::copyEntityFields; pero bueno
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
		
		if ( itsCode != null )
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

		
		//shallow!
		
		m.extraDescriptionArrays = extraDescriptionArrays;
		m.extraDescriptionNameArrays = extraDescriptionNameArrays;


	}


	/**
	 * Newer version of createNewInstance() that doesn't need old, unused parameters.
	 * @param cloneInventory Whether the inventory will be copied or left empty.
	 * @param cloneParts Whether the parts will be copied or left empty.
	 * @param uniqueName Unique name of the resulting Mobile.
	 * @return
	 */
	public Mobile createNewInstance ( boolean cloneInventory , boolean cloneParts , String uniqueName )
	{
		return createNewInstance ( mundo , cloneInventory , cloneParts , true , uniqueName );
	}

	public Mobile createNewInstance ( World mundo , boolean cloneInventory , boolean cloneParts , boolean cloneVirtual )
	{
		return createNewInstance ( mundo , cloneInventory , cloneParts , cloneVirtual , null );
	}
	
	//crea un nuevo Mobile que hereda de �ste y lo a�ade al mundo
	public Mobile createNewInstance( World mundo , boolean cloneInventory , boolean cloneParts , boolean cloneVirtual , String uniqueName  ) 
	{
		Mobile it = (Mobile) this.clone();
		it.inheritsFrom = 0;

		if ( this.isInstanceOf == null || StringMethods.isStringOfZeroes ( this.isInstanceOf ) )
		{
			it.isInstanceOf = title;
			Debug.println("1) instanceOf set to " + title);
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

		if ( it.wieldedWeapons != null )
			it.wieldedWeapons = (Inventory) it.wieldedWeapons.clone();
		
		if ( it.wornItems != null )
			it.wornItems = (Inventory) it.wornItems.clone();


		if ( cloneParts || cloneInventory )
		{
			//load relationship map to solve limbs/weapons cloning problem

			Inventory parts = it.getFlattenedPartsInventory();
			Inventory inv = it.getInventory();

			if ( it.wieldedWeapons == null ) it.wieldedWeapons = new Inventory(10000,10000);
			if ( it.wornItems == null ) it.wornItems = new Inventory(10000,10000);
			
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
		
		if ( uniqueName == null ) it.title = mundo.generateUnusedUniqueName(this.getUniqueName()); 
		else it.title = uniqueName;
		
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
		
		//write("No est�s blandiendo ning�n arma �til para suicidarte.\n");
		writeDenial(mundo.getMessages().getMessage("no.suicide.weapon",new Object[]{this}));

	}

	public void suicideWith ( Weapon w )
	{
		habitacionActual.reportAction ( this,null,new Entity[]{w} ,
				mundo.getMessages().getMessage("someone.suicides",new Object[]{this,w}),
				mundo.getMessages().getMessage("you.suicide",new Object[]{this,w}),
				mundo.getMessages().getMessage("you.suicide",new Object[]{this,w}),
				//"$1 se suicida con $3.\n", 
				//"Te suicidas con $3.\n", 
				//"Te suicidas con $3.\n", 
				true );
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


		Vector[] patternMatchVectorSingSing = Matches.toEntityVectors(el1.patternMatchTwo ( el2 , arguments , false , false )); //en singular y singular
		Vector[] patternMatchVectorSingPlur = Matches.toEntityVectors(el1.patternMatchTwo ( el2 , arguments , false , true )); //en singular y plural
		Vector[] patternMatchVectorPlurSing = Matches.toEntityVectors(el1.patternMatchTwo ( el2 , arguments , true , false )); //en plural y singular
		Vector[] patternMatchVectorPlurPlur = Matches.toEntityVectors(el1.patternMatchTwo ( el2 , arguments , true , true )); //en plural y plural

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
		InputOutputClient io = pl.getClient();
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
	
	public boolean playMidiIfAvailable ( URL u , int loopCount )
	{
		return playMidiIfAvailable ( u.toString() , loopCount );
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
	
	public boolean playMidiIfAvailable ( URL u  )
	{
		return playMidiIfAvailable ( u.toString() );
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


	public boolean playAudioIfAvailable ( URL u )
	{
		return playAudioIfAvailable(u.toString());
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

	public boolean playAudioIfAvailable ( URL u , int loopTimes )
	{
		return playAudioIfAvailable(u.toString() , loopTimes );
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
	
	public boolean stopAudioIfAvailable ( URL u  )
	{
		return stopAudioIfAvailable ( u.toString() );
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
	
	public boolean stopAudioIfAvailable ( URL u , boolean fade  )
	{
		return stopAudioIfAvailable ( u.toString() , fade );
	}
	
	public boolean stopAudioIfAvailable ( String audioFileName , boolean fade )
	{
		if ( ! fade ) return stopAudioIfAvailable ( audioFileName );
		else
		{
			SoundClient sc = getSoundClientIfAvailable();
			if ( sc == null ) return false;
			try
			{
				sc.audioFadeOut(audioFileName,1.0);
				return true;
			}
			catch ( Exception e )
			{
				return false;
			}
		}
	}
	
	public boolean playAudioIfAvailable ( URL u , int loopTimes , boolean fade )
	{
		return playAudioIfAvailable(u.toString() , loopTimes , fade );
	}
	
	public boolean playAudioIfAvailable ( String audioFileName , int loopTimes , boolean fade )
	{
		if ( !fade ) return playAudioIfAvailable ( audioFileName , loopTimes );
		else
		{
			SoundClient sc = getSoundClientIfAvailable();
			if ( sc == null ) return false;
			try
			{
				sc.audioFadeIn(audioFileName,loopTimes,1.0,1.2);
				return true;
			}
			catch ( Exception e )
			{
				return false;
			}
		}
	}
	
	public boolean setAudioGainIfAvailable ( String audioFileName , double gain )
	{
		SoundClient sc = getSoundClientIfAvailable();
		if ( sc == null ) return false;
		try
		{
			sc.audioSetGain ( audioFileName , gain );
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}
	
	public boolean setAudioGainIfAvailable ( URL u , double gain )
	{
		return setAudioGainIfAvailable ( u.toString() , gain );
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


	/**
	 * Obtain a list with the singular reference names of the mobile, in order.
	 */
	public List getSingularReferenceNames()
	{
		return (List) ((ArrayList)respondToSing).clone();
	}
	
	/**
	 * Obtain a list with the plural reference names of the mobile, in order.
	 */
	public List getPluralReferenceNames()
	{
		return (List) ((ArrayList)respondToPlur).clone();
	}
	
	
	
	/**
	 * Adds a new singular reference name, with lower priority than the existing reference names.
	 */
	public void addSingularReferenceName(String newName)
	{
		respondToSing.add(newName);
		mundo.getSpellChecker().addNewName(newName);
	}
	
	/**
	 * Adds a new singular reference name, located at the given index that defines its priority.
	 */
	public void addSingularReferenceName(int index,String newName)
	{
		respondToSing.add(index,newName);
		mundo.getSpellChecker().addNewName(newName);
	}
	
	/**
	 * Removes a singular reference name from the list of such names, if present. Returns whether it has actually been removed or not.
	 * Note that this does not remove any word from the world's vocabulary used by the spell checker.
	 * Call the rebuild() method in the world's spell checker if this effect is desired (but beware, that takes a nontrivial amount of time)
	 */
	public boolean removeSingularReferenceName(String oldName)
	{
		return respondToSing.remove(oldName);
	}
	
	/**
	 * Adds a new plural reference name, with lower priority than the existing reference names.
	 */
	public void addPluralReferenceName(String newName)
	{
		respondToPlur.add(newName);
		mundo.getSpellChecker().addNewName(newName);
	}
	
	/**
	 * Adds a new plural reference name, located at the given index that defines its priority.
	 */
	public void addPluralReferenceName(int index,String newName)
	{
		respondToPlur.add(index,newName);
		mundo.getSpellChecker().addNewName(newName);
	}
	
	/**
	 * Removes a plural reference name from the list of such names, if present. Returns whether it has actually been removed or not.
	 * Note that this does not remove any word from the world's vocabulary used by the spell checker.
	 * Call the rebuild() method in the world's spell checker if this effect is desired (but beware, that takes a nontrivial amount of time)
	 */
	public boolean removePluralReferenceName(String oldName)
	{
		return respondToPlur.remove(oldName);
	}
	
	
	
	

	public ObjectCode getAssociatedCode() 
	{
		return itsCode;
	}
	
	
	/**
	 * Builds a list with all the extra description names and returns it.
	 * Careful, each call to this method builds the list!
	 * @return
	 */
	public List getExtraDescriptionNames()
	{
		List result = new ArrayList();
		for ( int i = 0 ; i < extraDescriptionNameArrays.size() ; i++ )
		{
			String[] ar = (String[]) extraDescriptionNameArrays.get(i);
			for ( int j = 0 ; j < ar.length ; j++ )
			{
				result.add(ar[j]);
			}
		}
		return result;
	}
	
	/**
	 * Returns a list of all the locations this Mobile is in.
	 * Since a Mobile can only be in one place at once, and that place must be a Room (not e.g.
	 * a container or another Mobile), this method always returns a list containing that room.
	 * @return
	 */
	public EntityList getLocations()
	{
		EntityList result = new EntityList();
		result.addEntity(habitacionActual);
		return result;
	}
	
	/**
	 * Returns the Room this Mobile is in.
	 * This is equivalent to getRoom(), but is added for convenience, since a similar getLocation()
	 * method also exists for Items (although in this latter case, the location need not be a Room).
	 */
	public Entity getLocation()
	{
		return habitacionActual;
	}
	
	/**
	 * Changes the Room this Mobile is in, doing all the necessary internal data structure updates.
	 * This is equivalent to setRoom(r), but is added for convenience, since a similar moveTo()
	 * method also exists for Items (although the latter can also take non-Room entities as
	 * parameter).
	 * @param r
	 */
	public void moveTo ( Room r )
	{
		setRoom(r);
	}

	public void meterObjetoEnZRSingular(Item obj) {
		if ( obj.getGender() )
		{
			mentions.setLastMentionedObjectMS( obj.getBestReferenceName ( false ) );
			//ZR_objeto_masculino_singular = obj.getBestReferenceName ( false );
		}
		else
		{
			mentions.setLastMentionedObjectFS( obj.getBestReferenceName ( false ) );
			//ZR_objeto_femenino_singular = obj.getBestReferenceName ( false );
		}
		//ZR_objeto_singular = obj.getBestReferenceName ( false );
		mentions.setLastMentionedObjectS( obj.getBestReferenceName ( false ) );
	}

	public void resetZRPlural() {
		mentions.setLastMentionedObjectMP("");
		mentions.setLastMentionedObjectFP("");
		mentions.setLastMentionedObjectP("");
		/*
		ZR_objeto_masculino_plural = "";
		ZR_objeto_femenino_plural = "";
		ZR_objeto_plural = "";
		*/
	}

	public void meterObjetoEnZRPlural(Item obj) {
		if ( obj.getGender() )
		{
			/*
			if ( !ZR_objeto_masculino_plural.equals("") )
				ZR_objeto_masculino_plural += ", ";
			ZR_objeto_masculino_plural += obj.getBestReferenceName ( true );
			*/
			if ( !mentions.getLastMentionedObjectMP().equals("") )
				mentions.setLastMentionedObjectMP( mentions.getLastMentionedObjectMP() + ", ");
			mentions.setLastMentionedObjectMP( mentions.getLastMentionedObjectMP() + obj.getBestReferenceName ( true ));
		}
		else
		{
			/*
			if ( !ZR_objeto_femenino_plural.equals("") )
				ZR_objeto_femenino_plural += ", ";
			ZR_objeto_femenino_plural += obj.getBestReferenceName ( true );
			*/
			if ( !mentions.getLastMentionedObjectFP().equals("") )
				mentions.setLastMentionedObjectFP( mentions.getLastMentionedObjectFP() + ", ");
			mentions.setLastMentionedObjectFP( mentions.getLastMentionedObjectFP() + obj.getBestReferenceName ( true ));
		}
		if ( !mentions.getLastMentionedObjectP().equals("") )
			mentions.setLastMentionedObjectP( mentions.getLastMentionedObjectP() + ", ");
		mentions.setLastMentionedObjectP( mentions.getLastMentionedObjectP() + obj.getBestReferenceName ( true ));
		/*
		if ( !ZR_objeto_plural.equals("") )
			ZR_objeto_plural += ", ";
		ZR_objeto_plural += obj.getBestReferenceName ( true );	
		*/
	}

	public Mentions getMentions() {
		return mentions;
	}
	
	public boolean executeParseCommandForTwoEntities ( Entity obj1 , Entity obj2 , String args1 , String args2 , List path1 , List path2 , boolean onWorld )
	{
		
		
		boolean ejecutado = false;
		String fullArguments = args1 + " " + args2;
		
		for ( int ip1 = path1.size()-1 ; ip1 >= 0 ; ip1-- )
		{
			for ( int jp1 = path2.size()-1 ; jp1 >= 0 ; jp1-- )
			{
				Entity currentObject1 = (Entity) path1.get(ip1);	   
				Entity currentObject2 = (Entity) path2.get(jp1);


				//ejecutar parseCommandOnContents() de objeto 1
				if ( !onWorld && currentObject1 instanceof SupportingCode )
				{
					try
					{
						//parseCommandOnContents(Mobile,command,args,chain)
						ejecutado = ejecutado || ((SupportingCode)currentObject1).execCode ( "parseCommandOnContentsObj1" , new Object[] { this , command , args1 , args2 , path1 , path2 , currentObject2 } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsObj1(), command was " + command + " " + fullArguments + ", entity " + currentObject1 + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContentsObj1(), command was " + command + " " + fullArguments + ", entity " + currentObject1));
					}

					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)currentObject1).execCode ( "parseCommandOnContentsTwoObjects" , new Object[] { this , command , args1 , args2 , path1 , path2 , currentObject2 } );
						}
						catch ( ScriptException te )
						{
							//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsTwoObjects(), command was " + command + " " + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContentsTwoObjects(), command was " + command + " " + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2));
						}
					}
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)currentObject1).execCode( "parseCommandOnContentsGeneric",  new Object[] { this , command, args1 , args2 , path1 , path2 , currentObject1 , currentObject2 , new Boolean(true) /*isFirst==true*/ } );
						}
						catch ( ScriptException te )
						{
							//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric(), command was " + command + " " + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContentsGeneric(), command was " + command + " " + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2));
						}
					}
				}


				//ejecutar parseCommandOnContents() de objeto 2
				if ( !onWorld && currentObject2 instanceof SupportingCode )
				{
					try
					{
						//parseCommandOnContents(Mobile,command,args,chain)
						ejecutado = ejecutado || ((SupportingCode)currentObject2).execCode ( "parseCommandOnContentsObj2" , new Object[] { this , command , args1 , args2 , path1 , path2 , currentObject1 } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsObj2(), command was " + command + " " + fullArguments + ", entity " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContentsObj2(), command was " + command + " " + fullArguments + ", entity " + currentObject2));
					}

					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)currentObject2).execCode ( "parseCommandOnContentsTwoObjects" , new Object[] { this , command , args1 , args2 , path1 , path2 , currentObject1 } );
						}
						catch ( ScriptException te )
						{
							//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsTwoObjects(), command was " + command + " " + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContentsTwoObjects(), command was " + command + " " + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2));
						}
					}
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)currentObject2).execCode( "parseCommandOnContentsGeneric",  new Object[] { this , command, args1 , args2 , path1 , path2 , currentObject1 , currentObject2 , new Boolean(false) /*isFirst==false*/ } );
						}
						catch ( ScriptException te )
						{
							//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric(), command was " + command + " " + fullArguments + ", first object was " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContentsGeneric(), command was " + command + " " + fullArguments + ", first object was " + currentObject1 + ", second object was " + currentObject2));
						}
					}
				}
				
				
				//ejecutar parseCommandOnContents() en mundo referido a objetos
				if ( onWorld )
				{
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || mundo.execCode ( "parseCommandOnContentsTwoObjects" , new Object[] { this , command , args1 , args2 , path1 , path2 , currentObject1 , currentObject2 } );
						}
						catch ( ScriptException te )
						{
							//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsTwoObjects() executed from world, command was " + command + " " + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te,"bsh.TargetError found at parseCommandOnContentsTwoObjects() executed from world, command was " + command + " " + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2));
						}
					}
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || mundo.execCode( "parseCommandOnContentsGeneric",  new Object[] { this , command, args1 , args2 , path1 , path2 , currentObject1 , currentObject2  } );
						}
						catch ( ScriptException te )
						{
							//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric() executed from world, command was " + command + " " + fullArguments + ", first object was " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te,"bsh.TargetError found at parseCommandOnContentsGeneric() executed from world, command was " + command + " " + fullArguments + ", first object was " + currentObject1 + ", second object was " + currentObject2));
						}
					}		  
				}


			}
		}


		//standard (not "onContents") parseCommands:
		if ( (path1.size() == 1 && path2.size() == 1) || this.getPropertyValueAsBoolean("containedItemsInScope") ) 
		//in default mode, regular parseCommands are executed only for things NOT contained in anything (hence the path size check) 
		//in extended scope mode, also for contained items
		{
			//	    ejecutar parseCommand() de objeto 1
			if ( !onWorld && obj1 instanceof SupportingCode )
			{
			    	if ( !ejecutado )
			    	{
    					try
    					{
    						ejecutado = ejecutado || ((SupportingCode)obj1).execCode ( "parseCommandObj1" , new Object[] { this , command , args1 , args2 , obj2 } );
    					}
    					catch ( ScriptException te )
    					{
    						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandObj1(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj1.getID() + ", second object was " + obj2.getID() + ", error was " + te + io.getColorCode("reset") );
    						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandObj1(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj1.getID() + ", second object was " + obj2.getID()));
    					}
			    	}
				if ( !ejecutado )
				{
					try
					{
						ejecutado = ejecutado || ((SupportingCode)obj1).execCode ( "parseCommandTwoObjects" , new Object[] { this , command , args1 , args2 , obj2 } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandTwoObjects(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj1.getID() + ", second object was " + obj2.getID() + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandTwoObjects(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj1.getID() + ", second object was " + obj2.getID()));
					}
				}
				if ( !ejecutado )
				{
					try
					{
						//parseCommandGeneric ( Player aCreature , String verb , Sring args1 , Sring args2 , Entity obj1 , Entity obj2 , boolean isFirst )
						ejecutado = ejecutado || ((SupportingCode)obj1).execCode ( "parseCommandGeneric" , new Object[] { this , command , args1 , args2 , obj1 , obj2 , new Boolean(true) } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj1 + ", second object was " + obj2 + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandGeneric(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj1 + ", second object was " + obj2));
					}
				}
			} //obj1 instof suppcode
			if ( ejecutado ) //c�digo hizo end()
			{
				//luego esto lo hara el codigo
				setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}

			//	    ejecutar parseCommand() de objeto 2
			if ( !onWorld && obj2 instanceof SupportingCode )
			{
			    	if ( !ejecutado )
			    	{
    					try
    					{
    						ejecutado = ejecutado || ((SupportingCode)obj2).execCode ( "parseCommandObj2" , new Object[] { this , command , args1 , args2 , obj1 } );
    					}
    					catch ( ScriptException te )
    					{
    						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandObj2(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj2.getID() + ", first object was " + obj1.getID() + ", error was " + te + io.getColorCode("reset") );
    						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandObj2(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj2.getID() + ", first object was " + obj1.getID()));
    					}
			    	}
				if ( !ejecutado )
				{
					try
					{
						ejecutado = ejecutado || ((SupportingCode)obj2).execCode ( "parseCommandTwoObjects" , new Object[] { this , command , args1 , args2 , obj1 } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandTwoObjects(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj2.getID() + ", first object was " + obj1.getID() + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandTwoObjects(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj2.getID() + ", first object was " + obj1.getID()));
					}
				}
				if ( !ejecutado )
				{
					try
					{
						//parseCommandGeneric ( Player aCreature , String verb , Sring args1 , Sring args2 , Entity obj1 , Entity obj2 , boolean isFirst )
						ejecutado = ejecutado || ((SupportingCode)obj2).execCode ( "parseCommandGeneric" , new Object[] { this , command , args1 , args2 , obj1 , obj2 , new Boolean(false) } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj1 + ", second object was " + obj2 + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandGeneric(), command was " + command + " " + args1 + " " + args2 + ", entity number " + obj1 + ", second object was " + obj2));
					}
				}
			} //obj2 instof suppcode
			
			if ( onWorld )
			{
			    if ( !ejecutado )
			    {
					try
					{
						ejecutado = ejecutado || mundo.execCode ( "parseCommandTwoObjects" , new Object[] { this , command , args1 , args2 , obj1 , obj2 } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandTwoObjects() executed from world, command was " + command + " " + args1 + " " + args2 + ", entity number " + obj2.getID() + ", first object was " + obj1.getID() + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"bsh.TargetError found at parseCommandTwoObjects() executed from world, command was " + command + " " + args1 + " " + args2 + ", entity number " + obj2.getID() + ", first object was " + obj1.getID()));
					}
				
			    }
			    if ( !ejecutado )
			    {
					try
					{
						//parseCommandGeneric ( Player aCreature , String verb , Sring args1 , Sring args2 , Entity obj1 , Entity obj2 , boolean isFirst )
						ejecutado = ejecutado || mundo.execCode ( "parseCommandGeneric" , new Object[] { this , command , args1 , args2 , obj1 , obj2 } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric() executed from world, command was " + command + args1 + args2 + ", entity number " + obj1 + ", second object was " + obj2 + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"bsh.TargetError found at parseCommandGeneric() executed from world, command was " + command + args1 + args2 + ", entity number " + obj1 + ", second object was " + obj2));
					}
			    }
			}

		}
		
		return ejecutado;

		
	}
	

	/**
	 * Note: in practice there seems to be no difference at all between arguments and fullArguments - remove one of them?
	 * @param posiblesObjetivos
	 * @param arguments
	 * @param fullArguments
	 * @param onWorld
	 * @return
	 */
	public boolean resolveParseCommandForTwoEntities ( EntityList posiblesObjetivos , String arguments , String fullArguments , boolean onWorld )
	{

		boolean ejecutado = false;

		List matches_ss = ParserMethods.parseReferencesToEntitiesInRecursive  ( arguments,posiblesObjetivos,posiblesObjetivos,false,false);
		List matches_sp = ParserMethods.parseReferencesToEntitiesInRecursive  ( arguments,posiblesObjetivos,posiblesObjetivos,false,true);
		List matches_ps = ParserMethods.parseReferencesToEntitiesInRecursive  ( arguments,posiblesObjetivos,posiblesObjetivos,true,false);
		List matches_pp = ParserMethods.parseReferencesToEntitiesInRecursive  ( arguments,posiblesObjetivos,posiblesObjetivos,true,true);
				
		List allMatches = new ArrayList();
		allMatches.addAll(matches_ss);
		allMatches.addAll(matches_sp);
		allMatches.addAll(matches_ps);
		allMatches.addAll(matches_pp);
		
		
		//detect the case where the command does match two entities, but it matches one entity even better
		//e.g. item1 "libro verde", "libro", "verde"
		//item2 "libro rojo", "libro", "rojo"
		//"coger el libro rojo" matches (item1,item2) with priority 2, but it matches item2 only with priority 1 (better).
		if ( !allMatches.isEmpty() && matchedOneEntity )
		{
			int priority = ((SentenceInfo)allMatches.get(0)).getPriority();
			//System.err.println("Args: " + arguments);
			//System.err.println("One entity prio: " + oneEntityPriority);
			//System.err.println("Two entity prio: " + ((SentenceInfo)allMatches.get(0)).getPriority() + " for " + allMatches.get(0));
			if ( oneEntityPriority > 0 && priority > oneEntityPriority ) //the command matches two entities, but the best match is for one entity. Don't exec parseCommands for two entities.
			{
				matchedTwoEntities = false;
				matchedTwoEntitiesPermissive = false;
				return false;
			}
		}

		matchedTwoEntitiesPermissive = ( allMatches.size() > 0 );

		/**
		 * Store calls to parseCommands on two entities to avoid doing repeated invocations
		 * for (different args assignments of) the same entities. 
		 */
		class TwoEntityParseCommandAttempt
		{
			private Entity obj1;
			private Entity obj2;
			private List path1;
			private List path2;
			public TwoEntityParseCommandAttempt(Entity obj1,Entity obj2,List path1,List path2)
			{
				this.obj1 = obj1;
				this.obj2 = obj2;
				this.path1 = path1;
				this.path2 = path2;
			}
			public int hashCode() 
			{
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((obj1 == null) ? 0 : obj1.hashCode());
				result = prime * result
						+ ((obj2 == null) ? 0 : obj2.hashCode());
				result = prime * result
						+ ((path1 == null) ? 0 : path1.hashCode());
				result = prime * result
						+ ((path2 == null) ? 0 : path2.hashCode());
				return result;
			}
			public boolean equals(Object obj) 
			{
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (!(obj instanceof TwoEntityParseCommandAttempt))
					return false;
				TwoEntityParseCommandAttempt other = (TwoEntityParseCommandAttempt) obj;
				return this.obj1 == other.obj1
				&& this.obj2 == other.obj2
				&& this.path1.equals(other.path1)
				&& this.path2.equals(other.path2);
			}
		}
		Set attempts = new LinkedHashSet();
		
		
		for ( int i = 0 ; i < allMatches.size() ; i++ )
		{
			SentenceInfo si = (SentenceInfo) allMatches.get(i);
			String args1 = si.getArgs1();
			String args2 = si.getArgs2();
			Entity obj1 = si.getObj1();
			Entity obj2 = si.getObj2();
			List path1 = si.getPath1();
			List path2 = si.getPath2();

			TwoEntityParseCommandAttempt attempt = new TwoEntityParseCommandAttempt(obj1,obj2,path1,path2);
			if ( getPropertyValueAsBoolean("multipleArgsMatches") || !attempts.contains(attempt) )
			{
				ejecutado = executeParseCommandForTwoEntities ( obj1 , obj2 , args1 , args2 , path1 , path2 , onWorld );
				if ( !getPropertyValueAsBoolean("multipleArgsMatches") )
					attempts.add(attempt);
			}
			
			if ( ejecutado ) //c�digo hizo end()
			{
				//luego esto lo hara el codigo
				//setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}
			
		} //end for each possible match
		

		//no end() has been hit: try parseCommands for one entity!
		Set triedPaths = new LinkedHashSet();
		for ( int i = 0 ; i < allMatches.size() ; i++ )
		{
			SentenceInfo si = (SentenceInfo) allMatches.get(i);
			String args1 = si.getArgs1();
			List path1 = si.getPath1();
			if ( !triedPaths.contains(path1) )
			{
				ejecutado = resolveParseCommandForOneEntity ( posiblesObjetivos , args1 , fullArguments , onWorld , false );
				triedPaths.add(path1);
			}
			
			if ( !ejecutado )
			{
				si = (SentenceInfo) allMatches.get(i);
				String args2 = si.getArgs2();
				List path2 = si.getPath2();
				if ( !triedPaths.contains(path2) )
				{
					ejecutado = resolveParseCommandForOneEntity ( posiblesObjetivos , args2 , fullArguments , onWorld , false );
					triedPaths.add(path2);
				}
			}
			
			if ( ejecutado ) //c�digo hizo end()
			{
				//luego esto lo hara el codigo
				//setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}
		}
		
		return false;

	}


	public boolean resolveParseCommandForOneComponent ( EntityList posiblesObjetivos , String arguments )
	{
		
		boolean ejecutado = false;
		
		for ( int i = 0 ; i < posiblesObjetivos.size() && !ejecutado ; i++ )
		{
			Entity currentEntity = posiblesObjetivos.get(i);
			if 
			( 
				! ejecutado && (
					( currentEntity instanceof Room && ((Room)currentEntity).getExtraDescription(arguments,this) != null ) ||
					( currentEntity instanceof Item && ((Item)currentEntity).getExtraDescription(arguments,this) != null ) ||
					( currentEntity instanceof Mobile && ((Mobile)currentEntity).getExtraDescription(arguments,this) != null ) 
				)
			)
			{
				try
				{
					//parseCommandOnComponent(Mobile,command,args,chain)
					ejecutado = ejecutado || ((SupportingCode)currentEntity).execCode ( "parseCommandOnComponent" , new Object[] { this , command , arguments } );
				}
				catch ( ScriptException te )
				{
					//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnComponent(), command was " + command + " " + arguments + ", entity " + currentEntity + ", error was " + te + io.getColorCode("reset") );
					writeError(ExceptionPrinter.getExceptionReport(te,"bsh.TargetError found at parseCommandOnComponent(), command was " + command + " " + arguments + ", entity " + currentEntity));
				}
			}
		}
		
		if ( ejecutado ) //c�digo hizo end()
		{
			//luego esto lo hara el codigo
			//setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
		}
		return ejecutado;
		
	}
		

	/**
	 * Executes all the one-entity parseCommands applicable with the current command to the given arguments and path to a matching entity.
	 * Note that the objects itself on which to call the parseCommands are taken from the path param.
	 * @param currentObject
	 * @param fullArguments
	 * @param path
	 * @return
	 */
	private boolean executeParseCommandForOneEntity ( String fullArguments , List path , boolean onWorld , boolean enableGenerics , boolean plural )
	{
		
		boolean ejecutado = false;
		
		//TODO probably remove this if so onContents is executed always and not only for, well, contents
		//removed. Replace this if if onContents is not to be executed on objects not contained in others
		//if ( objetivoVector.size() > 1 )
		//{
		
		Entity currentObject;

		for ( int i = path.size()-1; i >= 0 ; i-- )
		{
			currentObject = (Entity) path.get(i);

			//ejecutar parseCommandOnContents() de objeto
			if ( !onWorld && currentObject instanceof SupportingCode )
			{
				try
				{
					//parseCommandOnContents(Mobile,command,args,chain)
					ejecutado = ejecutado || ((SupportingCode)currentObject).execCode ( "parseCommandOnContents" , new Object[] { this , command , fullArguments , path } );
				}
				catch ( ScriptException te )
				{
					//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContents(), command was " + command + " " + fullArguments + ", entity " + currentObject + ", error was " + te + io.getColorCode("reset") );
					writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContents(), command was " + command + " " + fullArguments + ", entity " + currentObject));
				}
				if ( !ejecutado && enableGenerics )
				{
					try
					{

						ejecutado = ejecutado || ((SupportingCode)currentObject).execCode ( "parseCommandOnContentsGeneric" , new Object[] { this , command , fullArguments , "" , path , null , currentObject , null , new Boolean(true) } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric(), command was " + command + " " + fullArguments + ", entity number " + currentObject + ", second object was " + null + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContentsGeneric(), command was " + command + " " + fullArguments + ", entity number " + currentObject + ", second object was " + null));
					}
				}
			}
			//lo mismo de mundo
			if ( onWorld )
			{
			    try
			    {
				ejecutado = ejecutado || mundo.execCode ( "parseCommandOnContents" , new Object[] { this , command , fullArguments  , path , currentObject } );
			    }
			    catch ( ScriptException te )
			    {
			    	//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContents() executed from world, command was " + command + " " + fullArguments + ", entity " + currentObject + ", error was " + te + io.getColorCode("reset") );
			    	writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContents() executed from world, command was " + command + " " + fullArguments + ", entity " + currentObject));
			    }
			    if ( !ejecutado && enableGenerics )
				{
					try
					{

						ejecutado = ejecutado || mundo.execCode ( "parseCommandOnContentsGeneric" , new Object[] { this , command , fullArguments , "" , path , null , currentObject , null } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric() executed from world, command was " + command + " " + fullArguments + ", entity number " + currentObject + ", second object was " + null + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandOnContentsGeneric() executed from world, command was " + command + " " + fullArguments + ", entity number " + currentObject + ", second object was " + null));
					}
				}
			}

		}

		//}


		Entity objetivo = (Entity) path.get(0);

		//�ste ser� el objeto principal sobre el que ejecutemos el comando en singular (cualquiera, aunque no sea BSH) si hace referencia a un solo objeto
		if ( objetivo instanceof Item )
		{
			if ( plural )
				meterObjetoEnZRPlural((Item)objetivo);
			else
				meterObjetoEnZRSingular((Item)objetivo);
		}



		//THIS CHUNK OF CODE IS ABSOLUTE LEGACY
		//"LOS INMORTALES" IS THE ONLY REASON TO KEEP IT
		//para cada grupo de primeras palabras del string (dar, dar ca�a, dar ca�a al, dar ca�a al di�bolo) intentamos ejecutar c�digo EVA.
		for ( int i = 0 ; i <= StringMethods.numToks (arguments,' ') && !ejecutado ; i++ )
		{

			Item ourItem;
			if ( objetivo instanceof Item )
				ourItem = (Item) objetivo;
			else
				continue;	

			try
			{
				if ( i == 0 ) ejecutado = ejecutado || ourItem.execCode ( "command_" + command , "this: " + ourItem.getID() + "\n" + "room: " + habitacionActual.getID() + "\n" + "location: inventory" + "\n" + "player: " + getID() );
				else ejecutado = ejecutado || ourItem.execCode ( ( "command_" + command + "_" + StringMethods.getToks( arguments , 1,  i , ' ' ) ).replace(' ','_') , "this: " + ourItem.getID() + "\n" + "room: " + habitacionActual.getID() + "\n" + "location: inventory" + "\n" + "player: " + getID()  );
			}
			catch ( EVASemanticException exc ) 
			{
				write(io.getColorCode("error") + "EVASemanticException found at command " + ( command + StringMethods.getToks( arguments , 1,  i , ' ' ) ).replace(' ','_') + ", item number " + ourItem.getID() + io.getColorCode("reset") );
			}
		}



		//ejecutar parseCommand() de objeto
		if ( !onWorld && objetivo instanceof SupportingCode )
		{
			//in default mode, the following if ensures that standard parseCommand's are not executed on objects that are inside containers.
			//to define commands on those objects we have to define parseCommandOnContents.
			//in extended scope mode, this check is bypassed.
			if ( path.size() == 1 || this.getPropertyValueAsBoolean("containedItemsInScope") )
			{
				if ( !ejecutado )
				{
					try
					{
						ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommand" , new Object[] { this , command , fullArguments } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + " " + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommand(), command was " + command + " " + fullArguments + ", entity " + objetivo));
					}
				}
				if ( !ejecutado && enableGenerics )
				{
					try
					{
						ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null , new Boolean(true) } );
					}
					catch ( ScriptException te )
					{
						//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + " " + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandGeneric(), command was " + command + " " + fullArguments + ", entity number " + objetivo + ", second object was " + null));
					}
				}
			}
		}
		//lo mismo de mundo
		if ( onWorld )
		{
		    if ( !ejecutado )
		    {
    			    try
    			    {
    				ejecutado = ejecutado || mundo.execCode ( "parseCommand" , new Object[] { this , command , fullArguments , objetivo } );
    			    }
    			    catch ( ScriptException te )
    			    {
    			    	//write(io.getColorCode("error") + "bsh.TargetError found at parseCommand() executed from world, command was " + command + " " + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
    			    	writeError(ExceptionPrinter.getExceptionReport(te,"parseCommand() executed from world, command was " + command + " " + fullArguments + ", entity " + objetivo));
    			    }
		    }
		    if ( !ejecutado && enableGenerics )
		    {
				try
				{
					ejecutado = ejecutado || mundo.execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null  } );
				}
				catch ( ScriptException te )
				{
					//write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric() executed from world, command was " + command + " " + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
					writeError(ExceptionPrinter.getExceptionReport(te,"parseCommandGeneric() executed from world, command was " + command + " " + fullArguments + ", entity " + objetivo + ", second object was " + null));
				}
		    }
		}
		
		return ejecutado;
				
	}
	
	/**
	 * @param objetivos_s
	 * @param objetivos_p
	 * @param arguments
	 * @param onWorld -> true if it's the world parsecommands for one entity, false if it's the entity parsecommands
	 * @param enableGenerics -> if true, parseCommand*Generic methods are also executed. If false, they aren't.
	 * @return true if an end() has been hit, false otherwise
	 */
	public boolean resolveParseCommandForOneEntity ( EntityList posiblesObjetivos , String arguments , String fullArguments , boolean onWorld , boolean enableGenerics )
	{


		boolean ejecutado = false;

		Vector objetivos_s = ParserMethods.refersToEntityInRecursive ( arguments,posiblesObjetivos,false ).toPathVector();
		Vector objetivos_p = ParserMethods.refersToEntityInRecursive ( arguments,posiblesObjetivos,true ).toPathVector();
		
		//objetivos_s has the form: [ [pearl,chest,box] , [pearl,bottle] ]
		//(each component is a path to a matched object, only the 1st (top priority) is really used).
		//in the plural case, all components are used.

		//TODO
		//we can now migrate this to refersToEntityInRecursive. This returns a Vector of Vectors with paths to stuff.
		//we can call parseCommandOnContents on the intermediate nodes in the path and finally parseCommand on the final entity
		//(if the previous parseCommandOnContents have let us, of course).
		//LET'S TRY TO F...ING DO THIS
		//[pearl,box,chest] vector
		
		matchedOneEntityPermissive = ( objetivos_s.size() + objetivos_p.size() > 0 );
	
		if ( objetivos_s.size() > 0 )
		{

			Vector objetivoVector = (Vector) objetivos_s.get(0);

			ejecutado = executeParseCommandForOneEntity(fullArguments,objetivoVector,onWorld,enableGenerics,false);
			
			if ( ejecutado ) //c�digo hizo end()
			{
				//luego esto lo hara el codigo
				//setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}

		}
		else if ( objetivos_p.size() > 0 )
		{
			//no era en singular, probamos en plural.
			Entity objetivo;			
			resetZRPlural();			
			for ( int w = 0 ; w < objetivos_p.size() ; w++ )
			{

				//begin copy-pasted from singular
				Vector objetivoVector = (Vector) objetivos_p.get(w);
				
				ejecutado = executeParseCommandForOneEntity(fullArguments,objetivoVector,onWorld,enableGenerics,true);
				
				if ( ejecutado ) //c�digo hizo end()
				{
					//luego esto lo hara el codigo
					//setNewState( 1 , 1 );
					mentions.setLastMentionedVerb(command);
					return true;
				}

				/*
				//removed so that onContents is also executed on object itself.
				//Replace this if if it is to be executed strictly on contents only.
				//if ( objetivoVector.size() > 1 )
				//{

					Entity currentObject;
					for ( int i = objetivoVector.size()-1; i >= 0 ; i-- )
					{
						currentObject = (Entity) objetivoVector.get(i);

						//ejecutar parseCommandOnContents() de objeto
						if ( !onWorld && currentObject instanceof SupportingCode )
						{
							if ( !ejecutado )
							{
								try
								{
									//parseCommandOnContents(Mobile,command,args,chain)
									ejecutado = ejecutado || ((SupportingCode)currentObject).execCode ( "parseCommandOnContents" , new Object[] { this , command , fullArguments , objetivoVector } );
								}
								catch ( ScriptException te )
								{
									write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContents(), command was " + command + fullArguments + ", entity " + currentObject + ", error was " + te + io.getColorCode("reset") );
									writeError(ExceptionPrinter.getExceptionReport(te));
								}
							}
							if ( !ejecutado && enableGenerics )
							{
								try
								{
									ejecutado = ejecutado || ((SupportingCode)currentObject).execCode ( "parseCommandOnContentsGeneric" , new Object[] { this , command , fullArguments , "" , objetivoVector , null  , currentObject , null , new Boolean(true)  } );
								}
								catch ( ScriptException te )
								{
									write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric(), command was " + command + fullArguments + ", entity number " + currentObject + ", second object was " + null + ", error was " + te + io.getColorCode("reset") );
									writeError(ExceptionPrinter.getExceptionReport(te));
								}
							}
						}
						if ( onWorld )
						{
						    if ( !ejecutado )
						    {
								try
								{
									ejecutado = ejecutado || mundo.execCode ( "parseCommandOnContents" , new Object[] { this , command , fullArguments  , objetivoVector , currentObject } );
								}
								catch ( ScriptException te )
								{
									write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContents() executed from world, command was " + command + fullArguments + ", entity " + currentObject + ", error was " + te + io.getColorCode("reset") );
									writeError(ExceptionPrinter.getExceptionReport(te));
								}
						    }
						    if ( !ejecutado && enableGenerics )
						    {
							try
							{
								ejecutado = ejecutado || mundo.execCode ( "parseCommandOnContentsGeneric" , new Object[] { this , command , fullArguments , "" , objetivoVector , null  , currentObject , null } );
							}
							catch ( ScriptException te )
							{
								write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric() executed from world, command was " + command + fullArguments + ", entity number " + currentObject + ", second object was " + null + ", error was " + te + io.getColorCode("reset") );
								writeError(ExceptionPrinter.getExceptionReport(te));
							}
						    }
						}

					}

				//}


				objetivo = (Entity) objetivoVector.get(0);
				//end copy-pasted from singular

				//a�adimos a ZR plural.
				if ( objetivo instanceof Item )
					meterObjetoEnZRPlural((Item)objetivo);


				//THIS CHUNK OF CODE IS ABSOLUTE LEGACY
				//"LOS INMORTALES" IS THE ONLY REASON TO KEEP IT
				//para cada grupo de primeras palabras del string (dar, dar ca�a, dar ca�a al, dar ca�a al di�bolo) intentamos ejecutar c�digo EVA.
				for ( int j = 0 ; j <= StringMethods.numToks (arguments,' ') && !ejecutado ; j++ )
				{

					Item ourItem;
					if ( objetivo instanceof Item )
						ourItem = (Item) objetivo;
					else
						continue;	

					try
					{
						if ( j == 0 ) ejecutado = ejecutado || ourItem.execCode ( "command_" + command , "this: " + ourItem.getID() + "\n" + "room: " + habitacionActual.getID() + "\n" + "location: inventory" + "\n" + "player: " + getID() );
						else ejecutado = ejecutado || ourItem.execCode ( ( "command_" + command + "_" + StringMethods.getToks( arguments , 1,  j , ' ' ) ).replace(' ','_') , "this: " + ourItem.getID() + "\n" + "room: " + habitacionActual.getID() + "\n" + "location: inventory" + "\n" + "player: " + getID()  );
					}
					catch ( EVASemanticException exc ) 
					{
						write(io.getColorCode("error") + "EVASemanticException found at command " + ( command + StringMethods.getToks( arguments , 1,  j , ' ' ) ).replace(' ','_') + ", item number " + ourItem.getID() + io.getColorCode("reset") );
					}
				}		

				//ahora vamos con el c�digo BeanShell, m�s simple, simplemente ejecutamos la funci�n parseCommand.
				if ( !onWorld && objetivo instanceof SupportingCode )
				{
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommand" , new Object[] { this , command , fullArguments } );
						}
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
					if ( !ejecutado && enableGenerics )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null , new Boolean(true) } );
						}
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
				}
				if ( onWorld )
				{
				    if ( !ejecutado )
				    {
						try
						{
							ejecutado = ejecutado || mundo.execCode ( "parseCommand" , new Object[] { this , command , fullArguments , objetivo } );
						}
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommand() executed from world, command was " + command + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
				    }
				    if ( !ejecutado && enableGenerics ) //this one was added 2011-10-04 - apparently missing before!
				    {
						try
						{
							ejecutado = ejecutado || mundo.execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null  } );
						}
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric() executed from world, command was " + command + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
				    }
				}
				*/

			}
			
			if ( ejecutado )
			{
				//luego esto lo hara el codigo
				//setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}		
			
		} //end if pattern matching vector size not zero


		return false; //no "end" has been hit

	}




	/**
	 * @param objetivos_s
	 * @param objetivos_p
	 * @param arguments
	 * @return true if an end() has been hit, false otherwise
	 */
	public boolean resolveParseCommandForOneEntity_old ( EntityList posiblesObjetivos , String arguments , String fullArguments )
	{


		boolean ejecutado = false;

		Vector objetivos_s = ParserMethods.refersToEntityIn ( arguments,posiblesObjetivos,false ).toEntityVector();
		Vector objetivos_p = ParserMethods.refersToEntityIn ( arguments,posiblesObjetivos,true ).toEntityVector();

		//TODO
		//we can now migrate this to refersToEntityInRecursive. This returns a Vector of Vectors with paths to stuff.
		//we can call parseCommandOnContents on the intermediate nodes in the path and finally parseCommand on the final entity
		//(if the previous parseCommandOnContents have let us, of course).

		if ( objetivos_s.size() > 0 )
		{

			Entity objetivo = (Entity)objetivos_s.get(0);

			//�ste ser� el objeto principal sobre el que ejecutemos el comando en singular (cualquiera, aunque no sea BSH) si hace referencia a un solo objeto
			if ( objetivo instanceof Item )
				meterObjetoEnZRSingular((Item)objetivo);



			//THIS CHUNK OF CODE IS ABSOLUTE LEGACY
			//"LOS INMORTALES" IS THE ONLY REASON TO KEEP IT
			//para cada grupo de primeras palabras del string (dar, dar ca�a, dar ca�a al, dar ca�a al di�bolo) intentamos ejecutar c�digo EVA.
			for ( int i = 0 ; i <= StringMethods.numToks (arguments,' ') && !ejecutado ; i++ )
			{

				Item ourItem;
				if ( objetivo instanceof Item )
					ourItem = (Item) objetivo;
				else
					continue;	

				try
				{
					if ( i == 0 ) ejecutado = ejecutado || ourItem.execCode ( "command_" + command , "this: " + ourItem.getID() + "\n" + "room: " + habitacionActual.getID() + "\n" + "location: inventory" + "\n" + "player: " + getID() );
					else ejecutado = ejecutado || ourItem.execCode ( ( "command_" + command + "_" + StringMethods.getToks( arguments , 1,  i , ' ' ) ).replace(' ','_') , "this: " + ourItem.getID() + "\n" + "room: " + habitacionActual.getID() + "\n" + "location: inventory" + "\n" + "player: " + getID()  );
				}
				catch ( EVASemanticException exc ) 
				{
					write(io.getColorCode("error") + "EVASemanticException found at command " + ( command + StringMethods.getToks( arguments , 1,  i , ' ' ) ).replace(' ','_') + ", item number " + ourItem.getID() + io.getColorCode("reset") );
				}
			}



			//ejecutar parseCommand() de objeto
			if ( objetivo instanceof SupportingCode )
			{
				try
				{
					ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommand" , new Object[] { this , command , fullArguments } );
				}
				catch ( ScriptException te )
				{
					write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + " " + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
					writeError(ExceptionPrinter.getExceptionReport(te));
				}
				if ( !ejecutado )
				{
					try
					{
						ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null , new Boolean(true) } );
					}
					catch ( ScriptException te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + " " + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
				}
			}

			if ( ejecutado ) //c�digo hizo end()
			{
				//luego esto lo hara el codigo
				setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}

		}
		else if ( objetivos_p.size() > 0 )
		{
			//no era en singular, probamos en plural.
			Entity objetivo;			
			resetZRPlural();			
			for ( int i = 0 ; i < objetivos_p.size() ; i++ )
			{

				objetivo = (Entity)objetivos_p.get(i);

				//a�adimos a ZR plural.
				if ( objetivo instanceof Item )
					meterObjetoEnZRPlural((Item)objetivo);


				//THIS CHUNK OF CODE IS ABSOLUTE LEGACY
				//"LOS INMORTALES" IS THE ONLY REASON TO KEEP IT
				//para cada grupo de primeras palabras del string (dar, dar ca�a, dar ca�a al, dar ca�a al di�bolo) intentamos ejecutar c�digo EVA.
				for ( int j = 0 ; j <= StringMethods.numToks (arguments,' ') && !ejecutado ; j++ )
				{

					Item ourItem;
					if ( objetivo instanceof Item )
						ourItem = (Item) objetivo;
					else
						continue;	

					try
					{
						if ( j == 0 ) ejecutado = ejecutado || ourItem.execCode ( "command_" + command , "this: " + ourItem.getID() + "\n" + "room: " + habitacionActual.getID() + "\n" + "location: inventory" + "\n" + "player: " + getID() );
						else ejecutado = ejecutado || ourItem.execCode ( ( "command_" + command + "_" + StringMethods.getToks( arguments , 1,  i , ' ' ) ).replace(' ','_') , "this: " + ourItem.getID() + "\n" + "room: " + habitacionActual.getID() + "\n" + "location: inventory" + "\n" + "player: " + getID()  );
					}
					catch ( EVASemanticException exc ) 
					{
						write(io.getColorCode("error") + "EVASemanticException found at command " + ( command + StringMethods.getToks( arguments , 1,  i , ' ' ) ).replace(' ','_') + ", item number " + ourItem.getID() + io.getColorCode("reset") );
					}
				}		

				//ahora vamos con el c�digo BeanShell, m�s simple, simplemente ejecutamos la funci�n parseCommand.
				if ( objetivo instanceof SupportingCode )
				{
					try
					{
						ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommand" , new Object[] { this , command , fullArguments } );
					}
					catch ( ScriptException te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + " " + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null , new Boolean(true) } );
						}
						catch ( ScriptException te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + " " + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
				}

			}
			if ( ejecutado )
			{
				//luego esto lo hara el codigo
				setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}		
		} //end if pattern matching vector size not zero


		return false; //no "end" has been hit

	}

	
	protected boolean hacerHechizo ( EntityList possibleTargets , SpellList possibleSpells ) //uses "arguments" attr
	{

		if ( possibleTargets == null || possibleTargets.isEmpty() ) return false;	

		boolean mirado = false;			

		Vector[] patternMatchVectorSingSing = Matches.toEntityVectors(possibleSpells.patternMatchTwo ( possibleTargets , arguments , false , false )); //en singular y singular
		Vector[] patternMatchVectorSingPlur = Matches.toEntityVectors(possibleSpells.patternMatchTwo ( possibleTargets , arguments , false , true )); //en singular y plural
		Vector[] patternMatchVectorPlurSing = Matches.toEntityVectors(possibleSpells.patternMatchTwo ( possibleTargets , arguments , true , false )); //en plural y singular
		Vector[] patternMatchVectorPlurPlur = Matches.toEntityVectors(possibleSpells.patternMatchTwo ( possibleTargets , arguments , true , true )); //en plural y plural

		if ( patternMatchVectorSingSing != null && patternMatchVectorSingSing[0].size() > 0 ) //hacemos un hechizo hacia un objetivo
		{
			mirado = true;
			Vector[] theVectors = patternMatchVectorSingSing;

			//OK, hacer el hechizo

			Entity objetivo = (Entity) theVectors[1].elementAt(0);

			//ADD ENEMY HERE JUSTIF COMBAT SPELL (SUBSTITUTE THAT COMMENT FOR IT)
			/*
			if ( !hasEnemy ( objetivo ) )
			{
				addEnemy(objetivo);
			}
			if ( !objetivo.hasEnemy(this) )
			{
				objetivo.addEnemy(this);
			}
			 */

			cast ( (Spell)(theVectors[0].elementAt(0)) , objetivo );

		}

		else //try to cast with nullified target
		{
			Vector patternMatchSpellOnly = possibleSpells.patternMatch ( arguments , false ).toEntityVector();
			if ( patternMatchSpellOnly != null && patternMatchSpellOnly.size() > 0 )
			{
				mirado = true;
				cast ( (Spell) patternMatchSpellOnly.elementAt(0) , null );
			}
		}

		return mirado;

	} //end method hacer hechizo

	
	/**
	 * Executes the parseCommand() methods that apply for the current command/arguments in a given scope.
	 * Returns true if execution of parseCommand() methods was terminated by an end(), and false otherwise.
	 * If execution was terminated by an end() and parseCommand() methods didn't explicitly update this Mobile's state, then this method updates the state and its timer to (IDLE,1).
	 * @param scope
	 * @return  true if end() was hit, false otherwise.
	 */
	protected boolean runParseCommandMethods ( EntityList scope )
	{
		
		/*
		 * As of 2013-03-17, we split this method in the following way:
		 * - This method does the state handling (i.e. sets the state to IDLE and timer to 1 if the parseCommands hit an end() but the state was not changed).
		 * - The execution of the parseCommands themselves is delegated into the doRunParseCommandMethods.
		 */
		
		int origState = getState();
		long origTimeLeft = getPropertyTimeLeft("state");
		
		setProperty ( "originState" , origState ); //this is just for compatibility
		
		boolean foundEnd = doRunParseCommandMethods(scope);
		
		if ( foundEnd && getState() == origState && getPropertyTimeLeft("state") == origTimeLeft ) 
		{
			/*
			 * if the parseCommand methods have hit an end() (and thereby declared the command as processed) but
			 * they haven't touched the state, we set state to (IDLE,1) so that the next command will be processed as normal.
			 */
			setNewState ( IDLE , 1 );
		}
		
		return foundEnd;
	}
	
	/**
	 * Executes the parseCommand() methods that apply for the current command/arguments in a given scope.
	 * @param scope
	 * @return true if end() was hit, false otherwise.
	 */
	private boolean doRunParseCommandMethods(EntityList scope) 
	{
		
		//***
		//*** BEGIN EXECUTION OF parseCommand() METHODS
		//***
	
		boolean ejecutado = false;
		
		//codigo bsh en el jugador
		try
		{
			ReturnValue retval = new ReturnValue(null);
			ejecutado = ejecutado || execCode( "parseCommand" , new Object[] { command , arguments } , retval );
			if ( retval.getRetVal() != null )
			{
				commandstring = (String)retval.getRetVal();
				//Debug.println("Command String Changed To " + (String)retval.getRetVal()); 
				command = lenguaje.extractVerb(commandstring).trim(); //StringMethods.getTok(commandstring,1,' ').trim();
				arguments = lenguaje.extractArguments(commandstring).trim(); //StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
			}
		}
		catch ( ScriptException te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at player's parseCommand, command was " + command + " " + arguments + ", error was " + te + io.getColorCode("reset") );
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
	
		if ( ejecutado )
		{
			//luego esto lo hara el codigo
			//setNewState( 1 /*IDLE*/, 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}
	
		//[04.03.04] Tocho grande de c�digo (ejecutar comando personalizado con items de
		//inventario y con items de habitaci�n) sustituido por tocho m�s simple (ejecutar
		//comando personalizado con objetivos)
	
		ejecutado = false;
	
		matchedOneEntity = false;
		matchedTwoEntities = false;
		matchedOneEntityPermissive = false;
		matchedTwoEntitiesPermissive = false;
	
		/*
	Vector[] objetivos_ss = ParserMethods.refersToEntitiesIn ( arguments,posiblesObjetivos,posiblesObjetivos,false,false);
	Vector[] objetivos_sp = ParserMethods.refersToEntitiesIn ( arguments,posiblesObjetivos,posiblesObjetivos,false,true);	
	Vector[] objetivos_ps = ParserMethods.refersToEntitiesIn ( arguments,posiblesObjetivos,posiblesObjetivos,true,false);
	Vector[] objetivos_pp = ParserMethods.refersToEntitiesIn ( arguments,posiblesObjetivos,posiblesObjetivos,true,true);
		 */
	
		List matches_ss = ParserMethods.parseReferencesToEntitiesIn( arguments,scope,scope,false,false);
		List matches_sp = ParserMethods.parseReferencesToEntitiesIn( arguments,scope,scope,false,true);
		List matches_ps = ParserMethods.parseReferencesToEntitiesIn( arguments,scope,scope,true,false);
		List matches_pp = ParserMethods.parseReferencesToEntitiesIn( arguments,scope,scope,true,true);
	
		List allMatches = new ArrayList();
		allMatches.addAll(matches_ss);
		allMatches.addAll(matches_sp);
		allMatches.addAll(matches_ps);
		allMatches.addAll(matches_pp);
	
		matchedTwoEntities = ( allMatches.size() > 0 );
		
		Matches matches_s = ParserMethods.refersToEntityInRecursive ( arguments,scope,false );
		Matches matches_p = ParserMethods.refersToEntityInRecursive ( arguments,scope,true );
		
		matchedOneEntity = ( matches_s.size() > 0 || matches_p.size() > 0 );
		
		if ( matches_s.getBestPriority() == 0 )
			oneEntityPriority = matches_p.getBestPriority();
		else if ( matches_p.getBestPriority() == 0 )
			oneEntityPriority = matches_s.getBestPriority();
		else oneEntityPriority = Math.min(matches_s.getBestPriority(),matches_p.getBestPriority());
		
		//let's see if this works.
		ejecutado = resolveParseCommandForTwoEntities ( scope , arguments , arguments , false );
		if ( ejecutado ) //c�digo hizo end()
		{
			//setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}
	
		//ejecutar parseCommand sobre una entidad, if possible
		if ( !matchedTwoEntitiesPermissive ) //TODO: add possibility of setting property so that parseCommands for one entity will also be executed when two are matched. 
			ejecutado = resolveParseCommandForOneEntity ( scope , arguments , arguments , false , true );
		if ( ejecutado ) //c�digo hizo end()
		{
			//setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}
		
		//A.
		//comandos sobre un componente: s�lo se ejecutan si no matchearon comandos con objetos
		//("coger bast�n del suelo" debe ejecutarse antes como comando sobre objeto bast�n que sobre componente suelo)
		//(also, comandos sobre componentes no se ejecutan si el verbo no es reconocido, para que funcione bien "coger espada y bast�n del suelo")
		//(si no, har�amos: coger espada (OK), bast�n del suelo (comando sobre "suelo", pifia)
		if ( !matchedOneEntity && !matchedTwoEntities && lenguaje.isVerb(command) )
		{
			EntityList posiblesObjetivosForComponents = (EntityList) scope.clone();
			posiblesObjetivosForComponents.addEntity(this.getRoom()); //componentes pueden ser de habitaci�n
			ejecutado = resolveParseCommandForOneComponent ( posiblesObjetivosForComponents , arguments );
			if ( ejecutado ) //c�digo hizo end()
			{
				//setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}
		}
	
		//primero vemos si hay una definicion especifica del comando en la habitacion, con o sin argumentos
		//si la definicion esta especificada sin argumentos y hay argumentos, los dejamos en el data segment.
		//(argumentos flexibles) <-- todo esto c�digo EVA
		ejecutado = false;
		try
		{
			ejecutado = habitacionActual.execCode("command_" + command + "_" + arguments.replace(' ' , '_' ), "this: " + habitacionActual.getID() + "\n" + "player: " + getID()  );
			if ( !ejecutado ) //no habia comando definido para esos argumentos concretos, probamos sin argumentos
			{
				if ( arguments != null ) ejecutado = habitacionActual.execCode("command_" + command , "this: " + habitacionActual.getID() + "\nargs: " + arguments + "\n" + "player: " + getID() );
				else if ( arguments == null ) ejecutado = habitacionActual.execCode("command_" + command , "this: " + habitacionActual.getID() + "\n" + "player: " + getID() );
			}
		}
		catch ( EVASemanticException exc ) 
		{
			write(io.getColorCode("error") + "EVASemanticException found at room command , room number " + habitacionActual.getID() + io.getColorCode("reset") );
		}
	
		//ahora vamos con el c�digo BeanShell, m�s simple, simplemente ejecutamos la funci�n parseCommand.
		try
		{
			ejecutado = ejecutado || habitacionActual.execCode ( "parseCommand" , new Object[] { this , command , arguments } );
		}
		catch ( ScriptException te )
		{
			te.printStackTrace();
			write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + " " + arguments + ", room number " + habitacionActual.getID() + ", error was " + te + io.getColorCode("reset") );
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
	
		if ( ejecutado ) 
		{
			mentions.setLastMentionedVerb(command);
			return true ;
		}
	
		//parseCommands de los objetos definidos en el mundo.
	
		ejecutado = resolveParseCommandForTwoEntities ( scope , arguments , arguments , true );
		if ( ejecutado ) //c�digo hizo end()
		{
			//setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}
		
		if ( !matchedTwoEntitiesPermissive ) //TODO: add possibility of setting property so that parseCommands for one entity will also be executed when two are matched. 
			ejecutado = resolveParseCommandForOneEntity ( scope , arguments , arguments , true , true );
		if ( ejecutado ) //c�digo hizo end()
		{
			//setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}
		
	
		//parseCommand() del mundo, lo �ltimo que se ejecuta antes de las respuestas por defecto del sistema.
		try
		{
			ReturnValue retval = new ReturnValue(null);
			ejecutado = ejecutado || mundo.execCode( "parseCommand" , new Object[] { this , command , arguments } , retval );
			if ( retval.getRetVal() != null )
			{
				commandstring = (String)retval.getRetVal();
				//Debug.println("Command String Changed To " + (String)retval.getRetVal()); 
				command = lenguaje.extractVerb(commandstring).trim(); //StringMethods.getTok(commandstring,1,' ').trim();
				arguments = lenguaje.extractArguments(commandstring).trim(); //StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
			}
		}
		catch ( ScriptException te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at world's parseCommand, command was " + command + " " + arguments + ", error was " + te + io.getColorCode("reset") );
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
	
		if ( ejecutado )
		{
			//luego esto lo hara el codigo
			//setNewState( 1 , 1 ); //idle state
			mentions.setLastMentionedVerb(command);
			return true;
		} 
	
		return false;
		
	}

	public void cancelPending() {
		commandQueue.removeAllElements();
	}

	public boolean oneTargetAction(String action, String arguments, EntityList posiblesObjetivos) {
		Vector patternMatchVectorSing = new Vector();
		Vector patternMatchVectorPlur = new Vector();
		if ( posiblesObjetivos != null  )
		{
			patternMatchVectorSing = posiblesObjetivos.patternMatch ( arguments , false ).toEntityVector(); //en singular
			patternMatchVectorPlur = posiblesObjetivos.patternMatch ( arguments , true ).toEntityVector(); //en plural
		}
		if ( patternMatchVectorSing.size() > 0 )
		{
	
			Entity ourEntity = (Entity)patternMatchVectorSing.elementAt(0); //el de mas prioridad	
			boolean hecho = executeAction ( action , new Object[] 
			                                                    {ourEntity
			                                                    } );
	
			if ( ! hecho )
			{
				mentions.setLastMentionedVerb(command);
				cancelPending();
				setNewState(IDLE,1); //the action failed, so the player is ready to do more stuff.
				return false;
			}
			else
			{
				//setNewState( 1 /*IDLE*/, 5 ); //action does it
				mentions.setLastMentionedVerb(command);
				return true;
			}		
	
		}
		else if ( patternMatchVectorPlur.size() > 0 )
		{
			for ( int i = 0 ; i < patternMatchVectorPlur.size() ; i++ )
			{
				Entity ourEntity = (Entity)patternMatchVectorPlur.elementAt(i);
	
				boolean hecho = executeAction ( action , new Object[] 
				                                                    {ourEntity
				                                                    } );
	
				if ( ! hecho )
				{
					mentions.setLastMentionedVerb(command);
					cancelPending();
					if ( i == 0 ) setNewState(IDLE,1); //the action (even the first one) failed, so the player is ready to do more stuff.
					return false;
				}
			}
	
			//setNewState( 1 /*IDLE*/, 5 ); //action does it
			mentions.setLastMentionedVerb(command);
			return true;
		}
		else			
		{
			//jugador.escribirDenegacionComando( io.getColorCode("denial") + "�Qu� pretendes quitarte?\n" + io.getColorCode("reset") );
			mentions.setLastMentionedVerb(command);
			cancelPending();
			setNewState(IDLE,1); //the action failed, so the player is ready to do more stuff.
			return false;		
		}
	
	
	}

	protected boolean cogerContenidoEspecificandoContenedor(String args,
			Inventory inv, String infoString) {
				boolean mirado = false;
				Vector patternMatchVectorSing = null; //containers s�lo en singular
			
				//Debug.println("Args: " + args + " Inv: " + inv );
			
			
				if ( inv != null && !inv.isEmpty() )
				{
					patternMatchVectorSing = inv.patternMatch( args , false ).toEntityVector(); //contenedor al fin
					if ( patternMatchVectorSing != null && patternMatchVectorSing.size() > 0 ) //ponemos una cosa en un sitio
					{
						for ( int i = 0 ; i < patternMatchVectorSing.size() ; i++ )
						{
							Item ourContainer = (Item)patternMatchVectorSing.elementAt(i);
							if ( ourContainer.isContainer() //container y abierto si se abre
									&& !( ourContainer.isCloseable() && !ourContainer.isOpen() ) )
							{
			
								//Debug.println("Our container: " + ourContainer);
			
								int ntokens = StringMethods.numToks ( args , ' ' );
								//eliminar el nombre del contenedor... en el ejemplo, quitar� el de armario al encontrar sentido a "pilas"
								//punto_division empieza en ntokens-2 porque uno nos lo cargamos fijo.
								for ( int punto_division = ntokens-1 ; punto_division >= 1 ; punto_division-- )
								{
									//Debug.println("Division: " +  StringMethods.getToks ( args , 1 , punto_division , ' ' ) );
									//Debug.println("mirado: " + mirado);
									mirado = mirado || cogerItem (  StringMethods.getToks ( args , 1 , punto_division , ' ' ) , ourContainer.getContents() , " " + mundo.getMessages().getMessage("get.prep.from") + " " + ourContainer.constructName2True ( 1 , this ) + infoString );
									//Debug.println("called cogerItem. Now mirado is " + mirado);
									if ( mirado ) break;
								}
			
								if ( mirado ) break;
			
							}
						}
					}
				}	
				return mirado;
			}

	private boolean cogerContenido(String args, Inventory inv, String infoString) {
		boolean mirado = false;
	
		if ( inv == null || inv.isEmpty() ) return false;
	
		for ( int i = 0 ; i < inv.size() ; i++ )
		{
			if ( inv.elementAt(i).isContainer() && !( inv.elementAt(i).isCloseable() && !inv.elementAt(i).isOpen() ) )
			{
				String tempstring = infoString;
				infoString += (" " + mundo.getMessages().getMessage("get.prep.from") + " ");
				infoString += inv.elementAt(i).constructName2True ( 1 , this );
				//para que muestre "coges la moneda EN el cofre", p.ej.
				mirado = cogerItem ( args , inv.elementAt(i).getContents() , infoString );
				if ( mirado ) break;
				infoString = tempstring;
			}
		}
		if ( !mirado )
		{
			for ( int i = 0 ; i < inv.size() ; i++ )
			{
				if ( inv.elementAt(i).isContainer() && !( inv.elementAt(i).isCloseable() && !inv.elementAt(i).isOpen() ) )
				{
					String tempstring = infoString;
					infoString += (" " + mundo.getMessages().getMessage("get.prep.from") + " ");
					infoString += inv.elementAt(i).constructName2True ( 1 , this );
					mirado = cogerContenido ( args , inv.elementAt(i).getContents() , infoString );
					if ( mirado ) break;
					infoString = tempstring;
				}
			}
		}
		return mirado;
	}

	protected boolean cogerContenido(Inventory inv, String infoString) {
		boolean mirado = false;
	
		if ( inv == null || inv.isEmpty() ) return false;
	
		for ( int i = 0 ; i < inv.size() ; i++ )
		{
			if ( inv.elementAt(i).isContainer() && !( inv.elementAt(i).isCloseable() && !inv.elementAt(i).isOpen() ) )
			{
				String tempstring = infoString;
				String toConcat = (" " + mundo.getMessages().getMessage("get.prep.from") + " ") + inv.elementAt(i).constructName2True ( 1 , this );
				infoString = toConcat + infoString;
				//para que muestre "coges la moneda EN el cofre", p.ej.
				mirado = cogerItem ( inv.elementAt(i).getContents() , infoString );
				if ( mirado ) break;
				infoString = tempstring;
			}
		}
		if ( !mirado )
		{
			for ( int i = 0 ; i < inv.size() ; i++ )
			{
				if ( inv.elementAt(i).isContainer() && !( inv.elementAt(i).isCloseable() && !inv.elementAt(i).isOpen() ) )
				{
					String tempstring = infoString;
					String toConcat = (" " + mundo.getMessages().getMessage("get.prep.from") + " ") + inv.elementAt(i).constructName2True ( 1 , this );
					infoString = toConcat + infoString;
					mirado = cogerContenido ( inv.elementAt(i).getContents() , infoString );
					if ( mirado ) break;
					infoString = tempstring;
				}
			}
		}
		return mirado;
	}

	protected boolean cogerItem(Inventory inv, String extraInfo) {
	
		return cogerItem ( arguments , inv , extraInfo );
	
	} //end method

	protected boolean abrirPuertaConLlave(Inventory i1, Inventory i2) {
	
		boolean mirado = false;
		List[] pairsVector = patternMatchPairs ( i1 , i2 , arguments );
		List puertas = pairsVector[0];
		List llaves = pairsVector[1];
		if ( puertas.size() == 0 ) return false; //no matching pairs present
		for ( int i = 0 ; i < puertas.size() ; i++ )
		{
			Item ourDoor = (Item)puertas.get(i);
			Item ourKey = (Item)llaves.get(i);
			habitacionActual.reportAction(this,ourDoor,new Entity[]{ourKey},"$1 intenta abrir $2 con $3.\n","$1 intenta abrirte con $3.\n","Intentas abrir $2 con $3.\n",false);
			write( io.getColorCode("action") + ((Item)ourDoor).unlock(ourKey,this) + io.getColorCode("reset") + "\n"   );
		}
		return true;
	
	}

	protected boolean bloquearBichoConArma(MobileList ml, Inventory i) {
	
		if ( i == null || i.isEmpty() || ml == null || ml.isEmpty() ) return false;	
	
		boolean mirado = false;			
	
		Vector[] patternMatchVectorSingSing = Matches.toEntityVectors(ml.patternMatchTwo ( i , arguments , false , false )); //en singular y singular
		Vector[] patternMatchVectorSingPlur = Matches.toEntityVectors(ml.patternMatchTwo ( i , arguments , false , true )); //en singular y plural
		Vector[] patternMatchVectorPlurSing = Matches.toEntityVectors(ml.patternMatchTwo ( i , arguments , true , false )); //en plural y singular
		Vector[] patternMatchVectorPlurPlur = Matches.toEntityVectors(ml.patternMatchTwo ( i , arguments , true , true )); //en plural y plural
	
		if ( patternMatchVectorSingSing != null && patternMatchVectorSingSing[0].size() > 0 ) //atacamos un bicho con un arma
		{
			mirado = true;
			Vector[] theVectors = patternMatchVectorSingSing;
	
			//OK, hacer el ataque
	
			Mobile objetivo = (Mobile) theVectors[0].elementAt(0);
	
			if ( !hasEnemy ( objetivo ) )
			{
				return false; //bloquearse de alguien que, el pobre, no nos pega
			}
	
			lastBlockWeapon =  (Weapon)(theVectors[1].elementAt(0)); //guardamos el arma que se us� por si en un ataque posterior no se especifica cu�l usar
			lastBlockedEnemy = objetivo; //idem con el bicho
	
			block (  objetivo , (Weapon)(theVectors[1].elementAt(0)) );
	
		}
	
		else //no se especifica bicho y arma. Mirar si se especifica uno de ellos, al menos.
		{
			Vector patternMatchVectorSingBicho = ml.patternMatch ( arguments , false ).toEntityVector();
			Vector patternMatchVectorSingArma = i.patternMatch ( arguments , false ).toEntityVector();
	
			if ( patternMatchVectorSingBicho != null && patternMatchVectorSingBicho.size() > 0 )
			{
				Mobile objetivo = (Mobile) patternMatchVectorSingBicho.elementAt(0);
				Weapon usada = null;
	
				if ( lastBlockWeapon != null && wieldedWeapons.contains(lastBlockWeapon) )
				{
					usada = lastBlockWeapon;
				}
				else if ( wieldedWeapons != null && wieldedWeapons.size() > 0 )
				{
					usada = (Weapon) wieldedWeapons.elementAt(0);
				}
	
				if ( usada == null )
				{
					mirado = false;
				}
	
				else
				{
					mirado = true;
					if ( !hasEnemy ( objetivo ) )
					{
						return false;
					}
	
					lastBlockWeapon = usada;
					lastBlockedEnemy = objetivo;
	
					block (  objetivo , usada );
	
				}
	
			}
			else if ( patternMatchVectorSingArma != null && patternMatchVectorSingArma.size() > 0 )
			{
				Mobile objetivo = null;
				Weapon usada = (Weapon) patternMatchVectorSingArma.elementAt(0);
	
				if ( lastAttackedEnemy != null && habitacionActual.hasMobile ( lastAttackedEnemy ) )
				{
					objetivo = lastAttackedEnemy;
				}
				/*
				else if ( getEnemies() != null && getEnemies().size() == 1 && habitacionActual.hasMobile((Mobile)getEnemies().get(0) ) )
				{
					//if there is a single enemy and it's in this room, there's no doubt we want to block him
					objetivo = (Mobile) getEnemies().get(0);
				}
				*/
				else if ( habitacionActual.getMobiles().size() == 2 && getEnemies() != null  )
				{
					//if we are alone in the room with an enemy, the target is obviously that enemy.
					if ( hasEnemy(habitacionActual.getMobiles().elementAt(0)) )
						objetivo = habitacionActual.getMobiles().elementAt(0);
					if ( hasEnemy(habitacionActual.getMobiles().elementAt(1)) )
						objetivo = habitacionActual.getMobiles().elementAt(1);
				}
					
				if ( objetivo == null )
				{
					mirado = false;
				}
	
				else
				{
					mirado = true;
					if ( !hasEnemy ( objetivo ) )
					{
						return false;
					}
	
					lastBlockWeapon = usada;
					lastBlockedEnemy = objetivo;
	
					block (  objetivo , usada );
	
				}
	
	
			}
			else //bicho and arma unspecified
			{
				Mobile objetivo = null;
				Weapon usada = null;
				
				//assign a weapon
				if ( lastBlockWeapon != null && wieldedWeapons.contains(lastBlockWeapon) )
				{
					usada = lastBlockWeapon;
				}
				else if ( wieldedWeapons != null && wieldedWeapons.size() > 0 )
				{
					usada = (Weapon) wieldedWeapons.elementAt(0);
				}
				
				//assign an enemy
				if ( lastAttackedEnemy != null && habitacionActual.hasMobile ( lastAttackedEnemy ) )
				{
					objetivo = lastAttackedEnemy;
				}
				/*
				else if ( getEnemies() != null && getEnemies().size() == 1 && habitacionActual.hasMobile((Mobile)getEnemies().get(0) ) )
				{
					//if there is a single enemy and it's in this room, there's no doubt we want to block him
					objetivo = (Mobile) getEnemies().get(0);
				}
				*/
				else if ( habitacionActual.getMobiles().size() == 2 && getEnemies() != null  )
				{
					//if we are alone in the room with an enemy, the target is obviously that enemy.
					if ( hasEnemy(habitacionActual.getMobiles().elementAt(0)) )
						objetivo = habitacionActual.getMobiles().elementAt(0);
					if ( hasEnemy(habitacionActual.getMobiles().elementAt(1)) )
						objetivo = habitacionActual.getMobiles().elementAt(1);
				}
				
				if ( objetivo == null || usada == null )
				{
					mirado = false;
				}
				else
				{
					mirado = true;
					if ( !hasEnemy ( objetivo ) )
					{
						return false;
					}
	
					lastBlockWeapon = usada;
					lastBlockedEnemy = objetivo;
	
					block (  objetivo , usada );
	
				}
	
			}
	
		}
	
		return mirado;
	
	}

	//return true if processed
	protected boolean atacarBichoConArma(MobileList ml, Inventory i) {
		
		if ( ml == null || ml.isEmpty() ) return false; //no one to attack	
		if ( i == null || i.isEmpty() )
		{
			//no weapons -> no podemos atacar
			writeDenial ( mundo.getMessages().getMessage("no.attack.weapon") );
			return true; //we have processed everything and given a message
		}
	
		boolean mirado = false;			
	
		Vector[] patternMatchVectorSingSing = Matches.toEntityVectors(ml.patternMatchTwo ( i , arguments , false , false )); //en singular y singular
		Vector[] patternMatchVectorSingPlur = Matches.toEntityVectors(ml.patternMatchTwo ( i , arguments , false , true )); //en singular y plural
		Vector[] patternMatchVectorPlurSing = Matches.toEntityVectors(ml.patternMatchTwo ( i , arguments , true , false )); //en plural y singular
		Vector[] patternMatchVectorPlurPlur = Matches.toEntityVectors(ml.patternMatchTwo ( i , arguments , true , true )); //en plural y plural
	
		if ( patternMatchVectorSingSing != null && patternMatchVectorSingSing[0].size() > 0 ) //atacamos un bicho con un arma
		{
			mirado = true;
			Vector[] theVectors = patternMatchVectorSingSing;
	
			//OK, hacer el ataque
	
			Mobile objetivo = (Mobile) theVectors[0].elementAt(0);
	
			if ( !hasEnemy ( objetivo ) )
			{
				addEnemy(objetivo);
			}
			if ( !objetivo.hasEnemy(this) )
			{
				objetivo.addEnemy(this);
			}
	
			lastAttackWeapon =  (Weapon)(theVectors[1].elementAt(0)); //guardamos el arma que se us� por si en un ataque posterior no se especifica cu�l usar
			lastAttackedEnemy = objetivo; //idem con el bicho
	
			attack (  objetivo , (Weapon)(theVectors[1].elementAt(0)) );
	
			//escribir( io.getColorCode("action") + ((Item)(theVectors[0].elementAt(0))).unlock( (Item)(theVectors[1].elementAt(0)) ) + io.getColorCode("reset") + "\n"  );
		}
	
		else //no se especifica bicho y arma. Mirar si se especifica uno de ellos, al menos.
		{
			Vector patternMatchVectorSingBicho = ml.patternMatch ( arguments , false ).toEntityVector();
			Vector patternMatchVectorSingArma = i.patternMatch ( arguments , false ).toEntityVector();
	
			if ( patternMatchVectorSingBicho != null && patternMatchVectorSingBicho.size() > 0 ) //bicho specified, arma unspecified
			{
				Mobile objetivo = (Mobile) patternMatchVectorSingBicho.elementAt(0);
				Weapon usada = null;
	
				Debug.println("Wielded Weapons Size " + wieldedWeapons.size() );
	
				Inventory usableWeapons = getUsableWeapons();
	
				if ( lastAttackWeapon != null && usableWeapons.contains(lastAttackWeapon) )
				{
					usada = lastAttackWeapon;
				}
				else if ( usableWeapons != null && usableWeapons.size() > 0 )
				{
					Debug.println("Setting first weapon as used");
					//como por aqu� todav�a ten�amos inventarios paralelos, con nulos y cosas irregulares, hacemos algo extra�o:
					for ( int k = usableWeapons.size()-1 ; k >= 0 ; k-- )
					{
						if ( usableWeapons.elementAt(k) != null )
							usada = (Weapon) usableWeapons.elementAt(k);	
					}
				}
	
				if ( usada == null )
				{
					mirado = false;
				}
	
				else
				{
					mirado = true;
					if ( !hasEnemy ( objetivo ) )
					{
						addEnemy(objetivo);
					}
					if ( !objetivo.hasEnemy(this) )
					{
						objetivo.addEnemy(this);
					}
	
					lastAttackWeapon = usada;
					lastAttackedEnemy = objetivo;
	
					attack (  objetivo , usada );
	
				}
	
			}
			else if ( patternMatchVectorSingArma != null && patternMatchVectorSingArma.size() > 0 ) //specified arma, but not bicho
			{
				Mobile objetivo = null;
				Weapon usada = (Weapon) patternMatchVectorSingArma.elementAt(0);
	
				if ( lastAttackedEnemy != null && habitacionActual.hasMobile ( lastAttackedEnemy ) )
				{
					objetivo = lastAttackedEnemy;
				}
				/*
				else if ( getEnemies() != null && getEnemies().size() == 1 && habitacionActual.hasMobile((Mobile)getEnemies().get(0) ) )
				{
					//if there is a single enemy and it's in this room, there's no doubt we want to block him
					objetivo = (Mobile) getEnemies().get(0);
				}
				*/
				else if ( habitacionActual.getMobiles().size() == 2 && getEnemies() != null  )
				{
					//if we are alone in the room with an enemy, the target is obviously that enemy.
					if ( hasEnemy(habitacionActual.getMobiles().elementAt(0)) )
						objetivo = habitacionActual.getMobiles().elementAt(0);
					if ( hasEnemy(habitacionActual.getMobiles().elementAt(1)) )
						objetivo = habitacionActual.getMobiles().elementAt(1);
				}
				
				if ( objetivo == null )
				{
					mirado = false;
				}
	
				else
				{
					mirado = true;
					if ( !hasEnemy ( objetivo ) )
					{
						addEnemy(objetivo);
					}
					if ( !objetivo.hasEnemy(this) )
					{
						objetivo.addEnemy(this);
					}
	
					lastAttackWeapon = usada;
					lastAttackedEnemy = objetivo;
	
					attack (  objetivo , usada );
	
				}
	
	
			}
			else //bicho and arma unspecified
			{
				Mobile objetivo = null;
				Weapon usada = null;
	
				Inventory usableWeapons = getUsableWeapons();
	
				//assign weapon
				if ( lastAttackWeapon != null && usableWeapons.contains(lastAttackWeapon) )
				{
					usada = lastAttackWeapon;
				}
				else if ( usableWeapons != null && usableWeapons.size() > 0 )
				{
					//como por aqu� todav�a ten�amos inventarios paralelos, con nulos y cosas irregulares, hacemos algo extra�o:
					for ( int k = usableWeapons.size()-1 ; k >= 0 ; k-- )
					{
						if ( usableWeapons.elementAt(k) != null )
							usada = (Weapon) usableWeapons.elementAt(k);
					}
				}
				
				//assign enemy
				if ( lastAttackedEnemy != null && habitacionActual.hasMobile ( lastAttackedEnemy ) )
				{
					objetivo = lastAttackedEnemy;
				}
				/*
				else if ( getEnemies() != null && getEnemies().size() == 1 && habitacionActual.hasMobile((Mobile)getEnemies().get(0) ) )
				{
					//if there is a single enemy and it's in this room, there's no doubt we want to block him
					objetivo = (Mobile) getEnemies().get(0);
				}
				*/
				else if ( habitacionActual.getMobiles().size() == 2 && getEnemies() != null  )
				{
					//if we are alone in the room with an enemy, the target is obviously that enemy.
					if ( hasEnemy(habitacionActual.getMobiles().elementAt(0)) )
						objetivo = habitacionActual.getMobiles().elementAt(0);
					if ( hasEnemy(habitacionActual.getMobiles().elementAt(1)) )
						objetivo = habitacionActual.getMobiles().elementAt(1);
				}
				
				if ( objetivo == null || usada == null )
				{
					mirado = false;
				}
				else
				{
					mirado = true;
					if ( !hasEnemy ( objetivo ) )
					{
						addEnemy(objetivo);
					}
					if ( !objetivo.hasEnemy(this) )
					{
						objetivo.addEnemy(this);
					}
	
					lastAttackWeapon = usada;
					lastAttackedEnemy = objetivo;
	
					attack (  objetivo , usada );
	
				}
	
			}
	
		}
	
		//de momento, no tratamos atacar a varios bichos con un arma.
		//oh, tal vez, si llega a haber alabardas o bastadas por el estilo...
		//mas not at the moment (03.02.25)
	
		/*
		else if ( patternMatchVectorSingPlur != null && patternMatchVectorSingPlur[0].size() > 0 )
		{
			mirado = true;
			Vector[] theVectors = patternMatchVectorSingPlur;
			for ( int i=0 ; i < theVectors[1].size() ; i++ )
			{
				Mobile ourMob = (Item)theVectors[0].elementAt(0);		
				Weapon ourKey = (Item)theVectors[1].elementAt(i);	
				escribir ( '\n' + "Intentas abrir "  + "con " +  ourKey.constructName2True ( 1 , ourKey.getState() ) + ": ");
				escribir( io.getColorCode("action") + ((Item)ourDoor).unlock(ourKey) + io.getColorCode("reset") + "\n"   );
			}
		}
		else if ( patternMatchVectorPlurSing != null && patternMatchVectorPlurSing[0].size() > 0 )
		{
			mirado = true;
			Vector[] theVectors = patternMatchVectorPlurSing;
			for ( int i=0 ; i < theVectors[0].size() ; i++ )
			{
				Item ourDoor = (Item)theVectors[0].elementAt(i);		
				Item ourKey = (Item)theVectors[1].elementAt(0);	
				escribir ( '\n' + "Intentas abrir" + " con " +  ourKey.constructName2True ( 1 , ourKey.getState() ) + ": ");
				escribir( io.getColorCode("action") + ((Item)ourDoor).unlock(ourKey) + io.getColorCode("reset") + "\n"   );
			}
		}
		else if ( patternMatchVectorPlurPlur != null && patternMatchVectorPlurPlur[0].size() > 0 )
		{
			mirado = true;
			Vector[] theVectors = patternMatchVectorPlurPlur;
			for ( int i=0 ; i < theVectors[0].size() ; i++ )
			{
				for ( int j = 0 ; j < theVectors[1].size() ; j++ )
				{
					Item ourDoor = (Item)theVectors[0].elementAt(i);		
					Item ourKey = (Item)theVectors[1].elementAt(j);	
					escribir ( '\n' + "Intentas abrir " + "con " +  ourKey.constructName2True ( 1 , ourKey.getState() ) + ": ");
					escribir( io.getColorCode("action") + ((Item)ourDoor).unlock(ourKey) + io.getColorCode("reset") + "\n"   );
				}
			}
		}
		 */
		return mirado;
	
	} //end method atacar bicho con arma

	protected boolean cerrarPuertaConLlave(Inventory i1, Inventory i2) {
	
		boolean mirado = false;
		List[] pairsVector = patternMatchPairs ( i1 , i2 , arguments );
		List puertas = pairsVector[0];
		List llaves = pairsVector[1];
		if ( puertas.size() == 0 ) return false; //no matching pairs present
		for ( int i = 0 ; i < puertas.size() ; i++ )
		{
			Item ourDoor = (Item)puertas.get(i);
			Item ourKey = (Item)llaves.get(i);
			habitacionActual.reportAction(this,ourDoor,new Entity[]{ourKey},"$1 intenta cerrar $2 con $3.\n","$1 intenta cerrarte con $3.\n","Intentas cerrar $2 con $3.\n",false);
			write( io.getColorCode("action") + ((Item)ourDoor).lock(ourKey,this) + io.getColorCode("reset") + "\n"   );
		}
		return true;
	
	}
	
	//fuerza un comando
	public void forceCommand(String s)
	{
		forced = true;
		force_string = s;
		if ( getState() == 0 ) setNewState( IDLE,1 ); //Mobiles by default have no state if they haven't done anything yet.
	}
	
	public void enqueueCommand(String s)
	{
		commandQueue.add(s);
		if ( getState() == 0 ) setNewState( IDLE,1 ); //Mobiles by default have no state if they haven't done anything yet.
	}

	public void setCommandString(String s)
	{
		commandstring=s;
	}

	public boolean separateSentences() {
	
		Vector tokensYSeparadores = StringMethods.tokenizeWithComplexSeparators ( commandstring , StringMethods.STANDARD_SENTENCE_SEPARATORS() , true );
		Vector tempCommandQueue = new Vector();
		int nComillas=0;
		String acum="";
		for ( int i = 0 ; i < tokensYSeparadores.size() ; i++ )
		{
			String cur = (String)tokensYSeparadores.elementAt(i);
			acum = acum + cur;
			for ( int k = 0 ; k < cur.length() ; k++ )
				if ( cur.charAt(k) == '\"' )
					nComillas++;
	
			if ( !(StringMethods.STANDARD_SENTENCE_SEPARATORS()).contains(cur) ) //not separator
			{
				if ( nComillas % 2 == 0 || i == tokensYSeparadores.size()-1 ) //n�mero par de comillas hasta aqu� o �ltimo token
				{
					tempCommandQueue.add(acum);
					Debug.println("ADDED: " + acum);
					acum="";
				}
			}
			else if ( nComillas % 2 == 0 )
				acum="";
		}
	
		//from the older version
		tempCommandQueue.addAll ( commandQueue );
		commandQueue = (Vector)tempCommandQueue.clone();
		if ( !commandQueue.isEmpty() )
		{
			commandstring = (String)commandQueue.elementAt(0);
			commandQueue.removeElementAt(0);
		}
		else
		{
			//comando nulo
			return false;
		}
		return true;
	
	}

	/**
	A pesar del nombre ambiguo (no se me ocurr�a ninguno mejor, la verdad) esta funci�n
	s�lo sirve para cuando no se ejecuta un comando porque no se entiende lo que viene
	despu�s (por ejemplo, "coger adfasfg" o "coger <algo que no hay en la habitaci�n o
	el programa no reconoce>"). Cuando no se ejecuta por motivos ya del juego (no coges
	porque llevas demasiado peso, no miras porque est�s ciego, etc.) no se debe usar esto,
	ya que en caso de second-chance da un mensaje que es claramente de error.
	 */
	private void escribirDenegacionComando(String s) {
		if ( secondChance ) //estamos en la segunda oportunidad, i.e. con verbo inventado...
		{
			escribirErrorNoEntiendo ( );
		}
		else write(s);
	}

	public boolean execCommand(String commandstring) {
		
		
		//conservative mode check
		//this feature is only implemented for Spanish pronouns
		if ( "es".equals(lenguaje.getLanguageCode()) && getPropertyValueAsBoolean("noPronounDisambiguation") && matchedTwoEntitiesPermissive )
		{
			if ( firstWord(commandstring).toLowerCase().endsWith ( "las" ) && firstWord(commandstring).length() > 3 
					|| firstWord(commandstring).toLowerCase().endsWith ( "los" ) && firstWord(commandstring).length() > 3
					|| firstWord(commandstring).toLowerCase().endsWith ( "la" ) && firstWord(commandstring).length() > 2
					|| firstWord(commandstring).toLowerCase().endsWith ( "lo" ) && firstWord(commandstring).length() > 2 )
			{
				
				//the following is just to get $command:
				commandstring = substitutePronounsInSentence(commandstring);
				//commandstring = commandstring.trim();
				//if ( !getPropertyValueAsBoolean("noVerbSpellChecking") )
				//	commandstring = lenguaje.correctVerb(commandstring); //ddone above
				commandstring = lenguaje.substituteVerb ( commandstring );
				commandstring = lenguaje.substituteAlias ( commandstring );
				commandstring = commandstring.trim();
				command = lenguaje.extractVerb(commandstring); //StringMethods.getTok(commandstring,1,' ').trim();	
				
				write ( io.getColorCode("denial") + mundo.getMessages().getMessage("ambiguous.pronoun","$command",command,new Object[]{this,commandstring}) + io.getColorCode("reset") );
				mentions.setLastMentionedVerb ( lenguaje.extractVerb(substitutePronounsInSentence(commandstring)) );
				cancelPending();
				return false;	
			}
		}
			
	
			
		//Debug.println("CommandString: " + commandstring );
	
		//commandstring es aqui ya el comando a ejecutar
	
		//{comando no nulo}
	
		String originalTrimmedCommandString = commandstring;
	
		//substitution of pronouns and synonyms
		//Debug.println("BEFORE SUBSTITUTION: " + commandstring);
		commandstring = substitutePronounsInSentence(commandstring);
		//Debug.println("AFTER SUBSTITUTION: " + commandstring);
		//commandstring = commandstring.trim();
		//commandstring = lenguaje.sustituirVerbos ( commandstring );
		//if ( !getPropertyValueAsBoolean("noVerbSpellChecking") )
			//commandstring = lenguaje.correctVerb(commandstring); //done above
		commandstring = lenguaje.substituteVerb ( commandstring );		
		commandstring = lenguaje.substituteAlias ( commandstring );
	
		commandstring = commandstring.trim();
		command = lenguaje.extractVerb(commandstring); //StringMethods.getTok(commandstring,1,' ').trim();
	
		//patch to undo synonym substitutions on command "decir"
		if ( "decir".equalsIgnoreCase(command) )
		{
			commandstring = originalTrimmedCommandString;
		}	
		arguments = lenguaje.extractArguments(commandstring).trim(); //StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
	
	
		//Debug.println("Definite command to exec: " + commandstring );
	
		//for statistics only:
		finalExecutedCommandLog.addElement(commandstring);
	
	
		//Sistema de acciones
		String actionName = "";
		Object[] actionArgs = null;
	
	
		
		EntityList posiblesObjetivos = getReachableEntities();
	
		
		//*** EXECUTE parseCommand() METHODS
		if ( runParseCommandMethods(posiblesObjetivos) ) return true;
	
	
		
		Vector patternMatchVectorSing = ParserMethods.refersToEntityIn ( arguments,posiblesObjetivos,false ).toEntityVector();
		Vector patternMatchVectorPlur = ParserMethods.refersToEntityIn ( arguments,posiblesObjetivos,true ).toEntityVector();
		
	
		//si no estaba definido en la habitacion		
		if ( lenguaje.translateVerb(command,"en").equalsIgnoreCase( "go" ) ) //ir
		{
	
			actionName = "go";
			actionArgs = new Object[1]; //en concreto, ser� un Path.
	
			if ( StringMethods.numToks(commandstring,' ') < 2 )
			{
	
				write ( io.getColorCode("denial") + mundo.getMessages().getMessage("go.nowhere",new Object[]{this}) + io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}	
			/*
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase(mundo.getMessages().getMessage("direction.n"))
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("n") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.NORTE ));
	
				actionArgs[0] = habitacionActual.getExit ( true , Path.NORTE );
	
			}
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase(mundo.getMessages().getMessage("direction.s"))
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("s") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.SUR ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.SUR );
			}
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase(mundo.getMessages().getMessage("direction.w"))
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("o") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.OESTE ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.OESTE );
			}
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase(mundo.getMessages().getMessage("direction.e"))
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("e") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.ESTE ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.ESTE );
			}	
	
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("sudeste")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("se") 
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("sureste") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.ESTE ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.SUDESTE );
			}	
	
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("sudoeste")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("so")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("suroeste") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.ESTE ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.SUROESTE );
			}	
	
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("nordeste")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("noreste")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("ne") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.ESTE ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.NORDESTE );
			}	
	
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("noroeste")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("no") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.ESTE ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.NOROESTE );
			}	
	
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("arriba")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("ar") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.ARRIBA ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.ARRIBA );
			}	
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("abajo")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("ab") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.ABAJO ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.ABAJO );
			}	
			*/ //refactored 2014-02-17, let's see how it works.
			actionArgs[0] = habitacionActual.getStandardExitMatchingArguments( arguments );
			if ( actionArgs[0] != null )
				mentions.setLastMentionedVerb(command);
			
			//else
			if ( actionArgs[0] == null || !((Path)actionArgs[0]).isValid() ) //changed to admit a custom exit to have a standard name
			{
				//Mirar las salidas personalizadas
				actionArgs[0] = habitacionActual.getNonStandardExitMatchingArguments(arguments);
				if ( actionArgs[0] != null )
					mentions.setLastMentionedVerb(command);
				
				//old. commented 2013-03-22
				/*
				for ( int i=0 ; i<habitacionActual.otherExits.length ; i++ )
				{
					if ( habitacionActual.isValidExit(false,i) && habitacionActual.getExit(false,i).matchExitCommand( arguments ) )
					{
						mentions.setLastMentionedVerb(command);
						actionArgs[0] = habitacionActual.getExit ( false , i );	
						//return go (habitacionActual.getExit( false,i ));
					}
				}
				*/
	
				//si no hab�a ninguna salida
				if ( actionArgs[0] == null )
				{
					escribirDenegacionComando(io.getColorCode("denial") + 
							mundo.getMessages().getMessage("go.where",new Object[]{this,arguments})  //"�C�mo? �Hacia d�nde quieres ir?\n" 
							+ io.getColorCode("reset") );
					mentions.setLastMentionedVerb(command);	
					cancelPending();
					return false;
				}
			} 
		} // FIN CMD IR
		else if ( lenguaje.translateVerb(command,"en").equalsIgnoreCase( "return" ) ) //volver
		{
			//vuelve a la ultima habitacion visitada (habitacionAnterior)
	
			actionName = "go";
			actionArgs = new Object[1];
	
			//mirar las salidas est�ndar
			for ( int i = 0 ; i < habitacionActual.standardExits.length ; i++ )
			{
				if ( habitacionActual.isValidExit(true,i) && mundo.getRoom(habitacionActual.getExit(true,i).getDestinationID()) == habitacionAnterior )
				{
					mentions.setLastMentionedVerb(command);
					//return go (habitacionActual.getExit( true , i ));	
					actionArgs[0] = habitacionActual.getExit ( true , i );
					break;
				}
			}
			//mirar las salidas personalizadas
			if ( actionArgs[0] == null )
			{
				for ( int i = 0 ; i < habitacionActual.otherExits.length ; i++ )
				{
					if ( habitacionActual.isValidExit(false,i) && mundo.getRoom(habitacionActual.getExit(false,i).getDestinationID()) == habitacionAnterior )
					{
						mentions.setLastMentionedVerb(command);
						//return go (habitacionActual.getExit( false , i ));
						actionArgs[0] = habitacionActual.getExit ( false , i );	
						break;			  
					}
				}
			}
			//si no hemos hecho return al llegar aqu�, es que no hay salida que nos lleve a la habitaci�n anterior
			if ( actionArgs[0] == null )
			{
				escribirDenegacionComando(io.getColorCode("denial") + mundo.getMessages().getMessage("cant.go.back",new Object[]{this,arguments}) + io.getColorCode("reset"));
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}
		} //FIN CMD VOLVER
		else if ( lenguaje.translateVerb(command,"en").equalsIgnoreCase( "look" ) ) //mirar
		{
			if ( StringMethods.numToks(commandstring,' ') < 2 )
			{
				//mirar a secas (no extrades)
				show_room(mundo);
				setNewState( 1 /*IDLE*/, 1 );
				mentions.setLastMentionedVerb(command);
				return true;	
			}		
			else //miras algo en concreto
			{
				String s;
				//extra descriptions habitaci�n
				//long comparand = (long)this.getRelationshipState( habitacionActual )*((long)Math.pow(2,32)) + habitacionActual.getState();
				if ( ( s = habitacionActual.getExtraDescription( arguments , this ) ) != null )
				{
					write(io.getColorCode("description")+s+io.getColorCode("reset")+"\n");
					setNewState( 1 /*IDLE*/, 1 );
					mentions.setLastMentionedVerb(command);
					return true;	
				}
				else
				{
					boolean mirado = false;
	
					//intentar mirar extras de items de habitaci�n
	
					Debug.println("Mirado init");
	
					if ( !mirado ) mirado = mirarExtrasItems ( arguments , habitacionActual.itemsInRoom );
	
					//intentar mirar extras de items de inventario -> �tal vez antes de los propios �tems?
	
					Debug.println("Mirado="+mirado);
	
					if ( !mirado ) mirado = mirarExtrasItems ( arguments , inventory );
	
					//intentar mirar un item de habitaci�n.
	
					Debug.println("Mirado="+mirado);
	
					if ( !mirado ) mirado = mirarItem ( arguments , habitacionActual.itemsInRoom );
	
					//contenido de items de habitaci�n.
	
					Debug.println("Mirado="+mirado);
	
					if ( !mirado ) mirado = mirarContenido ( arguments , habitacionActual.itemsInRoom );	
	
					//intentar mirar un item de inventario.
	
					Debug.println("Mirado="+mirado);
	
					if ( !mirado ) mirado = mirarItem ( arguments , inventory );
	
					//contenido de items de inventario.
	
					Debug.println("Mirado="+mirado);
	
					if ( !mirado ) mirado = mirarContenido ( arguments , inventory );						
	
					//intentar mirar extras de bichos de la habitaci�n
					
					Debug.println("Mirado="+mirado);
					
					if ( !mirado ) mirado = mirarExtrasBichos ( arguments , this.getRoom().getMobiles() );
					
					//intentar mirar un bicho de la habitaci�n
	
					Debug.println("Mirado="+mirado);
	
					if ( !mirado ) mirado = mirarBicho ( arguments , habitacionActual.mobsInRoom );
	
					//intentar mirar partes del propio cuerpo
	
					Debug.println("Mirado="+mirado);
	
					if ( !mirado ) mirado = mirarItem ( arguments , getFlattenedPartsInventory() );
	
					//intentar mirar extras propios
	
					Debug.println("Mirado="+mirado);
	
					MobileList yo = new MobileList();
					yo.addElement(this);
					
					//if ( !mirado ) mirado = mirarExtrasBichos ( arguments , yo );
	
					if(!mirado) //no miramos nada.
					{
						escribirDenegacionComando(io.getColorCode("denial") + 
								mundo.getMessages().getMessage("look.what",new Object[]{this,arguments})  //"�Qu� pretendes mirar?\n" 
								+ io.getColorCode("reset"));
						mentions.setLastMentionedVerb(command);
						cancelPending();
						return false;
					}
					else
					{
						mentions.setLastMentionedVerb(command);
	
						//added bugfix. without this, TU's can get negative if looking several times
						setNewState( 1 /*IDLE*/, 1 );
						//TODO Other actions don't do this...
	
						return true;
					}
				}	
			}
		} // FIN CMD MIRAR
	
		else if ( lenguaje.translateVerb(command,"en").equalsIgnoreCase( "attack" ) ) //atacar
		{
	
			boolean mirado = false;
	
			mirado = atacarBichoConArma ( habitacionActual.mobsInRoom , getUsableWeapons() );
	
	
	
			if(!mirado) //no atacamos nada, no nos entiende.
			{
	
				if ( inventory.patternMatch ( arguments , false ) != null && inventory.patternMatch ( arguments , false ).size() > 0 )
				{
					escribirDenegacionComando(io.getColorCode("denial")+"Para atacar con un arma, primero has de blandirla.\n"+io.getColorCode("reset") );
				}
	
	
				escribirDenegacionComando(io.getColorCode("denial")+
						mundo.getMessages().getMessage("attack.what",new Object[]{this,arguments})  //"�C�mo? �Atacar a qui�n?\n" 
						+io.getColorCode("reset")  );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
	
	
	
		} //FIN CMD ATACAR
	
		else if ( lenguaje.translateVerb(command,"en").equalsIgnoreCase( "block" ) || lenguaje.translateVerb(command,"en").equalsIgnoreCase( "defend" ) ) //bloquear, defender
		{
	
			boolean mirado = false;
	
			mirado = bloquearBichoConArma ( habitacionActual.mobsInRoom , getUsableWeapons() );
	
			if(!mirado) //no atacamos nada, no nos entiende.
			{
				escribirDenegacionComando(io.getColorCode("denial")+
						mundo.getMessages().getMessage("block.what",new Object[]{this,arguments})  //"�C�mo? �Defenderse de qui�n?\n" 
						+io.getColorCode("reset")  );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
	
		}
	
		else if ( lenguaje.translateVerb(command,"en").equalsIgnoreCase( "dodge" ) ) //esquivar
		{
			boolean mirado = false;
			mirado = esquivar();
			if ( !mirado ) //ein?
			{
				escribirDenegacionComando(io.getColorCode("denial") + 
						mundo.getMessages().getMessage("dodge.what",new Object[]{this,arguments})  //"No te atacan. �Esquivar qu�?\n" 
						+io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
		}
	
	
		else if ( lenguaje.translateVerb(command,"en").equalsIgnoreCase( "open" ) ) //abrir
		{
	
	
	
			//Paso 1: Abrir con llave. [si hay dos objetos en la entrada, el 1� ser� la puerta
			//y el 2� la llave, como en "abrir la puerta roja con la llave amarilla p�lida"
	
			boolean mirado = false;
	
			Vector[] patternMatchVectorSingSing = null;
			Vector[] patternMatchVectorSingPlur = null;
			Vector[] patternMatchVectorPlurSing = null;
			Vector[] patternMatchVectorPlurPlur = null;
	
			mirado = abrirPuertaConLlave ( habitacionActual.itemsInRoom , inventory );	
	
			//tratar de abrir con llave algo que est� en nuestro inventario, no en la habitaci�n.
			if ( !mirado )
				mirado = abrirPuertaConLlave ( inventory , inventory );
	
	
	
			if ( !mirado ) //abrir a secas, sin llave (buscar un solo objeto en string)
			{
	
				if ( habitacionActual.itemsInRoom != null && !habitacionActual.itemsInRoom.isEmpty() )
				{
	
					patternMatchVectorSing = new Vector();
					patternMatchVectorPlur = new Vector();
					if ( habitacionActual.itemsInRoom != null  )
					{
						patternMatchVectorSing = habitacionActual.itemsInRoom.patternMatch ( arguments , false ).toEntityVector(); //en singular
						patternMatchVectorPlur = habitacionActual.itemsInRoom.patternMatch ( arguments , true ).toEntityVector(); //en plural
					}
					if ( patternMatchVectorSing.size() > 0 ) //miramos un objeto
					{
						Item ourItem = ((Item)patternMatchVectorSing.elementAt(0));
						write( io.getColorCode("action") + ((Item)patternMatchVectorSing.elementAt(0)).abrir(this) + io.getColorCode("reset") + "\n" );
						habitacionActual.reportAction(this,ourItem,null,"$1 intenta abrir $2.\n","$1 intenta abrirte.\n","Intentas abrir $2.\n",false);
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
	
	
							write ( mundo.getMessages().getMessage("you.try.open.from.floor","$item",ourItem.constructName2True ( 1 , this ),new Object[]{this,arguments} ));
							//write( "Intentas abrir " + ourItem.constructName2True ( 1 , this ) + ": " );
	
							habitacionActual.reportAction(this,ourItem,null,"$1 intenta abrir $2.\n","$1 intenta abrirte.\n","Intentas abrir $2.\n",false);
	
	
							write( io.getColorCode("action") + ((Item)ourItem).abrir(this) + io.getColorCode("reset") + "\n"  );
	
						}	
					}
				} //fin (si hay items)	
				if ( inventory != null && !inventory.isEmpty() )
				{
	
					patternMatchVectorSing = new Vector();
					patternMatchVectorPlur = new Vector();
					if ( inventory != null  )
					{
						patternMatchVectorSing = inventory.patternMatch ( arguments , false ).toEntityVector(); //en singular
						patternMatchVectorPlur = inventory.patternMatch ( arguments , true ).toEntityVector(); //en plural
					}
					if ( patternMatchVectorSing.size() > 0  ) //miramos un objeto
					{
						Item ourItem = ((Item)patternMatchVectorSing.elementAt(0));
						mirado=true;
						habitacionActual.reportAction(this,ourItem,null,"$1 intenta abrir $2.\n","$1 intenta abrirte.\n","Intentas abrir $2.\n",false);
						write( io.getColorCode("action") +  ((Item)patternMatchVectorSing.elementAt(0)).abrir(this) + io.getColorCode("reset") + "\n" );
					}	
					else if ( patternMatchVectorPlur.size() > 0 )
					{
						mirado = true;
						//no era en singular, probamos en plural.
						Item ourItem;
						//mirar todos los items
						for ( int i = 0 ; i < patternMatchVectorPlur.size() ; i++ )
						{
							ourItem = (Item)patternMatchVectorPlur.elementAt(i);
	
							//write( "Tratas de abrir " + ourItem.constructName2True ( 1 , this ) + " que llevas: " );
							write ( mundo.getMessages().getMessage("you.try.open.from.inventory","$item",ourItem.constructName2True ( 1 , this ),new Object[]{this,arguments} ));							
							
							habitacionActual.reportAction(this,ourItem,null,"$1 intenta abrir $2 que lleva.\n","$1 intenta abrirte.\n","Intentas abrir $2 que llevas.\n",false);
							write( io.getColorCode("action") + ((Item)ourItem).abrir(this) + io.getColorCode("reset") + "\n" );
	
						}	
					}
				} //fin (si tienes inventario)
	
	
	
			} //end if single object in string
	
	
			if(!mirado) //no abrimos nada.
			{
				escribirDenegacionComando(io.getColorCode("denial")+
						mundo.getMessages().getMessage("open.what",new Object[]{this,arguments})  //"�Qu� pretendes abrir?\n" 
						+io.getColorCode("reset")  );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				setNewState( 1 /*IDLE*/, 1 );
				return true;
			}
	
		} // FIN CMD ABRIR (estructura igual a mirar)
		else if ( lenguaje.translateVerb(command,"en").equalsIgnoreCase( "close" ) ) //cerrar
		{
	
	
	
			//Paso 1: Cerrar con llave. [si hay dos objetos en la entrada, el 1� ser� la puerta
			//y el 2� la llave, como en "cerrar la puerta roja con la llave amarilla p�lida"
	
			boolean mirado = false;
	
			mirado = cerrarPuertaConLlave ( habitacionActual.itemsInRoom , inventory );
	
			//probar a cerrar; pero algo que est� en nuestro inventario
			if ( !mirado )
				mirado = cerrarPuertaConLlave ( inventory , inventory );
	
			if ( !mirado ) //sera cerrar a secas (sin llave)
			{
	
				if ( habitacionActual.itemsInRoom != null && !habitacionActual.itemsInRoom.isEmpty() )
				{
	
					patternMatchVectorSing = new Vector();
					patternMatchVectorPlur = new Vector();
					if ( habitacionActual.itemsInRoom != null  )
					{
						patternMatchVectorSing = habitacionActual.itemsInRoom.patternMatch ( arguments , false ).toEntityVector(); //en singular
						patternMatchVectorPlur = habitacionActual.itemsInRoom.patternMatch ( arguments , true ).toEntityVector(); //en plural
					}
					if ( patternMatchVectorSing.size() > 0 /* && !(((Item)patternMatchVectorSing.elementAt(0)).getDescription(this).equals("")) */ ) //miramos un objeto
					{
						Item ourItem = ((Item)patternMatchVectorSing.elementAt(0));
						write( io.getColorCode("action") + ((Item)patternMatchVectorSing.elementAt(0)).cerrar(this) + io.getColorCode("reset") + "\n" );
						habitacionActual.reportAction(this,ourItem,null,"$1 intenta cerrar $2.\n","$1 intenta cerrarte.\n","Intentas cerrar $2.\n",false);
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
							//this is plain nonsense.
							//if ( !((Item)ourItem).getDescription(this).equals("")  )
							//{
								
								//write( "Intentas cerrar " + ourItem.constructName2True ( 1 , this ) + ": "  );
								write ( mundo.getMessages().getMessage("you.try.close.from.floor","$item",ourItem.constructName2True ( 1 , this ),new Object[]{this,arguments} ));
							
							
								habitacionActual.reportAction(this,ourItem,null,"$1 intenta cerrar $2.\n","$1 intenta cerrarte.\n","Intentas cerrar $2.\n",false);
								write( io.getColorCode("action") + ((Item)ourItem).cerrar(this) + io.getColorCode("reset") +"\n" );
							//}
						}	
					}
				} //fin (si hay items)	
				if ( inventory != null && !inventory.isEmpty() )
				{
	
					patternMatchVectorSing = new Vector();
					patternMatchVectorPlur = new Vector();
					if ( inventory != null  )
					{
						patternMatchVectorSing = inventory.patternMatch ( arguments , false ).toEntityVector(); //en singular
						patternMatchVectorPlur = inventory.patternMatch ( arguments , true ).toEntityVector(); //en plural
					}
					if ( patternMatchVectorSing.size() > 0 /* && !(((Item)patternMatchVectorSing.elementAt(0)).getDescription(this).equals("") ) */ ) //miramos un objeto
					{
						mirado=true;
						write( io.getColorCode("action") + ((Item)patternMatchVectorSing.elementAt(0)).cerrar(this) + io.getColorCode("reset") + "\n" );
					}	
					else if ( patternMatchVectorPlur.size() > 0 )
					{
						mirado = true;
						//no era en singular, probamos en plural.
						Item ourItem;
						//mirar todos los items
						for ( int i = 0 ; i < patternMatchVectorPlur.size() ; i++ )
						{
							ourItem = (Item)patternMatchVectorPlur.elementAt(i);
							if ( !ourItem.constructName2( 1 , this ).equals("") )
							{
								//write( "Tratas de cerrar " + ourItem.constructName2True ( 1 , this ) + " que llevas: " );
								
								write ( mundo.getMessages().getMessage("you.try.close.from.inventory","$item",ourItem.constructName2True ( 1 , this ),new Object[]{this,arguments} ));															
								
								habitacionActual.reportAction(this,ourItem,null,"$1 intenta cerrar $2 que lleva.\n","$1 intenta cerrarte.\n","Intentas cerrar $2 que llevas.\n",false);
								write( io.getColorCode("action") + ((Item)ourItem).cerrar(this) + io.getColorCode("reset") + "\n" );
							}
						}	
					}
				} //fin (si tienes inventario)
	
	
	
			}
	
			if(!mirado) //no cerramos nada.
			{
				escribirDenegacionComando ( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("close.what",new Object[]{this,arguments})  //"�Qu� pretendes cerrar?\n"  
						+ io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				setNewState( 1 /*IDLE*/, 1 );
				return true;
			}	
	
	
		} // FIN CMD CERRAR (estructura igual a mirar)
	
	
	
	
		//experimental
	
	
	
		//else if ( command.equalsIgnoreCase( "poner" ) || command.equalsIgnoreCase( "meter" ) )
		else if ( "put".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //poner, meter
		{
	
			boolean mirado = false;
			
			//Paso 0: ponerme [algo de mi inventario]
			//TODO: This case is very doubtful in multilanguage model, should probably be either removed or made optional (issue #244)
			if ( command.equalsIgnoreCase( "poner" ) && ParserMethods.refersToEntity(arguments, this, false) ) //the player appears as an argument
			{
				if ( !oneTargetAction("wear",arguments,inventory) )
				{
					escribirDenegacionComando( io.getColorCode("denial") + 
							mundo.getMessages().getMessage("wear.what",new Object[]{this,arguments})  //"�Qu� pretendes vestir?\n"
							+ io.getColorCode("reset") );
					mentions.setLastMentionedVerb(command);
					cancelPending();
					return false;		
				}
				else
				{
					mentions.setLastMentionedVerb(command);
					return true;
				}
			}
			
			//Paso 1: poner [algo de mi inventario] en [contenedor de la habitaci�n]
			
			if ( !mirado && habitacionActual.itemsInRoom != null && !habitacionActual.itemsInRoom.isEmpty()
					&& inventory != null && !inventory.isEmpty()
			)
			{
		
				mirado = putInside ( inventory , habitacionActual.itemsInRoom , arguments );
	
			} //end if (par inventario-habitaci�n)
	
			//Paso 2: poner [algo de mi inventario] en [contenedor tambi�n del inventario]
	
			if ( !mirado
					&& inventory != null && !inventory.isEmpty()
			)
			{
				
				mirado = putInside ( inventory , inventory , arguments );
	
			} //end if (mirar par inventario-inventario)
	
			if(!mirado) //no ponemos nada
			{
				escribirDenegacionComando( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("put.what.where",new Object[]{this,arguments})  //"�C�mo? �Poner qu� d�nde?\n" 
						+ io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}	
	
	
		} // FIN CMD PONER (estructura parecida a abrir/cerrar con llave)
	
	
	
	
		else if ( "take".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //coger
		{
			
			//TODO: This verb uses quite ancient things (that string that we are passing...) and should be updated to make
			//an onGetFrom event. Perhaps use the devices that we used for the double parseCommands (search for onContentsGeneric, etc.)
			//But have to think on how to integrate that with the action framework.
	
			boolean mirado = false;
			
			setNewState( 1 /*IDLE*/, 1 );
			
			//Paso 0: cogerme (o sea, quitarme) [algo de mi inventario]
			if ( ParserMethods.refersToEntity(arguments, this, false) ) //the player appears as an argument
			{
				if ( !oneTargetAction("unwear",arguments,getWornItems()) )
				{
					escribirDenegacionComando( io.getColorCode("denial") + 
							mundo.getMessages().getMessage("unwear.what",new Object[]{this,arguments})  //"�Qu� pretendes quitarte?\n"
							+ io.getColorCode("reset") );
					mentions.setLastMentionedVerb(command);
					cancelPending();
					return false;		
				}
				else
				{
					mentions.setLastMentionedVerb(command);
					return true;
				}
			}
			
			if ( !mirado ) mirado = cogerContenidoEspecificandoContenedor ( arguments , habitacionActual.itemsInRoom , "" );
	
			if ( !mirado ) mirado = cogerItem ( habitacionActual.itemsInRoom , null );
	
			if ( !mirado ) mirado = cogerContenido ( habitacionActual.itemsInRoom , "" );
	
			if ( !mirado ) mirado = cogerContenidoEspecificandoContenedor ( arguments , inventory , "" );
	
			if ( !mirado ) mirado = cogerContenido ( inventory , "" );
	
			if ( mirado )
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
			else
			{
				escribirDenegacionComando( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("get.what",new Object[]{this,arguments})  //"�Qu� pretendes coger?\n"
						+ io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}		
		} //FIN CMD COGER
	
	
		else if ( "drop".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //dejar
		{
	
			if ( !oneTargetAction("drop",arguments,inventory) )
			{
				escribirDenegacionComando( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("drop.what",new Object[]{this,arguments})  //"�Qu� pretendes dejar?\n"
						+ io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;		
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
	
		} //FIN CMD DEJAR
		else if ( "inventory".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) && arguments.trim().length() < 1 ) //inventario. deja de valer poner inventario algo.
		{
			setNewState( 1 /*IDLE*/, 1 );
			showInventory();
			mentions.setLastMentionedVerb(command);
			return true;
		} //FIN CMD INVENTARIO
	
		else if ( "spells".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) && arguments.trim().length() < 1 ) //hechizos. deja de valer poner hechizos algo.
		{
			if ( spellRefs != null )
			{
				write( io.getColorCode("information") + "Hechizos conocidos:\n" + io.getColorCode("reset") );
				for ( int i = 0 ; i < spellRefs.size() ; i++ )
				{
					Spell current = (Spell)spellRefs.get(i);
					write( io.getColorCode("information") + current.getUniqueName() + io.getColorCode("reset") + "\n" );
				}
	
			}
			else
			{
				write( io.getColorCode("information") + "No sabes hacer magia.\n" );
			}
			setNewState( 1 /*IDLE*/, 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		} //END CMD SPELLS
	
		else if ( "suicide".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //suicidar, suicidarse
		//else if ( command.equalsIgnoreCase( "suicidar" ) || command.equalsIgnoreCase( "suicidarse" ) )
		{
			suicide();
			//no necesitar�s zr con esto.
			return true;
		}
	
		else if ( "unwear".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //desvestir
		{
			if ( !oneTargetAction("unwear",arguments,getWornItems()) )
			{
				escribirDenegacionComando( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("unwear.what",new Object[]{this,arguments})  //"�Qu� pretendes quitarte?\n"
						+ io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;		
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
		}
	
		else if ( "wear".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //vestir
		{
			if ( !oneTargetAction("wear",arguments,inventory) )
			{
				escribirDenegacionComando( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("wear.what",new Object[]{this,arguments})  //"�Qu� pretendes vestir?\n"
						+ io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;		
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
		}
	
		else if ( "wield".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //blandir
		{
			if ( !oneTargetAction("wield",arguments,inventory) )
			{
				escribirDenegacionComando( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("wield.what",new Object[]{this,arguments})  //"�Qu� arma pretendes blandir?\n"
						+ io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;		
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
		}
	
		else if ( "unwield".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //enfundar
		{
			if ( !oneTargetAction("unwield",arguments,wieldedWeapons) )			
			{
				escribirDenegacionComando( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("unwield.what",new Object[]{this,arguments})  //"�Qu� arma enfundar?\n"
						+ io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;		
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
		}
	
	
	
		else if ( "say".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //decir
		{
			//habitacionActual.informActionAuto ( this , null , "$1 dice \"" + arguments + "\"\n" );
			//say(arguments);
			//escribir("\n");
	
			setNewState( 1 /*IDLE*/, 1 );
			comandoDecir ( arguments );
			mentions.setLastMentionedVerb(command);
			return true;
		}
		else if ( command.equalsIgnoreCase( "salir" ) )
		{
			System.exit(0);
		}
		/*
		else if ( command.equalsIgnoreCase( "debug" ) )
		{
			write("\nDebug Output:");
			write("\nInventory:\n");
			if ( inventory != null )
			{
				for ( int i = 0 ; i < inventory.size() ; i ++ )
				{
					write(inventory.elementAt(i).getID() + "");
				}
			}
			//escribir("\nCurrent room inventory (raw string)");
			//Debug.println(habitacionActual.getContents());
			return true;
		}
		*/
		else if ( "wait".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //esperar
		{
			if ( StringMethods.numToks(commandstring,' ') < 2 )
			{
				//esperar 1 UGT
				setNewState( 1 /*IDLE*/, 1 );
				mentions.setLastMentionedVerb(command);
				return true;	
			}
			else
			{
				int nsecs = 1;
				try
				{
					nsecs = Integer.valueOf( arguments ).intValue();
				}
				catch ( NumberFormatException nfe )
				{
	
				}
				setNewState( 1 /*IDLE*/, nsecs );
				mentions.setLastMentionedVerb(command);
				return true;
			}
		}
		else if ( "talk".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //hablar
		{
			escribirDenegacionComando( io.getColorCode("denial") + "La mejor forma de hablar es decir algo.\n" + io.getColorCode("reset") );
			mentions.setLastMentionedVerb(command);
			cancelPending();
			return false;
		}
		/*
		else if ( command.equalsIgnoreCase( "salvar" ) || command.equalsIgnoreCase("save")
				|| command.equalsIgnoreCase( "cargar" ) || command.equalsIgnoreCase("load")
				|| command.equalsIgnoreCase( "grabar" ) || command.equalsIgnoreCase("restaurar") )
		*/
		else if ( "load".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) || "save".equalsIgnoreCase(lenguaje.translateVerb(command,"en"))  ) //salvar, grabar, cargar, restaurar, save, load
		{
			escribirDenegacionComando( io.getColorCode("error") + "De momento, las opciones de salvar y cargar no est�n disponibles por texto en este interfaz. Puedes usar los men�s para ello.\n" + io.getColorCode("reset") );
			cancelPending();
			return false;
		}
	
	
		//else if ( command.equalsIgnoreCase("invocar") || command.equalsIgnoreCase("convocar") || command.equalsIgnoreCase("hacer") || command.equalsIgnoreCase("realizar") || command.equalsIgnoreCase("usar") || command.equalsIgnoreCase("utilizar") || command.equalsIgnoreCase("crear") || command.equalsIgnoreCase("ejecutar") || command.equalsIgnoreCase("pronunciar") || command.equalsIgnoreCase("conjurar") )
		else if ( "invoke".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) || "convoke".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) 
				|| "make".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) || "realize".equalsIgnoreCase(lenguaje.translateVerb(command,"en"))
				|| "create".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) || "use".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) 
				|| "execute".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) || "pronounce".equalsIgnoreCase(lenguaje.translateVerb(command,"en"))
				|| "cast".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) )
		{
	
			//try to cast spell
	
			boolean mirado = false;
	
			EntityList possibleSpellTargets = new EntityList();
			for ( int i = 0 ; i < habitacionActual.mobsInRoom.size() ; i++ )
			{
				possibleSpellTargets.addEntity ( habitacionActual.mobsInRoom.elementAt(i) );
			}
			for ( int i = 0 ; i < inventory.size() ; i++ )
			{
				possibleSpellTargets.addEntity ( inventory.elementAt(i) );
			}
			//if ( habitacionActual.getInventory() != null )
			//{
			for ( int i = 0 ; i < habitacionActual.getInventory().size() ; i++ )
			{
				possibleSpellTargets.addEntity ( habitacionActual.getInventory().elementAt(i) );
			}
			//}
	
			mirado = hacerHechizo ( possibleSpellTargets , getSpells() );
	
			if(!mirado) //no atacamos nada, no nos entiende.
			{
				escribirDenegacionComando(io.getColorCode("denial")+mundo.getMessages().getMessage("cast.no.spell",new Object[]{this,arguments})+io.getColorCode("reset")  );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}
			else
			{
				mentions.setLastMentionedVerb(command);
				return true;
			}
	
	
	
	
		}
		
	
	
	
		//reconocemos el verbo; pero no tenemos npi de qu� hacer con �l...
		//TODO remmed "else" from here.
		else if ( lenguaje.isVerb ( command , true ) )
		{			
			String origCommand = "";
			if ( originalTrimmedCommandString != null )
				origCommand = StringMethods.getTok(originalTrimmedCommandString,1,' ').trim();
			//Debug.println("origCommand: " + origCommand +"ceremonia del te");
			if ( !origCommand.endsWith("me") && !origCommand.endsWith("te") && !origCommand.endsWith("se") )
			{
				//escribirDenegacionComando ( io.getColorCode("denial") + "�C�mo? �" + commandstring + "?\n" + io.getColorCode("reset") );
				escribirDenegacionComando ( io.getColorCode("denial") + mundo.getMessages().getMessage("undefined.action","$command",commandstring,new Object[]{this,commandstring}) + io.getColorCode("reset") );
			}
			else
			{
				//si el comando se refer�a al propio jugador, poner el string sin pronombres sustituidos, porque queda un poco feo que diga "�c�mo? �matar a jugador?"
				//escribirDenegacionComando ( io.getColorCode("denial") + "�C�mo? �" + originalTrimmedCommandString + "?\n" + io.getColorCode("reset") );
				escribirDenegacionComando ( io.getColorCode("denial") + mundo.getMessages().getMessage("undefined.action","$command",originalTrimmedCommandString,new Object[]{this,originalTrimmedCommandString}) + io.getColorCode("reset") );
			}
			mentions.setLastMentionedVerb(command);	
			cancelPending();
			return false;
		}
	
		//si llegamos aqui, el verbo no es ninguno de los reconocidos, ni ninguno
		//de los ejecutables en EVA, ni nada.
		//Vemos a ver si es el nombre de un bicho para hablarle (Mar�a, hola)
	
		else if ( !commandQueue.isEmpty() && ParserMethods.refersToEntityIn(command, this.getAllWorldMobiles(), false).size() > 0 )
		{
			String whatToSay = ((String)commandQueue.elementAt(0)).trim();
			commandQueue.removeElementAt(0);
			Mobile whomToSayItTo = (Mobile) ParserMethods.refersToEntityIn(command, this.getAllWorldMobiles(), false).toEntityVector().get(0);
			return execCommand("decir a " + whomToSayItTo.getBestReferenceName(false) + "\"" + whatToSay + "\"");
		}
	
		//Si llegamos aqu�, no s�lo no es un verbo sino que no es una construcci�n tipo "Mar�a, hola".
		//Ahora, si no lo hemos hecho ya, probamos
		//a ver si es que ha habido una elipsis de verbo, a�adiendo el verbo de la zona
		//de referencia.
		else if ( !mentions.getLastMentionedVerb().equalsIgnoreCase( command ) && 
				!command.matches("^"+mentions.getLastMentionedVerb()+"(_|\\b).*") /*for English phrasal verbs (1)*/ &&
				lenguaje.isGuessable(mentions.getLastMentionedVerb()) )
		{
	
			//(1) to solve cases like: last mentioned = look, command = look_up, and we add an extra "look".
			
			/*DON'T USE QUEUE, JUST EXEC'IT*/
			/*
			//aqui nos saltamos la cola de comandos cambiando el primero...
			//por algo es un vector, no una autentica cola :)
			Vector temp = (Vector)commandQueue.clone();
			commandQueue = new Vector();
			commandQueue.addElement(ZR_verbo + " " + commandstring);
			commandQueue.addAll(temp);	
			nextCommandSecondChance = true;
			return execCommand ( mundo );
			 */
	
			secondChance = true;
			
			//System.err.println("Last mentioned verb: " + mentions.getLastMentionedVerb());
			//System.err.println("Command: " + command);
	
			return execCommand (mentions.getLastMentionedVerb() + " " + commandstring );
	
	
		}
		else
		{
			escribirErrorNoEntiendo();
		}
	
	
	
		//here, if actionName and actionArgs are set, execute action!!!
	
		if ( ! actionName.equals("") )
		{
			return executeAction ( actionName , actionArgs );
		}
	
	
	
		return false;
	}

	public void show_room(World mundo) {
	
		long comparand;
		//escribir("\n");
	
		//escribir(habitacionActual.getTitle()+"\n");
	
		comparand = (long)this.getRelationshipState( habitacionActual )*((long)Math.pow(2,32)) + habitacionActual.getState();
		//Debug.println("CMP: " + comparand);
		//de momento el comparando viene a ser el estado de la habitacion.
		write(/*" "+*/io.getColorCode("description")+habitacionActual.getDescription(this)+io.getColorCode("reset")+"\n");
		//getitemdes
	
		try
		{
			habitacionActual.execCode("event_showroom","this: " + habitacionActual.getID() + "\n" + "player: " + getID() + "\n" + "orig: " + habitacionAnterior );		
			habitacionActual.execCode("onShowRoom" , new Object[] {this} );
		}
		catch ( EVASemanticException exc ) 
		{
			write( io.getColorCode("error") + "EVASemanticException found at event_showroom , room number " + habitacionActual.getID() + io.getColorCode("reset") );
		}
		catch ( ScriptException bshte )
		{
			write( io.getColorCode("error") + "bsh.TargetError found onShowRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
			writeError(ExceptionPrinter.getExceptionReport(bshte));
		}
	
	}

	/**
	 * Substitutes pronouns AND aplies spell checking if enabled
	 */
	public String substitutePronounsInSentence(String commandstring) { //matalo con el cuchillo
		StringTokenizer st = new StringTokenizer(commandstring);
		String originalVerb = st.nextToken(); //matalo
		String expandedString = lenguaje.substitutePronouns ( this , commandstring , mentions ); //mata Juanito con el cuchillo
		String expandedVerbWithoutPronoun = firstWord ( expandedString ); //mata
		//String expandedString = expandedVerb + " " + restWords(commandstring); //mata Juanito con el cuchillo
		String workingString;
		if ( /*!lenguaje.isVerb(originalVerb) &&*/ lenguaje.isVerb(firstWord(expandedVerbWithoutPronoun)) ) 
			workingString = expandedString; //pronoun substitution was useful
		else
			workingString = commandstring; //first word could be something like "habla" or "bote", better not to expand it if not sure.
		if ( !getPropertyValueAsBoolean("noVerbSpellChecking") )
		{
			return mundo.getSpellChecker().correctCommandString(workingString);
		}
		else
		{
			return workingString;
		}
		/*
		String subs_command = substitutePronounsIfVerb ( StringMethods.getTok(commandstring,1,' ') );		
		commandstring = subs_command + " " + StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ');
		return commandstring;
		*/
	}

	/**
	 * Used for commands of the kind "Mar�a, hola" (to recognise mobile names as verbs)
	 * @return
	 */
	private EntityList getAllWorldMobiles() {
		if ( mobilesCache == null )
			mobilesCache = mundo.getAllMobiles();
		return mobilesCache;
	}
	
	/**
	 * Moves the first command from the command queue to the commandstring attribute (command to be executed).
	 * Also handles the second chance flags if applicable.
	 * Returns false if for some reason no command was obtained.
	 * @return
	 */
	protected boolean obtainCommandFromQueue()
	{
		if ( nextCommandSecondChance )
		{
			secondChance = true; //estamos en un comando de segunda oportunidad
			nextCommandSecondChance = false;
		}

		//quitar el primer elemento (cabeza) de commandQueue
		commandstring = ((String)commandQueue.elementAt(0)).trim();

		//Debug.println("(1) Command string set to " + (String)commandQueue.elementAt(0) );
		commandQueue.removeElementAt(0);
		
		//en la cola metemos sentencias simples; pero al sustituir los pronombres pueden 
		//dar lugar de nuevo a multiplicidad.
		return separateSentences();
	}
	
	/**
	 * Executes the preprocessCommand() scripted method for the given input.
	 * @return true if preprocessCommand() has hit end(), false if not (thus if false we will contiune command execution).
	 */
	protected boolean runPreprocessCommand()
	{
		/*Llamada a preprocessCommand() configurable*/
		boolean executed = false;
		try
		{
			ReturnValue retval = new ReturnValue(null);
			executed = mundo.execCode( "preprocessCommand" , new Object[] { this , commandstring } , retval );
			if ( retval.getRetVal() != null )
			{
				commandstring = (String)retval.getRetVal();
				//Debug.println("Command String Changed To " + (String)retval.getRetVal()); 
				command = lenguaje.extractVerb(commandstring).trim(); //StringMethods.getTok(commandstring,1,' ').trim();
				arguments = lenguaje.extractArguments(commandstring).trim(); //StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
			}
		}
		catch ( ScriptException te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at preprocessCommand, raw command was " + commandstring + ", error was " + te + io.getColorCode("reset") );
			writeError(ExceptionPrinter.getExceptionReport(te));
		}
		if ( executed )
		{
			//luego esto lo hara el codigo
			setNewState( 1 /*IDLE*/, 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}
		else
		{
			return false;
		}
		/*Fin de preprocessCommand()*/
	}
	
	/**
	 * Runs a command prefixed by "eval" (to evaluate scripted code for debugging) if applicable (i.e. if the current command really
	 * starts with "eval") and returns true. If not applicable, returns false.
	 * @return
	 */
	protected boolean runEvalIfApplicable()
	{
		if ( commandstring.startsWith( "eval " ) && Debug.isEvalEnabled() )
		{
			ReturnValue retVal = new ReturnValue(null);
			arguments = StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
			try {
				mundo.getAssociatedCode().evaluate(arguments,this,retVal);
			} catch (ScriptException e) {
				writeError(ExceptionPrinter.getExceptionReport(e));
			}
			this.write(""+retVal.getRetVal()+"\n");
			return true;
		}
		return false;
	}
	
	
	/**
	 * Obtains an executes a command.
	 * The command can be obtained:
	 * - From the command queue,
	 * - From the "forced command" string.
	 * The subclass Player overrides this method in order to be able to obtain commands from logs or from client input as well.
	 */
	public boolean obtainAndExecCommand ( World mundo ) throws java.io.IOException
	{

		/*
		 * This loop is so that we can have metacommands that don't consume time.
		 * In normal cases, we execute a command and return from the function so only one iteration is ran.
		 * But when a metacommand is used, we do 'continue' so the function doesn't return (and no game time is consumed).
		 */
		for(;;)
		{
		
			secondChance = false; //luego se pone a true en obtainCommandFromQueue() si hace falta
			
			//mirar si cola de comandos vacia
			//el !forced es porque si hemos forzado un comando, pasa por delante de la cola
			if ( !commandQueue.isEmpty() && !forced ) //obtain enqueued piece of command - this was not a directly input command so it is not subject to preprocessCommand and eval
			{
				if ( !obtainCommandFromQueue() ) return false;
			}
			else
			{
				if ( forced )
				{
					forced = false;
					io.forceInput ( force_string , false );
					commandstring = force_string;
				}
				else
				{
					return false; //we don't have a command.
				}
	
				//Process raw command:
				
				/*Preparaci�n del comando:*/
				if ( commandstring != null ) commandstring = commandstring.trim();
				
				/*Llamada a preprocessCommand() configurable*/
				if ( runPreprocessCommand() ) return true;
	
				//comando nulo
				if ( commandstring == null || commandstring.equals("") ) return false;
	
				/*Comandos eval - continue porque no se ejecuta un comando normal, es un metacomando de fuera del mundo*/
				if ( runEvalIfApplicable() ) continue;
				
				/*Separate commands composed of several sentences. The sentences are placed in the queue. False is returned only if the command is actually
				 * empty (e.g. a command consisting only of commands)*/	
				if ( !separateSentences() ) return false;
	
			}
	
			//modular execCommand()
	
			if ( commandstring.isEmpty() ) return false; //empty strings can result if, for example, input was ",something", etc.
			
			return execCommand ( commandstring  );
		
		}

	}

	
}