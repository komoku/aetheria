package eu.irreality.age.swing.newloader;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.transform.TransformerException;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.config.AGEConfiguration;

public class SyncWithServerDialog extends JDialog 
{
	
	private JButton buttonOk = new JButton(UIMessages.getInstance().getMessage("button.ok"));
	private JButton buttonCancel = new JButton(UIMessages.getInstance().getMessage("button.can"));
	//private JButton buttonChangeUrl = new JButton("gameloader.change.url");

	private JTextField urlField = new JTextField();
	
	public SyncWithServerDialog ( Frame parent , boolean modal , final NewLoaderGamePanel thePanel )
	{
		super(parent,modal);
		setTitle(UIMessages.getInstance().getMessage("gameloader.sync"));
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		getContentPane().add(Box.createVerticalStrut(8));
		getContentPane().add(new JLabel(UIMessages.getInstance().getMessage("gameloader.sync.description")));
		getContentPane().add(Box.createVerticalStrut(8));
		urlField.setText(AGEConfiguration.getInstance().getProperty("catalogURL"));
		getContentPane().add(urlField);
		getContentPane().add(Box.createVerticalStrut(8));
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel,BoxLayout.LINE_AXIS));
		
		buttonsPanel.add(Box.createHorizontalStrut(8));
		//buttonsPanel.add(buttonChangeUrl);
		//buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(buttonCancel);
		buttonsPanel.add(Box.createHorizontalStrut(8));
		buttonsPanel.add(buttonOk);
		buttonsPanel.add(Box.createHorizontalStrut(8));
		
		getContentPane().add(Box.createVerticalStrut(8));
		getContentPane().add(buttonsPanel);
		getContentPane().add(Box.createVerticalStrut(8));
		
		buttonCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		
		buttonOk.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				try 
				{
					String catalogUrlString = AGEConfiguration.getInstance().getProperty("catalogURL");
					if ( urlField.getText() != null && !urlField.getText().equals("") )
						catalogUrlString = urlField.getText();
					thePanel.syncWithRemoteCatalog(new URL(catalogUrlString), false);
					AGEConfiguration.getInstance().setProperty("catalogURL", catalogUrlString); //only if didn't throw malformed URL exception
					dispose();
				} 
				catch (MalformedURLException e1) 
				{
					thePanel.showError(UIMessages.getInstance().getMessage("exception.malformed.url") + ": " + e1.getLocalizedMessage(),"Whoops!");
					e1.printStackTrace();
				}
			}
		});
		
		getRootPane().setDefaultButton(buttonOk);
		
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
}
