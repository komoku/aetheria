/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 28/01/2011 17:07:48
 */
package eu.irreality.age.spell;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class SimpleReverseCorrector implements SpellingCorrector
{

	private Set distance0 = new LinkedHashSet();
	private Map distance1 = new HashMap();
	private static final char WILDCARD = '?';
	
	public void init(Collection words) 
	{
		distance0 = new LinkedHashSet();
		distance1 = new HashMap();
		for ( Iterator iter = words.iterator() ; iter.hasNext(); )
		{
			String nextWord = (String) iter.next();
			addDictionaryWord ( nextWord );
		}
	}

	public void addDictionaryWord(String word) throws UnsupportedOperationException 
	{
		//add the word itself
		distance0.add(word);
		for ( int i = 0 ; i < word.length() ; i++ )
		{
			StringBuffer sb = new StringBuffer(word);
			//add substitution of character i
			sb.setCharAt(i,WILDCARD);
			distance1.put(sb.toString(),word);
			//add deletion of character i
			sb.deleteCharAt(i);
			distance1.put(sb.toString(),word);
			//add insertion before character i
			sb = new StringBuffer(word);
			sb.insert(i,WILDCARD);
			distance1.put(sb.toString(),word);
			//add swapping of characters i, i+1
			if ( i < word.length() - 1 )
			{
				sb = new StringBuffer(word);
				char temp = sb.charAt(i);
				sb.setCharAt(i,sb.charAt(i+1));
				sb.setCharAt(i+1,temp);
				distance1.put(sb.toString(),word);
			}
		}
		//add insertion at end
		distance1.put(word+WILDCARD,word);
	}


	public Correction getBestCorrection(String word) 
	{
		if ( distance0.contains(word) )
		{
			return new Correction(word,0);
		}
		else
		{
			String attempt = (String) distance1.get(word);
			if ( attempt != null ) //deletion or transposition
				return new Correction(attempt,1);
			for ( int i = 0 ; i < word.length() ; i++ )
			{
				//try substitution
				StringBuffer sb = new StringBuffer(word);
				sb.setCharAt(i,WILDCARD);
				attempt = (String) distance1.get(sb.toString());
				if ( attempt != null ) //substitution
					return new Correction(attempt,1);
				//try insertion before i
				sb = new StringBuffer(word);
				sb.insert(i,WILDCARD);
				attempt = (String) distance1.get(sb.toString());
				if ( attempt != null ) //insertion
					return new Correction(attempt,1);
			}
			//try insertion at end
			attempt = (String) distance1.get(word+WILDCARD);
			if ( attempt != null ) //insertion at end
				return new Correction(attempt,1);
		}
		return null;
	}
	
	//test
	public static void main ( String[] args )
	{
		SpellingCorrector c = new SimpleReverseCorrector();
		c.addDictionaryWord("casa");
		c.addDictionaryWord("coche");
		System.out.println(c.getBestCorrection("casa"));
		System.out.println(c.getBestCorrection("coche"));
		System.out.println(c.getBestCorrection("cesa"));
		System.out.println(c.getBestCorrection("cocho"));
		System.out.println(c.getBestCorrection("acsa"));
		System.out.println(c.getBestCorrection("casar"));
		System.out.println(c.getBestCorrection("czsa"));
		System.out.println(c.getBestCorrection("czss"));
	}
	
	public String toString()
	{
		return "[simple reverse corrector with " + distance0.size() + " words , " + distance1.size() + " extended forms]";
	}

}
