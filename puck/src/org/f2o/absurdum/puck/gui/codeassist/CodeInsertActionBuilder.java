/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.gui.codeassist;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;

import org.jedit.syntax.JEditTextArea;

public class CodeInsertActionBuilder 
{
	
	private JComponent jeta;

	public CodeInsertActionBuilder ( JComponent jeta )
	{
		this.jeta = jeta;
	}
	
	public Action getInsertAction ( final String toInsert )
	{
		
		return new AbstractAction() {
			public void actionPerformed ( ActionEvent evt )
			{
				if ( jeta instanceof JEditTextArea )
				{
					JEditTextArea theJeta = (JEditTextArea) jeta;
					theJeta.select(theJeta.getCaretPosition(),theJeta.getCaretPosition());
					theJeta.setSelectedText(toInsert);
				}
				if ( jeta instanceof JEditorPane )
				{
					JEditorPane theJeta = (JEditorPane) jeta;
					theJeta.select(theJeta.getCaretPosition(),theJeta.getCaretPosition());
					try
					{
						theJeta.getDocument().insertString(theJeta.getCaretPosition(),toInsert,null);
					}
					catch ( BadLocationException ble )
					{
						ble.printStackTrace();
					}
				}
			}
		};
		
	}
	
	
}
