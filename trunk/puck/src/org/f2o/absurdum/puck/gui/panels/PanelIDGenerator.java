/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 19:39:30
 * as file PanelIDGenerator.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 19:39:30
 */
public class PanelIDGenerator 
{

	private static int curID = 0;
	
	public static String newID ( )
	{
		return ""+(curID++);
	}

}
