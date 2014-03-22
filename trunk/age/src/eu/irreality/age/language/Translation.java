package eu.irreality.age.language;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import eu.irreality.age.StringMethods;
import eu.irreality.age.Utility;
import eu.irreality.age.filemanagement.Paths;

/**
 * This class translates verbs from one language to another. In order to do it,
 * a verb translation list file must be available.
 * @author carlos
 *
 */
public class Translation 
{
	
	/**
	 * This HashMap goes from a pair of codes separated by a dash (for example es-en) to anoter HashMap
	 * which translates verbs in the first language (in the example, es) to verbs in the second language
	 * (in the example, es).
	 */
	private static HashMap translationTables = new HashMap();

	/**
	 * Gets the path to a verb translation file.
	 * @return
	 */
	public static String getTranslationFilePath ( String sourceLanguageCode , String targetLanguageCode )
	{
		return Paths.LANG_FILES_PATH + "/tr-" + sourceLanguageCode + "-" + targetLanguageCode + ".lan";
	}
	
	/**
	 * Gets the translation map from a source language to a target language, loading it from a file
	 * if it has not been loaded before.
	 * @param sourceLanguageCode
	 * @param targetLanguageCode
	 * @return
	 */
	public static Map getTranslationMap ( String sourceLanguageCode , String targetLanguageCode )
	{
		Map theMap = (Map) translationTables.get ( sourceLanguageCode + "-" + targetLanguageCode );
		if ( theMap == null )
		{
			try
			{
				theMap = loadTranslationMap ( sourceLanguageCode , targetLanguageCode );
				translationTables.put ( sourceLanguageCode + "-" + targetLanguageCode , theMap ); //so that we don't need to load it again
			}
			catch ( IOException ioe )
			{
				System.err.println("Warning: could not find verb translation file from " + sourceLanguageCode + " to " + targetLanguageCode+". This could cause inability to recognize verbs in " + sourceLanguageCode);
			}
		}
		return theMap;
	}
	
	/**
	 * Loads a translation map from a source language to a target language from the corresponding
	 * file.
	 * @param sourceLanguageCode
	 * @param targetLanguageCode
	 * @return
	 */
	public static Map loadTranslationMap ( String sourceLanguageCode , String targetLanguageCode ) throws IOException , FileNotFoundException 
	{
		String path = getTranslationFilePath ( sourceLanguageCode , targetLanguageCode );
		return LanguageUtils.loadTableFromPath( path , '='  );
	}
	
	/**
	 * Translates a verb from source language to target language via the table taken from the translation file. Returns null if the translation
	 * is not found (be it because the file does not exist or because it does not contain the verb).
	 * @param verb
	 * @param sourceLanguage
	 * @param targetLanguage
	 * @return
	 */
	public static String translate ( String verb , String sourceLanguage , String targetLanguage )
	{
		Map m = getTranslationMap ( sourceLanguage , targetLanguage );
		if ( m == null ) return null;
		else return (String) m.get(verb);
	}
	
}
