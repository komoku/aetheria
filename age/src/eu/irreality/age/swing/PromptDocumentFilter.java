/*
 * (c) 2000-2010 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Document filter that disallows to edit the first n characters, marked as prompt.
 * @author Carlos Gómez
 * Created at mortadelo, 08/04/2010 15:49:14
 */
public class PromptDocumentFilter extends DocumentFilter
{

	private String leftPrompt = "";
	private String rightPrompt = "";

	public PromptDocumentFilter(String leftPrompt , String rightPrompt)
	{
		this.leftPrompt = leftPrompt;
		this.rightPrompt = rightPrompt;
	}
	
	public String getLeftPrompt() { return leftPrompt; }
	public String getRightPrompt() { return rightPrompt; }
	public void setLeftPrompt ( String leftPrompt ) { this.leftPrompt = leftPrompt; }
	public void setRightPrompt ( String rightPrompt ) { this.rightPrompt = rightPrompt; }
	
	public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException 
	{
		if ( offset >= leftPrompt.length() && offset <= fb.getDocument().getLength()-rightPrompt.length() )
			fb.insertString(offset, string, attr);
	}

	public void remove(FilterBypass fb, int offset, int length) throws BadLocationException 
	{
		int leftmost = offset;
		int rightmost = offset+length;
		leftmost = Math.max(leftmost,leftPrompt.length());
		leftmost = Math.min(leftmost,fb.getDocument().getLength()-rightPrompt.length());
		rightmost = Math.min(rightmost,fb.getDocument().getLength()-rightPrompt.length());
		rightmost = Math.max(rightmost,leftPrompt.length());
		fb.remove(leftmost,rightmost-leftmost);
	}

	public void replace(FilterBypass fb, int offset, int length, String text,
			AttributeSet attrs) throws BadLocationException 
	{
		int leftmost = offset;
		int rightmost = offset+length;
		leftmost = Math.max(leftmost,leftPrompt.length());
		leftmost = Math.min(leftmost,fb.getDocument().getLength()-rightPrompt.length());
		rightmost = Math.min(rightmost,fb.getDocument().getLength()-rightPrompt.length());
		rightmost = Math.max(rightmost,leftPrompt.length());
		fb.replace(leftmost,rightmost-leftmost,text,attrs);
	}
	
	
}
