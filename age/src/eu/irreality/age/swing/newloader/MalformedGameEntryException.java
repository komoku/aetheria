package eu.irreality.age.swing.newloader;

/**
 * Exception thrown when the entries indicating where a game has to be downloaded, or is stored, are malformed.
 * @author carlos
 *
 */
public class MalformedGameEntryException extends Exception 
{

	public MalformedGameEntryException ( String message )
	{
		super(message);
	}
	
	public MalformedGameEntryException ( Exception wrappedException )
	{
		super(wrappedException);
	}
	
}
