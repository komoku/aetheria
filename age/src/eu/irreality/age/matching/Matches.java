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
	
	private boolean sorted = false; //is the list sorted?
	
	private void sortIfUnsorted()
	{
		if ( !sorted )
			Collections.sort(theMatches);
		sorted = true;
	}
	
	public void addMatch ( Match m )
	{
		theMatches.add(m);
		sorted = false;
	}
	
	public int getBestPriority()
	{
		sortIfUnsorted();
		if ( theMatches.isEmpty() ) return 0;
		else return ((Match)theMatches.get(0)).getPriority();
	}
	
	public Iterator iterator()
	{
		sortIfUnsorted();
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
			//System.err.println(next+"\n");
			result.add(next.getPath());
		}
		return result;
	}
	
	/**
	 * For when we don't care about paths, just about plain entities (non-recursive cases).
	 * @return
	 */
	public Vector toEntityVector()
	{
		Vector result = new Vector();
		for ( Iterator it = iterator() ; it.hasNext() ; )
		{
			Match next = (Match)it.next();
			result.add(next.getPath().get(0));
		}
		return result;
	}
	
	public int size()
	{
		return theMatches.size();
	}
	
	public static Vector[] toEntityVectors( Matches[] ma )
	{
		Vector[] result = new Vector[ma.length];
		for ( int i = 0 ; i < ma.length ; i++ )
		{
			result[i] = ma[i].toEntityVector();
		}
		return result;
	}
	
	public static Vector[] toPathVectors( Matches[] ma )
	{
		Vector[] result = new Vector[ma.length];
		for ( int i = 0 ; i < ma.length ; i++ )
		{
			result[i] = ma[i].toPathVector();
		}
		return result;
	}
	
}
