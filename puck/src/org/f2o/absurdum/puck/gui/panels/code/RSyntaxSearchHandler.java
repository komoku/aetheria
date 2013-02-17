package org.f2o.absurdum.puck.gui.panels.code;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchDialogSearchContext;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchEngine;

/**
 * This class manages a RSyntaxTextArea FindDialog and ReplaceDialog, and uses them to find/replace code.
 * The pair of dialogs is common to all RSyntaxTextAreas spawned by a given PUCK instance, but the target changes so that we can
 * search in different such text areas.
 * @author carlos
 *
 */
public class RSyntaxSearchHandler implements ActionListener
{

	/**Singleton instance.*/
	private static RSyntaxSearchHandler instance;
	
	/**The find dialog that we are handling.*/
	private FindDialog findDialog;
	
	/**The find dialog that we are handling.*/
	private ReplaceDialog replaceDialog;
	
	/**The text area while the find dialog is performing search.*/
	private RSyntaxTextArea currentTarget;
	
	public static RSyntaxSearchHandler getInstance ( )
	{
		if ( instance == null )
			instance = new RSyntaxSearchHandler ( null );
		return instance;
	}
	
	/**
	 * Creates a handler for a given PUCK instance (parent frame).
	 * Currently, we always pass null as the parent frame.
	 * @param parentFrame
	 */
	private RSyntaxSearchHandler ( Frame parentFrame )
	{
		findDialog = new FindDialog ( parentFrame , this );
		replaceDialog = new ReplaceDialog ( parentFrame , this );
		replaceDialog.setSearchContext(findDialog.getSearchContext());
	}
	
	/**
	 * Actually performs the search.
	 */
	public void actionPerformed ( ActionEvent e )
	{
		String command = e.getActionCommand();
		SearchDialogSearchContext context = findDialog.getSearchContext();

		if (FindDialog.ACTION_FIND.equals(command)) 
		{
			if (!SearchEngine.find(currentTarget, context)) 
			{
				UIManager.getLookAndFeel().provideErrorFeedback(currentTarget);
			}
		}
		else if (ReplaceDialog.ACTION_REPLACE.equals(command)) 
		{
			if (!SearchEngine.replace(currentTarget, context)) 
			{
				UIManager.getLookAndFeel().provideErrorFeedback(currentTarget);
			}
		}
		else if (ReplaceDialog.ACTION_REPLACE_ALL.equals(command)) 
		{
			int count = SearchEngine.replaceAll(currentTarget, context);
			JOptionPane.showMessageDialog(null, count
					+ " occurrences replaced.");
		}
	}
	
	/**
	 * Changes the text area in which to perform search.
	 * @param newTarget
	 */
	public void setTarget ( RSyntaxTextArea newTarget )
	{
		currentTarget = newTarget;
	}
	
	/**
	 * Returns the find dialog associated with this handler.
	 * @return
	 */
	public FindDialog getFindDialog()
	{
		return findDialog;
	}
	
	/**
	 * Returns the find dialog associated with this handler.
	 * @return
	 */
	public FindDialog getReplaceDialog()
	{
		return findDialog;
	}
	
	/**
	 * Makes the find dialog visible, making the replace dialog invisible if needed.
	 */
	public void showFindDialog()
	{
		findDialog.setAlwaysOnTop(true);
		if (replaceDialog.isVisible()) {
			replaceDialog.setVisible(false);
		}
		findDialog.setVisible(true);
	}
	
	/**
	 * Makes the replace dialog visible, making the find dialog invisible if needed.
	 */
	public void showReplaceDialog()
	{
		replaceDialog.setAlwaysOnTop(true);
		if (findDialog.isVisible()) {
			findDialog.setVisible(false);
		}
		replaceDialog.setVisible(true);
	}
	
}
