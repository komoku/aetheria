/*
 * (c) 2000-2010 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.swing;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position.Bias;

/**
 * 
 * @author Carlos Gómez
 * Created at mortadelo, 08/04/2010 16:49:47
 *
 */
public class PromptNavigationFilter extends NavigationFilter
{

	private String leftPrompt = "";
	private String rightPrompt = "";
	private Document doc;

	public PromptNavigationFilter(String leftPrompt , String rightPrompt , Document doc)
	{
		this.leftPrompt = leftPrompt;
		this.rightPrompt = rightPrompt;
		this.doc = doc;
	}
	
	public String getLeftPrompt() { return leftPrompt; }
	public String getRightPrompt() { return rightPrompt; }
	public void setLeftPrompt ( String leftPrompt ) { this.leftPrompt = leftPrompt; }
	public void setRightPrompt ( String rightPrompt ) { this.rightPrompt = rightPrompt; }

	/*
	public int getNextVisualPositionFrom(JTextComponent text, int pos,
			Bias bias, int direction, Bias[] biasRet)
			throws BadLocationException {
		// TODO Auto-generated method stub
		return super.getNextVisualPositionFrom(text, pos, bias, direction, biasRet);
	}
	*/

	public void moveDot(FilterBypass fb, int dot, Bias bias) 
	{
		int pos = Math.max(dot,leftPrompt.length());
		pos = Math.min(pos,doc.getLength()-rightPrompt.length());
		fb.moveDot(pos, bias);
	}

	public void setDot(FilterBypass fb, int dot, Bias bias) 
	{
		int pos = Math.max(dot,leftPrompt.length());
		pos = Math.min(pos,doc.getLength()-rightPrompt.length());
		fb.setDot(pos, bias);
	}
	
	
}
