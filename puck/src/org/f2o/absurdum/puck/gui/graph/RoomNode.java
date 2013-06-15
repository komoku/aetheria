/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:58:52
 * as file RoomNode.java on package org.f2o.absurdum.puck.graph
 */
package org.f2o.absurdum.puck.gui.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.panels.EntityPanel;
import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;
import org.f2o.absurdum.puck.gui.panels.RoomPanel;
import org.f2o.absurdum.puck.gui.skin.ImageManager;
import org.f2o.absurdum.puck.i18n.UIMessages;


/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:58:52
 */
public class RoomNode extends Node 
{
	
	private static int defaultSize = Integer.parseInt(PuckConfiguration.getInstance().getProperty("roomDisplaySize"));
	
	private int explicitSize = -1;
	
	private Rectangle bounds = new Rectangle(0,0,defaultSize,defaultSize);
	
	private ArrayList arrows = new ArrayList();
	
	private GraphElementPanel associatedPanel;
	
	private void updateBounds ( )
	{
		if ( explicitSize >= 0 && bounds.width != explicitSize )
		{
			bounds.width = explicitSize;
			bounds.height = explicitSize;
		}
		else if ( explicitSize == -1 && bounds.width != defaultSize )
		{
			bounds.width = defaultSize;
			bounds.height = defaultSize;
		}
	}
	
	public static void setDefaultSize(int size)
	{
		defaultSize = size;
	}
	
	public static int getDefaultSize()
	{
		return defaultSize;
	}
	
	public int getSize()
	{
		if ( explicitSize >= 0 )
			return explicitSize;
		else
			return defaultSize;
	}
	
	public GraphElementPanel getAssociatedPanel()
	{
		if ( associatedPanel == null )
		{
			associatedPanel = new RoomPanel(this);
		}
		return associatedPanel;
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
		RoomNode r = new RoomNode((int)bounds.getX(),(int)bounds.getY());
		r.explicitSize = this.explicitSize;
		return r;
	}
	
	public RoomNode(int x, int y)
	{
		bounds.setLocation(x,y);
	}
	
	
	private static Image icon;
	
	static
	{
		try
		{
			icon = ImageManager.getInstance().getImage("room");
		}
		catch ( Exception e )
		{
			System.out.println("Warning: not using any image for room, a generic square will be used instead.");
		}
	}
	
	public static void refreshIcon()
	{
		icon = ImageManager.getInstance().getImage("room");
	}
	
	public void paint ( Graphics g  )
	{
		
		updateBounds();
		
		Color oldColor = g.getColor();
		
		if ( PuckConfiguration.getInstance().getProperty("showRoomNodes").equals("true") || isHighlighted() )
		{	
			
			if ( icon == null )
			{
				g.setColor(Color.RED);
				g.drawRect((int)bounds.getX(),(int)bounds.getY(),(int)bounds.getWidth(),(int)bounds.getHeight());
			}
			else
			{
				g.drawImage(icon,(int)bounds.getX(),(int)bounds.getY(),(int)((int)bounds.getWidth()),(int)((int)bounds.getHeight()),null);
			}
	
		}

		if ( PuckConfiguration.getInstance().getProperty("showRoomNames").equals("true") || isHighlighted() )
		{
		
			g.setColor(GraphColorSettings.getInstance().getColorSetting("text"));
			Font oldFont = g.getFont();
			Font newFont = oldFont.deriveFont(getNameFontSize());
			g.setFont(newFont);
			int swidth = g.getFontMetrics().stringWidth(getName());
			g.drawString(getName(),(int)bounds.getX()+12-swidth/2,(int)bounds.getY()+12);
			g.setFont(oldFont);
		
		}
		
		//g.drawString(getName(),(int)bounds.getX()+5,(int)bounds.getY()+5);
		
		
		
		g.setColor(oldColor);
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
		updateBounds();
		
		Color oldColor = g.getColor();
		
		if ( PuckConfiguration.getInstance().getProperty("showRoomNodes").equals("true") || isHighlighted() )
		{	
		
			if ( icon == null )
			{
				g.setColor(Color.RED);
				g.drawRect(x,y,(int)(bounds.getWidth()*zoom),(int)(bounds.getHeight()*zoom));
			}
			else
			{
				g.drawImage(icon,x,y,(int)((int)bounds.getWidth()*zoom),(int)((int)bounds.getHeight()*zoom),null);
			}
		}	
			
		if ( PuckConfiguration.getInstance().getProperty("showRoomNames").equals("true") || isHighlighted() )
		{
		
			g.setColor(GraphColorSettings.getInstance().getColorSetting("text"));
			Font oldFont = g.getFont();
			Font newFont = oldFont.deriveFont(getNameFontSize());
			g.setFont(newFont);
			int swidth = g.getFontMetrics().stringWidth(getName());
			g.drawString(getName(),x+12-swidth/2,y+12);
			g.setFont(oldFont);
		
		}
		
		g.setColor(oldColor);	
	}
	
	public String getName()
	{
		if ( associatedPanel != null /*&& associatedPanel instanceof EntityPanel*/ )
		{
			EntityPanel ep = (EntityPanel) associatedPanel;
			return ep.getPanelName();
		}
		else return UIMessages.getInstance().getMessage("unnamed.room");
	}
	
	public String toString()
	{
		return getName();
	}
	
	public void setExplicitSize(int size) 
	{
		explicitSize = size;
		if ( explicitSize >= 0 )
			bounds = new Rectangle(0,0,size,size);
	}
	
}
