/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

/*
 * Created at regulus on 09-mar-2009 12:32:10
 * as file IconSizesDialog.java on package org.f2o.absurdum.puck.gui.dialog
 */
package org.f2o.absurdum.puck.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.graph.AbstractEntityNode;
import org.f2o.absurdum.puck.gui.graph.CharacterNode;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
import org.f2o.absurdum.puck.gui.graph.RoomNode;
import org.f2o.absurdum.puck.gui.graph.SpellNode;
import org.f2o.absurdum.puck.i18n.Messages;

/**
 * @author carlos
 *
 * Created at regulus, 09-mar-2009 12:32:10
 */
public class IconSizesDialog extends JDialog 
{
	
	private JSlider roomSlider = new JSlider(5,80);
	private JSlider itemSlider = new JSlider(5,80);
	private JSlider charSlider = new JSlider(5,80);
	private JSlider spellSlider = new JSlider(5,80);
	private JSlider absSlider = new JSlider(5,80);
	
	//private JButton bApply = new JButton("button.app");
	//private JButton bCancel = new JButton("button.can");
	private JButton bClose = new JButton(Messages.getInstance().getMessage("button.clo"));
	
	public IconSizesDialog ( final PuckFrame parent , boolean modal )
	{
		
		super(parent,modal);
		this.setTitle(Messages.getInstance().getMessage("dialog.sizes"));
		
		roomSlider.setValue(RoomNode.getDefaultSize());
		itemSlider.setValue(ItemNode.getDefaultSize());
		charSlider.setValue(CharacterNode.getDefaultSize());
		spellSlider.setValue(SpellNode.getDefaultSize());
		absSlider.setValue(AbstractEntityNode.getDefaultSize());
		
		/*
		roomSlider.setLabelTable(roomSlider.createStandardLabels(10));
		itemSlider.setLabelTable(itemSlider.createStandardLabels(10));
		charSlider.setLabelTable(charSlider.createStandardLabels(10));
		spellSlider.setLabelTable(spellSlider.createStandardLabels(10));
		absSlider.setLabelTable(absSlider.createStandardLabels(10));
		*/
		
		roomSlider.setMajorTickSpacing(10);
		itemSlider.setMajorTickSpacing(10);
		charSlider.setMajorTickSpacing(10);
		spellSlider.setMajorTickSpacing(10);
		absSlider.setMajorTickSpacing(10);
		
		roomSlider.setMinorTickSpacing(5);
		itemSlider.setMinorTickSpacing(5);
		charSlider.setMinorTickSpacing(5);
		spellSlider.setMinorTickSpacing(5);
		absSlider.setMinorTickSpacing(5);
		
		roomSlider.setPaintLabels(true);
		itemSlider.setPaintLabels(true);
		charSlider.setPaintLabels(true);
		spellSlider.setPaintLabels(true);
		absSlider.setPaintLabels(true);
		
		roomSlider.setPaintTicks(true);
		itemSlider.setPaintTicks(true);
		charSlider.setPaintTicks(true);
		spellSlider.setPaintTicks(true);
		absSlider.setPaintTicks(true);
		

		
		roomSlider.addChangeListener ( new ChangeListener() 
				{
					public void stateChanged(ChangeEvent e) {
						if (!roomSlider.getValueIsAdjusting())
						{
							RoomNode.setDefaultSize(roomSlider.getValue());
							parent.repaint();
							
						}
					}
				}
		);
		
		itemSlider.addChangeListener ( new ChangeListener() 
				{
					public void stateChanged(ChangeEvent e) {
						if (!itemSlider.getValueIsAdjusting())
						{
							ItemNode.setDefaultSize(itemSlider.getValue());
							parent.repaint();
						}
					}
				}
		);
		
		charSlider.addChangeListener ( new ChangeListener() 
				{
					public void stateChanged(ChangeEvent e) {
						if (!charSlider.getValueIsAdjusting())
						{
							CharacterNode.setDefaultSize(charSlider.getValue());
							parent.repaint();
						}
					}
				}
		);
		
		spellSlider.addChangeListener ( new ChangeListener() 
				{
					public void stateChanged(ChangeEvent e) {
						if (!spellSlider.getValueIsAdjusting())
						{
							SpellNode.setDefaultSize(spellSlider.getValue());
							parent.repaint();
						}
					}
				}
		);
		
		absSlider.addChangeListener ( new ChangeListener() 
				{
					public void stateChanged(ChangeEvent e) {
						if (!absSlider.getValueIsAdjusting())
						{
							AbstractEntityNode.setDefaultSize(absSlider.getValue());
							parent.repaint();
						}
					}
				}
		);
		
		
		this.getContentPane().setLayout ( new BorderLayout() );
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout ( new GridLayout(5,2)  );
	
		
		mainPanel.add(new JLabel(Messages.getInstance().getMessage("sizes.room")));
		mainPanel.add(roomSlider);
		
		mainPanel.add(new JLabel(Messages.getInstance().getMessage("sizes.item")));
		mainPanel.add(itemSlider);
		
		mainPanel.add(new JLabel(Messages.getInstance().getMessage("sizes.character")));
		mainPanel.add(charSlider);
		
		mainPanel.add(new JLabel(Messages.getInstance().getMessage("sizes.spell")));
		mainPanel.add(spellSlider);
		
		mainPanel.add(new JLabel(Messages.getInstance().getMessage("sizes.abstract")));
		mainPanel.add(absSlider);
		
		this.getContentPane().add(mainPanel,BorderLayout.CENTER);
		
		
		JPanel buttonsPanel = new JPanel();
		
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonsPanel.add(Box.createHorizontalGlue());
		
		//buttonsPanel.add(bApply);
		//buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonsPanel.add(bClose);
		
		bClose.addActionListener ( new ActionListener() 
				{		
					public void actionPerformed(ActionEvent e) 
					{
					/*
						RoomNode.setDefaultSize(roomSlider.getValue());
						ItemNode.setDefaultSize(itemSlider.getValue());
						CharacterNode.setDefaultSize(charSlider.getValue());
						SpellNode.setDefaultSize(spellSlider.getValue());
						AbstractEntityNode.setDefaultSize(absSlider.getValue());
					*/
						
						PuckConfiguration.getInstance().setProperty("roomDisplaySize",String.valueOf(roomSlider.getValue()));
						PuckConfiguration.getInstance().setProperty("itemDisplaySize",String.valueOf(itemSlider.getValue()));
						PuckConfiguration.getInstance().setProperty("spellDisplaySize",String.valueOf(spellSlider.getValue()));
						PuckConfiguration.getInstance().setProperty("abstractEntityDisplaySize",String.valueOf(absSlider.getValue()));
						PuckConfiguration.getInstance().setProperty("characterDisplaySize",String.valueOf(charSlider.getValue()));
						
						IconSizesDialog.this.dispose();
					}
				}
		);
		
		getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
		
		pack();
		
		
	}
	
	

	

}
