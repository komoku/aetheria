/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 04/11/2012 13:13:59
 */
package eu.irreality.age.scripting;

/**
 * @author carlos
 * An exception that can be thrown by a script invoked from AGE.
 */
public abstract class ScriptException extends Exception 
{

	public abstract Throwable getTarget();
	
	public abstract String printTargetError( Throwable t );
	
	public abstract boolean inNativeCode();
	

}
