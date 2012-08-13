package eu.irreality.age.language;

import java.util.StringTokenizer;

import eu.irreality.age.NaturalLanguage;
import eu.irreality.age.Player;
import eu.irreality.age.StringMethods;
import eu.irreality.age.World;
import eu.irreality.age.spell.ReferenceNameCorrector;
import eu.irreality.age.spell.SimpleReverseCorrector;

public class Spanish extends NaturalLanguage
{
	
	public Spanish()
	{
		super("es");
	}
	
	
	private static String firstWord ( String s )
	{
	    StringTokenizer st = new StringTokenizer(s);
	    if ( st.hasMoreTokens() ) return st.nextToken();
	    else return "";
	}
	
	private static String restWords ( String s )
	{
	    StringTokenizer st = new StringTokenizer(s);
	    if ( !st.hasMoreTokens() ) return "";
	    else
	    {
		st.nextToken();
		if ( !st.hasMoreTokens() ) return "";
		else return st.nextToken("");
	    }
	}
	
	//sustituye los comandos al final del verbo por las ZR's correspondientes
	//this method NO LONGER checks if the input is actually a verb, that should be checked outside.
	public String substitutePronouns ( Player p , String command , Mentions mentions )
	{
		String thestring = command;	
		boolean doneSomething = false;
		
		if ( firstWord(thestring).toLowerCase().endsWith ( "las" ) && firstWord(thestring).length() > 3 )
		{
			//Pronombre femenino plural.
			doneSomething = true;
			//lo quitamos
			String cut = firstWord(thestring).substring(0,firstWord(thestring).length()-3);
			//añadimos la ZR femenina plural
			thestring = cut + " " + mentions.getLastMentionedObjectFP() + " " + restWords(thestring);
			doneSomething = true;
		}
		else if ( firstWord(thestring).toLowerCase().endsWith ( "los" ) && firstWord(thestring).length() > 3 )
		{
			//Pronombre masculino o neutro plural.
			doneSomething = true;
			//lo quitamos
			String cut = firstWord(thestring).substring(0,firstWord(thestring).length()-3);
			//añadimos la ZR plural
			thestring = cut + " " + mentions.getLastMentionedObjectP() + " " +  restWords(thestring);
		}
		else if ( firstWord(thestring).toLowerCase().endsWith ( "lo" ) && firstWord(thestring).length() > 2 )
		{
			//Pronombre masculino singular
			doneSomething = true;
			//lo quitamos
			String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-2);
			//añadimos la ZR masculina singular
			thestring = cut + " " + mentions.getLastMentionedObjectMS() + " " +  restWords(thestring);
		}
		else if ( firstWord(thestring).toLowerCase().endsWith ( "la" ) && firstWord(thestring).length() > 2 )
		{
			//Pronombre femenino singular
			doneSomething = true;
			//lo quitamos
			String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-2);
			//añadimos la ZR masculina singular
			thestring = cut + " " + mentions.getLastMentionedObjectFS() + " " +  restWords(thestring);
		}
		
		//these can appears with the others
		if ( firstWord(thestring).toLowerCase().endsWith ( "me" ) || firstWord(thestring).toLowerCase().endsWith ( "te" ) || firstWord(thestring).toLowerCase().endsWith ( "se" ) )
		{
			if ( !firstWord(thestring).toLowerCase().endsWith("este") && !firstWord(thestring).toLowerCase().endsWith("norte")  /*&& lenguaje.esVerboComando ( thestring.substring(0,thestring.length()-2) ) */ )
			{
				//Pronombre, se refiere al jugador
				doneSomething = true;
				//lo quitamos
				String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-2);
				//añadimos el nombre del jugador
				String playerRefName = p.getBestReferenceName(false);
				if ( playerRefName == null )
				{
					p.writeError("Error in player " + p + ": cannot apply pronoun substitution to \"" + firstWord(thestring) + "\" because player has no reference name. Add a singular reference name to fix this.\n" ); 
					return command;
				}
				thestring = cut + " " + playerRefName + " " +  restWords(thestring);
			}
		}

		//escribir ( "Substituted string " + thestring );

		//sanity check (added 2010.04.30): did we obtain a recognised verb? if not, the substitution is not valid (cases like habla -> "hab <so and so>"
		if ( !doneSomething ) return command;
		
		//{we have done some substitution}
		StringTokenizer st = new StringTokenizer(thestring.toLowerCase());
		String newVerb = st.nextToken().trim();
		String unaccentedVerb = this.removeAccents(newVerb); //verbos pierden acentos: cógelo -> cóge <tal> -> coge <tal>
		return unaccentedVerb + st.nextToken("");
	}
	
	
	
	
	//sustituye los comandos al final del verbo por las ZR's correspondientes
	//tambien corrige el verbo
	//UNUSED
	public String substitutePronounsIfVerb ( Player p , String command , Mentions mentions )
	{

		StringTokenizer st = new StringTokenizer(command);
		String origVerb = st.nextToken();
		
		String newCommand = substitutePronouns(p,command,mentions);
		newCommand = newCommand.trim();
		
		st = new StringTokenizer(newCommand);
		String uncorrectedVerbWithoutPronouns = st.nextToken();
		
		if ( !p.getPropertyValueAsBoolean("noVerbSpellChecking") )
			newCommand = this.correctVerb(newCommand);
		
		st = new StringTokenizer(newCommand);
		String correctedVerbWithoutPronouns = st.nextToken();

		if ( !this.isVerb(correctedVerbWithoutPronouns) ) //maybe change to corrected verb to correct things like "mriate"?
		{
			//check failed! no way to find a verb here, return original string.
			return command;
		}
		else if ( this.isVerb(correctedVerbWithoutPronouns) && //verb is recognised only if it is corrected, and pronoun substitution has taken place
				!this.isVerb(uncorrectedVerbWithoutPronouns) && 
				!uncorrectedVerbWithoutPronouns.equals(origVerb) )
		{
			return command;
		}
		else
		{
			//check passed.
			return newCommand;
		}
		
	}
	
	
	



	public String correctMorphology ( String s )
	{
		if ( s == null ) return null;
		String niceString = super.correctMorphology(s);
		niceString = niceString.replaceAll(" y i"," e i");
		niceString = niceString.replaceAll(" y í"," e í");
		return StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( niceString , " a el" , " al" ) , " de el" , " del" );
	}
	


	public String correctMorphologyWithoutTrimming ( String s )
	{
		if ( s == null ) return null;
		String niceString = super.correctMorphologyWithoutTrimming(s);
		niceString = niceString.replaceAll(" y i"," e i");
		niceString = niceString.replaceAll(" y í"," e í");
		return StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( niceString , " a el" , " al" ) , " de el" , " del" );
	}
	

	
	/**
	 * Gets a Spanish corrector for the reference names of the given world.
	 * @param w
	 * @return
	 */
	public ReferenceNameCorrector initNameCorrector ( World w )
	{
		ReferenceNameCorrector base = super.initNameCorrector(w);
		
		//add common synonyms of cardinal directions 
		base.addDictionaryWord("noreste");
		base.addDictionaryWord("sureste");
		base.addDictionaryWord("sudoeste");
		
		//add articles so that, for example, the article "las" is not corrected to the noun "alas"
		base.addDictionaryWord("el");
		base.addDictionaryWord("la");
		base.addDictionaryWord("los");
		base.addDictionaryWord("las");
		
		//"con": avoid confusion with verb "pon"
		base.addDictionaryWord("con");
		
		return base;
	}
	
	
}
