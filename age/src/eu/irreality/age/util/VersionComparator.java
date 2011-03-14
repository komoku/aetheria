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
	 * Compare two version strings.
	 */
	public int compare(Object arg0, Object arg1) 
	{
		if ( arg0 == null || arg1 == null ) return 0; //if version is unspecified we won't complain 
		String version0 = (String) arg0;
		String version1 = (String) arg1;
		if ( "".equals(arg0) || "".equals(arg1) ) return 0; //again, version unspecified
		if ( !version0.matches(".*\\d.*") || !version1.matches(".*\\d.*") ) return 0; //no numbers in version strings
		return NaturalOrderComparator.NUMERICAL_ORDER.compare(version0,version1);
	}
	


}
