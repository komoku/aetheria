/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:53:02
 * as file Node.java on package org.f2o.absurdum.puck.graph
 */
package org.f2o.absurdum.puck.gui.graph;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JPanel;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:53:02
 */
public abstract class Node implements GraphElement
{

	public abstract Object clone();
	public abstract Rectangle getBounds();
	public abstract List getArrows();
	public abstract void addArrow ( Arrow a );
	public abstract void removeArrow ( Arrow a );
	public abstract String getName();
	public abstract void paint ( Graphics g , int x , int y , double zoom );
	
	private boolean selected;
	
	public void setSelected ( boolean selected )
	{
		this.selected = selected;
	}
	
	public boolean isSelected()
	{
		return selected;
	}
	
	private boolean highlighted;
	
	public void setHighlighted ( boolean highlighted )
	{
		this.highlighted = highlighted;
	}
	
	public boolean isHighlighted()
	{
		return highlighted;
	}
	
	public void setLocation ( int x , int y )
	{
		getBounds().setLocation ( x , y );
	}
	
	public void paint ( Graphics g  )
	{	
		paint ( g , (int) getBounds().getX() , (int) getBounds().getY() );	
	}
	
	public void paintToView ( Graphics g , double viewZoom , double viewXOffset , double viewYOffset )
	{
		int viewXCoord = (int)(( (int) getBounds().getX() - viewXOffset ) * viewZoom);
		int viewYCoord = (int)(( (int) getBounds().getY() - viewYOffset ) * viewZoom);
		paint ( g , viewXCoord , viewYCoord , viewZoom );
	}
	
	//overrides bounds, good for prototypes
	public void paint ( Graphics g , int x , int y )
	{
		paint ( g , x , y , 1.0 );
	}
	
	public abstract void setExplicitSize ( int size );
	
	public static float getNameFontSize()
	{
		String sizeProp = PuckConfiguration.getInstance().getProperty("graphNodeFontSize");
		if ( sizeProp != null )
		{
			try
			{
				return Float.valueOf(sizeProp);
			}
			catch ( NumberFormatException nfe )
			{
				return (float) 11.0;
			}
		}
		else
			return (float) 11.0;
	}
	

	
	
}
