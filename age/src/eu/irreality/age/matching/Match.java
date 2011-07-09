/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 09/07/2011 19:45:37
 */
package eu.irreality.age.matching;

import java.util.Vector;

/**
 * @author carlos
 * Created at regulus2, 2011-07-09
 * 
 * This class represents a match, which has a priority (depending on how good the match was
 * between user input and an entity's reference name) and a container path to the entity.
 *
 */
public class Match implements Comparable
{
	
	private Vector path;
	private int priority;
	
	public Match ( Vector path , int priority )
	{
		this.path = path;
		this.priority = priority;
	}
	
	public Vector getPath()
	{
		return path;
	}
	
	public int getPriority()
	{
		return priority;
	}

	public int compareTo(Object o) 
	{
		Match other = (Match)o;
		int otherPrio = other.getPriority();
		if ( priority < otherPrio ) return -1;
		if ( priority > otherPrio ) return 1;
		else //here we could insert criteria like shortest path, if needed.
			//return this.path.toString().compareTo(other.path.toString());
			return 0;
	}
	
	public String toString()
	{
		return "Match: " + path + " with priority " + priority;
	}

}
