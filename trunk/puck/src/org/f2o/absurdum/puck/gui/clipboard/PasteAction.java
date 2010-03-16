/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.gui.clipboard;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import org.f2o.absurdum.puck.gui.PasteNodeAction;
import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.jedit.syntax.JEditTextArea;

public class PasteAction extends DefaultEditorKit.PasteAction
{

	public PasteAction()
	{
		super();
	}
	
	public void actionPerformed ( ActionEvent e )
	{
		
		KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		Component focusOwner = kfm.getPermanentFocusOwner();
		
		if ( focusOwner instanceof JTextComponent )
		{
			super.actionPerformed(e);
		}
		
		else //not pasted to a standard text comp.
		{
			if ( focusOwner != null )
			{
				if ( focusOwner instanceof JEditTextArea ) //not e.getSource(). How to find the focused component?????
				{
					JEditTextArea jta = (JEditTextArea) focusOwner;
					jta.paste();
				}
				else
				{
					//no text component, and no jedit text area? well, let's try to paste on the graph editing panel then.
					Window w = kfm.getFocusedWindow();
					if ( w instanceof PuckFrame )
					{
						PuckFrame pf = (PuckFrame) w;
						GraphEditingPanel gep = pf.getGraphEditingPanel();
						new PasteNodeAction(gep).actionPerformed(e);
					}
				}
			}
			
		}
	}
	
}
