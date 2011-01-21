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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.panels.CharPanel;
import org.f2o.absurdum.puck.gui.panels.EntityPanel;
import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;
import org.f2o.absurdum.puck.gui.skin.ImageManager;
import org.f2o.absurdum.puck.i18n.Messages;


/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:58:52
 */
public class CharacterNode extends Node 
{
	
	private static int defaultSize = Integer.parseInt(PuckConfiguration.getInstance().getProperty("characterDisplaySize"));
	
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
			associatedPanel = new CharPanel( this );
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
		return new CharacterNode((int)bounds.getX(),(int)bounds.getY());
	}
	
	public CharacterNode(int x, int y)
	{
		bounds.setLocation(x,y);
	}
	
	private static Image icon = ImageManager.getInstance().getImage("char");
	
	public static void refreshIcon()
	{
		icon = ImageManager.getInstance().getImage("char");
	}
	
	//private static final String IMAGE_FILE = "images/guy.gif";
	
	/*
	static
	{
		try
		{
			//icon = ImageIO.read( new File("images/guy.gif") );
			icon = ImageIO.read(CharacterNode.class.getClassLoader().getResource(IMAGE_FILE));
		}
		catch ( Exception e ) 
		{
			;
		}
	}
	*/
	
	public void paint ( Graphics g  )
	{
		updateBounds();
		
		Color oldColor = g.getColor();
		
		if ( PuckConfiguration.getInstance().getProperty("showCharacterNodes").equals("true") || isHighlighted() )
		{
			g.setColor(Color.BLUE);
			g.drawOval((int)bounds.getX(),(int)bounds.getY(),(int)bounds.getWidth(),(int)bounds.getHeight());
		}
		
		if ( PuckConfiguration.getInstance().getProperty("showCharacterNames").equals("true") || isHighlighted() )
		{
			g.setColor(Color.BLACK);
			Font oldFont = g.getFont();
			Font newFont = oldFont.deriveFont(getNameFontSize());
			g.setFont(newFont);
			int swidth = g.getFontMetrics().stringWidth(getName());
			g.drawString(getName(),(int)bounds.getX()+12-swidth/2,(int)bounds.getY()+12);
			g.setFont(oldFont);
		}
		
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
	
	//overrides bounds, good for prototypes
	public void paint ( Graphics g , int x , int y , double zoom )
	{
		updateBounds();
		
		Color oldColor = g.getColor();
		//g.setColor(Color.BLUE);
		//g.drawOval(x,y,(int)(bounds.getWidth()*zoom),(int)(bounds.getHeight()*zoom));
		
		if ( PuckConfiguration.getInstance().getProperty("showCharacterNodes").equals("true") || isHighlighted() )
		{
			g.drawImage(icon,x,y,(int)((int)bounds.getWidth()*zoom),(int)((int)bounds.getHeight()*zoom),null);
		}
		
		if ( PuckConfiguration.getInstance().getProperty("showCharacterNames").equals("true") || isHighlighted() )
		{
			g.setColor(Color.BLACK);
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
		if ( associatedPanel != null && associatedPanel instanceof EntityPanel )
		{
			EntityPanel ep = (EntityPanel) associatedPanel;
			return ep.getName();
		}
		else return Messages.getInstance().getMessage("unnamed.char");
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
