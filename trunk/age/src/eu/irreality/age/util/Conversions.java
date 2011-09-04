/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 20/03/2011 13:11:27
 */
package eu.irreality.age.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author carlos
 *
 */
public class Conversions 
{

	/**
	 * Converts a dollar-separated reference name list into a proper List with
	 * the names.
	 * @param legacyList The legacy list to convert.
	 * @return A list of the reference names.
	 */
	public static List getReferenceNameList( String legacyList )
	{
		List result = new ArrayList();
		if ( legacyList == null ) return result;
		StringTokenizer st = new StringTokenizer ( legacyList , "$" );
		while ( st.hasMoreTokens() ) 
		{
			result.add(st.nextToken());
		}
		return result;
	}
	
}
