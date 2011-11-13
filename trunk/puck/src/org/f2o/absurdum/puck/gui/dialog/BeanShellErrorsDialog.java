/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 03/04/2011 13:17:57
 */
package org.f2o.absurdum.puck.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.f2o.absurdum.puck.i18n.UIMessages;

/**
 * @author carlos
 *
 * Created 2011-04-03
 */
public class BeanShellErrorsDialog extends JDialog 
{

	 private JButton okButton = new JButton(UIMessages.getInstance().getMessage("bsh.errors.ok"));
	
	public BeanShellErrorsDialog ( Window owner , String text )
	{
		super(owner);
		this.setModal(true);
		this.setResizable(false);
		setTitle(UIMessages.getInstance().getMessage("bsh.errors.dialogtitle"));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		
		JLabel jl = new JLabel(UIMessages.getInstance().getMessage("bsh.errors.explanation"));
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel,BoxLayout.LINE_AXIS));
		labelPanel.add(jl);
		getContentPane().add(labelPanel);
		
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		
		JTextArea jta = new JTextArea(10,50);
		jta.setText(text);
		JScrollPane jsp = new JScrollPane(jta);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		getContentPane().add(jsp);
		
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel,BoxLayout.LINE_AXIS));
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(okButton);
		buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		getContentPane().add(buttonsPanel);
		
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		
		pack();
		this.setLocationRelativeTo(null);
		
		okButton.addActionListener( new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	dispose();
		    }
		}
		);
		
	}
	
}
