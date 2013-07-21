package eu.irreality.age.swing.sdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;

import eu.irreality.age.FiltroFicheroEstado;
import eu.irreality.age.GameInfo;
import eu.irreality.age.PartidaEntry;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.server.ServerHandler;
import eu.irreality.age.swing.FileSelectorDialogs;

public class LoadFromStateListener implements ActionListener
{
	SwingSDIInterface theWindow;

	public LoadFromStateListener ( SwingSDIInterface w )
	{
		theWindow = w;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		final String path = FileSelectorDialogs.showOpenStateDialog(theWindow);

		if( path != null ) 
		{

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			String worldFile = path; //world file = state file

			//test:
			//GameInfo gi = GameInfo.getGameInfoFromFile ( worldFile );
			//final PartidaEntry pe = new PartidaEntry ( gi , "noname" , 200 , null , true , true , true );

			theWindow.startGame( path , false , null , path );

			//working:
			//new SwingAetheriaGameLoader( moduledir , thePanel , false , null , selector.getSelectedFile().getAbsolutePath(), true ); //not client is true? yep: don't assign players until second load, stateload (will change w/diff)

		}
	}

}
