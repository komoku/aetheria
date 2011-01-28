/*
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * License/licencia: license.txt
 */
package eu.irreality.age.spell;

import java.util.Collection;

public interface SpellingCorrector 
{

	public void init ( Collection words );
	public void addDictionaryWord ( String word ) throws UnsupportedOperationException;
	public Correction getBestCorrection ( String word );
	
}
