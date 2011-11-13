/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 24-jul-2005 18:23:42
 * as file WorldNode.java on package org.f2o.absurdum.puck.gui.graph
 */
package org.f2o.absurdum.puck.gui.graph;

import org.f2o.absurdum.puck.gui.panels.EntityPanel;
import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;
import org.f2o.absurdum.puck.gui.panels.WorldPanel;
import org.f2o.absurdum.puck.i18n.UIMessages;

/**
 * @author carlos
 *
 * Created at regulus, 24-jul-2005 18:23:42
 */
public class WorldNode extends InvisibleNode 
{

	private WorldPanel wp;
	
	public WorldNode ( WorldPanel wp )
	{
		super(0,0);
		this.wp = wp;
	}

	public GraphElementPanel getAssociatedPanel()
	{
		return wp;
	}
	
	public String toString ()
	{
		return "World (" + super.toString() + ")";
	}
	
}
