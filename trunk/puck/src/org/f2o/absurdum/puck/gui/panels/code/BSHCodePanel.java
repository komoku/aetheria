package org.f2o.absurdum.puck.gui.panels.code;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class BSHCodePanel extends JPanel
{

	public abstract Node getXML ( Document d ); //TODO: pull up code?
	public abstract void initFromXML ( org.w3c.dom.Node n ); //TODO: pull up code?
	public abstract String getCode(); //TODO: pull up code?
	
}
