/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;
public class CodeRunner
{

	/*This is an EVA code runner only!*/

	private final int INIT = 0;
	private final int DATA = 1;
	private final int CODE = 2;
	private final int NONE = -1;
	
	private final int MAXSEGMENT = 1024;
	//tamaño máximo (instrucciones/etiquetas memoria, o líneas en general)
	//que puede tener un segmento
		
		//los registros del mico
		private String[] r = new String[100];
		private String exc = new String();
		private String obj = new String();
		private String[] a = new String[10];
		private String[] v = new String[10];
		private String ra;
		
		private int program_counter;
		
	private String[][] segmento = new String[3][];
	
	int[] cont = {0,0,0}; //longitud de segmentos init, data y code
		
	private World ourWorld;
	
	//la pila para el ensamblador
	private Stack asmStack;
		
	
	//pasará a true si se hace un "continue", es decir, el código termina; pero la ejecución
	//del comando ha de continuar como si el comando EVA hubiese fallado (no ejecutado, seguir
	//parseando)
	boolean continue_flag = false;
		
		
	public CodeRunner ( String theCode , World theWorld ) throws EVASyntaxException
	{
		ourWorld = theWorld;
		StringTokenizer lineas = new StringTokenizer ( theCode, "\n" );
		//nos curamos en salud dandole capacidad MAXSEGMENT al segmento
		//ergo, no se admiten mas de MAXSEGMENT instrucciones, mem, etc. 
		segmento = new String [3] [MAXSEGMENT];
		int cursegment = NONE;
		//organizar codigo en segmentos
		while ( lineas.hasMoreTokens() )
		{
			String lineaActual = lineas.nextToken();
			
			StringTokenizer palabrasEnLinea = new StringTokenizer ( lineaActual , " " );
			
			if ( palabrasEnLinea.hasMoreTokens() )
			{
				String primeraPalabra = palabrasEnLinea.nextToken().trim();
				if ( primeraPalabra.equalsIgnoreCase(".INIT") )
				{
					cursegment = INIT; //cambiamos de segmento
				}
				else if ( primeraPalabra.equalsIgnoreCase(".DATA") )
				{
					cursegment = DATA;
				}
				else if ( primeraPalabra.equalsIgnoreCase(".CODE") )
				{
					cursegment = CODE;
				}
				else if ( cursegment == NONE )
				{
					throw ( new EVASyntaxException ( "Lines of code without a segment" ) );
					//Debug.println("ERROR: No segment\n");
				}
				else
				{
					segmento[cursegment][cont[cursegment]] = lineaActual;
					//añadimos la linea al segmento en que estamos
					cont[cursegment]++;
				}
			}
		}
		//Debug.println("Code loaded.");
		//Debug.println("Init segment lines: " + cont[INIT] );
		//Debug.println("Data segment lines: " + cont[DATA] );
		//Debug.println("Code segment lines: " + cont[CODE] );
		//ya tenemos segment[CODE][i] linea i del segmento codigo, lo mismo para los otros	
	}
		
	//true si existe la rutina
	public boolean runCode ( String theCode , String routine , String dataSegment ) throws EVASyntaxException , EVASemanticException
	{
		addToDataSegment ( dataSegment );
		return runCode ( theCode , routine );	
	}

	/*prec: s escrito en EVA (Ens. Virt. Aetheria)*/
	public boolean runCode ( String theCode , String routine ) throws EVASyntaxException , EVASemanticException
	{
		//Debug.println("Going to run code.");
		//Debug.println("Routine = " + routine );
		
		//ejecutar el segmento init (estados iniciales)
		int curlimit = cont[INIT]; //longitud de init
		for ( int i = 0 ; i < curlimit ; i++ )
		{
			StringTokenizer initTokenizer = new StringTokenizer( segmento[INIT][i] , ":" );
			if ( initTokenizer.countTokens() < 2 )
			{
				throw( new EVASyntaxException(
				"Too few tokens with : at INIT segment, line " + i + " (" + segmento[INIT][i] + ")"  ) );
			}
			else
			{
				String reg = initTokenizer.nextToken().trim();
				String val = initTokenizer.nextToken().trim();
			}
		}
		
		//Debug.println("Initialization done.");
		
		program_counter = getLine ( routine , CODE );
		
		//Debug.println("PC = " + program_counter );
		
		if ( program_counter == -1 )
		{
			return false;
			//no ejecutado
		}
		else
		{
			while ( runNextInstruction() ) ;
			if ( !continue_flag )
				return true; //codigo ejecutado
			else
			{
				continue_flag = false;
				return false; //esto es si encontramos un continue, devolvemos "false" como si
				//no hubieramos ejecutado el código para que el parser siga ejecutando el comando.
			}
		}		
	}
	
	private boolean runNextInstruction() throws EVASyntaxException , EVASemanticException
	{
		String instruccion = segmento[CODE][program_counter];
		StringTokenizer st = new StringTokenizer( instruccion, " \t," );
		if ( st.hasMoreTokens() )
		{
			//no es una nop
			String codop = st.nextToken().trim();
			
			
			if ( codop.charAt( codop.length() - 1 ) == ':' ) //tiene una etiqueta
			{
				if ( st.hasMoreTokens() )
					codop = st.nextToken().trim(); //pasamos al siguiente
				else codop = "nop";
			}
			
			if ( codop.equalsIgnoreCase ( "end" ) )
			{
				return false;
			}
			else if ( codop.equalsIgnoreCase ( "continue" ) || codop.equalsIgnoreCase ( "resume" ) )
			{
				continue_flag = true;
				return false;
			}
			else if ( codop.equalsIgnoreCase ( "nop" ) || codop.equalsIgnoreCase ( "noop" ) || codop.equalsIgnoreCase ( "skip" ) )
			{
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "ld" ) || codop.equalsIgnoreCase ( "load" ) )
			{
				if ( st.countTokens() < 2 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String reg = st.nextToken().trim();
					String label = st.nextToken().trim();
					try
					{
						setReg ( reg , getMem ( label ) );
					}
					catch ( EVASyntaxException e )
					{
						throw ( new EVASyntaxException (
						e.getMessage() + ", line " + program_counter ) );
					}
				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "li" ) || codop.equalsIgnoreCase ( "loadi" ) )
			{
				if ( st.countTokens() < 2 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String reg = st.nextToken().trim();
					String val = "";
					//StringTokenizer st2 = new StringTokenizer( instruccion, " \t," , true ); //counting separators
					//st2.nextToken(); //codop
					//st2.nextToken(); //separator
					//st2.nextToken(); //reg
					//st2.nextToken(); //separator
					while ( true ) //coger los tokens pero con un sólo caracter para no alterar el string
					{
						try
						{
							val += st.nextToken("ç");
							val += "ç";
						}
						catch ( NoSuchElementException e )
						{
							break;
						}
					}
					//quitar la última cedilla añadida
						val = val.substring(0,val.length()-1);
					//quitar posibles delimitadores que hayan quedado al principio de val
						while ( val.charAt(0) == ',' || val.charAt(0) == ' ' || val.charAt(0) == '\t' )
						{
							val = val.substring(1);
						}
					try
					{
						setReg ( reg , val );
					}
					catch ( EVASyntaxException e )
					{
						throw ( new EVASyntaxException (
						e.getMessage() + ", line " + program_counter ) );
					}
				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "sd" ) || codop.equalsIgnoreCase ( "store" ) || codop.equalsIgnoreCase ( "stor" ) )
			{
				if ( st.countTokens() < 2 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String reg = st.nextToken().trim();
					String label = st.nextToken().trim();
					try
					{
						setMem ( label , getReg ( reg ) );
					}
					catch ( EVASyntaxException e )
					{
						throw ( new EVASyntaxException (
						e.getMessage() + ", line " + program_counter ) );
					}
				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "mov" ) || codop.equalsIgnoreCase ( "mv" ) || codop.equalsIgnoreCase ( "move" ) )
			{
				if ( st.countTokens() < 2 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String rt = st.nextToken().trim();
					String rs = st.nextToken().trim();
					try
					{
						setReg ( rt , getReg ( rs ) );
					}
					catch ( EVASyntaxException e )
					{
						throw ( new EVASyntaxException (
						e.getMessage() + ", line " + program_counter ) );
					}
				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "add" ) || codop.equalsIgnoreCase ( "sub" ) || codop.equalsIgnoreCase ( "mult" ) || codop.equalsIgnoreCase ( "mul" ) || codop.equalsIgnoreCase ( "div" ) )
			{
				if ( st.countTokens() < 3 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String rd = st.nextToken().trim();
					String rs = st.nextToken().trim();
					String rt = st.nextToken().trim();
					int operando1,operando2;
					try
					{
						operando1 = Integer.valueOf( getReg(rs) ).intValue();
						operando2 = Integer.valueOf( getReg(rt) ).intValue();
						try
						{
							if ( codop.equalsIgnoreCase("add") )
								setReg ( rd , String.valueOf(operando1+operando2) );
							if ( codop.equalsIgnoreCase("sub") )
								setReg ( rd , String.valueOf(operando1-operando2) );
							if ( codop.equalsIgnoreCase("mul") || codop.equalsIgnoreCase("mult") )
								setReg ( rd , String.valueOf(operando1*operando2) );
							if ( codop.equalsIgnoreCase("div") )
							{
								setReg ( rd , String.valueOf(operando1/operando2) );	
								if ( operando2 == 0 )
								{
									setReg ( "$Exc" , "DivisionByZero" );
									setReg ( rd , "0" );
								}
							}
						}
						catch ( EVASyntaxException e )
						{
							throw ( new EVASyntaxException (
							e.getMessage() + ", line " + program_counter ) );
						}
					}
					catch ( NumberFormatException en )
					{
						setReg ( "$Exc" , "NotANumber" );
						setReg ( rd , "0" ); 	
					}

				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "and" ) || codop.equalsIgnoreCase ( "or" ) )
			{
				if ( st.countTokens() < 3 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String rd = st.nextToken().trim();
					String rs = st.nextToken().trim();
					String rt = st.nextToken().trim();
					int operando1,operando2;
					try
					{
						operando1 = Integer.valueOf( getReg(rs) ).intValue();
						operando2 = Integer.valueOf( getReg(rt) ).intValue();
						try
						{
							if ( codop.equalsIgnoreCase("and") )
								setReg ( rd , String.valueOf(operando1&operando2) );
							if ( codop.equalsIgnoreCase("or") )
								setReg ( rd , String.valueOf(operando1|operando2) );
						}
						catch ( EVASyntaxException e )
						{
							throw ( new EVASyntaxException (
							e.getMessage() + ", line " + program_counter ) );
						}
					}
					catch ( NumberFormatException en )
					{
						setReg ( "$Exc" , "NotANumber" );
						setReg ( rd , "0" ); 	
					}

				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "fadd" ) || codop.equalsIgnoreCase ( "fsub" ) || codop.equalsIgnoreCase ( "fmult" ) || codop.equalsIgnoreCase ( "fmul" ) || codop.equalsIgnoreCase ( "fdiv" ) )
			{
				if ( st.countTokens() < 3 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String rd = st.nextToken().trim();
					String rs = st.nextToken().trim();
					String rt = st.nextToken().trim();
					double operando1,operando2;
					try
					{
						operando1 = Double.valueOf( getReg(rs) ).doubleValue();
						operando2 = Double.valueOf( getReg(rt) ).doubleValue();
						try
						{
							if ( codop.equalsIgnoreCase("fadd") )
								setReg ( rd , String.valueOf(operando1+operando2) );
							if ( codop.equalsIgnoreCase("fsub") )
								setReg ( rd , String.valueOf(operando1-operando2) );
							if ( codop.equalsIgnoreCase("fmul") || codop.equalsIgnoreCase("fmult") )
								setReg ( rd , String.valueOf(operando1*operando2) );
							if ( codop.equalsIgnoreCase("fdiv") )
							{
								setReg ( rd , String.valueOf(operando1/operando2) );	
								if ( operando2 == 0.0 )
								{
									setReg ( "$Exc" , "DivisionByZero" );
									setReg ( rd , "0" );
								}
							}
						}
						catch ( EVASyntaxException e )
						{
							throw ( new EVASyntaxException (
							e.getMessage() + ", line " + program_counter ) );
						}
					}
					catch ( NumberFormatException en )
					{
						setReg ( "$Exc" , "NotANumber" );
						setReg ( rd , "0" ); 	
					}

				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "addi" ) || codop.equalsIgnoreCase ( "subi" ) || codop.equalsIgnoreCase ( "multi" ) || codop.equalsIgnoreCase ( "muli" ) || codop.equalsIgnoreCase ( "divi" ) )
			{
				if ( st.countTokens() < 3 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String rd = st.nextToken().trim();
					String rs = st.nextToken().trim();
					String imm = st.nextToken().trim();
					int operando1,operando2;
					try
					{
						operando1 = Integer.valueOf( getReg(rs) ).intValue();
						operando2 = Integer.valueOf( imm ).intValue();
						try
						{
							if ( codop.equalsIgnoreCase("addi") )
								setReg ( rd , String.valueOf(operando1+operando2) );
							if ( codop.equalsIgnoreCase("subi") )
								setReg ( rd , String.valueOf(operando1-operando2) );
							if ( codop.equalsIgnoreCase("muli") || codop.equalsIgnoreCase("multi") )
								setReg ( rd , String.valueOf(operando1*operando2) );
							if ( codop.equalsIgnoreCase("divi") )
							{
								setReg ( rd , String.valueOf(operando1/operando2) );	
								if ( operando2 == 0 )
								{
									setReg ( "$Exc" , "DivisionByZero" );
									setReg ( rd , "0" );
								}
							}
						}
						catch ( EVASyntaxException e )
						{
							throw ( new EVASyntaxException (
							e.getMessage() + ", line " + program_counter ) );
						}
					}
					catch ( NumberFormatException en )
					{
						setReg ( "$Exc" , "NotANumber" );
						setReg ( rd , "0" ); 	
					}

				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "faddi" ) || codop.equalsIgnoreCase ( "fsubi" ) || codop.equalsIgnoreCase ( "fmulti" ) || codop.equalsIgnoreCase ( "fmuli" ) || codop.equalsIgnoreCase ( "fdivi" ) )
			{
				if ( st.countTokens() < 3 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String rd = st.nextToken().trim();
					String rs = st.nextToken().trim();
					String imm = st.nextToken().trim();
					double operando1,operando2;
					try
					{
						operando1 = Double.valueOf( getReg(rs) ).doubleValue();
						operando2 = Double.valueOf( imm ).doubleValue();
						try
						{
							if ( codop.equalsIgnoreCase("faddi") )
								setReg ( rd , String.valueOf(operando1+operando2) );
							if ( codop.equalsIgnoreCase("fsubi") )
								setReg ( rd , String.valueOf(operando1-operando2) );
							if ( codop.equalsIgnoreCase("fmuli") || codop.equalsIgnoreCase("fmulti") )
								setReg ( rd , String.valueOf(operando1*operando2) );
							if ( codop.equalsIgnoreCase("fdivi") )
							{
								setReg ( rd , String.valueOf(operando1/operando2) );	
								if ( operando2 == 0.0 )
								{
									setReg ( "$Exc" , "DivisionByZero" );
									setReg ( rd , "0" );
								}
							}
						}
						catch ( EVASyntaxException e )
						{
							throw ( new EVASyntaxException (
							e.getMessage() + ", line " + program_counter ) );
						}
					}
					catch ( NumberFormatException en )
					{
						setReg ( "$Exc" , "NotANumber" );
						setReg ( rd , "0" ); 	
					}

				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "bne" ) || codop.equalsIgnoreCase ( "beq" ) || codop.equalsIgnoreCase ( "bge" ) || codop.equalsIgnoreCase ( "bgt" ) || codop.equalsIgnoreCase ( "ble" ) || codop.equalsIgnoreCase ( "blt" ) )
			{
				if ( st.countTokens() < 3 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String r1 = st.nextToken().trim();
					String r2 = st.nextToken().trim();
					String label = st.nextToken().trim();
					int operando1,operando2;
					boolean condition = false;
					try
					{
						operando1 = Integer.valueOf( getReg(r1) ).intValue();
						operando2 = Integer.valueOf( getReg(r2) ).intValue();
							if ( codop.equalsIgnoreCase("bne") )
								condition = ( operando1 != operando2 );
							else if ( codop.equalsIgnoreCase("beq") )
								condition = ( operando1 == operando2 );
							else if ( codop.equalsIgnoreCase("bge") )
								condition = ( operando1 >= operando2 );
							else if ( codop.equalsIgnoreCase("bgt") )
								condition = ( operando1 > operando2 );
							else if ( codop.equalsIgnoreCase("ble") )
								condition = ( operando1 <= operando2 );
							else if ( codop.equalsIgnoreCase("blt") )
								condition = ( operando1 < operando2 );
					}
					catch ( NumberFormatException en )
					{
						//no son numeros. hacer igualdad de strings.
						if ( codop.equalsIgnoreCase("beq") )
							condition = getReg(r1).equals(getReg(r2));
						else if ( codop.equalsIgnoreCase("bne") )
							condition = !getReg(r1).equals(getReg(r2));	
						//no comparamos strings de superioridad o inferioridad
						else
						{
							setReg ( "$Exc" , "NotANumber" );
							condition = false;
						}
					}
					//hacer el salto
					if ( condition )
					{
						int direccion_salto = getLine ( label, CODE );
						if ( direccion_salto < 0 )
						{
							program_counter++;
							throw ( new EVASyntaxException (
							"Jump address not found, code line " + program_counter ) );
						}
						program_counter = direccion_salto;
					}
					else program_counter++;
				}

			}
			else if ( codop.equalsIgnoreCase ( "j" ) || codop.equalsIgnoreCase ( "goto" ) )
			{
				if ( !st.hasMoreTokens() ) 
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String label = st.nextToken();
					int direccion_salto = getLine ( label, CODE );
					if ( direccion_salto < 0 )
					{
						program_counter++;
						throw ( new EVASyntaxException (
						"Jump address not found, code line " + program_counter ) );
					}
					program_counter = direccion_salto;
				}
			}
			else if ( codop.equalsIgnoreCase ( "jal" ) )
			{
				if ( !st.hasMoreTokens() ) 
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String label = st.nextToken();
					int direccion_salto = getLine ( label, CODE );
					if ( direccion_salto < 0 )
					{
						program_counter++;
						throw ( new EVASyntaxException (
						"Jump address not found, code line " + program_counter ) );
					}		
					setReg ( "$ra" , String.valueOf(program_counter) );
					program_counter = direccion_salto;
				}
			}
			else if ( codop.equalsIgnoreCase ( "return" ) )
			{
				int direccion_salto;
				try
				{
					direccion_salto = Integer.valueOf(getReg ( "$ra" )).intValue();
				}
				catch ( NumberFormatException nf )
				{
					throw ( new EVASyntaxException( "Invalid call to return, code line " + program_counter ) );
				}
				program_counter = direccion_salto;
			}
			else if ( codop.equalsIgnoreCase ( "push" ) )
			{
				if ( st.countTokens() < 1 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String reg = st.nextToken().trim();
					String content;
					try
					{
						content = getReg ( reg );
					}
					catch ( EVASyntaxException e )
					{
						throw ( new EVASyntaxException (
						e.getMessage() + ", line " + program_counter ) );
					}
					asmStack.push(content);
				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "pop" ) )
			{
				if ( st.countTokens() < 1 )
				{
					throw ( new EVASyntaxException ( "Invalid instruction format, code line "+program_counter ) );
				}
				else
				{
					String reg = st.nextToken().trim();
					String content = (String)asmStack.pop();
					try
					{
						setReg ( reg , content );
					}
					catch ( EVASyntaxException e )
					{
						throw ( new EVASyntaxException (
						e.getMessage() + ", line " + program_counter ) );
					}
				}
				program_counter++;
			}
			else if ( codop.equalsIgnoreCase ( "exc" ) || codop.equalsIgnoreCase( "exception" ) || codop.equalsIgnoreCase( "throw" ) )
			{
				throw ( new EVASemanticException ( exc , program_counter ) );
			}
			else if ( codop.equalsIgnoreCase ( "fun" ) || codop.equalsIgnoreCase( "function" ) )
			{
				String instructionParameters = "";
				while ( st.hasMoreTokens() )
					instructionParameters = st.nextToken();
				try
				{
					execFunction ( instructionParameters );
				}	
				catch ( EVASyntaxException e )
				{
					throw ( new EVASyntaxException (
						e.getMessage() + ", line " + program_counter ) );
				}
				program_counter++;
			}
			else
			{
				throw ( new EVASyntaxException ( "Unidentified instruction " + "[" + codop + "]" + ", code line " + program_counter ) );
			}

			
			
		}
		
		//ejecutar la instruccion indicada por el PC.
		//codificarlórl.
		//end? return false.
		return true; //sí, hay más instrucciones.
	}

	private void setReg ( String reg , String val ) throws EVASyntaxException
	{
		String actual = reg;
		
		//Debug.println("Setting reg: " + actual );
		
		if ( reg.charAt(0) != '$' )
		{
			throw ( new EVASyntaxException (
			"Register must start with dollar sign" ) );
		}
		else
		{
			//quitar el dolar
			actual = actual.substring(1,actual.length());
		}
		
		//Debug.println("Setting reg: " + actual );
		
		if ( actual.substring(0,2).equalsIgnoreCase("ra") )
		{
			ra = val;
		}
		else if ( actual.charAt(0) == 'r' )
		{
			//quitar la r
			actual = actual.substring(1,actual.length());
			//a ve si es numero
			int regnum;
			try
			{
				regnum = Integer.valueOf(actual).intValue();
				r[regnum] = val;
			}
			catch ( NumberFormatException NumExc )
			{
				throw( new EVASyntaxException (
					"Wrong register format." ) );
			}
		}
		else if ( actual.charAt(0) == 'v' )
		{
			//quitar la v
			actual = actual.substring(1,actual.length());
			//a ve si es numero
			int regnum;
			try
			{
				regnum = Integer.valueOf(actual).intValue();
				v[regnum] = val;
			}
			catch ( NumberFormatException NumExc )
			{
				throw( new EVASyntaxException("Wrong register format"));
			}
		}
		else if ( actual.charAt(0) == 'a' )
		{
			//quitar la a
			actual = actual.substring(1,actual.length());
			
			//a ve si es numero
			int regnum;
			try
			{
				regnum = Integer.valueOf(actual).intValue();
				a[regnum] = val;
			}
			catch ( NumberFormatException NumExc )
			{
				throw( new EVASyntaxException("Wrong register format"));
			}
		}
		else if ( actual.substring(0,3).equalsIgnoreCase("exc") )
		{
			exc = val;
		}
		else if ( actual.substring(0,3).equalsIgnoreCase("obj") )
		{
			obj = val;
		}
		else
		{
			throw( new EVASyntaxException("Wrong register format"));
		}
		
	}
	
	//devuelve el valor en el registro reg
	private String getReg ( String reg ) throws EVASyntaxException
	{
		String actual = reg;
		if ( reg.charAt(0) != '$' )
		{
						throw ( new EVASyntaxException (
			"Register must start with dollar sign" ) );
		}
		else
		{
			//quitar el dolar
			actual = actual.substring(1,actual.length());
		}
		if ( actual.substring(0,2).equalsIgnoreCase("ra") )
		{
			return ra;
		}
		else if ( actual.charAt(0) == 'r' )
		{
			//quitar la r
			actual = actual.substring(1,actual.length());
			//a ve si es numero
			int regnum;
			try
			{
				regnum = Integer.valueOf(actual).intValue();
				return r[regnum];
			}
			catch ( NumberFormatException NumExc )
			{
				throw( new EVASyntaxException("Wrong register format"));
			}
		}
		else if ( actual.charAt(0) == 'v' )
		{
			//quitar la v
			actual = actual.substring(1,actual.length());
			//a ve si es numero
			int regnum;
			try
			{
				regnum = Integer.valueOf(actual).intValue();
				return v[regnum];
			}
			catch ( NumberFormatException NumExc )
			{
				throw( new EVASyntaxException("Wrong register format"));
			}
		}
		else if ( actual.charAt(0) == 'a' )
		{
			//quitar la r
			actual = actual.substring(1,actual.length());
			//a ve si es numero
			int regnum;
			try
			{
				regnum = Integer.valueOf(actual).intValue();
				return a[regnum];
			}
			catch ( NumberFormatException NumExc )
			{
				throw( new EVASyntaxException("Wrong register format"));
			}
		}
		else if ( actual.substring(0,3).equalsIgnoreCase("exc") )
		{
			return exc;
		}
		else if ( actual.substring(0,3).equalsIgnoreCase("obj") )
		{
			return obj;
		}
		else
		{
			throw( new EVASyntaxException("Wrong register format"));
		}
	}
	
	//busca etiqueta en code/data segment.
	private int getLine ( String toSearch , int elsegmento )
	{
		for ( int i = 0 ; i < cont[elsegmento] ; i++ )
		{
			StringTokenizer st = new StringTokenizer ( segmento[elsegmento][i],":" );
			if ( st.countTokens() > 1 )
			{
				String label = st.nextToken().trim();
				if ( label.equalsIgnoreCase(toSearch) ) return i;
			}
		}
		return -1;
	}
	
	private String getMem ( String toSearch ) throws EVASyntaxException
	{
		int l = getLine ( toSearch , DATA );
		if ( l < 0 )
			throw ( new EVASyntaxException("Line "+toSearch+" doesn't exist in data segment") );
		else
		{
			StringTokenizer st = new StringTokenizer ( segmento[DATA][l],":" );
			if ( st.countTokens() > 1 )
			{
				st.nextToken();
				return ( st.nextToken().trim() );
			}
			else return "";
		}		
	}
	
	private void setMem ( String toSearch , String value ) throws EVASyntaxException
	{
		int l = getLine ( toSearch , DATA );
		if ( l < 0 )
			throw ( new EVASyntaxException("Line "+toSearch+" doesn't exist in data segment") );
		else
		{
			StringTokenizer st = new StringTokenizer ( segmento[DATA][l],":" );
			segmento[DATA][l] = st.nextToken() + ": " + value.trim();
		}
	}
	
	//para el "add to data segment".
	//admite direcciones de memoria inexistentes, las crea.
	//si la direccion existe, sin embargo, la sobreescribe.
	private void addMem ( String toSearch , String value )
	{
		int l = getLine ( toSearch , DATA );
		if ( l >= 0 )
		{
			StringTokenizer st = new StringTokenizer ( segmento[DATA][l],":" );
			segmento[DATA][l] = st.nextToken() + ": " + value.trim();
		}
		else
		{
			segmento[DATA][cont[DATA]] = toSearch.trim() + ": " + value.trim();
			cont[DATA]++;
		}
	}
	
	private void addToDataSegment ( String stringToAdd )
	{
		StringTokenizer st = new StringTokenizer(stringToAdd,"\n");
		while ( st.hasMoreTokens() )
		{
			StringTokenizer linest = new StringTokenizer ( st.nextToken(),":" );
			String label = linest.nextToken();
			String content = "";
			while ( linest.hasMoreTokens() )
			{
				content += linest.nextToken();
			}
			addMem ( label , content );
		}
	}
	
	private void execFunction ( String functionToExec ) throws EVASyntaxException
	{
		//static function escribir
		if ( functionToExec.equalsIgnoreCase("escribir") )
		{
			ourWorld.write ( getReg( "$a0" )  );
		}
		else if ( functionToExec.equalsIgnoreCase("newline") )
		{
			ourWorld.write ("\n");
		}
		else if ( functionToExec.equalsIgnoreCase("getstate") )
		{
			int objid;
			try
			{
				objid = Integer.valueOf( getReg("$Obj") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Entity ourObject = ourWorld.getObject ( objid );
			if ( ourObject == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				setReg ( "$v0" , String.valueOf ( ourObject.getState() ) );
			}
		}
		else if ( functionToExec.equalsIgnoreCase("setstate") )
		{
			int objid;
			int newstate;
			long newtuleft;
			try
			{
				objid = Integer.valueOf( getReg("$Obj") ).intValue();
				newstate = Integer.valueOf( getReg("$a0") ).intValue();
				newtuleft = Long.valueOf( getReg("$a1") ).longValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Entity ourObject = ourWorld.getObject ( objid );
			if ( ourObject == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				ourObject.setNewState ( newstate , newtuleft );
			}
		}
		else if ( functionToExec.equalsIgnoreCase("getrelationshipstate") )
		{
			int objid1,objid2;
			try
			{
				objid1 = Integer.valueOf( getReg("$Obj") ).intValue();
				objid2 = Integer.valueOf( getReg("$a0") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Mobile ourObject1 = (Mobile) ourWorld.getObject ( objid1 );
			Entity ourObject2 = ourWorld.getObject ( objid2 );
			if ( ourObject1 == null || ourObject2 == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				setReg ( "$v0" , String.valueOf ( ourObject1.getRelationshipState( ourObject2 ) ) );
			}
		} //end eva function: getrelationshipsstate
		else if ( functionToExec.equalsIgnoreCase("setrelationshipstate") )
		{
			int objid1;
			int objid2;
			int newstate;
			//long newtuleft; <- luego habra que añadir tusleft a las relaciones
			try
			{
				objid1 = Integer.valueOf( getReg("$Obj") ).intValue();
				objid2 = Integer.valueOf( getReg("$a0" ) ).intValue();
				newstate = Integer.valueOf( getReg("$a1") ).intValue();
				//newtuleft = Long.valueOf( getReg("$a2") ).longValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				//Debug.println("Dos");
				return;
			}
			Mobile ourObject1 = (Mobile) ourWorld.getObject ( objid1 );
			//Debug.println(ourObject1.getID());
			Entity ourObject2 = ourWorld.getObject ( objid2 );
			if ( ourObject1 == null || ourObject2 == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				//Debug.println("Uno");
				//Debug.println(ourObject1 == null);
				return;
			}
			else
			{
				//Debug.println(ourObject1);
				//Debug.println(ourObject2);
				//Debug.println(newstate);
				ourObject1.setRelationshipState ( ourObject2 , newstate );
				long prueba = (long)ourObject1.getRelationshipState(ourObject2)*((long)Math.pow(2,32));
				//Debug.println( ourObject2.getState() + "::" + ourObject1.getRelationshipState(ourObject2) + "::" +  prueba + "::" + Long.MAX_VALUE  );
			}
		} //end eva function: setrelationshipsstate
		else if ( functionToExec.equalsIgnoreCase("waitkeypress") )
		{
			int objid1;
			try
			{
				objid1 = Integer.valueOf( getReg("$Obj") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Player ourObject1 = (Player) ourWorld.getObject ( objid1 );
			if ( ourObject1 == null  )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				ourObject1.waitKeyPress();
			}
		} //end eva function: waitkeypress
		else if ( functionToExec.equalsIgnoreCase("force") )
		{
			int objid1;
			String s;
			try
			{
				objid1 = Integer.valueOf( getReg("$Obj") ).intValue();
				s = getReg("$a0");
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Player ourObject1 = (Player) ourWorld.getObject ( objid1 );
			if ( ourObject1 == null  )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				ourObject1.forceCommand(s);
			}
		} //end eva function: force
		
		else if ( functionToExec.equalsIgnoreCase("clearscreen") )
		{
			int objid1;
			try
			{
				objid1 = Integer.valueOf( getReg("$Obj") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Player ourObject1 = (Player) ourWorld.getObject ( objid1 );
			if ( ourObject1 == null  )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				ourObject1.clearScreen();
			}
		} //end eva function: clearscreen
		
		else if ( functionToExec.equalsIgnoreCase("makerandomvalidmove") )
		{
			int objid1;
			try
			{
				objid1 = Integer.valueOf( getReg("$Obj") ).intValue();;
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Mobile ourObject1 = (Mobile) ourWorld.getObject ( objid1 );
			if ( ourObject1 == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				ourObject1.makeRandomValidMove();
			}
		} //end eva function: makerandomvalidmove
		
		else if ( functionToExec.equalsIgnoreCase("issubstringof") )
		{
			String s1,s2;
			s1 = getReg("$a0");
			s2 = getReg("$a1");
			setReg("$v0", String.valueOf ( StringMethods.isSubstringOf(s1,s2) ) );
			return;
		}
		
		else if ( functionToExec.equalsIgnoreCase("referstoentity") )
		{
			int objid1;
			try
			{
				objid1 = Integer.valueOf( getReg("$a0") ).intValue();;
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Entity ourObject1 = (Entity) ourWorld.getObject ( objid1 );
			if ( ourObject1 == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				//object OK, now get string and sing/plur
				String s = getReg("$Obj");
				String plurString = getReg("$a1");
				boolean plural = (plurString==null)?false:(plurString.equalsIgnoreCase("true"))?true:false;
				setReg("$v0", String.valueOf ( ParserMethods.refersToEntity(s,ourObject1,plural) ) );
			}
		} //end eva function: referstoentity
		
		else if ( functionToExec.equalsIgnoreCase("referstoentities") )
		{
			int objid1,objid2;
			try
			{
				objid1 = Integer.valueOf( getReg("$a0") ).intValue();
				objid2 = Integer.valueOf( getReg("$a1") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Entity ourObject1 = (Entity) ourWorld.getObject ( objid1 );
			Entity ourObject2 = (Entity) ourWorld.getObject ( objid2 );
			if ( ourObject1 == null || ourObject2 == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				//object OK, now get string and sing/plur
				String s = getReg("$Obj");
				String plurString1 = getReg("$a2");
				String plurString2 = getReg("$a3");
				boolean plural1 = (plurString1==null)?false:(plurString1.equalsIgnoreCase("true"))?true:false;
				boolean plural2 = (plurString2==null)?false:(plurString2.equalsIgnoreCase("true"))?true:false;
				setReg("$v0", String.valueOf ( ParserMethods.refersToEntities(s,ourObject1,ourObject2,plural1,plural2) ) );
			}
		} //end eva function: referstoentities
		
		else if ( functionToExec.equalsIgnoreCase("giveitem") )
		{
			int objid1,objid2;
			try
			{
				objid1 = Integer.valueOf( getReg("$Obj") ).intValue();
				objid2 = Integer.valueOf( getReg("$a0") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Mobile ourMobile=null;
			try
			{
				ourMobile = (Mobile) ourWorld.getObject ( objid1 );
			}
			catch ( ClassCastException cce )
			{
				//será una habitación
				Room ourRoom = (Room) ourWorld.getObject ( objid1 );
				Item ourItem = (Item) ourWorld.getObject ( objid2 );
				if ( ourRoom == null || ourItem == null )
				{
					setReg ( "$Exc" , "NotAnIDNumber" );
					return;
				}
				else
				{
					try
					{
						ourRoom.addItem(ourItem);
					}
					catch ( WeightLimitExceededException wlee )
					{
						setReg ( "$Exc" , "WeightLimitExceeded" );
					}
					catch ( VolumeLimitExceededException vlee )
					{
						setReg ( "$Exc" , "VolumeLimitExceeded" );
					}
				}
			}
			Item ourItem = (Item) ourWorld.getObject ( objid2 );
			if ( ourMobile == null || ourItem == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				try
				{
					ourMobile.addItem(ourItem);
				}
				catch ( WeightLimitExceededException wlee )
				{
					setReg ( "$Exc" , "WeightLimitExceeded" );
				}
				catch ( VolumeLimitExceededException vlee )
				{
					setReg ( "$Exc" , "VolumeLimitExceeded" );
				}
			}
		} //end eva function: giveitem
		
		else if ( functionToExec.equalsIgnoreCase("removeitem") )
		{
			int objid1,objid2;
			try
			{
				objid1 = Integer.valueOf( getReg("$Obj") ).intValue();
				objid2 = Integer.valueOf( getReg("$a0") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Mobile ourMobile=null;
			Item ourItem = (Item) ourWorld.getObject ( objid2 );
			try
			{
				ourMobile = (Mobile) ourWorld.getObject ( objid1 );
			}
			catch ( ClassCastException cce )
			{
				//será una habitación
				Room ourRoom = (Room) ourWorld.getObject ( objid1 );
				if ( ourRoom == null || ourItem == null )
				{
					setReg ( "$Exc" , "NotAnIDNumber" );
					return;
				}
				else
				{
					setReg("$v0", String.valueOf( ourRoom.removeItem(ourItem) ) );
				}
			}
			if ( ourMobile == null || ourItem == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				setReg("$v0", String.valueOf( ourMobile.removeItem(ourItem) ) );
			}
		} //end eva function: removeitem
		
		else if ( functionToExec.equalsIgnoreCase("hasitem") )
		{
			int objid1,objid2;
			try
			{
				objid1 = Integer.valueOf( getReg("$Obj") ).intValue();
				objid2 = Integer.valueOf( getReg("$a0") ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				setReg ( "$Exc" , "NotANumber" );
				return;
			}
			Mobile ourMobile=null;
			Item ourItem = (Item) ourWorld.getObject ( objid2 );
			try
			{
			 	ourMobile = (Mobile) ourWorld.getObject ( objid1 );
			}
			catch ( ClassCastException cce )
			{
				Room ourRoom = (Room) ourWorld.getObject ( objid1 );
				if ( ourRoom == null || ourItem == null )
				{
					setReg ( "$Exc" , "NotAnIDNumber" );
					return;
				}
				else
				{
					setReg("$v0", String.valueOf( ourRoom.hasItem(ourItem) ) );
				}
			}
			if ( ourMobile == null || ourItem == null )
			{
				setReg ( "$Exc" , "NotAnIDNumber" );
				return;
			}
			else
			{
				setReg("$v0", String.valueOf( ourMobile.hasItem(ourItem) ) );
			}
		} //end eva function: hasitem
		
		
		else
		{
			throw ( new EVASyntaxException ( "Unrecognized or unsupported function: " + functionToExec ) );
		}
	}
	
}


