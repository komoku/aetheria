/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;


//wrappea el valor de retorno de una función BeanShell. Que lo metan aquí.
public class ReturnValue 
{
	
	Object retVal;
	
	public void setRetVal( Object o )
	{
		retVal = o;
	}
	public Object getRetVal ( )
	{
		return retVal;
	}
	
	public ReturnValue ( Object o )
	{
		retVal = o;
	}
	
}

