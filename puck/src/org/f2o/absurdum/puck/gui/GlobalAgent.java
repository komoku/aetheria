/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

/**
 * A singleton, this handles the clipboard and other such stuff that may 
 * have effect on different instances.
 * @author carlos
 *
 */
public class GlobalAgent implements ClipboardOwner
{
	
	private static GlobalAgent instance = new GlobalAgent();
	
	public static GlobalAgent getInstance()
	{
		return instance;
	}

	public void lostOwnership(Clipboard arg0, Transferable arg1) 
	{
		//At the moment, do nothing.
	}
	
	//todo: register all PuckFrames, and when clipboard ownership is lost, disable "paste node" options in all of them.

}
