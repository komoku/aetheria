package eu.irreality.age.language;

import java.util.StringTokenizer;

import eu.irreality.age.Mobile;
import eu.irreality.age.NaturalLanguage;
import eu.irreality.age.StringMethods;
import eu.irreality.age.World;
import eu.irreality.age.spell.ReferenceNameCorrector;

public class Galician extends NaturalLanguage
{
	
	public Galician()
	{
		super("gl");
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
	
	private String uncontractClitics ( String verbWithPossibleClitics )
	{
		//uncontract clitics for easing processing.
		//for example, "mo" -> "me + o" -> we represent it as "meo" for processing.
		String result = verbWithPossibleClitics.replaceAll("mo$","meo");
		result = result.replaceAll("ma$","mea");
		result = result.replaceAll("mos$","meos");
		result = result.replaceAll("mas$","meas");
		result = result.replaceAll("llo$","lleo");
		result = result.replaceAll("lla$","llea");
		result = result.replaceAll("llos$","lleos");
		result = result.replaceAll("llas$","lleas");
		//the extra ifs are for the special cases coller/recoller/escoller, where the verb ending could be mistaken for a clitic but isn't
		if ( !result.endsWith("collelo") ) result = result.replaceAll("llelo$","lleslo");
		if ( !result.endsWith("collela") ) result = result.replaceAll("llela$","llesla");
		if ( !result.endsWith("collelos") ) result = result.replaceAll("llelos$","lleslos");
		if ( !result.endsWith("collelas") ) result = result.replaceAll("llelas$","lleslas");
		
		//some clitics on infinitive appear transformed:
		//abrir + a -> abrila (not abrira)
		//for ease of processing, we undo this linguistic change of r to l.
		if ( !result.endsWith("lla") ) result = result.replaceAll("la$","ra");
		if ( !result.endsWith("llo") ) result = result.replaceAll("lo$","ro");
		if ( !result.endsWith("llas") ) result = result.replaceAll("las$","ras");
		if ( !result.endsWith("llos") ) result = result.replaceAll("los$","ros");
		
		return result;
	}
	
	//sustituye los comandos al final del verbo por las ZR's correspondientes
	//this method NO LONGER checks if the input is actually a verb, that should be checked outside.
	//however, it now (2013-05-07) checks if the OUTPUT is a verb - it won't return outputs with unrecognized verbs.
	public String substitutePronouns ( Mobile p , String command , Mentions mentions )
	{
		String thestring = command;	
		boolean doneSomething = false;
		
		//uncontract clitics for easier processing.
		String newFirstWord = uncontractClitics (firstWord(thestring));
		thestring = newFirstWord + " " + restWords(thestring);
		
		if ( firstWord(thestring).toLowerCase().endsWith ( "as" ) && firstWord(thestring).length() > 2 )
		{
			//Pronombre femenino plural.
			doneSomething = true;
			//lo quitamos
			String cut = firstWord(thestring).substring(0,firstWord(thestring).length()-2);
			//añadimos la ZR femenina plural
			thestring = cut + " " + mentions.getLastMentionedObjectFP() + " " + restWords(thestring);
			doneSomething = true;
		}
		else if ( firstWord(thestring).toLowerCase().endsWith ( "os" ) && firstWord(thestring).length() > 2 )
		{
			//Pronombre masculino o neutro plural.
			doneSomething = true;
			//lo quitamos
			String cut = firstWord(thestring).substring(0,firstWord(thestring).length()-2);
			//añadimos la ZR plural
			thestring = cut + " " + mentions.getLastMentionedObjectP() + " " +  restWords(thestring);
		}
		else if ( firstWord(thestring).toLowerCase().endsWith ( "o" ) && firstWord(thestring).length() > 1 )
		{
			//Pronombre masculino singular
			doneSomething = true;
			//lo quitamos
			String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-1);
			//añadimos la ZR masculina singular
			thestring = cut + " " + mentions.getLastMentionedObjectMS() + " " +  restWords(thestring);
		}
		else if ( firstWord(thestring).toLowerCase().endsWith ( "a" ) && firstWord(thestring).length() > 1 )
		{
			//Pronombre femenino singular
			doneSomething = true;
			//lo quitamos
			String cut  = firstWord(thestring).substring(0,firstWord(thestring).length()-1);
			//añadimos la ZR masculina singular
			thestring = cut + " " + mentions.getLastMentionedObjectFS() + " " +  restWords(thestring);
		}
		
		//intermediate checkpoint: we store the string at this point, and whether the first word in this current string has a recognized verb.
		//we store this because sometimes a verb may look like it has two clitics, but only actually have one.
		//for example, in Spanish, suppose the input "mételo" (or "metelo").
		//after we substitute "lo", what we have ("mete") is a verb.
		//if we keep substituting (see below) thinking that "te" is a clitic, we will reach "me", but this is no longer a verb. "te" was not a clitic, it was a verb termination!
		//in cases like that, we will fall back to this intermediate checkpoint.
		boolean intermStringIsMeaningful = false;
		if ( doneSomething && isVerb(removeAccents(firstWord(thestring))) )
			intermStringIsMeaningful = true; //we have done some substitution, and obtained something that can be a verb.
		String intermString = thestring;
			

		
		
		//these pronouns can appear with the above ones (in combinations like melo, tela, selo, etc.) They can also appear on its own.
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
	
	
	private String doContractions ( String s )
	{
		String result = s.replaceAll(" a o"," ao");
		result = result.replaceAll(" de o"," do");
		result = result.replaceAll(" con o"," co");
		result = result.replaceAll(" con a"," coa");
		result = result.replaceAll(" por o"," polo");
		result = result.replaceAll(" por a"," pola");
		result = result.replaceAll(" en o"," no");
		result = result.replaceAll(" en a"," na");
		return result;
	}
	
	
	public String correctMorphology ( String s )
	{
		if ( s == null ) return null;
		String niceString = super.correctMorphology(s);
		return doContractions(niceString);
	}
	


	public String correctMorphologyWithoutTrimming ( String s )
	{
		if ( s == null ) return null;
		String niceString = super.correctMorphologyWithoutTrimming(s);
		return doContractions(niceString);
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
		base.addDictionaryWord("nordeste");
		base.addDictionaryWord("sureste");
		base.addDictionaryWord("sudoeste");
		
		//add articles so that, for example, the article "las" is not corrected to the noun "alas"
		//note: i don't think this is really needed for words of length < 3
		//in fact this is all legacy, common.lan should take care of it!
		base.addDictionaryWord("o");
		base.addDictionaryWord("a");
		base.addDictionaryWord("os");
		base.addDictionaryWord("as");
		base.addDictionaryWord("un");
		base.addDictionaryWord("unha");
		base.addDictionaryWord("uns");
		base.addDictionaryWord("unhas");
		
		return base;
	}
	
	/**
	 * Returns the default verb, i.e., verb that will be used by default if a reference name is typed
	 * at the beginning of a game without specifying a verb.
	 */
	public String getDefaultVerb()
	{
		return "mirar";
	}
	
	
}
