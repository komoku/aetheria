package org.f2o.absurdum.puck.gui.panels.code;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public interface RSyntaxOptionApplier 
{

	/**
	 * Enables/disables a configuration option for one given text area.
	 * @param ta
	 */
	public void setOptionEnabled ( RSyntaxTextArea ta , boolean enabled );
	
}
