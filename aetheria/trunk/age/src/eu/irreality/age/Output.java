/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
public interface Output
{

	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public void escribir ( String s );

	public void write ( String s );

}
