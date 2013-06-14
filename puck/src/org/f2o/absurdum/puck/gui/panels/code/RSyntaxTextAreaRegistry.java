package org.f2o.absurdum.puck.gui.panels.code;

import java.util.ArrayList;
import java.util.List;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

/**
 * Creates instances of RSyntaxTextArea and has access to all instances, for purposes like changing options that
 * must have effect across all text areas in the application.
 * @author carlos
 *
 */
public class RSyntaxTextAreaRegistry 
{

	/**Text areas for code frames*/
	private List largeTextAreas = new ArrayList();
	
	/**Text areas for code panels*/
	private List smallTextAreas = new ArrayList();
	
	/**Singleton instance*/
	private static RSyntaxTextAreaRegistry instance;
	
	/**
	 * The theme to apply to new code frames
	 */
	private Theme colorTheme;
	
	
	
	private RSyntaxTextAreaRegistry()
	{
	}
	
	public static RSyntaxTextAreaRegistry getInstance()
	{
		if ( instance == null ) instance = new RSyntaxTextAreaRegistry();
		return instance;
	}
	
	public RSyntaxTextArea createLargeTextArea()
	{
		RSyntaxTextArea inst = new RSyntaxTextArea();
		if ( colorTheme != null ) colorTheme.apply(inst);
		largeTextAreas.add(inst);
		return inst;
	}
	
	public RSyntaxTextArea createSmallTextArea()
	{
		RSyntaxTextArea inst = new RSyntaxTextArea();
		if ( colorTheme != null ) colorTheme.apply(inst);
		smallTextAreas.add(inst);
		return inst;
	}
	
	public RSyntaxTextArea createSmallTextArea( int rows , int cols )
	{
		RSyntaxTextArea inst = new RSyntaxTextArea( rows , cols );
		if ( colorTheme != null ) colorTheme.apply(inst);
		smallTextAreas.add(inst);
		return inst;
	}
	
	public List getLargeTextAreas ()
	{
		return largeTextAreas;
	}
	
	public List getSmallTextAreas ()
	{
		return smallTextAreas;
	}
	
	public void setThemeForNewAreas ( Theme theme )
	{
		colorTheme = theme;
	}
	
	public Theme getThemeForNewAreas ( )
	{
		return colorTheme;
	}
	
}
