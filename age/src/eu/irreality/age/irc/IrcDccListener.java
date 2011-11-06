/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.irc;
import java.util.*;

public interface IrcDccListener extends EventListener
{

	public void dccMsg ( String sender , String message );
	public void dccDisconnection ( String sender );
	public void dccConnection ( String sender );

}

