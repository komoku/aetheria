/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 02/04/2011 13:19:37
 */
package org.f2o.absurdum.puck.gui.dialog;

import javax.swing.table.DefaultTableModel;

/**
 * @author carlos
 *
 */
public class VerbListTableModel extends DefaultTableModel 
{

	public VerbListTableModel(Object[][] rowData, Object[] columnNames) 
	{
        super(rowData, columnNames);
	}
	
	public boolean isCellEditable ( int rowIndex , int columnIndex )
	{
		return false;
	}
	
}
