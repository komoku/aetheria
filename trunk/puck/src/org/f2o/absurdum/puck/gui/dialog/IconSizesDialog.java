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
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.graph.AbstractEntityNode;
import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.CharacterNode;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.gui.graph.RoomNode;
import org.f2o.absurdum.puck.gui.graph.SpellNode;
import org.f2o.absurdum.puck.i18n.UIMessages;

import eu.irreality.age.windowing.DialogUtils;

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
	
	private JSpinner nodeFontSpinner = new JSpinner(new SpinnerNumberModel(Node.getNameFontSize(),4.0,80.0,1.0));
	private JSpinner arrowFontSpinner = new JSpinner(new SpinnerNumberModel(Arrow.getNameFontSize(),4.0,80.0,1.0));
	
	//private JButton bApply = new JButton("button.app");
	//private JButton bCancel = new JButton("button.can");
	private JButton bClose = new JButton(UIMessages.getInstance().getMessage("button.clo"));
	
	public IconSizesDialog ( final PuckFrame parent , boolean modal )
	{
		
		super(parent,modal);
		this.setTitle(UIMessages.getInstance().getMessage("dialog.sizes"));
		
		roomSlider.setValue(RoomNode.getDefaultSize());
		itemSlider.setValue(ItemNode.getDefaultSize());
		charSlider.setValue(CharacterNode.getDefaultSize());
		spellSlider.setValue(SpellNode.getDefaultSize());
		absSlider.setValue(AbstractEntityNode.getDefaultSize());
		
		nodeFontSpinner.setValue(Node.getNameFontSize());
		arrowFontSpinner.setValue(Arrow.getNameFontSize());
		
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
		
		nodeFontSpinner.addChangeListener ( new ChangeListener() 
				{
					public void stateChanged(ChangeEvent e)
					{
						SpinnerNumberModel snm = (SpinnerNumberModel) nodeFontSpinner.getModel();
						String s = ""+snm.getNumber().floatValue();
						PuckConfiguration.getInstance().setProperty("graphNodeFontSize",s);
						parent.repaint();
					}
				}
		);
		
		arrowFontSpinner.addChangeListener ( new ChangeListener() 
		{
			public void stateChanged(ChangeEvent e)
			{
				SpinnerNumberModel snm = (SpinnerNumberModel) arrowFontSpinner.getModel();
				String s = ""+snm.getNumber().floatValue();
				PuckConfiguration.getInstance().setProperty("graphArrowFontSize",s);
				parent.repaint();
			}
		}
);
		
		
		this.getContentPane().setLayout ( new BorderLayout() );
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
		
		JPanel iconSizesPanel = new JPanel();
		
		iconSizesPanel.setLayout ( new GridLayout(5,2)  );
		
		iconSizesPanel.add(new JLabel(UIMessages.getInstance().getMessage("sizes.room")));
		iconSizesPanel.add(roomSlider);
		
		iconSizesPanel.add(new JLabel(UIMessages.getInstance().getMessage("sizes.item")));
		iconSizesPanel.add(itemSlider);
		
		iconSizesPanel.add(new JLabel(UIMessages.getInstance().getMessage("sizes.character")));
		iconSizesPanel.add(charSlider);
		
		iconSizesPanel.add(new JLabel(UIMessages.getInstance().getMessage("sizes.spell")));
		iconSizesPanel.add(spellSlider);
		
		iconSizesPanel.add(new JLabel(UIMessages.getInstance().getMessage("sizes.abstract")));
		iconSizesPanel.add(absSlider);
		
		iconSizesPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getInstance().getMessage("sizes.icons")));
		
		mainPanel.add(iconSizesPanel);
		
		JPanel fontSizesPanel = new JPanel();
		
		fontSizesPanel.setLayout( new GridLayout(2,2) );
		
		fontSizesPanel.add(new JLabel(UIMessages.getInstance().getMessage("sizes.font.node")));
		JPanel nodeFontSpinnerPanel = new JPanel();
		nodeFontSpinnerPanel.setLayout(new BoxLayout(nodeFontSpinnerPanel,BoxLayout.LINE_AXIS));
		nodeFontSpinnerPanel.add(Box.createHorizontalGlue());
		nodeFontSpinnerPanel.add(nodeFontSpinner);
		fontSizesPanel.add(nodeFontSpinnerPanel);
		
		fontSizesPanel.add(new JLabel(UIMessages.getInstance().getMessage("sizes.font.arrow")));
		JPanel arrowFontSpinnerPanel = new JPanel();
		arrowFontSpinnerPanel.setLayout(new BoxLayout(arrowFontSpinnerPanel,BoxLayout.LINE_AXIS));
		arrowFontSpinnerPanel.add(Box.createHorizontalGlue());
		arrowFontSpinnerPanel.add(arrowFontSpinner);
		fontSizesPanel.add(arrowFontSpinnerPanel);
		
		fontSizesPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getInstance().getMessage("sizes.fonts")));
		
		mainPanel.add(fontSizesPanel);
		
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
						saveAndDispose();
					}
				}
		);
		
		getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
		
		registerCloseAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		
		this.getRootPane().setDefaultButton(bClose);
		pack();
		setLocationRelativeTo(null);
		
		
	}
	
	private void saveAndDispose()
	{
		PuckConfiguration.getInstance().setProperty("roomDisplaySize",String.valueOf(roomSlider.getValue()));
		PuckConfiguration.getInstance().setProperty("itemDisplaySize",String.valueOf(itemSlider.getValue()));
		PuckConfiguration.getInstance().setProperty("spellDisplaySize",String.valueOf(spellSlider.getValue()));
		PuckConfiguration.getInstance().setProperty("abstractEntityDisplaySize",String.valueOf(absSlider.getValue()));
		PuckConfiguration.getInstance().setProperty("characterDisplaySize",String.valueOf(charSlider.getValue()));
		
		dispose();
	}
	
	private void registerCloseAction(KeyStroke keyStroke) 
	{
		ActionListener escListener = new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				IconSizesDialog.this.saveAndDispose();
			}
		};

		this.getRootPane().registerKeyboardAction(escListener,
				keyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	

}
