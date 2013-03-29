/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 12/11/2011 23:59:31
 */
package org.f2o.absurdum.puck.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import org.f2o.absurdum.puck.i18n.UIMessages;

/**
 * @author carlos
 *
 */
public class UILanguageSelectionMenu extends JMenu 
{
	
	private void showLanguageChangedDialog ( Frame frame )
	{
		JOptionPane.showMessageDialog(frame, UIMessages.getInstance().getMessage("language.changed"), UIMessages.getInstance().getMessage("language.changed.title"), JOptionPane.INFORMATION_MESSAGE );
	}

	public UILanguageSelectionMenu( final Frame frame )
	{
		super( UIMessages.getInstance().getMessage("language") );
		
		final String[] languageCodes = UIMessages.getInstance().getSupportedLanguages();
		JRadioButtonMenuItem[] languageMenuItems = new JRadioButtonMenuItem[languageCodes.length];
		
		for ( int i = 0 ; i < languageCodes.length ; i++ )
		{
			languageMenuItems[i] = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("language."+languageCodes[i]),false);
		}
		
		/*
		JRadioButtonMenuItem itemSpanish = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("language.es"),false);
		JRadioButtonMenuItem itemEnglish = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("language.en"),false);
		*/
		
		ButtonGroup bg = new ButtonGroup();
		
		/*
		bg.add ( itemSpanish );
		bg.add ( itemEnglish );
		*/
		
		for ( int i = 0 ; i < languageCodes.length ; i++ )
		{
			bg.add ( languageMenuItems[i] );
		}
		
		/*
		if ( UIMessages.getInstance().getPreferredLanguage().equals("es") )
			itemSpanish.setSelected(true);
		else
			itemEnglish.setSelected(true);
		*/
		
		for ( int i = 0 ; i < languageCodes.length ; i++ )
		{
			if ( UIMessages.getInstance().getPreferredLanguage().equals(languageCodes[i]) )
				languageMenuItems[i].setSelected(true);
		}
		
		/*
		itemSpanish.addActionListener( new ActionListener()
		{	
			public void actionPerformed ( ActionEvent evt )
			{
				if ( !UIMessages.getInstance().getPreferredLanguage().equals("es") )
				{
					UIMessages.getInstance().setPreferredLanguage("es");
					showLanguageChangedDialog ( frame );
				}
			}
		}
		);
		
		itemEnglish.addActionListener( new ActionListener()
		{	
			public void actionPerformed ( ActionEvent evt )
			{
				if ( !UIMessages.getInstance().getPreferredLanguage().equals("en") )
				{
					UIMessages.getInstance().setPreferredLanguage("en");
					showLanguageChangedDialog ( frame );
				}
			}
		}
		);
		*/
		
		for ( int i = 0 ; i < languageCodes.length ; i++ )
		{
			final int j = i;
			languageMenuItems[j].addActionListener( new ActionListener()
			{	
				public void actionPerformed ( ActionEvent evt )
				{
					if ( !UIMessages.getInstance().getPreferredLanguage().equals(languageCodes[j]) )
					{
						UIMessages.getInstance().setPreferredLanguage(languageCodes[j]);
						showLanguageChangedDialog ( frame );
					}
				}
			} );
		}
		
		for ( int i = 0 ; i < languageCodes.length ; i++ )
		{
			this.add(languageMenuItems[i]);
		}
		
		/*
		this.add( itemSpanish );
		this.add( itemEnglish );
		*/
		
	}
	
}
