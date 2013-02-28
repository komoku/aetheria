package eu.irreality.age.swing.newloader;

/**
 * Thrown when it is not possible to download a game for any reason.
 * @author carlos
 *
 */
public class GameDownloadException extends Exception
{
	
	public GameDownloadException ( String message )
	{
		super(message);
	}
	
	public GameDownloadException ( Exception wrappedException )
	{
		super(wrappedException);
	}

}
