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
import javax.swing.text.Caret;
import javax.swing.text.Element;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
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
				if ( jeta instanceof RSyntaxTextArea )
				{
					//copied some code from StaticCodeTemplate (RSyntaxTextArea lib) which also inserts code
					
					try
					{
					
					RSyntaxTextArea textArea = (RSyntaxTextArea) jeta;
						Caret c = textArea.getCaret();
						int dot = c.getDot();
						int mark = c.getMark();
						int p0 = Math.min(dot, mark);
						int p1 = Math.max(dot, mark);
						RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
						Element map = doc.getDefaultRootElement();

						int lineNum = map.getElementIndex(dot);
						Element line = map.getElement(lineNum);
						int start = line.getStartOffset();
						int end = line.getEndOffset()-1; // Why always "-1"?
						String s = textArea.getText(start,end-start);
						int len = s.length();

						// endWS is the end of the leading whitespace
						// of the current line.
						int endWS = 0;
						while (endWS<len && RSyntaxUtilities.isWhitespace(s.charAt(endWS))) {
							endWS++;
						}
						s = s.substring(0, endWS);
						
						//p0 -= getID().length();
						//String beforeText = getBeforeTextIndented(s);
						//String afterText = getAfterTextIndented(s);
						//doc.replace(p0,p1-p0, beforeText+afterText, null);
						//textArea.setCaretPosition(p0+beforeText.length());
						doc.replace(p0,p1-p0,toInsert,null);
						textArea.setCaretPosition(p0+toInsert.length());
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
