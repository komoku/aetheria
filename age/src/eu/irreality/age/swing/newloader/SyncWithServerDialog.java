package eu.irreality.age.swing.newloader;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JFrame;

import eu.irreality.age.i18n.UIMessages;

public class SyncWithServerDialog extends JDialog 
{

	public SyncWithServerDialog ( Frame parent , boolean modal )
	{
		super(parent,modal);
		setTitle(UIMessages.getInstance().getMessage("gameloader.sync"));
	}
	
}
