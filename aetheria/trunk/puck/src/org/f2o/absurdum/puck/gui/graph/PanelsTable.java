/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 18:03:52
 * as file PanelsTable.java on package org.f2o.absurdum.puck.gui.graph
 */
package org.f2o.absurdum.puck.gui.graph;

import java.util.HashMap;

import javax.swing.JPanel;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 18:03:52
 */
public class PanelsTable 
{

	private HashMap map = new HashMap();
	
	private static PanelsTable instance;
	
	private PanelsTable()
	{	
	}
	
	public static PanelsTable getInstance()
	{
		if ( instance == null )
			instance = new PanelsTable();
		return instance;
	}
	
	public void addPanel ( Object key , JPanel val )
	{
		map.put(key,val);
	}
	
	public JPanel getPanel ( Object key )
	{
		JPanel jp = ( JPanel ) map.get ( key );
		return jp;
	}
	
	

}
