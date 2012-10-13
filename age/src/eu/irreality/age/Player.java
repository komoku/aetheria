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
import eu.irreality.age.util.VersionComparator;
/**
 * Clase del personaje, jugador.
 *
 */
public class Player extends Mobile implements Informador
{

	//INSTANCE VARIABLES

	/**¿Estamos cargando de un log?*/
	protected boolean from_log;
	protected Vector logfile; //log de ENTRADA

	BufferedReader logReader;	


	//sólo a efectos de estadísticas, ya sale todo sustituido.
	Vector finalExecutedCommandLog = new Vector();

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


	void initDefaultProperties( World mundo )
	{
		//set multiple args matches which was default behaviour in versions [1.0,1.1.7]
		if ( new VersionComparator().compare(mundo.getParserVersion(),"1.0") >= 0 && new VersionComparator().compare(mundo.getParserVersion(),"1.1.7") <= 0 )
		{
			if ( this.getPropertyValueAsObject("multipleArgsMatches") == null )
				setProperty("multipleArgsMatches",true);
		}
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
			actionArgs = new Object[1]; //en concreto, será un Path.

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

					//intentar mirar extras de bichos de la habitación
					
					Debug.println("Mirado="+mirado);
					
					if ( !mirado ) mirado = mirarExtrasBichos ( arguments , this.getRoom().getMobiles() );
					
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
			//TODO: This case is very doubtful in multilanguage model, should probably be either removed or made optional (issue #244)
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
			Mobile whomToSayItTo = (Mobile) ParserMethods.refersToEntityIn(command, this.getAllWorldMobiles(), false).toEntityVector().get(0);
			return execCommand("decir a " + whomToSayItTo.getBestReferenceName(false) + "\"" + whatToSay + "\"");
		}

		//Si llegamos aquí, no sólo no es un verbo sino que no es una construcción tipo "María, hola".
		//Ahora, si no lo hemos hecho ya, probamos
		//a ver si es que ha habido una elipsis de verbo, añadiendo el verbo de la zona
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

		getClient().write("Has sido añadido al mundo.\n");

		getRoom().reportActionAuto(this,null,"De repente, $1 aparece de la nada.\n",false); 

		write("Old player rejoined the game.\n");

		setIO ( io );

	}

}




