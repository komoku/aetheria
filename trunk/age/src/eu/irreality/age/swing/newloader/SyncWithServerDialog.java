package eu.irreality.age.swing.newloader;

import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.config.AGEConfiguration;

public class SyncWithServerDialog extends JDialog 
{
	
	private JButton buttonOk = new JButton("button.ok");
	private JButton buttonCancel = new JButton("button.can");
	private JButton buttonChangeUrl = new JButton("gameloader.change.url");

	public SyncWithServerDialog ( Frame parent , boolean modal )
	{
		super(parent,modal);
		setTitle(UIMessages.getInstance().getMessage("gameloader.sync"));
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		getContentPane().add(new JLabel(UIMessages.getInstance().getMessage("gameloader.sync.description")));
		getContentPane().add(new JLabel(AGEConfiguration.getInstance().getProperty("catalogURL")));
		//TODO add panel with buttons here
		pack();
		setVisible(true);
	}
	
}
