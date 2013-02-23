package org.f2o.absurdum.puck.gui.panels.code;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
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
		
	/**Property in PuckConfiguration where the option will be stored.*/
	private String configProperty;
		
	/**Object that defines how to actually toggle the option*/
	private RSyntaxOption toggler;
	
	/**Button model for check boxes*/
	private ButtonModel bModel;
	
	/**Map for instances from config properties*/
	private static Map instances = Collections.synchronizedMap ( new HashMap() );
	
	private RSyntaxOptionToggleAction ( String actionName , String configProperty , RSyntaxOption toggler ) 
	{
		this.toggler = toggler;
		this.configProperty = configProperty;
		putValue(NAME, actionName);
		loadConfig();
	}
	
	public static RSyntaxOptionToggleAction getInstanceFor ( String actionName , String configProperty )
	{
		RSyntaxOptionToggleAction instance;
		instance = (RSyntaxOptionToggleAction) instances.get(configProperty);
		if ( instance != null ) return instance;
		//no instance yet for this config property: create and register it
		instance = new RSyntaxOptionToggleAction ( actionName , configProperty , RSyntaxOption.getInstanceFor(configProperty) );
		instances.put(configProperty, instance);
		return instance;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		toggler.setOptionEnabled(!toggler.isOptionEnabled());
		
		//save the configuration so it will be kept for future sessions
		PuckConfiguration.getInstance().setProperty(configProperty, String.valueOf(toggler.isOptionEnabled()));
	}
	
	/**
	 * Loads the value of the option from PUCK configuration and sets it.
	 */
	public void loadConfig()
	{
		toggler.setOptionEnabled(PuckConfiguration.getInstance().getBooleanProperty(configProperty));
	}
	
	public JCheckBoxMenuItem getCheckBox ( )
	{
		if ( bModel == null )
		{
			JCheckBoxMenuItem prototypeCheckBox = new JCheckBoxMenuItem(this);
			prototypeCheckBox.setSelected(toggler.isOptionEnabled());
			bModel = prototypeCheckBox.getModel();
		}
		JCheckBoxMenuItem requested = new JCheckBoxMenuItem(this);
		requested.setModel(bModel);
		return requested;
	}

	
	
	

	
	
	
}
