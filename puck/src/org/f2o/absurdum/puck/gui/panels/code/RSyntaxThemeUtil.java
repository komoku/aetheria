package org.f2o.absurdum.puck.gui.panels.code;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;

public class RSyntaxThemeUtil
{

	private static boolean THEMES_COLOR_ONLY = true; //apply only color when applying themes (not font size).
	
	public static void applyTheme ( Theme theme , RSyntaxTextArea ta )
	{
		RSyntaxTextAreaRegistry.getInstance().setThemeForNewAreas(theme); //so that new text areas are created with the current theme
		if ( THEMES_COLOR_ONLY )
		{
			int theFontSize = ta.getFont().getSize();
			System.err.println("Fontsize for " + ta + " is " + theFontSize);
			theme.apply(ta);
			//ta.setFont(ta.getFont().deriveFont(theFontSize));
			

		         SyntaxScheme ss = ta.getSyntaxScheme();
		         ss = (SyntaxScheme) ss.clone();
		         for (int i = 0; i < ss.getStyleCount(); i++) {
		            if (ss.getStyle(i) != null) {
		               ss.getStyle(i).font = ss.getStyle(i).font.deriveFont(theFontSize);
		            }
		         }
		         ta.setSyntaxScheme(ss);
		         
		         ta.setFont(ta.getFont().deriveFont(theFontSize));
			
		}
		else
		{
			theme.apply(ta);
		}
	}
	
}
