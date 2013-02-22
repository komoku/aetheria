package org.f2o.absurdum.puck.gui.panels.code;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class RSyntaxWordWrapAction extends AbstractAction
{

	private RSyntaxTextArea ta1, ta2;
	
	public RSyntaxWordWrapAction( RSyntaxTextArea ta1 , RSyntaxTextArea ta2 ) 
	{
		this.ta1 = ta1;
		this.ta2 = ta2;
		putValue(NAME, "Word Wrap");
	}

	public void actionPerformed(ActionEvent e) 
	{
		//change the line wrap option in both text areas
		ta1.setLineWrap(!ta1.getLineWrap());
		ta2.setLineWrap(!ta2.getLineWrap());
		
		//save the configuration so it will be kept for future sessions
		PuckConfiguration.getInstance().setProperty("rsyntaxWordWrap", String.valueOf(ta1.getLineWrap()));
	}
	
	public boolean isOptionEnabled()
	{
		return ta1.getLineWrap();
	}

}
