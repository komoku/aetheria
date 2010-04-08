/*
 * (c) 2000-2010 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.swing;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * JTextField with prompt support.
 * @author Carlos Gómez
 * Created at mortadelo, 08/04/2010 15:46:38
 *
 */
public class FancyJTextField extends JTextField
{

	private static final long serialVersionUID = 2248223696068023706L;

	private String leftPrompt = "";
	private String rightPrompt = "";
	private PromptDocumentFilter theDocFilter = new PromptDocumentFilter(leftPrompt,rightPrompt);	
	private PromptNavigationFilter theNavFilter; 	
	
	public void setPrompts ( String l , String r )
	{
		String left = l;
		String right = r;
		if ( left == null ) left = "";
		if ( right == null ) right = "";
		setLeftPrompt(left);
		setRightPrompt(right);
	}
	
	private void setLeftPrompt ( String newLeftPrompt )
	{
		AbstractDocument ad = (AbstractDocument) this.getDocument();
		ad.setDocumentFilter(null);
		MutableAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setItalic(attributes,true);
		try
		{
			ad.remove(0,leftPrompt.length());
			ad.insertString(0,newLeftPrompt,attributes);
		}
		catch ( BadLocationException ble )
		{
			ble.printStackTrace();
		}
		leftPrompt = newLeftPrompt;
		theDocFilter.setLeftPrompt(newLeftPrompt);
		theNavFilter.setLeftPrompt(newLeftPrompt);
		this.setCaretPosition(leftPrompt.length());
		ad.setDocumentFilter(theDocFilter);
	}
	
	private void setRightPrompt ( String newRightPrompt )
	{
		AbstractDocument ad = (AbstractDocument) this.getDocument();
		ad.setDocumentFilter(null);
		MutableAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setItalic(attributes,true);
		try
		{
			ad.remove(ad.getLength()-rightPrompt.length(),rightPrompt.length());
			ad.insertString(ad.getLength(),newRightPrompt,attributes);
		}
		catch ( BadLocationException ble )
		{
			ble.printStackTrace();
		}
		rightPrompt = newRightPrompt;
		theDocFilter.setRightPrompt(newRightPrompt);
		theNavFilter.setRightPrompt(newRightPrompt);
		this.setCaretPosition(leftPrompt.length());
		ad.setDocumentFilter(theDocFilter);
	}
	
	public FancyJTextField ( int columns )
	{
		super(columns);
		AbstractDocument ad = (AbstractDocument) this.getDocument();
		ad.setDocumentFilter(theDocFilter);
		theNavFilter = new PromptNavigationFilter(leftPrompt,rightPrompt,ad);
		this.setNavigationFilter(theNavFilter);
	}
	
	public String getText()
	{
		try 
		{
			return this.getDocument().getText(leftPrompt.length(),this.getDocument().getLength()-rightPrompt.length()-leftPrompt.length());
		} 
		catch (BadLocationException e) 
		{

			e.printStackTrace();
			return "";
		} 
	}
		
	
}
