/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 26/10/2012 17:52:27
 */
package eu.irreality.age.windowing;

import java.awt.event.KeyEvent;

import javax.swing.JTabbedPane;

/**
 * @author carlos
 *
 */
public class TabUtils 
{
	
	public static void setDefaultMnemonics ( JTabbedPane jtp )
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
	}
	
}
