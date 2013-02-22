package org.f2o.absurdum.puck.gui.panels.code;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**Defines how to toggle/untoggle an option on two RSyntaxTextAreas.*/
public interface RSyntaxOptionToggler 
{

	/**
	 * Sets the option as enabled or disabled in the text areas..
	 */
	public abstract void setOptionEnabled(RSyntaxTextArea ta1 , RSyntaxTextArea ta2 , boolean enabled);
	
	/**
	 * Returns whether the option is currently enabled.
	 * @return
	 */
	public abstract boolean isOptionEnabled(RSyntaxTextArea ta1 , RSyntaxTextArea ta2);
	
}
