package org.f2o.absurdum.puck.gui.panels.code;

import org.f2o.absurdum.puck.gui.panels.EntityPanel;

public class JSyntaxCodePanelFactory extends BSHCodePanelFactory
{
	
	public BSHCodePanel createPanel()
	{
		return new JSyntaxBSHCodePanel();
	}
	
	public BSHCodePanel createPanel( String context )
	{
		return new JSyntaxBSHCodePanel( context);
	}
	
	public BSHCodePanel createPanel( String context , EntityPanel ep )
	{
		return new JSyntaxBSHCodePanel( context , ep );
	}

}
