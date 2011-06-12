/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
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
import eu.irreality.age.language.Mentions;
/**
 * Clase del personaje, jugador.
 *
 */
public class Player extends Mobile implements Informador
{

	//INSTANCE VARIABLES

	/**Comando actual*/
	protected String commandstring , command , arguments;

	/**Cola para los comandos*/
	protected Vector commandQueue = new Vector();

	/**Variables del parser de strings: zonas de referencia*/
	
	/*
	protected String ZR_verbo = "mirar";
	protected String ZR_objeto_masculino_singular="";
	protected String ZR_objeto_femenino_singular="";
	protected String ZR_objeto_singular="";
	protected String ZR_objeto_masculino_plural="";
	protected String ZR_objeto_femenino_plural="";
	protected String ZR_objeto_plural="";
	*/
	Mentions mentions = new Mentions();
	
	//unused: protected String ZR_persona_masculino;
	//unused: protected String ZR_persona_femenino;
	//unused: protected String ZR_persona;

	/**¿Estamos cargando de un log?*/
	protected boolean from_log;
	protected Vector logfile; //log de ENTRADA

	/**¿Estamos ejecutando un comando forzado?*/
	protected boolean forced;
	protected String force_string; //string del force

	/**¿Estamos en la segunda oportunidad? (nos hemos improvisado el verbo anterior tras no reconocer el primero)*/
	protected boolean secondChance;
	/**Flag de control para indicar que tenemos que activar el second-chance al leer de la cola*/
	protected boolean nextCommandSecondChance;

	BufferedReader logReader;	


	//sólo a efectos de estadísticas, ya sale todo sustituido.
	Vector finalExecutedCommandLog = new Vector();

	//variables para el combate
	Weapon lastAttackWeapon = null;
	Mobile lastAttackedEnemy = null;
	Weapon lastBlockWeapon = null;
	Mobile lastBlockedEnemy = null;
	
	//a cuántas entidades se refirió el comando anterior
	private boolean matchedOneEntity = false;
	private boolean matchedTwoEntities = false;
	private boolean matchedOneEntityPermissive = false;
	private boolean matchedTwoEntitiesPermissive = false;



	public void setPlayerName( String nombre )
	{
		//Función de conveniencia para poner nombre a un jugador, que sirve como
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



	/**Lista dinámica de objetos.*/
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
	}

	//METHODS

	/*Para salvados: en vez de coger comandos de teclado, se ejecuta un fichero de log
	hasta que termine. Esto abre el fichero, lo demás ya lo hará el thread del engine
	(es automático) -> antes de hacer el start al thread, poner prepareLog si es un
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
			escribir(io.getColorCode("error") + "Excepción I/O al leer el log" + io.getColorCode("reset"));
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
		if ( this.getIO() instanceof MultimediaInputOutputClient )
		{
			MultimediaInputOutputClient mioc = (MultimediaInputOutputClient) this.getIO();
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
	A pesar del nombre ambiguo (no se me ocurría ninguno mejor, la verdad) esta función
	sólo sirve para cuando no se ejecuta un comando porque no se entiende lo que viene
	después (por ejemplo, "coger adfasfg" o "coger <algo que no hay en la habitación o
	el programa no reconoce>"). Cuando no se ejecuta por motivos ya del juego (no coges
	porque llevas demasiado peso, no miras porque estás ciego, etc.) no se debe usar esto,
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
					write( io.getColorCode("error") + "Excepción E/S en update() para propiedad custom_parsing" + io.getColorCode("reset") );	
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
				//se acabó el log
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

		//Ya tenemos lo que el tío escribió, en theCommand.

		List ofEntities = getRelatedEntities ( "custom_parser" ); 
		//sólo consideramos la primera no nula, porque sólo puede haber una.
		if ( ofEntities.size() < 1 )
		{
			write(io.getColorCode("error") + "Error: llamada a customParse() sin entidades con parsers activos para ésta." + io.getColorCode("reset"));
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
	 * Used for commands of the kind "María, hola" (to recognise mobile names as verbs)
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

		- Que hayan quedado comandos pendientes de ejecución de otra vez: estarán
		en la cola de comandos pendientes.
		- Que no: esperamos una entrada por la editbox, que cambiará la variable
		"commandstring" poniendo el nuevo comando y despertará el thread del wait.
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
			//pasado a más tarde

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


			/*Preparación del comando:*/

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
					command = StringMethods.getTok(commandstring,1,' ').trim();
					arguments = StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
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

			/*Aquí sustituimos los pronombres por las zonas de referencia.*/
			//substitutePronouns();
			//pasado a más tarde

			/*Separamos las subfrases*/	
			if ( !separateSentences() ) return false;

		}

		//modular execCommand()

		if ( commandstring.isEmpty() ) return false; //empty strings can result if, for example, input was ",something", etc.
		
		return execCommand ( commandstring  );

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
				command = StringMethods.getTok(commandstring,1,' ').trim();	
				
				write ( io.getColorCode("denial") + mundo.getMessages().getMessage("ambiguous.pronoun","$command",command,new Object[]{this,commandstring}) + io.getColorCode("reset") );
				mentions.setLastMentionedVerb ( firstWord(substitutePronounsInSentence(commandstring)) );
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
		command = StringMethods.getTok(commandstring,1,' ').trim();

		//patch to undo synonym substitutions on command "decir"
		if ( "decir".equalsIgnoreCase(command) )
		{
			commandstring = originalTrimmedCommandString;
		}	
		arguments = StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();


		//Debug.println("Definite command to exec: " + commandstring );

		//for statistics only:
		finalExecutedCommandLog.addElement(commandstring);


		//Sistema de acciones
		String actionName = "";
		Object[] actionArgs = null;



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
				command = StringMethods.getTok(commandstring,1,' ').trim();
				arguments = StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
			}
		}
		catch ( bsh.TargetError te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at player's parseCommand, command was " + command + arguments + ", error was " + te + io.getColorCode("reset") );
			writeError(ExceptionPrinter.getExceptionReport(te));
		}

		if ( ejecutado )
		{
			//luego esto lo hara el codigo
			setNewState( 1 /*IDLE*/, 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}

		/*
		//codigo bsh en el mundo
		try
		{
			ReturnValue retval = new ReturnValue(null);
			ejecutado = ejecutado || mundo.execCode( "parseCommand" , new Object[] { this , command , arguments } , retval );
			if ( retval.getRetVal() != null )
			{
				commandstring = (String)retval.getRetVal();
				//Debug.println("Command String Changed To " + (String)retval.getRetVal()); 
				command = StringMethods.getTok(commandstring,1,' ').trim();
				arguments = StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
			}
		}
		catch ( bsh.TargetError te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at world's parseCommand, command was " + command + arguments + ", error was " + te + io.getColorCode("reset") );
		}

		if ( ejecutado )
		{
			//luego esto lo hara el codigo
			setNewState( 1 , 1 ); //idle state
			mentions.setLastMentionedVerb(command);
			return true;
		} */

		//[04.03.04] Tocho grande de código (ejecutar comando personalizado con items de
		//inventario y con items de habitación) sustituido por tocho más simple (ejecutar
		//comando personalizado con objetivos)

		ejecutado = false;

		matchedOneEntity = false;
		matchedTwoEntities = false;
		matchedOneEntityPermissive = false;
		matchedTwoEntitiesPermissive = false;

		EntityList posiblesObjetivos = getReachableEntities();

		/*
	Vector[] objetivos_ss = ParserMethods.refersToEntitiesIn ( arguments,posiblesObjetivos,posiblesObjetivos,false,false);
	Vector[] objetivos_sp = ParserMethods.refersToEntitiesIn ( arguments,posiblesObjetivos,posiblesObjetivos,false,true);	
	Vector[] objetivos_ps = ParserMethods.refersToEntitiesIn ( arguments,posiblesObjetivos,posiblesObjetivos,true,false);
	Vector[] objetivos_pp = ParserMethods.refersToEntitiesIn ( arguments,posiblesObjetivos,posiblesObjetivos,true,true);
		 */

		EntityList posiblesObjetivosPermissive = getReachableEntities(true);

		List matches_ss = ParserMethods.parseReferencesToEntitiesIn( arguments,posiblesObjetivos,posiblesObjetivos,false,false);
		List matches_sp = ParserMethods.parseReferencesToEntitiesIn( arguments,posiblesObjetivos,posiblesObjetivos,false,true);
		List matches_ps = ParserMethods.parseReferencesToEntitiesIn( arguments,posiblesObjetivos,posiblesObjetivos,true,false);
		List matches_pp = ParserMethods.parseReferencesToEntitiesIn( arguments,posiblesObjetivos,posiblesObjetivos,true,true);

		List allMatches = new ArrayList();
		allMatches.addAll(matches_ss);
		allMatches.addAll(matches_sp);
		allMatches.addAll(matches_ps);
		allMatches.addAll(matches_pp);

		matchedTwoEntities = ( allMatches.size() > 0 );

		Vector matches_s = ParserMethods.refersToEntityInRecursive ( arguments,posiblesObjetivos,false );
		Vector matches_p = ParserMethods.refersToEntityInRecursive ( arguments,posiblesObjetivos,true );
		
		matchedOneEntity = ( matches_s.size() > 0 || matches_p.size() > 0 );
		
		//let's see if this works.
		ejecutado = resolveParseCommandForTwoEntities ( posiblesObjetivos , arguments , arguments , false );
		if ( ejecutado ) //código hizo end()
		{
			setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}

		//ejecutar parseCommand sobre una entidad, if possible
		ejecutado = resolveParseCommandForOneEntity ( posiblesObjetivos , arguments , arguments , false );
		if ( ejecutado ) //código hizo end()
		{
			setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}
		
		//A.
		//comandos sobre un componente: sólo se ejecutan si no matchearon comandos con objetos
		//("coger bastón del suelo" debe ejecutarse antes como comando sobre objeto bastón que sobre componente suelo)
		//(also, comandos sobre componentes no se ejecutan si el verbo no es reconocido, para que funcione bien "coger espada y bastón del suelo")
		//(si no, haríamos: coger espada (OK), bastón del suelo (comando sobre "suelo", pifia)
		if ( !matchedOneEntity && !matchedTwoEntities && lenguaje.isVerb(command) )
		{
			EntityList posiblesObjetivosForComponents = (EntityList) posiblesObjetivos.clone();
			posiblesObjetivosForComponents.addEntity(this.getRoom()); //componentes pueden ser de habitación
			ejecutado = resolveParseCommandForOneComponent ( posiblesObjetivosForComponents , arguments );
			if ( ejecutado ) //código hizo end()
			{
				setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}
		}
		
		//ahora vemos los posibles prefijos para el parseCommand. 
		//TODO: I think this is no longer needed but I may be wrong.
		//TODO: removed 2011-04-01. Let's see if it works.
		//seems to work fine.
		/*
		if ( !matchedTwoEntities )
		{
			int nArgToks = StringMethods.numToks(arguments,' ');
			for ( int i = nArgToks-1 ; i >= 1 ; i-- )
			{
				String currentArgs = StringMethods.getToks(arguments,1,i,' ').trim();
				ejecutado = resolveParseCommandForOneEntity ( posiblesObjetivos , currentArgs , arguments , false );
				if ( ejecutado ) //código hizo end()
				{
					setNewState( 1 , 1 );
					mentions.setLastMentionedVerb(command);
					return true;
				}
			}
		}
		*/




		Vector patternMatchVectorSing = ParserMethods.refersToEntityIn ( arguments,posiblesObjetivos,false );
		Vector patternMatchVectorPlur = ParserMethods.refersToEntityIn ( arguments,posiblesObjetivos,true );

		//primero vemos si hay una definicion especifica del comando en la habitacion, con o sin argumentos
		//si la definicion esta especificada sin argumentos y hay argumentos, los dejamos en el data segment.
		//(argumentos flexibles) <-- todo esto código EVA
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

		//ahora vamos con el código BeanShell, más simple, simplemente ejecutamos la función parseCommand.
		try
		{
			ejecutado = ejecutado || habitacionActual.execCode ( "parseCommand" , new Object[] { this , command , arguments } );
		}
		catch ( bsh.TargetError te )
		{
			te.printStackTrace();
			write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + arguments + ", room number " + habitacionActual.getID() + ", error was " + te + io.getColorCode("reset") );
			writeError(ExceptionPrinter.getExceptionReport(te));
		}

		if ( ejecutado ) 
		{
			mentions.setLastMentionedVerb(command);
			return true ;
		}

		//parseCommands de los objetos definidos en el mundo.

		ejecutado = resolveParseCommandForTwoEntities ( posiblesObjetivos , arguments , arguments , true );
		if ( ejecutado ) //código hizo end()
		{
			setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}
		ejecutado = resolveParseCommandForOneEntity ( posiblesObjetivos , arguments , arguments , true );
		if ( ejecutado ) //código hizo end()
		{
			setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
			return true;
		}
		

		//parseCommand() del mundo, lo último que se ejecuta antes de las respuestas por defecto del sistema.
		try
		{
			ReturnValue retval = new ReturnValue(null);
			ejecutado = ejecutado || mundo.execCode( "parseCommand" , new Object[] { this , command , arguments } , retval );
			if ( retval.getRetVal() != null )
			{
				commandstring = (String)retval.getRetVal();
				//Debug.println("Command String Changed To " + (String)retval.getRetVal()); 
				command = StringMethods.getTok(commandstring,1,' ').trim();
				arguments = StringMethods.getToks(commandstring,2,StringMethods.numToks(commandstring,' '),' ').trim();
			}
		}
		catch ( bsh.TargetError te )
		{
			write(io.getColorCode("error") + "bsh.TargetError found at world's parseCommand, command was " + command + arguments + ", error was " + te + io.getColorCode("reset") );
			writeError(ExceptionPrinter.getExceptionReport(te));
		}

		if ( ejecutado )
		{
			//luego esto lo hara el codigo
			setNewState( 1 , 1 ); //idle state
			mentions.setLastMentionedVerb(command);
			return true;
		} 




		//si no estaba definido en la habitacion		
		if ( lenguaje.translateVerb(command,"en").equalsIgnoreCase( "go" ) ) //ir
		{

			actionName = "go";
			actionArgs = new Object[1]; //en concreto, será un Path.

			if ( StringMethods.numToks(commandstring,' ') < 2 )
			{

				write ( io.getColorCode("denial") + mundo.getMessages().getMessage("go.nowhere",new Object[]{this}) + io.getColorCode("reset") );
				mentions.setLastMentionedVerb(command);
				cancelPending();
				return false;
			}	

			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("norte")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("n") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.NORTE ));

				actionArgs[0] = habitacionActual.getExit ( true , Path.NORTE );

			}
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("sur")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("s") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.SUR ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.SUR );
			}
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("oeste")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("e") )
			{
				mentions.setLastMentionedVerb(command);
				//return go (habitacionActual.getExit( true,Path.OESTE ));
				actionArgs[0] = habitacionActual.getExit ( true , Path.OESTE );
			}
			else if ( StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("este")
					|| StringMethods.getTok( arguments , StringMethods.numToks( arguments,' ' ) , ' ' ).equalsIgnoreCase("o") )
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
			else
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

				//si no había ninguna salida
				if ( actionArgs[0] == null )
				{
					escribirDenegacionComando(io.getColorCode("denial") + 
							mundo.getMessages().getMessage("go.where",new Object[]{this,arguments})  //"¿Cómo? ¿Hacia dónde quieres ir?\n" 
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

			//mirar las salidas estándar
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
			//si no hemos hecho return al llegar aquí, es que no hay salida que nos lleve a la habitación anterior
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
				//extra descriptions habitación
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

					//intentar mirar extras de items de habitación

					Debug.println("Mirado init");

					if ( !mirado ) mirado = mirarExtrasItems ( arguments , habitacionActual.itemsInRoom );

					//intentar mirar extras de items de inventario -> ¿tal vez antes de los propios ítems?

					Debug.println("Mirado="+mirado);

					if ( !mirado ) mirado = mirarExtrasItems ( arguments , inventory );

					//intentar mirar un item de habitación.

					Debug.println("Mirado="+mirado);

					if ( !mirado ) mirado = mirarItem ( arguments , habitacionActual.itemsInRoom );

					//contenido de items de habitación.

					Debug.println("Mirado="+mirado);

					if ( !mirado ) mirado = mirarContenido ( arguments , habitacionActual.itemsInRoom );	

					//intentar mirar un item de inventario.

					Debug.println("Mirado="+mirado);

					if ( !mirado ) mirado = mirarItem ( arguments , inventory );

					//contenido de items de inventario.

					Debug.println("Mirado="+mirado);

					if ( !mirado ) mirado = mirarContenido ( arguments , inventory );						

					//intentar mirar un bicho de la habitación

					Debug.println("Mirado="+mirado);

					if ( !mirado ) mirado = mirarBicho ( arguments , habitacionActual.mobsInRoom );

					//intentar mirar partes del propio cuerpo

					Debug.println("Mirado="+mirado);

					if ( !mirado ) mirado = mirarItem ( arguments , getFlattenedPartsInventory() );

					//intentar mirar extras propios

					Debug.println("Mirado="+mirado);

					MobileList yo = new MobileList();
					yo.addElement(this);

					if ( !mirado ) mirado = mirarExtrasBichos ( arguments , this.getRoom().getMobiles() );
					
					//if ( !mirado ) mirado = mirarExtrasBichos ( arguments , yo );

					if(!mirado) //no miramos nada.
					{
						escribirDenegacionComando(io.getColorCode("denial") + 
								mundo.getMessages().getMessage("look.what",new Object[]{this,arguments})  //"¿Qué pretendes mirar?\n" 
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
						mundo.getMessages().getMessage("attack.what",new Object[]{this,arguments})  //"¿Cómo? ¿Atacar a quién?\n" 
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
						mundo.getMessages().getMessage("block.what",new Object[]{this,arguments})  //"¿Cómo? ¿Defenderse de quién?\n" 
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
						mundo.getMessages().getMessage("dodge.what",new Object[]{this,arguments})  //"No te atacan. ¿Esquivar qué?\n" 
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



			//Paso 1: Abrir con llave. [si hay dos objetos en la entrada, el 1º será la puerta
			//y el 2º la llave, como en "abrir la puerta roja con la llave amarilla pálida"

			boolean mirado = false;

			Vector[] patternMatchVectorSingSing = null;
			Vector[] patternMatchVectorSingPlur = null;
			Vector[] patternMatchVectorPlurSing = null;
			Vector[] patternMatchVectorPlurPlur = null;

			mirado = abrirPuertaConLlave ( habitacionActual.itemsInRoom , inventory );	

			//tratar de abrir con llave algo que está en nuestro inventario, no en la habitación.
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
						patternMatchVectorSing = habitacionActual.itemsInRoom.patternMatch ( arguments , false ); //en singular
						patternMatchVectorPlur = habitacionActual.itemsInRoom.patternMatch ( arguments , true ); //en plural
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
						patternMatchVectorSing = inventory.patternMatch ( arguments , false ); //en singular
						patternMatchVectorPlur = inventory.patternMatch ( arguments , true ); //en plural
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
						mundo.getMessages().getMessage("open.what",new Object[]{this,arguments})  //"¿Qué pretendes abrir?\n" 
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



			//Paso 1: Cerrar con llave. [si hay dos objetos en la entrada, el 1º será la puerta
			//y el 2º la llave, como en "cerrar la puerta roja con la llave amarilla pálida"

			boolean mirado = false;

			mirado = cerrarPuertaConLlave ( habitacionActual.itemsInRoom , inventory );

			//probar a cerrar; pero algo que está en nuestro inventario
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
						patternMatchVectorSing = habitacionActual.itemsInRoom.patternMatch ( arguments , false ); //en singular
						patternMatchVectorPlur = habitacionActual.itemsInRoom.patternMatch ( arguments , true ); //en plural
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
						patternMatchVectorSing = inventory.patternMatch ( arguments , false ); //en singular
						patternMatchVectorPlur = inventory.patternMatch ( arguments , true ); //en plural
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
						mundo.getMessages().getMessage("close.what",new Object[]{this,arguments})  //"¿Qué pretendes cerrar?\n"  
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
			if ( command.equalsIgnoreCase( "poner" ) && ParserMethods.refersToEntity(arguments, this, false) ) //the player appears as an argument
			{
				if ( !oneTargetAction("wear",arguments,inventory) )
				{
					escribirDenegacionComando( io.getColorCode("denial") + 
							mundo.getMessages().getMessage("wear.what",new Object[]{this,arguments})  //"¿Qué pretendes vestir?\n"
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
			
			//Paso 1: poner [algo de mi inventario] en [contenedor de la habitación]
			
			if ( !mirado && habitacionActual.itemsInRoom != null && !habitacionActual.itemsInRoom.isEmpty()
					&& inventory != null && !inventory.isEmpty()
			)
			{
		
				mirado = putInside ( inventory , habitacionActual.itemsInRoom , arguments );

			} //end if (par inventario-habitación)

			//Paso 2: poner [algo de mi inventario] en [contenedor también del inventario]

			if ( !mirado
					&& inventory != null && !inventory.isEmpty()
			)
			{
				
				mirado = putInside ( inventory , inventory , arguments );

			} //end if (mirar par inventario-inventario)

			if(!mirado) //no ponemos nada
			{
				escribirDenegacionComando( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("put.what.where",new Object[]{this,arguments})  //"¿Cómo? ¿Poner qué dónde?\n" 
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
							mundo.getMessages().getMessage("unwear.what",new Object[]{this,arguments})  //"¿Qué pretendes quitarte?\n"
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
						mundo.getMessages().getMessage("get.what",new Object[]{this,arguments})  //"¿Qué pretendes coger?\n"
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
						mundo.getMessages().getMessage("drop.what",new Object[]{this,arguments})  //"¿Qué pretendes dejar?\n"
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
					write( io.getColorCode("information") + current.getTitle() + io.getColorCode("reset") + "\n" );
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
			//no necesitarás zr con esto.
			return true;
		}

		else if ( "unwear".equalsIgnoreCase(lenguaje.translateVerb(command,"en")) ) //desvestir
		{
			if ( !oneTargetAction("unwear",arguments,getWornItems()) )
			{
				escribirDenegacionComando( io.getColorCode("denial") + 
						mundo.getMessages().getMessage("unwear.what",new Object[]{this,arguments})  //"¿Qué pretendes quitarte?\n"
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
						mundo.getMessages().getMessage("wear.what",new Object[]{this,arguments})  //"¿Qué pretendes vestir?\n"
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
						mundo.getMessages().getMessage("wield.what",new Object[]{this,arguments})  //"¿Qué arma pretendes blandir?\n"
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
						mundo.getMessages().getMessage("unwield.what",new Object[]{this,arguments})  //"¿Qué arma enfundar?\n"
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
			escribirDenegacionComando( io.getColorCode("error") + "De momento, las opciones de salvar y cargar no están disponibles por texto en este interfaz. Puedes usar los menús para ello.\n" + io.getColorCode("reset") );
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
		



		//reconocemos el verbo; pero no tenemos npi de qué hacer con él...
		//TODO remmed "else" from here.
		else if ( lenguaje.isVerb ( command , true ) )
		{			
			String origCommand = "";
			if ( originalTrimmedCommandString != null )
				origCommand = StringMethods.getTok(originalTrimmedCommandString,1,' ').trim();
			//Debug.println("origCommand: " + origCommand +"ceremonia del te");
			if ( !origCommand.endsWith("me") && !origCommand.endsWith("te") && !origCommand.endsWith("se") )
			{
				//escribirDenegacionComando ( io.getColorCode("denial") + "¿Cómo? ¿" + commandstring + "?\n" + io.getColorCode("reset") );
				escribirDenegacionComando ( io.getColorCode("denial") + mundo.getMessages().getMessage("undefined.action","$command",commandstring,new Object[]{this,commandstring}) + io.getColorCode("reset") );
			}
			else
			{
				//si el comando se refería al propio jugador, poner el string sin pronombres sustituidos, porque queda un poco feo que diga "¿cómo? ¿matar a jugador?"
				//escribirDenegacionComando ( io.getColorCode("denial") + "¿Cómo? ¿" + originalTrimmedCommandString + "?\n" + io.getColorCode("reset") );
				escribirDenegacionComando ( io.getColorCode("denial") + mundo.getMessages().getMessage("undefined.action","$command",originalTrimmedCommandString,new Object[]{this,originalTrimmedCommandString}) + io.getColorCode("reset") );
			}
			mentions.setLastMentionedVerb(command);	
			cancelPending();
			return false;
		}

		//si llegamos aqui, el verbo no es ninguno de los reconocidos, ni ninguno
		//de los ejecutables en EVA, ni nada.
		//Vemos a ver si es el nombre de un bicho para hablarle (María, hola)

		else if ( !commandQueue.isEmpty() && ParserMethods.refersToEntityIn(command, this.getAllWorldMobiles(), false).size() > 0 )
		{
			String whatToSay = ((String)commandQueue.elementAt(0)).trim();
			commandQueue.removeElementAt(0);
			Mobile whomToSayItTo = (Mobile) ParserMethods.refersToEntityIn(command, this.getAllWorldMobiles(), false).get(0);
			return execCommand("decir a " + whomToSayItTo.getBestReferenceName(false) + "\"" + whatToSay + "\"");
		}

		//Si llegamos aquí, no sólo no es un verbo sino que no es una construcción tipo "María, hola".
		//Ahora, si no lo hemos hecho ya, probamos
		//a ver si es que ha habido una elipsis de verbo, añadiendo el verbo de la zona
		//de referencia.
		else if ( !mentions.getLastMentionedVerb().equalsIgnoreCase( command ) && lenguaje.isGuessable(mentions.getLastMentionedVerb()) )
		{

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
			write( io.getColorCode("error") + "Excepción E/S en characterChangeState()" + io.getColorCode("reset") );	
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


			//-> si hay Mobiles, pueden reaccionar también a que entres (onEnterRoom de Mobile)
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

			//mostrar sala si esto no está desactivado
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
		case SURPRISE_RECOVER: //para usar en código bsh (encuentros por sorpresa)
			//escribir("Te haces cargo de la situación.\n");
			setNewState ( IDLE , 0 );
			showCombatReport();
			break;
		case BLOCKING:
			write("Estás preparado para bloquear...\n");
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
			//escribir("\nEstás muerto.");
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

		matchedTwoEntitiesPermissive = ( allMatches.size() > 0 );

		for ( int i = 0 ; i < allMatches.size() ; i++ )
		{
			SentenceInfo si = (SentenceInfo) allMatches.get(i);
			String args1 = si.getArgs1();
			String args2 = si.getArgs2();
			Entity obj1 = si.getObj1();
			Entity obj2 = si.getObj2();
			List path1 = si.getPath1();
			List path2 = si.getPath2();


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
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsObj1(), command was " + command + fullArguments + ", entity " + currentObject1 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}

						if ( !ejecutado )
						{
							try
							{
								ejecutado = ejecutado || ((SupportingCode)currentObject1).execCode ( "parseCommandOnContentsTwoObjects" , new Object[] { this , command , args1 , args2 , path1 , path2 , currentObject2 } );
							}
							catch ( bsh.TargetError te )
							{
								write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsTwoObjects(), command was " + command + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
								writeError(ExceptionPrinter.getExceptionReport(te));
							}
						}
						if ( !ejecutado )
						{
							try
							{
								ejecutado = ejecutado || ((SupportingCode)currentObject1).execCode( "parseCommandOnContentsGeneric",  new Object[] { this , command, args1 , args2 , path1 , path2 , currentObject1 , currentObject2 , new Boolean(true) /*isFirst==true*/ } );
							}
							catch ( bsh.TargetError te )
							{
								write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric(), command was " + command + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
								writeError(ExceptionPrinter.getExceptionReport(te));
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
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsObj2(), command was " + command + fullArguments + ", entity " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}

						if ( !ejecutado )
						{
							try
							{
								ejecutado = ejecutado || ((SupportingCode)currentObject2).execCode ( "parseCommandOnContentsTwoObjects" , new Object[] { this , command , args1 , args2 , path1 , path2 , currentObject1 } );
							}
							catch ( bsh.TargetError te )
							{
								write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsTwoObjects(), command was " + command + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
								writeError(ExceptionPrinter.getExceptionReport(te));
							}
						}
						if ( !ejecutado )
						{
							try
							{
								ejecutado = ejecutado || ((SupportingCode)currentObject2).execCode( "parseCommandOnContentsGeneric",  new Object[] { this , command, args1 , args2 , path1 , path2 , currentObject1 , currentObject2 , new Boolean(false) /*isFirst==false*/ } );
							}
							catch ( bsh.TargetError te )
							{
								write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric(), command was " + command + fullArguments + ", first object was " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
								writeError(ExceptionPrinter.getExceptionReport(te));
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
							catch ( bsh.TargetError te )
							{
								write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsTwoObjects() executed from world, command was " + command + fullArguments + ", entity " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
								writeError(ExceptionPrinter.getExceptionReport(te));
							}
						}
						if ( !ejecutado )
						{
							try
							{
								ejecutado = ejecutado || mundo.execCode( "parseCommandOnContentsGeneric",  new Object[] { this , command, args1 , args2 , path1 , path2 , currentObject1 , currentObject2  } );
							}
							catch ( bsh.TargetError te )
							{
								write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric() executed from world, command was " + command + fullArguments + ", first object was " + currentObject1 + ", second object was " + currentObject2 + ", error was " + te + io.getColorCode("reset") );
								writeError(ExceptionPrinter.getExceptionReport(te));
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
        					catch ( bsh.TargetError te )
        					{
        						write(io.getColorCode("error") + "bsh.TargetError found at parseCommandObj1(), command was " + command + args1 + args2 + ", entity number " + obj1.getID() + ", second object was " + obj2.getID() + ", error was " + te + io.getColorCode("reset") );
        						writeError(ExceptionPrinter.getExceptionReport(te));
        					}
				    	}
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)obj1).execCode ( "parseCommandTwoObjects" , new Object[] { this , command , args1 , args2 , obj2 } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandTwoObjects(), command was " + command + args1 + args2 + ", entity number " + obj1.getID() + ", second object was " + obj2.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
					if ( !ejecutado )
					{
						try
						{
							//parseCommandGeneric ( Player aCreature , String verb , Sring args1 , Sring args2 , Entity obj1 , Entity obj2 , boolean isFirst )
							ejecutado = ejecutado || ((SupportingCode)obj1).execCode ( "parseCommandGeneric" , new Object[] { this , command , args1 , args2 , obj1 , obj2 , new Boolean(true) } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + args1 + args2 + ", entity number " + obj1 + ", second object was " + obj2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
				} //obj1 instof suppcode
				if ( ejecutado ) //código hizo end()
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
        					catch ( bsh.TargetError te )
        					{
        						write(io.getColorCode("error") + "bsh.TargetError found at parseCommandObj2(), command was " + command + args1 + args2 + ", entity number " + obj2.getID() + ", first object was " + obj1.getID() + ", error was " + te + io.getColorCode("reset") );
        						writeError(ExceptionPrinter.getExceptionReport(te));
        					}
				    	}
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)obj2).execCode ( "parseCommandTwoObjects" , new Object[] { this , command , args1 , args2 , obj1 } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandTwoObjects(), command was " + command + args1 + args2 + ", entity number " + obj2.getID() + ", first object was " + obj1.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
					if ( !ejecutado )
					{
						try
						{
							//parseCommandGeneric ( Player aCreature , String verb , Sring args1 , Sring args2 , Entity obj1 , Entity obj2 , boolean isFirst )
							ejecutado = ejecutado || ((SupportingCode)obj2).execCode ( "parseCommandGeneric" , new Object[] { this , command , args1 , args2 , obj1 , obj2 , new Boolean(false) } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + args1 + args2 + ", entity number " + obj1 + ", second object was " + obj2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
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
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandTwoObjects() executed from world, command was " + command + args1 + args2 + ", entity number " + obj2.getID() + ", first object was " + obj1.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					
				    }
				    if ( !ejecutado )
				    {
						try
						{
							//parseCommandGeneric ( Player aCreature , String verb , Sring args1 , Sring args2 , Entity obj1 , Entity obj2 , boolean isFirst )
							ejecutado = ejecutado || mundo.execCode ( "parseCommandGeneric" , new Object[] { this , command , args1 , args2 , obj1 , obj2 } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric() executed from world, command was " + command + args1 + args2 + ", entity number " + obj1 + ", second object was " + obj2 + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
				    }
				}

			}

			if ( ejecutado ) //código hizo end()
			{
				//luego esto lo hara el codigo
				setNewState( 1 , 1 );
				mentions.setLastMentionedVerb(command);
				return true;
			}


		} //end for each possible match

		//no end() has been hit
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
				catch ( bsh.TargetError te )
				{
					write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnComponent(), command was " + command + arguments + ", entity " + currentEntity + ", error was " + te + io.getColorCode("reset") );
					writeError(ExceptionPrinter.getExceptionReport(te));
				}
			}
		}
		
		if ( ejecutado ) //código hizo end()
		{
			//luego esto lo hara el codigo
			setNewState( 1 , 1 );
			mentions.setLastMentionedVerb(command);
		}
		return ejecutado;
		
	}
		

	
	/**
	 * @param objetivos_s
	 * @param objetivos_p
	 * @param arguments
	 * @param onWorld -> true if it's the world parsecommands for one entity, false if it's the entity parsecommands
	 * @return true if an end() has been hit, false otherwise
	 */
	public boolean resolveParseCommandForOneEntity ( EntityList posiblesObjetivos , String arguments , String fullArguments , boolean onWorld )
	{


		boolean ejecutado = false;

		Vector objetivos_s = ParserMethods.refersToEntityInRecursive ( arguments,posiblesObjetivos,false );
		Vector objetivos_p = ParserMethods.refersToEntityInRecursive ( arguments,posiblesObjetivos,true );
		
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

			//TODO probably remove this if so onContents is executed always and not only for, well, contents
			//removed. Replace this if if onContents is not to be executed on objects not contained in others
			//if ( objetivoVector.size() > 1 )
			//{

			Entity currentObject;
			for ( int i = objetivoVector.size()-1; i >= 0 ; i-- )
			{
				currentObject = (Entity) objetivoVector.get(i);

				//ejecutar parseCommandOnContents() de objeto
				if ( !onWorld && currentObject instanceof SupportingCode )
				{
					try
					{
						//parseCommandOnContents(Mobile,command,args,chain)
						ejecutado = ejecutado || ((SupportingCode)currentObject).execCode ( "parseCommandOnContents" , new Object[] { this , command , fullArguments , objetivoVector } );
					}
					catch ( bsh.TargetError te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContents(), command was " + command + fullArguments + ", entity " + currentObject + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejecutado )
					{
						try
						{

							ejecutado = ejecutado || ((SupportingCode)currentObject).execCode ( "parseCommandOnContentsGeneric" , new Object[] { this , command , fullArguments , "" , objetivoVector , null , currentObject , null , new Boolean(true) } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric(), command was " + command + fullArguments + ", entity number " + currentObject + ", second object was " + null + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
				}
				//lo mismo de mundo
				if ( onWorld )
				{
				    try
				    {
					ejecutado = ejecutado || mundo.execCode ( "parseCommandOnContents" , new Object[] { this , command , fullArguments  , objetivoVector , currentObject } );
				    }
				    catch ( bsh.TargetError te )
				    {
				    	write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContents() executed from world, command was " + command + fullArguments + ", entity " + currentObject + ", error was " + te + io.getColorCode("reset") );
				    	writeError(ExceptionPrinter.getExceptionReport(te));
				    }
				    if ( !ejecutado )
					{
						try
						{

							ejecutado = ejecutado || mundo.execCode ( "parseCommandOnContentsGeneric" , new Object[] { this , command , fullArguments , "" , objetivoVector , null , currentObject , null } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric() executed from world, command was " + command + fullArguments + ", entity number " + currentObject + ", second object was " + null + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
				}

			}

			//}


			Entity objetivo = (Entity) objetivoVector.get(0);

			//éste será el objeto principal sobre el que ejecutemos el comando en singular (cualquiera, aunque no sea BSH) si hace referencia a un solo objeto
			if ( objetivo instanceof Item )
				meterObjetoEnZRSingular((Item)objetivo);



			//THIS CHUNK OF CODE IS ABSOLUTE LEGACY
			//"LOS INMORTALES" IS THE ONLY REASON TO KEEP IT
			//para cada grupo de primeras palabras del string (dar, dar caña, dar caña al, dar caña al diábolo) intentamos ejecutar código EVA.
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
				if ( objetivoVector.size() == 1 || this.getPropertyValueAsBoolean("containedItemsInScope") )
				{
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommand" , new Object[] { this , command , fullArguments } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null , new Boolean(true) } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
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
        			    catch ( bsh.TargetError te )
        			    {
        			    	write(io.getColorCode("error") + "bsh.TargetError found at parseCommand() executed from world, command was " + command + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
        			    	writeError(ExceptionPrinter.getExceptionReport(te));
        			    }
			    }
			    if ( !ejecutado )
			    {
				try
				{
					ejecutado = ejecutado || mundo.execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null  } );
				}
				catch ( bsh.TargetError te )
				{
					write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric() executed from world, command was " + command + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
					writeError(ExceptionPrinter.getExceptionReport(te));
				}
			    }
			}

			if ( ejecutado ) //código hizo end()
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
			for ( int w = 0 ; w < objetivos_p.size() ; w++ )
			{

				//begin copy-pasted from singular
				Vector objetivoVector = (Vector) objetivos_p.get(w);

				if ( objetivoVector.size() > 1 )
				{

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
								catch ( bsh.TargetError te )
								{
									write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContents(), command was " + command + fullArguments + ", entity " + currentObject + ", error was " + te + io.getColorCode("reset") );
									writeError(ExceptionPrinter.getExceptionReport(te));
								}
							}
							if ( !ejecutado )
							{
								try
								{
									ejecutado = ejecutado || ((SupportingCode)currentObject).execCode ( "parseCommandOnContentsGeneric" , new Object[] { this , command , fullArguments , "" , objetivoVector , null  , currentObject , null , new Boolean(true)  } );
								}
								catch ( bsh.TargetError te )
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
								catch ( bsh.TargetError te )
								{
									write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContents() executed from world, command was " + command + fullArguments + ", entity " + currentObject + ", error was " + te + io.getColorCode("reset") );
									writeError(ExceptionPrinter.getExceptionReport(te));
								}
						    }
						    if ( !ejecutado )
						    {
							try
							{
								ejecutado = ejecutado || mundo.execCode ( "parseCommandOnContentsGeneric" , new Object[] { this , command , fullArguments , "" , objetivoVector , null  , currentObject , null } );
							}
							catch ( bsh.TargetError te )
							{
								write(io.getColorCode("error") + "bsh.TargetError found at parseCommandOnContentsGeneric() executed from world, command was " + command + fullArguments + ", entity number " + currentObject + ", second object was " + null + ", error was " + te + io.getColorCode("reset") );
								writeError(ExceptionPrinter.getExceptionReport(te));
							}
						    }
						}

					}

				}


				objetivo = (Entity) objetivoVector.get(0);
				//end copy-pasted from singular

				//añadimos a ZR plural.
				if ( objetivo instanceof Item )
					meterObjetoEnZRPlural((Item)objetivo);


				//THIS CHUNK OF CODE IS ABSOLUTE LEGACY
				//"LOS INMORTALES" IS THE ONLY REASON TO KEEP IT
				//para cada grupo de primeras palabras del string (dar, dar caña, dar caña al, dar caña al diábolo) intentamos ejecutar código EVA.
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

				//ahora vamos con el código BeanShell, más simple, simplemente ejecutamos la función parseCommand.
				if ( !onWorld && objetivo instanceof SupportingCode )
				{
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommand" , new Object[] { this , command , fullArguments } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
							writeError(ExceptionPrinter.getExceptionReport(te));
						}
					}
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null , new Boolean(true) } );
						}
						catch ( bsh.TargetError te )
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
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommand() executed from world, command was " + command + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
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




	/**
	 * @param objetivos_s
	 * @param objetivos_p
	 * @param arguments
	 * @return true if an end() has been hit, false otherwise
	 */
	public boolean resolveParseCommandForOneEntity_old ( EntityList posiblesObjetivos , String arguments , String fullArguments )
	{


		boolean ejecutado = false;

		Vector objetivos_s = ParserMethods.refersToEntityIn ( arguments,posiblesObjetivos,false );
		Vector objetivos_p = ParserMethods.refersToEntityIn ( arguments,posiblesObjetivos,true );

		//TODO
		//we can now migrate this to refersToEntityInRecursive. This returns a Vector of Vectors with paths to stuff.
		//we can call parseCommandOnContents on the intermediate nodes in the path and finally parseCommand on the final entity
		//(if the previous parseCommandOnContents have let us, of course).

		if ( objetivos_s.size() > 0 )
		{

			Entity objetivo = (Entity)objetivos_s.get(0);

			//éste será el objeto principal sobre el que ejecutemos el comando en singular (cualquiera, aunque no sea BSH) si hace referencia a un solo objeto
			if ( objetivo instanceof Item )
				meterObjetoEnZRSingular((Item)objetivo);



			//THIS CHUNK OF CODE IS ABSOLUTE LEGACY
			//"LOS INMORTALES" IS THE ONLY REASON TO KEEP IT
			//para cada grupo de primeras palabras del string (dar, dar caña, dar caña al, dar caña al diábolo) intentamos ejecutar código EVA.
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
				catch ( bsh.TargetError te )
				{
					write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
					writeError(ExceptionPrinter.getExceptionReport(te));
				}
				if ( !ejecutado )
				{
					try
					{
						ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null , new Boolean(true) } );
					}
					catch ( bsh.TargetError te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
				}
			}

			if ( ejecutado ) //código hizo end()
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

				//añadimos a ZR plural.
				if ( objetivo instanceof Item )
					meterObjetoEnZRPlural((Item)objetivo);


				//THIS CHUNK OF CODE IS ABSOLUTE LEGACY
				//"LOS INMORTALES" IS THE ONLY REASON TO KEEP IT
				//para cada grupo de primeras palabras del string (dar, dar caña, dar caña al, dar caña al diábolo) intentamos ejecutar código EVA.
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

				//ahora vamos con el código BeanShell, más simple, simplemente ejecutamos la función parseCommand.
				if ( objetivo instanceof SupportingCode )
				{
					try
					{
						ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommand" , new Object[] { this , command , fullArguments } );
					}
					catch ( bsh.TargetError te )
					{
						write(io.getColorCode("error") + "bsh.TargetError found at parseCommand(), command was " + command + fullArguments + ", item number " + objetivo.getID() + ", error was " + te + io.getColorCode("reset") );
						writeError(ExceptionPrinter.getExceptionReport(te));
					}
					if ( !ejecutado )
					{
						try
						{
							ejecutado = ejecutado || ((SupportingCode)objetivo).execCode ( "parseCommandGeneric" , new Object[] { this , command , fullArguments , "" , objetivo , null , new Boolean(true) } );
						}
						catch ( bsh.TargetError te )
						{
							write(io.getColorCode("error") + "bsh.TargetError found at parseCommandGeneric(), command was " + command + fullArguments + ", entity number " + objetivo + ", second object was " + objetivo + ", error was " + te + io.getColorCode("reset") );
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



	//nos dice "tal está a punto de atacarte, podrías bloquearlo... la espada de tal
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
						//ver si tiempo llegaría para bloquear

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
							toInform = "Seguramente te daría tiempo a bloquear el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						if ( inTimeProb >= 0.7 )
						{
							toInform = "Crees que te daría tiempo a bloquear el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						else if ( inTimeProb >= 0.5 )
						{
							toInform = "Es posible que te dé tiempo a bloquear el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						else if ( inTimeProb >= 0.3 )
						{
							toInform = "Será bastante difícil bloquear a tiempo el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						else if ( inTimeProb >= 0.1 )
						{
							toInform = "Será muy difícil bloquear a tiempo el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}
						else
						{
							toInform = "No crees que puedas bloquear a tiempo el golpe de $2 con " + w.constructName2OneItem(this) + ".";
						}

						habitacionActual.reportAction ( this , enemigo , null , null , toInform + "\n" , true );						

					} //end if weapon not null
				} //end for each weapon

				//ver si tiempo llegaría para esquivar

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
					toInform = "Seguramente podrías esquivar el ataque de $2 a tiempo."; 
				}
				else if ( dodgeInTimeProb >= 0.6 )
				{
					toInform = "Probablemente podrías esquivar a tiempo el ataque de $2.";
				}
				else if ( dodgeInTimeProb >= 0.4 )
				{
					toInform = enemigo.getCurrentWeapon().constructName2OneItem(this) + " de $2 está cerca, será difícil esquivar su ataque.";
				}
				else if ( dodgeInTimeProb >= 0.2 )
				{
					toInform = enemigo.getCurrentWeapon().constructName2OneItem(this) + " de $2 está casi encima , será muy difícil esquivar su ataque.";
				}
				else
				{
					toInform = enemigo.getCurrentWeapon().constructName2OneItem(this) + " de $2 está encima, no crees que puedas esquivar el ataque.";
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
						return; //no podemos dejar a todo el motor esperando si estamos en modo asíncrono y con más jugadores.
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

	//o más bien tener en cuenta para meter en zr cómo se refiere al objeto? (string ya dada)
	public void meterObjetoEnZRSingular ( Item obj )
	{
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

	public void resetZRPlural ( )
	{
		mentions.setLastMentionedObjectMP("");
		mentions.setLastMentionedObjectFP("");
		mentions.setLastMentionedObjectP("");
		/*
		ZR_objeto_masculino_plural = "";
		ZR_objeto_femenino_plural = "";
		ZR_objeto_plural = "";
		*/
	}

	//se van añadiendo a una lista con comas, cogible por comandos.
	public void meterObjetoEnZRPlural ( Item obj )
	{
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
				if ( nComillas % 2 == 0 || i == tokensYSeparadores.size()-1 ) //número par de comillas hasta aquí o último token
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



	//prácticamente igual a atacarBichoConArma() [copy-pasted, cambiando las comprobaciones de enemistad y la función attack por block]
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

			lastBlockWeapon =  (Weapon)(theVectors[1].elementAt(0)); //guardamos el arma que se usó por si en un ataque posterior no se especifica cuál usar
			lastBlockedEnemy = objetivo; //idem con el bicho

			block (  objetivo , (Weapon)(theVectors[1].elementAt(0)) );

		}

		else //no se especifica bicho y arma. Mirar si se especifica uno de ellos, al menos.
		{
			Vector patternMatchVectorSingBicho = ml.patternMatch ( arguments , false );
			Vector patternMatchVectorSingArma = i.patternMatch ( arguments , false );

			if ( patternMatchVectorSingBicho != null && patternMatchVectorSingBicho.size() > 0 )
			{
				Mobile objetivo = (Mobile) patternMatchVectorSingBicho.elementAt(0);
				Weapon usada = null;

				if ( lastAttackWeapon != null && wieldedWeapons.contains(lastAttackWeapon) )
				{
					usada = lastAttackWeapon;
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
				if ( lastAttackWeapon != null && wieldedWeapons.contains(lastAttackWeapon) )
				{
					usada = lastAttackWeapon;
				}
				else if ( wieldedWeapons != null && wieldedWeapons.size() > 0 )
				{
					usada = (Weapon) wieldedWeapons.elementAt(0);
				}
				if ( lastAttackedEnemy != null && habitacionActual.hasMobile ( lastAttackedEnemy ) )
				{
					objetivo = lastAttackedEnemy;
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
				addEnemy(objetivo);
			}
			if ( !objetivo.hasEnemy(this) )
			{
				objetivo.addEnemy(this);
			}

			lastAttackWeapon =  (Weapon)(theVectors[1].elementAt(0)); //guardamos el arma que se usó por si en un ataque posterior no se especifica cuál usar
			lastAttackedEnemy = objetivo; //idem con el bicho

			attack (  objetivo , (Weapon)(theVectors[1].elementAt(0)) );

			//escribir( io.getColorCode("action") + ((Item)(theVectors[0].elementAt(0))).unlock( (Item)(theVectors[1].elementAt(0)) ) + io.getColorCode("reset") + "\n"  );
		}

		else //no se especifica bicho y arma. Mirar si se especifica uno de ellos, al menos.
		{
			Vector patternMatchVectorSingBicho = ml.patternMatch ( arguments , false );
			Vector patternMatchVectorSingArma = i.patternMatch ( arguments , false );

			if ( patternMatchVectorSingBicho != null && patternMatchVectorSingBicho.size() > 0 )
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
					//como por aquí todavía teníamos inventarios paralelos, con nulos y cosas irregulares, hacemos algo extraño:
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
			else if ( patternMatchVectorSingArma != null && patternMatchVectorSingArma.size() > 0 )
			{
				Mobile objetivo = null;
				Weapon usada = (Weapon) patternMatchVectorSingArma.elementAt(0);

				if ( lastAttackedEnemy != null && habitacionActual.hasMobile ( lastAttackedEnemy ) )
				{
					objetivo = lastAttackedEnemy;
				}
				else if ( getEnemies() != null && getEnemies().size() > 0 && habitacionActual.hasMobile( getEnemies().elementAt(0) ) )
				{
					objetivo = getEnemies().elementAt(0);
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

				if ( lastAttackWeapon != null && usableWeapons.contains(lastAttackWeapon) )
				{
					usada = lastAttackWeapon;
				}
				else if ( usableWeapons != null && usableWeapons.size() > 0 )
				{
					//como por aquí todavía teníamos inventarios paralelos, con nulos y cosas irregulares, hacemos algo extraño:
					for ( int k = usableWeapons.size()-1 ; k >= 0 ; k-- )
					{
						if ( usableWeapons.elementAt(k) != null )
							usada = (Weapon) usableWeapons.elementAt(k);
					}
				}
				if ( lastAttackedEnemy != null && habitacionActual.hasMobile ( lastAttackedEnemy ) )
				{
					objetivo = lastAttackedEnemy;
				}
				else if ( getEnemies() != null && getEnemies().size() > 0 && habitacionActual.hasMobile( getEnemies().elementAt(0) ) )
				{
					objetivo = getEnemies().elementAt(0);
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
		Vector patternMatchVectorSing = null; //containers sólo en singular

		//Debug.println("Args: " + args + " Inv: " + inv );


		if ( inv != null && !inv.isEmpty() )
		{
			patternMatchVectorSing = inv.patternMatch( args , false ); //contenedor al fin
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
						//eliminar el nombre del contenedor... en el ejemplo, quitará el de armario al encontrar sentido a "pilas"
						//punto_division empieza en ntokens-2 porque uno nos lo cargamos fijo.
						for ( int punto_division = ntokens-1 ; punto_division >= 1 ; punto_division-- )
						{
							//Debug.println("Division: " +  StringMethods.getToks ( args , 1 , punto_division , ' ' ) );
							//Debug.println("mirado: " + mirado);
							mirado = mirado || cogerItem (  StringMethods.getToks ( args , 1 , punto_division , ' ' ) , ourContainer.getContents() , " de " + ourContainer.constructName2True ( 1 , this ) + infoString );
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
				infoString += " de ";
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
					infoString += " de ";
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
				infoString += " de ";
				infoString += inv.elementAt(i).constructName2True ( 1 , this );
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
					infoString += " de ";
					infoString += inv.elementAt(i).constructName2True ( 1 , this );
					mirado = cogerContenido ( inv.elementAt(i).getContents() , infoString );
					if ( mirado ) break;
					infoString = tempstring;
				}
			}
		}
		return mirado;
	}

	//mirar extra descriptions de items de habitación




	//coge un item del inventario inv.
	private boolean cogerItem ( Inventory inv , String extraInfo )
	{

		return cogerItem ( arguments , inv , extraInfo );

	} //end method


	public InputOutputClient getIO ( )
	{
		return io;
	}

	public Vector getFinalCommandLog() //para estadísticas
	{
		return finalExecutedCommandLog;
	}


	//Función que es llamada si el jugador es remoto (está asociado a un cliente remoto)
	//y quien lo maneja se desconecta. Hace todo lo necesario para manejar la desconexión
	//(id est, que la cosa dé el pego en el mundo)
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
			write("Hmm. ¿No estás conectado ya?\n");
			return;
		}

		setProperty ( "disconnected" , false );

		setNewState ( IDLE , 1 );

		Room enQueEstaba = mundo.getRoom ( getPropertyValueAsInteger("room") );

		enQueEstaba.addMob(this);

		getIO().write("Has sido añadido al mundo.\n");

		getRoom().reportActionAuto(this,null,"De repente, $1 aparece de la nada.\n",false); 

		write("Old player rejoined the game.\n");

		setIO ( io );

	}



	private boolean hacerHechizo ( EntityList possibleTargets , SpellList possibleSpells ) //uses "arguments" attr
	{

		if ( possibleTargets == null || possibleTargets.isEmpty() ) return false;	

		boolean mirado = false;			

		Vector[] patternMatchVectorSingSing = possibleSpells.patternMatchTwo ( possibleTargets , arguments , false , false ); //en singular y singular
		Vector[] patternMatchVectorSingPlur = possibleSpells.patternMatchTwo ( possibleTargets , arguments , false , true ); //en singular y plural
		Vector[] patternMatchVectorPlurSing = possibleSpells.patternMatchTwo ( possibleTargets , arguments , true , false ); //en plural y singular
		Vector[] patternMatchVectorPlurPlur = possibleSpells.patternMatchTwo ( possibleTargets , arguments , true , true ); //en plural y plural

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
			Vector patternMatchSpellOnly = possibleSpells.patternMatch ( arguments , false );
			if ( patternMatchSpellOnly != null && patternMatchSpellOnly.size() > 0 )
			{
				mirado = true;
				cast ( (Spell) patternMatchSpellOnly.elementAt(0) , null );
			}
		}

		return mirado;

	} //end method hacer hechizo




	//parsea y ejecuta una acción que referencia a un objetivo

	//returns: true si se ha encontrado objetivo; aunque la acción no se pudiese ejecutar
	//			false si no se han encontrado objetivos. Habrá que dar un mensaje "¿qué pretendes Xar?"
	//maneja la ZR.
	public boolean oneTargetAction ( String action , String arguments , EntityList posiblesObjetivos )
	{
		Vector patternMatchVectorSing = new Vector();
		Vector patternMatchVectorPlur = new Vector();
		if ( posiblesObjetivos != null  )
		{
			patternMatchVectorSing = posiblesObjetivos.patternMatch ( arguments , false ); //en singular
			patternMatchVectorPlur = posiblesObjetivos.patternMatch ( arguments , true ); //en plural
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
					return true;
				}
			}

			//setNewState( 1 /*IDLE*/, 5 ); //action does it
			mentions.setLastMentionedVerb(command);
			return true;
		}
		else			
		{
			//jugador.escribirDenegacionComando( io.getColorCode("denial") + "¿Qué pretendes quitarte?\n" + io.getColorCode("reset") );
			mentions.setLastMentionedVerb(command);
			cancelPending();
			return false;		
		}


	}



}




