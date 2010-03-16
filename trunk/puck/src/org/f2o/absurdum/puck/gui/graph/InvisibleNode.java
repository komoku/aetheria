/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:58:52
 * as file RoomNode.java on package org.f2o.absurdum.puck.graph
 */
package org.f2o.absurdum.puck.gui.graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;


/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:58:52
 */
public class InvisibleNode extends Node 
{
	
	private static int defaultSize = 20;
	
	private int explicitSize = -1;
	
	private Rectangle bounds = new Rectangle(0,0,defaultSize,defaultSize);
	
	private ArrayList arrows = new ArrayList();
	
	public static void setDefaultSize(int size)
	{
		defaultSize = size;
	}
	
	public void setSize(int size)
	{
		bounds = new Rectangle(0,0,size,size);
	}
	
	public void addArrow ( Arrow a )
	{
		arrows.add(a);
	}
	
	public void removeArrow ( Arrow a )
	{
		arrows.remove(a);
	}
	
	public List getArrows()
	{
		return arrows;
	}
	
	public Object clone()
	{
		return new InvisibleNode((int)bounds.getX(),(int)bounds.getY());
	}
	
	public InvisibleNode(int x, int y)
	{
		bounds.setLocation(x,y);
	}
	
	public void paint ( Graphics g  )
	{
		//it's invisible! it's not painted!
	}
	
	public void setLocation ( int x , int y )
	{
		bounds.setLocation ( x , y );
	}
	
	public Rectangle getBounds()
	{
		return bounds;
	}
	
	public void paint ( Graphics g , int x , int y , double zoom )
	{
		//it's invisible, I already told you!	
	}
	
	public GraphElementPanel getAssociatedPanel()
	{
		return new GraphElementPanel();
	}
	
	public String getName()
	{
		return "Invisible";
	}
	
	public void setExplicitSize(int size) 
	{
		explicitSize = size;
		if ( explicitSize >= 0 )
			bounds = new Rectangle(0,0,size,size);
	}
	
}
