package eu.irreality.age.language;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.irreality.age.Mobile;
import eu.irreality.age.NaturalLanguage;
import eu.irreality.age.Player;
import eu.irreality.age.StringMethods;
import eu.irreality.age.Utility;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;

public class English extends NaturalLanguage
{
	
	private List phrasalVerbs = new ArrayList();
		
	public English()
	{
		super("en");
		try 
		{
			loadPhrasalVerbs();
		} 
		catch  (IOException e) 
		{
			System.err.println(UIMessages.getInstance().getMessage("warning.no.phrasal.verb.file"));
			e.printStackTrace();
		}
	}
	
	/**
	 * Obtain path to phrasal verbs file.
	 * @return
	 */
	private String getPhrasalVerbPath ( )
	{
		return Paths.LANG_FILES_PATH + "/" + getLanguageCode() + "/phrasal.lan";
	}
	
	
	private void loadPhrasalVerbs() throws IOException , FileNotFoundException
	{
		String filePath = getPhrasalVerbPath();
		BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( NaturalLanguage.class.getClassLoader().getResourceAsStream(filePath) ) );
		String linea;
		while ( ( linea = filein.readLine() ) != null )
		{
			if ( !linea.trim().equals("") )
			{
				EnglishPhrasalVerb verb = new EnglishPhrasalVerb(linea);
				phrasalVerbs.add(verb);
			}
		}
	}
	
	
	
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
		String curWord;
		while ( st.hasMoreTokens() )
			if ( (curWord = st.nextToken()).equals(fromWord) && substituting )
			{
				result.append(toWord);
				if ( oneOnly ) substituting = false;
			}
			else
				result.append(curWord);
		return result.toString();
	}
	
	
	public String substitutePronouns ( Mobile m , String command , Mentions mentions )
	{
		String theString = command;
		
		//first, eliminate the proclitic pronoun "I" if the sentence is in the first person ("I" followed by verb)
		StringTokenizer st = new StringTokenizer ( theString );
		String firstWord = null;
		String secondWord = null;
		if ( st.hasMoreTokens() ) firstWord = st.nextToken().trim().toUpperCase();
		if ( st.hasMoreTokens() ) secondWord = st.nextToken().trim();
		if ( "I".equals(firstWord) && secondWord != null && isVerb(secondWord) ) //first person detected!
			theString = theString.substring(2);
		
		//then, locate personal pronouns and substitute them
		if ( containsWord(theString,"him") )
		{
			theString = substituteWord(theString,"him",mentions.getLastMentionedObjectMS(),false);
		}
		if ( containsWord(theString,"her") )
		{
			theString = substituteWord(theString,"her",mentions.getLastMentionedObjectFS(),false);
		}
		if ( containsWord(theString,"them") )
		{
			theString = substituteWord(theString,"them",mentions.getLastMentionedObjectP(),false);
		}
		if ( containsWord(theString,"it") )
		{
			theString = substituteWord(theString,"it",mentions.getLastMentionedObjectS(),false);
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
	
	
	/**
	 * Adds English phrasal verb handling to verb extraction.
	 */
	public String extractVerb ( String sentence )
	{
		//1. see if sentence matches a non-separable phrasal verb
		for ( Iterator it = phrasalVerbs.iterator() ; it.hasNext() ; )
		{
			EnglishPhrasalVerb phrasalVerb = (EnglishPhrasalVerb) it.next();
			if ( !phrasalVerb.isSeparable() && phrasalVerb.matches(sentence) )
				return phrasalVerb.extractVerb(sentence);
		}
		
		//2. see if sentence matches a separable phrasal verb
		for ( Iterator it = phrasalVerbs.iterator() ; it.hasNext() ; )
		{
			EnglishPhrasalVerb phrasalVerb = (EnglishPhrasalVerb) it.next();
			if ( phrasalVerb.isSeparable() && phrasalVerb.matches(sentence) )
				return phrasalVerb.extractVerb(sentence);
		}
		
		//3. no phrasal verb: standard approach.
		return super.extractVerb(sentence);
	}
	
	/**
	 * Adds English phrasal verb handling to argument extraction.
	 */
	public String extractArguments ( String sentence )
	{
		//1. see if sentence matches a non-separable phrasal verb
		for ( Iterator it = phrasalVerbs.iterator() ; it.hasNext() ; )
		{
			EnglishPhrasalVerb phrasalVerb = (EnglishPhrasalVerb) it.next();
			if ( !phrasalVerb.isSeparable() && phrasalVerb.matches(sentence) )
				return phrasalVerb.extractArguments(sentence);
		}
		
		//2. see if sentence matches a separable phrasal verb
		for ( Iterator it = phrasalVerbs.iterator() ; it.hasNext() ; )
		{
			EnglishPhrasalVerb phrasalVerb = (EnglishPhrasalVerb) it.next();
			if ( phrasalVerb.isSeparable() && phrasalVerb.matches(sentence) )
				return phrasalVerb.extractArguments(sentence);
		}
		
		//3. no phrasal verb: standard approach.
		return super.extractArguments(sentence);
	}
	
	/**
	 * Returns the default verb, i.e., verb that will be used by default if a reference name is typed
	 * at the beginning of a game without specifying a verb.
	 */
	public String getDefaultVerb()
	{
		return "look";
	}
	
}
