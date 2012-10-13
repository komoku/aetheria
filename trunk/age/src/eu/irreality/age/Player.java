/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
//package AetheriaAWT;
import java.util.*;
import java.io.*; //logs

import javax.swing.SwingUtilities;

import bsh.TargetError;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.matching.Matches;
import eu.irreality.age.util.VersionComparator;
/**
 * Clase del personaje, jugador.
 *
 */
public class Player extends Mobile implements Informador
{

	//INSTANCE VARIABLES

	/**�Estamos cargando de un log?*/
	protected boolean from_log;
	protected Vector logfile; //log de ENTRADA

	BufferedReader logReader;	


	//s�lo a efectos de estad�sticas, ya sale todo sustituido.
	Vector finalExecutedCommandLog = new Vector();

	//variables para el combate
	Weapon lastAttackWeapon = null;
	Mobile lastAttackedEnemy = null;
	Weapon lastBlockWeapon = null;
	Mobile lastBlockedEnemy = null;
	
	public void setPlayerName( String nombre )
	{
		//Funci�n de conveniencia para poner nombre a un jugador, que sirve como
		//title, reference name, etc.

		title = nombre;
		properName = true;

		//max priority as reference
		respondToSing = nombre+"$"+respondToSing;
		respondToPlur = nombre+"$"+respondToPlur;

		singNames = new Description[1];
		singNames[0] = new Description(nombre,0,0);

		plurNames = new Description[1];
		plurNames[0] = new Description(nombre,0,0);
	}


	void initDefaultProperties( World mundo )
	{
		//set multiple args matches which was default behaviour in versions [1.0,1.1.7]
		if ( new VersionComparator().compare(mundo.getParserVersion(),"1.0") >= 0 && new VersionComparator().compare(mundo.getParserVersion(),"1.1.7") <= 0 )
		{
			if ( this.getPropertyValueAsObject("multipleArgsMatches") == null )
				setProperty("multipleArgsMatches",true);
		}
	}

	/**Lista din�mica de objetos.*/
	//protected Inventory inventory; -> heredado de Mobile

	public Player ( World mundo , InputOutputClient io , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		super ( mundo , n );
		//this.mundo = mundo;
		this.io = io;
		//lenguaje = mundo.getLang();
		//setRoom ( mundo.getRoom(1) );
		//setRoom ( mundo.getRoom(1) );
		setNewState(1,1);
		//mundo.setPlayer(this);
		initDefaultProperties(mundo);
	}

	//TEMPORAL CONSTRUCTOR

	public Player ( World mundo , InputOutputClient io ) throws java.io.IOException
	{
		super ( mundo, Utility.playerFile(mundo) );
		//this.mundo = mundo;
		this.io = io;

		//Debug.println("Player's I/O: " + io);

		//lenguaje = mundo.getLang(); //toma las herramientas de lenguaje del mundo
		//name = "Desarrollador";

		//habitacionActual = mundo.getRoom(1);
		//habitacionAnterior = mundo.getRoom(1);
		//substituted by:
		setRoom(mundo.getRoom(1));
		setRoom(mundo.getRoom(1)); //quedan seteadas actual y anterior

		//state = 1; //001 IDLE 	
		//timeUnitsLeft = 1; //estado inicial.
		setNewState(1,1);
		//mundo.setPlayer(this);
		initDefaultProperties(mundo);
	}

	//METHODS

	/*Para salvados: en vez de coger comandos de teclado, se ejecuta un fichero de log
	hasta que termine. Esto abre el fichero, lo dem�s ya lo har� el thread del engine
	(es autom�tico) -> antes de hacer el start al thread, poner prepareLog si es un
	salvado.*/
	//TRIED TO RENDER IT UNUSEFUL AS OF 04.03.29
	/*
	public void prepareLog(String s) throws java.io.FileNotFoundException
	{
		FileInputStream logInput = new FileInputStream(s);
		logReader = new BufferedReader ( Utility.getBestInputStreamReader ( logInput ) );
		try
		{
			logReader.readLine(); //la primera linea no contiene un comando
			logReader.readLine(); //la segunda tampoco
		}
		catch ( java.io.IOException exc )
		{
			escribir(io.getColorCode("error") + "Excepci�n I/O al leer el log" + io.getColorCode("reset"));
		}
		from_log = true;
	}
	 */

	public void prepareLog ( BufferedReader br )
	{
		Debug.println("Preparing log input on " + this);
		this.logReader = br;
		from_log = true;
	}

	public void endOfLog()
	{
		from_log = false;
		if ( this.getClient() instanceof MultimediaInputOutputClient )
		{
			MultimediaInputOutputClient mioc = (MultimediaInputOutputClient) this.getClient();
			SoundClient sc = mioc.getSoundClient();
			if ( sc instanceof AGESoundClient )
			{
				AGESoundClient asc = (AGESoundClient) sc;
				asc.activate();
			}
		}
	}

	//fuerza un comando
	public void forceCommand(String s)
	{
		forced = true;
		force_string = s;
	}
	
	public void enqueueCommand(String s)
	{
		commandQueue.add(s);
	}

	public void setCommandString(String s)
	{
		commandstring=s;
	}

	synchronized public void resumeExecution ( )
	{
		notify();	
	}


	/**
	A pesar del nombre ambiguo (no se me ocurr�a ninguno mejor, la verdad) esta funci�n
	s�lo sirve para cuando no se ejecuta un comando porque no se entiende lo que viene
	despu�s (por ejemplo, "coger adfasfg" o "coger <algo que no hay en la habitaci�n o
	el programa no reconoce>"). Cuando no se ejecuta por motivos ya del juego (no coges
	porque llevas demasiado peso, no miras porque est�s ciego, etc.) no se debe usar esto,
	ya que en caso de second-chance da un mensaje que es claramente de error.
	 */
	private void escribirDenegacionComando ( String s )
	{
		if ( secondChance ) //estamos en la segunda oportunidad, i.e. con verbo inventado...
		{
			escribirErrorNoEntiendo ( );
		}
		else write(s);
	}



	//from Entity.
	//excludes property "state". <- ? Not sure about if this comment is actually true.
	public boolean update ( PropertyEntry pe , World mundo )
	{

		//Debug.println("[PROPERTY UPDATE: " + pe.getName() + "]" + new Vector(propertiesList).toString());


		String theProp = pe.getName();

		if ( theProp.equals("custom_parsing") )
		{
			boolean value = pe.getValueAsBoolean();
			if ( value )
			{
				try
				{
					return customParse();
				}
				catch ( IOException ioe )
				{
					write( io.getColorCode("error") + "Excepci�n E/S en update() para propiedad custom_parsing" + io.getColorCode("reset") );	
					return false;
				}
			}
			else return true;
		}
		else
		{
			//player's state must always be updated, can't just go and fall into negative numbers and be ignored
			if ( theProp.equals("state") && pe.getTimeLeft() < 0 )
				pe.setTime(0);

			return super.update(pe,mundo);
		}
	}

	public void setParseRoutine ( Entity holdingTheRoutine , String routineName )
	{
		setProperty("custom_parsing",true,0);
		pushPropertyToFront("custom_parsing"); //para que se actualice antes que state
		setRelationshipProperty(holdingTheRoutine,"custom_parser",routineName);
	}

	//preCD:
	//property "custom_parsing" of this player is true and just expired,
	//player has a relationship "custom_parser" with a single, coded Entity. Relationship value is the name of the (bsh) parse routine.
	synchronized public boolean customParse ( ) throws java.io.IOException
	{

		String theCommand;


		if ( from_log )
		{
			String newCommand = logReader.readLine();
			if ( newCommand == null )
			{
				//se acab� el log
				from_log = false;
				mundo.endOfLog();
				return customParse();
			}		
			else
			{
				//que aparezca el comando como si lo hubieramos introducido
				io.forceInput ( newCommand , true );
				theCommand = newCommand;
			}
		}
		else
		{

			//hacemos lo mismo que en execCommand() [el parser por defecto]

			GameEngineThread gte = (GameEngineThread)Thread.currentThread();
			if ( gte.isRealTimeEnabled() )
			{
				theCommand = io.getRealTimeInput(this);
				if ( theCommand == null )
				{
					//seguir esperando
					setPropertyTimeLeft ( "custom_parsing" , 1 );
					return false;
				}
			}
			else
			{
				theCommand = io.getInput(this);
				if ( theCommand == null )
				{
					if ( io.isDisconnected() ) //remote player disconnected
					{
						disconnect();
						return true;
					}
				}
			}	
		}

		//Ya tenemos lo que el t�o escribi�, en theCommand.

		List ofEntities = getRelatedEntities ( "custom_parser" ); 
		//s�lo consideramos la primera no nula, porque s�lo puede haber una.
		if ( ofEntities.size() < 1 )
		{
			write(io.getColorCode("error") + "Error: llamada a customParse() sin entidades con parsers activos para �sta." + io.getColorCode("reset"));
			return false;
		}
		else
		{
			Entity ourEntity=null;
			String routineName=null;
			for ( int i = 0 ; i < ofEntities.size() ; i++ )
			{
				ourEntity = (Entity) ofEntities.get(i);
				routineName = getRelationshipPropertyValueAsString ( ourEntity , "custom_parser" );
				if ( routineName != null ) break;
			}

			if ( ourEntity instanceof SupportingCode )
			{
				ReturnValue retVal = new ReturnValue(null);

				try
				{
					((SupportingCode)ourEntity).execCode ( routineName , new Object[] { this , theCommand } , retVal );
					//removed: may have to parse moar
					//setProperty("custom_parsing",false,0);
					//setRelationshipProperty(ourEntity,"custom_parser",null);
				}
				catch ( bsh.TargetError bshte )
				{
					write( io.getColorCode("error") + "bsh.TargetError found at customParse(), execcing from ID " + getID() + " the routine " + routineName + " of " + ourEntity.getID()  );
					bshte.printStackTrace();
					writeError(ExceptionPrinter.getExceptionReport(bshte));
				}
				if ( retVal.getRetVal() == null || ! ( retVal.getRetVal() instanceof Boolean ) )
				{
					setProperty("custom_parsing",false,0);
					setRelationshipProperty(ourEntity,"custom_parser",null);
					return false;
				}
				else
				{
					Boolean ret = (Boolean) retVal.getRetVal();
					if ( ret.booleanValue() == false )
						return customParse(); //parse until it's true! DO IT!
					else
					{
						//don't paser more
						setProperty("custom_parsing",false,0);
						setRelationshipProperty(ourEntity,"custom_parser",null);
						return ret.booleanValue();
					}
				}


			}
			else
			{
				write(io.getColorCode("error") + "Error: llamada a customParse() para clase que no lo soporta: " + ourEntity.getClass() );
				return false;
			}

		}
		//getObject(getTarget())


	}


	private EntityList mobilesCache;

	/**
	 * Used for commands of the kind "Mar�a, hola" (to recognise mobile names as verbs)
	 * @return
	 */
	private EntityList getAllWorldMobiles()
	{
		if ( mobilesCache == null )
			mobilesCache = mundo.getAllMobiles();
		return mobilesCache;
	}

	//cancel commands in the queue pending to be executed
	public void cancelPending()
	{
		commandQueue.removeAllElements();
	}

	/**Ejecutar un comando del jugador.*/
	//Este es el parser de comandos!
	synchronized public boolean execCommand ( World mundo ) throws java.io.IOException
	{

		/*Pueden darse dos casos:

		- Que hayan quedado comandos pendientes de ejecuci�n de otra vez: estar�n
		en la cola de comandos pendientes.
		- Que no: esperamos una entrada por la editbox, que cambiar� la variable
		"commandstring" poniendo el nuevo comando y despertar� el thread del wait.
		 */		

		secondChance = false; //luego se pone en el primer if a true si hace falta
		String originalTrimmedCommandString = null; //sin sustituir pronombres.

		//mirar si cola de comandos vacia
		//el !forced es porque si hemos forzado un comando, pasa por delante de la cola
		if ( !commandQueue.isEmpty() && !forced )
		{

			if ( nextCommandSecondChance )
			{
				secondChance = true; //estamos en un comando de segunda oportunidad
				nextCommandSecondChance = false;
			}


			//Debug.println("Cola [quitando]:");
			for ( int i = 0 ; i < commandQueue.size() ; i++ )
				//Debug.println(commandQueue.elementAt(i));
				;

			//quitar el primer elemento (cabeza) de commandQueue
			commandstring = ((String)commandQueue.elementAt(0)).trim();

			originalTrimmedCommandString = commandstring;

			//Debug.println("(1) Command string set to " + (String)commandQueue.elementAt(0) );
			commandQueue.removeElementAt(0);
			
			
			//substitutePronouns();
			//pasado a m�s tarde

			//Debug.println("Tras sustituir: " + commandstring);

			//en la cola metemos sentencias simples; pero al sustituir los pronombres pueden 
			//dar lugar de nuevo a multiplicidad.
			if ( !separateSentences() ) return false;

		}
		else
		{
			if ( forced )
			{
				forced = false;
				io.forceInput ( force_string , false );
				commandstring = force_string;
			}
			else if ( from_log )
			{
				String newCommand = logReader.readLine();
				if ( newCommand == null )
				{
					from_log = false;
					mundo.endOfLog();
					return execCommand(mundo);
				}		
				else
				{
					io.forceInput ( newCommand , true );
					commandstring = newCommand;
				}
			}
			else
			{
				//Esperamos por el comando.
				//Aqui se cambia la variable commandstring por efecto de la entrada
				//try
				//{
				//io.setWaitingPlayer(this);
				//wait();
				//}
				//catch ( InterruptedException intex ) 
				//{ 
				//	;
				//}

				GameEngineThread gte = (GameEngineThread)Thread.currentThread();
				if ( gte.isRealTimeEnabled() )
				{
					commandstring = io.getRealTimeInput(this);
					if ( commandstring == null ) 
					{
						commandstring = "";
						if ( io.isDisconnected() )
						{
							disconnect();
							return true;
						}
					}
				}
				else
				{
					commandstring = io.getInput(this);
					if ( commandstring == null && io.isDisconnected() )
					{
						disconnect();
						return true;
					}
				}

			}


			/*Preparaci�n del comando:*/

			if ( commandstring != null ) commandstring = commandstring.trim();



			
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
			catch ( bsh.TargetError te )
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
			/*Fin de preprocessCommand()*/


			//comando nulo
			if ( commandstring == null || commandstring.equals("") ) return false;

			
			/*Comandos eval*/
			if ( commandstring.startsWith( "eval " ) && Debug.isEvalEnabled() )
			{
				ReturnValue retVal = new ReturnValue(null);
				arguments = StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
				try {
					mundo.getAssociatedCode().evaluate(arguments,this,retVal);
				} catch (TargetError e) {
					writeError(ExceptionPrinter.getExceptionReport(e));
				}
				this.write(""+retVal.getRetVal()+"\n");
				return false;
			}
			
			
			
			originalTrimmedCommandString = commandstring;

			/*Aqu� sustituimos los pronombres por las zonas de referencia.*/
			//substitutePronouns();
			//pasado a m�s tarde

			/*Separamos las subfrases*/	
			if ( !separateSentences() ) return false;

		}

		//modular execCommand()

		if ( commandstring.isEmpty() ) return false; //empty strings can result if, for example, input was ",something", etc.
		
		return execCommand ( commandstring  );

	}

	
	/**
	 * Executes the parseCommand() methods that apply for the current command/arguments in a given scope.
	 * @param scope
	 * @return
	 */
	private boolean runParseCommandMethods ( EntityList scope )
	{
		//***
		//*** BEGIN EXECUTION OF parseCommand() METHODS
		//***

		boolean ejecutado = false;
		
		setProperty ( "originState" , getState() ); //state that lead into the command execution
		long originalTimeLeft = getPropertyTimeLeft("state");
		setNewState ( IDLE , 1 ); //by default, this will be the state at end of command execution.
			//Of course, this can be overridden by parseCommand methods or by the AGE core implementing the actual command. 
			//But e.g. after a parseCommand that doesn't do any setNewState(), this will be the resulting state.
		
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
		catch ( bsh.TargetError te )
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
		catch ( bsh.TargetError te )
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
		catch ( bsh.TargetError te )
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

		//this restores the state and its timer to their values before execution of parseCommand methods, just in case the AGE kernel code
		//needs to access the state. Which I think it actually doesn't, but well. It doesn't hurt to do this.
		setNewState ( getPropertyValueAsInteger("originState") , originalTimeLeft );
		
		//***
		//*** END EXECUTION OF parseCommand() METHODS
		//***
		
		//end() not found
		return false;
	}
	
	public boolean execCommand ( String commandstring  )
	{
		
		
		//conservative mode check
		if ( getPropertyValueAsBoolean("noPronounDisambiguation") && matchedTwoEntitiesPermissive )
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
				commandstring = lenguaje.sustituirVerbo ( commandstring );
				commandstring = lenguaje.sustituirAlias ( commandstring );
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
		commandstring = lenguaje.sustituirVerbo ( commandstring );		
		commandstring = lenguaje.sustituirAlias ( commandstring );

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
			
			//else
			if ( actionArgs[0] == null || !((Path)actionArgs[0]).isValid() ) //changed to admit a custom exit to have a standard name
			{
				//Mirar las salidas personalizadas
				for ( int i=0 ; i<habitacionActual.otherExits.length ; i++ )
				{
					if ( habitacionActual.isValidExit(false,i) && habitacionActual.getExit(false,i).matchExitCommand( arguments /*StringMethods.getTok( arguments,StringMethods.numToks(arguments,' '), ' ' ) */ ) )
					{
						mentions.setLastMentionedVerb(command);
						actionArgs[0] = habitacionActual.getExit ( false , i );	
						//return go (habitacionActual.getExit( false,i ));
					}
				}	

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
				setNewState( 1 /*IDLE*/, 1 );
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

		} //FIN CMD DEJAR
		else if ( "inventory".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) && arguments.trim().length() < 1 ) //inventario. deja de valer poner inventario algo.
		{
			showInventory();
			/*
			if ( inventory != null )
			{
				String str = inventory.toString(this);
				if ( str.equalsIgnoreCase("nada.") ) write( io.getColorCode("information") + "No tienes nada.\n" + io.getColorCode("reset") );
				else
				{
					write( io.getColorCode("information") + "Tienes " + str + "\n" + io.getColorCode("reset") );

					Inventory limbs = getFlattenedPartsInventory();
					//cosas que blandes
					if ( wieldedWeapons != null )
						for ( int i = 0 ; i < wieldedWeapons.size() ; i++ )
						{
							if ( wieldedWeapons.elementAt(i) != null )
							{
								Item arma = wieldedWeapons.elementAt(i);
								//buscar miembro que blande el arma
								for ( int j = 0 ; j < limbs.size() ; j++ )
								{
									Item miembro = limbs.elementAt(j);
									if ( miembro.getRelationshipPropertyValueAsBoolean(arma,"wields") )
									{
										write( io.getColorCode("information") + "Blandes " + arma.constructName2OneItem(this)  + " en " + miembro.constructName2OneItem(this) + ".\n" + io.getColorCode("reset") );
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
								write( io.getColorCode("information") + "Llevas " + vestido.constructName2OneItem(this)  + " en " + toOutput + ".\n" + io.getColorCode("reset") );
							}
						}
				}
			}
			else write( io.getColorCode("information") + "No tienes nada.\n" + io.getColorCode("reset") );
			 */
			setNewState( 1 /*IDLE*/, 1 );
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
		} //FIN CMD INVENTARIO

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
		}



		else if ( "say".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //decir
		{
			//habitacionActual.informActionAuto ( this , null , "$1 dice \"" + arguments + "\"\n" );
			//say(arguments);
			//escribir("\n");

			comandoDecir ( arguments );

			setNewState( 1 /*IDLE*/, 1 );
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





	public void die()
	{
		super.die();
		setRoom( mundo.getLimbo() ); //limbo
	}

	public void changeState ( World mundo )
	{

		Debug.println("Player state " + getState() + ", target " + getTarget() + ", tu's " + getPropertyTimeLeft("state") );

		//Debug.println("[PROPERTY UPDATE: " + "state" + "]" + new Vector(propertiesList).toString());

		//Debug.println("changeState()");
		try
		{
			characterChangeState( mundo );
		}
		catch ( java.io.IOException nopuidorl )
		{
			write( io.getColorCode("error") + "Excepci�n E/S en characterChangeState()" + io.getColorCode("reset") );	
		}
	}

	public void characterChangeState( World mundo ) throws java.io.IOException
	{

		//Debug.println("State: (" + getState() + "," + getPropertyTimeLeft("state") + ")");


		switch ( getState() )
		{
		case 1: //IDLE
			//if ( ! execCommand ( mundo ) )
			//	setNewState ( 1 /*IDLE*/, 1 /*penalizacion*/ );
			break;
		case 2: //GO

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
			catch ( bsh.TargetError bshte )
			{
				write( io.getColorCode("error") + "bsh.TargetError found onExitRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
				writeError(ExceptionPrinter.getExceptionReport(bshte));
			}

			habitacionActual.reportAction(this,null,"$1 se va hacia " + exitname + ".\n" , null , null , false );	

			setRoom ( mundo.getRoom(getTarget()) );

			habitacionActual.reportAction(this,null,"$1 llega desde " + Path.invert(exitname) + ".\n" , null , null , false );

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


			//-> si hay Mobiles, pueden reaccionar tambi�n a que entres (onEnterRoom de Mobile)
			MobileList ml = habitacionActual.getMobiles();
			if ( ml != null )
			{
				for ( int i = 0 ; i < ml.size() ; i++ )
				{
					Mobile bichoActual = ml.elementAt(i);

					try
					{
						//bichoActual.execCode("event_enterroom","this: " + habitacionActual.getID() + "\n" + "player: " + getID() + "\n" + "orig: " + habitacionAnterior );		
						bichoActual.execCode("onEnterRoom" , new Object[] {this} );
					}
					catch ( bsh.TargetError bshte )
					{
						write( io.getColorCode("error") + "bsh.TargetError found onEnterRoom , mobile number " + bichoActual.getID() + ": " + bshte + "\n" + bshte.getMessage() + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(bshte));
						bshte.printStackTrace();
					}

				}
			}	

			//mostrar sala si esto no est� desactivado
			if ( getPropertyValueAsBoolean("describeRoomsOnArrival") )
			{
			    show_room ( mundo );
			}
			
			setNewState ( 1 /*IDLE*/, 0 );
			break;

		case ATTACKING:
			manageEndOfAttackState(); //de Mobile
			//escribir("Attack done.");
			return;

		case CASTING:
			manageEndOfCastState(); //de Mobile
			return;
		case ATTACK_RECOVER:
			write("Te recuperas de tu movimiento de ataque.\n");
			setNewState ( IDLE , 0 );
			showCombatReport();
			break;
		case DAMAGE_RECOVER:
			write("Te recuperas del golpe recibido.\n");
			setNewState ( IDLE , 0 );
			showCombatReport();
			break;
		case BLOCK_RECOVER:
			write("Te recuperas de tu movimiento defensivo.\n");
			setNewState ( IDLE , 0 );
			showCombatReport();
			break;
		case SURPRISE_RECOVER: //para usar en c�digo bsh (encuentros por sorpresa)
			//escribir("Te haces cargo de la situaci�n.\n");
			setNewState ( IDLE , 0 );
			showCombatReport();
			break;
		case BLOCKING:
			write("Est�s preparado para bloquear...\n");
			setNewState ( READY_TO_BLOCK , 0 );
			return;
		case DODGING:
			setNewState ( READY_TO_DODGE , 0 );
			return;
		case READY_TO_BLOCK:
			//hold on that state
			setNewState ( READY_TO_BLOCK , 0 );
			return;
		case READY_TO_DODGE:
			//hold on to that state
			setNewState ( READY_TO_DODGE , 0 );
			return;

		case DYING:
			//die
			die();
			break;	

		case DEAD:
			//don't hold on that state itc, you're in Limbo, enjoy yourself
			setNewState ( IDLE , 1 );
			//escribir("\nEst�s muerto.");
			return;	

		case DISABLED: //disconnected, etc. Don't do anything.
			setNewState ( DISABLED , 1 );
			return;


		}
		//listen 4 events?
		if ( ! execCommand ( mundo ) )
			setNewState ( 1 /*IDLE*/, 1 /*penalizacion*/ );
	}



	/*
	public boolean resolveParseCommandOnContentsForOneEntity ( EntityList posiblesObjectivos , String arguments , String fullArguments )
	{

	}
	 */


	



	//nos dice "tal est� a punto de atacarte, podr�as bloquearlo... la espada de tal
	//ya casi te ha pillado..."
	public void showCombatReport ( )
	{

		if ( getEnemies() == null ) return;

		for ( int i = 0 ; i < getEnemies().size() ; i++ )
		{
			Mobile enemigo = getEnemies().elementAt(i);
			if ( enemigo.getState() == ATTACKING && enemigo.getTarget() == getID() )
			{
				long tiempo = enemigo.getPropertyTimeLeft ( "state" );

				int nSimulations = (int) getStat("INT");

				wieldedWeapons = getWieldedWeapons();
				
				for ( int j = 0 ; j < wieldedWeapons.size() ; j++ )
				{
					Weapon w = (Weapon) wieldedWeapons.elementAt(j);
					if ( w != null )
					{
						//ver si tiempo llegar�a para bloquear

						int blocksInTime = 0;

						for ( int s = 0 ; s < nSimulations ; s++ )
						{
							int t = generateBlockTime ( w );
							if ( t <= tiempo )
								blocksInTime++;
						}

						double inTimeProb = (double)blocksInTime / (double)nSimulations;

						String toInform;

						if ( inTimeProb >= 0.9 )
						{
							toInform = "Seguramente te dar�a tiempo a bloquear el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						if ( inTimeProb >= 0.7 )
						{
							toInform = "Crees que te dar�a tiempo a bloquear el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						else if ( inTimeProb >= 0.5 )
						{
							toInform = "Es posible que te d� tiempo a bloquear el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						else if ( inTimeProb >= 0.3 )
						{
							toInform = "Ser� bastante dif�cil bloquear a tiempo el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						else if ( inTimeProb >= 0.1 )
						{
							toInform = "Ser� muy dif�cil bloquear a tiempo el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						else
						{
							toInform = "No crees que puedas bloquear a tiempo el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}

						habitacionActual.reportAction ( this , enemigo , null , null , toInform + "\n" , true );						

					} //end if weapon not null
				} //end for each weapon

				//ver si tiempo llegar�a para esquivar

				int dodgesInTime = 0;

				for ( int s = 0 ; s < nSimulations ; s++ )
				{
					int t = generateDodgeTime ( );
					if ( t <= tiempo )
						dodgesInTime++;
				}

				double dodgeInTimeProb = (double)dodgesInTime / (double)nSimulations;

				String toInform;

				if ( dodgeInTimeProb >= 0.8 )
				{
					toInform = "Seguramente podr�as esquivar el ataque de $2 a tiempo."; 
				}
				else if ( dodgeInTimeProb >= 0.6 )
				{
					toInform = "Probablemente podr�as esquivar a tiempo el ataque de $2.";
				}
				else if ( dodgeInTimeProb >= 0.4 )
				{
					toInform = enemigo.getCurrentWeapon().constructName2OneItem(this) + " de $2 est� cerca, ser� dif�cil esquivar su ataque.";
				}
				else if ( dodgeInTimeProb >= 0.2 )
				{
					toInform = enemigo.getCurrentWeapon().constructName2OneItem(this) + " de $2 est� casi encima , ser� muy dif�cil esquivar su ataque.";
				}
				else
				{
					toInform = enemigo.getCurrentWeapon().constructName2OneItem(this) + " de $2 est� encima, no crees que puedas esquivar el ataque.";
				}

				habitacionActual.reportAction ( this , enemigo , null , null , toInform + "\n" , true );	


			} //end if enemy attacking		
		} //end for each enemy

	}



	public void show_room ( World mundo )
	{

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
		catch ( bsh.TargetError bshte )
		{
			write( io.getColorCode("error") + "bsh.TargetError found onShowRoom , room number " + habitacionActual.getID() + ": " + bshte + io.getColorCode("reset") );
			writeError(ExceptionPrinter.getExceptionReport(bshte));
		}

	}


	//pone el estado "press any key" de la entrada...
	public synchronized void waitKeyPress ( )
	{
		if ( !from_log ) //si estamos ejecutando un log (salvado) es absurdo
		{                //vernos interrumpidos por un "press any key"
			//try
			//{
			//io.setWaitingPlayer(this);
			//io.activatePressAnyKeyState(); 
			//wait();

			Thread th = Thread.currentThread();

			//Debug.println("WKP Thread: " + th + "( " + SwingUtilities.isEventDispatchThread() + " )");

			if ( th instanceof GameEngineThread )
			{
				GameEngineThread gte = (GameEngineThread)th;
				if ( gte.isRealTimeEnabled() )
				{
					if ( mundo.getPlayerList().size() > 1 )
					{
						io.write("--\n"); //comportamiento alternativo
						return; //no podemos dejar a todo el motor esperando si estamos en modo as�ncrono y con m�s jugadores.
					}
				}
			}

			Debug.println("WKP Call: " + io.getClass());


			io.waitKeyPress();

			//}
			//catch ( InterruptedException intex ) 
			//{ 
			//	;
			//}
		}
	}

	/**
	 * @deprecated Use {@link #clearScreen()} instead
	 */
	public void borrarPantalla()
	{
		clearScreen();
	}



	public void clearScreen()
	{
		io.clearScreen();
	}


	//de informador
	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public void escribir ( String s )
	{
		write(s);
	}



	//de informador
	public void write ( String s )
	{
		io.write(s);
	}
	public void setIO ( InputOutputClient es )
	{
		io = es;
	}



	//otra para plurales, y ZR's para plurales.	

	private static String firstWord ( String s )
	{
	    StringTokenizer st = new StringTokenizer(s);
	    if ( st.hasMoreTokens() ) return st.nextToken();
	    else return "";
	}
	
	private static String restWords ( String s )
	{
	    StringTokenizer st = new StringTokenizer(s);
	    if ( !st.hasMoreTokens() ) return "";
	    else
	    {
		st.nextToken();
		if ( !st.hasMoreTokens() ) return "";
		else return st.nextToken("");
	    }
	}
	
	



	public boolean separateSentences()
	{

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

	/*

	Obsolete. Newer version takes quotation marks into account.

	public boolean separateSentences ( )
	{

		//parse: 1. buscar separadores en la frase
		//separadores posibles: "," ";" " y " " e "

		//int current_index = 0;
		//while ( )
		//{
			//search for next separator
		//	indexOf( )
		//}

			//Debug.println("Separando: " + commandstring );

			Vector tokensConSeparadores = StringMethods.tokenizeWithComplexSeparators ( commandstring , StringMethods.STANDARD_SENTENCE_SEPARATORS() );
			Vector tempCommandQueue = new Vector();
			while ( !tokensConSeparadores.isEmpty() )
			{
				//insertar en cola
				tempCommandQueue.addElement(tokensConSeparadores.elementAt(0));
				tokensConSeparadores.removeElementAt(0);
			}
			tempCommandQueue.addAll ( commandQueue );
			commandQueue = (Vector)tempCommandQueue.clone();
			if ( !commandQueue.isEmpty() )
			{
				commandstring = (String)commandQueue.elementAt(0);
				//Debug.println("(2) Command string set to " + (String)commandQueue.elementAt(0) );
				commandQueue.removeElementAt(0);
			}
			else
			{
				//comando nulo
				return false;
			}
			//Debug.println("Cola:");
			for ( int i = 0 ; i < commandQueue.size() ; i++ )
				//Debug.println(commandQueue.elementAt(i));
				;
			return true;

	}
	 */

	/**
	 * Substitutes pronouns AND aplies spell checking if enabled
	 */
	public String substitutePronounsInSentence ( String commandstring )
	{
		StringTokenizer st = new StringTokenizer(commandstring);
		String originalVerb = st.nextToken(); //matalo
		String expandedVerb = lenguaje.substitutePronouns ( this , originalVerb , mentions ); //mata Juanito 
		String expandedVerbWithoutPronoun = firstWord ( expandedVerb ); //mata
		String expandedString = expandedVerb + " " + restWords(commandstring); //mata Juanito con el cuchillo
		String workingString;
		if ( !lenguaje.isVerb(originalVerb) && lenguaje.isVerb(firstWord(expandedVerbWithoutPronoun)) ) 
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



	private boolean abrirPuertaConLlave ( Inventory i1 , Inventory i2 )
	{

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





	//copy-pasted and modified from atacarBichoConArma
	/*
	private boolean bloquearBichoConArma ( MobileList ml , Inventory i ) //uses "arguments" attr
	{

		if ( i == null || i.isEmpty() || ml == null || ml.isEmpty() ) return false;	

		boolean mirado = false;	

		Vector[] patternMatchVectorSingSing = ml.patternMatchTwo ( i , arguments , false , false ); //en singular y singular
		Vector[] patternMatchVectorSingPlur = ml.patternMatchTwo ( i , arguments , false , true ); //en singular y plural
		Vector[] patternMatchVectorPlurSing = ml.patternMatchTwo ( i , arguments , true , false ); //en plural y singular
		Vector[] patternMatchVectorPlurPlur = ml.patternMatchTwo ( i , arguments , true , true ); //en plural y plural

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

			block (  objetivo , (Weapon)(theVectors[1].elementAt(0)) );

			//escribir( io.getColorCode("action") + ((Item)(theVectors[0].elementAt(0))).unlock( (Item)(theVectors[1].elementAt(0)) ) + io.getColorCode("reset") + "\n"  );
		}

		return mirado;


	}
	 */



	//pr�cticamente igual a atacarBichoConArma() [copy-pasted, cambiando las comprobaciones de enemistad y la funci�n attack por block]
	private boolean bloquearBichoConArma ( MobileList ml , Inventory i ) //uses "arguments" attr
	{

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




	private boolean atacarBichoConArma ( MobileList ml , Inventory i ) //uses "arguments" attr
	{

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






	private boolean cerrarPuertaConLlave ( Inventory i1 , Inventory i2 )
	{

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






	//ej. coger pilas de armario
	private boolean cogerContenidoEspecificandoContenedor ( String args , Inventory inv , String infoString )
	{
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

	private boolean cogerContenido ( String args , Inventory inv , String infoString )
	{
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

	private boolean cogerContenido ( Inventory inv , String infoString )
	{
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

	//mirar extra descriptions de items de habitaci�n




	//coge un item del inventario inv.
	private boolean cogerItem ( Inventory inv , String extraInfo )
	{

		return cogerItem ( arguments , inv , extraInfo );

	} //end method



	public Vector getFinalCommandLog() //para estad�sticas
	{
		return finalExecutedCommandLog;
	}


	//Funci�n que es llamada si el jugador es remoto (est� asociado a un cliente remoto)
	//y quien lo maneja se desconecta. Hace todo lo necesario para manejar la desconexi�n
	//(id est, que la cosa d� el pego en el mundo)
	public void disconnect()
	{

		//mark player as disconnected
		setProperty ( "disconnected" , true );

		//set disabled state so the Player object won't try to get input on changestates
		setNewState ( DISABLED , 1 );

		//store current room ID as property in order to be able to return player to room later
		setProperty ( "room" , habitacionActual.getID() );

		//remove player from its room for others not to interact with it
		habitacionActual.removeMob(this);	

		//inform that the player's leaving. Auto, for only 3rd person will be shown.
		habitacionActual.reportActionAuto ( this , null , "$1 desaparece en un mar de irrealidad.\n" , true );


	}

	//inversa de disconnect()
	public void reconnect( InputOutputClient io )
	{

		if ( getPropertyValueAsBoolean("disconnected") == false )
		{
			write("Hmm. �No est�s conectado ya?\n");
			return;
		}

		setProperty ( "disconnected" , false );

		setNewState ( IDLE , 1 );

		Room enQueEstaba = mundo.getRoom ( getPropertyValueAsInteger("room") );

		enQueEstaba.addMob(this);

		getClient().write("Has sido a�adido al mundo.\n");

		getRoom().reportActionAuto(this,null,"De repente, $1 aparece de la nada.\n",false); 

		write("Old player rejoined the game.\n");

		setIO ( io );

	}



	




	//parsea y ejecuta una acci�n que referencia a un objetivo

	//returns: true si se ha encontrado objetivo; aunque la acci�n no se pudiese ejecutar
	//			false si no se han encontrado objetivos. Habr� que dar un mensaje "�qu� pretendes Xar?"
	//maneja la ZR.
	public boolean oneTargetAction ( String action , String arguments , EntityList posiblesObjetivos )
	{
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
				return true;
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
					return true;
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

}




