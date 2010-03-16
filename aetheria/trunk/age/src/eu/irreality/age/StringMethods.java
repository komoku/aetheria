/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;

//package StringMethods;	
public class StringMethods
{
	/**
	* getTok: coge un determinado token (palabra) de un string.
	*
	* @param thestring la cadena de entrada
	* @param numtok el número de token que queremos
	* @param separator el caracter que usaremos como separador (por ejemplo, espacio para palabras)
	*/
	public static String getTok ( String thestring , int numtok , char separator )	
	{
		if ( thestring == null ) return null;
		int i = 0;
		int currenttoken = 1;
		int mark = 0;
		while ( i < thestring.length() )
		{
			if ( thestring.charAt(i) != separator )
				i++;
			else
			{
				if ( numtok == currenttoken )
				{
					return thestring.substring(mark,i);	
				}
				while ( thestring.charAt(i) == separator ) i++;
				mark=i;
				currenttoken++;	
			}
		}
		if ( numtok == currenttoken ) return thestring.substring(mark,i);
		else return null;	
	}
		
	/**
	* numToks: número de tokens (palabras) de un string.
	*
	* @param thestring la cadena de entrada
	* @param separator el caracter que usaremos como separador (por ejemplo, espacio para palabras)
	* @returns número de tokens del string.
	*/
	public static int numToks ( String thestring , char separator )
	{
		if ( thestring == null ) return 0;
		int i = 0;
		int currenttoken = 0;
		boolean flag = true;
		while ( i < thestring.length() )
		{
			if ( flag && thestring.charAt(i) != separator )
			{
				currenttoken++; flag = false;
			}
			if ( !flag && thestring.charAt(i) == separator )
			{
				flag = true;
			}
			i++;
		}
		return currenttoken;
	}
	/**
	* getToks: coge la zona que ocupan varios tokens (palabras) de un string.
	* Implementación muy ineficiente, y además es un stub (no hace exactamente esto), cambiar.
	*
	* @param thestring la cadena de entrada
	* @param first el número del primer token que queremos
	* @param last el número del último token que queremos
	* @param separator el caracter que usaremos como separador (por ejemplo, espacio para palabras)
	*/
	public static String getToks ( String thestring , int first , int last , char separator )
	{
		String return_string = "";
		if ( numToks( thestring , separator ) < last ) return null;
		for ( int i = first ; i <= last ; i++ )
		{
			if ( i != first ) return_string += separator;
			return_string += getTok ( thestring , i , separator );	
		}
		return return_string;	
	}	
	
	public static Vector tokenizeWithComplexSeparators ( String theString , Vector separators )
	{
		return tokenizeWithComplexSeparators ( theString , separators , false );
	}
	
	/**
	* tokenizeWithComplexSeparators: divide la cadena dada en diferentes trozos
	*(tokens) según la aparición de los separadores, que en este caso no son caracteres
	*simples sino strings.
	*
	* @param theString la cadena de entrada
	* @param separators vector de separadores (strings)
	* @param includeSeparators si se han de devolver o no los separadores (líneas etiquetadas new)
	* @return vector de strings separados.
	* Ejemplo: "El rey y la reyna" , [" y "] , false -> ["El rey" , "la reyna"]
	*/
	public static Vector tokenizeWithComplexSeparators ( String theString , Vector separators , boolean includeSeparators )
	{
		int last_index = 0;
		int new_index = 0;
		int sep_length = 1; //longitud del separador que procesamos
		/*new*/ String sep=null; //separador
		Vector resultado = new Vector();
		
		while ( new_index >= 0 )
		{
			//new_index = indice minimal de separadores en string desde last_index
				new_index = -1;
				for ( int i = 0 ; i < separators.size() ; i++ )
				{
					int candidato = theString.indexOf ( (String)separators.elementAt(i) , last_index );
					if ( candidato >= 0 && ( candidato < new_index || new_index == -1 ) ) 
					{
						new_index = candidato;
						sep_length = ((String)separators.elementAt(i)).length();
						/*new*/ sep = ((String)separators.elementAt(i));
					}	
				//{new index = indice de primera aparicion de separador en string desde last index}
				//{sep length = longitud de ese separador que aparece}
				}
			//next token = last index to new index
			//and update last index
				if ( new_index >= 0 )
				{	
					resultado.addElement( theString.substring(last_index , new_index ) );		
					/*new*/ if ( includeSeparators && sep!=null ) resultado.add(sep);
					last_index = new_index + sep_length;
				}
			
		
		}
		//last token
		resultado.addElement( theString.substring(last_index,theString.length()) );

		
		return resultado;
		
	}
	
	public static String textualSubstitution ( String orig , String vieja , String nueva )
	{
		
		int oldind = 0 , newind = 0;
		String retval = "";
		
		if ( orig == null ) return null;
		
		for (;;)
		{
			oldind = newind;
			newind = orig.indexOf ( vieja , oldind );
			if ( newind >= 0 )
			{
				retval += orig.substring ( oldind , newind );
				retval += nueva;
				newind += vieja.length();
			}
			else
			{
				retval += orig.substring(oldind);
				return retval;
			}
		}
		
		
	}
	
	
	//como la textual substitution; pero sólo substituye palabras (tokens)
	
	public static String tokenSubstitution ( String orig , String vieja , String nueva , String separator )
	{
		StringTokenizer st = new StringTokenizer ( orig , separator , true ); //(return delims)
		
		String retval = "";
		
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			if ( tok.equals ( vieja ) )
			{
				retval += nueva;
			}
			else
			{
				retval += tok;
			}
		}
		
		return retval;
	}
	
	public static String tokenSubstitution ( String orig , String vieja , String nueva , char separator )
	{
		return tokenSubstitution ( orig , vieja , nueva , String.valueOf(separator) );
	}
	
	
	
	public static boolean isSubstringOf ( String a , String b )
	{
		//HACER UN ALGORITMO MÁS EFICIENTE
		if ( a == null ) return true;
		if ( b == null ) return false;
		if ( a.length() > b.length() ) return false;
		
		for ( int i = 0 ; i <= b.length() - a.length() ; i++ )
		{
			if ( a.equals ( b.substring(i,i+a.length()) ) )
				return true;
		}
		return false;
	
	
	
	
	}
	
	
	public static Vector STANDARD_SENTENCE_SEPARATORS ()
	{
		Vector resultado = new Vector ();
		resultado.addElement ( " y " );
		resultado.addElement ( " e " );
		resultado.addElement ( "," );
		resultado.addElement ( ";" );
		resultado.addElement ( "." );
		return resultado;
	}
	
	//protected String GetWord ( String thestring , int i )
	
}