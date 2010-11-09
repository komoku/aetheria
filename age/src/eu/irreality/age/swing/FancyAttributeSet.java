package eu.irreality.age.swing;

import java.awt.Font;

import javax.swing.text.SimpleAttributeSet;

/**
 * Attribute set extended with the added capability of loading a true type font.
 * @author carlos
 *
 */
public class FancyAttributeSet extends SimpleAttributeSet
{

	private Font font;
	
	public void setFont ( Font f )
	{
		font = f;
	}
	
	public Font getFont()
	{
		return font;
	}
	
}
