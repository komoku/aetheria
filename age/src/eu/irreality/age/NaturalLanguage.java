/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.lang.*;
import java.io.*;
import java.util.*;

import eu.irreality.age.filemanagement.Paths;

public class NaturalLanguage
{

	public static String pathVerbos = Paths.LANG_FILES_PATH + "/verbos.lan";
	public static String pathSinonimos = Paths.LANG_FILES_PATH + "/sinon.lan";
	public static String pathAlias = Paths.LANG_FILES_PATH + "/alias.lan";
	public static String pathVerbos32 = Paths.LANG_FILES_PATH + "/verbos32.lan";

	private Hashtable imperativoAInfinitivo;
	private Hashtable infinitivoAImperativo;
	private Hashtable sinonimos;
	private Hashtable alias;
	private Hashtable terceraASegunda;
	
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

	private Hashtable loadTableFromPath ( String f , char separator ) throws IOException , FileNotFoundException
	{
		return loadTableFromPath ( f , separator , true );
	}
	
	private Hashtable loadInvertedTableFromPath ( String f , char separator ) throws IOException , FileNotFoundException
	{
		return loadInvertedTableFromPath ( f , separator , true );
	}

	private Hashtable loadTableFromPath ( String f , char separator , boolean dejarRepeticiones ) throws IOException , FileNotFoundException
	{
		//contar entradas que tendremos que meter en Hashtable
			BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( this.getClass().getClassLoader().getResourceAsStream(f) ) );
			int nlineas = 0;
			while ( filein.readLine() != null ) nlineas++;
		
		//crear la tabla teniendo en cuenta este número
			Hashtable tabla = new Hashtable ( (int)Math.round( nlineas * 1.5 ) );
		
		//ahora, meter los verbos en la tabla
			filein = new BufferedReader ( Utility.getBestInputStreamReader ( this.getClass().getClassLoader().getResourceAsStream(f) ) );
			String linea;
			while ( ( linea = filein.readLine() ) != null )
			{
				//usamos hashtable.put(key,value)
				String laClave = StringMethods.getTok( linea , 1 , separator ).trim();
				String elValor = StringMethods.getTok( linea , 2 , separator ).trim();
				
				//System.err.println(laClave + " , " + elValor);
				
				if ( dejarRepeticiones == false || tabla.get(laClave) == null )
					tabla.put ( laClave,elValor );	
				//tabla.put ( laClave,elValor );	
			}
		//devolvemos la hashtable.
			return tabla;
	}
	
	private Hashtable loadInvertedTableFromPath ( String f , char separator , boolean dejarRepeticiones ) throws IOException , FileNotFoundException
	{
		//contar entradas que tendremos que meter en Hashtable
			BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( this.getClass().getClassLoader().getResourceAsStream(f) ) );
			int nlineas = 0;
			while ( filein.readLine() != null ) nlineas++;
		
		//crear la tabla teniendo en cuenta este número
			Hashtable tabla = new Hashtable ( (int)Math.round( nlineas * 1.5 ) );
		
		//ahora, meter los verbos en la tabla
			filein = new BufferedReader ( Utility.getBestInputStreamReader ( this.getClass().getClassLoader().getResourceAsStream(f) ) );
			String linea;
			while ( ( linea = filein.readLine() ) != null )
			{
				//usamos hashtable.put(key,value)
				String laClave = StringMethods.getTok( linea , 2 , separator ).trim();
				String elValor = StringMethods.getTok( linea , 1 , separator ).trim();
				
				if ( dejarRepeticiones == false || tabla.get(laClave) == null )
					tabla.put ( laClave,elValor );	
			}
		//devolvemos la hashtable.
			return tabla;
	}

	public NaturalLanguage ( )
	{
		//load the files needed for the natural language utils to work
		
		try
		{
			imperativoAInfinitivo = loadTableFromPath ( pathVerbos , '=' );
			infinitivoAImperativo = loadInvertedTableFromPath ( pathVerbos , '=' , false );
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
			sinonimos = loadTableFromPath ( pathSinonimos , '=' );
		}
		catch ( Exception exc )
		{
			System.err.println("Aviso: no se ha encontrado fichero de sinónimos, la tabla de sinónimos estará vacía.");
			sinonimos = new Hashtable(1);
		}
		try
		{
			alias = loadTableFromPath ( pathAlias , '=' );
		}
		catch ( Exception exc )
		{
			System.err.println("Aviso: no se ha encontrado fichero de alias, la tabla de alias estará vacía.");
			alias = new Hashtable(1);
		}
		try
		{
			terceraASegunda = loadTableFromPath ( pathVerbos32 , ' ' );
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
	
	public String obtenerAlias ( String palabra )
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
			String sin = obtenerAlias(tok);
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
	
	public String gramaticalizar ( String s )
	{
		if ( s == null ) return null;
		String temp = StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( s , " a el" , " al" ) , " de el" , " del" );
		temp = temp.trim();
		if ( temp.length() > 0 )
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
		if ( temp.length() > 0 )
			if ( temp.charAt(temp.length()-1) != '.' ) temp += ".";
		return temp;
	}
	
	public String gramaticalizarSinTrimear ( String s )
	{
		if ( s == null ) return null;
		String temp = StringMethods.textualSubstitution ( StringMethods.textualSubstitution ( s , " a el" , " al" ) , " de el" , " del" );
		//temp = temp.trim();
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

}
