/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.util;

import java.util.Map;

/*
 Created 08/02/2008 19:25:27
 Converts legacy item ID's to names.
 */

public class IdToNameConverter 
{

	private Map idToNameMap;
	
	public IdToNameConverter ( Map idToNameMap )
	{
		this.idToNameMap = idToNameMap;
	}
	
	public String convertId ( String id )
	{
		return (String) idToNameMap.get(id);
	}
	
	/**
	 * 
	 * @param nameOrId The name or legacy numeric ID of an entity.
	 * @return The name of the given entity.
	 */
	public String normalize ( String nameOrId )
	{
		String result = (String) idToNameMap.get(nameOrId);
		if ( result == null )
			return nameOrId;
		else
			return result;
	}
	
	
}
