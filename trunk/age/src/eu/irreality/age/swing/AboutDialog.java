/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 13/11/2011 17:30:26
 */
package eu.irreality.age.swing;

import java.awt.Frame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import eu.irreality.age.i18n.UIMessages;

/**
 * @author carlos
 *
 */
public class AboutDialog extends JDialog
{

	public AboutDialog( Frame parent )
	{
		super(parent, UIMessages.getInstance().getMessage("about.frame.title"));
		setLocationRelativeTo(parent);
		
		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel,BoxLayout.PAGE_AXIS));
		aboutPanel.add(Box.createVerticalStrut(30));
		aboutPanel.add(new JLabel("Aetheria Game Engine"));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel(
				UIMessages.getInstance().getMessage("about.frame.version")
			+ " " + UIMessages.getInstance().getMessage("age.version")
		));
		aboutPanel.add(Box.createVerticalStrut(10));
		aboutPanel.add(new JLabel(
				UIMessages.getInstance().getMessage("about.frame.download")
			+ " " + UIMessages.getInstance().getMessage("age.download.url")
		));
		aboutPanel.add(Box.createVerticalStrut(30));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.LINE_AXIS));
		getContentPane().add(Box.createHorizontalStrut(20));
		getContentPane().add(aboutPanel);
		getContentPane().add(Box.createHorizontalStrut(20));
		
		pack();
		setVisible(true);
	}
	
}
