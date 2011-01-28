/*
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * License/licencia: license.txt
 */
package eu.irreality.age.spell;

public class Correction 
{

	private String word;
	private double distance;
	
	public String getWord()
	{
		return word;
	}
	
	public double getDistance()
	{
		return distance;
	}
	
	public Correction ( String word , double distance )
	{
		this.word = word;
		this.distance = distance;
	}
	
	public String toString()
	{
		return "[" + word + "," + distance + "]";
	}
	
}
