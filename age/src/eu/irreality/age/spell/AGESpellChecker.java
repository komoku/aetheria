/**
 * (c) 2000-2011 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 20/03/2011 13:49:03
 */
package eu.irreality.age.spell;

import java.util.StringTokenizer;

import eu.irreality.age.NaturalLanguage;
import eu.irreality.age.World;

/**
 * @author carlos
 *
 * Note that this doesn't implement SpellingCorrector because it is a higher level class
 * that uses correctors internally.
 */
public class AGESpellChecker 
{

	World w;
	NaturalLanguage lang;
	
	SpellingCorrector verbCorrector;
	ReferenceNameCorrector nameCorrector;
	
	private static int MINLENGTH = 3;
	
	public AGESpellChecker ( World w , NaturalLanguage lang )
	{
		this.w = w;
		this.lang = lang;
		verbCorrector = lang.getVerbSpellingCorrector();
		nameCorrector = lang.initNameCorrector(w);
	}
	
	/**
	 * If for some reason it is necessary to make modifications to word lists, it will be necessary to rebuild
	 * the corrector for removals to take place (words can be added on the fly via the SpellingCorrector
	 * interface, but not removed) 
	 */
	public void rebuild()
	{
		verbCorrector = lang.getVerbSpellingCorrector();
		nameCorrector = lang.initNameCorrector(w);
	}
		
	/**
	 * Changes a verb/name to a correct one. Can use dictionary of verbs, names, or both.
	 * @param word The word that is to be corrected.
	 * @param useVerbs use the verb dictionary if true.
	 * @param useNames use the name dictionary if true.
	 * @param caseInsensitive make the correction case-insensitive. Note that this makes the result (if correction kicks in) always lowercase, i.e. "COSER" and "COZER" will be corrected to "coser".
	 * @return
	 */
	public String correctVerbOrName ( String w , boolean useVerbs , boolean useNames , boolean caseInsensitive )
	{
		//TODO Eventually make this consistent, i.e., if original word was uppercase, return uppercase correction.
		
		/*
		StringTokenizer st = new StringTokenizer ( commandString );
		if ( !st.hasMoreTokens() ) return commandString;
		String firstWord = st.nextToken();
		*/
		String word = w;
		if ( caseInsensitive )
			word = w.toLowerCase();
		String corrected = w;
		if ( word.length() >= MINLENGTH )
		{
			Correction verbCorrection = verbCorrector.getBestCorrection(word);
			Correction nameCorrection = nameCorrector.getBestCorrection(word);
			if ( useVerbs && verbCorrection != null && verbCorrection.getDistance() < 0.01 )
				corrected = verbCorrection.getWord();
			else if ( useNames && nameCorrection != null && nameCorrection.getDistance() < 0.01 )
				corrected = nameCorrection.getWord();
			else if ( useVerbs && verbCorrection != null && verbCorrection.getDistance() < 1.01 ) 
				corrected = verbCorrection.getWord();
			else if ( useNames && nameCorrection != null && nameCorrection.getDistance() < 1.01 )
				corrected = nameCorrection.getWord();
		}
		else
		{
			//we don't correct anything for words of length <= minlength, but if we recognized the word as a verb, we set lowercase for consistency.
			//(not for nouns, we don't have data about which words of length <= 2 appear in reference names)
			if ( useVerbs && lang.isVerb(word) )
				corrected = word;
		}
		/*
		if ( corrected != null ) word = corrected;
		if ( !st.hasMoreTokens() ) return firstWord;
		else return firstWord + " " + st.nextToken("");
		*/
		return corrected;
	}
	
	/**
	 * Performs verb correction on the first word in the string,
	 * verb/name correction on the rest.
	 * @param s Command string to correct, example "mriar cocche".
	 * @return Corrected string, example "mirar coche".
	 */
	public String correctCommandString ( String s )
	{
		StringTokenizer st = new StringTokenizer ( s );
		StringBuffer result = new StringBuffer();
		if ( !st.hasMoreTokens() ) return s;
		String firstWord = st.nextToken();
		String correctedFirstWord = correctVerbOrName(firstWord,true,true,true);
		result.append(correctedFirstWord);
		while ( st.hasMoreTokens() )
		{
				String nextWord = st.nextToken();
				String correctedNextWord = correctVerbOrName(nextWord,false,true,true);
				result.append(" ");
				result.append(correctedNextWord);
		}
		return result.toString();
	}
	
	/**
	 * Adds a new name to the names known by the spell checker.
	 * This can be used by games that add reference names dynamically.
	 * @param s The name to be added.
	 */
	public void addNewName ( String s )
	{
		nameCorrector.addReferenceName(s);
	}
	
	
}
