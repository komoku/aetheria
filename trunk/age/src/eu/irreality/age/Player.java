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
import eu.irreality.age.scripting.ScriptException;
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


	public void setPlayerName( String nombre )
	{
		//Función de conveniencia para poner nombre a un jugador, que sirve como
		//title, reference name, etc.

		title = nombre;
		properName = true;

		//max priority as reference
		respondToSing.add(0,nombre);
		respondToPlur.add(0,nombre);
		
		//respondToSing = nombre+"$"+respondToSing;
		//respondToPlur = nombre+"$"+respondToPlur;

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
				catch ( ScriptException bshte )
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


	
	
	/**
	 * Obtains a command from the client linked to a Player, putting it into the commandstring attribute.
	 * Returns false if no command was obtained due to client having disconnected.
	 * @return
	 */
	private boolean obtainCommandFromClient()
	{
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
					return false;
				}
			}
		}
		else
		{
			commandstring = io.getInput(this);
			if ( commandstring == null && io.isDisconnected() )
			{
				disconnect();
				return false;
			}
		}
		return true;
	}
	
	
		
	/**
	 * Obtains an executes a command.
	 * The command can be obtained:
	 * - From the command queue,
	 * - From the "forced command" string,
	 * - From a log if we're executing a log,
	 * - From client input if none of the above apply.
	 */
	synchronized public boolean obtainAndExecCommand ( World mundo ) throws java.io.IOException
	{

		/*Pueden darse dos casos:
		- Que hayan quedado comandos pendientes de ejecución de otra vez: estarán
		en la cola de comandos pendientes.
		- Que no: esperamos una entrada por la editbox, que cambiará la variable
		"commandstring" poniendo el nuevo comando y despertará el thread del wait.
		 */		

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
			else if ( from_log )
			{
				String newCommand = logReader.readLine();
				if ( newCommand == null )
				{
					from_log = false;
					mundo.endOfLog();
					return obtainAndExecCommand(mundo);
				}		
				else
				{
					io.forceInput ( newCommand , true );
					commandstring = newCommand;
				}
			}
			else
			{
				//obtain command from player input
				//in asynchronous mode, we get it via a nonblocking call
				//in synchronous mode, we get it via a blocking call (we wait for input)

				if ( !obtainCommandFromClient() ) return true; //true bc. if player disconnected, we return true

			}

			//Process raw command:
			
			/*Preparación del comando:*/
			if ( commandstring != null ) commandstring = commandstring.trim();
			
			/*Llamada a preprocessCommand() configurable*/
			if ( runPreprocessCommand() ) return true;

			//comando nulo
			if ( commandstring == null || commandstring.equals("") ) return false;

			/*Comandos eval - false porque no se ejecuta un comando normal, es un metacomando de fuera del mundo*/
			if ( runEvalIfApplicable() ) return false;
			
			/*Separamos las subfrases*/	
			if ( !separateSentences() ) return false;

		}

		//modular execCommand()

		if ( commandstring.isEmpty() ) return false; //empty strings can result if, for example, input was ",something", etc.
		
		return execCommand ( commandstring  );

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
			catch ( ScriptException bshte )
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
			catch ( ScriptException bshte )
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
					catch ( ScriptException bshte )
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
			return;	

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
		if ( ! obtainAndExecCommand ( mundo ) )
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




