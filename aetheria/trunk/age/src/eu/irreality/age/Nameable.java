/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;


public interface Nameable
{

	public String constructName ( int nItems , Entity viewer );
	public String constructName2 ( int nItems , Entity viewer );

	public boolean isInvisible ( Entity viewer );

}
