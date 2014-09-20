/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
//package AetheriaAWT;
import java.util.*;
import java.io.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.messages.Messages;
import eu.irreality.age.scripting.ScriptException;
import eu.irreality.age.util.VersionComparator;
public class Room extends Entity implements Descriptible , SupportingCode, UniqueNamed
{

	//al final claudicamos
	private World mundo;
	
	//////////////////////
	//INSTANCE VARIABLES//
	//////////////////////
	
	/**ID de la habitaci�n que se usa para referirse a ella.*/
	/*01*/ private int idnumber;
	
	/**ID de la habitaci�n de que se hereda.*/
	/*02*/ private int inheritsFrom; 
	
	/*03*/ // inherited protected int state;
	// inherited protected long timeunitsleft;
	
	/**Nombre sint�tico de la habitaci�n.*/
	/*04*/ protected String title;
	
	/**Lista din�mica de descripciones.*/
	/*10*/ protected Description[] descriptionList;
	/**Lista din�mica de salidas est�ndar.*/
	/*11*/ protected Path[] standardExits;
	/**Lista din�mica de otras salidas.*/
	/*12*/ protected Path[] otherExits;
	
	/**Lista din�mica de objetos.*/
	//no s�, tal vez mejor vector, por aquello de din�mica.
	/*20*/ protected Inventory itemsInRoom; //item vector
	/**Lista din�mica de bichos.*/
	/*21*/ protected MobileList mobsInRoom;
	/**Lista din�mica de personajes.*/
	
	/**Descripciones de cosas de la habitaci�n.*/
	/*30*/ protected String extraDescriptions; //OLD
	protected List extraDescriptionArrays; /*List of Description Arrays*/
	protected List extraDescriptionNameArrays; /*List of String Arrays*/
	
	
	/**Restricciones de acceso y otras.*/
	/*31*/ protected Vector onlyRestrictions;
	
	/**C�digo en Ensamblador Virtual Aetheria (EVA)*/
	/*80*/ protected ObjectCode itsCode;
	
	
	
	private NaturalLanguage lenguaje;
	private java.util.Random aleat;
	

	///////////
	//METHODS//
	///////////
	
	/**
	* Este constructor solo llama a constructRoom, que es el constructor de verdad. Esta dualidad se debe a las llamadas recursivas que debe hacer constructRoom para soportar herencia dinamica (habitaciones que copian datos de otras)
	*
	*/
	public Room ( World mundo , String roomfile ) throws IOException , FileNotFoundException
	{
		constructRoom ( mundo , roomfile );
	}
	
	public Room ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		constructRoom ( mundo , n , true );
	}
	
	/**
	* El pedazo de constructor que lee una habitaci�n de un fichero.
	* <nota: random number seed se setea despu�s>
	*/
	public void constructRoom ( World mundo , String roomfile ) throws IOException, FileNotFoundException
	{
		
		this.mundo = mundo;
		
		/*este metodo nos dice cuando ya hemos realizado una llamada recursiva al constructor para la herencia (la herencia ha terminado)*/
		boolean inheritance_done = false;
		String linea;
		String id_linea;
		lenguaje = mundo.getLanguage();
//		aleat = mundo.getRandom();
//		Debug.println("Random number generator set to " + aleat );
		FileInputStream fp = new FileInputStream ( roomfile );
		BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( fp ) );
		for ( int line = 1 ; line < 100 ; line++ )
		{
			linea = filein.readLine();
			id_linea = StringMethods.getTok( linea , 1 , ' ' );
			linea = StringMethods.getToks( linea , 2 , StringMethods.numToks( linea , ' ' ) , ' ' );
			if ( id_linea != null ) switch ( Integer.valueOf(id_linea).intValue() )
			{
				case 1:
					idnumber = Integer.valueOf(linea).intValue(); break;
				case 2:
					inheritsFrom = Integer.valueOf(linea).intValue();
					if ( inheritsFrom < idnumber && !inheritance_done ) /*la habitacion de la que heredamos debe tener ID menor*/
					{
						/*construimos segun constructor de la habitacion de que heredamos*/
						constructRoom ( mundo , Utility.roomFile(mundo,inheritsFrom) ); 
						/*no haremos mas llamadas recursivas de estas*/
						inheritance_done = true;
						/*overrideamos lo que tengamos que overridear*/
						constructRoom ( mundo , roomfile );
						return; 
					}
					break;
				case 3:
					setNewState( Integer.valueOf(linea).intValue() ); 	break;
				case 4:
					title = linea; break;	
				case 10:
				//room description list line
				{
					//descriptionList = new Description[ StringMethods.numToks(linea,'&')/3 ];
					//for ( int i = 0 ; i+3 <= StringMethods.numToks(linea,'&') ; i+=3 )
					//{
					//	descriptionList[i/3] = new Description ( StringMethods.getTok( linea,i+3,'&' ) , Integer.valueOf(StringMethods.getTok( linea,i+1,'&' )).intValue() , Integer.valueOf(StringMethods.getTok( linea,i+2,'&' )).intValue()  );
						//comparando:token i+1
						//mascara:token i+2
						//texto:token i+3	
					//}
					descriptionList=Utility.loadDescriptionListFromString( linea );	
					//Debug.print(descriptionList[0].getText());
					break;
				}
				case 11:
				//standard exit line
				{
					standardExits = new Path[10];
					if ( StringMethods.numToks( linea , '@' ) < 10 )
						System.out.println("[SINTAXIS] l�nea 10 (" + roomfile + ") insuficientes tokens con @");
					for ( int i = 0 ; i < 10 ; i++ )
					{
						String curToken = StringMethods.getTok( linea , i+1 , '@' );
						standardExits[i] = new Path ( mundo , true , curToken ); //el true indica que es est�ndar.
					}
					break;
				}
				case 12:
				//non-standard exit line
				{
					otherExits = new Path[StringMethods.numToks( linea , '@' )];
					for ( int i = 0 ; i < otherExits.length ; i++ )
					{
						String curToken = StringMethods.getTok( linea , i+1 , '@' );
						otherExits[i] = new Path ( mundo , false , curToken ); //false = non-standard exit	
					}	
					break;
				}
				case 20:
				//item references line
				{
					int nObjects = StringMethods.numToks(linea,'$');
					itemsInRoom = new Inventory ( 1000000,1000000,nObjects );
					 //Item[nObjects];
					for ( int i = 0 ; i < nObjects ; i++ )
					{
						try
						{
						itemsInRoom.addItem ( mundo.getItem(StringMethods.getTok(linea,i+1,'$')) );
						}
						catch (WeightLimitExceededException exc) 
						{
							mundo.write("Item too heavy for room");
						}
						catch (VolumeLimitExceededException exc2)
						{
							mundo.write("Item too big for room");
						}
					}
					break;
				}
				case 21:
				//mobile references line
				{
					//System.out.println("Veintiuno");
					int nObjects = StringMethods.numToks(linea,'$');
					//System.out.println(nObjects + " in " + idnumber );
					mobsInRoom = new MobileList();
					for ( int i = 0 ; i < nObjects ; i++ )
					{
						Mobile ourMob = mundo.getMob(StringMethods.getTok(linea,i+1,'$'));
						mobsInRoom.addElement ( ourMob );
						ourMob.setRoom(this);
						ourMob.setRoom(this); //actual y anterior	
					}
					break;
				}
				case 30:
				//extra description line
				{
					extraDescriptions = linea;
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
		
		//poner bien la id
			if ( getID() < 10000000 )
			idnumber += 10000000; //prefijo de habitacion	
	}
	
	
	public void constructRoom ( World mundo , org.w3c.dom.Node n , boolean allowInheritance ) throws XMLtoWorldException
	{
	
		this.mundo = mundo;
		lenguaje = mundo.getLanguage();
	
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ( "Room node not Element" ) );
		}
		//{n is an Element}	
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;
		
		//weak inheritance?
		if ( e.hasAttribute("extends") && !e.getAttribute("extends").equals("0") && !e.getAttribute("extends").equals("null") && allowInheritance )
		{
			//item must extend from existing item.
			//clonamos ese item y overrideamos lo overrideable
			//(n�tese que la ID del item extendido ha de ser menor).
			
			//por eso los associated nodes de los items quedan guardados [por ref] en el World hasta que
			//haya concluido la construccion del mundo
			
			//1. overrideamos el super-item usando su associated node para construirlo
			
			constructRoom ( mundo , mundo.getRoomNode( e.getAttribute("extends") ) , true );
			
			//2. overrideamos lo que debamos overridear
			
			constructRoom ( mundo , n , false );
		}
	
		//mandatory XML-attribs exceptions
		
		/*as of 03.09.15, ID no longer mandatory*/
		//if ( !e.hasAttribute("id") )
		//	throw ( new XMLtoWorldException ( "Room node lacks attribute id" ) );
		
		if ( !e.hasAttribute("name") )
			throw ( new XMLtoWorldException ( "Room node lacks attribute name" ) );
	
		//mandatory XML-attribs parsing
		try
		{
			//id no longer mandatory
			if ( e.hasAttribute("id") )
				idnumber = Integer.valueOf ( e.getAttribute("id") ).intValue();
			//Debug.println("Room id " + idnumber);
		}
		catch ( NumberFormatException nfe )
		{
			throw ( new XMLtoWorldException ( "Bad number format at attribute id in mobile node" ) );
		}
		title = e.getAttribute("name");
		
		//Entity parsing
		readPropListFromXML ( mundo , n );
		
		//description list
		descriptionList = Utility.loadDescriptionListFromXML ( mundo , e , "DescriptionList" , true );
	
		//path lists: standardExits[10] and otherExits[..]
	
		org.w3c.dom.NodeList pathListNodes = e.getElementsByTagName ( "PathList" );
		if ( pathListNodes.getLength() > 0 )
		{
			
			org.w3c.dom.Element pathListNode = (org.w3c.dom.Element) pathListNodes.item(0);
			org.w3c.dom.NodeList pathNodes = pathListNode.getElementsByTagName ( "Path" );
			
			//count non-standard exits to init array
			int nNonStandard = 0;
			for ( int i = 0 ; i < pathNodes.getLength() ; i++ )
			{
				org.w3c.dom.Element curNode = (org.w3c.dom.Element) pathNodes.item(i);
				
				//check if there are custom commands defined for the exit
				boolean hasCommands = false;
				if  ( curNode.getElementsByTagName("CommandList").getLength() > 0 ) 
				{
					org.w3c.dom.Element commandListNode = (org.w3c.dom.Element) curNode.getElementsByTagName("CommandList").item(0);
					if ( commandListNode.getElementsByTagName("Command").getLength() > 0 )
						hasCommands = true;
				}
				
				if ( ! ( Boolean.valueOf ( curNode.getAttribute("standard") ).booleanValue() ) || hasCommands  )
					nNonStandard++;
			}
			
			//init arrays
			standardExits = new Path[10];
			otherExits = new Path[nNonStandard];
		
			//parse path nodes
			int nonStandardExitCounter = 0; //numero de nonstandard por la que vamos
			for ( int i = 0 ; i < pathNodes.getLength() ; i++ )
			{
				//Debug.println("Path node " + i);
				org.w3c.dom.Element curNode = (org.w3c.dom.Element)pathNodes.item(i);
				Path p = new Path ( mundo , curNode );
				Debug.println("Path " + i + " for room " + getID());
				if ( p.isStandard() )
				{
					int direccion = Path.nameToDirection( curNode.getAttribute("direction") );
					standardExits[direccion] = p;
					//la salida puede tener comandos aunque sea estandar
					if ( p.isExtended() )
					{
						otherExits[nonStandardExitCounter] = p;
						nonStandardExitCounter++;
					}
				}
				else
				{
					otherExits[nonStandardExitCounter] = p;
					nonStandardExitCounter++;
				}
			}
		}
		else
		{
			standardExits = new Path[10];
			otherExits = new Path[0];
		}
		
		//Debug.println("Will null-fill exits");
		
		//now fill standard exits with invalid exits on nulls
		for ( int i = 0 ; i < standardExits.length ; i++ )
		{
			if ( standardExits[i] == null )
			{
				standardExits[i] = new Path ( mundo , true , "0" ); //el true indica que es est�ndar.
				//el 0 har� que produzca una salida inv�lida
			}
		}
		
		//Debug.println("Will parse inventory");
		
		//no need for cargas diferidas for inventory and mobile list: items and mobiles load
		//before rooms!
		
		org.w3c.dom.NodeList inventoryNodes = e.getElementsByTagName ( "Inventory" );
		//solo nos interesan los hijos DIRECTOS
		List realInventoryNodes = new ArrayList();
		for ( int i = 0 ; i < inventoryNodes.getLength() ; i++ )
			if ( inventoryNodes.item(i).getParentNode() == e ) realInventoryNodes.add ( inventoryNodes.item(i) );
		
		if ( realInventoryNodes.size() < 1 )
		{
			//Debug.println("No inventory nodes, inventory will be left null.");
			itemsInRoom = null;
		}
		else
		{
			//Debug.println("Inventory nodes present.");
			itemsInRoom = new Inventory ( mundo , (org.w3c.dom.Node)realInventoryNodes.get(0) );
			//Debug.println("Inventory size: " + inventory.size() );
			//Debug.println("Inventory node: " + inventoryNodes.item(0) );
			//Debug.println("Inventory's parent: " + inventoryNodes.item(0).getParentNode() );
			//Debug.println(inventory);
		}		
		
		//add room refs to items
		if ( itemsInRoom != null )
			for ( int i = 0 ; i < itemsInRoom.size() ; i++ )
			{
				Item cur = itemsInRoom.elementAt(i);
				cur.addRoomReference(this);
			}
		
		
		
		//Debug.println("Will parse moblist");
		
		org.w3c.dom.NodeList moblistNodes = e.getElementsByTagName ( "MobileList" );
		if ( moblistNodes.getLength() < 1 )
			mobsInRoom = new MobileList();
		else
		{
			mobsInRoom = new MobileList ( mundo , moblistNodes.item(0) );
			
			if ( mobsInRoom == null )
				mobsInRoom = new MobileList();
			
			//la guardamos porque el hacer setRoom() puede alterar el orden de la MobileList
			MobileList copiaMobsInRoom = new MobileList ( mundo , moblistNodes.item(0) );
			
			//set current room var in mobiles
			for ( int i = 0 ; i < copiaMobsInRoom.size() ; i++ )
			{
				//Debug.println("Mobs in Room: " + mobsInRoom.size() );
				//Debug.println("Actually: " + mobsInRoom);
				Mobile ourMob = copiaMobsInRoom.elementAt(i);
				
				Debug.println("i= " + i + ": Setting room on mob " + ourMob + " to " + this);
				
				ourMob.setRoom(this);
				ourMob.setRoom(this); //actual y anterior
			}
			
			
		}	
		
		//Debug.println("Will parse extrades");
		
		//extra descriptions
		
		//extraDescriptions = Utility.loadExtraDescriptionsFromXML ( e , "ExtraDescriptionList" , true );
		
		//extraDescriptionList = Utility.loadExtraDescriptionsFromXML ( mundo , e , "ExtraDescriptionList" , true );
		
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
		
		
		//Debug.println("Will parse code");
		
		//code
		
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
		
		//Debug.println("Will correct ID");
		
		//poner bien la id
		if ( getID() == 0 )
		{
			//no se especific� id, la asignar� el mundo.
			;
		}
		else if ( getID() < 10000000 )
			idnumber += 10000000; //prefijo de habitacion	
	
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
			mundo.writeError(ExceptionPrinter.getExceptionReport(te));
			te.printStackTrace();
		}
		
	}
	
	public void setID ( int newid )
	{
		if ( newid < Utility.room_summand )
			idnumber = newid + Utility.room_summand;
		else
			idnumber = newid;
	}
	
	public boolean isValidExit( boolean isStandard , int exitn )
	{
		if ( isStandard )
			return standardExits[exitn].isValid();
		else
		{
			Debug.println("Other Exits: " + otherExits);
			Debug.println(""+exitn);
			Debug.println("Len " + otherExits.length);
			for ( int i = 0 ; i < otherExits.length ; i++ )
				Debug.println(otherExits[i]);
			return otherExits[exitn].isValid();	
		}
	}
	
	public Path[] getValidExits ( )
	{
		List caminos = new ArrayList();
		for ( int i = 0 ; i < standardExits.length ; i++ )
		{
			if ( standardExits[i].isValid() )
				caminos.add ( standardExits[i] );
		}
		for ( int i = 0 ; i < otherExits.length ; i++ )
		{
			if ( otherExits[i].isValid() )
				caminos.add ( otherExits[i] );
		}	
		Path[] caminos_ar = new Path[caminos.size()];
		for ( int i = 0 ; i < caminos_ar.length; i++ )
		{
			caminos_ar[i] = (Path) caminos.get(i);
		}
		return caminos_ar;
	}
	
	public Path getExit ( boolean isStandard , int exitn )
	{
		if ( isStandard ) return standardExits[exitn];
		else return otherExits[exitn];	
	}
	
	public Path getPath ( boolean isStandard , int exitn )
	{
		return getExit ( isStandard , exitn );
	}
	
	public Path[] getStandardExits ( )
	{
		return standardExits;
	}
	public Path[] getNonStandardExits ( )
	{
		return otherExits;
	}
	
	public int getRandomValidExitAsNumber (  )
	{
		int nvalid_standard = 0 , nvalid_nonstandard = 0;
		for ( int i = 0 ; i < standardExits.length ; i++ )
		{
			if ( isValidExit( true , i ) ) nvalid_standard++;
		}
		for ( int i = 0 ; i < otherExits.length ; i++ )
		{
			if ( isValidExit( false , i ) ) nvalid_nonstandard++;
		}
		
		//Debug.println("nvalid = " + nvalid_standard + " + " + nvalid_nonstandard );
		
		if ( nvalid_standard + nvalid_nonstandard < 1 ) return -1; //ninguna salida v�lida
		
		int numsalida = aleat.nextInt( nvalid_standard + nvalid_nonstandard );
		int i,j;
		i = 0;
		//System.err.println("Room " + this + " numsal " + numsalida);
		for ( int k = 0 ; k < standardExits.length && k < numsalida ; k++ )
		{
			if ( isValidExit( true , i ) ) i++;
			if ( i == numsalida )
			{
				return i-1;
			}
		}

		for ( j = i ; j-i < otherExits.length && j < numsalida ; )
		{
			if ( isValidExit(false,j-i) ) j++;
		}
		//return otherExits[j-i-1]; 
		return 10 /*nstandardexits*/ + j - i - 1; 
	}
	
	public Path getRandomValidExit ( )
	{
		int nsal = getRandomValidExitAsNumber();
		if ( nsal < 0 ) return null; 
		if ( nsal < 10 )
			return standardExits[nsal];
		else
			return otherExits[10-nsal];	
	}
	
	/**
	 * Obtains the non-standard path from this room that best matches the given arguments.
	 * @param arguments
	 * @return
	 */
	public Path getNonStandardExitMatchingArguments ( String arguments )
	{
		Path result = null;
		
		if ( new VersionComparator().compare(mundo.getParserVersion(),"1.3.0") < 0 )
		{
			
			//old behaviour: only matches with the end of the command
			//not sure if there is really any point on maintaining this. I can't really think of a case where going with the new behaviour in an old game could 
			//be problematic.
			//Mirar las salidas personalizadas
			for ( int i=0 ; i < otherExits.length ; i++ )
			{
				if ( isValidExit(false,i) && getExit(false,i).matchExitCommand( arguments , false ) >= 0 )
				{
					result = getExit ( false , i );	
				}
			}
		
		}
		else
		{
			
			//new behaviour: matches with any part of the command, returning the longest path name that is contained in it
			int bestMatch = -1;
			for ( int i=0 ; i < otherExits.length ; i++ )
			{
				if ( isValidExit(false,i) )
				{
					int lengthMatched = getExit(false,i).matchExitCommand( arguments , true );
					if ( lengthMatched > bestMatch )
					{
						result = getExit ( false , i );	
						bestMatch = lengthMatched;
					}
				}
			}
			
		}
		
		return result;
	}
	
	
	/**
	 * Obtains the standard path from this room that best matches the given arguments, if there's any.
	 * @param arguments
	 * @return
	 */
	public Path getStandardExitMatchingArguments ( String arguments ) 
	{
		
		Path standardPath = null;
		
		int direction = mundo.argumentsToDirection(arguments);
		
		if ( direction != Path.NO_DIRECTION )
			standardPath = getExit ( true , direction );
		
		//this logic is now moved to world: argumentsToDirection (which calls getNamesForDirection).
		
		/*
		if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase(mundo.getMessages().getMessage("direction.n"))
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("n") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.NORTE ));
	
			standardPath = getExit ( true , Path.NORTE );
	
		}
		else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase(mundo.getMessages().getMessage("direction.s"))
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("s") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.SUR ));
			standardPath = getExit ( true , Path.SUR );
		}
		else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase(mundo.getMessages().getMessage("direction.w"))
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("o") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.OESTE ));
			standardPath = getExit ( true , Path.OESTE );
		}
		else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase(mundo.getMessages().getMessage("direction.e"))
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("e") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.ESTE ));
			standardPath = getExit ( true , Path.ESTE );
		}	
	
		else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("sudeste")
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("se") 
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("sureste") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.ESTE ));
			standardPath = getExit ( true , Path.SUDESTE );
		}	
	
		else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("sudoeste")
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("so")
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("suroeste") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.ESTE ));
			standardPath = getExit ( true , Path.SUROESTE );
		}	
	
		else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("nordeste")
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("noreste")
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("ne") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.ESTE ));
			standardPath = getExit ( true , Path.NORDESTE );
		}	
	
		else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("noroeste")
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("no") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.ESTE ));
			standardPath = getExit ( true , Path.NOROESTE );
		}	
	
		else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("arriba")
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("ar") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.ARRIBA ));
			standardPath = getExit ( true , Path.ARRIBA );
		}	
		else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("abajo")
				|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("ab") )
		{
			//mentions.setLastMentionedVerb(command);
			//return go (getExit( true,Path.ABAJO ));
			standardPath = getExit ( true , Path.ABAJO );
		}	
		*/
		
		return standardPath;
		
	}
	
	/**
	 * Gets the best exit from this room that matches the given arguments, period.
	 * Encapsulates all the standard vs. nonstandard exit nonsense. Works with every kind of exit paths.
	 * @param arguments
	 * @return
	 */
	public Path getExitMatchingArguments ( String arguments )
	{
		Path standardPath = getStandardExitMatchingArguments ( arguments );
		if ( standardPath == null || !standardPath.isValid() )
			return getNonStandardExitMatchingArguments ( arguments );
		else
			return standardPath;
	}
		
	
	public String getExitName ( boolean isStandard , int exitn )
	{
		if ( isStandard )
		{
			switch ( exitn )
			{
				case Path.NORTE: return mundo.getMessages().getMessage("direction.full.n");
				case Path.SUR: return mundo.getMessages().getMessage("direction.full.s");
				case Path.ESTE: return mundo.getMessages().getMessage("direction.full.e");
				case Path.OESTE: return mundo.getMessages().getMessage("direction.full.w");
				case Path.NORDESTE: return mundo.getMessages().getMessage("direction.full.ne");
				case Path.NOROESTE: return mundo.getMessages().getMessage("direction.full.nw");
				case Path.SUDESTE: return mundo.getMessages().getMessage("direction.full.se");
				case Path.SUROESTE: return mundo.getMessages().getMessage("direction.full.sw");
				case Path.ARRIBA: return mundo.getMessages().getMessage("direction.full.u");
				case Path.ABAJO: return mundo.getMessages().getMessage("direction.full.d");
				//a�adir resto
				default: return "alg�n lado";
			}
		}
		else return otherExits[exitn].getNonStandardName();
	}
	
	//new as of 03.11.24
	public String getExitName ( Path p )
	{
		if ( p.isStandard() )
		{
			int exitn;
			for ( exitn = 0 ; exitn < standardExits.length ; exitn++ )
			{
				if ( standardExits[exitn] == p ) break;
			}
			return getExitName ( true , exitn );
		}
		else
			return p.getNonStandardName();
	
	}
	
	public List getExitNames ( Path p )
	{
		ArrayList result = new ArrayList();
		if ( p.isStandard() )
		{
			int exitn;
			for ( exitn = 0 ; exitn < standardExits.length ; exitn++ )
			{
				if ( standardExits[exitn] == p ) break;
			}
			
			result.add (getExitName ( true , exitn ));
		}
		String[] moreNames = p.getNonStandardNames();
		if ( moreNames != null )
		{
			for ( int i = 0 ; i < moreNames.length ; i++ )
				result.add(moreNames[i]);
		}
		return result;
	}
	
	public Room getPathDestination ( Path p , World w )
	{
		return w.getRoom(p.getDestinationID());
	}
	
	
	public int getID ( )
	{
		return idnumber;	
	}
	
	public int getItsID()
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
		//std. room description
		String desString="";
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			if ( descriptionList[i].matches(comparand) )
			{
				//desString += "\n";
				desString += descriptionList[i].getText();
			}
		}	
		//object description
		if ( itemsInRoom != null && !itemsInRoom.isEmpty() && !itemsInRoom.toString(mundo).equals(mundo.getMessages().getMessage("nothing")+".") ) //lo del "nada" es porque puede haber items; pero que sean invisibles.
		{
			/*luego aqu� poner en hab. par�metro "laying objs": "Sobre el duro suelo, sobre el blando suelo, sobre el puente..."*/
			desString+="\nAqu� hay ";
			desString+=itemsInRoom.toString();
		}
		if ( mobsInRoom != null && !mobsInRoom.isEmpty() && !mobsInRoom.toString(mundo).equals(mundo.getMessages().getMessage("nothing")+".") )
		{
			if ( mobsInRoom.size() > 1 )
				desString += "\nAqu� est�n ";
			else	
				desString += "\nAqu� est� ";
			desString+=mobsInRoom.toString(mundo);
		}
		
		return desString;	
	}


/*code flags exec example

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
				escribir(""+te);
				te.printStackTrace();
			}
			
			if ( ejecutado ) return;
*/


	//in order for bsh getDescription() to be able to use native getDescription()
	//without falling in infinite recursivity
	transient boolean getDescription_bsh_call = false;
	
	public String getDescription ( Entity viewer )
	{
		
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
			catch ( ScriptException te )
			{
				mundo.writeError(ExceptionPrinter.getExceptionReport(te));
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
		
		
		//std. room description
		String desString="";
		boolean quitado=false;
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			if ( descriptionList[i].matchesConditions(this,viewer) )
			{
				//desString += "\n";
				desString += descriptionList[i].getText();
			}
		}	
		//object description
		if ( itemsInRoom != null && !itemsInRoom.isEmpty() && !itemsInRoom.toString( viewer , mundo ).equals(mundo.getMessages().getMessage("nothing")+".") ) //lo del "nada" es porque puede haber items; pero que sean invisibles.
		{
			/*luego aqu� poner en hab. par�metro "laying objs": "Sobre el duro suelo, sobre el blando suelo, sobre el puente..."*/
			
			//old. remove.
			/*
			desString+="\nAqu� hay ";
			desString+=itemsInRoom.toString( viewer );
			*/
			desString+="\n"+getLayingObjectString(viewer);
		}
		if ( mobsInRoom != null && viewer instanceof Mobile ) quitado = mobsInRoom.removeElement((Mobile)viewer);
		if ( mobsInRoom != null && !mobsInRoom.isEmpty() && !mobsInRoom.toString( viewer , mundo ).equals(mundo.getMessages().getMessage("nothing")+".") )
		{
			/*
			if ( mobsInRoom.size() > 1 )
				desString += "\nAqu� est�n ";
			else	
				desString += "\nAqu� est� ";
			desString+=mobsInRoom.toString( viewer );
			*/
			if ( mobsInRoom.size() > 1 )
				desString += "\n" + getPresentMobilesPluralString ( viewer );
			else
				desString += "\n" + getPresentMobilesSingularString ( viewer );
		}
		if ( quitado ) mobsInRoom.addElement((Mobile)viewer);
		
		return desString;	
	}
	
	//returns string to describe things laying on floor of this room (like "Aqu� hay una espada y un escudo.")
	private String getLayingObjectString ( Entity viewer )
	{
		String customMessage = this.getPropertyValueAsString("itemsHereMessage");
		String itemsString = itemsInRoom.toString( viewer , mundo );
		String rawString = itemsString.substring(0,itemsString.length()-1);
		if ( customMessage != null )
			return mundo.getLanguage().correctMorphology(Messages.buildMessage(customMessage, 
					"$dotinventory", itemsString,
					"$inventory", rawString));
		else
			return mundo.getLanguage().correctMorphology(mundo.getMessages().getMessage("items.here",
					"$dotinventory",itemsString,
					"$inventory", rawString,
					new Object[]{this}));
	}
	
	//returns string to describe a mobile on a room (like "Aqu� est� Juan.")
	private String getPresentMobilesSingularString ( Entity viewer )
	{
		String customMessage = this.getPropertyValueAsString("mobileHereMessage");
		String mobsString = mobsInRoom.toString( viewer , mundo );
		String rawString = mobsString.substring(0,mobsString.length()-1);
		if ( customMessage != null )
			return Messages.buildMessage(customMessage, "$dotlist", mobsString , "$list" , rawString );
		else
			return mundo.getMessages().getMessage("mobile.here", "$dotlist", mobsString , "$list" , rawString , new Object[]{this});
	}
	
	//returns string to describe several mobiles on a room (like "Aqu� est�n Pedro y Juan.")
	private String getPresentMobilesPluralString ( Entity viewer )
	{
		String customMessage = this.getPropertyValueAsString("mobilesHereMessage");
		String mobsString = mobsInRoom.toString( viewer , mundo );
		String rawString = mobsString.substring(0,mobsString.length()-1);
		if ( customMessage != null )
			return Messages.buildMessage(customMessage, "$dotlist", mobsString , "$list" , rawString );
		else
			return mundo.getMessages().getMessage("mobiles.here", "$dotlist", mobsString, "$list", rawString, new Object[]{this});
	}
	 
	public String getDescription ( long comparand , Mobile toExclude )
	{
		//excluye al jugador, al yo.
		
				//std. room description
		String desString="";
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			if ( descriptionList[i].matches(comparand) )
			{
				//desString += "\n";
				desString += descriptionList[i].getText();
			}
		}	
		//object description
		if ( itemsInRoom != null && !itemsInRoom.isEmpty() && !itemsInRoom.toString(mundo).equals(mundo.getMessages().getMessage("nothing")+".") ) //lo del "nada" es porque puede haber items; pero que sean invisibles.
		{
			desString+="\nAqu� hay ";
			desString+=itemsInRoom.toString();
		}
		boolean quitado = false;
		if ( mobsInRoom != null ) quitado = mobsInRoom.removeElement(toExclude);
		if ( mobsInRoom != null && !mobsInRoom.isEmpty() && !mobsInRoom.toString(mundo).equals(mundo.getMessages().getMessage("nothing")+".") )
		{
			if ( mobsInRoom.size() > 1 )
				desString += "\nAqu� est�n ";
			else	
				desString += "\nAqu� est� ";
			desString+=mobsInRoom.toString();
		}
		if ( quitado ) mobsInRoom.addElement(toExclude);
		
		return desString;	
		
	}
	
	
	//updated to use matchesConditions instead of comparands and such.
	
	/*Legacy. Ahora el getDescription que solo coge viewer ya excluye al viewer.
	public String getDescription ( Entity viewer , Mobile toExclude )
	{
		//excluye al jugador, al yo.
		
		//std. room description
		
		
		
		
		String desString="";
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			if ( descriptionList[i].matchesConditions(this,viewer) )
			{
				//desString += "\n";
				desString += descriptionList[i].getText();
			}
		}	
		//object description
		if ( itemsInRoom != null && !itemsInRoom.isEmpty() && !itemsInRoom.toString(viewer).equals("nada.") ) //lo del "nada" es porque puede haber items; pero que sean invisibles.
		{
			desString+="\nAqu� hay ";
			desString+=itemsInRoom.toString(viewer);
		}
		boolean quitado = false;
		if ( mobsInRoom != null ) quitado = mobsInRoom.removeElement(toExclude);
		Debug.println("Mobs In Roome: " + mobsInRoom);
		Debug.println("Mobs In Roome Seez " + mobsInRoom.size() );
		if ( mobsInRoom != null && !mobsInRoom.isEmpty() && !mobsInRoom.toString(viewer).equals("nada.") )
		{
			if ( mobsInRoom.size() > 1 )
				desString += "\nAqu� est�n ";
			else	
				desString += "\nAqu� est� ";
			desString+=mobsInRoom.toString(viewer);
		}
		if ( quitado ) mobsInRoom.addElement(toExclude);
		
		return desString;	
		
	}
	*/
	
	
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
						//depende de estados (es una description list con comparandos y m�scaras)						
						//procesar comparandos y m�scaras como en getDescription
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
						else
							return null;	
					}
				}
			}
		}	
		return null;
	}
	*/
	
	/**
	 * Obtains an extra description, representing a description of a particular component, feature or aspect of this Room.
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
	
	//as of 03.09.03, extra descriptions are regular descriptions.
	//Parallel lists of Description[] and String[].
	/*
	public String getExtraDescription ( String thingieName , Entity viewer )
	{
		if ( thingieName == null || thingieName.length() == 0 ) return null;
		for ( int i = 0 ; i < extraDescriptionNameArrays.size() ; i++ )
		{
			String[] curNameArray = (String[]) extraDescriptionNameArrays.get(i);
			Description[] curDesArray = (Description[]) extraDescriptionArrays.get(i);
			
//			Modern Extra Description Support! On items as of 05-07-14! 
//			Pasted to rooms as of 05-09-27! Let's Rock It!
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
	*/
	
	//updated in order to use matchesConditions, instead of comparands and such.
	
	/*
	public String getExtraDescription ( String thingieName , Entity viewer )
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
						//depende de estados (es una description list con comparandos y m�scaras)						
						//procesar comparandos y m�scaras como en getDescription
						Description[] dList = Utility.loadDescriptionListFromString ( theDescription );
						String desString="";
						for ( int k = 0 ; k < dList.length ; k++ )
						{
							if ( dList[k].matchesConditions(this,viewer) ) //or general comparand?
							{
								desString += "\n";
								desString += dList[k].getText();
							}
						}	
						if ( desString.length() > 0 )
							return desString;
						else
							return null;	
					}
				}
			}
		}	
		return null;
	}
	*/
	
	//Hay que definir el changeState.
	public void changeState ( World mundo ) { }
	
	public void addItem ( Item nuevo ) throws WeightLimitExceededException,VolumeLimitExceededException
	{
		if ( itemsInRoom == null ) itemsInRoom = new Inventory(100000,100000,1);
		itemsInRoom.addItem(nuevo);	
		nuevo.addRoomReference(this);
	}
	
	public boolean removeItem ( Item viejo )
	{
		if ( itemsInRoom == null ) return false;
		else
		{
			viejo.removeRoomReference(this);
			return itemsInRoom.removeItem(viejo);
		}	
	}
	
	public void addMob ( Mobile nuevo )
	{
		if ( mobsInRoom == null ) mobsInRoom = new MobileList();
		mobsInRoom.addElement(nuevo);
	}
	
	public boolean removeMob ( Mobile viejo )
	{
		if ( mobsInRoom == null ) return false;
		return mobsInRoom.removeElement(viejo);
	}
	
	/*ejecuta el codigo de la habitacion correspondiente a la rutina dada si existe.
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


	//sustituye $1 por el nombre de source y $2 por el de target. De momento son mobiles, luego ya ser�n Entitys generales, o Referenciables, m�s bien.
	public String personalizeDescription ( String des , Mobile source , Mobile target )
	{
		String s = des;
		if ( source != null )
		{
			String nombre = source.constructName2(1,null); //personalize more (subjetivize) later!
			if ( nombre == null ) nombre = mundo.getMessages().getMessage("unnamed.mobile");
			s = StringMethods.textualSubstitution ( s , "$1" , nombre );
		}
		if ( target != null )
		{
			String nombre = target.constructName2(1,null); //personalize more (subjetivize) later!
			if ( nombre == null ) nombre = mundo.getMessages().getMessage("unnamed.mobile");
			s = StringMethods.textualSubstitution ( s , "$2" , nombre  );
		}
		return s;
	}
	
	
	//funci�n gen�rica que sustituye los $'s en las descripciones por los nombres de las cosas en funci�n de quien las ve (viewer).
	public String personalizeDescription ( String des , Entity viewer , Nameable[] dollarEntities )
	{
		String s = des;
		//sustituir $1 por el nombre de dollarEntities[0] seg�n viewer, $2 por el nombre de dollarEntities[1] seg�n viewer, etc. etc.
		for ( int i = 0 ; i < dollarEntities.length ; i++ )
		{
			if ( dollarEntities[i] != null )
			{
				String nombre = dollarEntities[i].constructName2(1,viewer);
				if ( nombre == null || dollarEntities[i].isInvisible(viewer) ) 
				{
					if ( dollarEntities[i] instanceof Mobile )
						nombre = mundo.getMessages().getMessage("unnamed.mobile");
					else
						nombre = mundo.getMessages().getMessage("unnamed.item");
				}
				s = StringMethods.textualSubstitution ( s , "$" + (i+1) , nombre );
			}
		}
		return lenguaje.correctMorphologyWithoutTrimming(s);
	}
	
	//the new, subjective inform action
	//warning: may throw ClassCastException if non-nameable entities are passed!
	/**
	 * @deprecated Use {@link #reportAction(Entity,Entity,Entity[],String,String,String,boolean)} instead
	 */
	public void informAction ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , String sufferDes , String execDes , boolean self_included )
	{
		reportAction(source, target, objects, thirdPersonDes, sufferDes,
				execDes, self_included);
	}

	/**
	 * calls version with style parameter.
	 * @param source
	 * @param target
	 * @param objects
	 * @param thirdPersonDes
	 * @param sufferDes
	 * @param execDes
	 * @param self_included
	 */
	public void reportAction ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , String sufferDes , String execDes , boolean self_included )
	{
		reportAction ( source , target , objects , thirdPersonDes , sufferDes , execDes , null , self_included );
	}
	
	//the new, subjective report action
	//warning: may throw ClassCastException if non-nameable entities are passed!
	public void reportAction ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , String sufferDes , String execDes , String style , boolean self_included )
	{
	
		//Debug.println("INFORMACTION BEGIN");
	
		Nameable[] dollarEntities;
		if ( objects == null ) dollarEntities = new Nameable[2];
		else dollarEntities = new Nameable[2+objects.length];
		
		//Debug.println("INFORMACTION INTERMEDIATE CIRCUS RELOADING STEP");
		
		dollarEntities[0]=(Nameable)source;
		
		//Debug.println("NO, No, No. MY FISH'S NAME IS ERIC. ERIC THE FISH.");
		
		dollarEntities[1]=(Nameable)target;
		
		//Debug.println("INFORMACTION NULLCHECK");
		
		if ( objects != null )
		{
			for ( int i = 0 ; i < objects.length ; i++ )
				dollarEntities[2+i]=(Nameable)objects[i];
		}
		
		//Debug.println("INFORMACTION CALLED" + thirdPersonDes);
		
		//informar a cada bicho de la habitaci�n.
		for ( int i = 0 ; i < mobsInRoom.size() ; i++ )
		{
			Mobile actual = mobsInRoom.elementAt(i);
			if ( ( actual == source ) && ( execDes != null ) && ( self_included ) )
			{
				if ( actual instanceof Informador )
					actual.writeWithTemplate ( style , personalizeDescription(execDes,actual,dollarEntities) );
			}
			else if ( actual == target && sufferDes != null )
			{
				if ( actual instanceof Informador )
					actual.writeWithTemplate ( style , personalizeDescription(sufferDes,actual,dollarEntities) );
			}
			else if ( thirdPersonDes != null && actual != source && actual != target )
			{
				if ( actual instanceof Informador )
					actual.writeWithTemplate ( style , personalizeDescription(thirdPersonDes,actual,dollarEntities) );
			}
		}
		//hacer que los bichos reaccionen.
		//nota: �por qu� lo hacemos en otro bucle y no en el mismo que lo anterior?
		//porque nos interesa que la informaci�n salga primero y despu�s todos los bichos reaccionen.
		//si lo meti�ramos todo a ca��n, podr�a haber bichos que reaccionaran antes de salir la informaci�n.
		for ( int i = 0 ; i < mobsInRoom.size() ; i++ )
		{
			Mobile actual = mobsInRoom.elementAt(i);
			if ( actual == source && execDes != null && self_included )
			{
				actual.reactToRoomText ( personalizeDescription(execDes,actual,dollarEntities) );
			}
			else if ( actual == target && sufferDes != null )
			{
				actual.reactToRoomText ( personalizeDescription(sufferDes,actual,dollarEntities) );
			}
			else if ( thirdPersonDes != null )
			{
				actual.reactToRoomText ( personalizeDescription(thirdPersonDes,actual,dollarEntities) );
			}
		}
		
		//room text reaction evt
		try
		{
			execCode ( "onRoomText" ,
			new Object[]
			{
				 personalizeDescription(thirdPersonDes,null,dollarEntities) 
			}
			);
		}
		catch ( ScriptException te )
		{
			System.err.println(te);
			mundo.writeError(ExceptionPrinter.getExceptionReport(te));
			te.printStackTrace();
		}
		//does nothing with the BshCodeExecutedOKException: irrelevant here.
				
		
	
	}
	
	//the new inform action auto: automatically obtain suffer and exec descriptions, then call
	//the new inform action.
	/**
	 * @deprecated Use {@link #reportActionAuto(Entity,Entity,Entity[],String,boolean)} instead
	 */
	public void informActionAuto ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , boolean self_included )
	{
		reportActionAuto(source, target, objects, thirdPersonDes, self_included);
	}

	/**
	 * The new reportActionAuto, without colour code parameter.
	 * @param source
	 * @param target
	 * @param objects
	 * @param thirdPersonDes
	 * @param self_included
	 */
	public void reportActionAuto ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , boolean self_included )
	{
		reportActionAuto ( source , target , objects , thirdPersonDes , null , self_included );
	}
	
	//the new report action auto: automatically obtain suffer and exec descriptions, then call
	//the new report action.
	public void reportActionAuto ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , String style , boolean self_included )
	{
	
		
		Debug.println("Autoinforming " + thirdPersonDes + " at roome " + getID() + ":" + getUniqueName());
		
		//intentar obtener autom�ticamente execdes
		StringTokenizer st = new StringTokenizer ( thirdPersonDes , " " , true );
		String execDes = "";
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			
			if ( tok.equals("$1") )
			{
				if ( st.hasMoreTokens() ) 
				{
					//el separador
					execDes += st.nextToken();
					if ( st.hasMoreTokens() )
					{
						String posibleVerbo = st.nextToken();
						String segundaPers = lenguaje.terceraASegunda ( posibleVerbo );
						if ( segundaPers != null )
						{
							execDes += segundaPers;
						}
						else
						{
							execDes += posibleVerbo;
						}
					}
				}
			}
			else
			{
				execDes += tok;
			}	
		}
		
		//trim left only
		execDes = execDes+"a";
		execDes = execDes.trim();
		execDes = execDes.substring(0,execDes.length()-1);
		
		//hacerlo con el sufferdes, de momento no hay.
		String sufferDes = null;
	
		Debug.println("Gonna deft'ly call informAction");
	
		//llamar a informAction
		reportAction ( source , target , objects , thirdPersonDes , sufferDes , execDes , style , self_included );
	
	}

	
	//old method's redirect
	/**
	 * @deprecated Use {@link #reportAction(Mobile,Mobile,String,String,String,boolean)} instead
	 */
	public void informAction ( Mobile source , Mobile target , String thirdPersonDes , String sufferDes , String execDes , boolean self_included )
	{
		reportAction(source, target, thirdPersonDes, sufferDes, execDes,
				self_included);
	}

	//old method's redirect
	/**
	 * @deprecated Use {@link #reportAction(Entity,Entity,Entity[],String,String,String,boolean)} instead
	 */
	public void reportAction ( Mobile source , Mobile target , String thirdPersonDes , String sufferDes , String execDes , boolean self_included )
	{
		reportAction ( source , target , null , thirdPersonDes , sufferDes , execDes, self_included );
	}
	
	//old method's redirect
	/**
	 * @deprecated Use {@link #reportActionAuto(Mobile,Mobile,String,boolean)} instead
	 */
	public void informActionAuto ( Mobile source , Mobile target , String thirdPersonDes  , boolean self_included )
	{
		reportActionAuto(source, target, thirdPersonDes, self_included);
	}

	//old method's redirect
	/**
	 * @deprecated Use {@link #reportActionAuto(Entity,Entity,Entity[],String,boolean)} instead
	 */
	public void reportActionAuto ( Mobile source , Mobile target , String thirdPersonDes  , boolean self_included )
	{
		reportActionAuto ( source , target , null , thirdPersonDes , self_included );
	}

	
	/*informa de algo que ha sucedido a todos los jugadores / Informadores.*/
	/*Novedad 03.03.26: puede haber descripciones null (no se escribe nada)*/
	/*de hecho me parece que self_included equivale a hacer final_execDes != null*/
	
	
	/*
	
	public void informAction ( Mobile source , Mobile target , String thirdPersonDes , String sufferDes , String execDes , boolean self_included )
	{
		//notese que gramaticalizar puede manejar null
		String final_thirdPersonDes = lenguaje.gramaticalizarSinTrimear ( personalizeDescription ( thirdPersonDes , source , target ) );
		String final_sufferDes = lenguaje.gramaticalizarSinTrimear ( personalizeDescription ( sufferDes , source , target ) );
		String final_execDes = lenguaje.gramaticalizarSinTrimear ( personalizeDescription ( execDes , source , target ) );
		if ( mobsInRoom == null ) return;
		for ( int i = 0 ; i < mobsInRoom.size() ; i++ )
		{
			if ( mobsInRoom.elementAt(i) instanceof Informador )
			{
				if ( source == mobsInRoom.elementAt(i) )
				{
					if ( self_included && final_execDes != null )
						((Informador)mobsInRoom.elementAt(i)).escribir ( final_execDes   );
				}
				else if ( target == mobsInRoom.elementAt(i) && final_sufferDes != null )
				{
					((Informador)mobsInRoom.elementAt(i)).escribir ( final_sufferDes  );
				}
				else if ( final_thirdPersonDes != null )
				{
					((Informador)mobsInRoom.elementAt(i)).escribir ( final_thirdPersonDes  );
				}
			}
		}
		for ( int i = 0 ; i < mobsInRoom.size() ; i++ )
		{
			if ( !(mobsInRoom.elementAt(i) instanceof Informador) )
			{
				if ( source == mobsInRoom.elementAt(i) )
				{
					if ( self_included )
						(mobsInRoom.elementAt(i)).reactToRoomText ( final_execDes );
				}
				else if ( target == mobsInRoom.elementAt(i) )
				{
					(mobsInRoom.elementAt(i)).reactToRoomText ( final_sufferDes );
				}
				else
				{
					(mobsInRoom.elementAt(i)).reactToRoomText ( final_thirdPersonDes );
				}
			}
		}
	}
	

	
	*/
	
	
	
	
	
	
	
	
	
	

	/*description not nullifiable. (de momento)*/

	/*

	public void informActionAuto ( Mobile source , Mobile target , String thirdPersonDes , boolean self_included )
	{
		//descripci�n 3� pers: trivial.
		String final_thirdPersonDes = lenguaje.gramaticalizarSinTrimear ( personalizeDescription ( thirdPersonDes , source , target ) );
		
		Debug.println("Two times: " + final_thirdPersonDes + final_thirdPersonDes );
		
		//obtener descripci�n 1� pers: cambiamos todas las apariciones de "$1 [verbo en 3�]" por [verbo en 2�]
		//ej. "$1 dice" por "dices".
		
		StringTokenizer st = new StringTokenizer ( thirdPersonDes , " " , true );
		String final_execDes = "";
		
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			
			if ( tok.equals("$1") )
			{
				if ( st.hasMoreTokens() ) 
				{
					//el separador
					final_execDes += st.nextToken();
					if ( st.hasMoreTokens() )
					{
						String posibleVerbo = st.nextToken();
						//Debug.println("Posible verbo: " + posibleVerbo );
						String segundaPers = lenguaje.terceraASegunda ( posibleVerbo );
						if ( segundaPers != null )
						{
							final_execDes += segundaPers;
						}
						else
						{
							final_execDes += posibleVerbo;
						}
					}
				}
			}
			else
			{
				final_execDes += tok;
			}
			
		}
		
		//hacemos el personalize para sustituir $2, que puede quedar por ahi.
		Debug.println("Personalized: [" + personalizeDescription ( final_execDes , source , target ) +  "]");
		Debug.println("Gramaticized: [" + lenguaje.gramaticalizar ( personalizeDescription ( final_execDes , source , target ) ) +  "]");
		final_execDes = lenguaje.gramaticalizarSinTrimear ( personalizeDescription ( final_execDes , source , target ) );
		
		//ya tenemos final_execDes. Queda final_sufferDes.
	
	
	
		//informamos
		if ( mobsInRoom == null ) return;
		for ( int i = 0 ; i < mobsInRoom.size() ; i++ )
		{
			if ( mobsInRoom.elementAt(i) instanceof Informador )
			{
				if ( source == mobsInRoom.elementAt(i) )
				{
					if ( self_included )
						((Informador)mobsInRoom.elementAt(i)).escribir (  final_execDes   );
				}
			 //	else if ( target == mobsInRoom.elementAt(i) )
			//	{
			//		((Informador)mobsInRoom.elementAt(i)).escribir ( final_sufferDes );
			//	} [de momento no tratamos descripci�n de acciones que sufre el jugador]
				else
				{
					((Informador)mobsInRoom.elementAt(i)).escribir (  final_thirdPersonDes  );
				}
			}
		}
		for ( int i = 0 ; i < mobsInRoom.size() ; i++ )
		{
			if ( ! ( mobsInRoom.elementAt(i) instanceof Informador ) )
			{
				if ( source == mobsInRoom.elementAt(i) )
				{
					if ( self_included )
						(mobsInRoom.elementAt(i)).reactToRoomText ( final_execDes );
				}
				//else if ( target == mobsInRoom.elementAt(i) )
				//{
				//	((Informador)mobsInRoom.elementAt(i)).escribir ( final_sufferDes );
				//} [de momento no tratamos descripci�n de acciones que sufre el jugador]
				else
				{
					(mobsInRoom.elementAt(i)).reactToRoomText ( final_thirdPersonDes );
				}
			}
		}
	
		
	} //end method
	
	*/

	public void loadNumberGenerator ( World mundo )
	{
		aleat = mundo.getRandom();
	}
	
	public boolean hasItem ( Item it )
	{
		return itemsInRoom.contains(it);
	}
	
	public boolean hasMobile ( Mobile m )
	{
		return mobsInRoom.contains(m);
	}
	
	
	public Inventory getInventory ( )
	{
		if ( itemsInRoom == null )
			itemsInRoom = new Inventory(10000,10000);
		return itemsInRoom;
	}
	
	public Inventory getContents()
	{
		return getInventory();
	}

	public MobileList getMobiles ( )
	{
		if ( mobsInRoom == null )
			mobsInRoom = new MobileList();
		return mobsInRoom;
	}

	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
	
		org.w3c.dom.Element suElemento = doc.createElement( "Room" );
		
		suElemento.setAttribute ( "id" , String.valueOf( idnumber ) );
		suElemento.setAttribute ( "name" , String.valueOf ( title ) );
		suElemento.setAttribute ( "extends" , String.valueOf ( inheritsFrom ) );
		
		suElemento.appendChild ( getPropListXMLRepresentation(doc) );
		
		suElemento.appendChild ( getRelationshipListXMLRepresentation(doc) );
		
		org.w3c.dom.Element listaDesc = doc.createElement("DescriptionList");
		//Debug.println("Room id " + idnumber);
		for ( int i = 0 ; i < descriptionList.length ; i++ )
		{
			Description nuestraDescripcion = descriptionList[i];
			listaDesc.appendChild ( nuestraDescripcion.getXMLRepresentation(doc) );
		}
		suElemento.appendChild(listaDesc);
		
		org.w3c.dom.Element listaSal = doc.createElement("PathList");
		if ( standardExits != null )
		{
			for ( int i = 0 ; i < standardExits.length ; i++ )
			{
				if ( standardExits[i] != null && standardExits[i].getDestinationID() != 0 /*zeroes are non-valid exits*/ )
					listaSal.appendChild ( standardExits[i].getXMLRepresentation(doc , Path.directionName(i)) );
			}
		}
		if ( otherExits != null )
		{
			for ( int i = 0 ; i < otherExits.length ; i++ )
			{
				if ( otherExits[i] != null )
				{
					if ( !otherExits[i].isExtended() ) //extended exits are put in standard list
						listaSal.appendChild ( otherExits[i].getXMLRepresentation(doc) );
				}
			}
		}
		suElemento.appendChild(listaSal);
		
		//inventario
		if ( itemsInRoom != null )
			suElemento.appendChild(itemsInRoom.getXMLRepresentation(doc));
		//mobile list
		if ( mobsInRoom != null )
		{
			//Debug.println("Mobile List Saving: " + mobsInRoom);
			suElemento.appendChild(mobsInRoom.getXMLRepresentation(doc));
			//Debug.println("Node is: " + mobsInRoom.getXMLRepresentation(doc));
		}
		
		//extra descriptions (temp XML representation) -> obsolete. Change.
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
		
		//only restrictions: not at the moment
		
		//object code
		if ( itsCode != null )
			suElemento.appendChild(itsCode.getXMLRepresentation(doc));

		return suElemento;
		
	}
	
	
	
	//devuelve un Inventory formado por el contenido, el contenido del contenido, etc. etc.
	public Inventory getFlattenedInventory()
	{
		
		if ( itemsInRoom == null ) return new Inventory(1,1);
		
		Inventory result = new Inventory(itemsInRoom.getWeightLimit(),itemsInRoom.getVolumeLimit());
		
		for ( int i = 0 ; i < itemsInRoom.size() ; i++ )
		{
			Item thisPart = itemsInRoom.elementAt(i);
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
	
	
	//for clone()'s and similars!
	
	//notaci�n un poco inconsistente con la de Entity::copyEntityFields; pero bueno
	public void copyRoomFieldsTo ( Room r )
	{
	
		r.copyEntityFields(this); //estados, propiedades, etc.
	
		//Debug.println("Random from SOURCE item: " + getRandom());
	
		r.mundo = mundo;
		
		r.aleat = getRandom();
		r.extraDescriptions = extraDescriptions;
		r.idnumber = idnumber; //will have to change the ID later!
	
		//it.inheritsFrom = 0; //creates an identical (strong inherit) item -> No weak inherit
		//nay, identical copy
		
		r.inheritsFrom = inheritsFrom;
		
		if ( itemsInRoom != null )
			r.itemsInRoom = (Inventory) itemsInRoom.clone();
		else r.itemsInRoom = null;
		
		//it.isInstanceOf = idnumber; //creates an identical (strong inherit) item
		//nay, identical copy
		
		//r.isInstanceOf = isInstanceOf;

		if ( itsCode != null )
			r.itsCode = itsCode.cloneIfNecessary();

		
		r.title = title;
		
		r.descriptionList = new Description[descriptionList.length];
		for ( int i = 0 ; i < r.descriptionList.length ; i++ )
		{
			r.descriptionList[i] = (Description) descriptionList[i].clone();
		}
		
		//mob refs and only restrictions are obsolete, do not copy
	
	
		r.lenguaje = lenguaje;
		
		
		if ( mobsInRoom != null )
			r.mobsInRoom = (MobileList) mobsInRoom.clone();
		else r.mobsInRoom = null;
		
		r.standardExits = new Path[ this.standardExits.length ];
		for ( int i = 0 ; i < this.standardExits.length ; i++ )
		{
			if ( this.standardExits[i] != null )
				r.standardExits[i] = (Path) this.standardExits[i].clone();
			else
				r.standardExits[i] = null;
		}
		
		r.otherExits = new Path[ this.otherExits.length ];
		for ( int i = 0 ; i < this.otherExits.length ; i++ )
		{
			if ( this.otherExits[i] != null )
				r.otherExits[i] = (Path) this.otherExits[i].clone();
			else
				r.otherExits[i] = null;
		}	
		
		//shallow!
		
		r.extraDescriptionArrays = extraDescriptionArrays;
		r.extraDescriptionNameArrays = extraDescriptionNameArrays;
		
	
	}
	
	
	
	
	public java.util.Random getRandom()
	{
		return aleat;
	}
	
	
	public Room createNewInstance ( World mundo , boolean cloneContents )
	{
		return createNewInstance ( mundo , cloneContents , null );
	}
	
	
	public Room createNewInstance( World mundo , boolean cloneContents , String uniqueName ) 
	{
		Room r = (Room) this.clone();
		r.inheritsFrom = idnumber;
		
		if ( uniqueName == null ) r.title = mundo.generateUnusedUniqueName(this.getUniqueName()); 
		else r.title = uniqueName;
		
		mundo.addRoomAssigningID(r);
		
		return r;
	}
	
	public Object clone( )
	{
		//do it!
		
		Room r = new Room();
		
		copyRoomFieldsTo(r);
		
		return r;
	}
	
	//for clones
	public Room ()
	{
		;
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
	 * Returns true if this room (directly) contains the specified item.
	 * @param it
	 * @return
	 */
	public boolean contains ( Item it )
	{
		return getContents().contains(it);
	}
	
	/**
	 * Returns true if this room (directly) contains the specified mobile.
	 * @param it
	 * @return
	 */
	public boolean contains ( Mobile m )
	{
		return getMobiles().contains(m);
	}
	
}
