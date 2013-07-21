package eu.irreality.age.swing.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
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
import eu.irreality.age.Utility;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.server.ServerHandler;
import eu.irreality.age.swing.FileSelectorDialogs;

public class EscuchadorCargarDesdeEstado implements ActionListener
{
	JDesktopPane thePanel;

	public EscuchadorCargarDesdeEstado ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		final String path = FileSelectorDialogs.showOpenStateDialog(thePanel);

		if( path != null ) 
		{
			
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			//String worldFile = moduledir + "/world.xml"; //TODO Doesn't have to be world.xml now!
			String worldFile = path;

			//test:
			GameInfo gi = GameInfo.getGameInfoFromFile ( worldFile );
			final PartidaEntry pe = new PartidaEntry ( gi , "noname" , 200 , null , true , true , true );

			Thread thr = new Thread() {
				public void run()
				{
					ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , path , null , thePanel );
				}
			};
			thr.start();

			//working:
			//new SwingAetheriaGameLoader( moduledir , thePanel , false , null , selector.getSelectedFile().getAbsolutePath(), true ); //not client is true? yep: don't assign players until second load, stateload (will change w/diff)

		}
	}

}