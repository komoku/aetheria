/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.gui.dialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.graph.AbstractEntityNode;
import org.f2o.absurdum.puck.gui.graph.CharacterNode;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
import org.f2o.absurdum.puck.gui.graph.RoomNode;
import org.f2o.absurdum.puck.gui.graph.SpellNode;
import org.f2o.absurdum.puck.i18n.UIMessages;

import eu.irreality.age.windowing.DialogUtils;

public class ShowHideDialog extends JDialog
{

	private JCheckBox cbRoomNames = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.names.room"));
	private JCheckBox cbItemNames = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.names.item"));
	private JCheckBox cbCharacterNames = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.names.char"));
	private JCheckBox cbSpellNames = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.names.spell"));
	private JCheckBox cbAbstractEntityNames = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.names.abs"));
	private JCheckBox cbArrowNames = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.names.rel"));
	
	private JCheckBox cbRoomNodes = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.nodes.room"));
	private JCheckBox cbItemNodes = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.nodes.item"));
	private JCheckBox cbCharacterNodes = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.nodes.char"));
	private JCheckBox cbSpellNodes = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.nodes.spell"));
	private JCheckBox cbAbstractEntityNodes = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.nodes.abs"));
	private JCheckBox cbArrows = new JCheckBox(UIMessages.getInstance().getMessage("cb.showhide.rel"));
	
	private JButton bClose = new JButton(UIMessages.getInstance().getMessage("button.clo"));
	
	public ShowHideDialog ( final PuckFrame parent , boolean modal )
	{
		
		super(parent,modal);
		this.setTitle(UIMessages.getInstance().getMessage("dialog.showhide"));
		
		this.getContentPane().setLayout( new BorderLayout() );
		JPanel mainPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();
		
		cbRoomNames.setSelected(PuckConfiguration.getInstance().getProperty("showRoomNames").equals("true"));
		cbItemNames.setSelected(PuckConfiguration.getInstance().getProperty("showItemNames").equals("true"));
		cbCharacterNames.setSelected(PuckConfiguration.getInstance().getProperty("showCharacterNames").equals("true"));
		cbSpellNames.setSelected(PuckConfiguration.getInstance().getProperty("showSpellNames").equals("true"));
		cbAbstractEntityNames.setSelected(PuckConfiguration.getInstance().getProperty("showAbstractEntityNames").equals("true"));
		cbArrowNames.setSelected(PuckConfiguration.getInstance().getProperty("showArrowNames").equals("true"));
		
		cbRoomNodes.setSelected(PuckConfiguration.getInstance().getProperty("showRoomNodes").equals("true"));
		cbItemNodes.setSelected(PuckConfiguration.getInstance().getProperty("showItemNodes").equals("true"));
		cbCharacterNodes.setSelected(PuckConfiguration.getInstance().getProperty("showCharacterNodes").equals("true"));
		cbSpellNodes.setSelected(PuckConfiguration.getInstance().getProperty("showSpellNodes").equals("true"));
		cbAbstractEntityNodes.setSelected(PuckConfiguration.getInstance().getProperty("showAbstractEntityNodes").equals("true"));
		cbArrows.setSelected(PuckConfiguration.getInstance().getProperty("showArrows").equals("true"));
		
		mainPanel.setLayout( new GridLayout(6,2) );
		
		mainPanel.add(cbRoomNodes);
		mainPanel.add(cbRoomNames);

		mainPanel.add(cbItemNodes);
		mainPanel.add(cbItemNames);

		mainPanel.add(cbCharacterNodes);
		mainPanel.add(cbCharacterNames);

		mainPanel.add(cbSpellNodes);
		mainPanel.add(cbSpellNames);
		
		mainPanel.add(cbAbstractEntityNodes);
		mainPanel.add(cbAbstractEntityNames);
		
		mainPanel.add(cbArrows);
		mainPanel.add(cbArrowNames);
		
		this.getContentPane().add(mainPanel,BorderLayout.CENTER);
		
		linkToConfigProperty(parent,cbRoomNodes,"showRoomNodes");
		linkToConfigProperty(parent,cbRoomNames,"showRoomNames");
		
		linkToConfigProperty(parent,cbItemNodes,"showItemNodes");
		linkToConfigProperty(parent,cbItemNames,"showItemNames");
		
		linkToConfigProperty(parent,cbCharacterNodes,"showCharacterNodes");
		linkToConfigProperty(parent,cbCharacterNames,"showCharacterNames");
		
		linkToConfigProperty(parent,cbSpellNodes,"showSpellNodes");
		linkToConfigProperty(parent,cbSpellNames,"showSpellNames");
		
		linkToConfigProperty(parent,cbAbstractEntityNodes,"showAbstractEntityNodes");
		linkToConfigProperty(parent,cbAbstractEntityNames,"showAbstractEntityNames");
		
		linkToConfigProperty(parent,cbArrows,"showArrows");
		linkToConfigProperty(parent,cbArrowNames,"showArrowNames");
		
		bClose.addActionListener ( new ActionListener() 
		{		
			public void actionPerformed(ActionEvent e) 
			{

				ShowHideDialog.this.dispose();
			}
		}
);
		
		DialogUtils.registerEscapeAction(this);
		DialogUtils.registerCloseAction(this,KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		
		this.getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(null);
		
	}
	
	private void linkToConfigProperty(final PuckFrame frame , final JCheckBox cb , final String property)
	{
		cb.addItemListener ( new ItemListener() 
		{

			public void itemStateChanged(ItemEvent arg0) 
			{
				String boolString = cb.isSelected() ? "true" : "false";
				PuckConfiguration.getInstance().setProperty(property,boolString);
				frame.repaint();
			}
		}
		);
	}
	
	
	
}
