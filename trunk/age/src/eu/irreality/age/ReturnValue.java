/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;


//wrappea el valor de retorno de una funci�n BeanShell. Que lo metan aqu�.
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

