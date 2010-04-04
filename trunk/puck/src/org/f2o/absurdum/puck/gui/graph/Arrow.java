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

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:53:02
 */
public abstract class Arrow implements GraphElement
{

	public abstract Object clone();
	public abstract Node getSource();
	public abstract Node getDestination();
	public abstract void setSource ( Node n );
	public abstract void setDestination ( Node n );
	public abstract void paintTo ( Graphics g , int x , int y ); //with null source
	public abstract int[] getPaintingCoords();
	public abstract void paint ( Graphics g , int srcX , int srcY , int dstX , int dstY );
	public abstract String getName();
	
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
	
	public void paint(Graphics g) 
	{			
		int [] coords = getPaintingCoords();
		paint(g,(int)coords[0],(int)coords[1],(int)coords[2],(int)coords[3]);
	}	
	
	public void paintToView ( Graphics g , double viewZoom , double viewXOffset , double viewYOffset )
	{
		
		int [] coords = getPaintingCoords();
		
		//transform coords
		coords[0] = (int)(( (int) coords[0] - viewXOffset ) * viewZoom);
		coords[1] = (int)(( (int) coords[1] - viewYOffset ) * viewZoom);
		coords[2] = (int)(( (int) coords[2] - viewXOffset ) * viewZoom);
		coords[3] = (int)(( (int) coords[3] - viewYOffset ) * viewZoom);
		
		paint(g,(int)coords[0],(int)coords[1],(int)coords[2],(int)coords[3]);

	}
	
	public static float getNameFontSize()
	{
		String sizeProp = PuckConfiguration.getInstance().getProperty("graphArrowFontSize");
		if ( sizeProp != null )
		{
			try
			{
				return Float.valueOf(sizeProp);
			}
			catch ( NumberFormatException nfe )
			{
				return (float) 10.0;
			}
		}
		else
			return (float) 10.0;
	}
	
}
