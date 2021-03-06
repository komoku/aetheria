/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;

import eu.irreality.age.bsh.ExtendedBSHInterpreter;
import eu.irreality.age.debug.Debug;
import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.scripting.ScriptException;
import eu.irreality.age.scripting.bsh.BSHScriptException;
import eu.irreality.age.util.VersionComparator;
import bsh.*;
public class ObjectCode
{

	public static String getInterpreterVersion ( )
	{
		return ("EVA 0.2, Beanshell 2.0 beta 2"); 
	}

	public String toString()
	{
		return theCode;
	}

	public Object clone()
	{
		ObjectCode oc = new ObjectCode ( theCode , codeVersion , theWorld );
		oc.permanent = this.permanent;
		oc.permanentInterpreter = null;
		oc.isConditionCode = this.isConditionCode;
		return oc;
	}	
	
	public ObjectCode cloneIfNecessary ()
	{
		if ( permanent )
			return (ObjectCode)clone();
		else
			return this;
	}

	private String codeVersion;
	private String theCode;
	private World theWorld;
	
	private boolean isConditionCode = false; //if it is a dynamic condition
	
	
	boolean permanent = true;
	ExtendedBSHInterpreter permanentInterpreter = null;
	
	
	//legacy behavior
	public static int INNER_END_RETURNS = 0;
	
	//new behavior
	public static int INNER_END_THROWS = 1;
	
	//what does an end() called from another BSH method do?
	private int endBehavior = INNER_END_THROWS;
	
	public void setEndBehavior ( int behavior )
	{
		endBehavior = behavior;
	}
	
	public int getEndBehavior ( )
	{
		return endBehavior;
	}
	
	public void configureEndBehavior ( World w )
	{
		if ( new VersionComparator().compare(w.getParserVersion(),"1.2.3") < 0 )
			setEndBehavior(INNER_END_RETURNS);
		else
			setEndBehavior(INNER_END_THROWS);
	}
	
	
	/**
	* Version may be [string literal]:
	* - "EVA" (Ensamblador Virtual de Aetheria)
	* - "BeanShell"
	* 
	* This constructor is apparently only used by legacy code and by clone()
	*/
	public ObjectCode ( String code , String version , World world )
	{
		theCode=code;
		codeVersion=version;
		theWorld=world;
		configureEndBehavior(world);
	}
	
	void resetPermanentInterpreter (  )
	{
		permanentInterpreter = null;
	}	
	
	//true si existe la rutina y llegamos al end
	//for EVA code execution only
	public boolean run ( String aroutine ) throws EVASemanticException
	{
		if ( !codeVersion.equalsIgnoreCase("EVA") ) return false;
		try
		{
			CodeRunner maquina = new CodeRunner ( theCode , theWorld );
			return maquina.runCode ( theCode , aroutine );
		}
		catch ( EVASyntaxException syn )
		{
			theWorld.writeError("Error de sintaxis en el c�digo EVA.\n");
			theWorld.writeError("En concreto: " + syn.getMessage() + "\n");
		}	
		return false;
	}
	
	/**
	This method is for EVA code execution only, needed data is placed in data segment.
	@return true: si hemos ejecutado el c�digo correctamente hasta un end.
	@return false: si no hemos ejecutado bien el c�digo o bien hemos llegado a un continue.
	@exception EVASemanticException: si el c�digo EVA tira una excepci�n.
	*/
	public boolean run ( String aroutine , String dataSegment ) throws EVASemanticException
	{
		if ( !codeVersion.equalsIgnoreCase("EVA") ) return false;
		try
		{
			CodeRunner maquina = new CodeRunner ( theCode , theWorld );
			return maquina.runCode ( theCode , aroutine , dataSegment );
		}
		catch ( EVASyntaxException syn )
		{
			theWorld.writeError("Error de sintaxis en el c�digo EVA.\n");
			theWorld.writeError("En concreto: " + syn.getMessage() + "\n");
		}	
		return false;
	}
	
	/**
	 * Makes the given interpreter load the stdfunct.bsh file.
	 * @param i
	 * @throws EvalError
	 */
	private void sourceStandardLibrary ( ExtendedBSHInterpreter i ) throws EvalError
	{
		try
		{
			i.source(this.getClass().getClassLoader().getResource("stdfunct.bsh"));
		}
		catch ( java.io.FileNotFoundException fnfe )
		{
			System.err.println("Warning: BeanShell standard function library stdfunct.bsh not found!");
		}
		catch ( java.io.IOException fnfe )
		{
			System.err.println("Warning: BeanShell standard function library stdfunct.bsh couldn't be read!");
		}
	}
	
	/**
	 * Sets a variable for each property in the given Entity.
	 * @param i The interpreter in which to set the variables.
	 * @param theCaller The entity whose properties we set the variables with.
	 * @throws EvalError
	 */
	private void setPropertyVariables ( ExtendedBSHInterpreter i , Object theCaller ) throws EvalError
	{
		//set entity properties for convenience IF entity passed
		//further sets in the code will override these if present
		Entity context = null;
		if ( theCaller instanceof Entity ) context = (Entity)theCaller;
		if ( context != null )
		{
			List pList = context.getProperties();
			for ( int k = 0 ; k < pList.size() ; k++ )
			{
				PropertyEntry pe = (PropertyEntry) pList.get(k);
				i.set ( pe.getName() , pe.getValueAsBoolean() );
			}
		}
	}
	
	/**
	 * Makes the interpreter set variables named arg0, arg1, ... , argk and returns the string "arg0, arg1, ..., argk" that is used to call the method via source()
	 * @param i
	 * @param theArguments
	 * @return
	 * @throws EvalError
	 */
	private String prepareArguments ( ExtendedBSHInterpreter i , Object[] theArguments ) throws EvalError
	{
		//set the arguments!
		String argString = " ";
		for ( int k = 0 ; k < theArguments.length ; k++ )
		{
			i.set("arg" + k , theArguments[k]);
			argString += ( (k>0?", arg":"arg") + k );	
		}
		return argString;
	}
	
	/**
	 * Processes an Object[][] which is an array of pairs (String,Objects); and sets in the interpreter
	 * the variables named by the Strings to the values in the Objects. 
	 * @param i
	 * @param initializations
	 * @throws EvalError
	 */
	private void doInitializations ( ExtendedBSHInterpreter i , Object[][] initializations ) throws EvalError
	{
		for ( int w = 0 ; w < initializations.length ; w++ )
		{
			Object[] curInit = initializations[w];
			if ( curInit.length < 2 ) continue;
			i.set ( (String)curInit[0] , curInit[1] );
		}
	}
	
	/**
	 * Sets the standard variables obj, self and world in bsh code.
	 * @param i Interpreter to set the variables in.
	 * @param theCaller Entity that called the bsh code.
	 * @throws EvalError
	 */
	private void setStandardVariables ( ExtendedBSHInterpreter i  , Object theCaller ) throws EvalError
	{
		i.set("obj",theCaller);
		i.set("self",theCaller);
		i.set("world",theWorld);
	}
	
	
	/**This attribute is used to control the execution of initFakeInterpreter() so that it is executed only once.*/
	private static boolean fakeInitializationDone = false;
	
	/**
	 * This method is used to prevent a bounded memory leak caused by BeanShell.
	 * The first time an Interpreter is initialized, BeanShell keeps a static reference to it (as a kind of fallback interpreter, I think).
	 * This prevents the GC from collecting anything that is linked from variables in the scripted context of that interpreter.
	 * Therefore, we use this method so that the first interpreter we create in a given session is "fake" and we don't really use it,
	 * so that variables are not created on its context and thus the leak doesn't happen.
	 */
	private void initFakeInterpreter()
	{
		new ExtendedBSHInterpreter();
		fakeInitializationDone = true;
	}
	
	/**
	 * Initializes a beanshell interpreter for a given entity.
	 * This includes loading the standard library, setting the standard variables (world, self)
	 * and setting the property variables.
	 * @param theCaller
	 * @return
	 * @throws EvalError
	 */
	private ExtendedBSHInterpreter initInterpreter ( Object theCaller ) throws EvalError
	{
		//the following line is to avoid a pitfall with beanshell that can cause a (bounded) memory leak. It's not needed to understand this code, since it does nothing that has an influence
		//on the working of this class:
		if ( !fakeInitializationDone ) initFakeInterpreter();
		
		ExtendedBSHInterpreter i;
		i = new ExtendedBSHInterpreter();
		permanentInterpreter = i;
		sourceStandardLibrary(i);
		setStandardVariables(i,theCaller);
		setPropertyVariables(i,theCaller);
		return i;
	}
	
	/**
	This method is for BeanShell code execution only.
	With aroutine != null, evaluates the code and executes the given routine with no parameters:
		*Throwing any non-end exception found as a TargetError,
		*Returning true if an end exception (BSHCodeExecutedOKException) is found,
		*Returning false if code is successfully executed with no end exception found (event not exclusive)
			or if code cannot be executed (nonexistent routine...)
	With aroutine == null, just evaluates the code.
	*/
	public boolean run ( String aroutine , Object theCaller , Object[] theArguments ) throws ScriptException
	{

			return run ( aroutine , theCaller , theArguments , null );

		//unified with method below as of 2011-03-26, delete the following if it works
		/*
		if ( !codeVersion.equalsIgnoreCase("BeanShell") ) return false;
		try
		{
		
			ExtendedBSHInterpreter i;
		
			if ( permanent && permanentInterpreter != null )
			{
				//Debug.println("Using permanent for " + aroutine + " at " + theCaller);
				i = permanentInterpreter;
				
			}
			else
			{
				//Debug.println("Using nonpermanent for " + aroutine + " at " + theCaller);
				
				i = initInterpreter(theCaller);

				i.eval(theCode);
				
				if ( aroutine == null )
				{
					return false; //no se nos pide ejecutar una rutina. No se encontr� end.
				}
					
				setStandardVariables(i,theCaller);
			
			}
			
			//see if routine actually exists!
			
			if ( !existsMethod ( i , aroutine , theArguments ) ) return false;
			
			//set the arguments!
			String argString = prepareArguments(i,theArguments);
			debugInfo ( aroutine , theCaller , theArguments );
			i.eval(aroutine + "(" + argString + ")"); //TODO: Why doesn't this throw EvalError properly in Zendyr's serverintro?
		}
		catch ( TargetError te ) //excepci�n tirada a prop�sito por el script
		{
			Throwable lastExcNode = te;
			while ( lastExcNode instanceof TargetError )
				lastExcNode = ((TargetError)lastExcNode).getTarget();
			if ( lastExcNode instanceof BSHCodeExecutedOKException ) return true; //lleg� al end
			else throw te;
		}
		catch ( EvalError pe )
		{
			reportEvalError(pe,aroutine,theCaller,theArguments);
		}
		catch ( Exception e )
		{
		    theWorld.writeError("Catched the following exception: " + e);
		    e.printStackTrace();
		}
		return false;
		*/
	}
	
	
	/**
	 * Evaluates some expression in the context of this code, not necessarily a function call.
	 * @param code Code to evaluate.
	 * @param theCaller Entity in which the code is evaluated.
	 * @param retval Parameter that will hold the return value.
	 * @return
	 * @throws TargetError
	 */
	public boolean evaluate ( String code , Object theCaller , ReturnValue retval ) throws ScriptException
	{
		if ( !codeVersion.equalsIgnoreCase("BeanShell") ) return false;
		try
		{
		
			ExtendedBSHInterpreter i;
		
			if ( permanent && permanentInterpreter != null )
			{
				i = permanentInterpreter;
			}
			else
			{
			
				//System.out.println("Using a nonpermanent for " + aroutine + " at " + theCaller);
			
				i = initInterpreter(theCaller);
				
				i.eval ( theCode );
				
				setStandardVariables(i,theCaller);
				
			}
			
			Object returned = i.eval(code);
			retval.setRetVal(returned);
			return false;
		}
		catch ( TargetError te ) //excepci�n tirada a prop�sito por el script
		{
			Throwable lastExcNode = te;
			while ( lastExcNode instanceof TargetError )
				lastExcNode = ((TargetError)lastExcNode).getTarget();
			if ( lastExcNode instanceof BSHCodeExecutedOKException ) return true; //lleg� al end
			else throw new BSHScriptException(te);
		}
		catch ( EvalError pe )
		{
			reportEvalError(pe,null,theCaller,null);
		}
		catch ( Exception e )
		{
		    theWorld.writeError("Catched the following exception: " + e);
		    e.printStackTrace();
		}
		return false;
	}
	
	/**
	This method is for BeanShell code execution only.
	With aroutine != null, evaluates the code and executes the given routine with no parameters:
		*Throwing any non-end exception found as a TargetError,
		*Returning true if an end exception (BSHCodeExecutedOKException) is found,
		*Returning false if code is successfully executed with no end exception found (event not exclusive)
			or if code cannot be executed (nonexistent routine...)
	With aroutine == null, just evaluates the code.
	ReturnValue holds the routine or code (usually predicate) return value, if any.
	*/
	public boolean run ( String aroutine , Object theCaller , Object[] theArguments , ReturnValue retval  ) throws ScriptException
	{
		if ( !codeVersion.equalsIgnoreCase("BeanShell") ) return false;
		try
		{
		
			ExtendedBSHInterpreter i;
		
			if ( permanent && permanentInterpreter != null )
			{
				i = permanentInterpreter;
			}
			else
			{
			
				//System.out.println("Using a nonpermanent for " + aroutine + " at " + theCaller);
			
				i = initInterpreter(theCaller);
				
				Object returned = i.eval ( theCode );
				
				if ( aroutine == null )
				{
					if (retval != null ) retval.setRetVal ( returned );
					return false; //OK, no se nos pidi� ejecutar una rutina, se ejecut� el c�digo, se guard� el valor de retorno y no se encontr� end.
				}
		
				setStandardVariables(i,theCaller);
				
			}

			if ( !existsMethod ( i , aroutine , theArguments ) ) return false;
			
			//set the arguments!
			String argString = prepareArguments(i,theArguments);

			debugInfo ( aroutine , theCaller , theArguments );
			Object returned = i.eval(aroutine + "(" + argString + ")");
			if ( retval != null ) retval.setRetVal ( returned );
				
		}
		catch ( TargetError te ) //excepci�n tirada a prop�sito por el script
		{
			Throwable lastExcNode = te;
			while ( lastExcNode instanceof TargetError )
				lastExcNode = ((TargetError)lastExcNode).getTarget();
			if ( lastExcNode instanceof BSHCodeExecutedOKException ) return true; //lleg� al end
			else throw new BSHScriptException(te);
		}
		catch ( EvalError pe )
		{
			reportEvalError(pe,aroutine,theCaller,theArguments);
		}
		catch ( Exception e )
		{
		    theWorld.writeError("Catched the following exception: " + e);
		    e.printStackTrace();
		}
		return false;
	}
	

	
	/**
	This method is for BeanShell code execution only.
	With aroutine != null, evaluates the code and executes the given routine with no parameters:
		*Throwing any non-end exception found as a TargetError,
		*Returning true if an end exception (BSHCodeExecutedOKException) is found,
		*Returning false if code is successfully executed with no end exception found (event not exclusive)
			or if code cannot be executed (nonexistent routine...)
	With aroutine == null, just evaluates the code.
	ReturnValue holds the routine or code (usually predicate) return value, if any.
	initalizations is an Object[] consistent of {String,Object} pairs to set at init (Object[]'s too)
	IMPORTANT: This method always runs the full code again (in case it uses the initializations). This is a difference
	with the other methods which apply permanent interpreter optimization so as not to evaluate the full code.
	*/
	public boolean run ( String aroutine , Object theCaller , Object[] theArguments , ReturnValue retval , Object[][] initializations  ) throws ScriptException
	{
		if ( !codeVersion.equalsIgnoreCase("BeanShell") ) return false;
		try
		{
			ExtendedBSHInterpreter i;
		
			if ( permanent && permanentInterpreter != null )
			{
				i = permanentInterpreter;
			}
			else
			{
				//Debug.println("Using nonpermanent for " + aroutine + " at " + theCaller);
				i = initInterpreter(theCaller);	
			}
			
			doInitializations ( i , initializations );
			
			Object returned = i.eval(theCode);
			
			if ( aroutine == null )
			{
				retval.setRetVal ( returned );
				return false; //OK, no se nos pidi� ejecutar una rutina, se ejecut� el c�digo, se guard� el valor de retorno y no se encontr� end.
			}
			
			setStandardVariables(i,theCaller);			
			
			if ( !existsMethod ( i , aroutine , theArguments ) ) return false;
			
			//set the arguments!
			String argString = prepareArguments(i,theArguments);

			debugInfo ( aroutine , theCaller , theArguments );
			returned = i.eval(aroutine + "(" + argString + ")");
			retval.setRetVal ( returned );
				
			//Debug.println("Returnin':" + retval.getRetVal());
		}
		catch ( TargetError te ) //excepci�n tirada a prop�sito por el script
		{
			Throwable lastExcNode = te;
			while ( lastExcNode instanceof TargetError )
				lastExcNode = ((TargetError)lastExcNode).getTarget();
			if ( lastExcNode instanceof BSHCodeExecutedOKException ) return true; //lleg� al end
			else throw new BSHScriptException(te);
		}
		catch ( EvalError pe )
		{
			reportEvalError(pe,aroutine,theCaller,theArguments);
		}
		return false;
	}
	
	//todo: maybe check World getDebugMode() and then println
	private void debugInfo ( String aroutine , Object theCaller , Object[] theArguments )
	{
	    Debug.printlnCodeDebugging("Calling BSH method: " + aroutine);
	    Debug.printlnCodeDebugging("On object: " + theCaller);
	    Debug.printlnCodeDebugging("With arguments:");
	    if ( theArguments.length == 0 ) Debug.printlnCodeDebugging("(no arguments");
	    else
	    {
		for ( int i = 0 ; i < theArguments.length ; i++ )
		{
		    Debug.printlnCodeDebugging("#" + (i+1) + ": " + prettyPrint(theArguments[i]));
		}
	    }
	}
	
	private String prettyPrint(Object o)
	{
	    if ( o == null ) return "null";
	    String theClass = o.getClass().getSimpleName();
	    return theClass + ": " + o;
	}
	
	
	void reportEvalError ( EvalError pe , String aroutine , Object theCaller , Object[] theArguments )
	{
		String methodInfo = aroutine;
		if ( methodInfo == null && isConditionCode ) methodInfo = "(dynamic name or description condition code)";
		ExceptionPrinter.reportEvalError(pe, theWorld, methodInfo, theCaller, theArguments);
		/*
		theWorld.writeError("Error de sintaxis en el c�digo BeanShell.\n");
		//theWorld.writeError("En concreto: " + pe + "\n"); 
		theWorld.writeError("Error: "+pe.getMessage()+"\n"); 
		//theWorld.writeError("["+pe.getErrorSourceFile()+"]"); 
		theWorld.writeError("En: c�digo del objeto " + theCaller + "\n"); 
		//System.err.println(pe.getMessage());
		//pe.printStackTrace();
		theWorld.writeError("Cargado para llamar la rutina: " + aroutine + "\n");
		//theWorld.writeError("Objeto del c�digo: " + theCaller + "\n");
		theWorld.writeError("Con argumentos: ");
		for ( int i = 0 ; i < theArguments.length ; i++ )
			theWorld.writeError(theArguments[i] + " "); 
		theWorld.writeError("\n");
		*/
	}
	
	
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
	
		return getXMLRepresentation ( doc , "Code" );
		
	}
	
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc , String tagName )
	{
		
		org.w3c.dom.Element suElemento = doc.createElement( tagName );
		
		org.w3c.dom.Text t = doc.createTextNode( theCode );
		
		suElemento.appendChild(t);
		
		suElemento.setAttribute ( "language" , String.valueOf( codeVersion ) );		

		return suElemento;

	}

	public ObjectCode ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		this ( mundo , n , false );
	}
	
	public ObjectCode ( World mundo , org.w3c.dom.Node n , boolean isConditionCode ) throws XMLtoWorldException
	{
		
		this.isConditionCode = isConditionCode;
		
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ("Code node not Element") );
		}
		
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;
		
		if ( ! e.hasAttribute("language") )
		{
			throw ( new XMLtoWorldException ("Code node lacks language attribute") );
		}
		
		codeVersion = e.getAttribute("language");
		
		org.w3c.dom.Node cdataNode = e.getFirstChild();
		if ( cdataNode != null ) theCode = cdataNode.getNodeValue(); //should be a Text
		else theCode = "";
		
		theWorld = mundo;
		
		configureEndBehavior(mundo);
		
	}
	
	/**
	 * Checks for existence of a specified method in the beanshell code.
	 * @param methodName Name of the required method.
	 * @param theCaller Entity that owns the bsh method.
	 * @param arguments Arguments to be passed to the method.
	 * @return
	 */
	public boolean existsMethod ( String methodName , Object theCaller , Object[] arguments )
	{
		ExtendedBSHInterpreter i;
		if ( permanent && permanentInterpreter != null )
		{
			i = permanentInterpreter;
		}
		else
		{
			try
			{
				i = initInterpreter(theCaller);	
				i.eval ( theCode );
				setStandardVariables(i,theCaller);
			}
			catch ( EvalError ee )
			{
				reportEvalError(ee,methodName,theCaller,arguments);
				return false;
			}
		}
		return existsMethod ( i , methodName , arguments );
	}

	
	private boolean existsMethod ( Interpreter i , String methodName , Object[] arguments )
	{
		BshMethod[] metodos = i.getNameSpace().getMethods();
		int m;
		for ( m = 0 ; m < metodos.length ; m++ )
		{
			BshMethod method = metodos[m];
						
			if ( method.getName().equals(methodName) && method.getParameterTypes().length == arguments.length )
			{
				boolean correct = true;
				for ( int k = 0 ; k < arguments.length ; k++ )
				{
					
					Class argumentClass = null;
										
					if ( arguments[k] instanceof bsh.Primitive ) //if code is invoked from the bsh innards, we will get basic type values wrapped in bsh.Primitive
					{
						argumentClass = ((bsh.Primitive)arguments[k]).getType();
						//this includes a primitive null if null is passed from bsh innards. In that case, we unbox arguments[k]
						//to simplify the null checks ahead.
						if ( argumentClass == null ) arguments[k] = null;
					}
					else //if code is invoked from AGE, we don't wrap basic type values, we use them directly as arguments
					{
						if ( arguments[k] != null )
							argumentClass = arguments[k].getClass();
					}
					
					if ( arguments[k] == null && Object.class.isAssignableFrom(method.getParameterTypes()[k]) )
						; //    correct = true;
					
					else if ( arguments[k] == null && !Object.class.isAssignableFrom(method.getParameterTypes()[k]) )
						correct = false; //null parameter, basic type
						
					else if ( ( argumentClass == int.class || argumentClass == Integer.class ) 
							&& ( method.getParameterTypes()[k] == int.class || method.getParameterTypes()[k] == Integer.class ) )
						; //	correct = true;
					
					else if ( ( argumentClass == long.class || argumentClass == Long.class ) 
							&& ( method.getParameterTypes()[k] == long.class || method.getParameterTypes()[k] == Long.class ) )
						; // correct = true;
					
					else if ( ( argumentClass == boolean.class || argumentClass == Boolean.class ) 
							&& ( method.getParameterTypes()[k] == boolean.class || method.getParameterTypes()[k] == Boolean.class ) )
						; // correct = true;
					
					else if ( ( argumentClass == char.class || argumentClass == Character.class ) 
							&& ( method.getParameterTypes()[k] == char.class || method.getParameterTypes()[k] == Character.class ) )
						; // correct = true;
					
					else if ( ( argumentClass == float.class || argumentClass == Float.class ) 
							&& ( method.getParameterTypes()[k] == float.class || method.getParameterTypes()[k] == Float.class ) )
						; // correct = true;
					
					else if ( ( argumentClass == double.class || argumentClass == Double.class ) 
							&& ( method.getParameterTypes()[k] == double.class || method.getParameterTypes()[k] == Double.class ) )
						; // correct = true;
					
					else if ( arguments[k] != null && !method.getParameterTypes()[k].isAssignableFrom(argumentClass) )
						correct = false;
					
				}
				if ( correct ) break;
			}
		}
		if ( m >= metodos.length ) return false; //no existe el m�todo.
		else return true; //hicimos break => existe.
	}
	
	
}
