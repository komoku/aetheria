/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 23-jul-2005 21:59:26
 * as file TwoStringCellRenderer.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.f2o.absurdum.puck.i18n.Messages;



class PropertiesCellRenderer extends JLabel implements ListCellRenderer 
{

	
	public PropertiesCellRenderer()
	{
		;
	}

    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.

    public Component getListCellRendererComponent(
      JList list,
      Object value,            // value to display
      int index,               // cell index
      boolean isSelected,      // is the cell selected
      boolean cellHasFocus)    // the list and the cell have the focus
    {
    	        
    	String labelText;
    	
    	if ( value instanceof String[] && ((String[])value).length >= 3 )
    	{
    		String[] vals = ((String[])value);
    		
    		labelText = vals[0] + " = " + vals[1];
    		if ( vals[2] != null && vals[2].length() > 0 )
    		{
        		labelText += " (" + vals[2] + ")";
    		}

    	}
    	else
    		labelText = value.toString();
    	
    	setText(labelText);
    	
    	if ( isSelected )
    		this.setForeground(Color.RED);
    	else
    		this.setForeground(Color.BLACK);
    	return this;
    	
    
    }
}

