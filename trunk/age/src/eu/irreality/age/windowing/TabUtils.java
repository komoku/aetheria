/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 26/10/2012 17:52:27
 */
package eu.irreality.age.windowing;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

/**
 * @author carlos
 *
 */
public class TabUtils 
{
	
	public static void setDefaultMnemonics ( final JTabbedPane jtp )
	{
		for ( int i = 0 ; i < jtp.getTabCount() ; i++ )
		{
			switch ( i )
			{
				case 0:
					jtp.setMnemonicAt(i, KeyEvent.VK_1); break;
				case 1:
					jtp.setMnemonicAt(i, KeyEvent.VK_2); break;
				case 2:
					jtp.setMnemonicAt(i, KeyEvent.VK_3); break;
				case 3:
					jtp.setMnemonicAt(i, KeyEvent.VK_4); break;
				case 4:
					jtp.setMnemonicAt(i, KeyEvent.VK_5); break;
				case 5:
					jtp.setMnemonicAt(i, KeyEvent.VK_6); break;
				case 6:
					jtp.setMnemonicAt(i, KeyEvent.VK_7); break;
				case 7:
					jtp.setMnemonicAt(i, KeyEvent.VK_8); break;
				case 8:
					jtp.setMnemonicAt(i, KeyEvent.VK_9); break;
			}
		}
		
		Action cycleLeft = new AbstractAction()
		{
			public void actionPerformed ( ActionEvent e )
			{
				int newIndex = jtp.getSelectedIndex() - 1;
				if ( newIndex < 0 )
					newIndex = jtp.getTabCount()-1;
				jtp.setSelectedIndex(newIndex);
			}
		}	;
		
		Action cycleRight = new AbstractAction()
		{
			public void actionPerformed ( ActionEvent e )
			{
				int newIndex = jtp.getSelectedIndex() + 1;
				if ( newIndex >= jtp.getTabCount() )
					newIndex = 0;
				jtp.setSelectedIndex(newIndex);
			}
		}	;
		
		InputMap inputMap = jtp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put( KeyStroke.getKeyStroke(KeyEvent.VK_LEFT , InputEvent.ALT_DOWN_MASK ) , "CycleLeft" );
		inputMap.put( KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP , InputEvent.CTRL_DOWN_MASK ) , "CycleLeft" );
		inputMap.put( KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT , InputEvent.ALT_DOWN_MASK ) , "CycleRight" );
		inputMap.put( KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN , InputEvent.CTRL_DOWN_MASK ) , "CycleRight" );
		ActionMap actionMap = jtp.getActionMap();
		actionMap.put("CycleLeft", cycleLeft);
		actionMap.put("CycleRight", cycleRight);
		
		
	}
	
}
