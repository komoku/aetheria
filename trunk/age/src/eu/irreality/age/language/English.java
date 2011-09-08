package eu.irreality.age.language;

import java.util.StringTokenizer;

import eu.irreality.age.NaturalLanguage;
import eu.irreality.age.Player;
import eu.irreality.age.StringMethods;

public class English extends NaturalLanguage
{
	
	public English()
	{
		super("en");
	}
	
	//TODO: custom obtain verb, args from sentence - for phrasal verbs?
	
	
	private boolean containsWord ( String sentence , String word )
	{
		StringTokenizer st = new StringTokenizer(sentence," \n\t,.-_", true);
		while ( st.hasMoreTokens() )
			if ( st.nextToken().equals(word) ) return true;
		return false;
	}
	
	
	private String substituteWord ( String sentence , String fromWord , String toWord , boolean oneOnly )
	{
		StringTokenizer st = new StringTokenizer(sentence," \n\t,.-_", true);
		StringBuffer result = new StringBuffer();
		boolean substituting = true;
		while ( st.hasMoreTokens() )
			if ( st.nextToken().equals(fromWord) && substituting )
			{
				result.append(toWord);
				if ( oneOnly ) substituting = false;
			}
			else
				result.append(fromWord);
		return result.toString();
	}
	
	
	public String substitutePronouns ( Player p , String command , Mentions mentions )
	{
		String theString = command;
		if ( containsWord(command,"him") )
		{
			theString = substituteWord(theString,"him",mentions.getLastMentionedObjectMS(),false);
		}
		if ( containsWord(command,"her") )
		{
			theString = substituteWord(theString,"him",mentions.getLastMentionedObjectFS(),false);
		}
		if ( containsWord(command,"them") )
		{
			theString = substituteWord(theString,"him",mentions.getLastMentionedObjectP(),false);
		}
		if ( containsWord(command,"it") )
		{
			theString = substituteWord(theString,"him",mentions.getLastMentionedObjectS(),false);
		}
		return theString;
	}
	
	//change a to an when needed
	public String correctMorphology ( String s )
	{
		if ( s == null ) return null;
		String niceString = super.correctMorphology(s);
		return niceString.replaceAll("\\A[\\W]a\\Wa","\\A[\\W]an\\Wa")
						.replaceAll("\\A[\\W]a\\We","\\A[\\W]an\\We")
						.replaceAll("\\A[\\W]a\\Wi","\\A[\\W]an\\Wi")
						.replaceAll("\\A[\\W]a\\Wo","\\A[\\W]an\\Wo");
						//.replaceAll("\\A[\\W]a\\Wu","\\A[\\W]an\\Wu");
	}

}
