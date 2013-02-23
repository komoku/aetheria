package org.f2o.absurdum.puck.gui.panels.code;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.f2o.absurdum.puck.i18n.UIMessages;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

public class RSyntaxFindNextOrPrevAction extends AbstractAction 
{

	private RSyntaxTextArea targetTextArea;
	
	/**True for find next, false for find previous*/
	private boolean forward;
	
	public RSyntaxFindNextOrPrevAction ( RSyntaxTextArea target , boolean forward )
	{
		if ( forward )
		{
			this.putValue(Action.NAME, UIMessages.getInstance().getMessage("rsyntax.find.next"));
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
		}
		else 
		{
			this.putValue(Action.NAME, UIMessages.getInstance().getMessage("rsyntax.find.prev"));
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.SHIFT_DOWN_MASK));
		}	
		
		
		this.targetTextArea = target;
		this.forward = forward;
	}

	public void actionPerformed(ActionEvent e) 
	{
		SearchContext sc = RSyntaxSearchHandler.getInstance().getFindDialog().getSearchContext();
		if ( sc == null ) return;
		if ( sc.getSearchFor() == null || sc.getSearchFor().length() < 1 ) return;
		sc.setSearchForward(forward);
		boolean found = SearchEngine.find( targetTextArea , sc );
	    if (!found) 
	    {
	         JOptionPane.showMessageDialog(targetTextArea, "Text not found");
	    }
	}
	
}
