package eu.irreality.age.language;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import eu.irreality.age.NaturalLanguage;
import eu.irreality.age.StringMethods;
import eu.irreality.age.Utility;

/**
 * A collection of utility functions to handle natural language files.
 * @author carlos
 *
 */
public class LanguageUtils 
{

	public static Map loadTableFromPath ( String f , char separator , boolean dejarRepeticiones ) throws IOException , FileNotFoundException
	{
		//contar entradas que tendremos que meter en Hashtable
			BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( NaturalLanguage.class.getClassLoader().getResourceAsStream(f) ) );
			int nlineas = 0;
			while ( filein.readLine() != null ) nlineas++;
		
		//crear la tabla teniendo en cuenta este número
			Hashtable tabla = new Hashtable ( (int)Math.round( nlineas * 1.5 ) );
		
		//ahora, meter los verbos en la tabla
			filein = new BufferedReader ( Utility.getBestInputStreamReader ( NaturalLanguage.class.getClassLoader().getResourceAsStream(f) ) );
			String linea;
			while ( ( linea = filein.readLine() ) != null )
			{
				//usamos hashtable.put(key,value)
				String laClave = StringMethods.getTok( linea , 1 , separator ).trim();
				String elValor = StringMethods.getTok( linea , 2 , separator ).trim();
				
				//System.err.println(laClave + " , " + elValor);
				
				if ( dejarRepeticiones == true || tabla.get(laClave) == null )
					tabla.put ( laClave,elValor );	
				//tabla.put ( laClave,elValor );	
			}
		//devolvemos la hashtable.
			return tabla;
	}

	public static Map loadInvertedTableFromPath ( String f , char separator , boolean dejarRepeticiones ) throws IOException , FileNotFoundException
	{
		//contar entradas que tendremos que meter en Hashtable
			BufferedReader filein = new BufferedReader ( Utility.getBestInputStreamReader ( NaturalLanguage.class.getClassLoader().getResourceAsStream(f) ) );
			int nlineas = 0;
			while ( filein.readLine() != null ) nlineas++;
		
		//crear la tabla teniendo en cuenta este número
			Hashtable tabla = new Hashtable ( (int)Math.round( nlineas * 1.5 ) );
		
		//ahora, meter los verbos en la tabla
			filein = new BufferedReader ( Utility.getBestInputStreamReader ( NaturalLanguage.class.getClassLoader().getResourceAsStream(f) ) );
			String linea;
			while ( ( linea = filein.readLine() ) != null )
			{
				//usamos hashtable.put(key,value)
				String laClave = StringMethods.getTok( linea , 2 , separator ).trim();
				String elValor = StringMethods.getTok( linea , 1 , separator ).trim();
				
				if ( dejarRepeticiones == true || tabla.get(laClave) == null )
					tabla.put ( laClave,elValor );	
			}
		//devolvemos la hashtable.
			return tabla;
	}

	public static Map loadTableFromPath ( String f , char separator ) throws IOException , FileNotFoundException
	{
		return loadTableFromPath ( f , separator , true );
	}

	public static Map loadInvertedTableFromPath ( String f , char separator ) throws IOException , FileNotFoundException
	{
		return loadInvertedTableFromPath ( f , separator , true );
	}

}
