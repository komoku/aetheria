/*
 * (c) 2005-2010 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package org.f2o.absurdum.puck.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.gui.panels.WorldPanel;
import org.f2o.absurdum.puck.i18n.UIMessages;
import org.f2o.absurdum.puck.util.swing.DialogUtils;

public class FindEntityDialog extends JDialog
{

	private JButton findButton = new JButton(UIMessages.getInstance().getMessage("button.find"));
	private JButton closeButton = new JButton(UIMessages.getInstance().getMessage("button.clo"));
	private JTextField nameTextField = new JTextField(20);
	private JLabel resultLabel = new JLabel();
	
	public FindEntityDialog ( final PuckFrame parent , boolean modal )
	{
		
		super(parent,modal);
		this.setTitle(UIMessages.getInstance().getMessage("dialog.find.entity"));
		
		this.getContentPane().setLayout(new BorderLayout());
		
		JPanel mainPanel = new JPanel(new FlowLayout());
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel,BoxLayout.LINE_AXIS));
		
		mainPanel.add(new JLabel(UIMessages.getInstance().getMessage("label.find.entity")));
		mainPanel.add(nameTextField);
		getContentPane().add(mainPanel,BorderLayout.CENTER);
		
		buttonsPanel.add(resultLabel);
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(findButton);
		buttonsPanel.add(closeButton);
		getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
		
		closeButton.addActionListener ( new ActionListener() 
		{		
			public void actionPerformed(ActionEvent e) 
			{
				dispose();
			}
		} );
		
		DialogUtils.registerEscapeAction(this);
		
		findButton.addActionListener ( new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				WorldPanel wp = (WorldPanel) parent.getGraphEditingPanel().getWorldNode().getAssociatedPanel();
				if ( nameTextField.getText() != null )
				{
					Node n = wp.nameToNode(nameTextField.getText());
					if ( n == null )
						resultLabel.setText(UIMessages.getInstance().getMessage("label.entity.notfound"));
					else
					{
						resultLabel.setText(UIMessages.getInstance().getMessage("label.entity.found"));
						/*
						parent.getGraphEditingPanel().resetSelections();
						parent.getGraphEditingPanel().selectNode(n);
						parent.getGraphEditingPanel().getPropertiesPanel().show(n);
						parent.getGraphEditingPanel().centerViewOn(n);
						parent.getGraphEditingPanel().repaint();
						*/
						parent.getGraphEditingPanel().focusOnNode(n,true);
					}
				}
			}
		} );
		
		this.getRootPane().setDefaultButton(findButton);
		
		pack();
		setLocationRelativeTo(null);
		
	}
	
	public void setVisible ( boolean visible )
	{
		super.setVisible(visible);
		if ( visible == true ) this.getRootPane().setDefaultButton(findButton);
	}
	
}
