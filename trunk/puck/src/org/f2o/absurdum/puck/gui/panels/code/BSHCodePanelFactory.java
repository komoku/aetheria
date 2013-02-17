package org.f2o.absurdum.puck.gui.panels.code;

import org.f2o.absurdum.puck.gui.panels.EntityPanel;

public abstract class BSHCodePanelFactory 
{
	
	private static BSHCodePanelFactory instance = new JSyntaxCodePanelFactory();
	
	public static BSHCodePanelFactory getInstance()
	{
		return instance;
	}
	
	public abstract BSHCodePanel createPanel();
	public abstract BSHCodePanel createPanel( String context );
	public abstract BSHCodePanel createPanel( String context , EntityPanel ep );
	
}
