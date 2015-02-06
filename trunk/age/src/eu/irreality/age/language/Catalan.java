package eu.irreality.age.language; 

import java.util.StringTokenizer;

import eu.irreality.age.Mobile;
import eu.irreality.age.NaturalLanguage;
import eu.irreality.age.StringMethods;
import eu.irreality.age.World;
import eu.irreality.age.spell.ReferenceNameCorrector;
import eu.irreality.age.spell.SimpleReverseCorrector;

public class Catalan extends NaturalLanguage
{
	
	public Catalan()
	{
		super("ca");
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
	
	//
	//sustituye los comandos al final del verbo por las ZR's correspondientes
	//this method NO LONGER checks if the input is actually a verb, that should be checked outside.
	//however, it now (2013-05-07) checks if the OUTPUT is a verb - it won't return outputs with unrecognized verbs.
	public String substitutePronouns ( Mobile p , String command , Mentions mentions )
	{
		String thestring = command;	
		boolean doneSomething = false;
		
		
		if ( firstWord(thestring).toLowerCase().endsWith ( "-les" ) && firstWord(thestring).length() > 4 ) //les agafar-les o agafa-les
		{
			//Pronombre femenino plural.
			doneSomething = true;
			//lo quitamos
			String cut = firstWord(thestring).substring(0,firstWord(thestring).length()-4);  // -4 hay que quitar el guión
			//añadimos la ZR femenina plural
			thestring = cut + " " + mentions.getLastMentionedObjectFP() + " " + restWords(thestring);
			doneSomething = true;
		}
		else if ( firstWord(thestring).toLowerCase().endsWith ( "-los" ) && firstWord(thestring).length() > 4 ) //los agafar-los o agafa-los
		{
			//Pronombre masculino o neutro plural.
			doneSomething = true;
			//lo quitamos
			String cut = firstWord(thestring).substring(0,firstWord(thestring).length()-4); // -4 hay que quitar el guión
			//añadimos la ZR plural
			thestring = cut + " " + mentions.getLastMentionedObjectP() + " " +  restWords(thestring);
		}
		else if ( firstWord(thestring).toLowerCase().endsWith ( "-ho" ) && firstWord(thestring).length() > 3 ) // ho agafar-ho 
		{
			//Pronombre masculino singular
			doneSomething = true;
			//lo quitamos
			String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-3); // -3 hay que quitar el guión
			//añadimos la ZR masculina singular
			thestring = cut + " " + mentions.getLastMentionedObjectMS() + " " +  restWords(thestring);
		}
		else if ( firstWord(thestring).toLowerCase().endsWith ( "-la" ) && firstWord(thestring).length() > 3 ) // agafar-la
		{
			//Pronombre femenino singular
			doneSomething = true;
			//lo quitamos
			String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-3); // -3 hay que quitar el guión
			//añadimos la ZR masculina singular
			thestring = cut + " " + mentions.getLastMentionedObjectFS() + " " +  restWords(thestring);
		}
		else if ( ( firstWord(thestring).toLowerCase().endsWith ( "'ls" )   // agafa'ls 
		         || firstWord(thestring).toLowerCase().endsWith ( "'ls" ) )	// los dos tipos de apostrofe	
		         && firstWord(thestring).length() > 3 ) // agafa'ls igual para el masculino y fenemino
		{
		    //Pronombre femenino/masculino plural
			doneSomething = true;
			//lo quitamos
			String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-3); // -3 hay que quitar el apostrofe
			//añadimos la ZR masculina singular
			thestring = cut + " " + mentions.getLastMentionedObjectFS() + " " +  restWords(thestring);
		
		}
		else if ( ( firstWord(thestring).toLowerCase().endsWith ( "'l" )   // agafa'l, con el apostrofe, hay palabras que acaban en l
		         || firstWord(thestring).toLowerCase().endsWith ( "'l" ) )  // los dos tipos de apostrofe		
		         && firstWord(thestring).length() > 2 ) // agafa'l 
		{
		    //Pronombre femenino/masculino singular
			doneSomething = true;
			//lo quitamos
			String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-2); // -3 hay que quitar el apostrofe
			//añadimos la ZR masculina singular
			thestring = cut + " " + mentions.getLastMentionedObjectFS() + " " +  restWords(thestring);
		
		}
		
		//intermediate checkpoint: we store the string at this point, and whether the first word in this current string has a recognized verb.
		//we store this because sometimes a verb may look like it has two clitics, but only actually have one.
		//for example, suppose the input "mételo" (or "metelo").
		//after we substitute "lo", what we have ("mete") is a verb.
		//if we keep substituting (see below) thinking that "te" is a clitic, we will reach "me", but this is no longer a verb. "te" was not a clitic, it was a verb termination!
		//in cases like that, we will fall back to this intermediate checkpoint.
		boolean intermStringIsMeaningful = false;
		if ( doneSomething && isVerb(removeAccents(firstWord(thestring))) )
			intermStringIsMeaningful = true; //we have done some substitution, and obtained something that can be a verb.
		String intermString = thestring;
			
		//rentar-me  
		//rentar-te
		//rentar-se
		
		//these pronouns can appear with the above ones (in combinations like melo, tela, selo, etc.) They can also appear on its own.
		if ( firstWord(thestring).toLowerCase().endsWith ( "me" ) || firstWord(thestring).toLowerCase().endsWith ( "te" ) || firstWord(thestring).toLowerCase().endsWith ( "se" ) )
		{
		    /*
		     este if creo que no hace falta, los puntos cardinales en catalán no se escribe así
			*/
		    if ( !firstWord(thestring).toLowerCase().endsWith("este") && !firstWord(thestring).toLowerCase().endsWith("norte")  /*&& lenguaje.esVerboComando ( thestring.substring(0,thestring.length()-2) ) */ )
			{
				//Pronombre, se refiere al jugador
				doneSomething = true;
				//lo quitamos
				String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-3); // hay que quitar el guión
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
		
		//if we haven't done any substitutions, we return the original string.
		if ( !doneSomething ) return command;
		
		if ( !isVerb(removeAccents(firstWord(thestring))) )
		{
			if ( intermStringIsMeaningful )
			{
				//this is the case where we have made two substitutions, so that the first one produced a correct verb,
				//but the last one produced something that is not a verb (like substituting "te" and "lo" in "mételo").
				//in this case, we go back to the intermediate result (substituting "lo" but not "te"), which is likely to be the correct one according to our verb lists.
				thestring = intermString;
			}
			else
			{
				//in this case, neither the first nor the second step of the substitution produced a known verb.
				//therefore, we return the original string (we could be facing a noun ending with something that looks like a critic but isn't, like
				//"bote" or "cola" - substitutions shouldn't have an effect in this case).
				return command;
			}
		}
			
		//{when we reach this point, we have made some substitution and thestring starts with a recognized verb}
		StringTokenizer st = new StringTokenizer(thestring.toLowerCase());
		String newVerb = st.nextToken().trim();
		String unaccentedVerb = this.removeAccents(newVerb); //verbos pierden acentos: cógelo -> cóge <tal> -> coge <tal>
		return unaccentedVerb + st.nextToken("");
	}
	
	
	//sustituye los comandos al final del verbo por las ZR's correspondientes
	//tambien corrige el verbo
	//UNUSED
	public String substitutePronounsIfVerb ( Mobile p , String command , Mentions mentions )
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
    
	// a + el = al       a + els = als     
	// de + el = del     de + els = dels
	// per + el = pel    per + els = pels
	
	private String doContractions ( String s )
    {
        String result = s.replaceAll(" a el"," al");
        result = result.replaceAll(" de el"," del");
        result = result.replaceAll(" per el"," pel");
        result = result.replaceAll(" a els"," als");
        result = result.replaceAll(" de els"," dels");
        result = result.replaceAll(" per els"," pels");
        result = result.replaceAll(" la a", " l'a");   // la avioneta -> l'avioneta
        result = result.replaceAll(" la e", " l'e"); 
        result = result.replaceAll(" la o", " l'o");
        result = result.replaceAll(" el a", "l'a");   // el avión -> l'avión
        result = result.replaceAll(" el e", "l'e");
        result = result.replaceAll(" el o", "l'o");
        
        return result;
    }
    
	
	public String correctMorphology ( String s )
	{
		if ( s == null ) return null;
		String niceString = super.correctMorphology(s);
		niceString = doContractions(niceString);
		//TODO This is a temporary workaround due to the limitations of $oa to parameterize messages as male/female. A more flexible system for that should be added and then this will become unnecessary.
		return StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( niceString , "tancado" , " tancat" ) , "mateixo" , "mateix" );
	}
	


	public String correctMorphologyWithoutTrimming ( String s )
	{
		if ( s == null ) return null;
		String niceString = super.correctMorphologyWithoutTrimming(s);
		niceString = doContractions(niceString);
		//TODO This is a temporary workaround due to the limitations of $oa to parameterize messages as male/female. A more flexible system for that should be added and then this will become unnecessary.
		return StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( niceString , "tancado" , " tancat" ) , "mateixo" , "mateix" );
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
		base.addDictionaryWord("sud-oest");
		base.addDictionaryWord("sud-est");
		base.addDictionaryWord("nord-est");
		base.addDictionaryWord("nord-oest");
		
		//add articles so that, for example, the article "las" is not corrected to the noun "alas"
		base.addDictionaryWord("el");
		base.addDictionaryWord("la");
		base.addDictionaryWord("els");
		base.addDictionaryWord("les");
		
		base.addDictionaryWord("un");
		base.addDictionaryWord("una");
		base.addDictionaryWord("uns");
		base.addDictionaryWord("unes");
		
		//"con": avoid confusion with verb "pon"
		//base.addDictionaryWord("con");
		
		return base;
	}
	
	
}
