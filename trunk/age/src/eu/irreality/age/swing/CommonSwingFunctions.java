/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 29/12/2011 12:22:12
 */
package eu.irreality.age.swing;

import eu.irreality.age.GameEngineThread;
import eu.irreality.age.InputOutputClient;
import eu.irreality.age.ObjectCode;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.applet.SwingSDIApplet;
import eu.irreality.age.windowing.AGEClientWindow;

/**
 * @author carlos
 *
 */
public class CommonSwingFunctions 
{

	public static void writeIntroductoryInfo ( AGEClientWindow w )
	{
		w.write("Aetheria Game Engine v " + UIMessages.getInstance().getMessage("age.version") + "\n");
		
		w.write( UIMessages.getInstance().getMessage("age.copyright") + "\n" );
		w.write( UIMessages.getInstance().getMessage("intro.legal") + "\n" );

		w.write("\n=== === === === === === === === === === === === === === === ===");
		w.write("\n" + w.getIO().getColorCode("information") + "Engine-related Version Info:");
		w.write("\n" + w.getIO().getColorCode("information") + "[OS Layer]           " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + w.getIO().getColorCode("reset"));
		w.write("\n" + w.getIO().getColorCode("information") + "[Java Layer]         " + System.getProperty("java.version") + w.getIO().getColorCode("reset"));
		w.write("\n" + w.getIO().getColorCode("information") + "[Simulation Layer]   " + GameEngineThread.getVersion() + w.getIO().getColorCode("reset"));
		w.write("\n" + w.getIO().getColorCode("information") + "[Object Code Layer]  " + ObjectCode.getInterpreterVersion() + w.getIO().getColorCode("reset"));
		w.write("\n" + w.getIO().getColorCode("information") + "[UI Layer]           " + w.getVersion() + w.getIO().getColorCode("reset"));
		w.write("\n=== === === === === === === === === === === === === === === ===\n\n");

	}
	
}
