/*
 * (c) 2005-2006 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 15-dic-2006 17:45:58
 * as file CursorHandler.java on package org.f2o.absurdum.puck.gui.cursors
 */
package org.f2o.absurdum.puck.gui.cursors;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.f2o.absurdum.puck.gui.skin.ImageManager;

/**
 * @author carlos
 *
 * Created at regulus, 15-dic-2006 17:45:58
 */
public class CursorHandler 
{
	
	/**singleton instance*/
	private static CursorHandler instance = new CursorHandler();
	
	private HashMap cursors = new HashMap();
	
	public Cursor getCursor(String cursorName)
	{
		return (Cursor) cursors.get(cursorName);
	}
	
	/**singleton constructor*/
	private CursorHandler()
	{
		/*
		cursors.put("ADD",CursorFactory.getInstance().getCursorFromImage(new File("images/addCursor32t.gif"),"ADD"));
		cursors.put("ADDARROW",CursorFactory.getInstance().getCursorFromImage(new File("images/addArrowCursor32t.gif"),"ADDARROW"));
		cursors.put("ZOOM",CursorFactory.getInstance().getCursorFromImage(new File("images/looglass32t.gif"),"ZOOM"));
		cursors.put("MOVE",CursorFactory.getInstance().getCursorFromImage(new File("images/trans32t.gif"),"MOVE"));
		cursors.put("DEFAULT",Cursor.getDefaultCursor());
		*/
		//cursors.put("ADD",CursorFactory.getInstance().getCursorFromStream(getClass().getClassLoader().getResourceAsStream("images/addCursor32t.gif"),"ADD"));
		
		InputStream addCursorStream = ImageManager.getInstance().getImageStream("addCursor");
		cursors.put("ADD",CursorFactory.getInstance().getCursorFromStream(addCursorStream,"ADD"));
		
		//cursors.put("ADDARROW",CursorFactory.getInstance().getCursorFromStream(getClass().getClassLoader().getResourceAsStream("images/addArrowCursor32t.gif"),"ADDARROW"));
		addCursorStream = ImageManager.getInstance().getImageStream("addArrowCursor");
		cursors.put("ADDARROW",CursorFactory.getInstance().getCursorFromStream(addCursorStream,"ADDARROW"));
		
		//cursors.put("ZOOM",CursorFactory.getInstance().getCursorFromStream(getClass().getClassLoader().getResourceAsStream("images/looglass32t.gif"),"ZOOM"));
		addCursorStream = ImageManager.getInstance().getImageStream("looGlassCursor");
		cursors.put("ZOOM",CursorFactory.getInstance().getCursorFromStream(addCursorStream,"ZOOM"));
		
		//cursors.put("MOVE",CursorFactory.getInstance().getCursorFromStream(getClass().getClassLoader().getResourceAsStream("images/trans32t.gif"),"MOVE"));
		addCursorStream = ImageManager.getInstance().getImageStream("transCursor");
		cursors.put("MOVE",CursorFactory.getInstance().getCursorFromStream(addCursorStream,"MOVE"));
		
		cursors.put("DEFAULT",Cursor.getDefaultCursor());
	
	}
	
	/**singleton accessor*/
	public static CursorHandler getInstance()
	{
		return instance;
	}


	

}
