/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.lang.*;
import java.io.*;
import java.util.*;

import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.language.English;
import eu.irreality.age.language.LanguageUtils;
import eu.irreality.age.language.Mentions;
import eu.irreality.age.language.Spanish;
import eu.irreality.age.language.Translation;
import eu.irreality.age.spell.Correction;
import eu.irreality.age.spell.ReferenceNameCorrector;
import eu.irreality.age.spell.SimpleReverseCorrector;
import eu.irreality.age.spell.SpellingCorrector;

public class NaturalLanguage
{

	private static String defaultVerbPath = Paths.LANG_FILES_PATH + "/verbos.lan";
	private static String defaultSynonymPath = Paths.LANG_FILES_PATH + "/sinon.lan";
	private static String defaultAliasPath = Paths.LANG_FILES_PATH + "/alias.lan";
	private static String defaultVerb32Path = Paths.LANG_FILES_PATH + "/verbos32.lan";

	private Map imperativoAInfinitivo;
	private Map infinitivoAImperativo;
	private Map sinonimos;
	private Map alias;
	private Map terceraASegunda;
	
	private SpellingCorrector corrector;
	
	public static String DEFAULT_LANGUAGE_CODE = "es";
	
	/**
	 * The ISO code for this language.
	 */
	private String languageCode = null;
	
	/**
	 * ISO language code.
	 * @return
	 */
	public String getLanguageCode()
	{
		return languageCode;
	}
	
	/**
	 * Obtain path to verb file.
	 * @return
	 */
	String getVerbPath ( )
	{
		if ( languageCode != null ) return Paths.LANG_FILES_PATH + "/" + languageCode + "/verbos.lan";
		return defaultVerbPath;
	}
	
	/**
	 * Obtain path to synonym file (unused).
	 * @return
	 */
	String getSynonymPath ( )
	{
		if ( languageCode != null ) return Paths.LANG_FILES_PATH + "/" + languageCode + "/sinon.lan";
		return defaultSynonymPath;
	}
	
	/**
	 * Obtain path to alias file.
	 * @return
	 */
	String getAliasPath ( )
	{
		if ( languageCode != null ) return Paths.LANG_FILES_PATH + "/" + languageCode + "/alias.lan";
		return defaultAliasPath;
	}
	
	/**
	 * Obtain path to verb 3rd to 2nd person file.
	 * @return
	 */
	String getVerb32Path ( )
	{
		if ( languageCode != null ) return Paths.LANG_FILES_PATH + "/" + languageCode + "/verbos32.lan";
		return defaultVerb32Path;
	}
	
	/**Verbs that are considered guessable by second-chance mode 
	 * even if the guess policy is set to false
	 * */
	private Set guessable = new LinkedHashSet();
	
	/**
	 * Verbs that are considered not guessable in second-chance mode
	 * even if the guess policy is set to true
	 */
	private Set unguessable = new LinkedHashSet();
	
	/**
	 * If true, all verbs are guessable unless in the unguessable set.
	 * (default).
	 * If false, all verbs are unguessable unless in the guessable set.
	 */
	private boolean defaultGuessPolicy = true;
	
	public void setUnguessable ( String verb )
	{
		unguessable.add(verb);
		guessable.remove(verb);
	}
	
	public void setGuessable ( String verb )
	{
		unguessable.remove(verb);
		guessable.add(verb);
	}
	
	public void setAllGuessable ( )
	{
		unguessable.clear();
		guessable.clear();
		defaultGuessPolicy = true;
	}
	
	public void setAllUnguessable ( )
	{
		unguessable.clear();
		guessable.clear();
		defaultGuessPolicy = false;
	}
	
	public boolean isGuessable ( String verb )
	{
		if ( defaultGuessPolicy )
		{
			return !unguessable.contains(verb);
		}
		else
		{
			return guessable.contains(verb);
		}
	}

	public static NaturalLanguage getInstance ( )
	{
		return getInstance ( DEFAULT_LANGUAGE_CODE );
	}
	
	public static NaturalLanguage getInstance ( String languageCode )
	{
		if ( languageCode.equals("es") )
			return new Spanish();
		else if ( languageCode.equals("en") )
			return new English();
		else
			return new NaturalLanguage(languageCode);
	}
	
	private NaturalLanguage ( )
	{
		this( DEFAULT_LANGUAGE_CODE );
	}
	
	protected NaturalLanguage ( String languageCode )
	{
		this.languageCode = languageCode;
		
		//load the files needed for the natural language utils to work
		
		try
		{
			imperativoAInfinitivo = LanguageUtils.loadTableFromPath ( getVerbPath() , '=' );
			infinitivoAImperativo = LanguageUtils.loadInvertedTableFromPath ( getVerbPath() , '=' , false );
			//false: en este caso solo las primeras, i.e. a imperativo, no a 1ª pers
		}
		catch ( Exception exc )
		{
			System.err.println("Aviso: no se ha encontrado fichero de verbos, la tabla de verbos estará vacía.");
			exc.printStackTrace();
			imperativoAInfinitivo = new Hashtable(1);
		}
		
		try
		{
			sinonimos = LanguageUtils.loadTableFromPath ( getSynonymPath() , '=' );
		}
		catch ( Exception exc )
		{
			System.err.println("Aviso: no se ha encontrado fichero de sinónimos, la tabla de sinónimos estará vacía.");
			sinonimos = new Hashtable(1);
		}
		try
		{
			alias = LanguageUtils.loadTableFromPath ( getAliasPath() , '=' );
		}
		catch ( Exception exc )
		{
			System.err.println("Aviso: no se ha encontrado fichero de alias, la tabla de alias estará vacía.");
			alias = new Hashtable(1);
		}
		try
		{
			terceraASegunda = LanguageUtils.loadTableFromPath ( getVerb32Path() , ' ' );
		}
		catch ( Exception exc )
		{
			System.err.println("Aviso: no se ha encontrado fichero de conjugación en 2ª persona, la tabla estará vacía.");
			terceraASegunda = new Hashtable(1);
		}
	}
	

	/**
	 * @deprecated Use {@link #toInfinitive(String)} instead
	 */
	public String imperativoAInfinitivo ( String presente )
	{
	    return toInfinitive(presente);
	}

	public String toInfinitive ( String presente )
	{
		return (String) imperativoAInfinitivo.get ( presente.toLowerCase() );
	}

	public String obtenerSinonimo ( String palabra )
	{
		return (String) sinonimos.get ( palabra.toLowerCase() );
	}
	
	/**
	 * @deprecated Use {@link #getAlias(String)} instead
	 */
	public String obtenerAlias ( String palabra )
	{
		return getAlias(palabra);
	}

	public String getAlias ( String palabra )
	{
		return (String) alias.get ( palabra.toLowerCase() );
	}
	
	public String sustituirSinonimos ( String s )
	{
		StringTokenizer st = new StringTokenizer ( s , " " , true );
		String nueva = "";
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			String sin = obtenerSinonimo(tok);
			if ( sin == null )
			{
				nueva += tok;
			}
			else
			{
				nueva += sin;
			}
		}
		return nueva;
	}
	
	public String sustituirVerbos ( String s )
	{
		StringTokenizer st = new StringTokenizer ( s , " " , true );
		String nueva = "";
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			String sin = toInfinitive(tok);
			if ( sin == null )
			{
				nueva += tok;
			}
			else
			{
				nueva += sin;
			}
		}
		return nueva;
	}
	
	//sustituye verbo como en sustituirVerbos() pero sólo si el verbo es la primera palabra
	public String sustituirVerbo ( String s )
	{
		StringTokenizer st = new StringTokenizer ( s , " " , true );
		String nueva = "";
		int tokcnt = 0;
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			String sin = toInfinitive(tok);
			if ( sin == null || tokcnt > 0 )
			{
				nueva += tok;
			}
			else
			{
				nueva += sin;
			}
			tokcnt++;
		}
		return nueva;
	}
	
	//devuelve true si la palabra dada es un posible comando. (ir, salir...) false si no. (mesa...)
	/**
	 * @deprecated Use {@link #isVerb(String)} instead
	 */
	public boolean esVerboComando ( String s )
	{
	    return isVerb(s);
	}

	//devuelve true si la palabra dada es un posible comando. (ir, salir...) false si no. (mesa...)
	public boolean isVerb ( String s )
	{
		if ( infinitivoAImperativo.get(s.toLowerCase().trim()) != null ||
			imperativoAInfinitivo.get(s.toLowerCase().trim()) != null )
		{
			return true;
		}
		else return false;
	}
	
	/**
	 * Adds an entry to the verb table.
	 * @param imperative Imperative or 1st person form of the verb.
	 * @param infinitive Infinitive form of the verb.
	 */
	public void addVerbEntry ( String imperative , String infinitive )
	{
		imperativoAInfinitivo.put(imperative,infinitive);
		infinitivoAImperativo.put(infinitive,imperative);
	}
	
	public void removeVerbEntry ( String imperative , String infinitive )
	{
		imperativoAInfinitivo.remove(imperative);
		infinitivoAImperativo.remove(infinitive);
	}
	
	/**
	 * Removes an verb in all of its forms, given the infinitive, if it exists.
	 * @param source
	 */
	public void removeVerbEntry ( String infinitive )
	{
		String imper = (String) infinitivoAImperativo.get(infinitive);
		infinitivoAImperativo.remove(infinitive);
		if ( imper != null )
		{
			imperativoAInfinitivo.remove(imper);
		}
	}
	
	public Set getVerbForms()
	{
		return imperativoAInfinitivo.keySet();
	}
	
	/**
	 * Adds an entry to the aliases table.
	 * @param source The source of the alias.
	 * @param target The target of the alias.
	 */
	public void addAlias ( String source , String target )
	{
		alias.put(source,target);
	}
	
	/**
	 * Removes a (source,target) entry from the alias association if it exists.
	 * @param source
	 * @param target
	 */
	public void removeAlias ( String source , String target )
	{
		if ( alias.get(source).equals(target) )
			removeAlias(source);
	}
	
	/**
	 * Removes an alias if it exists.
	 * @param source
	 */
	public void removeAlias ( String source )
	{
		alias.remove(source);
	}
	

	
	/**
	 * Devuelve Comprueba si una palabra dada es un verbo, incluyendo soporte de "le".
	 * @param s Palabra a comprobar.
	 * @param includeLe true si se quiere que se admitan como verbo formas con el sufijo le.
	 * @return true si la palabra dada es un verbo reconocido (en imperativo, infinitivo o 1ª persona).
	 * Si el parámetro includeLe es true, entonces también devuelve true si es un verbo al que se ha
	 * añadido "le" (escupirle, dale, beberle)
	 */
	public boolean isVerb ( String s , boolean includeLe )
	{
		if ( !includeLe ) return isVerb(s);
		else
		{
			if ( isVerb(s) ) return true;
			else
			{
				if ( s.endsWith("le") )
				{
					String verbForm = s.substring(0,s.length()-2);
					return isVerb(verbForm);
				}
				else if ( s.endsWith("les") )
				{
					String verbForm = s.substring(0,s.length()-3);
					return isVerb(verbForm);
				}
				else
					return false;
			}
		}
	}
	
	
	public String sustituirAlias ( String s )
	{
		/*
		String al = obtenerAlias(s);
		if ( al == null ) return s;
		else return al;
		*/
		StringTokenizer st = new StringTokenizer ( s , " " , true );
		String nueva = "";
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			String sin = getAlias(tok);
			if ( sin == null )
			{
				nueva += tok;
			}
			else
			{
				nueva += sin;
			}
		}
		return nueva;
	}
	
	public String terceraASegunda ( String verbo )
	{
		return (String) terceraASegunda.get ( verbo.toLowerCase() );
	}
	
	/**
	 * By default, do nothing language-specific. Will be overridden by concrete languages.
	 * @param s
	 * @return
	 * @deprecated Use {@link #correctMorphology(String)} instead
	 */
	public String gramaticalizar ( String s )
	{
		return correctMorphology(s);
	}

	/**
	 * By default, do nothing language-specific. Will be overridden by concrete languages.
	 * @param s
	 * @return
	 */
	public String correctMorphology ( String s )
	{
		if ( s == null ) return null;
		String temp = s;
		temp = temp.trim();
		if ( temp.length() > 0 )
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
		if ( temp.length() > 0 )
			if ( temp.charAt(temp.length()-1) != '.' ) temp += ".";
		return temp;
	}
	
	/**
	 * By default, do nothing language-specific. Will be overridden by concrete languages.
	 * @param s
	 * @return
	 * @deprecated Use {@link #correctMorphologyWithoutTrimming(String)} instead
	 */
	public String gramaticalizarSinTrimear ( String s )
	{
		return correctMorphologyWithoutTrimming(s);
	}

	/**
	 * By default, do nothing language-specific. Will be overridden by concrete languages.
	 * @param s
	 * @return
	 */
	public String correctMorphologyWithoutTrimming ( String s )
	{
		if ( s == null ) return null;
		String temp = s;
		if ( temp.length() > 0 )
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
		if ( temp.length() > 0 )
			if ( temp.charAt(temp.length()-1) != '\n' && temp.charAt(temp.length()-1) != '.' ) temp += ".";
		return temp;
	}
	
	public String removeAccents ( String s )
	{
		s = s.replaceAll("[èéêë]","e");
	    s = s.replaceAll("[ûùúü]","u");
	    s = s.replaceAll("[ïîí]","i");
	    s = s.replaceAll("[àâá]","a");
	    s = s.replaceAll("[óòô]","o");
	    s = s.replaceAll("[ÈÉÊË]","E");
	    s = s.replaceAll("[ÛÙÚÜ]","U");
	    s = s.replaceAll("[ÏÎÍ]","I");
	    s = s.replaceAll("[ÀÂÁ]","A");
	    s = s.replaceAll("[ÓÒ]","O");
		return s;
	}
	
	public void initVerbSpellingCorrector ( )
	{
		corrector = new SimpleReverseCorrector();
		for ( Iterator iter = infinitivoAImperativo.keySet().iterator() ; iter.hasNext() ; )
		{
			String nextWord = (String) iter.next();
			corrector.addDictionaryWord(nextWord);
		}
		for ( Iterator iter = imperativoAInfinitivo.keySet().iterator() ; iter.hasNext() ; )
		{
			String nextWord = (String) iter.next();
			corrector.addDictionaryWord(nextWord);
		}
		//System.err.println(corrector);
	}
	
	/**
	 * Gets a corrector for the reference names of the given world.
	 * @param w
	 * @return
	 */
	public ReferenceNameCorrector initNameCorrector ( World w )
	{
		return new ReferenceNameCorrector ( w, new SimpleReverseCorrector() );
	}
	
	private Correction getBestCorrection ( String tentativeVerb )
	{
		if ( corrector == null )
		{
			if ( isVerb(tentativeVerb) ) return new Correction(tentativeVerb,0);
			else return null;
		}
		return corrector.getBestCorrection(tentativeVerb);
	}
	
	/**
	 * Changes a mistyped verb (1st word) in the given command string to a correct one.
	 * @param commandString
	 * @return
	 */
	public String correctVerb ( String commandString )
	{
		if ( corrector == null ) initVerbSpellingCorrector();
		StringTokenizer st = new StringTokenizer ( commandString );
		if ( !st.hasMoreTokens() ) return commandString;
		String verb = st.nextToken();
		Correction c = getBestCorrection(verb);
		if ( verb.length() > 2 && c != null && c.getWord() != null ) //solo corregimos para length > 2 para evitar por ejemplo i->di
		{
			verb = c.getWord();
		}
		if ( !st.hasMoreTokens() ) return verb;
		else return verb + " " + st.nextToken("");
	}

	/**
	 * Returns the language's verb spelling corrector (initialising it first if it was not
	 * already initialised).
	 */
	public SpellingCorrector getVerbSpellingCorrector()
	{
		if ( corrector == null ) initVerbSpellingCorrector();
		return corrector;
	}
	
	
	/**
	 * By default, do nothing. Concrete languages will override this with their
	 * pronoun handling.
	 * @param p
	 * @param command
	 * @param mentions
	 * @return
	 */
	public String substitutePronouns ( Player p , String command , Mentions mentions )
	{
		return command;
	}
	
	/**
	 * By default, do nothing. Concrete languages will override this with their
	 * pronoun handling.
	 * @param p
	 * @param command
	 * @param mentions
	 * @return
	 */
	public String substitutePronounsIfVerb ( Player p , String command , Mentions mentions )
	{
		return command;
	}
	
	
	/**
	 * Translates a verb from this language into another, using the translation tables obtained from the corresponding files.
	 * The nullIfNotFound parameter controls what happens when a translation is not available: if the parameter is true, then the method
	 * returns null in that case, if it is false, then it returns the original verb. 
	 * @param verb
	 * @param targetLanguage
	 * @param nullIfNotFound
	 * @return
	 */
	public String translateVerb ( String verb , String targetLanguage , boolean nullIfNotFound )
	{
		if ( this.getLanguageCode().equals(targetLanguage) ) return verb; //translation from one language to itself
		String translation = Translation.translate( verb , this.getLanguageCode() , targetLanguage );
		if ( translation == null && !nullIfNotFound ) translation = verb;
		return translation;
	}
	
	/**
	 * Translates a verb from this language into another, using the translation tables obtained from the corresponding files.
	 * Returns the verb as it was if a translation is not found.
	 * @param verb
	 * @param targetLanguage
	 * @return
	 */
	public String translateVerb ( String verb , String targetLanguage )
	{
		return translateVerb ( verb , targetLanguage , false );
	}
	
	
}
