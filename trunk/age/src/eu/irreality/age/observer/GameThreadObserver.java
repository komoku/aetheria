package eu.irreality.age.observer;

import eu.irreality.age.GameEngineThread;

/**
 * An object that can be attached to, or detached from, a game engine thread.
 * @author carlos
 *
 */
public interface GameThreadObserver 
{

	public void onAttach ( GameEngineThread thread );
	public void onDetach ( GameEngineThread thread );
	
}
