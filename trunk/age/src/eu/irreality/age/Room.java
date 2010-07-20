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
public class Room extends Entity implements Descriptible , SupportingCode
{

	//al final claudicamos
	private World mundo;
	
	//////////////////////
	//INSTANCE VARIABLES//
	//////////////////////
	
	/**ID de la habitación que se usa para referirse a ella.*/
	/*01*/ private int idnumber;
	
	/**ID de la habitación de que se hereda.*/
	/*02*/ private int inheritsFrom; 
	
	/*03*/ // inherited protected int state;
	// inherited protected long timeunitsleft;
	
	/**Nombre sintético de la habitación.*/
	/*04*/ protected String title;
	
	/**Lista dinámica de descripciones.*/
	/*10*/ protected Description[] descriptionList;
	/**Lista dinámica de salidas estándar.*/
	/*11*/ protected Path[] standardExits;
	/**Lista dinámica de otras salidas.*/
	/*12*/ protected Path[] otherExits;
	
	/**Lista dinámica de objetos.*/
	//no sé, tal vez mejor vector, por aquello de dinámica.
	/*20*/ protected Inventory itemsInRoom; //item vector
	/**Lista dinámica de bichos.*/
	/*21*/ protected MobileList mobsInRoom;
	/**Lista dinámica de personajes.*/
	
	/**Descripciones de cosas de la habitación.*/
	/*30*/ protected String extraDescriptions; //OLD
	protected List extraDescriptionArrays; /*List of Description Arrays*/
	protected List extraDescriptionNameArrays; /*List of String Arrays*/
	
	
	/**Restricciones de acceso y otras.*/
	/*31*/ protected Vector onlyRestrictions;
	
	/**Código en Ensamblador Virtual Aetheria (EVA)*/
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
	* El pedazo de constructor que lee una habitación de un fichero.
	* <nota: random number seed se setea después>
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
						System.out.println("[SINTAXIS] línea 10 (" + roomfile + ") insuficientes tokens con @");
					for ( int i = 0 ; i < 10 ; i++ )
					{
						String curToken = StringMethods.getTok( linea , i+1 , '@' );
						standardExits[i] = new Path ( mundo , true , curToken ); //el true indica que es estándar.
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
		if ( e.hasAttribute("extends") && !e.getAttribute("extends").equals("0") && allowInheritance )
		{
			//item must extend from existing item.
			//clonamos ese item y overrideamos lo overrideable
			//(nótese que la ID del item extendido ha de ser menor).
			
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
				standardExits[i] = new Path ( mundo , true , "0" ); //el true indica que es estándar.
				//el 0 hará que produzca una salida inválida
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
			//no se especificó id, la asignará el mundo.
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
		catch ( bsh.TargetError te )
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
		
		if ( nvalid_standard + nvalid_nonstandard < 1 ) return -1; //ninguna salida válida
		
		int numsalida = aleat.nextInt( nvalid_standard + nvalid_nonstandard );
		int i,j;
		i = 0;
		System.err.println("Room " + this + " numsal " + numsalida);
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
	
	public String getExitName ( boolean isStandard , int exitn )
	{
		if ( isStandard )
		{
			switch ( exitn )
			{
				case Path.NORTE: return "el norte";
				case Path.SUR: return "el sur";
				case Path.ESTE: return "el este";
				case Path.OESTE: return "el oeste";
				//añadir resto
				default: return "algún lado";
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
		
	public String getTitle ( )
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
		if ( itemsInRoom != null && !itemsInRoom.isEmpty() && !itemsInRoom.toString().equals("nada.") ) //lo del "nada" es porque puede haber items; pero que sean invisibles.
		{
			/*luego aquí poner en hab. parámetro "laying objs": "Sobre el duro suelo, sobre el blando suelo, sobre el puente..."*/
			desString+="\nAquí hay ";
			desString+=itemsInRoom.toString();
		}
		if ( mobsInRoom != null && !mobsInRoom.isEmpty() && !mobsInRoom.toString().equals("nada.") )
		{
			if ( mobsInRoom.size() > 1 )
				desString += "\nAquí están ";
			else	
				desString += "\nAquí está ";
			desString+=mobsInRoom.toString();
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
			catch ( bsh.TargetError te )
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
		if ( itemsInRoom != null && !itemsInRoom.isEmpty() && !itemsInRoom.toString( viewer ).equals("nada.") ) //lo del "nada" es porque puede haber items; pero que sean invisibles.
		{
			/*luego aquí poner en hab. parámetro "laying objs": "Sobre el duro suelo, sobre el blando suelo, sobre el puente..."*/
			desString+="\nAquí hay ";
			desString+=itemsInRoom.toString( viewer );
		}
		if ( mobsInRoom != null && viewer instanceof Mobile ) quitado = mobsInRoom.removeElement((Mobile)viewer);
		if ( mobsInRoom != null && !mobsInRoom.isEmpty() && !mobsInRoom.toString( viewer ).equals("nada.") )
		{
			if ( mobsInRoom.size() > 1 )
				desString += "\nAquí están ";
			else	
				desString += "\nAquí está ";
			desString+=mobsInRoom.toString( viewer );
		}
		if ( quitado ) mobsInRoom.addElement((Mobile)viewer);
		
		return desString;	
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
		if ( itemsInRoom != null && !itemsInRoom.isEmpty() && !itemsInRoom.toString().equals("nada.") ) //lo del "nada" es porque puede haber items; pero que sean invisibles.
		{
			desString+="\nAquí hay ";
			desString+=itemsInRoom.toString();
		}
		boolean quitado = false;
		if ( mobsInRoom != null ) quitado = mobsInRoom.removeElement(toExclude);
		if ( mobsInRoom != null && !mobsInRoom.isEmpty() && !mobsInRoom.toString().equals("nada.") )
		{
			if ( mobsInRoom.size() > 1 )
				desString += "\nAquí están ";
			else	
				desString += "\nAquí está ";
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
			desString+="\nAquí hay ";
			desString+=itemsInRoom.toString(viewer);
		}
		boolean quitado = false;
		if ( mobsInRoom != null ) quitado = mobsInRoom.removeElement(toExclude);
		Debug.println("Mobs In Roome: " + mobsInRoom);
		Debug.println("Mobs In Roome Seez " + mobsInRoom.size() );
		if ( mobsInRoom != null && !mobsInRoom.isEmpty() && !mobsInRoom.toString(viewer).equals("nada.") )
		{
			if ( mobsInRoom.size() > 1 )
				desString += "\nAquí están ";
			else	
				desString += "\nAquí está ";
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
						else
							return null;	
					}
				}
			}
		}	
		return null;
	}
	*/
	
	
	//as of 03.09.03, extra descriptions are regular descriptions.
	//Parallel lists of Description[] and String[].
	
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
						//depende de estados (es una description list con comparandos y máscaras)						
						//procesar comparandos y máscaras como en getDescription
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
		Debug.println("Mobs In Roome After Addendum " + mobsInRoom);
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


	//sustituye $1 por el nombre de source y $2 por el de target. De momento son mobiles, luego ya serán Entitys generales, o Referenciables, más bien.
	public String personalizeDescription ( String des , Mobile source , Mobile target )
	{
		String s = des;
		if ( source != null )
		{
			String nombre = source.constructName2(1,null); //personalize more (subjetivize) later!
			if ( nombre == null ) nombre = "alguien";
			s = StringMethods.textualSubstitution ( s , "$1" , nombre );
		}
		if ( target != null )
		{
			String nombre = target.constructName2(1,null); //personalize more (subjetivize) later!
			if ( nombre == null ) nombre = "alguien"; 
			s = StringMethods.textualSubstitution ( s , "$2" , nombre  );
		}
		return s;
	}
	
	
	//función genérica que sustituye los $'s en las descripciones por los nombres de las cosas en función de quien las ve (viewer).
	public String personalizeDescription ( String des , Entity viewer , Nameable[] dollarEntities )
	{
		String s = des;
		//sustituir $1 por el nombre de dollarEntities[0] según viewer, $2 por el nombre de dollarEntities[1] según viewer, etc. etc.
		for ( int i = 0 ; i < dollarEntities.length ; i++ )
		{
			if ( dollarEntities[i] != null )
			{
				String nombre = dollarEntities[i].constructName2(1,viewer);
				if ( nombre == null || dollarEntities[i].isInvisible(viewer) ) 
				{
					if ( dollarEntities[i] instanceof Mobile )
						nombre = "alguien";
					else
						nombre = "algo";
				}
				s = StringMethods.textualSubstitution ( s , "$" + (i+1) , nombre );
			}
		}
		return lenguaje.gramaticalizarSinTrimear(s);
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

	//the new, subjective inform action
	//warning: may throw ClassCastException if non-nameable entities are passed!
	public void reportAction ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , String sufferDes , String execDes , boolean self_included )
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
		
		//informar a cada bicho de la habitación.
		for ( int i = 0 ; i < mobsInRoom.size() ; i++ )
		{
			Mobile actual = mobsInRoom.elementAt(i);
			if ( ( actual == source ) && ( execDes != null ) && ( self_included ) )
			{
				if ( actual instanceof Informador )
					actual.write ( personalizeDescription(execDes,actual,dollarEntities) );
			}
			else if ( actual == target && sufferDes != null )
			{
				if ( actual instanceof Informador )
					actual.write ( personalizeDescription(sufferDes,actual,dollarEntities) );
			}
			else if ( thirdPersonDes != null && actual != source && actual != target )
			{
				if ( actual instanceof Informador )
					actual.write ( personalizeDescription(thirdPersonDes,actual,dollarEntities) );
			}
		}
		//hacer que los bichos reaccionen.
		//nota: ¿por qué lo hacemos en otro bucle y no en el mismo que lo anterior?
		//porque nos interesa que la información salga primero y después todos los bichos reaccionen.
		//si lo metiéramos todo a cañón, podría haber bichos que reaccionaran antes de salir la información.
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
		catch ( bsh.TargetError te )
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

	//the new inform action auto: automatically obtain suffer and exec descriptions, then call
	//the new inform action.
	public void reportActionAuto ( Entity source /*$1*/ , Entity target /*$2*/ , Entity[] objects /*$3..$n*/ , String thirdPersonDes , boolean self_included )
	{
	
		
		Debug.println("Autoinforming " + thirdPersonDes + " at roome " + getID() + ":" + getTitle());
		
		//intentar obtener automáticamente execdes
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
		reportAction ( source , target , objects , thirdPersonDes , sufferDes , execDes , self_included );
	
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
		//descripción 3ª pers: trivial.
		String final_thirdPersonDes = lenguaje.gramaticalizarSinTrimear ( personalizeDescription ( thirdPersonDes , source , target ) );
		
		Debug.println("Two times: " + final_thirdPersonDes + final_thirdPersonDes );
		
		//obtener descripción 1ª pers: cambiamos todas las apariciones de "$1 [verbo en 3ª]" por [verbo en 2ª]
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
			//	} [de momento no tratamos descripción de acciones que sufre el jugador]
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
				//} [de momento no tratamos descripción de acciones que sufre el jugador]
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
				if ( standardExits[i] != null )
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
	
	//notación un poco inconsistente con la de Entity::copyEntityFields; pero bueno
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
	
	
	
	
	public Room createNewInstance( World mundo , boolean cloneContents  ) 
	{
		Room r = (Room) this.clone();
		r.inheritsFrom = idnumber;
		
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
	
	
}
