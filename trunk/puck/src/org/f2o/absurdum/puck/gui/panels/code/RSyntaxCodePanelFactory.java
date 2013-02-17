package org.f2o.absurdum.puck.gui.panels.code;

import org.f2o.absurdum.puck.gui.panels.EntityPanel;

public class RSyntaxCodePanelFactory extends BSHCodePanelFactory
{

	public BSHCodePanel createPanel()
	{
		return new RSyntaxBSHCodePanel();
	}
	
	public BSHCodePanel createPanel( String context )
	{
		return new RSyntaxBSHCodePanel( context);
	}
	
	public BSHCodePanel createPanel( String context , EntityPanel ep )
	{
		return new RSyntaxBSHCodePanel( context , ep );
	}
	
}
