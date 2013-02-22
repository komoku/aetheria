package org.f2o.absurdum.puck.gui.panels.code;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Action to toggle a configuration option in the RSyntaxTextAreas.
 * @author carlos
 *
 */
public class RSyntaxOptionToggleAction extends AbstractAction 
{
	
	/**The text areas affected by the option.*/
	private RSyntaxTextArea ta1, ta2;
	
	/**Property in PuckConfiguration where the option will be stored.*/
	private String configProperty;
	
	/**Checkbox for the first text area*/
	private JCheckBoxMenuItem cb1;
	
	/**Checkbox for the second text area*/
	private JCheckBoxMenuItem cb2;
	
	/**Object that defines how to actually toggle the option*/
	private RSyntaxOptionToggler toggler;
	
	public RSyntaxOptionToggleAction ( RSyntaxTextArea ta1 , RSyntaxTextArea ta2 , String actionName , String configProperty , RSyntaxOptionToggler toggler ) 
	{
		this.ta1 = ta1;
		this.ta2 = ta2;
		this.toggler = toggler;
		this.configProperty = configProperty;
		putValue(NAME, actionName);
		loadConfig();
		initCheckBoxes();
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		toggler.setOptionEnabled(ta1,ta2,!toggler.isOptionEnabled(ta1,ta2));
		
		//save the configuration so it will be kept for future sessions
		PuckConfiguration.getInstance().setProperty(configProperty, String.valueOf(toggler.isOptionEnabled(ta1,ta2)));
	}
	
	/**
	 * Loads the value of the option from PUCK configuration and sets it.
	 */
	public void loadConfig()
	{
		toggler.setOptionEnabled(ta1,ta2,PuckConfiguration.getInstance().getBooleanProperty(configProperty));
	}
	
	public void initCheckBoxes()
	{
		cb1 = new JCheckBoxMenuItem(this);
		cb2 = new JCheckBoxMenuItem(this);
		cb1.setSelected(toggler.isOptionEnabled(ta1,ta2));
		
		//link both checkboxes
		cb2.setModel(cb1.getModel());
		
		/*
		cb2.setSelected(isOptionEnabled());
		
		//link both checkboxes
		cb1.addItemListener( new ItemListener()
		{
			public void itemStateChanged ( ItemEvent e )
			{
				cb2.setSelected(cb1.isSelected());
			}
		}
		);
		
		cb2.addItemListener( new ItemListener()
		{
			public void itemStateChanged ( ItemEvent e )
			{
				cb1.setSelected(cb2.isSelected());
			}
		}
		);
		*/
	}
	
	public JCheckBoxMenuItem getCheckBoxFor ( RSyntaxTextArea ta )
	{
		if ( ta == ta1 ) return cb1;
		else if ( ta == ta2 ) return cb2;
		else return null;
	}
	

	
	
	
}
