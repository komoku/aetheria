/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 20/03/2011 13:15:41
 */
package eu.irreality.age.spell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import eu.irreality.age.EntityList;
import eu.irreality.age.Item;
import eu.irreality.age.Mobile;
import eu.irreality.age.Path;
import eu.irreality.age.Room;
import eu.irreality.age.Spell;
import eu.irreality.age.World;

/**
 * @author carlos
 *
 * Decorator of a SpellingCorrector to correct using a dictionary extracted from the reference
 * names in a given world.
 */
public class ReferenceNameCorrector implements SpellingCorrector
{

	private SpellingCorrector theCorrector;
	
	/**
	 * Words with length smaller than this are not included in the corrector, and corrections are not attempted for them. This is to prevent AGE from trying to correct
	 * things like articles, prepositions, etc. to their closest name.
	 */
	private static int MINLENGTH = 4;
	
	/**
	 * Extracts a set of words for corrections from a list of reference names.
	 * Words are extracted if their length is at least MINLENGTH.
	 */
	private Set extractRelevantWords ( List names )
	{
		Set s = new LinkedHashSet();
		for ( int i = 0 ; i < names.size() ; i++ )
		{
			String name = (String)names.get(i);
			StringTokenizer st = new StringTokenizer(name);
			while ( st.hasMoreTokens() )
			{
				String word = st.nextToken();
				if ( word.length() >= MINLENGTH ) s.add(word);
			}
		}
		return s;
	}
	
	
	/**
	 * Builds a corrector that uses a dictionary extracted from the reference names of the given
	 * world, using the given corrector as the base.
	 */
	public ReferenceNameCorrector ( World w , SpellingCorrector c )
	{
		theCorrector = c;
		EntityList items = w.getAllItems();
		EntityList mobiles = w.getAllMobiles();
		EntityList rooms = w.getAllRooms();
		EntityList spells = w.getAllSpells();
		Set words = new LinkedHashSet();
		
		//add item reference names
		for ( int i = 0 ; i < items.size() ; i++ )
		{
			Item it = (Item) items.get(i);
			words.addAll( extractRelevantWords(it.getSingularReferenceNames()));
			words.addAll( extractRelevantWords(it.getPluralReferenceNames()));
			words.addAll( extractRelevantWords(it.getExtraDescriptionNames()));
		}
		
		//add mobile reference names
		for ( int i = 0 ; i < mobiles.size() ; i++ )
		{
			Mobile mob = (Mobile) mobiles.get(i);
			words.addAll( extractRelevantWords(mob.getSingularReferenceNames()));
			words.addAll( extractRelevantWords(mob.getPluralReferenceNames()));
			words.addAll( extractRelevantWords(mob.getExtraDescriptionNames()));
		}
		
		//add spell reference names
		for ( int i = 0 ; i < spells.size() ; i++ )
		{
			Spell spell = (Spell) spells.get(i);
			words.addAll( extractRelevantWords(spell.getSingularReferenceNames()));
			words.addAll( extractRelevantWords(spell.getPluralReferenceNames()));
		}
		
		//add words coming from names of exits/paths
		for ( int i = 0 ; i < rooms.size() ; i++ )
		{
			Room r = (Room) rooms.get(i);
			words.addAll( extractRelevantWords(r.getExtraDescriptionNames()));
			Path[] exits = r.getNonStandardExits();
			for ( int j = 0 ; j < exits.length ; j++ )
			{
				List names = r.getExitNames(exits[j]); 
				words.addAll( extractRelevantWords(names) );
			}
			exits = r.getStandardExits();
			for ( int j = 0 ; j < exits.length ; j++ )
			{
				if ( r.isValidExit(true,j))
				{
					List names = r.getExitNames(exits[j]); 
					words.addAll( extractRelevantWords(names) );
				}
			}
		}
		
		//add common words for the language (which don't refer to anything in the world but are expected to appear, e.g. this, another, some, etc.)
		List commonWords = w.getLanguage().getCommonWordsList();
		words.addAll( extractRelevantWords(commonWords) );
		
		init(words);
	}

	public void init(Collection words) 
	{
		theCorrector.init(words);
	}

	public void addDictionaryWord(String word) throws UnsupportedOperationException 
	{	
		theCorrector.addDictionaryWord(word);
	}
	
	/**
	 * Adds a new reference name (single-word or multiword) to the corrector.
	 * This can be used by games that dynamically add reference names to entities.
	 * @param name
	 */
	public void addReferenceName(String name)
	{
		List singleElementList = new ArrayList();
		singleElementList.add(name);
		Set relevantWords = extractRelevantWords(singleElementList);
		for ( Iterator it = relevantWords.iterator() ; it.hasNext(); )
		{
			String word = (String) it.next();
			addDictionaryWord(word);
		}
	}

	public Correction getBestCorrection(String word) 
	{
		if ( word.length() < MINLENGTH ) return new Correction(word,0.0); //don't attempt to correct short words like articles, prepositions, etc.
		return theCorrector.getBestCorrection(word);
	}
	
}
