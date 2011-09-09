package eu.irreality.age.language;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
		String patternStr = "\\b([Aa])\\b\\s*\\b([aeioAEIO])";
		String replaceStr = "$1n $2";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(niceString);
		return matcher.replaceAll(replaceStr);
		// a (b c) d <ef> g
		
		
		//return niceString.replaceAll("\\ba\\b\\s*\\ba","an a");
	}
	
	//change a to an when needed
	public String correctMorphologyWithoutTrimming ( String s )
	{
		if ( s == null ) return null;
		String niceString = super.correctMorphologyWithoutTrimming(s);

		String patternStr = "\\b([Aa])\\b\\s*\\b([aeioAEIO])";
		String replaceStr = "$1n $2";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(niceString);
		return matcher.replaceAll(replaceStr);
	}

}
