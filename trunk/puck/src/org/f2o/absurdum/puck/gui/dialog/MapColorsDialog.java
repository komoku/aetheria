package org.f2o.absurdum.puck.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.SpacingPanel;
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
	
	public Color getCurrentColor()
	{
		return gep.getColorSetting(colorSettingName);
	}
	
	public void showDialog ( MapColorsDialog parentDialog )
	{
		Color existingColor = getCurrentColor();
		Color c = JColorChooser.showDialog(parentDialog,UIMessages.getInstance().getMessage("choose.color"),existingColor);
		gep.setColorSetting(colorSettingName, c);
		gep.repaint();
	}
}

class MapColorChangePanel extends JPanel
{
	
	private String colorSettingName;
	private GraphEditingPanel gep;
	private MapColorsDialog parentDialog;
	
	private JPanel demonstrationPanel = new JPanel();
	
	public MapColorChangePanel ( MapColorsDialog parentDialog , GraphEditingPanel gep , String colorSettingName , String colorSettingLabel )
	{
		this.colorSettingName = colorSettingName;
		this.gep = gep;
		this.parentDialog = parentDialog;
		setLayout ( new GridLayout(1,3) );
		JLabel label = new JLabel(colorSettingLabel);
		add(new SpacingPanel(label));
		final MapColorHandler ch = new MapColorHandler( gep , colorSettingName );
		demonstrationPanel.setBackground(ch.getCurrentColor());
		demonstrationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(new SpacingPanel(demonstrationPanel));
		JButton button = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.change"));
		//button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		button.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent evt)
			{
				ch.showDialog(MapColorChangePanel.this.parentDialog);
				demonstrationPanel.setBackground(ch.getCurrentColor());
			}
		});
		demonstrationPanel.addMouseListener( new MouseAdapter() 
		{
			public void mouseClicked ( MouseEvent evt )
			{
				 if ( evt.getButton() == MouseEvent.BUTTON1 )
				 {
					 ch.showDialog(MapColorChangePanel.this.parentDialog);
					 demonstrationPanel.setBackground(ch.getCurrentColor());
				 }
			}
		});
		add(new SpacingPanel(button));
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
		
		/*
		JButton buttonBackground = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.background"));
		JButton buttonText = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.text"));
		JButton buttonAuxText = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.auxtext"));
		JButton buttonGrid = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.grid"));
		JButton buttonArrow = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.arrow"));
		JButton buttonHighArrow = new JButton(UIMessages.getInstance().getMessage("button.mapcolors.higharrow"));
		
		addColorChangeActionListener ( buttonBackground , "background" );
		addColorChangeActionListener ( buttonText , "text" );
		addColorChangeActionListener ( buttonGrid , "grid" );
		addColorChangeActionListener ( buttonArrow , "arrow" );
		addColorChangeActionListener ( buttonHighArrow , "highArrow" );
		addColorChangeActionListener ( buttonAuxText , "auxText" );
		*/
		
		this.getContentPane().setLayout ( new BorderLayout() );
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
		
		/*
		mainPanel.add(buttonBackground);
		mainPanel.add(buttonText);
		mainPanel.add(buttonAuxText);
		mainPanel.add(buttonGrid);
		mainPanel.add(buttonArrow);
		mainPanel.add(buttonHighArrow);
		*/
		
		GraphEditingPanel gep = parent.getGraphEditingPanel();
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(new MapColorChangePanel( this , gep , "background" , UIMessages.getInstance().getMessage("button.mapcolors.background") ));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(new MapColorChangePanel( this , gep , "text" , UIMessages.getInstance().getMessage("button.mapcolors.text") ));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(new MapColorChangePanel( this , gep , "auxText" , UIMessages.getInstance().getMessage("button.mapcolors.auxtext") ));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(new MapColorChangePanel( this , gep , "grid" , UIMessages.getInstance().getMessage("button.mapcolors.grid") ));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(new MapColorChangePanel( this , gep , "arrow" , UIMessages.getInstance().getMessage("button.mapcolors.arrow") ));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(new MapColorChangePanel( this , gep , "highArrow" , UIMessages.getInstance().getMessage("button.mapcolors.higharrow") ));
		mainPanel.add(Box.createVerticalStrut(10));
		
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
