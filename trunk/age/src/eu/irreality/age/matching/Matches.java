/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 09/07/2011 19:53:23
 */
package eu.irreality.age.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author carlos
 *
 * This class keeps an ordered collection of entity matches.
 */
public class Matches
{

	//private TreeSet theMatches = new TreeSet();
	
	private List theMatches = new ArrayList();
	
	public void addMatch ( Match m )
	{
		theMatches.add(m);
	}
	
	public Iterator iterator()
	{
		Collections.sort(theMatches);
		return theMatches.iterator();
	}
	
	/**
	 * Builds a vector of Match objects from this collection.
	 * @return
	 */
	public Vector toMatchesVector()
	{
		Vector result = new Vector();
		for ( Iterator it = iterator() ; it.hasNext() ; )
		{
			result.add(it.next());
		}
		return result;
	}
	
	/**
	 * Builds a vector of path vectors from this collection.
	 * @return
	 */
	public Vector toPathVector()
	{
		Vector result = new Vector();
		for ( Iterator it = iterator() ; it.hasNext() ; )
		{
			Match next = (Match)it.next();
			result.add(next.getPath());
		}
		return result;
	}
	
}
