package eu.irreality.age.filemanagement;

public interface GameStarter 
{
	
	/**
	 * Start a game.
	 * Uses log if logFile != null.
	 * Uses state if stateFile != null.
	 * @param moduledir Path to a world to load.
	 * @param logFile Path to log file to use, if any.
	 * @param stateFile Path to state file to use, if any.
	 */
	public void startGame ( final String moduledir , final String logFile , final String stateFile );
	
}
