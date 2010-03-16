/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

/*
 * Created at regulus on 08-abr-2007 11:46:29
 * as file ArrayNodeList.java on package org.f2o.absurdum.puck.util.xml
 */
package org.f2o.absurdum.puck.util.xml;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A trivial implementation of a DOM XML node list.
 * @author carlos
 *
 * Created at regulus, 08-abr-2007 11:46:29
 */
public class ArrayNodeList implements NodeList
{

	private ArrayList impl;
	
	public ArrayNodeList()
	{
		impl = new ArrayList();
	}
	
	public void add ( Node n )
	{
		impl.add(n);
	}
	
	public boolean remove ( Node n )
	{
		return impl.remove(n);
	}
	
	public int getLength()
	{
		return impl.size();
	}
	
	public Node item ( int index )
	{
		try
		{
			return (Node) impl.get(index);
		}
		catch ( IndexOutOfBoundsException ioobe )
		{
			//as strange as it may seem, the org.w3c.dom.NodeList interface's contract says
			//this method should return null for invalid indexes
			return null;
		}
	}
	
}
