/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;


public class BSHAssertionFailedException extends Exception
{

	public BSHAssertionFailedException()
	{
		super("Assertion failed (no assertion message)");
	}
	
	public BSHAssertionFailedException(String s)
	{
		super("Assertion failed: " + s);
	}

}
