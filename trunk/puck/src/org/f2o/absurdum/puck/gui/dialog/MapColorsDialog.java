package org.f2o.absurdum.puck.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.panels.WorldPanel;
import org.f2o.absurdum.puck.i18n.UIMessages;

class MapColorHandler
{
	private String colorSettingName;
	private GraphEditingPanel gep;
	
	public MapColorHandler ( GraphEditingPanel gep , String colorSettingName )
	{
		this.colorSettingName = colorSettingName;
		this.gep = gep;
	}
	
	public void showDialog ( MapColorsDialog parentDialog )
	{
		Color existingColor = gep.getColorSetting(colorSettingName);
		Color c = JColorChooser.showDialog(parentDialog,UIMessages.getInstance().getMessage("choose.color"),existingColor);
		gep.setColorSetting(colorSettingName, c);
		gep.repaint();
	}
}

public class MapColorsDialog extends JDialog
{

	private JButton bClose = new JButton(UIMessages.getInstance().getMessage("button.clo"));
	private PuckFrame parent;
		
	private void addColorChangeActionListener ( JButton button , final String colorSettingName )
	{
		button.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent evt)
			{
				new MapColorHandler( parent.getGraphEditingPanel(), colorSettingName ).showDialog(MapColorsDialog.this);
			}
		});
	}
	
	public MapColorsDialog ( final PuckFrame parent , boolean modal )
	{
		super(parent,modal);
		this.parent = parent;
		this.setTitle(UIMessages.getInstance().getMessage("dialog.mapcolors"));
		
		JButton buttonBackground = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.background"));
		JButton buttonText = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.text"));
		JButton buttonAuxText = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.auxtext"));
		JButton buttonGrid = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.grid"));
		JButton buttonArrow = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.arrow"));
		JButton buttonHighArrow = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.higharrow"));
		
		addColorChangeActionListener ( buttonBackground , "background" );
		addColorChangeActionListener ( buttonGrid , "grid" );
		
		this.getContentPane().setLayout ( new BorderLayout() );
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
		
		mainPanel.add(buttonBackground);
		//mainPanel.add(buttonText);
		//mainPanel.add(buttonAuxText);
		mainPanel.add(buttonGrid);
		//mainPanel.add(buttonArrow);
		//mainPanel.add(buttonHighArrow);
		
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
						dispose();
					}
				}
		);
		
		this.getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
		
		registerCloseAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		
		this.getRootPane().setDefaultButton(bClose);
		pack();
		setLocationRelativeTo(null);
		
	}
	
	private void registerCloseAction(KeyStroke keyStroke) 
	{
		ActionListener escListener = new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				MapColorsDialog.this.dispose();
			}
		};

		this.getRootPane().registerKeyboardAction(escListener,
				keyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
}
