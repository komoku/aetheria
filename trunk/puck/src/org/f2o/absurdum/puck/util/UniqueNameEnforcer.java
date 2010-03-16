/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.util;

import java.util.HashMap;
import java.util.Map;

import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;

public class UniqueNameEnforcer 
{

	public static String makeUnique ( String name , Map namesMap )
	{
		if ( namesMap.get(name) == null ) return name;
		else
		{
			String curName = name;
			int i = 1;
			while ( namesMap.get(curName) != null )
			{
				curName = name + " #" + i;
				i++;
			}
			return curName;
		}
	}
	

	
	
}
