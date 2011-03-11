/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 11/03/2011 19:05:59
 */
package org.f2o.absurdum.puck.util.swing;

import java.awt.Color;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author carlos
 *
 * A subclass of JList that takes a gray colour to prevent users from mistaking it
 * with an input field.
 */
public class EnhancedJList extends JList 
{

	public EnhancedJList()
	{
		super();
		init();
	}
	
	public EnhancedJList(ListModel m)
	{
		super(m);
		init();
	}
	
	public EnhancedJList(Object[] data)
	{
		super(data);
		init();
	}
	
	public EnhancedJList(Vector listData)
	{
		super(listData);
		init();
	}
	
	public void init()
	{
		addListener();
		updateLooks();
	}
	
	private void addListener()
	{
		this.getModel().addListDataListener( new ListDataListener()
		{
			public void intervalAdded(ListDataEvent e) 
			{
				updateLooks();
			}

			public void intervalRemoved(ListDataEvent e) 
			{
				updateLooks();				
			}

			@Override
			public void contentsChanged(ListDataEvent e) 
			{
				updateLooks();
			}
			
		}
		);
	}
	
	private static Color getDefaultListBackgroundColor()
	{
		Color c = UIManager.getColor("List.background");
		if ( c != null ) return c;
		else return Color.WHITE;
	}
	
	public static Color getDisabledListBackgroundColor()
	{
		//There isn't a property for the disabled color of a JList (since by default they are white, just like enabled).
		//But we can assume the disabled color of a JTextField will be decent for a JList too.
		Color c = UIManager.getColor("TextField.inactiveBackground");
		if ( c != null ) return c;
		else return new Color(230,230,230);
	}
	
	/**
	 * Disables the list if it is empty (so that users don't confuse it with the input field
	 * and mistakenly try to type entries into the list) or enables it if it is not empty.
	 */
	public void updateLooks ( )
	{
		if ( getModel().getSize() > 0 )
		{
			setEnabled(true);
			setBackground(getDefaultListBackgroundColor());
			repaint();
			//theList.setBackground(Color.GREEN);
		}
		else
		{
			setEnabled(false);
			setBackground(getDisabledListBackgroundColor());
			repaint();
			//theList.setBackground(Color.RED);
		}
	}
	
	
}
