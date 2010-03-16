/*
 * (c) 2005-2006 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 15-dic-2006 17:43:31
 * as file CursorFactory.java on package org.f2o.absurdum.puck.gui.cursors
 */
package org.f2o.absurdum.puck.gui.cursors;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * @author carlos
 *
 * Created at regulus, 15-dic-2006 17:43:31
 */
public class CursorFactory 
{

	/**singleton instance*/
	private static CursorFactory instance = new CursorFactory();
	
	/**singleton constructor*/
	private CursorFactory(){;}
	
	/**singleton accessor*/
	public static CursorFactory getInstance()
	{
		return instance;
	}

	public Cursor getCursorFromImage ( File f , String newCursorName )
	{
		//Image img = Toolkit.getDefaultToolkit().createImage("addCursor32.png");
		Image img = null;
		try
		{
			img = ImageIO.read(f);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		Cursor cur = Toolkit.getDefaultToolkit().createCustomCursor(img,new Point(0,0),newCursorName);
		return cur;
	}
	
	public Cursor getCursorFromStream ( InputStream is , String newCursorName )
	{
		//Image img = Toolkit.getDefaultToolkit().createImage("addCursor32.png");
		Image img = null;
		try
		{
			img = ImageIO.read(is);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		Cursor cur = Toolkit.getDefaultToolkit().createCustomCursor(img,new Point(0,0),newCursorName);
		return cur;
	}
	
	
}
