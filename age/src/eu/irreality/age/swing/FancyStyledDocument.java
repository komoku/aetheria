/*
 * created on denebola3, 2010-11-09
 */

package eu.irreality.age.swing;

import java.awt.Font;

import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;

/**
 * StyledDocument with the capability of handling FancyAttributeSets
 * with loaded true type fonts.
 * @author carlos
 *
 */
public class FancyStyledDocument extends DefaultStyledDocument 
{
	
	public Font getFont(AttributeSet attr)
	{
		if ( attr instanceof FancyAttributeSet )
		{
			if ( ((FancyAttributeSet)attr).getFont() != null )
				return ((FancyAttributeSet)attr).getFont();
		}
		return super.getFont(attr);
	}
	
}
