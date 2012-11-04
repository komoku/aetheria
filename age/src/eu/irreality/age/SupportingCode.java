/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import eu.irreality.age.scripting.ScriptException;

//interfaz para las entidades y objetos que soporten c�digo beanshell.
public interface SupportingCode
{

	public boolean execCode ( String routine , Object[] args , ReturnValue retval ) throws ScriptException;
	public boolean execCode ( String routine , Object[] args ) throws ScriptException;
	public ObjectCode getAssociatedCode ();
	
}