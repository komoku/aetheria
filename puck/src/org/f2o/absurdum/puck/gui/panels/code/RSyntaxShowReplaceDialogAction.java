package org.f2o.absurdum.puck.gui.panels.code;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.fife.rsta.ui.search.FindDialog;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class RSyntaxShowReplaceDialogAction extends AbstractAction
{
		
	private Frame dialogParent;
	private RSyntaxTextArea targetTextArea;
	
	/**
	 * Obtains the find handler associated with the given parent frame (if it doesn't already exists, we create it),
	 * and sets it to search with the target text area.
	 * Currently we are using null as dialog parent all the time.
	 * @param dialogParent Unused.
	 * @return
	 */
	public RSyntaxSearchHandler readySearchHandler ( Frame dialogParent , RSyntaxTextArea targetTextArea )
	{
		//RSyntaxSearchHandler fh = (RSyntaxSearchHandler) findHandlers.get(dialogParent);
		
		RSyntaxSearchHandler fh = RSyntaxSearchHandler.getInstance();
		
		/*
		if ( fh == null )
		{
			fh = RSyntaxSearchHandler.getInstance();
			//findHandlers.put(dialogParent, fh);
		}
		fh.setTarget(targetTextArea);
		*/
		fh.setTarget(targetTextArea);
		return fh;
	}
	
	public RSyntaxSearchHandler readyFindHandler ( RSyntaxTextArea targetTextArea )
	{
		return readySearchHandler ( null , targetTextArea );
	}
	
	private RSyntaxShowReplaceDialogAction( Frame dialogParent , RSyntaxTextArea targetTextArea ) 
	{
		super("Replace...");
		//FindDialog findDialog = dialogForParent ( dialogParent );
		int c = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, c));
		this.dialogParent = dialogParent;
		this.targetTextArea = targetTextArea;
	}
	
	public RSyntaxShowReplaceDialogAction( RSyntaxTextArea targetTextArea )
	{
		this ( null , targetTextArea );
	}

	
	public void actionPerformed(ActionEvent e) 
	{
		RSyntaxSearchHandler fh = readySearchHandler ( dialogParent , targetTextArea );
		fh.showReplaceDialog();
	}
	
}
