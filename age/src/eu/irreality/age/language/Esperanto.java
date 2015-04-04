package eu.irreality.age.language;

import java.io.IOException;

import eu.irreality.age.NaturalLanguage;
import eu.irreality.age.i18n.UIMessages;

public class Esperanto extends NaturalLanguage
{

	public Esperanto()
	{
		super("eo");
	}
	
	/**
	 * Returns the default verb, i.e., verb that will be used by default if a reference name is typed
	 * at the beginning of a game without specifying a verb.
	 */
	public String getDefaultVerb()
	{
		return "rigardi";
	}
	
}
