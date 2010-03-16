/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;


public class EVASemanticException extends Exception
{
	private int linenumber;
	public EVASemanticException ( String mensaje )
	{
		super(mensaje);
	}
	public EVASemanticException ( String mensaje , int linenumber )
	{
		super(mensaje);
		this.linenumber = linenumber;
	}
	
	public int getLineNumber ( )
	{
		return linenumber;
	}
}
