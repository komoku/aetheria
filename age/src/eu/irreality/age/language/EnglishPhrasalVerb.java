/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 27/11/2011 13:00:54
 */
package eu.irreality.age.language;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author carlos
 *
 * Represents an English phrasal verb.
 */
public class EnglishPhrasalVerb 
{
	
	public boolean separable; /*Whether the object can appear in between verb and particle(s)*/
	
	private String firstComponent=""; /*First part to be matched, e.g. in pick * up, pick*/
	
	private String secondComponent=""; /*Second part to be matched if any, e.g. in pick * up, up*/
	
	private Pattern firstPattern = null;
	
	private Pattern secondPattern = null;
	
	/**
	 * Constructs an English phrasal verb object from a descriptive string.
	 * Such strings have a format like:
	 * 1. pick * up * (to indicate that the object can be in between or at the end)
	 * 2. zero in on * (no object in between)
	 * 3. show up (no object at all, but we won't really distinguish between this and case 2,
	 * we are only interested on whether objects can appear in between)
	 * @param description
	 */
	public EnglishPhrasalVerb ( String description )
	{
		description = description.trim();
		
		//we're not using placeholders at end of descriptions, so remove them:
		if ( description.endsWith("*") )
			description = description.substring(0,description.length()-1); 
		
		//now if there's a placeholder left then the verb is separable:
		separable = description.contains("*");
		
		//and we store its parts:
		StringTokenizer st = new StringTokenizer(description,"*");
		if ( st.hasMoreTokens() )
		{
			firstComponent = st.nextToken().trim(); 
			firstPattern = 
	            Pattern.compile("^"+firstComponent+"\b");
		}
		if ( st.hasMoreTokens() ) 
		{
			secondComponent = st.nextToken().trim(); 
			secondPattern = 
	            Pattern.compile("\b"+secondComponent+"\b");
		}
	}
	
	/**
	 * Returns whether this phrasal verb is separable, i.e., if the object can
	 * appear between the verb and the particle (as in "pick someone up").
	 * @return
	 */
	public boolean isSeparable()
	{
		return separable;
	}
	
	/**
	 * Returns whether this phrasal verb is the main verb of the given imperative
	 * sentence.
	 * @param sentence
	 * @return
	 */
	public boolean matches ( String sentence )
	{
		if ( !separable )
			return sentence.trim().matches(firstComponent+"\b.*");
		else
			return sentence.trim().matches(firstComponent+"\b.*\b"+secondComponent+"\b.*");
	}
	
	/**
	 * Precondition: matches(sentence) is true.
	 * @param sentence
	 * @return
	 */
	public String extractVerb ( String sentence )
	{
		if ( !separable )
		{
			Matcher matcher = firstPattern.matcher(sentence);
			if ( matcher.find() )
				return sentence.substring(matcher.start(),matcher.end());
			else
				return null;
		}
		else
		{
			//NOT DONE, MODIFY THIS
			Matcher matcher = firstPattern.matcher(sentence);
			if ( matcher.find() )
				return sentence.substring(matcher.start(),matcher.end());
			else
				return null;
		}
	}
	
	
}
