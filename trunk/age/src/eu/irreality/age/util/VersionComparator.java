package eu.irreality.age.util;

import java.util.Comparator;

import org.cougaar.util.NaturalOrderComparator;

/**
 * Compares strings that describe versions.
 * @author carlos
 *
 */
public class VersionComparator implements Comparator
{

	/**
	 * If the string ends with a digit -> adds .a
	 * If the string ends with a letter and no dot before the letter -> add dot before the letter (if not already there).
	 * So that:
	 * 1.3.0 -> 1.3.0.a
	 * 1.3.0b -> 1.3.0.b
	 * So that NaturalLanguageComparator handles well cases like 1.2.0b vs 1.2.3
	 * @param versionString
	 * @return
	 */
	public String normalizeVersionString ( String versionString )
	{
		if ( versionString == null ) return null;
		if ( versionString.length() == 0 ) return "";
		if ( Character.isDigit(versionString.charAt(versionString.length()-1)) )
		{
			return versionString + ".a";
		}
		else if ( Character.isLetter(versionString.charAt(versionString.length()-1)) )
		{
			if ( versionString.length() >= 2 && versionString.charAt(versionString.length()-2) != '.'  )
				return versionString.substring(0,versionString.length()-1) + "." + versionString.charAt(versionString.length()-1);		
			else
				return versionString;
		}
		else //we don't know what this is 
		{
			return versionString;
		}
	}
	
	
	/**
	 * Compare two version strings.
	 */
	public int compare(Object arg0, Object arg1) 
	{
		if ( arg0 == null || arg1 == null ) return 0; //if version is unspecified we won't complain 
		String version0 = (String) arg0;
		String version1 = (String) arg1;
		if ( "".equals(arg0) || "".equals(arg1) ) return 0; //again, version unspecified
		if ( !version0.matches(".*\\d.*") || !version1.matches(".*\\d.*") ) return 0; //no numbers in version strings
		return NaturalOrderComparator.NUMERICAL_ORDER.compare(normalizeVersionString(version0),normalizeVersionString(version1));
	}
	


}
